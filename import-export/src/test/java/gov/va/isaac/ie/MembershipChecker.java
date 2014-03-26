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
import gov.va.isaac.models.fhim.importer.FHIMMetadataBinding;
import gov.va.isaac.models.util.ExporterBase;
import gov.va.isaac.util.WBUtility;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;
import org.ihtsdo.otf.tcc.model.cc.NidPairForRefex;
import org.ihtsdo.otf.tcc.model.cc.refex.type_string.StringMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.google.common.base.Preconditions;

/**
 * Utility class for manually checking imported information model refset membership in OTF.
 *
 * @author ocarlsen
 */
public class MembershipChecker extends ExporterBase {

    private static final Logger LOG = LoggerFactory.getLogger(MembershipChecker.class);

    private final ConceptVersionBI refsetConcept;

    private MembershipChecker(ConceptSpec refsetSpec) throws Exception {
        super();

        // Make sure refset concept exists.
        UUID refsetConceptUUID = refsetSpec.getUuids()[0];
        this.refsetConcept = WBUtility.getConceptVersion(refsetConceptUUID);
        LOG.warn("refsetConcept: " + refsetConcept.toString());
        LOG.warn("annotationStyleRefex="+refsetConcept.isAnnotationStyleRefex());

        // Sanity check.
        Preconditions.checkState(refsetConcept.getNid() == refsetSpec.getNid());
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    private <T> List<T> getAnnotations(UUID focusComponentUUID,
            ConceptSpec refsetSpec, Class<T> type)
            throws ContradictionException, IOException {
        ConceptVersionBI focusConcept = getDataStore().getConcept(focusComponentUUID).getVersion(getVC());
        LOG.info("focusConcept: " + focusConcept.toString());

        return filterAnnotations(focusConcept.getAnnotations(), refsetSpec, type);
    }

    private Collection<? extends RefexChronicleBI<?>> getRefexes()
            throws IOException {
        return refsetConcept.getRefexes();
    }

    private Collection<? extends RefexChronicleBI<?>> getRefexMembers()
            throws IOException {
        int refsetNid = refsetConcept.getNid();
        return refsetConcept.getRefexMembers(refsetNid);
    }

    private Collection<? extends RefexChronicleBI<?>> getRefsetMembers()
            throws IOException {
        return refsetConcept.getRefsetMembers();
    }

    private Collection<? extends RefexChronicleBI<?>> getCurrentRefexMembers()
            throws IOException {
        int refsetNid = refsetConcept.getNid();
        return refsetConcept.getCurrentRefexMembers(refsetNid);
    }

    private Collection<? extends RefexChronicleBI<?>> getCurrentRefsetMembers()
            throws IOException {
        return refsetConcept.getCurrentRefsetMembers(getVC());
    }

    private List<NidPairForRefex> getRefexPairs() {
        int refsetNid = refsetConcept.getNid();
        return getDataStore().getRefexPairs(refsetNid);
    }

    public void shutdown() {
        getDataStore().shutdown();
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

        // FHIM Models RS.
        MembershipChecker tester = new MembershipChecker(FHIMMetadataBinding.FHIM_MODELS_REFSET);

        // getAnnotations API.
        UUID bpUUID = UUID.fromString("215fd598-e21d-3e27-a0a2-8e23b1b36dfc");  // "Blood pressure taking (procedure)"
        List<StringMember> annotations =
                tester.getAnnotations(bpUUID, FHIMMetadataBinding.FHIM_MODELS_REFSET, StringMember.class);
        int annotationCount = annotations.size();
        LOG.warn("annotationCount="+annotationCount);
        for (StringMember annotation : annotations) {
            System.out.println("annotation="+annotation);
        }

        // getRefexes API.
        Collection<? extends RefexChronicleBI<?>> refexes = tester.getRefexes();
        int refexCount = refexes.size();
        LOG.warn("refexCount="+refexCount);
        for (RefexChronicleBI<?> refex : refexes) {
            System.out.println("refex="+refex);
        }

        // getRefexMembers API.
        Collection<? extends RefexChronicleBI<?>> refexMembers = tester.getRefexMembers();
        int refexMemberCount = refexMembers.size();
        LOG.warn("refexMemberCount="+refexMemberCount);
        for (RefexChronicleBI<?> refexMember : refexMembers) {
            System.out.println("refexMember="+refexMember);
        }

        // getRefsetMembers API.
        Collection<? extends RefexChronicleBI<?>> refsetMembers = tester.getRefsetMembers();
        int refsetMemberCount = refsetMembers.size();
        LOG.warn("refsetMemberCount="+refsetMemberCount);
        for (RefexChronicleBI<?> refsetMember : refsetMembers) {
            System.out.println("refsetMember="+refsetMember);
        }

        // getCurrentRefexMembers API.
        Collection<? extends RefexChronicleBI<?>> currentRefexMembers = tester.getCurrentRefexMembers();
        int currentRefexMemberCount = currentRefexMembers.size();
        LOG.warn("currentRefexMemberCount="+currentRefexMemberCount);
        for (RefexChronicleBI<?> currentRefexMember : currentRefexMembers) {
            System.out.println("currentRefexMember="+currentRefexMember);
        }

        // getCurrentRefsetMembers API.
        Collection<? extends RefexChronicleBI<?>> currentRefsetMembers = tester.getCurrentRefsetMembers();
        int currentRefsetMemberCount = currentRefsetMembers.size();
        LOG.warn("currentRefsetMemberCount="+currentRefsetMemberCount);
        for (RefexChronicleBI<?> currentRefsetMember : currentRefsetMembers) {
            System.out.println("currentRefsetMember="+currentRefsetMember);
        }

        // getRefexPairs API.
        List<NidPairForRefex> refexPairs = tester.getRefexPairs();
        int refexPairCount = refexPairs.size();
        LOG.warn("refexPairCount="+refexPairCount);
        for (NidPairForRefex refexPair : refexPairs) {
            System.out.println("refexPair="+refexPair);
        }

        tester.shutdown();
    }
}
