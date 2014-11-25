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
package gov.va.isaac.gui.refexViews.dynamicRefexListView;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.gui.ConceptNode;
import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.gui.refexViews.dynamicRefexListView.referencedItemsView.DynamicReferencedItemsView;
import gov.va.isaac.gui.refexViews.refexEdit.ConfigureDynamicRefexIndexingView;
import gov.va.isaac.gui.refexViews.util.DynamicRefexDataColumnListCell;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.util.CommonMenus;
import gov.va.isaac.util.CommonMenusNIdProvider;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.WBUtility;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.ihtsdo.otf.query.lucene.LuceneDynamicRefexIndexer;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.RefexDynamic;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicUsageDescription;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.RefexDynamicUsageDescriptionBuilder;
import org.ihtsdo.otf.tcc.model.index.service.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link DynamicRefexListViewController}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */

public class DynamicRefexListViewController
{
	@FXML private ResourceBundle resources;
	@FXML private URL location;
	@FXML private ListView<SimpleDisplayConcept> refexList;
	@FXML private ChoiceBox<String> refexStyleFilter;
	@FXML private Label refexStyleLabel;
	@FXML private Label referencedComponentTypeLabel;
	@FXML private AnchorPane rootPane;
	@FXML private Button clearFilterButton;
	@FXML private TextField descriptionMatchesFilter;
	@FXML private Button viewUsage;
	@FXML private Label statusLabel;
	@FXML private Label selectedRefexDescriptionLabel;
	@FXML private ListView<RefexDynamicColumnInfo> extensionFields;
	@FXML private ToolBar executeOperationsToolbar;
	@FXML private Label selectedRefexNameLabel;
	@FXML private VBox conceptNodeFilterPlaceholder;
	@FXML private ProgressIndicator readingRefexProgress;
	@FXML private ProgressIndicator selectedRefexProgressIndicator;

	private enum PendingRead
	{
		IDLE, FILTER_UPDATE_PROGRESS, FULL_READ_IN_PROGRESS, DO_FILTER_READ, DO_FULL_READ
	};

	//TODO this needs some sort of hook to refresh the list when a new one is defined
	
	private ConceptNode conceptNode;
	private volatile boolean disableRead = true;
	private volatile PendingRead readStatusTracker = PendingRead.IDLE;
	private Object readStatusLock = new Object();
	private int currentlyRenderedRefexNid = 0;
	private ContextMenu refexDefinitionsContextMenu_;

	private HashSet<SimpleDisplayConcept> allRefexDefinitions;

	private final Logger log = LoggerFactory.getLogger(DynamicRefexListViewController.class);

	protected static DynamicRefexListViewController construct() throws IOException
	{
		// Load from FXML.
		URL resource = DynamicRefexListViewController.class.getResource("DynamicRefexListView.fxml");
		FXMLLoader loader = new FXMLLoader(resource);
		loader.load();
		return loader.getController();
	}

