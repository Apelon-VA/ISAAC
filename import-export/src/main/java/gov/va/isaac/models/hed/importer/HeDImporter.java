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

import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.ie.ImportHandler;
import gov.va.isaac.model.InformationModelType;
import gov.va.isaac.models.InformationModel;
import gov.va.isaac.models.api.InformationModelService;
import gov.va.isaac.models.hed.HeDInformationModel;
import gov.va.isaac.models.hed.HeDModelReference;
import gov.va.isaac.models.hed.HeDXmlConstants;
import gov.va.isaac.models.util.ImporterBase;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;

/**
 * An {@link ImportHandler} for importing a HeD model from an XML file.
 * 
 * @author bcarlsenca
 */
public class HeDImporter extends ImporterBase implements ImportHandler,
    HeDXmlConstants {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(HeDImporter.class);

  /**
   * Instantiates an empty {@link HeDImporter}.
   */
  public HeDImporter() {
    super();
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.ie.ImportHandler#importModel(java.io.File)
   */
  @Override
  public InformationModel importModel(File file) throws Exception {
    FileInputStream in = null;
    try {
      LOG.info("Preparing to import CEM model from: " + file.getName());
      in = new FileInputStream(file);
      LOG.info("Ending import of CEM model from: " + file.getName());
      InformationModel model = importModel(in);
      in.close();
      return model;
    } catch (Exception e) {
      in.close();
      throw e;
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.ie.ImportHandler#importModel(java.io.InputStream)
   */
  @Override
  public InformationModel importModel(InputStream in) throws Exception {
    LOG.info("Preparing to import HeD model from stream");

    // Make sure in background thread.
    FxUtils.checkBackgroundThread();

    // Obtain service
    InformationModelService service = getInformationModelService();

    // focus concept is no longer hardcoded - this will be connected to model
    // LATER using UI
    // Get focus concept.
    // String focusConceptUuid = "215fd598-e21d-3e27-a0a2-8e23b1b36dfc";
    // ConceptChronicleBI focusConcept =
    // getDataStore().getConcept(UUID.fromString(focusConceptUuid));
    // LOG.info("focusConcept: " + focusConcept.toString());

    // Load DOM tree from file.
    Node domRoot = loadModel(in);

    // Parse into HeD model.
    HeDInformationModel infoModel = createInformationModel(domRoot);

    // Save the information model
    // if (service.exists(infoModel)) {
    // throw new IOException("Model already imported.");
    // }
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
   */
  private HeDInformationModel createInformationModel(Node kdNode)
    throws IOException {

    // Create the model
    HeDInformationModel infoModel = new HeDInformationModel();
    infoModel.setType(InformationModelType.HeD);

    // Iterate through KNOWLEDGE_DOCUMENT node children and process.
    NodeList kdNodeChildren = kdNode.getChildNodes();
    for (int nodeCount = 0; nodeCount < kdNodeChildren.getLength(); nodeCount++) {
      Node loopNode = kdNodeChildren.item(nodeCount);
      LOG.debug("loopNode : " + nodeCount + " - " + loopNode.getNodeName());

      switch (loopNode.getNodeName()) {
        case METADATA:
          String key = getIdentifier(loopNode);
          LOG.debug("      key = " + key);
          infoModel.setKey(key);
          String name = getName(loopNode);
          LOG.debug("      name = " + name);
          infoModel.setName(name);
          String artifactType = getArtifactType(loopNode);
          LOG.debug("      artifactType = " + artifactType);
          if (artifactType != null)
            infoModel.setArtifactType(artifactType);

          List<HeDModelReference> dataModels = getDataModels(loopNode);
          LOG.debug("      dataModels.ct = " + dataModels.size());
          if (dataModels.size() > 0) {
            infoModel.setDataModels(dataModels);
          }

          break;

        default:

          break;
      }
    }

    return infoModel;
  }

  /**
   * Returns the identifier.
   *
   * @param metadataNode the metadata node
   * @return the identifier
   */
  private String getIdentifier(Node metadataNode) {
    Node identifiersNode = getChildNodeByName(IDENTIFIERS, metadataNode);
    Node identifierNode = getChildNodeByName(IDENTIFIER, identifiersNode);
    return identifierNode.getAttributes().getNamedItem(ROOT).getTextContent();
  }

  /**
   * Returns the name.
   *
   * @param metadataNode the metadata node
   * @return the name
   */
  private String getName(Node metadataNode) {
    Node titleNode = getChildNodeByName(TITLE, metadataNode);
    return titleNode.getAttributes().getNamedItem(VALUE).getTextContent();
  }

  /**
   * Returns the artifact type.
   *
   * @param metadataNode the metadata node
   * @return the artifact type
   */
  private String getArtifactType(Node metadataNode) {
    Node titleNode = getChildNodeByName(ARTIFACT_TYPE, metadataNode);
    return titleNode == null ? null : titleNode.getAttributes()
        .getNamedItem(VALUE).getTextContent();
  }

  /**
   * Returns the data models.
   *
   * @param metadataNode the metadata node
   * @return the data models
   */
  private List<HeDModelReference> getDataModels(Node metadataNode) {
    List<HeDModelReference> dataModels = new ArrayList<>();
    Node dataModelNodes = getChildNodeByName(DATA_MODELS, metadataNode);
    for (Node modelRefNode : getChildNodes(dataModelNodes)) {
      if (modelRefNode.getNodeName().equals(MODEL_REFERENCE)) {
        HeDModelReference ref = new HeDModelReference();
        Node description = getChildNodeByName(DESCRIPTION, modelRefNode);
        Node referencedModel =
            getChildNodeByName(REFERENCED_MODEL, modelRefNode);
        ref.setDescription(description.getAttributes().getNamedItem(VALUE)
            .getTextContent());
        ref.setReferencedModel(referencedModel.getAttributes()
            .getNamedItem(VALUE).getTextContent());
        dataModels.add(ref);
      }

    }
    return dataModels;

  }

  /**
   * Returns the child node by name.
   *
   * @param name the name
   * @param parentNode the parent node
   * @return the child node by name
   */
  private Node getChildNodeByName(String name, Node parentNode) {
    NodeList childNodes = parentNode.getChildNodes();
    for (int nodeCount = 0; nodeCount < childNodes.getLength(); nodeCount++) {
      Node loopNode = childNodes.item(nodeCount);
      LOG.debug("loopNode : " + nodeCount + " - " + loopNode.getNodeName());
      if (loopNode.getNodeName().equals(name)) {
        return loopNode;
      }
    }
    throw new IllegalArgumentException("No child node exists with name " + name);
  }

  /**
   * Returns the child nodes as an iterable.
   *
   * @param node the node
   * @return the child nodes
   */
  private List<Node> getChildNodes(Node node) {
    List<Node> nodes = new ArrayList<>();
    NodeList childNodes = node.getChildNodes();
    for (int nodeCount = 0; nodeCount < childNodes.getLength(); nodeCount++) {
      Node loopNode = childNodes.item(nodeCount);
      nodes.add(loopNode);
    }
    return nodes;
  }

  /**
   * Load model from XML file into a {@link Node}.
   *
   * @param in the input stream
   * @return the node
   * @throws ParserConfigurationException the parser configuration exception
   * @throws SAXException the SAX exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private Node loadModel(InputStream in) throws ParserConfigurationException,
    SAXException, IOException {
    // Parse XML file.
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document document = db.parse(in);

    NodeList childNodes = document.getChildNodes();
    Node rootNode = null;
    for (int nodeCount = 0; nodeCount < childNodes.getLength(); nodeCount++) {
      Node loopNode = childNodes.item(nodeCount);
      LOG.debug("loopNode: " + loopNode.getNodeName());
      if (loopNode.getNodeName().equals(KNOWLEDGE_DOCUMENT)) {
        rootNode = loopNode;
        break;
      }
    }
    LOG.debug("rootNode: " + rootNode.getNodeName());

    // Sanity check.
    Preconditions.checkState(rootNode != null, "No " + KNOWLEDGE_DOCUMENT
        + " root node in XML");

    return rootNode;
  }
}
