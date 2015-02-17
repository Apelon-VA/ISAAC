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
package gov.va.isaac.drools.manager;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.config.profiles.UserProfile;
import gov.va.isaac.config.profiles.UserProfileManager;
import gov.va.isaac.drools.gui.DroolsValidationFailureView;
import gov.va.isaac.drools.helper.ResultsCollector;
import gov.va.isaac.drools.helper.ResultsItem;
import gov.va.isaac.drools.helper.templates.AbstractTemplate;
import gov.va.isaac.drools.helper.templates.DescriptionTemplate;
import gov.va.isaac.drools.testmodel.DrDescription;
import gov.va.isaac.interfaces.utility.CommitListenerI;
import gov.va.isaac.interfaces.utility.ServicesToPreloadI;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.SimpleBooleanProperty;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.store.TerminologyDI.CONCEPT_EVENT;
import org.ihtsdo.otf.tcc.model.cc.concept.ConceptChronicle;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * {@link DroolsCommitListener}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Singleton
@Named(value = "Drools PreCommit")
public class DroolsCommitListener implements CommitListenerI, VetoableChangeListener, ServicesToPreloadI
{
	private final Logger LOG = LoggerFactory.getLogger(this.getClass());
	private boolean enabled = false;
	
	@Inject private DroolsExecutorsManager dem_;

	private DroolsCommitListener()
	{
		// for HK2 to create
	}

	/**
	 * @see gov.va.isaac.interfaces.utility.ServicesToPreloadI#loadRequested()
	 */
	@Override
	public void loadRequested()
	{
		AppContext.getService(UserProfileManager.class).registerLoginCallback((user) -> {
			enable();
		});
	}

	/**
	 * @see gov.va.isaac.interfaces.utility.ServicesToPreloadI#shutdown()
	 */
	@Override
	public void shutdown()
	{
		// noop
	}

	/**
	 * @see gov.va.isaac.interfaces.utility.CommitListenerI#getListenerName()
	 */
	@Override
	public String getListenerName()
	{
		return "Drools PreCommit";
	}

	/**
	 * @see gov.va.isaac.interfaces.utility.CommitListenerI#enable()
	 */
	@Override
	public void enable()
	{
		UserProfile loggedIn = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
		if (!enabled && loggedIn != null && loggedIn.isRunDroolsBeforeEachCommit())
		{
			LOG.info("Enabling the drools pre-commit listener");
			ExtendedAppContext.getDataStore().addVetoablePropertyChangeListener(CONCEPT_EVENT.PRE_COMMIT, this);
			enabled = true;
		}

	}

	/**
	 * @see gov.va.isaac.interfaces.utility.CommitListenerI#disable()
	 */
	@Override
	public void disable()
	{
		if (enabled)
		{
			LOG.info("Disabling the workflow commit listener");
			//TODO file yet another OTF bug - this doesn't work.  We still get property change notifications after calling remove...
			//TODO this one is missing (removeVetoableChangeListener)
			//ExtendedAppContext.getDataStore().removePropertyChangeListener(this);
			enabled = false;
		}

	}

	
	/**
	 * @see java.beans.VetoableChangeListener#vetoableChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException
	{
		//TODO this shouldn't be necessary, but OTF has a bug....
		if (!enabled)
		{
			return;
		}
		try
		{
			SimpleBooleanProperty fail = new SimpleBooleanProperty(false);
			if (CONCEPT_EVENT.PRE_COMMIT.name().equals(evt.getPropertyName()))
			{
				LOG.debug("pre-commit triggered in drools listener");
				
				int[] allConceptNids = ((NativeIdSetBI) evt.getNewValue()).getSetValues();
				
				for (int nid : allConceptNids)
				{
					ConceptChronicleBI ccbi = ExtendedAppContext.getDataStore().getConceptForNid(nid);
					if (ccbi instanceof ConceptChronicle)
					{
						ConceptChronicle cc = (ConceptChronicle)ccbi;
						for (int componentNid : cc.getUncommittedNids().getListArray())
						{
							ComponentChronicleBI<?> comChronicleBI = ExtendedAppContext.getDataStore().getComponent(componentNid);
							
							if (comChronicleBI instanceof DescriptionChronicleBI)
							{
								for (String s : dem_.getLoadedExecutors())
								{
									
									LOG.debug("Running rule {}", s);
									DroolsExecutor de = dem_.getDroolsExecutor(s);

									DescriptionChronicleBI dc = (DescriptionChronicleBI)comChronicleBI;
									dc.getVersions().forEach((dcvi) ->
									{
										try
										{
											if (!dcvi.isUncommitted())
											{
												return;
											}
											ArrayList<Object> facts = new ArrayList<>();
											
											DrDescription d = new DrDescription();
											d.setLang(dcvi.getLang());
											d.setStatusUuid(dcvi.getStatus() == Status.ACTIVE ? SnomedMetadataRf2.ACTIVE_VALUE_RF2.getUuids()[0].toString() : 
												SnomedMetadataRf2.INACTIVE_VALUE_RF2.getUuids()[0].toString());
											d.setText(dcvi.getText());
											
											facts.add(d);
											
											Map<String, Object> globals = new HashMap<>();
											
											ResultsCollector rc = new ResultsCollector();
											
											globals.put("resultsCollector", rc);
											
											int fireCount = de.fireAllRules(globals, facts);
											
											LOG.debug("Fire count was " + fireCount);
											
											if (rc.getResultsItems().size() > 0)
											{
												fail.set(true);
												System.out.println("Failed test info:");
												for (ResultsItem r : rc.getResultsItems())
												{
													try
													{
														AppContext.getService(DroolsValidationFailureView.class).addFailure(r);
													}
													catch (Exception e)
													{
														LOG.error("Problem displaying error");
													}
													System.out.println("resultsItem: ");
													System.out.println(r.getMessage());
													System.out.println(r.getSeverity());
													System.out.println(r.getRuleUuid());
													System.out.println(r.getErrorCode());
												}
												
												for (AbstractTemplate  t :rc.getTemplates())
												{
													
													if (t instanceof DescriptionTemplate)
													{
														System.out.println("Suggested fix: '" + ((DescriptionTemplate)t).getText() + "'");
													}
													else
													{
														System.out.println(t.toString());
													}
												}
											}
										}
										catch (Exception e)
										{
											LOG.error("Unexpected error running pre commit drools rule", e);
										}
									});
								}
							}
						}
						
					}
					else
					{
						LOG.error("Unexpected chronicle type {}!", ccbi);
					}
				}
				
				if (fail.get())
				{
					throw new PropertyVetoException("Failed Drools Validators", evt);
				}
			}
		}
		catch (PropertyVetoException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			LOG.error("Unexpected error processing commit notification", e);
		}
	}

}
