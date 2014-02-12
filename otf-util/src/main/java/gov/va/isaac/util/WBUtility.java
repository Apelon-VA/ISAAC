package gov.va.isaac.util;

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.interfaces.utility.UserPreferencesI;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.uuid.UuidFactory;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.description.DescriptionChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.description.DescriptionVersionDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.RefexChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp.RefexCompVersionDdo;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid.NidMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for accessing Workbench APIs.
 *
 * @author ocarlsen
 */
public class WBUtility {

    private static final Logger LOG = LoggerFactory.getLogger(WBUtility.class);

    private static final UUID ID_UUID = TermAux.SNOMED_IDENTIFIER.getUuids()[0]; //SNOMED integer id
    private static final UUID FSN_UUID = SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getUuids()[0];
    private static final UUID PREFERRED_UUID = SnomedMetadataRf2.PREFERRED_RF2.getUuids()[0];
    private static final UUID SYNONYM_UUID = SnomedMetadataRf2.SYNONYM_RF2.getUuids()[0];

    private static Integer fsnTypeNid = null;
    private static Integer preferredNid = null;
    private static Integer synonymNid = null;

    private static BdbTerminologyStore dataStore = ExtendedAppContext.getDataStore();
    private static UserPreferencesI userPrefs = ExtendedAppContext.getService(UserPreferencesI.class);
    private static String useFSN = "useFSN";

    public static String getDescription(UUID uuid) {
        try {
            ConceptVersionBI conceptVersion = dataStore.getConceptVersion(StandardViewCoordinates.getSnomedInferredThenStatedLatest(), uuid);
            return getDescription(conceptVersion);
        } catch (Exception ex) {
            LOG.warn("Unexpected error looking up description", ex);
            return null;
        }
    }

    /**
     * Note, this method isn't smart enough to work with multiple versions properly....
     * assumes you only pass in a concept with current values.
     */
    public static String getDescription(ConceptVersionBI concept) {
        String fsn = null;
        String preferred = null;
        String bestFound = null;
        try {
            if (concept.getDescriptions() != null) {
                for (DescriptionChronicleBI desc : concept.getDescriptions()) {
                    int versionCount = desc.getVersions().size();
                    DescriptionVersionBI<?> descVer = desc.getVersions()
                            .toArray(new DescriptionVersionBI[versionCount])[versionCount - 1];

                    if (descVer.getTypeNid() == getFSNTypeNid()) {
                        if (descVer.getStatus() == Status.ACTIVE) {
                            if (userPrefs.getBoolean(useFSN, true)) {
                                return descVer.getText();
                            } else {
                                fsn = descVer.getText();
                            }

                        } else {
                            bestFound = descVer.getText();
                        }
                    } else if (descVer.getTypeNid() == getSynonymTypeNid() && isPreferred(descVer.getAnnotations())) {
                        if (descVer.getStatus() == Status.ACTIVE) {
                            if (! userPrefs.getBoolean(useFSN, true)) {
                                return descVer.getText();
                            } else {
                                preferred = descVer.getText();
                            }
                        } else {
                            bestFound = descVer.getText();
                        }
                    }
                }
            }
        } catch (IOException e) {
            // noop
        }
        // If we get here, we didn't find what they were looking for. Pick
        // something....
        return (fsn != null ? fsn : (preferred != null ? preferred
                : (bestFound != null ? bestFound : concept.toUserString())));
    }

    private static int getFSNTypeNid() {
        if (fsnTypeNid == null) {
            try {
                fsnTypeNid = dataStore.getNidForUuids(FSN_UUID);
            } catch (IOException ex) {
                LOG.error("Could not find nid for FSN UUID: " + FSN_UUID, ex);
                fsnTypeNid = -1;
            }
        }
        return fsnTypeNid;
    }

    private static int getSynonymTypeNid() {
        // Lazily load.
        if (synonymNid == null) {
            try {
                synonymNid = dataStore.getNidForUuids(SYNONYM_UUID);
            } catch (IOException ex) {
                LOG.error("Could not find nid for synonymNid UUID: " + SYNONYM_UUID, ex);
                synonymNid = -1;
            }
        }
        return synonymNid;
    }

    private static int getPreferredTypeNid() {
        // Lazily load.
        if (preferredNid == null) {
            try {
                preferredNid = dataStore.getNidForUuids(PREFERRED_UUID);
            } catch (IOException ex) {
                LOG.error("Could not find nid for Preferred UUID: " + PREFERRED_UUID, ex);
                preferredNid = -1;
            }
        }
        return preferredNid;
    }


