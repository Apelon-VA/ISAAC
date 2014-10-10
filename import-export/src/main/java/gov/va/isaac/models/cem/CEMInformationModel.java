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
package gov.va.isaac.models.cem;

import gov.va.isaac.models.DefaultInformationModelProperty;
import gov.va.isaac.models.InformationModel;
import gov.va.isaac.models.InformationModelProperty;
import gov.va.isaac.models.util.DefaultInformationModel;

import java.util.HashSet;
import java.util.Set;

/**
 * An {@link InformationModel} for allowing applications to interact more
 * naturally with CEM models.
 *
 * @author ocarlsen
 * @author bcarlsenca
 */
public class CEMInformationModel extends DefaultInformationModel {

  /**
   * Instantiates an empty {@link CEMInformationModel}.
   */
  public CEMInformationModel() {
    // do nothing
  }

  /**
   * Instantiates an empty {@link CEMInformationModel} from the specified model;
   * @param model the model
   */
  public CEMInformationModel(InformationModel model) {
    super(model);
  }

  /**
   * Sets the data type.
   *
   * @param dataType the data type
   */
  public void setDataType(String dataType) {
    // Remove any "data" properties
    removePropertiesByLabel("data");

    // Add new one
    InformationModelProperty dataTypeProperty =
        new DefaultInformationModelProperty();
    dataTypeProperty.setLabel("data");
    dataTypeProperty.setType(dataType);
    addProperty(dataTypeProperty);
  }

  /**
   * Returns the definition
   *
   * @return the definition
   */
  public String getDefinition() {
    return getPropertyByLabel("definition").getType();
  }

  /**
   * Sets the definition
   *
   * @param definition the definition
   */
  public void setDefinition(String definition) {
    // Remove any "definition" properties
    removePropertiesByLabel("definition");

    // Add new one
    InformationModelProperty definitionProperty =
        new DefaultInformationModelProperty();
    definitionProperty.setLabel("definition");
    definitionProperty.setType(definition);
    addProperty(definitionProperty);
  }

  /**
   * Returns the data type.
   *
   * @return the data type
   */
  public String getDataType() {
    InformationModelProperty property = getPropertyByLabel("data");
    if (property == null)
      return null;
    return property.getType();
  }

  /**
   * Adds the component.
   *
   * @param component the component
   */
  private void addComponent(CEMComponent component) {
    InformationModelProperty property = new DefaultInformationModelProperty();
    property.setLabel(component.getComponentType().getCemlTagName());
    property.setType(component.getDateTypeRef());
    property.setName(component.getName());
    property.setValue(component.getValue() == null ? "" : component.getValue());
    property.setCardinalityMin(component.getCardinalityMin());
    property.setCardinalityMax(component.getCardinalityMax());
    addProperty(property);
  }

  /**
   * Removes the component.
   *
   * @param component the component
   */
  private void removeComponent(CEMComponent component) {
    InformationModelProperty property = new DefaultInformationModelProperty();
    property.setLabel(component.getComponentType().getCemlTagName());
    property.setType(component.getDateTypeRef());
    property.setName(component.getName());
    property.setValue(component.getValue() == null ? "" : component.getValue());
    property.setCardinalityMin(component.getCardinalityMin());
    property.setCardinalityMax(component.getCardinalityMax());
    removeProperty(property);
  }

  /**
   * Returns the components.
   *
   * @param type the type
   * @return the components
   */
  private Set<CEMComponent> getComponents(CEMComponentType type) {
    Set<CEMComponent> components = new HashSet<>();
    for (InformationModelProperty property : properties) {
      if (property.getLabel().equals(type.getCemlTagName())) {
        CEMComponent component = new CEMComponent();
        component.setComponentType(type);
        component.setDateTypeRef(property.getType());
        component.setName(property.getName());
        property.setValue(component.getValue() == null ? "" : component.getValue());
        component.setCardinalityMin(property.getCardinalityMin());
        component.setCardinalityMax(property.getCardinalityMax());
        components.add(component);
      }
    }
    return components;
  }

  /**
   * Adds the qualifier component.
   *
   * @param qual the qual
   */
  public void addQualifier(CEMComponent qual) {
    if (qual.getComponentType() != CEMComponentType.QUALIFIER) {
      throw new IllegalArgumentException("Unexpected component type "
          + qual.getCardinalityMax());
    }
    addComponent(qual);
  }