	@FXML
	void initialize()
	{
		assert refexList != null : "fx:id=\"refexList\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";
		assert refexStyleFilter != null : "fx:id=\"refexStyleFilter\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";
		assert refexStyleLabel != null : "fx:id=\"refexStyleLabel\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";
		assert rootPane != null : "fx:id=\"rootPane\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";
		assert clearFilterButton != null : "fx:id=\"clearFilterButton\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";
		assert descriptionMatchesFilter != null : "fx:id=\"descriptionMatchesFilter\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";
		assert viewUsage != null : "fx:id=\"viewUsage\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";
		assert statusLabel != null : "fx:id=\"statusLabel\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";
		assert selectedRefexDescriptionLabel != null : "fx:id=\"selectedRefexDescriptionLabel\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";
		assert extensionFields != null : "fx:id=\"extensionFields\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";
		assert executeOperationsToolbar != null : "fx:id=\"executeOperationsToolbar\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";
		assert selectedRefexNameLabel != null : "fx:id=\"selectedRefexNameLabel\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";
		assert conceptNodeFilterPlaceholder != null : "fx:id=\"conceptNodeFilterPlaceholder\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";
		assert readingRefexProgress != null : "fx:id=\"readingRefexProgress\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";

		refexStyleFilter.getItems().add("All");
		refexStyleFilter.getItems().add("Annotations");
		refexStyleFilter.getItems().add("Member Refset");
		refexStyleFilter.getSelectionModel().select(0);
		refexStyleFilter.valueProperty().addListener((change) -> {
			rebuildList(false);
		});

		descriptionMatchesFilter.textProperty().addListener((change) -> {
			rebuildList(false);
		});

		conceptNode = new ConceptNode(null, false);
		conceptNode.getConceptProperty().addListener((invalidation) -> {
			ConceptVersionBI cv = conceptNode.getConceptProperty().get();  //Need to do a get after each invalidation, otherwise, we won't get the next invalidation
			if (cv != null)
			{
				//see if it is a valid Dynamic Refex Assemblage
				try
				{
					RefexDynamicUsageDescription.read(cv.getNid());
				}
				catch (Exception e)
				{
					conceptNode.isValid().setInvalid("The specified concept is not constructed as a Dynamic Sememe Assemblage concept");
				}
			}
			rebuildList(false);
		});

		conceptNodeFilterPlaceholder.getChildren().add(conceptNode.getNode());

		statusLabel.setText("Reading Sememes");

		clearFilterButton.setOnAction((event) -> {
			disableRead = true;
			refexStyleFilter.getSelectionModel().select(0);
			descriptionMatchesFilter.setText("");
			conceptNode.set((ConceptVersionBI) null);
			disableRead = false;
			rebuildList(true);
		});

		refexList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		refexList.getSelectionModel().selectedItemProperty().addListener((change) -> {
			showRefexDetails(refexList.getSelectionModel().getSelectedItem());
		});
		
		refexDefinitionsContextMenu_ = new ContextMenu();
		refexDefinitionsContextMenu_.setAutoHide(true);
		
		MenuItem mi = new MenuItem("View Usage");
		mi.setOnAction((action) ->
		{
			SimpleDisplayConcept sdc = refexList.getSelectionModel().getSelectedItem();
			if (sdc != null)
			{
				DynamicReferencedItemsView driv = new DynamicReferencedItemsView(sdc);
				driv.showView(null);
			}
		});
		mi.setGraphic(Images.SEARCH.createImageView());
		refexDefinitionsContextMenu_.getItems().add(mi);
		
		mi = new MenuItem("Configure Sememe Indexing");
		mi.setOnAction((action) ->
		{
			SimpleDisplayConcept sdc = refexList.getSelectionModel().getSelectedItem();
			if (sdc != null)
			{
				new ConfigureDynamicRefexIndexingView(WBUtility.getConceptVersion(sdc.getNid())).showView(null);
			}
		});
		mi.setGraphic(Images.CONFIGURE.createImageView());
		refexDefinitionsContextMenu_.getItems().add(mi);
		
		CommonMenus.addCommonMenus(refexDefinitionsContextMenu_, new CommonMenusNIdProvider()
		{
			@Override
			public Collection<Integer> getNIds()
			{
				SimpleDisplayConcept sdc = refexList.getSelectionModel().getSelectedItem();
				return Arrays.asList(sdc == null ? new Integer[] {} : new Integer[] {sdc.getNid()});
			}
		});
		
		refexList.addEventHandler(MouseEvent.MOUSE_CLICKED, (mouseEvent) -> 
		{
			if (mouseEvent.getButton().equals(MouseButton.SECONDARY) && refexList.getSelectionModel().getSelectedItem() != null)
			{
				refexDefinitionsContextMenu_.show(refexList, mouseEvent.getScreenX(), mouseEvent.getScreenY());
			}
			if (mouseEvent.getButton().equals(MouseButton.PRIMARY))
			{
				if (refexDefinitionsContextMenu_.isShowing())
				{
					refexDefinitionsContextMenu_.hide();
				}
			}
		});
		
		viewUsage.setDisable(true);
		viewUsage.setOnAction((event) -> {
			DynamicReferencedItemsView driv = new DynamicReferencedItemsView(refexList.getSelectionModel().getSelectedItem());
			driv.showView(null);
		});
		extensionFields.setCellFactory(new Callback<ListView<RefexDynamicColumnInfo>, ListCell<RefexDynamicColumnInfo>>()
		{
			@Override
			public ListCell<RefexDynamicColumnInfo> call(ListView<RefexDynamicColumnInfo> param)
			{
				return new DynamicRefexDataColumnListCell();
			}
		});

		disableRead = false;
		rebuildList(true);
	}

