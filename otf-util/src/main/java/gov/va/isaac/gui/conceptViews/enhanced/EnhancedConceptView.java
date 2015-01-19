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
 *	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.gui.conceptViews.enhanced;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.gui.constants.ConceptViewMode;
import gov.va.isaac.interfaces.gui.constants.SharedServiceNames;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.PopupConceptViewI;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.OTFUtility;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javax.inject.Named;
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
@Service @Named(value=SharedServiceNames.MODERN_STYLE)
@PerLookup
public class EnhancedConceptView implements PopupConceptViewI {
	private final Logger LOG = LoggerFactory.getLogger(this.getClass());
	
	private Stage s;
	private EnhancedConceptViewController controller = null;

	private ObservableList<Integer> conceptHistoryStack = FXCollections.observableArrayList();
	
	private static int currentConNid;
	private static UUID currentConUuid;
	private static ConceptChronicleDdo currentCon;
	
	private EnhancedConceptView() throws IOException {
		//This is for HK2 to construct...
		super();

		// Load from FXML.
		URL resource = this.getClass().getResource("EnhancedView.fxml");
		FXMLLoader loader = new FXMLLoader(resource);
		Region root = loader.load();
		root.getStylesheets().add(EnhancedConceptView.class.getResource("EnhancedView.css").toString());
		controller = loader.getController();
		controller.setConceptView(this);
	}

	private void setConcept(ConceptChronicleDdo concept) {
		// Make sure in application thread.
		FxUtils.checkFxUserThread();
		controller.setConcept(concept.getPrimordialUuid(), controller.getViewMode(), conceptHistoryStack);
	}

	@Override
	public void showView(Window parent) {
		s = new Stage();
		s.initOwner(parent);
		s.initModality(Modality.NONE);
		s.initStyle(StageStyle.DECORATED);

		s.setScene(new Scene(getView()));
		s.sizeToScene(); 
		
		s.getScene().getStylesheets().add(EnhancedConceptView.class.getResource("/isaac-shared-styles.css").toString());
		s.getIcons().add(Images.CONCEPT_VIEW.getImage());

		// Title will change after concept is set.
		s.setTitle(controller.getTitle());
		s.show();
		

		//doesn't come to the front unless you do this (on linux, at least)
		Platform.runLater(() -> 
		{
			s.setHeight(s.getHeight() + 30);  //coming up slightly to small on linux for some reason
			s.toFront();
		});
	}

	@Override
	public void setConcept(UUID conceptUUID) {
		// TODO this needs to be rewritten so that the dialog displays immediately
		//but with a progress indicator while we wait for the concept to be found..
		
		Task<ConceptChronicleDdo> task = new Task<ConceptChronicleDdo>()
		{

			@Override
			protected ConceptChronicleDdo call() throws Exception
			{
				LOG.info("Loading concept with UUID " + conceptUUID);
				ConceptChronicleDdo concept = ExtendedAppContext.getDataStore().getFxConcept(conceptUUID, OTFUtility.getViewCoordinate(),
						VersionPolicy.ACTIVE_VERSIONS, RefexPolicy.REFEX_MEMBERS, RelationshipPolicy.ORIGINATING_AND_DESTINATION_TAXONOMY_RELATIONSHIPS);
				 LOG.info("Finished loading concept with UUID " + conceptUUID);

				return concept;
			}

			@Override
			protected void succeeded()
			{
				try
				{
					currentCon = this.getValue();
					setConcept(currentCon);
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

		Utility.execute(task);	}

	@Override
	public void setConcept(int conceptNid) {
		//TODO fix threading issues on this too...
		try
		{
			ConceptChronicleBI concept = ExtendedAppContext.getDataStore().getConcept(conceptNid);
			if (concept != null)
			{
				setConcept(concept.getPrimordialUuid());
			}
		}
		catch (IOException e)
		{
			String title = "Unexpected error loading concept with nid " + conceptNid;
			String msg = e.getClass().getName();
			LOG.error(title, e);
			AppContext.getCommonDialogs().showErrorDialog(title, msg, e.getMessage());
		}
	}

	@Override
	public UUID getConceptUuid() {
		return currentConUuid;
	}

	@Override
	public int getConceptNid() {
		return currentConNid;
	}

	@Override
	public void setViewMode(ConceptViewMode mode) {
		controller.setViewMode(mode);
	}
	
	@Override
	public ConceptViewMode getViewMode() {
		return controller.getViewMode();
	}

	@Override
	public Region getView() {
		return controller.getRootNode();
	}
}
