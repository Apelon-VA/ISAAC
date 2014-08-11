package gov.va.isaac.gui.conceptViews.componentRows;

import gov.va.isaac.gui.conceptViews.componentRows.Row;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerLabelHelper;
import javafx.scene.layout.GridPane;

import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;

public abstract class TermRow extends Row {
	int counter = 0;

	public TermRow(ConceptViewerLabelHelper labelHelper) {
		super(labelHelper);
	}
	
	protected String getBooleanValue(boolean val) {
		if (val) {
			return "true";
		} else {
			return "false";
		}
	}
	

	abstract public void addTermRow(DescriptionVersionBI rel);

}
