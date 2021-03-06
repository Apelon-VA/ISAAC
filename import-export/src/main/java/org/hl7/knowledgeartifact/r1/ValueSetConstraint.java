//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.09.30 at 06:15:10 PM PDT 
//


package org.hl7.knowledgeartifact.r1;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * A constraint specifying that the value is an item from the
 * 				value set specified. This constraint applies to list type constraints only.
 * 				The response data type for this DocumentationItem will be Code.
 * 
 * <p>Java class for ValueSetConstraint complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ValueSetConstraint">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:hl7-org:knowledgeartifact:r1}RangeConstraint">
 *       &lt;sequence>
 *         &lt;element name="valueSet" type="{urn:hl7-org:knowledgeartifact:r1}ValueSet"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ValueSetConstraint", propOrder = {
    "valueSet"
})
public class ValueSetConstraint
    extends RangeConstraint
{

    @XmlElement(required = true)
    protected ValueSet valueSet;

    /**
     * Gets the value of the valueSet property.
     * 
     * @return
     *     possible object is
     *     {@link ValueSet }
     *     
     */
    public ValueSet getValueSet() {
        return valueSet;
    }

    /**
     * Sets the value of the valueSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link ValueSet }
     *     
     */
    public void setValueSet(ValueSet value) {
        this.valueSet = value;
    }

}
