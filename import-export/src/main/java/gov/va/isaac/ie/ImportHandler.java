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
package gov.va.isaac.ie;

import gov.va.isaac.models.InformationModel;

import java.io.File;
import java.io.InputStream;

/**
 * Interface for handling the ISAAC import functionality.
 *
 * @author ocarlsen
 * @author bcarlsenca
 */
public interface ImportHandler {

  /**
   * Method called by the ISAAC application to perform the import. Will be
   * invoked on a background thread.
   * @param file the input file
   * @return the information model
   * @throws Exception if anything goes wrong
   */
  public InformationModel importModel(File file) throws Exception;

  /**
   * Method called by the ISAAC application to perform the import. Will be
   * invoked on a background thread.
   * @param in the input stream
   * @return the information model
   * @throws Exception if anything goes wrong
   */
  public InformationModel importModel(InputStream in) throws Exception;
}
