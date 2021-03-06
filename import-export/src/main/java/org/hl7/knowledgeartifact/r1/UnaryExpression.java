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
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * The UnaryExpression type defines the abstract base type for all expressions that take a single argument.
 * 
 * <p>Java class for UnaryExpression complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UnaryExpression">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:hl7-org:knowledgeartifact:r1}Expression">
 *       &lt;sequence>
 *         &lt;element name="operand" type="{urn:hl7-org:knowledgeartifact:r1}Expression"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UnaryExpression", propOrder = {
    "operand"
})
@XmlSeeAlso({
    DateOf.class,
    Abs.class,
    Truncate.class,
    Ceiling.class,
    IsEmpty.class,
    Collapse.class,
    Floor.class,
    As.class,
    Upper.class,
    Convert.class,
    Negate.class,
    Ln.class,
    IsNotEmpty.class,
    Succ.class,
    IsNull.class,
    Pred.class,
    Length.class,
    End.class,
    TimeOf.class,
    Begin.class,
    Lower.class,
    Is.class,
    InValueSet.class,
    Not.class,
    Expand.class
})
public abstract class UnaryExpression
    extends Expression
{

    @XmlElement(required = true)
    protected Expression operand;

    /**
     * Gets the value of the operand property.
     * 
     * @return
     *     possible object is
     *     {@link Expression }
     *     
     */
    public Expression getOperand() {
        return operand;
    }

    /**
     * Sets the value of the operand property.
     * 
     * @param value
     *     allowed object is
     *     {@link Expression }
     *     
     */
    public void setOperand(Expression value) {
        this.operand = value;
    }

}
