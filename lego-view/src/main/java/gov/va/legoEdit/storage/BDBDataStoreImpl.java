/**
 * Copyright 2013
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

import gov.va.legoEdit.model.ModelUtil;
import gov.va.legoEdit.model.bdbModel.LegoBDB;
import gov.va.legoEdit.model.bdbModel.LegoListBDB;
import gov.va.legoEdit.model.bdbModel.PncsBDB;
import gov.va.legoEdit.model.bdbModel.StampBDB;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.LegoList;
import gov.va.legoEdit.model.schemaModel.Pncs;
import gov.va.legoEdit.model.schemaModel.Stamp;
import gov.va.legoEdit.model.userPrefs.UserPreferences;
import gov.va.legoEdit.storage.util.LegoBDBConvertingIterator;
import gov.va.legoEdit.storage.util.LegoListBDBConvertingIterator;
import gov.va.legoEdit.storage.util.PncsBDBConvertingIterator;
import gov.va.legoEdit.util.TimeConvert;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.Transaction;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityIndex;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.StoreConfig;

/**
 * 
 * BDBDataStoreImpl
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 */
public class BDBDataStoreImpl implements DataStoreInterface
{
	private static volatile BDBDataStoreImpl instance_;

	public static File dbFolderPath = new File("legoData.db");

	private Environment myEnv;
	private EntityStore store;
	// LegoList accessors
	private PrimaryIndex<String, LegoListBDB> legoListByUUID;
	private SecondaryIndex<String, String, LegoListBDB> legoListByName;
	private SecondaryIndex<String, String, LegoListBDB> legoListByContainedLego;
	// Lego accessors
	private PrimaryIndex<String, LegoBDB> legoByUniqueId;
	private SecondaryIndex<String, String, LegoBDB> legoByUUID;
	private SecondaryIndex<String, String, LegoBDB> legoByPncsId;
	private SecondaryIndex<String, String, LegoBDB> legoByContainingAssertionUUID;
	private SecondaryIndex<String, String, LegoBDB> legoBySctIdentifiers;
	private SecondaryIndex<String, String, LegoBDB> legoByUsingAssertionUUID;
	// Stamp accessors
	private PrimaryIndex<String, StampBDB> stampByUniqueId;
	// pncs accessors
	private PrimaryIndex<String, PncsBDB> pncsByUniqueId;
	private SecondaryIndex<Integer, String, PncsBDB> pncsById;

	Logger logger = LoggerFactory.getLogger(BDBDataStoreImpl.class);

	ScheduledExecutorService sec = Executors.newSingleThreadScheduledExecutor();

	public static DataStoreInterface getInstance() throws DataStoreException
	{
		if (instance_ == null)
		{
			synchronized (BDBDataStoreImpl.class)
			{
				if (instance_ == null)
				{
					instance_ = new BDBDataStoreImpl();
				}
			}
		}
		if (instance_.store == null)
		{
			throw new DataStoreException("Already shutdown.  No further operations are allowed.");
		}
		return instance_;
	}

	private BDBDataStoreImpl() throws DataStoreException
	{
		try
		{
			logger.info("Configuring the Database");
			EnvironmentConfig myEnvConfig = new EnvironmentConfig();
			myEnvConfig.setTransactional(true);
			StoreConfig storeConfig = new StoreConfig();

			myEnvConfig.setAllowCreate(true);
			storeConfig.setAllowCreate(true);
			storeConfig.setTransactional(true);

			// Open the environment and entity store
			logger.info("Reading the file {}", dbFolderPath);
			dbFolderPath.mkdir();
			myEnv = new Environment(dbFolderPath, myEnvConfig);
			store = new EntityStore(myEnv, "EntityStore", storeConfig);

			legoListByUUID = store.getPrimaryIndex(String.class, LegoListBDB.class);
			legoListByName = store.getSecondaryIndex(legoListByUUID, String.class, "groupName");
			legoListByContainedLego = store.getSecondaryIndex(legoListByUUID, String.class, "legoUUIDs");

			legoByUniqueId = store.getPrimaryIndex(String.class, LegoBDB.class);
			legoByUUID = store.getSecondaryIndex(legoByUniqueId, String.class, "legoUUID");
			legoByPncsId = store.getSecondaryIndex(legoByUniqueId, String.class, "pncsId");
			legoByContainingAssertionUUID = store.getSecondaryIndex(legoByUniqueId, String.class, "usedAssertionUUIDs");
			legoByUsingAssertionUUID = store.getSecondaryIndex(legoByUniqueId, String.class, "compositeAssertionUUIDs");
			legoBySctIdentifiers = store.getSecondaryIndex(legoByUniqueId, String.class, "usedSCTIdentifiers");

			stampByUniqueId = store.getPrimaryIndex(String.class, StampBDB.class);

			pncsByUniqueId = store.getPrimaryIndex(String.class, PncsBDB.class);
			pncsById = store.getSecondaryIndex(pncsByUniqueId, Integer.class, "id");
		}
		catch (DatabaseException e)
		{
			logger.error("Failure loading the DB", e);
			throw new DataStoreException("Error opening database storage.", e);
		}
	}

