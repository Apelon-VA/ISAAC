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
 * TaskModelFactory
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.workflow.taskmodel;

import javafx.scene.control.ComboBox;
import gov.va.isaac.workflow.LocalTask;
import gov.va.isaac.workflow.taskmodel.TaskModel.UserActionOutputResponse;

/**
 * TaskModelFactory
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public class TaskModelFactory {
	enum TaskType {
		edit_content("Edit content"),
		review_content("Review content"),
		approve_content("Approve content"),
		
		dual_review_edit_content_1("Edit content 1"),
		dual_review_edit_content_2("Edit content 2"),
		dual_review_content_1("Review content 1"),
		dual_review_content_2("Review content 2"),
		dual_review_adjudicate_content("Adjudicate content");

		private final String nodeName;
		private TaskType(String nodeName) {
			this.nodeName = nodeName;
		}

		public String getNodeName() {
			return nodeName;
		}

		public static TaskType valueOfNodeName(String str) {
			for (TaskType taskType : TaskType.values()) {
				if (taskType.getNodeName().equals(str)) {
					return taskType;
				}
			}

			throw new IllegalArgumentException("Invalid TaskType value \"" + str + "\"");
		}
	};

	public static TaskModel newTaskModel(LocalTask task, ComboBox<UserActionOutputResponse> userActionResponseComboBox) {
		TaskType taskType = TaskType.valueOfNodeName(task.getName());

		switch(taskType) {
		case edit_content:
			return new EditContentTaskModel(task, userActionResponseComboBox);
		case review_content:
			return new ReviewContentTaskModel(task, userActionResponseComboBox);
		case approve_content:
			return new ApproveContentTaskModel(task, userActionResponseComboBox);
		case dual_review_edit_content_1:
		case dual_review_edit_content_2:
			return new DualReviewEditContentTaskModel(task, userActionResponseComboBox);
		case dual_review_content_1:
		case dual_review_content_2:
			return new DualReviewReviewContentTaskModel(task, userActionResponseComboBox);
		case dual_review_adjudicate_content:
			return new DualReviewAdjudicateContentTaskModel(task, userActionResponseComboBox);

		default: throw new IllegalArgumentException("Unsupported TaskType " + taskType);
		}
	}
}
