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
 * HeaderNode
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.gui.refexViews.refexEdit;

import gov.va.isaac.gui.dialog.UserPrompt.UserPromptResponse;
import gov.va.isaac.gui.util.Images;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import com.sun.javafx.collections.ObservableListWrapper;

/**
 * HeaderNode
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 *
 */
public class HeaderNode {
	public static interface StringProvider {
		public String getString(RefexDynamicGUI source);
	}
	private final Button filterConfigurationButton = new Button();
	private final TreeTableColumn<RefexDynamicGUI, ?> column;
	private final ObservableList<String> valuesToFilter = new ObservableListWrapper<String>(new ArrayList<>());
	private final Scene scene;
	private final StringProvider stringProvider;
	
	
	private final ImageView image = Images.FILTER_16.createImageView();

	public HeaderNode(TreeTableColumn<RefexDynamicGUI, ?> col, Scene scene, StringProvider stringProvider) {
		this.column = col;
		this.scene = scene;
		this.image.setFitHeight(8);
		this.image.setFitWidth(8);
		this.stringProvider = stringProvider;

//		if (label.getText().startsWith("info model property ")) {
//			label.setText(label.getText().replace("info model property ",""));
//		}
		
		filterConfigurationButton.setGraphic(image);
		
		valuesToFilter.addListener(new ListChangeListener<String>() {
			@Override
			public void onChanged(
					javafx.collections.ListChangeListener.Change<? extends String> c) {
				updateButton();
			}
		});
		updateButton();
		
		filterConfigurationButton.setOnAction(event -> { setUserFilters(column.getText()); });
	}
	
	private void updateButton() {
		if (valuesToFilter.size() > 0) {
			filterConfigurationButton.setStyle(
					"-fx-background-color: red;"
							//+ "-fx-text-fill: white;"
							+ "-fx-padding: 0 0 0 0;");
		} else {
			filterConfigurationButton.setStyle(
					"-fx-background-color: white;"
							//+ "-fx-text-fill: white;"
							+ "-fx-padding: 0 0 0 0;");
		}
	}

	private static Set<String> getUniqueDisplayStrings(TreeItem<RefexDynamicGUI> item, StringProvider stringProvider) {
		Set<String> stringSet = new HashSet<>();
		
		if (item == null) {
			return stringSet;
		}

		if (item.getValue() != null) {
			stringSet.add(stringProvider.getString(item.getValue()));
		}
		
		for (TreeItem<RefexDynamicGUI> childItem : item.getChildren()) {
			stringSet.addAll(getUniqueDisplayStrings(childItem, stringProvider));
		}
		
		return stringSet;
	}
	
	private void setUserFilters(String text) {
		List<String> testList = new ArrayList<String>();
		testList.addAll(getUniqueDisplayStrings(column.getTreeTableView().getRoot(), stringProvider));
		
		Collections.sort(testList);
		
		RefexContentFilterPrompt prompt = new RefexContentFilterPrompt(text, testList);
		prompt.showUserPrompt((Stage)scene.getWindow(), "Select Filters");

		if (prompt.getButtonSelected() == UserPromptResponse.APPROVE) {
			valuesToFilter.setAll(prompt.getSelectedValues());
		} else {		
			valuesToFilter.clear();
		}
	}

	public Button getButton() { return filterConfigurationButton; }
	public TreeTableColumn<RefexDynamicGUI, ?> getColumn() { return column; }
	public ObservableList<String> getUserFilters() { return valuesToFilter; }

	public Node getNode() { return filterConfigurationButton; }
}
