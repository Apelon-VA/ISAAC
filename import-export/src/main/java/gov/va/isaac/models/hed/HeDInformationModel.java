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
package gov.va.isaac.models.hed;

import gov.va.isaac.models.InformationModel;
import gov.va.isaac.models.util.DefaultInformationModel;

/**
 * An {@link InformationModel} for allowing applications to interact more
 * naturally with HeD models.
 *
 * @author bcarlsenca
 */
public class HeDInformationModel extends DefaultInformationModel {

  /**
   * Instantiates an empty {@link HeDInformationModel}.
   */
  public HeDInformationModel() {
    // do nothing
  }

  /**
   * Instantiates an empty {@link HeDInformationModel} from the specified model;
   * @param model the model
   */
  public HeDInformationModel(InformationModel model) {
    super(model);
  }

}
