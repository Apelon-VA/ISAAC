package gov.va.isaac.gui.conceptViews.componentRows;

import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerLabelHelper;
import javafx.scene.layout.GridPane;

import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;

public abstract class RelRow extends Row {
	int counter = 0;
	protected GridPane dgp;
	protected GridPane currentPane;
	boolean isDetailed = false;

	public void resetCounter() {
		counter = 0;
		currentPane = dgp;
		isDetailed = true;
	}

	public RelRow(ConceptViewerLabelHelper labelHelper) {
		super(labelHelper);
	}

	abstract public void addRelRow(RelationshipVersionBI rel);
}
