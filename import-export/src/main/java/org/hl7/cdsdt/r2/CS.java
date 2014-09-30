//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.09.26 at 04:34:25 PM PDT 
//

package org.hl7.cdsdt.r2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * Coded data in its simplest form, where only the code is not predetermined.
 * 
 * The code system and code system version are implied and fixed by the context
 * in which the CS value occurs.
 * 
 * Due to its highly restricted functionality, CS SHALL only be used for simple
 * structural attributes with highly controlled and stable terminologies where:
 * - all codes come from a single code system - codes are not reused if their
 * concept is deprecated - the publication and extensibility properties of the
 * code system are well described and understood
 * 
 * <p>
 * Java class for CS complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="CS">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:hl7-org:cdsdt:r2}ANY">
 *       &lt;attribute name="code" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CS")
public class CS extends ANY {

  @XmlAttribute(name = "code", required = true)
  protected String code;

  /**
   * Gets the value of the code property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getCode() {
    return code;
  }

  /**
   * Sets the value of the code property.
   * 
   * @param value allowed object is {@link String }
   * 
   */
  public void setCode(String value) {
    this.code = value;
  }

}
