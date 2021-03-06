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

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.config.generated.StatedInferredOptions;
import gov.va.isaac.config.profiles.UserProfile;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.ihtsdo.otf.tcc.api.blueprint.ConceptCB;
import org.ihtsdo.otf.tcc.api.blueprint.DescriptionCAB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDynamicCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RelationshipCAB;
import org.ihtsdo.otf.tcc.api.blueprint.TerminologyBuilderBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.Position;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.lang.LanguageCode;
import org.ihtsdo.otf.tcc.api.metadata.ComponentType;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf1;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedRelType;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipType;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
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
 * {@link OTFUtility}
 * 
 * Utility for accessing OTF APIs.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 * @author ocarlsen
 * @author jefron
 */
public class OTFUtility {
	private static final Logger LOG = LoggerFactory.getLogger(OTFUtility.class);

	private static final UUID FSN_UUID = SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getUuids()[0];
	private static final UUID PREFERRED_UUID = SnomedMetadataRf2.PREFERRED_RF2.getUuids()[0];
	private static final UUID SYNONYM_UUID = SnomedMetadataRf2.SYNONYM_RF2.getUuids()[0];
	private static final UUID FSN_RF1_UUID = SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUuids()[0];
	private static final UUID PREFERRED_RF1_UUID = SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1.getUuids()[0];
	private static final UUID SYNONYM_RF1_UUID = SnomedMetadataRf1.SYNOMYM_DESCRIPTION_TYPE_RF1.getUuids()[0];

	private static Integer fsnNid = null;
	private static Integer preferredNid = null;
	private static Integer synonymNid = null;
	private static Integer fsnRf1Nid = null;
	private static Integer preferredRf1Nid = null;
	private static Integer synonymRf1Nid = null;
	private static Integer langTypeNid = null;
	
	private static BdbTerminologyStore dataStore = ExtendedAppContext.getDataStore();

	private static final Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");

	private static Set<UUID> rootNodeList = null;

	public static TerminologyBuilderBI getBuilder() {
		return new BdbTermBuilder(getEditCoordinate(), getViewCoordinateAllowInactive());
	}
	public static TerminologyBuilderBI getBuilder(EditCoordinate ec, ViewCoordinate vc) {
		return new BdbTermBuilder(ec, vc);
	}
	
	public static ViewCoordinate getViewCoordinate() {
		ViewCoordinate vc = null;
		try {
			UserProfile userProfile = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
			StatedInferredOptions policy = userProfile.getStatedInferredPolicy();
			switch(policy) {
			case STATED:
				vc = StandardViewCoordinates.getSnomedStatedLatest();
				break;
			case INFERRED:
				vc = StandardViewCoordinates.getSnomedInferredLatest();
				break;
			case INFERRED_THEN_STATED:
				vc = StandardViewCoordinates.getSnomedInferredThenStatedLatest();
				break;
			default: // Should never happen unless a new policy has been coded
				throw new RuntimeException("Unsupported StatedInferredOptions policy " + policy);
			}

			//LOG.info("Using {} policy for view coordinate", policy);

			final UUID pathUuid = userProfile.getViewCoordinatePath();
			final Long time = userProfile.getViewCoordinateTime();
			final ConceptChronicleBI pathChronicle = dataStore.getConcept(pathUuid);
			final int pathNid = pathChronicle.getNid();

			// Start with standard view coordinate and override the path setting to
			// use the preferred path
			Position position = dataStore.newPosition(dataStore.getPath(pathNid), time);

			vc.setViewPosition(position);

			//LOG.info("Using ViewCoordinate policy={}, path nid={}, uuid={}, desc={}", policy, pathNid, pathUuid, OTFUtility.getDescription(pathChronicle));
		} catch (NullPointerException e) {
			LOG.error("View path UUID does not exist", e);
		} catch (IOException e) {
			LOG.error("Unexpected error fetching view coordinates!", e);
		}

		return vc;
	}
	
	public static ViewCoordinate getViewCoordinateAllowInactive() 
	{
		ViewCoordinate vc = getViewCoordinate();
		vc.getAllowedStatus().add(Status.INACTIVE);
		vc.getAllowedStatus().add(Status.ACTIVE);
		return vc;
	}

