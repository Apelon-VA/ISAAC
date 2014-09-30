//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.09.26 at 04:34:12 PM PDT 
//

package org.hl7.knowledgeartifact.r1;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for PrecheckBehaviorType.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * <p>
 * 
 * <pre>
 * &lt;simpleType name="PrecheckBehaviorType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Yes"/>
 *     &lt;enumeration value="No"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "PrecheckBehaviorType", namespace = "urn:hl7-org:knowledgeartifact:r1")
@XmlEnum
public enum PrecheckBehaviorType {

  /**
   * An action with this behavior is one of the most frequent actions that is,
   * or should be, included by an end user, for the particular context in which
   * the action occurs. The system displaying the action to the end user should
   * consider "pre-checking" such an action as a convenience for the user.
   * 
   */
  @XmlEnumValue("Yes")
  YES("Yes"),

  /**
   * An action with this behavior is one of the less frequent actions included
   * by the end user, for the particular context in which the action occurs. The
   * system displaying the actions to the end user would typically not
   * "pre-check" such an action.
   * 
   */
  @XmlEnumValue("No")
  NO("No");
  private final String value;

  PrecheckBehaviorType(String v) {
    value = v;
  }

  public String value() {
    return value;
  }

  public static PrecheckBehaviorType fromValue(String v) {
    for (PrecheckBehaviorType c : PrecheckBehaviorType.values()) {
      if (c.value.equals(v)) {
        return c;
      }
    }
    throw new IllegalArgumentException(v);
  }

}
