package gov.va.isaac.gui.enhancedsearchview.model;

import java.util.List;

import gov.va.isaac.search.CompositeSearchResult;

public class PerConceptLuceneSearchStrategy extends AbstractPerConceptLuceneSearchStrategy<CompositeSearchResult> {
	public PerConceptLuceneSearchStrategy() {
		super();
	}

	public PerConceptLuceneSearchStrategy(List<CompositeSearchResult> resultsList) {
		super(resultsList);
	}

	@Override
	public CompositeSearchResult transform(CompositeSearchResult result) {
		return result;
	}
}
