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
package gov.va.isaac.models.hed.importer;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.constants.InformationModels;
import gov.va.isaac.ie.ImportHandler;
import gov.va.isaac.model.InformationModelType;
import gov.va.isaac.models.InformationModel;
import gov.va.isaac.models.api.InformationModelService;
import gov.va.isaac.models.hed.HeDInformationModel;
import gov.va.isaac.models.hed.HeDXmlUtils;
import gov.va.isaac.models.util.ImporterBase;
import gov.va.isaac.util.WBUtility;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.hl7.cdsdt.r2.CD;
import org.hl7.knowledgeartifact.r1.ActionBase;
import org.hl7.knowledgeartifact.r1.ActionGroup;
import org.hl7.knowledgeartifact.r1.Actor;
import org.hl7.knowledgeartifact.r1.ClinicalRequest;
import org.hl7.knowledgeartifact.r1.Condition;
import org.hl7.knowledgeartifact.r1.Conditions;
import org.hl7.knowledgeartifact.r1.Expression;
import org.hl7.knowledgeartifact.r1.ExpressionDef;
import org.hl7.knowledgeartifact.r1.InlineResource;
import org.hl7.knowledgeartifact.r1.KnowledgeDocument;
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
import org.hl7.knowledgeartifact.r1.ValueSet;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.RefexDynamicUsageDescriptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * An {@link ImportHandler} for importing a HeD model from an XML file.
 * 
 * @author bcarlsenca
 */
public class HeDImporter extends ImporterBase implements ImportHandler {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(HeDImporter.class);

  /** The utils. */
  private HeDXmlUtils utils;

