/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.va.isaac.ie.exporter;

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerHelper;
import gov.va.isaac.ie.exporter.Rf2File.ReleaseType;
import gov.va.isaac.util.AbstractProgressReporter;
import gov.va.isaac.util.ProgressListener;
import gov.va.isaac.util.WBUtility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeChronicleBI;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptFetcherBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionManagerPolicy;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.country.COUNTRY_CODE;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.lang.LanguageCode;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRfx;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.api.time.TimeHelper;
import org.ihtsdo.otf.tcc.api.uuid.UuidT5Generator;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates a Release Format 2 style release files for a specified set of
 * concepts in the database. This class implements
 * <code>ProcessUnfetchedConceptDataBI</code> and can be "run" using the
 * terminology store method iterateConceptDataInParallel.
 *
 * @see <a href="http://www.snomed.org/tig?t=tig_release_files">IHTSDO Technical
 *      Implementation Guide - Release File Specifications</a>
 */
public class Rf2Export extends AbstractProgressReporter implements Exporter,
    org.ihtsdo.otf.tcc.api.concept.ProcessUnfetchedConceptDataBI {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(OWLExporter.class);

  /**
   * The set of concepts to process.
   */
  public NativeIdSetBI conceptsToProcess;

  /** The country. */
  private COUNTRY_CODE country;

  /** The effective date string. */
  private String effectiveDateString;

  /** The concepts writer. */
  private Writer conceptsWriter;

  /** The descriptions writer. */
  private Writer descriptionsWriter;

  /** The text definition writer. */
  private Writer textDefinitionsWriter;

  /** The identifiers writer. */
  private Writer identifiersWriter;

  /** The public identifiers writer. */
  private Writer publicIdentifiersWriter;

  /** The language. */
  private LanguageCode language;

  /** The namespace. */
  private String namespace;

  /** The relationships writer. */
  private Writer relationshipsWriter;

  /** The relationships stated writer. */
  private Writer relationshipsStatedWriter;

  /** The release type. */
  private ReleaseType releaseType;

  /** The view coordinate. */
  private ViewCoordinate viewCoordinate;

  /** The lang refsets writer. */
  private Writer langRefsetsWriter;

  /** The other lang refsets writer. */
  private Writer otherLangRefsetsWriter;

  /** The mod depend writer. */
  private Writer modDependWriter;

  /** The desc type writer. */
  private Writer descTypeWriter;

  /** The refset desc writer. */
  private Writer refsetDescWriter;

  /** The association writer. */
  private Writer associationWriter;

  /** The attribute value writer. */
  private Writer attributeValueWriter;

  /** The excluded refset ids. */
  private Set<Integer> excludedRefsetIds;

  /** The lang refex nids. */
  private Set<Integer> langRefexNids = new HashSet<>();

  /** The possible lang refex nids. */
  private Set<Integer> possibleLangRefexNids = new HashSet<>();

  /** The desc types. */
  private Set<ConceptSpec> descTypes = new HashSet<>();

  /** The refset desc namespace. */
  private static UUID REFSET_DESC_NAMESPACE = UUID
      .fromString("d1871eb0-8a47-11e1-b0c4-0800200c9a66");

  /**  The request cancel. */
  private boolean requestCancel = false;
  
  /** The count. */
  @SuppressWarnings("unused")
  private int count = 0;

  /** The all concept count. */
  private int allCount = 0;

  /**
   * private static UUID MODULE_DEPEND_NAMESPACE = UUID
   * .fromString("d1871eb2-8a47-11e1-b0c4-0800200c9a66");
   * 
   * private static UUID DESC_TYPE_NAMESPACE = UUID
   * .fromString("d1871eb3-8a47-11e1-b0c4-0800200c9a66");
   **/

  private static UUID SOME = UUID
      .fromString("f9bf3eab-02bb-3d49-b2a2-58ed96fa897f");

  /** The directory. */
  private File directory;

  /** The taxonomy parent nids. */
  private Collection<Integer> taxonomyParentNids;

  /** The path nid. */
  private int pathNid;

  /** The data store. */
  private static BdbTerminologyStore dataStore = ExtendedAppContext
      .getDataStore();

  /** the count so far. */
  private int progress = 0;

  /** the total. */
  private int progressMax = 0;

  /**
   * Instantiates a new rf2 export.
   *
   * @param directory specifying where to write the files
   * @param releaseType the <code>ReleaseType</code> representing the type of
   *          release
   * @param language the <code>LanguageCode</code> representing the language of
   *          the release
   * @param country the <code>COUNTRY_CODE</code> representing the country
   *          associated with the release
   * @param namespace the String representing the namespace associated with the
   *          released content
   * @param effectiveDate specifying the official release date
   * @param pathNid the path nid
   * @param viewCoordinate specifying which versions of the concepts to include
   *          in this release. Should not include all status values.
   * @param taxonomyParentNids an integer representing the parent of taxonomy to
   *          release
   * @throws IOException signals that an I/O exception has occurred
   * @throws ContradictionException the contradiction exception
   */
  public Rf2Export(File directory, ReleaseType releaseType,
      LanguageCode language, COUNTRY_CODE country, String namespace,
      Date effectiveDate, int pathNid, ViewCoordinate viewCoordinate,
      Collection<Integer> taxonomyParentNids) throws IOException,
      ContradictionException {
    this.directory = directory;
    this.releaseType = releaseType;
    this.language = language;
    this.country = country;
    this.namespace = namespace;
    this.pathNid = pathNid;
    this.viewCoordinate = viewCoordinate;
    this.effectiveDateString =
        TimeHelper.getShortFileDateFormat().format(effectiveDate);
    this.taxonomyParentNids = taxonomyParentNids;
    setup();
  }

  /**
   * Setup. Opens files, etc.
   *
   * @throws FileNotFoundException the file not found exception
   * @throws UnsupportedEncodingException the unsupported encoding exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ContradictionException the contradiction exception
   */
  private void setup() throws FileNotFoundException,
    UnsupportedEncodingException, IOException, ContradictionException {
    File terminologyDir = new File(directory, "Terminology");

    terminologyDir.mkdirs();
    File conceptsFile =
        new File(terminologyDir, "sct2_Concept_" + releaseType.suffix + "_"
            + country.getFormatedCountryCode().toUpperCase() + namespace + "_"
            + effectiveDateString + ".txt");
    File descriptionsFile =
        new File(terminologyDir, "sct2_Description_" + releaseType.suffix + "-"
            + language.getFormatedLanguageCode() + "_"
            + country.getFormatedCountryCode().toUpperCase() + namespace + "_"
            + effectiveDateString + ".txt");
    File textDefinitionsFile =
        new File(terminologyDir, "sct2_TextDefinition_" + releaseType.suffix
            + "-" + language.getFormatedLanguageCode() + "_"
            + country.getFormatedCountryCode().toUpperCase() + namespace + "_"
            + effectiveDateString + ".txt");
    File relationshipsFile =
        new File(terminologyDir, "sct2_Relationship_" + releaseType.suffix
            + "_" + country.getFormatedCountryCode().toUpperCase() + namespace
            + "_" + effectiveDateString + ".txt");
    File relationshipsStatedFile =
        new File(terminologyDir, "sct2_StatedRelationship_"
            + releaseType.suffix + "_"
            + country.getFormatedCountryCode().toUpperCase() + namespace + "_"
            + effectiveDateString + ".txt");
    File identifiersFile = null;
    identifiersFile =
        new File(terminologyDir, "sct2_Identifier_" + releaseType.suffix + "_"
            + country.getFormatedCountryCode().toUpperCase() + namespace
            + effectiveDateString + ".txt");
    File contentDir =
        new File(directory, "Refset" + File.separator + "Content");
    contentDir.mkdirs();
    File associationFile =
        new File(contentDir, "der2_cRefset_AssociationReference"
            + releaseType.suffix + "_"
            + country.getFormatedCountryCode().toUpperCase() + namespace + "_"
            + effectiveDateString + ".txt");
    File attributeValueFile =
        new File(contentDir, "der2_cRefset_AttributeValue" + releaseType.suffix
            + "_" + country.getFormatedCountryCode().toUpperCase() + namespace
            + "_" + effectiveDateString + ".txt");
    File languageDir =
        new File(directory, "Refset" + File.separator + "Language");
    languageDir.mkdirs();
    File langRefsetsFile =
        new File(languageDir, "der2_cRefset_Language" + releaseType.suffix
            + "-" + language.getFormatedLanguageCode() + "_"
            + country.getFormatedCountryCode().toUpperCase() + namespace + "_"
            + effectiveDateString + ".txt");
    File otherLangRefsetsFile = null;
    if (!language.getFormatedLanguageNoDialectCode().equals(
        LanguageCode.EN.getFormatedLanguageCode())) {
      otherLangRefsetsFile =
          new File(languageDir, "der2_cRefset_Language" + releaseType.suffix
              + "-" + language.getFormatedLanguageNoDialectCode() + "_"
              + country.getFormatedCountryCode().toUpperCase() + namespace
              + effectiveDateString + ".txt");
    }
    File metadataDir =
        new File(directory, "Refset" + File.separator + "Metadata");
    metadataDir.mkdirs();
    File modDependFile =
        new File(metadataDir, "der2_ssRefset_ModuleDependency"
            + releaseType.suffix + "_"
            + country.getFormatedCountryCode().toUpperCase() + namespace + "_"
            + effectiveDateString + ".txt");
    File descTypeFile =
        new File(metadataDir, "der2_ciRefset_DescriptionType"
            + releaseType.suffix + "_"
            + country.getFormatedCountryCode().toUpperCase() + namespace + "_"
            + effectiveDateString + ".txt");
    File refsetDescFile =
        new File(metadataDir, "der2_cciRefset_RefsetDescriptor"
            + releaseType.suffix + "_"
            + country.getFormatedCountryCode().toUpperCase() + namespace + "_"
            + effectiveDateString + ".txt");

    FileOutputStream conceptOs = new FileOutputStream(conceptsFile);
    conceptsWriter =
        new BufferedWriter(new OutputStreamWriter(conceptOs, "UTF8"));
    FileOutputStream descriptionOs = new FileOutputStream(descriptionsFile);
    descriptionsWriter =
        new BufferedWriter(new OutputStreamWriter(descriptionOs, "UTF8"));
    FileOutputStream textDefinitionOs =
        new FileOutputStream(textDefinitionsFile);
    textDefinitionsWriter =
        new BufferedWriter(new OutputStreamWriter(textDefinitionOs, "UTF8"));
    FileOutputStream relOs = new FileOutputStream(relationshipsFile);
    relationshipsWriter =
        new BufferedWriter(new OutputStreamWriter(relOs, "UTF8"));
    FileOutputStream relStatedOs =
        new FileOutputStream(relationshipsStatedFile);
    relationshipsStatedWriter =
        new BufferedWriter(new OutputStreamWriter(relStatedOs, "UTF8"));

    FileOutputStream pubIdOs = new FileOutputStream(identifiersFile);
    identifiersWriter =
        new BufferedWriter(new OutputStreamWriter(pubIdOs, "UTF8"));
    FileOutputStream associationOs = new FileOutputStream(associationFile);
    associationWriter =
        new BufferedWriter(new OutputStreamWriter(associationOs, "UTF8"));
    FileOutputStream attributeValueOs =
        new FileOutputStream(attributeValueFile);
    attributeValueWriter =
        new BufferedWriter(new OutputStreamWriter(attributeValueOs, "UTF8"));
    FileOutputStream langRefOs = new FileOutputStream(langRefsetsFile);
    langRefsetsWriter =
        new BufferedWriter(new OutputStreamWriter(langRefOs, "UTF8"));
    if (otherLangRefsetsFile != null) {
      FileOutputStream langOs = new FileOutputStream(otherLangRefsetsFile);
      otherLangRefsetsWriter =
          new BufferedWriter(new OutputStreamWriter(langOs, "UTF8"));
    }
    FileOutputStream modDependOs = new FileOutputStream(modDependFile);
    modDependWriter =
        new BufferedWriter(new OutputStreamWriter(modDependOs, "UTF8"));
    FileOutputStream descTypeOs = new FileOutputStream(descTypeFile);
    descTypeWriter =
        new BufferedWriter(new OutputStreamWriter(descTypeOs, "UTF8"));
    FileOutputStream refDescOs = new FileOutputStream(refsetDescFile);
    refsetDescWriter =
        new BufferedWriter(new OutputStreamWriter(refDescOs, "UTF8"));

    for (Rf2File.ConceptsFileFields field : Rf2File.ConceptsFileFields.values()) {
      conceptsWriter.write(field.headerText + field.seperator);
    }

    for (Rf2File.DescriptionsFileFields field : Rf2File.DescriptionsFileFields
        .values()) {
      descriptionsWriter.write(field.headerText + field.seperator);
    }

    // TextDefinition has same format as descriptions
    for (Rf2File.DescriptionsFileFields field : Rf2File.DescriptionsFileFields
        .values()) {
      textDefinitionsWriter.write(field.headerText + field.seperator);
    }

    for (Rf2File.RelationshipsFileFields field : Rf2File.RelationshipsFileFields
        .values()) {
      relationshipsWriter.write(field.headerText + field.seperator);
    }

    for (Rf2File.StatedRelationshipsFileFields field : Rf2File.StatedRelationshipsFileFields
        .values()) {
      relationshipsStatedWriter.write(field.headerText + field.seperator);
    }

    for (Rf2File.AssociationRefsetFileFields field : Rf2File.AssociationRefsetFileFields
        .values()) {
      associationWriter.write(field.headerText + field.seperator);
    }

    for (Rf2File.AttribValueRefsetFileFields field : Rf2File.AttribValueRefsetFileFields
        .values()) {
      attributeValueWriter.write(field.headerText + field.seperator);
    }

    for (Rf2File.IdentifiersFileFields field : Rf2File.IdentifiersFileFields
        .values()) {
      identifiersWriter.write(field.headerText + field.seperator);
    }

    for (Rf2File.LanguageRefsetFileFields field : Rf2File.LanguageRefsetFileFields
        .values()) {
      langRefsetsWriter.write(field.headerText + field.seperator);
    }
    if (otherLangRefsetsWriter != null) {
      for (Rf2File.LanguageRefsetFileFields field : Rf2File.LanguageRefsetFileFields
          .values()) {
        otherLangRefsetsWriter.write(field.headerText + field.seperator);
      }
    }

    for (Rf2File.ModuleDependencyFileFields field : Rf2File.ModuleDependencyFileFields
        .values()) {
      modDependWriter.write(field.headerText + field.seperator);
    }

    for (Rf2File.DescTypeFileFields field : Rf2File.DescTypeFileFields.values()) {
      descTypeWriter.write(field.headerText + field.seperator);
    }

    for (Rf2File.RefsetDescriptorFileFields field : Rf2File.RefsetDescriptorFileFields
        .values()) {
      refsetDescWriter.write(field.headerText + field.seperator);
    }

    ConceptSpec fsnDesc =
        new ConceptSpec("Fully specified name (core metadata concept)",
            UUID.fromString("00791270-77c9-32b6-b34f-d932569bd2bf"));
    ConceptSpec synDesc =
        new ConceptSpec("Synonym (core metadata concept)",
            UUID.fromString("8bfba944-3965-3946-9bcb-1e80a5da63a2"));
    descTypes.add(synDesc);
    descTypes.add(fsnDesc);

    // Collect children and grandchildren of the ancestor metadata concept for
    // language refsets
    ConceptSpec langRefexParent =
        new ConceptSpec("Language type reference set",
            UUID.fromString("84a0b03b-220c-3d69-8487-2e019c933687"));
    Set<ConceptVersionBI> descs =
        WBUtility.getAllChildrenOfConcept(
            langRefexParent.getLenient().getNid(), true);
    for (ConceptVersionBI desc : descs) {
      possibleLangRefexNids.add(desc.getNid());
    }
    conceptsToProcess = dataStore.getAllConceptNids();
  }

  /**
   * Adds a {@link ProgressListener}.
   * @param l thef{@link ProgressListener}
   */
  @Override
  public void addProgressListener(ProgressListener l) {
    progress = 0;
    try {
      progressMax = dataStore.getConceptCount();
    } catch (IOException e) {
      throw new IllegalStateException("This should never happen");
    }
    super.addProgressListener(l);
  }

  /**
   * Export.
   *
   * @throws Exception the exception
   */
  @Override
  public void export(int pathNid) throws Exception {
    dataStore.iterateConceptDataInSequence(this);
  }

  /**
   * Closes the release file writers.
   *
   * @throws IOException signals that an I/O exception has occurred
   */
  public void close() throws IOException {
    if (conceptsWriter != null) {
      conceptsWriter.close();
    }

    if (descriptionsWriter != null) {
      descriptionsWriter.close();
    }

    if (textDefinitionsWriter != null) {
      textDefinitionsWriter.close();
    }

    if (relationshipsWriter != null) {
      relationshipsWriter.close();
    }

    if (relationshipsStatedWriter != null) {
      relationshipsStatedWriter.close();
    }

    if (identifiersWriter != null) {
      identifiersWriter.close();
    }

    if (publicIdentifiersWriter != null) {
      publicIdentifiersWriter.close();
    }

    if (associationWriter != null) {
      associationWriter.close();
    }

    if (attributeValueWriter != null) {
      attributeValueWriter.close();
    }

    if (langRefsetsWriter != null) {
      langRefsetsWriter.close();
    }

    if (otherLangRefsetsWriter != null) {
      otherLangRefsetsWriter.flush();
      otherLangRefsetsWriter.close();
    }

    if (modDependWriter != null) {
      modDependWriter.close();
    }

    if (descTypeWriter != null) {
      descTypeWriter.close();
    }

    if (refsetDescWriter != null) {
      refsetDescWriter.close();
    }
  }

  /**
   * Continue work.
   *
   * @return <code>true</code>
   */
  @Override
  public boolean continueWork() {
    return !requestCancel;
  }

  /**
   * Processes the components, annotations, and language refset members of the
   * specified <code>concept</code>. Any components that has a stamp nid in the
   * set of specified stamp nids will be written to the release file.
   *
   * @param concept the concept to process
   * @throws Exception indicates an exception has occurred
   */
  private void process(ConceptChronicleBI concept) throws Exception {
    boolean write = true;
    // NO NEED for this check because each component individually checks
    //if (concept.getVersion(viewCoordinate).getPathNid() != pathNid) {
    //  write = false;
    //}
    if (write) {
      ConceptAttributeChronicleBI ca = concept.getConceptAttributes();
      processConceptAttribute(ca);
      if (concept.getDescriptions() != null) {
        for (DescriptionChronicleBI d : concept.getDescriptions()) {
          // also processes text definitions
          processDescription(d);
          if (d.getAnnotations() != null) {
            for (RefexChronicleBI<?> annot : d.getAnnotations()) {
              annot.getNid();
              if (possibleLangRefexNids.contains(annot.getNid())) {
                langRefexNids.add(annot.getNid());
                processLangRefsets(annot);
              }
            }
          }
        }
      }

      if (concept.getRelationshipsOutgoing() != null) {
        for (RelationshipChronicleBI r : concept.getRelationshipsOutgoing()) {
          processRelationship(r);
        }
      }
    }
  }

  /**
   * Writes the modular dependency, description types, and refset descriptions
   * files.
   *
   * @throws IOException signals that an I/O exception has occurred
   * @throws NoSuchAlgorithmException indicates a no such algorithm exception
   *           has occurred
   * @throws ContradictionException the contradiction exception
   */
  public void writeOneTimeFiles() throws IOException, NoSuchAlgorithmException,
    ContradictionException {
    for (Integer refexNid : langRefexNids) {
      processLanguageRefsetDesc(refexNid);
    }
  }

  /**
   * Writes the concepts file according to the fields specified in <code>
   * Rf2File.ConceptsFileFields</code>. Only the versions which have a stamp nid
   * in the specified collection of stamp nids will be written.
   *
   * @param conceptAttributeChronicle the concept attribute to process
   * @throws IOException signals that an I/O exception has occurred
   * @throws Exception the exception
   * @see Rf2File.ConceptsFileFields
   */
  private void processConceptAttribute(
    ConceptAttributeChronicleBI conceptAttributeChronicle) throws IOException,
    Exception {
    if (conceptAttributeChronicle != null) {
      Collection<ConceptAttributeVersionBI<?>> versions = new HashSet<>();

      // TODO: remove all "full" and "delta" stuff - or fully implement it
      if (releaseType.equals(ReleaseType.FULL)) {
        // if not previously released or latest version remove
        for (ConceptAttributeVersionBI<?> ca : conceptAttributeChronicle
            .getVersions()) {
          if (ca.getPathNid() == pathNid) {
            versions.add(ca);
          }
        }
      } else {
        ConceptAttributeVersionBI<?> version =
            conceptAttributeChronicle.getVersion(viewCoordinate);
        if (version != null) {
          versions.add(version);
        }
      }
      boolean write = true;

      for (ConceptAttributeVersionBI<?> car : versions) {
        if (car.getPathNid() != pathNid) {
          write = false;
        }
        if (write) {
          for (Rf2File.ConceptsFileFields field : Rf2File.ConceptsFileFields
              .values()) {
            switch (field) {
              case ACTIVE:
                conceptsWriter.write((car.isActive() ? "1" : "0")
                    + field.seperator);

                break;

              case DEFINITION_STATUS_ID:
                conceptsWriter.write(car.isDefined() + field.seperator);

                break;

              case EFFECTIVE_TIME:
                conceptsWriter.write(TimeHelper.getShortFileDateFormat()
                    .format(car.getTime()) + field.seperator);
                break;

              case ID:
                conceptsWriter.write(getSctIdOrUuidForNid(car.getNid())
                    + field.seperator);

                break;

              case MODULE_ID:
                conceptsWriter.write(getSctIdOrUuidForNid(car.getModuleNid())
                    + field.seperator);

                break;
              default:
                break;
            }
          }
        }
      }
    }
  }

  /**
   * Writes the descriptions file according to the fields specified in <code>
   * Rf2File.DescriptionsFileFields</code>. Only the versions which have a stamp
   * nid in the specified collection of stamp nids will be written.
   *
   * @param descriptionChronicle the description to process
   * @throws IOException signals that an I/O exception has occurred
   * @throws Exception the exception
   * @see Rf2File.DescriptionsFileFields
   */
  private void processDescription(DescriptionChronicleBI descriptionChronicle)
    throws IOException, Exception {
    if (descriptionChronicle != null) {
      Collection<DescriptionVersionBI<?>> versions = new HashSet<>();
      if (releaseType.equals(ReleaseType.FULL)) {
        // if not previously released or latest version remove
        for (DescriptionVersionBI<?> d : descriptionChronicle.getVersions()) {
          if (d.getPathNid() == pathNid) {
            versions.add(d);
          }
        }
      } else {
        DescriptionVersionBI<?> version =
            descriptionChronicle.getVersion(viewCoordinate);
        if (version != null) {
          versions.add(version);
        }
      }
      boolean write = true;

      for (DescriptionVersionBI<?> descr : versions) {
        if (descr.getPathNid() != pathNid) {
          write = false;
        }
        if (write) {
          // Determine whether description or definition data
          Writer writer = descriptionsWriter;
          if (descr.getTypeNid() != SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2
              .getLenient().getNid()
              && descr.getTypeNid() != SnomedMetadataRf2.SYNONYM_RF2
                  .getLenient().getNid()) {
            writer = textDefinitionsWriter;
          }
          for (Rf2File.DescriptionsFileFields field : Rf2File.DescriptionsFileFields
              .values()) {
            switch (field) {
              case ACTIVE:
                writer.write((descr.isActive() ? "1" : "0") + field.seperator);

                break;

              case EFFECTIVE_TIME:
                writer.write(TimeHelper.getShortFileDateFormat().format(
                    descr.getTime())
                    + field.seperator);
                break;

              case ID:
                writer
                    .write(getSctIdOrUuidForNid(descriptionChronicle.getNid())
                        + field.seperator);

                break;

              case MODULE_ID:
                writer.write(getSctIdOrUuidForNid(descr.getModuleNid())
                    + field.seperator);

                break;

              case CONCEPT_ID:
                writer.write(getSctIdOrUuidForNid(descriptionChronicle
                    .getConceptNid()) + field.seperator);

                break;

              case LANGUAGE_CODE:
                writer.write(descr.getLang() + field.seperator);

                break;

              case TYPE_ID:
                writer.write(getSctIdOrUuidForNid(descr.getTypeNid())
                    + field.seperator);

                break;

              case TERM:
                writer.write(descr.getText() + field.seperator);

                break;

              case CASE_SIGNIFICANCE_ID:
                writer
                    .write(descr.isInitialCaseSignificant() + field.seperator);

                break;
              default:
                break;
            }
          }
        }
      }
    }
  }

  /**
   * Returns the sct id or uuid for nid.
   *
   * @param typeNid the type nid
   * @return the sct id or uuid for nid
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private String getSctIdOrUuidForNid(int typeNid) throws IOException {
    String sctId = null;
    try {
      viewCoordinate
          .setContradictionManagerPolicy(ContradictionManagerPolicy.LAST_COMMIT_WINS);
      ComponentVersionBI componentVersion =
          dataStore.getComponentVersion(viewCoordinate, typeNid);
      if (componentVersion != null) {
        sctId = ConceptViewerHelper.getSctId(componentVersion);
      }
    } catch (ContradictionException e) {
      throw new IOException("Unable to get SCTID for " + typeNid, e);
    }

    if (sctId == null || "Unreleased".equals(sctId)) {
      sctId = dataStore.getUuidPrimordialForNid(typeNid).toString();
    }
    if ("Unreleased".equals(sctId)) {
      LOG.info("Unreleased SCTID for " + sctId);
    }
    return sctId;
  }

  /**
   * Determines if a relationship is stated or inferred and processes the
   * relationship accordingly. Only the versions which have a stamp nid in the
   * specified collection of stamp nids will be written.
   *
   * @param relationshipChronicle the relationship to process
   * @throws IOException signals that an I/O exception has occurred
   * @throws Exception the exception
   */
  private void processRelationship(RelationshipChronicleBI relationshipChronicle)
    throws IOException, Exception {
    if (relationshipChronicle != null) {
      Collection<RelationshipVersionBI<?>> versions = new HashSet<>();
      if (!releaseType.equals(ReleaseType.FULL)) {
        // if not previously released or latest version remove
        for (RelationshipVersionBI<?> r : relationshipChronicle.getVersions()) {
          if (r.getPathNid() == pathNid) {
            versions.add(r);
          }
        }
      } else {
        RelationshipVersionBI<?> version =
            relationshipChronicle.getVersion(viewCoordinate);
        if (version != null) {
          versions.add(version);
        }
      }
      boolean write = true;

      boolean inTaxonomy = false;
      for (RelationshipVersionBI<?> rv : versions) {
        // TODO: need to support refset specs
        if (rv.getPathNid() != pathNid) {
          write = false;
        }
        if (write) {
          for (int parentNid : taxonomyParentNids) {
            if (dataStore.isKindOf(rv.getDestinationNid(), parentNid,
                viewCoordinate)) {
              inTaxonomy = true;
            }
          }
          if (inTaxonomy) {

            if (rv.getCharacteristicNid() == SnomedMetadataRfx
                .getREL_CH_INFERRED_RELATIONSHIP_NID()) {
              processInferredRelationship(rv);
            } else if (rv.getCharacteristicNid() == SnomedMetadataRfx
                .getREL_CH_STATED_RELATIONSHIP_NID()) {
              processStatedRelationship(rv);
            }
          }

        }
      }
    }
  }

  /**
   * Writes the inferred relationships file according to the fields specified in
   * <code>
   * Rf2File.RelationshipsFileFields</code>.
   *
   * @param relationshipVersion the relationship version to write
   * @throws IOException signals that an I/O exception has occurred
   * @see Rf2File.RelationshipsFileFields
   */
  private void processInferredRelationship(
    RelationshipVersionBI<?> relationshipVersion) throws IOException {
    for (Rf2File.RelationshipsFileFields field : Rf2File.RelationshipsFileFields
        .values()) {
      switch (field) {
        case ACTIVE:
          relationshipsWriter
              .write((relationshipVersion.isActive() ? "1" : "0")
                  + field.seperator);

          break;

        case EFFECTIVE_TIME:
          relationshipsWriter.write(TimeHelper.getShortFileDateFormat().format(
              relationshipVersion.getTime())
              + field.seperator);
          break;

        case ID:
          relationshipsWriter.write(getSctIdOrUuidForNid(relationshipVersion
              .getNid()) + field.seperator);

          break;

        case MODULE_ID:
          relationshipsWriter.write(getSctIdOrUuidForNid(relationshipVersion
              .getModuleNid()) + field.seperator);

          break;

        case SOURCE_ID:
          relationshipsWriter.write(getSctIdOrUuidForNid(relationshipVersion
              .getConceptNid()) + field.seperator);

          break;

        case DESTINATION_ID:
          relationshipsWriter.write(getSctIdOrUuidForNid(relationshipVersion
              .getDestinationNid()) + field.seperator);

          break;

        case RELATIONSHIP_GROUP:
          relationshipsWriter.write(relationshipVersion.getGroup()
              + field.seperator);

          break;

        case TYPE_ID:
          relationshipsWriter.write(getSctIdOrUuidForNid(relationshipVersion
              .getTypeNid()) + field.seperator);

          break;

        case CHARCTERISTIC_ID:
          relationshipsWriter.write(getSctIdOrUuidForNid(relationshipVersion
              .getCharacteristicNid()) + field.seperator);

          break;

        case MODIFIER_ID:
          relationshipsWriter.write(SOME + field.seperator);

          break;
        default:
          break;
      }
    }
  }

  /**
   * Writes the stated relationships file according to the fields specified in
   * <code>
   * Rf2File.RelationshipsFileFields</code>.
   *
   * @param relationshipVersion the relationship version to write
   * @throws IOException signals that an I/O exception has occurred
   * @see Rf2File.RelationshipsFileFields
   */
  private void processStatedRelationship(
    RelationshipVersionBI<?> relationshipVersion) throws IOException {
    for (Rf2File.RelationshipsFileFields field : Rf2File.RelationshipsFileFields
        .values()) {
      switch (field) {
        case ACTIVE:
          relationshipsStatedWriter.write((relationshipVersion.isActive() ? "1"
              : "0") + field.seperator);

          break;

        case EFFECTIVE_TIME:
          relationshipsStatedWriter.write(TimeHelper.getShortFileDateFormat()
              .format(relationshipVersion.getTime()) + field.seperator);
          break;

        case ID:
          relationshipsStatedWriter
              .write(getSctIdOrUuidForNid(relationshipVersion.getNid())
                  + field.seperator);

          break;

        case MODULE_ID:
          relationshipsStatedWriter
              .write(getSctIdOrUuidForNid(relationshipVersion.getModuleNid())
                  + field.seperator);

          break;

        case SOURCE_ID:
          relationshipsStatedWriter
              .write(getSctIdOrUuidForNid(relationshipVersion.getConceptNid())
                  + field.seperator);

          break;

        case DESTINATION_ID:
          relationshipsStatedWriter
              .write(getSctIdOrUuidForNid(relationshipVersion
                  .getDestinationNid()) + field.seperator);

          break;

        case RELATIONSHIP_GROUP:
          relationshipsStatedWriter.write(relationshipVersion.getGroup()
              + field.seperator);

          break;

        case TYPE_ID:
          relationshipsStatedWriter
              .write(getSctIdOrUuidForNid(relationshipVersion.getTypeNid())
                  + field.seperator);

          break;

        case CHARCTERISTIC_ID:
          relationshipsStatedWriter
              .write(getSctIdOrUuidForNid(relationshipVersion
                  .getCharacteristicNid()) + field.seperator);

          break;

        case MODIFIER_ID:
          relationshipsStatedWriter.write(SOME + field.seperator);

          break;
        default:
          break;
      }
    }
  }

  /**
   * Processes a language refex member to determine if the language is English
   * or another language. Only the versions which have a stamp nid in the
   * specified collection of stamp nids will be written.
   *
   * @param refexChronicle refex member to process
   * @throws IOException signals that an I/O exception has occurred
   * @throws ContradictionException the contradiction exception
   */
  private void processLangRefsets(RefexChronicleBI<?> refexChronicle)
    throws IOException, ContradictionException {
    if (refexChronicle != null) {
      if (!excludedRefsetIds.contains(refexChronicle.getNid())) {
        Collection<RefexVersionBI<?>> versions = new HashSet<>();
        if (releaseType.equals(ReleaseType.FULL)) {
          for (Object o : refexChronicle.getVersions()) {
            RefexVersionBI<?> r = (RefexVersionBI<?>) o;
            if (r.getPathNid() == pathNid) {
              versions.add(r);
            }
          }
        } else {
          RefexVersionBI<?> version = refexChronicle.getVersion(viewCoordinate);
          if (version != null) {
            versions.add(version);
          }
        }
        boolean write = true;
        for (RefexVersionBI<?> rv : versions) {
          if (!rv.isActive()) {
            if (rv.getPathNid() != pathNid) {
              write = false;
            }
          }
          if (rv.isActive()) { // CHANGE FOR DK, source data incorrect, retired
                               // descriptions should also have retired lang
                               // refsets
            ComponentVersionBI rc =
                dataStore.getComponentVersion(viewCoordinate,
                    rv.getReferencedComponentNid());
            if (rc == null || !rc.isActive()) {
              write = false;
            }
          }
          if (write) {
            if (refexChronicle.getNid() == SnomedMetadataRf2.GB_ENGLISH_REFSET_RF2
                .getNid()) {
              processLang(rv);
            } else if (refexChronicle.getNid() == SnomedMetadataRf2.US_ENGLISH_REFSET_RF2
                .getLenient().getNid()) {
              processLang(rv);
            } else {
              if (otherLangRefsetsWriter != null) {
                processOtherLang(rv);
              }
            }
          }

        }
      }
    }
  }

  /**
   * Writes the English language refset file according to the fields specified
   * in <code>
   * Rf2File.LanguageRefsetFileFields</code>.
   *
   * @param refexVersion the refex member to write
   * @throws IOException signals that an I/O exception has occurred
   */
  private void processLang(RefexVersionBI<?> refexVersion) throws IOException {
    RefexNidVersionBI<?> refexNidVersion = (RefexNidVersionBI<?>) refexVersion;
    if (refexVersion != null) {
      for (Rf2File.LanguageRefsetFileFields field : Rf2File.LanguageRefsetFileFields
          .values()) {
        switch (field) {
          case ID:
            langRefsetsWriter.write(refexVersion.getPrimordialUuid()
                + field.seperator);

            break;

          case EFFECTIVE_TIME:
            langRefsetsWriter.write(TimeHelper.getShortFileDateFormat().format(
                refexVersion.getTime())
                + field.seperator);
            break;

          case ACTIVE:
            langRefsetsWriter.write((refexVersion.isActive() ? "1" : "0")
                + field.seperator);

            break;

          case MODULE_ID:
            langRefsetsWriter.write(getSctIdOrUuidForNid(refexVersion
                .getModuleNid()) + field.seperator);

            break;

          case REFSET_ID:
            langRefexNids.add(refexVersion.getNid());
            langRefsetsWriter.write(dataStore.getComponent(
                refexVersion.getNid()).getPrimordialUuid()
                + field.seperator);

            break;

          case REFERENCED_COMPONENT_ID:
            langRefsetsWriter.write(dataStore.getComponent(
                refexVersion.getReferencedComponentNid()).getPrimordialUuid()
                + field.seperator);

            break;

          case ACCEPTABILITY:
            langRefsetsWriter.write(dataStore.getComponent(
                refexNidVersion.getNid1()).getPrimordialUuid()
                + field.seperator);

            break;
          default:
            break;
        }
      }
    }
  }

  /**
   * Writes the language refset file according to the fields specified in <code>
   * Rf2File.LanguageRefsetFileFields</code>. Uses the language specified in the
   * constructor.
   *
   * @param refexVersion the refex member to write
   * @throws IOException signals that an I/O exception has occurred
   */
  private void processOtherLang(RefexVersionBI<?> refexVersion)
    throws IOException {
    if (refexVersion != null) {
      RefexNidVersionBI<?> refexNidVersion =
          (RefexNidVersionBI<?>) refexVersion;
      for (Rf2File.LanguageRefsetFileFields field : Rf2File.LanguageRefsetFileFields
          .values()) {
        switch (field) {
          case ID:
            otherLangRefsetsWriter.write(refexVersion.getPrimordialUuid()
                + field.seperator);

            break;

          case EFFECTIVE_TIME:
            otherLangRefsetsWriter.write(TimeHelper.getShortFileDateFormat()
                .format(refexVersion.getTime()) + field.seperator);
            break;

          case ACTIVE:
            otherLangRefsetsWriter.write((refexVersion.isActive() ? "1" : "0")
                + field.seperator);

            break;

          case MODULE_ID:
            otherLangRefsetsWriter.write(getSctIdOrUuidForNid(refexVersion
                .getModuleNid()) + field.seperator);

            break;

          case REFSET_ID:
            langRefexNids.add(refexVersion.getNid());
            otherLangRefsetsWriter.write(dataStore.getComponent(
                refexVersion.getNid()).getPrimordialUuid()
                + field.seperator);

            break;

          case REFERENCED_COMPONENT_ID:
            otherLangRefsetsWriter.write(dataStore.getComponent(
                refexVersion.getReferencedComponentNid()).getPrimordialUuid()
                + field.seperator);

            break;

          case ACCEPTABILITY:
            otherLangRefsetsWriter.write(dataStore.getComponent(
                refexNidVersion.getNid1()).getPrimordialUuid()
                + field.seperator);

            break;
          default:
            break;
        }
      }
    }
  }

  /**
   * Writes the refset description file for the language refexes according to
   * the <code>Rf2File.RefsetDescriptorFileFields</code>.
   *
   * @param refexNid the nid associated with a language refex
   * @throws IOException signals that an I/O exception has occurred
   * @throws NoSuchAlgorithmException indicates that a no such algorithm
   *           exception has occurred
   * @throws ContradictionException the contradiction exception
   * @see Rf2File.RefsetDescriptorFileFields
   */
  private void processLanguageRefsetDesc(int refexNid) throws IOException,
    NoSuchAlgorithmException, ContradictionException {
    ConceptSpec refsetDescriptor =
        new ConceptSpec(
            "Reference set descriptor reference set (foundation metadata concept)",
            UUID.fromString("5ddff82f-5aee-3b16-893f-6b7aa726cc4b"));
    ConceptSpec attribDesc =
        new ConceptSpec("Description in dialect (foundation metadata concept)",
            UUID.fromString("db73d522-612f-3dcd-a793-f62d4f0c41fe"));
    ConceptSpec attribType =
        new ConceptSpec(
            "Description type component (foundation metadata concept)",
            UUID.fromString("4ea66278-f8a7-37d3-90fa-88c19cc107a6"));
    for (Rf2File.RefsetDescriptorFileFields field : Rf2File.RefsetDescriptorFileFields
        .values()) {
      switch (field) {
        case ID:
          // referenced component, attribute order
          UUID uuid =
              UuidT5Generator.get(REFSET_DESC_NAMESPACE, dataStore
                  .getUuidPrimordialForNid(refexNid).toString() + 0);
          refsetDescWriter.write(uuid + field.seperator);

          break;

        case EFFECTIVE_TIME:
          refsetDescWriter.write(effectiveDateString + field.seperator);

          break;

        case ACTIVE:
          refsetDescWriter.write("1" + field.seperator);

          break;

        case MODULE_ID:
          refsetDescWriter.write(getSctIdOrUuidForNid((dataStore
              .getComponentVersion(viewCoordinate, refexNid).getModuleNid()))
              + field.seperator);

          break;

        case REFSET_ID:
          refsetDescWriter.write(refsetDescriptor.getStrict(viewCoordinate)
              .getPrimordialUuid() + field.seperator);

          break;

        case REFERENCED_COMPONENT_ID:
          refsetDescWriter.write(getSctIdOrUuidForNid(refexNid)
              + field.seperator);

          break;

        case ATTRIB_DESC:
          refsetDescWriter.write(attribDesc.getStrict(viewCoordinate)
              .getPrimordialUuid() + field.seperator);

          break;

        case ATTRIB_TYPE:
          refsetDescWriter.write(attribType.getStrict(viewCoordinate)
              .getPrimordialUuid() + field.seperator);

          break;

        case ATTRIB_ORDER:
          refsetDescWriter.write(0 + field.seperator);

          break;
        default:
          break;
      }
    }
  }

  /**
   * Processes each concept to determine if the concept or any of its components
   * should be written to the release files.
   *
   * @param cNid the nid of the concept to process
   * @param fetcher the fetcher for getting the concept associated with * * the
   *          <code>cNid</code> from the database
   * @throws Exception indicates an exception has occurred
   */
  @SuppressWarnings("cast")
  @Override
  public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher)
    throws Exception {
    if (conceptsToProcess.isMember(cNid)) {
      count++;
      ConceptVersionBI concept = fetcher.fetch(WBUtility.getViewCoordinate());
      LOG.debug("Process concept " + concept.getPrimordialUuid());
      process(concept);
    }
    allCount++;
    // Handle progress monitor
    if ((int) ((allCount * 100) / progressMax) > progress) {
      progress = (allCount * 100) / progressMax;
      fireProgressEvent(progress, progress + " % finished");
    }
  }

  /**
   * Returns the nid set.
   *
   * @return the set of nids associated with concept to be included in this
   *         release
   * @throws IOException signals that an I/O exception has occurred
   */
  @Override
  public NativeIdSetBI getNidSet() throws IOException {
    return conceptsToProcess;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.tcc.api.concept.ProcessUnfetchedConceptDataBI#allowCancel()
   */
  @Override
  public boolean allowCancel() {
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.tcc.api.concept.ProcessUnfetchedConceptDataBI#getTitle()
   */
  @Override
  public String getTitle() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void cancel() {
    requestCancel = true;    
  }


}
