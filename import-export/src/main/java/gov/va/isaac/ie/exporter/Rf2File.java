/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.va.isaac.ie.exporter;

/**
 * The Class Rf2File contains enumerations for the different types of RF2
 * release files.
 *
 * @see <a href="http://www.snomed.org/tig?t=rf2_title">IHTSDO Technical
 *      Implementation Guide - Release Format 2</a>
 *
 */
public class Rf2File {
  // ~--- enums ---------------------------------------------------------------

  /**
   * The Enum ConceptsFileFields represents the fields needed in the concepts
   * file.
   */
  public enum ConceptsFileFields {

    /**
     * The concept id field.
     */
    ID("id", "\t"),
    /**
     * The effective time field.
     */
    EFFECTIVE_TIME("effectiveTime", "\t"),
    /**
     * The active status field.
     */
    ACTIVE("active", "\t"),
    /**
     * The module id field.
     */
    MODULE_ID("moduleId", "\t"),
    /**
     * The definition status id field.
     */
    DEFINITION_STATUS_ID("definitionStatusId", "\r\n");
    public final String headerText;

    public final String seperator;

    // ~--- constructors -----------------------------------------------------
    /**
     * Instantiates a new concepts file fields.
     *
     * @param headerText the text to display in the header
     * @param seperator the separator to use for separating the field
     */
    private ConceptsFileFields(String headerText, String seperator) {
      this.headerText = headerText;
      this.seperator = seperator;
    }
  }

  /**
   * The Enum DescriptionsFileFields represents the fields needed in the
   * descriptions file.
   */
  public enum DescriptionsFileFields {

    /**
     * The description id field.
     */
    ID("id", "\t"),
    /**
     * The effective time field.
     */
    EFFECTIVE_TIME("effectiveTime", "\t"),
    /**
     * The active status field.
     */
    ACTIVE("active", "\t"),
    /**
     * The module id field.
     */
    MODULE_ID("moduleId", "\t"),
    /**
     * The concept id field.
     */
    CONCEPT_ID("conceptId", "\t"),
    /**
     * The language code field.
     */
    LANGUAGE_CODE("languageCode", "\t"),
    /**
     * The description type id field.
     */
    TYPE_ID("typeId", "\t"),
    /**
     * The description text field.
     */
    TERM("term", "\t"),
    /**
     * The case significance id field.
     */
    CASE_SIGNIFICANCE_ID("caseSignificanceId", "\r\n");
    public final String headerText;

    public final String seperator;

    // ~--- constructors -----------------------------------------------------
    /**
     * Instantiates a new descriptions file fields.
     *
     * @param headerText the text to display in the header
     * @param seperator the separator to use for separating the field
     */
    private DescriptionsFileFields(String headerText, String seperator) {
      this.headerText = headerText;
      this.seperator = seperator;
    }
  }

  /**
   * The Enum IdentifiersFileFields represents the fields needed in the
   * identifiers file.
   */
  public enum IdentifiersFileFields {

    /**
     * The identifier scheme id field.
     */
    IDENTIFIER_SCHEME_ID("identifierSchemeId", "\t"),
    /**
     * The alternate identifier field.
     */
    ALTERNATE_IDENTIFIER("alternateIdentifier", "\t"),
    /**
     * The effective time field.
     */
    EFFECTIVE_TIME("effectiveTime", "\t"),
    /**
     * The active status field.
     */
    ACTIVE("active", "\t"),
    /**
     * The module id field.
     */
    MODULE_ID("moduleId", "\t"),
    /**
     * The referenced component id field.
     */
    REFERENCED_COMPONENT_ID("referencedComponentId", "\r\n");
    public final String headerText;

    public final String seperator;

    // ~--- constructors -----------------------------------------------------
    /**
     * Instantiates a new identifiers file fields.
     *
     * @param headerText the text to display in the header
     * @param seperator the separator to use for separating the field
     */
    private IdentifiersFileFields(String headerText, String seperator) {
      this.headerText = headerText;
      this.seperator = seperator;
    }
  }

  /**
   * The Enum RelationshipsFileFields represents the fields needed in the
   * relationships file.
   */
  public enum RelationshipsFileFields {

