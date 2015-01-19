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
import gov.va.isaac.util.OTFUtility;

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
 * Goal which sets up the path within the ISAAC environment
 * 
 * @goal create-path
 * 
 * @phase process-sources
 */
public class PathBuilder extends AbstractMojo {

  // TODO use MojoConceptSpec for these parameters, for clarity
  /**
   * Origin path UUIDs to connect the new path to.
   * @parameter
   */
  private String[] originPathUuids;

  /**
   * Origin path FSNs to connect the new path to. Used if UUIDs are not known.
   * @parameter
   */
  private String[] originPathFsns;

  /**
   * Parent path UUID of the current path.
   * @parameter
   */
  private String parentPathUuid;

  /**
   * Parent path FSN of the current path.
   * @parameter
   */
  private String parentPathFsn;

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
    ConceptChronicleBI bpConcept = null;
    if (parentPathUuid != null) {
      bpConcept = dataStore.getConcept(UUID.fromString(parentPathUuid));
    } else if (parentPathFsn != null) {
      bpConcept = dataStore.getConcept(OTFUtility.getUuidForFsn(parentPathFsn,parentPathFsn));
    } else {
      throw new IOException("Missing parent path parameter");
    }
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
    dataStore.commit();
    getLog().info("New Path " + pathConcept.getPrimordialUuid());

    // Add to path refset
    ConceptChronicleBI pathRefset =
        dataStore.getConcept(PATH_REFSET.getLenient().getPrimordialUuid());
    ConceptChronicleBI path =
        dataStore.getConcept(PATH.getLenient().getPrimordialUuid());
    getLog().info("  Add new path concept to PATH_REFSET");
    addNidMember(pathRefset, path, pathConcept);
    dataStore.commit();

    // Add each origin to PATH_ORIGIN_REFSET
    ConceptChronicleBI pathOriginRefset =
        dataStore.getConcept(PATH_ORIGIN_REFSET.getLenient()
            .getPrimordialUuid());
    if (originPathUuids != null) {
      for (String originUuid : originPathUuids) {
        ConceptChronicleBI originConcept =
            dataStore.getConcept(UUID.fromString(originUuid));
        getLog().info(
            "  Add new path concept to PATH_ORIGINS_REFSET - " + originUuid);
        addNidLongMember(pathOriginRefset, pathConcept, originConcept);

        // Make sure each origin concept is correctly modeled as a path
        // NOTE: this is probably not necessary
        // getLog().info("    Add origin path to PATH_REFSET");
        // getLog().info("    " + originConcept.toUserString());
        // addNidMember(pathRefset, path, originConcept);
      }
    } else if (originPathFsns != null) {
      for (String originFsn : originPathFsns) {
        ConceptChronicleBI originConcept =
            dataStore.getConcept(OTFUtility.getUuidForFsn(originFsn, originFsn));
        getLog().info(
            "  Add new path concept to PATH_ORIGINS_REFSET - " + originFsn + " " + OTFUtility.getUuidForFsn(originFsn,originFsn));
        addNidLongMember(pathOriginRefset, pathConcept, originConcept);
      }
    } else {
      throw new Exception("Origin paths not specified");
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
      getLog().info("  sememe : " + refex.toUserString());
    }

    getLog().info("WRITE path origin refset info");
    ConceptChronicleBI pathOrigins =
        dataStore.getConcept(TermAux.PATH_ORIGIN_REFSET.getLenient().getNid());
    ConceptVersionBI pathOriginsVersion =
        pathOrigins.getVersion(StandardViewCoordinates.getSnomedStatedLatest());
    getLog().info(
        "paths: " + pathOrigins.getNid() + " " + pathOrigins.toUserString());
    for (RefexChronicleBI<?> refex : pathOriginsVersion.getRefsetMembers()) {
      getLog().info("  sememe : " + refex.toUserString());
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
    //getLog().info("  add to path refset: " + refCon.getPrimordialUuid() + ", " 
    //    + refComp.getPrimordialUuid() + ", " + value.getPrimordialUuid());

    // GENERATE_REFEX_CONTENT_HASH is ideal here but there is an OTF bug
    // Use GENERATE_RANDOM for now instead.
    RefexCAB newMember =
        new RefexCAB(RefexType.CID, refComp.getNid(), refCon.getNid(),
            IdDirective.GENERATE_RANDOM, RefexDirective.EXCLUDE);
    newMember.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, value.getNid());

    @SuppressWarnings("unused")
    RefexChronicleBI<?> newMemChron = getBuilder().construct(newMember);
    //getLog().info("  new member uuid = " + newMemChron.getPrimordialUuid());

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
    //getLog().info("  add to path origins refset: " + refCon.getPrimordialUuid() + ", " 
    //    + refComp.getPrimordialUuid() + ", " + value.getPrimordialUuid());

    RefexCAB newMember;
    // GENERATE_REFEX_CONTENT_HASH is ideal here but there is an OTF bug
    // Use GENERATE_RANDOM for now instead.
    // will have more than one entry.
    // EXCLUDE is OK because these things dont have connected reset members
    newMember =
        new RefexCAB(RefexType.CID_LONG, refComp.getConceptNid(),
            refCon.getNid(), IdDirective.GENERATE_RANDOM,
            RefexDirective.EXCLUDE);
    newMember.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, value.getNid());
    // The "INT" is a version for a position, I think max value is "latest"
    newMember.put(ComponentProperty.LONG_EXTENSION_1, Long.MAX_VALUE);

    @SuppressWarnings("unused")
    RefexChronicleBI<?> newMemChron = getBuilder().construct(newMember);
    //getLog().info("  new member uuid = " + newMemChron.getPrimordialUuid());
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
    // Should be able to be wb aux but this causes weird problems
    int editPathNid = TermAux.WB_AUX_PATH.getLenient().getConceptNid();
    return new EditCoordinate(authorNid, module, editPathNid);
  }

}
