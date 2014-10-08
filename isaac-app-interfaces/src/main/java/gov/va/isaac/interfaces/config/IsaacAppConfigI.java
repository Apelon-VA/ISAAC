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
package gov.va.isaac.interfaces.config;

import java.net.URL;
import java.util.UUID;
import org.jvnet.hk2.annotations.Contract;

/**
 * {@link IsaacAppConfigI}
 * 
 * This interface only exists as a mechanism to provide read-only access to the items generated 
 * out of the AppConfigSchema.xsd file.  This interface should be kept in sync with the definitions
 * within the AppConfigSchema.xsd file - as that is what end users will be writing - which eventually 
 * populates the values that return from these getters.  
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Contract
public interface IsaacAppConfigI
{
	/**
	 * The text string that is displayed in the ISAAC title bar, about box, and other typical locations.
	 */
	public String getApplicationTitle();

	/**
	 * The SVN or GIT URL that will be used to synchronize user profiles and changesets for this bundle.
	 */
	public String getUserRepositoryPath();
	
	/**
	 * The full URL for the REST API of the KIE Workflow server.
	 */
	public URL getWorkflowServerURLasURL();
	
	/**
	 * The deployment ID for the KIE workflow server
	 */
	public String getWorkflowServerDeploymentID();
	
	/**
	 * The UUID for the Path to which content published via Workflow will automatically be promoted to
	 * @return
	 */
	public UUID getPromotionPathAsUUID();
}
