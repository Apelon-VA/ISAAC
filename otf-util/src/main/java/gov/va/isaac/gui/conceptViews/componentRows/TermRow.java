package gov.va.isaac.gui.conceptViews.componentRows;

import java.io.IOException;

import gov.va.isaac.gui.conceptViews.componentRows.Row;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerLabelHelper;
import gov.va.isaac.util.OTFUtility;

import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TermRow extends Row {
	int counter = 0;
	static protected String prefTermTypeStr = null;
	static protected int prefTermTypeNid = 0;
	protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

	public TermRow(ConceptViewerLabelHelper labelHelper) {
		super(labelHelper);
		
		if (prefTermTypeStr == null) {
			try {
				prefTermTypeNid = SnomedMetadataRf2.PREFERRED_RF2.getNid();
				prefTermTypeStr = OTFUtility.getConPrefTerm(prefTermTypeNid);
			} catch (IOException e) {
				LOG.error("Unable to defined Preferred RF2 Term", e);
			}
		}
	}
	
	protected String getBooleanValue(boolean val) {
		if (val) {
			return "true";
		} else {
			return "false";
		}
	}
	

	abstract public void addTermRow(DescriptionVersionBI<?> rel, boolean isPrefTerm);

}
