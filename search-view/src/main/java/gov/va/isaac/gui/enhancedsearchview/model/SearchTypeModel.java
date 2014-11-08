package gov.va.isaac.gui.enhancedsearchview.model;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.enhancedsearchview.EnhancedSearchViewBottomPane;
import gov.va.isaac.gui.enhancedsearchview.IntegerField;
import gov.va.isaac.gui.enhancedsearchview.SearchTypeEnums.ResultsType;
import gov.va.isaac.gui.enhancedsearchview.SearchTypeEnums.Tasks;
import gov.va.isaac.gui.enhancedsearchview.resulthandler.ResultsToTaxonomy;
import gov.va.isaac.search.CompositeSearchResult;
import gov.va.isaac.search.SearchHandle;
import gov.va.isaac.util.TaskCompleteCallback;
import gov.va.isaac.util.WBUtility;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;

import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SearchTypeModel implements TaskCompleteCallback {
	protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

	private TableView<CompositeSearchResult> resultsTable;

	protected static EnhancedSearchViewBottomPane bottomPane;
	protected static SplitPane splitPane;
	protected static BorderPane taxonomyPane;

	protected final StringProperty name = new SimpleStringProperty();
	protected final StringProperty description = new SimpleStringProperty();
	protected final ObjectProperty<ViewCoordinate> viewCoordinateProperty = new SimpleObjectProperty(WBUtility.getViewCoordinate());
	protected final BooleanProperty isSearchRunnableProperty = new SimpleBooleanProperty();
	
	private final IntegerProperty maxResults = new SimpleIntegerProperty(100);
	private final StringProperty droolsExpr = new SimpleStringProperty();

	protected SearchHandle ssh = null;
	protected BooleanProperty searchRunning = new SimpleBooleanProperty(false);


	abstract public void typeSpecificCopy(SearchTypeModel other);
	abstract public String getModelDisplayString();
	abstract protected boolean isValidTypeModel(String errorDialogTitle);
	abstract public void executeSearch(ResultsType resultsType, String modelMaxResults);

	
	protected SearchTypeModel() {
		viewCoordinateProperty.set(WBUtility.getViewCoordinate());
		
		viewCoordinateProperty.addListener(new ChangeListener<ViewCoordinate>() {
			@Override
			public void changed(
					ObservableValue<? extends ViewCoordinate> observable,
					ViewCoordinate oldValue, ViewCoordinate newValue) {
				isSearchRunnableProperty.set(isValidTypeModel(null));
			}
		});

	}


	public void copy(SearchTypeModel other) {
		name.set(other.getName());
		description.set(other.getDescription());
		//if (other.getSearchType() == null || searchTypeFilterProperty.get() == null || searchTypeFilterProperty.get().getClass() != other.getSearchType().getClass()) {
		viewCoordinateProperty.set(other.viewCoordinateProperty.get());
		maxResults.set(other.getMaxResults());
		droolsExpr.set(other.getDroolsExpr());
		
		typeSpecificCopy(other);
	}
	
	public String getName() {
		return name.getValue();
	}
	public void setName(String name) {
		this.name.set(name);
	}

	public StringProperty getDescriptionProperty() {
		return description;
	}
	public String getDescription() {
		return description.getValue();
	}
	public void setDescription(String description) {
		this.description.set(description);
	}

	public StringProperty getNameProperty() {
		return name;
	}
	public ViewCoordinate getViewCoordinate() {
		return viewCoordinateProperty.get();
	}
	public ObjectProperty<ViewCoordinate> getViewCoordinateProperty() {
		return viewCoordinateProperty;
	}
	public void setViewCoordinate(ViewCoordinate viewCoordinate) {
		viewCoordinateProperty.set(viewCoordinate);
	}
	public BooleanProperty getIsSearchRunnableProperty() {
		return isSearchRunnableProperty;
	}
	public IntegerProperty getMaxResultsProperty() {
		return maxResults;
	}
	public int getMaxResults() {
		return maxResults.get();
	}
	public void setMaxResults(int maxResults) {
		this.maxResults.set(maxResults);
	}

	public StringProperty getDroolsExprProperty() {
		return droolsExpr;
	}
	public String getDroolsExpr() {
		return droolsExpr.get();
	}
	public void setDroolsExpr(String droolsExpr) {
		this.droolsExpr.set(droolsExpr);
	}
	
	public boolean validateSearchTypeModel(String errorDialogTitle) {
		if (getViewCoordinate() == null) {
			String details = "View coordinate is null: " + this;
			LOG.warn("Invalid search model (name=" + getName() + "). " + details);

			if (errorDialogTitle != null) {
				AppContext.getCommonDialogs().showErrorDialog(errorDialogTitle, errorDialogTitle, details, AppContext.getMainApplicationWindow().getPrimaryStage());
			}

			return false;
		} else {
			return isValidTypeModel(errorDialogTitle);
		}
	}
	
	public void taskComplete(long taskStartTime, Integer taskId) {
		if (taskId == Tasks.SEARCH.ordinal()) {
			// Run on JavaFX thread.
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					try {
						if (! ssh.isCancelled()) {
							resultsTable.setItems(FXCollections.observableArrayList(ssh.getResults()));
							
							bottomPane.refreshBottomPanel();
							bottomPane.refreshTotalResultsSelectedLabel();
							
							if (splitPane.getItems().contains(taxonomyPane)) {
								ResultsToTaxonomy.resultsToSearchTaxonomy();
							}
						}
					} catch (Exception ex) {
						searchRunning.set(false);
						String title = "Unexpected Search Error";
						LOG.error(title, ex);
						AppContext.getCommonDialogs().showErrorDialog(title,
								"There was an unexpected error running the search",
								ex.toString(), AppContext.getMainApplicationWindow().getPrimaryStage());
						//searchResultsTable.getItems().clear();
						bottomPane.refreshBottomPanel();
						bottomPane.refreshTotalResultsSelectedLabel();
					} finally {
						searchRunning.set(false);
					}
				}
			});
		}
	}


	public synchronized void search(TableView<CompositeSearchResult> results, ResultsType resultsType, IntegerField maxRequestedResults ) {
		// Sanity check if search already running.
		if (searchRunning.get()) {
			return;
		}

		try {
			searchRunning.set(true);

			results.getItems().clear();
			this.resultsTable = results;
			bottomPane.refreshBottomPanel();
			bottomPane.refreshTotalResultsSelectedLabel();

			if (! validateSearchTypeModel("Cannot execute search")) {
				searchRunning.set(false);

				return;
			}
			
			executeSearch(resultsType, maxRequestedResults.getText());
		} catch (Exception e) {
			LOG.error("Search failed unexpectedly...", e);
			searchRunning.set(false);
			ssh = null;  //force a null ptr in taskComplete, so an error is displayed.
			taskComplete(0, null);
		}
	}
	public static void setPanes(EnhancedSearchViewBottomPane bottomPane, SplitPane splitPane, BorderPane taxonomyPane) {
		SearchTypeModel.bottomPane = bottomPane;
		SearchTypeModel.splitPane = splitPane;
		SearchTypeModel.taxonomyPane = taxonomyPane;
	}
}