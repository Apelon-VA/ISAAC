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
package gov.va.isaac.ie;

import gov.va.isaac.AppContext;
import gov.va.isaac.models.cem.importer.CEMMetadataBinding;
import gov.va.isaac.models.util.ImporterBase;
import gov.va.isaac.util.WBUtility;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * Utility class for manually testing ISAAC refex amendment functionality.
 *
 * @see https://jira.ihtsdotools.org/browse/OTFISSUE-22
 *
 * @author ocarlsen
 */
public class RefexAmender extends ImporterBase {

    private static final Logger LOG = LoggerFactory.getLogger(RefexAmender.class);

    private final ConceptVersionBI focusConcept;
    private final ConceptVersionBI qualRefsetConcept;
    private final ConceptVersionBI valueRefsetConcept;

    private RefexAmender(UUID focusConceptUuid) throws Exception {
        super();

        this.focusConcept = getDataStore().getConcept(focusConceptUuid).getVersion(getVC());
        LOG.info("focusConcept: " + focusConcept.toString());

        UUID qualRefsetConceptUuid = CEMMetadataBinding.CEM_COMPOSITION_REFSET.getUuids()[0];
        this.qualRefsetConcept = getDataStore().getConcept(qualRefsetConceptUuid).getVersion(getVC());
        LOG.info("qualRefsetConcept: " + qualRefsetConcept.toString());

        UUID valueRefsetConceptUuid = CEMMetadataBinding.CEM_VALUE_REFSET.getUuids()[0];
        this.valueRefsetConcept = getDataStore().getConcept(valueRefsetConceptUuid).getVersion(getVC());
        LOG.info("valueRefsetConcept: " + valueRefsetConcept.toString());

        // Make sure CEM metadata concepts have been created.
        final int cemTypeRefsetNid = CEMMetadataBinding.CEM_TYPE_REFSET.getNid();
        System.out.println("cemTypeRefsetNid="+cemTypeRefsetNid);
    }

    public void shutdown() {
        getDataStore().shutdown();
    }

    private RefexChronicleBI<?> addQualRefsetMember(ConceptSpec componentExtension, String stringExtension)
            throws Exception {

        RefexCAB newRefexCab = new RefexCAB(RefexType.CID_STR,
                focusConcept.getNid(),
                qualRefsetConcept.getNid(),
                IdDirective.GENERATE_REFEX_CONTENT_HASH,
                RefexDirective.INCLUDE);

        newRefexCab.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, componentExtension.getNid());
        newRefexCab.put(ComponentProperty.STRING_EXTENSION_1, stringExtension);

        RefexChronicleBI<?> newMemChron = getBuilder().construct(newRefexCab);
        WBUtility.addUncommitted(focusConcept);

        return newMemChron;
    }

    private RefexChronicleBI<?> addValueRefsetMember(RefexChronicleBI<?> qualRefex, String stringExtension)
            throws IOException, InvalidCAB, ContradictionException {

        RefexCAB newMember = new RefexCAB(RefexType.STR,
                qualRefex.getNid(),
                valueRefsetConcept.getNid(),
                IdDirective.GENERATE_RANDOM,
                RefexDirective.EXCLUDE);

        newMember.put(ComponentProperty.STRING_EXTENSION_1, stringExtension);

        RefexChronicleBI<?> newMemChron = WBUtility.getBuilder().construct(newMember);

        qualRefex.addAnnotation(newMemChron);
        WBUtility.addUncommitted(focusConcept);

        return newMemChron;
    }

    private RefexChronicleBI<?> attemptRefexAmendment(RefexChronicleBI<?> refexChron, String newStringExtension)
            throws ContradictionException, InvalidCAB, IOException {
        ViewCoordinate viewCoordinate = WBUtility.getViewCoordinate();
        RefexVersionBI<?> refex = refexChron.getVersion(viewCoordinate);
        RefexCAB bp = refex.makeBlueprint(viewCoordinate, IdDirective.PRESERVE, RefexDirective.INCLUDE);

        bp.put(ComponentProperty.STRING_EXTENSION_1, newStringExtension);

        RefexChronicleBI<?> newMemChron = WBUtility.getBuilder().constructIfNotCurrent(bp);
        WBUtility.addUncommitted(focusConcept);

        return newMemChron;
    }

    public static void main(String[] args) throws Exception {

        // Set up like ISAAC App.
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        AppContext.setup();
        // TODO OTF fix: this needs to be fixed so I don't have to hack it with reflection....(https://jira.ihtsdotools.org/browse/OTFISSUE-11)
        Field f = Hk2Looker.class.getDeclaredField("looker");
        f.setAccessible(true);
        f.set(null, AppContext.getServiceLocator());
        System.setProperty(BdbTerminologyStore.BDB_LOCATION_PROPERTY,
                new File("../isaac-app/berkeley-db").getAbsolutePath());

        // "Blood pressure taking (procedure)"
        UUID focusConceptUuid = UUID.fromString("215fd598-e21d-3e27-a0a2-8e23b1b36dfc");
        RefexAmender tester = new RefexAmender(focusConceptUuid);

        // Initial state.
        RefexChronicleBI<?> qualRefex = tester.addQualRefsetMember(CEMMetadataBinding.CEM_QUAL, "Observation");
        System.out.println(qualRefex);
        RefexChronicleBI<?> valueRefex = tester.addValueRefsetMember(qualRefex, "10");
        System.out.println(valueRefex);

        // Now attempt to amend valueRefex.
        RefexChronicleBI<?> amendedRefex = tester.attemptRefexAmendment(valueRefex, "11");
        System.out.println(amendedRefex);

        tester.shutdown();
    }
}
