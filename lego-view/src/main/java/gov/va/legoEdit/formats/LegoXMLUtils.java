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
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.legoEdit.formats;

import gov.va.legoEdit.model.schemaModel.Assertion;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.model.schemaModel.Discernible;
import gov.va.legoEdit.model.schemaModel.Expression;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.LegoList;
import gov.va.legoEdit.model.schemaModel.Qualifier;
import gov.va.legoEdit.model.schemaModel.Value;
import gov.va.legoEdit.util.Utility;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.tidy.Tidy;
//import org.w3c.tidy.Tidy;
import org.xml.sax.SAXException;

/**
 * 
 * LegoXMLUtils
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 */
public class LegoXMLUtils
{
	static Logger logger = LoggerFactory.getLogger(LegoXMLUtils.class);
	static Schema schema;
	static JAXBContext jc;
	static Transformer xmlToHTMLTransformer;
	static Tidy htmlTidy = null;

	static
	{
		try
		{
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			schema = schemaFactory.newSchema(LegoXMLUtils.class.getResource("/LEGO.xsd"));
			jc = JAXBContext.newInstance(LegoList.class);
			TransformerFactory tf = TransformerFactory.newInstance();
			xmlToHTMLTransformer = tf.newTransformer(new StreamSource(LegoXMLUtils.class.getResourceAsStream("/xslTransforms/xmlRenderedAsHTML.xslt")));
		}
		catch (SAXException | JAXBException | TransformerConfigurationException e)
		{
			throw new RuntimeException("Build Error", e);
		}
	}

	public static void validate(File path) throws IOException, SAXException
	{
		logger.debug("Validating the XML file {}", path);
		try
		{
			Source xmlFile = new StreamSource(path);
			Validator validator = schema.newValidator();
			validator.validate(xmlFile);
			logger.debug("The XML file {} is valid", path);
		}
		catch (SAXException | IOException e)
		{
			logger.debug("The XML file {} is invalid: {}", path, e.getLocalizedMessage());
			throw e;
		}
	}

	public static LegoList readLegoList(File path) throws JAXBException, FileNotFoundException
	{
		Unmarshaller um = jc.createUnmarshaller();
		return (LegoList) um.unmarshal(new FileReader(path));
	}
	
	public static LegoList readLegoList(InputStream is) throws JAXBException
	{
		return (LegoList) jc.createUnmarshaller().unmarshal(is);
	}

	public static String toXML(Lego l) throws PropertyException, JAXBException
	{
		return marshall(l, true);
	}
	
	public static String toXML(Concept c) throws PropertyException, JAXBException
	{
		return marshall(c, true);
	}

	public static String toXML(LegoList ll) throws PropertyException, JAXBException
	{
		return marshall(ll, false);
	}

	public static String toXML(Value v) throws PropertyException, JAXBException
	{
		return marshall(v, true);
	}

	public static String toXML(Assertion a) throws PropertyException, JAXBException
	{
		return marshall(a, true);
	}

	public static String toXML(Discernible d) throws PropertyException, JAXBException
	{
		return marshall(d, true);
	}

	public static String toXML(Qualifier q) throws PropertyException, JAXBException
	{
		return marshall(q, true);
	}

	public static String toXML(Expression e) throws PropertyException, JAXBException
	{
		return marshall(e, true);
	}

