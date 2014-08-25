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
package gov.va.isaac.gui.refexViews.util;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicByteArray;

/**
 * {@link DynamicRefexDataColumnListCell}
 *
 * Display code for a data column of a Dynamic Refex, when shown within a list view (one cell per refex column)
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DynamicRefexDataColumnListCell extends ListCell<RefexDynamicColumnInfo>
{
	/**
	 * @see javafx.scene.control.Cell#updateItem(java.lang.Object, boolean)
	 */
	@Override
	protected void updateItem(RefexDynamicColumnInfo item, boolean empty)
	{
		super.updateItem(item, empty);
		if (item != null)
		{
			setText("");

			GridPane gp = new GridPane();
			gp.setHgap(0.0);
			gp.setVgap(0.0);
			gp.setPadding(new Insets(5, 5, 5, 5));
			gp.setMinWidth(250);

			ColumnConstraints constraint1 = new ColumnConstraints();
			constraint1.setFillWidth(false);
			constraint1.setHgrow(Priority.NEVER);
			constraint1.setMinWidth(160);
			constraint1.setMaxWidth(160);
			gp.getColumnConstraints().add(constraint1);

			ColumnConstraints constraint2 = new ColumnConstraints();
			constraint2.setFillWidth(true);
			constraint2.setHgrow(Priority.ALWAYS);
			gp.getColumnConstraints().add(constraint2);

			int row = 0;

			gp.add(wrapAndStyle(makeBoldLabel("Column Name"), row), 0, row);
			Label name = new Label(item.getColumnName());
			name.setWrapText(true);
			name.maxWidthProperty().bind(this.widthProperty().subtract(210));
			gp.add(wrapAndStyle(name, row), 1, row++);

			gp.add(wrapAndStyle(makeBoldLabel("Column Description"), row), 0, row);
			Label description = new Label(item.getColumnDescription());
			description.setWrapText(true);
			description.maxWidthProperty().bind(this.widthProperty().subtract(210));

			gp.add(wrapAndStyle(description, row), 1, row++);

			gp.add(wrapAndStyle(makeBoldLabel("Column Order"), row), 0, row);
			gp.add(wrapAndStyle(new Label(item.getColumnOrder() + 1 + ""), row), 1, row++);

			gp.add(wrapAndStyle(makeBoldLabel("Data Type"), row), 0, row);
			gp.add(wrapAndStyle(new Label(item.getColumnDataType().getDisplayName()), row), 1, row++);

			gp.add(wrapAndStyle(makeBoldLabel("Column Required"), row), 0, row);
			gp.add(wrapAndStyle(new Label(item.isColumnRequired() + ""), row), 1, row++);

			gp.add(wrapAndStyle(makeBoldLabel("Default Value"), row), 0, row);
			String temp = "";
			if (item.getDefaultColumnValue() != null)
			{
				if (item.getColumnDataType() == RefexDynamicDataType.BYTEARRAY)
				{
					temp = "Byte array of size " + ((RefexDynamicByteArray) item.getDefaultColumnValue()).getDataByteArray().length;
				}
				else
				{
					temp = item.getDefaultColumnValue().getDataObject().toString();
				}
			}
			Label defaultValue = new Label(temp);
			defaultValue.setWrapText(true);
			defaultValue.maxWidthProperty().bind(this.widthProperty().subtract(210));
			gp.add(wrapAndStyle(defaultValue, row), 1, row++);

			gp.add(wrapAndStyle(makeBoldLabel("Validator"), row), 0, row);
			gp.add(wrapAndStyle(new Label(item.getValidator() == null ? "" : item.getValidator().getDisplayName()), row), 1, row++);

			if (item.getValidator() != null)
			{
				gp.add(wrapAndStyle(makeBoldLabel("Validator Data"), row), 0, row);
				String valdiatorData = "";
				if (item.getValidatorData().getRefexDataType() == RefexDynamicDataType.BYTEARRAY)
				{
					valdiatorData = "Byte array of size " + ((RefexDynamicByteArray) item.getValidatorData()).getDataByteArray().length;
				}
				else
				{
					valdiatorData = item.getValidatorData().getDataObject().toString();
				}
				Label valData = new Label(valdiatorData);
				valData.setWrapText(true);
				valData.maxWidthProperty().bind(this.widthProperty().subtract(210));
				gp.add(wrapAndStyle(valData, row), 1, row++);
			}

			setGraphic(gp);
			this.setStyle("-fx-border-width:  0 0 2 0; -fx-border-color: grey; ");

		}
		else
		{
			setText("");
			setGraphic(null);
			this.setStyle("");
		}
	}

	private Node wrapAndStyle(Region node, int rowNumber)
	{
		Pane p = new Pane(node);
		node.setPadding(new Insets(5.0));
		GridPane.setFillWidth(p, true);
		p.minHeightProperty().bind(node.heightProperty());
		//Hack - wrapped labels don't seem to fire their height property changes at the right time - leaving the surrounding Pane node too small.
		//this seems to help...
		Platform.runLater(() -> p.autosize());
		p.getStyleClass().add(((rowNumber % 2 == 0) ? "evenGridRow" : "oddGridRow"));
		return p;
	}

	private Label makeBoldLabel(String labelText)
	{
		Label l = new Label(labelText);
		l.getStyleClass().add("boldLabel");
		return l;
	}
}
