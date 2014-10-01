/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.models.hed;

// TODO: Auto-generated Javadoc
/**
 * Utility interface providing {@link String} constants for importing/exporting
 * HeD models from/to XML.
 *
 * @author bcarlsenca
 */
public interface HeDXmlConstants {

  // version stuff
  /** The Constant SCHEMA_IDENTIFIER. */
  public static final String SCHEMA_IDENTIFIER = "schemaIdentifier";

  /** The Constant SCHEMA_IDENTIFIER_ROOT. */
  public static final String SCHEMA_IDENTIFIER_ROOT =
      "urn:hl7-org:knowledgeartifact:r1";

  /** The Constant SCHEMA_IDENTIFIER_VERSION. */
  public static final String SCHEMA_IDENTIFIER_VERSION = "1.0";

  // tags
  /** The Constant KNOWLEDGE_DOCUMENT. */
  public static final String KNOWLEDGE_DOCUMENT = "knowledgeDocument";

  /** The Constant XMLNS. */
  public static final String XMLNS = "xmlns";

  /** The Constant XMLNS_VMR. */
  public static final String XMLNS_VMR = "xmlns:vmr";

  /** The Constant XMLNS_DT. */
  public static final String XMLNS_DT = "xmlns:dt";

  /** The Constant XMLNS_XML. */
  public static final String XMLNS_XML = "xmlns:xml";

  /** The Constant XMLNS_P1. */
  public static final String XMLNS_P1 = "xmlns:p1";

  /** The Constant XMLNS_XSI. */
  public static final String XMLNS_XSI = "xmlns:xsi";

  /** The Constant XSI_SCHEMA_LOCATION. */
  public static final String XSI_SCHEMA_LOCATION = "xsi:schemaLocation";

  /** The Constant METADATA. */
  public static final String METADATA = "metadata";

  /** The Constant IDENTIFIER. */
  public static final String IDENTIFIER = "identifier";

  /** The Constant IDENTIFIERS. */
  public static final String IDENTIFIERS = "identifiers";

  /** The Constant ROOT. */
  public static final String ROOT = "root";

  /** The Constant ROOT. */
  public static final String VERSION = "version";

  /** The Constant TITLE. */
  public static final String TITLE = "title";

  /** The Constant VALUE. */
  public static final String VALUE = "value";

  /** The Constant EXTERNAL_DATA. */
  public static final String EXTERNAL_DATA = "externalData";

  /** The Constant CONDITIONS. */
  public static final String CONDITIONS = "conditions";

  /** The Constant ACTION_GROUP. */
  public static final String ACTION_GROUP = "actionGroup";

  /** The Constant ARTIFACT_TYPE. */
  public static final String ARTIFACT_TYPE = "artifactType";

  /** The Constant DATA_MODELS. */
  public static final String DATA_MODELS = "dataModels";

  /**  The Constant EVENT_HISTORY. */
  public static final String EVENT_HISTORY = "eventHistory";
  
  /**  The Constant CONTRIBUTIONS. */
  public static final String CONTRIBUTIONS = "contributions";
  
  /**  The Constant PUBLISHERS. */
  public static final String PUBLISHERS = "publishers";

  /**  The Constant USAGE_TERMS. */
  public static final String USAGE_TERMS = "usageTerms";

  /** The Constant KEY_TERM. */
  public static final String KEY_TERM = "keyTerm";

  /** The Constant MODEL_REFERENCE. */
  public static final String MODEL_REFERENCE = "modelReference";

  /** The Constant DESCRIPTION. */
  public static final String DESCRIPTION = "description";

  /** The Constant REFERENCED_MODEL. */
  public static final String REFERENCED_MODEL = "referencedModel";

  
}
