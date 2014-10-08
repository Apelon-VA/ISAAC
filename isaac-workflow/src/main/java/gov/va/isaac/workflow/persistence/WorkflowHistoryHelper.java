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
 * WorkflowHistoryHelper
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.workflow.persistence;

import gov.va.isaac.workflow.Action;
import gov.va.isaac.workflow.LocalTask;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WorkflowHistoryHelper
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public class WorkflowHistoryHelper {
	private final static Logger logger = LoggerFactory.getLogger(WorkflowHistoryHelper.class);

	private final static String WF_HISTORY_MAP_VARIABLE_NAME = "wf_history";

	/*
	 * Author
	 * Time
	 * Task name
	 * Complete/Release
	 * Output variables
	 *
	 */
	enum WorkflowHistoryVariable {
		wf_hist_task_name,
		wf_hist_component_id,
		wf_hist_component_name,
		wf_hist_action_owner,
		wf_hist_action_time,
		wf_hist_action
	}

	public static List<Map<String, String>> getHistoryEntries(LocalTask task) {
		List<Map<String, String>> existingEntries = deserializeMaps(task.getInputVariables().get(WF_HISTORY_MAP_VARIABLE_NAME));

		return existingEntries;
	}

	// For logging purposes only
	private static String historyEntryToString(Map<String, String> entry) {
		Map<String, String> tempMap = new HashMap<>(entry);
		
		tempMap.put(WorkflowHistoryVariable.wf_hist_action_time.name(), new Date(Long.valueOf(entry.get(WorkflowHistoryVariable.wf_hist_action_time.name()))).toString());
		
		return tempMap.toString();
	}
	
	// For logging purposes only
	public static String historyEntriesToString(List<Map<String, String>> entries) {
		StringBuilder builder = new StringBuilder();
		
		for (Map<String, String> entry : entries) {
			if (builder.toString().length() > 0) {
				builder.append("\n");
			}
			builder.append(historyEntryToString(entry));
		}
		
		return builder.toString();
	}
	
	private static Map<String, String> createNewHistoryEntry(LocalTask task, Action action) {
		long time = new Date().getTime();

		Map<String, String> newEntry = new HashMap<>();
		newEntry.put(WorkflowHistoryVariable.wf_hist_task_name.name(), task.getName());
		newEntry.put(WorkflowHistoryVariable.wf_hist_component_id.name(), task.getComponentId());
		newEntry.put(WorkflowHistoryVariable.wf_hist_component_name.name(), task.getComponentName());
		newEntry.put(WorkflowHistoryVariable.wf_hist_action_owner.name(), task.getOwner());
		newEntry.put(WorkflowHistoryVariable.wf_hist_action_time.name(), Long.toString(time));
		newEntry.put(WorkflowHistoryVariable.wf_hist_action.name(), action.name());

		return newEntry;
	}

	public static void createAndAddNewEntry(LocalTask task, Action action, Map<String, String> outputVariables) {

		List<Map<String, String>> existingEntries = getHistoryEntries(task);
		int numExistingEntries = existingEntries.size();
		Map<String, String> newEntry = createNewHistoryEntry(task, action);
		outputVariables.putAll(newEntry);
		existingEntries.add(outputVariables);

		String serializedEntries = serializeMaps(existingEntries);
		outputVariables.put(WF_HISTORY_MAP_VARIABLE_NAME, serializedEntries);

		logger.debug("Added new history entry for task #{} with {} existing entries: {}", task.getId(), numExistingEntries, outputVariables);
	}

	private static String serializeMaps(List<Map<String, String>> list) {
		if (list == null) {
			return "";
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		XMLEncoder xmlEncoder = new XMLEncoder(bos);
		xmlEncoder.writeObject(list);
		xmlEncoder.close();

		String serializedMaps = bos.toString();
		return serializedMaps;
	}

	private static List<Map<String, String>> deserializeMaps(String serializedMaps) {
		if (serializedMaps == null || serializedMaps.isEmpty()) {
			return new ArrayList<Map<String, String>>();
		} else {
			XMLDecoder xmlDecoder = new XMLDecoder(new ByteArrayInputStream(serializedMaps.getBytes()));
			@SuppressWarnings("unchecked")
			List<Map<String, String>> parsedMaps = (List<Map<String, String>>) xmlDecoder.readObject();
			xmlDecoder.close();
			return parsedMaps;
		}
	}
}
