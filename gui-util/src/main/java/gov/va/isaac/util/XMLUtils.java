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
package gov.va.isaac.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link XMLUtils}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class XMLUtils
{
	static Logger logger = LoggerFactory.getLogger(XMLUtils.class);
	static Transformer xmlToHTMLTransformer;
	
	static
	{
		try
		{
			TransformerFactory tf = TransformerFactory.newInstance();
			xmlToHTMLTransformer = tf.newTransformer(new StreamSource(XMLUtils.class.getResourceAsStream("/xslTransforms/xmlRenderedAsHTML.xslt")));
		}
		catch (TransformerConfigurationException e)
		{
			throw new RuntimeException("Build Error", e);
		}
	}
	
	public static String toHTML(String xml) throws TransformerException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		StreamResult result = new StreamResult(baos);
		xmlToHTMLTransformer.transform(new StreamSource(new ByteArrayInputStream(xml.getBytes())), result);
		return baos.toString();
	}
}
