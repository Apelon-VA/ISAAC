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
package gov.va.isaac.models;

import java.util.UUID;

/**
 * Generically represents a "property" of an information model.
 *
 * We could consider "improvement" so this model by typing the various fields
 * more precisely, but then the underlying refset model has to be similiarly
 * typed. Using Strings is super flexible and voids the need ] to have any
 * requirements.
 * 
 * @author bcarlsenca
 */
public interface InformationModelProperty {

  /**
   * Returns the uuid.
   *
   * @return the uuid
   */
  public UUID getUuid();

  /**
   * Sets the uuid.
   *
   * @param uuid the uuid to set
   */
  public void setUuid(UUID uuid);

  /**
   * Returns the label.
   *
   * @return the label
   */
  public String getLabel();

  /**
   * Sets the label.
   *
   * @param label the label to set
   */
  public void setLabel(String label);

  /**
   * Returns the name.
   *
   * @return the name
   */
  public String getName();

  /**
   * Sets the name.
   *
   * @param name the name to set
   */
  public void setName(String name);

  /**
   * Returns the type.
   *
   * @return the type
   */
  public String getType();

  /**
   * Sets the type.
   *
   * @param type the type to set
   */
  public void setType(String type);

  /**
   * Returns the value.
   *
   * @return the value
   */
  public String getValue();

  /**
   * Sets the value.
   *
   * @param value the value to set
   */
  public void setValue(String value);

  /**
   * Returns the default value.
   *
   * @return the defaultValue
   */
  public String getDefaultValue();

  /**
   * Sets the default value.
   *
   * @param defaultValue the defaultValue to set
   */
  public void setDefaultValue(String defaultValue);

  /**
   * Returns the cardinality min.
   *
   * @return the cardinalityMin
   */
  public String getCardinalityMin();

  /**
   * Sets the cardinality min.
   *
   * @param cardinalityMin the cardinalityMin to set
   */
  public void setCardinalityMin(String cardinalityMin);

  /**
   * Returns the cardinality max.
   *
   * @return the cardinalityMax
   */
  public String getCardinalityMax();

  /**
   * Sets the cardinality max.
   *
   * @param cardinalityMax the cardinalityMax to set
   */
  public void setCardinalityMax(String cardinalityMax);

  /**
   * Returns the visibility.
   *
   * @return the visibility
   */
  public String getVisibility();

  /**
   * Sets the visibility.
   *
   * @param visibility the visibility to set
   */
  public void setVisibility(String visibility);

}
