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
package gov.va.legoEdit.storage;

import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.LegoList;
import gov.va.legoEdit.model.schemaModel.Pncs;
import gov.va.legoEdit.model.schemaModel.Stamp;
import java.util.List;

/**
 * 
 * DataStoreInterface
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 * Copyright 2013
 */
public interface DataStoreInterface
{

	/**
	 * Get the legoList object with the specified name.
	 */
	public LegoList getLegoListByName(String legoListGroupName) throws DataStoreException;

	/**
	 * Get the legoList object with the specified uuid.
	 */
	public LegoList getLegoListByID(String legoListUUID) throws DataStoreException;

	/**
	 * Get an iterator that traverses all stored legoLists.
	 */
	public CloseableIterator<LegoList> getLegoLists() throws DataStoreException;

	/**
	 * Get all LEGO (versions) in the DB with the provided UUID.
	 */
	public List<Lego> getLegos(String legoUUID) throws DataStoreException;

	/**
	 * Get the exact lego that matches the specified ID and Stamp
	 */
	public Lego getLego(String legoUUID, String stampUUID) throws DataStoreException;

	/**
	 * Get all LEGOs which contain (define) the assertion with the specified assertion UUID. (There may be more than one stamped lego with the same
	 * UUID - up to the caller to decide which stamp they want) Note - this does not return legos that simply link to the named assertion via an
	 * assertionComponent
	 */
	public List<Lego> getLegosContainingAssertion(String assertionUUID);

	/**
	 * Get all LEGOs which use the assertion with the specified assertion UUID as part of an assertionComponent
	 */
	public List<Lego> getLegosUsingAssertion(String assertionUUID);

	/**
	 * Get all LEGOs which contain the specified snomed concept (as any child).
	 */
	public List<Lego> getLegosContainingConceptIdentifiers(String... conceptUuidOrSCTId) throws DataStoreException;

	/**
	 * Get an iterator that traverses all stored LEGO objects.
	 */
	public CloseableIterator<Lego> getLegos() throws DataStoreException;

	/**
	 * Get all LEGOs which reference the specified PNCS.
	 */
	public List<Lego> getLegosForPncs(int id, String value) throws DataStoreException;

	/**
	 * Get all LEGOs which reference the specified PNCS.
	 */
	public List<Lego> getLegosForPncs(int id) throws DataStoreException;

	/**
	 * Get all of the PNCS objects currently in the system.
	 */
	public CloseableIterator<Pncs> getPncs() throws DataStoreException;

	/**
	 * Get all of the PNCS objects which have a matching id.
	 */
	public List<Pncs> getPncs(int id) throws DataStoreException;

	/**
	 * Get the PNCS for the specified id and value
	 */
	public Pncs getPncs(int id, String value);

	/**
	 * Get the UUID of the legoList(s) which contains the specified LEGO.
	 * 
	 * @return null if no legoList contains the specified LEGO
	 */
	public List<String> getLegoListByLego(String legoUUID) throws DataStoreException;

	/**
	 * Create and store a new (empty) LegoList.
	 * 
	 * @throws WriteException if the operation fails
	 */
	public LegoList createLegoList(String uuid, String groupName, String groupDescription, String comments) throws WriteException;

	/**
	 * A method to allow updating the the name, description and comments field that is stored on the lego list.
	 * 
	 * @param groupName - the new name, or null to leave the existing name in place. Cannot be an empty string.
	 * @param groupDescription - the new description, or null to leave the existing description in place
	 * @param comments - the new comments, or null to leave the existing comments in place.
	 */
	public void updateLegoListMetadata(String legoListUUID, String groupName, String groupDescription, String comments) throws WriteException;

	/**
	 * Store the specified legoList. Note - this method cannot be used to replace an existing LegoList.
	 * 
	 * This method does not make any changes to the STAMP values of any contained LEGO objects.
	 * 
	 * @throws WriteException if a legoList of the specified name already exists or if the store operation fails
	 */
	public void importLegoList(LegoList legoList) throws WriteException;

	/**
	 * Delete the specified legoList (and all LEGO children contained within).
	 * 
	 * @throws WriteException if the operation fails
	 */
	public void deleteLegoList(String legoListUUID) throws WriteException;

	/**
	 * Delete the exact lego that matches the specified ID and Stamp within the specified LegoList
	 */
	public void deleteLego(String legoListUUID, String legoUUID, String stampUUID) throws WriteException;

	/**
	 * This method always adds a new LEGO to the specified legoList - even if a LEGO with the same UUID already exists in the legoList.
	 * 
	 * The new LEGO will be differentiated from other LEGOs with the same UUID by the STAMP value (status, time, author, module, path)
	 * 
	 * The time and UUID fields of the STAMP will be updated by this method - time set to now - UUID set to a new unique value.
	 * 
	 * If status, author, module or path are missing, they will be set to the default values.
	 * 
	 * The updated STAMP will be returned to the caller.
	 * 
	 * @return The STAMP given to the LEGO as committed.
	 * 
	 * @throws WriteException if the specified legoList does not exist or if the write operation fails for any reason.
	 */
	public Stamp commitLego(Lego lego, String legoListUUID) throws WriteException;

	/**
	 * Call to notify the backend to shutdown cleanly.
	 */
	public void shutdown();
}