	private void rebuildList(boolean fullRebuild)
	{
		if (disableRead)
		{
			log.debug("Skip rebuild");
			return;
		}

		synchronized (readStatusLock)
		{
			if (readStatusTracker != PendingRead.IDLE)
			{
				//already a read in progress.  Add this request to the list, return.
				if (fullRebuild && readStatusTracker != PendingRead.DO_FULL_READ)
				{
					readStatusTracker = PendingRead.DO_FULL_READ;
				}
				else if (readStatusTracker != PendingRead.DO_FULL_READ && readStatusTracker != PendingRead.DO_FILTER_READ)
				{
					readStatusTracker = PendingRead.DO_FILTER_READ;
				}
				log.debug("Queued rebuild " + readStatusTracker);
				return;
			}
			else
			{
				readStatusTracker = (fullRebuild ? PendingRead.FULL_READ_IN_PROGRESS : PendingRead.FILTER_UPDATE_PROGRESS);
			}
		}

		statusLabel.setText("Reading Sememes");
		readingRefexProgress.setVisible(true);
		SimpleDisplayConcept selectedBefore = refexList.getSelectionModel().getSelectedItem();
		refexList.getSelectionModel().clearSelection();
		refexList.getItems().clear();

		Task<Void> t = new Task<Void>()
		{
			ArrayList<SimpleDisplayConcept> filteredList;

			@Override
			protected Void call() throws Exception
			{
				log.debug("Rebuild request running: " + readStatusTracker);
				if (fullRebuild)
				{
					allRefexDefinitions = null;
				}

				if (allRefexDefinitions == null)
				{
					allRefexDefinitions = new HashSet<>();
					
					LuceneDynamicRefexIndexer indexer = AppContext.getService(LuceneDynamicRefexIndexer.class);
					List<SearchResult> refexes = indexer.queryAssemblageUsage(RefexDynamic.REFEX_DYNAMIC_DEFINITION_DESCRIPTION.getNid(), 1000, Long.MAX_VALUE);
					for (SearchResult sr : refexes)
					{
						RefexDynamicChronicleBI<?> rc = (RefexDynamicChronicleBI<?>) ExtendedAppContext.getDataStore().getComponent(sr.getNid());
						//These are nested refex references - it returns a description component - concept we want is the parent of that.
						allRefexDefinitions.add(new SimpleDisplayConcept(
								ExtendedAppContext.getDataStore().getComponent(rc.getReferencedComponentNid()).getEnclosingConcept(),
								null));
					}
				}
				
				//This code for adding the concept from the concept filter panel can be removed, if we fix the above code to actually
				//find all dynamic refexes in the system.
				boolean conceptFromOutsideTheList = true;
				SimpleDisplayConcept enteredConcept = null;
				if (conceptNode.getConcept() != null && conceptNode.isValid().get())
				{
					enteredConcept = new SimpleDisplayConcept(conceptNode.getConcept());
				}

				filteredList = new ArrayList<>();
				for (SimpleDisplayConcept sdc : allRefexDefinitions)
				{
					if (enteredConcept != null && sdc.getNid() == enteredConcept.getNid())
					{
						conceptFromOutsideTheList = false;
					}
					if (passesFilters(sdc))
					{
						filteredList.add(sdc);
					}
				}
				
				if (enteredConcept != null && conceptFromOutsideTheList)
				{
					filteredList.add(enteredConcept);
				}
				
				Collections.sort(filteredList);
				
				return null;
			}

			/**
			 * @see javafx.concurrent.Task#succeeded()
			 */
			@Override
			protected void succeeded()
			{
				finished();
			}

			/**
			 * @see javafx.concurrent.Task#failed()
			 */
			@Override
			protected void failed()
			{
				log.error("Unexpected error building Sememe List", this.getException());
				AppContext.getCommonDialogs().showErrorDialog("Error reading Dynamic sememes", this.getException());
				finished();
			}

			private void finished()
			{
				log.debug("Sememe Definition refresh complete");
				refexList.getItems().addAll(filteredList);
				if (selectedBefore != null && refexList.getItems().contains(selectedBefore))
				{
					refexList.getSelectionModel().select(selectedBefore);
				}
				showRefexDetails(refexList.getSelectionModel().getSelectedItem());
				statusLabel.setText("Showing " + filteredList.size() + " of " + allRefexDefinitions.size() + " Sememes");
				readingRefexProgress.setVisible(false);
				synchronized (readStatusLock)
				{
					if (readStatusTracker == PendingRead.DO_FILTER_READ || readStatusTracker == PendingRead.DO_FULL_READ)
					{
						boolean rebuild = readStatusTracker == PendingRead.DO_FULL_READ ? true : false;
						readStatusTracker = PendingRead.IDLE;
						//Another request came in while we were running.  Run again.
						rebuildList(rebuild);
					}
					else
					{
						readStatusTracker = PendingRead.IDLE;
					}
				}
			}
		};

		Utility.execute(t);
	}

