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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.model;

import gov.va.isaac.constants.InformationModels;
import java.util.UUID;
import javafx.collections.ObservableList;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import com.sun.javafx.collections.ImmutableObservableList;

/**
 * An enumerated type representing various information models.
 *
 * @author ocarlsen
 * @author bcarlsenca
 */
public enum InformationModelType {

  /** The CEM entry. */
  CEM(InformationModels.CEM, "xml"),

  /** The cimi. */
  //CIMI("Clinical Information Model Initiative", null, null),

  /** The FHIM entry. */
  FHIM(InformationModels.FHIM, "uml"),

  /** The He d. */
  HeD(InformationModels.HED, "xml");

  /** The Constant VALUES. */
  private static final ImmutableObservableList<InformationModelType> VALUES =
      new ImmutableObservableList<>(InformationModelType.values());

  /** The display name. */
  private String displayName;

  /** The file extension. */
  private String fileExtension;

  /** The uuid */
  private UUID uuid = null;

  /**
   * Instantiates a {@link InformationModelType} from the specified parameters.
   *
   * @param displayName the display name
   * @param fileExtension the file extension
   */
  private InformationModelType(ConceptSpec spec, String fileExtension) {
    this.displayName = spec.getDescription();
    this.fileExtension = fileExtension;
    this.uuid = spec.getUuids()[0];
  }

  /**
   * Returns the display name.
   *
   * @return the display name
   */
  public String getDisplayName() {
    return this.displayName;
  }

  /**
   * Returns the file extension.
   *
   * @return the file extension
   */
  public String getFileExtension() {
    return fileExtension;
  }

  /**
   * Returns the uuid.
   *
   * @return the uuid
   */
  public UUID getUuid() {
    return uuid;
  }

  /**
   * As observable list.
   *
   * @return the observable list
   */
  public static ObservableList<InformationModelType> asObservableList() {
    return VALUES;
  }
}
