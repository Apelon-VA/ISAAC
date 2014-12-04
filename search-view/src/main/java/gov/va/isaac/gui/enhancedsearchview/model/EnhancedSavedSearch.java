package gov.va.isaac.gui.enhancedsearchview.model;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.constants.Search;
import gov.va.isaac.gui.dialog.UserPrompt.UserPromptResponse;
import gov.va.isaac.gui.enhancedsearchview.SearchConceptHelper;
import gov.va.isaac.gui.enhancedsearchview.SearchConceptHelper.SearchConceptException;
import gov.va.isaac.gui.enhancedsearchview.SearchDisplayConcept;
import gov.va.isaac.gui.enhancedsearchview.SearchTypeEnums.ComponentSearchType;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
			if (! SearchModel.getSearchTypeSelector().getTypeSpecificModel().isSavableSearch()) {
				AppContext.getCommonDialogs().showErrorDialog("Save of Search Failed", "Search is not savable", SearchModel.getSearchTypeSelector().getTypeSpecificModel().getSearchSavabilityValidationFailureMessage());
			} else {
				saveSearch();
			}
		});
		
		restoreSearchButton = new Button("Restore Search");
		restoreSearchButton.setPrefWidth(Control.USE_COMPUTED_SIZE);
		restoreSearchButton.setMinWidth(Control.USE_PREF_SIZE);

		restoreSearchButton.setOnAction((e) -> {
			loadSavedSearch(); 
		});
		restoreSearchButton.setDisable(true);
		savedSearchesComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<SearchDisplayConcept>() {
			@Override
			public void changed(
					ObservableValue<? extends SearchDisplayConcept> observable,
					SearchDisplayConcept oldValue, SearchDisplayConcept newValue) {
				restoreSearchButton.setDisable(newValue == null);
			}
		});
		

		
		if (! searchTypeComboBoxChangeListenerSet) {
			SearchModel.getSearchTypeSelector().getSearchTypeComboBox().getSelectionModel().selectedItemProperty().addListener(new ChangeListener<SearchType>() {
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
			
			SearchType currentType = SearchModel.getSearchTypeSelector().getCurrentType();
			SearchModel.getSearchTypeSelector().getSearchTypeComboBox().getSelectionModel().select(null);
			SearchModel.getSearchTypeSelector().getSearchTypeComboBox().getSelectionModel().select(currentType);
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
		SaveSearchPrompt prompt = new SaveSearchPrompt();
		prompt.showUserPrompt(AppContext.getMainApplicationWindow().getPrimaryStage(), "Define Refset");


		if (prompt.getButtonSelected() == UserPromptResponse.APPROVE) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd @ HH:mm:ss");
			LocalDateTime dateTime = LocalDateTime.now();
			String formattedDateTime = dateTime.format(formatter);
			String user = ExtendedAppContext.getCurrentlyLoggedInUserProfile().getUserLogonName();
			final String nameToSave = prompt.getNameTextField().getText() + " by " + user + " on " + formattedDateTime;

			SearchConceptHelper.buildAndSaveSearchConcept(searchModel, nameToSave, prompt.getDescTextField().getText());
			refreshSavedSearchComboBox();

			return prompt.getNameTextField().getText();
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

	public static void refreshSavedSearchComboBox() {
		Task<List<SearchDisplayConcept>> loadSavedSearches = new Task<List<SearchDisplayConcept>>() {
			private ObservableList<SearchDisplayConcept> searches = FXCollections.observableList(new ArrayList<>());

			@Override
			protected List<SearchDisplayConcept> call() throws Exception {
				List<ConceptVersionBI> savedSearches = WBUtility.getAllChildrenOfConcept(Search.SEARCH_PERSISTABLE.getNid(), true);

				SearchType currentSearchType = SearchModel.getSearchTypeSelector().getSearchTypeComboBox().getSelectionModel().getSelectedItem();
				for (ConceptVersionBI concept : savedSearches) {
					if (getCachedSearchTypeFromSearchConcept(concept) == currentSearchType) {
						boolean addSearchToList = true;
						if (currentSearchType == SearchType.TEXT) {
							ComponentSearchType currentlyViewedComponentSearchType = TextSearchTypeModel.getCurrentComponentSearchType();
							ComponentSearchType loadedComponentSearchType = getCachedComponentSearchTypeFromSearchConcept(concept);
							
							if (currentlyViewedComponentSearchType != null && loadedComponentSearchType != null) {
								if (currentlyViewedComponentSearchType != loadedComponentSearchType) {
									addSearchToList = false;
								}
							}
						}
						
						if (addSearchToList) {
							String fsn = WBUtility.getFullySpecifiedName(concept);
							String preferredTerm = WBUtility.getConPrefTerm(concept.getNid());
							searches.add(new SearchDisplayConcept(fsn, preferredTerm, concept.getNid()));
						}
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
		Bindings.bindBidirectional(searchSaveNameTextField.textProperty(), SearchModel.getSearchTypeSelector().getTypeSpecificModel().getNameProperty());
		Bindings.bindBidirectional(searchSaveDescriptionTextField.textProperty(), SearchModel.getSearchTypeSelector().getTypeSpecificModel().getDescriptionProperty());

		Bindings.bindBidirectional(searchModel.getMaxResultsCustomTextField().valueProperty(), SearchModel.getSearchTypeSelector().getTypeSpecificModel().getMaxResultsProperty());

		Bindings.bindBidirectional(droolsExprTextField.textProperty(), SearchModel.getSearchTypeSelector().getTypeSpecificModel().getDroolsExprProperty());
	}

	public Button getSaveButton() {
 		return saveSearchButton;
	}

	public Button getRestoreSearchButton() {
		return restoreSearchButton;
	}
	
	private static Map<Integer, SearchType> conceptSearchTypeCache = new HashMap<>();
	private static SearchType getCachedSearchTypeFromSearchConcept(ConceptVersionBI concept) throws IOException {
		synchronized (conceptSearchTypeCache) {
			if (conceptSearchTypeCache.get(concept.getNid()) == null) {
				conceptSearchTypeCache.put(concept.getConceptNid(), getSearchTypeFromSearchConcept(concept));
			}
		}

		return conceptSearchTypeCache.get(concept.getNid());
	}
	private static Set<Integer> badSearchConceptsToIgnore = new HashSet<>();
	private static SearchType getSearchTypeFromSearchConcept(ConceptVersionBI concept) throws IOException {
		synchronized (badSearchConceptsToIgnore) {
			if (badSearchConceptsToIgnore.contains(concept.getConceptNid())) {
				LOG.debug("Ignoring invalid/unsupported search filter concept nid=" + concept.getConceptNid() + ", status=" + concept.getStatus() + ", uuid=" + concept.getPrimordialUuid() + ", desc=\"" + ComponentDescriptionHelper.getComponentDescription(concept) + "\"");
			
				return null;
			}
		}

		Collection<? extends RefexDynamicVersionBI<?>> refexes = concept.getRefexesDynamicActive(WBUtility.getViewCoordinate());
		for (RefexDynamicVersionBI<?> refex : refexes) {
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
			} else {
				LOG.debug("getSearchTypeFromSearchConcept() ignoring refex \"" + dud.getRefexName() + "\" on search filter concept nid=" + concept.getConceptNid() + ", status=" + concept.getStatus() + ", uuid=" + concept.getPrimordialUuid() + ", desc=\"" + ComponentDescriptionHelper.getComponentDescription(concept) + "\""); 
			}
		}
		
		//String warn = "Automatically RETIRING invalid/unsupported search filter concept nid=" + concept.getConceptNid() + ", status=" + concept.getStatus() + ", uuid=" + concept.getPrimordialUuid() + ", desc=\"" + ComponentDescriptionHelper.getComponentDescription(concept) + "\"";
		String warn = "Invalid/unsupported search filter concept nid=" + concept.getConceptNid() + ", status=" + concept.getStatus() + ", uuid=" + concept.getPrimordialUuid() + ", desc=\"" + ComponentDescriptionHelper.getComponentDescription(concept) + "\"";

		LOG.warn(warn);
		
		synchronized (badSearchConceptsToIgnore) {
			// Retire Concept for bad search refex
//			RuntimeGlobalsI globals = AppContext.getService(RuntimeGlobalsI.class);
			try {
				// disable WorkflowInitiationPropertyChangeListener
//				globals.disableAllCommitListeners();

				// TODO: Make retirement of bad search concepts work
//				ConceptAttributeAB cab = new ConceptAttributeAB(concept.getConceptNid(), /* concept.getConceptAttributesActive().isDefined() */ true, RefexDirective.EXCLUDE);
//				ConceptAttributeChronicleBI cabi = WBUtility.getBuilder().constructIfNotCurrent(cab);
//				
//				//ConceptCB cab = concept.makeBlueprint(WBUtility.getViewCoordinate(), IdDirective.PRESERVE, RefexDirective.INCLUDE);
//				//ConceptChronicleBI cabi = WBUtility.getBuilder().constructIfNotCurrent(cab);
//				
//				cab.setStatus(Status.INACTIVE);
//				
//				WBUtility.addUncommitted(cabi.getEnclosingConcept());
//				
//				// Commit
//				WBUtility.commit(concept);
			} catch (Exception e) {
				String error = "FAILED to automatically retire invalid/unsupported search filter concept nid=" + concept.getConceptNid() + ", status=" + concept.getStatus() + ", uuid=" + concept.getPrimordialUuid() + ", desc=\"" + ComponentDescriptionHelper.getComponentDescription(concept) + "\".  Caught " + e.getClass().getName() + " " + e.getLocalizedMessage();
				LOG.error(error, e);
				e.printStackTrace();
			} finally {
//				globals.enableAllCommitListeners();

				badSearchConceptsToIgnore.add(concept.getConceptNid());
			}
		}
		return null;
	}
	
	private static Map<Integer, ComponentSearchType> componentSearchTypeCache = new HashMap<>();
	private static ComponentSearchType getCachedComponentSearchTypeFromSearchConcept(ConceptVersionBI concept) throws IOException {
		synchronized (componentSearchTypeCache) {
			if (componentSearchTypeCache.get(concept.getNid()) == null) {
				componentSearchTypeCache.put(concept.getConceptNid(), getComponentSearchTypeFromSearchConcept(concept));
			}
		}
		
		return componentSearchTypeCache.get(concept.getNid());
	}
	private static ComponentSearchType getComponentSearchTypeFromSearchConcept(ConceptVersionBI concept) throws IOException {
		Collection<? extends RefexDynamicVersionBI<?>> refexes = concept.getRefexesDynamicActive(WBUtility.getViewCoordinate());
		for (RefexDynamicVersionBI<?> refex : refexes) {
			RefexDynamicUsageDescription dud = null;
			try {
				dud = refex.getRefexDynamicUsageDescription();
			} catch (IOException | ContradictionException e) {
				LOG.error("Failed performing getRefexDynamicUsageDescription(): caught " + e.getClass().getName() + " \"" + e.getLocalizedMessage() + "\"", e);

				return null;
			}

			if (dud.getRefexName().equals(Search.SEARCH_LUCENE_FILTER.getDescription())) {
				return ComponentSearchType.LUCENE;
			} else if (dud.getRefexName().equals(Search.SEARCH_REGEXP_FILTER.getDescription())) {
				return ComponentSearchType.REGEXP;
			}
		}
		
		String error = "Invalid/unsupported search filter concept nid=" + concept.getConceptNid() + ", uuid=" + concept.getPrimordialUuid() + ", desc=\"" + ComponentDescriptionHelper.getComponentDescription(concept) + "\"";
		LOG.error(error);
		return null;
	}
}
