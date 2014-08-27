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

  /**
   * Instantiates an empty {@link DefaultInformationModel}.
   */
  public DefaultInformationModel() {
    // do nothing
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

  /* (non-Javadoc)
   * @see gov.va.isaac.models.InformationModel#setMetadata(gov.va.isaac.models.InformationModelMetadata)
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

}
