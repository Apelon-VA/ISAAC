package gov.va.isaac.mojos.dbBuilder;

import gov.va.isaac.AppContext;

import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
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

      getLog().info("Done shutting down terminology store");
    } catch (Exception e) {
      throw new MojoExecutionException("Database build failure", e);
    }
  }
}
