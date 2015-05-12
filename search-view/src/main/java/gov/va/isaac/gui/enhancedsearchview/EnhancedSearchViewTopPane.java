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
	
	private Button searchButton = new Button("Search");
	
	private SearchModel searchModel = new SearchModel();
	
	private Font boldFont = new Font("System Bold", 13.0);

	private final EnhancedSavedSearch searchSaver;

	public EnhancedSearchViewTopPane() {
		searchSaver = new EnhancedSavedSearch();

		topPanelVBox = new VBox(10);
		topPanelVBox.setAlignment(Pos.CENTER);
		topPanelVBox.setPadding(new Insets(5));
		GridPane staticTopPanePortionGridPane = new GridPane();
		staticTopPanePortionGridPane.setHgap(15);
		staticTopPanePortionGridPane.setPadding(new Insets(5));

		// Handle Type DropDown
		initializeSearchTypeComboBox();

		// Handle Max Results
		initializeMaxResultsComboBox();

		// Add Search Button
		initializeSearchButton();
		
		//searchSaver.getSaveButton().disableProperty().bind(SearchModel.isSearchSavableProperty().not());

		staticTopPanePortionGridPane.setConstraints(searchTypeHBox,  0,  0,  1,  1,  HPos.LEFT,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);
		staticTopPanePortionGridPane.setConstraints(maxResultsHBox,  1,  0,  1,  1,  HPos.LEFT,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);
		staticTopPanePortionGridPane.setConstraints(searchModel.getResultsTypeComboBox(),  2,  0,  1,  1,  HPos.LEFT,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);
		//staticTopPanePortionGridPane.setConstraints(searchSaver.getSaveButton(),  3,  0,  1,  1,  HPos.LEFT,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);
		//staticTopPanePortionGridPane.setConstraints(searchButton,  4,  0,  1,  1,  HPos.RIGHT,  VPos.CENTER, Priority.ALWAYS, Priority.ALWAYS);
		staticTopPanePortionGridPane.setConstraints(searchButton,  3,  0,  1,  1,  HPos.LEFT,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);

		//staticTopPanePortionGridPane.addRow(0, searchTypeHBox, maxResultsHBox, searchModel.getResultsTypeComboBox(), searchSaver.getSaveButton(), searchButton);
		staticTopPanePortionGridPane.addRow(0, searchTypeHBox, maxResultsHBox, searchModel.getResultsTypeComboBox(), searchButton);
		topPanelVBox.getChildren().add(staticTopPanePortionGridPane);

		searchModel.initializeCriteriaPane(maxResultsHBox, searchModel.getResultsTypeComboBox(), searchModel.getSearchResultsTable());
		topPanelVBox.getChildren().add(SearchModel.getSearchTypeSelector().getResultsPane());
	}

	public EnhancedSavedSearch getSearchSaver() { return searchSaver; }
	
	private void initializeMaxResultsComboBox() {
		maxResultsHBox = new HBox(5);
		maxResultsHBox.setAlignment(Pos.CENTER);

		maxResultsCustomTextFieldLabel = new Label("Max Results");
		maxResultsHBox.getChildren().add(maxResultsCustomTextFieldLabel);
		maxResultsHBox.getChildren().add(searchModel.getMaxResultsCustomTextField());
	}

	private void initializeSearchButton() {
		searchButton.requestFocus();
		searchButton.disableProperty().bind(SearchModel.isSearchRunnableProperty().not());
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

