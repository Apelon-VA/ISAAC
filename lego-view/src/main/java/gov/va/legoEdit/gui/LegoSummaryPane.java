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
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.legoEdit.gui;

import gov.va.isaac.util.Utility;
import gov.va.legoEdit.model.SchemaToString;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.LegoList;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * {@link LegoSummaryPane} Just a trivial view of the simplified toString for a LegoList or a Lego.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class LegoSummaryPane extends VBox
{
	private TextArea summary;
	private String title;
	Logger logger = LoggerFactory.getLogger(LegoSummaryPane.class);
	private String legoListUUID_;
	
	public LegoSummaryPane(LegoList legoList)
	{
		legoListUUID_ = legoList.getLegoListUUID();
		title = "LEGO List " + legoList.getGroupName(); 
		setup(() ->
		{
			return SchemaToString.summary(legoList);
		});
	}
	
	public LegoSummaryPane(Lego lego)
	{
		title = "LEGO " + lego.getLegoUUID(); 
		setup(() ->
		{
			return SchemaToString.summary(lego);
		});
	}
	
	private void setup(Supplier<String> stringRepresentation)
	{
		getStyleClass().add("itemBorder");
		Label l = new Label("Summary");
		l.getStyleClass().add("boldLabel");
		getChildren().add(l);
		summary = new TextArea();
		summary.setEditable(false);
		summary.setWrapText(false);
		summary.setFocusTraversable(false);
		updateSummary(stringRepresentation);
		getChildren().add(summary);
		VBox.setVgrow(summary, Priority.ALWAYS);
		setMinWidth(400.0);
		setMinHeight(300.0);
		VBox.setVgrow(this, Priority.NEVER);
		if (legoListUUID_ != null)
		{
			Button export = new Button("Export");
			export.setOnAction((a) ->
			{
				ArrayList<LegoList> temp = new ArrayList<LegoList>();
				temp.add(BDBDataStoreImpl.getInstance().getLegoListByID(legoListUUID_));
				try
				{
					new ExportDialog(temp, getScene().getWindow());
				}
				catch (IOException e)
				{
					logger.error("Unexpected error launching export", e);
				}
			});
			getChildren().add(export);
		}
	}
	
	private void updateSummary(Supplier<String> stringRepresentation)
	{
		Utility.execute(() -> 
		{
			final StringBuilder value = new StringBuilder();
			value.append(stringRepresentation.get());
			Platform.runLater(new Runnable()
			{
				@Override
				public void run()
				{
					double scrollTop = summary.getScrollTop();
					summary.setText(value.toString());
					summary.setScrollTop(scrollTop);
				}
			});
		});
	}
	
	public void show()
	{
		Stage stage = new Stage();
		stage.setScene(new Scene(this));
		stage.getScene().getStylesheets().add(LegoSummaryPane.class.getResource("/isaac-shared-styles.css").toString());
		stage.setTitle(title);
		stage.setWidth(700);
		stage.setHeight(600);
		stage.show();
	}
}
