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
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

import javax.inject.Singleton;

import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * {@link DroolsValidationFailureView}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Singleton
public class DroolsValidationFailureView extends Stage
{
	static Logger logger = LoggerFactory.getLogger(DroolsValidationFailureView.class);
	
	ListView<ResultsItem> results = new ListView<>();

	private DroolsValidationFailureView()
	{
		// created by HK2
		super(StageStyle.DECORATED);
		initModality(Modality.NONE);
		initOwner(null);
		setWidth(600);
		setHeight(300);
		//getIcons().add(Images.XML_VIEW_16.getImage());
		//getIcons().add(Images.XML_VIEW_32.getImage());
		setTitle("Drools Validation Failures");
		BorderPane bp = new BorderPane();
		Label l = new Label("Drools Validation Failures");
		l.getStyleClass().add("boldLabel");
		l.getStyleClass().add("headerBackground");
		l.setMaxWidth(Double.MAX_VALUE);
		l.setMinHeight(40.0);
		l.setAlignment(Pos.CENTER);
		bp.setTop(l);
		Scene scene = new Scene(bp);
		scene.getStylesheets().add(DroolsValidationFailureView.class.getResource("/isaac-shared-styles.css").toString());
		setScene(scene);
		
		results.setCellFactory(new Callback<ListView<ResultsItem>, ListCell<ResultsItem>>()
		{
			@Override
			public ListCell<ResultsItem> call(ListView<ResultsItem> param)
			{
				return new ResultsItemListCell();
			}
		});
		
		bp.setCenter(results);
	}

	public void addFailure(ResultsItem ri)
	{
		Platform.runLater(() ->
		{	
			results.getItems().add(ri);
			show();
		});
		Platform.runLater(() ->
		{	
			this.toFront();
		});
		
	}
}