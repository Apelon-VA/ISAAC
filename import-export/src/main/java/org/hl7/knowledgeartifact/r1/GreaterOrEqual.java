//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.09.26 at 04:34:23 PM PDT 
//

package org.hl7.knowledgeartifact.r1;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * The GreaterOrEqual operator returns true if the first argument is greater
 * than or equal to the second argument.
 * 
 * If either argument is null, the result is null.
 * 
 * The GreaterOrEqual operator is defined for the Integer, Real, String,
 * Timestamp, and PhysicalQuantity types.
 * 
 * <p>
 * Java class for GreaterOrEqual complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="GreaterOrEqual">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:hl7-org:knowledgeartifact:r1}BinaryExpression">
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GreaterOrEqual")
public class GreaterOrEqual extends BinaryExpression {

}