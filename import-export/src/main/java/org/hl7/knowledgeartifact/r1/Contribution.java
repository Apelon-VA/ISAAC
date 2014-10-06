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
import javax.xml.bind.annotation.XmlType;


/**
 * A contribution is made by a specific contributor
 * 				(organization, person, etc.), and was made in a particular way, as
 * 				specified by the contributor's role. For example, a contributor may
 * 				have been an author, or may have been a reviewer.
 * 			
 * 
 * <p>Java class for Contribution complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Contribution">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="contributor" type="{urn:hl7-org:knowledgeartifact:r1}Party"/>
 *         &lt;element name="role">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="value" type="{urn:hl7-org:knowledgeartifact:r1}ContributorType" />
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
@XmlType(name = "Contribution", propOrder = {
    "contributor",
    "role"
})
public class Contribution {

    @XmlElement(required = true)
    protected Party contributor;
    @XmlElement(required = true)
    protected Contribution.Role role;

    /**
     * Gets the value of the contributor property.
     * 
     * @return
     *     possible object is
     *     {@link Party }
     *     
     */
    public Party getContributor() {
        return contributor;
    }

    /**
     * Sets the value of the contributor property.
     * 
     * @param value
     *     allowed object is
     *     {@link Party }
     *     
     */
    public void setContributor(Party value) {
        this.contributor = value;
    }

    /**
     * Gets the value of the role property.
     * 
     * @return
     *     possible object is
     *     {@link Contribution.Role }
     *     
     */
    public Contribution.Role getRole() {
        return role;
    }

    /**
     * Sets the value of the role property.
     * 
     * @param value
     *     allowed object is
     *     {@link Contribution.Role }
     *     
     */
    public void setRole(Contribution.Role value) {
        this.role = value;
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
     *       &lt;attribute name="value" type="{urn:hl7-org:knowledgeartifact:r1}ContributorType" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Role {

        @XmlAttribute(name = "value")
        protected ContributorType value;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link ContributorType }
         *     
         */
        public ContributorType getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link ContributorType }
         *     
         */
        public void setValue(ContributorType value) {
            this.value = value;
        }

    }

}