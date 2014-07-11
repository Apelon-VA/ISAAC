package gov.va.isaac.gui.enhancedsearchview.model;

import java.util.Comparator;
import java.util.List;

import gov.va.isaac.search.CompositeSearchResult;


public interface SearchStrategyI<T> {
	String getSearchTextParameter();
	void setSearchTextParameter(String text);

	public abstract Comparator<T> getComparator();
	public abstract void setComparator(Comparator<T> comparator);
	
	public abstract List<SearchResultsFilterI> getSearchResultsFilters();
	public abstract void setSearchResultsFilters(List<SearchResultsFilterI> filters);

	public abstract T transform(CompositeSearchResult result);

	boolean isValid();
	
	void search();
}