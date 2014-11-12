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
package gov.va.isaac.config.changesets;

import gov.va.isaac.AppContext;
import gov.va.isaac.config.profiles.UserProfileManager;
import gov.va.isaac.interfaces.utility.ServicesToPreloadI;
import gov.va.isaac.util.Utility;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Singleton;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.model.cs.ChangeSetReader;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ChangesetProcessor}
 *
 * Utility code to process all changesets found in the user profile folder.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Singleton
public class ChangesetProcessor implements ServicesToPreloadI
{
	private static Logger logger = LoggerFactory.getLogger(ChangesetProcessor.class);
	
	private ChangesetProcessor()
	{
		//for HK2
	}

	/**
	 * @see gov.va.isaac.interfaces.utility.ServicesToPreloadI#loadRequested()
	 */
	@Override
	public void loadRequested()
	{
		Utility.execute(() -> processChangeSets(AppContext.getService(UserProfileManager.class).getProfilesFolder()));
	}

	/**
	 * @see gov.va.isaac.interfaces.utility.ServicesToPreloadI#shutdown()
	 */
	@Override
	public void shutdown()
	{
		//noop
	}

	/**
	 * recursively process any changeset (*.eccs) files found in the specified folder.
	 * @param rootFolder
	 * @throws IOException 
	 */
	public static void processChangeSets(File rootFolder)
	{
		try
		{
			AppContext.getService(BdbTerminologyStore.class).suspendChangeNotifications();
			Files.walkFileTree(rootFolder.toPath(), new SimpleFileVisitor<Path>()
			{
				/**
				 * @see java.nio.file.SimpleFileVisitor#visitFile(java.lang.Object, java.nio.file.attribute.BasicFileAttributes)
				 */
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
				{
					if (file.toFile().getName().toLowerCase().endsWith(".eccs"))
					{
						try
						{
							logger.info("Processing {}", file);
							ChangeSetReader csr = new ChangeSetReader();
							csr.setChangeSetFile(file.toFile());
							Set<ConceptChronicleBI> indexedAnnotationConcepts = new HashSet<>();
							csr.read(indexedAnnotationConcepts);
							if (indexedAnnotationConcepts.size() > 0)
							{
								logger.info("Dan doesn't know what to do with this after change set processing: {}", indexedAnnotationConcepts);
							}
						}
						catch (Exception e)
						{
							logger.error("Error processing changeset file " + file, e);
						}
					}
					return FileVisitResult.CONTINUE;
				}
				
			});
		}
		catch (IOException e)
		{
			logger.error("Error walking profiles folder for changesets - " + rootFolder, e);
		}
		finally
		{
			AppContext.getService(BdbTerminologyStore.class).resumeChangeNotifications();
		}
	}
}
