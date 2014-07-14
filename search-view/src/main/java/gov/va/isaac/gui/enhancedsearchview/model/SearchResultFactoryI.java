package gov.va.isaac.gui.enhancedsearchview.model;

import gov.va.isaac.search.CompositeSearchResult;

public interface SearchResultFactoryI<T> {
	T transform(CompositeSearchResult result);
}
