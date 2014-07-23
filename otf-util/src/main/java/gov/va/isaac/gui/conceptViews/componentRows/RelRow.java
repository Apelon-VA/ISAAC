package gov.va.isaac.gui.conceptViews;

import javafx.scene.layout.GridPane;

import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;

import gov.va.isaac.gui.conceptViews.componentRows.Row;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerLabelHelper;

public abstract class RelRow extends Row {

	public RelRow(ConceptViewerLabelHelper labelHelper) {
		super(labelHelper);
	}
	
	abstract public GridPane createRelGridPane(RelationshipVersionBI rel);
}
