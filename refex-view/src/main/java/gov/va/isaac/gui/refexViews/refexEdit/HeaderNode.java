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
package gov.va.isaac.gui.refexViews.refexEdit;

import gov.va.isaac.gui.dialog.UserPrompt.UserPromptResponse;
import gov.va.isaac.gui.util.Images;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
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
public class HeaderNode<T> {
	public static interface DataProvider<T> {
		public T getData(RefexDynamicGUI source);
	}
	public static class Filter<T> {
		private final ObservableList<Object> filterValues = new ObservableListWrapper<>(new ArrayList<>());
		private final ColumnId columnId;
		private DataProvider<T> dataProvider;
		/**
		 * @param columnKey
		 */
		public Filter(ColumnId columnId, DataProvider<T> dataProvider) {
			super();
			this.columnId = columnId;
			this.dataProvider = dataProvider;
		}
		
		public boolean accept(RefexDynamicGUI data) {
			if (filterValues.size() > 0) {
				return filterValues.contains(dataProvider.getData(data));
			} else {
				return true;
			}
		}
		
		public ObservableList<Object> getFilterValues() {
			return filterValues;
		}

		/**
		 * @return the columnId
		 */
		public ColumnId getColumnId() {
			return columnId;
		}
	}
	
	private final TreeTableColumn<RefexDynamicGUI, ?> column;
	private final Scene scene;
	private final DataProvider<T> dataProvider;
	private final Button filterConfigurationButton = new Button();
	private final Filter<T> filter;
	
	private final ImageView image = Images.FILTER_16.createImageView();

	@SuppressWarnings("unchecked")
	private Filter<T> castFilterFromCache(Filter<?> filter) {
		return (Filter<T>)filter;
	}
	
	public HeaderNode(
			ObservableMap<ColumnId, Filter<?>> filterCache,
			TreeTableColumn<RefexDynamicGUI, ?> col,
			ColumnId columnId,
			Scene scene,
			DataProvider<T> dataProvider) {
		this.column = col;
		this.scene = scene;
		
		this.image.setFitHeight(8);
		this.image.setFitWidth(8);
		this.dataProvider = dataProvider;
		
		if (filterCache.get(columnId) != null) {
			this.filter = castFilterFromCache(filterCache.get(columnId));
			this.filter.dataProvider = dataProvider;
		} else {
			this.filter = new Filter<>(columnId, dataProvider);
			filterCache.put(columnId, filter);
		}
		
		filterConfigurationButton.setGraphic(image);
		Platform.runLater(() ->
		{
			filterConfigurationButton.setTooltip(new Tooltip("Press to configure filters for " + col.getText()));
		});
		
		filter.getFilterValues().addListener(new ListChangeListener<Object>() {
			@Override
			public void onChanged(
					javafx.collections.ListChangeListener.Change<? extends Object> c) {
				updateButton();
			}
		});
		updateButton();

		filterConfigurationButton.setOnAction(event -> { setUserFilters(column.getText()); });
	}
	
	private void updateButton() {
		if (filter.getFilterValues().size() > 0) {
			filterConfigurationButton.setStyle(
					"-fx-background-color: red;"
							+ "-fx-padding: 0 0 0 0;");
		} else {
			filterConfigurationButton.setStyle(
					"-fx-background-color: white;"
							+ "-fx-padding: 0 0 0 0;");
		}
	}

	private static Set<Object> getUniqueDisplayObjects(TreeItem<RefexDynamicGUI> item, DataProvider<?> dataProvider) {
		Set<Object> stringSet = new HashSet<>();
		
		if (item == null) {
			return stringSet;
		}

		if (item.getValue() != null) {
			stringSet.add(dataProvider.getData(item.getValue()).toString());
		}
		
		for (TreeItem<RefexDynamicGUI> childItem : item.getChildren()) {
			stringSet.addAll(getUniqueDisplayObjects(childItem, dataProvider));
		}
		
		return stringSet;
	}
	
	private void setUserFilters(String text) {
		List<String> testList = new ArrayList<String>();

		for (Object obj : getUniqueDisplayObjects(column.getTreeTableView().getRoot(), dataProvider)) {
			if (obj != null) {
				testList.add(obj.toString());
			}
		}

		Collections.sort(testList);
		
		RefexContentFilterPrompt prompt = new RefexContentFilterPrompt(text, testList, filter.getFilterValues());
		prompt.showUserPrompt((Stage)scene.getWindow(), "Select Filters");

		if (prompt.getButtonSelected() == UserPromptResponse.APPROVE) {
			filter.getFilterValues().setAll(prompt.getSelectedValues());
		} else {
			filter.getFilterValues().clear();
		}
	}

	public Button getButton() { return filterConfigurationButton; }
	public TreeTableColumn<RefexDynamicGUI, ?> getColumn() { return column; }
	public ObservableList<Object> getUserFilters() { return filter.getFilterValues(); }

	public Node getNode() { return filterConfigurationButton; }
}
