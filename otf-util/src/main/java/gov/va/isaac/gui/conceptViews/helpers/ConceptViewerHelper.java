package gov.va.isaac.gui.conceptViews.helpers;

import gov.va.isaac.util.WBUtility;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.type_long.RefexLongVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConceptViewerHelper {
	private static boolean controlKeyPressed = false;
	private static int snomedAssemblageNid;
	private static final Logger LOG = LoggerFactory.getLogger(ConceptViewerHelper.class);

	public enum ComponentType {
		CONCEPT, DESCRIPTION, RELATIONSHIP;
	}

	public ConceptViewerHelper() {
		snomedAssemblageNid = WBUtility.getConceptVersion(TermAux.SNOMED_IDENTIFIER.getUuids()[0]).getNid();
	}

	boolean getControlKeyPressed() {
		return controlKeyPressed;
	}

	void setControlKeyPressed(boolean pressed) {
		controlKeyPressed = pressed;
	}

	public int getSnomedAssemblageNid() {
		return snomedAssemblageNid;
	}

	// Helper Methods
	protected void copyToClipboard(String txt) {
        final ClipboardContent content = new ClipboardContent();
        content.putString(txt);
        Clipboard.getSystemClipboard().setContent(content);
	}

	public String getSctId(ConceptAttributeVersionBI attr)  {
        String sctidString = "Unreleased";
        // Official approach found int AlternativeIdResource.class
        
        try {
	        for (RefexChronicleBI<?> annotation : attr.getAnnotations()) {
				if (annotation.getAssemblageNid() == getSnomedAssemblageNid()) {
					RefexLongVersionBI sctid = (RefexLongVersionBI) annotation.getPrimordialVersion();
					sctidString = Long.toString(sctid.getLong1());
				}
			}
        } catch (Exception e) {
        	LOG.error("Could not access annotations for: " + attr.getPrimordialUuid());
        }
        return sctidString;
	}


	public String getPrimDef(ConceptAttributeVersionBI attr) {
        String status = "Primitive";
		if (attr.isDefined()) {
			status = "Fully Defined";
		}
		
        return status;
	}
	
	public int getPrimDefNid(ConceptAttributeVersionBI attr) {
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


	public ConceptAttributeVersionBI getConceptAttributes(ConceptVersionBI con) {
		try {
			ConceptAttributeVersionBI attr = con.getConceptAttributesActive();
			if (attr == null) {
				attr = con.getConceptAttributes().getVersion(WBUtility.getViewCoordinate());
				if (attr == null) {
					// handle Unhandled functionality
			        attr = (ConceptAttributeVersionBI) con.getConceptAttributes().getVersions().toArray()[con.getConceptAttributes().getVersions().size() - 1];
				}
			}
		
			return attr;
		} catch (Exception e) {
			LOG.debug("Cannot access concept's attributes for concept: " + con.getNid(), e);
			return null;
		}
	}

}
