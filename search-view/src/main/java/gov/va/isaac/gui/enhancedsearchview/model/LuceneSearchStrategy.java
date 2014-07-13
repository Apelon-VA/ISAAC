package gov.va.isaac.gui.enhancedsearchview.model;

import java.util.List;

import gov.va.isaac.search.CompositeSearchResult;

public class LuceneSearchStrategy extends AbstractLuceneSearchStrategy<CompositeSearchResult> {
	public LuceneSearchStrategy() {
		super();
	}

	public LuceneSearchStrategy(List<CompositeSearchResult> resultsList) {
		super(resultsList);
	}

	@Override
	public CompositeSearchResult transform(CompositeSearchResult result) {
		return result;
	}
}
