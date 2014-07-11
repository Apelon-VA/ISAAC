package gov.va.isaac.gui.enhancedsearchview.model;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.search.CompositeSearchResult;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.WBUtility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.ihtsdo.otf.query.lucene.LuceneDescriptionIndexer;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionAnalogBI;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.model.index.service.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractLuceneSearchStrategy<T> implements SearchStrategyI<T> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractLuceneSearchStrategy.class);

	final List<T> userResults;

    private Comparator<T> comparator;
    private List<SearchResultsFilterI> filters;
	private String searchTextParameter;

	public AbstractLuceneSearchStrategy(List<T> resultsList) {
		userResults = resultsList;
	}
	public AbstractLuceneSearchStrategy() {
		userResults = new ArrayList<>();
	}
	
	@Override
	public synchronized void search() {
		// Just strip out parens, which are common in FSNs, but also lucene search operators (which our users likely won't use)
		String query = searchTextParameter;
        query = query.replaceAll("\\(", "");
		query = query.replaceAll("\\)", "");
		
		final String localQuery = query;
		BdbTerminologyStore dataStore = ExtendedAppContext.getDataStore();
		HashMap<Integer, CompositeSearchResult> tempUserResults = new HashMap<>();
		try
		{
			if (localQuery.length() > 0)
			{
				// If search query is an ID, look up concept and add the result.
				if (Utility.isUUID(localQuery) || Utility.isLong(localQuery))
				{
					ConceptVersionBI temp = WBUtility.lookupIdentifier(localQuery);
					if (temp != null)
					{
						CompositeSearchResult gsr = new CompositeSearchResult(temp.getConceptNid(), 2.0f, temp);
						gsr.addMatchingString(localQuery);
						tempUserResults.put(temp.getConceptNid(), gsr);
					}
				}

				LOG.debug("Lucene Search: '" + localQuery + "'");

				LuceneDescriptionIndexer descriptionIndexer = AppContext.getService(LuceneDescriptionIndexer.class);
				if (descriptionIndexer == null)
				{
					LOG.warn("No description indexer found, aborting.");
				}
				else
				{
					// Look for description matches.
					ComponentProperty field = ComponentProperty.DESCRIPTION_TEXT;
					int limit = 1000;
					List<SearchResult> searchResults = descriptionIndexer.query(localQuery, false, field, limit, Long.MIN_VALUE);
					final int resultCount = searchResults.size();
					LOG.debug(resultCount + " prefiltered results");

					if (resultCount > 0)
					{
						// Compute the max score of all results.
						float maxScore = 0.0f;
						for (SearchResult searchResult : searchResults)
						{
							float score = searchResult.getScore();
							if (score > maxScore)
							{
								maxScore = score;
							}
						}

						for (SearchResult searchResult : searchResults) {
							// Get the description object.
							ComponentVersionBI cc = dataStore.getComponent(searchResult.getNid()).getVersion(WBUtility.getViewCoordinate());

							// Create a search result for the corresponding concept.
							final int conceptNid = cc.getConceptNid();
							CompositeSearchResult gsr = tempUserResults.get(conceptNid);
							
							if (gsr == null)
							{
								ConceptVersionBI concept = dataStore.getConceptVersion(WBUtility.getViewCoordinate(), cc.getConceptNid());

								// "normalize the scores between 0 and 1"
								float normScore = (searchResult.getScore() / maxScore);
								gsr = new CompositeSearchResult(conceptNid, normScore, concept);
								tempUserResults.put(conceptNid, gsr);
							}

							// Set the matching string.
							String matchingString = null;
							
							if (cc instanceof DescriptionAnalogBI)
							{
								matchingString = ((DescriptionAnalogBI<?>) cc).getText();
								//descTypeNid = ((DescriptionAnalogBI<?>) cc).getTypeNid();
							}
							else
							{
								LOG.error("Unexpected type returned from search: " + cc.getClass().getName());
								matchingString = "oops";
							}
							gsr.addMatchingString(matchingString);
							gsr.getComponents().add(cc);
						}
					}
				}
			}

			// sort results
			userResults.clear();
			if (userResults instanceof ArrayList) {
				((ArrayList<T>)userResults).ensureCapacity(tempUserResults.size());
			}
			for (CompositeSearchResult result : tempUserResults.values()) {
				CompositeSearchResult tempResult = result;
				if (filters != null) {
					for (SearchResultsFilterI filter : filters) {
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
		catch (Exception ex)
		{
			LOG.error(ex.getClass().getName() + " exception \"" + ex.getLocalizedMessage() + "\" thrown during lucene search", ex);
		}
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
	public void setSearchResultsFilters(List<SearchResultsFilterI> filter) {
		this.filters = filter;
	}

	@Override
	public List<SearchResultsFilterI> getSearchResultsFilters() {
		return filters;
	}

	@Override
	public void setComparator(
			Comparator<T> comparator) {
		this.comparator = comparator;
	}

	@Override
	public Comparator<T> getComparator() {
		return this.comparator;
	}
	
	@Override
	public abstract T transform(CompositeSearchResult result);
}