	private boolean passesFilters(SimpleDisplayConcept sdc) throws IOException
	{
		if (!conceptNode.isValid().get())
		{
			return false;
		}
		else if (conceptNode.getConcept() != null && conceptNode.getConcept().getConceptNid() != sdc.getNid())
		{
			return false;
		}
		else if (descriptionMatchesFilter.getText().length() > 0)
		{
			if (!sdc.getDescription().toLowerCase().contains(descriptionMatchesFilter.getText().toLowerCase()))
			{
				return false;
			}
		}
		else if (refexStyleFilter.getSelectionModel().getSelectedIndex() != 0)
		{
			ConceptVersionBI c = WBUtility.getConceptVersion(sdc.getNid());
			if ((c.isAnnotationStyleRefex() && refexStyleFilter.getSelectionModel().getSelectedIndex() == 2)
					|| (!c.isAnnotationStyleRefex() && refexStyleFilter.getSelectionModel().getSelectedIndex() == 1))
			{
				return false;
			}
		}
		return true;
	}

	private void showRefexDetails(SimpleDisplayConcept sdn)
	{
		if (sdn != null && sdn.getNid() == currentlyRenderedRefexNid)
		{
			return;
		}
		else
		{
			currentlyRenderedRefexNid = (sdn == null ? 0 : sdn.getNid());
		}
		selectedRefexNameLabel.setText("");
		selectedRefexDescriptionLabel.setText("");
		refexStyleLabel.setText("");
		referencedComponentTypeLabel.setText("");
		extensionFields.getItems().clear();
		
		if (sdn == null)
		{
			viewUsage.setDisable(true);
			return;
		}
		else
		{
			viewUsage.setDisable(false);
		}
		selectedRefexProgressIndicator.setVisible(true);
		selectedRefexNameLabel.setText(sdn.getDescription());

		Task<Void> t = new Task<Void>()
		{
			ArrayList<RefexDynamicColumnInfo> tempColumnInfo = new ArrayList<>();
			
			@Override
			protected Void call() throws Exception
			{
				RefexDynamicUsageDescription rdud = RefexDynamicUsageDescriptionBuilder.readRefexDynamicUsageDescriptionConcept(sdn.getNid());
				//fill in the header stuff
				Platform.runLater(() -> 
				{
					selectedRefexNameLabel.setText(rdud.getRefexName());
					selectedRefexDescriptionLabel.setText(rdud.getRefexUsageDescription());
					refexStyleLabel.setText(rdud.isAnnotationStyle() ? "Annotation" : "Embedded");
					referencedComponentTypeLabel.setText(rdud.getReferencedComponentTypeRestriction() == null ? "No restriction" : 
						"Must be " + rdud.getReferencedComponentTypeRestriction().toString());
				});
				
				//now fill in the data column details...
				
				for (RefexDynamicColumnInfo rdci : rdud.getColumnInfo())
				{
					//force the read on the column info - this may have to be read from the DB.
					rdci.getColumnName();
					tempColumnInfo.add(rdci);
				}
				
				return null;
			}

			/**
			 * @see javafx.concurrent.Task#succeeded()
			 */
			@Override
			protected void succeeded()
			{
				extensionFields.getItems().addAll(tempColumnInfo);
				extensionFields.scrollTo(0);
				finished();
			}

			/**
			 * @see javafx.concurrent.Task#failed()
			 */
			@Override
			protected void failed()
			{
				log.error("Unexpected error building selected sememe", this.getException());
				AppContext.getCommonDialogs().showErrorDialog("Error reading Dynamic Sememe", this.getException());
				finished();
			}

			private void finished()
			{
				selectedRefexProgressIndicator.setVisible(false);
			}
		};

		Utility.execute(t);
	}
	
	public Region getRoot()
	{
		return rootPane;
	}
}
