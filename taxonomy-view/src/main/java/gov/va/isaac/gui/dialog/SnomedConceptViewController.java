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
package gov.va.isaac.gui.dialog;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.config.generated.StatedInferredOptions;
import gov.va.isaac.config.profiles.UserProfile;
import gov.va.isaac.config.profiles.UserProfileBindings;
import gov.va.isaac.config.profiles.UserProfileManager;
import gov.va.isaac.config.profiles.UserProfileBindings.RelationshipDirection;
import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.gui.dragAndDrop.DragRegistry;
import gov.va.isaac.gui.dragAndDrop.SingleConceptIdProvider;
import gov.va.isaac.gui.treeview.SctTreeViewIsaacView;
import gov.va.isaac.gui.util.CopyableLabel;
import gov.va.isaac.gui.util.CustomClipboard;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.RefexViewI;
import gov.va.isaac.util.CommonlyUsedConcepts;
import gov.va.isaac.util.OTFUtility;
import java.util.UUID;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.attribute.ConceptAttributesChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.attribute.ConceptAttributesVersionDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.identifier.IdentifierDdo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller class for {@link ConceptView}.
 *
 * @author ocarlsen
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class SnomedConceptViewController {

	private static final Logger LOG = LoggerFactory.getLogger(SnomedConceptViewController.class);

	@FXML private AnchorPane anchorPane;
	@FXML private Label conceptDefinedLabel;
	@FXML private Label conceptStatusLabel;
	@FXML private VBox descriptionsTableHolder;
	@FXML private Label fsnLabel;
	@FXML private VBox idVBox;
	@FXML private VBox relationshipsTableHolder;
	@FXML private SplitPane splitPane;
	@FXML private VBox splitRight;
	@FXML private Label uuidLabel;
	@FXML private VBox annotationsRegion;
	@FXML private ToggleButton stampToggle;
	@FXML private ToggleButton historyToggle;
	@FXML private ToggleButton activeOnlyToggle;
	@FXML private Button descriptionTypeButton;
	@FXML private HBox sourceRelTitleHBox;

	private Button showInTreeButton;
	private ProgressIndicator treeViewProgress;
	private final BooleanProperty treeViewSearchRunning = new SimpleBooleanProperty(false);

	private SctTreeViewIsaacView sctTree;
	
	private UUID conceptUuid;
	private int conceptNid = 0;

	public Region getRootNode()
	{
		return anchorPane;
	}

	public void setConcept(ConceptChronicleDdo concept) {
		conceptUuid = concept.getPrimordialUuid();
		splitPane.setDividerPositions(0.7);

		// Update text of labels.
		ConceptAttributesChronicleDdo attributeChronicle = concept.getConceptAttributes();
		final ConceptAttributesVersionDdo conceptAttributes = attributeChronicle.getVersions().get(attributeChronicle.getVersions().size() - 1);
		conceptDefinedLabel.setText(conceptAttributes.isDefined() + "");
		conceptStatusLabel.setText(conceptAttributes.getStatus().name());
		fsnLabel.setText(OTFUtility.getDescription(concept));
		CopyableLabel.addCopyMenu(fsnLabel);
		
		MenuItem copyFull = new MenuItem("Copy Full Concept");
		copyFull.setGraphic(Images.COPY.createImageView());

		copyFull.setOnAction(e -> CustomClipboard.set(OTFUtility.getConceptVersion(concept.getPrimordialUuid()).toLongString()));

		fsnLabel.getContextMenu().getItems().add(copyFull);
		
		AppContext.getService(DragRegistry.class).setupDragOnly(fsnLabel, new SingleConceptIdProvider()
		{
			@Override
			public String getConceptId()
			{
				return uuidLabel.getText();
			}
		});
		uuidLabel.setText(concept.getPrimordialUuid().toString());
		AppContext.getService(DragRegistry.class).setupDragOnly(uuidLabel, new SingleConceptIdProvider()
		{
			@Override
			public String getConceptId()
			{
				return uuidLabel.getText();
			}
		});
		CopyableLabel.addCopyMenu(uuidLabel);
		
		stampToggle.setText("");
		stampToggle.setGraphic(Images.STAMP.createImageView());
		stampToggle.setTooltip(new Tooltip("Show Stamp columns when pressed, Hides Stamp columns when not pressed"));
		stampToggle.setSelected(true);

		activeOnlyToggle.setText("");
		activeOnlyToggle.setGraphic(Images.FILTER_16.createImageView());
		activeOnlyToggle.setTooltip(new Tooltip("Filter to only show active items when pressed, Show all items when not pressed"));
		activeOnlyToggle.setSelected(true);
		
		historyToggle.setText("");
		historyToggle.setGraphic(Images.HISTORY.createImageView());
		historyToggle.setTooltip(new Tooltip("Shows full history when pressed, Only shows current items when not pressed"));
		
		descriptionTypeButton.setText("");
		ImageView displayFsn = Images.DISPLAY_FSN.createImageView();
		Tooltip.install(displayFsn, new Tooltip("Displaying the Fully Specified Name - click to display the Preferred Term"));
		displayFsn.visibleProperty().bind(AppContext.getService(UserProfileBindings.class).getDisplayFSN());
		ImageView displayPreferred = Images.DISPLAY_PREFERRED.createImageView();
		displayPreferred.visibleProperty().bind(AppContext.getService(UserProfileBindings.class).getDisplayFSN().not());
		Tooltip.install(displayPreferred, new Tooltip("Displaying the Preferred Term - click to display the Fully Specified Name"));
		descriptionTypeButton.setGraphic(new StackPane(displayFsn, displayPreferred));
		descriptionTypeButton.prefHeightProperty().bind(historyToggle.heightProperty());
		descriptionTypeButton.prefWidthProperty().bind(historyToggle.widthProperty());
		descriptionTypeButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				try
				{
					UserProfile up = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
					up.setDisplayFSN(AppContext.getService(UserProfileBindings.class).getDisplayFSN().not().get());
					ExtendedAppContext.getService(UserProfileManager.class).saveChanges(up);
				}
				catch (Exception e)
				{
					LOG.error("Unexpected error storing pref change", e);
				}
			}
		});
		
		ConceptVersionBI conceptVersionBI = OTFUtility.getConceptVersion(concept.getPrimordialUuid());
		AppContext.getService(CommonlyUsedConcepts.class).addConcept(new SimpleDisplayConcept(conceptVersionBI));

		// Add context menu items for additional identifiers.
		for (final IdentifierDdo id : attributeChronicle.getAdditionalIds()) {

			MenuItem mi = new MenuItem("View Concept");
			mi.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent ignore) {
					AppContext.getCommonDialogs().showConceptDialog(id.getAuthorityRef().getUuid());
				}
			});

			CopyableLabel l = new CopyableLabel(id.getAuthorityRef().getText());
			l.getContextMenu().getItems().add(mi);
			l.getStyleClass().add("boldLabel");

			HBox hbox = new HBox();
			hbox.getChildren().add(l);
			hbox.getChildren().add(new CopyableLabel(id.getDenotation().toString()));
			hbox.setSpacing(5.0);

			idVBox.getChildren().add(hbox);
		}
		
		try
		{
			DescriptionTableView dtv = new DescriptionTableView(stampToggle.selectedProperty(), historyToggle.selectedProperty(), activeOnlyToggle.selectedProperty());
			dtv.setConcept(conceptVersionBI);
			descriptionsTableHolder.getChildren().add(dtv.getNode());
			VBox.setVgrow(dtv.getNode(), Priority.ALWAYS);
		}
		catch (Exception e)
		{
			LOG.error("Error configuring description view", e);
			descriptionsTableHolder.getChildren().add(new Label("Unexpected error configuring descriptions view"));
		}
		
		//rel table section
		try
		{
			Button taxonomyViewMode = new Button();
			taxonomyViewMode.setPadding(new Insets(2.0));
			ImageView taxonomyInferred = Images.TAXONOMY_INFERRED.createImageView();
			taxonomyInferred.visibleProperty().bind(AppContext.getService(UserProfileBindings.class).getStatedInferredPolicy().isEqualTo(StatedInferredOptions.INFERRED));
			Tooltip.install(taxonomyInferred, new Tooltip("Displaying the Inferred view- click to display the Inferred then Stated view"));
			ImageView taxonomyStated = Images.TAXONOMY_STATED.createImageView();
			taxonomyStated.visibleProperty().bind(AppContext.getService(UserProfileBindings.class).getStatedInferredPolicy().isEqualTo(StatedInferredOptions.STATED));
			Tooltip.install(taxonomyStated, new Tooltip("Displaying the Stated view- click to display the Inferred view"));
			ImageView taxonomyInferredThenStated = Images.TAXONOMY_INFERRED_THEN_STATED.createImageView();
			taxonomyInferredThenStated.visibleProperty().bind(AppContext.getService(UserProfileBindings.class).getStatedInferredPolicy().isEqualTo(StatedInferredOptions.INFERRED_THEN_STATED));
			Tooltip.install(taxonomyInferredThenStated, new Tooltip("Displaying the Inferred then Stated view- click to display the Stated view"));
			taxonomyViewMode.setGraphic(new StackPane(taxonomyInferred, taxonomyStated, taxonomyInferredThenStated));
			taxonomyViewMode.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent event)
				{
					try
					{
						UserProfile up = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
						StatedInferredOptions sip = null;
						if (AppContext.getService(UserProfileBindings.class).getStatedInferredPolicy().get() == StatedInferredOptions.STATED)
						{
							sip = StatedInferredOptions.INFERRED;
						}
						else if (AppContext.getService(UserProfileBindings.class).getStatedInferredPolicy().get() == StatedInferredOptions.INFERRED)
						{
							sip = StatedInferredOptions.INFERRED_THEN_STATED;
						}
						else if (AppContext.getService(UserProfileBindings.class).getStatedInferredPolicy().get() == StatedInferredOptions.INFERRED_THEN_STATED)
						{
							sip = StatedInferredOptions.STATED;
						}
						else
						{
							LOG.error("Unexpected error!");
							return;
						}
						up.setStatedInferredPolicy(sip);
						ExtendedAppContext.getService(UserProfileManager.class).saveChanges(up);
					}
					catch (Exception e)
					{
						LOG.error("Unexpected error storing pref change", e);
					}
				}
			});
			HBox.setMargin(taxonomyViewMode, new Insets(0, 0, 0, 5.0));
			sourceRelTitleHBox.getChildren().add(taxonomyViewMode);
			
			Button relationshipViewMode = new Button();
			relationshipViewMode.setPadding(new Insets(2.0));
			ImageView taxonomySource = Images.TAXONOMY_SOURCE.createImageView();
			taxonomySource.visibleProperty().bind(AppContext.getService(UserProfileBindings.class).getDisplayRelDirection().isEqualTo(RelationshipDirection.SOURCE));
			Tooltip.install(taxonomySource, new Tooltip("Displaying the Source Relationships only, click to display Target"));
			ImageView taxonomyTarget = Images.TAXONOMY_TARGET.createImageView();
			taxonomyTarget.visibleProperty().bind(AppContext.getService(UserProfileBindings.class).getDisplayRelDirection().isEqualTo(RelationshipDirection.TARGET));
			Tooltip.install(taxonomyTarget, new Tooltip("Displaying the Target Relationships only, click to display Source and Target"));
			ImageView taxonomySourceAndTarget = Images.TAXONOMY_SOURCE_AND_TARGET.createImageView();
			taxonomySourceAndTarget.visibleProperty().bind(AppContext.getService(UserProfileBindings.class).getDisplayRelDirection().isEqualTo(RelationshipDirection.SOURCE_AND_TARGET));
			Tooltip.install(taxonomySourceAndTarget, new Tooltip("Displaying the Source and Target Relationships, click to display Source only"));
			relationshipViewMode.setGraphic(new StackPane(taxonomySource, taxonomyTarget, taxonomySourceAndTarget));
			relationshipViewMode.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent event)
				{
					try
					{
						UserProfile up = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
						if (up.getDisplayRelDirection() == RelationshipDirection.SOURCE)
						{
							up.setDisplayRelDirection(RelationshipDirection.TARGET);
						}
						else if (up.getDisplayRelDirection() == RelationshipDirection.TARGET)
						{
							up.setDisplayRelDirection(RelationshipDirection.SOURCE_AND_TARGET);
						}
						else if (up.getDisplayRelDirection() == RelationshipDirection.SOURCE_AND_TARGET)
						{
							up.setDisplayRelDirection(RelationshipDirection.SOURCE);
						}
						else 
						{
							LOG.error("Unhandeled case!");
						}
						ExtendedAppContext.getService(UserProfileManager.class).saveChanges(up);
					}
					catch (Exception e)
					{
						LOG.error("Unexpected error storing pref change", e);
					}
				}
			});
			HBox.setMargin(relationshipViewMode, new Insets(0, 0, 0, 5.0));
			sourceRelTitleHBox.getChildren().add(relationshipViewMode);

			RelationshipTableView rtv = new RelationshipTableView(stampToggle.selectedProperty(), historyToggle.selectedProperty(), activeOnlyToggle.selectedProperty());
			rtv.setConcept(conceptVersionBI);
			relationshipsTableHolder.getChildren().add(rtv.getNode());
			VBox.setVgrow(rtv.getNode(), Priority.ALWAYS);
			
			Label summary = new Label();
			HBox.setMargin(summary, new Insets(0, 0, 0, 5.0));
			sourceRelTitleHBox.getChildren().add(summary);
			summary.textProperty().bind(rtv.getSummaryText());
			
		}
		catch (Exception e)
		{
			LOG.error("Error configuring relationship view", e);
			descriptionsTableHolder.getChildren().add(new Label("Unexpected error configuring descriptions view"));
		}
		
		RefexViewI v = AppContext.getService(RefexViewI.class, "DynamicRefexView");
		v.setComponent(conceptVersionBI.getNid(), stampToggle.selectedProperty(), activeOnlyToggle.selectedProperty(), historyToggle.selectedProperty(), false);
		v.getView().setMinHeight(100.0);
		VBox.setVgrow(v.getView(), Priority.ALWAYS);
		annotationsRegion.getChildren().add(v.getView());
		
		//TODO this is a hack - I want these off by default, but there is a bug in javafx that messes up the table
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				stampToggle.setSelected(false);
			}
		});

		// Load the inner tree view.
		try {
			sctTree = AppContext.getService(SctTreeViewIsaacView.class); 
			sctTree.init();
			
			showInTreeButton = new Button();
			showInTreeButton.setPadding(new Insets(2.0));
			showInTreeButton.setGraphic(Images.TAXONOMY_SEARCH_RESULT_ANCESTOR.createImageView());
			showInTreeButton.setTooltip(new Tooltip("Find Concept"));
			showInTreeButton.visibleProperty().bind(treeViewSearchRunning.not());
			showInTreeButton.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					treeViewSearchRunning.set(true);
					sctTree.locateConcept(conceptAttributes.getConcept().getPrimordialUuid(), treeViewSearchRunning);
				}
			});
			
			treeViewProgress = new ProgressIndicator(-1);
			treeViewProgress.setMaxSize(16, 16);
			treeViewProgress.visibleProperty().bind(treeViewSearchRunning);

			StackPane sp = new StackPane(showInTreeButton, treeViewProgress);
			sctTree.addToToolBar(sp);
			
			Region r = sctTree.getView();
			splitRight.getChildren().add(r);
			VBox.setVgrow(r, Priority.ALWAYS);
			treeViewSearchRunning.set(true);
			sctTree.locateConcept(concept.getPrimordialUuid(), treeViewSearchRunning);
		} catch (Exception ex) {
			LOG.error("Error creating tree view", ex);
			splitRight.getChildren().add(new Label("Unexpected error building tree"));
		}
	}

	public StringProperty getTitle() {
		return fsnLabel.textProperty();
	}

	public UUID getConceptUuid() {
		return conceptUuid;
	}

	public int getConceptNid() {
		if (conceptNid == 0) {
			conceptNid = OTFUtility.getConceptVersion(conceptUuid).getNid();
		}
		
		return conceptNid;
	}

	/**
	 * See  {@link SctTreeViewIsaacView#cancelOperations()}
	 */
	public void stopOperations()
	{
		sctTree.cancelOperations();
	}
}
