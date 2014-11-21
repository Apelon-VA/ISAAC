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

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.dialog.UserPrompt.UserPromptResponse;
import gov.va.isaac.gui.util.Images;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

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
	final ObservableList<Filter> filters = new ObservableListWrapper<Filter>(new ArrayList<>());
	final HBox hbox = new HBox();
	final Label label;
	final Button filterConfigurationButton = new Button();
	final TreeTableColumn<RefexDynamicGUI, ?> column;
	private ObservableList<String> valuesToFilter;

	public HeaderNode(TreeTableColumn<RefexDynamicGUI, ?> col) {
		this(col, new Label(col.getText()));
	}

	public HeaderNode(TreeTableColumn<RefexDynamicGUI, ?> col, Label label) {
		column = col;
		this.label = label;
		filterConfigurationButton.setGraphic(Images.EXCLAMATION.createImageView());
		
		hbox.getChildren().addAll(label, filterConfigurationButton);

		filters.addListener(new ListChangeListener<Filter>() {
			@Override
			public void onChanged(
					javafx.collections.ListChangeListener.Change<? extends Filter> c) {
				updateTextFillColor();
			}
		});
		
		filterConfigurationButton.setOnAction(event -> { getUserFilters(label.getText()); });

		updateTextFillColor();
	}
	
	private void getUserFilters(String text) {
		List<String> testList = new ArrayList<String>();
		testList.add("Jesse");
		testList.add("Dan");
		testList.add("Joel");
		
		RefexContentFilterPrompt prompt = new RefexContentFilterPrompt(text, testList);
		prompt.showUserPrompt(AppContext.getMainApplicationWindow().getPrimaryStage(), "Select Filters");


		if (prompt.getButtonSelected() == UserPromptResponse.APPROVE) {
			valuesToFilter = prompt.getSelectedValues();
		} else {		
			valuesToFilter = null;
		}
	}

	public ObservableList<Filter> getFilters() { return filters; }
	public Label getLabel() { return label; }
	public Button getButton() { return filterConfigurationButton; }
	public TreeTableColumn<RefexDynamicGUI, ?> getColumn() { return column; }
	
	public javafx.scene.Node getNode() { return hbox; }
	
	private void updateTextFillColor() {
		Color color = Color.BLACK;
		if (filters.size() == 0) {
			color = Color.BLACK;
		} else {
			color = Color.RED;
		}
		label.setTextFill(color);
	}
}
