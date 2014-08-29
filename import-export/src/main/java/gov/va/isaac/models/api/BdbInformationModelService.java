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
package gov.va.isaac.models.api;

import gov.va.isaac.AppContext;
import gov.va.isaac.model.InformationModelType;
import gov.va.isaac.models.DefaultInformationModelProperty;
import gov.va.isaac.models.InformationModel;
import gov.va.isaac.models.InformationModelAux;
import gov.va.isaac.models.InformationModelMetadata;
import gov.va.isaac.models.InformationModelProperty;
import gov.va.isaac.models.util.DefaultInformationModel;
import gov.va.isaac.util.WBUtility;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.lucene.queryparser.classic.ParseException;
import org.ihtsdo.otf.query.lucene.LuceneDescriptionIndexer;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.ConceptCB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDynamicCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RelationshipCAB;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.lang.LanguageCode;
import org.ihtsdo.otf.tcc.api.metadata.binding.RefexDynamic;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicUsageDescription;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipType;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.RefexDynamicUsageDescriptionBuilder;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicString;
import org.ihtsdo.otf.tcc.model.index.service.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a service for interacting with information models that uses an
 * ISAAC BDB back-end for persistence.
 *
 * @author bcarlsenca
 */
public class BdbInformationModelService implements InformationModelService {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory
      .getLogger(BdbInformationModelService.class);

  /** The data store. */
  private BdbTerminologyStore dataStore;

