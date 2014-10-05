//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.09.30 at 06:15:10 PM PDT 
//


package org.hl7.vmr.r2;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * The findings from an observation represented as a composition of child observation results.  CompositeObservationResult may consist of two or more ObservationResults, one or more CompositeObservationResults, or two or more of a combination of ObservationResult and CompositeObservationResult. E.g., Complete Blood Count, Basic Chemistry Panel. A 
 * 
 * <p>Java class for CompositeObservationResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CompositeObservationResult">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:hl7-org:vmr:r2}ObservationBase">
 *       &lt;sequence>
 *         &lt;element name="observationResult" type="{urn:hl7-org:vmr:r2}ObservationBase" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CompositeObservationResult", propOrder = {
    "observationResult"
})
public class CompositeObservationResult
    extends ObservationBase
{

    @XmlElement(required = true)
    protected List<ObservationBase> observationResult;

    /**
     * Gets the value of the observationResult property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the observationResult property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getObservationResult().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ObservationBase }
     * 
     * 
     */
    public List<ObservationBase> getObservationResult() {
        if (observationResult == null) {
            observationResult = new ArrayList<ObservationBase>();
        }
        return this.observationResult;
    }

}
