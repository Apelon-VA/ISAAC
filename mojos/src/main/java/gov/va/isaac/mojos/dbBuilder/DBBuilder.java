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
package gov.va.isaac.mojos.dbBuilder;

import gov.va.isaac.AppContext;
import gov.va.isaac.constants.ISAAC;
import gov.va.isaac.constants.InformationModels;
import gov.va.isaac.constants.MappingConstants;
import gov.va.isaac.constants.Search;
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.otf.mojo.GenerateMetadataEConcepts;
import org.ihtsdo.otf.query.lucene.LuceneIndexer;
import org.ihtsdo.otf.tcc.api.io.FileIO;
import org.ihtsdo.otf.tcc.api.metadata.binding.RefexDynamic;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.api.spec.RelSpec;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;
import org.ihtsdo.otf.tcc.model.index.service.IndexerBI;

/**
 * Goal which loads a database from an eConcept file, and generates the indexes.
 * 
 * @goal load-index-bdb
 * 
 * @phase process-sources
 */
public class DBBuilder extends AbstractMojo
{

	/**
	 * true if the mutable database should replace the read-only database after
	 * load is complete.  Only applicable if setupAndShutdown is set to true, otherwise, ignored.
	 * Also ignored if the bdbFolderLocation already contains a database.
	 * 
	 * @parameter default-value=true
	 * @required
	 */
	private boolean moveToReadOnly = true;

	/**
	 * true if this mojo should handle setup/shutdown operations.  False if it should assume the DB is already open, 
	 * and retreieve it from HK2.
	 * 
	 * If setting to false, assume you are using {@link Setup} and {@link Shutdown} before and after this mojo call.
	 * 
	 * @parameter default-value=true
	 * @required
	 */
	private boolean setupAndShutdown = true;

	/**
	 * Location of the file.  Ignored if setupAndShutdown is set to false
	 * 
	 * @parameter expression="${project.build.directory}/berkeley-db"
	 * @required
	 */
	private String bdbFolderLocation;

	/**
	 * <code>eConcept format</code> files to import.
	 * 
	 * @parameter
	 * @required
	 */
	private String[] econFileStrings;
	
	/**
	 * UUIDs of root concepts from the terminologies that are being loaded which should be added
	 * as children of {@link ISAAC#ISAAC_ROOT}.  Eventually specifying these should be unecssary, 
	 * as eConcepts file should specify their own root concepts via a refex membership.  But many 
	 * eConcept files don't yet contain that metadata.
	 * @parameter
	 */
	private MojoConceptSpec[] terminologyRoots;

	/**
	 * Whether or not to include the default metadata concepts in the constructed
	 * DB. This includes concepts that would be created for ConceptSpec entries in {@link RefexDynamic}, for example.
	 * Defaults to true, there may not actually be a use case where you wouldn't want this...
	 * 
	 * @parameter default-value=true
	 * @required
	 */
	private boolean loadDefaultMetadata = true;

