package gov.va.isaac.gui.enhancedsearchview.model;

import gov.va.isaac.AppContext;
import gov.va.isaac.constants.Search;
import gov.va.isaac.gui.enhancedsearchview.SearchConceptHelper;
import gov.va.isaac.gui.enhancedsearchview.SearchConceptHelper.SearchConceptException;
import gov.va.isaac.gui.enhancedsearchview.SearchDisplayConcept;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.WBUtility;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;

import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnhancedSavedSearch {
	public TextField getSearchSaveNameTextField() {
		return searchSaveNameTextField;
	}

	public TextField getSearchSaveDescriptionTextField() {
		return searchSaveDescriptionTextField;
	}

	public TextField getDroolsExprTextField() {
		return droolsExprTextField;
	}

	public ComboBox<SearchDisplayConcept> getSavedSearchesComboBox() {
		return savedSearchesComboBox;
	}
	
	private static Button saveSearchButton;
	private static ComboBox<SearchDisplayConcept> savedSearchesComboBox;

	private static TextField searchSaveNameTextField;
	private static TextField searchSaveDescriptionTextField;
	private static TextField droolsExprTextField;

	private SearchModel searchModel = new SearchModel();
	private static final Logger LOG = LoggerFactory.getLogger(EnhancedSavedSearch.class);

	public EnhancedSavedSearch() {
		
		if (searchSaveNameTextField == null) {
			searchSaveNameTextField = new TextField();
		}
		if (searchSaveDescriptionTextField == null) {
			searchSaveDescriptionTextField = new TextField();
		}
		if (droolsExprTextField == null) {
			droolsExprTextField = new TextField();
		}

		initializeSavedSearchComboBox();
		
		saveSearchButton = new Button("Save Search");
		saveSearchButton.setPrefWidth(Control.USE_COMPUTED_SIZE);
		saveSearchButton.setMinWidth(Control.USE_PREF_SIZE);

		saveSearchButton.setOnAction((action) -> {
			// TODO: Create BooleanProperty and bind to saveSearchButton to disable
			Object buttonCellObject = savedSearchesComboBox.valueProperty().getValue();
			if (buttonCellObject != null && (buttonCellObject instanceof String)) {
				saveSearch(); 
			}
		});
	}
	
	void loadSavedSearch(SearchDisplayConcept displayConcept) {
		LOG.info("loadSavedSearch(" + displayConcept + ")");

		SearchModel model = null;
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
			
		/* All for Component Search 
			if (!SearchValidator.validateComponentSearchViewModel(model, "Failed loading saved search " + displayConcept)) {
				return;
			} else {
				searchModel.getSearchTypeComboBox().getSelectionModel().select(null);

				searchModel.getSearchViewModel().copy(model);

				searchModel.getSearchTypeComboBox().getSelectionModel().select(searchModel.getSearchViewModel().getSearchType().getSearchType());

				refreshSearchViewModelBindings();
				
				searchModel.getSearchFilterGridPane().getChildren().clear();

				for (NonSearchTypeFilter<? extends NonSearchTypeFilter<?>> filter : searchModel.getSearchViewModel().getFilters()) {
					addSearchFilter(filter);
				}

				//this.currentSearchViewCoordinate = model.getViewCoordinate();

				// TODO: set drools expression somewhere

				LOG.debug("loadSavedSearch() loaded model: " + model);

				return;
			}
		*/
		} else {
			LOG.error("Failed loading saved search " + displayConcept);
			return;
		}
	}

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
		try {
			SearchConceptHelper.buildAndSaveSearchConcept(searchModel);

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

	private void initializeSavedSearchComboBox() {
		savedSearchesComboBox = new ComboBox<SearchDisplayConcept>();

		// Force single selection
		savedSearchesComboBox.getSelectionModel().selectFirst();
		savedSearchesComboBox.setPrefWidth(250);
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
					
					searchSaveNameTextField.clear();
					searchSaveDescriptionTextField.clear();
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

	void refreshSearchViewModelBindings() {
		Bindings.bindBidirectional(searchSaveNameTextField.textProperty(), searchModel.getSearchTypeSelector().getTypeSpecificModel().getNameProperty());
		Bindings.bindBidirectional(searchSaveDescriptionTextField.textProperty(), searchModel.getSearchTypeSelector().getTypeSpecificModel().getDescriptionProperty());

		Bindings.bindBidirectional(searchModel.getMaxResultsCustomTextField().valueProperty(), searchModel.getSearchTypeSelector().getTypeSpecificModel().getMaxResultsProperty());

		Bindings.bindBidirectional(droolsExprTextField.textProperty(), searchModel.getSearchTypeSelector().getTypeSpecificModel().getDroolsExprProperty());
	}

	public Button getSaveButton() {
 		return saveSearchButton;
	}
}