    /**
     * The relationship id field.
     */
    ID("id", "\t"),
    /**
     * The effective time field.
     */
    EFFECTIVE_TIME("effectiveTime", "\t"),
    /**
     * The active status field.
     */
    ACTIVE("active", "\t"),
    /**
     * The module id field.
     */
    MODULE_ID("moduleId", "\t"),
    /**
     * The source concept id field.
     */
    SOURCE_ID("sourceId", "\t"),
    /**
     * The destination concepts id field.
     */
    DESTINATION_ID("destinationId", "\t"),
    /**
     * The relationship group field.
     */
    RELATIONSHIP_GROUP("relationshipGroup", "\t"),
    /**
     * The relationship type id field.
     */
    TYPE_ID("typeId", "\t"),
    /**
     * The characteristic id field.
     */
    CHARCTERISTIC_ID("characteristicTypeId", "\t"),
    /**
     * The modifier id field.
     */
    MODIFIER_ID("modifierId", "\r\n");
    public final String headerText;

    public final String seperator;

    // ~--- constructors -----------------------------------------------------
    /**
     * Instantiates a new relationships file fields.
     *
     * @param headerText the text to display in the header
     * @param seperator the separator to use for separating the field
     */
    private RelationshipsFileFields(String headerText, String seperator) {
      this.headerText = headerText;
      this.seperator = seperator;
    }
  }

  /**
   * The Enum StatedRelationshipsFileFields represents the fields needed in the
   * stated relationships file.
   */
  public enum StatedRelationshipsFileFields {

    /**
     * The relationship id field.
     */
    ID("id", "\t"),
    /**
     * The effective time field.
     */
    EFFECTIVE_TIME("effectiveTime", "\t"),
    /**
     * The active status field.
     */
    ACTIVE("active", "\t"),
    /**
     * The module id field.
     */
    MODULE_ID("moduleId", "\t"),
    /**
     * The source id field.
     */
    SOURCE_ID("sourceId", "\t"),
    /**
     * The destination id field.
     */
    DESTINATION_ID("destinationId", "\t"),
    /**
     * The relationship group field.
     */
    RELATIONSHIP_GROUP("relationshipGroup", "\t"),
    /**
     * The type id field.
     */
    TYPE_ID("typeId", "\t"),
    /**
     * The characteristic id field.
     */
    CHARCTERISTIC_ID("characteristicTypeId", "\t"),
    /**
     * The modifier id field.
     */
    MODIFIER_ID("modifierId", "\r\n");
    public final String headerText;

    public final String seperator;

    // ~--- constructors -----------------------------------------------------
    /**
     * Instantiates a new stated relationships file fields.
     *
     * @param headerText the text to display in the header
     * @param seperator the separator to use for separating the field
     */
    private StatedRelationshipsFileFields(String headerText, String seperator) {
      this.headerText = headerText;
      this.seperator = seperator;
    }
  }

  /**
   * The Enum LanguageRefsetFileFields represents the fields needed in the
   * language refsets files.
   */
  public enum LanguageRefsetFileFields {

    /**
     * The member id field.
     */
    ID("id", "\t"),
    /**
     * The effective time field.
     */
    EFFECTIVE_TIME("effectiveTime", "\t"),
    /**
     * The active status field.
     */
    ACTIVE("active", "\t"),
    /**
     * The module id field.
     */
    MODULE_ID("moduleId", "\t"),
    /**
     * The refset id field.
     */
    REFSET_ID("refsetId", "\t"),
    /**
     * The referenced component id field.
     */
    REFERENCED_COMPONENT_ID("referencedComponentId", "\t"),
    /**
     * The acceptability field.
     */
    ACCEPTABILITY("acceptabilityId", "\r\n");
    public final String headerText;

    public final String seperator;

    // ~--- constructors -----------------------------------------------------
    /**
     * Instantiates a new language refset file fields.
     *
     * @param headerText the text to display in the header
     * @param seperator the separator to use for separating the field
     */
    private LanguageRefsetFileFields(String headerText, String seperator) {
      this.headerText = headerText;
      this.seperator = seperator;
    }
  }

  /**
   * The Enum ModuleDependencyFileFields represents the fields needed in the
   * module dependency file.
   */
  public enum ModuleDependencyFileFields {

    /**
     * The id field.
     */
    ID("id", "\t"),
    /**
     * The effective time field.
     */
    EFFECTIVE_TIME("effectiveTime", "\t"),
    /**
     * The active field.
     */
    ACTIVE("active", "\t"),
    /**
     * The module id field.
     */
    MODULE_ID("moduleId", "\t"),
    /**
     * The refset id field.
     */
    REFSET_ID("refsetId", "\t"),
    /**
     * The referenced component id field.
     */
    REFERENCED_COMPONENT_ID("referencedComponentId", "\t"),
    /**
     * The source time field.
     */
    SOURCE_TIME("sourceEffectiveTime", "\t"),
    /**
     * The target time field.
     */
    TARGET_TIME("targetEffectiveTime", "\r\n");
    public final String headerText;

