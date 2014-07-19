package gov.va.isaac.mojos.dbBuilder;

import gov.va.isaac.AppContext;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.otf.tcc.api.io.FileIO;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.ihtsdo.otf.tcc.model.index.service.IndexerBI;

/**
 * Goal which loads a database from an eConcept file, and generates the indexes.
 * 
 * @goal shutdown-terminology-store
 * 
 * @phase process-sources
 */
public class Shutdown extends AbstractMojo {

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
   * @parameter
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
      getLog().info("Shutdown terminology store");
      // ASSUMES setup has run already
      TerminologyStoreDI store =
          AppContext.getService(TerminologyStoreDI.class);
      List<IndexerBI> indexers =
          AppContext.getServiceLocator().getAllServices(IndexerBI.class);

      // Assume this was run with setup and 
      // the process started with mvn clean
      for (IndexerBI indexer : indexers) {
        indexer.setEnabled(true);
      }
      getLog().info("  Batch Indexing");
      store.index();

      getLog().info("  Shutting Down");
      store.shutdown();
      for (IndexerBI indexer : indexers) {
        indexer.closeWriter();
      }

      if (moveToReadOnly) {
        getLog().info("moving mutable to read-only");
        File readOnlyDir = new File(bdbFolderLocation, "read-only");
        FileIO.recursiveDelete(readOnlyDir);
        File mutableDir = new File(bdbFolderLocation, "mutable");
        mutableDir.renameTo(readOnlyDir);
      }
      
      getLog().info("Done shutting down terminology store");
    } catch (Exception e) {
      throw new MojoExecutionException("Database build failure", e);
    }
  }
}
