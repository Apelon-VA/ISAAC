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
import gov.va.isaac.gui.enhancedsearchview.filters.Filter;
import gov.va.isaac.gui.enhancedsearchview.filters.Invertable;
import gov.va.isaac.gui.enhancedsearchview.filters.IsAFilter;
import gov.va.isaac.gui.enhancedsearchview.filters.IsDescendantOfFilter;
import gov.va.isaac.gui.enhancedsearchview.filters.LuceneSearchTypeFilter;
import gov.va.isaac.gui.enhancedsearchview.filters.NonSearchTypeFilter;
import gov.va.isaac.gui.enhancedsearchview.filters.RegExpSearchTypeFilter;
import gov.va.isaac.gui.enhancedsearchview.filters.SearchTypeFilter;
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
import java.util.UUID;

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
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicBooleanBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicByteArrayBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicIntegerBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicStringBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicUUIDBI;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.RefexDynamicData;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.RefexDynamicUsageDescriptionBuilder;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicBoolean;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicByteArray;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicInteger;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicString;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicUUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SearchConceptHelper
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
@SuppressWarnings("unused")
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


	public static ConceptChronicleBI buildSearchConcept(SearchViewModel model) throws SearchConceptException {
		return buildSearchConcept(model, null, null);
	}
	public static ConceptChronicleBI buildSearchConcept(
			SearchViewModel model,
			String passedSaveConceptFSN,
			String passedSaveConceptPT) throws SearchConceptException
	{
		final String saveConceptFSN = passedSaveConceptFSN != null ? passedSaveConceptFSN : model.getName();
		final String saveConceptPT = passedSaveConceptPT != null ? passedSaveConceptPT : model.getDescription();

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
				RefexDynamicData viewCoordinateColumnData = new RefexDynamicByteArray(output.toByteArray());
				searchGlobalAttributesData[0] = viewCoordinateColumnData;
				RefexDynamicData maxResultsColumnData = new RefexDynamicInteger(model.getMaxResults());
				searchGlobalAttributesData[1] = maxResultsColumnData;
				if (model.getDroolsExpr() != null) {
					RefexDynamicData droolsExprColumnData = new RefexDynamicString(model.getDroolsExpr());
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
				globalAttributesCAB.setData(searchGlobalAttributesData, WBUtility.getViewCoordinate());

				conceptAttributeBlueprintAmender.addAnnotationBlueprint(globalAttributesCAB);

				TerminologyBuilderBI builder = ExtendedAppContext.getDataStore().getTerminologyBuilder(WBUtility.getEC(), WBUtility.getViewCoordinate());
				builder.construct(globalAttributesCAB);
			}

			// Add search type filter as index 0
			addFilterToRefex(searchConcept, conceptAttributeBlueprintAmender, model.getSearchType(), 0);

			// Add additional filter as higher indices
			for (int filterIndex = 0; filterIndex < model.getFilters().size(); ++filterIndex) {
				addFilterToRefex(searchConcept, conceptAttributeBlueprintAmender, model.getFilters().get(filterIndex), filterIndex + 1);
			}
			
			LOG.debug("Displaying newly created save concept refexes");
			DynamicRefexHelper.displayDynamicRefexes(searchConcept);
			
			return searchConcept;
		} catch (IOException | InvalidCAB | ContradictionException | PropertyVetoException e) {
			throw new SearchConceptException(e.getLocalizedMessage(), e);
		}
	}

	private static void addFilterToRefex(ConceptChronicleBI searchConcept, ConceptAttributeAB conceptAttributeBlueprintAmender, Filter<? extends Filter<?>> currentFilter, int filterIndex) throws ValidationException, PropertyVetoException, IOException, ContradictionException, SearchConceptException, InvalidCAB {
		RefexDynamicUsageDescription filterRDUD = null;

		if (currentFilter instanceof SearchTypeFilter) {
			if (filterIndex > 0) {
				throw new SearchConceptException("A SearchTypeFilter Filter must be first in refex list. " + currentFilter.getClass().getName() + " is a SearchTypeFilter filter, but its specified index is " + filterIndex);
			}
		} else /* if (! (currentFilter instanceof SearchTypeFilter)) */ {
			if (filterIndex == 0) {
				throw new SearchConceptException("A non-SearchTypeFilter Filter must not be first in refex list. " + currentFilter.getClass().getName() + " is a non-SearchTypeFilter filter, but its specified index is " + filterIndex);
			}
		}

		ConceptSpec filterConceptSpec = null;
		if (currentFilter instanceof LuceneSearchTypeFilter) {
			filterConceptSpec = Search.SEARCH_LUCENE_FILTER;
		} else if (currentFilter instanceof RegExpSearchTypeFilter) {
			filterConceptSpec = Search.SEARCH_REGEXP_FILTER;
		} else if (currentFilter instanceof IsDescendantOfFilter) {
			filterConceptSpec = Search.SEARCH_ISDESCENDANTOF_FILTER;
		} else if (currentFilter instanceof IsAFilter) {
			filterConceptSpec = Search.SEARCH_ISA_FILTER;
		} else {
			throw new SearchConceptException("Unsupported Filter type " + currentFilter.getClass().getName());
		}

		filterRDUD = RefexDynamicUsageDescriptionBuilder.readRefexDynamicUsageDescriptionConcept(filterConceptSpec.getNid());

		// First create Filter, which has its own attributes plus common Filter attributes
		RefexDynamicData[] filterRefexData = new RefexDynamicData[filterRDUD.getColumnInfo().length];

		if (currentFilter instanceof LuceneSearchTypeFilter) {
			// Construct and populate RefexDynamicData for search parameter
			LuceneSearchTypeFilter filter = (LuceneSearchTypeFilter)currentFilter;

			if (filter.getSearchParameter() != null) {

				RefexDynamicData searchParameterData = new RefexDynamicString(filter.getSearchParameter());
				filterRefexData[0] = searchParameterData;
			}
		} else if (currentFilter instanceof RegExpSearchTypeFilter) {
			// Construct and populate RefexDynamicData for search parameter
			RegExpSearchTypeFilter filter = (RegExpSearchTypeFilter)currentFilter;

			if (filter.getSearchParameter() != null) {

				RefexDynamicData searchParameterData = new RefexDynamicString(filter.getSearchParameter());
				filterRefexData[0] = searchParameterData;
			}
		} else if (currentFilter instanceof IsDescendantOfFilter) {
			// Construct and populate RefexDynamicData for search ascendant uuid
			IsDescendantOfFilter isDescendantOfFilter = (IsDescendantOfFilter)currentFilter;

			if (isDescendantOfFilter.getNid() != 0) {
				UUID uuid = WBUtility.getConceptVersion(isDescendantOfFilter.getNid()).getPrimordialUuid();
				RefexDynamicData ascendantUuidData = new RefexDynamicUUID(uuid);
				filterRefexData[0] = ascendantUuidData;
			}
		} else if (currentFilter instanceof IsAFilter) {
			// Construct and populate RefexDynamicData for search ascendant uuid
			IsAFilter isAFilter = (IsAFilter)currentFilter;

			if (isAFilter.getNid() != 0) {
				UUID uuid = WBUtility.getConceptVersion(isAFilter.getNid()).getPrimordialUuid();
				RefexDynamicData matchUuidData = new RefexDynamicUUID(uuid);
				filterRefexData[0] = matchUuidData;
			}
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
		filterRefexCAB.setData(filterRefexData, WBUtility.getViewCoordinate());

		conceptAttributeBlueprintAmender.addAnnotationBlueprint(filterRefexCAB);

		// Handle Search Filter Attributes for Filter
		RefexDynamicUsageDescription nestedFilterAttributesRDUD = RefexDynamicUsageDescriptionBuilder.readRefexDynamicUsageDescriptionConcept(Search.SEARCH_FILTER_ATTRIBUTES.getNid());

		// Common Filter attributes
		RefexDynamicData[] nestedFilterAttributesRefexData = new RefexDynamicData[nestedFilterAttributesRDUD.getColumnInfo().length];

		// Construct and populate RefexDynamicData for search order
		RefexDynamicData filterOrderData = new RefexDynamicInteger(filterIndex);
		nestedFilterAttributesRefexData[0] = filterOrderData;
		// If relevant, populate with invert boolean
		if (currentFilter instanceof Invertable) {
			RefexDynamicData filterInvertData = new RefexDynamicBoolean(((Invertable)currentFilter).getInvert());
			nestedFilterAttributesRefexData[1] = filterInvertData;
		}

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
		nestedFilterAttributesCAB.setData(nestedFilterAttributesRefexData, WBUtility.getViewCoordinate());

		filterRefexCAB.addAnnotationBlueprint(nestedFilterAttributesCAB);

		TerminologyBuilderBI builder = ExtendedAppContext.getDataStore().getTerminologyBuilder(WBUtility.getEC(), WBUtility.getViewCoordinate());
		builder.construct(filterRefexCAB);
	}

	public static void buildAndSaveSearchConcept(SearchViewModel model) throws SearchConceptException {
		buildAndSaveSearchConcept(model, null, null);
	}
	public static void buildAndSaveSearchConcept(
			SearchViewModel model,
			String passedSaveConceptFSN,
			String passedSaveConceptPT) throws SearchConceptException
	{
		final String saveConceptFSN = passedSaveConceptFSN != null ? passedSaveConceptFSN : model.getName();
		final String saveConceptPT = passedSaveConceptPT != null ? passedSaveConceptPT : model.getDescription();

		LOG.debug("buildAndSaveSearchConcept(): saving concept for search fsn=\"" + saveConceptFSN + "\", pt=\"" + saveConceptPT + "\": " + model);

		try {
			ConceptChronicleBI searchConcept = buildSearchConcept(model, saveConceptFSN, saveConceptPT);

			ExtendedAppContext.getDataStore().addUncommitted(searchConcept);
			ExtendedAppContext.getDataStore().commit(searchConcept);

		} catch (IOException e) {
			throw new SearchConceptException(e.getLocalizedMessage(), e);
		}
	}

	private static <T extends SearchTypeFilter<T>> void loadEmbeddedSearchTypeFilterAttributes(RefexDynamicVersionBI<?> refex, T newFilter) throws InvalidNameException, IndexOutOfBoundsException, IOException, ContradictionException, SearchConceptException {
		loadEmbeddedSearchFilterAttributes(refex, newFilter, null);
	}

	private static <T extends NonSearchTypeFilter<T>> void loadEmbeddedSearchFilterAttributes(RefexDynamicVersionBI<?> refex, Filter<?> newFilter, Map<Integer, Collection<T>> filterOrderMap) throws InvalidNameException, IndexOutOfBoundsException, IOException, ContradictionException, SearchConceptException {
		LOG.debug("Loading data into model from embedded Search Filter Attributes refex");

		SearchTypeFilter<?> searchTypeFilter = null;
		T nonSearchTypeFilter = null;
		
		if (newFilter instanceof SearchTypeFilter) {
			searchTypeFilter = (SearchTypeFilter<?>)newFilter;
			if (filterOrderMap != null) {
				throw new SearchConceptException("Cannot call this method with a non-null filterOrderMap when passing a SearchTypeFilter. " + newFilter.getClass().getName() + " is a SearchTypeFilter filter.");
			}
		} else if (newFilter instanceof NonSearchTypeFilter) {
			nonSearchTypeFilter = (T)newFilter;
			if (filterOrderMap == null) {
				throw new SearchConceptException("Cannot call this method with a null filterOrderMap when passing a non-SearchTypeFilter. " + newFilter.getClass().getName() + " is a non-SearchTypeFilter filter.");
			}
		} else {
			throw new SearchConceptException("Unsupported Filter type " + newFilter.getClass().getName() + ". Must be of either " + SearchTypeFilter.class.getName() + " or " + NonSearchTypeFilter.class.getName());
		}
		
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
				RefexDynamicIntegerBI filterOrderCol = (RefexDynamicIntegerBI)embeddedRefex.getData(Search.ORDER_COLUMN.getDescription());

				if (newFilter instanceof SearchTypeFilter) {
					if (filterOrderCol != null && filterOrderCol.getDataInteger() > 0) {
						throw new SearchConceptException("A SearchTypeFilter Filter must be first in list. " + newFilter.getClass().getName() + " is a SearchTypeFilter filter, but its stored index is " + filterOrderCol.getDataInteger());
					}
				} else /* if (! (currentFilter instanceof SearchTypeFilter)) */ {
					if (filterOrderMap.get(filterOrderCol.getDataInteger()) == null) {
						filterOrderMap.put(filterOrderCol.getDataInteger(), new ArrayList<>());
					}
					filterOrderMap.get(filterOrderCol.getDataInteger()).add(nonSearchTypeFilter);

					if (filterOrderCol == null || filterOrderCol.getDataInteger() == 0) {
						throw new SearchConceptException("A non-SearchTypeFilter Filter must not be first in list. " + newFilter.getClass().getName() + " is a non-SearchTypeFilter filter, but its stored index is " + (filterOrderCol != null ? filterOrderCol.getDataInteger() : filterOrderCol));
					}
				}

				LOG.debug("Read Integer filter order from " + embeddedRefexDUD.getRefexName() + " refex: \"" + (filterOrderCol != null ? filterOrderCol.getDataInteger() : filterOrderCol) + "\"");

				RefexDynamicBooleanBI filterInvertCol = (RefexDynamicBooleanBI)embeddedRefex.getData(Search.FILTER_INVERT_COLUMN.getDescription());
				if (filterInvertCol != null) {
					if (newFilter instanceof Invertable) {
						((Invertable) newFilter).setInvert(filterInvertCol.getDataBoolean());
						LOG.debug("Read Boolean filter invert from " + embeddedRefexDUD.getRefexName() + " refex: \"" + filterInvertCol.getDataBoolean() + "\"");

					} else if (filterInvertCol.getDataBoolean()) {
						LOG.error("Cannot make invertable non-invertable Filter of type " + newFilter.getClass().getName());

						throw new SearchConceptException("Cannot make invertable non-invertable Filter of type " + newFilter.getClass().getName());
					}
				}
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

				Map<Integer, Collection<NonSearchTypeFilter>> filterOrderMap = new TreeMap<>();

				model = new SearchViewModel();

				model.setName(WBUtility.getFullySpecifiedName(matchingConcept));
				model.setDescription(WBUtility.getConPrefTerm(matchingConcept.getNid()));

				try {
					LOG.debug("loadSavedSearch(): concept \"" + displayConcept + "\" all refexes: " +  matchingConcept.getRefexes().size());
					LOG.debug("loadSavedSearch(): concept \"" + displayConcept + "\" all dynamic refexes: " +  matchingConcept.getRefexesDynamic().size());
					LOG.debug("loadSavedSearch(): concept \"" + displayConcept + "\" active dynamic refexes (StandardViewCoordinates.getWbAuxiliary()): " +  matchingConcept.getRefexesDynamicActive(StandardViewCoordinates.getWbAuxiliary()).size());
					LOG.debug("loadSavedSearch(): concept \"" + displayConcept + "\" active dynamic refexes (WBUtility.getViewCoordinate()): " +  matchingConcept.getRefexesDynamicActive(WBUtility.getViewCoordinate()).size());
			
					LOG.debug("Displaying newly loaded save concept refexes");
					DynamicRefexHelper.displayDynamicRefexes(matchingConcept);

				} catch (IOException e) {
					LOG.warn("Failed displaying attached refexes. Caught " + e.getClass().getName() + " \"" + e.getLocalizedMessage() + "\"");
					e.printStackTrace();
				}

				int i = 0;
				Collection<? extends RefexDynamicVersionBI<?>> refexes = matchingConcept.getRefexesDynamicActive(WBUtility.getViewCoordinate());
				for (RefexDynamicVersionBI<?> refex : refexes) {
					LOG.debug("Displaying refex #" + (++i) + " of " + refexes.size());
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
						RefexDynamicByteArrayBI serializedViewCoordinate = (RefexDynamicByteArrayBI)refex.getData(Search.VIEW_COORDINATE_COLUMN.getDescription());

						// Serialize passed View Coordinate into byte[]serializedViewCoordinate.getData()
						ByteArrayInputStream input = new ByteArrayInputStream(serializedViewCoordinate.getDataByteArray());

						ObjectInputStream oos = new ObjectInputStream(input);
						ViewCoordinate vc = new ViewCoordinate();
						vc.readExternal(oos);
						model.setViewCoordinate(vc);
						LOG.debug("Read View Coordinate from " + dud.getRefexName() + " refex: " + model.getViewCoordinate());

						// Loading maxResults
						RefexDynamicIntegerBI maxResults = (RefexDynamicIntegerBI)refex.getData(Search.MAX_RESULTS_COLUMN.getDescription());
						model.setMaxResults(maxResults.getDataInteger());
						LOG.debug("Read max results from " + dud.getRefexName() + " refex: " + model.getMaxResults());

						// Loading drools expression
						RefexDynamicStringBI droolsExpr = (RefexDynamicStringBI)refex.getData(Search.DROOLS_EXPR_COLUMN.getDescription());
						model.setDroolsExpr(droolsExpr != null ? droolsExpr.getDataString() : null);
						LOG.debug("Read drools expression from " + dud.getRefexName() + " refex: " + model.getDroolsExpr());

					} else if (dud.getRefexName().equals(Search.SEARCH_LUCENE_FILTER.getDescription() /*"Search Lucene Filter"*/)) {
						// handle "Search Lucene Filter"

						LOG.debug("Loading data into model from Search Lucene Filter refex");

						LuceneSearchTypeFilter newFilter = new LuceneSearchTypeFilter();

						RefexDynamicStringBI searchParamCol = (RefexDynamicStringBI)refex.getData(Search.PARAMETER_COLUMN.getDescription());
						newFilter.setSearchParameter(searchParamCol != null ? searchParamCol.getDataString() : null);
						LOG.debug("Read String search parameter from " + dud.getRefexName() + " refex: \"" + newFilter.getSearchParameter() + "\"");

						loadEmbeddedSearchTypeFilterAttributes(refex, newFilter);

						if (model.getSearchType() != null) {
							throw new SearchConceptException("Model already contains SearchTypeFilter of type " + model.getSearchType().getClass().getName() + ". Cannot add second SearchTypeFilter of type " + newFilter.getClass().getName());
						} else {
							model.setSearchType(newFilter);
						}
					} else if (dud.getRefexName().equals(Search.SEARCH_REGEXP_FILTER.getDescription() /*"Search RegExp Filter"*/)) {
						// handle "Search RegExp Filter"

						LOG.debug("Loading data into model from Search RegExp Filter refex");

						RegExpSearchTypeFilter newFilter = new RegExpSearchTypeFilter();

						RefexDynamicStringBI searchParamCol = (RefexDynamicStringBI)refex.getData(Search.PARAMETER_COLUMN.getDescription());
						newFilter.setSearchParameter(searchParamCol != null ? searchParamCol.getDataString() : null);
						LOG.debug("Read String search parameter from " + dud.getRefexName() + " refex: \"" + newFilter.getSearchParameter() + "\"");

						loadEmbeddedSearchTypeFilterAttributes(refex, newFilter);

						if (model.getSearchType() != null) {
							throw new SearchConceptException("Model already contains SearchTypeFilter of type " + model.getSearchType().getClass().getName() + ". Cannot add second SearchTypeFilter of type " + newFilter.getClass().getName());
						} else {
							model.setSearchType(newFilter);
						}
					} else if (dud.getRefexName().equals(Search.SEARCH_ISDESCENDANTOF_FILTER.getDescription() /*"Search IsKindOf Filter"*/)) {
						// handle "Search RegExp Filter"

						LOG.debug("Loading data into model from Search IsKindOf Filter refex");

						IsDescendantOfFilter newFilter = new IsDescendantOfFilter();

						RefexDynamicUUIDBI ascendantUuidCol = (RefexDynamicUUIDBI)refex.getData(Search.ANCESTOR_COLUMN.getDescription());
						if (ascendantUuidCol != null) {
							UUID uuid = ascendantUuidCol.getDataUUID();
							int nid = WBUtility.getConceptVersion(uuid).getNid();
							newFilter.setNid(nid);
							LOG.debug("Read UUID (nid=" + nid + ") from " + dud.getRefexName() + " refex: \"" + uuid + "\"");
						}

						loadEmbeddedSearchFilterAttributes(refex, newFilter, filterOrderMap);
					} else if (dud.getRefexName().equals(Search.SEARCH_ISA_FILTER.getDescription() /*"Search IsA Filter"*/)) {
						// handle "Search IsA Filter"

						LOG.debug("Loading data into model from Search IsA Filter refex");

						IsAFilter newFilter = new IsAFilter();

						RefexDynamicUUIDBI matchUuidCol = (RefexDynamicUUIDBI)refex.getData(Search.MATCH_COLUMN.getDescription());
						if (matchUuidCol != null) {
							UUID uuid = matchUuidCol.getDataUUID();
							int nid = WBUtility.getConceptVersion(uuid).getNid();
							newFilter.setNid(nid);
							LOG.debug("Read UUID (nid=" + nid + ") from " + dud.getRefexName() + " refex: \"" + uuid + "\"");
						}

						loadEmbeddedSearchFilterAttributes(refex, newFilter, filterOrderMap);
					} else {
						// handle or ignore
						LOG.warn("Concept \"" + displayConcept + "\" contains unexpected refex \"" + dud.getRefexName() + "\".  Ignoring...");
					}
				}

				// At this point the search should have a SearchTypeFilter
				if (model.getSearchType() == null) {
					throw new SearchConceptException("Model does not contain a SearchTypeFilter");
				}
				
				// At this point the filterOrderMap should NOT contain any SearchTypeFilter filter
				for (int order : filterOrderMap.keySet()) {
					for (NonSearchTypeFilter<?> f : filterOrderMap.get(order)) {
						model.getFilters().add(f);
					}
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