	public static EditCoordinate getEditCoordinate() {
		try {
			UserProfile userProfile = ExtendedAppContext.getCurrentlyLoggedInUserProfile();

			int authorNid = dataStore.getNidForUuids(ExtendedAppContext.getCurrentlyLoggedInUserProfile().getConceptUUID());
			int module = Snomed.CORE_MODULE.getLenient().getNid();

			int pathNid = 0;
			ConceptChronicleBI pathChronicle = null;
			UUID pathUuid = userProfile.getEditCoordinatePath();
			if (pathUuid != null && (pathChronicle = dataStore.getConcept(pathUuid)) != null) {
				pathNid = pathChronicle.getNid();
			} else {
				pathNid = TermAux.WB_AUX_PATH.getLenient().getConceptNid();
				pathChronicle = dataStore.getConcept(pathNid);
				pathUuid = pathChronicle.getPrimordialUuid();
			}

			LOG.info("Using EditCoordinate path nid={}, uuid={}, desc={}", pathNid, pathUuid, OTFUtility.getDescription(pathChronicle));

			// Override edit path
			return new EditCoordinate(authorNid, module, pathNid);
		} catch (NullPointerException e) {
			LOG.error("Edit path UUID does not exist", e);
		} catch (IOException e) {
			LOG.error("error configuring edit coordinate", e);
		}

		return null;
	}
	
