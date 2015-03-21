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
package gov.va.isaac.gui.refexViews.refexEdit;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.config.profiles.UserProfile;
import gov.va.isaac.config.profiles.UserProfileBindings;
import gov.va.isaac.config.profiles.UserProfileManager;
import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.gui.dialog.YesNoDialog;
import gov.va.isaac.gui.refexViews.dynamicRefexListView.referencedItemsView.DynamicReferencedItemsView;
import gov.va.isaac.gui.refexViews.refexEdit.HeaderNode.Filter;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.gui.util.TableHeaderRowTooltipInstaller;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.RefexViewI;
import gov.va.isaac.interfaces.utility.DialogResponse;
import gov.va.isaac.util.OTFUtility;
import gov.va.isaac.util.UpdateableBooleanBinding;
import gov.va.isaac.util.Utility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Platform;
import javafx.beans.binding.FloatBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javax.inject.Named;
import org.apache.lucene.queryparser.classic.ParseException;
import org.glassfish.hk2.api.PerLookup;
import org.ihtsdo.otf.query.lucene.LuceneDynamicRefexIndexer;
import org.ihtsdo.otf.query.lucene.LuceneDynamicRefexIndexerConfiguration;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDynamicCAB;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicUsageDescription;
import org.ihtsdo.otf.tcc.model.index.service.IndexedGenerationCallable;
import org.ihtsdo.otf.tcc.model.index.service.SearchResult;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.javafx.collections.ObservableMapWrapper;
import com.sun.javafx.tk.Toolkit;


/**
 * 
 * Refset View
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */

@Service
@Named (value="DynamicRefexView")
@PerLookup
public class DynamicRefexView implements RefexViewI
{
	private VBox rootNode_ = null;
	private TreeTableView<RefexDynamicGUI> ttv_;
	private TreeItem<RefexDynamicGUI> treeRoot_;
	private Button retireButton_, addButton_, commitButton_, cancelButton_, editButton_, viewUsageButton_;
	private Label summary_ = new Label("");
	private ToggleButton stampButton_, activeOnlyButton_, historyButton_;
	private Button displayFSNButton_;
	private UpdateableBooleanBinding currentRowSelected_, selectedRowIsActive_;
	private UpdateableBooleanBinding showStampColumns_, showActiveOnly_, showFullHistory_, showViewUsageButton_;
	private TreeTableColumn<RefexDynamicGUI, String> stampColumn_;
	private BooleanProperty hasUncommitted_ = new SimpleBooleanProperty(false);

	private Text placeholderText = new Text("No Dynamic Sememes were found associated with the component");
	private Button backgroundSearchCancelButton_;
	private ProgressBar progressBar_;
	private RefexAnnotationSearcher processor_;
	
	private Button clearColumnHeaderNodesButton_ = new Button("Clear Filters");
	
	private Logger logger_ = LoggerFactory.getLogger(this.getClass());

	private InputType setFromType_ = null;
	private Integer newComponentHint_ = null;  //Useful when viewing from the assemblage perspective, and they add a new component - we can't find it without an index.
	IndexedGenerationCallable newComponentIndexGen_ = null; //Useful when adding from the assemblage perspective - if they are using an index, we need to wait till the new thing is indexed
	private DialogResponse dr_ = null;
	private final Object dialogThreadBlock_ = new Object();
	private AtomicInteger noRefresh_ = new AtomicInteger(0);
	
	@SuppressWarnings("unused")
	private UpdateableBooleanBinding refreshRequiredListenerHack;

	private final ObservableMap<ColumnId, Filter<?>> filterCache_ = new ObservableMapWrapper<>(new WeakHashMap<>());
	
	private final MapChangeListener<ColumnId, Filter<?>> filterCacheListener_ = new MapChangeListener<ColumnId, Filter<?>>() {
		@Override
		public void onChanged(
				javafx.collections.MapChangeListener.Change<? extends ColumnId, ? extends Filter<?>> c) {
			if (c.wasAdded() || c.wasRemoved()) {
				refresh();
			}
		}
	};
	private final ListChangeListener<Object> filterListener_ = new ListChangeListener<Object>() {
		@Override
		public void onChanged(
				javafx.collections.ListChangeListener.Change<? extends Object> c) {
			while (c.next()) {
				if (c.wasPermutated()) {
					// irrelevant
				} else if (c.wasUpdated()) {
					// irrelevant
				} else {
					refresh();
					break;
				}
			}
		}
	};
	private void addFilterCacheListeners() {
		removeFilterCacheListeners();
		filterCache_.addListener(filterCacheListener_);
		for (HeaderNode.Filter<?> filter : filterCache_.values()) {
			filter.getFilterValues().addListener(filterListener_);
		}
	}
	private void removeFilterCacheListeners() {
		filterCache_.removeListener(filterCacheListener_);
		for (HeaderNode.Filter<?> filter : filterCache_.values()) {
			filter.getFilterValues().removeListener(filterListener_);
		}
	}

	private DynamicRefexView() 
	{
		//Created by HK2 - no op - delay till getView called
	}
	