  /**
   * Removes the qualifier component.
   *
   * @param qual the qual
   */
  public void removeQualifier(CEMComponent qual) {
    if (qual.getComponentType() != CEMComponentType.QUALIFIER) {
      throw new IllegalArgumentException("Unexpected component type "
          + qual.getCardinalityMax());
    }
    removeComponent(qual);
  }

  /**
   * Returns the qualifiers.
   *
   * @return the qualifiers
   */
  public Set<CEMComponent> getQualifiers() {
    return getComponents(CEMComponentType.QUALIFIER);
  }

  /**
   * Adds the modifier component.
   *
   * @param mod the mod
   */
  public void addModifier(CEMComponent mod) {
    if (mod.getComponentType() != CEMComponentType.MODIFIER) {
      throw new IllegalArgumentException("Unexpected component type "
          + mod.getCardinalityMax());
    }
    addComponent(mod);
  }

  /**
   * Remove the modifier component.
   *
   * @param mod the mod
   */
  public void removeModifier(CEMComponent mod) {
    if (mod.getComponentType() != CEMComponentType.MODIFIER) {
      throw new IllegalArgumentException("Unexpected component type "
          + mod.getCardinalityMax());
    }
    removeComponent(mod);
  }

  /**
   * Returns the modifiers
   *
   * @return the modifiers
   */
  public Set<CEMComponent> getModifiers() {
    return getComponents(CEMComponentType.MODIFIER);
  }

  /**
   * Adds the attribution component.
   *
   * @param att the att
   */
  public void addAttribution(CEMComponent att) {
    if (att.getComponentType() != CEMComponentType.ATTRIBUTION) {
      throw new IllegalArgumentException("Unexpected component type "
          + att.getCardinalityMax());
    }
    addComponent(att);
  }

  /**
   * Remove the attribution component.
   *
   * @param att the att
   */
  public void removeAttribution(CEMComponent att) {
    if (att.getComponentType() != CEMComponentType.ATTRIBUTION) {
      throw new IllegalArgumentException("Unexpected component type "
          + att.getCardinalityMax());
    }
    removeComponent(att);
  }

  /**
   * Returns the attribution.
   *
   * @return the attributions
   */
  public Set<CEMComponent> getAttributions() {
    return getComponents(CEMComponentType.ATTRIBUTION);
  }

  /**
   * Adds the constraint.
   *
   * @param constraint the constraint
   */
  public void addConstraint(CEMConstraint constraint) {
    InformationModelProperty property = new DefaultInformationModelProperty();
    property.setLabel(CEMConstraint.getCemlTagName());
    property.setName(constraint.getPath());
    property.setValue(constraint.getValue());
    addProperty(property);
  }

  /**
   * Removes the constraint.
   *
   * @param constraint the constraint
   */
  public void removeConstraint(CEMConstraint constraint) {
    InformationModelProperty property = new DefaultInformationModelProperty();
    property.setLabel(CEMConstraint.getCemlTagName());
    property.setName(constraint.getPath());
    property.setValue(constraint.getValue());
    removeProperty(property);
  }

  /**
   * Returns the constraints.
   *
   * @return the constraints
   */
  public Set<CEMConstraint> getConstraints() {
    Set<CEMConstraint> constraints = new HashSet<>();
    for (InformationModelProperty property : properties) {
      if (property.getLabel().equals(CEMConstraint.getCemlTagName())) {
        CEMConstraint constraint = new CEMConstraint();
        constraint.setPath(property.getName());
        constraint.setValue(property.getValue());
        constraints.add(constraint);
      }
    }
    return constraints;
  }

  /**
   * Returns the constraints for component.
   *
   * @param component the component
   * @return the constraints for component
   */
  public Set<CEMConstraint> getConstraintsForComponent(CEMComponent component) {
    Set<CEMConstraint> constraints = new HashSet<>();
    for (InformationModelProperty property : properties) {
      // Check "constraint" properties
      if (property.getLabel().equals(CEMConstraint.getCemlTagName())) {

        // e.g. "qual.methodDevice"
        // e.g. "data.pq"
        if (property.getName().startsWith(
            component.getComponentType().getCemlTagName() + "."
                + component.getName() + ".")
            ||

            property.getName().startsWith(
                component.getComponentType().getCemlTagName() + "."
                    + component.getDateTypeRef() + ".")) {

          CEMConstraint constraint = new CEMConstraint();
          constraint.setPath(property.getName());
          constraint.setValue(property.getValue());
          constraints.add(constraint);
        }
      }
    }
    return constraints;

  }
}
