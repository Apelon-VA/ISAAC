package gov.va.isaac.gui.enhancedsearchview.model;

import gov.va.isaac.search.CompositeSearchResult;

public interface SearchResultsFilterI {
	CompositeSearchResult filter(CompositeSearchResult result);
}
