package gov.va.isaac.gui.enhancedsearchview.model;

import gov.va.isaac.search.CompositeSearchResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractLuceneSearchStrategy<T> implements SearchStrategyI<T>{
	protected static final Logger LOG = LoggerFactory.getLogger(AbstractLuceneSearchStrategy.class);

	protected SearchResultFactoryI<T> searchResultFactory;
	protected final List<T> userResults;
	protected Comparator<T> comparator;
	protected List<SearchResultFilterI> filters;
	protected String searchTextParameter;

	public AbstractLuceneSearchStrategy(SearchResultFactoryI<T> srf) {
		super();
		userResults = new ArrayList<>();
		searchResultFactory = srf;
	}
	public AbstractLuceneSearchStrategy(SearchResultFactoryI<T> srf, List<T> userResults) {
		super();
		searchResultFactory = srf;
		this.userResults = userResults;
	}

	@Override
	public void setSearchTextParameter(String text) {
		this.searchTextParameter = text;
	}

	@Override
	public String getSearchTextParameter() {
		return searchTextParameter;
	}

	@Override
	public boolean isValid() {
		return searchTextParameter != null && searchTextParameter.trim().length() > 0;
	}

	@Override
	public void setSearchResultFilters(List<SearchResultFilterI> filter) {
		this.filters = filter;
	}

	@Override
	public List<SearchResultFilterI> getSearchResultFilters() {
		return filters;
	}

	@Override
	public void setComparator(Comparator<T> comparator) {
		this.comparator = comparator;
	}

	@Override
	public Comparator<T> getComparator() {
		return this.comparator;
	}

	@Override
	public T transform(CompositeSearchResult result) {
		return searchResultFactory.transform(result);
	}
	
	protected void processResults(Collection<CompositeSearchResult> results) {
		// sort results
		userResults.clear();
		if (userResults instanceof ArrayList) {
			((ArrayList<T>)userResults).ensureCapacity(results.size());
		}
		for (CompositeSearchResult result : results) {
			CompositeSearchResult tempResult = result;
			if (filters != null) {
				for (SearchResultFilterI filter : filters) {
					if (tempResult == null) {
						break;
					} else {
						tempResult = filter.filter(tempResult);
					}
				}
				
			}
			if (tempResult != null) {
				userResults.add(transform(result));
			}
		}
		
		if (comparator != null) {
			Collections.sort(userResults, comparator /* new CompositeSearchResultComparator() */);
		}
	}
}