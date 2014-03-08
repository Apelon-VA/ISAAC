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
package gov.va.legoEdit.model;

import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.storage.wb.WBUtility;
import gov.va.legoEdit.util.Utility;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * PendingConcepts
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 */
public class PendingConcepts implements Observable
{
	public static File pendingConceptsFile = new File("pendingConcepts.tsv");
	private static Logger logger = LoggerFactory.getLogger(PendingConcepts.class);
	private HashMap<String, PendingConcept> pendingConcepts = new HashMap<>();  //UUID to concept
	private HashMap<Long, Concept> parentConcepts = new HashMap<>();
	private long highestInUseId = 0;
	private static volatile PendingConcepts instance_;
	private ArrayList<InvalidationListener> listeners_ = new ArrayList<>();
	private volatile boolean loadCompleted = false;

	public static PendingConcepts getInstance()
	{
		if (instance_ == null)
		{
			synchronized (PendingConcepts.class)
			{
				if (instance_ == null)
				{
					instance_ = new PendingConcepts();
				}
			}
		}
		return instance_;
	}

	private PendingConcepts()
	{
		Utility.tpe.submit(new ReadData());
	}
	
	private void loadCheck()
	{
		while (!loadCompleted)
		{
			synchronized (PendingConcepts.class)
			{
				if (!loadCompleted)
				{
					try
					{
						PendingConcepts.class.wait();
					}
					catch (InterruptedException e)
					{
						//noop
					}
				}
			}
		}
	}
	
	public List<PendingConcept> getPendingConcepts()
	{
		loadCheck();
		ArrayList<PendingConcept> results = new ArrayList<>(pendingConcepts.size());
		results.addAll(pendingConcepts.values());
		return results;
	}
	
	private boolean areIdentifiersUnique(Concept potentialPendingConcept)
	{
		//no load check here - will cause a startup deadlock.  
		if (pendingConcepts.containsKey(potentialPendingConcept.getUuid()))
		{
			return false;
		}

		if (null != WBUtility.lookupSnomedIdentifierAsCV(potentialPendingConcept.getSctid().toString()))
		{
			return false;
		}
		if (potentialPendingConcept.getUuid() != null && null != WBUtility.lookupSnomedIdentifierAsCV(potentialPendingConcept.getUuid().toString()))
		{
			return false;
		}
		return true;
	}
	
	public void addConcept(long id, String description, Long parent) throws IllegalArgumentException
	{
		loadCheck();
		PendingConcept c = new PendingConcept();
		c.setSctid(id);
		c.setDesc(description);
		c.setUuid(UUID.nameUUIDFromBytes((c.getSctid() + "").getBytes()).toString());
		if (areIdentifiersUnique(c))
		{
			pendingConcepts.put(c.getUuid(), c);
			if (c.getSctid() > highestInUseId)
			{
				highestInUseId = c.getSctid();
			}
			if (parent != null)
			{
				//allow ref to pending, but not self....
				Concept parentConcept =  WBUtility.lookupSnomedIdentifier(parent + "");
				if (parentConcept != null && parentConcept.getSctid().longValue() != id)
				{
					parentConcepts.put(id, parentConcept);
				}
				else
				{
					throw new IllegalArgumentException("The specified parent SCTID isn't a valid snomed concept");
				}
			}
			notifyListeners();
		}
		else
		{
			throw new IllegalArgumentException("The provided concept is not unique");
		}
		try
		{
			rewritePendingConceptsFile();
		}
		catch (IOException e)
		{
			logger.error("Pending concepts Store failed", e);
			pendingConcepts.remove(c.getUuid());
			parentConcepts.remove(id);
			throw new IllegalArgumentException("Sorry, store failed");
		}
	}
	
	public void deleteConcept(long id) throws IllegalArgumentException
	{
		loadCheck();
		PendingConcept pending = pendingConcepts.remove(UUID.nameUUIDFromBytes((id + "").getBytes()).toString());
		Concept parent = parentConcepts.remove(id);
		try
		{
			rewritePendingConceptsFile();
			notifyListeners();
		}
		catch (IOException e)
		{
			logger.error("Pending concepts Store failed", e);
			pendingConcepts.put(pending.getUuid(),  pending);
			if(parent != null)
			{
				parentConcepts.put(id, parent);
			}
			throw new IllegalArgumentException("Sorry, store failed");
		}
	}
	
	public long getUnusedId()
	{
		loadCheck();
		while (true)
		{
			long temp = ++highestInUseId;
			Concept possible = new PendingConcept();
			possible.setSctid(temp);
			if (areIdentifiersUnique(possible))
			{
				return temp;
			}
		}
	}
	