	@Override
	public void shutdown()
	{
		logger.info("Shutdown called");
		sec.shutdownNow();
		if (store != null)
		{
			try
			{
				store.close();
			}
			catch (DatabaseException dbe)
			{
				logger.error("Failure closing the DB store!", dbe);
			}
		}

		if (myEnv != null)
		{
			try
			{
				// Finally, close environment.
				myEnv.close();
			}
			catch (DatabaseException dbe)
			{
				logger.error("Failure closing the DB environment!", dbe);
			}
		}
		store = null;
		myEnv = null;
		logger.info("Shutdown complete");
	}

	@Override
	public LegoList getLegoListByName(String legoListGroupName) throws DataStoreException
	{
		try
		{
			LegoListBDB ll = legoListByName.get(legoListGroupName);
			if (ll == null)
			{
				return null;
			}
			else
			{
				return ll.toSchemaLegoList();
			}
		}
		catch (DatabaseException e)
		{
			logger.error("Unexpected error reading data", e);
			throw new DataStoreException("Data read failure", e);
		}
	}

	@Override
	public LegoList getLegoListByID(String legoListUUID) throws DataStoreException
	{
		try
		{
			LegoListBDB ll = legoListByUUID.get(legoListUUID);
			if (ll == null)
			{
				return null;
			}
			else
			{
				return ll.toSchemaLegoList();
			}
		}
		catch (DatabaseException e)
		{
			logger.error("Unexpected error reading data", e);
			throw new DataStoreException("Data read failure", e);
		}
	}

	@Override
	public CloseableIterator<LegoList> getLegoLists() throws DataStoreException
	{
		try
		{
			return new LegoListBDBConvertingIterator(sec, legoListByUUID.entities());
		}
		catch (DatabaseException e)
		{
			logger.error("Unexpected error reading data", e);
			throw new DataStoreException("Data read failure", e);
		}

	}

	@Override
	public List<Lego> getLegos(String legoUUID)
	{
		EntityCursor<LegoBDB> ec = null;
		try
		{
			ArrayList<Lego> result = new ArrayList<>();
			EntityIndex<String, LegoBDB> ei = legoByUUID.subIndex(legoUUID);
			ec = ei.entities();

			for (LegoBDB current = ec.first(); current != null; current = ec.nextNoDup())
			{
				while (current != null)
				{
					result.add(current.toSchemaLego());
					current = ec.nextDup();
				}
			}
			return result;
		}
		catch (DatabaseException e)
		{
			logger.error("Unexpected error reading data", e);
			throw new DataStoreException("Data read failure", e);
		}
		finally
		{
			if (ec != null)
			{
				try
				{
					ec.close();
				}
				catch (DatabaseException e)
				{
					logger.error("Unexpected error closing cursor", e);
				}
			}
		}
	}

	@Override
	public Lego getLego(String legoUUID, String stampUUID)
	{
		EntityCursor<LegoBDB> ec = null;
		try
		{
			EntityIndex<String, LegoBDB> ei = legoByUUID.subIndex(legoUUID);
			ec = ei.entities();

			for (LegoBDB current = ec.first(); current != null; current = ec.nextNoDup())
			{
				while (current != null)
				{
					if (current.getStampBDB().getStampId().equals(stampUUID))
					{
						return current.toSchemaLego();
					}
					current = ec.nextDup();
				}
			}
			return null;
		}
		catch (DatabaseException e)
		{
			logger.error("Unexpected error reading data", e);
			throw new DataStoreException("Data read failure", e);
		}
		finally
		{
			if (ec != null)
			{
				try
				{
					ec.close();
				}
				catch (DatabaseException e)
				{
					logger.error("Unexpected error closing cursor", e);
				}
			}
		}
	}

