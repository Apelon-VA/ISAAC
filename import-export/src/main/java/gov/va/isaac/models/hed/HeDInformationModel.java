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

import gov.va.isaac.models.DefaultInformationModelProperty;
import gov.va.isaac.models.InformationModel;
import gov.va.isaac.models.InformationModelProperty;
import gov.va.isaac.models.util.DefaultInformationModel;

import java.util.ArrayList;
import java.util.List;

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

  /**
   * Sets the artifact type.
   *
   * @param artifactType the artifact type
   */
  public void setArtifactType(String artifactType) {
    // Remove any "data" properties
    removePropertiesByLabel("artifactType");

    // Add new one
    InformationModelProperty artifactTypeProperty =
        new DefaultInformationModelProperty();
    artifactTypeProperty.setLabel("artifactType");
    artifactTypeProperty.setType(artifactType);
    addProperty(artifactTypeProperty);
  }

  /**
   * Returns the artifact type.
   *
   * @return the artifact type
   */
  public String getArtifactType() {
    return getPropertyByLabel("artifactType").getType();
  }

  /**
   * Sets the data models.
   *
   * @param dataModels the data models
   */
  public void setDataModels(List<HeDModelReference> dataModels) {
    // Remove any data model properties
    removePropertiesByLabel("modelReference");

    // Set new ones
    for (HeDModelReference dataModel : dataModels) {
      InformationModelProperty property = new DefaultInformationModelProperty();
      property.setLabel("modelReference");
      property.setType(dataModel.getReferencedModel());
      property.setName(dataModel.getDescription());
      addProperty(property);
    }
  }

  /**
   * Returns the data models.
   *
   * @param component the component
   * @return the data models
   */
  public List<HeDModelReference> getDataModels() {
    List<HeDModelReference> dataModels = new ArrayList<>();
    for (InformationModelProperty property : getPropertiesByLabel("modelReference")) {
      HeDModelReference ref = new HeDModelReference();
      ref.setReferencedModel(property.getType());
      ref.setDescription(property.getName());
      dataModels.add(ref);
    }
    return dataModels;
  }
}
