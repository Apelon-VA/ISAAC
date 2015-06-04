package gov.va.isaac.gui.enhancedsearchview.model.type.text;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.enhancedsearchview.SearchTypeEnums.ComponentSearchType;
import gov.va.isaac.gui.enhancedsearchview.SearchTypeEnums.ResultsType;
import gov.va.isaac.gui.enhancedsearchview.SearchTypeEnums.Tasks;
import gov.va.isaac.gui.enhancedsearchview.filters.Filter;
import gov.va.isaac.gui.enhancedsearchview.filters.NonSearchTypeFilter;
import gov.va.isaac.gui.enhancedsearchview.filters.SearchTypeFilter;
import gov.va.isaac.gui.enhancedsearchview.model.SearchModel;
import gov.va.isaac.gui.enhancedsearchview.model.SearchResultsTable;
import gov.va.isaac.gui.enhancedsearchview.model.SearchTypeModel;
import gov.va.isaac.gui.enhancedsearchview.resulthandler.ResultsToTaxonomy;
import gov.va.isaac.gui.enhancedsearchview.searchresultsfilters.SearchResultsFilterHelper;
import gov.va.isaac.search.CompositeSearchResult;
import gov.va.isaac.search.SearchBuilder;
import gov.va.isaac.search.SearchHandle;
import gov.va.isaac.search.SearchHandleBuilder;
import gov.va.isaac.search.SearchResultsFilterException;
import gov.va.isaac.search.SearchResultsIntersectionFilter;
import gov.va.isaac.util.OTFUtility;
import gov.va.isaac.util.TaskCompleteCallback;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import org.apache.mahout.math.Arrays;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;

public class TextSearchTypeModel extends SearchTypeModel implements TaskCompleteCallback {
	private final ObjectProperty<SearchTypeFilter<?>> searchTypeFilterProperty = new SimpleObjectProperty<SearchTypeFilter<?>>();
	private final ObservableList<NonSearchTypeFilter<? extends NonSearchTypeFilter<?>>> filters = FXCollections.observableArrayList();
	private SearchHandle ssh = null;

