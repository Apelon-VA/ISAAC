//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.09.30 at 06:15:10 PM PDT 
//


package org.hl7.knowledgeartifact.r1;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for EventType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="EventType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="DataEvent"/>
 *     &lt;enumeration value="PeriodicEvent"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "EventType")
@XmlEnum
public enum EventType {


    /**
     * An event in which a data item is created, removed, updated, or accessed.
     * 					Expression is expected to be an ExpressionRef
     * 						that references an ExpressionDef in ExternalData that contains a
     * 						Request with a triggerType attribute specified.
     * 					
     * 
     */
    @XmlEnumValue("DataEvent")
    DATA_EVENT("DataEvent"),

    /**
     * A time-based event which occurs at the specified period. Expression is expected to be a Period literal
     * 						expression specifying the period on which the
     * 						event should be repeated
     * 					
     * 
     */
    @XmlEnumValue("PeriodicEvent")
    PERIODIC_EVENT("PeriodicEvent");
    private final String value;

    EventType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EventType fromValue(String v) {
        for (EventType c: EventType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
