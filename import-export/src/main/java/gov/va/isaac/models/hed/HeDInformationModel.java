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

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerConfigurationException;

import org.hl7.knowledgeartifact.r1.ActionGroup;
import org.hl7.knowledgeartifact.r1.Conditions;
import org.hl7.knowledgeartifact.r1.KnowledgeDocument.ExternalData;
import org.hl7.knowledgeartifact.r1.Metadata.Contributions;
import org.hl7.knowledgeartifact.r1.Metadata.DataModels;
import org.hl7.knowledgeartifact.r1.Metadata.EventHistory;
import org.hl7.knowledgeartifact.r1.Metadata.Publishers;
import org.hl7.knowledgeartifact.r1.Metadata.UsageTerms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link InformationModel} for allowing applications to interact more
 * naturally with HeD models.
 *
 * @author bcarlsenca
 */
public class HeDInformationModel extends DefaultInformationModel implements
    HeDXmlConstants {

  /** The Constant LOG. */
  @SuppressWarnings("unused")
  private static final Logger LOG = LoggerFactory
      .getLogger(HeDInformationModel.class);

  /** The utils. */
  private HeDXmlUtils utils;

  /**
   * Instantiates an empty {@link HeDInformationModel}.
   * @throws TransformerConfigurationException
   */
  public HeDInformationModel() throws TransformerConfigurationException {
    utils = new HeDXmlUtils();
  }

  /**
   * Instantiates an empty {@link HeDInformationModel} from the specified model;
   * @param model the model
   * @throws TransformerConfigurationException
   */
  public HeDInformationModel(InformationModel model)
      throws TransformerConfigurationException {
    super(model);
    utils = new HeDXmlUtils();
  }

  /**
   * Sets the artifact type.
   *
   * @param artifactType the artifact type
   */
  public void setArtifactType(String artifactType) {
    // Remove any "data" properties
    removePropertiesByLabel(ARTIFACT_TYPE);

    // Add new one
    InformationModelProperty artifactTypeProperty =
        new DefaultInformationModelProperty();
    artifactTypeProperty.setLabel(ARTIFACT_TYPE);
    artifactTypeProperty.setType(artifactType);
    addProperty(artifactTypeProperty);
  }

  /**
   * Returns the artifact type.
   *
   * @return the artifact type
   */
  public String getArtifactType() {
    return getPropertyByLabel(ARTIFACT_TYPE).getType();
  }

  /**
   * Sets the schema idenfiier.
   *
   * @param schemaIdentifier the schema idenfiier
   */
  public void setschemaIdentifier(String schemaIdentifier) {
    // Remove any "data" properties
    removePropertiesByLabel(SCHEMA_IDENTIFIER);

    // Add new one
    InformationModelProperty schemaIdentifierProperty =
        new DefaultInformationModelProperty();
    schemaIdentifierProperty.setLabel(SCHEMA_IDENTIFIER);
    schemaIdentifierProperty.setType(schemaIdentifier);
    addProperty(schemaIdentifierProperty);
  }

  /**
   * Returns the schema idenfiier.
   *
   * @return the schema idenfiier
   */
  public String getschemaIdentifier() {
    return getPropertyByLabel(SCHEMA_IDENTIFIER).getType();
  }

  /**
   * Sets the data models.
   *
   * @return the data models
   * @throws JAXBException
   * @throws TransformerConfigurationException
   */
  public DataModels getDataModels() throws JAXBException,
    TransformerConfigurationException {
    InformationModelProperty property = getPropertyByLabel(DATA_MODELS);
    return (DataModels) new HeDXmlUtils().getGraphForString(
        property.getValue(), DataModels.class);
  }

  /**
   * Sets the data models.
   *
   * @param dataModels the data models
   * @throws JAXBException
   */
  public void setDataModels(DataModels dataModels) throws JAXBException {
    // Remove any data model properties
    removePropertiesByLabel(DATA_MODELS);

    InformationModelProperty property = new DefaultInformationModelProperty();
    property.setLabel(DATA_MODELS);
    property.setValue(utils.getStringForGraph(dataModels));
    addProperty(property);
  }

  /**
   * Sets the key terms.
   * @param keyTerms the key terms
   */
  public void setKeyTerms(List<String> keyTerms) {
    // Remove any key term properties
    removePropertiesByLabel(KEY_TERM);
    // Set new ones
    for (String keyTerm : keyTerms) {
      InformationModelProperty property = new DefaultInformationModelProperty();
      property.setLabel(KEY_TERM);
      property.setName(keyTerm);
      addProperty(property);
    }
  }

  /**
   * Returns the key terms.
   * @return the key terms
   */
  public List<String> getKeyTerms() {
    List<String> keyTerms = new ArrayList<>();
    for (InformationModelProperty property : getPropertiesByLabel(KEY_TERM)) {
      String keyTerm = property.getName();
      keyTerms.add(keyTerm);
    }
    return keyTerms;
  }

  /**
   * Sets the event history.
   *
   * @return the event history
   * @throws JAXBException
   */
  public EventHistory getEventHistory() throws JAXBException {
    InformationModelProperty property = getPropertyByLabel(EVENT_HISTORY);
    return (EventHistory) utils.getGraphForString(property.getValue(),
        EventHistory.class);
  }

  /**
   * Sets the event history.
   *
   * @param eventHistory the event history
   * @throws JAXBException
   */
  public void setEventHistory(EventHistory eventHistory) throws JAXBException {
    // Remove any data model properties
    removePropertiesByLabel(EVENT_HISTORY);
    InformationModelProperty property = new DefaultInformationModelProperty();
    property.setLabel(EVENT_HISTORY);
    property.setValue(utils.getStringForGraph(eventHistory));
    addProperty(property);
  }

  /**
   * Sets the contributions.
   *
   * @return the contributions
   * @throws JAXBException
   */
  public Contributions getContributions() throws JAXBException {
    InformationModelProperty property = getPropertyByLabel(CONTRIBUTIONS);
    return (Contributions) utils.getGraphForString(property.getValue(),
        Contributions.class);
  }

  /**
   * Sets the contributions.
   * @param contributions
   * @throws JAXBException
   */
  public void setContributions(Contributions contributions)
    throws JAXBException {
    // Remove any data model properties
    removePropertiesByLabel(CONTRIBUTIONS);
    InformationModelProperty property = new DefaultInformationModelProperty();
    property.setLabel(CONTRIBUTIONS);
    property.setValue(utils.getStringForGraph(contributions));
    addProperty(property);
  }

  /**
   * Sets the publishers.
   *
   * @return the publishers
   * @throws JAXBException
   */
  public Publishers getPublishers() throws JAXBException {
    InformationModelProperty property = getPropertyByLabel(PUBLISHERS);
    return (Publishers) utils.getGraphForString(property.getValue(),
        Publishers.class);
  }

  /**
   * Sets the publishers.
   * @param publishers
   * @throws JAXBException
   */
  public void setPublishers(Publishers publishers) throws JAXBException {
    // Remove any data model properties
    removePropertiesByLabel(PUBLISHERS);
    InformationModelProperty property = new DefaultInformationModelProperty();
    property.setLabel(PUBLISHERS);
    property.setValue(utils.getStringForGraph(publishers));
    addProperty(property);
  }

  /**
   * Sets the usage terms.
   *
   * @return the usage terms
   * @throws JAXBException
   */
  public UsageTerms getUsageTerms() throws JAXBException {
    InformationModelProperty property = getPropertyByLabel(USAGE_TERMS);
    return (UsageTerms) utils.getGraphForString(property.getValue(),
        UsageTerms.class);
  }

  /**
   * Sets the usage terms.
   *
   * @param usageTerms the usage terms
   * @throws JAXBException
   */
  public void setUsageTerms(UsageTerms usageTerms) throws JAXBException {
    // Remove any data model properties
    removePropertiesByLabel(USAGE_TERMS);
    InformationModelProperty property = new DefaultInformationModelProperty();
    property.setLabel(USAGE_TERMS);
    property.setValue(utils.getStringForGraph(usageTerms));
    addProperty(property);
  }

  /**
   * Sets the external data.
   *
   * @return the external data
   * @throws JAXBException
   */
  public ExternalData getExternalData() throws JAXBException {
    InformationModelProperty property = getPropertyByLabel(EXTERNAL_DATA);
    return (ExternalData) utils.getGraphForString(property.getValue(),
        ExternalData.class);
  }

  /**
   * Sets the external data.
   * @param externalData
   * @throws JAXBException
   */
  public void setExternalData(ExternalData externalData) throws JAXBException {
    // Remove any data model properties
    removePropertiesByLabel(EXTERNAL_DATA);
    InformationModelProperty property = new DefaultInformationModelProperty();
    property.setLabel(EXTERNAL_DATA);
    property.setValue(utils.getStringForGraph(externalData));
    addProperty(property);
  }

  /**
   * Sets the conditions.
   *
   * @return the conditions
   * @throws JAXBException
   */
  public Conditions getConditions() throws JAXBException {
    InformationModelProperty property = getPropertyByLabel(CONDITIONS);
    return (Conditions) utils.getGraphForString(property.getValue(),
        Conditions.class);
  }

  /**
   * Sets the conditions.
   * @param conditions
   * @throws JAXBException
   */
  public void setConditions(Conditions conditions) throws JAXBException {
    // Remove any data model properties
    removePropertiesByLabel(CONDITIONS);
    InformationModelProperty property = new DefaultInformationModelProperty();
    property.setLabel(CONDITIONS);
    property.setValue(utils.getStringForGraph(conditions));
    addProperty(property);
  }

  /**
   * Sets the action group.
   *
   * @return the action group
   * @throws JAXBException
   */
  public ActionGroup getActionGroup() throws JAXBException {
    InformationModelProperty property = getPropertyByLabel(ACTION_GROUP);
    return (ActionGroup) utils.getGraphForString(property.getValue(),
        ActionGroup.class);
  }

  /**
   * Sets the action group.
   * @param actionGroup
   * @throws JAXBException
   */
  public void setActionGroup(ActionGroup actionGroup) throws JAXBException {
    // Remove any data model properties
    removePropertiesByLabel(ACTION_GROUP);
    InformationModelProperty property = new DefaultInformationModelProperty();
    property.setLabel(ACTION_GROUP);
    property.setValue(utils.getStringForGraph(actionGroup));
    addProperty(property);
  }

}
