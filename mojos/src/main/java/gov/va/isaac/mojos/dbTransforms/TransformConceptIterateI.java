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
package gov.va.isaac.mojos.dbTransforms;

import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.jvnet.hk2.annotations.Contract;

/**
 * {@link TransformConceptIterateI}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
@Contract
public interface TransformConceptIterateI extends TransformI
{

	/**
	 * Transforms that just need to iterate over each concept in the DB may implement this interface - 
	 * this transform method will be called once for each concept in the DB.  Transformations should be limited
	 * to operations that involve this concept.
	 * @param ts - the term store
	 * @param cc - the current concept
	 * @return false if this impl made no change - true if this impl made a change that will need committing.
	 * Individual implementations should not call commit - the process feeding in the concepts will commit 
	 * periodically.
	 * @throws Exception
	 */
	public boolean transform(TerminologyStoreDI ts, ConceptChronicleBI cc) throws Exception;
	
}
