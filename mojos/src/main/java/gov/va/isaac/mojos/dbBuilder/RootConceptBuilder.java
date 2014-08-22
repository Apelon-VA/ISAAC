package gov.va.isaac.mojos.dbBuilder;

import gov.va.isaac.AppContext;
import java.io.IOException;
import java.util.UUID;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.otf.tcc.api.blueprint.ConceptCB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RelationshipCAB;
import org.ihtsdo.otf.tcc.api.blueprint.TerminologyBuilderBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.lang.LanguageCode;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipType;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.ihtsdo.otf.tcc.datastore.BdbTermBuilder;

/**
 * Goal which create root concept
 * 
 * @goal create-root-concept
 * 
 * @phase process-sources
 */
public class RootConceptBuilder extends AbstractMojo {

  /**
   * FSN for root concept 
   * @parameter
   * @required
   */
  private String rootFsn;

  /**
   * PT for root concept 
   * @parameter
   * @required
   */
  private String rootPrefTerm;

  /**
   * FSN for each child
   * @parameter
   */
  private String[] childFsns;

  /**
   * PT for each child
   * @parameter
   */
  private String[] childPrefTerms;

  /**
   * UUID for each child
   * @parameter
   */
  private String[] childUuids;

  /** The data store. */
  private TerminologyStoreDI dataStore = null;

  /**
   * To execute this mojo, you need to first have run the "Setup" mojo against
   * the same database. Here, we assume the data store is ready to go and we can
   * acquire it simply as shown in the createPath method below.
   * 
   * If not yet initialized, this will fail.
   */
  @Override
  public void execute() throws MojoExecutionException {
    try {
      getLog().info("Creating new concept = " + rootFsn);
      createRootConcept();
      getLog().info("Done creating new concept.");
    } catch (Exception e) {
      e.printStackTrace();
      throw new MojoExecutionException("Unexpected failure when creating concept",
          e);
    }
  }

  /**
   * Creates the root concept.
   *
   * @throws Exception the exception
   */
  private void createRootConcept() throws Exception {

    // Obtain already-open datastore
    dataStore = AppContext.getService(TerminologyStoreDI.class);

    ConceptChronicleBI chron = createNewConcept(null, rootFsn, rootPrefTerm);
    ConceptVersionBI root = chron.getVersion(getVC());
    getLog().info("root concept " + root.getPrimordialUuid());
    if(childFsns != null) {
	    int i=0;
	    for(String fsn : childFsns) {
	    	createNewConcept(root, fsn, childPrefTerms[i++]);
	    }
    }
    
    if(childUuids != null) {
    	for(String childUuid : childUuids) {
    		ConceptVersionBI childConcept = dataStore.getConcept(UUID.fromString(childUuid)).getVersion(getVC());
    		createNewRelationship(childConcept, root);
    	}
    	
    }
    // Commit uncommitted changes
    dataStore.commit();


  }
  
	private ConceptChronicleBI createNewConcept(ConceptVersionBI con, String fsn, String prefTerm) throws Exception {
		LanguageCode lc = LanguageCode.EN_US;
		UUID isA = Snomed.IS_A.getUuids()[0];
		IdDirective idDir = IdDirective.GENERATE_HASH;
        UUID module = Snomed.CORE_MODULE.getLenient().getPrimordialUuid();
        UUID parents[] = null;
        if(con != null) {
	        parents = new UUID[1];
	        parents[0] = con.getPrimordialUuid();
        }
		ConceptCB newConCB = new ConceptCB(fsn, prefTerm, lc, isA, idDir, module, parents);
		newConCB.getConceptAttributeAB();								
		ConceptChronicleBI newCon = getBuilder().construct(newConCB);
		dataStore.addUncommitted(newCon);

		return newCon;

	}

	private void createNewRelationship(ConceptVersionBI con, ConceptVersionBI newCon) throws Exception {
		UUID typeUid =  Snomed.IS_A.getLenient().getPrimordialUuid();
		int group = 0;
		RelationshipType relType = RelationshipType.STATED_ROLE;
		IdDirective idDir = IdDirective.GENERATE_HASH;

				
		RelationshipCAB newRel = new RelationshipCAB(con.getPrimordialUuid(), typeUid, newCon.getPrimordialUuid(),
													 group, relType, idDir);
		getBuilder().construct(newRel);
		dataStore.addUncommitted(con);
		
	}

  /**
   * Returns the terminology builder.
   * @return the terminology builder
   * @throws Exception
   */
  private TerminologyBuilderBI getBuilder() throws Exception {
    BdbTermBuilder builder = null;
    // Must use a top level path as the path for creating the new path
    builder =
        new BdbTermBuilder(getEC(),
            getVC());
    return builder;
  }

  /**
   * Returns the ec.
   *
   * @return the ec
   * @throws ValidationException the validation exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static EditCoordinate getEC() throws ValidationException, IOException {
    int authorNid = TermAux.USER.getLenient().getConceptNid();
    int module = Snomed.CORE_MODULE.getLenient().getNid();
    int editPathNid = TermAux.SNOMED_CORE.getLenient().getConceptNid();
    return new EditCoordinate(authorNid, module, editPathNid);
  }
  
  public static ViewCoordinate getVC() throws IOException {
	  return StandardViewCoordinates.getSnomedStatedLatest();
  }

}
