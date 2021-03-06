//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.09.30 at 06:15:10 PM PDT 
//


package org.hl7.vmr.r2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import org.hl7.cdsdt.r2.CD;
import org.hl7.cdsdt.r2.INT;
import org.hl7.cdsdt.r2.IVLTS;
import org.hl7.cdsdt.r2.PQ;


/**
 * A clinical proposal for provision of a supply of a medication generally with the intention that it is subsequently consumed by a patient (usually in response to a prescription). 
 * 
 * An proposal for a substance to be dispensed but not administered.  (E.g.,, �naloxone at bedside�).
 * 
 * <p>Java class for SubstanceDispenseProposal complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SubstanceDispenseProposal">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:hl7-org:vmr:r2}SubstanceClinicalStatementBase">
 *       &lt;sequence>
 *         &lt;element name="dispenseType" type="{urn:hl7-org:cdsdt:r2}CD" minOccurs="0"/>
 *         &lt;element name="proposedDispenseTime" type="{urn:hl7-org:cdsdt:r2}IVL_TS" minOccurs="0"/>
 *         &lt;element name="dispenseQuantity" type="{urn:hl7-org:cdsdt:r2}PQ" minOccurs="0"/>
 *         &lt;element name="numberOfFillsAllowed" type="{urn:hl7-org:cdsdt:r2}INT" minOccurs="0"/>
 *         &lt;element name="supplyDuration" type="{urn:hl7-org:cdsdt:r2}PQ" minOccurs="0"/>
 *         &lt;element name="reason" type="{urn:hl7-org:cdsdt:r2}CD" minOccurs="0"/>
 *         &lt;element name="urgency" type="{urn:hl7-org:cdsdt:r2}CD" minOccurs="0"/>
 *         &lt;element name="validityPeriod" type="{urn:hl7-org:cdsdt:r2}IVL_TS" minOccurs="0"/>
 *         &lt;element name="proposalEventTime" type="{urn:hl7-org:cdsdt:r2}IVL_TS" minOccurs="0"/>
 *         &lt;element name="originationMode" type="{urn:hl7-org:cdsdt:r2}CD" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SubstanceDispenseProposal", propOrder = {
    "dispenseType",
    "proposedDispenseTime",
    "dispenseQuantity",
    "numberOfFillsAllowed",
    "supplyDuration",
    "reason",
    "urgency",
    "validityPeriod",
    "proposalEventTime",
    "originationMode"
})
@XmlSeeAlso({
    EnteralFeedingDispenseProposal.class
})
public class SubstanceDispenseProposal
    extends SubstanceClinicalStatementBase
{

    protected CD dispenseType;
    protected IVLTS proposedDispenseTime;
    protected PQ dispenseQuantity;
    protected INT numberOfFillsAllowed;
    protected PQ supplyDuration;
    protected CD reason;
    protected CD urgency;
    protected IVLTS validityPeriod;
    protected IVLTS proposalEventTime;
    protected CD originationMode;

    /**
     * Gets the value of the dispenseType property.
     * 
     * @return
     *     possible object is
     *     {@link CD }
     *     
     */
    public CD getDispenseType() {
        return dispenseType;
    }

    /**
     * Sets the value of the dispenseType property.
     * 
     * @param value
     *     allowed object is
     *     {@link CD }
     *     
     */
    public void setDispenseType(CD value) {
        this.dispenseType = value;
    }

    /**
     * Gets the value of the proposedDispenseTime property.
     * 
     * @return
     *     possible object is
     *     {@link IVLTS }
     *     
     */
    public IVLTS getProposedDispenseTime() {
        return proposedDispenseTime;
    }

    /**
     * Sets the value of the proposedDispenseTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link IVLTS }
     *     
     */
    public void setProposedDispenseTime(IVLTS value) {
        this.proposedDispenseTime = value;
    }

    /**
     * Gets the value of the dispenseQuantity property.
     * 
     * @return
     *     possible object is
     *     {@link PQ }
     *     
     */
    public PQ getDispenseQuantity() {
        return dispenseQuantity;
    }

    /**
     * Sets the value of the dispenseQuantity property.
     * 
     * @param value
     *     allowed object is
     *     {@link PQ }
     *     
     */
    public void setDispenseQuantity(PQ value) {
        this.dispenseQuantity = value;
    }

    /**
     * Gets the value of the numberOfFillsAllowed property.
     * 
     * @return
     *     possible object is
     *     {@link INT }
     *     
     */
    public INT getNumberOfFillsAllowed() {
        return numberOfFillsAllowed;
    }

    /**
     * Sets the value of the numberOfFillsAllowed property.
     * 
     * @param value
     *     allowed object is
     *     {@link INT }
     *     
     */
    public void setNumberOfFillsAllowed(INT value) {
        this.numberOfFillsAllowed = value;
    }

    /**
     * Gets the value of the supplyDuration property.
     * 
     * @return
     *     possible object is
     *     {@link PQ }
     *     
     */
    public PQ getSupplyDuration() {
        return supplyDuration;
    }

    /**
     * Sets the value of the supplyDuration property.
     * 
     * @param value
     *     allowed object is
     *     {@link PQ }
     *     
     */
    public void setSupplyDuration(PQ value) {
        this.supplyDuration = value;
    }

    /**
     * Gets the value of the reason property.
     * 
     * @return
     *     possible object is
     *     {@link CD }
     *     
     */
    public CD getReason() {
        return reason;
    }

    /**
     * Sets the value of the reason property.
     * 
     * @param value
     *     allowed object is
     *     {@link CD }
     *     
     */
    public void setReason(CD value) {
        this.reason = value;
    }

    /**
     * Gets the value of the urgency property.
     * 
     * @return
     *     possible object is
     *     {@link CD }
     *     
     */
    public CD getUrgency() {
        return urgency;
    }

    /**
     * Sets the value of the urgency property.
     * 
     * @param value
     *     allowed object is
     *     {@link CD }
     *     
     */
    public void setUrgency(CD value) {
        this.urgency = value;
    }

    /**
     * Gets the value of the validityPeriod property.
     * 
     * @return
     *     possible object is
     *     {@link IVLTS }
     *     
     */
    public IVLTS getValidityPeriod() {
        return validityPeriod;
    }

    /**
     * Sets the value of the validityPeriod property.
     * 
     * @param value
     *     allowed object is
     *     {@link IVLTS }
     *     
     */
    public void setValidityPeriod(IVLTS value) {
        this.validityPeriod = value;
    }

    /**
     * Gets the value of the proposalEventTime property.
     * 
     * @return
     *     possible object is
     *     {@link IVLTS }
     *     
     */
    public IVLTS getProposalEventTime() {
        return proposalEventTime;
    }

    /**
     * Sets the value of the proposalEventTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link IVLTS }
     *     
     */
    public void setProposalEventTime(IVLTS value) {
        this.proposalEventTime = value;
    }

    /**
     * Gets the value of the originationMode property.
     * 
     * @return
     *     possible object is
     *     {@link CD }
     *     
     */
    public CD getOriginationMode() {
        return originationMode;
    }

    /**
     * Sets the value of the originationMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link CD }
     *     
     */
    public void setOriginationMode(CD value) {
        this.originationMode = value;
    }

}