	@Override
	public List<Lego> getLegosContainingAssertion(String assertionUUID)
	{
		EntityCursor<LegoBDB> ec = null;
		try
		{
			ArrayList<Lego> result = new ArrayList<>();
			EntityIndex<String, LegoBDB> ei = legoByContainingAssertionUUID.subIndex(assertionUUID);
			ec = ei.entities();

			for (LegoBDB current = ec.first(); current != null; current = ec.nextNoDup())
			{
				// Don't need to check the dupes - they should all point to the same lego
				result.add(current.toSchemaLego());
			}

			return result;
		}
		catch (DatabaseException e)
		{
			logger.error("Unexpected error reading data", e);
			throw new DataStoreException("Data read failure", e);
		}
		finally
		{
			if (ec != null)
			{
				try
				{
					ec.close();
				}
				catch (DatabaseException e)
				{
					logger.error("Unexpected error closing cursor", e);
				}
			}
		}
	}

	@Override
	public List<Lego> getLegosUsingAssertion(String assertionUUID)
	{
		EntityCursor<LegoBDB> ec = null;
		try
		{
			HashMap<String, LegoBDB> uniqueLegos = new HashMap<>();

			EntityIndex<String, LegoBDB> ei = legoByUsingAssertionUUID.subIndex(assertionUUID);
			ec = ei.entities();

			for (LegoBDB current = ec.first(); current != null; current = ec.nextNoDup())
			{
				while (current != null)
				{
					uniqueLegos.put(current.getUniqueId(), current);
					current = ec.nextDup();
				}
			}

			ArrayList<Lego> result = new ArrayList<>();
			for (LegoBDB lego : uniqueLegos.values())
			{
				result.add(lego.toSchemaLego());
			}

			return result;
		}
		catch (DatabaseException e)
		{
			logger.error("Unexpected error reading data", e);
			throw new DataStoreException("Data read failure", e);
		}
		finally
		{
			if (ec != null)
			{
				try
				{
					ec.close();
				}
				catch (DatabaseException e)
				{
					logger.error("Unexpected error closing cursor", e);
				}
			}
		}
	}

	public List<Lego> getLegosContainingConceptIdentifiers(String... conceptUuidOrSCTId)
	{
		EntityCursor<LegoBDB> ec = null;
		try
		{
			HashMap<String, LegoBDB> uniqueLegos = new HashMap<>();

			for (String s : conceptUuidOrSCTId)
			{

				EntityIndex<String, LegoBDB> ei = legoBySctIdentifiers.subIndex(s);
				ec = ei.entities();

				for (LegoBDB current = ec.first(); current != null; current = ec.nextNoDup())
				{
					while (current != null)
					{
						uniqueLegos.put(current.getUniqueId(), current);
						current = ec.nextDup();
					}
				}
				ec.close();
			}

			ArrayList<Lego> result = new ArrayList<>();
			for (LegoBDB lego : uniqueLegos.values())
			{
				result.add(lego.toSchemaLego());
			}

			return result;
		}
		catch (DatabaseException e)
		{
			logger.error("Unexpected error reading data", e);
			throw new DataStoreException("Data read failure", e);
		}
		finally
		{
			if (ec != null)
			{
				try
				{
					ec.close();
				}
				catch (DatabaseException e)
				{
					logger.error("Unexpected error closing cursor", e);
				}
			}
		}
	}

	@Override
	public CloseableIterator<Lego> getLegos()
	{
		try
		{
			return new LegoBDBConvertingIterator(sec, legoByUniqueId.entities());
		}
		catch (DatabaseException e)
		{
			logger.error("Unexpected error reading data", e);
			throw new DataStoreException("Data read failure", e);
		}
	}

	@Override
	public List<Lego> getLegosForPncs(int id, String value)
	{
		EntityCursor<LegoBDB> ec = null;
		try
		{
			HashMap<String, LegoBDB> uniqueLegos = new HashMap<>();
			EntityIndex<String, LegoBDB> ei = legoByPncsId.subIndex(PncsBDB.makeUniqueId(id, value));
			ec = ei.entities();

			for (LegoBDB current = ec.first(); current != null; current = ec.nextNoDup())
			{
				while (current != null)
				{
					uniqueLegos.put(current.getUniqueId(), current);
					current = ec.nextDup();
				}
			}

			ArrayList<Lego> result = new ArrayList<>();
			for (LegoBDB lego : uniqueLegos.values())
			{
				result.add(lego.toSchemaLego());
			}
			return result;
		}
		catch (DatabaseException e)
		{
			logger.error("Unexpected error reading data", e);
			throw new DataStoreException("Data read failure", e);
		}
		finally
		{
			if (ec != null)
			{
				try
				{
					ec.close();
				}
				catch (DatabaseException e)
				{
					logger.error("Unexpected error closing cursor", e);
				}
			}
		}
	}

