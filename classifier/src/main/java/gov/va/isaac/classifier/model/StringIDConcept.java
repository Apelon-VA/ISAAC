/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
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
package gov.va.isaac.classifier.model;

/**
 * The Class StringIDConcept. Represents an extension for concept, adding
 * stringId, definition status and module.
 * 
 * @author Alejandro Rodriguez.
 *
 * @version 1.0
 */
public class StringIDConcept extends Concept {

  /** The string id. */
  private String stringId;

  /** The module. */
  private String module;

  /** The definition status id. */
  private boolean definitionStatusId;

  /**
   * Instantiates a new concept string id.
   *
   * @param id the cont
   * @param stringId the string id
   * @param definitionStatusId the definition status id
   * @param module the module
   */
  public StringIDConcept(int id, String stringId, boolean definitionStatusId,
      String module) {
    super.id = id;
    this.stringId = stringId;
    this.definitionStatusId = definitionStatusId;
    this.module = module;

  }

  /**
   * Gets the string id.
   *
   * @return the string id
   */
  public String getStringId() {
    return stringId;
  }

  /**
   * Sets the string id.
   *
   * @param stringId the new string id
   */
  public void setStringId(String stringId) {
    this.stringId = stringId;
  }

  /**
   * Instantiates a new concept string id.
   *
   * @param id the id
   * @param stringId the string id
   * @param isDefined the is defined
   */
  public StringIDConcept(int id, String stringId, boolean isDefined) {
    super(id, isDefined);

    this.stringId = stringId;
  }

  /**
   * Gets the module.
   *
   * @return the module
   */
  public String getModule() {
    return module;
  }

  /**
   * Sets the module.
   *
   * @param module the new module
   */
  public void setModule(String module) {
    this.module = module;
  }

  /**
   * Gets the id.
   *
   * @return the id
   */
  public int getId() {
    return id;
  }

  /**
   * Sets the id.
   *
   * @param id the new id
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * Checks if is definition status id.
   *
   * @return true, if is definition status id
   */
  public boolean isDefinitionStatusId() {
    return definitionStatusId;
  }

  /**
   * Sets the definition status id.
   *
   * @param definitionStatusId the new definition status id
   */
  public void setDefinitionStatusId(boolean definitionStatusId) {
    this.definitionStatusId = definitionStatusId;
  }

}
