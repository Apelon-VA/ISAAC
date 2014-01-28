/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.models.cem.importer;

import gov.va.isaac.gui.AppContext;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.blueprint.TerminologyBuilderBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.datastore.BdbTermBuilder;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author alo
 */
public class CEMImporter {

    private static final Logger LOG = LoggerFactory.getLogger(CEMImporter.class);

    private AppContext appContext;
    private BdbTerminologyStore ds;
    private TerminologyBuilderBI builder;

    public void ImportCEMModel(File file, AppContext appContext) throws IOException, InvalidCAB, ContradictionException {
        LOG.info("Preparing to import CEM model from: " + file.getName());
        this.appContext = appContext;
        ds = appContext.getDataStore();
        getBuilder();

        ConceptChronicleBI bloodPressureTaking = ds.getConcept(UUID.fromString("215fd598-e21d-3e27-a0a2-8e23b1b36dfc"));
        LOG.info("BloodPressureTaking concept:" + bloodPressureTaking.toString());

        RefexChronicleBI modelDataMember = addMemberInDataRefset(bloodPressureTaking, CEMMetadata.CEM_PQ.getNid());
        addStringAnnotation(bloodPressureTaking, CEMMetadata.CEM_TYPE_REFSET.getNid(), "DiastolicBloodPressureMeas");
        addStringAnnotation(bloodPressureTaking, CEMMetadata.CEM_KEY_REFSET.getNid(), "DiastolicBloodPressure_KEY_ECID");
        addStringAnnotation(bloodPressureTaking, CEMMetadata.CEM_IFO_REFSET.getNid(), "DiastolicBloodPressureMeas model is a measurement model. A measurement model holds a \"question\" or a \"test\" in the key and holds a numeric value (PQ) answer in data. For example, in the HeartRateMeas model, the \"question\" or the \"test\" is \"What is the heart rate measurement?\", and the \"answer\" in data can be \"100 bpm\".");

        RefexChronicleBI methodDevice = addComponent(bloodPressureTaking, CEMMetadata.CEM_QUAL.getNid(), "MethodDevice");
        addConstraint(methodDevice, CEMMetadata.CEM_CARDINALITY_CONSTRAINT.getNid(), methodDevice.getNid(), "0-1");

        RefexChronicleBI bodyLocationPrecoord = addComponent(bloodPressureTaking, CEMMetadata.CEM_QUAL.getNid(), "BodyLocationPrecoord");
        addConstraint(bodyLocationPrecoord, CEMMetadata.CEM_CARDINALITY_CONSTRAINT.getNid(), bodyLocationPrecoord.getNid(), "0-1");

        RefexChronicleBI bodyPosition = addComponent(bloodPressureTaking, CEMMetadata.CEM_QUAL.getNid(), "BodyPosition");
        addConstraint(bodyPosition, CEMMetadata.CEM_CARDINALITY_CONSTRAINT.getNid(), bodyPosition.getNid(), "0-1");

        RefexChronicleBI abnormalFlag = addComponent(bloodPressureTaking, CEMMetadata.CEM_QUAL.getNid(), "AbnormalFlag");
        addConstraint(abnormalFlag, CEMMetadata.CEM_CARDINALITY_CONSTRAINT.getNid(), abnormalFlag.getNid(), "0-1");

        RefexChronicleBI deltaFlag = addComponent(bloodPressureTaking, CEMMetadata.CEM_QUAL.getNid(), "DeltaFlag");
        addConstraint(deltaFlag, CEMMetadata.CEM_CARDINALITY_CONSTRAINT.getNid(), deltaFlag.getNid(), "0-1");

        RefexChronicleBI referenceRangeNar = addComponent(bloodPressureTaking, CEMMetadata.CEM_QUAL.getNid(), "ReferenceRangeNar");
        addConstraint(referenceRangeNar, CEMMetadata.CEM_CARDINALITY_CONSTRAINT.getNid(), referenceRangeNar.getNid(), "0-1");

        RefexChronicleBI relativeTemporalContext = addComponent(bloodPressureTaking, CEMMetadata.CEM_QUAL.getNid(), "RelativeTemporalContext");
        addConstraint(relativeTemporalContext, CEMMetadata.CEM_CARDINALITY_CONSTRAINT.getNid(), relativeTemporalContext.getNid(), "0-M");

        RefexChronicleBI subject = addComponent(bloodPressureTaking, CEMMetadata.CEM_MOD.getNid(), "Subject");
        addConstraint(subject, CEMMetadata.CEM_CARDINALITY_CONSTRAINT.getNid(), subject.getNid(), "0-1");

        RefexChronicleBI observed = addComponent(bloodPressureTaking, CEMMetadata.CEM_ATTR.getNid(), "Observed");
        addConstraint(observed, CEMMetadata.CEM_CARDINALITY_CONSTRAINT.getNid(), observed.getNid(), "0-1");

        RefexChronicleBI reportedReceived = addComponent(bloodPressureTaking, CEMMetadata.CEM_ATTR.getNid(), "ReportedReceived");
        addConstraint(reportedReceived, CEMMetadata.CEM_CARDINALITY_CONSTRAINT.getNid(), reportedReceived.getNid(), "0-1");

        RefexChronicleBI verified = addComponent(bloodPressureTaking, CEMMetadata.CEM_ATTR.getNid(), "Verified");
        addConstraint(verified, CEMMetadata.CEM_CARDINALITY_CONSTRAINT.getNid(), verified.getNid(), "0-1");

        addConstraint(abnormalFlag, CEMMetadata.CEM_DOMAIN_CONSTRAINT.getNid(),
                CEMMetadata.CEM_CODE_FIELD.getNid(), "AbnormalFlagNumericNom_DOMAIN_ECID");
        addConstraint(deltaFlag, CEMMetadata.CEM_DOMAIN_CONSTRAINT.getNid(),
                CEMMetadata.CEM_CODE_FIELD.getNid(), "DeltaFlagNumericNom_DOMAIN_ECID");
        addConstraint(modelDataMember, CEMMetadata.CEM_DOMAIN_CONSTRAINT.getNid(),
                CEMMetadata.CEM_UNIT_FIELD.getNid(), "PressureUnits_DOMAIN_ECID");
        addConstraint(modelDataMember, CEMMetadata.CEM_NORMAL_CONSTRAINT.getNid(),
                CEMMetadata.CEM_UNIT_FIELD.getNid(), "MilliMetersOfMercury_ECID");
        addConstraint(methodDevice, CEMMetadata.CEM_DOMAIN_CONSTRAINT.getNid(),
                CEMMetadata.CEM_CODE_FIELD.getNid(), "BloodPressureMeasurementDevice_DOMAIN_ECID");

        // TODO: Links
        ds.addUncommitted(bloodPressureTaking);
        ds.commit();

        LOG.info("Long form after commit:" + bloodPressureTaking.toLongString());

    }