    private static boolean isPreferred(Collection<? extends RefexChronicleBI<?>> collection) {
        for (RefexChronicleBI<?> rc : collection) {
            if (rc.getRefexType() == RefexType.CID) {
                int nid1 = ((NidMember) rc).getNid1();  // RefexType.CID means NidMember.
                if (nid1 == getPreferredTypeNid()) {
                    return true;
                }
            }
        }
        return false;
    }
    public static String getDescription(ConceptChronicleDdo concept) {
        // Go hunting for a FSN
        if (concept == null) {
            return null;
        }
        if (concept.getDescriptions() == null) {
            if (concept.getConceptReference() == null) {
                return null;
            }
            return concept.getConceptReference().getText();
        }

        String fsn = null;
        String preferred = null;
        String bestFound = null;
        for (DescriptionChronicleDdo d : concept.getDescriptions()) {
            DescriptionVersionDdo dv = d.getVersions().get(d.getVersions().size() - 1);
            if (dv.getTypeReference().getUuid().equals(FSN_UUID)) {
                if (dv.getStatus() == Status.ACTIVE) {
                    if (userPrefs.getBoolean(useFSN, true)) {
                        return dv.getText();
                    } else {
                        fsn = dv.getText();
                    }
                } else {
                    bestFound = dv.getText();
                }
            } else if (dv.getTypeReference().getUuid().equals(SYNONYM_UUID)) {
                if ((dv.getStatus() == Status.ACTIVE) && isPreferred(dv.getAnnotations())) {
                    if (! userPrefs.getBoolean(useFSN, true)) {
                        return dv.getText();
                    } else {
                        preferred = dv.getText();
                    }
                } else {
                    bestFound = dv.getText();
                }
            }
        }
        // If we get here, we didn't find what they were looking for. Pick
        // something....
        return (fsn != null ? fsn : (preferred != null ? preferred
                : (bestFound != null ? bestFound : concept
                        .getConceptReference().getText())));
    }

    private static boolean isPreferred(List<RefexChronicleDdo<?, ?>> annotations) {
        for (RefexChronicleDdo<?, ?> frc : annotations) {
            for (Object version : frc.getVersions()) {
                if (version instanceof RefexCompVersionDdo) {
                    UUID uuid = ((RefexCompVersionDdo<?, ?>) version).getComp1Ref().getUuid();
                    return uuid.equals(PREFERRED_UUID);
                }
            }
        }
        return false;
    }

    /**
     * Creates a {@link UUID} from the {@code identifier} parameter and
     * calls {@link #lookupSnomedIdentifierAsCV(UUID)}.
     */
    public static ConceptVersionBI lookupSnomedIdentifierAsCV(String identifier) {
        LOG.debug("WB DB String Lookup '" + identifier + "'");

        // Null or empty check.
        if ((identifier == null) || (identifier.trim().length() == 0)) {
            return null;
        }

        UUID uuid = UUID.fromString(identifier.trim());
        return lookupSnomedIdentifierAsCV(uuid);
    }

    /**
     * Looks up the identifier (sctid or UUID).
     */
    public static ConceptVersionBI lookupSnomedIdentifierAsCV(UUID conceptUUID) {
        LOG.debug("WB DB UUID Lookup '" + conceptUUID + "'");

        // Null check.
        if (conceptUUID == null) {
            return null;
        }

        ConceptVersionBI result = getConceptVersion(conceptUUID);
        if (result == null) {

            // Try looking up by ID.
            // dataStore#getConceptVersionFromAlternateId seems broke after
            // the DB update, make the UUID myself instead.
            UUID alternateUUID = UuidFactory.getUuidFromAlternateId(ID_UUID, conceptUUID.toString().trim());

            // Try again.
            result = getConceptVersion(alternateUUID);
        }
        return result;
    }

    private static ConceptVersionBI getConceptVersion(UUID uuid) {
        try {
            ConceptVersionBI result = dataStore.getConceptVersion(
                    StandardViewCoordinates.getSnomedInferredThenStatedLatest(), uuid);

            // This is garbage that the WB API invented. Nothing like an
            // undocumented getter which, rather than returning null when
            // the thing
            // you are asking for doesn't exist - it goes off and returns
            // essentially a new, empty, useless node. Sigh.
            if (result.getUUIDs().size() == 0) {
                return null;
            }

            return result;
        } catch (IOException ex) {
            LOG.warn("Trouble getting concept: " + uuid, ex);
        }
        return null;
    }
}
