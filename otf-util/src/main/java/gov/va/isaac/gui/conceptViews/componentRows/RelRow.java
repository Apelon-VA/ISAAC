package gov.va.isaac.gui.conceptViews.componentRows;

import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerLabelHelper;
import javafx.scene.layout.GridPane;

import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;

public abstract class RelRow extends Row {

	public RelRow(ConceptViewerLabelHelper labelHelper) {
		super(labelHelper);
	}
	
	abstract public GridPane createRelGridPane(RelationshipVersionBI rel);
}
