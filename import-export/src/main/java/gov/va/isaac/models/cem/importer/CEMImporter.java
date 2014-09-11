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
package gov.va.isaac.models.cem.importer;

import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.ie.ImportHandler;
import gov.va.isaac.model.InformationModelType;
import gov.va.isaac.models.InformationModel;
import gov.va.isaac.models.api.InformationModelService;
import gov.va.isaac.models.cem.CEMComponent;
import gov.va.isaac.models.cem.CEMComponentType;
import gov.va.isaac.models.cem.CEMConstraint;
import gov.va.isaac.models.cem.CEMInformationModel;
import gov.va.isaac.models.cem.CEMXmlConstants;
import gov.va.isaac.models.util.ImporterBase;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

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
 * An {@link ImportHandler} for importing a CEM model from an XML file.
 * 
 * @author alo
 * @author ocarlsen
 * @author bcarlsenca
 */
public class CEMImporter extends ImporterBase implements ImportHandler,
    CEMXmlConstants {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(CEMImporter.class);

  /**
   * Instantiates an empty {@link CEMImporter}.
   */
  public CEMImporter() {
    super();
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.ie.ImportHandler#importModel(java.io.File)
   */
  @Override
  public InformationModel importModel(File file) throws Exception {
    LOG.info("Preparing to import CEM model from: " + file.getName());

    // Make sure in background thread.
    FxUtils.checkBackgroundThread();

    // Obtain service
    InformationModelService service = getInformationModelService();

    // Load DOM tree from file.
    Node domRoot = loadModel(file);

    // Parse into CEM model.
    CEMInformationModel infoModel = createInformationModel(domRoot);

    // TODO: this should be made dynamic within the app
    // Sample connection to a terminology concept
    // This is to retain functionality from before
    UUID conceptUuid = UUID.fromString("215fd598-e21d-3e27-a0a2-8e23b1b36dfc");
    infoModel.addAssociatedConceptUuid(conceptUuid);
    
    // Save the information model
//    if (service.exists(infoModel)) {
//      throw new IOException(
//          "Model already imported");
//    }
    service.saveInformationModel(infoModel);

    LOG.info("Ending import of CEM model from: " + file.getName());

    return infoModel;
  }

  /**
   * Creates the information model from the XML DOM {@link Node}.
   *
   * @param rootNode the root node
   * @return the CEM information model
   * @throws IOException
   */
  private CEMInformationModel createInformationModel(Node rootNode)
    throws IOException {

    // Look for CETYPE child node.
    Node cetypeNode = null;
    NodeList childNodes = rootNode.getChildNodes();
    for (int nodeCount = 0; nodeCount < childNodes.getLength(); nodeCount++) {
      Node childNode = childNodes.item(nodeCount);
      LOG.debug("childNode : " + nodeCount + " - " + childNode.getNodeName());

      if (childNode.getNodeName().equals(CETYPE)) {
        cetypeNode = childNode;
        break;
      }
    }

    // Sanity check.
    Preconditions.checkNotNull(cetypeNode, "No CETYPE child node in XML file!");
    LOG.debug("cetype: " + cetypeNode.getNodeName());

    // Create the model
    CEMInformationModel infoModel = new CEMInformationModel();
    infoModel.setType(InformationModelType.CEM);

    // Parse CETYPE node attributes.
    String name =
        cetypeNode.getAttributes().getNamedItem(NAME).getTextContent();
    infoModel.setName(name);
    LOG.info("name: " + name);

    // Iterate through CETYPE node children and process.
    NodeList cetypeChildren = cetypeNode.getChildNodes();
    for (int nodeCount = 0; nodeCount < cetypeChildren.getLength(); nodeCount++) {
      Node loopNode = cetypeChildren.item(nodeCount);
      LOG.debug("loopNode : " + nodeCount + " - " + loopNode.getNodeName());

      switch (loopNode.getNodeName()) {
        case KEY:
          String key =
              loopNode.getAttributes().getNamedItem(CODE).getTextContent();
          LOG.info("key: " + key);
          infoModel.setKey(key);
          break;

        case DATA:
          String data =
              loopNode.getAttributes().getNamedItem(TYPE).getTextContent();
          LOG.info("data: " + data);
          infoModel.setDataType(data);
          break;

        case QUAL:
          String qualName =
              loopNode.getAttributes().getNamedItem(NAME).getTextContent();
          String qualType =
              loopNode.getAttributes().getNamedItem(TYPE).getTextContent();
          String qualCard =
              loopNode.getAttributes().getNamedItem(CARD).getTextContent();
          String qualCompValue = loopNode.getTextContent();
          LOG.info(String.format("mod: %s %s %s %s", qualName, qualType,
              qualCard, qualCompValue));
          CEMComponent qual = new CEMComponent();
          qual.setComponentType(CEMComponentType.QUALIFIER);
          qual.setDateTypeRef(qualType);
          qual.setName(qualName);
          if (qualCard.length() == 3) {
            qual.setCardinalityMin(qualCard.substring(0, 1));
            qual.setCardinalityMax(qualCard.substring(2, 3));
          } else if (qualCard.length() == 0) {
            // do nothing
          } else {
            throw new IOException("Unable to handle cardinality value: "
                + qualCard);
          }
          infoModel.addQualifier(qual);
          break;

        case MOD:
          String modName =
              loopNode.getAttributes().getNamedItem(NAME).getTextContent();
          String modType =
              loopNode.getAttributes().getNamedItem(TYPE).getTextContent();
          String modCard =
              loopNode.getAttributes().getNamedItem(CARD).getTextContent();
          String modCompValue = loopNode.getTextContent();
          LOG.info(String.format("mod: %s %s %s %s", modName, modType, modCard,
              modCompValue));
          CEMComponent mod = new CEMComponent();
          mod.setComponentType(CEMComponentType.MODIFIER);
          mod.setDateTypeRef(modType);
          mod.setName(modName);
          if (modCard.length() == 3) {
            mod.setCardinalityMin(modCard.substring(0, 1));
            mod.setCardinalityMax(modCard.substring(2, 3));
          } else if (modCard.length() == 0) {
            // do nothing
          } else {
            throw new IOException("Unable to handle cardinality value: "
                + modCard);
          }
          infoModel.addModifier(mod);
          break;

        case ATT:
          String attName =
              loopNode.getAttributes().getNamedItem(NAME).getTextContent();
          String attType =
              loopNode.getAttributes().getNamedItem(TYPE).getTextContent();
          String attCard =
              loopNode.getAttributes().getNamedItem(CARD).getTextContent();
          String attCompValue = loopNode.getTextContent();
          LOG.info(String.format("att: %s %s %s %s", attName, attType, attCard,
              attCompValue));

          CEMComponent att = new CEMComponent();
          att.setComponentType(CEMComponentType.ATTRIBUTION);
          att.setDateTypeRef(attType);
          att.setName(attName);
          if (attCard.length() == 3) {
            att.setCardinalityMin(attCard.substring(0, 1));
            att.setCardinalityMax(attCard.substring(2, 3));
          } else if (attCard.length() == 0) {
            // do nothing
          } else {
            throw new IOException("Unable to handle cardinality value: "
                + attCard);
          }
          infoModel.addAttribution(att);
          break;

        case CONSTRAINT:
          String path =
              loopNode.getAttributes().getNamedItem(PATH).getTextContent();
          String value =
              loopNode.getAttributes().getNamedItem(VALUE).getTextContent();
          LOG.info(String.format("constraint: %s %s", path, value));

          CEMConstraint constraint = new CEMConstraint();
          constraint.setPath(path);
          constraint.setValue(value);
          infoModel.addConstraint(constraint);
          break;

        default:

          break;
      }
    }

    return infoModel;
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

    Node rootNode = document.getFirstChild();
    LOG.info("rootNode: " + rootNode.getNodeName());

    // Sanity check.
    Preconditions.checkState(rootNode.getNodeName().toLowerCase().equals(CEML),
        "No CEML root node in XML file! " + file.getName());

    LOG.info("File OK: " + file.getName());
    return rootNode;
  }
}
