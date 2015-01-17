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
package gov.va.isaac.drools.gui;

import gov.va.isaac.drools.helper.ResultsItem;
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

/**
 * {@link ResultsItemListCell}
 *
 * Display code for a data column of a Dynamic Refex, when shown within a list view (one cell per refex column)
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class ResultsItemListCell extends ListCell<ResultsItem>
{
	//TODO display cleanup, mechnism to remove failures
	/**
	 * @see javafx.scene.control.Cell#updateItem(java.lang.Object, boolean)
	 */
	@Override
	protected void updateItem(ResultsItem item, boolean empty)
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
			constraint2.setHgrow(Priority.SOMETIMES);
			gp.getColumnConstraints().add(constraint2);

			int row = 0;

			gp.add(wrapAndStyle(makeBoldLabel("Message"), row), 0, row);
			Label message = new Label(item.getMessage());
			message.setWrapText(true);
			message.maxWidthProperty().bind(this.widthProperty().subtract(180));
			gp.add(wrapAndStyle(message, row), 1, row++);

			gp.add(wrapAndStyle(makeBoldLabel("Severity"), row), 0, row);
			Label severity = new Label(item.getSeverity().getName());
			severity.setWrapText(true);
			severity.maxWidthProperty().bind(this.widthProperty().subtract(180));
			gp.add(wrapAndStyle(severity, row), 1, row++);
			
			gp.add(wrapAndStyle(makeBoldLabel("Rule UUID"), row), 0, row);
			Label ruleUUID = new Label(item.getRuleUuid());
			ruleUUID.setWrapText(true);
			ruleUUID.maxWidthProperty().bind(this.widthProperty().subtract(180));
			gp.add(wrapAndStyle(ruleUUID, row), 1, row++);
			
			gp.add(wrapAndStyle(makeBoldLabel("Error Code"), row), 0, row);
			Label errorCode = new Label(item.getErrorCode() + "");
			errorCode.setWrapText(true);
			errorCode.maxWidthProperty().bind(this.widthProperty().subtract(180));
			gp.add(wrapAndStyle(errorCode, row), 1, row++);

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
