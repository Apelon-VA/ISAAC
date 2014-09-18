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
import gov.va.isaac.interfaces.gui.views.ConceptWorkflowViewI;
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
 * {@link AdvanceWorkflowView}
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */

@Service
@PerLookup
public class AdvanceWorkflowView extends Stage implements ConceptWorkflowViewI
{
	private final Logger logger = LoggerFactory.getLogger(AdvanceWorkflowView.class);

	private AdvanceWorkflowViewController controller_;

	private boolean shown = false;
	
	private AdvanceWorkflowView() throws IOException
	{
		super();

		URL resource = this.getClass().getResource("AdvanceWorkflowView.fxml");
		FXMLLoader loader = new FXMLLoader(resource);
		Parent root = (Parent) loader.load();
		setScene(new Scene(root));
		getScene().getStylesheets().add(AdvanceWorkflowView.class.getResource("/isaac-shared-styles.css").toString());
		getIcons().add(Images.INBOX.getImage());

		controller_ = loader.getController();
		
		setTitle("Advance Workflow");
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

	public void setConcept(ConceptVersionBI concept) {
		// Make sure in application thread.
		FxUtils.checkFxUserThread();
		controller_.setConcept(concept);
	}
	
	@Override
	public void setConcept(UUID conceptUUID) {
		try {
			setConcept(ExtendedAppContext.getDataStore().getConceptVersion(WBUtility.getViewCoordinate(), conceptUUID));
		} catch (IOException e) {
			String title = "Unexpected error loading concept with UUID " + conceptUUID;
			String msg = e.getClass().getName();
			logger.error(title, e);
			AppContext.getCommonDialogs().showErrorDialog(title, msg, e.getMessage());
		}
	}

	@Override
	public void setConcept(int conceptNid) {
		try {
			setConcept(ExtendedAppContext.getDataStore().getConceptVersion(WBUtility.getViewCoordinate(), conceptNid));
		} catch (IOException e) {
			String title = "Unexpected error loading concept with Nid " + conceptNid;
			String msg = e.getClass().getName();
			logger.error(title, e);
			AppContext.getCommonDialogs().showErrorDialog(title, msg, e.getMessage());
		}
	}

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
	 * @see gov.va.isaac.interfaces.gui.views.ConceptWorkflowViewI#setInitialTask(long)
	 */
	@Override
	public void setInitialTask(long taskId) {
		controller_.setInitialTask(taskId);
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.gui.views.ConceptWorkflowViewI#getInitialTask()
	 */
	@Override
	public Long getInitialTask() {
		return controller_.getInitialTask() != null ? controller_.getInitialTask().getId() : null;
	}
}