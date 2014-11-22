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
import java.util.List;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ToggleButton;
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
	public interface Filter {
		// Mockup
	}
	private final ToggleButton filterConfigurationButton = new ToggleButton();
	private final TreeTableColumn<RefexDynamicGUI, ?> column;
	private final ObservableList<String> valuesToFilter = new ObservableListWrapper<String>(new ArrayList<>());
	private Scene scene;
	
	private final ImageView image = Images.FILTER_16.createImageView();

	public HeaderNode(TreeTableColumn<RefexDynamicGUI, ?> col, Scene scene) {
		column = col;
		this.scene = scene;

		image.setFitHeight(8);
		image.setFitWidth(8);

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
		filterConfigurationButton.selectedProperty().set(valuesToFilter.size() > 0);
	}
	
	private void setUserFilters(String text) {
		List<String> testList = new ArrayList<String>();
		testList.add("Jesse");
		testList.add("Dan");
		testList.add("Joel");
		
		RefexContentFilterPrompt prompt = new RefexContentFilterPrompt(text, testList);
		prompt.showUserPrompt((Stage)scene.getWindow(), "Select Filters");

		if (prompt.getButtonSelected() == UserPromptResponse.APPROVE) {
			valuesToFilter.setAll(prompt.getSelectedValues());
		} else {		
			valuesToFilter.clear();
		}
	}

	public ToggleButton getButton() { return filterConfigurationButton; }
	public TreeTableColumn<RefexDynamicGUI, ?> getColumn() { return column; }
	public ObservableList<String> getUserFilters() { return valuesToFilter; }

	public Node getNode() { return filterConfigurationButton; }
	
//	private void updateTextFillColor() {
//		Color color = Color.BLACK;
//		if (valuesToFilter.size() == 0) {
//			color = Color.BLACK;
//		} else {
//			color = Color.RED;
//		}
//		filterConfigurationButton.setTextFill(color);
//	}
}
