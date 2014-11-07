package gov.va.isaac.gui.enhancedsearchview.model;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.enhancedsearchview.SearchTypeEnums.ResultsType;
import gov.va.isaac.gui.enhancedsearchview.SearchTypeEnums.Tasks;
import gov.va.isaac.gui.enhancedsearchview.filters.Filter;
import gov.va.isaac.gui.enhancedsearchview.filters.LuceneSearchTypeFilter;
import gov.va.isaac.gui.enhancedsearchview.filters.NonSearchTypeFilter;
import gov.va.isaac.gui.enhancedsearchview.filters.SearchTypeFilter;
import gov.va.isaac.gui.enhancedsearchview.searchresultsfilters.SearchResultsFilterHelper;
import gov.va.isaac.search.SearchBuilder;
import gov.va.isaac.search.SearchHandler;
import gov.va.isaac.search.SearchResultsFilter;
import gov.va.isaac.search.SearchResultsFilterException;
import gov.va.isaac.search.SearchResultsIntersectionFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import org.apache.mahout.math.Arrays;

public class ComponentContentSearchTypeModel extends SearchTypeModel {
	private final ObjectProperty<SearchTypeFilter> searchTypeFilterProperty = new SimpleObjectProperty();
	private final ObservableList<NonSearchTypeFilter<? extends NonSearchTypeFilter<?>>> filters = FXCollections.observableArrayList();

	public ComponentContentSearchTypeModel() {
		searchTypeFilterProperty.addListener(new ChangeListener<SearchTypeFilter>() {
			@Override
			public void changed(
					ObservableValue<? extends SearchTypeFilter> observable,
					SearchTypeFilter oldValue, SearchTypeFilter newValue) {
				isSearchRunnableProperty.set(isValid());
			}
		});

		filters.addListener(new ListChangeListener<NonSearchTypeFilter<?>>() {
			@Override
			public void onChanged(
					javafx.collections.ListChangeListener.Change<? extends NonSearchTypeFilter<?>> c) {
				isSearchRunnableProperty.set(isValid());
			}
		});
	}
	public SearchTypeFilter<?> getSearchType() {
		return searchTypeFilterProperty.get();
	}
	public void setSearchType(SearchTypeFilter<?> searchTypeFilter) {
		this.searchTypeFilterProperty.set(searchTypeFilter);
	}

	public ObservableList<NonSearchTypeFilter<? extends NonSearchTypeFilter<?>>> getFilters() {
		return filters;
	}
	
	public Collection<Filter<?>> getValidFilters() {
		List<Filter<?>> validFilters = new ArrayList<>();
		
		for (Filter<?> filter : filters) {
			if (filter.isValid()) {
				validFilters.add(filter);
			}
		}
		
		return Collections.unmodifiableCollection(validFilters);
	}

	public Collection<Filter<?>> getInvalidFilters() {
		List<Filter<?>> invalidFilters = new ArrayList<>();
		
		for (Filter<?> filter : filters) {
			if (! filter.isValid()) {
				invalidFilters.add(filter);
			}
		}
		
		return Collections.unmodifiableCollection(invalidFilters);
	}

	public  boolean isValid() {
		if (getInvalidFilters().size() > 0) {
			return false;
		}

		if (viewCoordinateProperty.get() == null) {
			return false;
		}
		
		if (searchTypeFilterProperty.get() == null || ! searchTypeFilterProperty.get().isValid()) {
			return false;
		}
		
		return true;
	}
	@Override
	public void typeSpecificCopy(SearchTypeModel other) {
		filters.clear();
		filters.addAll(((ComponentContentSearchTypeModel)other).getFilters());
		searchTypeFilterProperty.set(((ComponentContentSearchTypeModel)other).getSearchType());
	}

	@Override
	public String getModelDisplayString() {
		return ", searchTypeFilter=" + searchTypeFilterProperty.get() 
				+ ", filter=" + Arrays.toString(filters.toArray());
	}
	
	@Override
	protected boolean valideSearchTypeSpecificModel(String errorDialogTitle) {
		if (getSearchType() == null) {
			String details = "No SearchTypeFilter specified: " + this;
			LOG.warn("Invalid search model (name=" + getName() + "). " + details);

			if (errorDialogTitle != null) {
				AppContext.getCommonDialogs().showErrorDialog(errorDialogTitle, errorDialogTitle, details, AppContext.getMainApplicationWindow().getPrimaryStage());
			}

			return false;
		} else if (getInvalidFilters().size() > 0) {
			String details = "Found " + getInvalidFilters().size() + " invalid filter: " + Arrays.toString(getFilters().toArray());
			LOG.warn("Invalid filter in search model (name=" + getName() + "). " + details);

			if (errorDialogTitle != null) {
				AppContext.getCommonDialogs().showErrorDialog(errorDialogTitle, errorDialogTitle, details, AppContext.getMainApplicationWindow().getPrimaryStage());
			}

			return false;
		}

		return true;
	}
	