  /**
   * Instantiates a {@link BdbInformationModelService} from the specified
   * parameters.
   *
   * @param dataStore the data store
   * @throws IOException
   */
  public BdbInformationModelService(BdbTerminologyStore dataStore)
      throws IOException {
    if (dataStore == null) {
      throw new IOException("Data store unexpectedly null");
    }
    this.dataStore = dataStore;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.va.isaac.models.api.InformationModelService#exists(gov.va.isaac.models
   * .InformationModel)
   */
  @Override
  public boolean exists(InformationModel model) throws IOException {
    LOG.info("Check whether information model exists: "
        + model.getType().getDisplayName() + ", " + model.getKey());

    if (model.getType() == null) {
      throw new IOException("Model unexpectedly has null type");
    }
    if (model.getKey() == null) {
      throw new IOException("Model unexpectedly has null key");
    }
    try {
      boolean flag =
          getInformationModel(model.getType(), model.getKey()) != null;
      LOG.debug("  " + flag);
      return flag;
    } catch (ContradictionException e) {
      e.printStackTrace();
      throw new IOException(
          "Unexpected inability to acquire UUID for type and key, "
              + model.getType() + ", " + model.getKey());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.va.isaac.models.api.InformationModelService#getInformationModel(java
   * .util.UUID)
   */
  @Override
  public InformationModel getInformationModel(UUID uuid) throws IOException,
    ContradictionException {
    if (uuid == null) {
      throw new IOException("UUID unexpectedly null");
    }
    LOG.info("Get information model: " + uuid);
    InformationModel model =
        conceptToInformationModel(dataStore.getConcept(uuid));
    LOG.debug("  " + model.getType().getDisplayName() + ", " + model.getKey());
    return model;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.va.isaac.models.api.InformationModelService#getInformationModel(java
   * .lang.String, java.lang.String)
   */
  @Override
  public InformationModel getInformationModel(InformationModelType type,
    String key) throws IOException, ContradictionException {
    if (type == null) {
      throw new IllegalArgumentException("Unexpected null type parameter.");
    }
    LOG.info("Get information model: " + type.getDisplayName() + ", " + key);
    if (key == null) {
      throw new IllegalArgumentException("Unexpected null key parameter.");
    }
    try {
      LOG.debug("  Lucene Search: '" + key + "'");
      LuceneDescriptionIndexer descriptionIndexer =
          AppContext.getService(LuceneDescriptionIndexer.class);
      if (descriptionIndexer == null) {
        throw new IOException("No description indexer found, aborting.");
      }
      // Look for description matches.
      ComponentProperty field = ComponentProperty.DESCRIPTION_TEXT;
      int limit = 10;
      List<SearchResult> searchResults;
      searchResults =
          descriptionIndexer.query(key, false, field, limit, Long.MIN_VALUE);

      // Results are descriptions, need to look up concepts
      for (SearchResult result : searchResults) {
        int conceptNid =
            dataStore.getComponent(result.getNid()).getConceptNid();
        ConceptVersionBI conceptVersion =
            WBUtility.getConceptVersion(conceptNid);
        LOG.debug("    Check " + conceptVersion.getPrimordialUuid() + ", "
            + conceptVersion.getPreferredDescription().getText());

        if (conceptVersion.getPreferredDescription().getText().equals(key)
            || conceptVersion.getFullySpecifiedDescription().getText()
                .equals(key)) {
          LOG.debug("    Found match, compute information model type");
          InformationModelType computedType =
              computeInformationModelType(conceptVersion);
          if (computedType == null) {
            LOG.debug("      type = null");
            continue;
          } else {
            LOG.debug("      type = " + computedType.getDisplayName());
          }
          if (type == computedType) {
            InformationModel model =
                getInformationModel(conceptVersion.getPrimordialUuid());
            LOG.debug("  Model found, UUID = " + model.getUuid());
            return model;
          }
        }
      }
      LOG.debug("  Model not found");
      return null;
    } catch (ParseException e) {
      throw new IOException("Unable to parse key for lucene search, " + key, e);
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.va.isaac.models.api.InformationModelService#createOrAmmendInformationModel
   * (gov.va.isaac.models.InformationModel)
   */
  @Override
  public void saveInformationModel(InformationModel model) throws IOException,
    InvalidCAB, ContradictionException, NoSuchAlgorithmException,
    PropertyVetoException {
    if (model == null) {
      throw new IOException("Model unexpectedly null");
    }
    LOG.info("Save information model: " + model.getType().getDisplayName()
        + ", " + model.getKey());
    ConceptChronicleBI modelConcept = syncInformationModelConcept(model);
    LOG.debug("  add uncommitted");
    WBUtility.addUncommitted(modelConcept);
    LOG.debug("  commit");
    WBUtility.commit();
  }

  /**
   * Concept to information model.
   *
   * @param modelConcept the model concept
   * @return the information model
   * @throws ContradictionException
   * @throws IOException
   */
  private InformationModel conceptToInformationModel(
    ConceptChronicleBI modelConcept) throws IOException, ContradictionException {
    if (modelConcept == null) {
      throw new IOException("Model concept unexpectedly null");
    }

    LOG.info("Convert concept to information model: "
        + modelConcept.getPrimordialUuid());

    // Key is the FN
    String key = WBUtility.getFullySpecifiedName(modelConcept);
    LOG.debug("  key = " + key);
    // Name is the PT
    String name = WBUtility.getConPrefTerm(modelConcept.getNid());
    LOG.debug("  name = " + name);
    // UUID of the concept
    UUID uuid = modelConcept.getPrimordialUuid();
    LOG.debug("  uuid = " + uuid);
    // information model type - obtain by walking up the tree
    InformationModelType type = computeInformationModelType(modelConcept);
    LOG.debug("  type = " + type.getDisplayName());
    InformationModel model = new DefaultInformationModel(key, name, uuid, type);

    // Assemble Metadata
    // Need to look at stamp nid for concept attributes instead of concept
    int stampNid =
        modelConcept.getConceptAttributes()
            .getVersion(WBUtility.getViewCoordinate()).getStamp();
    InformationModelMetadata metadata =
        InformationModelMetadata.newInstance(stampNid, dataStore,
            WBUtility.getViewCoordinate());
    model.setMetadata(metadata);

    // Get property information from dynamic refset members
    // Create refex entries for properties
    RefexDynamicUsageDescription propertyRefset =
        RefexDynamicUsageDescriptionBuilder
            .readRefexDynamicUsageDescriptionConcept(InformationModelAux.INFORMATION_MODEL_PROPERTIES_REFSET
                .getLenient().getNid());
    LOG.debug("  property refset = " + propertyRefset.getRefexName());
    for (RefexDynamicChronicleBI<?> refex : modelConcept
        .getRefexDynamicAnnotations()) {
      RefexDynamicVersionBI<?> refexVersion =
          refex.getVersion(WBUtility.getViewCoordinate());
      // Look for matching refex id and "active" flag
      if (refex.getAssemblageNid() == propertyRefset
          .getRefexUsageDescriptorNid() && refexVersion.isActive()) {
        // Create properties for each annotation
        InformationModelProperty property =
            new DefaultInformationModelProperty();
        RefexDynamicDataBI[] data = refexVersion.getData();
        property.setLabel(((RefexDynamicString) data[0]).getDataString());
        property.setType(((RefexDynamicString) data[1]).getDataString());
        property.setName(((RefexDynamicString) data[2]).getDataString());
        property
            .setDefaultValue(((RefexDynamicString) data[3]).getDataString());
        property.setValue(((RefexDynamicString) data[4]).getDataString());
        property.setCardinalityMin(((RefexDynamicString) data[5])
            .getDataString());
        property.setCardinalityMax(((RefexDynamicString) data[6])
            .getDataString());
        property.setVisibility(((RefexDynamicString) data[7]).getDataString());
        LOG.debug("    property " + property.getLabel() + ", "
            + property.getName());
        model.addProperty(property);
      }
    }

    // Build associated concept UUIDs from relationships
    for (RelationshipChronicleBI rel : modelConcept.getRelationshipsOutgoing()) {
      RelationshipVersionBI<?> relVersion =
          rel.getVersion(WBUtility.getViewCoordinate());
      // Look for matching typeId and "active" flag
      if (relVersion.getTypeNid() == InformationModelAux.HAS_TERMINOLOGY_CONCEPT
          .getLenient().getNid() && relVersion.isActive()) {
        // Add the destination UUID
        model.addAssociatedConceptUuid(WBUtility.getConceptVersion(
            relVersion.getDestinationNid()).getPrimordialUuid());
      }
    }

    return model;
  }

  /**
   * Returns the information model type. Walks up the tree from the
   * corresponding concept to the level just below "information models" root and
   * returns the type corresponding with the preferred name of the concept at
   * that level.
   *
   * @param modelConcept the model concept
   * @return the information model type
   * @throws ContradictionException
   * @throws IOException
   */
  private InformationModelType computeInformationModelType(
    ConceptChronicleBI modelConcept) throws IOException, ContradictionException {
    if (modelConcept == null) {
      throw new IOException("Model concept unexpectedly null");
    }
    LOG.info("Compute information model type: "
        + modelConcept.getPrimordialUuid());
    ConceptVersionBI concept =
        WBUtility.getConceptVersion(modelConcept.getNid());
    if (concept == null) {
      throw new IOException("Model concept version unexpectedly null "
          + modelConcept.getPrimordialUuid());
    }
    if (!concept.isActive()) {
      throw new IOException("Model concept is inactive "
          + modelConcept.getPrimordialUuid());
    }

    InformationModelType[] values = InformationModelType.values();
    // Walk up tree until we encounter a "type" concept
    while (true) {
      // Look for match (the pref name will match an information model type)
      String prefName = WBUtility.getConPrefTerm(concept.getNid());
      if (prefName == null) {
        throw new IOException("Concept preferred name unexepectedly null "
            + concept.getPrimordialUuid());
      }
      for (InformationModelType type : values) {
        if (prefName.equals(type.getDisplayName())) {
          LOG.debug("  FOUND TYPE: " + type.getDisplayName());
          return type;
        }
      }

      // continue walking
      @SuppressWarnings("rawtypes")
      Collection<? extends RelationshipVersionBI> rels =
          concept.getRelationshipsOutgoingActiveIsa();
      if (rels.size() < 1) {
        break;
      }
      concept =
          WBUtility.getConceptVersion(rels.iterator().next()
              .getDestinationNid());
      LOG.debug("  " + concept.getPrimordialUuid());
    }
    throw new IOException("Information model type not found for "
        + modelConcept.getPrimordialUuid());
  }

  /**
   * Synchronizes the current state of the information model with the underlying
   * strored concept representation. If it does not exist, it creates it. If it
   * has changed, it updates it.
   *
   * TODO: to properly implement sync, we should take the old version of the
   * model and the new version of the model and compare each field to see
   * whether its changed. either retire or add the appropriate underlying data
   * element.
   * @param modelConcept the model concept
   * @return the information model
   * @throws ContradictionException
   * @throws InvalidCAB
   * @throws IOException
   * @throws UnsupportedEncodingException
   * @throws NoSuchAlgorithmException
   * @throws PropertyVetoException
   */
  private ConceptChronicleBI syncInformationModelConcept(InformationModel model)
    throws NoSuchAlgorithmException, UnsupportedEncodingException, IOException,
    InvalidCAB, ContradictionException, PropertyVetoException {
    if (model == null) {
      throw new IOException("Model unexpectedly null");
    }
    if (exists(model)) {
      throw new IOException(
          "Model already imported, sync to allow reimport of models is underway but not ready yet");
      // need to handle importing of a retired model if we do this.
      // consider whether "exists" means "exists or active?"
    }
    LOG.info("Convert information model to concept");

    // Determine parent
    InformationModelType type = model.getType();
    ConceptChronicleBI parent = dataStore.getConcept(type.getUuid());

    // Create concept
    LOG.debug("  parent = " + parent.getPrimordialUuid());
    LOG.debug("  FN = " + model.getKey());
    LOG.debug("  PT = " + model.getName());
    ConceptChronicleBI modelConcept =
        createNewConcept(parent, model.getKey(), model.getName());
    LOG.debug("  UUID = " + modelConcept.getPrimordialUuid());

    // Create refex entries for properties
    RefexDynamicUsageDescription propertyRefset =
        RefexDynamicUsageDescriptionBuilder
            .readRefexDynamicUsageDescriptionConcept(InformationModelAux.INFORMATION_MODEL_PROPERTIES_REFSET
                .getLenient().getNid());
    LOG.debug("  Found " + propertyRefset.getRefexName());
    // Iterate through information model properties and add refexes
    LOG.debug("  Iterate through properties");
    for (InformationModelProperty property : model.getProperties()) {
      LOG.debug("    " + property.getLabel() + ", " + property.getName());
      // Need better understanding of what IdDirective to use here
      RefexDynamicCAB refexBlueprint =
          new RefexDynamicCAB(modelConcept.getNid(),
              propertyRefset.getRefexUsageDescriptorNid());

      // The order of these columns is tightly bound to the definition,
      // if the definition changes, this has to be updated as well.
      if (propertyRefset.getColumnInfo().length != 8) {
        throw new IOException(
            "Information model properties refset has unexpected number of columns");
      }
      RefexDynamicDataBI[] data =
          new RefexDynamicDataBI[propertyRefset.getColumnInfo().length];
      data[0] = new RefexDynamicString(property.getLabel());
      data[1] = new RefexDynamicString(property.getType());
      data[2] = new RefexDynamicString(property.getName());
      data[3] = new RefexDynamicString(property.getDefaultValue());
      data[4] = new RefexDynamicString(property.getValue());
      data[5] = new RefexDynamicString(property.getCardinalityMin());
      data[6] = new RefexDynamicString(property.getCardinalityMax());
      data[7] = new RefexDynamicString(property.getVisibility());
      refexBlueprint.setData(data, WBUtility.getViewCoordinate());

      // Construct and wire the dynamic refex
      RefexDynamicChronicleBI<?> refex = WBUtility.getBuilder().construct(refexBlueprint);
      modelConcept.addDynamicAnnotation(refex);
      LOG.debug("    UUID = " + refex.getPrimordialUuid());
    }

    // Sync "has terminology concept" relationships
    LOG.debug("  Iterate through associated UUIDs");
    Set<UUID> associatedConceptUuids = model.getAssociatedConceptUuids();
    // Retire any active relationships to UUIDs no longer in this set
    for (RelationshipChronicleBI rel : modelConcept.getRelationshipsOutgoing()) {
      RelationshipVersionBI<?> relVersion =
          rel.getVersion(WBUtility.getViewCoordinate());
      // Look for matching typeId and "active" flag
      if (relVersion.getTypeNid() == InformationModelAux.HAS_TERMINOLOGY_CONCEPT
          .getLenient().getNid() && relVersion.isActive()) {
        UUID uuid =
            WBUtility.getConceptVersion(relVersion.getDestinationNid())
                .getPrimordialUuid();
        // If the UUID is not in range, retire the rel
        if (!associatedConceptUuids.contains(uuid)) {
          LOG.debug("    Found relationship to retire - "
              + WBUtility.getConPrefTerm(relVersion.getDestinationNid()));
          RelationshipCAB relCab =
              relVersion.makeBlueprint(WBUtility.getViewCoordinate(),
                  IdDirective.PRESERVE, RefexDirective.INCLUDE);
          relCab.setRetired();
          WBUtility.getBuilder().constructIfNotCurrent(relCab);
        }

        // Otherwise, remove from list, no need to create it
        else {
          associatedConceptUuids.remove(uuid);
        }
      }

      // Create rels for any UUIDs not accounted for
      for (UUID destinationUuid : associatedConceptUuids) {
        LOG.debug("  Create relationship for " + modelConcept.getPrimordialUuid() 
            + " => " + destinationUuid);
        UUID typeUid =
            InformationModelAux.HAS_TERMINOLOGY_CONCEPT.getLenient()
                .getPrimordialUuid();
        int group = 0;
        RelationshipType relType = RelationshipType.STATED_ROLE;
        IdDirective idDir = IdDirective.GENERATE_HASH;
        RelationshipCAB newRel =
            new RelationshipCAB(modelConcept.getPrimordialUuid(), typeUid,
                destinationUuid, group, relType, idDir);
        WBUtility.getBuilder().construct(newRel);
      }
    }

    return modelConcept;
  }

  /**
   * Creates the new concept and preserves the computed UUID.
   *
   * @param parent the parent
   * @param fsn the fsn
   * @param prefTerm the pref term
   * @return the concept chronicle bi
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws InvalidCAB the invalid cab
   * @throws ContradictionException the contradiction exception
   */
  private ConceptChronicleBI createNewConcept(ConceptChronicleBI parent,
    String fsn, String prefTerm) throws IOException, InvalidCAB,
    ContradictionException {
    if (parent == null) {
      throw new IOException("Parent is unexpectedly null");
    }
    if (fsn == null) {
      throw new IOException("FN is unexpectedly null");
    }
    if (prefTerm == null) {
      throw new IOException("PT is unexpectedly null");
    }
    ConceptCB newConCB = createNewConceptBlueprint(parent, fsn, prefTerm);
    ConceptChronicleBI newCon =
        WBUtility.getBuilder().constructIfNotCurrent(newConCB);
    return newCon;
  }

  /**
   * Creates the new concept blueprint
   *
   * @param parent the parent
   * @param fsn the fsn
   * @param prefTerm the pref term
   * @return the concept cb
   * @throws ValidationException the validation exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws InvalidCAB the invalid cab
   * @throws ContradictionException the contradiction exception
   */
  private ConceptCB createNewConceptBlueprint(ConceptChronicleBI parent,
    String fsn, String prefTerm) throws ValidationException, IOException,
    InvalidCAB, ContradictionException {
    if (parent == null) {
      throw new IOException("Parent is unexpectedly null");
    }
    if (fsn == null) {
      throw new IOException("FN is unexpectedly null");
    }
    if (prefTerm == null) {
      throw new IOException("PT is unexpectedly null");
    }
    // Assume language US English
    LanguageCode lc = LanguageCode.EN_US;
    // Assume hierarchy is represented as IS_A rel
    UUID isA = Snomed.IS_A.getUuids()[0];
    // Compute UUIDs in standard way
    IdDirective idDir = IdDirective.GENERATE_HASH;
    // Assume module should be SNOMED core module (this may be something to
    // change)
    UUID module = Snomed.CORE_MODULE.getLenient().getPrimordialUuid();
    // Create single parent
    UUID parentUUIDs[] = new UUID[1];
    parentUUIDs[0] = parent.getPrimordialUuid();
    return new ConceptCB(fsn, prefTerm, lc, isA, idDir, module, parentUUIDs);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.va.isaac.models.api.InformationModelService#createMetadataConcepts()
   */
  @Override
  public void createMetadataConcepts() throws IOException, InvalidCAB,
    ContradictionException, PropertyVetoException {
    LOG.info("Create information models metadata concepts");

    // Create columns for dynamic refsets
    String[] columnNames =
        new String[] {
            "info model property label", "info model property type",
            "info model property name", "info model property default value",
            "info model property value", "info model property cardinality min",
            "info model property cardinality max",
            "info model property visibility"
        };
    String[] columnDescriptions =
        new String[] {
            "Used to capture the label for a property as used by the native information model type, e.g. 'qual' in CEM",
            "Used to capture the property type as expressed in the model, e.g. 'MethodDevice' in CEM",
            "Used to capture the property name as expressed in the model",
            "Used to capture any default value the property has in the model",
            "Used to capture any actual value the property has (for demo purposes)",
            "Used to capture the cardinality lower limit in the model",
            "Used to capture the cardinality upper limit in the model",
            "Used to capture the property visibility in the model"
        };
    List<RefexDynamicColumnInfo> columns = new ArrayList<>();
    for (int i = 0; i < columnNames.length; i++) {
      LOG.debug("  Attempting to create COLUMN " + columnNames[i]);
      ConceptChronicleBI columnConcept =
          RefexDynamicColumnInfo.createNewRefexDynamicColumnInfoConcept(
              columnNames[i], columnDescriptions[i]);
      LOG.debug("    PT = " + WBUtility.getConPrefTerm(columnConcept.getNid()));
      LOG.debug("    UUID = " + columnConcept.getPrimordialUuid());
      RefexDynamicColumnInfo column =
          new RefexDynamicColumnInfo(i, columnConcept.getPrimordialUuid(),
              RefexDynamicDataType.STRING, null, true, null, null);
      columns.add(column);
    }

    // Create dynamic refset for information model properties
    RefexDynamicColumnInfo[] columnArray =
        columns.toArray(new RefexDynamicColumnInfo[] {});
    LOG.debug("  Attempting to create info model property refset");
    RefexDynamicUsageDescription refex =
        RefexDynamicUsageDescriptionBuilder
            .createNewRefexDynamicUsageDescriptionConcept(
                "Information model property refset",
                "Information model property refset",
                "Used to capture information about information model properties",
                columnArray, RefexDynamic.REFEX_DYNAMIC_IDENTITY.getLenient()
                    .getPrimordialUuid(), true);
    ConceptVersionBI refexConcept =
        WBUtility.getConceptVersion(refex.getRefexUsageDescriptorNid());
    LOG.debug("    PT = " + refexConcept.getPreferredDescription().getText());
    LOG.debug("    UUID = " + refexConcept.getPrimordialUuid());

    // Create relationship type metadata for connecting
    // info models to terminology - this is to avoid having
    // a dependency on a SNOMED relationship - though there is
    // now a dependency on term aux.
    LOG.debug("  Create rel type concept");
    UUID parentUuid = UUID.fromString("4c3152a8-c927-330f-868d-b065a3362558");
    ConceptChronicleBI parent = WBUtility.getConceptVersion(parentUuid);
    ConceptChronicleBI relTypeConcept =
        createNewConcept(parent, "Has terminology concept",
            "Has terminology concept");
    LOG.debug("    PT = " + WBUtility.getConPrefTerm(relTypeConcept.getNid()));
    LOG.debug("    UUID = " + relTypeConcept.getPrimordialUuid());
    dataStore.addUncommitted(relTypeConcept);
    dataStore.commit(relTypeConcept);

  }

}
