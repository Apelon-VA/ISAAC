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
 * SearchConceptBuilder
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

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import org.ihtsdo.otf.tcc.api.blueprint.ConceptAttributeAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDynamicCAB;
import org.ihtsdo.otf.tcc.api.blueprint.TerminologyBuilderBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.Search;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicUsageDescription;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.RefexDynamicData;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.RefexDynamicUsageDescriptionBuilder;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexByteArray;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexInteger;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SearchConceptBuilder
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public class SearchConceptBuilder {
	private static final Logger logger_ = LoggerFactory.getLogger(SearchConceptBuilder.class);

	public static void doSave(
			String saveConceptFSN,
			String saveConceptPT,
			SearchViewModel model)
	{
		logger_.debug("doSave(): saving model for search fsn=\"" + saveConceptFSN + "\", pt=\"" + saveConceptPT + "\": " + model);
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

		// 
		try
		{
			logger_.debug("doSave(\"" + saveConceptFSN + "\", \"" + saveConceptPT + "\"");
			
			ConceptChronicleBI searchConcept = WBUtility.createNewConcept(WBUtility.getConceptVersion(Search.SEARCH_PERSISTABLE.getUuids()[0]), saveConceptFSN, saveConceptPT);
			ConceptAttributeAB conceptAttributeBlueprintAmender = new ConceptAttributeAB(searchConcept.getConceptNid(), searchConcept.getVersion(WBUtility.getViewCoordinate()).getConceptAttributesActive().isDefined(), RefexDirective.INCLUDE); //bp.getConceptAttributeAB();

			{
				// Start with Search Global Attributes
				RefexDynamicUsageDescription searchGlobalAttributesRDUD = RefexDynamicUsageDescriptionBuilder.readRefexDynamicUsageDescriptionConcept(Search.SEARCH_GLOBAL_ATTRIBUTES.getNid());
				// Currently just has View Coordinate byte[]
				RefexDynamicData[] searchGlobalAttributesData = new RefexDynamicData[searchGlobalAttributesRDUD.getColumnInfo().length];

				// Serialize passed View Coordinate into byte[]
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(output);
				model.getViewCoordinate().writeExternal(oos);
				oos.flush();

				// Construct and populate RefexDynamicData for View Coordinate
				RefexDynamicData viewCoordinateColumnData = new RefexByteArray(output.toByteArray(), RefexDynamicUsageDescription.read(Search.SEARCH_GLOBAL_ATTRIBUTES.getNid()).getColumnInfo()[0].getColumnName());
				searchGlobalAttributesData[0] = viewCoordinateColumnData;

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
				//
				//			ExtendedAppContext.getDataStore().addUncommitted(WBUtility.getConceptVersion(bp.getComponentUuid()));
				//
				//			ExtendedAppContext.getDataStore().commit(WBUtility.getConceptVersion(bp.getComponentUuid()));
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
						throw new RuntimeException("Unsupported SingleStringParameterFilter type " + singleStringParameterFilter.getClass().getName());
					}

					filterRDUD = RefexDynamicUsageDescriptionBuilder.readRefexDynamicUsageDescriptionConcept(filterConceptSpec.getNid());

					// First create Filter, which has a single String param plus common Filter attributes
					RefexDynamicData[] filterRefexData = new RefexDynamicData[filterRDUD.getColumnInfo().length];

					// Construct and populate RefexDynamicData for search parameter
					RefexDynamicData searchParameterData = new RefexString(singleStringParameterFilter.getSearchParameter(), RefexDynamicUsageDescription.read(filterConceptSpec.getNid()).getColumnInfo()[0].getColumnName());
					filterRefexData[0] = searchParameterData;

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
					
//					builder = ExtendedAppContext.getDataStore().getTerminologyBuilder(WBUtility.getEC(), WBUtility.getViewCoordinate());
//					builder.construct(filterRefexCAB);
//
//					ExtendedAppContext.getDataStore().addUncommitted(WBUtility.getConceptVersion(bp.getComponentUuid()));
//
//					ExtendedAppContext.getDataStore().commit(WBUtility.getConceptVersion(bp.getComponentUuid()));

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
					throw new RuntimeException("Unsupported Filter type " + currentFilter.getClass().getName());
				}
			}
			
			ExtendedAppContext.getDataStore().addUncommitted(WBUtility.getConceptVersion(searchConcept.getPrimordialUuid()));

			ExtendedAppContext.getDataStore().commit(WBUtility.getConceptVersion(searchConcept.getPrimordialUuid()));
		}
		catch (Exception e)
		{
			logger_.error("Error saving refex", e);
		}
	}
}
