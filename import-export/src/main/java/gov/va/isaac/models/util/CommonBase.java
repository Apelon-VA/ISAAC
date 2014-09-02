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

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.util.WBUtility;

import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;

// TODO: Auto-generated Javadoc
/**
 * Shared base class for all information model importers & exporters.
 *
 * @author ocarlsen
 * @author bcarlsenca
 */
public class CommonBase {
  // @Inject
  /**  The data store. */
  private BdbTerminologyStore dataStore = ExtendedAppContext.getDataStore();

  /**
   * Instantiates an empty {@link CommonBase}.
   */
  protected CommonBase() {
    super();
    // Hk2Looker.get().inject(this);
  }

  /**
   * Returns the data store.
   *
   * @return the data store
   */
  protected final BdbTerminologyStore getDataStore() {
    return dataStore;
  }

  /**
   * Returns the edit coordinate.
   *
   * @return the edit coordinate
   */
  protected final EditCoordinate getEC() {
    return WBUtility.getEC();
  }

  /**
   * Returns the view coordinate
   *
   * @return the view coordinate
   */
  protected final ViewCoordinate getVC() {
    return WBUtility.getViewCoordinate();
  }
}
