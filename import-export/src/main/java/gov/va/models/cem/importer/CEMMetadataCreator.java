/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.models.cem.importer;

import gov.va.isaac.gui.AppContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.blueprint.ConceptCB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.TerminologyBuilderBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.lang.LanguageCode;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.datastore.BdbTermBuilder;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author alo
 */
public class CEMMetadataCreator {

    private static final Logger LOG = LoggerFactory.getLogger(CEMMetadataCreator.class);

    private static AppContext appContext;
    private static BdbTerminologyStore ds;
    private static TerminologyBuilderBI builder;

    public static void createMetadata(final AppContext appContext) throws Exception {
        LOG.info("Preparing to create metadata");
        CEMMetadataCreator.appContext = appContext;
        ds = appContext.getDataStore();

        ConceptChronicleBI refsetsRoot = ds.getConcept(UUID.fromString("7e38cd2d-6f1a-3a81-be0b-21e6090573c2"));
        LOG.info("Refsets root:" + refsetsRoot.toString());

        ConceptChronicleBI CEMRoot = createNewConcept(refsetsRoot, "CEM reference sets (foundation metadata concept)", "CEM reference sets");

        ConceptChronicleBI CEMDataRefset = createNewConcept(CEMRoot, "CEM data reference set (foundation metadata concept)", "CEM data reference set");
        ConceptChronicleBI CEMTypeRefset = createNewConcept(CEMRoot, "CEM type reference set (foundation metadata concept)", "CEM type reference set");
        ConceptChronicleBI CEMKeyRefset = createNewConcept(CEMRoot, "CEM key reference set (foundation metadata concept)", "CEM key reference set");
        ConceptChronicleBI CEMInfoRefset = createNewConcept(CEMRoot, "CEM info reference set (foundation metadata concept)", "CEM info reference set");
        ConceptChronicleBI CEMCompositionRefset = createNewConcept(CEMRoot, "CEM composition reference set (foundation metadata concept)", "CEM composition reference set");
        ConceptChronicleBI CEMConstraintsRefset = createNewConcept(CEMRoot, "CEM constraints reference set (foundation metadata concept)", "CEM constraints reference set");

        ConceptChronicleBI attributesRoot = ds.getConcept(UUID.fromString("7e52203e-8a35-3121-b2e7-b783b34d97f2"));
        ConceptChronicleBI CEMAttributes = createNewConcept(attributesRoot, "CEM attributes (foundation metadata concept)", "CEM attributes");

        ConceptChronicleBI CEMDataTypes = createNewConcept(CEMAttributes, "CEM data types (foundation metadata concept)", "CEM data types");
        ConceptChronicleBI CEMCDType = createNewConcept(CEMDataTypes, "CEM CD data type (foundation metadata concept)", "CEM CD data type");
        ConceptChronicleBI CEMCOType = createNewConcept(CEMDataTypes, "CEM CO data type (foundation metadata concept)", "CEM CO data type");
        ConceptChronicleBI CEMINTType = createNewConcept(CEMDataTypes, "CEM INT data type (foundation metadata concept)", "CEM INT data type");
        ConceptChronicleBI CEMREALType = createNewConcept(CEMDataTypes, "CEM REAL data type (foundation metadata concept)", "CEM REAL data type");
        ConceptChronicleBI CEMPQType = createNewConcept(CEMDataTypes, "CEM PQ data type (foundation metadata concept)", "CEM PQ data type");
        ConceptChronicleBI CEMIVLPQType = createNewConcept(CEMDataTypes, "CEM IVLPQ data type (foundation metadata concept)", "CEM IVLPQ data type");
        ConceptChronicleBI CEMRTOType = createNewConcept(CEMDataTypes, "CEM RTO data type (foundation metadata concept)", "CEM RTO data type");
        ConceptChronicleBI CEMIIType = createNewConcept(CEMDataTypes, "CEM II data type (foundation metadata concept)", "CEM II data type");
        ConceptChronicleBI CEMSTType = createNewConcept(CEMDataTypes, "CEM ST data type (foundation metadata concept)", "CEM ST data type");
        ConceptChronicleBI CEMTSType = createNewConcept(CEMDataTypes, "CEM TS data type (foundation metadata concept)", "CEM TS data type");
        ConceptChronicleBI CEMITType = createNewConcept(CEMDataTypes, "CEM IT data type (foundation metadata concept)", "CEM ID data type");

        ConceptChronicleBI CEMComponentTypes = createNewConcept(CEMAttributes, "CEM component types (foundation metadata concept)", "CEM component types");
        ConceptChronicleBI CEMItem = createNewConcept(CEMComponentTypes, "CEM item (foundation metadata concept)", "CEM item");
        ConceptChronicleBI CEMQual = createNewConcept(CEMComponentTypes, "CEM qualifier (foundation metadata concept)", "CEM quaifier");
        ConceptChronicleBI CEMMod = createNewConcept(CEMComponentTypes, "CEM modifier (foundation metadata concept)", "CEM modifier");
        ConceptChronicleBI CEMAttr = createNewConcept(CEMComponentTypes, "CEM attribution (foundation metadata concept)", "CEM attribution");
        ConceptChronicleBI CEMLink = createNewConcept(CEMComponentTypes, "CEM link (foundation metadata concept)", "CEM link");

        ConceptChronicleBI CEMConstraintTypes = createNewConcept(CEMAttributes, "CEM constraint types (foundation metadata concept)", "CEM constraint types");
        ConceptChronicleBI CEMCardinality = createNewConcept(CEMConstraintTypes, "CEM cardinality constraint (foundation metadata concept)", "CEM cardinality constraint");
        ConceptChronicleBI CEMDomain = createNewConcept(CEMConstraintTypes, "CEM domain constraint (foundation metadata concept)", "CEM domain constraint");
        ConceptChronicleBI CEMNormal = createNewConcept(CEMConstraintTypes, "CEM normal constraint (foundation metadata concept)", "CEM normal constraint");

        ConceptChronicleBI CEMCardinalityValue = createNewConcept(CEMAttributes, "CEM cardinality values (foundation metadata concept)", "CEM cardinality values");
        ConceptChronicleBI CEMCardinality01 = createNewConcept(CEMCardinalityValue, "CEM cardinality 0-1 (foundation metadata concept)", "CEM cardinality 0-1");
        ConceptChronicleBI CEMCardinality11 = createNewConcept(CEMCardinalityValue, "CEM cardinality 1-1 (foundation metadata concept)", "CEM cardinality 1-1");
        ConceptChronicleBI CEMCardinality0M = createNewConcept(CEMCardinalityValue, "CEM cardinality 0-M (foundation metadata concept)", "CEM cardinality 0-M");
        ConceptChronicleBI CEMCardinality1M = createNewConcept(CEMCardinalityValue, "CEM cardinality 1-M (foundation metadata concept)", "CEM cardinality 1-M");

        List<ConceptChronicleBI> parents = new ArrayList<ConceptChronicleBI>();

        parents.clear();
        parents.add(CEMCDType);
        parents.add(CEMCOType);
        ConceptChronicleBI CEMCodeField = createNewConcept(parents, "CEM code field (foundation metadata concept)", "CEM code field");

        parents.clear();
        parents.add(CEMCDType);
        parents.add(CEMCOType);
        parents.add(CEMRTOType);
        ConceptChronicleBI CEMCodingRationaleField = createNewConcept(parents, "CEM coding rationale field (foundation metadata concept)", "CEM coding rationale field");

        parents.clear();
        parents.add(CEMCDType);
        parents.add(CEMCOType);
        parents.add(CEMPQType);
        parents.add(CEMIVLPQType);
        parents.add(CEMRTOType);
        parents.add(CEMTSType);
        ConceptChronicleBI CEMOriginalTextField = createNewConcept(parents, "CEM original text field (foundation metadata concept)", "CEM original text field");

        parents.clear();
        parents.add(CEMCDType);
        parents.add(CEMCOType);
        parents.add(CEMPQType);
        parents.add(CEMIVLPQType);
        parents.add(CEMRTOType);
        parents.add(CEMSTType);
        ConceptChronicleBI CEMTranslationField = createNewConcept(parents, "CEM translation field (foundation metadata concept)", "CEM translation field");

        parents.clear();
        parents.add(CEMCOType);
        parents.add(CEMINTType);
        parents.add(CEMREALType);
        parents.add(CEMPQType);
        parents.add(CEMTSType);
        parents.add(CEMSTType);
        ConceptChronicleBI CEMValueField = createNewConcept(parents, "CEM value field (foundation metadata concept)", "CEM value field");

        parents.clear();
        parents.add(CEMINTType);
        parents.add(CEMREALType);
        parents.add(CEMPQType);
        parents.add(CEMTSType);
        ConceptChronicleBI CEMOperatorField = createNewConcept(parents, "CEM operator field (foundation metadata concept)", "CEM operator field");

        parents.clear();
        parents.add(CEMREALType);
        parents.add(CEMPQType);
        parents.add(CEMTSType);
        ConceptChronicleBI CEMStoragePrecisionField = createNewConcept(parents, "CEM storage precision field (foundation metadata concept)", "CEM storage precision field");

        parents.clear();
        parents.add(CEMPQType);
        parents.add(CEMIVLPQType);
        ConceptChronicleBI CEMUnitField = createNewConcept(parents, "CEM unit field (foundation metadata concept)", "CEM unit field");

        parents.clear();
        parents.add(CEMPQType);
        parents.add(CEMIVLPQType);
        ConceptChronicleBI CEMUnitOriginalTextField = createNewConcept(parents, "CEM unit original text field (foundation metadata concept)", "CEM unit original text field");

        ConceptChronicleBI CEMHighField = createNewConcept(CEMIVLPQType, "CEM high field (foundation metadata concept)", "CEM high field");
        ConceptChronicleBI CEMLowField = createNewConcept(CEMIVLPQType, "CEM low field (foundation metadata concept)", "CEM low field");
        ConceptChronicleBI CEMNumeratorField = createNewConcept(CEMRTOType, "CEM numerator field (foundation metadata concept)", "CEM numerator field");
        ConceptChronicleBI CEMDenominatorField = createNewConcept(CEMRTOType, "CEM denominator field (foundation metadata concept)", "CEM denominator field");
        ConceptChronicleBI CEMSubTypeField = createNewConcept(CEMRTOType, "CEM subtype field (foundation metadata concept)", "CEM subtype field");
        ConceptChronicleBI CEMCorrelationIdField = createNewConcept(CEMIIType, "CEM correlation id field (foundation metadata concept)", "CEM correlation id field");
        ConceptChronicleBI CEMExtensionField = createNewConcept(CEMIIType, "CEM extension field (foundation metadata concept)", "CEM extension field");
        ConceptChronicleBI CEMTypeField = createNewConcept(CEMIIType, "CEM type field (foundation metadata concept)", "CEM type field");
        ConceptChronicleBI CEMLanguageField = createNewConcept(CEMSTType, "CEM language field (foundation metadata concept)", "CEM language field");
        ConceptChronicleBI CEMTimeZoneField = createNewConcept(CEMTSType, "CEM time zone field (foundation metadata concept)", "CEM time zone field");

        for (ConceptChronicleBI loopUc : ds.getUncommittedConcepts()) {
            LOG.info("Uncommitted concept:" + loopUc.toString() + " - " + loopUc.getPrimordialUuid());
        }

        ds.commit();

        LOG.info("Metadata creation finished");

    }

