package gov.va.isaac.gui.enhancedsearchview.model;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.constants.Search;
import gov.va.isaac.gui.enhancedsearchview.DynamicRefexHelper;
import gov.va.isaac.gui.enhancedsearchview.SearchConceptHelper;
import gov.va.isaac.gui.enhancedsearchview.SearchConceptHelper.SearchConceptException;
import gov.va.isaac.gui.enhancedsearchview.SearchDisplayConcept;
import gov.va.isaac.gui.enhancedsearchview.SearchTypeEnums.SearchType;
import gov.va.isaac.gui.enhancedsearchview.model.type.text.TextSearchTypeModel;
import gov.va.isaac.gui.enhancedsearchview.resulthandler.SaveSearchPrompt;
import gov.va.isaac.util.ComponentDescriptionHelper;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;

import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicUsageDescription;
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
	private static Button restoreSearchButton;
	private static ComboBox<SearchDisplayConcept> savedSearchesComboBox;

	private static TextField searchSaveNameTextField;
	private static TextField searchSaveDescriptionTextField;
	private static TextField droolsExprTextField;

	private SearchModel searchModel = new SearchModel();
	private static final Logger LOG = LoggerFactory.getLogger(EnhancedSavedSearch.class);
	private static boolean searchTypeComboBoxChangeListenerSet = false;
	
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

		saveSearchButton.setOnAction((e) -> {
			saveSearch(); 
		});
		
		restoreSearchButton = new Button("Restore Search");
		restoreSearchButton.setPrefWidth(Control.USE_COMPUTED_SIZE);
		restoreSearchButton.setMinWidth(Control.USE_PREF_SIZE);

		restoreSearchButton.setOnAction((e) -> {
			loadSavedSearch(); 
		});
		
		if (! searchTypeComboBoxChangeListenerSet) {
			searchModel.getSearchTypeSelector().getSearchTypeComboBox().getSelectionModel().selectedItemProperty().addListener(new ChangeListener<SearchType>() {
				@Override
				public void changed(
						ObservableValue<? extends SearchType> observable,
						SearchType oldValue, SearchType newValue) {
					if (oldValue != newValue) {
						refreshSavedSearchComboBox();
					}
				}
			});

			searchTypeComboBoxChangeListenerSet = true;
		}
	}
	
	void loadSavedSearch() {
		SearchDisplayConcept searchToRestore = savedSearchesComboBox.getSelectionModel().getSelectedItem();
		LOG.info("loadSavedSearch(" + searchToRestore + ")");

		SearchModel model = null;
		try {
			model = SearchConceptHelper.loadSavedSearch(searchToRestore);
			
			SearchType currentType = model.getSearchTypeSelector().getCurrentType();
			model.getSearchTypeSelector().getSearchTypeComboBox().getSelectionModel().select(null);
			model.getSearchTypeSelector().getSearchTypeComboBox().getSelectionModel().select(currentType);
		} catch (SearchConceptException e) {
			LOG.error("Failed loading saved search. Caught " + e.getClass().getName() + " \"" + e.getLocalizedMessage() + "\"");
			e.printStackTrace();

			String title = "Failed loading saved search";
			String msg = "Cannot load existing saved search \"" + searchToRestore + "\"";
			String details = "Caught " + e.getClass().getName() + " \"" + e.getLocalizedMessage() + "\"." + "\n" + "model:" + model;
			AppContext.getCommonDialogs().showErrorDialog(title, msg, details, AppContext.getMainApplicationWindow().getPrimaryStage());

			return;
		}

	}

	private void saveSearch() {
		LOG.debug("saveSearch() called.  Search specified: " + savedSearchesComboBox.valueProperty().getValue());
		// Save Search popup
		try {
			String searchName = getSaveSearchRequest();

			if (searchName != null) {
				AppContext.getCommonDialogs().showInformationDialog("Search Successfully Saved", "Saved new search:" + searchName);
			}
		} catch (Exception e) {
			LOG.error("Failed saving search.  Caught {} \"{}\"", e.getClass().getName(), e.getLocalizedMessage());
			e.printStackTrace();
			AppContext.getCommonDialogs().showErrorDialog("Save Search Failure", "Save Search Failure", "Failed to save new search\n\n" + e.getLocalizedMessage()); 
		}
	}

	private String getSaveSearchRequest() throws SearchConceptException {
		SaveSearchPrompt.showContentGatheringDialog(AppContext.getMainApplicationWindow().getPrimaryStage(), "Define Refset");


		if (SaveSearchPrompt.getButtonSelected() == SaveSearchPrompt.Response.SAVE) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd @ HH:mm:ss");
			LocalDateTime dateTime = LocalDateTime.now();
			String formattedDateTime = dateTime.format(formatter);
			String user = ExtendedAppContext.getCurrentlyLoggedInUserProfile().getUserLogonName();
			final String nameToSave = SaveSearchPrompt.getNameTextField().getText() + " by " + user + " on " + formattedDateTime;

			SearchConceptHelper.buildAndSaveSearchConcept(searchModel, nameToSave, SaveSearchPrompt.getDescTextField().getText());
			refreshSavedSearchComboBox();

			return SaveSearchPrompt.getNameTextField().getText();
		}
		
		return null;
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
		
		refreshSavedSearchComboBox();
	}

	private void refreshSavedSearchComboBox() {
		Task<List<SearchDisplayConcept>> loadSavedSearches = new Task<List<SearchDisplayConcept>>() {
			private ObservableList<SearchDisplayConcept> searches = FXCollections.observableList(new ArrayList<>());

			@Override
			protected List<SearchDisplayConcept> call() throws Exception {
				List<ConceptVersionBI> savedSearches = WBUtility.getAllChildrenOfConcept(Search.SEARCH_PERSISTABLE.getNid(), true);

				SearchType currentSearchType = searchModel.getSearchTypeSelector().getSearchTypeComboBox().getSelectionModel().getSelectedItem();
				for (ConceptVersionBI concept : savedSearches) {
					if (getCachedSearchTypeFromSearchConcept(concept) == currentSearchType) {
						String fsn = WBUtility.getFullySpecifiedName(concept);
						String preferredTerm = WBUtility.getConPrefTerm(concept.getNid());
						searches.add(new SearchDisplayConcept(fsn, preferredTerm, concept.getNid()));
					}
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

	public Button getRestoreSearchButton() {
		return restoreSearchButton;
	}
	
	private static Map<Integer, SearchType> conceptSearchTypeCache = new HashMap<>();
	private static SearchType getCachedSearchTypeFromSearchConcept(ConceptVersionBI concept) throws IOException {
		if (conceptSearchTypeCache.get(concept.getNid()) == null) {
			conceptSearchTypeCache.put(concept.getConceptNid(), getSearchTypeFromSearchConcept(concept));
		}
		
		return conceptSearchTypeCache.get(concept.getNid());
	}
	private static SearchType getSearchTypeFromSearchConcept(ConceptVersionBI concept) throws IOException {
		Collection<? extends RefexDynamicVersionBI<?>> refexes = concept.getRefexesDynamicActive(WBUtility.getViewCoordinate());
		int i = 0;
		for (RefexDynamicVersionBI<?> refex : refexes) {
//			LOG.debug("Displaying sememe #" + (++i) + " of " + refexes.size());
//			DynamicRefexHelper.displayDynamicRefex(refex);

			RefexDynamicUsageDescription dud = null;
			try {
				dud = refex.getRefexDynamicUsageDescription();
			} catch (IOException | ContradictionException e) {
				LOG.error("Failed performing getRefexDynamicUsageDescription(): caught " + e.getClass().getName() + " \"" + e.getLocalizedMessage() + "\"", e);

				return null;
			}

			if (dud.getRefexName().equals(Search.SEARCH_LUCENE_FILTER.getDescription())) {
				return SearchType.TEXT;
			} else if (dud.getRefexName().equals(Search.SEARCH_REGEXP_FILTER.getDescription())) {
				return SearchType.TEXT;
			} else if (dud.getRefexName().equals(Search.SEARCH_SEMEME_CONTENT_FILTER.getDescription())) {
				return SearchType.SEMEME;
			}
		}
		
		String error = "Invalid/unsupported search filter concept nid=" + concept.getConceptNid() + ", uuid=" + concept.getPrimordialUuid() + ", desc=\"" + ComponentDescriptionHelper.getComponentDescription(concept) + "\"";
		LOG.error(error);
		return null;
	}
}
