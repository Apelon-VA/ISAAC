/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.workflow.engine;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.interfaces.utility.ServicesToPreloadI;
import gov.va.isaac.interfaces.workflow.ProcessInstanceCreationRequestI;
import gov.va.isaac.workflow.Action;
import gov.va.isaac.workflow.LocalTask;
import gov.va.isaac.workflow.LocalTasksServiceBI;
import gov.va.isaac.workflow.LocalWorkflowRuntimeEngineBI;
import gov.va.isaac.workflow.ProcessInstanceServiceBI;
import gov.va.isaac.workflow.TaskActionStatus;
import gov.va.isaac.workflow.exceptions.DatastoreException;
import gov.va.isaac.workflow.gui.WorkflowInbox;
import gov.va.isaac.workflow.sync.TasksFetcher;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jvnet.hk2.annotations.Service;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RemoteSynchronizer}
 *
 * @author alo
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Singleton
public class RemoteSynchronizer implements ServicesToPreloadI
{
	private static final Logger log = LoggerFactory.getLogger(RemoteSynchronizer.class);
	private SynchronizeResult mostRecentResult_ = null;
	private ArrayBlockingQueue<Consumer<SynchronizeResult>> queuedSyncRequests_ = new ArrayBlockingQueue<>(10);
	private volatile boolean run_ = true;
	private Thread runThread_;

	@Inject private ProcessInstanceServiceBI pis_;
	@Inject private LocalTasksServiceBI lts_;
	@Inject private RemoteWfEngine rwe_;
	@Inject private LocalWorkflowRuntimeEngineBI lwre_;

	//Used to put things in the queue, when no callback was requested.
	private Consumer<SynchronizeResult> noCallBackRequested = new Consumer<SynchronizeResult>()
	{
		@Override
		public void accept(SynchronizeResult t)
		{
			//noop
		}
	};

	private RemoteSynchronizer()
	{
		//for HK2
	}

	/**
	 * Request a remote synchronization. This call returns immediately, the call runs in a background thread.
	 * 
	 * @param callback - optional - pass in a callback function if you want notification of when the synchronize completes.
	 */
	public void synchronize(Consumer<SynchronizeResult> callback)
	{
		log.info("Queuing a sync request");
		queuedSyncRequests_.add(callback == null ? noCallBackRequested : callback);
	}

	/**
	 * Request a remote synchronization. This call blocks until the operation is complete,
	 * or the thread is interrupted.
	 * 
	 * @throws InterruptedException
	 */
	public SynchronizeResult blockingSynchronize() throws InterruptedException
	{
		log.info("Queuing a blocking sync request");
		final MutableObject<SynchronizeResult> result = new MutableObject<SynchronizeResult>();
		final CountDownLatch cdl = new CountDownLatch(1);
		Consumer<SynchronizeResult> callback = new Consumer<SynchronizeResult>()
		{
			@Override
			public void accept(SynchronizeResult t)
			{
				result.setValue(t);
				cdl.countDown();
			}
		};

		synchronize(callback);
		cdl.await();
		return result.getValue();
	}

	/**
	 * @see gov.va.isaac.interfaces.utility.ServicesToPreloadI#loadRequested()
	 */
	@Override
	public void loadRequested()
	{
		log.debug("load requested");
		runThread_ = new Thread(new SyncRunner(), "Workflow sync");
		runThread_.setDaemon(true);
		runThread_.start();
	}

	/**
	 * @see gov.va.isaac.interfaces.utility.ServicesToPreloadI#shutdown()
	 */
	@Override
	public void shutdown()
	{
		log.debug("shutdown called");
		run_ = false;
		queuedSyncRequests_.clear();
		runThread_.interrupt();
	}

	private class SyncRunner implements Runnable
	{
		@Override
		public void run()
		{
			log.debug("Background sync thread launches");
			while (run_)
			{
				Consumer<SynchronizeResult> callback = null;
				try
				{
					callback = queuedSyncRequests_.take();
					log.debug("Running synchronize");
					mostRecentResult_ = synchronizeWithRemote();
					callback.accept(mostRecentResult_);
					callback = null;
				}
				catch (InterruptedException e)
				{
					log.debug("SyncRunner was interrupted");
				}
				catch (Exception e)
				{
					log.error("Unexpected error", e);
					if (callback != null)
					{
						SynchronizeResult sr = new SynchronizeResult();
						sr.unexpectedException(new DatastoreException("Unexpected error running sync", e));
						callback.accept(sr);
					}
				}
			}
			log.debug("Background sync thread ends");
		}
	}

