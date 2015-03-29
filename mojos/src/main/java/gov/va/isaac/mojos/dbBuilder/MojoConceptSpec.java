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
package gov.va.isaac.mojos.dbBuilder;

import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;

/**
 * {@link MojoConceptSpec}
 * 
 * A class that Maven can utilize to create a real ConceptSpec from snippits in our pom.xml files.
 * Should look like:
 * <pre>
 * <MojoConceptSpec>
 *     <fsn>The FSN</fsn>
 *     <uuid>f4d2fabc-7e96-3b3a-a348-ae867ba74029</uuid>
 * </MojoConceptSpec>
 * </pre>
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class MojoConceptSpec
{
	private String fsn;
	private String uuid;
	
	public ConceptSpec getConceptSpec()
	{
		return new ConceptSpec(fsn, uuid);
	}
	
	public void setFsn(String thisFsn) {
		fsn = thisFsn;
	}
	
	public String getFsn() {
		return fsn;
	}
	
	public void setUuid(String uuidInput) {
		uuid = uuidInput;
	}
	
	public String getUuid() {
		return uuid;
	}
}