	@Override
	public List<Lego> getLegosForPncs(int id)
	{
		EntityCursor<PncsBDB> pec = null;
		EntityCursor<LegoBDB> lec = null;
		try
		{
			// First go to the pncs table to get all of the pncs objects by ID, get a unique list of their unique IDs.

			HashSet<String> uniquePncsIds = new HashSet<>();
			EntityIndex<String, PncsBDB> pei = pncsById.subIndex(id);
			pec = pei.entities();

			for (PncsBDB current = pec.first(); current != null; current = pec.nextNoDup())
			{
				while (current != null)
				{
					uniquePncsIds.add(current.getUniqueId());
					current = pec.nextDup();
				}
			}
			pec.close();

			// Now go get all the LEGOs that are involved with this pncs IDs

			HashMap<String, LegoBDB> uniqueLegos = new HashMap<>();
			for (String pncsId : uniquePncsIds)
			{
				EntityIndex<String, LegoBDB> lei = legoByPncsId.subIndex(pncsId);
				lec = lei.entities();

				for (LegoBDB current = lec.first(); current != null; current = lec.nextNoDup())
				{
					while (current != null)
					{
						uniqueLegos.put(current.getUniqueId(), current);
						current = lec.nextDup();
					}
				}
				lec.close();
			}

			ArrayList<Lego> result = new ArrayList<>();
			for (LegoBDB lego : uniqueLegos.values())
			{
				result.add(lego.toSchemaLego());
			}

			return result;
		}
		catch (DatabaseException e)
		{
			logger.error("Unexpected error reading data", e);
			throw new DataStoreException("Data read failure", e);
		}
		finally
		{
			if (pec != null)
			{
				try
				{
					pec.close();
				}
				catch (DatabaseException e)
				{
					logger.error("Unexpected error closing cursor", e);
				}
			}
			if (lec != null)
			{
				try
				{
					lec.close();
				}
				catch (DatabaseException e)
				{
					logger.error("Unexpected error closing cursor", e);
				}
			}
		}
	}

	@Override
	public CloseableIterator<Pncs> getPncs() throws DataStoreException
	{
		try
		{
			return new PncsBDBConvertingIterator(sec, pncsByUniqueId.entities());
		}
		catch (DatabaseException e)
		{
			logger.error("Unexpected error reading data", e);
			throw new DataStoreException("Data read failure", e);
		}
	}

	@Override
	public List<Pncs> getPncs(int id) throws DataStoreException
	{
		EntityCursor<PncsBDB> pec = null;
		try
		{
			// First go to the pncs table to get all of the pncs objects by ID, get a unique list of their unique IDs.
			ArrayList<Pncs> results = new ArrayList<>();
			EntityIndex<String, PncsBDB> pei = pncsById.subIndex(id);
			pec = pei.entities();

			for (PncsBDB current = pec.first(); current != null; current = pec.next())
			{
				while (current != null)
				{
					results.add(current.toSchemaPncs());
					current = pec.nextDup();
				}
			}
			pec.close();
			return results;
		}
		catch (DatabaseException e)
		{
			logger.error("Unexpected error reading data", e);
			throw new DataStoreException("Data read failure", e);
		}
		finally
		{
			if (pec != null)
			{
				try
				{
					pec.close();
				}
				catch (DatabaseException e)
				{
					logger.error("Unexpected error closing cursor", e);
				}
			}
		}
	}

	@Override
	public List<String> getLegoListByLego(String legoUUID)
	{
		EntityCursor<LegoListBDB> ec = null;
		try
		{
			ArrayList<String> uniqueLegoLists = new ArrayList<>();
			EntityIndex<String, LegoListBDB> ei = legoListByContainedLego.subIndex(legoUUID);
			ec = ei.entities();

			for (LegoListBDB current = ec.first(); current != null; current = ec.nextNoDup())
			{
				uniqueLegoLists.add(current.getLegoListUUID());
			}

			return uniqueLegoLists;
		}
		catch (DatabaseException e)
		{
			logger.error("Unexpected error reading data", e);
			throw new DataStoreException("Data read failure", e);
		}
		finally
		{
			if (ec != null)
			{
				try
				{
					ec.close();
				}
				catch (DatabaseException e)
				{
					logger.error("Unexpected error closing cursor", e);
				}
			}
		}
	}

