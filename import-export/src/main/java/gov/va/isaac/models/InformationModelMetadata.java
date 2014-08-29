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

import java.io.IOException;

import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Path;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;

/**
 * Represents metadata about an information model.
 */
public class InformationModelMetadata {

  /** The stamp nid. */
  private int stampNid;

  /** The importer name. */
  private final String importerName;

  /** The time. */
  private final long time;

  /** The path. */
  private final Path path;

  /** The module name. */
  private final String moduleName;

  /**
   * New instance from the specified parameters
   *
   * @param stampNid the stamp nid for the model
   * @param dataStore the data store
   * @param vc the view coordinate
   * @return the information model metadata
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ContradictionException the contradiction exception
   */
  public static InformationModelMetadata newInstance(int stampNid,
    BdbTerminologyStore dataStore, ViewCoordinate vc) throws IOException,
    ContradictionException {
    String importerName = "Hard-coded placeholder";

    long time = dataStore.getTimeForStamp(stampNid);

    int pathNid = dataStore.getPathNidForStamp(stampNid);
    Path path = dataStore.getPath(pathNid);

    int moduleNid = dataStore.getModuleNidForStamp(stampNid);
    ConceptChronicleBI module = dataStore.getConcept(moduleNid);
    ConceptVersionBI version = module.getVersion(vc);
    String moduleName = version.getFullySpecifiedDescription().getText();

    return new InformationModelMetadata(importerName, time, path, moduleName,
        stampNid);
  }

  /**
   * Instantiates a {@link InformationModelMetadata} from the specified
   * parameters.
   *
   * @param importerName the importer name
   * @param time the time
   * @param path the path
   * @param moduleName the module name
   * @param stampNid the STAMP nid
   */
  public InformationModelMetadata(String importerName, long time, Path path,
      String moduleName, int stampNid) {
    this.stampNid = stampNid;
    this.importerName = importerName;
    this.time = time;
    this.path = path;
    this.moduleName = moduleName;
  }

  /**
   * Returns the importer name.
   *
   * @return the importer name
   */
  public String getImporterName() {
    return importerName;
  }

  /**
   * Returns the time.
   *
   * @return the time
   */
  public long getTime() {
    return time;
  }

  /**
   * Returns the path.
   *
   * @return the path
   */
  public Path getPath() {
    return path;
  }

  /**
   * Returns the module name.
   *
   * @return the module name
   */
  public String getModuleName() {
    return moduleName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + stampNid;
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
    InformationModelMetadata other = (InformationModelMetadata) obj;
    if (stampNid != other.stampNid)
      return false;
    return true;
  }
}
