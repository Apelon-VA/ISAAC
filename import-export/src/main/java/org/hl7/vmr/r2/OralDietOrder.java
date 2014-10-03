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
import org.hl7.cdsdt.r2.IVLTS;

/**
 * A class representing a wide variety of allowable types of meals and/or
 * specification of meal and/or nutrient restrictions for an individual patient,
 * based on the patient's clinical condition.
 * 
 * Includes diet- and nutrition-related orders for a patient/resident including
 * orders for oral diet, either general or therapeutic (medical) nutritional
 * supplements.
 * 
 * <p>
 * Java class for OralDietOrder complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="OralDietOrder">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:hl7-org:vmr:r2}OralDietBase">
 *       &lt;sequence>
 *         &lt;element name="dietEffectiveTime" type="{urn:hl7-org:cdsdt:r2}IVL_TS" minOccurs="0"/>
 *         &lt;element name="orderEventTime" type="{urn:hl7-org:cdsdt:r2}IVL_TS" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OralDietOrder", propOrder = {
    "dietEffectiveTime", "orderEventTime"
})
public class OralDietOrder extends OralDietBase {

  protected IVLTS dietEffectiveTime;

  protected IVLTS orderEventTime;

  /**
   * Gets the value of the dietEffectiveTime property.
   * 
   * @return possible object is {@link IVLTS }
   * 
   */
  public IVLTS getDietEffectiveTime() {
    return dietEffectiveTime;
  }

  /**
   * Sets the value of the dietEffectiveTime property.
   * 
   * @param value allowed object is {@link IVLTS }
   * 
   */
  public void setDietEffectiveTime(IVLTS value) {
    this.dietEffectiveTime = value;
  }

  /**
   * Gets the value of the orderEventTime property.
   * 
   * @return possible object is {@link IVLTS }
   * 
   */
  public IVLTS getOrderEventTime() {
    return orderEventTime;
  }

  /**
   * Sets the value of the orderEventTime property.
   * 
   * @param value allowed object is {@link IVLTS }
   * 
   */
  public void setOrderEventTime(IVLTS value) {
    this.orderEventTime = value;
  }

}