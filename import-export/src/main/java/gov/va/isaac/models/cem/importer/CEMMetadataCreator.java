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
package gov.va.isaac.models.cem.importer;

import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.models.util.MetadataCreator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to create CEM metadata concepts.
 *
 * @author alo
 * @author ocarlsen
 */
public class CEMMetadataCreator extends MetadataCreator {

    private static final Logger LOG = LoggerFactory.getLogger(CEMMetadataCreator.class);

    public CEMMetadataCreator() throws ValidationException, IOException {
        super();
    }

    @Override
    @SuppressWarnings("unused")
    public void createMetadata() throws Exception {
        LOG.info("Preparing to create metadata");

        // Make sure NOT in application thread.
        FxUtils.checkBackgroundThread();

        ConceptChronicleBI refsetsRoot = getDataStore().getConcept(UUID.fromString(REFSET_CONCEPT));
        LOG.info("Refsets root:" + refsetsRoot.toString());

        ConceptChronicleBI CEMRefset = createNewConcept(refsetsRoot, "CEM reference sets (foundation metadata concept)", "CEM reference sets");

        ConceptChronicleBI CEMDataRefset = createNewConcept(CEMRefset, "CEM data reference set (foundation metadata concept)", "CEM data reference set");
        ConceptChronicleBI CEMTypeRefset = createNewConcept(CEMRefset, "CEM type reference set (foundation metadata concept)", "CEM type reference set");
        ConceptChronicleBI CEMKeyRefset = createNewConcept(CEMRefset, "CEM key reference set (foundation metadata concept)", "CEM key reference set");
        ConceptChronicleBI CEMInfoRefset = createNewConcept(CEMRefset, "CEM info reference set (foundation metadata concept)", "CEM info reference set");
        ConceptChronicleBI CEMCompositionRefset = createNewConcept(CEMRefset, "CEM composition reference set (foundation metadata concept)", "CEM composition reference set");
        ConceptChronicleBI CEMConstraintsRefset = createNewConcept(CEMRefset, "CEM constraints reference set (foundation metadata concept)", "CEM constraints reference set");
        ConceptChronicleBI CEMConstraintPath = createNewConcept(CEMRefset, "CEM constraints path reference set (foundation metadata concept)", "CEM constraint path");
        ConceptChronicleBI CEMConstraintValue = createNewConcept(CEMRefset, "CEM constraints value reference set (foundation metadata concept)", "CEM constraint value");
        ConceptChronicleBI CEMValue = createNewConcept(CEMRefset, "CEM value reference set (foundation metadata concept)", "CEM constraint path");

        ConceptChronicleBI attributesRoot = getDataStore().getConcept(UUID.fromString(REFSET_ATTRIBUTE_CONCEPT));
        LOG.info("Attributes root:" + attributesRoot.toString());

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

        for (ConceptChronicleBI loopUc : getDataStore().getUncommittedConcepts()) {
            LOG.debug("Uncommitted concept:" + loopUc.toString() + " - " + loopUc.getPrimordialUuid());
        }

        getDataStore().commit();

        LOG.info("Metadata creation finished");
    }
}
