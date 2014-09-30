//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.09.26 at 04:34:14 PM PDT 
//

package org.hl7.knowledgeartifact.r1;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for ContributorType.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * <p>
 * 
 * <pre>
 * &lt;simpleType name="ContributorType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Author"/>
 *     &lt;enumeration value="Editor"/>
 *     &lt;enumeration value="Endorser"/>
 *     &lt;enumeration value="Reviewer"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ContributorType", namespace = "urn:hl7-org:knowledgeartifact:r1")
@XmlEnum
public enum ContributorType {

  @XmlEnumValue("Author")
  AUTHOR("Author"), @XmlEnumValue("Editor")
  EDITOR("Editor"), @XmlEnumValue("Endorser")
  ENDORSER("Endorser"), @XmlEnumValue("Reviewer")
  REVIEWER("Reviewer");
  private final String value;

  ContributorType(String v) {
    value = v;
  }

  public String value() {
    return value;
  }

  public static ContributorType fromValue(String v) {
    for (ContributorType c : ContributorType.values()) {
      if (c.value.equals(v)) {
        return c;
      }
    }
    throw new IllegalArgumentException(v);
  }

}