    private static ConceptChronicleBI createNewConcept(ConceptChronicleBI parent, String fsn,
            String prefTerm) throws IOException, InvalidCAB, ContradictionException {
        List<ConceptChronicleBI> oneParent = new ArrayList<ConceptChronicleBI>();
        oneParent.add(parent);
        return createNewConcept(oneParent, fsn, prefTerm);
    }

    private static ConceptChronicleBI createNewConcept(List<ConceptChronicleBI> parents, String fsn,
            String prefTerm) throws IOException, InvalidCAB, ContradictionException {
        LanguageCode lc = LanguageCode.EN_US;
        UUID isA = Snomed.IS_A.getUuids()[0];
        IdDirective idDir = IdDirective.GENERATE_HASH;
        UUID module = Snomed.CORE_MODULE.getLenient().getPrimordialUuid();
        UUID[] parentsUuids = new UUID[parents.size()];
        int count = 0;
        for (ConceptChronicleBI parent : parents) {
            parentsUuids[count] = parent.getPrimordialUuid();
            count++;
        }
        ConceptCB newConCB = new ConceptCB(fsn, prefTerm, lc, isA, idDir, module, parentsUuids);

        ConceptChronicleBI newCon = getBuilder().construct(newConCB);
        ds.addUncommitted(newCon);

        return newCon;

    }

    private static EditCoordinate getEC() throws ValidationException, IOException {
        int authorNid = TermAux.USER.getLenient().getConceptNid();
        int module = Snomed.CORE_MODULE.getLenient().getNid();
        int editPathNid = ds.getNidForUuids(UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2")); // SNOMED CORE path

        return new EditCoordinate(authorNid, module, editPathNid);
    }

    private static ViewCoordinate getVC() throws IOException {
        return StandardViewCoordinates.getSnomedStatedLatest();
    }

    private static TerminologyBuilderBI getBuilder() throws IOException {
        if (CEMMetadataCreator.builder == null) {
            CEMMetadataCreator.builder = new BdbTermBuilder(getEC(), getVC());
        }
        return CEMMetadataCreator.builder;
    }

}