	private static String marshall(Object o, boolean fragment) throws PropertyException, JAXBException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		writeXML(o, baos, fragment);
		return new String(baos.toByteArray());
	}

	public static void writeXML(Object o, OutputStream os, boolean fragment) throws PropertyException, JAXBException
	{
		Marshaller m = jc.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		if (fragment)
		{
			m.setProperty(Marshaller.JAXB_FRAGMENT, true);
		}
		else
		{
			m.setProperty(Marshaller.JAXB_FRAGMENT, false);
			m.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, "LEGO.xsd");
		}
		m.marshal(o, os);
	}

	public static Assertion readAssertion(String xmlAssertion) throws JAXBException
	{
		Unmarshaller um = jc.createUnmarshaller();
		return (Assertion) um.unmarshal(new ByteArrayInputStream(xmlAssertion.getBytes()));
	}

	public static Value readValue(String xmlValue) throws JAXBException
	{
		Unmarshaller um = jc.createUnmarshaller();
		return (Value) um.unmarshal(new ByteArrayInputStream(xmlValue.getBytes()));
	}

	public static Discernible readDiscernible(String xmlDiscernible) throws JAXBException
	{
		Unmarshaller um = jc.createUnmarshaller();
		return (Discernible) um.unmarshal(new ByteArrayInputStream(xmlDiscernible.getBytes()));
	}

	public static Qualifier readQualifier(String xmlQualifier) throws JAXBException
	{
		Unmarshaller um = jc.createUnmarshaller();
		return (Qualifier) um.unmarshal(new ByteArrayInputStream(xmlQualifier.getBytes()));
	}

	public static Expression readExpression(String xmlExpression) throws JAXBException
	{
		return (Expression) jc.createUnmarshaller().unmarshal(new ByteArrayInputStream(xmlExpression.getBytes()));
	}

	public static Lego readLego(String xmlLego) throws JAXBException
	{
		return (Lego) jc.createUnmarshaller().unmarshal(new ByteArrayInputStream(xmlLego.getBytes()));
	}
	
	public static Concept readConcept(String xmlConcept) throws JAXBException
	{
		return (Concept) jc.createUnmarshaller().unmarshal(new ByteArrayInputStream(xmlConcept.getBytes()));
	}

	public static Object read(InputStream is) throws JAXBException
	{
		Unmarshaller um = jc.createUnmarshaller();
		return um.unmarshal(is);
	}
	public static String toHTML(LegoList ll) throws PropertyException, JAXBException, TransformerConfigurationException, TransformerException
	{
		String asXML = toXML(ll);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		StreamResult result = new StreamResult(baos);
		xmlToHTMLTransformer.transform(new StreamSource(new ByteArrayInputStream(asXML.getBytes())), result);
		return baos.toString();
	}

	public static void schemaValidateLego(final Lego lego, final LegoValidateCallback callback)
	{
		Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					logger.debug("Schema validating a Lego");
					//In order to schema validate, we need to check a full lego list.
					LegoList ll = new LegoList();
					ll.setGroupName("foo");
					ll.setLegoListUUID(UUID.randomUUID().toString());
					ll.getLego().add(lego);
					
					Source xmlFile = new StreamSource(new ByteArrayInputStream(toXML(ll).getBytes()));
					Validator validator = schema.newValidator();
					validator.validate(xmlFile);
					callback.validateComplete(true, null);
				}
				catch (Exception e)
				{
					callback.validateComplete(false, e.getMessage());
				}
			}
		};
		Utility.tpe.execute(r);
	}
	
	public static Tidy getTidy()
	{
		if (htmlTidy == null)
		{
			htmlTidy = new Tidy();
			htmlTidy.setXHTML(true);
			htmlTidy.setIndentContent(true);
			htmlTidy.setSmartIndent(true);
			htmlTidy.setXmlTags(true);
			htmlTidy.setWraplen(150);
			htmlTidy.setQuiet(true);
		}
		return htmlTidy;
	}
	
	public static void transform(LegoList ll, FileOutputStream target, Transformer transformer, boolean tidyHtml ) throws IOException, TransformerException, PropertyException, JAXBException
	{
		String legoListAsXML = toXML(ll);
		if (transformer != null)
		{
			ByteArrayInputStream bais = new ByteArrayInputStream(legoListAsXML.getBytes());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			transformer.transform(new StreamSource(bais), new StreamResult(baos));
			if (tidyHtml)
			{
				getTidy().parse(new ByteArrayInputStream(baos.toByteArray()), target);
			}
			else
			{
				target.write(baos.toByteArray());
			}
		}
		else
		{
			target.write(legoListAsXML.getBytes());
		}
	}
	
	public static void transform(Lego l, FileOutputStream target, Transformer transformer, boolean tidyHtml ) throws IOException, TransformerException, PropertyException, JAXBException
	{
		String legoAsXML = toXML(l);
		if (transformer != null)
		{
			ByteArrayInputStream bais = new ByteArrayInputStream(legoAsXML.getBytes());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			transformer.transform(new StreamSource(bais), new StreamResult(baos));
			if (tidyHtml)
			{
				getTidy().parse(new ByteArrayInputStream(baos.toByteArray()), target);
			}
			else
			{
				target.write(baos.toByteArray());
			}
		}
		else
		{
			target.write(legoAsXML.getBytes());
		}
	}
}