	@Override
	public void execute() throws MojoExecutionException
	{
		try
		{
			getLog().info("Start loading database data from econcepts files.");
			long startTime = System.currentTimeMillis();
			File bdbFolderFile = new File(bdbFolderLocation);
			boolean dbExists = bdbFolderFile.exists();

			if (setupAndShutdown)
			{
				System.setProperty(BdbTerminologyStore.BDB_LOCATION_PROPERTY, bdbFolderLocation);
				System.setProperty(LuceneIndexer.LUCENE_ROOT_LOCATION_PROPERTY, bdbFolderLocation);

				AppContext.setup();

				// TODO OTF fix: this needs to be fixed so I don't have to hack it with
				// reflection.... https://jira.ihtsdotools.org/browse/OTFISSUE-11
				Field f = Hk2Looker.class.getDeclaredField("looker");
				f.setAccessible(true);
				f.set(null, AppContext.getServiceLocator());
			}

			TerminologyStoreDI store = AppContext.getService(TerminologyStoreDI.class);
			List<IndexerBI> indexers = AppContext.getServiceLocator().getAllServices(IndexerBI.class);

			getLog().info("Located " + indexers.size() + " indexers");
			getLog().info("Disabling indexers for batch index");
			for (IndexerBI indexer : indexers)
			{
				indexer.setEnabled(false);
			}

			//Classes listed and processed here will have any ConceptSpec entries automatically turned into Concepts in the DB (if they don't exist)
			String[] classesWithDefaultSpecConcepts = new String[] {};
			if (loadDefaultMetadata)
			{
				getLog().info("Will load default metadata");
				classesWithDefaultSpecConcepts = new String[] {RefexDynamic.class.getName(), Search.class.getName(), ISAAC.class.getName(), 
						InformationModels.class.getName(), MappingConstants.class.getName()};
			}
			
			ConceptSpec[] conceptsToAddAsRoots = new ConceptSpec[] {};
			if (terminologyRoots != null && terminologyRoots.length > 0)
			{
				getLog().info("Will create " + terminologyRoots.length + " additional root concepts");
				conceptsToAddAsRoots = new ConceptSpec[terminologyRoots.length];
				for (int i = 0; i < conceptsToAddAsRoots.length; i++)
				{
					conceptsToAddAsRoots[i] = terminologyRoots[i].getConceptSpec();
					//Need to do stated and inferred, otherwise, we can't browse, on inferred mode, nor on inferred_then_stated mode
					conceptsToAddAsRoots[i].setRelSpecs(new RelSpec[] {new RelSpec(conceptsToAddAsRoots[i], Snomed.IS_A, ISAAC.ISAAC_ROOT), //stated
							new RelSpec(conceptsToAddAsRoots[i], Snomed.IS_A, ISAAC.ISAAC_ROOT, SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2)}); //inferred  
				}
			}
			
			if (classesWithDefaultSpecConcepts.length > 0 || conceptsToAddAsRoots.length > 0)
			{
				getLog().info("Creating and loading the metadata and additional root concepts");
				File metaData = File.createTempFile("WBMetaData-", ".jbin");
				
				GenerateMetadataEConcepts gmc = new GenerateMetadataEConcepts(metaData, classesWithDefaultSpecConcepts, conceptsToAddAsRoots, false);
				gmc.execute();
				store.loadEconFiles(new File[] { metaData });
				metaData.delete();
			}
			
			getLog().info("Loading specified data files");
			store.loadEconFiles(econFileStrings);
			
			for (IndexerBI indexer : indexers)
			{
				indexer.setEnabled(true);
			}
			getLog().info("Batch Indexing");
			store.index();

			if (setupAndShutdown)
			{
				getLog().info("Shutting Down");
				store.shutdown();

				if (!dbExists && moveToReadOnly)
				{
					//TODO OTF Bug - figure out why on earth we need this arbitrary sleep.
					Thread.sleep(5000);
					getLog().info("moving mutable to read-only");
					File readOnlyDir = new File(bdbFolderLocation, "read-only");
					FileIO.recursiveDelete(readOnlyDir);
					File mutableDir = new File(bdbFolderLocation, "mutable");
					mutableDir.renameTo(readOnlyDir);
				}
			}

			getLog().info("Done building db from econcepts files.  Elapsed Time: " + ((System.currentTimeMillis() - startTime) / 1000) + " seconds");
		}
		catch (Exception e)
		{
			throw new MojoExecutionException("Database build failure", e);
		}
	}

	/**
	 * Alt application entry point.
	 *
	 * @param args the command line arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception
	{
		DBBuilder foo = new DBBuilder();
		foo.bdbFolderLocation = "target/berkeley-db";
		FileIO.recursiveDelete(new File(foo.bdbFolderLocation));
		foo.econFileStrings = new String[] { "/mnt/d/scratch/sct-econcept-2014.01.31-build-3-active-only.jbin" };
		foo.moveToReadOnly = true;
		foo.execute();
	}
}
