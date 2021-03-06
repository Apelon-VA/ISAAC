//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.09.30 at 06:15:10 PM PDT 
//


package org.hl7.vmr.r2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import org.hl7.cdsdt.r2.IVLPQ;


/**
 * A class representing enteral nutrition proposals for the delivery of enteral-fed substances (eg, Nutren, Ensure, RenalCal) for patients who are unable to consume diets orally; enteral feedings can be delivered to the stomach or varying parts of the small intestines using a variety of tube placement methods, depending on the clinical scenario. For instance, Nutren via nasogastric tube, 20 ml/hour, increase by 20 ml every 4 hours, goal of 75 ml/hour, water flushes 125 ml every shift.
 * 
 * <p>Java class for EnteralFeedingProposal complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EnteralFeedingProposal">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:hl7-org:vmr:r2}SubstanceAdministrationProposal">
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
@XmlType(name = "EnteralFeedingProposal", propOrder = {
    "caloricDensity"
})
public class EnteralFeedingProposal
    extends SubstanceAdministrationProposal
{

    protected IVLPQ caloricDensity;

    /**
     * Gets the value of the caloricDensity property.
     * 
     * @return
     *     possible object is
     *     {@link IVLPQ }
     *     
     */
    public IVLPQ getCaloricDensity() {
        return caloricDensity;
    }

    /**
     * Sets the value of the caloricDensity property.
     * 
     * @param value
     *     allowed object is
     *     {@link IVLPQ }
     *     
     */
    public void setCaloricDensity(IVLPQ value) {
        this.caloricDensity = value;
    }

}
