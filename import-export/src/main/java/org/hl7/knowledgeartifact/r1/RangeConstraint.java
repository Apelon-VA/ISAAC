//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.09.30 at 06:15:10 PM PDT 
//


package org.hl7.knowledgeartifact.r1;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RangeConstraint complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RangeConstraint">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="constraintType">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="value" type="{urn:hl7-org:knowledgeartifact:r1}RangeConstraintType" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RangeConstraint", propOrder = {
    "constraintType"
})
@XmlSeeAlso({
    ExpressionConstraint.class,
    ListConstraint.class,
    ValueSetConstraint.class
})
public abstract class RangeConstraint {

    @XmlElement(required = true)
    protected RangeConstraint.ConstraintType constraintType;

    /**
     * Gets the value of the constraintType property.
     * 
     * @return
     *     possible object is
     *     {@link RangeConstraint.ConstraintType }
     *     
     */
    public RangeConstraint.ConstraintType getConstraintType() {
        return constraintType;
    }

    /**
     * Sets the value of the constraintType property.
     * 
     * @param value
     *     allowed object is
     *     {@link RangeConstraint.ConstraintType }
     *     
     */
    public void setConstraintType(RangeConstraint.ConstraintType value) {
        this.constraintType = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="value" type="{urn:hl7-org:knowledgeartifact:r1}RangeConstraintType" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class ConstraintType {

        @XmlAttribute(name = "value")
        protected RangeConstraintType value;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link RangeConstraintType }
         *     
         */
        public RangeConstraintType getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link RangeConstraintType }
         *     
         */
        public void setValue(RangeConstraintType value) {
            this.value = value;
        }

    }

}