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
import gov.va.isaac.interfaces.gui.views.EnhancedConceptViewI;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.UUID;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.glassfish.hk2.api.PerLookup;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
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
public class EnhancedConceptView implements EnhancedConceptViewI {


	public class ConceptViewStage  extends Stage{

		private Stack<Integer> previousConceptStack;

		public ConceptViewStage(Stack<Integer> stack) {
			previousConceptStack = stack;
		}

		public Stack<Integer> getPreviousConceptStack() {
			return previousConceptStack;
		}

	}

	private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private final SimpleConceptViewController basicController;
    private DetailConceptViewController detailedController = null;
    
	private Region root;
/*
        URL resource = this.getClass().getResource("SimpleView.fxml");
        FXMLLoader loader = new FXMLLoader(resource);
        root = (Parent) loader.load();
        root.getStylesheets().add(SimpleConceptView.class.getResource("SimpleView.css").toString());

        this.basicController = loader.getController();

 */
    private EnhancedConceptView() throws IOException {
        // Load from FXML.
        URL basicResource = this.getClass().getResource("SimpleView.fxml");
        FXMLLoader basicLoader = new FXMLLoader(basicResource);
        root = (Region) basicLoader.load();
        root.getStylesheets().add(EnhancedConceptView.class.getResource("SimpleView.css").toString());
        this.basicController = basicLoader.getController();
    }

    private ConceptViewStage getStage(Stack<Integer> stack) {
    	ConceptViewStage st = new ConceptViewStage(stack);
    	
        st.initOwner(null);
        st.initModality(Modality.NONE);
        st.initStyle(StageStyle.DECORATED);

        st.setScene(new Scene(root));
        st.getScene().getStylesheets().add(EnhancedConceptView.class.getResource("/isaac-shared-styles.css").toString());
        st.getIcons().add(Images.CONCEPT_VIEW.getImage());
        
        return st;
    }
    
    /**
     * @see gov.va.isaac.interfaces.gui.views.EnhancedConceptViewI#getConceptViewerPanel(UUID)
     */
	@Override
    public Node getConceptViewerPanel(UUID conceptUUID) {
    	try
    	{
	        LOG.info("Loading concept with UUID " + conceptUUID);
	        ConceptChronicleDdo concept = ExtendedAppContext.getDataStore().getFxConcept(conceptUUID, WBUtility.getViewCoordinate(),
	                VersionPolicy.ACTIVE_VERSIONS, RefexPolicy.REFEX_MEMBERS, RelationshipPolicy.ORIGINATING_AND_DESTINATION_TAXONOMY_RELATIONSHIPS);
	        LOG.info("Finished loading concept with UUID " + conceptUUID);
	        basicController.setConcept(concept, ViewType.SIMPLE_VIEW);
    	} catch (IOException | ContradictionException e) {
            String title = "Unexpected error loading concept with UUID " + conceptUUID;
            String msg = e.getClass().getName();
            LOG.error(title, e);
            AppContext.getCommonDialogs().showErrorDialog(title, msg, e.getMessage());
    	}

    	return getView();
    }

    /**
     * @see gov.va.isaac.interfaces.gui.views.EnhancedConceptViewI#getConceptViewerPanel(int)
     */
	@Override
    public Node getConceptViewerPanel(int conceptNid) {        
    	try
    	{
	        ConceptChronicleBI concept = ExtendedAppContext.getDataStore().getConcept(conceptNid);
	        return getConceptViewerPanel(concept.getPrimordialUuid());
    	} catch (IOException e) {
            String title = "Unexpected error loading concept with nid " + conceptNid;
            String msg = e.getClass().getName();
            LOG.error(title, e);
            AppContext.getCommonDialogs().showErrorDialog(title, msg, e.getMessage());
        	
            return getView();
    	}
    }
	
    /**
     * @param view 
     * @see gov.va.isaac.interfaces.gui.views.EnhancedConceptViewI#changeConcept(Stage, UUID)
     */
	@Override
    public Node changeConcept(Stage stage, UUID conceptUUID, ViewType view) {
    	try
    	{
	        LOG.info("Loading concept with UUID " + conceptUUID);
	        ConceptChronicleDdo concept = ExtendedAppContext.getDataStore().getFxConcept(conceptUUID, WBUtility.getViewCoordinate(),
	                VersionPolicy.ACTIVE_VERSIONS, RefexPolicy.REFEX_MEMBERS, RelationshipPolicy.ORIGINATING_AND_DESTINATION_TAXONOMY_RELATIONSHIPS);
	        LOG.info("Finished loading concept with UUID " + conceptUUID);
	        
	        if (view == ViewType.SIMPLE_VIEW) {
		        basicController.setConcept(concept, view, ((ConceptViewStage)stage).getPreviousConceptStack());
	        } else if (view == ViewType.DETAIL_VIEW) {
	        	if (detailedController == null) {
	                URL detailedResource = this.getClass().getResource("DetailedView.fxml");
	                FXMLLoader detailedLoader = new FXMLLoader(detailedResource);
	                //TODO umm... we have handed out references to this node previously... which could be anywhere in the GUI... and now we just swap it?
	                //This GUI needs to figure out if it is providing a panel, or if it is providing a popup... if it is providing a panel... we can't just 
	                //toss the old panel, and use a new one out of the blue....
	                root = (Region) detailedLoader.load();
	                root.getStylesheets().add(EnhancedConceptView.class.getResource("SimpleView.css").toString());
	                this.detailedController = detailedLoader.getController();
	        	}
	        	detailedController.setConcept(concept, view, ((ConceptViewStage)stage).getPreviousConceptStack());
	        }
    	} catch (IOException | ContradictionException e) {
            String title = "Unexpected error loading concept with UUID " + conceptUUID;
            String msg = e.getClass().getName();
            LOG.error(title, e);
            AppContext.getCommonDialogs().showErrorDialog(title, msg, e.getMessage());
    	}

    	//TODO this IS a stage.  Why are we mucking with a different stage???
    	stage.setScene(new Scene(root));

    	return getView();
    }

    
    /**
     * @see gov.va.isaac.interfaces.gui.views.EnhancedConceptViewI#changeConcept(Stage, int)
     */
	@Override
	public Node changeConcept(Stage stage, int conceptNid,ViewType view) {
		try
    	{
	        ConceptChronicleBI concept = ExtendedAppContext.getDataStore().getConcept(conceptNid);
	        return changeConcept(stage, concept.getPrimordialUuid(), view);
    	} catch (IOException e) {
            String title = "Unexpected error loading concept with nid " + conceptNid;
            String msg = e.getClass().getName();
            LOG.error(title, e);
            AppContext.getCommonDialogs().showErrorDialog(title, msg, e.getMessage());
        	
            return getView();
    	}
	}

