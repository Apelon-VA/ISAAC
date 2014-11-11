package gov.va.isaac.gui.enhancedsearchview.model.type.sememe;

import gov.va.isaac.gui.enhancedsearchview.model.SearchTypeModel;
import gov.va.isaac.gui.enhancedsearchview.model.type.SearchTypeSpecificView;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SememeSearchTypeView implements SearchTypeSpecificView {
	private VBox criteriaPane = new VBox(5);

	private Font boldFont = new Font("System Bold", 13.0);


	final static Logger logger = LoggerFactory.getLogger(SememeSearchTypeView.class);


	@Override
	public Pane setContents(SearchTypeModel typeModel) {
		SememeSearchTypeModel SememeModel = (SememeSearchTypeModel)typeModel;

		Pane topPane = setupTextSelection(SememeModel);
		Pane bottomPane = setupAssemblageSelection(SememeModel);
		
		criteriaPane.getChildren().addAll(topPane, bottomPane);
		
		return criteriaPane;
	}

	private Pane setupTextSelection(SememeSearchTypeModel sememeModel) {
		HBox searchTextHBox = new HBox(10);
		
		Label rootExp = new Label("Enter Search Text");
		rootExp.setFont(boldFont);
		
		searchTextHBox.getChildren().addAll(rootExp, sememeModel.getSearchText());
		return searchTextHBox;
	}

	private Pane setupAssemblageSelection(SememeSearchTypeModel sememeModel) {
		if (sememeModel.getOptionsContentVBox() == null) {
			sememeModel.setOptionsContentVBox(new VBox());
			sememeModel.getOptionsContentVBox().getChildren().add(sememeModel.getSearchInRefexHBox());
		}
		
		return sememeModel.getOptionsContentVBox();
	}


}
