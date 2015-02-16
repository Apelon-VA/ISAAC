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
 * Search
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.constants;

import java.util.UUID;
import org.ihtsdo.otf.tcc.api.metadata.binding.RefexDynamic;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpecWithDescriptions;
import org.ihtsdo.otf.tcc.api.spec.DynamicRefexConceptSpec;

/**
 * Search
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public class Search {

	/*
	 * Note - the order of definition matters - make sure that anything referenced is already declared higher in the class, 
	 * otherwise, you get null pointers in the class initializer at runtime.
	 */
	private Search() {
		//Not intended to be constructed
	}
	//This concept doesn't need to be in the taxonomy, it is just used as salt for generating other UUIDs
	public static ConceptSpec SEARCH_NAMESPACE = new ConceptSpec("Search Namespace", 
			UUID.fromString("3c92adee-13dc-5c6a-bfe1-acf0d31d05d7"));

	//an organizational concept for all of the new concepts being added to the Refset Auxiliary Concept tree
	public static ConceptSpec SEARCH_TYPES = new ConceptSpec("search refex types", 
			UUID.fromString("d2db2e2a-2d4d-5705-b164-65ee5c1ece58"), 
			RefexDynamic.REFEX_DYNAMIC_IDENTITY);
	
	public static ConceptSpec SEARCH_PERSISTABLE = new ConceptSpec("Persistable Searches", 
			UUID.fromString("80d39126-7814-5812-b01f-d6cda1d86496"), 
			SEARCH_TYPES);
	
	public static ConceptSpec VIEW_COORDINATE_COLUMN = new ConceptSpecWithDescriptions(
			"view coordinate", 
			UUID.fromString("5010f18f-c469-5315-8c5e-f7d9b65373c5"),
			new String[] { "view coordinate" }, 
			new String[] { "view coordinate column" },
			RefexDynamic.REFEX_DYNAMIC_COLUMNS);
	
	public static ConceptSpec MAX_RESULTS_COLUMN = new ConceptSpecWithDescriptions(
			"max results", 
			UUID.fromString("63981b45-bbbe-5247-b571-d7fee02aad79"),
			new String[] { "max results" }, 
			new String[] { "maximum displayable results column" },
			RefexDynamic.REFEX_DYNAMIC_COLUMNS);
	
	public static ConceptSpec DROOLS_EXPR_COLUMN = new ConceptSpecWithDescriptions(
			"drools", 
			UUID.fromString("c0091cf4-f063-5964-85c5-0fdf14b5bb00"),
			new String[] { "drools" }, 
			new String[] { "drools expression column" },
			RefexDynamic.REFEX_DYNAMIC_COLUMNS);

	public static ConceptSpec UUID_COLUMN = new ConceptSpecWithDescriptions(
			"uuid", 
			UUID.fromString("72a4b2de-3854-55f2-ba36-47f2e675351c"),
			new String[] { "uuid" }, 
			new String[] { "uuid column" },
			RefexDynamic.REFEX_DYNAMIC_COLUMNS);

	public static DynamicRefexConceptSpec SEARCH_GLOBAL_ATTRIBUTES = new DynamicRefexConceptSpec("Search Global Attributes", 
			UUID.fromString("27316605-16ea-536e-9acd-40f0277e20ad"),
			true, 
			"Search Global Attributes is for attributes effecting all filters on a search concept", 
			new RefexDynamicColumnInfo[] {
				new RefexDynamicColumnInfo(0, Search.VIEW_COORDINATE_COLUMN.getUuids()[0], RefexDynamicDataType.BYTEARRAY, null, false, null, null),
				new RefexDynamicColumnInfo(1, Search.MAX_RESULTS_COLUMN.getUuids()[0], RefexDynamicDataType.INTEGER, null, false, null, null),
				new RefexDynamicColumnInfo(2, Search.DROOLS_EXPR_COLUMN.getUuids()[0], RefexDynamicDataType.STRING, null, false, null, null)},
			SEARCH_TYPES);
	
	public static ConceptSpec ORDER_COLUMN = new ConceptSpecWithDescriptions(
			"order", 
			UUID.fromString("795bade0-9ffb-54ef-8385-8570b4f708cf"),
			new String[] { "order" }, 
			new String[] { "order column" },
			RefexDynamic.REFEX_DYNAMIC_COLUMNS);
	
	public static ConceptSpec FILTER_INVERT_COLUMN = new ConceptSpecWithDescriptions(
			"invert", 
			UUID.fromString("59e916fc-4632-5574-97c2-6e63b74a2ca3"),
			new String[] { "invert" }, 
			new String[] { "invert filter/match results column" },
			RefexDynamic.REFEX_DYNAMIC_COLUMNS);
	
	public static DynamicRefexConceptSpec SEARCH_FILTER_ATTRIBUTES = new DynamicRefexConceptSpec("Search Filter Attributes", 
			UUID.fromString("b3ac9404-883b-5ba4-b65f-b629970ecc17"),
			true, 
			"Search Type Attributes is for attributes effecting all filters of a certain type such as Lucene or RegExp",
			new RefexDynamicColumnInfo[] {
				new RefexDynamicColumnInfo(0, Search.ORDER_COLUMN.getUuids()[0], RefexDynamicDataType.INTEGER, null, false, null, null),
				new RefexDynamicColumnInfo(1, Search.FILTER_INVERT_COLUMN.getUuids()[0], RefexDynamicDataType.BOOLEAN, null, false, null, null)},
			SEARCH_TYPES);

	public static ConceptSpec PARAMETER_COLUMN = new ConceptSpecWithDescriptions("param",
			UUID.fromString("e28f2c45-1c0b-569a-a329-304ea04ade17"),
			new String[] { "param" }, 
			new String[] { "parameter column" },
			RefexDynamic.REFEX_DYNAMIC_COLUMNS);
	
	public static DynamicRefexConceptSpec SEARCH_SEMEME_CONTENT_FILTER = new DynamicRefexConceptSpec(
			"Search Sememe Content Filter", 
			UUID.fromString("1723aa79-ac7f-520f-a2f5-cd9e03dc4142"),
			true, 
			"Search Sememe Content Filter is for attributes effecting this Sememe Content search",
			new RefexDynamicColumnInfo[] {
				new RefexDynamicColumnInfo(0, Search.PARAMETER_COLUMN.getUuids()[0], RefexDynamicDataType.STRING, null, true, null, null),
				new RefexDynamicColumnInfo(1, Search.UUID_COLUMN.getUuids()[0], RefexDynamicDataType.UUID, null, false, null, null)
			},
			SEARCH_TYPES);
	
	public static DynamicRefexConceptSpec SEARCH_LUCENE_FILTER = new DynamicRefexConceptSpec("Search Lucene Filter", 
			UUID.fromString("4ece37d7-1ae0-5c5e-b475-f8e3bdce4d86"),
			true, 
			"Search Lucene Filter is for attributes effecting this Lucene search",
			new RefexDynamicColumnInfo[] {
				new RefexDynamicColumnInfo(0, Search.PARAMETER_COLUMN.getUuids()[0], RefexDynamicDataType.STRING, null, false, null, null)},
			SEARCH_TYPES);

	public static DynamicRefexConceptSpec SEARCH_REGEXP_FILTER = new DynamicRefexConceptSpec("Search RegExp Filter", 
			UUID.fromString("39c21ff8-cd48-5ac8-8110-40b7d8b30e61"),
			true, 
			"Search RegExp Filter is for attributes effecting this RegExp search",
			new RefexDynamicColumnInfo[] {
				new RefexDynamicColumnInfo(0, Search.PARAMETER_COLUMN.getUuids()[0], RefexDynamicDataType.STRING, null, false, null, null)},
			SEARCH_TYPES);

	public static ConceptSpec ANCESTOR_COLUMN = new ConceptSpecWithDescriptions(
			"ancestor",
			UUID.fromString("fdcac37e-e22f-5f51-b7a6-f8de283c6cf0"),
			new String[] { "ancestor" }, 
			new String[] { "ancestor concept column" },
			RefexDynamic.REFEX_DYNAMIC_COLUMNS);
	
	public static DynamicRefexConceptSpec SEARCH_ISDESCENDANTOF_FILTER = new DynamicRefexConceptSpec("Search IsDescendantOf Filter", 
			UUID.fromString("58bea66c-65fb-5c52-bf71-d742aebe3822"),
			true, 
			"Search IsDescendantOf Filter is for attributes effecting this IsDescendantOf search",
			new RefexDynamicColumnInfo[] {
				new RefexDynamicColumnInfo(0, Search.ANCESTOR_COLUMN.getUuids()[0], RefexDynamicDataType.UUID, null, false, null, null)},
			SEARCH_TYPES);
	
	public static ConceptSpec MATCH_COLUMN = new ConceptSpecWithDescriptions(
			"match",
			UUID.fromString("53b89cac-54c4-5cf8-bf87-baee591729f5"),
			new String[] { "match" }, 
			new String[] { "matching concept column" },
			RefexDynamic.REFEX_DYNAMIC_COLUMNS);
	
	public static DynamicRefexConceptSpec SEARCH_ISA_FILTER = new DynamicRefexConceptSpec("Search IsA Filter", 
			UUID.fromString("77823bc2-5924-544e-9496-bb54cad41d63"),
			true, 
			"Search IsA Filter is for attributes effecting this IsA search",
			new RefexDynamicColumnInfo[] {
				new RefexDynamicColumnInfo(0, Search.MATCH_COLUMN.getUuids()[0], RefexDynamicDataType.UUID, null, false, null, null)},
			SEARCH_TYPES);
}