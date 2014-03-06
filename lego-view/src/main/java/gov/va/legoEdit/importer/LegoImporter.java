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
package gov.va.legoEdit.importer;

import gov.va.legoEdit.formats.LegoXMLUtils;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.model.schemaModel.LegoList;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import gov.va.legoEdit.storage.wb.WBUtility;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LegoImporter - The actual import logic, intended to be run in a background thread.
 * Callbacks are used to signal the GUI that things can be updated during the import, 
 * and again upon completion.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class LegoImporter implements Runnable
{
	private List<File> files;
	int count = 0;
	StringBuilder status = new StringBuilder();
	HashMap<String, Concept> missingConcepts = new HashMap<>();
	ImportStatusCallback callback;
	ArrayList<String> importedLegoListIds = new ArrayList<String>();
	
	Logger logger = LoggerFactory.getLogger(LegoImporter.class);

	public LegoImporter(List<File> files, ImportStatusCallback callback)
	{
		this.files = files;
		this.callback = callback;
	}

	@Override
	public void run()
	{
		try
		{
			for (final File f : files)
			{
				final String temp = status.toString();
				status.setLength(0);
				callback.setCurrentItemName("Importing " + f.getName() + "...");
				callback.setProgress((double) count / (double) files.size());
				callback.appendDetails(temp);

				if (f.exists() && f.isFile())
				{
					try
					{
						String fileName = f.getName().toLowerCase();
						if (fileName.endsWith(".xml"))
						{
							try
							{
								LegoXMLUtils.validate((f));
							}
							catch (Exception e)
							{
								status.append("Warning - The file '" + f.getName() + "' is not schema valid.  Will attempt to import, but may fail.");
								status.append("  The schema error was: " + e.getMessage());
								status.append(System.getProperty("line.separator"));
								status.append(System.getProperty("line.separator"));
							}
							LegoList ll = LegoXMLUtils.readLegoList(f);
							List<Concept> failures = WBUtility.lookupAllConcepts(ll);
							BDBDataStoreImpl.getInstance().importLegoList(ll);
							importedLegoListIds.add(ll.getLegoListUUID());
							for (Concept c : failures)
							{
								if (c.getSctid() != null)
								{
									missingConcepts.put(c.getSctid() + "", c);
								}
								else if (c.getUuid() != null && c.getUuid().length() > 0)
								{
									missingConcepts.put(c.getUuid(), c);
								}
								else
								{
									missingConcepts.put(c.getDesc(), c);
								}
							}
						}
						else
						{
							status.append("Warning - The file '" + f.getName() + "' is does not have a supported file extension.  Must be .xml  "
									+ "File has been skipped.");
							status.append(System.getProperty("line.separator"));
							status.append(System.getProperty("line.separator"));
						}
						status.append("Completed " + f.getName());
					}
					catch (Exception ex)
					{
						logger.info("Error loading file " + f.getName(), ex);
						status.append("Error loading file " + f.getName() + ": ");
						status.append((ex.getLocalizedMessage() == null ? ex.toString() : ex.getLocalizedMessage()));
					}
				}
				else
				{
					status.append("Skipped " + f.getName());
				}

				status.append(System.getProperty("line.separator"));
				status.append(System.getProperty("line.separator"));
				count++;
			}
			
			//TODO cleanup
			//LegoGUI.getInstance().getLegoGUIController().getCommonlyUsedConcept().rebuildDBStats();
			
			callback.appendDetails(status.toString());
//TODO cleanup - this is where we would update whatever shows the full list of Legos...
//			importName.setText("Updating Editor");
//			progress.setProgress(99.0);
//			LegoGUIModel.getInstance().updateLegoLists();
			callback.setProgress(100.0);
			callback.setCurrentItemName("Import Complete");
			if (missingConcepts.size() > 0)
			{
				StringBuilder temp = new StringBuilder();
				temp.append("Some concepts specified in the imported Legos do not exist in the SCT DB or the pending concepts file:");
				temp.append(System.getProperty("line.separator"));
				for (Concept c : missingConcepts.values())
				{
					temp.append(c.getSctid() + "\t" + c.getDesc() + (c.getUuid() != null ? "\t" + c.getUuid() : ""));
					temp.append(System.getProperty("line.separator"));
				}
				callback.appendDetails(temp.toString());
			}
		}
		finally
		{
			callback.importComplete(importedLegoListIds);
		}
	}
}
