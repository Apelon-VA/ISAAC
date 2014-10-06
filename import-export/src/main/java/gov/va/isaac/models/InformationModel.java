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

import gov.va.isaac.model.InformationModelType;

import java.util.Set;
import java.util.UUID;

/**
 * Generically represents an information model.
 *
 * @author ocarlsen
 * @author bcarlsenca
 */
public interface InformationModel extends Comparable<InformationModel> {

  /**
   * Returns the key identifying this model.
   *
   * @return the key identifying this model
   */
  public String getKey();

  /**
   * Sets the key.
   *
   * @param key the key
   */
  public void setKey(String key);

  /**
   * Returns the name.
   *
   * @return the name
   */
  public String getName();

  /**
   * Sets the name.
   *
   * @param name the name
   */
  public void setName(String name);

  /**
   * Returns the UUID.
   *
   * @return the UUID
   */
  public UUID getUuid();

  /**
   * Sets the uuid.
   *
   * @param uuid the uuid
   */
  public void setUuid(UUID uuid);

  /**
   * Returns the type.
   *
   * @return the type
   */
  public InformationModelType getType();

  /**
   * Sets the type.
   *
   * @param type the type
   */
  public void setType(InformationModelType type);

  /**
   * Returns the metadata.
   *
   * @return the metadata
   */
  public InformationModelMetadata getMetadata();

  /**
   * Sets the metadata.
   *
   * @param metadata the metadata
   */
  public void setMetadata(InformationModelMetadata metadata);

  /**
   * Adds the property.
   *
   * @param property the property
   */
  public void addProperty(InformationModelProperty property);

  /**
   * Removes the property.
   *
   * @param property the property
   */
  public void removeProperty(InformationModelProperty property);

  /**
   * Returns the properties.
   *
   * @return the properties
   */
  public Set<InformationModelProperty> getProperties();

  /**
   * Sets the properties.
   *
   * @param properties the properties
   */
  public void setProperties(Set<InformationModelProperty> properties);

  /**
   * Adds the associated concept UUID.
   *
   * @param uuid the associated concept uuid
   */
  public void addAssociatedConceptUuid(UUID uuid);

  /**
   * Removes the associated concept UUID.
   *
   * @param uuid
   */
  public void removeAssociatedConceptUuid(UUID uuid);

  /**
   * Returns the associated concept UUIDs.
   *
   * @return the associated concept UUIDs
   */
  public Set<UUID> getAssociatedConceptUuids();

  /**
   * Sets the associated concept UUIDs.
   *
   * @param uuids associated concept UUIDs
   */
  public void setAssociatedConceptUuids(Set<UUID> uuids);

  /**
   * Checks for the existence of a super model UUID.
   *
   * @return true, if successful
   */
  public boolean hasSuperModelUuid();

  /**
   * Returns the super model uuid. NOTE: only single-inheritance hierarchy is
   * supported.
   * @return the super model
   */
  public UUID getSuperModelUuid();;

  /**
   * Sets the super model uuid.
   *
   * @param uuid the super model uuid 
   */
  public void setSuperModelUuid(UUID uuid);

}