  /**
   * Instantiates an empty {@link HeDImporter}.
   * @throws TransformerConfigurationException
   */
  public HeDImporter() throws TransformerConfigurationException {
    super();
    utils = new HeDXmlUtils();
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.ie.ImportHandler#importModel(java.io.File)
   */
  @Override
  public InformationModel importModel(File file) throws Exception {
    LOG.info("Preparing to import CEM model from: " + file.getName());
    LOG.info("Ending import of CEM model from: " + file.getName());
    KnowledgeDocument document =
        (KnowledgeDocument) utils
            .getGraphForFile(file, KnowledgeDocument.class);
    // Parse into HeD model.
    HeDInformationModel infoModel = createInformationModel(document);

    // Save the information model
    // if (service.exists(infoModel)) {
    // throw new IOException("Model already imported.");
    // }
    InformationModelService service = getInformationModelService();
    service.saveInformationModel(infoModel);
    LOG.info("Ending import of HeD model from stream");
    return infoModel;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.ie.ImportHandler#importModel(java.io.InputStream)
   */
  @Override
  public InformationModel importModel(InputStream in) throws Exception {
    KnowledgeDocument document =
        (KnowledgeDocument) utils
            .getGraphForStream(in, KnowledgeDocument.class);

    // Parse into HeD model.
    HeDInformationModel infoModel = createInformationModel(document);

    // Save the information model
    // if (service.exists(infoModel)) {
    // throw new IOException("Model already imported.");
    // }
    InformationModelService service = getInformationModelService();
    service.saveInformationModel(infoModel);
    LOG.info("Ending import of HeD model from stream");
    return infoModel;

  }

  /**
   * Creates the information model from the XML DOM {@link Node}.
   *
   * @param rootNode the root node
   * @return the HeD information model
   * @throws IOException
   * @throws JAXBException
   * @throws TransformerConfigurationException
   * @throws UnsupportedEncodingException
   * @throws ParserConfigurationException
   * @throws TransformerException
   */
  private HeDInformationModel createInformationModel(KnowledgeDocument kd)
    throws JAXBException, TransformerConfigurationException,
    UnsupportedEncodingException {

    // Create the model
    HeDInformationModel infoModel = new HeDInformationModel();
    infoModel.setType(InformationModelType.HeD);

    String key =
        kd.getMetadata().getIdentifiers().getIdentifier().get(0).getRoot();
    LOG.debug("      key = " + key);
    // Key may contain things indexer can't handle, URL encode
    infoModel.setKey(URLEncoder.encode(key, StandardCharsets.UTF_8.name()));

    Identifiers identifiers = kd.getMetadata().getIdentifiers();
    LOG.debug("      identifiers = " + identifiers);
    infoModel.setIdentifiers(identifiers);

    String name = kd.getMetadata().getTitle().getValue();
    LOG.debug("      name = " + name);
    infoModel.setName(name);

    String artifactType =
        kd.getMetadata().getArtifactType().getValue().toString();
    LOG.debug("      artifactType = " + artifactType);
    infoModel.setArtifactType(artifactType);

    String schemaIdentifier = kd.getMetadata().getSchemaIdentifier().getRoot();
    LOG.debug("      schemaIdentifier = " + schemaIdentifier);
    infoModel.setSchemaIdentifier(schemaIdentifier);

    TemplateIds templateIds = kd.getMetadata().getTemplateIds();
    LOG.debug("      templateIds = " + templateIds);
    infoModel.setTemplateIds(templateIds);

    DataModels dataModels = kd.getMetadata().getDataModels();
    LOG.debug("      dataModels.ct = " + dataModels.getModelReference().size());
    infoModel.setDataModels(dataModels);

    Libraries libraries = kd.getMetadata().getLibraries();
    LOG.debug("      libraries = " + libraries);
    infoModel.setLibraries(libraries);

    if (kd.getMetadata().getDescription() != null) {
      String description = kd.getMetadata().getDescription().getValue();
      LOG.debug("      description = " + description);
      infoModel.setDescription(description);
    }

    List<String> keyTerms = new ArrayList<>();
    if (kd.getMetadata().getKeyTerms() != null) {
      for (CD keyTerm : kd.getMetadata().getKeyTerms().getTerm()) {
        keyTerms.add(keyTerm.getOriginalText().getValue());
      }
      LOG.debug("      keyTerms.ct = " + keyTerms.size());
      if (keyTerms.size() > 0) {
        infoModel.setKeyTerms(keyTerms);
      }
    }

    String status = kd.getMetadata().getStatus().getValue().toString();
    LOG.debug("      status = " + status);
    infoModel.setStatus(status);

    InlineResource documentation = kd.getMetadata().getDocumentation();
    LOG.debug("      documentation = " + documentation);
    infoModel.setDocumentation(documentation);

    RelatedResources relatedResources = kd.getMetadata().getRelatedResources();
    LOG.debug("      relatedResources = " + relatedResources);
    infoModel.setRelatedResources(relatedResources);

    SupportingEvidence supportingEvidence =
        kd.getMetadata().getSupportingEvidence();
    LOG.debug("      supportingEvidence = " + supportingEvidence);
    infoModel.setSupportingEvidence(supportingEvidence);

    Applicability applicability = kd.getMetadata().getApplicability();
    LOG.debug("      applicability = " + applicability);
    infoModel.setApplicability(applicability);

    Categories categories = kd.getMetadata().getCategories();
    LOG.debug("      categories = " + categories);
    infoModel.setCategories(categories);

    EventHistory eventHistory = kd.getMetadata().getEventHistory();
    LOG.debug("      event history = " + eventHistory);
    infoModel.setEventHistory(eventHistory);

    Contributions contributions = kd.getMetadata().getContributions();
    LOG.debug("      contributions = " + contributions);
    infoModel.setContributions(contributions);

    Publishers publishers = kd.getMetadata().getPublishers();
    LOG.debug("      publishers = " + publishers);
    infoModel.setPublishers(publishers);

    UsageTerms usageTerms = kd.getMetadata().getUsageTerms();
    LOG.debug("      usageTerms = " + usageTerms);
    infoModel.setUsageTerms(usageTerms);

    // Content

    ExternalData externalData = kd.getExternalData();
    LOG.debug("      externalData = " + externalData);
    infoModel.setExternalData(externalData);

    Conditions conditions = kd.getConditions();
    LOG.debug("      conditions = " + conditions);
    infoModel.setConditions(conditions);

    ActionGroup actionGroup = kd.getActionGroup();
    LOG.debug("      actionGroup = " + actionGroup);
    infoModel.setActionGroup(actionGroup);

    handleEnumerations(externalData);
    handleEnumerations(conditions);
    handleEnumerations(actionGroup);
    
    return infoModel;
  }

	private void handleEnumerations(ActionGroup actionGroup) {
		if (actionGroup != null) {
			for (ActionBase subElement : actionGroup.getSubElements().getSimpleActionOrActionGroupOrActionRef()) {
				handleEnumerations((subElement.getConditions()));
				
				if (subElement.getActors() != null) {
					for (Actor actor : subElement.getActors().getActor()) {
						handleEnumerations(actor.getActor());
					}
				}
				
				if (subElement instanceof ActionGroup) {
					handleEnumerations((ActionGroup)subElement);
				}
			}
		}		
	}
	
	private void handleEnumerations(Conditions conditions) {
		if (conditions != null) {
			for (Condition condition : conditions.getCondition()) {
				handleEnumerations(condition.getLogic());
			}
		}		
	}
	
	private void handleEnumerations(ExternalData externalData) {
		if (externalData != null && externalData.getDef() != null) {
			for (ExpressionDef expDef : externalData.getDef()) {
				handleEnumerations(expDef.getExpression());
			}
		}		
	}

	private void handleEnumerations(Expression exp) {
		if (exp != null) {
			if (exp instanceof ClinicalRequest) {
				if (((ClinicalRequest)exp).getCodes() != null && 
					((ClinicalRequest)exp).getCodes() instanceof ValueSet) {
					// Have Value Set
					ValueSet vs = (ValueSet)((ClinicalRequest)exp).getCodes();
					
					// Find Values
					String auth = vs.getAuthority();
					String id = vs.getId();
					String vers = vs.getVersion();

					// Create Name
					String value = auth + "'s " + id + " " + " v" + vers + " HeD Referenced Enumeration";
			
					// Only create if doesn't already exist
					if (!ExtendedAppContext.getDataStore().hasUuid(WBUtility.getUuidForFsn(value, value))) {
						// Create Refex's Description
						StringBuilder enumerationDesc = new StringBuilder();
						enumerationDesc.append("Enumeration Sememe for " + auth + "'s " + id);
						if (vs.getDescription() != null) {
							enumerationDesc.append("\r\n" + vs.getDescription());
						}
						enumerationDesc.append("\r\n");					
						enumerationDesc.append("Based on version: " + vers);	

						// create Refex
						try {
							// Create Enumeration
							AppContext.getRuntimeGlobals().disableAllCommitListeners();
							RefexDynamicUsageDescriptionBuilder.createNewRefexDynamicUsageDescriptionConcept(value, value, enumerationDesc.toString(), 
																											 new RefexDynamicColumnInfo[] {},
																											 InformationModels.HED_ENUMERATIONS.getUuids()[0], 
																											 false);
						} catch (IOException | ContradictionException | InvalidCAB
								| PropertyVetoException e) {
							LOG.error("Unable to create HED Enumeration for " + value);
						}
					}

				}
			}
			
		}
	}
}