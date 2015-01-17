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

import java.io.File;
import org.jvnet.hk2.annotations.Contract;

/**
 * {@link TransformI}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
@Contract
public interface TransformI
{
	/**
	 * Pass in the configuration file that will be used to setup this transform
	 * @param configFile
	 */
	public void configure(File configFile);
	
	/**
	 * Execute the transform
	 */
	public void transform();

	/**
	 * @return a user-friendly description of what this transform does
	 */
	public String getDescription();
}
