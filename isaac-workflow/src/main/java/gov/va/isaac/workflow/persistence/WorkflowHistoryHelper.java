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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;

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

	private final static String WF_IN_HISTORY_MAP_VARIABLE_NAME = "in_history";
	private final static String WF_OUT_HISTORY_MAP_VARIABLE_NAME = "out_history";

	private final static SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy @ HH:mm:ss");

	/*
	 * Author
	 * Time
	 * Task name
	 * Complete/Release
	 * Output variables
	 *
	 */
	enum WorkflowHistoryVariable {
		wf_hist_action_owner,
		wf_hist_action_time,
		wf_hist_action, 
		wf_hist_comment,
		wf_hist_task_name
	}

	public static List<Map<String, String>> getHistoryEntries(LocalTask task) {
		List<Map<String, String>> existingEntries = deserializeMaps(task.getInputVariables().get(WF_IN_HISTORY_MAP_VARIABLE_NAME));

		logger.debug("Got {} history entries from task #{}: {}", existingEntries.size(), task.getId(), historyEntriesToString(existingEntries));
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
	
	private static Map<String, String> createNewHistoryEntry(LocalTask task, Action action, String comment) {
		long time = new Date().getTime();

		Map<String, String> newEntry = new HashMap<>();
		newEntry.put(WorkflowHistoryVariable.wf_hist_action_owner.name(), task.getOwner());
		newEntry.put(WorkflowHistoryVariable.wf_hist_action_time.name(), Long.toString(time));
		newEntry.put(WorkflowHistoryVariable.wf_hist_action.name(), action.name());
		newEntry.put(WorkflowHistoryVariable.wf_hist_comment.name(), comment);
		newEntry.put(WorkflowHistoryVariable.wf_hist_task_name.name(), task.getName());

		return newEntry;
	}

	public static void createAndAddNewEntry(LocalTask task, Action action, Map<String, String> outputVariables) {
		List<Map<String, String>> existingEntries = getHistoryEntries(task);
		int numExistingEntries = existingEntries.size();
		Map<String, String> newEntry = createNewHistoryEntry(task, action, outputVariables.get("out_comment"));
		//outputVariables.putAll(newEntry);
		//existingEntries.add(outputVariables);
		existingEntries.add(newEntry);

		String serializedEntries = serializeMaps(existingEntries);
		outputVariables.put(WF_OUT_HISTORY_MAP_VARIABLE_NAME, serializedEntries);

		logger.debug("Added new history entry for task #{} with {} existing entries: {}", task.getId(), numExistingEntries, existingEntries);
	}

	private static String serializeMaps(List<Map<String, String>> listOfHistoryEntryMaps) {
		if (listOfHistoryEntryMaps == null) {
			logger.warn("serialized null listOfHistoryEntryMaps into zero-length string \"\"");
			return "";
		}
		logger.debug("serializing listOfHistoryEntryMaps of size {}: {}", listOfHistoryEntryMaps.size(), listOfHistoryEntryMaps);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		XMLEncoder xmlEncoder = new XMLEncoder(bos);
		xmlEncoder.writeObject(listOfHistoryEntryMaps);
		xmlEncoder.close();

		String serializedMaps = bos.toString();
		
		logger.debug("serialized listOfHistoryEntryMaps of size {} into XML: {}", listOfHistoryEntryMaps.size(), serializedMaps);
		return serializedMaps;
	}

	private static List<Map<String, String>> deserializeMaps(String listOfSerializedMaps) {
		if (listOfSerializedMaps == null || listOfSerializedMaps.isEmpty()) {
			logger.warn("null or empty xml string passed.  Returning empty history entry map list.");
			return new ArrayList<Map<String, String>>();
		} else {
			XMLDecoder xmlDecoder = new XMLDecoder(new ByteArrayInputStream(listOfSerializedMaps.getBytes()));
			@SuppressWarnings("unchecked")
			List<Map<String, String>> parsedMaps = (List<Map<String, String>>) xmlDecoder.readObject();
			xmlDecoder.close();
			logger.debug("deserialized xml string \"{}\" into list of history entry maps with {} entries: {}", listOfSerializedMaps, parsedMaps.size(), parsedMaps.toString());
			return parsedMaps;
		}
	}

	public static void loadGridPane(GridPane gp, LocalTask task) {
		List<Map<String, String>> historyEntryMaps = getHistoryEntries(task);
		
		logger.debug("Loading GridPane with {} history entries from task #{}: {}", historyEntryMaps.size(), task.getId(), historyEntriesToString(historyEntryMaps));

		int counter = 0;
		for (Map<String, String> historyEntryMap : historyEntryMaps) {
			String owner = "Owner unset";
			String timestamp = "Timestamp unset";
			String comment = "Comment unset";
			String taskName = "Task name unset";
			
			for (String key : historyEntryMap.keySet()) {
				if (key.equals(WorkflowHistoryVariable.wf_hist_action_owner.toString())) {
					owner = historyEntryMap.get(key);
				} else if (key.equals(WorkflowHistoryVariable.wf_hist_action_time.toString())) {
					Long time = Long.parseLong(historyEntryMap.get(key));
					
			        timestamp = sdf.format(time);
				} else if (key.equals(WorkflowHistoryVariable.wf_hist_comment.toString())) {
					comment = historyEntryMap.get(key);
				} else if (key.equals(WorkflowHistoryVariable.wf_hist_task_name.toString())) {
					taskName = historyEntryMap.get(key);
				}
			}

			Label ownerLabel = new Label(owner);
			ownerLabel.setFont(new Font("System Bold", 14));
			Label taskNameLabel = new Label(taskName);
			taskNameLabel.setFont(new Font("System Bold", 14));
			Label timeLabel = new Label(timestamp);
			timeLabel.setFont(new Font("System Bold", 14));
			Label commentLabel = new Label(comment);
			commentLabel.setFont(new Font("System Bold", 14));

			gp.addRow(counter++, ownerLabel, taskNameLabel, timeLabel, commentLabel);
		}
	}
}
