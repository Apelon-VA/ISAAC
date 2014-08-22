/**
 * Copyright Notice
 * 
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.gui.enhancedsearchview;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.ConceptNode;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerHelper;
import gov.va.isaac.gui.dragAndDrop.DragRegistry;
import gov.va.isaac.gui.dragAndDrop.SingleConceptIdProvider;
import gov.va.isaac.gui.enhancedsearchview.SearchConceptHelper.SearchConceptException;
import gov.va.isaac.gui.enhancedsearchview.filters.IsDescendantOfFilter;
import gov.va.isaac.gui.enhancedsearchview.filters.LuceneSearchTypeFilter;
import gov.va.isaac.gui.enhancedsearchview.filters.NonSearchTypeFilter;
import gov.va.isaac.gui.enhancedsearchview.filters.RegExpSearchTypeFilter;
import gov.va.isaac.gui.enhancedsearchview.filters.SearchTypeFilter;
import gov.va.isaac.gui.enhancedsearchview.searchresultsfilters.SearchResultsFilterFactory;
import gov.va.isaac.interfaces.gui.views.ListBatchViewI;
import gov.va.isaac.interfaces.workflow.ConceptWorkflowServiceI;
import gov.va.isaac.interfaces.workflow.ProcessInstanceCreationRequestI;
import gov.va.isaac.search.CompositeSearchResult;
import gov.va.isaac.search.CompositeSearchResultComparator;
import gov.va.isaac.search.DescriptionAnalogBITypeComparator;
import gov.va.isaac.search.SearchBuilder;
import gov.va.isaac.search.SearchHandle;
import gov.va.isaac.search.SearchHandler;
import gov.va.isaac.search.SearchResultsFilter;
import gov.va.isaac.search.SearchResultsFilterException;
import gov.va.isaac.search.SearchResultsIntersectionFilter;
import gov.va.isaac.util.CommonMenus;
import gov.va.isaac.util.CommonMenusDataProvider;
import gov.va.isaac.util.CommonMenusNIdProvider;
import gov.va.isaac.util.TaskCompleteCallback;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.WBUtility;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.math.RoundingMode;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Callback;

import org.apache.mahout.math.Arrays;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.description.DescriptionAnalogBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * EnhancedSearchViewController
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
public class EnhancedSearchViewController implements TaskCompleteCallback {
	private static final Logger LOG = LoggerFactory.getLogger(EnhancedSearchViewController.class);

	enum Tasks {
		SEARCH,
		WORKFLOW_EXPORT
	}

	enum AggregationType {
		CONCEPT("Concept"),
		DESCRIPTION("Description");

		private final String display;

		private AggregationType(String display) {
			this.display = display;
		}

		@Override
		public String toString() {
			return display;
		}
	}

	enum SearchType {
		LUCENE("Lucene"),
		REGEXP("RegExp");

		private final String display;

		private SearchType(String display) {
			this.display = display;
		}

		public String toString() { return display; }
	}

	@FXML private HBox maxResultsHBox;
	@FXML private Label maxResultsCustomTextFieldLabel;
	private IntegerField maxResultsCustomTextField;

	@FXML private Button saveSearchButton;
	@FXML private ComboBox<SearchDisplayConcept> savedSearchesComboBox;
	@FXML private Button searchButton;

	@FXML private Label totalResultsDisplayedLabel;
	@FXML private Pane pane;
	@FXML private ComboBox<AggregationType> aggregationTypeComboBox;
	@FXML private TableView<CompositeSearchResult> searchResultsTable;
	@FXML private Button exportSearchResultsAsTabDelimitedValuesButton;
	@FXML private Button exportSearchResultsToListBatchViewButton;
	@FXML private Button exportSearchResultsToWorkflowButton;
	@FXML private ProgressIndicator searchProgress;
	@FXML private Label totalResultsSelectedLabel;
	@FXML private Button resetDefaultsButton;
	@FXML private Button addIsDescdantOfFilterButton;

	@FXML private HBox searchTypeControlsHbox;
	@FXML private ComboBox<SearchType> searchTypeComboBox;

	//@FXML private ListView<DisplayableFilter> searchFilterListView;
	@FXML private GridPane searchFilterGridPane;

	@FXML private TextField searchSaveNameTextField;
	@FXML private TextField searchSaveDescriptionTextField;
	@FXML private TextField droolsExprTextField;

	final private SearchViewModel searchViewModel = new SearchViewModel();

	private final BooleanProperty searchRunning = new SimpleBooleanProperty(false);
	private SearchHandle ssh = null;

	private Window windowForTableViewExportDialog;

	ConceptWorkflowServiceI conceptWorkflowService;

	public static EnhancedSearchViewController init() throws IOException {
		// Load FXML
		URL resource = EnhancedSearchViewController.class.getResource("EnhancedSearchView.fxml");
		LOG.debug("FXML for " + EnhancedSearchViewController.class + ": " + resource);
		FXMLLoader loader = new FXMLLoader(resource);
		loader.load();
		return loader.getController();
	}

	@FXML
	public void initialize() {
		assert searchButton != null : "fx:id=\"searchButton\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		//assert searchText != null : "fx:id=\"searchText\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert pane != null : "fx:id=\"pane\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert exportSearchResultsToListBatchViewButton != null : "fx:id=\"exportSearchResultsToListBatchViewButton\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert exportSearchResultsToWorkflowButton != null : "fx:id=\"exportSearchResultsToWorkflowButton\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert totalResultsSelectedLabel != null : "fx:id=\"totalResultsSelectedLabel\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert resetDefaultsButton != null : "fx:id=\"resetDefaultsButton\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert maxResultsHBox != null : "fx:id=\"maxResultsHBox\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert maxResultsCustomTextFieldLabel != null : "fx:id=\"maxResultsCustomTextFieldLabel\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert saveSearchButton != null : "fx:id=\"saveSearchButton\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert savedSearchesComboBox != null : "fx:id=\"savedSearchesComboBox\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert addIsDescdantOfFilterButton != null : "fx:id=\"addIsDescdantOfFilterButton\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert searchFilterGridPane != null : "fx:id=\"searchFilterGridPane\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert searchTypeComboBox != null : "fx:id=\"searchTypeComboBox\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert searchTypeControlsHbox != null : "fx:id=\"searchTypeControlsHbox\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";

		String styleSheet = EnhancedSearchViewController.class.getResource("/isaac-shared-styles.css").toString();
		if (! pane.getStylesheets().contains(styleSheet)) {
			pane.getStylesheets().add(styleSheet);
		}

		if (searchSaveNameTextField == null) {
			searchSaveNameTextField = new TextField();
		}
		if (searchSaveDescriptionTextField == null) {
			searchSaveDescriptionTextField = new TextField();
		}
		if (droolsExprTextField == null) {
			droolsExprTextField = new TextField();
		}

		initializeSearchTypeComboBox();

		addIsDescdantOfFilterButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				addSearchFilter(new IsDescendantOfFilter());
			}
		});

		initializeWorkflowServices();

		//final BooleanProperty searchTextValid = new SimpleBooleanProperty(false);
		//searchButton.disableProperty().bind(searchTextValid.not());
		searchProgress.visibleProperty().bind(searchRunning);

		maxResultsCustomTextFieldLabel.setText("Max Results");
		maxResultsCustomTextField = new IntegerField();
		maxResultsCustomTextField.setMaxWidth(50);
		ObservableList<Node> hBoxChildren = maxResultsHBox.getChildren();
		hBoxChildren.add(maxResultsCustomTextField);

		initializeSearchViewModel();

		// Search results table
		initializeSearchResultsTable();
		initializeAggregationTypeComboBox();
		initializeSavedSearchComboBox();

		exportSearchResultsAsTabDelimitedValuesButton.setOnAction((e) -> exportSearchResultsAsTabDelimitedValues());
		exportSearchResultsToListBatchViewButton.setOnAction((e) -> exportSearchResultsToListBatchView());
		exportSearchResultsToWorkflowButton.setOnAction((e) -> exportSearchResultsToWorkflow());
		resetDefaultsButton.setOnAction((e) -> resetDefaults());

		saveSearchButton.setOnAction((action) -> {
			// TODO: Create BooleanProperty and bind to saveSearchButton to disable
			Object buttonCellObject = savedSearchesComboBox.valueProperty().getValue();
			if (buttonCellObject != null && (buttonCellObject instanceof String)) {
				saveSearch(); 
			}
		});

		searchButton.setOnAction((action) -> {
			if (searchRunning.get() && ssh != null) {
				ssh.cancel();
			} else {
				search();
			}
		});
		searchRunning.addListener((observable, oldValue, newValue) -> {
			if (searchRunning.get()) {
				searchButton.setText("Cancel");
			} else {
				searchButton.setText("Search");
			}
		});
	}

	private void initializeSearchViewModel() {
		searchViewModel.setViewCoordinate(WBUtility.getViewCoordinate());

		refreshSearchViewModelBindings();
	}

	private void refreshSearchViewModelBindings() {
		Bindings.bindBidirectional(searchSaveNameTextField.textProperty(), searchViewModel.getNameProperty());
		Bindings.bindBidirectional(searchSaveDescriptionTextField.textProperty(), searchViewModel.getDescriptionProperty());

		Bindings.bindBidirectional(maxResultsCustomTextField.valueProperty(), searchViewModel.getMaxResultsProperty());

		Bindings.bindBidirectional(droolsExprTextField.textProperty(), searchViewModel.getDroolsExprProperty());
	}

	/*
	 * This method adds new DisplayableFilter to both GridPane and searchViewModel,
	 * if DisplayableFilter not already in searchViewModel
	 * 
	 * It also adds a Remove button that removes the specified DisplayableFilter from
	 * both the GridPane and the searchViewModel
	 * 
	 */
	private void addSearchFilter(NonSearchTypeFilter filter) {
		int index = searchFilterGridPane.getChildren().size();

		HBox row = new HBox();
		HBox.setMargin(row, new Insets(5, 5, 5, 5));
		row.setUserData(filter);
		if (! searchViewModel.getFilters().contains(filter)) {
			searchViewModel.getFilters().add(filter);
		}

		// TODO: add binding to disable deletion of first filter in list containing other filter
		Button removeFilterButton = new Button("Remove");
		removeFilterButton.setMinWidth(55);
		removeFilterButton.setPadding(new Insets(5.0));
		removeFilterButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				// Create temp save list of nodes from searchFilterGridPane
				List<Node> newNodes = new ArrayList<>(searchFilterGridPane.getChildren());

				// Remove this node from temp save list of nodes
				newNodes.remove(row);

				// Remove this filter from searchViewModel
				searchViewModel.getFilters().remove(filter);
				LOG.debug("searchViewModel should no longer contain filter " + filter + ": " + Arrays.toString(searchViewModel.getFilters().toArray()));

				// Remove all nodes from searchFilterGridPane
				searchFilterGridPane.getChildren().clear();

				// Recreate and add each node to searchFilterGridPane
				for (int i = 0; i < newNodes.size(); ++i) {
					addSearchFilter((NonSearchTypeFilter)newNodes.get(i).getUserData());
				}
			}
		});
		row.getChildren().add(removeFilterButton);

		if (filter instanceof IsDescendantOfFilter) {
			IsDescendantOfFilter displayableIsDescendantOfFilter = (IsDescendantOfFilter)filter;

			Label searchParamLabel = new Label("Ascendant");
			searchParamLabel.setPadding(new Insets(5.0));
			searchParamLabel.setMinWidth(70);

			CheckBox excludeMatchesCheckBox = new CheckBox("Exclude Matches");
			excludeMatchesCheckBox.setPadding(new Insets(5.0));
			excludeMatchesCheckBox.setMinWidth(150);
			excludeMatchesCheckBox.setSelected(((IsDescendantOfFilter) filter).getInvert());
			excludeMatchesCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(
						ObservableValue<? extends Boolean> observable,
						Boolean oldValue,
						Boolean newValue) {
					((IsDescendantOfFilter) filter).setInvert(newValue);
				}});

			final ConceptNode cn = new ConceptNode(null, false);
			cn.setPromptText("Type, drop or select a concept to add");
			//HBox.setHgrow(cn.getNode(), Priority.SOMETIMES);
			//HBox.setMargin(cn.getNode(), new Insets(5, 5, 5, 5));

			cn.getConceptProperty().addListener(new ChangeListener<ConceptVersionBI>()
					{
				@Override
				public void changed(ObservableValue<? extends ConceptVersionBI> observable, ConceptVersionBI oldValue, ConceptVersionBI newValue)
				{
					if (newValue != null)
					{
						displayableIsDescendantOfFilter.setNid(newValue.getConceptNid());
						LOG.debug("isDescendantFilter should now contain concept with NID " + displayableIsDescendantOfFilter.getNid() + ": " + Arrays.toString(searchViewModel.getFilters().toArray()));
					}
				}
					});
			if (filter.isValid()) {
				cn.set(WBUtility.getConceptVersion(((IsDescendantOfFilter) filter).getNid()));
			}

			row.getChildren().addAll(searchParamLabel, cn.getNode(), excludeMatchesCheckBox);
		} 
		else {
			String msg = "Failed creating DisplayableFilter GridPane cell for filter of unsupported type " + filter.getClass().getName();
			LOG.error(msg);
			throw new RuntimeException(msg);
		}

		searchFilterGridPane.addRow(index, row);
		RowConstraints rowConstraints = new RowConstraints();
		rowConstraints.setVgrow(Priority.NEVER);
		searchFilterGridPane.getRowConstraints().add(index, rowConstraints);
	}

	private boolean validateSearchViewModel(SearchViewModel model) {
		return validateSearchViewModel(model, null);
	}

	private boolean validateSearchViewModel(SearchViewModel model, String errorDialogTitle) {
		if (model.getSearchType() == null) {
			String details = "No SearchTypeFilter specified: " + model;
			LOG.warn("Invalid search model (name=" + model.getName() + "). " + details);

			if (errorDialogTitle != null) {
				AppContext.getCommonDialogs().showErrorDialog(errorDialogTitle, errorDialogTitle, details, AppContext.getMainApplicationWindow().getPrimaryStage());
			}

			return false;
		} else if (model.getViewCoordinate() == null) {
			String details = "View coordinate is null: " + model;
			LOG.warn("Invalid search model (name=" + model.getName() + "). " + details);

			if (errorDialogTitle != null) {
				AppContext.getCommonDialogs().showErrorDialog(errorDialogTitle, errorDialogTitle, details, AppContext.getMainApplicationWindow().getPrimaryStage());
			}

			return false;
		}
		else if (model.getInvalidFilters().size() > 0) {
			String details = "Found " + model.getInvalidFilters().size() + " invalid filter: " + Arrays.toString(model.getFilters().toArray());
			LOG.warn("Invalid filter in search model (name=" + model.getName() + "). " + details);

			if (errorDialogTitle != null) {
				AppContext.getCommonDialogs().showErrorDialog(errorDialogTitle, errorDialogTitle, details, AppContext.getMainApplicationWindow().getPrimaryStage());
			}

			return false;
		}

		return true;
	}

	private void loadSavedSearch(SearchDisplayConcept displayConcept) {
		LOG.info("loadSavedSearch(" + displayConcept + ")");

		SearchViewModel model = null;
		try {
			model = SearchConceptHelper.loadSavedSearch(displayConcept);
			
		} catch (SearchConceptException e) {
			LOG.error("Failed loading saved search. Caught " + e.getClass().getName() + " \"" + e.getLocalizedMessage() + "\"");
			e.printStackTrace();

			String title = "Failed loading saved search";
			String msg = "Cannot load existing saved search \"" + displayConcept + "\"";
			String details = "Caught " + e.getClass().getName() + " \"" + e.getLocalizedMessage() + "\"." + "\n" + "model:" + model;
			AppContext.getCommonDialogs().showErrorDialog(title, msg, details, AppContext.getMainApplicationWindow().getPrimaryStage());

			return;
		}

		if (model != null) {
			if (! validateSearchViewModel(model, "Failed loading saved search " + displayConcept)) {
				return;
			} else {

				searchViewModel.copy(model);
				initializeSearchTypeComboBox(searchViewModel.getSearchType());
				refreshSearchViewModelBindings();
				
				searchFilterGridPane.getChildren().clear();

				for (NonSearchTypeFilter<? extends NonSearchTypeFilter<?>> filter : model.getFilters()) {
					addSearchFilter(filter);
				}

				//this.currentSearchViewCoordinate = model.getViewCoordinate();

				// TODO: set drools expression somewhere

				LOG.debug("loadSavedSearch() loaded model: " + model);

				return;
			}
		} else {
			LOG.error("Failed loading saved search " + displayConcept);
			return;
		}
	}

	//	private void displaySaveSearchPopup() {
	//		// New stage to popup blocking dialog
	//		Stage saveSearchPopupStage = new Stage();
	//		saveSearchPopupStage.initModality(Modality.WINDOW_MODAL);
	//		saveSearchPopupStage.initOwner(getRoot().getScene().getWindow());
	//		
	//		HBox descriptionEntryDialogVbox = new HBox();
	//		descriptionEntryDialogVbox.getChildren().add(new Label("Description for search \"" + nameToSave + "\""));
	//		descriptionEntryDialogVbox.getChildren().add(saveSearchDescriptionTextField);
	//		Button saveButton = new Button("Save");
	//		saveButton.setOnAction((action) -> {
	//			saveSearch();
	//		});
	//		descriptionEntryDialogVbox.getChildren().add(saveButton);
	//		saveSearchPopupStage.setScene(new Scene(new Label("banana")));
	//		saveSearchPopupStage.show();
	//	}

	//	public void showSaveSearchDialogView(Stage primaryStage) throws IOException {
	//		Parent root = FXMLLoader.load(EnhancedSearchViewController.class.getResource("SaveSearchDialogView.fxml"));
	//		primaryStage.initModality(Modality.APPLICATION_MODAL); // 1 Add one
	//		Scene scene = new Scene(root);		
	//		primaryStage.setScene(scene);
	//		primaryStage.initOwner(primaryStage.getScene().getWindow());// 2 Add two
	//		primaryStage.show();
	//	}

	private void saveSearch() {
		LOG.debug("saveSearch() called.  Search specified: " + savedSearchesComboBox.valueProperty().getValue());

		Object valueAsObject = savedSearchesComboBox.valueProperty().getValue();

		if (valueAsObject != null) {
			SearchDisplayConcept existingSavedSearch = null;
			String specifiedDescription = null;

			if (valueAsObject instanceof SearchDisplayConcept) {
				existingSavedSearch = (SearchDisplayConcept)valueAsObject;
				specifiedDescription = existingSavedSearch.getFullySpecifiedName();
			} else if (valueAsObject instanceof String && ((String)valueAsObject).length() > 0) {
				specifiedDescription = (String)valueAsObject;
				for (SearchDisplayConcept saveSearchInComboBoxList : savedSearchesComboBox.getItems()) {
					if (saveSearchInComboBoxList != null && valueAsObject.equals(saveSearchInComboBoxList.getFullySpecifiedName())) {
						existingSavedSearch = saveSearchInComboBoxList;
						break;
					}
				}
			} else {
				String title = "Failed saving search";
				String msg = "Unsupported valueProperty value type in savedSearchesComboBox: " + valueAsObject.getClass().getName();
				String details = "Must either select or specify search name in order to save search and valueProperty must be either of type String or SimpleDisplayConcept";
				LOG.error(title + ". " + msg + details);
				AppContext.getCommonDialogs().showErrorDialog(title, msg, details, AppContext.getMainApplicationWindow().getPrimaryStage());

				return;
			}

			if (existingSavedSearch != null) {
				final String nameToSave = existingSavedSearch.getFullySpecifiedName();

				LOG.debug("saveSearch(): modifying existing saved search: " + existingSavedSearch + " (nid=" + existingSavedSearch.getNid() + ")");

				// TODO: remove this when modification/replacement is implemented
				String title = "Failed saving search";
				String msg = "Cannot modify existing saved search \"" + nameToSave + "\"";
				String details = "Modification or replacement of existing saves is not currently supported";
				AppContext.getCommonDialogs().showErrorDialog(title, msg, details, AppContext.getMainApplicationWindow().getPrimaryStage());

				return;
			} else {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd @ HH:mm:ss");
				LocalDateTime dateTime = LocalDateTime.now();
				String formattedDateTime = dateTime.format(formatter);
				final String nameToSave = specifiedDescription + " by " + System.getProperty("user.name") + " on " + formattedDateTime;
				//final String specifiedDescriptionToSave = specifiedDescription;

				// Save Search popup
				Popup saveSearchPopup = new Popup();
				//final TextField saveSearchPopupFullySpecifiedNameTextField = new TextField();
				//final TextField saveSearchPopupPreferredTermDescriptionTextField = new TextField();
				Button saveSearchPopupSaveButton = new Button("Save");
				Button saveSearchPopupCancelButton = new Button("Cancel");

				searchSaveNameTextField.setText(nameToSave);
				searchSaveNameTextField.setDisable(true);

				saveSearchPopupCancelButton.setOnAction(new EventHandler<ActionEvent>() {
					@Override public void handle(ActionEvent event) {
						//searchSaveNameTextField.clear();
						//saveSearchPopupPreferredTermDescriptionTextField.clear();
						saveSearchPopup.hide();
					}
				});

				saveSearchPopupSaveButton.setOnAction(new EventHandler<ActionEvent>() {
					@Override public void handle(ActionEvent event) {
						doSaveSearch();
						searchSaveNameTextField.clear();
						searchSaveDescriptionTextField.clear();
						saveSearchPopup.hide();
					}
				});
				VBox vbox = new VBox();
				vbox.getChildren().addAll(new Label("Search Name"), searchSaveNameTextField);
				HBox descriptionHBox = new HBox();
				descriptionHBox.getChildren().addAll(new Label("Search Description"), searchSaveDescriptionTextField);
				vbox.getChildren().add(descriptionHBox);
				HBox controlsHBox = new HBox();
				controlsHBox.getChildren().addAll(saveSearchPopupSaveButton, saveSearchPopupCancelButton);
				vbox.getChildren().add(controlsHBox);

				Pane popupPane = new Pane();
				popupPane.getChildren().add(vbox);

				//saveSearchPopup.setX(300); 
				//saveSearchPopup.setY(200);
				saveSearchPopup.setOpacity(1.0);
				saveSearchPopup.getScene().setFill(Color.WHITE);
				saveSearchPopup.getContent().add(popupPane);

				saveSearchPopup.show(AppContext.getMainApplicationWindow().getPrimaryStage());
			}

		} else {
			String title = "Failed saving search";
			String msg = "No search name or concept specified or selected";
			String details = "Must either select or specify search name in order to save search";
			AppContext.getCommonDialogs().showErrorDialog(title, msg, details, AppContext.getMainApplicationWindow().getPrimaryStage());
		}
	}

	private void doSaveSearch() {
		SearchViewModel model = searchViewModel;

		try {
			SearchConceptHelper.buildAndSaveSearchConcept(model);

			refreshSavedSearchComboBox();
		} catch (SearchConceptException e) {
			String title = "Failed saving search";
			String msg = "Caught " + e.getClass().getName() + " \"" + e.getLocalizedMessage() + "\"";

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos);
			e.printStackTrace(ps);
			String details = baos.toString();

			AppContext.getCommonDialogs().showErrorDialog(title, msg, details, AppContext.getMainApplicationWindow().getPrimaryStage());
		}
	}

	private void resetDefaults() {
		maxResultsCustomTextField.setText("");
		initializeSearchResultsTable();
	}

	private void refreshTotalResultsSelectedLabel() {
		int numSelected = searchResultsTable.getSelectionModel().getSelectedIndices().size();
		if (searchResultsTable.getItems().size() == 0) {
			totalResultsSelectedLabel.setVisible(false);
		}

		switch (numSelected) {
		case 1:
			totalResultsSelectedLabel.setText(numSelected + " result selected");
			break;

		case 0:
		default:
			totalResultsSelectedLabel.setText(numSelected + " results selected");
			break;
		}

		if (searchResultsTable.getItems().size() > 0) {
			totalResultsSelectedLabel.setVisible(true);
		}
	}

	// TODO: This doesn't make sense here.  Should be exported to listView, then Workflow
	private void exportSearchResultsToWorkflow() {
		initializeWorkflowServices();

		// Use HashSet to ensure that only one workflow is created for each concept
		if (searchResultsTable.getItems().size() > 0) {
			conceptWorkflowService.synchronizeWithRemote();
		}

		Set<Integer> concepts = new HashSet<>();
		for (CompositeSearchResult result : searchResultsTable.getItems()) {
			if (! concepts.contains(result.getConceptNid())) {
				concepts.add(result.getConceptNid());

				exportSearchResultToWorkflow(result.getConcept());
			}
		}

		if (searchResultsTable.getItems().size() > 0) {
			conceptWorkflowService.synchronizeWithRemote();
		}
	}

	// TODO: this should be invoked by context menu
	private void exportSearchResultToWorkflow(ConceptVersionBI conceptVersion) {
		initializeWorkflowServices();

		// TODO: eliminate hard-coding of processName "terminology-authoring.test1"
		final String processName = "terminology-authoring.test1";
		// TODO: eliminate hard-coding of userName
		final String userName = "alejandro";
		String preferredDescription = null;
		try {
			preferredDescription = conceptVersion.getPreferredDescription().getText();
		} catch (IOException | ContradictionException e1) {
			String title = "Failed creating new concept workflow";
			String msg = "Unexpected error calling getPreferredDescription() of conceptVersion (nid=" + conceptVersion.getConceptNid() + ", uuid=" + conceptVersion.getPrimordialUuid().toString() + "): caught " + e1.getClass().getName();
			LOG.error(title, e1);
			AppContext.getCommonDialogs().showErrorDialog(title, msg, e1.getMessage(), AppContext.getMainApplicationWindow().getPrimaryStage());
			e1.printStackTrace();
		}

		LOG.debug("Invoking createNewConceptWorkflowRequest(preferredDescription=\"" + preferredDescription + "\", conceptUuid=\"" + conceptVersion.getPrimordialUuid().toString() + "\", user=\"" + userName + "\", processName=\"" + processName + "\")");
		ProcessInstanceCreationRequestI createdRequest = conceptWorkflowService.createNewConceptWorkflowRequest(preferredDescription, conceptVersion.getPrimordialUuid(), userName, processName);
		LOG.debug("Created ProcessInstanceCreationRequestI: " + createdRequest);
	}

	private void exportSearchResultsToListBatchView() {
		ListBatchViewI lv = AppContext.getService(ListBatchViewI.class);

		AppContext.getMainApplicationWindow().ensureDockedViewIsVisble(lv);

		List<Integer> nids = new ArrayList<>();
		for (CompositeSearchResult result : searchResultsTable.getItems()) {
			if (! nids.contains(result.getConceptNid())) {
				nids.add(result.getConceptNid());
			}
		}

		lv.addConcepts(nids);
	}

	protected void windowForTableViewExportDialog(Window window) {
		this.windowForTableViewExportDialog = window;
	}

	private void refreshTotalResultsDisplayedLabel() {
		if (searchResultsTable.getItems().size() == 1) {
			totalResultsDisplayedLabel.setText(searchResultsTable.getItems().size() + " entry displayed");
		} else {
			totalResultsDisplayedLabel.setText(searchResultsTable.getItems().size() + " entries displayed");
		}

		refreshTotalResultsSelectedLabel();
	}

	@Override
	public void taskComplete(long taskStartTime, Integer taskId) {
		if (taskId == Tasks.SEARCH.ordinal()) {
			// Run on JavaFX thread.
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					try {
						if (! ssh.isCancelled()) {
							searchResultsTable.getItems().addAll(ssh.getResults());

							refreshTotalResultsDisplayedLabel();
						}
					} catch (Exception ex) {
						String title = "Unexpected Search Error";
						LOG.error(title, ex);
						AppContext.getCommonDialogs().showErrorDialog(title,
								"There was an unexpected error running the search",
								ex.toString(), AppContext.getMainApplicationWindow().getPrimaryStage());
						searchResultsTable.getItems().clear();
						refreshTotalResultsDisplayedLabel();
					} finally {
						searchRunning.set(false);
					}
				}
			});
		}
	}

	// MyTableCellCallback adds hooks for double-click and/or other mouse actions to String cells
	private class MyTableCellCallback<T> implements Callback<TableColumn<CompositeSearchResult, T>, TableCell<CompositeSearchResult, T>> {
		/* (non-Javadoc)
		 * @see javafx.util.Callback#call(java.lang.Object)
		 */
		public TableCell<CompositeSearchResult, T> createNewCell() {
			TableCell<CompositeSearchResult, T> cell = new TableCell<CompositeSearchResult, T>() {
				@Override
				public void updateItem(T item, boolean empty) {
					super.updateItem(item, empty);
					setText(empty ? null : getString());
					setGraphic(null);
				}

				private String getString() {
					return getItem() == null ? "" : getItem().toString();
				}
			};

			return cell;
		}

		// This method can be overridden to customize cells
		public TableCell<CompositeSearchResult, T> modifyCell(TableCell<CompositeSearchResult, T> cell) {
			//			// This is an example of an EventFilter			
			//			cell.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			//				@Override
			//				public void handle(MouseEvent event) {
			//					TableCell<?, ?> c = (TableCell<?,?>) event.getSource();
			//					
			//					if (event.getClickCount() == 1) {
			//						LOG.debug(event.getButton() + " single clicked. Cell text: " + c.getText());
			//					} else if (event.getClickCount() > 1) {
			//						LOG.debug(event.getButton() + " double clicked. Cell text: " + c.getText());
			//					}
			//				}
			//			});

			return cell;
		}

		@Override
		public TableCell<CompositeSearchResult, T> call(
				TableColumn<CompositeSearchResult, T> param) {
			TableCell<CompositeSearchResult, T> newCell = createNewCell();
			newCell.setUserData(param.getCellData(newCell.getIndex()));

			// This event filter adds a concept-specific context menu to all cells based on underlying concept
			// It is in this method because it should be common to all cells, even those overriding modifyCell()
			newCell.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					refreshTotalResultsSelectedLabel();

					if (event.getButton() == MouseButton.SECONDARY) {
						@SuppressWarnings("unchecked")
						TableCell<CompositeSearchResult, T> c = (TableCell<CompositeSearchResult, T>) event.getSource();

						if (c != null && c.getIndex() < c.getTableView().getItems().size()) {
							CommonMenusDataProvider dp = new CommonMenusDataProvider() {
								@Override
								public String[] getStrings() {
									List<String> items = new ArrayList<>();
									for (Integer index : c.getTableView().getSelectionModel().getSelectedIndices()) {
										items.add(c.getTableColumn().getCellData(index).toString());
									}

									String[] itemArray = items.toArray(new String[items.size()]);

									// TODO: determine why we are getting here multiple (2 or 3) times for each selection
									//System.out.println("Selected strings: " + Arrays.toString(itemArray));

									return itemArray;
								}
							};
							CommonMenusNIdProvider nidProvider = new CommonMenusNIdProvider() {
								@Override
								public Set<Integer> getNIds() {
									Set<Integer> nids = new HashSet<>();

									for (CompositeSearchResult r : (ObservableList<CompositeSearchResult>)c.getTableView().getSelectionModel().getSelectedItems()) {
										nids.add(r.getConceptNid());
									}

									// TODO: determine why we are getting here multiple (2 or 3) times for each selection
									//System.out.println("Selected nids: " + Arrays.toString(nids.toArray()));

									return nids;
								}
							};

							ContextMenu cm = new ContextMenu();
							CommonMenus.addCommonMenus(cm, dp, nidProvider);

							c.setContextMenu(cm);
						}
					}
				}
			});

			return modifyCell(newCell);
		}	
	}

	//	private void populateConceptSearchResultsTableFromDescriptionSearchResultsTable() {		
	//		Map<Integer, CompositeSearchResult> concepts = new HashMap<>();
	//		for (CompositeSearchResult result : descriptionSearchResultsTable.getItems()) {
	//			if (concepts.get(result.getConceptNid()) == null) {
	//				concepts.put(result.getConceptNid(), new CompositeSearchResult(result));
	//			} else if (concepts.get(result.getConceptNid()).getBestScore() < result.getBestScore()) {
	//				CompositeSearchResult copyOfResult = new CompositeSearchResult(result);
	//				copyOfResult.getMatchStrings().addAll(concepts.get(result.getConceptNid()).getMatchStrings());
	//				copyOfResult.getComponents().addAll(concepts.get(result.getConceptNid()).getComponents());
	//				concepts.put(result.getConceptNid(), copyOfResult);
	//			} else {
	//				concepts.get(result.getConceptNid()).getComponents().addAll(result.getComponents());
	//			}
	//		}
	//
	//		List<CompositeSearchResult> conceptResults = new ArrayList<>(concepts.values());
	//		Collections.sort(conceptResults, new CompositeSearchResultComparator());
	//		conceptSearchResultsTable.getItems().setAll(conceptResults);
	//	}
	private void initializeSearchResultsTable() {
		assert searchResultsTable != null : "fx:id=\"searchResultsTable\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";

		// Enable selection of multiple rows.  Context menu handlers are coded to send collections.
		searchResultsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		// Backup existing data in order to restore after reinitializing
		List<CompositeSearchResult> searchResultsTableBackup = new ArrayList<>(searchResultsTable.getItems());

		// Clear underlying data structure
		searchResultsTable.getItems().clear();

		// Enable optional menu to make visible columns invisible and currently invisible columns visible
		searchResultsTable.setTableMenuButtonVisible(true);

		// Disable editing of table data
		searchResultsTable.setEditable(false);

		// Match quality between 0 and 1
		TableColumn<CompositeSearchResult, Number> scoreCol = new TableColumn<>("Score");
		scoreCol.setCellValueFactory((param) -> new SimpleDoubleProperty(param.getValue().getBestScore()));
		scoreCol.setCellFactory(new MyTableCellCallback<Number>());
		scoreCol.setCellFactory(new MyTableCellCallback<Number>() {
			@Override
			public TableCell<CompositeSearchResult, Number> createNewCell() {

				final DecimalFormat fmt = new DecimalFormat("#.####");

				TableCell<CompositeSearchResult, Number> cell = new TableCell<CompositeSearchResult, Number>() {
					@Override
					public void updateItem(Number item, boolean empty) {
						super.updateItem(item, empty);
						setText(empty ? null : getString());
						setGraphic(null);
					}

					private String getString() {	
						fmt.setRoundingMode(RoundingMode.HALF_UP);
						return getItem() == null ? "" : fmt.format(getItem().doubleValue());
					}
				};

				cell.addEventFilter(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						TableCell<?, ?> c = (TableCell<?,?>) event.getSource();

						if (c != null && c.getItem() != null) {
							Tooltip tooltip = new Tooltip(c.getItem().toString());
							Tooltip.install(cell, tooltip);
						}
					}
				});

				return cell;
			}
		});

		// Active status
		TableColumn<CompositeSearchResult, String> statusCol = new TableColumn<>("Status");
		statusCol.setCellValueFactory((param) -> new SimpleStringProperty(param.getValue().getConcept().getStatus().toString().trim()));
		statusCol.setCellFactory(new MyTableCellCallback<String>());

		// numMatchesCol only meaningful for AggregationType CONCEPT
		// When AggregationTyppe is DESCRIPTION should always be 1
		TableColumn<CompositeSearchResult, Number> numMatchesCol = new TableColumn<>("Matches");
		numMatchesCol.setCellValueFactory((param) -> new SimpleIntegerProperty(param.getValue().getMatchingDescriptionComponents().size()));
		numMatchesCol.setCellFactory(new MyTableCellCallback<Number>() {
			@Override
			public TableCell<CompositeSearchResult, Number> modifyCell(TableCell<CompositeSearchResult, Number> cell) {

				cell.addEventFilter(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						TableCell<?, ?> c = (TableCell<?,?>) event.getSource();

						if (c != null && c.getIndex() < searchResultsTable.getItems().size()) {
							CompositeSearchResult result = searchResultsTable.getItems().get(c.getIndex());
							StringBuilder buffer = new StringBuilder();
							String fsn = null;
							try {
								fsn = result.getConcept().getFullySpecifiedDescription().getText().trim();
							} catch (IOException | ContradictionException e) {
								String title = "Failed getting FSN";
								String msg = "Failed getting fully specified description";
								LOG.error(title);
								AppContext.getCommonDialogs().showErrorDialog(title, msg, "Encountered " + e.getClass().getName() + ": " + e.getLocalizedMessage(), AppContext.getMainApplicationWindow().getPrimaryStage());
								e.printStackTrace();
							}

							List<DescriptionAnalogBI<?>> matchingDescComponents = new ArrayList<DescriptionAnalogBI<?>>(result.getMatchingDescriptionComponents());
							Collections.sort(matchingDescComponents, new DescriptionAnalogBITypeComparator());
							for (DescriptionAnalogBI<?> descComp : matchingDescComponents) {
								String type = WBUtility.getConPrefTerm(descComp.getTypeNid());
								buffer.append(type + ": " + descComp.getText() + "\n");
							}
							Tooltip tooltip = new Tooltip("Matching descriptions for \"" + fsn + "\":\n" + buffer.toString());

							Tooltip.install(cell, tooltip);
						}
					}
				});

				return cell;
			}
		});

		// Preferred term
		TableColumn<CompositeSearchResult, String> preferredTermCol = new TableColumn<>("Term");
		preferredTermCol.setCellFactory(new MyTableCellCallback<String>());
		preferredTermCol.setCellValueFactory((param) -> {
			try {
				return new SimpleStringProperty(param.getValue().getConcept().getPreferredDescription().getText().trim());
			} catch (IOException | ContradictionException e) {
				String title = "Failed getting preferred description";
				String msg = "Failed getting preferred description";
				LOG.error(title);
				AppContext.getCommonDialogs().showErrorDialog(title, msg, "Encountered " + e.getClass().getName() + ": " + e.getLocalizedMessage(), AppContext.getMainApplicationWindow().getPrimaryStage());
				e.printStackTrace();
				return null;
			}
		});

		// Fully Specified Name
		TableColumn<CompositeSearchResult, String> fsnCol = new TableColumn<>("FSN");
		fsnCol.setCellFactory(new MyTableCellCallback<String>());
		fsnCol.setCellValueFactory((param) -> {
			try {
				return new SimpleStringProperty(param.getValue().getConcept().getFullySpecifiedDescription().getText().trim());
			} catch (IOException | ContradictionException e) {
				String title = "Failed getting FSN";
				String msg = "Failed getting fully specified description";
				LOG.error(title);
				AppContext.getCommonDialogs().showErrorDialog(title, msg, "Encountered " + e.getClass().getName() + ": " + e.getLocalizedMessage(), AppContext.getMainApplicationWindow().getPrimaryStage());
				e.printStackTrace();
				return null;
			}
		});

		// Matching description text.
		// If AggregationType is CONCEPT then arbitrarily picks first matching description
		TableColumn<CompositeSearchResult, String> matchingTextCol = new TableColumn<>("Text");
		matchingTextCol.setCellValueFactory((param) -> new SimpleStringProperty(param.getValue().getMatchStrings().iterator().next().trim()));
		matchingTextCol.setCellFactory(new MyTableCellCallback<String>());

		// matchingDescTypeCol is string value type of matching description term displayed
		// Only meaningful for AggregationType DESCRIPTION
		// When AggregationTyppe is CONCEPT should always be type of first match
		TableColumn<CompositeSearchResult, String> matchingDescTypeCol = new TableColumn<>("Type");
		matchingDescTypeCol.setCellValueFactory((param) -> new SimpleStringProperty(WBUtility.getConPrefTerm(param.getValue().getMatchingDescriptionComponents().iterator().next().getTypeNid())));
		matchingDescTypeCol.setCellFactory(new MyTableCellCallback<String>());
		// matchingDescTypeCol defaults to invisible for anything but DESCRIPTION
		if (aggregationTypeComboBox.getSelectionModel().getSelectedItem() != AggregationType.DESCRIPTION) {
			matchingDescTypeCol.setVisible(false);
		}

		// NID set to invisible because largely for debugging purposes only
		TableColumn<CompositeSearchResult, Number> nidCol = new TableColumn<>("NID");
		nidCol.setCellValueFactory((param) -> new SimpleIntegerProperty(param.getValue().getConcept().getNid()));
		nidCol.setCellFactory(new MyTableCellCallback<Number>());
		nidCol.setVisible(false);

		// UUID set to invisible because largely for debugging purposes only
		TableColumn<CompositeSearchResult, String> uuIdCol = new TableColumn<>("UUID");
		uuIdCol.setCellValueFactory((param) -> new SimpleStringProperty(param.getValue().getConcept().getPrimordialUuid().toString().trim()));
		uuIdCol.setVisible(false);
		uuIdCol.setCellFactory(new MyTableCellCallback<String>());

		// Optional SCT ID
		TableColumn<CompositeSearchResult, String> sctIdCol = new TableColumn<>("SCTID");
		sctIdCol.setCellValueFactory((param) -> new SimpleStringProperty(ConceptViewerHelper.getSctId(ConceptViewerHelper.getConceptAttributes(param.getValue().getConcept())).trim()));
		sctIdCol.setCellFactory(new MyTableCellCallback<String>());

		searchResultsTable.getColumns().clear();

		// Default column ordering. May be changed within session
		searchResultsTable.getColumns().add(scoreCol);
		searchResultsTable.getColumns().add(statusCol);
		searchResultsTable.getColumns().add(fsnCol);
		searchResultsTable.getColumns().add(preferredTermCol);
		if (aggregationTypeComboBox.getSelectionModel().getSelectedItem() == AggregationType.CONCEPT) {
			searchResultsTable.getColumns().add(numMatchesCol);
		}
		if (aggregationTypeComboBox.getSelectionModel().getSelectedItem() == AggregationType.DESCRIPTION) {
			searchResultsTable.getColumns().add(matchingTextCol);
			searchResultsTable.getColumns().add(matchingDescTypeCol);
		}
		searchResultsTable.getColumns().add(sctIdCol);
		searchResultsTable.getColumns().add(uuIdCol);
		searchResultsTable.getColumns().add(nidCol);

		AppContext.getService(DragRegistry.class).setupDragOnly(searchResultsTable, new SingleConceptIdProvider() {
			@Override
			public String getConceptId()
			{
				CompositeSearchResult dragItem = searchResultsTable.getSelectionModel().getSelectedItem();
				if (dragItem != null)
				{
					LOG.debug("Dragging concept id " + dragItem.getConceptNid());
					return dragItem.getConceptNid() + "";
				}
				return null;
			}
		});

		Collections.sort(searchResultsTableBackup, new CompositeSearchResultComparator());
		searchResultsTable.getItems().addAll(searchResultsTableBackup);

		refreshTotalResultsDisplayedLabel();
	}

	private void initializeWorkflowServices() {
		if (conceptWorkflowService == null) {
			conceptWorkflowService = AppContext.getService(ConceptWorkflowServiceI.class);
		}

		assert conceptWorkflowService != null;
	}

	private void initializeSavedSearchComboBox() {
		assert savedSearchesComboBox != null : "fx:id=\"savedSearchesComboBox\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";

		// Force single selection
		savedSearchesComboBox.getSelectionModel().selectFirst();
		savedSearchesComboBox.setCellFactory((p) -> {
			final ListCell<SearchDisplayConcept> cell = new ListCell<SearchDisplayConcept>() {
				@Override
				protected void updateItem(SearchDisplayConcept c, boolean emptyRow) {
					super.updateItem(c, emptyRow);

					if(c == null) {
						setText(null);
					}else {
						setText(c.getFullySpecifiedName());
						Tooltip tooltip = new Tooltip(c.getPreferredTerm());
						Tooltip.install(this, tooltip);
					}
				}
			};

			return cell;
		});
		savedSearchesComboBox.setButtonCell(new ListCell<SearchDisplayConcept>() {
			@Override
			protected void updateItem(SearchDisplayConcept c, boolean emptyRow) {
				super.updateItem(c, emptyRow); 
				if (emptyRow) {
					setText("");
				} else {
					setText(c.getFullySpecifiedName());
					Tooltip tooltip = new Tooltip(c.getPreferredTerm());
					Tooltip.install(this, tooltip);
				}
			}
		});

		savedSearchesComboBox.valueProperty().addListener(new ChangeListener<Object>() {
			@Override public void changed(ObservableValue<? extends Object> ov, Object t, Object t1) {

				LOG.trace("savedSearchesComboBox ObservableValue: " + ov);

				if (t instanceof SearchDisplayConcept) {
					SearchDisplayConcept tSearchDisplayConcept = (SearchDisplayConcept)t;

					LOG.trace("savedSearchesComboBox old value: " + tSearchDisplayConcept != null ? (tSearchDisplayConcept.getFullySpecifiedName() + " (nid=" + tSearchDisplayConcept.getNid() + ")") : null);
				} else {
					LOG.trace("savedSearchesComboBox old value: " + t);
				}
				if (t1 instanceof SearchDisplayConcept) {
					SearchDisplayConcept t1SearchDisplayConcept = (SearchDisplayConcept)t1;

					LOG.debug("savedSearchesComboBox new value: " + t1SearchDisplayConcept);

					loadSavedSearch(t1SearchDisplayConcept);
				} else {
					LOG.trace("savedSearchesComboBox new value: " + t1);
				}
			}	
		});

		savedSearchesComboBox.setEditable(true);

		refreshSavedSearchComboBox();
	}

	private void refreshSavedSearchComboBox() {
		Task<List<SearchDisplayConcept>> loadSavedSearches = new Task<List<SearchDisplayConcept>>() {
			private ObservableList<SearchDisplayConcept> searches = FXCollections.observableList(new ArrayList<>());

			@Override
			protected List<SearchDisplayConcept> call() throws Exception {
				List<ConceptVersionBI> savedSearches = WBUtility.getAllChildrenOfConcept(Search.SEARCH_PERSISTABLE.getNid(), true);

				for (ConceptVersionBI concept : savedSearches) {
					String fsn = WBUtility.getFullySpecifiedName(concept);
					String preferredTerm = WBUtility.getConPrefTerm(concept.getNid());
					searches.add(new SearchDisplayConcept(fsn, preferredTerm, concept.getNid()));
				}

				return searches;
			}

			@Override
			protected void succeeded() {
				super.succeeded();

				savedSearchesComboBox.setItems(searches);
			}
		};

		savedSearchesComboBox.getItems().clear();
		Utility.execute(loadSavedSearches);
	}
	
	private void initializeSearchTypeComboBox() {
		initializeSearchTypeComboBox(null);
	}

	private void initializeSearchTypeComboBox(final SearchTypeFilter passedSearchTypeFilter) {
		assert searchTypeComboBox != null : "fx:id=\"searchTypeComboBox\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";

		searchTypeComboBox.setEditable(false);

		// Force single selection
		searchTypeComboBox.getSelectionModel().selectFirst();
		searchTypeComboBox.setCellFactory((p) -> {
			final ListCell<SearchType> cell = new ListCell<SearchType>() {
				@Override
				protected void updateItem(SearchType a, boolean bln) {
					super.updateItem(a, bln);

					if(a != null){
						setText(a.toString() + " Search");
					}else{
						setText(null);
					}
				}
			};

			return cell;
		});
		searchTypeComboBox.setButtonCell(new ListCell<SearchType>() {
			@Override
			protected void updateItem(SearchType searchType, boolean bln) {
				super.updateItem(searchType, bln); 
				if (bln) {
					setText("");
					this.setGraphic(null);
					searchTypeControlsHbox.getChildren().clear();
					searchTypeControlsHbox.setUserData(null);
					searchViewModel.setSearchType(null);
				} else {
					setText(searchType.toString() + " Search");
					this.setGraphic(null);

					searchTypeControlsHbox.getChildren().clear();

					SearchTypeFilter filter = null;

					if (searchType == SearchType.LUCENE) {
						LuceneSearchTypeFilter displayableLuceneFilter = (passedSearchTypeFilter != null && passedSearchTypeFilter instanceof LuceneSearchTypeFilter) ? (LuceneSearchTypeFilter)passedSearchTypeFilter : new LuceneSearchTypeFilter();
						filter = displayableLuceneFilter;

						Label searchParamLabel = new Label("Lucene Param");
						searchParamLabel.setPadding(new Insets(5.0));

						TextField searchParamTextField = new TextField();

						if (searchViewModel.getSearchType() != filter) {
							searchViewModel.setSearchType(filter);
						}
						Bindings.bindBidirectional(searchParamTextField.textProperty(), ((LuceneSearchTypeFilter)searchViewModel.getSearchType()).getSearchParameterProperty());

						searchParamTextField.setPadding(new Insets(5.0));
						searchParamTextField.setPromptText("Enter search text");
						if (displayableLuceneFilter.getSearchParameter() != null) {
							searchParamTextField.setText(displayableLuceneFilter.getSearchParameter());
						}

						searchTypeControlsHbox.getChildren().addAll(searchParamLabel, searchParamTextField);
					} 
					else if (searchType == SearchType.REGEXP) {
						RegExpSearchTypeFilter displayableRegExpFilter = (passedSearchTypeFilter != null && passedSearchTypeFilter instanceof RegExpSearchTypeFilter) ? (RegExpSearchTypeFilter)passedSearchTypeFilter : new RegExpSearchTypeFilter();

						filter = displayableRegExpFilter;

						Label searchParamLabel = new Label("RegExp Param");
						searchParamLabel.setPadding(new Insets(5.0));

						TextField searchParamTextField = new TextField();

						searchViewModel.setSearchType(filter);
						Bindings.bindBidirectional(searchParamTextField.textProperty(), ((RegExpSearchTypeFilter)searchViewModel.getSearchType()).getSearchParameterProperty());

						searchParamTextField.setPadding(new Insets(5.0));
						searchParamTextField.setPromptText("Enter search text");
						if (displayableRegExpFilter.getSearchParameter() != null) {
							searchParamTextField.setText(displayableRegExpFilter.getSearchParameter());
						}

						searchTypeControlsHbox.getChildren().addAll(searchParamLabel, searchParamTextField);
					} else {
						throw new RuntimeException("Unsupported SearchType " + searchType);
					}

					searchTypeControlsHbox.setUserData(filter);
				}
			}
		});
		searchTypeComboBox.setOnAction((event) -> {
			LOG.trace("aggregationTypeComboBox event (selected: " + aggregationTypeComboBox.getSelectionModel().getSelectedItem() + ")");

			searchResultsTable.getItems().clear();
			initializeSearchResultsTable();
		});

		searchTypeComboBox.setItems(FXCollections.observableArrayList(SearchType.values()));
		if (passedSearchTypeFilter == null || (passedSearchTypeFilter != null && (passedSearchTypeFilter instanceof LuceneSearchTypeFilter))) {
			searchTypeComboBox.getSelectionModel().select(SearchType.LUCENE);
		} else if (passedSearchTypeFilter != null && (passedSearchTypeFilter instanceof RegExpSearchTypeFilter)) {
			searchTypeComboBox.getSelectionModel().select(SearchType.REGEXP);
		} else {
			throw new RuntimeException("Unsupported SearchTypeFilter " + passedSearchTypeFilter.getClass().getName() + ".  Must be either LuceneSearchTypeFilter or RegExpSearchTypeFilter.");
		}
	}

	private void initializeAggregationTypeComboBox() {
		assert aggregationTypeComboBox != null : "fx:id=\"aggregationTypeComboBox\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";

		// Force single selection
		aggregationTypeComboBox.getSelectionModel().selectFirst();
		aggregationTypeComboBox.setCellFactory((p) -> {
			final ListCell<AggregationType> cell = new ListCell<AggregationType>() {
				@Override
				protected void updateItem(AggregationType a, boolean bln) {
					super.updateItem(a, bln);

					if(a != null){
						setText("Display " + a.toString());
					}else{
						setText(null);
					}
				}
			};

			return cell;
		});
		aggregationTypeComboBox.setButtonCell(new ListCell<AggregationType>() {
			@Override
			protected void updateItem(AggregationType t, boolean bln) {
				super.updateItem(t, bln); 
				if (bln) {
					setText("");
				} else {
					setText("Display " + t.toString());
				}
			}
		});
		aggregationTypeComboBox.setOnAction((event) -> {
			LOG.trace("aggregationTypeComboBox event (selected: " + aggregationTypeComboBox.getSelectionModel().getSelectedItem() + ")");

			searchResultsTable.getItems().clear();
			initializeSearchResultsTable();
		});

		aggregationTypeComboBox.setItems(FXCollections.observableArrayList(AggregationType.values()));
		aggregationTypeComboBox.getSelectionModel().select(AggregationType.CONCEPT);
	}

	private synchronized void search() {
		// Sanity check if search already running.
		if (searchRunning.get()) {
			return;
		}

		searchRunning.set(true);

		searchResultsTable.getItems().clear();
		refreshTotalResultsDisplayedLabel();

		SearchViewModel model = searchViewModel;

		if (! validateSearchViewModel(model, "Cannot execute save")) {
			searchRunning.set(false);

			return;
		}

		SearchTypeFilter filter = model.getSearchType();

		if (! (filter instanceof LuceneSearchTypeFilter)) {
			String title = "Search failed";

			String msg = "SearchTypeFilter " + filter.getClass().getName() + " not supported";
			AppContext.getCommonDialogs().showErrorDialog(title, msg, "Only SearchTypeFilter LuceneSearchTypeFilter currently supported", AppContext.getMainApplicationWindow().getPrimaryStage());

			searchRunning.set(false);
			return;
		}

		LuceneSearchTypeFilter displayableLuceneFilter = (LuceneSearchTypeFilter)filter;

		SearchResultsFilter searchResultsFilter = null;
		if (model.getFilters() != null) {
			List<SearchResultsFilter> searchResultsFilters = new ArrayList<>();
			for (NonSearchTypeFilter nonSearchTypeFilter : model.getFilters()) {
				// TODO: This must be changed when support for other NonSearchTypeFilter types added
				searchResultsFilters.add(SearchResultsFilterFactory.createSearchResultsFilter((IsDescendantOfFilter)nonSearchTypeFilter));
			}
			searchResultsFilter = new SearchResultsIntersectionFilter(searchResultsFilters);
			
//			try {
//				searchResultsFilter = SearchResultsFilterFactory.createNonSearchTypeFilterSearchResultsIntersectionFilter(model.getFilters().toArray(new NonSearchTypeFilter[model.getFilters().size()]));
//			} catch (SearchResultsFilterException e) {
//				String title = "Failed creating SearchResultsFilter";
//				String msg = title + ". Encountered " + e.getClass().getName() + " " + e.getLocalizedMessage();
//				String details =  msg + " applying " + model.getFilters().size() + " NonSearchResultFilter filters: " + Arrays.toString(model.getFilters().toArray());
//				LOG.error(details);
//				AppContext.getCommonDialogs().showErrorDialog(title, msg, details, AppContext.getMainApplicationWindow().getPrimaryStage());
//			
//				return;
//			}
		}

		// "we get called back when the results are ready."
		switch (aggregationTypeComboBox.getSelectionModel().getSelectedItem()) {
		case  CONCEPT:
		{
			SearchBuilder builder = SearchBuilder.conceptDescriptionSearchBuilder(displayableLuceneFilter.getSearchParameter());
			builder.setCallback(this);
			builder.setTaskId(Tasks.SEARCH.ordinal());
			if (searchResultsFilter != null) {
				builder.setFilter(searchResultsFilter);
			}
			if (maxResultsCustomTextField.getText() != null && maxResultsCustomTextField.getText().length() > 0) {
				Integer maxResults = Integer.valueOf(maxResultsCustomTextField.getText());
				if (maxResults != null && maxResults > 0) {
					builder.setSizeLimit(maxResults);
				}
			}
			ssh = SearchHandler.doConceptSearch(builder);
			break;
		}
		case DESCRIPTION:
		{
			SearchBuilder builder = SearchBuilder.descriptionSearchBuilder(displayableLuceneFilter.getSearchParameter());
			builder.setCallback(this);
			builder.setTaskId(Tasks.SEARCH.ordinal());
			if (searchResultsFilter != null) {
				builder.setFilter(searchResultsFilter);
			}
			if (maxResultsCustomTextField.getText() != null && maxResultsCustomTextField.getText().length() > 0) {
				Integer maxResults = Integer.valueOf(maxResultsCustomTextField.getText());
				if (maxResults != null && maxResults > 0) {
					builder.setSizeLimit(maxResults);
				}
			}
			ssh = SearchHandler.doDescriptionSearch(builder);
			break;
		}
		default:
			String title = "Unsupported Aggregation Type";
			String msg = "Aggregation Type " + aggregationTypeComboBox.getSelectionModel().getSelectedItem() + " not supported";
			LOG.error(title);
			AppContext.getCommonDialogs().showErrorDialog(title, msg, "Aggregation Type must be one of " + Arrays.toString(aggregationTypeComboBox.getItems().toArray()), AppContext.getMainApplicationWindow().getPrimaryStage());							break;
		}
	}

	public Pane getRoot() {
		return pane;
	}

	interface ColumnValueExtractor {
		String extract(TableColumn<CompositeSearchResult, ?> col);
	}
	private static String getTableViewRow(TableView<CompositeSearchResult> table, String delimiter, String lineTerminator, ColumnValueExtractor extractor) {
		ObservableList<TableColumn<CompositeSearchResult, ?>> columns = table.getColumns();
		StringBuilder row = new StringBuilder();

		for (int colIndex = 0; colIndex < columns.size(); ++colIndex) {
			TableColumn<CompositeSearchResult, ?> col = columns.get(colIndex);
			if (! col.isVisible()) {
				// Ensure that newline is written even if column is not
				if (colIndex == (columns.size() - 1) && lineTerminator != null) {
					// Append newline to row
					row.append(lineTerminator);
				}

				continue;
			}
			// Extract text or data from column and append to row
			row.append(extractor.extract(col));
			if (colIndex < (columns.size() - 1)) {
				if (delimiter != null) {
					// Ensure that delimiter is written only if there are remaining visible columns to be written
					boolean hasMoreVisibleCols = false;
					for (int remainingColsIndex = colIndex + 1; remainingColsIndex < columns.size(); ++remainingColsIndex) {
						if (columns.get(remainingColsIndex).isVisible()) {
							hasMoreVisibleCols = true;
							break;
						}
					}
					if (hasMoreVisibleCols) {
						// Append delimiter to row
						row.append(delimiter);
					}
				}
			} else if (colIndex == (columns.size() - 1) && lineTerminator != null) {
				// Append newline to row
				row.append(lineTerminator);
			}
		}

		return row.toString();
	}

	private void exportSearchResultsAsTabDelimitedValues() {
		FileChooser fileChooser = new FileChooser();
		final String delimiter = "\t";
		final String newLine = "\n";

		//Set extension filter
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
		fileChooser.getExtensionFilters().add(extFilter);

		//Show save file dialog
		File file = fileChooser.showSaveDialog(windowForTableViewExportDialog);

		//String tempDir = System.getenv("TEMP");
		//File file = new File(tempDir + File.separator + "EnhanceSearchViewControllerTableViewData.csv");

		if (file == null) {
			LOG.warn("FileChooser returned null export file.  Cancel possibly requested.");
		} else { // if (file != null)
			LOG.debug("Writing TableView data to file \"" + file.getAbsolutePath() + "\"...");

			Writer writer = null;
			try {
				writer = new BufferedWriter(new FileWriter(file));
				String headerRow = getTableViewRow(searchResultsTable, delimiter, newLine, (col) -> col.getText());

				LOG.trace(headerRow);
				writer.write(headerRow);

				for (int rowIndex = 0; rowIndex < searchResultsTable.getItems().size(); ++rowIndex) {
					final int finalRowIndex = rowIndex;
					String dataRow = getTableViewRow(searchResultsTable, delimiter, newLine, (col) -> col.getCellObservableValue(finalRowIndex).getValue().toString());
					LOG.trace(dataRow);
					writer.write(dataRow);
				}

				LOG.debug("Wrote " + searchResultsTable.getItems().size() + " rows of TableView data to file \"" + file.getAbsolutePath() + "\".");
			} catch (IOException e) {
				LOG.error("FAILED writing TableView data to file \"" + file.getAbsolutePath() + "\". Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
				e.printStackTrace();
			}
			finally {
				try {
					writer.flush();
				} catch (IOException e) {
					LOG.error("FAILED flushing TableView data file \"" + file.getAbsolutePath() + "\". Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
					e.printStackTrace();
				}
				try {
					writer.close();
				} catch (IOException e) {
					LOG.error("FAILED closing TableView data file \"" + file.getAbsolutePath() + "\". Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
					e.printStackTrace();
				}
			}
		}
	}
}