    /**
     * @see gov.va.isaac.interfaces.gui.views.EnhancedConceptViewI#changeConcept(Stage, int)
     */
	@Override
	public Node changeViewType(Stage stage, int conceptNid, ViewType view) {
		try
    	{
	        ConceptChronicleBI concept = ExtendedAppContext.getDataStore().getConcept(conceptNid);
	        return changeViewType(stage, concept.getPrimordialUuid(), view);
    	} catch (IOException e) {
            String title = "Unexpected error loading concept with nid " + conceptNid;
            String msg = e.getClass().getName();
            LOG.error(title, e);
            AppContext.getCommonDialogs().showErrorDialog(title, msg, e.getMessage());
        	
            return getView();
    	}
	}
	
    /**
     * @see gov.va.isaac.interfaces.gui.views.EnhancedConceptViewI#changeConcept(Stage, UUID)
     */
	@Override
    public Node changeViewType(Stage stage, UUID conceptUUID, ViewType view) {
    	try
    	{
	        LOG.info("Loading concept with UUID " + conceptUUID);
	        ConceptChronicleDdo concept = ExtendedAppContext.getDataStore().getFxConcept(conceptUUID, WBUtility.getViewCoordinate(),
	                VersionPolicy.ACTIVE_VERSIONS, RefexPolicy.REFEX_MEMBERS, RelationshipPolicy.ORIGINATING_AND_DESTINATION_TAXONOMY_RELATIONSHIPS);
	        LOG.info("Finished loading concept with UUID " + conceptUUID);
	        if (view == ViewType.SIMPLE_VIEW) {
	        	basicController.setConcept(concept, view, ((ConceptViewStage)stage).getPreviousConceptStack());
	        } else if (view == ViewType.DETAIL_VIEW) {
	        	if (detailedController == null) {
	                URL detailedResource = this.getClass().getResource("DetailedView.fxml");
	                FXMLLoader detailedLoader = new FXMLLoader(detailedResource);
	                root = (Region) detailedLoader.load();
	                root.getStylesheets().add(EnhancedConceptView.class.getResource("SimpleView.css").toString());
	                this.detailedController = detailedLoader.getController();
	        	}
	        	detailedController.setConcept(concept, view, ((ConceptViewStage)stage).getPreviousConceptStack());
	        }
    	} catch (IOException | ContradictionException e) {
            String title = "Unexpected error loading concept with UUID " + conceptUUID;
            String msg = e.getClass().getName();
            LOG.error(title, e);
            AppContext.getCommonDialogs().showErrorDialog(title, msg, e.getMessage());
    	}

    	stage.setScene(new Scene(root));

    	return getView();
    }


	private void setConcept(ConceptChronicleDdo concept) {
        // Make sure in application thread.
        FxUtils.checkFxUserThread();
        Stack<Integer> stack = new Stack<Integer>();
        basicController.setConcept(concept, ViewType.SIMPLE_VIEW, stack);

        // Title will change after concept is set.
        ConceptViewStage st = getStage(stack);
        st.setTitle(basicController.getTitle());
        st.show();
        //doesn't come to the front unless you do this (on linux, at least)
        Platform.runLater(() -> {st.toFront();});
    }
    
	// Put this on right-click List 
    /**
     * @see gov.va.isaac.interfaces.gui.views.ConceptViewI#setConcept(UUID)
     */
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
                    //TODO this violates the interface spec.  setConcept is NOT supposed to show a popup.
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
	 * @see gov.va.isaac.interfaces.gui.views.EnhancedConceptView#getView()
	 */
	@Override
	public Region getView()
	{
		return root;
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.EnhancedConceptView#getMenuBarMenus()
	 */
	@Override
	public List<MenuItemI> getMenuBarMenus()
	{
		// TODO
		return new ArrayList<MenuItemI>();
	}
}
