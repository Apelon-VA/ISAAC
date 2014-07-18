package gov.va.isaac.mojos.dbBuilder;

import gov.va.isaac.AppContext;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.otf.query.lucene.LuceneIndexer;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;
import org.ihtsdo.otf.tcc.model.index.service.IndexerBI;

/**
 * Goal which loads a database from an eConcept file, and generates the indexes.
 * 
 * @goal setup-terminology-store
 * 
 * @phase process-sources
 */
public class Setup extends AbstractMojo {

  /**
   * Location of the file.
   * 
   * @parameter expression="${project.build.directory}/berkeley-db"
   * @required
   */
  private String bdbFolderLocation;

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.maven.plugin.Mojo#execute()
   */
  @Override
  public void execute() throws MojoExecutionException {
    try {
      getLog().info("Setup terminology store");
      File bdbFolderFile = new File(bdbFolderLocation);
      boolean dbExists = bdbFolderFile.exists();

      System.setProperty(BdbTerminologyStore.BDB_LOCATION_PROPERTY,
          bdbFolderLocation);
      System.setProperty(LuceneIndexer.LUCENE_ROOT_LOCATION_PROPERTY,
          bdbFolderLocation);

      getLog().info("  Setup AppContext, bdb location = " + bdbFolderLocation);
      AppContext.setup();

      // TODO OTF fix: this needs to be fixed so I don't have to hack it with
      // reflection.... https://jira.ihtsdotools.org/browse/OTFISSUE-11
      Field f = Hk2Looker.class.getDeclaredField("looker");
      f.setAccessible(true);
      f.set(null, AppContext.getServiceLocator());

      getLog().info("  Test loading terminology store");
      AppContext.getService(TerminologyStoreDI.class);
      getLog().info("  Test locating indexing services");
      List<IndexerBI> indexers =
          AppContext.getServiceLocator().getAllServices(IndexerBI.class);
      getLog().info("  Located " + indexers.size() + " indexers");

      if (!dbExists) {
        getLog().info("  DB did not exist - disabling indexers for batch index");
        for (IndexerBI indexer : indexers) {
          indexer.setEnabled(false);
        }
      }      
      getLog().info("Done setting up terminology store");
    } catch (Exception e) {
      throw new MojoExecutionException("Database build failure", e);
    }
  }
}
