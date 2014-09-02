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
 * Default implementation of {@link InformationModelProperty}.
 * 
 * Default values of fields are "" because refset extension columns cant be
 * null.
 * 
 * @author bcarlsenca
 */
public class DefaultInformationModelProperty implements
    InformationModelProperty {

  /** The uuid. */
  private UUID uuid;

  /** The label. */
  private String label = "";

  /** The name. */
  private String name = "";

  /** The type. */
  private String type = "";

  /** The value. */
  private String value = "";

  /** The default value. */
  private String defaultValue = "";

  /** The cardinality min - this is a String because it may not exist. */
  private String cardinalityMin = "";

  /**
   * The cardinality max - this is a String because it may have a value like
   * 'M'.
   */
  private String cardinalityMax = "";

  /**
   * The visibility - this could be based on an enum, but that may limit its
   * value.
   */
  private String visibility = "";

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.models.InformationModelProperty#getUuid()
   */
  @Override
  public UUID getUuid() {
    return uuid;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.models.InformationModelProperty#setUuid(java.util.UUID)
   */
  @Override
  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.models.InformationModelProperty#getLabel()
   */
  @Override
  public String getLabel() {
    return label;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.va.isaac.models.InformationModelProperty#setLabel(java.lang.String)
   */
  @Override
  public void setLabel(String label) {
    this.label = label;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.models.InformationModelProperty#getName()
   */
  @Override
  public String getName() {
    return name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.models.InformationModelProperty#setName(java.lang.String)
   */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.models.InformationModelProperty#getType()
   */
  @Override
  public String getType() {
    return type;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.models.InformationModelProperty#setType(java.lang.String)
   */
  @Override
  public void setType(String type) {
    this.type = type;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.models.InformationModelProperty#getValue()
   */
  @Override
  public String getValue() {
    return value;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.va.isaac.models.InformationModelProperty#setValue(java.lang.String)
   */
  @Override
  public void setValue(String value) {
    this.value = value;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.models.InformationModelProperty#getDefaultValue()
   */
  @Override
  public String getDefaultValue() {
    return defaultValue;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.va.isaac.models.InformationModelProperty#setDefaultValue(java.lang.
   * String)
   */
  @Override
  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.models.InformationModelProperty#getCardinalityMin()
   */
  @Override
  public String getCardinalityMin() {
    return cardinalityMin;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.va.isaac.models.InformationModelProperty#setCardinalityMin(java.lang
   * .String)
   */
  @Override
  public void setCardinalityMin(String cardinalityMin) {
    this.cardinalityMin = cardinalityMin;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.models.InformationModelProperty#getCardinalityMax()
   */
  @Override
  public String getCardinalityMax() {
    return cardinalityMax;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.va.isaac.models.InformationModelProperty#setCardinalityMax(java.lang
   * .String)
   */
  @Override
  public void setCardinalityMax(String cardinalityMax) {
    this.cardinalityMax = cardinalityMax;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.models.InformationModelProperty#getVisibility()
   */
  @Override
  public String getVisibility() {
    return visibility;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.va.isaac.models.InformationModelProperty#setVisibility(java.lang.String
   * )
   */
  @Override
  public void setVisibility(String visibility) {
    this.visibility = visibility;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    if (uuid != null) {
      return uuid.hashCode();
    }
    
    final int prime = 31;
    int result = 1;
    result =
        prime * result
            + ((cardinalityMax == null) ? 0 : cardinalityMax.hashCode());
    result =
        prime * result
            + ((cardinalityMin == null) ? 0 : cardinalityMin.hashCode());
    result =
        prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
    result = prime * result + ((label == null) ? 0 : label.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    result =
        prime * result + ((visibility == null) ? 0 : visibility.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    DefaultInformationModelProperty other =
        (DefaultInformationModelProperty) obj;
    // Stop at UUID if they are set
    if (uuid == null) {
      if (other.uuid != null)
        return false;
    } else { return uuid.equals(other.uuid); }

    if (cardinalityMax == null) {
      if (other.cardinalityMax != null)
        return false;
    } else if (!cardinalityMax.equals(other.cardinalityMax))
      return false;
    if (cardinalityMin == null) {
      if (other.cardinalityMin != null)
        return false;
    } else if (!cardinalityMin.equals(other.cardinalityMin))
      return false;
    if (defaultValue == null) {
      if (other.defaultValue != null)
        return false;
    } else if (!defaultValue.equals(other.defaultValue))
      return false;
    if (label == null) {
      if (other.label != null)
        return false;
    } else if (!label.equals(other.label))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (type == null) {
      if (other.type != null)
        return false;
    } else if (!type.equals(other.type))
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    if (visibility == null) {
      if (other.visibility != null)
        return false;
    } else if (!visibility.equals(other.visibility))
      return false;
    return true;
  }

}
