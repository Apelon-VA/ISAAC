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
package gov.va.isaac.mojos.dbBuilder;

import gov.va.isaac.classifier.Classifier;
import gov.va.isaac.classifier.SnomedSnorocketClassifier;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.otf.tcc.api.metadata.binding.Taxonomies;

/**
 * Mojo for running classifier.
 *
 * @goal run-classifier
 * 
 * @phase process-sources
 */
public class ClassifierMojo extends AbstractMojo {

  /** The data store. */
  //private TerminologyStoreDI dataStore = null;

  /**
   * Execute.
   *
   * @throws MojoExecutionException the mojo execution exception
   */
  @Override
  public void execute() throws MojoExecutionException {
    try {

      // Obtain already-open datastore
      //dataStore = AppContext.getService(TerminologyStoreDI.class);

      // Run classifier
      Classifier classifier = new SnomedSnorocketClassifier();
      classifier.setSaveCycleCheckReport(false);
      classifier.setSaveEquivalentConceptsReport(false);
      classifier.classify(Taxonomies.SNOMED.getNid());
      classifier.clearStaticState();
      
    } catch (Exception e) {
      throw new MojoExecutionException("Database build failure", e);
    }
  }

}