    public final String seperator;

    // ~--- constructors -----------------------------------------------------
    /**
     * Instantiates a new module dependency file fields.
     *
     * @param headerText the text to display in the header
     * @param seperator the separator to use for separating the field
     */
    private ModuleDependencyFileFields(String headerText, String seperator) {
      this.headerText = headerText;
      this.seperator = seperator;
    }
  }

  /**
   * The Enum DescTypeFileFields represents the fields needed in the description
   * type file.
   */
  public enum DescTypeFileFields {

    /**
     * The id field.
     */
    ID("id", "\t"),
    /**
     * The effective time field.
     */
    EFFECTIVE_TIME("effectiveTime", "\t"),
    /**
     * The active field.
     */
    ACTIVE("active", "\t"),
    /**
     * The module id field.
     */
    MODULE_ID("moduleId", "\t"),
    /**
     * The refset id field.
     */
    REFSET_ID("refsetId", "\t"),
    /**
     * The referenced component id field.
     */
    REFERENCED_COMPONENT_ID("referencedComponentId", "\t"),
    /**
     * The desc format field.
     */
    DESC_FORMAT("descriptionFormat", "\t"),
    /**
     * The desc length field.
     */
    DESC_LENGTH("descriptionLength", "\r\n");
    public final String headerText;

    public final String seperator;

    // ~--- constructors -----------------------------------------------------
    /**
     * Instantiates a new desc type file fields.
     *
     * @param headerText the text to display in the header
     * @param seperator the separator to use for separating the field
     */
    private DescTypeFileFields(String headerText, String seperator) {
      this.headerText = headerText;
      this.seperator = seperator;
    }
  }

  /**
   * The Enum RefsetDescriptorFileFields represents the fields needed in the
   * refset descriptor file.
   */
  public enum RefsetDescriptorFileFields {

    /**
     * The id field.
     */
    ID("id", "\t"),
    /**
     * The effective time field.
     */
    EFFECTIVE_TIME("effectiveTime", "\t"),
    /**
     * The active status field.
     */
    ACTIVE("active", "\t"),
    /**
     * The module id field.
     */
    MODULE_ID("moduleId", "\t"),
    /**
     * The refset id field.
     */
    REFSET_ID("refsetId", "\t"),
    /**
     * The referenced component id field.
     */
    REFERENCED_COMPONENT_ID("referencedComponentId", "\t"),
    /**
     * The attribute desc field.
     */
    ATTRIB_DESC("attributeDescription", "\t"),
    /**
     * The attribute type field.
     */
    ATTRIB_TYPE("attributeType", "\t"),
    /**
     * The attribute order field.
     */
    ATTRIB_ORDER("attributeOrder", "\r\n");

    public final String headerText;

    public final String seperator;

    // ~--- constructors -----------------------------------------------------
    /**
     * Instantiates a new refset descriptor file fields.
     *
     * @param headerText the text to display in the header
     * @param seperator the separator to use for separating the field
     */
    private RefsetDescriptorFileFields(String headerText, String seperator) {
      this.headerText = headerText;
      this.seperator = seperator;
    }
  }

  /**
   * The Enum SimpleRefsetFileFields represents the fields needed in the simple
   * refsets file.
   */
  public enum SimpleRefsetFileFields {

    /**
     * The id field.
     */
    ID("id", "\t"),
    /**
     * The effective time field.
     */
    EFFECTIVE_TIME("effectiveTime", "\t"),
    /**
     * The active field.
     */
    ACTIVE("active", "\t"),
    /**
     * The module id field.
     */
    MODULE_ID("moduleId", "\t"),
    /**
     * The refset id field.
     */
    REFSET_ID("refsetId", "\t"),
    /**
     * The referenced component id field.
     */
    REFERENCED_COMPONENT_ID("referencedComponentId", "\r\n");

    public final String headerText;

    public final String seperator;

    // ~--- constructors -----------------------------------------------------
    /**
     * Instantiates a new simple refset file fields.
     *
     * @param headerText the text to display in the header
     * @param seperator the separator to use for separating the field
     */
    private SimpleRefsetFileFields(String headerText, String seperator) {
      this.headerText = headerText;
      this.seperator = seperator;
    }
  }

  /**
   * The Enum ConNumRefsetFileFields represents the fields needed in the concept
   * number refsets file.
   */
  public enum ConNumRefsetFileFields {

