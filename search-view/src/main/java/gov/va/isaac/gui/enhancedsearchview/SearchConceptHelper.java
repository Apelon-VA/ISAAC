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

/**
 * SearchConceptHelper
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.gui.enhancedsearchview;

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.gui.enhancedsearchview.SearchViewModel.Filter;
import gov.va.isaac.gui.enhancedsearchview.SearchViewModel.LuceneFilter;
import gov.va.isaac.gui.enhancedsearchview.SearchViewModel.RegExpFilter;
import gov.va.isaac.gui.enhancedsearchview.SearchViewModel.SingleStringParameterFilter;
import gov.va.isaac.util.WBUtility;

import java.beans.PropertyVetoException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import javax.naming.InvalidNameException;

import org.ihtsdo.otf.tcc.api.blueprint.ConceptAttributeAB;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDynamicCAB;
import org.ihtsdo.otf.tcc.api.blueprint.TerminologyBuilderBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.metadata.binding.Search;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicUsageDescription;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicByteArrayBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicIntegerBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicStringBI;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.RefexDynamicData;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.RefexDynamicUsageDescriptionBuilder;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexByteArray;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexInteger;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SearchConceptHelper
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public class SearchConceptHelper {
	public static class SearchConceptException extends Exception {
		private static final long serialVersionUID = 1L;

		public SearchConceptException(String message, Throwable cause,
				boolean enableSuppression, boolean writableStackTrace) {
			super(message, cause, enableSuppression, writableStackTrace);
		}

		public SearchConceptException(String message, Throwable cause) {
			super(message, cause);
		}

		public SearchConceptException(String message) {
			super(message);
		}

		public SearchConceptException(Throwable cause) {
			super(cause);
		}
	}
	
	private static final Logger LOG = LoggerFactory.getLogger(SearchConceptHelper.class);

	public static ConceptChronicleBI buildSearchConcept(
			String saveConceptFSN,
			String saveConceptPT,
			SearchViewModel model) throws SearchConceptException
	{
		LOG.debug("buildSearchConcept(): saving model for search fsn=\"" + saveConceptFSN + "\", pt=\"" + saveConceptPT + "\": " + model);
		//
		// Construct new containing Search Concept
		//
		// Construct Search Global Attributes refex containing vc (View Coordinate)
		//		Populate vc from GUI 
		//
		// Add Search Global Attributes refex to containing Search Concept
		//
		// For each Filter spec in GUI
		//		Construct appropriate new Search {Lucene|RegExp} Filter refex containing param
		//			Populate param from Filter spec in GUI
		//			Construct new nested Search Filter Attributes refex containing order
		//				Populate nested Search Filter Attributes refex with order from GUI
		//			Nest nested Search Filter Attributes refex within Search {Lucene|RegExp} Filter refex
		//
		//		Add Search {Lucene|RegExp} Filter refex to containing Search Concept
		//

		try {
			ConceptChronicleBI searchConcept = WBUtility.createNewConcept(WBUtility.getConceptVersion(Search.SEARCH_PERSISTABLE.getUuids()[0]), saveConceptFSN, saveConceptPT);
			ConceptAttributeAB conceptAttributeBlueprintAmender = new ConceptAttributeAB(searchConcept.getConceptNid(), searchConcept.getVersion(WBUtility.getViewCoordinate()).getConceptAttributesActive().isDefined(), RefexDirective.INCLUDE); //bp.getConceptAttributeAB();

			{
				// Start with Search Global Attributes
				RefexDynamicUsageDescription searchGlobalAttributesRDUD = RefexDynamicUsageDescriptionBuilder.readRefexDynamicUsageDescriptionConcept(Search.SEARCH_GLOBAL_ATTRIBUTES.getNid());
				
				// Add View Coordinate byte[]
				RefexDynamicData[] searchGlobalAttributesData = new RefexDynamicData[searchGlobalAttributesRDUD.getColumnInfo().length];

				// Serialize passed View Coordinate into byte[]
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(output);
				model.getViewCoordinate().writeExternal(oos);
				oos.flush();

				// Construct and populate RefexDynamicData for View Coordinate
				RefexDynamicData viewCoordinateColumnData = new RefexByteArray(output.toByteArray(), RefexDynamicUsageDescription.read(Search.SEARCH_GLOBAL_ATTRIBUTES.getNid()).getColumnInfo()[0].getColumnName());
				searchGlobalAttributesData[0] = viewCoordinateColumnData;
				RefexDynamicData maxResultsColumnData = new RefexInteger(model.getMaxResults(), RefexDynamicUsageDescription.read(Search.SEARCH_GLOBAL_ATTRIBUTES.getNid()).getColumnInfo()[1].getColumnName());
				searchGlobalAttributesData[1] = maxResultsColumnData;
				if (model.getDroolsExpr() != null) {
					RefexDynamicData droolsExprColumnData = new RefexString(model.getDroolsExpr(), RefexDynamicUsageDescription.read(Search.SEARCH_GLOBAL_ATTRIBUTES.getNid()).getColumnInfo()[2].getColumnName());
					searchGlobalAttributesData[2] = droolsExprColumnData;
				}

				RefexDynamicCAB globalAttributesCAB;
				// cab.addAnnotationBlueprint(annotationBlueprint); for nesting
				//if (inputType_.getRefex() == null)
				//{

				// Creates new refex
				globalAttributesCAB = new RefexDynamicCAB(searchConcept.getPrimordialUuid(), Search.SEARCH_GLOBAL_ATTRIBUTES.getUuids()[0]);
				//}
				//else
				//{
				// This only for editing existing concept
				//cab = inputType_.getRefex().makeBlueprint(WBUtility.getViewCoordinate(),IdDirective.PRESERVE, RefexDirective.INCLUDE);
				//}
				globalAttributesCAB.setData(searchGlobalAttributesData);

				conceptAttributeBlueprintAmender.addAnnotationBlueprint(globalAttributesCAB);

				TerminologyBuilderBI builder = ExtendedAppContext.getDataStore().getTerminologyBuilder(WBUtility.getEC(), WBUtility.getViewCoordinate());
				builder.construct(globalAttributesCAB);
			}

			// Handle Filters
			for (int filterIndex = 0; filterIndex < model.getFilters().size(); ++filterIndex) {
				Filter currentFilter = model.getFilters().get(filterIndex);

				if (currentFilter instanceof SingleStringParameterFilter) {
					SingleStringParameterFilter singleStringParameterFilter = (SingleStringParameterFilter)currentFilter;

					RefexDynamicUsageDescription filterRDUD = null;

					ConceptSpec filterConceptSpec = Search.SEARCH_LUCENE_FILTER;
					if (singleStringParameterFilter instanceof LuceneFilter) {
						filterConceptSpec = Search.SEARCH_LUCENE_FILTER;
					} else if (singleStringParameterFilter instanceof RegExpFilter) {
						filterConceptSpec = Search.SEARCH_REGEXP_FILTER;
					} else {
						throw new SearchConceptException("Unsupported SingleStringParameterFilter type " + singleStringParameterFilter.getClass().getName());
					}

					filterRDUD = RefexDynamicUsageDescriptionBuilder.readRefexDynamicUsageDescriptionConcept(filterConceptSpec.getNid());

					// First create Filter, which has a single String param plus common Filter attributes
					RefexDynamicData[] filterRefexData = new RefexDynamicData[filterRDUD.getColumnInfo().length];

					// Construct and populate RefexDynamicData for search parameter
					if (singleStringParameterFilter.getSearchParameter() != null) {
						RefexDynamicData searchParameterData = new RefexString(singleStringParameterFilter.getSearchParameter(), RefexDynamicUsageDescription.read(filterConceptSpec.getNid()).getColumnInfo()[0].getColumnName());
						filterRefexData[0] = searchParameterData;
					}

					RefexDynamicCAB filterRefexCAB;
					// cab.addAnnotationBlueprint(annotationBlueprint); for nesting
					//if (inputType_.getRefex() == null)
					//{

					// Creates new refex
					filterRefexCAB = new RefexDynamicCAB(searchConcept.getPrimordialUuid(), filterConceptSpec.getUuids()[0]);
					//}
					//else
					//{
					// This only for editing existing concept
					//cab = inputType_.getRefex().makeBlueprint(WBUtility.getViewCoordinate(),IdDirective.PRESERVE, RefexDirective.INCLUDE);
					//}
					filterRefexCAB.setData(filterRefexData);

					conceptAttributeBlueprintAmender.addAnnotationBlueprint(filterRefexCAB);

					// Handle Search Filter Attributes for Filter
					RefexDynamicUsageDescription nestedFilterAttributesRDUD = RefexDynamicUsageDescriptionBuilder.readRefexDynamicUsageDescriptionConcept(Search.SEARCH_FILTER_ATTRIBUTES.getNid());

					// First create Filter, which has a single String param plus common Filter attributes
					RefexDynamicData[] nestedFilterAttributesRefexData = new RefexDynamicData[nestedFilterAttributesRDUD.getColumnInfo().length];

					// Construct and populate RefexDynamicData for search parameter
					RefexDynamicData filterOrderData = new RefexInteger(filterIndex, RefexDynamicUsageDescription.read(Search.SEARCH_FILTER_ATTRIBUTES.getNid()).getColumnInfo()[0].getColumnName());
					nestedFilterAttributesRefexData[0] = filterOrderData;

					RefexDynamicCAB nestedFilterAttributesCAB;
					// cab.addAnnotationBlueprint(annotationBlueprint); for nesting
					//if (inputType_.getRefex() == null)
					//{

					// Creates new refex
					nestedFilterAttributesCAB = new RefexDynamicCAB(filterRefexCAB.getComponentUuid(), Search.SEARCH_FILTER_ATTRIBUTES.getUuids()[0]);
					//}
					//else
					//{
					// This only for editing existing concept
					//cab = inputType_.getRefex().makeBlueprint(WBUtility.getViewCoordinate(),IdDirective.PRESERVE, RefexDirective.INCLUDE);
					//}
					nestedFilterAttributesCAB.setData(nestedFilterAttributesRefexData);

					filterRefexCAB.addAnnotationBlueprint(nestedFilterAttributesCAB);
					
					TerminologyBuilderBI builder = ExtendedAppContext.getDataStore().getTerminologyBuilder(WBUtility.getEC(), WBUtility.getViewCoordinate());
					builder.construct(filterRefexCAB);
							
				} else {
					throw new SearchConceptException("Unsupported Filter type " + currentFilter.getClass().getName());
				}
			}
			
			return searchConcept;
		} catch (IOException | InvalidCAB | ContradictionException | PropertyVetoException e) {
			throw new SearchConceptException(e.getLocalizedMessage(), e);
		}
	}
	
	public static void buildAndSaveSearchConcept(
			String saveConceptFSN,
			String saveConceptPT,
			SearchViewModel model) throws SearchConceptException
	{
		LOG.debug("buildAndSaveSearchConcept(): saving concept for search fsn=\"" + saveConceptFSN + "\", pt=\"" + saveConceptPT + "\": " + model);

		try {
			ConceptChronicleBI searchConcept = buildSearchConcept(saveConceptFSN, saveConceptPT, model);
			
			ExtendedAppContext.getDataStore().addUncommitted(searchConcept);
			ExtendedAppContext.getDataStore().commit(searchConcept);

		} catch (IOException e) {
			throw new SearchConceptException(e.getLocalizedMessage(), e);
		}
	}
	
	private static void loadEmbeddedSearchFilterAttributes(RefexDynamicVersionBI<?> refex, Map<Integer, Collection<Filter>> filterOrderMap, Filter newFilter) throws InvalidNameException, IndexOutOfBoundsException, IOException, ContradictionException {
		LOG.debug("Loading data into model from embedded Search Filter Attributes refex");

		// Now read SEARCH_FILTER_ATTRIBUTES refex column
		for (RefexDynamicVersionBI<?> embeddedRefex : refex.getRefexesDynamicActive(WBUtility.getViewCoordinate())) {
			DynamicRefexHelper.displayDynamicRefex(embeddedRefex);
			
			RefexDynamicUsageDescription embeddedRefexDUD = null;
			try {
				embeddedRefexDUD = embeddedRefex.getRefexDynamicUsageDescription();
			} catch (IOException | ContradictionException e) {
				LOG.error("Failed performing getRefexDynamicUsageDescription() on embedded refex: caught " + e.getClass().getName() + " \"" + e.getLocalizedMessage() + "\"", e);
				
				return;
			}
			
			if (embeddedRefexDUD.getRefexName().equals(Search.SEARCH_FILTER_ATTRIBUTES.getDescription() /*"Search Filter Attributes"*/)) {
				RefexDynamicIntegerBI filterOrderCol = (RefexDynamicIntegerBI)embeddedRefex.getData(Search.SEARCH_FILTER_ATTRIBUTES_FILTER_ORDER_COLUMN.getDescription());
				if (filterOrderMap.get(filterOrderCol.getDataInteger()) == null) {
					filterOrderMap.put(filterOrderCol.getDataInteger(), new ArrayList<>());
				}
				filterOrderMap.get(filterOrderCol.getDataInteger()).add(newFilter);
				
				LOG.debug("Read Integer filter order from " + embeddedRefexDUD.getRefexName() + " refex: \"" + filterOrderCol.getDataInteger() + "\"");
			} else {
				LOG.warn("Encountered unexpected embedded refex \"" + embeddedRefexDUD.getRefexName() + "\". Ignoring...");
			}
		}
	}
	
	public static SearchViewModel loadSavedSearch(SearchDisplayConcept displayConcept) throws SearchConceptException {
		LOG.info("loadSavedSearch(" + displayConcept + ")");

		SearchViewModel model = null;

		try {
			ConceptVersionBI matchingConcept = WBUtility.getConceptVersion(displayConcept.getNid());

			if (matchingConcept != null) {
				LOG.debug("loadSavedSearch(): savedSearchesComboBox has concept: " + matchingConcept);

				Map<Integer, Collection<Filter>> filterOrderMap = new TreeMap<>();
				
				model = new SearchViewModel();
				
				try {
					LOG.debug("loadSavedSearch(): concept \"" + displayConcept + "\" all refexes: " +  matchingConcept.getRefexes().size());
					LOG.debug("loadSavedSearch(): concept \"" + displayConcept + "\" all dynamic refexes: " +  matchingConcept.getRefexesDynamic().size());
					LOG.debug("loadSavedSearch(): concept \"" + displayConcept + "\" active dynamic refexes (StandardViewCoordinates.getWbAuxiliary()): " +  matchingConcept.getRefexesDynamicActive(StandardViewCoordinates.getWbAuxiliary()).size());
					LOG.debug("loadSavedSearch(): concept \"" + displayConcept + "\" active dynamic refexes (WBUtility.getViewCoordinate()): " +  matchingConcept.getRefexesDynamicActive(WBUtility.getViewCoordinate()).size());
				} catch (IOException e) {
					LOG.warn("Failed displaying attached refexes. Caught " + e.getClass().getName() + " \"" + e.getLocalizedMessage() + "\"");
					e.printStackTrace();
				}
				
				for (RefexDynamicVersionBI<?> refex : matchingConcept.getRefexesDynamicActive(WBUtility.getViewCoordinate())) {
					DynamicRefexHelper.displayDynamicRefex(refex);
					
					RefexDynamicUsageDescription dud = null;
					try {
						dud = refex.getRefexDynamicUsageDescription();
					} catch (IOException | ContradictionException e) {
						LOG.error("Failed performing getRefexDynamicUsageDescription(): caught " + e.getClass().getName() + " \"" + e.getLocalizedMessage() + "\"", e);
						
						return null;
					}

					if (dud.getRefexName().equals(Search.SEARCH_GLOBAL_ATTRIBUTES.getDescription() /*"Search Global Attributes"*/)) {
						// handle "Search Global Attributes"
						
						LOG.debug("Loading data into model from Search Global Attributes refex");
						
						// Loading view coordinate
						RefexDynamicByteArrayBI serializedViewCoordinate = (RefexDynamicByteArrayBI)refex.getData(Search.SEARCH_GLOBAL_ATTRIBUTES_VIEW_COORDINATE_COLUMN.getDescription());
						
						// Serialize passed View Coordinate into byte[]serializedViewCoordinate.getData()
						ByteArrayInputStream input = new ByteArrayInputStream(serializedViewCoordinate.getDataByteArray());
						
						ObjectInputStream oos = new ObjectInputStream(input);
						ViewCoordinate vc = new ViewCoordinate();
						vc.readExternal(oos);
						model.setViewCoordinate(vc);
						LOG.debug("Read View Coordinate from " + dud.getRefexName() + " refex: " + model.getViewCoordinate());
		
						// Loading maxResults
						RefexDynamicIntegerBI maxResults = (RefexDynamicIntegerBI)refex.getData(Search.SEARCH_GLOBAL_ATTRIBUTES_MAX_RESULTS_COLUMN.getDescription());
						model.setMaxResults(maxResults.getDataInteger());
						LOG.debug("Read max results from " + dud.getRefexName() + " refex: " + model.getMaxResults());
						
						// Loading drools expression
						RefexDynamicStringBI droolsExpr = (RefexDynamicStringBI)refex.getData(Search.SEARCH_GLOBAL_ATTRIBUTES_DROOLS_EXPR_COLUMN.getDescription());
						model.setDroolsExpr(droolsExpr != null ? droolsExpr.getDataString() : null);
						LOG.debug("Read drools expression from " + dud.getRefexName() + " refex: " + model.getDroolsExpr());

					} else if (dud.getRefexName().equals(Search.SEARCH_LUCENE_FILTER.getDescription() /*"Search Lucene Filter"*/)) {
						// handle "Search Lucene Filter"

						LOG.debug("Loading data into model from Search Lucene Filter refex");
						
						LuceneFilter newFilter = new LuceneFilter();
						
						RefexDynamicStringBI searchParamCol = (RefexDynamicStringBI)refex.getData(Search.SEARCH_LUCENE_FILTER_PARAMETER_COLUMN.getDescription());
						newFilter.setSearchParameter(searchParamCol != null ? searchParamCol.getDataString() : null);
						LOG.debug("Read String search parameter from " + dud.getRefexName() + " refex: \"" + newFilter.getSearchParameter() + "\"");

						loadEmbeddedSearchFilterAttributes(refex, filterOrderMap, newFilter);
					} else if (dud.getRefexName().equals(Search.SEARCH_REGEXP_FILTER.getDescription() /*"Search RegExp Filter"*/)) {
						// handle "Search RegExp Filter"

						LOG.debug("Loading data into model from Search RegExp Filter refex");
						
						RegExpFilter newFilter = new RegExpFilter();
						
						RefexDynamicStringBI searchParamCol = (RefexDynamicStringBI)refex.getData(Search.SEARCH_REGEXP_FILTER_PARAMETER_COLUMN.getDescription());
						newFilter.setSearchParameter(searchParamCol != null ? searchParamCol.getDataString() : null);
						LOG.debug("Read String search parameter from " + dud.getRefexName() + " refex: \"" + newFilter.getSearchParameter() + "\"");

						loadEmbeddedSearchFilterAttributes(refex, filterOrderMap, newFilter);
					} else {
						// handle or ignore
						LOG.warn("Concept \"" + displayConcept + "\" contains unexpected refex \"" + dud.getRefexName() + "\".  Ignoring...");
					}
				}

				for (int order : filterOrderMap.keySet()) {
					model.getFilters().addAll(filterOrderMap.get(order));
				}

				LOG.debug("loadSavedSearch() loaded search view model for \"" + matchingConcept + "\": " + model);

				return model;
			} else {
				LOG.error("Failed loading saved search " + displayConcept);
				return null;
			}
		} catch (IOException | InvalidNameException | IndexOutOfBoundsException | ContradictionException | ClassNotFoundException e) {
			LOG.error("Failed loading saved search. Caught " + e.getClass().getName() + " \"" + e.getLocalizedMessage() + "\"");
			throw new SearchConceptException(e.getLocalizedMessage(), e);
		}
	}
}
