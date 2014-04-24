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
package gov.va.isaac.util;

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.interfaces.utility.UserPreferencesI;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.ihtsdo.otf.tcc.api.blueprint.ConceptCB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.TerminologyBuilderBI;
import org.ihtsdo.otf.tcc.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.otf.tcc.api.changeset.ChangeSetGeneratorBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.lang.LanguageCode;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.uuid.UuidFactory;
import org.ihtsdo.otf.tcc.datastore.BdbTermBuilder;
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
 * 
 * {@link WBUtility}
 * 
 * Utility for accessing Workbench APIs.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 * @author ocarlsen
 * @author jefron
 */
public class WBUtility {

	private static final Logger LOG = LoggerFactory.getLogger(WBUtility.class);

	private static final UUID FSN_UUID = SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getUuids()[0];
	private static final UUID PREFERRED_UUID = SnomedMetadataRf2.PREFERRED_RF2.getUuids()[0];
	private static final UUID SYNONYM_UUID = SnomedMetadataRf2.SYNONYM_RF2.getUuids()[0];

	private static Integer fsnTypeNid = null;
	private static Integer preferredNid = null;
	private static Integer synonymNid = null;

	private static BdbTerminologyStore dataStore = ExtendedAppContext.getDataStore();
	private static TerminologyBuilderBI dataBuilder;
	private static UserPreferencesI userPrefs = ExtendedAppContext.getService(UserPreferencesI.class);
	private static String useFSN = "useFSN";

	private static EditCoordinate editCoord = null;
	private static ViewCoordinate vc = null;
	private static boolean changeSetCreated;

	public static TerminologyBuilderBI getBuilder() {
		if (dataBuilder == null) {
			dataBuilder = new BdbTermBuilder(getEC(), getViewCoordinate());
		}
		
		return dataBuilder;
	}
	
	public static EditCoordinate getEC()  {
		if (editCoord == null) {
			try {
				int authorNid   = TermAux.USER.getLenient().getConceptNid();
				int module = Snomed.CORE_MODULE.getLenient().getNid();
				int editPathNid = TermAux.SNOMED_CORE.getLenient().getConceptNid();
				
				editCoord =  new EditCoordinate(authorNid, module, editPathNid);
			} catch (IOException e) {
				LOG.error("error configuring edit coordinate", e);
			}
		}

		return editCoord;
	}

