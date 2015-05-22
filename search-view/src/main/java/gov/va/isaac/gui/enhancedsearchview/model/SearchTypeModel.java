package gov.va.isaac.gui.enhancedsearchview.model;

import java.util.ArrayList;
import java.util.Collection;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.enhancedsearchview.EnhancedSearchViewBottomPane;
import gov.va.isaac.gui.enhancedsearchview.IntegerField;
import gov.va.isaac.gui.enhancedsearchview.SearchTypeEnums.ResultsType;
import gov.va.isaac.gui.enhancedsearchview.resulthandler.ResultsToTaxonomy;
import gov.va.isaac.search.CompositeSearchResult;
import gov.va.isaac.util.OTFUtility;
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
import javafx.scene.layout.BorderPane;

import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SearchTypeModel {
	protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
	
	//TODO (artf231410) rewrite this mess of static / nonstatic confusing muddle with a proper HK2 pattern.
	//We obviously need a training session for some folks on what HK2 is good at.  
	
	protected static EnhancedSearchViewBottomPane bottomPane;
	protected static SplitPane splitPane;
	protected static BorderPane taxonomyPane;

	protected final StringProperty name = new SimpleStringProperty(getClass().getName().replaceAll(".*\\.", ""));
	protected final StringProperty description = new SimpleStringProperty();
	protected final ObjectProperty<ViewCoordinate> viewCoordinateProperty = new SimpleObjectProperty<>(OTFUtility.getViewCoordinate());
	protected final BooleanProperty isSearchTypeRunnableProperty = new SimpleBooleanProperty(false);
	protected final BooleanProperty isSearchTypeSavableProperty = new SimpleBooleanProperty(false);

	private final IntegerProperty maxResults = new SimpleIntegerProperty(100);
	private final StringProperty droolsExpr = new SimpleStringProperty();

	abstract public void typeSpecificCopy(SearchTypeModel other);
	abstract public String getModelDisplayString();
	abstract public void executeSearch(ResultsType resultsType, String modelMaxResults);

	public boolean isSavableSearch() {
		return isSavableSearch(null);
	}
	public boolean isSavableSearch(String errorDialogTitle) {
		String validationError = getSearchSavabilityValidationFailureMessage();
		if (validationError == null) {
			return true;
		} else {
			String details = "Invalid search type model for save (name=" + getName() + "). " + validationError;
			LOG.info(details);

			if (errorDialogTitle != null) {
				AppContext.getCommonDialogs().showErrorDialog(errorDialogTitle, errorDialogTitle, details, AppContext.getMainApplicationWindow().getPrimaryStage());
			}

			return false;
		}
	}
	public String getSearchSavabilityValidationFailureMessage() {
		return getValidationFailureMessage();
	}

	abstract public String getValidationFailureMessage();
	final protected boolean isValidSearch() {
		return isValidSearch(null);
	}
	final protected boolean isValidSearch(String errorDialogTitle) {
		String validationError = getValidationFailureMessage();
		if (validationError == null) {
			return true;
		} else {
			String details = "Invalid search type model (name=" + getName() + "). " + validationError;
			LOG.info(details);

			if (errorDialogTitle != null) {
				AppContext.getCommonDialogs().showErrorDialog(errorDialogTitle, errorDialogTitle, details, AppContext.getMainApplicationWindow().getPrimaryStage());
			}

			return false;
		}
	}
	
	protected SearchTypeModel() {
		viewCoordinateProperty.set(OTFUtility.getViewCoordinate());
		
		viewCoordinateProperty.addListener(new ChangeListener<ViewCoordinate>() {
			@Override
			public void changed(
					ObservableValue<? extends ViewCoordinate> observable,
					ViewCoordinate oldValue, ViewCoordinate newValue) {
				isSearchTypeRunnableProperty.set(isValidSearch());
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

	public BooleanProperty getSearchRunning() { return SearchModel.getSearchRunning(); }
	
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
		return isSearchTypeRunnableProperty;
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
			LOG.info("Invalid search model (name=" + getName() + "). " + details);

			if (errorDialogTitle != null) {
				AppContext.getCommonDialogs().showErrorDialog(errorDialogTitle, errorDialogTitle, details, AppContext.getMainApplicationWindow().getPrimaryStage());
			}

			return false;
		} else {
			return isValidSearch(errorDialogTitle);
		}
	}
	

	public synchronized void search(ResultsType resultsType, IntegerField maxRequestedResults ) {
		// Sanity check if search already running.
		if (getSearchRunning().get()) {
			return;
		}

		try {
			getSearchRunning().set(true);

			SearchModel.getSearchResultsTable().getResults().getItems().clear();
			bottomPane.refreshBottomPanel();
			bottomPane.refreshTotalResultsSelectedLabel();

			if (! validateSearchTypeModel("Cannot execute search")) {
				getSearchRunning().set(false);

				return;
			}
			
			executeSearch(resultsType, maxRequestedResults.getText());
		} catch (Exception e) {
			LOG.error("Search failed unexpectedly...", e);
			getSearchRunning().set(false);
		}
	}
	public static void setPanes(EnhancedSearchViewBottomPane bottomPane, SplitPane splitPane, BorderPane taxonomyPane) {
		SearchTypeModel.bottomPane = bottomPane;
		SearchTypeModel.splitPane = splitPane;
		SearchTypeModel.taxonomyPane = taxonomyPane;
	}
	
	public static int removeNullResults(Collection<CompositeSearchResult> results) {
		Collection<CompositeSearchResult> nullResults = new ArrayList<>();
		if (results != null) {
			for (CompositeSearchResult result : results) {
				if (result.getContainingConcept() == null) {
					nullResults.add(result);
				}
			}
			results.removeAll(nullResults);
		}
		return nullResults.size();
	}
	
	public static void setResults(Collection<CompositeSearchResult> results) {
		int filteredResults = removeNullResults(results);
		
		SearchModel.getSearchResultsTable().getResults().setItems(FXCollections.observableArrayList(results));
		SearchModel.setResultsOffPathCount(filteredResults);
		
		bottomPane.refreshBottomPanel();
		bottomPane.refreshTotalResultsSelectedLabel();

		if (splitPane.getItems().contains(taxonomyPane)) {
			ResultsToTaxonomy.resultsToSearchTaxonomy();
		}
	}

}