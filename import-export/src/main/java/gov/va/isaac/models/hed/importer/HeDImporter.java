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
import gov.va.isaac.models.hed.HeDXmlConstants;
import gov.va.isaac.models.util.ImporterBase;

import java.io.File;
import java.io.IOException;

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
    LOG.info("Preparing to import HeD model from: " + file.getName());

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
    Node domRoot = loadModel(file);

    // Parse into HeD model.
    HeDInformationModel infoModel = createInformationModel(domRoot);

    // Save the information model
    if (service.exists(infoModel)) {
      throw new IOException(
          "Model already imported.");
    }
    service.saveInformationModel(infoModel);

    LOG.info("Ending import of HeD model from: " + file.getName());

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
          infoModel.setKey(key);
          String name = getName(loopNode);
          infoModel.setName(name);
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
   * Load model from XML file into a {@link Node}.
   *
   * @param file the file
   * @return the node
   * @throws ParserConfigurationException the parser configuration exception
   * @throws SAXException the SAX exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private Node loadModel(File file) throws ParserConfigurationException,
    SAXException, IOException {
    // Parse XML file.
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document document = db.parse(file);

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
        + " root node in XML file! " + file.getName());

    LOG.info("File OK: " + file.getName());
    return rootNode;
  }
}
