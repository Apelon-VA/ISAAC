/**
 * Copyright Notice
 * 
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.search;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.util.TaskCompleteCallback;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.WBUtility;

import java.util.ArrayList;
import java.util.Collection;
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

/**
 * Class for handling the ISAAC search functionality.
 * <p>
 * Logic has been mostly copied from LEGO {@code WBDataStore#search}, but now rewritten and updated to use the newer
 * search APIs provided in OTF
 * 
 * @author ocarlsen
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class SearchHandler
{
	private static final Logger LOG = LoggerFactory.getLogger(SearchHandler.class);

	/**
	 * Logs an error and returns no results if a local database is not available. Otherwise, returns results sorted by score.
	 */
	public static SearchHandle conceptPrefixSearch(String query, int sizeLimit, TaskCompleteCallback callback, Integer taskId)
	{
		return doConceptSearch(query, sizeLimit, true, callback, taskId, (SearchResultsFilter)null, new CompositeSearchResultComparator());
	}

	/**
	 * Logs an error and returns no results if a local database is not available. Otherwise, returns results sorted by score.
	 */
	public static SearchHandle conceptSearch(String query, TaskCompleteCallback callback)
	{
		return doConceptSearch(query, Integer.MAX_VALUE, false, callback, (Integer)null, (SearchResultsFilter)null, new CompositeSearchResultComparator());
	}

	public static SearchHandle doConceptSearch(SearchBuilder builder) {
		return doConceptSearch(
				builder.getQuery(), 
				builder.getSizeLimit(), 
				builder.isPrefixSearch(), 
				builder.getCallback(), 
				builder.getTaskId(),
				builder.getFilter(),
				builder.getComparator());
	}
	private static SearchHandle doConceptSearch(
			String query, 
			final int resultLimit, 
			final boolean prefixSearch, 
			final TaskCompleteCallback callback, 
			final Integer taskId, 
			final SearchResultsFilter filters,
			Comparator<CompositeSearchResult> comparator)
	{
		final SearchHandle searchHandle = new SearchHandle();

		if (!prefixSearch)
		{
			// Just strip out parens, which are common in FSNs, but also lucene search operators (which our users likely won't use)
			query = query.replaceAll("\\(", "");
			query = query.replaceAll("\\)", "");
		}

		final String localQuery = query;

		// Do search in background.
		Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				//make sure the data store is loaded
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
							int limit = resultLimit;
							List<SearchResult> searchResults = descriptionIndexer.query(localQuery, prefixSearch, field, limit, Long.MIN_VALUE);
							final int resultCount = searchResults.size();
							LOG.debug(resultCount + " results");

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

								for (SearchResult searchResult : searchResults)
								{
									// Abort if search has been cancelled.
									if (searchHandle.isCancelled())
									{
										break;
									}

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
									}
									else
									{
										LOG.error("Unexpected type returned from search: " + cc.getClass().getName());
										matchingString = "oops";
									}
									gsr.addMatchingString(matchingString);
									gsr.getComponents().add(cc);

									// "add one to the scores when we are doing a prefix search, and it hits."
									if (prefixSearch && gsr.getBestScore() <= 1.0f)
									{
										float adjustValue = 0f;

										if (matchingString.toLowerCase().equals(localQuery.trim().toLowerCase()))
										{
											// "exact match, bump by 2"
											adjustValue = 2.0f;
										}
										else if (matchingString.toLowerCase().startsWith(localQuery.trim().toLowerCase()))
										{
											// "add 1, plus a bit more boost based on the length of the matches (shorter matches get more boost)"
											adjustValue = 1.0f + (1.0f - ((float) (matchingString.length() - localQuery.trim().length()) / (float) matchingString
													.length()));
										}
										if (adjustValue > 0f)
										{
											gsr.adjustScore(gsr.getBestScore() + adjustValue);
										}
									}
								}
							}
						}
					}

					// "Now, process the results."
					processResults(searchHandle, resultLimit, tempUserResults.values(), filters, comparator);
				}
				catch (Exception ex)
				{
					LOG.error("Unexpected error during lucene search", ex);
					searchHandle.setError(ex);
				}
				callback.taskComplete(searchHandle.getSearchStartTime(), taskId);
			}
		};

		Utility.execute(r);
		return searchHandle;
	}

	/**
	 * Logs an error and returns no results if a local database is not available. Otherwise, returns results sorted by score.
	 */
	public static SearchHandle descriptionPrefixSearch(String query, int sizeLimit, TaskCompleteCallback callback, Integer taskId)
	{
		return doDescriptionSearch(query, sizeLimit, true, callback, taskId, (SearchResultsFilter)null, new CompositeSearchResultComparator());
	}

	/**
	 * Logs an error and returns no results if a local database is not available. Otherwise, returns results sorted by score.
	 */
	public static SearchHandle descriptionSearch(String query, TaskCompleteCallback callback) {
		return doDescriptionSearch(query, Integer.MAX_VALUE, false, callback, (Integer)null, (SearchResultsFilter)null, new CompositeSearchResultComparator());
	}

	public static SearchHandle doDescriptionSearch(SearchBuilder builder) {
		return doDescriptionSearch(
				builder.getQuery(), 
				builder.getSizeLimit(), 
				builder.isPrefixSearch(), 
				builder.getCallback(), 
				builder.getTaskId(), 
				builder.getFilter(),
				builder.getComparator());
	}

	private static SearchHandle doDescriptionSearch(
			String query, 
			final int resultLimit, 
			final boolean prefixSearch, 
			final TaskCompleteCallback callback, 
			final Integer taskId,
			final SearchResultsFilter filters,
			Comparator<CompositeSearchResult> comparator)
	{
		final SearchHandle searchHandle = new SearchHandle();

		if (!prefixSearch)
		{
			// Just strip out parens, which are common in FSNs, but also lucene search operators (which our users likely won't use)
			query = query.replaceAll("\\(", "");
			query = query.replaceAll("\\)", "");
		}

		final String localQuery = query;

		// Do search in background.
		Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				//make sure the data store is loaded
				BdbTerminologyStore dataStore = ExtendedAppContext.getDataStore();
				List<CompositeSearchResult> tempUserResults = new ArrayList<>();
				try
				{
					if (localQuery.length() > 0)
					{
						LOG.debug("Lucene Search: '" + localQuery + "'");

						// If search query is an ID, look up concept and add the result.
						if (Utility.isUUID(localQuery) || Utility.isLong(localQuery))
						{
							ConceptVersionBI temp = WBUtility.lookupIdentifier(localQuery);
							if (temp != null)
							{
								CompositeSearchResult gsr = new CompositeSearchResult(temp.getConceptNid(), 2.0f, temp);
								gsr.addMatchingString(localQuery);
								tempUserResults.add(gsr);
							}
						}

						LuceneDescriptionIndexer descriptionIndexer = AppContext.getService(LuceneDescriptionIndexer.class);
						if (descriptionIndexer == null)
						{
							LOG.warn("No description indexer found, aborting.");
						}
						else
						{
							// Look for description matches.
							ComponentProperty field = ComponentProperty.DESCRIPTION_TEXT;
							int limit = resultLimit;
							List<SearchResult> searchResults = descriptionIndexer.query(localQuery, prefixSearch, field, limit, Long.MIN_VALUE);
							final int resultCount = searchResults.size();
							LOG.debug(resultCount + " results");

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

								for (SearchResult searchResult : searchResults)
								{
									// Abort if search has been cancelled.
									if (searchHandle.isCancelled())
									{
										break;
									}

									// Get the description object.
									ComponentVersionBI cc = dataStore.getComponent(searchResult.getNid()).getVersion(WBUtility.getViewCoordinate());

									// Create a search result for the corresponding concept.
									final int conceptNid = cc.getConceptNid();
									CompositeSearchResult gsr = null;

									ConceptVersionBI concept = dataStore.getConceptVersion(WBUtility.getViewCoordinate(), cc.getConceptNid());

									// "normalize the scores between 0 and 1"
									float normScore = (searchResult.getScore() / maxScore);
									gsr = new CompositeSearchResult(conceptNid, normScore, concept);
									tempUserResults.add(gsr);


									// Set the matching string.
									String matchingString = null;
									if (cc instanceof DescriptionAnalogBI)
									{
										matchingString = ((DescriptionAnalogBI<?>) cc).getText();
									}
									else
									{
										LOG.error("Unexpected type returned from search: " + cc.getClass().getName());
										matchingString = "oops";
									}
									gsr.addMatchingString(matchingString);
									gsr.getComponents().add(cc);

									// "add one to the scores when we are doing a prefix search, and it hits."
									if (prefixSearch && gsr.getBestScore() <= 1.0f)
									{
										float adjustValue = 0f;

										if (matchingString.toLowerCase().equals(localQuery.trim().toLowerCase()))
										{
											// "exact match, bump by 2"
											adjustValue = 2.0f;
										}
										else if (matchingString.toLowerCase().startsWith(localQuery.trim().toLowerCase()))
										{
											// "add 1, plus a bit more boost based on the length of the matches (shorter matches get more boost)"
											adjustValue = 1.0f + (1.0f - ((float) (matchingString.length() - localQuery.trim().length()) / (float) matchingString
													.length()));
										}
										if (adjustValue > 0f)
										{
											gsr.adjustScore(gsr.getBestScore() + adjustValue);
										}
									}
								}
							}
						}
					}

					processResults(searchHandle, resultLimit, tempUserResults, filters, comparator);
				}
				catch (Exception ex)
				{
					LOG.error("Unexpected error during lucene search", ex);
					searchHandle.setError(ex);
				}
				callback.taskComplete(searchHandle.getSearchStartTime(), taskId);
			}
		};

		Utility.execute(r);
		return searchHandle;
	}

	private static void processResults(SearchHandle searchHandle, int resultLimit, Collection<CompositeSearchResult> tempResults, final SearchResultsFilter filter, Comparator<CompositeSearchResult> comparator) throws SearchResultsFilterException {
		// "Now, sort the results."
		ArrayList<CompositeSearchResult> userResults = new ArrayList<>(tempResults.size());
		
		if (filter != null) {
			LOG.debug("Applying SearchResultsFilter " + filter + " to " + tempResults.size() + " search results");
			Collection<CompositeSearchResult> filteredResults = filter.filter(tempResults);

			LOG.debug(filteredResults.size() + " results remained after filtering a total of " + tempResults.size() + " search results");
			userResults.addAll(filteredResults);
		} else {
			userResults.addAll(tempResults);
		}
		
		if (comparator != null) {
			Collections.sort(userResults, comparator);
		}
		
		if (userResults.size() > resultLimit)
		{
			searchHandle.setResults(userResults.subList(0, resultLimit));
		}
		else
		{
			searchHandle.setResults(userResults);
		}
	}
}
