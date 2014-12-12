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
 * UserProfileDefaults
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.config.profiles;

import java.util.UUID;

import gov.va.isaac.AppContext;
import gov.va.isaac.config.generated.StatedInferredOptions;

/**
 * UserProfileDefaults
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public final class UserProfileDefaults {
	private UserProfileDefaults() {}
	
	public static StatedInferredOptions getDefaultStatedInferredPolicy() {
		return StatedInferredOptions.INFERRED_THEN_STATED;
	}

	public static boolean getDefaultDisplayFSN() { 
		return true;
	}

	public static boolean getDefaultLaunchWorkflowForEachCommit() {
		return true;
	}

	public static boolean getDefaultRunDroolsBeforeEachCommit() {
		return true;
	}

	public static String getDefaultWorkflowServerDeploymentId() {
		return AppContext.getAppConfiguration().getDefaultWorkflowServerDeploymentId();
	}

	public static UUID getDefaultViewCoordinatePath() {
		return UUID.fromString(AppContext.getAppConfiguration().getDefaultViewPathUuid());
	}

	public static UUID getDefaultEditCoordinatePath() {
		return UUID.fromString(AppContext.getAppConfiguration().getDefaultEditPathUuid());
	}

	public static UUID getDefaultWorkflowPromotionPathPath() {
		return AppContext.getAppConfiguration().getDefaultWorkflowPromotionPathUuidAsUUID();
	}

	public static String getDefaultWorkflowServerUrl() {
		return AppContext.getAppConfiguration().getDefaultWorkflowServerUrl();
	}

	public static String getDefaultChangeSetUrl() {
		return AppContext.getAppConfiguration().getDefaultChangeSetUrl();
	}
	
	public static String getDefaultReleaseVersion() {
		return AppContext.getAppConfiguration().getDefaultReleaseVersion();
	}
	
	public static String getDefaultExtensionNamespace() {
		return AppContext.getAppConfiguration().getDefaultExtensionNamespace();
	}
}
