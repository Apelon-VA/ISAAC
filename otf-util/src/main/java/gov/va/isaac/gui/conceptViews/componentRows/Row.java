package gov.va.isaac.gui.conceptViews.componentRows;

import gov.va.isaac.gui.conceptViews.helpers.AnnotationRectangle;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerLabelHelper;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;

public abstract class Row {
	GridPane gp = null;

	protected ConceptViewerLabelHelper labelHelper;

	abstract public void createGridPane();
	
	public Row(ConceptViewerLabelHelper labelHelper) {
		this.labelHelper = labelHelper;
	}

	protected Rectangle createAnnotRectangle(ComponentVersionBI comp) {
		return AnnotationRectangle.create(comp);
	}

	public GridPane getGridPane() {
		return gp;
	}
}