    public RefexChronicleBI addMemberInDataRefset(ConceptChronicleBI concept, int cid) throws IOException, InvalidCAB, ContradictionException {
        RefexCAB newRefexCab = new RefexCAB(RefexType.CID,
                concept.getPrimordialUuid(),
                CEMMetadata.CEM_DATA_REFSET.getUuids()[0],
                IdDirective.GENERATE_RANDOM,
                RefexDirective.EXCLUDE);

        newRefexCab.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, cid);

        RefexChronicleBI<?> newRefex = builder.constructIfNotCurrent(newRefexCab);

        LOG.info("newRefex UUID:" + newRefex.getPrimordialUuid());

        concept.addAnnotation(newRefex);

        return newRefex;
    }

    public RefexChronicleBI addStringAnnotation(ConceptChronicleBI concept, int refsetId, String value) throws IOException, InvalidCAB, ContradictionException {
        RefexCAB newRefexCab = new RefexCAB(RefexType.STR,
                concept.getPrimordialUuid(),
                ds.getUuidPrimordialForNid(refsetId),
                IdDirective.GENERATE_RANDOM,
                RefexDirective.EXCLUDE);

        newRefexCab.put(ComponentProperty.STRING_EXTENSION_1, value);

        RefexChronicleBI<?> newRefex = builder.constructIfNotCurrent(newRefexCab);

        LOG.info("newRefex string UUID:" + newRefex.getPrimordialUuid());

        concept.addAnnotation(newRefex);

        return newRefex;
    }

    public RefexChronicleBI addComponent(ConceptChronicleBI concept, int type, String componentKey) throws IOException, InvalidCAB, ContradictionException {
        RefexCAB newRefexCab = new RefexCAB(RefexType.CID_STR,
                concept.getPrimordialUuid(),
                CEMMetadata.CEM_COMPOSITION_REFSET.getUuids()[0],
                IdDirective.GENERATE_RANDOM,
                RefexDirective.EXCLUDE);

        newRefexCab.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, type);
        newRefexCab.put(ComponentProperty.STRING_EXTENSION_1, componentKey);

        RefexChronicleBI<?> newRefex = builder.constructIfNotCurrent(newRefexCab);

        LOG.info("newRefex component UUID:" + newRefex.getPrimordialUuid());

        concept.addAnnotation(newRefex);

        return newRefex;
    }

    public RefexChronicleBI addConstraint(RefexChronicleBI cemMember, int constraint, int focusField, String value) throws IOException, InvalidCAB, ContradictionException {
        RefexCAB newRefexCab = new RefexCAB(RefexType.CID_CID_STR,
                cemMember.getPrimordialUuid(),
                CEMMetadata.CEM_CONSTRAINTS_REFSET.getUuids()[0],
                IdDirective.GENERATE_RANDOM,
                RefexDirective.EXCLUDE);

        newRefexCab.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, constraint);
        newRefexCab.put(ComponentProperty.COMPONENT_EXTENSION_2_ID, focusField);
        newRefexCab.put(ComponentProperty.STRING_EXTENSION_1, value);

        RefexChronicleBI<?> newRefex = builder.constructIfNotCurrent(newRefexCab);

        LOG.info("newRefex contraint UUID:" + newRefex.getPrimordialUuid());

        cemMember.addAnnotation(newRefex);

        return newRefex;
    }

    private EditCoordinate getEC() throws ValidationException, IOException {
        int authorNid = TermAux.USER.getLenient().getConceptNid();
        int module = Snomed.CORE_MODULE.getLenient().getNid();
        int editPathNid = ds.getNidForUuids(UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2")); // SNOMED CORE path

        return new EditCoordinate(authorNid, module, editPathNid);
    }

    private ViewCoordinate getVC() throws IOException {
        return StandardViewCoordinates.getSnomedStatedLatest();
    }

    private TerminologyBuilderBI getBuilder() throws IOException {
        if (builder == null) {
            builder = new BdbTermBuilder(getEC(), getVC());
        }
        return builder;
    }

}
