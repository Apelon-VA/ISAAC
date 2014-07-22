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
import gov.va.isaac.interfaces.gui.ApplicationMenus;
import gov.va.isaac.interfaces.gui.MenuItemI;
import gov.va.isaac.interfaces.gui.views.ConceptViewMode;
import gov.va.isaac.interfaces.gui.views.ConceptWorkflowViewI;
import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
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
 * {@link ConceptDetailWorkflow}
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */

@Service
@PerLookup
public class ConceptDetailWorkflow extends Stage implements ConceptWorkflowViewI
{
	private final Logger logger = LoggerFactory.getLogger(ConceptDetailWorkflow.class);

	private ConceptDetailWorkflowController controller_;

	private boolean shown = false;
	
	private ConceptDetailWorkflow() throws IOException
	{
		super();

		URL resource = this.getClass().getResource("ConceptDetailWorkflow.fxml");
		FXMLLoader loader = new FXMLLoader(resource);
		Parent root = (Parent) loader.load();
		setScene(new Scene(root));
		getScene().getStylesheets().add(ConceptDetailWorkflow.class.getResource("/isaac-shared-styles.css").toString());
		getIcons().add(Images.INBOX.getImage());

		controller_ = loader.getController();
		
		setTitle("Concept Detail Workflow");
		setResizable(true);

		setWidth(600);
		setHeight(400);
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.IsaacViewI#getMenuBarMenus()
	 */
	@Override
	public List<MenuItemI> getMenuBarMenus()
	{
		// We don't currently have any custom menus with this view
		ArrayList<MenuItemI> menus = new ArrayList<>();
		
		MenuItemI mi = new MenuItemI()
		{
			@Override
			public void handleMenuSelection(Window parent)
			{
				showView(parent);
			}
			
			@Override
			public int getSortOrder()
			{
				return 25;
			}
			
			@Override
			public String getParentMenuId()
			{
				return ApplicationMenus.ACTIONS.getMenuId();
			}
			
			@Override
			public String getMenuName()
			{
				return "View Concept Detail Workflow";
			}
			
			@Override
			public String getMenuId()
			{
				return "viewConceptDetailWorkflowMenu";
			}
			
			@Override
			public boolean enableMnemonicParsing()
			{
				return false;
			}

			/**
			 * @see gov.va.isaac.interfaces.gui.MenuItemI#getImage()
			 */
			@Override
			public Image getImage()
			{
				return Images.INBOX.getImage();
			}
		};
		menus.add(mi);
		return menus;
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

	public Region getView() {
		return controller_.getRootNode();
	}

	@Override
	public UUID getConceptUuid() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getConceptNid() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setViewMode(ConceptViewMode mode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ConceptViewMode getViewMode() {
		// TODO Auto-generated method stub
		return null;
	}
}