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
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.api.spec.RelSpec;

/**
 * {@link InformationModels}
 *
 * InformationModel related constants for ISAAC in ConceptSpec form for reuse.
 * 
 * The DBBuilder mojo processes this class, and creates these concept / relationships as necessary during build.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class InformationModels
{
	//Information models is a top level child of root
	public static ConceptSpec INFORMATION_MODELS = new ConceptSpec("Information Models", UUID.fromString("ab09b185-b93d-577b-a350-622be832e6c7"),
			new RelSpec(null, Snomed.IS_A, ISAAC.ISAAC_ROOT), new RelSpec(null, Snomed.IS_A, ISAAC.ISAAC_ROOT, SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2));
	static
	{
		//can't set these properly up above, without causing an infinite loop...
		INFORMATION_MODELS.getRelSpecs()[0].setOriginSpec(INFORMATION_MODELS);
		INFORMATION_MODELS.getRelSpecs()[1].setOriginSpec(INFORMATION_MODELS);
	}
	
	//current information models
	public static ConceptSpec CEM = new ConceptSpec("Clinical Element Model", UUID.fromString("0a9c9ba5-410e-5a40-88f4-b0cdd17325e1"),
			INFORMATION_MODELS);
	
	public static ConceptSpec FHIM = new ConceptSpec("Federal Health Information Model", UUID.fromString("9eddce80-784c-50a3-8ec6-e92278ac7691"),
			INFORMATION_MODELS);

	public static ConceptSpec HED = new ConceptSpec("Health eDecision", UUID.fromString("1cdae521-c637-526a-bf88-134de474f824"),
			INFORMATION_MODELS);
	
	public static ConceptSpec CEM_ENUMERATIONS = new ConceptSpec("Clinical Element Model Enumerations", UUID.fromString("ee5da47f-562f-555d-b7dd-e18697e11ece"), CEM);	

	public static ConceptSpec FHIM_ENUMERATIONS= new ConceptSpec("Federal Health Information Model Enumerations",  UUID.fromString("78e5feff-faf7-5666-a2e1-21bdfe688e13"), FHIM);
			
	public static ConceptSpec HED_ENUMERATIONS = new ConceptSpec("Health eDecision Enumerations",  UUID.fromString("5f4cf488-38bd-54b0-8d08-809599d6db82"), HED);
	
}
