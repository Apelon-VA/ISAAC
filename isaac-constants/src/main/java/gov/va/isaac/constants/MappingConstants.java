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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.constants;

import java.beans.PropertyVetoException;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.metadata.binding.RefexDynamic;
import org.ihtsdo.otf.tcc.api.metadata.binding.Taxonomies;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicValidatorType;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpecWithDescriptions;
import org.ihtsdo.otf.tcc.api.spec.DynamicRefexConceptSpec;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicUUID;

/**
 * {@link MappingConstants}
 * 
 * Various constants for ISAAC in ConceptSpec form for reuse.
 * 
 * The DBBuilder mojo processes this class, and creates these concept / relationships as necessary during build.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
@SuppressWarnings("unused")
public class MappingConstants
{
	//This concept doesn't need to be in the taxonomy, it is just used as salt for generating other UUIDs
	public static ConceptSpecWithDescriptions MAPPING_NAMESPACE = new ConceptSpecWithDescriptions("Mapping Namespace", 
			UUID.fromString("e33c8a61-c956-50ce-8ba2-4defa430c65e"),
			new String[] {"Mapping Namespace"},
			new String[] {"A concept used to hold the UUID used as the namespace ID generation when creating mappings"},
			null);
	
	public static ConceptSpecWithDescriptions MAPPING_QUALIFIERS = new ConceptSpecWithDescriptions("mapping qualifiers", 
		UUID.fromString("83204ca8-bd51-530c-af04-5edbec04a7c6"), 
		new String[] {"mapping qualifiers"},
		new String[] {"Stores the editor selected mapping qualifier"},
		Taxonomies.WB_AUX);
	
	//These don't have to be public - just want the hierarchy created during the DB build
	private static ConceptSpec broader = new ConceptSpec("Broader Than", 
		UUID.fromString("c1068428-a986-5c12-9583-9b2d3a24fdc6"), 
		MAPPING_QUALIFIERS);
	
	private static ConceptSpec exact = new ConceptSpec("Exact", 
		UUID.fromString("8aa6421d-4966-5230-ae5f-aca96ee9c2c1"), 
		MAPPING_QUALIFIERS);
	
	private static ConceptSpec narrower = new ConceptSpec("Narrower Than", 
		UUID.fromString("250d3a08-4f28-5127-8758-e8df4947f89c"), 
		MAPPING_QUALIFIERS);
	
	public static ConceptSpecWithDescriptions MAPPING_STATUS = new ConceptSpecWithDescriptions("mapping status type", 
			UUID.fromString("f4523b36-3714-5d0e-999b-edb8f21dc0fa"), 
			new String[] {"mapping status type"},
			new String[] {"Stores the editor selected status of the mapping set or mapping instance"},
			Taxonomies.WB_AUX);
	
	private static ConceptSpec pending = new ConceptSpec("Pending", 
		UUID.fromString("d481125e-b8ca-537c-b688-d09d626e5ff9"), 
		MAPPING_STATUS);
	
	private static ConceptSpec reviewed = new ConceptSpec("Reviewed", 
		UUID.fromString("45b49b0d-e2d2-5a27-a08d-8f79856b6307"), 
		MAPPING_STATUS);
	
	public static ConceptSpecWithDescriptions COLUMN_PURPOSE = new ConceptSpecWithDescriptions("purpose", 
		UUID.fromString("e5de9548-35b9-5e3b-9968-fd9c0a665b51"),
		new String[] {"purpose"},
		new String[] {"Stores the editor stated purpose of the mapping set"},
		RefexDynamic.REFEX_DYNAMIC_COLUMNS);
	
	public static DynamicRefexConceptSpec MAPPING_SEMEME_TYPE;
	static
	{
		try
		{
			//This sememe defines how the mapping sememe's are constructed
			MAPPING_SEMEME_TYPE = new DynamicRefexConceptSpec("Mapping Sememe Type", 
				UUID.fromString("aa4c75a1-fc69-51c9-88dc-a1a1c7f84e01"),
				true, 
				"A Sememe used to specify how user-created mapping Sememes are structured", 
				new RefexDynamicColumnInfo[] {
					new RefexDynamicColumnInfo(0, MAPPING_STATUS.getPrimodialUuid(), RefexDynamicDataType.UUID, null, false, 
						RefexDynamicValidatorType.IS_KIND_OF, new RefexDynamicUUID(MAPPING_STATUS.getPrimodialUuid())),
					new RefexDynamicColumnInfo(1, COLUMN_PURPOSE.getPrimodialUuid(), RefexDynamicDataType.STRING, null, false, null, null)},
				RefexDynamic.REFEX_DYNAMIC_IDENTITY,
				new Integer[] {});  //want to index this sememe, but don't need to index the data columns
		}
		catch (PropertyVetoException e)
		{
			throw new RuntimeException(e);
		}
	}
}