	public TextSearchTypeModel() {
		viewCoordinateProperty.addListener(new ChangeListener<ViewCoordinate>() {
			@Override
			public void changed(
					ObservableValue<? extends ViewCoordinate> observable,
					ViewCoordinate oldValue, ViewCoordinate newValue) {	
				isSearchTypeRunnableProperty.set(isValidSearch());
				isSearchTypeSavableProperty.set(isSavableSearch());
			}
		});

		searchTypeFilterProperty.addListener(new ChangeListener<SearchTypeFilter<?>>() {
			@Override
			public void changed(
					ObservableValue<? extends SearchTypeFilter<?>> observable,
							SearchTypeFilter<?> oldValue, SearchTypeFilter<?> newValue) {
				if (newValue != null) {
					newValue.isValidProperty().addListener(new ChangeListener<Boolean>() {
						@Override
						public void changed(
								ObservableValue<? extends Boolean> observable,
								Boolean oldValue, Boolean newValue) {
							isSearchTypeRunnableProperty.set(isValidSearch());
							isSearchTypeSavableProperty.set(isSavableSearch());
						}
					});
				}
				isSearchTypeRunnableProperty.set(isValidSearch());
				isSearchTypeSavableProperty.set(isSavableSearch());
			}
		});

		if (searchTypeFilterProperty.get() != null) {
			searchTypeFilterProperty.get().isValidProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(
						ObservableValue<? extends Boolean> observable,
						Boolean oldValue, Boolean newValue) {
					isSearchTypeRunnableProperty.set(isValidSearch());
					isSearchTypeSavableProperty.set(isSavableSearch());
				}
			});
		}

		filters.addListener(new ListChangeListener<NonSearchTypeFilter<?>>() {
			@Override
			public void onChanged(
					javafx.collections.ListChangeListener.Change<? extends NonSearchTypeFilter<?>> c) {

				isSearchTypeRunnableProperty.set(isValidSearch());

				while (c.next()) {
					if (c.wasPermutated()) {
						// irrelevant
						//	                     for (int i = c.getFrom(); i < c.getTo(); ++i) {
						//	                          //permutate
						//	                     }
					} else if (c.wasUpdated()) {
						// irrelevant
					} else {
						//	                     for (NonSearchTypeFilter remitem : c.getRemoved()) {
						//	                         remitem.remove(Outer.this);
						//	                     }
						for (NonSearchTypeFilter<?> additem : c.getAddedSubList()) {
							additem.isValidProperty().addListener(new ChangeListener<Boolean>() {
								@Override
								public void changed(
										ObservableValue<? extends Boolean> observable,
										Boolean oldValue, Boolean newValue) {
									isSearchTypeRunnableProperty.set(isValidSearch());
									isSearchTypeSavableProperty.set(isSavableSearch());
								}
							});
						}
					}
				}
			}
		});

		isSearchTypeRunnableProperty.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable,
					Boolean oldValue, Boolean newValue) {
				if (SearchModel.getSearchTypeSelector().getTypeSpecificModel() == TextSearchTypeModel.this) {
					SearchModel.isSearchRunnableProperty().set(newValue);
				}
			}
		});
		isSearchTypeSavableProperty.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable,
					Boolean oldValue, Boolean newValue) {
				if (SearchModel.getSearchTypeSelector().getTypeSpecificModel() == TextSearchTypeModel.this) {
					SearchModel.isSearchSavableProperty().set(newValue);
				}
			}
		});
	}
	
	public static ComponentSearchType getCurrentComponentSearchType() {
		return TextSearchTypeView.getCurrentComponentSearchType();
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
	@Override
	public void typeSpecificCopy(SearchTypeModel other) {
		filters.clear();
		filters.addAll(((TextSearchTypeModel)other).getFilters());
		searchTypeFilterProperty.set(((TextSearchTypeModel)other).getSearchType());
	}

	@Override
	public String getModelDisplayString() {
		return ", searchTypeFilter=" + searchTypeFilterProperty.get() 
				+ ", filter=" + Arrays.toString(filters.toArray());
	}

	@Override
	public String getValidationFailureMessage() {
		if (getSearchType() == null) {
			return "No SearchTypeFilter specified";
		} else if (viewCoordinateProperty.get() == null) {
			return "Invalid (null) ViewCoordinate";
		} else if (getInvalidFilters().size() > 0) {
			return "Found " + getInvalidFilters().size() + " invalid filter(s): " + Arrays.toString(getFilters().toArray());
		} else if (! getSearchType().isValid() && getValidFilters().size() == 0) {
			return getSearchType() + " filter is unset/invalid and no other valid filters are set";
		}

		return null;
	}

	@Override
	public void executeSearch(ResultsType resultsType, String modelMaxResults) {

		SearchTypeFilter<?> filter = getSearchType();

		if (resultsType == ResultsType.DESCRIPTION && (filter.getSearchParameter() == null || filter.getSearchParameterProperty().isEmpty().get())) {
			String title = "Search failed";

			String msg = "Cannot search on filters and select to return Descriptions.  Must return Concepts instead";
			getSearchRunning().set(false);
			AppContext.getCommonDialogs().showErrorDialog(title, "Failure to search filters-only", msg, AppContext.getMainApplicationWindow().getPrimaryStage());

			return;
		} else  if (! (filter instanceof SearchTypeFilter)) {
			String title = "Search failed";

			String msg = "SearchTypeFilter " + filter.getClass().getName() + " not supported";
			getSearchRunning().set(false);
			AppContext.getCommonDialogs().showErrorDialog(title, msg, "Only SearchTypeFilter LuceneSearchTypeFilter and RegExpSearchFilter currently supported", AppContext.getMainApplicationWindow().getPrimaryStage());

			return;
		}

		Function<List<CompositeSearchResult>, List<CompositeSearchResult>> searchResultsFilter = null;
		if (getFilters() != null) {
			List<Function<List<CompositeSearchResult>, List<CompositeSearchResult>>> searchResultsFilters = new ArrayList<>();

			try {
				for (NonSearchTypeFilter<?> nonSearchTypeFilter : getFilters()) {
					Function<List<CompositeSearchResult>, List<CompositeSearchResult>> newSearchResultsFilter = 
							SearchResultsFilterHelper.createSearchResultsFilter(nonSearchTypeFilter);

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
				taskComplete(0, null);

				return;
			}
		}

		// "we get called back when the results are ready."
		switch (resultsType) {
		case CONCEPT:
		{
			SearchBuilder builder = SearchBuilder.conceptDescriptionSearchBuilder(filter.getSearchParameter() != null ? filter.getSearchParameter() : "");
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

			if (getSearchType().getSearchParameter() != null && !getSearchType().getSearchParameter().isEmpty()) {
				ssh = SearchHandleBuilder.descriptionSearch(builder);
			} else {
				builder.setQuery("");
				
				Function<List<CompositeSearchResult>, List<CompositeSearchResult>> filterSupplier = 
						new Function<List<CompositeSearchResult>, List<CompositeSearchResult>>()
				{
					@Override
					public List<CompositeSearchResult> apply(List<CompositeSearchResult> t)
					{
						Set<Integer> filterList = new HashSet<Integer>();
						for (NonSearchTypeFilter<? extends NonSearchTypeFilter<?>> f : filters) {
							filterList = f.gatherNoSearchTermCaseList(filterList);
						}

						Set<CompositeSearchResult> filterCompositeSearchResultList = new HashSet<CompositeSearchResult>();

						for (Integer c : filterList) {
							filterCompositeSearchResultList.add(new CompositeSearchResult(OTFUtility.getConceptVersion(c), 0));
						}
						
						t.addAll(filterCompositeSearchResultList);
						return t;
					}
				};
				
				if (builder.getFilter() == null)
				{
					builder.setFilter(filterSupplier);
				}
				else if (builder.getFilter() instanceof SearchResultsIntersectionFilter)
				{
					((SearchResultsIntersectionFilter)builder.getFilter()).getFilters().add(filterSupplier);
				}
				else
				{
					builder.setFilter(new SearchResultsIntersectionFilter(builder.getFilter(), filterSupplier));
				}

				ssh = SearchHandleBuilder.descriptionSearch(builder);
			}
			break;
		}
		case DESCRIPTION:
		{
			SearchBuilder builder = SearchBuilder.descriptionSearchBuilder(filter.getSearchParameter() != null ? filter.getSearchParameter() : "");
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
			ssh = SearchHandleBuilder.descriptionSearch(builder);
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
	@Override
	public void taskComplete(long taskStartTime, Integer taskId) {
		if (taskId == Tasks.SEARCH.ordinal()) {
			// Run on JavaFX thread.
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					try {
						if (! ssh.isCancelled()) {
							setResults(ssh.getResults());
							
							/*
							Collection<CompositeSearchResult> results = ssh.getResults();
							SearchModel.removeNullResults(results);
							SearchModel.getSearchResultsTable().getResults().setItems(FXCollections.observableArrayList(results));
							//SearchModel.getSearchResultsTable().getResults().setItems(FXCollections.observableArrayList(ssh.getResults()));
							
							bottomPane.refreshBottomPanel();
							bottomPane.refreshTotalResultsSelectedLabel();

							if (splitPane.getItems().contains(taxonomyPane)) {
								ResultsToTaxonomy.resultsToSearchTaxonomy();
							}
							*/
						}
					} catch (Exception ex) {
						getSearchRunning().set(false);
						String title = "Unexpected Search Error";
						LOG.error(title, ex);
						AppContext.getCommonDialogs().showErrorDialog(title,
								"There was an unexpected error running the search",
								ex.toString(), AppContext.getMainApplicationWindow().getPrimaryStage());
						//searchResultsTable.getItems().clear();
						bottomPane.refreshBottomPanel();
						bottomPane.refreshTotalResultsSelectedLabel();
					} finally {
						getSearchRunning().set(false);
					}
				}
			});
		}
	}
}
