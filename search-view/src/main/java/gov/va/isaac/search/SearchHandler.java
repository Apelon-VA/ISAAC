package gov.va.isaac.search;

import gov.va.isaac.util.Utility;
import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.description.DescriptionAnalogBI;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;
import org.ihtsdo.otf.tcc.model.index.service.IndexerBI;
import org.ihtsdo.otf.tcc.model.index.service.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for handling the ISAAC search functionality.
 * <p>
 * Logic has been mostly copied from LEGO {@code WBDataStore#search}.
 * Original author comments are in "quotes".
 *
 * @author ocarlsen
 */
public class SearchHandler {

    /**
     * Callback interface.
     */
    public static interface Callback {
        public void taskComplete(long taskStartTime, Integer taskId);
    }

    private static final Logger LOG = LoggerFactory.getLogger(SearchHandler.class);

    private final BdbTerminologyStore dataStore;

    private IndexerBI descriptionIndexer;

    public SearchHandler(BdbTerminologyStore dataStore) {
        super();
        this.dataStore = dataStore;
    }

    /**
     * "Logs an error and returns no results if a local database is not
     * available. Otherwise, returns results sorted by score."
     */
    public SearchHandle descriptionSearch(String query, Callback callback) {
        return doSearch(query, Integer.MAX_VALUE, false, callback, null);
    }

    private SearchHandle doSearch(String query, final int resultLimit,
            final boolean prefixSearch, final Callback callback, final Integer taskId) {
        final SearchHandle searchHandle = new SearchHandle();

        if (! prefixSearch) {
            // "Just strip out parens, which are common in FSNs, but also lucene
            // search operators (which our users likely won't use)"
            query = query.replaceAll("\\(", "");
            query = query.replaceAll("\\)", "");
        }

        final String localQuery = query;

        // Do search in background.
        Runnable r = new Runnable() {

            @Override
            public void run() {
                HashMap<Integer, GuiSearchResult> tempUserResults = new HashMap<>();
                try {
                    if (localQuery.length() > 0) {

                        // If search query is an ID, look up concept and add the result.
                        if (Utility.isUUID(localQuery) || Utility.isLong(localQuery)) {
                           ConceptVersionBI temp = WBUtility.lookupSnomedIdentifierAsCV(localQuery);
                            if (temp != null) {
                                GuiSearchResult gsr = new GuiSearchResult(temp.getConceptNid(), 2.0f, temp);
                                gsr.addMatchingString(localQuery);
                                tempUserResults.put(temp.getConceptNid(), gsr);
                            }
                        }

                        LOG.debug("Lucene Search: '" + localQuery + "'");

                        IndexerBI descriptionIndexer = getDescriptionIndexer();
                        if (descriptionIndexer == null) {
                            LOG.warn("No description indexer found, aborting.");
                        } else {

                            // Look for description matches.
                            ComponentProperty field = ComponentProperty.DESCRIPTION_TEXT;
                            int limit = 1000;
                            List<SearchResult> searchResults = descriptionIndexer.query(localQuery, field, limit);
                            final int resultCount = searchResults.size();
                            LOG.debug(resultCount + " results");

                            if (resultCount > 0) {
                                // Compute the max score of all results.
                                float maxScore = 0.0f;
                                for (SearchResult searchResult : searchResults) {
                                    float score = searchResult.getScore();
                                    if (score > maxScore) {
                                        maxScore = score;
                                    }
                                }

                                for (SearchResult searchResult : searchResults) {

                                    // Abort if search has been cancelled.
                                    if (searchHandle.isCancelled()) {
                                        break;
                                    }

                                    // Get the description object.
                                    ComponentChronicleBI<?> cc = dataStore.getComponent(searchResult.getNid());

                                    // Create a search result for the corresponding concept.
                                    final int conceptNid = cc.getConceptNid();
                                    GuiSearchResult gsr = tempUserResults.get(conceptNid);
                                    if (gsr == null) {

                                        // "I tried using the FXConcept API here, but the performance was dreadful
                                        // concept = WBDataStore.Ts().getFxConcept(WBDataStore.Ts().getUuidPrimordialForNid(conceptNid),
                                        // StandardViewCoordinates.getSnomedLatest());"
                                        ConceptVersionBI concept = dataStore.getConceptVersion(
                                                StandardViewCoordinates.getSnomedInferredLatest(),
                                                cc.getConceptNid());

                                        // "normalize the scores between 0 and 1"
                                        float normScore = (searchResult.getScore() / maxScore);
                                        gsr = new GuiSearchResult(conceptNid, normScore, concept);
                                        tempUserResults.put(conceptNid, gsr);
                                    }

                                    // Set the matching string.
                                    String matchingString = null;
                                    if (cc instanceof DescriptionAnalogBI) {
                                        matchingString = ((DescriptionAnalogBI<?>) cc).getText();
                                    } else {
                                        LOG.error("Unexpected type returned from search: " + cc.getClass().getName());
                                        matchingString = "oops";
                                    }
                                    gsr.addMatchingString(matchingString);

                                    // "add one to the scores when we are doing a prefix search, and it hits."
                                    if (prefixSearch && gsr.getBestScore() <= 1.0f) {
                                        float adjustValue = 0f;

                                        if (matchingString.toLowerCase().equals(localQuery.trim().toLowerCase())) {
                                            // "exact match, bump by 2"
                                            adjustValue = 2.0f;
                                        } else if (matchingString.toLowerCase().startsWith(localQuery.trim().toLowerCase())) {
                                            // "add 1, plus a bit more boost based on the length of the matches (shorter matches get more boost)"
                                            adjustValue = 1.0f + (1.0f - ((float)(matchingString.length() - localQuery.trim().length()) / (float)matchingString.length()));
                                        }

                                        if (adjustValue > 0f) {
                                            gsr.adjustScore(gsr.getBestScore() + adjustValue);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // "Now, sort the results."
                    ArrayList<GuiSearchResult> userResults = new ArrayList<>(tempUserResults.size());
                    userResults.addAll(tempUserResults.values());
                    Collections.sort(userResults, new GuiSearchResultComparator());
                    if (userResults.size() > resultLimit) {
                        searchHandle.setResults(userResults.subList(0, resultLimit - 1));
                    } else {
                        searchHandle.setResults(userResults);
                    }
                } catch (Exception ex) {
                    LOG.error("Unexpected error during lucene search", ex);
                    searchHandle.setError(ex);
                }
                callback.taskComplete(searchHandle.getSearchStartTime(), taskId);
            }
        };

        Utility.execute(r);
        return searchHandle;
    }

    private IndexerBI getDescriptionIndexer() throws IOException {
        if (descriptionIndexer == null) {
            List<IndexerBI> indexers = Hk2Looker.get().getAllServices(IndexerBI.class);
            for (IndexerBI indexer : indexers) {
                if (indexer.getIndexerName().equals("descriptions")) {
                    this.descriptionIndexer = indexer;
                }
            }
        }
        return descriptionIndexer;
    }
}
