//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.09.26 at 04:34:25 PM PDT 
//

package org.hl7.vmr.r2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import org.hl7.cdsdt.r2.CD;

/**
 * Proposal, e.g., by a CDS system, for establishing the goal specified.
 * 
 * <p>
 * Java class for GoalProposal complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="GoalProposal">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:hl7-org:vmr:r2}GoalBase">
 *       &lt;sequence>
 *         &lt;element name="urgency" type="{urn:hl7-org:cdsdt:r2}CD" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GoalProposal", propOrder = {
  "urgency"
})
public class GoalProposal extends GoalBase {

  protected CD urgency;

  /**
   * Gets the value of the urgency property.
   * 
   * @return possible object is {@link CD }
   * 
   */
  public CD getUrgency() {
    return urgency;
  }

  /**
   * Sets the value of the urgency property.
   * 
   * @param value allowed object is {@link CD }
   * 
   */
  public void setUrgency(CD value) {
    this.urgency = value;
  }

}
