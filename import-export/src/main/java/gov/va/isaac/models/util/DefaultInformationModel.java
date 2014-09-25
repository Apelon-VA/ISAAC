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
package gov.va.isaac.models.util;

import gov.va.isaac.model.InformationModelType;
import gov.va.isaac.models.InformationModel;
import gov.va.isaac.models.InformationModelMetadata;
import gov.va.isaac.models.InformationModelProperty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.google.common.base.Objects;

/**
 * Represents an {@link InformationModel}.
 *
 * @author ocarlsen
 * @author bcarlsenca
 */
public class DefaultInformationModel implements InformationModel {

  /** The key. */
  private String key;

  /** The name. */
  private String name;

  /** The uuid. */
  private UUID uuid;

  /** The type. */
  private InformationModelType type;

  /** The metadata. */
  private InformationModelMetadata metadata;

  /** The properties. */
  protected Set<InformationModelProperty> properties = new HashSet<>();

  /** The associated concept uuids */
  protected Set<UUID> associatedConceptUuids = new HashSet<>();

  /** The super model. */
  private UUID superModelUuid = null;

  /**
   * Instantiates an empty {@link DefaultInformationModel}.
   */
  public DefaultInformationModel() {
    // do nothing
  }

  /**
   * Instantiates a {@link DefaultInformationModel} from the specified model.
   * @param model the model
   */
  public DefaultInformationModel(InformationModel model) {
    this.key = model.getKey();
    this.name = model.getName();
    this.uuid = model.getUuid();
    this.type = model.getType();
    this.metadata = model.getMetadata();
    this.properties = model.getProperties();
    this.associatedConceptUuids = model.getAssociatedConceptUuids();
  }

  /**
   * Instantiates a {@link DefaultInformationModel} from the specified
   * parameters.
   *
   * @param key the key
   * @param name the name
   * @param uuid the uuid
   * @param type the type
   */
  public DefaultInformationModel(String key, String name, UUID uuid,
      InformationModelType type) {
    super();
    this.key = key;
    this.name = name;
    this.uuid = uuid;
    this.type = type;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.models.InformationModel#getKey()
   */
  @Override
  public final String getKey() {
    return key;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.models.InformationModel#setKey(java.lang.String)
   */
  @Override
  public void setKey(String key) {
    this.key = key;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.models.InformationModel#getName()
   */
  @Override
  public final String getName() {
    return name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.models.InformationModel#setName(java.lang.String)
   */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the uuid.
   *
   * @return the uuid
   */
  @Override
  public UUID getUuid() {
    return uuid;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.models.InformationModel#setUuid(java.util.UUID)
   */
  @Override
  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.models.InformationModel#getType()
   */
  @Override
  public final InformationModelType getType() {
    return type;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.models.InformationModel#setType(gov.va.isaac.model.
   * InformationModelType)
   */
  @Override
  public void setType(InformationModelType type) {
    this.type = type;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.models.InformationModel#getMetadata()
   */
  @Override
  public final InformationModelMetadata getMetadata() {
    return metadata;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.models.InformationModel#setMetadata(gov.va.isaac.models.
   * InformationModelMetadata)
   */
  @Override
  public final void setMetadata(InformationModelMetadata metadata) {
    this.metadata = metadata;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("name", name)
        .add("type", getType()).toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.models.InformationModel#addProperty(gov.va.isaac.models.
   * InformationModelProperty)
   */
  @Override
  public void addProperty(InformationModelProperty property) {
    properties.add(property);

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.va.isaac.models.InformationModel#removeProperty(gov.va.isaac.models
   * .InformationModelProperty)
   */
  @Override
  public void removeProperty(InformationModelProperty property) {
    properties.remove(property);
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.models.InformationModel#getProperties()
   */
  @Override
  public Set<InformationModelProperty> getProperties() {
    return new HashSet<>(properties);
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.models.InformationModel#setProperties(java.util.Set)
   */
  @Override
  public void setProperties(Set<InformationModelProperty> properties) {
    this.properties = new HashSet<>(properties);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.va.isaac.models.InformationModel#addAssociatedConceptUuid(java.util
   * .UUID)
   */
  @Override
  public void addAssociatedConceptUuid(UUID uuid) {
    this.associatedConceptUuids.add(uuid);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.va.isaac.models.InformationModel#removeAssociatedConceptUuid(java.util
   * .UUID)
   */
  @Override
  public void removeAssociatedConceptUuid(UUID uuid) {
    this.associatedConceptUuids.remove(uuid);
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.models.InformationModel#getAssociatedConceptUuids()
   */
  @Override
  public Set<UUID> getAssociatedConceptUuids() {
    return new HashSet<>(associatedConceptUuids);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.va.isaac.models.InformationModel#setAssociatedConceptUuids(java.util
   * .Set)
   */
  @Override
  public void setAssociatedConceptUuids(Set<UUID> uuids) {
    this.associatedConceptUuids = new HashSet<>(uuids);
  }

  /*
   * (non-Javadoc)
   * 
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
        prime
            * result
            + ((associatedConceptUuids == null) ? 0 : associatedConceptUuids
                .hashCode());
    result = prime * result + ((key == null) ? 0 : key.hashCode());
    result = prime * result + ((metadata == null) ? 0 : metadata.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result =
        prime * result + ((properties == null) ? 0 : properties.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
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
    DefaultInformationModel other = (DefaultInformationModel) obj;
    if (uuid != null && other.uuid != null) {
      return !uuid.equals(other.uuid);
    }

    if (associatedConceptUuids == null) {
      if (other.associatedConceptUuids != null)
        return false;
    } else if (!associatedConceptUuids.equals(other.associatedConceptUuids))
      return false;
    if (key == null) {
      if (other.key != null)
        return false;
    } else if (!key.equals(other.key))
      return false;
    if (metadata == null) {
      if (other.metadata != null)
        return false;
    } else if (!metadata.equals(other.metadata))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (properties == null) {
      if (other.properties != null)
        return false;
    } else if (!properties.equals(other.properties))
      return false;
    if (type != other.type)
      return false;
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.models.InformationModel#hasSuperModelUuid()
   */
  @Override
  public boolean hasSuperModelUuid() {
    return superModelUuid != null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.models.InformationModel#getSuperModelUuid()
   */
  @Override
  public UUID getSuperModelUuid() {
    return superModelUuid;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.models.InformationModel#setSuperModelUuid(java.util.UUID)
   */
  @Override
  public void setSuperModelUuid(UUID uuid) {
    this.superModelUuid = uuid;
  }

  /**
   * Removes the properties by label.
   *
   * @param label the label
   */
  protected void removePropertiesByLabel(String label) {
    // Remove any "data" properties
    Set<InformationModelProperty> properties = new HashSet<>(getProperties());
    for (InformationModelProperty property : properties) {
      if (property.getLabel().equals(label)) {
        removeProperty(property);
        break;
      }
    }
  }

  /**
   * Returns the property by label.
   *
   * @param label the label
   * @return the property by label
   */
  protected InformationModelProperty getPropertyByLabel(String label) {
    for (InformationModelProperty property : properties) {
      if (property.getLabel().equals(label)) {
        return property;
      }
    }
    return null;
  }

 
  /**
   * Returns the properties by label.
   *
   * @param label the label
   * @return the properties by label
   */
  protected List<InformationModelProperty> getPropertiesByLabel(String label) {
    List<InformationModelProperty> propsByLabel = new ArrayList<>();
    for (InformationModelProperty property : properties) {
      if (property.getLabel().equals(label)) {
        propsByLabel.add(property);
      }
    }
    return propsByLabel;
  }
}
