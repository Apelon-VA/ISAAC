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
package gov.va.isaac.workflow.gui;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.gui.util.FxUtils;
//import gov.va.isaac.gui.conceptViews.SimpleConceptView;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.gui.views.ConceptViewMode;
import gov.va.isaac.interfaces.gui.views.WorkflowAdvancementViewI;
import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import org.glassfish.hk2.api.PerLookup;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link WorkflowAdvancementView}
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */

@Service
@PerLookup
public class WorkflowAdvancementView extends Stage implements WorkflowAdvancementViewI
{
	private final Logger logger = LoggerFactory.getLogger(WorkflowAdvancementView.class);

	private WorkflowAdvancementViewController controller_;

	private boolean shown = false;
	
	private WorkflowAdvancementView() throws IOException
	{
		super();

		URL resource = this.getClass().getResource("WorkflowAdvancementView.fxml");
		FXMLLoader loader = new FXMLLoader(resource);
		Parent root = (Parent) loader.load();
		setScene(new Scene(root));
		getScene().getStylesheets().add(WorkflowAdvancementView.class.getResource("/isaac-shared-styles.css").toString());
		getIcons().add(Images.INBOX.getImage());

		controller_ = loader.getController();
		
		setTitle("Workflow Advancement");
		setResizable(true);

		setWidth(600);
		setHeight(400);
	}

	/**
	 * Call setReferencedComponent first
	 * 
	 * @see gov.va.isaac.interfaces.gui.views.PopupViewI#showView(javafx.stage.Window)
	 */
	@Override
	public void showView(Window parent)
	{
		if (! shown) {
			shown = true;

			initOwner(parent);
			initModality(Modality.NONE);
			initStyle(StageStyle.DECORATED);
		}

		show();
	}

//	public void setConcept(ConceptVersionBI concept) {
//		// Make sure in application thread.
//		FxUtils.checkFxUserThread();
//		controller_.setConcept(concept);
//	}
	
//	@Override
//	public void setConcept(UUID conceptUUID) {
//		try {
//			setConcept(ExtendedAppContext.getDataStore().getConceptVersion(WBUtility.getViewCoordinate(), conceptUUID));
//		} catch (IOException e) {
//			String title = "Unexpected error loading concept with UUID " + conceptUUID;
//			String msg = e.getClass().getName();
//			logger.error(title, e);
//			AppContext.getCommonDialogs().showErrorDialog(title, msg, e.getMessage());
//		}
//	}
//
//	@Override
//	public void setConcept(int conceptNid) {
//		try {
//			setConcept(ExtendedAppContext.getDataStore().getConceptVersion(WBUtility.getViewCoordinate(), conceptNid));
//		} catch (IOException e) {
//			String title = "Unexpected error loading concept with Nid " + conceptNid;
//			String msg = e.getClass().getName();
//			logger.error(title, e);
//			AppContext.getCommonDialogs().showErrorDialog(title, msg, e.getMessage());
//		}
//	}

	@Override
	public Region getView() {
		return controller_.getRootNode();
	}

	@Override
	public UUID getConceptUuid() {
		return controller_.getConcept().getPrimordialUuid();
	}

	@Override
	public int getConceptNid() {
		return controller_.getConcept().getConceptNid();
	}

	@Override
	public void setViewMode(ConceptViewMode mode) {
		throw new RuntimeException("setViewMode(" + mode + ") not supported");
	}

	@Override
	public ConceptViewMode getViewMode() {
		return null;
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.gui.views.WorkflowAdvancementViewI#setInitialTask(long)
	 */
	@Override
	public void setTask(long taskId) {
		FxUtils.checkFxUserThread();

		controller_.setTask(taskId);
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.gui.views.WorkflowAdvancementViewI#getInitialTask()
	 */
	@Override
	public Long getTask() {
		return controller_.getTask() != null ? controller_.getTask().getId() : null;
	}
}