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
import org.hl7.cdsdt.r2.IVLPQ;

/**
 * A clinical proposal for dispensing an enteral feeding product. That is, the
 * product is to be dispensed but not administered to the patient.
 * 
 * <p>
 * Java class for EnteralFeedingDispenseProposal complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="EnteralFeedingDispenseProposal">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:hl7-org:vmr:r2}SubstanceDispenseProposal">
 *       &lt;sequence>
 *         &lt;element name="caloricDensity" type="{urn:hl7-org:cdsdt:r2}IVL_PQ" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EnteralFeedingDispenseProposal", propOrder = {
  "caloricDensity"
})
public class EnteralFeedingDispenseProposal extends SubstanceDispenseProposal {

  protected IVLPQ caloricDensity;

  /**
   * Gets the value of the caloricDensity property.
   * 
   * @return possible object is {@link IVLPQ }
   * 
   */
  public IVLPQ getCaloricDensity() {
    return caloricDensity;
  }

  /**
   * Sets the value of the caloricDensity property.
   * 
   * @param value allowed object is {@link IVLPQ }
   * 
   */
  public void setCaloricDensity(IVLPQ value) {
    this.caloricDensity = value;
  }

}