	private SynchronizeResult synchronizeWithRemote()
	{
		log.info("Performing remote sync");
		SynchronizeResult result = new SynchronizeResult();
		try
		{
			// Upload pending actions
			TaskService remoteService = rwe_.getRemoteTaskService();
			int countActions = 0;
			String userId = ExtendedAppContext.getCurrentlyLoggedInUserProfile().getWorkflowUsername();
			List<LocalTask> actions = lts_.getOwnedTasksByActionStatus(TaskActionStatus.Pending);
			for (LocalTask loopTask : actions)
			{
				try
				{
					Task remoteTask = remoteService.getTaskById(loopTask.getId());
					if (remoteTask != null)
					{
						remoteTask.getTaskData().getStatus();
						if (remoteTask.getTaskData().getStatus().equals(Status.Completed))
						{
							// too late, task not available
						}
						else if (remoteTask.getTaskData().getStatus().equals(Status.Reserved))
						{
							// start and action
							if (loopTask.getAction().equals(Action.COMPLETE))
							{
								remoteService.start(loopTask.getId(), userId);
								remoteService.complete(loopTask.getId(), userId, toObjectValueMap(loopTask.getOutputVariables()));
								lts_.setAction(loopTask.getId(), loopTask.getAction(), TaskActionStatus.Complete, loopTask.getOutputVariables());
							}
							else if (loopTask.getAction().equals(Action.RELEASE))
							{
								remoteService.release(loopTask.getId(), userId);
								lts_.setAction(loopTask.getId(), loopTask.getAction(), TaskActionStatus.Canceled, loopTask.getOutputVariables());
							}
						}
						else if (remoteTask.getTaskData().getStatus().equals(Status.InProgress))
						{
							// action
							if (loopTask.getAction().equals(Action.COMPLETE))
							{
								remoteService.complete(loopTask.getId(), userId, toObjectValueMap(loopTask.getOutputVariables()));
								lts_.setAction(loopTask.getId(), loopTask.getAction(), TaskActionStatus.Complete, loopTask.getOutputVariables());
							}
							else if (loopTask.getAction().equals(Action.RELEASE))
							{
								remoteService.release(loopTask.getId(), userId);
								lts_.setAction(loopTask.getId(), loopTask.getAction(), TaskActionStatus.Canceled, loopTask.getOutputVariables());
							}
						}
					}
				}
				catch (DatastoreException e)
				{
					result.addError(loopTask, e);
					log.error("Error during local task loop: " + loopTask.toString(), e);
				}
				countActions++;
			}

			// Upload pending requests
			int countInstances = 0;
			List<ProcessInstanceCreationRequestI> pendingRequests = pis_.getOpenOwnedRequests(userId);
			for (ProcessInstanceCreationRequestI loopP : pendingRequests)
			{
				try
				{
					lwre_.requestProcessInstanceCreationToServer(loopP);
					countInstances++;
				}
				catch (RemoteException | DatastoreException e)
				{
					result.addError(loopP, e);
					log.error("Error during pending requests loop: " + loopP.toString(), e);
				}
			}

			// Sync tasks
			TasksFetcher tf = new TasksFetcher();
			String fetchSummary = tf.fetchTasks(userId);

			log.info("Remote Sync finished " + (result.hasError() ? "with errors" : "successfully"));
			log.debug("   - Actions processed: {}", countActions);
			log.debug("   - Instances processed: {}", countInstances);
			log.debug("   - Fetch Summary: {}", fetchSummary);
			
			result.setResults(countActions, countInstances, fetchSummary);
			if (!result.hasError())
			{
				AppContext.getService(WorkflowInbox.class).reloadContent();
			}
		}
		catch (RemoteException | DatastoreException ex)
		{
			log.error("Error synchronizing", ex);
			result.unexpectedException(ex);
		}
		return result;
	}

	private HashMap<String, Object> toObjectValueMap(Map<String, String> sourceMap)
	{
		HashMap<String, Object> result = new HashMap<String, Object>();
		for (String loopSourceKey : sourceMap.keySet())
		{
			result.put(loopSourceKey, sourceMap.get(loopSourceKey));
		}
		return result;
	}

}
