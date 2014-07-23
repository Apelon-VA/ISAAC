package gov.va.isaac.gui.conceptViews.componentRows;

import gov.va.isaac.gui.conceptViews.helpers.AnnotationRectangle;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerLabelHelper;
import javafx.scene.shape.Rectangle;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;

public abstract class Row {
	protected static ConceptViewerLabelHelper labelHelper;

	public Row(ConceptViewerLabelHelper labelHelper) {
		this.labelHelper = labelHelper;
	}

	protected Rectangle createAnnotRectangle(ComponentVersionBI comp) {
		return AnnotationRectangle.create(comp);
	}

}
