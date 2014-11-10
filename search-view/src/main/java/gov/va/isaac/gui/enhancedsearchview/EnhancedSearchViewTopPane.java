package gov.va.isaac.gui.enhancedsearchview;

import gov.va.isaac.gui.enhancedsearchview.model.EnhancedSavedSearch;
import gov.va.isaac.gui.enhancedsearchview.model.SearchModel;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class EnhancedSearchViewTopPane {
	private VBox topPanelVBox;
	
	private HBox searchTypeHBox;
	private Label searchTypeLabel;

	private Label maxResultsCustomTextFieldLabel;
	private HBox maxResultsHBox;
	
	private Button searchButton = new Button("SEARCH");
	
	private SearchModel searchModel = new SearchModel();
	
	private Font boldFont = new Font("System Bold", 13.0);


	public EnhancedSearchViewTopPane() {
		EnhancedSavedSearch savedSearch = new EnhancedSavedSearch();

		topPanelVBox = new VBox(10);
		topPanelVBox.setAlignment(Pos.CENTER);
		topPanelVBox.setPadding(new Insets(15));
		GridPane staticTopPanePortionGridPane = new GridPane();
		staticTopPanePortionGridPane.setHgap(15);

		// Handle Type DropDown
		initializeSearchTypeComboBox();

		// Handle Max Results
		initializeMaxResultsComboBox();

		// Add Search Button
		initializeSearchButton();

		staticTopPanePortionGridPane.setConstraints(searchTypeHBox,  0,  0,  1,  1,  HPos.LEFT,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);
		staticTopPanePortionGridPane.setConstraints(maxResultsHBox,  1,  0,  1,  1,  HPos.LEFT,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);
		staticTopPanePortionGridPane.setConstraints(searchModel.getResultsTypeComboBox(),  2,  0,  1,  1,  HPos.LEFT,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);
		staticTopPanePortionGridPane.setConstraints(savedSearch.getSaveButton(),  3,  0,  1,  1,  HPos.LEFT,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);
		staticTopPanePortionGridPane.setConstraints(searchButton,  4,  0,  1,  1,  HPos.RIGHT,  VPos.CENTER, Priority.ALWAYS, Priority.ALWAYS);

		staticTopPanePortionGridPane.addRow(0, searchTypeHBox, maxResultsHBox, searchModel.getResultsTypeComboBox(), savedSearch.getSaveButton(), searchButton);
		topPanelVBox.getChildren().add(staticTopPanePortionGridPane);

		searchModel.initializeCriteriaPane(maxResultsHBox, searchModel.getResultsTypeComboBox());
		topPanelVBox.getChildren().add(searchModel.getSearchTypeSelector().getResultsPane());
	}

	private void initializeMaxResultsComboBox() {
		maxResultsHBox = new HBox(5);
		maxResultsHBox.setAlignment(Pos.CENTER);

		maxResultsCustomTextFieldLabel = new Label("Max Results");
		maxResultsHBox.getChildren().add(maxResultsCustomTextFieldLabel);
		maxResultsHBox.getChildren().add(searchModel.getMaxResultsCustomTextField());
	}

	private void initializeSearchButton() {
		searchButton.requestFocus();
	}

	private void initializeSearchTypeComboBox() {
		searchTypeHBox = new HBox(10);
		searchTypeHBox.setAlignment(Pos.CENTER);
		
		searchTypeLabel = new Label("Select Search Type");
		searchTypeLabel.setFont(boldFont);

		searchTypeHBox.getChildren().add(searchTypeLabel);
		searchTypeHBox.getChildren().add(searchModel.getSearchTypeSelector().getSearchTypeComboBox());
	}

	public Button getSearchButton() {
		return searchButton;
	}

	public VBox getTopPaneVBox() {
		return topPanelVBox;
	}

	public IntegerField getMaxResults() {
		return null;
	}
}

