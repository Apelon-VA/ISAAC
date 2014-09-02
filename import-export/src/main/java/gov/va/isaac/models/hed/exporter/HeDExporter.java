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
import gov.va.isaac.models.cem.CEMComponent;
import gov.va.isaac.models.cem.CEMConstraint;
import gov.va.isaac.models.cem.CEMInformationModel;
import gov.va.isaac.models.hed.HeDInformationModel;
import gov.va.isaac.models.hed.HeDXmlConstants;
import gov.va.isaac.models.util.ExporterBase;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

  /** The document. */
  private Document document;

  /**
   * Instantiates a {@link HeDExporter} from the specified parameters.
   *
   * @param outputStream the output stream
   */
  public HeDExporter(OutputStream outputStream) {
    super();
    this.outputStream = outputStream;
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
    this.document = buildDom();
    Element root = buildHedTree(infoModel);
    document.appendChild(root);

    // Transform DOM tree into stream.
    Transformer transformer = buildTransformer();
    DOMSource source = new DOMSource(document);
    StreamResult result = new StreamResult(outputStream);
    transformer.transform(source, result);

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
   * Builds the cem tree.
   *
   * @param infoModel the info model
   * @return the element
   * @throws ValidationException the validation exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ContradictionException the contradiction exception
   */
  private Element buildHedTree(HeDInformationModel infoModel)
    throws ValidationException, IOException, ContradictionException {
    LOG.debug("  Build Cem Tree");
    LOG.debug("    Handle " + KNOWLEDGE_DOCUMENT);
    Element root = document.createElement(KNOWLEDGE_DOCUMENT);
    Attr namespace = document.createAttribute(XMLNS);
    namespace.setNodeValue("urn:hl7-org:knowledgeartifact:r1");
    root.setAttributeNode(namespace);
    namespace = document.createAttribute(XMLNS_VMR);
    namespace.setNodeValue("urn:hl7-org:vmr:r2");
    root.setAttributeNode(namespace);
    namespace = document.createAttribute(XMLNS_DT);
    namespace.setNodeValue("urn:hl7-org:cdsdt:r2");
    root.setAttributeNode(namespace);
    namespace = document.createAttribute(XMLNS_P1);
    namespace.setNodeValue("http://www.w3.org/1999/xhtml");
    root.setAttributeNode(namespace);
    namespace = document.createAttribute(XMLNS_XML);
    namespace.setNodeValue("http://www.w3.org/XML/1998/namespace");
    root.setAttributeNode(namespace);
    namespace = document.createAttribute(XMLNS_XSI);
    namespace.setNodeValue("http://www.w3.org/2001/XMLSchema-instance");
    root.setAttributeNode(namespace);
    namespace = document.createAttribute(XSI_SCHEMA_LOCATION);
    namespace.setNodeValue("urn:hl7-org:knowledgeartifact:r1 ../schema/knowledgeartifact/knowledgedocument.xsd ");
    root.setAttributeNode(namespace);
    
    // CETYPE element.
    Element metadata = buildMetadataElement(infoModel);
    root.appendChild(metadata);

    return root;
  }

  /**
   * Builds the metadata element.
   *
   * @param infoModel the info model
   * @return the element
   * @throws ValidationException the validation exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ContradictionException the contradiction exception
   */
  private Element buildMetadataElement(HeDInformationModel infoModel)
    throws ValidationException, IOException, ContradictionException {
    LOG.debug("    Handle " + METADATA);
    Element metadata = document.createElement(METADATA);

    // key
    String key = infoModel.getKey();
    LOG.debug("      key = " + key);
    Element identifiers = buildIdentifiers(key);
    metadata.appendChild(identifiers);

    // name
    String name = infoModel.getKey();
    LOG.debug("      name = " + key);
    Element title = buildTitle(name);
    metadata.appendChild(title);

    return metadata;
  }

  
  /**
   * Builds the identifiers.
   *
   * @param key the key
   * @return the element
   */
  private Element buildIdentifiers(String key) {
    Element identifiers = document.createElement(IDENTIFIERS);

    Element identifier = document.createElement(IDENTIFIER);
    identifiers.appendChild(identifier);

    Attr rootAttr = document.createAttribute(ROOT);
    rootAttr.setNodeValue(key);
    identifier.setAttributeNode(rootAttr);

    Attr versionAttr = document.createAttribute(VERSION);
    versionAttr.setNodeValue("1.0");
    identifier.setAttributeNode(versionAttr);
    
    return identifiers;
  }

  /**
   * Builds the title.
   *
   * @param name the name
   * @return the element
   */
  private Element buildTitle(String name) {
    Element title = document.createElement(TITLE);

    Attr valueAttr = document.createAttribute(VALUE);
    valueAttr.setNodeValue(name);
    title.setAttributeNode(valueAttr);

    return title;
  }

  /**
   * Builds the dom.
   *
   * @return the document
   * @throws ParserConfigurationException the parser configuration exception
   */
  private Document buildDom() throws ParserConfigurationException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    return builder.newDocument();
  }

  /**
   * Builds the transformer.
   *
   * @return the transformer
   * @throws Exception the exception
   */
  private Transformer buildTransformer() throws Exception {
    TransformerFactory factory = TransformerFactory.newInstance();
    Transformer transformer = factory.newTransformer();

    // Indent output.
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
        "4");

    // Skip XML declaration header.
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

    return transformer;
  }

  /**
   * Utility method to take a string and convert it to normal Java variable name
   * capitalization. This normally means converting the first character from
   * upper case to lower case, but in the (unusual) special case when there is
   * more than one character and both the first and second characters are upper
   * case, we leave it alone.
   * 
   * Thus "FooBah" becomes "fooBah" and "X" becomes "x", but "URL" stays as
   * "URL".
   * 
   * Parameters
   * @param name The string to be decapitalized. Returns:
   * @return The decapitalized version of the string.
   * 
   *         Note, this was copied from 1.7_40 release of the JDK, as it was
   *         removed from com.sun.xml.internal.ws.util.StringUtils in 1.8.
   */

  public static String decapitalize(String name) {
    if (name == null || name.length() == 0) {
      return name;
    }
    if (name.length() > 1 && Character.isUpperCase(name.charAt(1))
        && Character.isUpperCase(name.charAt(0))) {
      return name;
    }
    char chars[] = name.toCharArray();
    chars[0] = Character.toLowerCase(chars[0]);
    return new String(chars);
  }
}
