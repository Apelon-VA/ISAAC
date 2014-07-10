package gov.va.isaac.gui.enhancedsearchview.model;

import java.util.Comparator;

import gov.va.isaac.search.CompositeSearchResult;


public interface SearchStrategyI<T> {
	String getSearchTextParameter();
	void setSearchTextParameter(String text);

	public abstract Comparator<T> getComparator();
	public abstract void setComparator(Comparator<T> comparator);
	
	public abstract SearchResultsFilterI getSearchResultsFilter();
	public abstract void setSearchResultsFilter(SearchResultsFilterI filter);

	public abstract T transform(CompositeSearchResult result);

	boolean isValid();
	
	void search();
}