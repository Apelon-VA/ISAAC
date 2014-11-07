package gov.va.isaac.gui.enhancedsearchview.type;

import gov.va.isaac.gui.enhancedsearchview.model.SearchTypeModel;
import javafx.scene.layout.Pane;

public interface SearchTypeSpecificView {
	Pane setContents(SearchTypeModel typeModel);
}