	@Override
	public LegoList createLegoList(String uuid, String groupName, String groupDescription, String comments) throws WriteException
	{
		try
		{
			if (legoListByName.contains(groupName))
			{
				throw new WriteException("A legoList already exists with the name " + groupName);
			}
			LegoListBDB ll = new LegoListBDB(uuid, groupName, groupDescription, comments);
			legoListByUUID.put(ll);
			return ll.toSchemaLegoList();
		}
		catch (DatabaseException e)
		{
			logger.error("Unexpected error storing data", e);
			throw new WriteException("Unexpected Error storing data", e);
		}
	}

	@Override
	public void importLegoList(LegoList legoList) throws WriteException
	{
		if (legoListByUUID.contains(legoList.getLegoListUUID()))
		{
			String name = legoListByUUID.get(legoList.getLegoListUUID()).getGroupName();
			throw new WriteException("A legoList (" + name + ") already exists with the uuid " + legoList.getLegoListUUID());
		}

		if (legoListByName.contains(legoList.getGroupName()))
		{
			throw new WriteException("A legoList already exists with the name " + legoList.getGroupName());
		}

		Transaction txn = null;
		try
		{

			txn = myEnv.beginTransaction(null, null);
			LegoListBDB legoListBDB = new LegoListBDB(legoList);
			legoListByUUID.put(txn, legoListBDB);

			// legoList doesn't store the actual legos.
			for (LegoBDB legoBDB : legoListBDB.getLegoBDBs())
			{
				if (legoByUniqueId.contains(txn, legoBDB.getUniqueId(), LockMode.READ_UNCOMMITTED))
				{
					throw new WriteException("A lego in this lego list already exists in the system - uniqueID " + legoBDB.getUniqueId());
				}
				legoByUniqueId.put(txn, legoBDB);

				// Lego doesn't store stamp or pncs
				stampByUniqueId.put(txn, legoBDB.getStampBDB());
				pncsByUniqueId.put(txn, legoBDB.getPncsBDB());
			}
			txn.commit();
		}
		catch (WriteException | DatabaseException e)
		{
			if (txn != null)
			{
				try
				{
					txn.abort();
				}
				catch (DatabaseException ex)
				{
					logger.error("Unxpected error during abort", ex);
				}
			}
			if (e instanceof WriteException)
			{
				throw e;
			}
			logger.error("Unexpected error storing data", e);
			throw new WriteException("Unexpected error - LegoList was not stored", e);
		}
	}

	@Override
	public void deleteLego(String legoListUUID, String legoUUID, String stampUUID) throws WriteException
	{
		Transaction txn = null;
		try
		{
			LegoListBDB legoListBDB = legoListByUUID.get(legoListUUID);
			if (legoListBDB != null)
			{
				// verify that this lego is actually in this lego list...
				String legoUniqueIdToDelete = ModelUtil.makeUniqueLegoID(legoUUID, stampUUID);
				if (!legoListBDB.getUniqueLegoIds().contains(legoUniqueIdToDelete))
				{
					return;
				}

				txn = myEnv.beginTransaction(null, null);

				LegoBDB legoBDBToDelete = legoByUniqueId.get(txn, legoUniqueIdToDelete, LockMode.READ_UNCOMMITTED);

				// Lego doesn't store stamp, or pncs so delete them seperately
				if (legoBDBToDelete != null)
				{
					stampByUniqueId.delete(txn, legoBDBToDelete.getStampId());

					// pncs IDs might be in use by others. Only delete if not.
					boolean inUse = false;
					EntityCursor<LegoBDB> ec = null;
					try
					{
						EntityIndex<String, LegoBDB> ei = legoByPncsId.subIndex(legoBDBToDelete.getPncsId());
						ec = ei.entities(txn, CursorConfig.READ_UNCOMMITTED);
						for (LegoBDB current = ec.first(); current != null; current = ec.nextNoDup())
						{
							if (!current.getUniqueId().equals(legoUniqueIdToDelete))
							{
								inUse = true;
								break;
							}
						}
					}
					finally
					{
						ec.close();
					}

					if (!inUse)
					{
						pncsByUniqueId.delete(txn, legoBDBToDelete.getPncsId());
					}
				}

				// Remove the Lego (reference) from the LegoList
				legoListBDB.removeLego(legoUUID, legoUniqueIdToDelete);

				// Re-store the LegoList (minus the Lego ref)
				legoListByUUID.put(txn, legoListBDB);

				// one could check to see if the assertions from this LegoList are being used within another LegoList - but delete really isn't
				// intended to be used much in practice, so user beware.
				// Delete the Lego
				legoByUniqueId.delete(txn, legoUniqueIdToDelete);
			}
			txn.commit();
		}
		catch (DatabaseException e)
		{
			logger.error("Unexpected error storing data", e);
			if (txn != null)
			{
				try
				{
					txn.abort();
				}
				catch (DatabaseException ex)
				{
					logger.error("Unxpected error during abort", ex);
				}
			}
			throw new WriteException("Unexpected error during delete - Delete was not completed.", e);
		}
	}

