package gov.va.isaac.gui.enhancedsearchview;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.enhancedsearchview.model.EnhancedSavedSearch;
import gov.va.isaac.gui.enhancedsearchview.model.SearchModel;
import gov.va.isaac.gui.enhancedsearchview.resulthandler.ResultsToReport;
import gov.va.isaac.gui.enhancedsearchview.resulthandler.ResultsToTaxonomy;
import gov.va.isaac.gui.enhancedsearchview.resulthandler.ResultsToWorkflow;
import gov.va.isaac.interfaces.gui.views.ListBatchViewI;
import gov.va.isaac.search.CompositeSearchResult;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnhancedSearchViewBottomPane {
	private static final Logger LOG = LoggerFactory.getLogger(EnhancedSearchViewBottomPane.class);

	private GridPane bottomPanelGridPane;

	private VBox resultsVBox;
	private Button resultsToReportButton = new Button("Report");
	private Button resultsToListButton = new Button("List");
	private Button resultsToWorkflowButton = new Button("Workflow");
	private Button resultsToTaxonomyButton = new Button("Taxonomy");
	
	private Button resetDefaultsButton;

	private VBox labelsVBox;
	private Label totalResultsSelectedLabel;
	private Label totalResultsReturnedLabel;

	private VBox saveSearchContainerVBox;

	private SearchModel searchModel = new SearchModel();

	private Font boldFont = new Font("System Bold", 13.0);
	private BorderStroke borderStroke = new BorderStroke(Paint.valueOf("BLACK"), BorderStrokeStyle.SOLID, new CornerRadii(10), new BorderWidths(1), new Insets(5));

	public EnhancedSearchViewBottomPane() {
		bottomPanelGridPane = new GridPane();
		bottomPanelGridPane.setHgap(30);
		bottomPanelGridPane.setAlignment(Pos.CENTER);
		bottomPanelGridPane.setPadding(new Insets(15, 30, 15, 30));
		
		// Handle Result Labels
		initializeResultLabels();

		// Handle Results
		initializeResultsOptions();
		
		// Handle Save Search 
		initializeSaveSearchOptions();
		
		initializeDefaultOptions();

		bottomPanelGridPane.setConstraints(labelsVBox,  0,  0,  1,  1,  HPos.LEFT,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);
		bottomPanelGridPane.setConstraints(resultsVBox,  1,  0,  1,  1,  HPos.CENTER,  VPos.CENTER, Priority.ALWAYS, Priority.ALWAYS);
		bottomPanelGridPane.setConstraints(saveSearchContainerVBox,  2,  0,  1,  1,  HPos.CENTER,  VPos.CENTER, Priority.ALWAYS, Priority.ALWAYS);
		bottomPanelGridPane.setConstraints(resetDefaultsButton,  3,  0,  1,  1,  HPos.RIGHT,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);

		bottomPanelGridPane.addRow(0, labelsVBox, resultsVBox, saveSearchContainerVBox, resetDefaultsButton);
	}

	private void initializeResultLabels() {
		totalResultsReturnedLabel = new Label();
		totalResultsReturnedLabel.setFont(boldFont);
		totalResultsReturnedLabel.setPrefWidth(Control.USE_COMPUTED_SIZE);
		totalResultsReturnedLabel.setMinWidth(Control.USE_PREF_SIZE);
		
		totalResultsSelectedLabel = new Label();
		totalResultsSelectedLabel.setFont(boldFont);
		totalResultsSelectedLabel.setPrefWidth(Control.USE_COMPUTED_SIZE);
		totalResultsSelectedLabel.setMinWidth(Control.USE_PREF_SIZE);

		labelsVBox = new VBox(15);
		labelsVBox.getChildren().add(totalResultsReturnedLabel);
		labelsVBox.getChildren().add(totalResultsSelectedLabel);
	}

	private void initializeSaveSearchOptions() {
		EnhancedSavedSearch savedSearch = new EnhancedSavedSearch();
		saveSearchContainerVBox = new VBox(15);
		saveSearchContainerVBox.setAlignment(Pos.CENTER);
		saveSearchContainerVBox.setBorder(new Border(borderStroke ));
		
		Label saveSearchLabel = new Label("Save/Restore Searches");
		saveSearchLabel.setFont(boldFont);
		
		HBox saveSearchHBox = new HBox(15);
		saveSearchHBox.setAlignment(Pos.CENTER);

//		saveSearchLabel.setPadding(new Insets(5,0,0,0));
//		savedSearch.getSavedSearchesComboBox().setPadding(new Insets(0, 0, 5, 5));
//		savedSearch.getSaveButton().setPadding(new Insets(0, 5, 5, 0));

		saveSearchHBox.getChildren().add(savedSearch.getSavedSearchesComboBox());
		saveSearchHBox.getChildren().add(savedSearch.getSaveButton());
		saveSearchContainerVBox.getChildren().add(saveSearchLabel);
		saveSearchContainerVBox.getChildren().add(saveSearchHBox);
	}

	public void refreshBottomPanel() {
		if (searchModel.getSearchResultsTable().getResults().getItems().size() == 1) {
			totalResultsReturnedLabel.setText(searchModel.getSearchResultsTable().getResults().getItems().size() + " entry displayed");
		} else {
			totalResultsReturnedLabel.setText(searchModel.getSearchResultsTable().getResults().getItems().size() + " entries displayed");
		}

		disableButtons(true);		
	}


	public void refreshTotalResultsSelectedLabel() {
		int numSelected = searchModel.getSearchResultsTable().getResults().getSelectionModel().getSelectedIndices().size();
		switch (numSelected) {
		case 0:
			totalResultsSelectedLabel.setText("No results selected");
			break;
		case 1:
			totalResultsSelectedLabel.setText(numSelected + " result selected");
			break;

		default:
			totalResultsSelectedLabel.setText(numSelected + " results selected");
			break;
		}

	}

	private void initializeDefaultOptions() {
		resetDefaultsButton = new Button("Reset Defaults");
		resetDefaultsButton.setPrefWidth(Control.USE_COMPUTED_SIZE);
		resetDefaultsButton.setMinWidth(Control.USE_PREF_SIZE);
		resetDefaultsButton.setOnAction((e) -> resetDefaults());
	}

	private void initializeResultsOptions() {
		initializeButtons();
		ResultsToTaxonomy.initializeTaxonomyPanel();

		resultsVBox = new VBox(15);
		resultsVBox.setAlignment(Pos.CENTER);
		resultsVBox.setBorder(new Border(borderStroke));
		
		Label resultsLabel = new Label("Handle Results");
		resultsLabel.setFont(boldFont);
		
		HBox resultsButtonHBox = new HBox(15);
		resultsButtonHBox.setAlignment(Pos.CENTER);
		resultsButtonHBox.getChildren().add(resultsToReportButton);
		resultsButtonHBox.getChildren().add(resultsToListButton);
		resultsButtonHBox.getChildren().add(resultsToWorkflowButton);
		resultsButtonHBox.getChildren().add(resultsToTaxonomyButton);
		
//		resultsLabel.setPadding(new Insets(5,0,0,0));
//		resultsToReportButton.setPadding(new Insets(0, 0, 5, 5));
//		resultsToTaxonomyButton.setPadding(new Insets(0, 5, 5, 0));

		resultsVBox.getChildren().add(resultsLabel);
		resultsVBox.getChildren().add(resultsButtonHBox);
	}

	private void initializeButtons() {
		// TODO: either fix or remove exportSearchResultsToWorkflow
		resultsToListButton.setOnAction((e) -> resultsToList());
		resultsToReportButton.setOnAction((e) -> ResultsToReport.resultsToReport());
		resultsToWorkflowButton.setOnAction((e) -> ResultsToWorkflow.multipleResultsToWorkflow());
		resultsToTaxonomyButton.setOnAction((e) -> ResultsToTaxonomy.resultsToSearchTaxonomy());
		
		resultsToListButton.setPrefWidth(Control.USE_COMPUTED_SIZE);
		resultsToReportButton.setPrefWidth(Control.USE_COMPUTED_SIZE);
		resultsToWorkflowButton.setPrefWidth(Control.USE_COMPUTED_SIZE);
		resultsToTaxonomyButton.setPrefWidth(Control.USE_COMPUTED_SIZE);

		resultsToListButton.setMinWidth(Control.USE_PREF_SIZE);
		resultsToReportButton.setMinWidth(Control.USE_PREF_SIZE);
		resultsToWorkflowButton.setMinWidth(Control.USE_PREF_SIZE);
		resultsToTaxonomyButton.setMinWidth(Control.USE_PREF_SIZE);

		disableButtons(true);	
}


	private void resultsToList() {
		ListBatchViewI lv = AppContext.getService(ListBatchViewI.class);

		AppContext.getMainApplicationWindow().ensureDockedViewIsVisble(lv);

		List<Integer> nids = new ArrayList<>();
		for (CompositeSearchResult result : searchModel.getSearchResultsTable().getResults().getItems()) {
			if (! nids.contains(result.getContainingConcept().getNid())) {
				nids.add(result.getContainingConcept().getNid());
			}
		}

		lv.addConcepts(nids);
	}


	public GridPane getBottomPaneHBox() {
		return bottomPanelGridPane;
	}
	private void resetDefaults() {

		searchModel.getMaxResultsCustomTextField().setText("");
		searchModel.getSearchResultsTable().initializeSearchResultsTable(searchModel.getResultsTypeComboBox().getSelectionModel().getSelectedItem());
		disableButtons(true);		
	}

	private void disableButtons(boolean val) {
		resultsToReportButton.setDisable(val);
		resultsToListButton.setDisable(val);
		resultsToWorkflowButton.setDisable(val);
		resultsToTaxonomyButton.setDisable(val);
	}

}

