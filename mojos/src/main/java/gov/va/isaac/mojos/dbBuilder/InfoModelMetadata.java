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

import gov.va.isaac.AppContext;
import gov.va.isaac.models.api.BdbInformationModelService;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.ihtsdo.otf.tcc.datastore.BdbPathManager;

/**
 * Goal which creates information model refset metadata in the ISAAC environment
 * 
 * @goal create-metadata
 * 
 * @phase process-sources
 */
public class InfoModelMetadata extends AbstractMojo {

  /** The data store. */
  private TerminologyStoreDI dataStore = null;

  /**
   * To execute this mojo, you need to first have run the "Setup" mojo against
   * the same database. Here, we assume the data store is ready to go and we can
   * acquire it simply as shown in the createPath method below.
   * 
   * If not yet initialized, this will fail.
   */
  @Override
  public void execute() throws MojoExecutionException {
    try {
      getLog().info("Creating metadata");
      BdbPathManager.get().resetPathMap();
      createMetadata();
      getLog().info("Done creating new path.");
    } catch (Exception e) {
      e.printStackTrace();
      throw new MojoExecutionException("Unexpected failure when creating path",
          e);
    }
  }

  /**
   * Creates the information model metadata.
   *
   * @throws Exception the exception
   */
  private void createMetadata() throws Exception {

    // Obtain already-open datastore and use it
    dataStore = AppContext.getService(TerminologyStoreDI.class);
    BdbInformationModelService service = new BdbInformationModelService(
        dataStore);
    service.createMetadataConcepts();
  }
}