	@Override
	public void deleteLegoList(String legoListUUID) throws WriteException
	{
		Transaction txn = null;
		try
		{
			LegoListBDB legoListBDB = legoListByUUID.get(legoListUUID);
			if (legoListBDB != null)
			{

				txn = myEnv.beginTransaction(null, null);

				// legoList doesn't store the actual legos.
				for (String legoUniqueIdToDelete : legoListBDB.getUniqueLegoIds())
				{
					LegoBDB legoBDBToDelete = legoByUniqueId.get(txn, legoUniqueIdToDelete, LockMode.READ_UNCOMMITTED);

					// Lego doesn't store stamp, or pncs so delete them seperately
					if (legoBDBToDelete != null)
					{
						stampByUniqueId.delete(txn, legoBDBToDelete.getStampId());

						// pncs IDs might be in use by others. Only delete if not.
						boolean inUse = false;
						EntityCursor<LegoBDB> ec = null;
						try
						{
							EntityIndex<String, LegoBDB> ei = legoByPncsId.subIndex(legoBDBToDelete.getPncsId());
							ec = ei.entities(txn, CursorConfig.READ_UNCOMMITTED);
							for (LegoBDB current = ec.first(); current != null; current = ec.nextNoDup())
							{
								if (!current.getUniqueId().equals(legoUniqueIdToDelete))
								{
									inUse = true;
									break;
								}
							}
						}
						finally
						{
							ec.close();
						}

						if (!inUse)
						{
							pncsByUniqueId.delete(txn, legoBDBToDelete.getPncsId());
						}
					}

					// one could check to see if the assertions from this LegoList are being used within another LegoList - but delete really isn't
					// intended to be used much in practice, so user beware.
					legoByUniqueId.delete(txn, legoUniqueIdToDelete);
				}

				// Finally, delete the legoList
				legoListByUUID.delete(txn, legoListUUID);
				txn.commit();
			}
		}
		catch (DatabaseException e)
		{
			logger.error("Unexpected error storing data", e);
			if (txn != null)
			{
				try
				{
					txn.abort();
				}
				catch (DatabaseException ex)
				{
					logger.error("Unxpected error during abort", ex);
				}
			}
			throw new WriteException("Unexpected error during delete - Delete was not completed.", e);
		}
	}

	@Override
	public Stamp commitLego(Lego lego, String legoListUUID) throws WriteException
	{
		Transaction txn = null;

		try
		{
			txn = myEnv.beginTransaction(null, null);

			LegoListBDB legoListBDB = legoListByUUID.get(legoListUUID);
			if (legoListBDB == null)
			{
				throw new WriteException("The specified legoList does not exist: " + legoListUUID);
			}

			Stamp s = lego.getStamp();
			if (s == null)
			{
				s = new Stamp();
				s.setStatus(UserPreferences.getDefaultStatus());
			}
			if (s.getAuthor() == null || s.getAuthor().length() == 0)
			{
				s.setAuthor(UserPreferences.getAuthor());
			}
			if (s.getModule() == null || s.getModule().length() == 0)
			{
				s.setModule(UserPreferences.getModule());
			}
			if (s.getPath() == null || s.getPath().length() == 0)
			{
				s.setPath(UserPreferences.getPath());
			}
			s.setTime(TimeConvert.convert(System.currentTimeMillis()));
			s.setUuid(UUID.randomUUID().toString());
			lego.setStamp(s);

			LegoBDB legoBDB = new LegoBDB(lego);
			legoListBDB.addLego(legoBDB);

			legoListByUUID.put(txn, legoListBDB);
			legoByUniqueId.put(txn, legoBDB);

			// Lego doesn't store stamp, or pncs
			stampByUniqueId.put(txn, legoBDB.getStampBDB());
			pncsByUniqueId.put(txn, legoBDB.getPncsBDB());

			txn.commit();
			return s;
		}
		catch (WriteException | DatabaseException e)
		{
			if (txn != null)
			{
				try
				{
					txn.abort();
				}
				catch (DatabaseException ex)
				{
					logger.error("Unxpected error during abort", ex);
				}
			}
			if (e instanceof WriteException)
			{
				throw e;
			}
			logger.error("Unexpected error storing data", e);
			throw new WriteException("Unexpected error storing to the DB - LegoList was not stored", e);
		}
	}

