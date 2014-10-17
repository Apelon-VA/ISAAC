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
 * WorkflowProcessModel
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.interfaces.workflow;

/**
 * WorkflowProcess
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public enum WorkflowProcess {
	//REVIEW("terminology-authoring.ReviewWorkflow"),
	//PROMPT("Choose Workflow Process Definition"),
	REVIEW3("terminology-authoring.ReviewWorkflow3"),
	DUAL_REVIEW("terminology-authoring.DualReviewWorkflow");
	
	private final String text;
	
	private WorkflowProcess(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
	
	public static WorkflowProcess valueOfText(String str) {
		if (str == null)
			throw new NullPointerException("String value for WorkflowProcess is null");

		for (WorkflowProcess value : values()) {
			if (str.equals(value.getText())) {
				return value;
			}
		}

		throw new IllegalArgumentException(
				"No WorkflowProcess constant with text=\"" + str + "\"");
	}
}
