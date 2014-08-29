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
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.models.util;

import gov.va.isaac.models.api.BdbInformationModelService;
import gov.va.isaac.models.api.InformationModelService;

import java.io.IOException;

import org.slf4j.Logger;

/**
 * Base class containing common methods for exporting information models.
 *
 * @author ocarlsen
 * @author bcarlsenca
 */
public abstract class ExporterBase extends CommonBase {

  protected ExporterBase() {
    super();
  }

  protected abstract Logger getLogger();

  /**
   * Returns the information model service.
   *
   * @return the information model service
   * @throws IOException Signals that an I/O exception has occurred.
   */
  protected InformationModelService getInformationModelService()
    throws IOException {
    return new BdbInformationModelService(getDataStore());
  }

}
