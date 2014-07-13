package gov.va.isaac.gui.enhancedsearchview.model;

import java.util.List;

import gov.va.isaac.search.CompositeSearchResult;

public class PerMatchLuceneSearchStrategy extends AbstractPerMatchLuceneSearchStrategy<CompositeSearchResult> {
	public PerMatchLuceneSearchStrategy() {
		super();
	}

	public PerMatchLuceneSearchStrategy(List<CompositeSearchResult> resultsList) {
		super(resultsList);
	}

	@Override
	public CompositeSearchResult transform(CompositeSearchResult result) {
		return result;
	}
}
