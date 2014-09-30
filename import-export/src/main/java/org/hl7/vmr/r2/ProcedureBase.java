//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.09.26 at 04:34:25 PM PDT 
//

package org.hl7.vmr.r2;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import org.hl7.cdsdt.r2.CD;

/**
 * Abstract base class for a procedure, which is a series of steps taken on a
 * subject to accomplish a clinical goal. Procedures include diagnostic testing,
 * consultations, referrals, nursing procedures, making observations, and other
 * clinical interventions excluding substance administrations.
 * 
 * <p>
 * Java class for ProcedureBase complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="ProcedureBase">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:hl7-org:vmr:r2}ClinicalStatement">
 *       &lt;sequence>
 *         &lt;element name="procedureCode" type="{urn:hl7-org:cdsdt:r2}CD"/>
 *         &lt;element name="procedureMethod" type="{urn:hl7-org:cdsdt:r2}CD" minOccurs="0"/>
 *         &lt;element name="approachBodySite" type="{urn:hl7-org:vmr:r2}BodySite" minOccurs="0"/>
 *         &lt;element name="targetBodySite" type="{urn:hl7-org:vmr:r2}BodySite" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcedureBase", propOrder = {
    "procedureCode", "procedureMethod", "approachBodySite", "targetBodySite"
})
@XmlSeeAlso({
    ProcedureEvent.class, UndeliveredProcedure.class, ScheduledProcedure.class,
    ProcedureProposal.class, ProcedureOrder.class
})
public abstract class ProcedureBase extends ClinicalStatement {

  @XmlElement(required = true)
  protected CD procedureCode;

  protected CD procedureMethod;

  protected BodySite approachBodySite;

  protected List<BodySite> targetBodySite;

  /**
   * Gets the value of the procedureCode property.
   * 
   * @return possible object is {@link CD }
   * 
   */
  public CD getProcedureCode() {
    return procedureCode;
  }

  /**
   * Sets the value of the procedureCode property.
   * 
   * @param value allowed object is {@link CD }
   * 
   */
  public void setProcedureCode(CD value) {
    this.procedureCode = value;
  }

  /**
   * Gets the value of the procedureMethod property.
   * 
   * @return possible object is {@link CD }
   * 
   */
  public CD getProcedureMethod() {
    return procedureMethod;
  }

  /**
   * Sets the value of the procedureMethod property.
   * 
   * @param value allowed object is {@link CD }
   * 
   */
  public void setProcedureMethod(CD value) {
    this.procedureMethod = value;
  }

  /**
   * Gets the value of the approachBodySite property.
   * 
   * @return possible object is {@link BodySite }
   * 
   */
  public BodySite getApproachBodySite() {
    return approachBodySite;
  }

  /**
   * Sets the value of the approachBodySite property.
   * 
   * @param value allowed object is {@link BodySite }
   * 
   */
  public void setApproachBodySite(BodySite value) {
    this.approachBodySite = value;
  }

  /**
   * Gets the value of the targetBodySite property.
   * 
   * <p>
   * This accessor method returns a reference to the live list, not a snapshot.
   * Therefore any modification you make to the returned list will be present
   * inside the JAXB object. This is why there is not a <CODE>set</CODE> method
   * for the targetBodySite property.
   * 
   * <p>
   * For example, to add a new item, do as follows:
   * 
   * <pre>
   * getTargetBodySite().add(newItem);
   * </pre>
   * 
   * 
   * <p>
   * Objects of the following type(s) are allowed in the list {@link BodySite }
   * 
   * 
   */
  public List<BodySite> getTargetBodySite() {
    if (targetBodySite == null) {
      targetBodySite = new ArrayList<BodySite>();
    }
    return this.targetBodySite;
  }

}
