//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.09.26 at 04:33:59 PM PDT 
//

package org.hl7.cdsoutputspecification.r2;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import org.hl7.vmr.r2.CodedIdentifier;

/**
 * Abstract base class specifying the output to be provided by a specific CDS
 * use case.
 * 
 * As a specific example, a CDSOutputSpecification may be used to specify
 * details on the CDS output that will be returned by a CDS guidance service
 * compliant with the HL7 Decision Support Service standard. Specifically, this
 * type of specification can be encapsulated within the �CDS output
 * specification� section of a Decision Support Service�s specification of
 * knowledge module evaluation result semantics. Further information regarding
 * this type of use case can be found in the HL7 Decision Support Service
 * specification and the HL7 Decision Support Service Implementation Guide.
 * 
 * <p>
 * Java class for CDSOutputSpecification complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="CDSOutputSpecification">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cdsOutputTemplate" type="{urn:hl7-org:vmr:r2}CodedIdentifier" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CDSOutputSpecification", propOrder = {
  "cdsOutputTemplate"
})
@XmlSeeAlso({
    CDSOutputAsDataTypeSpecification.class,
    CDSOutputAsStringNameValuePairSpecification.class,
    CDSOutputAsVMRSpecification.class
})
public abstract class CDSOutputSpecification {

  protected List<CodedIdentifier> cdsOutputTemplate;

  /**
   * Gets the value of the cdsOutputTemplate property.
   * 
   * <p>
   * This accessor method returns a reference to the live list, not a snapshot.
   * Therefore any modification you make to the returned list will be present
   * inside the JAXB object. This is why there is not a <CODE>set</CODE> method
   * for the cdsOutputTemplate property.
   * 
   * <p>
   * For example, to add a new item, do as follows:
   * 
   * <pre>
   * getCdsOutputTemplate().add(newItem);
   * </pre>
   * 
   * 
   * <p>
   * Objects of the following type(s) are allowed in the list
   * {@link CodedIdentifier }
   * 
   * 
   */
  public List<CodedIdentifier> getCdsOutputTemplate() {
    if (cdsOutputTemplate == null) {
      cdsOutputTemplate = new ArrayList<CodedIdentifier>();
    }
    return this.cdsOutputTemplate;
  }

}