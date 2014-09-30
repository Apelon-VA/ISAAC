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
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import org.hl7.cdsdt.r2.CD;

/**
 * Abstract base class for giving a material of a particular constitution to a
 * person to enable a clinical effect.
 * 
 * <p>
 * Java class for SubstanceClinicalStatementBase complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="SubstanceClinicalStatementBase">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:hl7-org:vmr:r2}ClinicalStatement">
 *       &lt;sequence>
 *         &lt;element name="substance" type="{urn:hl7-org:vmr:r2}AdministrableSubstance" minOccurs="0"/>
 *         &lt;element name="substanceAdministrationGeneralPurpose" type="{urn:hl7-org:cdsdt:r2}CD" minOccurs="0"/>
 *         &lt;element name="substitutionType" type="{urn:hl7-org:cdsdt:r2}CD" minOccurs="0"/>
 *         &lt;element name="substitutionReason" type="{urn:hl7-org:cdsdt:r2}CD" minOccurs="0"/>
 *         &lt;element name="dose" type="{urn:hl7-org:vmr:r2}Dose" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SubstanceClinicalStatementBase", propOrder = {
    "substance", "substanceAdministrationGeneralPurpose", "substitutionType",
    "substitutionReason", "dose"
})
@XmlSeeAlso({
    SubstanceDispenseEvent.class, SubstanceDispenseProposal.class,
    SubstanceDispenseOrder.class, SubstanceAdministrationEvent.class,
    SubstanceAdministrationOrder.class, SubstanceAdministrationProposal.class,
    UndeliveredSubstanceAdministration.class
})
public abstract class SubstanceClinicalStatementBase extends ClinicalStatement {

  protected AdministrableSubstance substance;

  protected CD substanceAdministrationGeneralPurpose;

  protected CD substitutionType;

  protected CD substitutionReason;

  protected List<Dose> dose;

  /**
   * Gets the value of the substance property.
   * 
   * @return possible object is {@link AdministrableSubstance }
   * 
   */
  public AdministrableSubstance getSubstance() {
    return substance;
  }

  /**
   * Sets the value of the substance property.
   * 
   * @param value allowed object is {@link AdministrableSubstance }
   * 
   */
  public void setSubstance(AdministrableSubstance value) {
    this.substance = value;
  }

  /**
   * Gets the value of the substanceAdministrationGeneralPurpose property.
   * 
   * @return possible object is {@link CD }
   * 
   */
  public CD getSubstanceAdministrationGeneralPurpose() {
    return substanceAdministrationGeneralPurpose;
  }

  /**
   * Sets the value of the substanceAdministrationGeneralPurpose property.
   * 
   * @param value allowed object is {@link CD }
   * 
   */
  public void setSubstanceAdministrationGeneralPurpose(CD value) {
    this.substanceAdministrationGeneralPurpose = value;
  }

  /**
   * Gets the value of the substitutionType property.
   * 
   * @return possible object is {@link CD }
   * 
   */
  public CD getSubstitutionType() {
    return substitutionType;
  }

  /**
   * Sets the value of the substitutionType property.
   * 
   * @param value allowed object is {@link CD }
   * 
   */
  public void setSubstitutionType(CD value) {
    this.substitutionType = value;
  }

  /**
   * Gets the value of the substitutionReason property.
   * 
   * @return possible object is {@link CD }
   * 
   */
  public CD getSubstitutionReason() {
    return substitutionReason;
  }

  /**
   * Sets the value of the substitutionReason property.
   * 
   * @param value allowed object is {@link CD }
   * 
   */
  public void setSubstitutionReason(CD value) {
    this.substitutionReason = value;
  }

  /**
   * Gets the value of the dose property.
   * 
   * <p>
   * This accessor method returns a reference to the live list, not a snapshot.
   * Therefore any modification you make to the returned list will be present
   * inside the JAXB object. This is why there is not a <CODE>set</CODE> method
   * for the dose property.
   * 
   * <p>
   * For example, to add a new item, do as follows:
   * 
   * <pre>
   * getDose().add(newItem);
   * </pre>
   * 
   * 
   * <p>
   * Objects of the following type(s) are allowed in the list {@link Dose }
   * 
   * 
   */
  public List<Dose> getDose() {
    if (dose == null) {
      dose = new ArrayList<Dose>();
    }
    return this.dose;
  }

}
