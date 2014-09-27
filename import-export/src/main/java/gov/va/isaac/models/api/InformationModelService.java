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
package gov.va.isaac.models.api;

import gov.va.isaac.model.InformationModelType;
import gov.va.isaac.models.InformationModel;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;

/**
 * Generically represents a service for interacting with information models.
 *
 * @author bcarlsenca
 */
public interface InformationModelService {

  /**
   * Indicates whether or not the information model exists.
   *
   * @param model the model
   * @return true, if successful
   * @throws IOException if anything goes wrong
   */
  public boolean exists(InformationModel model) throws IOException;

  /**
   * Returns the information model for the specified uuid. Returns null if there
   * is no corresponding information model.
   *
   * @param uuid the uuid
   * @return the information model
   * @throws IOException if anything goes wrong
   * @throws ContradictionException if anything goes wrong
   */
  public InformationModel getInformationModel(UUID uuid) throws IOException,
    ContradictionException;

  /**
   * Returns the information model for the specified model-specific key. Returns
   * null if there is no corresponding information model.
   * 
   * For example, this takes an information model type, like
   * InformationModelType.CEM, and an identifier or unique name in that type,
   * like DiastolicBloodPressure_KEY_ECID, and returns the corresponding model
   * object. This is a way of finding a model when you do not know its ISAAC
   * UUID.
   * 
   * In the background, this is implemented by using the lucene index.
   * @param type the type
   * @param key the key
   * @return the information model
   * @throws IOException if anything goes wrong
   * @throws ContradictionException if anything goes wrong
   */
  public InformationModel getInformationModel(InformationModelType type,
    String key) throws IOException, ContradictionException;

  /**
   * Returns the information model children.
   *
   * @param model the model
   * @return the information model children
   * @throws ContradictionException 
   * @throws IOException 
   * @throws ValidationException 
   */
  public Set<InformationModel> getInformationModelChildren(InformationModel model) throws ValidationException, IOException, ContradictionException;
    

  /**
   * Adds or saves changes to the information model.
   *
   * @param model the model
   * @throws IOException if anything goes wrong
   * @throws ContradictionException if anything goes wrong
   * @throws InvalidCAB if anything goes wrong
   * @throws NoSuchAlgorithmException if anything goes wrong
   * @throws PropertyVetoException 
   */
  public void saveInformationModel(InformationModel model) throws IOException,
    InvalidCAB, ContradictionException, NoSuchAlgorithmException, PropertyVetoException;
}
