//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.09.26 at 04:34:25 PM PDT 
//

package org.hl7.cdsdt.r2;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Mailing and home or office addresses.
 * 
 * AD is primarily used to communicate data that will allow printing mail
 * labels, or that will allow a person to physically visit that address. The
 * postal address datatype is not supposed to be a container for additional
 * information that might be useful for finding geographic locations (e.g., GPS
 * coordinates) or for performing epidemiological studies. Such additional
 * information should be captured by other, more appropriate data structures.
 * 
 * Addresses are essentially sequences of address parts, but add a "use" code
 * and a valid time range for information about if and when the address can be
 * used for a given purpose.
 * 
 * <p>
 * Java class for AD complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="AD">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:hl7-org:cdsdt:r2}ANY">
 *       &lt;sequence>
 *         &lt;element name="part" type="{urn:hl7-org:cdsdt:r2}ADXP" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="use" type="{urn:hl7-org:cdsdt:r2}set_PostalAddressUse" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AD", propOrder = {
  "part"
})
public class AD extends ANY {

  @XmlElement(required = true)
  protected List<ADXP> part;

  @XmlAttribute(name = "use")
  protected List<PostalAddressUse> use;

  /**
   * Gets the value of the part property.
   * 
   * <p>
   * This accessor method returns a reference to the live list, not a snapshot.
   * Therefore any modification you make to the returned list will be present
   * inside the JAXB object. This is why there is not a <CODE>set</CODE> method
   * for the part property.
   * 
   * <p>
   * For example, to add a new item, do as follows:
   * 
   * <pre>
   * getPart().add(newItem);
   * </pre>
   * 
   * 
   * <p>
   * Objects of the following type(s) are allowed in the list {@link ADXP }
   * 
   * 
   */
  public List<ADXP> getPart() {
    if (part == null) {
      part = new ArrayList<ADXP>();
    }
    return this.part;
  }

  /**
   * Gets the value of the use property.
   * 
   * <p>
   * This accessor method returns a reference to the live list, not a snapshot.
   * Therefore any modification you make to the returned list will be present
   * inside the JAXB object. This is why there is not a <CODE>set</CODE> method
   * for the use property.
   * 
   * <p>
   * For example, to add a new item, do as follows:
   * 
   * <pre>
   * getUse().add(newItem);
   * </pre>
   * 
   * 
   * <p>
   * Objects of the following type(s) are allowed in the list
   * {@link PostalAddressUse }
   * 
   * 
   */
  public List<PostalAddressUse> getUse() {
    if (use == null) {
      use = new ArrayList<PostalAddressUse>();
    }
    return this.use;
  }

}