	/**
	 * Returns null if no concept exists with this nid
	 */
	public static String getDescriptionIfConceptExists(UUID uuid)
	{
		ConceptVersionBI result = getConceptVersion(uuid);
		return (result == null ? null : getDescription(result));
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
	 * Returns null if no concept exists with this nid
	 */
	public static String getDescriptionIfConceptExists(int nid)
	{
		ConceptVersionBI result = getConceptVersion(nid);
		return (result == null ? null : getDescription(result));
	}
	
	public static String getDescription(int nid) {
		try {
			if (!dataStore.hasConcept(nid))
			{
				return null;
			}
			ConceptVersionBI conceptVersion = dataStore.getConceptVersion(getViewCoordinate(), nid);
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

					if (descVer.getTypeNid() == getFSNNid() || descVer.getTypeNid() == getFsnRf1Nid()) {
						if (descVer.getStatus() == Status.ACTIVE) {
							if (ExtendedAppContext.getCurrentlyLoggedInUserProfile().getDisplayFSN()) {
								return descVer.getText();
							} else {
								fsn = descVer.getText();
							}

						} else {
							bestFound = descVer.getText();
						}
					} else if ((descVer.getTypeNid() == getSynonymTypeNid() || descVer.getTypeNid() == getSynonymRf1TypeNid()) && 
							isPreferred(descVer.getAnnotations())) {
						if (descVer.getStatus() == Status.ACTIVE) {
							if (!ExtendedAppContext.getCurrentlyLoggedInUserProfile().getDisplayFSN()) {
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
	public static String getFullySpecifiedName(ConceptChronicleBI concept) {
		try {
			if (concept.getDescriptions() != null) {
				for (DescriptionChronicleBI desc : concept.getDescriptions()) {
					int versionCount = desc.getVersions().size();
					DescriptionVersionBI<?> descVer = desc.getVersions()
							.toArray(new DescriptionVersionBI[versionCount])[versionCount - 1];

					if (descVer.getTypeNid() == getFSNNid() || descVer.getTypeNid() == getFsnRf1Nid()) {
						if (descVer.getStatus() == Status.ACTIVE) {
								return descVer.getText();
						}
					}
				}
			}
		} catch (IOException e) {
			// noop
		}
		
		return null;
	}
	
	private static int getFSNNid() {
		if (fsnNid == null) {
			fsnNid = dataStore.getNidForUuids(FSN_UUID);
		}
		return fsnNid;
	}

	private static int getFsnRf1Nid() {
		if (fsnRf1Nid == null) {
			fsnRf1Nid = dataStore.getNidForUuids(FSN_RF1_UUID);
		}
		return fsnRf1Nid;
	}

	private static int getSynonymTypeNid() {
		// Lazily load.
		if (synonymNid == null) {
			synonymNid = dataStore.getNidForUuids(SYNONYM_UUID);
		}
		return synonymNid;
	}

	private static int getSynonymRf1TypeNid() {
		// Lazily load.
		if (synonymRf1Nid == null) {
			synonymRf1Nid = dataStore.getNidForUuids(SYNONYM_RF1_UUID);
		}
		return synonymRf1Nid;
	}
	
	public static int getLangTypeNid() {
		// Lazily load.
		if (langTypeNid == null) {
			langTypeNid = dataStore.getNidForUuids(Snomed.LANGUAGE_REFEX.getPrimodialUuid());
		}
		return langTypeNid;
	}

	private static int getPreferredTypeNid() {
		// Lazily load.
		if (preferredNid == null) {
			preferredNid = dataStore.getNidForUuids(PREFERRED_UUID);
		}
		return preferredNid;
	}

	private static int getPreferredRf1TypeNid() {
		// Lazily load.
		if (preferredRf1Nid == null) {
			preferredRf1Nid = dataStore.getNidForUuids(PREFERRED_RF1_UUID);
		}
		return preferredRf1Nid;
	}

	/**
	 * Pass in the annotations on a description component to determine if one of the 
	 * annotations is the isPreferred annotation
	 */
	public static boolean isPreferred(Collection<? extends RefexChronicleBI<?>> collection) {
		for (RefexChronicleBI<?> rc : collection) {
			if (rc.getRefexType() == RefexType.CID) {
				int nid1 = ((NidMember) rc).getNid1();  // RefexType.CID means NidMember.
				if (nid1 == getPreferredTypeNid() || nid1 == getPreferredRf1TypeNid()) {
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
			if (dv.getTypeReference().getUuid().equals(FSN_UUID) || dv.getTypeReference().getUuid().equals(FSN_RF1_UUID)) {
				if (dv.getStatus() == Status.ACTIVE) {
					if (ExtendedAppContext.getCurrentlyLoggedInUserProfile().getDisplayFSN()) {
						return dv.getText();
					} else {
						fsn = dv.getText();
					}
				} else {
					bestFound = dv.getText();
				}
			} else if (dv.getTypeReference().getUuid().equals(SYNONYM_UUID) || dv.getTypeReference().getUuid().equals(SYNONYM_RF1_UUID)) {
				if ((dv.getStatus() == Status.ACTIVE) && isPreferred(dv.getAnnotations())) {
					if (!ExtendedAppContext.getCurrentlyLoggedInUserProfile().getDisplayFSN()) {
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
					return uuid.equals(PREFERRED_UUID) || uuid.equals(PREFERRED_RF1_UUID);
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
		ViewCoordinate vc = getViewCoordinate();
		
		return OTFUtility.getConceptVersion(nid, vc);
	}
	
	/**
	 * Get the ConceptVersion identified by NID on the ViewCoordinate configured by {@link #getViewCoordinate()} but 
	 * only if the concept exists at that point.  Returns null otherwise.
	 */
	public static ConceptVersionBI getConceptVersion(int nid, ViewCoordinate vc)
	{
		LOG.debug("Get concept by nid: '{}'", nid);
		if (nid == 0)
		{
			return null;
		}
		try
		{
			ConceptVersionBI result = dataStore.getConceptVersion(vc, nid);
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
	 * Get the ComponentVersionBI identified by NID on the ViewCoordinate configured by {@link #getViewCoordinate()} but 
	 * only if the Component exists at that point.  Returns null otherwise.
	 */
	public static ComponentVersionBI getComponentVersion(int nid)
	{
		LOG.debug("Get component by nid: '{}'", nid);
		if (nid == 0)
		{
			return null;
		}
		
		try
		{
			ComponentChronicleBI<?> componentChronicle = getComponentChronicle(nid);
			
			ComponentVersionBI componentVersion = componentChronicle.getVersion(getViewCoordinate());
			// Nothing like an undocumented getter which, rather than returning null when
			// the thing you are asking for doesn't exist - it goes off and returns
			// essentially a new, empty, useless node. Sigh.
			if (componentVersion == null || componentVersion.getUUIDs().size() == 0)
			{
				return null;
			} else {
				return componentVersion;
			}
		} catch (ContradictionException e) {
			LOG.error("Trouble getting concept " + nid + ".  Caught " + e.getClass().getName() + " " + e.getLocalizedMessage(), e);

			return null;
		}
	}
	
	/**
	 * Get the ComponentVersionBI identified by NID on the ViewCoordinate configured by {@link #getViewCoordinate()} but 
	 * only if the Component exists at that point.  Returns null otherwise.
	 */
	public static ComponentVersionBI getComponentVersion(UUID uuid)
	{
		LOG.debug("Get component by nid: '{}'", uuid);
		
		try
		{
			ComponentChronicleBI<?> componentChronicle = getComponentChronicle(uuid);
			
			ComponentVersionBI componentVersion = componentChronicle.getVersion(getViewCoordinate());
			// Nothing like an undocumented getter which, rather than returning null when
			// the thing you are asking for doesn't exist - it goes off and returns
			// essentially a new, empty, useless node. Sigh.
			if (componentVersion.getUUIDs().size() == 0)
			{
				return null;
			} else {
				return componentVersion;
			}
		} catch (ContradictionException e) {
			LOG.error("Trouble getting concept " + uuid + ".  Caught " + e.getClass().getName() + " " + e.getLocalizedMessage(), e);

			return null;
		}
	}
	
	/**
	 * Get the Component identified by NID on the ViewCoordinate configured by {@link #getViewCoordinate()} but 
	 * only if it exists at that point.  Returns null otherwise.
	 */
	public static ComponentChronicleBI<?> getComponentChronicle(int nid)
	{
		LOG.debug("Get component chronicle by nid: '{}'", nid);
		if (nid == 0)
		{
			return null;
		}
		try
		{
			ComponentChronicleBI<?> result = dataStore.getComponent(nid);
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
			LOG.error("Trouble getting component: " + nid, ex);
		}
		return null;
	}

	/**
	 * Get the Component identified by NID on the ViewCoordinate configured by {@link #getViewCoordinate()} but 
	 * only if it exists at that point.  Returns null otherwise.
	 */
	public static ComponentChronicleBI<?> getComponentChronicle(UUID uuid)
	{
		LOG.debug("Get component chronicle by uuid: '{}'", uuid);
		if (uuid == null)
		{
			return null;
		}
		try
		{
			ComponentChronicleBI<?> result = dataStore.getComponent(uuid);
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
			LOG.error("Trouble getting component: " + uuid, ex);

			return null;
		}
	}
	
	public static RefexVersionBI<?> getRefsetMember(int nid) {
		try {
			RefexChronicleBI<?> refexChron = (RefexChronicleBI<?>) dataStore.getComponent(nid);

			if (refexChron != null) {
				ViewCoordinate tempVc = getViewCoordinate();
				tempVc.getAllowedStatus().add(Status.INACTIVE);
				RefexVersionBI<?> refexChronVersion = refexChron.getVersion(tempVc);
				
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

	public static boolean isUncommittened(ConceptVersionBI con) {
		return dataStore.getUncommittedConcepts().contains(con.getChronicle());
	}
	
	public static boolean commit(ConceptVersionBI con) throws IOException {
		return dataStore.commit(con);
	}

	public static boolean commit() throws IOException {
		return dataStore.commit();
	}

	public static void addUncommitted(ConceptChronicleBI newCon) throws IOException {
		dataStore.addUncommitted(newCon);
	}

	public static void addUncommitted(ConceptVersionBI newCon) throws IOException {
		dataStore.addUncommitted(newCon);
	}

	public static void addUncommitted(int nid) throws IOException {
		ConceptVersionBI con = getConceptVersion(nid);
		dataStore.addUncommitted(con);
	}

	public static void addUncommitted(UUID uuid) throws IOException {
		ConceptVersionBI con = getConceptVersion(uuid);
		dataStore.addUncommitted(con);
	}
	
	/**
	 * Recursively find the leaf nodes of a concept hierarchy
	 * @param nid - starting concept
	 */
	public static Set<ConceptVersionBI> getAllLeafChildrenOfConcept(int nid) throws IOException, ContradictionException
	{
		return getAllChildrenOfConcept(new HashSet<>(), getConceptVersion(nid), true, true);
	}
	
	/**
	 * Recursively get Is a children of a concept
	 */
	public static Set<ConceptVersionBI> getAllChildrenOfConcept(int nid, boolean recursive) throws IOException, ContradictionException
	{
		return getAllChildrenOfConcept(new HashSet<>(), getConceptVersion(nid), recursive, false);
	}
	
	/**
	 * Recursively get Is a children of a concept
	 */
	public static Set<ConceptVersionBI> getAllChildrenOfConcept(ConceptVersionBI concept, boolean recursive) throws IOException, ContradictionException
	{
		return getAllChildrenOfConcept(new HashSet<>(), concept, recursive, false);
	}
	
	/**
	 * Recursively get Is a children of a concept
	 */
	private static Set<ConceptVersionBI> getAllChildrenOfConcept(Set<Integer> handledConceptNids, ConceptVersionBI concept, boolean recursive, boolean leafOnly) 
			throws IOException, ContradictionException
	{
		Set<ConceptVersionBI> results = new HashSet<>();
		
		// This both prevents infinite recursion and avoids processing or returning of duplicates
		if (handledConceptNids.contains(concept.getNid())) {
			LOG.debug("Encountered already-handled concept \"{}\".  May be result of OTF-returned duplicate or source of potential infinite loop", OTFUtility.getDescription(concept.getNid()));
			return results;
		}

		//TODO OTF Bug - OTF is broken, this returns all kinds of duplicates  https://jira.ihtsdotools.org/browse/OTFISSUE-21
		int size = 0;
		for (RelationshipVersionBI<?> r : concept.getRelationshipsIncomingActiveIsa())
		{
			size++;
			if (handledConceptNids.contains(r.getOriginNid())) {
				// avoids processing or returning of duplicates
				LOG.debug("Encountered already-handled ORIGIN child concept \"{}\".  May be result of OTF-returned duplicate or source of potential infinite loop", OTFUtility.getDescription(r.getOriginNid()));

				continue;
			}

			ConceptVersionBI originConcept = getConceptVersion(r.getOriginNid());
			if (!leafOnly)
			{
				results.add(originConcept);
			}
			if (recursive)
			{
				results.addAll(getAllChildrenOfConcept(handledConceptNids, originConcept, recursive, leafOnly));
			}
		}
		
		if (leafOnly && size == 0 && !handledConceptNids.contains(concept.getNid()))
		{
			results.add(concept);
		}
		handledConceptNids.add(concept.getNid());
		return results;
	}

	public static Set<Integer> getAllChildrenOfConceptAsNids(Integer conceptNid, boolean recursive) throws IOException, ContradictionException
	{
		Set<ConceptVersionBI> resultsAsConceptVersions = getAllChildrenOfConcept(conceptNid, recursive);
		Set<Integer> results = new HashSet<>();
		
		for (ConceptVersionBI conceptVersion : resultsAsConceptVersions) {
			results.add(conceptVersion.getNid());
		}

		return results;
	}

	/**
	 * Recursively get Is a parents of a concept
	 */
	public static Set<ConceptVersionBI> getConceptAncestors(int nid) throws IOException, ContradictionException
	{
		return getConceptAncestors(getConceptVersion(nid));
	}
	/**
	 * Recursively get Is a parents of a concept
	 */
	public static Set<ConceptVersionBI> getConceptAncestors(ConceptVersionBI concept) throws IOException, ContradictionException
	{
		Set<Integer> handledNids = new HashSet<>();
		
		return getConceptAncestors(handledNids, concept);
	}
	private static Set<ConceptVersionBI> getConceptAncestors(Set<Integer> handledNids, ConceptVersionBI concept) throws IOException, ContradictionException
	{
		Set<ConceptVersionBI> results = new HashSet<>();
		
		// This both prevents infinite recursion and avoids processing or returning of duplicates
		if (handledNids.contains(concept.getNid())) {
			LOG.debug("Encountered already-handled concept \"{}\".  May be result of OTF-returned duplicate or source of potential infinite loop", OTFUtility.getDescription(concept.getNid()));
			return results;
		}
		
		//TODO OTF Bug - OTF is broken, this returns all kinds of duplicates  https://jira.ihtsdotools.org/browse/OTFISSUE-21
		for (RelationshipVersionBI<?> r : concept.getRelationshipsOutgoingActiveIsa())
		{
			if (handledNids.contains(r.getDestinationNid())) {
				// avoids processing or returning of duplicates
				LOG.debug("Encountered already-handled DESTINATION ancestor concept \"{}\".  May be result of OTF-returned duplicate or source of potential infinite loop", OTFUtility.getDescription(r.getDestinationNid()));
				continue;
			}

			ConceptVersionBI destConcept = getConceptVersion(r.getDestinationNid());
			results.add(destConcept);
			results.addAll(getConceptAncestors(handledNids, destConcept));
		}

		handledNids.add(concept.getNid());

		return results;
	}

	/**
	 * Finds just the concept's parents
	 */
	public static Set<ConceptVersionBI> getConceptParents(ConceptVersionBI concept) throws IOException, ContradictionException
	{
		Set<ConceptVersionBI> results = new HashSet<>();
		
		for (RelationshipVersionBI<?> r : concept.getRelationshipsOutgoingActiveIsa())
		{
			results.add(getConceptVersion(r.getDestinationNid()));
		}
		return results;
	}

	/**
	 * Recursively looks at parents until finds a concept without a parent
	 */
	public static ConceptVersionBI getRootConcept(ConceptVersionBI concept) throws IOException, ContradictionException
	{
		ConceptVersionBI parent = OTFUtility.getConceptVersion(concept.getRelationshipsOutgoingActiveIsa().iterator().next().getDestinationNid());

		//TODO wouldn't it be far safer to just look and see if the parent is ISAAC.ISAAC_ROOT ?
		//And document the method that way?
		if (!getRootNodeList().contains(parent.getPrimordialUuid())) {
			return getRootConcept(parent);
		} else {
			return parent;
		}
	}

	private static Set<UUID> getRootNodeList() {
		if (rootNodeList == null) {
			rootNodeList = new HashSet<UUID>();
			rootNodeList.add(UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8")); // SCT
			rootNodeList.add(UUID.fromString("3958d043-9e8c-508e-bf6d-fd9c83a856da")); // LOINC
			rootNodeList.add(UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8"));
		}
		
		return rootNodeList;
	}

	public static ConceptChronicleBI createNewConcept(ConceptChronicleBI parent, String fsn,
			String prefTerm) throws IOException, InvalidCAB, ContradictionException {
		ConceptCB newConCB = createNewConceptBlueprint(parent, fsn, prefTerm);

		ConceptChronicleBI newCon = getBuilder().construct(newConCB);

		return newCon;
	}
	
	public static ConceptChronicleBI createAndCommitNewConcept(ConceptChronicleBI parent, String fsn,
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

	public static boolean commit(int nid) throws IOException {
		ConceptVersionBI con = getConceptVersion(nid);
		return commit(con);
	}

	public static void cancel() {
		dataStore.cancel();
	}

	public static String getConPrefTerm(int nid) {
		try {
			return OTFUtility.getConceptVersion(nid).getPreferredDescription().getText();
		} catch (IOException | ContradictionException e) {
			LOG.error("Unable to identify description.  Points to larger problem", e);
			return "ERROR";
		}
	}

	public static String getStatusString(ComponentVersionBI comp) {
		return comp.getStatus() == Status.ACTIVE ? "Active" : "Inactive";
	}

	public static String getAuthorString(ComponentVersionBI comp) {
		return getConPrefTerm(comp.getAuthorNid());
	}

	public static String getModuleString(ComponentVersionBI comp) {
		return getConPrefTerm(comp.getModuleNid());
	}

	public static String getPathString(ComponentVersionBI comp) {
		return getConPrefTerm(comp.getPathNid());
	}

	public static String getTimeString(ComponentVersionBI comp) {
		if (comp.getTime() != Long.MAX_VALUE) {
			Date date = new Date(comp.getTime());
	
			return format.format(date);		
		} else {
			return "Uncommitted";
		}
	}

	public static void createNewDescription(int conNid, int typeNid, LanguageCode lang, String term, boolean isInitial) throws IOException, InvalidCAB, ContradictionException {
		DescriptionCAB newDesc = new DescriptionCAB(conNid, typeNid, lang, term, isInitial, IdDirective.GENERATE_HASH); 

		getBuilder().construct(newDesc);
		addUncommitted(conNid);
	}

	public static void createNewRelationship(int conNid, int typeNid, int targetNid, int group, RelationshipType type) throws IOException, InvalidCAB, ContradictionException {
		RelationshipCAB newRel = new RelationshipCAB(conNid, typeNid, targetNid, group, type, IdDirective.GENERATE_HASH);
		
		getBuilder().construct(newRel);
		addUncommitted(conNid);
	}
		
	public static void createNewDescription(int conNid, String term) throws IOException, InvalidCAB, ContradictionException {
		DescriptionCAB newDesc = new DescriptionCAB(conNid, SnomedMetadataRf2.SYNONYM_RF2.getNid(), LanguageCode.EN_US, term, false, IdDirective.GENERATE_HASH); 

		getBuilder().construct(newDesc);
		addUncommitted(conNid);
	}

	public static void createNewRole(int conNid, int typeNid, int targetNid) throws IOException, InvalidCAB, ContradictionException {
		RelationshipCAB newRel = new RelationshipCAB(conNid, typeNid, targetNid, 0, RelationshipType.STATED_ROLE, IdDirective.GENERATE_HASH);
		
		getBuilder().construct(newRel);
		addUncommitted(conNid);
	}

	public static void createNewParent(int conNid, int targetNid) throws ValidationException, IOException, InvalidCAB, ContradictionException {
		RelationshipCAB newRel = new RelationshipCAB(conNid, SnomedRelType.IS_A.getNid(), targetNid, 0, RelationshipType.STATED_HIERARCHY, IdDirective.GENERATE_HASH);
		
		getBuilder().construct(newRel);
		addUncommitted(conNid);
	}

	public static void addUncommittedNoChecks(ConceptChronicleBI con) throws IOException {
		dataStore.addUncommittedNoChecks(con);
	}
	
	public static List<ConceptChronicleBI> getPathConcepts() throws ValidationException, IOException, ContradictionException {
		ConceptChronicleBI pathRefset =
				dataStore.getConcept(TermAux.PATH_REFSET.getLenient().getPrimordialUuid());
			Collection<? extends RefexChronicleBI<?>> members = pathRefset.getRefsetMembers();
			List<ConceptChronicleBI> pathConcepts = new ArrayList<>();
			for (RefexChronicleBI<?> member : members) {
				int memberNid = ((NidMember)member).getC1Nid();
				ConceptChronicleBI pathConcept = dataStore.getConcept(memberNid);
				pathConcepts.add(pathConcept);
			}
			return pathConcepts;
	}

	public static ComponentVersionBI getLastCommittedVersion(ComponentChronicleBI<?> chronicle) {
		// Strictly Time-Based sorting.  Should suffice until a) Path setup changes or b) Proper implementation added to tcc
		@SuppressWarnings("unchecked")
		Collection<ComponentVersionBI> versions = (Collection<ComponentVersionBI>) chronicle.getVersions();
		
		ComponentVersionBI latestVersion = null;
		for (ComponentVersionBI v : versions) {
			if ((v.getTime() != Long.MAX_VALUE) && 
				(latestVersion == null || v.getTime() > latestVersion.getTime())) {
				latestVersion = v;
			}
		}
		return latestVersion;
	}

	public static void addToPromotionPath(UUID compUuid) throws IOException, ContradictionException, InvalidCAB {
		// Setup Edit Path to be promotion path
		ConceptVersionBI pp = getConceptVersion(AppContext.getAppConfiguration().getCurrentWorkflowPromotionPathUuidAsUUID());
		ConceptSpec cs = new ConceptSpec(pp.getNid());
		
		List<ConceptSpec> editPaths = new ArrayList<ConceptSpec>();
		editPaths.add(cs);
		EditCoordinate editCoord = getEditCoordinate();
		editCoord.setEditPathListSpecs(editPaths);
		TerminologyBuilderBI builder = getBuilder(editCoord, getViewCoordinate());
		
		// Create new version of all uncommitted components in concept
		ConceptVersionBI conceptWithComp = OTFUtility.getConceptVersion(getComponentVersion(compUuid).getConceptNid());
		Set<ComponentVersionBI> componentsInConcept = getConceptComponents(conceptWithComp);

		int devPathNid = ExtendedAppContext.getDataStore().getNidForUuids(UUID.fromString(AppContext.getAppConfiguration().getDefaultEditPathUuid()));
		
		for (ComponentVersionBI comp : componentsInConcept) {
			if (comp.getPathNid() == devPathNid) {
				ComponentType type = ComponentType.getComponentVersionType(comp);
				@SuppressWarnings("unused")
				ComponentChronicleBI<?> cbi = null;
	
				if (type == ComponentType.CONCEPT) {
					ConceptCB cab = (ConceptCB) comp.makeBlueprint(getViewCoordinate(), IdDirective.PRESERVE, RefexDirective.EXCLUDE);
					cbi = builder.construct(cab);
				} else if (type == ComponentType.DESCRIPTION) {
					DescriptionCAB cab = (DescriptionCAB) comp.makeBlueprint(getViewCoordinate(), IdDirective.PRESERVE, RefexDirective.EXCLUDE);
					cbi = builder.construct(cab);
				} else if (type == ComponentType.RELATIONSHIP) {
					RelationshipCAB cab = (RelationshipCAB) comp.makeBlueprint(getViewCoordinate(), IdDirective.PRESERVE, RefexDirective.EXCLUDE);
					cbi = builder.construct(cab);
				} else if (type == ComponentType.SEMEME) {
					RefexCAB cab = (RefexCAB) comp.makeBlueprint(getViewCoordinate(), IdDirective.PRESERVE, RefexDirective.EXCLUDE);
					cbi = builder.construct(cab);
				} else if (type == ComponentType.SEMEME_DYNAMIC) {
					RefexDynamicCAB cab = (RefexDynamicCAB) comp.makeBlueprint(getViewCoordinate(), IdDirective.PRESERVE, RefexDirective.EXCLUDE);
					cbi = builder.construct(cab);
				}
			}	
		}
		
		commit(conceptWithComp.getVersion(getViewCoordinate()));
	}
	
	
	public static Set<ComponentVersionBI> getConceptComponents(
			ConceptVersionBI conceptWithComp) throws IOException, ContradictionException {
		Set<ComponentVersionBI> retSet = new HashSet<>();
		
		retSet.add(conceptWithComp);
		
		for(DescriptionChronicleBI desc : conceptWithComp.getDescriptions()) {
			retSet.add(desc.getVersion(conceptWithComp.getViewCoordinate()));
		}

		for(RelationshipChronicleBI rel : conceptWithComp.getRelationshipsOutgoing()) {
			retSet.add(rel.getVersion(conceptWithComp.getViewCoordinate()));
		}

		for(RefexChronicleBI<?> refsetMember : conceptWithComp.getRefsetMembers()) {
			retSet.add(refsetMember.getVersion(conceptWithComp.getViewCoordinate()));
		}

		for(RefexDynamicChronicleBI<?> dynRef : conceptWithComp.getRefexesDynamic()) {
			retSet.add(dynRef.getVersion(conceptWithComp.getViewCoordinate()));
		}

		return retSet;
	}
	
	public static ConceptAttributeVersionBI<?> getLatestAttributes(@SuppressWarnings("rawtypes") Collection<? extends ConceptAttributeVersionBI> collection)
	{
		ConceptAttributeVersionBI<?> newest = null;
		long newestTime = Long.MIN_VALUE;
		for (ConceptAttributeVersionBI<?> x : collection)
		{
			if (x.getTime() > newestTime)
			{
				newest = x;
				newestTime = x.getTime();
			}
		}
		return newest;
	}
	
	public static DescriptionVersionBI<?> getLatestDescVersion(@SuppressWarnings("rawtypes") Collection<? extends DescriptionVersionBI> collection)
	{
		DescriptionVersionBI<?> newest = null;
		long newestTime = Long.MIN_VALUE;
		for (DescriptionVersionBI<?> x : collection)
		{
			if (x.getTime() > newestTime)
			{
				newest = x;
				newestTime = x.getTime();
			}
		}
		return newest;
	}
	
	public static RefexVersionBI<?> getLatestRefexVersion(@SuppressWarnings("rawtypes") Collection<? extends RefexVersionBI> collection)
	{
		RefexVersionBI<?> newest = null;;
		long newestTime = Long.MIN_VALUE;
		for (RefexVersionBI<?> x : collection)
		{
			if (x.getTime() > newestTime)
			{
				newest = x;
				newestTime = x.getTime();
			}
		}
		return newest;
	}
	
	public static RefexDynamicVersionBI<?> getLatestDynamicRefexVersion(@SuppressWarnings("rawtypes") Collection<? extends RefexDynamicVersionBI> collection)
	{
		RefexDynamicVersionBI<?> newest = null;;
		long newestTime = Long.MIN_VALUE;
		for (RefexDynamicVersionBI<?> x : collection)
		{
			if (x.getTime() > newestTime)
			{
				newest = x;
				newestTime = x.getTime();
			}
		}
		return newest;
	}

	/**
	 * Returns the uuid for fsn and pt based on the ConceptCB assignment algorithm.
	 *
	 * @param fsn the fsn
	 * @param pt the pt
	 * @return the uuid for fsn
	 */
	public static UUID getUuidForFsn(String fsn, String pt) {
		List<String> fsns = new ArrayList<>();
		fsns.add(fsn);
		List<String> pts = new ArrayList<>();
		pts.add(pt);
		return ConceptCB.computeComponentUuid(IdDirective.GENERATE_REFEX_CONTENT_HASH, fsns, pts, null);
	}
}
