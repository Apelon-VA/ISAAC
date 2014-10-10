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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.workflow.engine;

import gov.va.isaac.interfaces.workflow.ProcessInstanceCreationRequestI;
import gov.va.isaac.workflow.LocalTask;
import gov.va.isaac.workflow.exceptions.DatastoreException;
import java.util.ArrayList;
import javafx.util.Pair;

/**
 * {@link SynchronizeResult}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class SynchronizeResult
{
	private Exception unexpectedException_;
	private ArrayList<Pair<LocalTask, DatastoreException>> localTaskErrors_ = new ArrayList<>();
	private ArrayList<Pair<ProcessInstanceCreationRequestI, Exception>> creationRequestErrors_ = new ArrayList<>();
	
	private int actionsProcessed_, instancesProcessed_;
	private String summary_ = "Unexpected Error";
	
	protected SynchronizeResult()
	{
		
	}
	
	protected void unexpectedException(Exception e)
	{
		unexpectedException_ = e;
	}

	protected void addError(LocalTask loopTask, DatastoreException e)
	{
		localTaskErrors_.add(new Pair<>(loopTask, e));
		
	}

	protected void addError(ProcessInstanceCreationRequestI loopP, Exception e)
	{
		creationRequestErrors_.add(new Pair<>(loopP, e));
	}

	protected void setResults(int countActions, int countInstances, String fetchSummary)
	{
		actionsProcessed_ = countActions;
		instancesProcessed_ = countInstances;
		summary_ = fetchSummary;
	}
	
	public boolean hasError()
	{
		if (unexpectedException_ != null || localTaskErrors_.size() > 0 || creationRequestErrors_.size() > 0)
		{
			return true;
		}
		return false;
	}

	public Exception getUnexpectedException()
	{
		return unexpectedException_;
	}

	public ArrayList<Pair<LocalTask, DatastoreException>> getLocalTaskErrors()
	{
		return localTaskErrors_;
	}

	public ArrayList<Pair<ProcessInstanceCreationRequestI, Exception>> getCreationRequestErrors()
	{
		return creationRequestErrors_;
	}

	public int getActionsProcessed()
	{
		return actionsProcessed_;
	}

	public int getInstancesProcessed()
	{
		return instancesProcessed_;
	}

	public String getSummary()
	{
		return summary_;
	}

	/**
	 * Summarize the errors, if any - otherwise, returns an empty string.
	 */
	public String getErrorSummary()
	{
		StringBuilder sb = new StringBuilder();
		
		if (unexpectedException_ != null)
		{
			sb.append(unexpectedException_.toString());
			sb.append(", ");
		}
		for (Pair<LocalTask, DatastoreException> error : getLocalTaskErrors())
		{
			sb.append("Task failure on task ");
			sb.append(error.getKey().toString());
			sb.append(": ");
			sb.append(error.getValue().toString());
			sb.append(", ");
		}
		for (Pair<ProcessInstanceCreationRequestI, Exception> error : getCreationRequestErrors())
		{
			sb.append("Task failure on creation request ");
			sb.append(error.getKey().toString());
			sb.append(": ");
			sb.append(error.getValue().toString());
			sb.append(", ");
		}
		if (sb.length() >= 2)
		{
			sb.setLength(sb.length() - 2);
		}
		return sb.toString();
	}
}
