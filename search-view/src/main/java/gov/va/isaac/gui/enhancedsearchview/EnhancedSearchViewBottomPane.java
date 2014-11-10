package gov.va.isaac.gui.enhancedsearchview;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.enhancedsearchview.model.EnhancedSavedSearch;
import gov.va.isaac.gui.enhancedsearchview.model.SearchModel;
import gov.va.isaac.gui.enhancedsearchview.resulthandler.ResultsToDrools;
import gov.va.isaac.gui.enhancedsearchview.resulthandler.ResultsToRefset;
import gov.va.isaac.gui.enhancedsearchview.resulthandler.ResultsToReport;
import gov.va.isaac.gui.enhancedsearchview.resulthandler.ResultsToTaxonomy;
import gov.va.isaac.gui.enhancedsearchview.resulthandler.ResultsToWorkflow;
import gov.va.isaac.interfaces.gui.constants.SharedServiceNames;
import gov.va.isaac.interfaces.gui.views.DockedViewI;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.ListBatchViewI;
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
import javafx.stage.Stage;

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
	private Button resultsToSememeButton = new Button("Sememe");
	private Button resultsToDroolsButton = new Button("Drools");
	
	private Button resetDefaultsButton;

	private VBox labelsVBox;
	private Label totalResultsSelectedLabel;
	private Label totalResultsReturnedLabel;

	private VBox saveSearchContainerVBox;

	private SearchModel searchModel = new SearchModel();

	private Font boldFont = new Font("System Bold", 13.0);
	private BorderStroke borderStroke = new BorderStroke(Paint.valueOf("BLACK"), BorderStrokeStyle.SOLID, new CornerRadii(10), new BorderWidths(1), new Insets(5));

	public EnhancedSearchViewBottomPane(Stage stage) {
		bottomPanelGridPane = new GridPane();
		bottomPanelGridPane.setHgap(30);
		bottomPanelGridPane.setAlignment(Pos.CENTER);
		bottomPanelGridPane.setPadding(new Insets(15, 30, 15, 30));
		
		// Handle Result Labels
		initializeResultLabels();

		// Handle Results
		initializeResultsOptions(stage);
		
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
		saveSearchContainerVBox.setPadding(new Insets(10));
		saveSearchContainerVBox.setBorder(new Border(borderStroke ));

		Label saveSearchLabel = new Label("Restore Searches");
		saveSearchLabel.setFont(boldFont);
		
		HBox saveSearchHBox = new HBox(15);
		saveSearchHBox.setAlignment(Pos.CENTER);

		saveSearchHBox.getChildren().add(savedSearch.getSavedSearchesComboBox());
		saveSearchHBox.getChildren().add(savedSearch.getRestoreSearchButton());
		saveSearchContainerVBox.getChildren().add(saveSearchLabel);
		saveSearchContainerVBox.getChildren().add(saveSearchHBox);
	}

	public void refreshBottomPanel() {
		if (searchModel.getSearchResultsTable().getResults().getItems().size() == 0) {
			disableButtons(true);		
		} else {
			disableButtons(false);		
			if (searchModel.getSearchResultsTable().getResults().getItems().size() == 1) {
				totalResultsReturnedLabel.setText(searchModel.getSearchResultsTable().getResults().getItems().size() + " entry displayed");
			} else {
				totalResultsReturnedLabel.setText(searchModel.getSearchResultsTable().getResults().getItems().size() + " entries displayed");
			}
		}

		disableButtons(false);		
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
		resetDefaultsButton = new Button("Reset Display Table");
		resetDefaultsButton.setPrefWidth(Control.USE_COMPUTED_SIZE);
		resetDefaultsButton.setMinWidth(Control.USE_PREF_SIZE);
		resetDefaultsButton.setOnAction((e) -> resetDefaults());
	}

	private void initializeResultsOptions(Stage stage) {
		initializeButtons(stage);
		ResultsToTaxonomy.initializeTaxonomyPanel();

		resultsVBox = new VBox(15);
		resultsVBox.setAlignment(Pos.CENTER);
		resultsVBox.setPadding(new Insets(10));
		resultsVBox.setBorder(new Border(borderStroke));
		
		Label resultsLabel = new Label("Handle Results");
		resultsLabel.setFont(boldFont);
		
		HBox resultsButtonHBox = new HBox(15);
		resultsButtonHBox.setAlignment(Pos.CENTER);
		resultsButtonHBox.getChildren().add(resultsToReportButton);
		resultsButtonHBox.getChildren().add(resultsToListButton);
		resultsButtonHBox.getChildren().add(resultsToWorkflowButton);
		resultsButtonHBox.getChildren().add(resultsToTaxonomyButton);
		resultsButtonHBox.getChildren().add(resultsToSememeButton); 
		resultsButtonHBox.getChildren().add(resultsToDroolsButton); 

		resultsVBox.getChildren().add(resultsLabel);
		resultsVBox.getChildren().add(resultsButtonHBox);
	}

	private void initializeButtons(Stage stage) {
		// TODO: either fix or remove exportSearchResultsToWorkflow
		resultsToListButton.setOnAction((e) -> resultsToList());
		resultsToReportButton.setOnAction((e) -> ResultsToReport.resultsToReport());
		resultsToWorkflowButton.setOnAction((e) -> ResultsToWorkflow.multipleResultsToWorkflow());
		resultsToTaxonomyButton.setOnAction((e) -> ResultsToTaxonomy.resultsToSearchTaxonomy());
		resultsToSememeButton.setOnAction((e) -> createSememe(stage));
		resultsToDroolsButton.setOnAction((e) -> ResultsToDrools.createDroolsOnClipboard());
		
		resultsToListButton.setPrefWidth(Control.USE_COMPUTED_SIZE);
		resultsToReportButton.setPrefWidth(Control.USE_COMPUTED_SIZE);
		resultsToWorkflowButton.setPrefWidth(Control.USE_COMPUTED_SIZE);
		resultsToTaxonomyButton.setPrefWidth(Control.USE_COMPUTED_SIZE);
		resultsToSememeButton.setPrefWidth(Control.USE_COMPUTED_SIZE);
		resultsToDroolsButton.setPrefWidth(Control.USE_COMPUTED_SIZE);

		resultsToListButton.setMinWidth(Control.USE_PREF_SIZE);
		resultsToReportButton.setMinWidth(Control.USE_PREF_SIZE);
		resultsToWorkflowButton.setMinWidth(Control.USE_PREF_SIZE);
		resultsToTaxonomyButton.setMinWidth(Control.USE_PREF_SIZE);
		resultsToSememeButton.setMinWidth(Control.USE_PREF_SIZE);
		resultsToDroolsButton.setMinWidth(Control.USE_PREF_SIZE);
		disableButtons(true);	
}


	private void createSememe(Stage stage) {
		try {
			String refexName = ResultsToRefset.resultsToRefset(stage, searchModel.getSearchResultsTable().getResults());
			
			if (refexName != null) {
				AppContext.getCommonDialogs().showInformationDialog("Sememe Successfully Created", "Created and populated new Sememe (" + refexName + ") with all values in results table");
			}
		} catch (Exception e) {
			AppContext.getCommonDialogs().showErrorDialog("Sememe Creation Failure", "Sememe Creation Failure", "Failed to create and populate Sememe with values in results table"); 
		}
	}

	private void resultsToList() {
		ListBatchViewI lv = AppContext.getService(ListBatchViewI.class, SharedServiceNames.DOCKED);
		AppContext.getMainApplicationWindow().ensureDockedViewIsVisble((DockedViewI) lv);

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

		searchModel.getSearchResultsTable().initializeSearchResultsTable(searchModel.getSearchTypeSelector().getCurrentType(), searchModel.getResultsTypeComboBox().getSelectionModel().getSelectedItem());
	}

	private void disableButtons(boolean val) {
		resultsToReportButton.setDisable(val);
		resultsToListButton.setDisable(val);
		resultsToWorkflowButton.setDisable(val);
		resultsToTaxonomyButton.setDisable(val);
		resultsToSememeButton.setDisable(val);
		resultsToDroolsButton.setDisable(val);
	}

}

