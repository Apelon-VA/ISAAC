package gov.va.isaac.mojos.dbBuilder;

import gov.va.isaac.AppContext;

import java.io.IOException;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.ConceptCB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.blueprint.TerminologyBuilderBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.lang.LanguageCode;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.ihtsdo.otf.tcc.datastore.BdbTermBuilder;

/**
 * Goal which loads a database from an eConcept file, and generates the indexes.
 * 
 * @goal create-path
 * 
 * @phase process-sources
 */
public class PathBuilder extends AbstractMojo {

  /**
   * Origin path UUIDs to connect the new path to.
   * @parameter
   * @required
   */
  private String[] originPathUuids;

  /**
   * Parent path UUID of the current path.
   * @parameter
   * @required
   */
  private String parentPathUuid;

  /**
   * The fully specified name of the path concept.
   * @parameter
   * @required
   */
  String pathFsn = "ISAAC development path";

  /**
   * The preferred term of the path concept.
   * @parameter
   * @required
   */
  String pathPrefTerm = "ISAAC development path";

  /** The "path" concept spec - for use in path reference set */
  private final static ConceptSpec PATH = TermAux.PATH;

  /** The "path reference set" concept spec */
  private final static ConceptSpec PATH_REFSET = TermAux.PATH_REFSET;

  /** The "path origin reference set" concept spec */
  private final static ConceptSpec PATH_ORIGIN_REFSET =
      TermAux.PATH_ORIGIN_REFSET;

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
      getLog().info("Creating new path = " + pathFsn);
      createPath();
      getLog().info("Done creating new path.");
    } catch (Exception e) {
      e.printStackTrace();
      throw new MojoExecutionException("Unexpected failure when creating path",
          e);
    }
  }

  /**
   * Creates the ISAAC Path.
   *
   * @throws Exception the exception
   */
  private void createPath() throws Exception {

    // Obtain already-open datastore
    dataStore = AppContext.getService(TerminologyStoreDI.class);

    // Set up lanugage code, Isa rel, and some other biolerplate
    // If necessary, these could be parameterized
    LanguageCode lc = LanguageCode.EN_US;
    UUID isA = Snomed.IS_A.getUuids()[0];
    IdDirective idDir = IdDirective.GENERATE_HASH;
    UUID module = Snomed.CORE_MODULE.getLenient().getPrimordialUuid();

    // Setup parent concept
    UUID parents[] = new UUID[1];
    ConceptChronicleBI bpConcept =
        dataStore.getConcept(UUID.fromString(parentPathUuid));
    // Must use standard view coordinate here, otherwise we won't be able to
    // find this concept.
    ConceptVersionBI con =
        bpConcept.getVersion(StandardViewCoordinates.getSnomedStatedLatest());
    parents[0] = con.getPrimordialUuid();

    // Prepare new concept for creation
    ConceptCB newConCB =
        new ConceptCB(pathFsn, pathPrefTerm, lc, isA, idDir, module, parents);

    // Create concept and mark for commit
    ConceptChronicleBI pathConcept = getBuilder().construct(newConCB);
    dataStore.addUncommitted(pathConcept);

    // Add to path refset
    ConceptChronicleBI pathRefset =
        dataStore.getConcept(PATH_REFSET.getLenient().getPrimordialUuid());
    ConceptChronicleBI path =
        dataStore.getConcept(PATH.getLenient().getPrimordialUuid());
    getLog().info("  Add new path concept to PATH_REFSET");
    addNidMember(pathRefset, path, pathConcept);

    // Add each origin to PATH_ORIGIN_REFSET
    ConceptChronicleBI pathOriginRefset =
        dataStore.getConcept(PATH_ORIGIN_REFSET.getLenient()
            .getPrimordialUuid());
    for (String originUuid : originPathUuids) {
      ConceptChronicleBI originConcept =
          dataStore.getConcept(UUID.fromString(originUuid));
      getLog().info(
          "  Add new path concept to PATH_ORIGINS_REFSET - " + originUuid);
      addNidLongMember(pathOriginRefset, pathConcept, originConcept);

      // Make sure each origin concept is correctly modeled as a path
      // getLog().info("    Add origin path to PATH_REFSET");
      // getLog().info("    " + originConcept.toUserString());
      // addNidMember(pathRefset, path, originConcept);
    }

    // Commit uncommitted changes
    dataStore.commit();

    getLog().info("WRITE path refset info");
    ConceptChronicleBI paths =
        dataStore.getConcept(TermAux.PATH_REFSET.getLenient().getNid());
    ConceptVersionBI pathsVersion =
        paths.getVersion(StandardViewCoordinates.getSnomedStatedLatest());
    getLog().info("paths: " + paths.getNid() + " " + paths.toUserString());
    for (RefexChronicleBI<?> refex : pathsVersion.getRefsetMembers()) {
      getLog().info("  refex : " + refex.toUserString());
    }

    getLog().info("WRITE path origin refset info");
    ConceptChronicleBI pathOrigins =
        dataStore.getConcept(TermAux.PATH_ORIGIN_REFSET.getLenient().getNid());
    ConceptVersionBI pathOriginsVersion =
        pathOrigins.getVersion(StandardViewCoordinates.getSnomedStatedLatest());
    getLog().info(
        "paths: " + pathOrigins.getNid() + " " + pathOrigins.toUserString());
    for (RefexChronicleBI<?> refex : pathOriginsVersion.getRefsetMembers()) {
      getLog().info("  refex : " + refex.toUserString());
    }

  }

  /**
   * Adds the refset component as a member to the refset concept and the
   * specified value as a component extension.
   *
   * @param refCon the refset concept
   * @param refComp the referenced component
   * @param value the value of the component extension
   * @throws Exception the exception
   */
  private void addNidMember(ConceptChronicleBI refCon,
    ConceptChronicleBI refComp, ConceptChronicleBI value) throws Exception {
    RefexCAB newMember =
        new RefexCAB(RefexType.CID, refComp.getNid(), refCon.getNid(),
            IdDirective.GENERATE_HASH, RefexDirective.INCLUDE);
    newMember.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, value.getNid());

    @SuppressWarnings("unused")
    RefexChronicleBI<?> newMemChron = getBuilder().construct(newMember);

    if (!refCon.isAnnotationStyleRefex()) {
      dataStore.addUncommitted(refCon);
    } else {
      dataStore.addUncommitted(refComp);
    }

  }

  /**
   * Adds the extension member.
   *
   * @param refCon the refset concept
   * @param refComp the referenced component
   * @param value the value
   * @throws Exception the exception
   */
  private void addNidLongMember(ConceptChronicleBI refCon,
    ConceptChronicleBI refComp, ConceptChronicleBI value) throws Exception {
    RefexCAB newMember;
    newMember =
        new RefexCAB(RefexType.CID_LONG, refComp.getConceptNid(),
            refCon.getNid(), IdDirective.GENERATE_HASH, RefexDirective.INCLUDE);
    newMember.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, value.getNid());
    // The "LONG" is a version for a position, I think max value is "latest"
    newMember.put(ComponentProperty.LONG_EXTENSION_1, Long.MAX_VALUE);

    @SuppressWarnings("unused")
    RefexChronicleBI<?> newMemChron = getBuilder().construct(newMember);
    if (!refCon.isAnnotationStyleRefex()) {
      dataStore.addUncommitted(refCon);
    } else {
      dataStore.addUncommitted(refComp);
    }
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
            StandardViewCoordinates.getSnomedStatedLatest());
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

}