	public static String getDescription(UUID uuid) {
		try {
			ConceptVersionBI conceptVersion = dataStore.getConceptVersion(getViewCoordinate(), uuid);
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
	public static String getDescription(ConceptChronicleBI concept) {
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
	 * If the passed in value is a {@link UUID}, calls {@link #getConceptVersion(UUID)}
	 * Next, if no hit, if the passed in value is parseable as a long, treats it as an SCTID and converts that to UUID and 
	 * then calls {@link #getConceptVersion(UUID)}
	 * Next, if no hit, if the passed in value is parseable as a int, calls {@link #getConceptVersion(int)}
	 */
	public static ConceptVersionBI lookupIdentifier(String identifier)
	{
		LOG.debug("WB DB String Lookup '{}'", identifier);

		if (StringUtils.isBlank(identifier))
		{
			return null;
		}
		String localIdentifier = identifier.trim();

		UUID uuid = Utility.getUUID(localIdentifier);
		if (uuid != null)
		{
			return getConceptVersion(uuid);
		}
		
		if (Utility.isLong(localIdentifier))
		{
			UUID alternateUUID = UuidFactory.getUuidFromAlternateId(TermAux.SNOMED_IDENTIFIER.getUuids()[0], localIdentifier);
			LOG.debug("WB DB String Lookup as SCTID converted to UUID {}", alternateUUID);
			ConceptVersionBI cv = getConceptVersion(alternateUUID);
			if (cv != null)
			{
				return cv;
			}
		}
		
		Integer i = Utility.getInt(localIdentifier);
		if (i != null)
		{
			return getConceptVersion(i);
		}
		return null;
	}
	
	/**
	 * If the passed in value is a {@link UUID}, calls {@link #getConceptVersion(UUID)}
	 * Next, if no hit, if the passed in value is parseable as a long, treats it as an SCTID and converts that to UUID and 
	 * then calls {@link #getConceptVersion(UUID)}
	 * Next, if no hit, if the passed in value is parseable as a int, calls {@link #getConceptVersion(int)}
	 * 
	 * All done in a background thread, method returns immediately
	 * 
	 * @param identifier - what to search for
	 * @param callback - who to inform when lookup completes
	 * @param callId - An arbitrary identifier that will be returned to the caller when this completes
	 */
	public static void lookupIdentifier(final String identifier, final ConceptLookupCallback callback, final Integer callId)
	{
		LOG.debug("Threaded Lookup: '{}'", identifier);
		final long submitTime = System.currentTimeMillis();
		Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				ConceptVersionBI c = lookupIdentifier(identifier);
				callback.lookupComplete(c, submitTime, callId);
			}
		};
		Utility.execute(r);
	}
	
	/**
	 * 
	 * All done in a background thread, method returns immediately
	 * 
	 * @param identifier - The NID to search for
	 * @param callback - who to inform when lookup completes
	 * @param callId - An arbitrary identifier that will be returned to the caller when this completes
	 */
	public static void getConceptVersion(final int nid, final ConceptLookupCallback callback, final Integer callId)
	{
		LOG.debug("Threaded Lookup: '{}'", nid);
		final long submitTime = System.currentTimeMillis();
		Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				ConceptVersionBI c = getConceptVersion(nid);
				callback.lookupComplete(c, submitTime, callId);
			}
		};
		Utility.execute(r);
	}

	/**
	 * Get the ConceptVersion identified by UUID on the ViewCoordinate configured by {@link #getViewCoordinate()} but 
	 * only if the concept exists at that point.  Returns null otherwise.
	 */
	public static ConceptVersionBI getConceptVersion(UUID uuid)
	{
		LOG.debug("Get ConceptVersion: '{}'", uuid);
		
		if (uuid == null)
		{
			return null;
		}
		try
		{
			ConceptVersionBI result = dataStore.getConceptVersion(getViewCoordinate(), uuid);

			// Nothing like an undocumented getter which, rather than returning null when
			// the thing you are asking for doesn't exist - it goes off and returns
			// essentially a new, empty, useless node. Sigh.
			if (result.getUUIDs().size() == 0)
			{
				return null;
			}

			return result;
		}
		catch (IOException ex)
		{
			LOG.error("Trouble getting concept: " + uuid, ex);
		}
		return null;
	}
	
	/**
	 * Get the ConceptVersion identified by NID on the ViewCoordinate configured by {@link #getViewCoordinate()} but 
	 * only if the concept exists at that point.  Returns null otherwise.
	 */
	public static ConceptVersionBI getConceptVersion(int nid)
	{
		LOG.debug("Get concept by nid: '{}'", nid);
		if (nid == 0)
		{
			return null;
		}
		try
		{
			ConceptVersionBI result = dataStore.getConceptVersion(getViewCoordinate(), nid);
			// Nothing like an undocumented getter which, rather than returning null when
			// the thing you are asking for doesn't exist - it goes off and returns
			// essentially a new, empty, useless node. Sigh.
			if (result.getUUIDs().size() == 0)
			{
				return null;
			}
			return result;
		}
		catch (IOException ex)
		{
			LOG.error("Trouble getting concept: " + nid, ex);
		}
		return null;
	}

	/**
	 * Currently configured to return InferredThenStatedLatest + INACTIVE status
	 */
	public static ViewCoordinate getViewCoordinate()
	{
		if (vc == null) {
			try
			{
				vc = StandardViewCoordinates.getSnomedStatedLatest();
			} catch (IOException e) {
				LOG.error("Unexpected error fetching view coordinates!", e);
			}
		}
		
		return vc;
	}

	public static RefexVersionBI<?> getRefsetMember(int nid) {
		try {
			RefexChronicleBI<?> refexChron = (RefexChronicleBI<?>) dataStore.getComponent(nid);

			if (refexChron != null) {
				getViewCoordinate().getAllowedStatus().add(Status.INACTIVE);
				
				RefexVersionBI<?> refexChronVersion = refexChron.getVersion(getViewCoordinate());
				
				getViewCoordinate().getAllowedStatus().remove(Status.INACTIVE);
				
				return refexChronVersion;
			}
		} catch (Exception ex) {
			LOG.warn("perhaps unexpected?", ex);
		}

		return null;
	}

	public static RefexChronicleBI<?> getAllVersionsRefsetMember(int nid) {
		try {
			return (RefexChronicleBI<?>) dataStore.getComponent(nid);

		} catch (Exception ex) {
			LOG.warn("perhaps unexpected?", ex);
		}

		return null;
	}

	public static void commit(ConceptVersionBI con) {
		try {
			dataStore.commit(con);
		} catch (IOException e) {
			// TODO this should be a thrown exception, knowing the commit failed is slightly important...
			LOG.error("commit failure", e);
		}
	}

	public static void commit() {
		try {
			
			if (!changeSetCreated) {
				TerminologySnapshotDI ts = dataStore.getSnapshot(getViewCoordinate());
				
				String name = "user#1#" + UUID.randomUUID();
				String tmpName = "user#0#" + UUID.randomUUID();
				//TODO fix OTF https://jira.ihtsdotools.org/browse/OTFISSUE-15
				File mainFile = new File(name + ".eccs").getCanonicalFile();// new File("C:\\Users\\yishai\\Desktop\\ISAAC\\" + name + ".eccs");
				File tempFile = new File(tmpName + ".eccs").getCanonicalFile(); //new File("C:\\Users\\yishai\\Desktop\\ISAAC\\" + tmpName + ".eccs");
				System.out.println(mainFile.getAbsolutePath());
				System.out.println(tempFile.getAbsolutePath());
				ChangeSetGeneratorBI csFile = ts.createDtoChangeSetGenerator(mainFile, tempFile, ChangeSetGenerationPolicy.MUTABLE_ONLY);
				ts.addChangeSetGenerator("OnlyOne", csFile);
			
				changeSetCreated = true;
			}
			
			dataStore.commit();
		} catch (Exception e) {
			// TODO this should be a thrown exception, knowing the commit failed is slightly important...
			LOG.error("commit failure", e);
		}
	}

	public static void addUncommitted(ConceptChronicleBI newCon) {
		try {
			dataStore.addUncommitted(newCon);
		} catch (IOException e) {
			// TODO this should be a thrown exception, knowing the commit failed is slightly important...
			LOG.error("addUncommitted failure", e);
		}
	}

	public static void addUncommitted(ConceptVersionBI newCon) {
		try {
			dataStore.addUncommitted(newCon);
		} catch (IOException e) {
			// TODO this should be a thrown exception, knowing the commit failed is slightly important...
			LOG.error("addUncommitted failure", e);
		}
	}

	public static void addUncommitted(int nid) {
		try {
			ConceptVersionBI con = getConceptVersion(nid);
			dataStore.addUncommitted(con);
		} catch (IOException e) {
			// TODO this should be a thrown exception, knowing the commit failed is slightly important...
			LOG.error("addUncommitted failure", e);
		}
	}

	public static void addUncommitted(UUID uuid) {
		try {
			ConceptVersionBI con = getConceptVersion(uuid);
			dataStore.addUncommitted(con);
		} catch (IOException e) {
			// TODO this should be a thrown exception, knowing the commit failed is slightly important...
			LOG.error("addUncommitted failure", e);
		}
	}
	
	/**
	 * Recursively get Is a children of a concept
	 */
	public static ArrayList<ConceptVersionBI> getAllChildrenOfConcept(int nid) throws IOException, ContradictionException
	{
		return getAllChildrenOfConcept(getConceptVersion(nid));
	}
	
	/**
	 * Recursively get Is a children of a concept
	 */
	public static ArrayList<ConceptVersionBI> getAllChildrenOfConcept(ConceptVersionBI concept) throws IOException, ContradictionException
	{
		ArrayList<ConceptVersionBI> results = new ArrayList<>();
		
		//TODO OTF Bug - OTF is broken, this returns all kinds of duplicates   https://jira.ihtsdotools.org/browse/OTFISSUE-21
		for (RelationshipVersionBI<?> r : concept.getRelationshipsIncomingActiveIsa())
		{
			results.add(getConceptVersion(r.getOriginNid()));
			results.addAll(getAllChildrenOfConcept(r.getOriginNid()));
		}
		return results;
	}

    public static ConceptChronicleBI createNewConcept(ConceptChronicleBI parent, String fsn,
            String prefTerm) throws IOException, InvalidCAB, ContradictionException {
        ConceptCB newConCB = createNewConceptBlueprint(parent, fsn, prefTerm);

        ConceptChronicleBI newCon = getBuilder().construct(newConCB);

        addUncommitted(newCon);
        commit();

        return newCon;
    }

    public static ConceptCB createNewConceptBlueprint(ConceptChronicleBI parent, String fsn, String prefTerm) throws ValidationException, IOException, InvalidCAB, ContradictionException {
        LanguageCode lc = LanguageCode.EN_US;
        UUID isA = Snomed.IS_A.getUuids()[0];
        IdDirective idDir = IdDirective.GENERATE_HASH;
        UUID module = Snomed.CORE_MODULE.getLenient().getPrimordialUuid();
        UUID parentUUIDs[] = new UUID[1];
        parentUUIDs[0] = parent.getPrimordialUuid();
        return new ConceptCB(fsn, prefTerm, lc, isA, idDir, module, parentUUIDs);
    }
}