	private void initialInit()
	{
		if (rootNode_ == null)
		{
			noRefresh_.getAndIncrement();
			ttv_ = new TreeTableView<>();
			ttv_.setTableMenuButtonVisible(true);
			ttv_.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
	
			treeRoot_ = new TreeItem<>();
			treeRoot_.setExpanded(true);
			ttv_.setShowRoot(false);
			ttv_.setRoot(treeRoot_);
			progressBar_ = new ProgressBar(-1);
			progressBar_.setPrefWidth(200);
			progressBar_.setPadding(new Insets(15, 15, 15, 15));
			ttv_.setPlaceholder(progressBar_);
			
			rootNode_ = new VBox();
			rootNode_.setFillWidth(true);
			rootNode_.getChildren().add(ttv_);
			VBox.setVgrow(ttv_, Priority.ALWAYS);
			
			ToolBar t = new ToolBar();
			
			clearColumnHeaderNodesButton_.setOnAction(event -> {
				removeFilterCacheListeners();
				for (HeaderNode.Filter<?> filter : filterCache_.values()) {
					filter.getFilterValues().clear();
				}
				refresh();
			});
			t.getItems().add(clearColumnHeaderNodesButton_);
			
			currentRowSelected_ = new UpdateableBooleanBinding()
			{
				{
					addBinding(ttv_.getSelectionModel().getSelectedItems());
				}
				@Override
				protected boolean computeValue()
				{
					if (ttv_.getSelectionModel().getSelectedItems().size() > 0 && 
							ttv_.getSelectionModel().getSelectedItem() != null && 
							ttv_.getSelectionModel().getSelectedItem().getValue() != null)
					{
						return ttv_.getSelectionModel().getSelectedItem().getValue().isCurrent();
					}
					else
					{
						return false;
					}
				}
			};
			
			selectedRowIsActive_ = new UpdateableBooleanBinding()
			{
				{
					addBinding(ttv_.getSelectionModel().getSelectedItems());
				}
				@Override
				protected boolean computeValue()
				{
					if (ttv_.getSelectionModel().getSelectedItems().size() > 0 && ttv_.getSelectionModel().getSelectedItem() != null 
							&& ttv_.getSelectionModel().getSelectedItem().getValue() != null)
					{
						try
						{
							return ttv_.getSelectionModel().getSelectedItem().getValue().getRefex().isActive();
						}
						catch (IOException e)
						{
							logger_.error("Unexpected error!", e);
							return false;
						}
					}
					else
					{
						return false;
					}
				}
			};
			
			retireButton_ = new Button(null, Images.MINUS.createImageView());
			retireButton_.setTooltip(new Tooltip("Retire Selected Sememe Extension(s)"));
			retireButton_.disableProperty().bind(selectedRowIsActive_.and(currentRowSelected_).not());
			retireButton_.setOnAction((action) ->
			{
				try
				{
					YesNoDialog dialog = new YesNoDialog(rootNode_.getScene().getWindow());
					DialogResponse dr = dialog.showYesNoDialog("Retire?", "Do you want to retire the selected sememe entries?");
					if (DialogResponse.YES == dr)
					{
						ObservableList<TreeItem<RefexDynamicGUI>> selected = ttv_.getSelectionModel().getSelectedItems();
						if (selected != null && selected.size() > 0)
						{
							for (TreeItem<RefexDynamicGUI> refexTreeItem : selected)
							{
								RefexDynamicGUI refex = refexTreeItem.getValue();
								if (!refex.getRefex().isActive())
								{
									continue;
								}
								RefexDynamicCAB rcab =  refex.getRefex().makeBlueprint(OTFUtility.getViewCoordinate(), IdDirective.PRESERVE, RefexDirective.INCLUDE);
								rcab.setStatus(Status.INACTIVE);
								OTFUtility.getBuilder().construct(rcab);
								
								ConceptVersionBI assemblage = OTFUtility.getConceptVersion(refex.getRefex().getAssemblageNid());
								ExtendedAppContext.getDataStore().addUncommitted(ExtendedAppContext.getDataStore().getConceptForNid(refex.getRefex().getReferencedComponentNid()));
								if (!assemblage.isAnnotationStyleRefex())
								{
									ExtendedAppContext.getDataStore().addUncommitted(assemblage);
								}
							}
							ExtendedAppContext.getDataStore().waitTillWritesFinished();
							refresh();
						}
					}
				}
				catch (Exception e)
				{
					logger_.error("Unexpected error retiring sememe", e);
					AppContext.getCommonDialogs().showErrorDialog("Error", "There was an unexpected error retiring the sememe", e.getMessage(), rootNode_.getScene().getWindow());
				}
			});
			
			t.getItems().add(retireButton_);
			
			addButton_ = new Button(null, Images.PLUS.createImageView());
			addButton_.setTooltip(new Tooltip("Add a new Sememe Extension"));
			addButton_.setOnAction((action) ->
			{
				AddRefexPopup arp = AppContext.getService(AddRefexPopup.class);
				arp.finishInit(setFromType_, this);
				arp.showView(rootNode_.getScene().getWindow());
			});
			
			addButton_.setDisable(true);
			t.getItems().add(addButton_);
			
			editButton_ = new Button(null, Images.EDIT.createImageView());
			editButton_.setTooltip(new Tooltip("Edit a Sememe"));
			editButton_.disableProperty().bind(currentRowSelected_.not());
			editButton_.setOnAction((action) ->
			{
				AddRefexPopup arp = AppContext.getService(AddRefexPopup.class);
				arp.finishInit(ttv_.getSelectionModel().getSelectedItem().getValue(), this);
				arp.showView(rootNode_.getScene().getWindow());
			});
			t.getItems().add(editButton_);
			
			viewUsageButton_ = new Button(null, Images.SEARCH.createImageView());
			viewUsageButton_.setTooltip(new Tooltip("The displayed concept also defines a sememe dynamic itself.  Click to see the usage of this sememe."));
			viewUsageButton_.setOnAction((action) ->
			{
				try
				{
					ConceptChronicleBI c;
					if (setFromType_.getComponentBI() instanceof ComponentChronicleBI)
					{
						c = ((ComponentChronicleBI<?>) setFromType_.getComponentBI()).getEnclosingConcept();
					}
					else if (setFromType_.getComponentBI() instanceof ConceptChronicleBI)
					{
						c = ((ConceptChronicleBI) setFromType_.getComponentBI());
					}
					else
					{
						throw new Exception("Unexpected case");
					}
					SimpleDisplayConcept sdc = new SimpleDisplayConcept(c, null);
					DynamicReferencedItemsView driv = new DynamicReferencedItemsView(sdc);
					driv.showView(null);
				}
				catch (Exception e)
				{
					logger_.error("Error launching sememe dynamic member viewer", e);
					AppContext.getCommonDialogs().showErrorDialog("Error", "There was an unexpected launching the sememe member viewer", e.getMessage(), 
							(rootNode_.getScene() == null ? null : rootNode_.getScene().getWindow()));
				}
			});
			showViewUsageButton_ = new UpdateableBooleanBinding()
			{
				{
					setComputeOnInvalidate(true);
				}
				@Override
				protected boolean computeValue()
				{
					boolean show = false;
					if (setFromType_ != null && setFromType_.getComponentNid() != null)
					{
						//Need to find out if this component has a the dynamic refex definition annotation on it.
						try
						{
							RefexDynamicUsageDescription.read(setFromType_.getComponentNid());
							show = true;
						}
						catch (Exception e)
						{
							//noop - this concept simply isn't configured as a dynamic refex concept.
						}
					}
					return show;
				}
			};
			viewUsageButton_.visibleProperty().bind(showViewUsageButton_);
			t.getItems().add(viewUsageButton_);
			
			t.getItems().add(summary_);
			
			//fill to right
			Region r = new Region();
			HBox.setHgrow(r, Priority.ALWAYS);
			t.getItems().add(r);
			
			stampButton_ = new ToggleButton("");
			stampButton_.setGraphic(Images.STAMP.createImageView());
			stampButton_.setTooltip(new Tooltip("Show / Hide Stamp Attributes"));
			stampButton_.setVisible(false);
			stampButton_.setSelected(true);
			t.getItems().add(stampButton_);
			
			showStampColumns_ = new UpdateableBooleanBinding()
			{
				{
					setComputeOnInvalidate(true);
				}
				@Override
				protected boolean computeValue()
				{
					boolean visible = false;
					if (listeningTo.size() > 0)
					{
						visible = ((ReadOnlyBooleanProperty)listeningTo.iterator().next()).get();
					}
					if (stampColumn_ != null)
					{
						stampColumn_.setVisible(visible);
						for (TreeTableColumn<RefexDynamicGUI, ?> nested : stampColumn_.getColumns())
						{
							nested.setVisible(visible);
						}
					}
					return visible;
				}
			};
			
			activeOnlyButton_ = new ToggleButton("");
			activeOnlyButton_.setGraphic(Images.FILTER_16.createImageView());
			activeOnlyButton_.setTooltip(new Tooltip("Show Active Only / Show All"));
			activeOnlyButton_.setVisible(false);
			activeOnlyButton_.setSelected(true);
			t.getItems().add(activeOnlyButton_);
			
			showActiveOnly_ = new UpdateableBooleanBinding()
			{
				{
					setComputeOnInvalidate(true);
				}
				@Override
				protected boolean computeValue()
				{
					boolean showActive = false;
					if (listeningTo.size() > 0)
					{
						showActive = ((ReadOnlyBooleanProperty)listeningTo.iterator().next()).get();
					}
					refresh();
					return showActive;
				}
			};
			
			historyButton_ = new ToggleButton("");
			historyButton_.setGraphic(Images.HISTORY.createImageView());
			historyButton_.setTooltip(new Tooltip("Show Current Only / Show Full History"));
			historyButton_.setVisible(false);
			t.getItems().add(historyButton_);
			
			showFullHistory_ = new UpdateableBooleanBinding()
			{
				{
					setComputeOnInvalidate(true);
				}
				@Override
				protected boolean computeValue()
				{
					boolean showFullHistory = false;
					if (listeningTo.size() > 0)
					{
						showFullHistory = ((ReadOnlyBooleanProperty)listeningTo.iterator().next()).get();
					}
					refresh();
					return showFullHistory;
				}
			};
			
			displayFSNButton_ = new Button("");
			ImageView displayFsn = Images.DISPLAY_FSN.createImageView();
			Tooltip.install(displayFsn, new Tooltip("Displaying the Fully Specified Name - click to display the Preferred Term"));
			displayFsn.visibleProperty().bind(AppContext.getService(UserProfileBindings.class).getDisplayFSN());
			ImageView displayPreferred = Images.DISPLAY_PREFERRED.createImageView();
			displayPreferred.visibleProperty().bind(AppContext.getService(UserProfileBindings.class).getDisplayFSN().not());
			Tooltip.install(displayPreferred, new Tooltip("Displaying the Preferred Term - click to display the Fully Specified Name"));
			displayFSNButton_.setGraphic(new StackPane(displayFsn, displayPreferred));
			displayFSNButton_.prefHeightProperty().bind(historyButton_.heightProperty());
			displayFSNButton_.prefWidthProperty().bind(historyButton_.widthProperty());
			displayFSNButton_.setOnAction(new EventHandler<ActionEvent>()
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
						logger_.error("Unexpected error storing pref change", e);
					}
				}
			});
			t.getItems().add(displayFSNButton_);
			
			cancelButton_ = new Button("Cancel");
			cancelButton_.disableProperty().bind(hasUncommitted_.not());
			t.getItems().add(cancelButton_);
			cancelButton_.setOnAction((action) ->
			{
				try
				{
					ExtendedAppContext.getDataStore().cancel();
					//TODO (artf231427) Cancel isn't working properly on annotation-style refexes - works on member style.
					HashSet<Integer> componentNids = getAllComponentNids(treeRoot_.getChildren());
					for (Integer i : componentNids)
					{
						ConceptChronicleBI cc = ExtendedAppContext.getDataStore().getConceptForNid(i);
						if (cc.isUncommitted() || cc.getConceptAttributes().isUncommitted())
						{
							ExtendedAppContext.getDataStore().cancel(cc);
						}
					}
					
					HashSet<Integer> assemblageNids = getAllAssemblageNids(treeRoot_.getChildren());
					for (Integer i : assemblageNids)
					{
						ConceptChronicleBI cc = ExtendedAppContext.getDataStore().getConcept(i);
						if (!cc.isAnnotationStyleRefex() && cc.isUncommitted())
						{
							ExtendedAppContext.getDataStore().cancel(cc);
						}
					}
					ExtendedAppContext.getDataStore().waitTillWritesFinished();
				}
				catch (Exception e)
				{
					logger_.error("Error cancelling", e);
					AppContext.getCommonDialogs().showErrorDialog("Error", "There was an unexpected during cancel", e.getMessage(), 
							(rootNode_.getScene() == null ? null : rootNode_.getScene().getWindow()));
				}
				refresh();
			});
			
			commitButton_ = new Button("Commit");
			commitButton_.disableProperty().bind(hasUncommitted_.not());
			t.getItems().add(commitButton_);
			
			commitButton_.setOnAction((action) ->
			{
				try
				{
					HashSet<Integer> componentNids = getAllComponentNids(treeRoot_.getChildren());
					for (Integer i : componentNids)
					{
						ConceptChronicleBI cc = ExtendedAppContext.getDataStore().getConceptForNid(i);
						if (cc.isUncommitted() || cc.getConceptAttributes().isUncommitted())
						{
							ExtendedAppContext.getDataStore().commit(cc);
						}
					}
					
					HashSet<Integer> assemblageNids = getAllAssemblageNids(treeRoot_.getChildren());
					for (Integer i : assemblageNids)
					{
						ConceptChronicleBI cc = ExtendedAppContext.getDataStore().getConcept(i);
						if (!cc.isAnnotationStyleRefex() && cc.isUncommitted())
						{
							ExtendedAppContext.getDataStore().commit(cc);
						}
					}
					ExtendedAppContext.getDataStore().waitTillWritesFinished();
				}
				catch (Exception e)
				{
					logger_.error("Error committing", e);
					AppContext.getCommonDialogs().showErrorDialog("Error", "There was an unexpected during commit", e.getMessage(), 
							(rootNode_.getScene() == null ? null : rootNode_.getScene().getWindow()));
				}
				refresh();
			});
			
			backgroundSearchCancelButton_ = new Button("Cancel Scan");
			backgroundSearchCancelButton_.setOnAction((action) ->
			{
				RefexAnnotationSearcher processor = processor_;
				if (processor != null)
				{
					processor.requestStop();
				}
			});
			
			rootNode_.getChildren().add(t);
			
			Platform.runLater(new Runnable()
			{
				@Override
				public void run()
				{
					//TODO hack for bug in javafx - need to start as true, then later toggle to false.
					stampButton_.setSelected(false);
				}
			});
			
			refreshRequiredListenerHack = new UpdateableBooleanBinding()
			{
				private volatile AtomicBoolean refreshQueued = new AtomicBoolean(false);
				{
					setComputeOnInvalidate(true);
					addBinding(AppContext.getService(UserProfileBindings.class).getViewCoordinatePath(), AppContext.getService(UserProfileBindings.class).getDisplayFSN());
				}

				@Override
				protected boolean computeValue()
				{
					synchronized (refreshQueued)
					{
						if (refreshQueued.get())
						{
							logger_.info("Skip DynRefex refresh() due to pending refresh");
							return false;
						}
						else
						{
							refreshQueued.set(true);
							logger_.debug("DynRefex refresh() due to change of an observed user property");
							Utility.schedule(() -> 
							{
								Platform.runLater(() -> 
								{
									synchronized (refreshQueued)
									{
										refreshQueued.set(false);
									}
									try
									{
										refresh();
									}
									catch (Exception e)
									{
										logger_.error("Unexpected error running refresh", e);
									}
								});
							}, 10, TimeUnit.MILLISECONDS);
						}
					}
					return false;
				}
			};
			noRefresh_.decrementAndGet();
		}
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.DockedViewI#getView()
	 */
	@Override
	public Region getView()
	{
		//setting up the binding stuff is causing refresh calls
		initialInit();
		return rootNode_;
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.RefexViewI#setComponent(int, javafx.beans.property.ReadOnlyBooleanProperty, 
	 * javafx.beans.property.ReadOnlyBooleanProperty, javafx.beans.property.ReadOnlyBooleanProperty, boolean)
	 */
	@Override
	public void setComponent(int componentNid, ReadOnlyBooleanProperty showStampColumns, ReadOnlyBooleanProperty showActiveOnly, 
			ReadOnlyBooleanProperty showFullHistory, boolean displayFSNButton)
	{
		//disable refresh, as the bindings mucking causes many refresh calls
		noRefresh_.getAndIncrement();
		initialInit();
		setFromType_ = new InputType(componentNid, false);
		handleExternalBindings(showStampColumns, showActiveOnly, showFullHistory, displayFSNButton);
		showViewUsageButton_.invalidate();
		newComponentHint_ = null;
		noRefresh_.getAndDecrement();
		initColumnsLoadData();
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.RefexViewI#setAssemblage(int, javafx.beans.property.ReadOnlyBooleanProperty, 
	 * javafx.beans.property.ReadOnlyBooleanProperty, javafx.beans.property.ReadOnlyBooleanProperty, boolean)
	 */
	@Override
	public void setAssemblage(int assemblageConceptNid, ReadOnlyBooleanProperty showStampColumns, ReadOnlyBooleanProperty showActiveOnly, 
			ReadOnlyBooleanProperty showFullHistory, boolean displayFSNButton)
	{
		//disable refresh, as the bindings mucking causes many refresh calls
		noRefresh_.getAndIncrement();
		initialInit();
		setFromType_ = new InputType(assemblageConceptNid, true);
		handleExternalBindings(showStampColumns, showActiveOnly, showFullHistory, displayFSNButton);
		newComponentHint_ = null;
		noRefresh_.getAndDecrement();
		initColumnsLoadData();
	}
	
	private void handleExternalBindings(ReadOnlyBooleanProperty showStampColumns, ReadOnlyBooleanProperty showActiveOnly, 
			ReadOnlyBooleanProperty showFullHistory, boolean displayFSNButton)
	{
		showStampColumns_.clearBindings();
		showActiveOnly_.clearBindings();
		showFullHistory_.clearBindings();
		if (showStampColumns == null)
		{
			//Use our own button
			stampButton_.setVisible(true);
			showStampColumns_.addBinding(stampButton_.selectedProperty());
		}
		else
		{
			stampButton_.setVisible(false);
			showStampColumns_.addBinding(showStampColumns);
		}
		if (showActiveOnly == null)
		{
			//Use our own button
			activeOnlyButton_.setVisible(true);
			showActiveOnly_.addBinding(activeOnlyButton_.selectedProperty());
		}
		else
		{
			activeOnlyButton_.setVisible(false);
			showActiveOnly_.addBinding(showActiveOnly);
		}
		if (showFullHistory == null)
		{
			//Use our own button
			historyButton_.setVisible(true);
			showFullHistory_.addBinding(historyButton_.selectedProperty());
		}
		else
		{
			historyButton_.setVisible(false);
			showFullHistory_.addBinding(showFullHistory);
		}
		
		if (displayFSNButton)
		{
			//Use our own button
			displayFSNButton_.setVisible(true);
		}
		else
		{
			displayFSNButton_.setVisible(false);
		}
	}
	
	protected void setNewComponentHint(int componentNid, IndexedGenerationCallable indexGen)
	{
		newComponentHint_ = componentNid;
		newComponentIndexGen_ = indexGen;
	}
	
	protected void refresh()
	{
		if (noRefresh_.get() > 0)
		{
			logger_.info("Skip refresh of dynamic refex due to wait count {}" + noRefresh_.get());
			return;
		}

		Utility.execute(() ->
		{
			try
			{
				loadRealData();
			}
			catch (Exception e)
			{
				logger_.error("Unexpected error building the sememe display", e);
				//null check, as the error may happen before the scene is visible
				AppContext.getCommonDialogs().showErrorDialog("Error", "There was an unexpected error building the sememe display", e.getMessage(), 
						(rootNode_.getScene() == null ? null : rootNode_.getScene().getWindow()));
			}
		});
	}
	
	private void storeTooltip(Map<String, List<String>> store, String key, String value)
	{
		List<String> list = store.get(key);
		if (list == null)
		{
			list = new ArrayList<>();
			store.put(key, list);
		}
		list.add(value);
	}

	private void initColumnsLoadData()
	{
		noRefresh_.getAndIncrement();
		Utility.execute(() -> {
			try
			{
				removeFilterCacheListeners();
				
				final ArrayList<TreeTableColumn<RefexDynamicGUI, ?>> treeColumns = new ArrayList<>();
				Map<String, List<String>> toolTipStore = new HashMap<>();
				
				TreeTableColumn<RefexDynamicGUI, RefexDynamicGUI> ttStatusCol = new TreeTableColumn<>(DynamicRefexColumnType.STATUS_CONDENSED.toString());
				storeTooltip(toolTipStore, ttStatusCol.getText(), "Status Markers - for active / inactive and current / historical and uncommitted");

				ttStatusCol.setSortable(true);
				ttStatusCol.setResizable(true);
				ttStatusCol.setComparator(new Comparator<RefexDynamicGUI>()
				{
					@Override
					public int compare(RefexDynamicGUI o1, RefexDynamicGUI o2)
					{
						return o1.compareTo(DynamicRefexColumnType.STATUS_CONDENSED, null, o2);
					}
				});
				ttStatusCol.setCellFactory((colInfo) -> 
				{
					return new StatusCell();
				});
				ttStatusCol.setCellValueFactory((callback) ->
				{
					return new ReadOnlyObjectWrapper<RefexDynamicGUI>(callback.getValue().getValue());
				});
				treeColumns.add(ttStatusCol);
				
				//Create columns for basic info
				if (setFromType_.getComponentNid() == null)
				{
					//If the component is null, the assemblage is always the same - don't show.
					TreeTableColumn<RefexDynamicGUI, RefexDynamicGUI>  ttCol = buildComponentCellColumn(DynamicRefexColumnType.COMPONENT);
					storeTooltip(toolTipStore, ttCol.getText(), "The Referenced component of this Sememe");
					HeaderNode<String> headerNode = new HeaderNode<>(
							filterCache_,
							ttCol,
							ColumnId.getInstance(DynamicRefexColumnType.COMPONENT),
							rootNode_.getScene(),
							new HeaderNode.DataProvider<String>() {
						@Override
						public String getData(RefexDynamicGUI source) {
							return source.getDisplayStrings(DynamicRefexColumnType.COMPONENT, null).getKey();
						}
					});
					ttCol.setGraphic(headerNode.getNode());
					
					treeColumns.add(ttCol);
				}
				else
				{
					//if the assemblage is null, the component is always the same - don't show.
					TreeTableColumn<RefexDynamicGUI, RefexDynamicGUI>  ttCol = buildComponentCellColumn(DynamicRefexColumnType.ASSEMBLAGE);
					storeTooltip(toolTipStore, ttCol.getText(), "The Assemblage concept that defines this Sememe");
					HeaderNode<String> headerNode = new HeaderNode<>(
							filterCache_,
							ttCol,
							ColumnId.getInstance(DynamicRefexColumnType.ASSEMBLAGE),
							rootNode_.getScene(),
							new HeaderNode.DataProvider<String>() {
						@Override
						public String getData(RefexDynamicGUI source) {
							return source.getDisplayStrings(DynamicRefexColumnType.ASSEMBLAGE, null).getKey();
						}
					});
					ttCol.setGraphic(headerNode.getNode());

					treeColumns.add(ttCol);
				}
				
				TreeTableColumn<RefexDynamicGUI, String> ttStringCol = new TreeTableColumn<>();
				ttStringCol = new TreeTableColumn<>();
				ttStringCol.setText(DynamicRefexColumnType.ATTACHED_DATA.toString());
				storeTooltip(toolTipStore, ttStringCol.getText(), "The various data fields attached to this Sememe instance");
				ttStringCol.setSortable(false);
				ttStringCol.setResizable(true);
				//don't add yet - we might not need this column.  throw away later, if we don't need it
				
				/**
				 * The key of the first hashtable is the column description concept, while the key of the second hashtable is the assemblage concept
				 * Since the same column could be used in multiple assemblages - need to keep those separate, even though the rest of the column details 
				 * will be the same.  The List in the third level is for cases where a single assemblage concept re-uses the same column description 
				 * details multiple times.
				*/
				Hashtable<UUID, Hashtable<UUID, List<RefexDynamicColumnInfo>>> uniqueColumns;
				
				if (setFromType_.getComponentNid() != null)
				{
					if (setFromType_.getComponentBI().isUncommitted())
					{
						hasUncommitted_.set(true);
					}
					else
					{
						hasUncommitted_.set(false);
					}
					uniqueColumns = getUniqueColumns(setFromType_.getComponentBI());
				}
				else
				{
					if (ExtendedAppContext.getDataStore().getConcept(setFromType_.getAssemblyNid()).isUncommitted())
					{
						hasUncommitted_.set(true);
					}
					else
					{
						hasUncommitted_.set(false);
					}
					
					//This case is easy - as there is only one assemblage.  The 3 level mapping stuff is way overkill for this case... but don't
					//want to rework it at this point... might come back and cleanup this mess later.
					uniqueColumns = new Hashtable<>();
					
					RefexDynamicUsageDescription rdud = RefexDynamicUsageDescription.read(setFromType_.getAssemblyNid());
					for (RefexDynamicColumnInfo col : rdud.getColumnInfo())
					{
						Hashtable<UUID, List<RefexDynamicColumnInfo>> nested = uniqueColumns.get(col.getColumnDescriptionConcept());
						if (nested == null)
						{
							nested = new Hashtable<>();
							uniqueColumns.put(col.getColumnDescriptionConcept(), nested);
						}
						
						UUID assemblyUUID = ExtendedAppContext.getDataStore().getUuidPrimordialForNid(rdud.getRefexUsageDescriptorNid());
						List<RefexDynamicColumnInfo> doubleNested = nested.get(assemblyUUID);
						if (doubleNested == null)
						{
							doubleNested = new ArrayList<>();
							nested.put(assemblyUUID, doubleNested);
						}
						doubleNested.add(col);
					}
				}
				
				ArrayList<Hashtable<UUID, List<RefexDynamicColumnInfo>>> sortedUniqueColumns = new ArrayList<>();
				sortedUniqueColumns.addAll(uniqueColumns.values());
				Collections.sort(sortedUniqueColumns, new Comparator<Hashtable<UUID, List<RefexDynamicColumnInfo>>>()
					{
						@Override
						public int compare(Hashtable<UUID, List<RefexDynamicColumnInfo>> o1, Hashtable<UUID, List<RefexDynamicColumnInfo>> o2)
						{
							return Integer.compare(o1.values().iterator().next().get(0).getColumnOrder(),o2.values().iterator().next().get(0).getColumnOrder()); 
						}
					});
				
				//Create columns for every different type of data column we see in use
				for (Hashtable<UUID, List<RefexDynamicColumnInfo>> col : sortedUniqueColumns)
				{
					int max = 0;
					for (List<RefexDynamicColumnInfo> item : col.values())
					{
						if (item.size() > max)
						{
							max = item.size();
						}
					}
					
					for (int i = 0; i < max; i++)
					{
						final String name = col.values().iterator().next().get(0).getColumnName(); //all the same, just pick the first
						TreeTableColumn<RefexDynamicGUI, RefexDynamicGUI> nestedCol = new TreeTableColumn<>(name);
						storeTooltip(toolTipStore, nestedCol.getText(), col.values().iterator().next().get(0).getColumnDescription());
						
						// FILTER ID
						final ColumnId columnKey = ColumnId.getInstance(col.values().iterator().next().get(0).getColumnDescriptionConcept(), i);

						final Integer listItem = i;
						HeaderNode<String> ttNestedColHeaderNode = new HeaderNode<>(
								filterCache_,
								nestedCol,
								columnKey,
								rootNode_.getScene(),
								new HeaderNode.DataProvider<String>() {
							@Override
							public String getData(RefexDynamicGUI source) {
								if (source == null) {
									return null;
								}
								try
								{
									for (UUID uuid : col.keySet())
									{
										assert source != null;
										assert source.getRefex() != null;
										
										if (ExtendedAppContext.getDataStore().getNidForUuids(uuid) == source.getRefex().getAssemblageNid())
										{
											List<RefexDynamicColumnInfo> colInfo =  col.get(uuid);
											Integer refexColumnOrder = (colInfo.size() > listItem ? 
													(source.getRefex().getData().length <= colInfo.get(listItem).getColumnOrder() ? null 
														: colInfo.get(listItem).getColumnOrder()): null);
											
											if (refexColumnOrder != null)
											{
												return source.getDisplayStrings(DynamicRefexColumnType.ATTACHED_DATA, refexColumnOrder).getKey();
											}
										}
									}
								}
								catch (Exception e)
								{
									logger_.error("Unexpected error getting string data from attribute", e);
								}
								return null;  //not applicable / blank row
							}
						});
						nestedCol.setGraphic(ttNestedColHeaderNode.getNode());
						
						nestedCol.setSortable(true);
						nestedCol.setResizable(true);
						nestedCol.setComparator(new Comparator<RefexDynamicGUI>()
						{
							@Override
							public int compare(RefexDynamicGUI o1, RefexDynamicGUI o2)
							{
								try
								{
									for (UUID uuid : col.keySet())
									{
										assert o1 != null;
										assert o1.getRefex() != null;
										
										if (ExtendedAppContext.getDataStore().getNidForUuids(uuid) == o1.getRefex().getAssemblageNid())
										{
											List<RefexDynamicColumnInfo> colInfo =  col.get(uuid);
											Integer refexColumnOrder = (colInfo.size() > listItem ? 
													(o1.getRefex().getData().length <= colInfo.get(listItem).getColumnOrder() ? null 
														: colInfo.get(listItem).getColumnOrder()): null);
											
											if (refexColumnOrder != null)
											{
												return o1.compareTo(DynamicRefexColumnType.ATTACHED_DATA, refexColumnOrder, o2);
											}
										}
									}
								}
								catch (Exception e)
								{
									logger_.error("Unexpected error sorting data attributes", e);
								}
								return 1;  //not applicable / blank row
								
							}
						});
						
						nestedCol.setCellFactory(new AttachedDataCellFactory(col, i));
						
						nestedCol.setCellValueFactory((callback) ->
						{
							return new ReadOnlyObjectWrapper<>(callback.getValue().getValue());
						}); 
						
						ttStringCol.getColumns().add(nestedCol);
					}
				}
				
				//Only add attached data column if necessary
				if (ttStringCol.getColumns().size() > 0)
				{
					treeColumns.add(ttStringCol);
				}
				
				//Create the STAMP columns
				ttStringCol = new TreeTableColumn<>();
				ttStringCol.setText("STAMP");
				storeTooltip(toolTipStore, "STAMP", "The Status, Time, Author, Module and Path columns");
				ttStringCol.setSortable(false);
				ttStringCol.setResizable(true);
				stampColumn_ = ttStringCol;
				treeColumns.add(ttStringCol);
				
				TreeTableColumn<RefexDynamicGUI, String> nestedCol = new TreeTableColumn<>();
				nestedCol.setText(DynamicRefexColumnType.STATUS_STRING.toString());
				storeTooltip(toolTipStore, nestedCol.getText(), "The status of the instance");
				HeaderNode<String> nestedColHeaderNode = new HeaderNode<>(
						filterCache_,
						nestedCol,
						ColumnId.getInstance(DynamicRefexColumnType.STATUS_STRING),
						rootNode_.getScene(),
						new HeaderNode.DataProvider<String>() {
							@Override
							public String getData(RefexDynamicGUI source) {
								return source.getDisplayStrings(DynamicRefexColumnType.STATUS_STRING, null).getKey();
							}
						});
				nestedCol.setGraphic(nestedColHeaderNode.getNode());
				nestedCol.setSortable(true);
				nestedCol.setResizable(true);
				nestedCol.setCellValueFactory((callback) ->
				{
					return new ReadOnlyStringWrapper(callback.getValue().getValue().getDisplayStrings(DynamicRefexColumnType.STATUS_STRING, null).getKey());
				});
				ttStringCol.getColumns().add(nestedCol);
				

				nestedCol = new TreeTableColumn<>();
				nestedCol.setText(DynamicRefexColumnType.TIME.toString());
				storeTooltip(toolTipStore, nestedCol.getText(), "The time when the instance was created or edited");
				nestedColHeaderNode = new HeaderNode<>(
						filterCache_,
						nestedCol,
						ColumnId.getInstance(DynamicRefexColumnType.TIME),
						rootNode_.getScene(),
						new HeaderNode.DataProvider<String>() {
							@Override
							public String getData(RefexDynamicGUI source) {
								return source.getDisplayStrings(DynamicRefexColumnType.TIME, null).getKey();
							}
						});
				nestedCol.setGraphic(nestedColHeaderNode.getNode());
				nestedCol.setSortable(true);
				nestedCol.setResizable(true);
				nestedCol.setCellValueFactory((callback) ->
				{
					return new ReadOnlyStringWrapper(callback.getValue().getValue().getDisplayStrings(DynamicRefexColumnType.TIME, null).getKey());
				});
				ttStringCol.getColumns().add(nestedCol);
				
				TreeTableColumn<RefexDynamicGUI, RefexDynamicGUI> nestedIntCol = buildComponentCellColumn(DynamicRefexColumnType.AUTHOR); 
				storeTooltip(toolTipStore, nestedIntCol.getText(), "The author of the instance");
				ttStringCol.getColumns().add(nestedIntCol);
				
				nestedIntCol = buildComponentCellColumn(DynamicRefexColumnType.MODULE);
				storeTooltip(toolTipStore, nestedIntCol.getText(), "The module of the instance");
				ttStringCol.getColumns().add(nestedIntCol);
				
				nestedIntCol = buildComponentCellColumn(DynamicRefexColumnType.PATH); 
				storeTooltip(toolTipStore, nestedIntCol.getText(), "The path of the instance");
				ttStringCol.getColumns().add(nestedIntCol);

				Platform.runLater(() ->
				{
					for (TreeTableColumn<RefexDynamicGUI, ?> tc : treeColumns)
					{
						ttv_.getColumns().add(tc);
					}
					
					//Horrible hack to set a reasonable default size on the columns.
					//Min width to the width of the header column.
					Font f = new Font("System Bold", 13.0);
					for (final TreeTableColumn<RefexDynamicGUI, ?> col : ttv_.getColumns())
					{
						for (TreeTableColumn<RefexDynamicGUI, ?> nCol : col.getColumns())
						{
							String text = (nCol.getGraphic() != null 
									&& (nCol.getGraphic() instanceof Label || nCol.getGraphic() instanceof HBox) 
										? (nCol.getGraphic() instanceof Label ? ((Label)nCol.getGraphic()).getText() 
												: ((Label)((HBox)nCol.getGraphic()).getChildren().get(0)).getText()) 
										: nCol.getText());
							nCol.setMinWidth(Toolkit.getToolkit().getFontLoader().computeStringWidth(text, f) + 70);
						}
						
						if (col.getColumns().size() > 0)
						{
							FloatBinding binding = new FloatBinding()
							{
								{
									for (TreeTableColumn<RefexDynamicGUI, ?> nCol : col.getColumns())
									{
										bind(nCol.widthProperty());
										bind(nCol.visibleProperty());
									}
								}
								@Override
								protected float computeValue()
								{
									float temp = 0;
									for (TreeTableColumn<RefexDynamicGUI, ?> nCol : col.getColumns())
									{
										if (nCol.isVisible())
										{
											temp += nCol.getWidth();
										}
									}
									float parentColWidth = Toolkit.getToolkit().getFontLoader().computeStringWidth(col.getText(), f) + 70;
									if (temp < parentColWidth)
									{
										//bump the size of the first nested column, so the parent doesn't get clipped
										col.getColumns().get(0).setMinWidth(parentColWidth);
									}
									return temp;
								}
							};
							col.minWidthProperty().bind(binding);
						}
						else
						{
							String text = col.getText();
							
							if (text == null) {
								text = (col.getGraphic() != null && (col.getGraphic() instanceof Label || col.getGraphic() instanceof HBox)
										? (col.getGraphic() instanceof Label ? ((Label)col.getGraphic()).getText() : ((Label)((HBox)col.getGraphic()).getChildren().get(0)).getText()) 
										: col.getText());
							}
							
							if (text.equalsIgnoreCase(DynamicRefexColumnType.ASSEMBLAGE.toString()) 
									|| text.equalsIgnoreCase(DynamicRefexColumnType.COMPONENT.toString()))
							{
								col.setPrefWidth(250);
							}
							if (text.equalsIgnoreCase("s"))
							{
								col.setPrefWidth(32);
								col.setMinWidth(32);
							}
							else
							{
								col.setMinWidth(Toolkit.getToolkit().getFontLoader().computeStringWidth(text, f) + 70);
							}
						}
					}
					
					showStampColumns_.invalidate();
				});
				
				TableHeaderRowTooltipInstaller.installTooltips(ttv_, toolTipStore);

				loadRealData();
			}
			catch (Exception e)
			{
				logger_.error("Unexpected error building the sememe display", e);
				//null check, as the error may happen before the scene is visible
				AppContext.getCommonDialogs().showErrorDialog("Error", "There was an unexpected error building the sememe display", e.getMessage(), 
						(rootNode_.getScene() == null ? null : rootNode_.getScene().getWindow()));
			}
			finally
			{
				noRefresh_.getAndDecrement();
			}
		});
	}
	
	private synchronized void loadRealData() throws IOException, ContradictionException, NumberFormatException, InterruptedException, ParseException
	{
		//Now add the data
		ArrayList<TreeItem<RefexDynamicGUI>> rowData;
		if (setFromType_.getComponentNid() != null)
		{
			rowData = getDataRows(setFromType_.getComponentBI(), null);
		}
		else
		{
			rowData = getDataRows(setFromType_.getAssemblyNid());
		}
		
		logger_.info("Found {} rows of data in a dynamic refex", rowData.size());
		
		Utility.execute(() ->
		{
			checkForUncommittedRefexes(rowData);
		});
		
		Platform.runLater(() ->
		{
			treeRoot_.getChildren().clear();
			addButton_.setDisable(false);
			treeRoot_.getChildren().addAll(rowData);
			summary_.setText(rowData.size() + " entries");
			ttv_.setPlaceholder(placeholderText);
			ttv_.getSelectionModel().clearSelection();
		});
		
		// ADD LISTENERS TO headerNode.getUserFilters() TO EXECUTE REFRESH WHENEVER FILTER SETS CHANGE
		addFilterCacheListeners();
	}
	
	private TreeTableColumn<RefexDynamicGUI, RefexDynamicGUI> buildComponentCellColumn(DynamicRefexColumnType type)
	{
		TreeTableColumn<RefexDynamicGUI, RefexDynamicGUI> ttCol = new TreeTableColumn<>(type.toString());
		HeaderNode<String> headerNode = new HeaderNode<>(
				filterCache_,
				ttCol,
				ColumnId.getInstance(type),
				rootNode_.getScene(),
				new HeaderNode.DataProvider<String>() {
					@Override
					public String getData(RefexDynamicGUI source) {
						return source.getDisplayStrings(type, null).getKey();
					}
				});
		ttCol.setGraphic(headerNode.getNode());
		
		ttCol.setSortable(true);
		ttCol.setResizable(true);
		ttCol.setComparator(new Comparator<RefexDynamicGUI>()
		{
			@Override
			public int compare(RefexDynamicGUI o1, RefexDynamicGUI o2)
			{
				return o1.compareTo(type, null, o2);
			}
		});
		ttCol.setCellFactory((colInfo) -> {return new ComponentDataCell(type);});
		ttCol.setCellValueFactory((callback) -> {return new ReadOnlyObjectWrapper<RefexDynamicGUI>(callback.getValue().getValue());});
		return ttCol;
	}

	
	private ArrayList<TreeItem<RefexDynamicGUI>> getDataRows(ComponentChronicleBI<?> component, TreeItem<RefexDynamicGUI> nestUnder) 
			throws IOException, ContradictionException
	{
		if (component instanceof ConceptChronicleBI)
		{
			component = ((ConceptChronicleBI) component).getConceptAttributes();
		}
		
		if (component == null)
		{
			return (nestUnder == null ? new ArrayList<>() : null);
		}

		ArrayList<TreeItem<RefexDynamicGUI>> rowData = createFilteredRowData(component.getRefexesDynamic());
		
		if (nestUnder != null)
		{
			nestUnder.getChildren().addAll(rowData);
			return null;
		}
		else
		{
			return rowData;
		}
	}
	
	private ArrayList<TreeItem<RefexDynamicGUI>> createFilteredRowData(Collection<? extends RefexDynamicChronicleBI<?>> refexChronicles) throws IOException, 
		ContradictionException
	{
		ArrayList<TreeItem<RefexDynamicGUI>> rowData = new ArrayList<>();
		ArrayList<RefexDynamicVersionBI<?>> allVersions = new ArrayList<>();
		
		//Since the WB API is horribly undocumented, we have no idea what the behavior of these methods are.  Are chonicles ordered?
		//Are they guaranteed to remain in order?  What order?  Same question with versions.  Is the order maintained?  What order is it?
		for (RefexDynamicChronicleBI<?> refexChronicle: refexChronicles)
		{
			for (RefexDynamicVersionBI<?> refexVersion : refexChronicle.getVersions())
			{
				allVersions.add(refexVersion);
			}
		}
		
		//Sort the newest to the top.
		Collections.sort(allVersions, new Comparator<RefexDynamicVersionBI<?>>()
		{
			@Override
			public int compare(RefexDynamicVersionBI<?> o1, RefexDynamicVersionBI<?> o2)
			{
				if (o1.getPrimordialUuid().equals(o2.getPrimordialUuid()))
				{
					return o2.getStamp() - o1.getStamp();
				}
				else
				{
					return o1.getPrimordialUuid().compareTo(o2.getPrimordialUuid());
				}
			}
		});
		
		UUID lastSeenRefex = null;
		
		for (RefexDynamicVersionBI<?> r : allVersions)
		{
			if (!showFullHistory_.get() && r.getPrimordialUuid().equals(lastSeenRefex))
			{
				continue;
			}
			if (showActiveOnly_.get() == false || r.isActive())
			{
				RefexDynamicGUI newRefexDynamicGUI = new RefexDynamicGUI(r, !r.getPrimordialUuid().equals(lastSeenRefex));  //first one we see with a new UUID is current, others are historical
				
				// HeaderNode FILTERING DONE HERE
				boolean filterOut = false;
				for (HeaderNode.Filter<?> filter : filterCache_.values()) {
					if (filter.getFilterValues().size() > 0) {
						if (! filter.accept(newRefexDynamicGUI)) {
							filterOut = true;
							break;
						}
					}
				}
				
				if (! filterOut) {
					TreeItem<RefexDynamicGUI> ti = new TreeItem<>();

					ti.setValue(newRefexDynamicGUI);
					//recurse
					getDataRows(r, ti);  
					rowData.add(ti);
				}
			}
			lastSeenRefex = r.getPrimordialUuid();
		}

		return rowData;
	}

	/**
	 * The key of the first hashtable is the column description concept, while the key of the second hashtable is the assemblage concept
	 * Since the same column could be used in multiple assemblages - need to keep those separate, even though the rest of the column details 
	 * will be the same.  The List in the third level is for cases where a single assemblage concept re-uses the same column description 
	 * details multiple times.
	 */
	private Hashtable<UUID, Hashtable<UUID, List<RefexDynamicColumnInfo>>> getUniqueColumns(ComponentChronicleBI<?> component) throws IOException, ContradictionException
	{
		Hashtable<UUID, Hashtable<UUID, List<RefexDynamicColumnInfo>>> columns = new Hashtable<>();

		if (component instanceof ConceptChronicleBI)
		{
			component = ((ConceptChronicleBI) component).getConceptAttributes();
		}
		
		if (component == null)
		{
			return columns;
		}

		for (RefexDynamicChronicleBI<?> refex : component.getRefexesDynamic())
		{
			boolean assemblageWasNull = false;
			for (RefexDynamicColumnInfo column : refex.getRefexDynamicUsageDescription().getColumnInfo())
			{
				Hashtable<UUID, List<RefexDynamicColumnInfo>> inner = columns.get(column.getColumnDescriptionConcept());
				if (inner == null)
				{
					inner = new Hashtable<>();
					columns.put(column.getColumnDescriptionConcept(), inner);
				}
				List<RefexDynamicColumnInfo> innerValues = inner.get(column.getAssemblageConcept());
				if (innerValues == null)
				{
					assemblageWasNull = true;
					innerValues = new ArrayList<>();
					inner.put(column.getAssemblageConcept(), innerValues);
				}
				if (assemblageWasNull)  //We only want to populate this on the first pass - the columns on an assemblage will never change from one pass to another.
				{
					innerValues.add(column);
				}
			}
			
			for (RefexDynamicChronicleBI<?> refexNested: refex.getRefexesDynamic())
			{
				//recurse
				Hashtable<UUID, Hashtable<UUID, List<RefexDynamicColumnInfo>>> nested = getUniqueColumns(refexNested);
				for (Entry<UUID, Hashtable<UUID, List<RefexDynamicColumnInfo>>> nestedItem : nested.entrySet())
				{
					if (columns.get(nestedItem.getKey()) == null)
					{
						columns.put(nestedItem.getKey(), nestedItem.getValue());
					}
					else
					{
						Hashtable<UUID, List<RefexDynamicColumnInfo>> mergeInto = columns.get(nestedItem.getKey());
						for (Entry<UUID, List<RefexDynamicColumnInfo>> toMergeItem : nestedItem.getValue().entrySet())
						{
							if (mergeInto.get(toMergeItem.getKey()) == null)
							{
								mergeInto.put(toMergeItem.getKey(), toMergeItem.getValue());
							}
							else
							{
								//don't care - we already have this assemblage concept - the column values will be the same as what we already have.
							}
						}
					}
				}
			}
		}
		return columns;
	}
	
	private void checkForUncommittedRefexes(List<TreeItem<RefexDynamicGUI>> items)
	{
		if (hasUncommitted_.get())
		{
			return;
		}
		if (items == null)
		{
			return;
		}
		for (TreeItem<RefexDynamicGUI> item : items)
		{
			if (item.getValue() != null && item.getValue().getRefex().isUncommitted())
			{
				//TODO add some indication that this is either running / finished
				Platform.runLater(() ->
				{
					hasUncommitted_.set(true);
				});
				return;
			}
			checkForUncommittedRefexes(item.getChildren());
		}
	}
	
	private HashSet<Integer> getAllAssemblageNids(List<TreeItem<RefexDynamicGUI>> items)
	{
		HashSet<Integer> results = new HashSet<Integer>();
		if (items == null)
		{
			return results;
		}
		for (TreeItem<RefexDynamicGUI> item : items)
		{
			if (item.getValue() != null)
			{
				RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>> refex = item.getValue().getRefex();
				results.add(refex.getAssemblageNid());
			}
			results.addAll(getAllAssemblageNids(item.getChildren()));
		}
		return results;
	}
	
	private HashSet<Integer> getAllComponentNids(List<TreeItem<RefexDynamicGUI>> items)
	{
		HashSet<Integer> results = new HashSet<Integer>();
		if (items == null)
		{
			return results;
		}
		for (TreeItem<RefexDynamicGUI> item : items)
		{
			if (item.getValue() != null)
			{
				RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>> refex = item.getValue().getRefex();
				results.add(refex.getReferencedComponentNid());
			}
			results.addAll(getAllComponentNids(item.getChildren()));
		}
		return results;
	}
	

	@SuppressWarnings("unchecked")
	private ArrayList<TreeItem<RefexDynamicGUI>> getDataRows(int assemblageNid) 
			throws IOException, ContradictionException, InterruptedException, NumberFormatException, ParseException
	{
		Collection<RefexDynamicChronicleBI<?>> refexMembers;
		
		dr_ = null;
		
		ConceptVersionBI assemblageConceptFull = OTFUtility.getConceptVersion(assemblageNid);
		if (!assemblageConceptFull.isAnnotationStyleRefex())
		{
			refexMembers = (Collection<RefexDynamicChronicleBI<?>>)assemblageConceptFull.getRefsetDynamicMembers();
		}
		else
		{
			refexMembers = new ArrayList<>();
			
			if (LuceneDynamicRefexIndexerConfiguration.isAssemblageIndexed(assemblageConceptFull.getNid()))
			{
				Platform.runLater(() ->
				{
					progressBar_.setProgress(-1);
					ttv_.setPlaceholder(progressBar_);
				});
				
				LuceneDynamicRefexIndexer ldri = AppContext.getService(LuceneDynamicRefexIndexer.class);
				
				long waitForLatch = Long.MAX_VALUE;
				if (newComponentIndexGen_ != null)
				{
					try
					{
						logger_.debug("waiting for index latch");
						waitForLatch = newComponentIndexGen_.call();
					}
					catch (Exception e)
					{
						logger_.error("Unexpected error getting latch");
					}
					//We never need to wait for this again.
					newComponentIndexGen_ = null;
				}
				
				List<SearchResult> results = ldri.queryAssemblageUsage(assemblageConceptFull.getNid(), Integer.MAX_VALUE, waitForLatch);
				for (SearchResult sr : results)
				{
					refexMembers.add((RefexDynamicChronicleBI<?>)ExtendedAppContext.getDataStore().getComponent(sr.getNid()));
				}
			}
			else
			{
				Platform.runLater(() ->
				{
					synchronized (dialogThreadBlock_)
					{
						YesNoDialog dialog = new YesNoDialog(rootNode_.getScene().getWindow());
						dr_ = dialog.showYesNoDialog("Scan for Annotation Sememe entries?", "This is an annotation style Sememe with no index."
								+ "  Without a supporting index, displaying the sememe entries will take a long time.  Scan for entries?");
						if (dr_ == DialogResponse.NO)
						{
							placeholderText.setText("No index is available to fetch the entries");
						}
						else if (dr_ == DialogResponse.YES)
						{
							VBox temp = new VBox();
							temp.setAlignment(Pos.CENTER);
							temp.getChildren().add(progressBar_);
							progressBar_.setProgress(-1);
							temp.getChildren().add(backgroundSearchCancelButton_);
							ttv_.setPlaceholder(temp);
						}
						dialogThreadBlock_.notifyAll();
					}
				});

				while (dr_ == null)
				{
					//wait until they click yes, no, or close the dialog
					//sync block will block us until they answer
					synchronized (dialogThreadBlock_)
					{
						if (dr_ != null)
						{
							break;
						}
					}
					if (dr_ == null)
					{
						//Means that our thread got here before the other thread started and acquired the
						//sync lock.  sleep, try to acquire the lock again - at which point, we will block 
						//until the dialog is closed.
						Thread.sleep(50);
					}
				}
				
				if (dr_ == DialogResponse.YES)
				{
					processor_ = new RefexAnnotationSearcher((refex) -> 
					{
						if (refex.getAssemblageNid() == assemblageConceptFull.getConceptNid())
						{
							return true;
						}
						return false;
					}, progressBar_);

					try
					{
						ExtendedAppContext.getDataStore().iterateConceptDataInParallel(processor_);
					}
					catch (Exception e)
					{
						logger_.error("Unexpected error during background processing", e);
						AppContext.getCommonDialogs().showErrorDialog("Error", "There was an unexpected error scanning the database", e.getMessage(), 
								(rootNode_.getScene() == null ? null : rootNode_.getScene().getWindow()));
					}
					
					refexMembers.addAll(processor_.getResults());
				}
				else
				{
					//add in the newComponentHint - because they said no to the scan, and we have no index
					if (newComponentHint_ != null)
					{
						ConceptChronicleBI c = ExtendedAppContext.getDataStore().getConcept(newComponentHint_);
						if (c != null)
						{
							Collection<RefexDynamicChronicleBI<?>> dynamicAnnotations = (Collection<RefexDynamicChronicleBI<?>>)c.getRefexDynamicAnnotations();
							
							for (RefexDynamicChronicleBI<?> r : dynamicAnnotations)
							{
								if (r.getAssemblageNid() == assemblageNid)
								{
									refexMembers.add(r);
								}
							}
						}
					}
				}
			}
		}
		
		ArrayList<TreeItem<RefexDynamicGUI>> rowData = createFilteredRowData(refexMembers);

		if (rowData.size() == 0)
		{
			placeholderText.setText("No Dynamic Sememes were found using this Assemblage");
		}
		return rowData;
	}
}
