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
import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.WorkflowInitiationViewI;
import gov.va.isaac.util.OTFUtility;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.glassfish.hk2.api.PerLookup;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link WorkflowInitiationViewI} which can be used to initiate a new workflow instance
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
@Service
@PerLookup
public class WorkflowInitiationView extends Stage implements WorkflowInitiationViewI
{
	private final Logger logger = LoggerFactory.getLogger(WorkflowInitiationView.class);

	private WorkflowInitiationViewController controller_;

	private boolean shown = false;
	
	private WorkflowInitiationView() throws IOException
	{
		super();

		URL resource = this.getClass().getResource("WorkflowInitiationView.fxml");
		FXMLLoader loader = new FXMLLoader(resource);
		Parent root = (Parent) loader.load();
		setScene(new Scene(root));
		getScene().getStylesheets().add(WorkflowInitiationView.class.getResource("/isaac-shared-styles.css").toString());
		getIcons().add(Images.INBOX.getImage());

		controller_ = loader.getController();
		
		controller_.setView(this);
		
		setTitle("Workflow Initiation");
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

	public void setComponent(ComponentVersionBI componentOrConcept) {
		// Make sure in application thread.
		FxUtils.checkFxUserThread();
		try {
			controller_.setComponent(componentOrConcept);
		} catch (Exception e) {
			String title = "Unexpected error loading component " + componentOrConcept;
			String msg = "Caught " + e.getClass().getName() + " " + e.getLocalizedMessage();
			logger.error(title + ". " + msg, e);
			AppContext.getCommonDialogs().showErrorDialog(title, msg, e.getMessage());
		}
	}
	
	@Override
	public void setComponent(UUID uuid) {
		try {
			setComponent(OTFUtility.getComponentVersion(uuid));
		} catch (Exception e) {
			String title = "Unexpected error loading component with UUID " + uuid;
			String msg = "Caught " + e.getClass().getName() + " " + e.getLocalizedMessage();
			logger.error(title + ". " + msg, e);
			AppContext.getCommonDialogs().showErrorDialog(title, msg, e.getMessage());
		}
	}

	@Override
	public void setComponent(int nid) {
		try {
			//TODO (artf231900) don't do BDB lookups on the FX Thread
			setComponent(OTFUtility.getComponentVersion(nid));
		} catch (Exception e) {
			String title = "Unexpected error loading component with UUID " + nid;
			String msg = "Caught " + e.getClass().getName() + " " + e.getLocalizedMessage();
			logger.error(title + ". " + msg, e);
			AppContext.getCommonDialogs().showErrorDialog(title, msg, e.getMessage());
		}
	}

	@Override
	public UUID getComponentUuid() {
		return controller_.getComponent().getPrimordialUuid();
	}

	@Override
	public int getComponentNid() {
		return controller_.getComponent().getNid();
	}
}