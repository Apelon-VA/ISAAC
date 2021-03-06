package gov.va.isaac.gui.conceptViews.helpers;

import gov.va.isaac.util.OTFUtility;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.id.IdBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_long.RefexLongVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConceptViewerHelper {
	private static Integer snomedAssemblageNid;
	private static final Logger LOG = LoggerFactory.getLogger(ConceptViewerHelper.class);

	private ConceptViewerHelper()
	{
		//helper, don't construct....
	}

	public static int getSnomedAssemblageNid() {
		if (snomedAssemblageNid == null)
		{
			snomedAssemblageNid = OTFUtility.getConceptVersion(TermAux.SNOMED_IDENTIFIER.getUuids()[0]).getNid();
		}
		return snomedAssemblageNid;
	}

	public static String getSctId(ComponentVersionBI attr)  {
		String sctidString = "Unreleased";
		// Official approach found int AlternativeIdResource.class
		
		boolean found = false;
		try {
			for (RefexChronicleBI<?> annotation : attr.getAnnotations()) {
				if (annotation.getAssemblageNid() == getSnomedAssemblageNid()) {
					RefexLongVersionBI<?> sctid = (RefexLongVersionBI<?>) annotation.getPrimordialVersion();
					sctidString = Long.toString(sctid.getLong1());
					found = true;
				}
			}

			if (!found) {
				// legacy representation of SCTID for use with older econcepts files
				for (IdBI id : attr.getAllIds()) {
					// Identify "SCT" identifiers
					if (id.getAuthorityNid() == TermAux.SNOMED_IDENTIFIER.getLenient().getNid()) {
						// Found SCTID, return it
						sctidString = id.getDenotation().toString();
					}
				}
			}

		} catch (Exception e) {
			LOG.error("Could not access annotations for: " + attr.getPrimordialUuid());
		}
		
		return sctidString;
	}


	public static String getPrimDef(ConceptAttributeVersionBI<?> attr) {
		String status = "Primitive";
		if (attr.isDefined()) {
			status = "Fully Defined";
		}
		
		return status;
	}
	
	public static int getPrimDefNid(ConceptAttributeVersionBI<?> attr) {
		try {
			int nid = SnomedMetadataRf2.PRIMITIVE_RF2.getLenient().getNid();
			if (attr.isDefined()) {
				nid = SnomedMetadataRf2.DEFINED_RF2.getLenient().getNid();
			}
			return nid;
		} catch (Exception e) {
			LOG.error("Unable to access ConceptSpec Nids", e);
			return -1;
		}
	}


	public static ConceptAttributeVersionBI<?> getConceptAttributes(ConceptVersionBI con) {
		try {
			ConceptAttributeVersionBI<?> attr = con.getConceptAttributesActive();
			if (attr == null) {
				attr = con.getConceptAttributes().getVersion(OTFUtility.getViewCoordinate());
				if (attr == null) {
					// handle Unhandled functionality
					attr = (ConceptAttributeVersionBI<?>) con.getConceptAttributes().getVersions().toArray()[con.getConceptAttributes().getVersions().size() - 1];
				}
			}
		
			return attr;
		} catch (Exception e) {
			LOG.debug("Cannot access concept's attributes for concept: " + con.getNid(), e);
			return null;
		}
	}

	public static Set<RefexVersionBI<?>> getAnnotations(ComponentVersionBI comp) {
		Set<RefexVersionBI<?>> retSet = new HashSet<>();
		
		try {
			for (RefexVersionBI<?> annot : comp.getAnnotationsActive(OTFUtility.getViewCoordinate())) {
				if (annot.getAssemblageNid() != getSnomedAssemblageNid()) {
					retSet.add(annot);
				}
			}
		} catch (IOException e) {
			LOG.error("Unable to access annotations", e);
		}
		
		return retSet;
	}
	
}