	@Override
	public void executeSearch(ResultsType resultsType, String modelMaxResults) {

		SearchTypeFilter<?> filter = getSearchType();

		if (! (filter instanceof LuceneSearchTypeFilter)) {
			String title = "Search failed";

			String msg = "SearchTypeFilter " + filter.getClass().getName() + " not supported";
			AppContext.getCommonDialogs().showErrorDialog(title, msg, "Only SearchTypeFilter LuceneSearchTypeFilter currently supported", AppContext.getMainApplicationWindow().getPrimaryStage());

			searchRunning.set(false);
			return;
		}

		LuceneSearchTypeFilter displayableLuceneFilter = (LuceneSearchTypeFilter)filter;

		SearchResultsFilter searchResultsFilter = null;
		if (getFilters() != null) {
			List<SearchResultsFilter> searchResultsFilters = new ArrayList<>();

			try {
				for (NonSearchTypeFilter<?> nonSearchTypeFilter : getFilters()) {
					SearchResultsFilter newSearchResultsFilter = SearchResultsFilterHelper.createSearchResultsFilter(nonSearchTypeFilter);

					searchResultsFilters.add(newSearchResultsFilter);
				}
				LOG.debug("Constructing a new SearchResultsIntersectionFilter with " + searchResultsFilters.size() + " SearchResultsFilter instances: " + Arrays.toString(searchResultsFilters.toArray()));
				searchResultsFilter = new SearchResultsIntersectionFilter(searchResultsFilters);

				//searchResultsFilter = SearchResultsFilterHelper.createNonSearchTypeFilterSearchResultsIntersectionFilter(getFilters().toArray(new NonSearchTypeFilter[getFilters().size()]));
			} catch (SearchResultsFilterException e) {
				String title = "Failed creating SearchResultsFilter";
				String msg = title + ". Encountered " + e.getClass().getName() + " " + e.getLocalizedMessage();
				String details =  msg + " applying " + getFilters().size() + " NonSearchResultFilter filters: " + Arrays.toString(getFilters().toArray());
				LOG.error(details);
				AppContext.getCommonDialogs().showErrorDialog(title, msg, details, AppContext.getMainApplicationWindow().getPrimaryStage());

				ssh.cancel();

				return;
			}
		}

		// "we get called back when the results are ready."
		switch (resultsType) {
			case CONCEPT:
			{
				SearchBuilder builder = SearchBuilder.conceptDescriptionSearchBuilder(displayableLuceneFilter.getSearchParameter() != null ? displayableLuceneFilter.getSearchParameter() : "");
				builder.setCallback(this);
				builder.setTaskId(Tasks.SEARCH.ordinal());
				if (searchResultsFilter != null) {
					builder.setFilter(searchResultsFilter);
				}
				if (modelMaxResults != null && modelMaxResults.length() > 0) {
					Integer maxResults = Integer.valueOf(modelMaxResults);
					if (maxResults != null && maxResults > 0) {
						builder.setSizeLimit(maxResults);
					}
				}
				builder.setMergeResultsOnConcept(true);
				ssh = SearchHandler.descriptionSearch(builder);
				break;
			}
			case DESCRIPTION:
			{
				SearchBuilder builder = SearchBuilder.descriptionSearchBuilder(displayableLuceneFilter.getSearchParameter() != null ? displayableLuceneFilter.getSearchParameter() : "");
				builder.setCallback(this);
				builder.setTaskId(Tasks.SEARCH.ordinal());
				if (searchResultsFilter != null) {
					builder.setFilter(searchResultsFilter);
				}
				if (modelMaxResults != null && modelMaxResults.length() > 0) {
					Integer maxResults = Integer.valueOf(modelMaxResults);
					if (maxResults != null && maxResults > 0) {
						builder.setSizeLimit(maxResults);
					}
				}
				ssh = SearchHandler.descriptionSearch(builder);
				break;
			}
			default:
				String title = "Unsupported Aggregation Type";
				String msg = "Aggregation Type " + resultsType + " not supported";
				LOG.error(title);
				AppContext.getCommonDialogs().showErrorDialog(title, msg, "Aggregation Type must be one of " + Arrays.toString(ResultsType.values()), AppContext.getMainApplicationWindow().getPrimaryStage());
	
				ssh.cancel();
				break;
			}
		}
}
