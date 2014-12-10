package gov.va.isaac.gui.enhancedsearchview.resulthandler;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.enhancedsearchview.SctTreeItemSearchResultsDisplayPolicies;
import gov.va.isaac.gui.enhancedsearchview.SearchTypeEnums.TaxonomyViewMode;
import gov.va.isaac.gui.enhancedsearchview.model.SearchModel;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.taxonomyView.TaxonomyViewI;
import gov.va.isaac.search.CompositeSearchResult;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.WBUtility;

import java.util.HashSet;
import java.util.Set;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;

import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResultsToTaxonomy {
	private static TaxonomyViewI taxonomyView = null;
	private static SctTreeItemSearchResultsDisplayPolicies taxonomyDisplayPolicies = null;
	private static final BooleanProperty taxonomyPanelShouldFilterProperty = new SimpleBooleanProperty(false);

	private static Task<SctTreeItemSearchResultsDisplayPolicies> configureDisplayPoliciesTask = null;

	private final static BorderPane taxonomyPanelBorderPane = new BorderPane();
	private static SplitPane searchResultsAndTaxonomySplitPane;

	private static ComboBox<TaxonomyViewMode> taxonomyPanelViewModeComboBox;
	private static Button taxonomyPanelCloseButton;

	private static final Logger LOG = LoggerFactory.getLogger(ResultsToTaxonomy.class);

	public static void resultsToSearchTaxonomy() {
		initializeTaxonomyPanel();
		
		if (taxonomyView == null) {
			taxonomyView = AppContext.getService(TaxonomyViewI.class);

			taxonomyDisplayPolicies = new SctTreeItemSearchResultsDisplayPolicies(taxonomyView.getDefaultDisplayPolicies());
			taxonomyDisplayPolicies.setFilterMode(taxonomyPanelShouldFilterProperty);

			taxonomyView.setDisplayPolicies(taxonomyDisplayPolicies);

			taxonomyPanelBorderPane.setCenter(taxonomyView.getView());
		}

		if (! searchResultsAndTaxonomySplitPane.getItems().contains(taxonomyPanelBorderPane)) {
			searchResultsAndTaxonomySplitPane.getItems().add(taxonomyPanelBorderPane);
			searchResultsAndTaxonomySplitPane.setDividerPositions(0.6);
			LOG.debug("Added taxonomyPanelBorderPane to searchResultsAndTaxonomySplitPane");
		}
		
		if (configureDisplayPoliciesTask != null) {
			if (configureDisplayPoliciesTask.isRunning()) {
				configureDisplayPoliciesTask.cancel();
			}
		}
		configureDisplayPoliciesTask = new Task<SctTreeItemSearchResultsDisplayPolicies>() {
			boolean cancelled = false;
			
			@Override
			protected SctTreeItemSearchResultsDisplayPolicies call() throws Exception {
				HashSet<Integer> searchResultAncestors = new HashSet<>();
				HashSet<Integer> searchResults = new HashSet<>();

				// TODO this throws ConcurrentModificationException if called before prior export completed
				for (CompositeSearchResult c : SearchModel.getSearchResultsTable().getResults().getItems()) {
					if (cancelled) {
						return taxonomyDisplayPolicies;
					}

					searchResults.add(c.getContainingConcept().getNid());

					Set<ConceptVersionBI> ancestorNids = null;
					ancestorNids = WBUtility.getConceptAncestors(c.getContainingConcept().getNid());

					for (ConceptVersionBI concept : ancestorNids) {
						searchResultAncestors.add(concept.getNid());
					}

				}
				
				taxonomyDisplayPolicies.setSearchResultAncestors(searchResultAncestors);
				taxonomyDisplayPolicies.setSearchResults(searchResults);
				
				return taxonomyDisplayPolicies;
			}

			@Override
			protected void succeeded() {
				if (! cancelled) {
					taxonomyView.refresh();
				}
			}
			
			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				cancelled = true;

				return super.cancel(mayInterruptIfRunning);
			}

			@Override
			protected void failed() {
				Throwable e = getException();
				
				if (e != null) {
					String title = "Failed sending search results to SearchResultsTaxonomy Panel";
					String msg = "Failed sending " + SearchModel.getSearchResultsTable().getResults().getItems().size() + " search results to SearchResultsTaxonomy Panel";
					String details = "Caught " + e.getClass().getName() + " \"" + e.getLocalizedMessage() + "\".";
					AppContext.getCommonDialogs().showErrorDialog(title, msg, details, AppContext.getMainApplicationWindow().getPrimaryStage());

					e.printStackTrace();
				} else {
					LOG.error("Task configureDisplayPoliciesTask FAILED without throwing an exception/throwable");
				}
			}
		};

		Utility.execute(configureDisplayPoliciesTask);

	}
	
	public static BorderPane getTaxonomyPanelBorderPane() {
		return taxonomyPanelBorderPane;
	}

	public static void initializeTaxonomyPanel() {
		initializeTaxonomyViewModeComboBox();
		
		taxonomyPanelBorderPane.setTop(taxonomyPanelViewModeComboBox);
		taxonomyPanelCloseButton = new Button("Close");
		taxonomyPanelCloseButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				searchResultsAndTaxonomySplitPane.getItems().remove(taxonomyPanelBorderPane);
				LOG.debug("Removed taxonomyPanelBorderPane from searchResultsAndTaxonomySplitPane");
			}
		});
		taxonomyPanelBorderPane.setBottom(taxonomyPanelCloseButton);
	}
	
	private static void initializeTaxonomyViewModeComboBox() {
		taxonomyPanelViewModeComboBox = new ComboBox<>();

		// Force single selection
		taxonomyPanelViewModeComboBox.getSelectionModel().selectFirst();
		taxonomyPanelViewModeComboBox.setCellFactory((p) -> {
			final ListCell<TaxonomyViewMode> cell = new ListCell<TaxonomyViewMode>() {
				@Override
				protected void updateItem(TaxonomyViewMode a, boolean bln) {
					super.updateItem(a, bln);

					if(a != null){
						setText(a.toString() + " view mode");
					}else{
						setText(null);
					}
				}
			};

			return cell;
		});
		taxonomyPanelViewModeComboBox.setButtonCell(new ListCell<TaxonomyViewMode>() {
			@Override
			protected void updateItem(TaxonomyViewMode t, boolean bln) {
				super.updateItem(t, bln); 
				if (bln) {
					setText("");
				} else {
					setText(t.toString() + " view mode");
				}
			}
		});
		taxonomyPanelViewModeComboBox.setOnAction((event) -> {
			LOG.trace("taxonomyPanelViewModeComboBox event (selected: " + taxonomyPanelViewModeComboBox.getSelectionModel().getSelectedItem() + ")");

			boolean statusChanged = false;
			
			switch (taxonomyPanelViewModeComboBox.getSelectionModel().getSelectedItem()) {
			case FILTERED:
				if (! taxonomyPanelShouldFilterProperty.get()) {
					statusChanged = true;
				}
				taxonomyPanelShouldFilterProperty.set(true);
				break;
			case UNFILTERED:
				if (taxonomyPanelShouldFilterProperty.get()) {
					statusChanged = true;
				}
				taxonomyPanelShouldFilterProperty.set(false);
				break;

				default:
					throw new RuntimeException("Unsupported TaxonomyViewMode value \"" + taxonomyPanelViewModeComboBox.getSelectionModel().getSelectedItem() + "\"");
			}
			
			if (statusChanged) {
				taxonomyView.refresh();
			}
		});

		taxonomyPanelViewModeComboBox.setItems(FXCollections.observableArrayList(TaxonomyViewMode.values()));
		taxonomyPanelViewModeComboBox.getSelectionModel().select(TaxonomyViewMode.UNFILTERED);
	}
	
	public static void setSearchAndTaxonomySplitPane(SplitPane pane) {
		searchResultsAndTaxonomySplitPane = pane;
	}
}
