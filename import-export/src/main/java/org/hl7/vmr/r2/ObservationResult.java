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

/**
 * The findings from an observation.
 * 
 * <p>
 * Java class for ObservationResult complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="ObservationResult">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:hl7-org:vmr:r2}ObservationBase">
 *       &lt;sequence>
 *         &lt;element name="observationValue" type="{urn:hl7-org:vmr:r2}Value" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ObservationResult", propOrder = {
  "observationValue"
})
public class ObservationResult extends ObservationBase {

  protected Value observationValue;

  /**
   * Gets the value of the observationValue property.
   * 
   * @return possible object is {@link Value }
   * 
   */
  public Value getObservationValue() {
    return observationValue;
  }

  /**
   * Sets the value of the observationValue property.
   * 
   * @param value allowed object is {@link Value }
   * 
   */
  public void setObservationValue(Value value) {
    this.observationValue = value;
  }

}
