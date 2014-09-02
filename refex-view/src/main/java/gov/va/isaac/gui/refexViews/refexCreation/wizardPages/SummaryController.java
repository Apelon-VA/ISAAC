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
package gov.va.isaac.gui.refexViews.refexCreation.wizardPages;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.refexViews.refexCreation.PanelControllersI;
import gov.va.isaac.gui.refexViews.refexCreation.RefexData;
import gov.va.isaac.gui.refexViews.refexCreation.ScreensController;
import gov.va.isaac.gui.refexViews.util.DynamicRefexDataColumnListCell;
import gov.va.isaac.util.WBUtility;
import java.beans.PropertyVetoException;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.RefexDynamicUsageDescriptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 
 * {@link SummaryController}
 *
 * @author <a href="jefron@apelon.com">Jesse Efron</a>
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class SummaryController implements PanelControllersI {
	@FXML private Label actualRefexName;
	@FXML private Label actualRefexDescription;
	@FXML private Label actualParentConcept;
	@FXML private Label actualRefexType;
	@FXML private BorderPane summaryPane;
	@FXML private ListView<RefexDynamicColumnInfo> detailsListView;
	@FXML private Button cancelButton;
	@FXML private Button startOverButton;
	@FXML private Button commitButton;
	@FXML private Button backButton;

	ScreensController processController_;
	Region sceneParent_;

	private final Logger logger = LoggerFactory.getLogger(SummaryController.class);

	@Override
	public void initialize() {
		assert actualRefexDescription != null : "fx:id=\"actualRefexDescription\" was not injected: check your FXML file 'summary.fxml'.";
		assert actualRefexName != null : "fx:id=\"actualRefexName\" was not injected: check your FXML file 'summary.fxml'.";
		assert cancelButton != null : "fx:id=\"cancelButton\" was not injected: check your FXML file 'summary.fxml'.";
		assert startOverButton != null : "fx:id=\"startOverButton\" was not injected: check your FXML file 'summary.fxml'.";
		assert commitButton != null : "fx:id=\"commitButton\" was not injected: check your FXML file 'summary.fxml'.";
		assert backButton != null : "fx:id=\"commitButton\" was not injected: check your FXML file 'summary.fxml'.";
		assert actualRefexType != null : "fx:id=\"actualRefexType\" was not injected: check your FXML file 'summary.fxml'.";
		assert summaryPane != null : "fx:id=\"summaryPane\" was not injected: check your FXML file 'summary.fxml'.";
		assert actualParentConcept != null : "fx:id=\"actualParentConcept\" was not injected: check your FXML file 'summary.fxml'.";
		assert detailsListView != null : "fx:id=\"detailsListView\" was not injected: check your FXML file 'summary.fxml'.";

		cancelButton.setOnAction(e -> ((Stage)summaryPane.getScene().getWindow()).close());
	
		commitButton.setOnAction(e -> 
		{
			storeValues();
			((Stage)summaryPane.getScene().getWindow()).close();
		});

		startOverButton.setOnAction(e -> processController_.showFirstScreen());
		backButton.setOnAction(e -> processController_.showPreviousScreen());
		
		detailsListView.setCellFactory(new Callback<ListView<RefexDynamicColumnInfo>, ListCell<RefexDynamicColumnInfo>>()
		{
			@Override
			public ListCell<RefexDynamicColumnInfo> call(ListView<RefexDynamicColumnInfo> param)
			{
				return new DynamicRefexDataColumnListCell();
			}
		});
	}

	private void setupRefexContent(RefexData refexData) {
		actualRefexName.setText(refexData.getRefexName());
		actualRefexDescription.setText(refexData.getRefexDescription());
		actualParentConcept.setText(WBUtility.getDescription(refexData.getParentConcept()));
		
		if (refexData.isAnnotatedStyle()) {
			actualRefexType.setText("Annotated");
		} else {
			actualRefexType.setText("Refset");
		}
		
		detailsListView.getItems().clear();
		detailsListView.getItems().addAll(refexData.getColumnInfo());
		detailsListView.scrollTo(0);
	}

	public void storeValues() {
		try {
			RefexData refexData = processController_.getWizardData();
			RefexDynamicUsageDescriptionBuilder.createNewRefexDynamicUsageDescriptionConcept(refexData.getRefexName(),
					refexData.getRefexName(), refexData.getRefexDescription(), refexData.getColumnInfo().toArray(new RefexDynamicColumnInfo[0]), 
					refexData.getParentConcept().getPrimordialUuid(), 
					refexData.isAnnotatedStyle());
		} catch (IOException | ContradictionException | InvalidCAB | PropertyVetoException e) {
			logger.error("Unable to create and/or commit refset concept and metadata", e);
			AppContext.getCommonDialogs().showErrorDialog("Error Creating Refex", "Unexpected error creating the Refex", e.getMessage(), summaryPane.getScene().getWindow());
		}
	}
	
	public void updateValues(RefexData refexData)
	{
		setupRefexContent(refexData);
	}

	/**
	 * @see gov.va.isaac.gui.refexViews.refexCreation.PanelControllersI#finishInit(gov.va.isaac.gui.refexViews.refexCreation.ScreensController, javafx.scene.Parent)
	 */
	@Override
	public void finishInit(ScreensController screenController, Region parent)
	{
		processController_ = screenController;
		sceneParent_ = parent;
	}

	/**
	 * @see gov.va.isaac.gui.refexViews.refexCreation.PanelControllersI#getParent()
	 */
	@Override
	public Region getParent()
	{
		return sceneParent_;
	}
}