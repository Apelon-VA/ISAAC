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
package gov.va.isaac.gui.conceptViews;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.gui.MenuItemI;
import gov.va.isaac.interfaces.gui.views.ConceptViewI;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.glassfish.hk2.api.PerLookup;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.RefexPolicy;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.RelationshipPolicy;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.VersionPolicy;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * @author <a href="jefron@apelon.com">Jesse Efron</a>
 */
@Service
@PerLookup
public class SimpleConceptView implements ConceptViewI {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private final SimpleConceptViewController controller;
	private Parent root;

    private SimpleConceptView() throws IOException {
        // Load from FXML.
        URL resource = this.getClass().getResource("SimpleView.fxml");
        FXMLLoader loader = new FXMLLoader(resource);
        root = (Parent) loader.load();

        this.controller = loader.getController();
    }

    private Stage getStage() {
    	Stage st = new Stage();
    	
        st.initOwner(null);
        st.initModality(Modality.NONE);
        st.initStyle(StageStyle.DECORATED);

        st.setScene(new Scene(root));
        st.getScene().getStylesheets().add(SimpleConceptView.class.getResource("/isaac-shared-styles.css").toString());
        st.getIcons().add(Images.CONCEPT_VIEW.getImage());
        
        return st;
    }
    
    // Put this on list tab
    public Node getConceptViewerPanel() {
    	return root;
    }

    private void setConcept(ConceptChronicleDdo concept) {
        // Make sure in application thread.
        FxUtils.checkFxUserThread();
        controller.setConcept(concept);

        // Title will change after concept is set.
        Stage st = getStage();
        st.setTitle(controller.getTitle());
        st.show();
        //doesn't come to the front unless you do this (on linux, at least)
        Platform.runLater(() -> {st.toFront();});
    }
    
    // Put this on right-click List 
    @Override
	public void setConcept(final UUID conceptUUID)
    {
        // TODO this needs to be rewritten so that the dialog displays immediately
        //but with a progress indicator while we wait for the concept to be found..
         Task<ConceptChronicleDdo> task = new Task<ConceptChronicleDdo>()
         {
             @Override
             protected ConceptChronicleDdo call() throws Exception
             {
                 LOG.info("Loading concept with UUID " + conceptUUID);
                 ConceptChronicleDdo concept = ExtendedAppContext.getDataStore().getFxConcept(conceptUUID, WBUtility.getViewCoordinate(),
                         VersionPolicy.ACTIVE_VERSIONS, RefexPolicy.REFEX_MEMBERS, RelationshipPolicy.ORIGINATING_AND_DESTINATION_TAXONOMY_RELATIONSHIPS);
                 LOG.info("Finished loading concept with UUID " + conceptUUID);

                 return concept;
             }

             @Override
             protected void succeeded()
             {
                 try
                {
                    ConceptChronicleDdo result = this.getValue();
                    setConcept(result);
                }
                catch (Exception e)
                {
                     String title = "Unexpected error loading concept with UUID " + conceptUUID;
                     String msg = e.getClass().getName();
                     LOG.error(title, e);
                     AppContext.getCommonDialogs().showErrorDialog(title, msg, e.getMessage());
                }
             }

             @Override
             protected void failed()
             {
                 Throwable ex = getException();
                 String title = "Unexpected error loading concept with UUID " + conceptUUID;
                 String msg = ex.getClass().getName();
                 LOG.error(title, ex);
                 AppContext.getCommonDialogs().showErrorDialog(title, msg, ex.getMessage());
             }
         };

         Utility.execute(task);
    }

    //TODO concept-view-tree is not stopping background threaded operations when this window is closed....
    //TODO is also seems to fall into infinite loops at times...
    
    /**
     * @see gov.va.isaac.interfaces.gui.views.ConceptViewI#showConcept(int)
     */
    // Put this on right-click List 
    @Override
    public void setConcept(int nid)
    {
        //TODO fix threading issues on this too...
        try
        {
            ConceptChronicleBI concept = ExtendedAppContext.getDataStore().getConcept(nid);
            if (concept != null)
            {
                setConcept(concept.getPrimordialUuid());
            }
        }
        catch (IOException e)
        {
            String title = "Unexpected error loading concept with nid " + nid;
            String msg = e.getClass().getName();
            LOG.error(title, e);
            AppContext.getCommonDialogs().showErrorDialog(title, msg, e.getMessage());
        }

    }


	/**
	 * @see gov.va.isaac.interfaces.gui.views.SimpleConceptView#getView()
	 */
	@Override
	public Region getView()
	{
		//TODO 
		return new Label("Not implemented");
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.SimpleConceptView#getMenuBarMenus()
	 */
	@Override
	public List<MenuItemI> getMenuBarMenus()
	{
		// TODO
		return new ArrayList<MenuItemI>();
	}
}
