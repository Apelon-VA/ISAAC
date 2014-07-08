package gov.va.isaac.gui.searchview;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.search.CompositeSearchResult;
import gov.va.isaac.search.CompositeSearchResultComparator;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

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
 * EnhancedSearchViewController
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
public class EnhancedSearchViewController {
    private static final Logger LOG = LoggerFactory.getLogger(EnhancedSearchViewController.class);
   
    @FXML private Button searchButton;
    @FXML private TextField searchText;
    @FXML private BorderPane borderPane;

    public static EnhancedSearchViewController init() throws IOException {
    	// Load FXML
        URL resource = EnhancedSearchViewController.class.getResource("EnhancedSearchView.fxml");
        FXMLLoader loader = new FXMLLoader(resource);
        loader.load();
        return loader.getController();
    }
    
    private boolean isSearchTextValid() {
    	return searchText.getText().trim().length() > 0;
    }

    @FXML
    public void initialize() {
        // search when button pressed
        searchButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
            	if (isSearchTextValid()) {
                    search();
                }
            }
        });

        // search on Enter
        searchText.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (isSearchTextValid()) {
                    search();
                }
            }
        });
    }
    
    public BorderPane getRoot() {
        return borderPane;
    }
    
    private synchronized void search() {
		// Just strip out parens, which are common in FSNs, but also lucene search operators (which our users likely won't use)
		String query = searchText.getText();
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
							}
							else
							{
								LOG.error("Unexpected type returned from search: " + cc.getClass().getName());
								matchingString = "oops";
							}
							gsr.addMatchingString(matchingString);
						}
					}
				}
			}

			// sort results
			ArrayList<CompositeSearchResult> userResults = new ArrayList<>(tempUserResults.size());
			userResults.addAll(tempUserResults.values());
			Collections.sort(userResults, new CompositeSearchResultComparator());

			for (CompositeSearchResult result : userResults) {

				final ConceptVersionBI wbConcept = result.getConcept();
                final String preferredText = wbConcept.getPreferredDescription().getText();
				System.out.println(wbConcept.toUserString() + " (" + result.getConceptNid() + "):");

                for (String matchString : result.getMatchStrings()) {
                    if (! matchString.equals(preferredText)) {
        				System.out.println("\t" + matchString);
                    }
                }
			}
		}
		catch (Exception ex)
		{
			LOG.error(ex.getClass().getName() + " exception \"" + ex.getLocalizedMessage() + "\" thrown during lucene search", ex);
		}
    }
}