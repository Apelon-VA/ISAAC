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
package gov.va.isaac.export;

import gov.va.isaac.AppContext;
import gov.va.models.cem.importer.CEMMetadataBinding;
import gov.va.models.util.ImporterBase;

import java.io.File;
import java.lang.reflect.Field;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * Utility class for manually testing ISAAC import functionality.
 *
 * @see https://jira.ihtsdotools.org/browse/OTFISSUE-3
 * @see https://jira.ihtsdotools.org/browse/OTFISSUE-10
 *
 * @author ocarlsen
 */
public class ImportTester extends ImporterBase {

    private static final Logger LOG = LoggerFactory.getLogger(ImportTester.class);

    private final ConceptVersionBI focusConcept;
    private final ConceptVersionBI refsetConcept;

    private ImportTester(UUID focusConceptUuid, UUID refsetConceptUuid) throws Exception {
        super();

        this.focusConcept = getDataStore().getConcept(focusConceptUuid).getVersion(getVC());
        LOG.info("focusConcept: " + focusConcept.toString());

        this.refsetConcept = getDataStore().getConcept(refsetConceptUuid).getVersion(getVC());
        LOG.info("refsetConcept: " + refsetConcept.toString());

        // Make sure CEM metadata concepts have been created.
        final int cemTypeRefsetNid = CEMMetadataBinding.CEM_TYPE_REFSET.getNid();
        System.out.println("cemTypeRefsetNid="+cemTypeRefsetNid);
    }

    public void shutdown() {
        getDataStore().shutdown();
    }

    private RefexChronicleBI<?> testAddExtensionMember(ConceptSpec componentExtension, String stringExtension) throws Exception {
        return addExtensionMember(focusConcept, refsetConcept,
                componentExtension, stringExtension);
    }

    private RefexChronicleBI<?> addExtensionMember(ConceptVersionBI refComp, ConceptVersionBI refsetConcept,
            ConceptSpec componentExtension, String stringExtension) throws Exception {
        RefexCAB newRefexCab = new RefexCAB(RefexType.CID_STR,
                refComp.getNid(),
                refsetConcept.getNid(),
                IdDirective.GENERATE_REFEX_CONTENT_HASH,
                RefexDirective.INCLUDE);

        System.out.println();
        System.out.println("componentExtension="+componentExtension);
        System.out.println("stringExtension="+stringExtension);

        newRefexCab.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, componentExtension.getNid());
        newRefexCab.put(ComponentProperty.STRING_EXTENSION_1, stringExtension);

        System.out.println("newRefexCab UUID: "+newRefexCab.getComponentUuid());
        newRefexCab.recomputeUuid();
        System.out.println("newRefexCab UUID: "+newRefexCab.getComponentUuid());

        RefexChronicleBI<?> newMemChron = getBuilder().construct(newRefexCab);
        System.out.println("newMemChron UUID: " + newMemChron.getPrimordialUuid());
        System.out.println();

        return newMemChron;
    }

    public static void main(String[] args) throws Exception {

        // Set up like ISAAC App.
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        AppContext.setup();
        // TODO OTF fix: this needs to be fixed so I don't have to hack it with reflection....
        Field f = Hk2Looker.class.getDeclaredField("looker");
        f.setAccessible(true);
        f.set(null, AppContext.getServiceLocator());
        System.setProperty(BdbTerminologyStore.BDB_LOCATION_PROPERTY,
                new File("../isaac-app/berkeley-db").getAbsolutePath());

        // "Blood pressure taking (procedure)"
        UUID focusConceptUuid = UUID.fromString("215fd598-e21d-3e27-a0a2-8e23b1b36dfc");
        ImportTester tester = new ImportTester(focusConceptUuid, CEMMetadataBinding.CEM_COMPOSITION_REFSET.getUuids()[0]);

        RefexChronicleBI<?> refex1 = tester.testAddExtensionMember(CEMMetadataBinding.CEM_QUAL, "MethodDevice");
        RefexChronicleBI<?> refex2 = tester.testAddExtensionMember(CEMMetadataBinding.CEM_QUAL, "BodyLocationPrecoord");
        System.out.println("uuid1="+refex1.getPrimordialUuid());
        System.out.println("uuid2="+refex2.getPrimordialUuid());

        tester.shutdown();
    }
}
