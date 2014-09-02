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
package gov.va.isaac.models.cem.exporter;

import gov.va.isaac.models.InformationModel;
import gov.va.isaac.models.api.InformationModelService;
import gov.va.isaac.models.cem.CEMComponent;
import gov.va.isaac.models.cem.CEMConstraint;
import gov.va.isaac.models.cem.CEMInformationModel;
import gov.va.isaac.models.cem.CEMXmlConstants;
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
 * Class for exporting a CEM model to an XML {@link File}.
 *
 * @author ocarlsen
 * @author bcarlsenca
 */
public class CEMExporter extends ExporterBase implements CEMXmlConstants {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(CEMExporter.class);

  /** The output stream. */
  private final OutputStream outputStream;

  /** The document. */
  private Document document;

  /**
   * Instantiates a {@link CEMExporter} from the specified parameters.
   *
   * @param outputStream the output stream
   */
  public CEMExporter(OutputStream outputStream) {
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
    LOG.info("Starting export of CEM model");
    LOG.debug("  UUID = " + uuid);

    InformationModelService service = getInformationModelService();

    // Parse into CEM model.
    InformationModel model = service.getInformationModel(uuid);
    // Abort if not available.
    if (model == null) {
      LOG.warn("No CEM model to export on " + uuid);
      return;
    }
    CEMInformationModel infoModel = new CEMInformationModel(model);

    // Build a DOM tree in the style of CEM.
    this.document = buildDom();
    Element root = buildCemTree(infoModel);
    document.appendChild(root);

    // Transform DOM tree into stream.
    Transformer transformer = buildTransformer();
    DOMSource source = new DOMSource(document);
    StreamResult result = new StreamResult(outputStream);
    transformer.transform(source, result);

    LOG.info("Ending export of CEM model");
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
  private Element buildCemTree(CEMInformationModel infoModel)
    throws ValidationException, IOException, ContradictionException {
    LOG.debug("  Build Cem Tree");
    LOG.debug("    Handle " + CEML);
    Element root = document.createElement(CEML);

    // CETYPE element.
    Element cetype = buildCetypeElement(infoModel);
    root.appendChild(cetype);

    return root;
  }

  /**
   * The spec for this model is the DiastolicBloodPressureMeas.xml document in
   * ISAAC/import-export/cem.
   *
   * @param infoModel the info model
   * @return the element
   * @throws ValidationException the validation exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ContradictionException the contradiction exception
   */
  private Element buildCetypeElement(CEMInformationModel infoModel)
    throws ValidationException, IOException, ContradictionException {
    LOG.debug("    Handle " + CETYPE);
    Element cetype = document.createElement(CETYPE);

    // Name attribute (1).
    String name = infoModel.getName();
    LOG.debug("      name = " + name);
    Attr nameAttr = buildNameAttr(name);
    cetype.setAttributeNode(nameAttr);

    // Key element (0-1).
    String key = infoModel.getKey();
    LOG.debug("      key = " + key);
    Element keyElement = buildKeyElement(key);
    cetype.appendChild(keyElement);

    // Data element (0-1).
    String dataType = infoModel.getDataType();
    LOG.debug("      dataType = " + dataType);
    Element dataElement = buildDataElement(dataType);
    cetype.appendChild(dataElement);

    // Qual elements (0-M).
    LOG.debug("      quals");
    Set<CEMComponent> quals = infoModel.getQualifiers();
    for (CEMComponent qual : quals) {
      Element qualElement = buildCompositionElement(QUAL, qual);
      cetype.appendChild(qualElement);
      LOG.debug("        " + qual.getName());
    }

    // Mod elements (0-M).
    LOG.debug("      mods");
    Set<CEMComponent> mods = infoModel.getModifiers();
    for (CEMComponent mod : mods) {
      Element modElement = buildCompositionElement(MOD, mod);
      cetype.appendChild(modElement);
      LOG.debug("        " + mod.getName());
    }

    // Att elements (0-M).
    LOG.debug("      atts");
    Set<CEMComponent> atts = infoModel.getAttributions();
    for (CEMComponent att : atts) {
      Element attElement = buildCompositionElement(ATT, att);
      cetype.appendChild(attElement);
      LOG.debug("        " + att.getName());
    }

    // Constraint elements (0-M).
    LOG.debug("      constraints");
    Set<CEMConstraint> constraints = infoModel.getConstraints();
    for (CEMConstraint constraint : constraints) {
      Element constraintElement = buildConstraintElement(constraint);
      cetype.appendChild(constraintElement);
      LOG.debug("        " + constraint.getPath());
    }

    return cetype;
  }

  /**
   * Builds the constraint element.
   *
   * @param constraint the constraint
   * @return the element
   */
  private Element buildConstraintElement(CEMConstraint constraint) {
    Element e = document.createElement(CONSTRAINT);

    // Path attribute (1).
    Attr pathAttr = document.createAttribute(PATH);
    String path = constraint.getPath();
    if (path != null) {
      pathAttr.setNodeValue(path);
    }
    e.setAttributeNode(pathAttr);

    // Value attribute (1).
    Attr valueAttr = document.createAttribute(VALUE);
    String value = constraint.getValue();
    if (value != null) {
      valueAttr.setNodeValue(value);
    }
    e.setAttributeNode(valueAttr);

    return e;
  }

  /**
   * Builds the composition element.
   *
   * @param elementName the element name
   * @param component the composition
   * @return the element
   */
  private Element buildCompositionElement(String elementName,
    CEMComponent component) {
    Element e = document.createElement(elementName);

    // Type attribute.
    Attr typeAttr = document.createAttribute(TYPE);
    String type = component.getDateTypeRef();
    typeAttr.setNodeValue(type);
    e.setAttributeNode(typeAttr);

    // Name attribute.
    Attr nameAttr = document.createAttribute(NAME);
    String name = decapitalize(type);
    nameAttr.setNodeValue(name);
    e.setAttributeNode(nameAttr);

    // Constraint attribute (0-1).
    if (!component.getCardinalityMin().isEmpty()) {
      Attr constraintAttr = document.createAttribute("card");
      constraintAttr.setNodeValue(component.getCardinalityMin() + "-"
          + component.getCardinalityMax());
      e.setAttributeNode(constraintAttr);
    }

    // Value element - only really here for demo puropses
    String value = component.getValue();
    if (value != null) {
      e.setTextContent(value);
    }

    return e;
  }

  /**
   * Builds the data element.
   *
   * @param dataType the data type
   * @return the element
   * @throws ValidationException the validation exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private Element buildDataElement(String dataType) throws ValidationException,
    IOException {
    Element data = document.createElement(DATA);
    // Type attribute.
    Attr typeAttr = document.createAttribute(TYPE);
    typeAttr.setNodeValue(dataType);
    data.setAttributeNode(typeAttr);
    return data;
  }

  /**
   * Builds the key element.
   *
   * @param code the code
   * @return the element
   */
  private Element buildKeyElement(String code) {
    Element key = document.createElement(KEY);

    // Code attribute.
    Attr codeAttr = document.createAttribute(CODE);
    codeAttr.setNodeValue(code);
    key.setAttributeNode(codeAttr);

    return key;
  }

  /**
   * Builds the name attr.
   *
   * @param name the name
   * @return the attr
   */
  private Attr buildNameAttr(String name) {
    Attr nameAttr = document.createAttribute(NAME);

    nameAttr.setNodeValue(name);

    return nameAttr;
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
