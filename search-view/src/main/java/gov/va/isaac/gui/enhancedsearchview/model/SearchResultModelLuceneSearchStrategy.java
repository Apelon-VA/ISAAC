package gov.va.isaac.gui.enhancedsearchview.model;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.va.isaac.search.CompositeSearchResult;

public class SearchResultModelLuceneSearchStrategy extends AbstractLuceneSearchStrategy<SearchResultModel> {
	private final Logger logger = LoggerFactory.getLogger(SearchResultModelLuceneSearchStrategy.class);

	public SearchResultModelLuceneSearchStrategy() {
		super();
	}

	public SearchResultModelLuceneSearchStrategy(
			List<SearchResultModel> resultsList) {
		super(resultsList);
	}
	
	@Override
	public SearchResultModel transform(CompositeSearchResult result) {
		SearchResultModel newModel = new SearchResultModel();

		// TODO: trim
		newModel.setId(result.getConcept().getConceptNid());
		newModel.setStatus(result.getConcept().getStatus());
		newModel.setScore(result.getBestScore());
		newModel.setUuId(result.getConcept().getPrimordialUuid().toString());
		Set<String> matchingStrings = result.getMatchStrings();
		newModel.setMatchingText(matchingStrings.toArray()[0].toString());

		try {
			newModel.setFsn(result.getConcept().getFullySpecifiedDescription().getText());
		} catch (IOException | ContradictionException e1) {
			logger.error("Error calling ConceptVersionBI.getFullySpecifiedDescription(): Caught " + e1.getClass().getName() + " \"" + e1.getLocalizedMessage() + "\"");
			e1.printStackTrace();
		}
		try {
			newModel.setPreferredTerm(result.getConcept().getPreferredDescription().getText());
		} catch (IOException | ContradictionException e) {
			logger.error("Error calling ConceptVersionBI.getPreferredDescription(): Caught " + e.getClass().getName() + " \"" + e.getLocalizedMessage() + "\"");
			e.printStackTrace();
		}
		
		return newModel;
	}
}
