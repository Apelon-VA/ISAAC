package gov.va.isaac.mojos.dbBuilder;

import gov.va.isaac.AppContext;
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.otf.query.lucene.LuceneIndexer;
import org.ihtsdo.otf.tcc.api.io.FileIO;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.RefexDynamicData;
import org.ihtsdo.otf.tcc.model.index.service.IndexerBI;
import org.ihtsdo.otf.mojo.GenerateMetadataEConcepts;

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
	 * load is complete.
	 * 
	 * @parameter default-value=true
	 * @required
	 */
	private boolean moveToReadOnly = true;

	/**
	 * Location of the file.
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
	 * Whether or not to include the default metadata concepts in the constructed DB.
	 * This includes concepts that would be created for ConceptSpec entries in {@link RefexDynamic}, 
	 * for example.
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
			long startTime = System.currentTimeMillis();
			File bdbFolderFile = new File(bdbFolderLocation);
			boolean dbExists = bdbFolderFile.exists();
			
			System.setProperty(BdbTerminologyStore.BDB_LOCATION_PROPERTY, bdbFolderLocation);
			System.setProperty(LuceneIndexer.LUCENE_ROOT_LOCATION_PROPERTY, bdbFolderLocation);
			
			AppContext.setup();
			
			// TODO OTF fix: this needs to be fixed so I don't have to hack it with reflection.... https://jira.ihtsdotools.org/browse/OTFISSUE-11
			Field f = Hk2Looker.class.getDeclaredField("looker");
			f.setAccessible(true);
			f.set(null, AppContext.getServiceLocator());
			
			TerminologyStoreDI store = AppContext.getService(TerminologyStoreDI.class);
			List<IndexerBI> indexers = AppContext.getServiceLocator().getAllServices(IndexerBI.class);
			
			getLog().info("Located " + indexers.size() + " indexers");
			
			if (!dbExists)
			{
				getLog().info("DB did not exist - disabling indexers for batch index");
				for (IndexerBI indexer : indexers)
				{
					indexer.setEnabled(false);
				}
			}
			store.loadEconFiles(econFileStrings);
			if (loadDefaultMetadata)
			{
				getLog().info("Creating and loading the metadata");
				File metaData = File.createTempFile("WBMetaData-", ".jbin");
				GenerateMetadataEConcepts gmc = new GenerateMetadataEConcepts(metaData, new String[] {"org.ihtsdo.otf.tcc.api.metadata.binding.RefexDynamic"},
						new ConceptSpec[0], false);
				gmc.execute();
				store.loadEconFiles(new File[] {metaData});
				metaData.delete();
			}

			if (!dbExists)
			{
				for (IndexerBI indexer : indexers)
				{
					indexer.setEnabled(true);
				}
				getLog().info("Batch Indexing");
				store.index();
			}

			getLog().info("Shutting Down");
			
			store.shutdown();
			for (IndexerBI indexer : indexers)
			{
				indexer.closeWriter();
			}
			

			if (!dbExists && moveToReadOnly)
			{
				getLog().info("moving mutable to read-only");
				File readOnlyDir = new File(bdbFolderLocation, "read-only");
				FileIO.recursiveDelete(readOnlyDir);
				File mutableDir = new File(bdbFolderLocation, "mutable");
				mutableDir.renameTo(readOnlyDir);
			}

			getLog().info("Done building the DB.  Elapsed Time: " + ((System.currentTimeMillis() - startTime)/1000) + " seconds");
		}
		catch (Exception e)
		{
			throw new MojoExecutionException("Database build failure", e);
		}
	}

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
