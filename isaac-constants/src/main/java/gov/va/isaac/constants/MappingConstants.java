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
	public static ConceptSpec MAPPING_STATUS = new ConceptSpec("mapping status types", 
		UUID.fromString("1d28d3a2-1b3d-5f97-add5-06b5f8ef08d7"), 
		Taxonomies.WB_AUX);
	
	//These don't have to be public - just want the hierarchy created during the DB build
	private static ConceptSpec broader = new ConceptSpec("Broader Than", 
		UUID.fromString("c1068428-a986-5c12-9583-9b2d3a24fdc6"), 
		MAPPING_STATUS);
	
	private static ConceptSpec exact = new ConceptSpec("Exact", 
		UUID.fromString("8aa6421d-4966-5230-ae5f-aca96ee9c2c1"), 
		MAPPING_STATUS);
	
	private static ConceptSpec narrower = new ConceptSpec("Narrower Than", 
		UUID.fromString("250d3a08-4f28-5127-8758-e8df4947f89c"), 
		MAPPING_STATUS);
	
	public static ConceptSpec MAPPING_QUALIFIERS = new ConceptSpec("mapping qualifiers", 
		UUID.fromString("83204ca8-bd51-530c-af04-5edbec04a7c6"), 
		Taxonomies.WB_AUX);
	
	private static ConceptSpec pending = new ConceptSpec("Pending", 
		UUID.fromString("d481125e-b8ca-537c-b688-d09d626e5ff9"), 
		MAPPING_QUALIFIERS);
	
	private static ConceptSpec reviewed = new ConceptSpec("Reviewed", 
		UUID.fromString("45b49b0d-e2d2-5a27-a08d-8f79856b6307"), 
		MAPPING_QUALIFIERS);
	
	public static DynamicRefexConceptSpec MAPPING_SEMEME_TYPE;
	static
	{
		try
		{
			//This sememe defines how the mapping sememe's are constructed
			//Column 0 is 'Status (attribute) - c1a45484-707b-3447-9145-0f20b53dd10c'
			//Column 1 is 'Purpose (attribute)' - 94cb845e-83b8-330d-bc9d-a758dceb6d81
			MAPPING_SEMEME_TYPE = new DynamicRefexConceptSpec("Mapping Sememe Type", 
				UUID.fromString("aa4c75a1-fc69-51c9-88dc-a1a1c7f84e01"),
				true, 
				"A Sememe used to specify how user-created mapping Sememes are structured", 
				new RefexDynamicColumnInfo[] {
					new RefexDynamicColumnInfo(0, UUID.fromString("c1a45484-707b-3447-9145-0f20b53dd10c"), RefexDynamicDataType.NID, null, false, 
						RefexDynamicValidatorType.IS_KIND_OF, new RefexDynamicUUID(MAPPING_STATUS.getPrimodialUuid())),
					new RefexDynamicColumnInfo(1, UUID.fromString("94cb845e-83b8-330d-bc9d-a758dceb6d81"), RefexDynamicDataType.STRING, null, false, null, null)},
				RefexDynamic.REFEX_DYNAMIC_IDENTITY,
				new Integer[] {});  //want to index this sememe, but don't need to index the data columns
		}
		catch (PropertyVetoException e)
		{
			throw new RuntimeException(e);
		}
	}
}