	private void rewritePendingConceptsFile() throws IOException
	{
		//Read through the existing file, keeping the comments, and any lines we don't understand.  
		//only keep the concepts if they our in our current list.  Finally, add any concepts that are missing.
		loadCheck();
		HashSet<String> unstoredConcepts = new HashSet<>();
		unstoredConcepts.addAll(pendingConcepts.keySet());
		
		List<String> lines = Files.readAllLines(pendingConceptsFile.toPath(), StandardCharsets.UTF_8);
		StringBuilder replacement = new StringBuilder();
		String eol = System.getProperty("line.separator");
		for (String line : lines)
		{
			if (line.startsWith("#") || line.length() == 0)
			{
				replacement.append(line);
				replacement.append(eol);
			}
			else
			{
				String[] parts = line.split("\t");
				try
				{
					long id = Long.parseLong(parts[0]);
					String uuid = UUID.nameUUIDFromBytes((id + "").getBytes()).toString();
					if (pendingConcepts.containsKey(uuid))
					{
						replacement.append(buildLine(pendingConcepts.get(uuid)));
						replacement.append(eol);
						unstoredConcepts.remove(uuid);
					}
					
				}
				catch (Exception e) 
				{
					replacement.append(line);
					replacement.append(eol);
				}
			}
		}
		for (String uuid : unstoredConcepts)
		{
			replacement.append(buildLine(pendingConcepts.get(uuid)));
			replacement.append(eol);
		}
		
		Files.write(pendingConceptsFile.toPath(), replacement.toString().getBytes(), 
				StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
	}
	
	private String buildLine(Concept c)
	{
		Concept parent = parentConcepts.get(c.getSctid());
		return c.getSctid() + "\t" + c.getDesc() + (parent == null ? "" : "\t" + parent.getSctid() + "\t" + parent.getDesc());
	}

	public PendingConcept getConcept(String sctIdOrUUID)
	{
		if (sctIdOrUUID == null)
		{
			return null;
		}
		loadCheck();
		PendingConcept temp = pendingConcepts.get(sctIdOrUUID.trim());
		if (temp == null)
		{
			temp = pendingConcepts.get(UUID.nameUUIDFromBytes((sctIdOrUUID.trim() + "").getBytes()).toString()); 
		}
		return temp;
	}
	
	/**
	 * Two warnings about this method:
	 * It only accepts UUIDs, not SCTIDs.  SCTIDs will always return false.
	 * It doesn't check if the load is complete - if called before the load completes, it may return false when it should return true.
	 */
	public boolean hasConcept(String conceptUUIDIdentifier)
	{
		return pendingConcepts.containsKey(conceptUUIDIdentifier);
	}
	
	/**
	 * May be a PendingConcept instead of Concept, but you will have to check
	 */
	public Concept getParentConcept(long pendingConceptId)
	{
		loadCheck();
		return parentConcepts.get(pendingConceptId);
	}
	
	private void notifyListeners()
	{
		for (InvalidationListener il : listeners_)
		{
			il.invalidated(this);
		}
	}

	@Override
	public void addListener(InvalidationListener arg0)
	{
		listeners_.add(arg0);
	}

	@Override
	public void removeListener(InvalidationListener arg0)
	{
		listeners_.remove(arg0);
	}
	
	private class ReadData implements Runnable
	{
		@Override
		public void run()
		{
			logger.info("Loading pending concepts from: " + pendingConceptsFile.getAbsolutePath());
			try
			{
				if (pendingConceptsFile.exists())
				{
					List<String> lines = Files.readAllLines(pendingConceptsFile.toPath(), StandardCharsets.UTF_8);
					for (String s : lines)
					{
						if (s.startsWith("#") || s.length() == 0)
						{
							continue;
						}
						String[] parts = s.split("\t");
						if (parts.length > 1)
						{
							PendingConcept c = new PendingConcept();
							try
							{
								c.setSctid(Long.parseLong(parts[0]));
							}
							catch (NumberFormatException e)
							{
								logger.error("Invalid ID in pending concepts file - line '" + s + "'");
								continue;
							}
							c.setDesc(parts[1]);
							c.setUuid(UUID.nameUUIDFromBytes(parts[0].getBytes()).toString());
							
							Concept parent = null;

							if (parts.length > 2)
							{
								long parentSCTID = Long.parseLong(parts[2]);
								//Use this lookup, since it doesn't loop back to pending.
								ConceptVersionBI wbParentConcept =  WBUtility.lookupSnomedIdentifierAsCV(parentSCTID + "");
								if (wbParentConcept == null)
								{
									//See if it is a different pending concept (must be higher in the file, for this to work)
									parent = pendingConcepts.get(UUID.nameUUIDFromBytes((parentSCTID + "").getBytes()).toString());
								}
								else if (wbParentConcept != null)
								{
									parent = WBUtility.convertConcept(wbParentConcept);
								}
								if (parent == null)
								{
									logger.error("The specified parent concept for " + c.getSctid() + " doesn't exist and will be ignored");
								}
							}
							
							if (!areIdentifiersUnique(c))
							{
								logger.error("Pending concepts contains a value which is a duplicate, or already exists in snomed '" + c.getSctid() + "'.  Ignoring.");
								continue;
							}
							
							pendingConcepts.put(c.getUuid(), c);
							if (c.getSctid() > highestInUseId)
							{
								highestInUseId = c.getSctid();
							}
							if (parent != null)
							{
								parentConcepts.put(c.getSctid(), parent);
							}
						}
						else
						{
							logger.error("Pending concepts need an ID and a description");
						}
					}
				}
			}
			catch (IOException e)
			{
				logger.error("Unexpected error loading pending concepts file", e);
			}
			synchronized (PendingConcepts.class)
			{
				loadCompleted = true;
				PendingConcepts.class.notifyAll();
			}
			logger.info("Loaded " + pendingConcepts.size() + " pending concepts");
		}
		
	}
}
