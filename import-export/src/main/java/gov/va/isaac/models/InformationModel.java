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

import java.util.UUID;

/**
 * Generically represents an information model.
 *
 * @author ocarlsen
 * @author bcarlsenca
 */
public interface InformationModel {

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

}
