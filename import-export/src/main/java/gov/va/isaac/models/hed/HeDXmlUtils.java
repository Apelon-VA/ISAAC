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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * HeD Xml utilities.
 */
public class HeDXmlUtils {

  /** The transformer for DOM -> XML. */
  private Transformer transformer;

  /**
   * Instantiates an empty {@link HeDXmlUtils}.
   *
   * @throws TransformerConfigurationException the transformer configuration
   *           exception
   */
  public HeDXmlUtils() throws TransformerConfigurationException {
    TransformerFactory factory = TransformerFactory.newInstance();
    transformer = factory.newTransformer();
    // Indent output.
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
        "4");
    // Skip XML declaration header.
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
  }

  /**
   * Returns the graph for string.
   *
   * @param xml the xml
   * @param graphClass the graph class
   * @return the graph for string
   * @throws JAXBException the JAXB exception
   */
  @SuppressWarnings("static-method")
  public Object getGraphForString(String xml, Class<?> graphClass)
    throws JAXBException {
    JAXBContext context = JAXBContext.newInstance(graphClass);
    Unmarshaller unmarshaller = context.createUnmarshaller();
    return unmarshaller.unmarshal(new StreamSource(new StringReader(xml)));
  }

  /**
   * Returns the graph for file.
   *
   * @param file the file
   * @param graphClass the graph class
   * @return the graph for file
   * @throws FileNotFoundException the file not found exception
   * @throws JAXBException the JAXB exception
   */
  @SuppressWarnings("resource")
  public Object getGraphForFile(File file, Class<?> graphClass)
    throws FileNotFoundException, JAXBException {
    return getGraphForString(new Scanner(file, "UTF-8").useDelimiter("\\A")
        .next(), graphClass);
  }

  /**
   * Returns the graph for stream.
   *
   * @param in the in
   * @param graphClass the graph class
   * @return the graph for stream
   * @throws FileNotFoundException the file not found exception
   * @throws JAXBException the JAXB exception
   */
  @SuppressWarnings("resource")
  public Object getGraphForStream(InputStream in, Class<?> graphClass)
    throws FileNotFoundException, JAXBException {
    return getGraphForString(new Scanner(in, "UTF-8").useDelimiter("\\A")
        .next(), graphClass);
  }

  /**
   * Returns the XML string for for graph object.
   *
   * @param object the object
   * @return the string for for graph
   * @throws JAXBException the JAXB exception
   */
  @SuppressWarnings("static-method")
  public String getStringForGraph(Object object) throws JAXBException {
    StringWriter writer = new StringWriter();
    JAXBContext jaxbContext = null;
    jaxbContext = JAXBContext.newInstance(object.getClass());
    Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
    jaxbMarshaller.marshal(object, writer);
    return writer.toString();
  }

  /**
   * Returns the node for string.
   *
   * @param xml the xml
   * @return the node for string
   * @throws ParserConfigurationException the parser configuration exception
   * @throws SAXException the SAX exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @SuppressWarnings("static-method")
  public Node getNodeForString(String xml) throws ParserConfigurationException,
    SAXException, IOException {

    InputStream in =
        new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
    // Parse XML file.
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document document = db.parse(in);
    Node rootNode = document.getFirstChild();
    return rootNode;
  }

  /**
   * Returns the node for file.
   *
   * @param file the file
   * @return the node for file
   * @throws ParserConfigurationException the parser configuration exception
   * @throws SAXException the SAX exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @SuppressWarnings("static-method")
  public Node getNodeForFile(File file) throws ParserConfigurationException,
    SAXException, IOException {
    InputStream in = new FileInputStream(file);
    // Parse XML file.
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document document = db.parse(in);
    Node rootNode = document.getFirstChild();
    in.close();
    return rootNode;
  }

  /**
   * Returns the string for node.
   *
   * @param root the root node
   * @return the string for node
   * @throws TransformerException the transformer exception
   * @throws ParserConfigurationException the parser configuration exception
   */
  public String getStringForNode(Node root) throws TransformerException,
    ParserConfigurationException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.newDocument();
    document.appendChild(document.importNode(root, true));
    DOMSource source = new DOMSource(document);
    StringWriter out = new StringWriter();
    StreamResult result = new StreamResult(out);
    transformer.transform(source, result);
    return out.toString();
  }

  /**
   * Returns the graph for node.
   *
   * @param node the node
   * @param graphClass the graph class
   * @return the graph for node
   * @throws JAXBException the JAXB exception
   * @throws TransformerException the transformer exception
   * @throws ParserConfigurationException the parser configuration exception
   */
  public Object getGraphForNode(Node node, Class<?> graphClass)
    throws JAXBException, TransformerException, ParserConfigurationException {
    return getGraphForString(getStringForNode(node), graphClass);
  }

  /**
   * Returns the node for graph.
   *
   * @param object the object
   * @return the node for graph
   * @throws ParserConfigurationException the parser configuration exception
   * @throws SAXException the SAX exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws JAXBException the JAXB exception
   */
  public Node getNodeForGraph(Object object)
    throws ParserConfigurationException, SAXException, IOException,
    JAXBException {
    return getNodeForString(getStringForGraph(object));
  }

  /**
   * Pretty format.
   *
   * @param input the input
   * @param indent the indent
   * @return the string
   */
  @SuppressWarnings("static-method")
  public String prettyFormat(String input, int indent) {
    try {
      Source xmlInput = new StreamSource(new StringReader(input));
      StringWriter stringWriter = new StringWriter();
      StreamResult xmlOutput = new StreamResult(stringWriter);
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      transformerFactory.setAttribute("indent-number", indent);
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.transform(xmlInput, xmlOutput);
      return xmlOutput.getWriter().toString();
    } catch (Exception e) {
      throw new RuntimeException(e); // simple exception handling, please review
                                     // it
    }
  }
}
