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
package gov.va.isaac.gui.dialog;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.gui.views.ConceptViewI;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.WBUtility;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
 * A {@link Stage} which can be used to show a concept detail view .
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * @author ocarlsen
 */
@Service
@PerLookup
public class ConceptView extends Stage implements ConceptViewI {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private final SnomedConceptViewController controller;

    private ConceptView() throws IOException {
        //This is for HK2 to construct...
        super();

        initOwner(null);
        initModality(Modality.NONE);
        initStyle(StageStyle.DECORATED);

        // Load from FXML.
        URL resource = this.getClass().getResource("SnomedConceptView.fxml");
        FXMLLoader loader = new FXMLLoader(resource);
        Parent root = (Parent) loader.load();
        setScene(new Scene(root));

        getIcons().add(Images.CONCEPT_VIEW.getImage());

        this.controller = loader.getController();
    }

    public void showConcept(ConceptChronicleDdo concept) {
        // Make sure in application thread.
        FxUtils.checkFxUserThread();
        controller.setConcept(concept);

        // Title will change after concept is set.
        this.setTitle(controller.getTitle());
        this.show();
    }
    
    public void showConcept(final UUID conceptUUID)
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
                    showConcept(result);
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
    @Override
    public void showConcept(int nid)
    {
        //TODO fix threading issues on this too...
        try
        {
            ConceptChronicleBI concept = ExtendedAppContext.getDataStore().getConcept(nid);
            if (concept != null)
            {
                showConcept(concept.getPrimordialUuid());
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
}
