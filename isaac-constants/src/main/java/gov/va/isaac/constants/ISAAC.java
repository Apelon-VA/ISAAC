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

import java.util.UUID;
import org.ihtsdo.otf.tcc.api.metadata.ComponentType;
import org.ihtsdo.otf.tcc.api.metadata.binding.RefexDynamic;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.metadata.binding.Taxonomies;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpecWithDescriptions;
import org.ihtsdo.otf.tcc.api.spec.DynamicRefexConceptSpec;
import org.ihtsdo.otf.tcc.api.spec.RelSpec;

/**
 * {@link ISAAC}
 * 
 * Various constants for ISAAC in ConceptSpec form for reuse.
 * 
 * The DBBuilder mojo processes this class, and creates these concept / relationships as necessary during build.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class ISAAC
{
	//Root node
	public static ConceptSpec ISAAC_ROOT = new ConceptSpec("ISAAC Root", UUID.fromString("c767a452-41e3-5835-90b7-439f5b738035"));
	
	//Other children of the root node - just reference here, so that the DB Builder can process them and create the rels.
	//they can be private.  DBBuilder still reads them.  Anyone else should use the refs in Taxonomies.
	private static ConceptSpec REFSET_AUXILLIARY_REF = Taxonomies.REFSET_AUX;
	static 
	{
		//Need to do stated and inferred, otherwise, we can't browse, on inferred mode, nor on inferred_then_stated mode
		REFSET_AUXILLIARY_REF.setRelSpecs(new RelSpec[] {new RelSpec(REFSET_AUXILLIARY_REF, Snomed.IS_A, ISAAC_ROOT), //stated
				new RelSpec(REFSET_AUXILLIARY_REF, Snomed.IS_A, ISAAC.ISAAC_ROOT, SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2)});  //inferred
	}
	
	private static ConceptSpec TERMINOLOGY_AUXILLIARY_REF = Taxonomies.WB_AUX;
	static 
	{
		//Need to do stated and inferred, otherwise, we can't browse, on inferred mode, nor on inferred_then_stated mode
		TERMINOLOGY_AUXILLIARY_REF.setRelSpecs(new RelSpec[] {new RelSpec(TERMINOLOGY_AUXILLIARY_REF, Snomed.IS_A, ISAAC_ROOT),  //stated
				new RelSpec(TERMINOLOGY_AUXILLIARY_REF, Snomed.IS_A, ISAAC_ROOT, SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2)});  //inferred
	}
	
	//Set up a generic comments sememe (next 3 items)
	public static ConceptSpecWithDescriptions EDITOR_COMMENT = new ConceptSpecWithDescriptions("editor comment", 
			UUID.fromString("2b38b1a9-ce6e-5be2-8885-65cd76f40929"),
			new String[] {"editor comment"},
			new String[] {"Stores the comment created by the editor"},
			RefexDynamic.REFEX_DYNAMIC_COLUMNS);
	
	public static ConceptSpecWithDescriptions EDITOR_COMMENT_CONTEXT = new ConceptSpecWithDescriptions("editor comment context", 
			UUID.fromString("2e4187ca-ba45-5a87-8484-1f86801a331a"),
			new String[] {"editor comment context"},
			new String[] {"Stores an optional value that may be used to group comments, such as 'mapping comment' or 'assertion comment' which"
				+ " then would allow programmatic filtering of comments to be context specific."},
			RefexDynamic.REFEX_DYNAMIC_COLUMNS);
	
	public static DynamicRefexConceptSpec COMMENT_ATTRIBUTE = new DynamicRefexConceptSpec("Comment", 
			UUID.fromString("147832d4-b9b8-5062-8891-19f9c4e4760a"),
			true, 
			"A Sememe used to store comments on arbitrary items (concepts, relationships, sememes, etc)", 
			new RefexDynamicColumnInfo[] {
				new RefexDynamicColumnInfo(0, EDITOR_COMMENT.getUuids()[0], RefexDynamicDataType.STRING, null, true, null, null),
				new RefexDynamicColumnInfo(1, EDITOR_COMMENT_CONTEXT.getUuids()[0], RefexDynamicDataType.STRING, null, false, null, null)},
			RefexDynamic.REFEX_DYNAMIC_IDENTITY,
			new Integer[] {0,1});  //Index the comments, and the columns
	
	public static ConceptSpecWithDescriptions REFEX_COLUMN_TARGET_COMPONENT = new ConceptSpecWithDescriptions("target", 
			UUID.fromString("e598e12f-3d39-56ac-be68-4e9fca98fb7a"),
			new String[] {"target"},
			new String[] {"Stores the (optional) target concept or component of an association or mapping"},
			RefexDynamic.REFEX_DYNAMIC_COLUMNS);
	
	public static DynamicRefexConceptSpec ASSOCIATION_REFEX = new DynamicRefexConceptSpec("Sememe represents association", 
			UUID.fromString("d4d5909f-ca6e-52af-87bf-2c8199b28f25"),
			true, 
			"A Sememe used to annotate other sememes which define an association, which is defined as a sememe which contains "
			+ "a data column named 'target concept', among other criteria.", 
			new RefexDynamicColumnInfo[] {},
			RefexDynamic.REFEX_DYNAMIC_IDENTITY,
			new Integer[] {});  //Index the associations
	
	public static DynamicRefexConceptSpec ASSOCIATION_INVERSE_NAME = new DynamicRefexConceptSpec("inverse name", 
			UUID.fromString("c342d18a-ec1c-5583-bfe3-59e6324ae189"),
			new String[] {"inverse name"},
			new String[0],
			true, 
			"This is the extended description type that may be attached to a description within a concept that defines an Association Refex to signify that "
					+ "the referenced description is the inverse of the association name.",
			new RefexDynamicColumnInfo[0],
			RefexDynamic.REFEX_DYNAMIC_IDENTITY,
			ComponentType.DESCRIPTION,
			null);
}