	/*
	 * Below here are non-public interface methods, which should not be used by outside (non BDB interface) code.
	 */
	public Lego getLegoByUniqueId(String uniqueId)
	{
		try
		{
			LegoBDB l = legoByUniqueId.get(uniqueId);
			if (l != null)
			{
				return l.toSchemaLego();
			}
			else
			{
				return null;
			}

		}
		catch (Exception e)
		{
			logger.error("Unexpected error reading data", e);
			throw new DataStoreException("Data read failure", e);
		}
	}

	public StampBDB getStampByUniqueId(String uniqueId)
	{
		try
		{
			return stampByUniqueId.get(uniqueId);
		}
		catch (Exception e)
		{
			logger.error("Unexpected error reading data", e);
			throw new DataStoreException("Data read failure", e);
		}
	}
	
	@Override
	public Pncs getPncs(int id, String value)
	{
		return getPncsByUniqueId(PncsBDB.makeUniqueId(id, value));
	}

	public Pncs getPncsByUniqueId(String uniqueId)
	{
		try
		{
			PncsBDB pncs = pncsByUniqueId.get(uniqueId);
			if (pncs == null)
			{
				return null;
			}
			else
			{
				return pncs.toSchemaPncs();
			}
		}
		catch (Exception e)
		{
			logger.error("Unexpected error reading data", e);
			throw new DataStoreException("Data read failure", e);
		}
	}

	public Set<String> getLegoUUIDsContainingAssertion(String assertionUUID)
	{
		EntityCursor<LegoBDB> ec = null;
		try
		{
			HashSet<String> result = new HashSet<>();
			EntityIndex<String, LegoBDB> ei = legoByContainingAssertionUUID.subIndex(assertionUUID);
			ec = ei.entities();

			for (LegoBDB current = ec.first(); current != null; current = ec.nextNoDup())
			{
				// Don't need to check the dupes - they should all point to the same lego
				result.add(current.getLegoUUID());
			}

			return result;
		}
		catch (DatabaseException e)
		{
			logger.error("Unexpected error reading data", e);
			throw new DataStoreException("Data read failure", e);
		}
		finally
		{
			if (ec != null)
			{
				try
				{
					ec.close();
				}
				catch (DatabaseException e)
				{
					logger.error("Unexpected error closing cursor", e);
				}
			}
		}
	}

	@Override
	public void updateLegoListMetadata(String legoListUUID, String groupName, String groupDescription, String comments) throws WriteException
	{
		try
		{
			LegoListBDB ll = legoListByUUID.get(legoListUUID);
			if (ll == null)
			{
				throw new WriteException("Could not find the LegoList to update");
			}
			else
			{
				if (groupName != null && !(ll.getGroupName().equals(groupName)))
				{
					if (groupName.length() == 0)
					{
						throw new WriteException("No group name specified");
					}
					if (legoListByName.contains(groupName))
					{
						throw new WriteException("A legoList already exists with the name " + groupName);
					}
					ll.setGroupName(groupName);
				}
				if (groupDescription != null)
				{
					ll.setGroupDescription(groupDescription);
				}
				if (comments != null)
				{
					ll.setComment(comments);
				}
				if (groupDescription != null || comments != null)
				{
					legoListByUUID.put(ll);
				}
			}
		}
		catch (DatabaseException e)
		{
			logger.error("Unexpected error writing data", e);
			throw new DataStoreException("Data write failure", e);
		}
	}
}
