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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.w3._1999.xhtml.Div;


/**
 * An Inline Resource consists of both the resource
 * 				reference information and the actual resource content/payload to be
 * 				inserted inline. The content of the document must be represented in
 * 				valid xhtml format within the content/div node.
 * 			
 * 
 * <p>Java class for InlineResource complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="InlineResource">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:hl7-org:knowledgeartifact:r1}KnowledgeResource">
 *       &lt;sequence>
 *         &lt;element name="content" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element ref="{http://www.w3.org/1999/xhtml}div"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InlineResource", propOrder = {
    "content"
})
public class InlineResource
    extends KnowledgeResource
{

    protected InlineResource.Content content;

    /**
     * Gets the value of the content property.
     * 
     * @return
     *     possible object is
     *     {@link InlineResource.Content }
     *     
     */
    public InlineResource.Content getContent() {
        return content;
    }

    /**
     * Sets the value of the content property.
     * 
     * @param value
     *     allowed object is
     *     {@link InlineResource.Content }
     *     
     */
    public void setContent(InlineResource.Content value) {
        this.content = value;
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
     *       &lt;sequence>
     *         &lt;element ref="{http://www.w3.org/1999/xhtml}div"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "div"
    })
    public static class Content {

        @XmlElement(namespace = "http://www.w3.org/1999/xhtml", required = true)
        protected Div div;

        /**
         * Gets the value of the div property.
         * 
         * @return
         *     possible object is
         *     {@link Div }
         *     
         */
        public Div getDiv() {
            return div;
        }

        /**
         * Sets the value of the div property.
         * 
         * @param value
         *     allowed object is
         *     {@link Div }
         *     
         */
        public void setDiv(Div value) {
            this.div = value;
        }

    }

}
