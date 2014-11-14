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
package gov.va.isaac.classifier.utils;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.classifier.Classifier;
import gov.va.isaac.classifier.SnomedSnorocketClassifier;
import gov.va.isaac.config.profiles.UserProfile;
import gov.va.isaac.config.profiles.UserProfileManager;
import gov.va.isaac.interfaces.utility.CommitListenerI;
import gov.va.isaac.interfaces.utility.ServicesToPreloadI;
import gov.va.isaac.util.WBUtility;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.UUID;

import javafx.application.Platform;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.store.TerminologyDI.CONCEPT_EVENT;
import org.ihtsdo.otf.tcc.model.cc.concept.ConceptChronicle;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Commit listener for incremental classification.
 * @author bcarlsenca
 */
@SuppressWarnings("restriction")
@Service
@Singleton
@Named(value = "Classifier")
public class ClassifierCommitListener implements PropertyChangeListener,
    ServicesToPreloadI, CommitListenerI {

  /** The log. */
  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  /** The enabled flag. */
  private boolean enabled = false;

  /**
   * Instantiates an empty {@link ClassifierCommitListener}.
   */
  private ClassifierCommitListener() {
    // for HK2 to create
  }

  /**
   * Returns the listener name.
   *
   * @return the listener name
   * @see gov.va.isaac.interfaces.utility.CommitListenerI#getListenerName()
   */
  @Override
  public String getListenerName() {
    return "Classifier";
  }

  /**
   * Indicates whether or not enabled is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Disable.
   *
   * @see gov.va.isaac.interfaces.utility.CommitListenerI#disable()
   */
  @Override
  public void disable() {
    if (enabled) {
      LOG.info("Disabling the classifier commit listener");
      // TODO file yet another OTF bug - this doesn't work. We still get
      // property change notifications after calling remove...
      ExtendedAppContext.getDataStore().removePropertyChangeListener(this);
      enabled = false;
    }
  }

  /**
   * Enable.
   *
   * @see gov.va.isaac.interfaces.utility.CommitListenerI#enable()
   */
  @Override
  public void enable() {
    UserProfile loggedIn = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
    if (!enabled && loggedIn != null
        && loggedIn.isLaunchWorkflowForEachCommit()) {
      LOG.info("Enabling the classifier commit listener");
      ExtendedAppContext.getDataStore().addPropertyChangeListener(
          CONCEPT_EVENT.POST_COMMIT, this);
      enabled = true;
    }
  }

  /**
   * Property change.
   *
   * @param evt the evt
   */
  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    // TODO this shouldn't be necessary, but OTF has a bug....
    if (!enabled) {
      return;
    }
    try {
      if (CONCEPT_EVENT.POST_COMMIT.name().equals(evt.getPropertyName())) {
        LOG.debug("post-commit triggered in classifier commit listener");
        final int[] allConceptNids =
            ((NativeIdSetBI) evt.getNewValue()).getSetValues();

        //
        Platform.runLater(() -> {
          try {
            Classifier classifier = new SnomedSnorocketClassifier();

            // Identify if any components have been retired
            // if so, clear the classifier state and send user a warning
            LOG.debug(" Check for retirements");
            if (includesRetirements(allConceptNids)) {
              LOG.debug("   retirements = true");
              classifier.clearStaticState();
              throw new Exception("Commit included retirements, you must perform full classification again.");
            }
            
            LOG.debug(" Incremental classify");
            // classifier.incrementalClassify((IntSet)evt.getNewValue());
          } catch (Exception e) {
            e.printStackTrace();
            AppContext.getCommonDialogs().showErrorDialog(e.getMessage(), e);
          }

        });
      }
    } catch (Exception e) {
      LOG.error("Unexpected error processing commit notification", e);
    }
  }

  /**
   * Includes retirements.
   *
   * @param allConceptNids the all concept nids
   * @return true, if successful
   * @throws Exception the exception
   */
  private boolean includesRetirements(int[] allConceptNids) throws Exception {
    // Iterate through
    for (int i = 0; i < allConceptNids.length; i++) {
      // get concepts
      ConceptChronicle c =
          (ConceptChronicle) WBUtility.getConceptVersion(allConceptNids[i])
              .getChronicle();
      // get components
      int componentNid = 0;
      for (int nid : c.getUncommittedNids().getListArray()) {
        if (componentNid == 0) {
          componentNid = nid;
        } else {
          componentNid = allConceptNids[i];
          break;
        }
      }

      try {
        UUID componentId =
            WBUtility.getComponentChronicle(componentNid).getPrimordialUuid();
        WBUtility.getComponentVersion(componentId).getConceptNid();
      } catch (NullPointerException npe) {

        // retired component?
        return true;
      }
    }

    return false;
  }

  /**
   * Load requested.
   *
   * @see gov.va.isaac.interfaces.utility.ServicesToPreloadI#loadRequested()
   */
  @Override
  public void loadRequested() {
    AppContext.getService(UserProfileManager.class).registerLoginCallback(
        (user) -> {
          enable();
        });
  }

  /**
   * Shutdown.
   *
   * @see gov.va.isaac.interfaces.utility.ServicesToPreloadI#shutdown()
   */
  @Override
  public void shutdown() {
    // noop
  }
}
