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
package gov.va.isaac.models.hed.exporter;

import gov.va.isaac.models.InformationModel;
import gov.va.isaac.models.api.InformationModelService;
import gov.va.isaac.models.hed.HeDInformationModel;
import gov.va.isaac.models.hed.HeDXmlConstants;
import gov.va.isaac.models.hed.HeDXmlUtils;
import gov.va.isaac.models.util.ExporterBase;

import java.io.File;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerConfigurationException;

import org.hl7.cdsdt.r2.CD;
import org.hl7.cdsdt.r2.ST;
import org.hl7.knowledgeartifact.r1.ArtifactStatusType;
import org.hl7.knowledgeartifact.r1.ArtifactType;
import org.hl7.knowledgeartifact.r1.KnowledgeDocument;
import org.hl7.knowledgeartifact.r1.Metadata;
import org.hl7.knowledgeartifact.r1.Metadata.Identifiers;
import org.hl7.knowledgeartifact.r1.Metadata.KeyTerms;
import org.hl7.knowledgeartifact.r1.Metadata.Status;
import org.hl7.knowledgeartifact.r1.VersionedIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HAndler for exporting an HeD model to an XML {@link File}.
 *
 * @author bcarlsenca
 */
public class HeDExporter extends ExporterBase implements HeDXmlConstants {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(HeDExporter.class);

  /** The output stream. */
  private final OutputStream outputStream;

  /** The a. */
  private HeDXmlUtils utils;

  /**
   * Instantiates a {@link HeDExporter} from the specified parameters.
   *
   * @param outputStream the output stream
   * @throws TransformerConfigurationException
   */
  public HeDExporter(OutputStream outputStream)
      throws TransformerConfigurationException {
    super();
    this.outputStream = outputStream;
    utils = new HeDXmlUtils();
  }

  /**
   * Export model.
   *
   * @param uuid the concept uuid
   * @throws Exception the exception
   */
  public void exportModel(UUID uuid) throws Exception {
    LOG.info("Starting export of HeD model");
    LOG.debug("  UUID = " + uuid);

    InformationModelService service = getInformationModelService();

    // Parse into HeD model.
    InformationModel model = service.getInformationModel(uuid);
    // Abort if not available.
    if (model == null) {
      LOG.warn("No HeD model to export on " + uuid);
      return;
    }
    HeDInformationModel infoModel = new HeDInformationModel(model);

    // Build a DOM tree in the style of HeD.
    KnowledgeDocument kd = buildKnowledgeDocument(infoModel);
    outputStream.write(utils.prettyFormat(utils.getStringForGraph(kd), 2)
        .getBytes(StandardCharsets.UTF_8));
    outputStream.flush();

    LOG.info("Ending export of HeD model");
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.models.util.ExporterBase#getLogger()
   */
  @Override
  protected Logger getLogger() {
    return LOG;
  }

  /**
   * Builds the knowledge document to convert to XML.
   *
   * @param infoModel the info model
   * @return the knowledge document
   * @throws JAXBException
   * @throws TransformerConfigurationException
   */
  private KnowledgeDocument buildKnowledgeDocument(HeDInformationModel infoModel)
    throws JAXBException, TransformerConfigurationException {
    KnowledgeDocument kd = new KnowledgeDocument();

    // Build Metadata
    Metadata metadata = buildMetadata(infoModel);
    kd.setMetadata(metadata);

    if (infoModel.getExternalData() != null)
      kd.setExternalData(infoModel.getExternalData());

    if (infoModel.getExpressions() != null)
      kd.setExpressions(infoModel.getExpressions());

    if (infoModel.getExpressions() != null)
      kd.setExpressions(infoModel.getExpressions());

    if (infoModel.getConditions() != null)
      kd.setConditions(infoModel.getConditions());

    if (infoModel.getActionGroup() != null)
      kd.setActionGroup(infoModel.getActionGroup());

    return kd;
  }

  /**
   * Builds the metadata.
   *
   * @param infoModel the info model
   * @return the metadata
   * @throws JAXBException
   * @throws TransformerConfigurationException
   */
  private Metadata buildMetadata(HeDInformationModel infoModel)
    throws JAXBException, TransformerConfigurationException {
    Metadata metadata = new Metadata();

    // key
    Identifiers ids = infoModel.getIdentifiers();
    metadata.setIdentifiers(ids);

    // artifact type
    Metadata.ArtifactType artifactType = new Metadata.ArtifactType();
    artifactType.setValue(ArtifactType.valueOf(infoModel.getArtifactType()));
    metadata.setArtifactType(artifactType);

    // schema identifier
    VersionedIdentifier schemaId = new VersionedIdentifier();
    schemaId.setRoot(infoModel.getschemaIdentifier());
    schemaId.setVersion("1.0");
    metadata.setSchemaIdentifier(schemaId);

    // dataModels
    if (infoModel.getDataModels() != null) {
      metadata.setDataModels(infoModel.getDataModels());
    }

    if (infoModel.getLibraries() != null)
      metadata.setLibraries(infoModel.getLibraries());

    // title
    ST title = new ST();
    title.setValue(infoModel.getName());
    metadata.setTitle(title);

    // description
    ST description = new ST();
    description.setValue(infoModel.getDescription());
    metadata.setDescription(description);

    // key terms
    KeyTerms keyTerms = new KeyTerms();
    metadata.setKeyTerms(keyTerms);
    for (String keyTerm : infoModel.getKeyTerms()) {
      CD cd = new CD();
      ST originalText = new ST();
      originalText.setValue(keyTerm);
      cd.setOriginalText(originalText);
      keyTerms.getTerm().add(cd);
    }

    // status
    Status status = new Status();
    status.setValue(ArtifactStatusType.valueOf(infoModel.getStatus()));

    // documentation
    if (infoModel.getDocumentation() != null)
      metadata.setDocumentation(infoModel.getDocumentation());

    // related resources
    if (infoModel.getRelatedResources() != null)
      metadata.setRelatedResources(infoModel.getRelatedResources());

    // supporting evidence
    if (infoModel.getSupportingEvidence() != null)
      metadata.setSupportingEvidence(infoModel.getSupportingEvidence());

    // applicability
    if (infoModel.getApplicability() != null)
      metadata.setApplicability(infoModel.getApplicability());

    // categories
    if (infoModel.getCategories() != null)
      metadata.setCategories(infoModel.getCategories());

    // event history
    if (infoModel.getEventHistory() != null)
      metadata.setEventHistory(infoModel.getEventHistory());

    // contributions
    if (infoModel.getContributions() != null)
      metadata.setContributions(infoModel.getContributions());

    // publishers
    if (infoModel.getPublishers() != null)
      metadata.setPublishers(infoModel.getPublishers());

    return metadata;
  }

}
