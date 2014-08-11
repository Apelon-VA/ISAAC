package gov.va.isaac.gui.conceptViews.helpers;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;

public class AnnotationRectangle {

	public static Rectangle create(ComponentVersionBI comp) {
		Rectangle rec = new Rectangle(5, 5);
		rec.setFill(Color.BLACK);
		
		rec.setVisible(!ConceptViewerHelper.getAnnotations(comp).isEmpty());
		
		return rec;
	}
}
