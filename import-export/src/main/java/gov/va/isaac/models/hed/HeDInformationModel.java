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
import org.hl7.knowledgeartifact.r1.InlineResource;
import org.hl7.knowledgeartifact.r1.KnowledgeDocument.Expressions;
import org.hl7.knowledgeartifact.r1.KnowledgeDocument.ExternalData;
import org.hl7.knowledgeartifact.r1.Metadata.Applicability;
import org.hl7.knowledgeartifact.r1.Metadata.Categories;
import org.hl7.knowledgeartifact.r1.Metadata.Contributions;
import org.hl7.knowledgeartifact.r1.Metadata.DataModels;
import org.hl7.knowledgeartifact.r1.Metadata.EventHistory;
import org.hl7.knowledgeartifact.r1.Metadata.Identifiers;
import org.hl7.knowledgeartifact.r1.Metadata.Libraries;
import org.hl7.knowledgeartifact.r1.Metadata.Publishers;
import org.hl7.knowledgeartifact.r1.Metadata.RelatedResources;
import org.hl7.knowledgeartifact.r1.Metadata.TemplateIds;
import org.hl7.knowledgeartifact.r1.Metadata.UsageTerms;
import org.hl7.knowledgeartifact.r1.SupportingEvidence;
import org.hl7.knowledgeartifact.r1.Triggers;
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
   * Sets the identifiers.
   *
   * @return the identifiers
   * @throws JAXBException
   * @throws TransformerConfigurationException
   */
  public Identifiers getIdentifiers() throws JAXBException,
    TransformerConfigurationException {
    InformationModelProperty property = getPropertyByLabel(IDENTIFIERS);
    if (property == null)
      return null;
    return (Identifiers) new HeDXmlUtils().getGraphForString(
        property.getValue(), Identifiers.class);
  }

  /**
   * Sets the identifiers;
   *
   * @param identifiers
   * @throws JAXBException
   */
  public void setIdentifiers(Identifiers identifiers) throws JAXBException {
    // Remove any data model properties
    removePropertiesByLabel(IDENTIFIERS);

    InformationModelProperty property = new DefaultInformationModelProperty();
    property.setLabel(IDENTIFIERS);
    property.setValue(utils.getStringForGraph(identifiers));
    addProperty(property);
  }

  /**
   * Sets the template ids.
   *
   * @return the template ids
   * @throws JAXBException
   * @throws TransformerConfigurationException
   */
  public TemplateIds getTemplateIds() throws JAXBException,
    TransformerConfigurationException {
    InformationModelProperty property = getPropertyByLabel(TEMPLATE_IDS);
    if (property == null)
      return null;
    return (TemplateIds) new HeDXmlUtils().getGraphForString(
        property.getValue(), TemplateIds.class);
  }

  /**
   * Sets the template ids;
   *
   * @param templateIds
   * @throws JAXBException
   */
  public void setTemplateIds(TemplateIds templateIds) throws JAXBException {
    // Remove any data model properties
    removePropertiesByLabel(TEMPLATE_IDS);

    if (templateIds == null)
      return;

    InformationModelProperty property = new DefaultInformationModelProperty();
    property.setLabel(TEMPLATE_IDS);
    property.setValue(utils.getStringForGraph(templateIds));
    addProperty(property);
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
  public void setSchemaIdentifier(String schemaIdentifier) {
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
    if (property == null)
      return null;
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

    if (dataModels == null)
      return;

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
   * Sets the libraries.
   *
   * @return the libraries
   * @throws JAXBException
   * @throws TransformerConfigurationException
   */
  public Libraries getLibraries() throws JAXBException,
    TransformerConfigurationException {
    InformationModelProperty property = getPropertyByLabel(LIBRARIES);
    if (property == null)
      return null;
    return (Libraries) new HeDXmlUtils().getGraphForString(property.getValue(),
        Libraries.class);
  }

  /**
   * Sets the libraries;
   *
   * @param libraries
   * @throws JAXBException
   */
  public void setLibraries(Libraries libraries) throws JAXBException {
    // Remove any data model properties
    removePropertiesByLabel(LIBRARIES);

    if (libraries == null)
      return;

    InformationModelProperty property = new DefaultInformationModelProperty();
    property.setLabel(LIBRARIES);
    property.setValue(utils.getStringForGraph(libraries));
    addProperty(property);
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
    if (property == null)
      return null;
    return (EventHistory) utils.getGraphForString(property.getValue(),
        EventHistory.class);
  }

  /**
   * Sets the documentation.
   *
   * @return the documentation
   * @throws JAXBException
   * @throws TransformerConfigurationException
   */
  public InlineResource getDocumentation() throws JAXBException,
    TransformerConfigurationException {
    InformationModelProperty property = getPropertyByLabel(DOCUMENTATION);
    if (property == null)
      return null;
    return (InlineResource) new HeDXmlUtils().getGraphForString(
        property.getValue(), InlineResource.class);
  }

  /**
   * Sets the documentation;
   *
   * @param documentation
   * @throws JAXBException
   */
  public void setDocumentation(InlineResource documentation)
    throws JAXBException {
    // Remove any data model properties
    removePropertiesByLabel(DOCUMENTATION);

    if (documentation == null)
      return;

    InformationModelProperty property = new DefaultInformationModelProperty();
    property.setLabel(DOCUMENTATION);
    property.setValue(utils.getStringForGraph(documentation));
    addProperty(property);
  }

  /**
   * Sets the related resources.
   *
   * @return the related resources
   * @throws JAXBException
   * @throws TransformerConfigurationException
   */
  public RelatedResources getRelatedResources() throws JAXBException,
    TransformerConfigurationException {
    InformationModelProperty property = getPropertyByLabel(RELATED_RESOURCES);
    if (property == null)
      return null;
    return (RelatedResources) new HeDXmlUtils().getGraphForString(
        property.getValue(), RelatedResources.class);
  }

  /**
   * Sets the related resources;
   *
   * @param relatedResources
   * @throws JAXBException
   */
  public void setRelatedResources(RelatedResources relatedResources)
    throws JAXBException {
    // Remove any data model properties
    removePropertiesByLabel(RELATED_RESOURCES);

    if (relatedResources == null)
      return;

    InformationModelProperty property = new DefaultInformationModelProperty();
    property.setLabel(RELATED_RESOURCES);
    property.setValue(utils.getStringForGraph(relatedResources));
    addProperty(property);
  }

  /**
   * Sets the supporting evidence.
   *
   * @return the supporting evidence
   * @throws JAXBException
   * @throws TransformerConfigurationException
   */
  public SupportingEvidence getSupportingEvidence() throws JAXBException,
    TransformerConfigurationException {
    InformationModelProperty property = getPropertyByLabel(SUPPORTING_EVIDENCE);
    if (property == null)
      return null;
    return (SupportingEvidence) new HeDXmlUtils().getGraphForString(
        property.getValue(), SupportingEvidence.class);
  }

  /**
   * Sets the supporting evidence;
   *
   * @param supportingEvidence
   * @throws JAXBException
   */
  public void setSupportingEvidence(SupportingEvidence supportingEvidence)
    throws JAXBException {
    // Remove any data model properties
    removePropertiesByLabel(SUPPORTING_EVIDENCE);

    if (supportingEvidence == null)
      return;

    InformationModelProperty property = new DefaultInformationModelProperty();
    property.setLabel(SUPPORTING_EVIDENCE);
    property.setValue(utils.getStringForGraph(supportingEvidence));
    addProperty(property);
  }

  /**
   * Sets the applicability.
   *
   * @return the applicability
   * @throws JAXBException
   * @throws TransformerConfigurationException
   */
  public Applicability getApplicability() throws JAXBException,
    TransformerConfigurationException {
    InformationModelProperty property = getPropertyByLabel(APPLICABILITY);
    if (property == null)
      return null;
    return (Applicability) new HeDXmlUtils().getGraphForString(
        property.getValue(), Applicability.class);
  }

  /**
   * Sets the applicability;
   *
   * @param applicability
   * @throws JAXBException
   */
  public void setApplicability(Applicability applicability)
    throws JAXBException {
    // Remove any data model properties
    removePropertiesByLabel(APPLICABILITY);

    if (applicability == null)
      return;

    InformationModelProperty property = new DefaultInformationModelProperty();
    property.setLabel(APPLICABILITY);
    property.setValue(utils.getStringForGraph(applicability));
    addProperty(property);
  }

  /**
   * Sets the categories.
   *
   * @return the categories
   * @throws JAXBException
   * @throws TransformerConfigurationException
   */
  public Categories getCategories() throws JAXBException,
    TransformerConfigurationException {
    InformationModelProperty property = getPropertyByLabel(CATEGORIES);
    if (property == null)
      return null;
    return (Categories) new HeDXmlUtils().getGraphForString(
        property.getValue(), Categories.class);
  }

  /**
   * Sets the categories;
   *
   * @param categories
   * @throws JAXBException
   */
  public void setCategories(Categories categories) throws JAXBException {
    // Remove any data model properties
    removePropertiesByLabel(CATEGORIES);

    if (categories == null)
      return;

    InformationModelProperty property = new DefaultInformationModelProperty();
    property.setLabel(CATEGORIES);
    property.setValue(utils.getStringForGraph(categories));
    addProperty(property);
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

    if (eventHistory == null)
      return;

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
    if (property == null)
      return null;
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

    if (contributions == null)
      return;

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
    if (property == null)
      return null;
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

    if (publishers == null)
      return;

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
    if (property == null)
      return null;
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

    if (usageTerms == null)
      return;

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
    if (property == null)
      return null;
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

    if (externalData == null)
      return;

    InformationModelProperty property = new DefaultInformationModelProperty();
    property.setLabel(EXTERNAL_DATA);
    property.setValue(utils.getStringForGraph(externalData));
    addProperty(property);
  }

  /**
   * Sets the expressions.
   *
   * @return the expressions
   * @throws JAXBException
   */
  public Expressions getExpressions() throws JAXBException {
    InformationModelProperty property = getPropertyByLabel(EXPRESSIONS);
    if (property == null)
      return null;
    return (Expressions) utils.getGraphForString(property.getValue(),
        Expressions.class);
  }

  /**
   * Sets the expressions.
   * @param expressions
   * @throws JAXBException
   */
  public void setExpressions(Expressions expressions) throws JAXBException {
    // Remove any data model properties
    removePropertiesByLabel(EXPRESSIONS);

    if (expressions == null)
      return;

    InformationModelProperty property = new DefaultInformationModelProperty();
    property.setLabel(EXPRESSIONS);
    property.setValue(utils.getStringForGraph(expressions));
    addProperty(property);
  }

  /**
   * Sets the triggers.
   *
   * @return the triggers
   * @throws JAXBException
   */
  public Triggers getTriggers() throws JAXBException {
    InformationModelProperty property = getPropertyByLabel(TRIGGERS);
    if (property == null)
      return null;
    return (Triggers) utils.getGraphForString(property.getValue(),
        Triggers.class);
  }

  /**
   * Sets the triggers.
   * @param triggers
   * @throws JAXBException
   */
  public void setTriggers(Triggers triggers) throws JAXBException {
    // Remove any data model properties
    removePropertiesByLabel(TRIGGERS);

    if (triggers == null)
      return;

    InformationModelProperty property = new DefaultInformationModelProperty();
    property.setLabel(TRIGGERS);
    property.setValue(utils.getStringForGraph(triggers));
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
    if (property == null)
      return null;
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

    if (conditions == null)
      return;

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
    if (property == null)
      return null;
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

    if (actionGroup == null)
      return;

    InformationModelProperty property = new DefaultInformationModelProperty();
    property.setLabel(ACTION_GROUP);
    property.setValue(utils.getStringForGraph(actionGroup));
    addProperty(property);
  }

}
