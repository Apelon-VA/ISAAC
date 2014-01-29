/**
 * Copyright 2014
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

import gov.va.legoEdit.model.SchemaToString;
import gov.va.legoEdit.model.schemaModel.LegoList;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import gov.va.legoEdit.util.Utility;
import java.io.IOException;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * LegoSummaryPane - Just a trivial view of the simplified toString for a Lego.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class LegoSummaryPane extends VBox
{
	private TextArea summary;
	private final LegoList legoList;
	
	Logger logger = LoggerFactory.getLogger(LegoSummaryPane.class);
	
	public LegoSummaryPane(final LegoList legoList)
	{
		this.legoList = legoList;
		getStyleClass().add("itemBorder");
		Label l = new Label("Summary");
		l.getStyleClass().add("boldLabel");
		getChildren().add(l);
		summary = new TextArea();
		summary.setEditable(false);
		summary.setWrapText(false);
		summary.setFocusTraversable(false);
		updateSummary();
		getChildren().add(summary);
		VBox.setVgrow(summary, Priority.ALWAYS);
		setMinWidth(400.0);
		setMinHeight(300.0);
		VBox.setVgrow(this, Priority.NEVER);
		Button export = new Button("Export");
		export.setOnAction(new EventHandler<ActionEvent>()
		{
			
			@Override
			public void handle(ActionEvent arg0)
			{
				ArrayList<LegoList> temp = new ArrayList<LegoList>();
				temp.add(BDBDataStoreImpl.getInstance().getLegoListByID(legoList.getLegoListUUID()));
				try
				{
					LegoGUI.export(temp, getScene().getWindow());
				}
				catch (IOException e)
				{
					logger.error("Unexpected error launching export", e);
				}
			}
		});
		getChildren().add(export);
	}
	
	private void updateSummary()
	{
		Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				final StringBuilder value = new StringBuilder();
				value.append(SchemaToString.summary(legoList));
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
				
			}
		};
		
		Utility.tpe.execute(r);
	}
}
