//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.09.30 at 06:15:10 PM PDT 
//


package org.hl7.vmr.r2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.hl7.cdsdt.r2.IVLTS;


/**
 * EncounterEvent is the record of an interaction between an EvaluatedPerson and the healthcare system.  It can be used to group observations and interventions performed during that interaction, through the use of relatedClinicalStatements.
 * 
 * <p>Java class for EncounterEvent complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EncounterEvent">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:hl7-org:vmr:r2}EncounterBase">
 *       &lt;sequence>
 *         &lt;element name="encounterEventTime" type="{urn:hl7-org:cdsdt:r2}IVL_TS"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EncounterEvent", propOrder = {
    "encounterEventTime"
})
public class EncounterEvent
    extends EncounterBase
{

    @XmlElement(required = true)
    protected IVLTS encounterEventTime;

    /**
     * Gets the value of the encounterEventTime property.
     * 
     * @return
     *     possible object is
     *     {@link IVLTS }
     *     
     */
    public IVLTS getEncounterEventTime() {
        return encounterEventTime;
    }

    /**
     * Sets the value of the encounterEventTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link IVLTS }
     *     
     */
    public void setEncounterEventTime(IVLTS value) {
        this.encounterEventTime = value;
    }

}
