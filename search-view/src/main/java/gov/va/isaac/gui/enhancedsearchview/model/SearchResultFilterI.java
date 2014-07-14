package gov.va.isaac.gui.enhancedsearchview.model;

import gov.va.isaac.search.CompositeSearchResult;

public interface SearchResultFilterI {
	CompositeSearchResult filter(CompositeSearchResult result);
}