    /**
     * The id field.
     */
    ID("id", "\t"),
    /**
     * The effective time field.
     */
    EFFECTIVE_TIME("effectiveTime", "\t"),
    /**
     * The active field.
     */
    ACTIVE("active", "\t"),
    /**
     * The module id field.
     */
    MODULE_ID("moduleId", "\t"),
    /**
     * The refset id field.
     */
    REFSET_ID("refsetId", "\t"),
    /**
     * The referenced component id field.
     */
    REFERENCED_COMPONENT_ID("referencedComponentId", "\t"),
    /**
     * The additional concept field.
     */
    ADDITIONAL_CONCEPT_ID("conceptId", "\t"),
    /**
     * The additional number field.
     */
    NUMBER("number", "\r\n");

    public final String headerText;

    public final String seperator;

    // ~--- constructors -----------------------------------------------------
    /**
     * Instantiates a new simple refset file fields.
     *
     * @param headerText the text to display in the header
     * @param seperator the separator to use for separating the field
     */
    private ConNumRefsetFileFields(String headerText, String seperator) {
      this.headerText = headerText;
      this.seperator = seperator;
    }
  }

  /**
   * The Enum AttribValueRefsetFileFields represents the fields needed in the
   * attribute value refset file.
   */
  public enum AttribValueRefsetFileFields {

    /**
     * The id field.
     */
    ID("id", "\t"),
    /**
     * The effective time field.
     */
    EFFECTIVE_TIME("effectiveTime", "\t"),
    /**
     * The active status field.
     */
    ACTIVE("active", "\t"),
    /**
     * The module id field.
     */
    MODULE_ID("moduleId", "\t"),
    /**
     * The refset id field.
     */
    REFSET_ID("refsetId", "\t"),
    /**
     * The referenced component id field.
     */
    REFERENCED_COMPONENT_ID("referencedComponentId", "\t"),
    /**
     * The value id field.
     */
    VALUE_ID("valueId", "\r\n");

    public final String headerText;

    public final String seperator;

    // ~--- constructors -----------------------------------------------------
    /**
     * Instantiates a new attribute value refset file fields.
     *
     * @param headerText the text to display in the header
     * @param seperator the separator to use for separating the field
     */
    private AttribValueRefsetFileFields(String headerText, String seperator) {
      this.headerText = headerText;
      this.seperator = seperator;
    }
  }

  /**
   * The Enum AssociationRefsetFileFields represents the fields needed in the
   * associated refsets file.
   */
  public enum AssociationRefsetFileFields {

    /**
     * The id field.
     */
    ID("id", "\t"),
    /**
     * The effective time field.
     */
    EFFECTIVE_TIME("effectiveTime", "\t"),
    /**
     * The active field.
     */
    ACTIVE("active", "\t"),
    /**
     * The module id field.
     */
    MODULE_ID("moduleId", "\t"),
    /**
     * The refset id field.
     */
    REFSET_ID("refsetId", "\t"),
    /**
     * The referenced component id field.
     */
    REFERENCED_COMPONENT_ID("referencedComponentId", "\t"),
    /**
     * The target field.
     */
    TARGET("targetComponent", "\r\n");

    public final String headerText;

    public final String seperator;

    // ~--- constructors -----------------------------------------------------
    /**
     * Instantiates a new association refset file fields.
     *
     * @param headerText the text to display in the header
     * @param seperator the separator to use for separating the field
     */
    private AssociationRefsetFileFields(String headerText, String seperator) {
      this.headerText = headerText;
      this.seperator = seperator;
    }
  }

  /**
   * The Enum UuidToSctMapFileFields represents the fields needed in the
   * uuid-sctid file.
   */
  public enum UuidToSctMapFileFields {

    /**
     * The sct field.
     */
    SCT("sctId", "\t"),
    /**
     * The uuid field.
     */
    UUID("uuid", "\r\n");

    public final String headerText;

    public final String seperator;

    // ~--- constructors -----------------------------------------------------
    /**
     * Instantiates a new uuid to sct map file fields.
     *
     * @param headerText the text to display in the header
     * @param seperator the separator to use for separating the field
     */
    private UuidToSctMapFileFields(String headerText, String seperator) {
      this.headerText = headerText;
      this.seperator = seperator;
    }
  }

  /**
   * The Enum ReleaseType represents the different RF2 release types.
   */
  public enum ReleaseType {

    /**
     * The delta.
     */
    DELTA("Delta"),
    /**
     * The full.
     */
    FULL("Full"),
    /**
     * The snapshot.
     */
    SNAPSHOT("Snapshot");

    public final String suffix;

    // ~--- constructors -----------------------------------------------------
    /**
     * Instantiates a new release type.
     *
     * @param suffix the suffix to add to the release file name
     */
    private ReleaseType(String suffix) {
      this.suffix = suffix;
    }
  }
}
