//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.09.26 at 04:34:02 PM PDT 
//

package org.hl7.kaoutput.r1;

import javax.xml.bind.annotation.XmlRegistry;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the org.hl7.kaoutput.r1 package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the
 * Java representation for XML content. The Java representation of XML content
 * can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory
 * methods for each of these are provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

  /**
   * Create a new ObjectFactory that can be used to create new instances of
   * schema derived classes for package: org.hl7.kaoutput.r1
   * 
   */
  public ObjectFactory() {
  }

  /**
   * Create an instance of {@link CDSExecutionMessage }
   * 
   */
  public CDSExecutionMessage createCDSExecutionMessage() {
    return new CDSExecutionMessage();
  }

  /**
   * Create an instance of {@link CDSExecutionMessage.Reason }
   * 
   */
  public CDSExecutionMessage.Reason createCDSExecutionMessageReason() {
    return new CDSExecutionMessage.Reason();
  }

  /**
   * Create an instance of {@link CDSExecutionMessage.Level }
   * 
   */
  public CDSExecutionMessage.Level createCDSExecutionMessageLevel() {
    return new CDSExecutionMessage.Level();
  }

  /**
   * Create an instance of {@link CDSExecutionMessage.SourceComponentType }
   * 
   */
  public CDSExecutionMessage.SourceComponentType createCDSExecutionMessageSourceComponentType() {
    return new CDSExecutionMessage.SourceComponentType();
  }

}