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
import gov.va.isaac.gui.dialog.YesNoDialog;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.gui.views.RefexViewI;
import gov.va.isaac.interfaces.utility.DialogResponse;
import gov.va.isaac.util.UpdateableBooleanBinding;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.WBUtility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import javafx.application.Platform;
import javafx.beans.binding.FloatBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
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
import org.ihtsdo.otf.tcc.model.index.service.SearchResult;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private TreeTableView<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>> ttv_;
	private TreeItem<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>> treeRoot_;
	private Button removeButton_, addButton_, commitButton_, cancelButton_, editButton_;
	private ToggleButton stampButton_, activeOnlyButton_, historyButton_;
	private UpdateableBooleanBinding rowSelected_;
	private UpdateableBooleanBinding showStampColumns_, showActiveOnly_, showFullHistory_;
	private TreeTableColumn<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>, String> stampColumn_;
	private BooleanProperty hasUncommitted_ = new SimpleBooleanProperty(false);
	
	private Text placeholderText = new Text("No Dynamic Refexes were found associated with the component");
	private Button backgroundSearchCancelButton_;
	private ProgressBar progressBar_;
	private RefexAnnotationSearcher processor_;
	
	private Logger logger_ = LoggerFactory.getLogger(this.getClass());

	private InputType setFromType_ = null;
	private Integer newComponentHint = null;  //Useful when viewing from the assemblage perspective, and they add a new component - we can't find it without an index.
	private DialogResponse dr_ = null;
	private final Object dialogThreadBlock_ = new Object();
	private volatile boolean noRefresh_ = false;
	
	private DynamicRefexView() 
	{
		//Created by HK2 - no op - delay till getView called
	}
	
	private void initialInit()
	{
		if (rootNode_ == null)
		{
			ttv_ = new TreeTableView<>();
			ttv_.setTableMenuButtonVisible(true);
	
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
			
			rowSelected_ = new UpdateableBooleanBinding()
			{
				{
					addBinding(ttv_.getSelectionModel().getSelectedCells());
				}
				@Override
				protected boolean computeValue()
				{
					return ttv_.getSelectionModel().getSelectedCells().size() > 0;
				}
			};
			
			removeButton_ = new Button(null, Images.MINUS.createImageView());
			removeButton_.setTooltip(new Tooltip("Retire Selected Refex Extension(s)"));
			removeButton_.disableProperty().bind(rowSelected_.not());
			removeButton_.setOnAction((action) ->
			{
				try
				{
					YesNoDialog dialog = new YesNoDialog(rootNode_.getScene().getWindow());
					DialogResponse dr = dialog.showYesNoDialog("Retire?", "Do you want to retire the selected refex entries?");
					if (DialogResponse.YES == dr)
					{
						ObservableList<TreeItem<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>>> selected = ttv_.getSelectionModel().getSelectedItems();
						if (selected != null && selected.size() > 0)
						{
							for (TreeItem<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>> refexTreeItem : selected)
							{
								//TODO - have a bug here - should only be able to remove if it is currently active.
								//but how to know if it is currently active?  We have both...
								//TODO add some sort of graphical marker about current vs historical - also active vs retired... need a place to put this data.
								RefexDynamicVersionBI<?> refex = refexTreeItem.getValue();
								if (!refex.isActive())
								{
									continue;
								}
								RefexDynamicCAB rcab =  refex.makeBlueprint(WBUtility.getViewCoordinate(), IdDirective.PRESERVE, RefexDirective.INCLUDE);
								rcab.setStatus(Status.INACTIVE);
								WBUtility.getBuilder().construct(rcab);
								
								ConceptVersionBI assemblage = WBUtility.getConceptVersion(refex.getAssemblageNid());
								ExtendedAppContext.getDataStore().addUncommitted(ExtendedAppContext.getDataStore().getConceptForNid(refex.getReferencedComponentNid()));
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
					logger_.error("Unexpected error retiring refex", e);
					AppContext.getCommonDialogs().showErrorDialog("Error", "There was an unexpected error retiring the refex", e.getMessage(), rootNode_.getScene().getWindow());
				}
			});
			
			t.getItems().add(removeButton_);
			
			addButton_ = new Button(null, Images.PLUS.createImageView());
			addButton_.setTooltip(new Tooltip("Add a new Refex Extension"));
			addButton_.setOnAction((action) ->
			{
				AddRefexPopup arp = AppContext.getService(AddRefexPopup.class);
				arp.finishInit(setFromType_, this);
				arp.showView(rootNode_.getScene().getWindow());
			});
			
			addButton_.setDisable(true);
			t.getItems().add(addButton_);
			
			//TODO similar bug on to the retire but here - should only be able to edit the current one.  but which one is current?
			editButton_ = new Button(null, Images.EDIT.createImageView());
			editButton_.setTooltip(new Tooltip("Edit a Refex"));
			editButton_.disableProperty().bind(rowSelected_.not());
			editButton_.setOnAction((action) ->
			{
				AddRefexPopup arp = AppContext.getService(AddRefexPopup.class);
				arp.finishInit(ttv_.getSelectionModel().getSelectedItem().getValue(), this);
				arp.showView(rootNode_.getScene().getWindow());
			});
			t.getItems().add(editButton_);
			
			//fill to right
			Region r = new Region();
			HBox.setHgrow(r, Priority.ALWAYS);
			t.getItems().add(r);
			
			stampButton_ = new ToggleButton("");
			stampButton_.setGraphic(Images.STAMP.createImageView());
			stampButton_.setTooltip(new Tooltip("Show / Hide Stamp Columns"));
			stampButton_.setVisible(false);
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
						for (TreeTableColumn<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>, ?> nested : stampColumn_.getColumns())
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
			
			cancelButton_ = new Button("Cancel");
			cancelButton_.disableProperty().bind(hasUncommitted_.not());
			t.getItems().add(cancelButton_);
			cancelButton_.setOnAction((action) ->
			{
				try
				{
					ExtendedAppContext.getDataStore().cancel();
					
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
		}
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.DockedViewI#getView()
	 */
	@Override
	public Region getView()
	{
		//setting up the binding stuff is causing refresh calls
		noRefresh_ = true;
		initialInit();
		noRefresh_ = false;
		return rootNode_;
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.RefexViewI#setComponent(int, ReadOnlyBooleanProperty, ReadOnlyBooleanProperty, ReadOnlyBooleanProperty))
	 */
	@Override
	public void setComponent(int componentNid, ReadOnlyBooleanProperty showStampColumns, ReadOnlyBooleanProperty showActiveOnly, 
			ReadOnlyBooleanProperty showFullHistory)
	{
		//disable refresh, as the bindings mucking causes many refresh calls
		noRefresh_ = true;
		initialInit();
		setFromType_ = new InputType(componentNid, false);
		handleExternalBindings(showStampColumns, showActiveOnly, showFullHistory);
		newComponentHint = null;
		noRefresh_ = false;
		refresh();
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.RefexViewI#setAssemblage(int, ReadOnlyBooleanProperty, ReadOnlyBooleanProperty, ReadOnlyBooleanProperty)
	 */
	@Override
	public void setAssemblage(int assemblageConceptNid, ReadOnlyBooleanProperty showStampColumns, ReadOnlyBooleanProperty showActiveOnly, 
			ReadOnlyBooleanProperty showFullHistory)
	{
		//disable refresh, as the bindings mucking causes many refresh calls
		noRefresh_ = true;
		initialInit();
		setFromType_ = new InputType(assemblageConceptNid, true);
		handleExternalBindings(showStampColumns, showActiveOnly, showFullHistory);
		newComponentHint = null;
		noRefresh_ = false;
		refresh();
	}
	
	private void handleExternalBindings(ReadOnlyBooleanProperty showStampColumns, ReadOnlyBooleanProperty showActiveOnly, 
			ReadOnlyBooleanProperty showFullHistory)
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
	}
	
	protected void setNewComponentHint(int componentNid)
	{
		newComponentHint = componentNid;
	}
	
	protected void refresh()
	{
		if (noRefresh_)
		{
			return;
		}
		//This is silly - throwing out the entire table...
		//But JavaFX seems to be broken, and the code that should work - simply removing
		//all children from the rootNode - doesn't work.
		//So build a completely new table upon every refresh, for now.
		rootNode_.getChildren().remove(ttv_);
		ttv_ = new TreeTableView<>();
		ttv_.setTableMenuButtonVisible(true);
		VBox.setVgrow(ttv_, Priority.ALWAYS);
		rootNode_.getChildren().add(0, ttv_);
		
		treeRoot_ = new TreeItem<>();
		treeRoot_.setExpanded(true);
		ttv_.setShowRoot(false);
		ttv_.setRoot(treeRoot_);

		ttv_.setPlaceholder(progressBar_);
		progressBar_.setProgress(-1);
		rowSelected_.clearBindings();
		rowSelected_.addBinding(ttv_.getSelectionModel().getSelectedCells());
		
		loadData();
	}

	private void loadData()
	{
		Utility.execute(() -> {
			try
			{
				final ArrayList<TreeTableColumn<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>, ?>> treeColumns = new ArrayList<>();
				
				
				//Create columns for basic info
				if (setFromType_.getComponentNid() == null)
				{
					//If the component is null, the assemblage is always the same - don't show.
					TreeTableColumn<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>, Integer>  ttCol = new TreeTableColumn<>();
					ttCol.setText("Component");
					ttCol.setSortable(true);
					ttCol.setResizable(true);
					ttCol.setCellFactory((colInfo) -> 
					{
						return new ConceptDataCell();
						
					});
					ttCol.setCellValueFactory((callback) ->
					{
						return new ReadOnlyObjectWrapper<Integer>(callback.getValue().getValue().getReferencedComponentNid());
					});
					treeColumns.add(ttCol);
				}
				else
				{
					//if the assemblage is null, the component is always the same - don't show.
					TreeTableColumn<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>, Integer>  ttCol = new TreeTableColumn<>();
					ttCol.setText("Assemblage");
					ttCol.setSortable(true);
					ttCol.setResizable(true);
					ttCol.setCellFactory((colInfo) -> 
					{
						return new ConceptDataCell(true);
						
					});
					ttCol.setCellValueFactory((callback) ->
					{
						return new ReadOnlyObjectWrapper<Integer>(callback.getValue().getValue().getAssemblageNid());
					});
					treeColumns.add(ttCol);
				}
				
				TreeTableColumn<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>, String> ttStringCol = new TreeTableColumn<>();
				ttStringCol.setText("Attached Data");
				ttStringCol.setSortable(true);
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
				
				HashMap<Label , String> tooltipsToInstall = new HashMap<>();
				
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
						TreeTableColumn<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>, RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>> nestedCol = 
								new TreeTableColumn<>();
						Label l = new Label(col.values().iterator().next().get(0).getColumnName());  //all the same, just pick the first
						tooltipsToInstall.put(l, col.values().iterator().next().get(0).getColumnDescription());
						nestedCol.setGraphic(l);
						nestedCol.setSortable(true);
						nestedCol.setResizable(true);
						
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
				ttStringCol.setSortable(true);
				ttStringCol.setResizable(true);
				stampColumn_ = ttStringCol;
				treeColumns.add(ttStringCol);
				
				TreeTableColumn<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>, String> nestedCol = new TreeTableColumn<>();
				nestedCol.setText("Status");
				nestedCol.setSortable(true);
				nestedCol.setResizable(true);
				nestedCol.setCellValueFactory((callback) ->
				{
					return new ReadOnlyStringWrapper(callback.getValue().getValue().getStatus().toString());
				});
				ttStringCol.getColumns().add(nestedCol);
				

				nestedCol = new TreeTableColumn<>();
				nestedCol.setText("Time");
				nestedCol.setSortable(true);
				nestedCol.setResizable(true);
				nestedCol.setCellValueFactory((callback) ->
				{
					long l = callback.getValue().getValue().getTime();
					if (l == Long.MAX_VALUE)
					{
						return new ReadOnlyStringWrapper("-Uncommitted-");
					}
					else
					{
						return new ReadOnlyStringWrapper(new Date(l).toString());
					}
				});
				ttStringCol.getColumns().add(nestedCol);
				
				TreeTableColumn<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>, Integer> nestedIntCol = new TreeTableColumn<>();
				nestedIntCol.setText("Author");
				nestedIntCol.setSortable(true);
				nestedIntCol.setResizable(true);
				nestedIntCol.setCellFactory((colInfo) -> 
				{
					return new ConceptDataCell();
					
				});
				nestedIntCol.setCellValueFactory((callback) ->
				{
					return new ReadOnlyObjectWrapper<Integer>(callback.getValue().getValue().getAuthorNid());
				});

				ttStringCol.getColumns().add(nestedIntCol);
				
				nestedIntCol = new TreeTableColumn<>();
				nestedIntCol.setText("Module");
				nestedIntCol.setSortable(true);
				nestedIntCol.setResizable(true);
				nestedIntCol.setVisible(false);
				nestedIntCol.setCellFactory((colInfo) -> 
				{
					return new ConceptDataCell();
					
				});
				nestedIntCol.setCellValueFactory((callback) ->
				{
					return new ReadOnlyObjectWrapper<Integer>(callback.getValue().getValue().getModuleNid());
				});
				ttStringCol.getColumns().add(nestedIntCol);
				
				nestedIntCol = new TreeTableColumn<>();
				nestedIntCol.setText("Path");
				nestedIntCol.setSortable(true);
				nestedIntCol.setResizable(true);
				nestedIntCol.setVisible(false);
				nestedIntCol.setCellFactory((colInfo) -> 
				{
					return new ConceptDataCell();
					
				});
				nestedIntCol.setCellValueFactory((callback) ->
				{
					return new ReadOnlyObjectWrapper<Integer>(callback.getValue().getValue().getPathNid());
				});
				ttStringCol.getColumns().add(nestedIntCol);

				Platform.runLater(() ->
				{
					for (TreeTableColumn<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>, ?> tc : treeColumns)
					{
						ttv_.getColumns().add(tc);
					}
					
					for (Entry<Label, String> tooltips : tooltipsToInstall.entrySet())
					{
						Tooltip t = new Tooltip(tooltips.getValue());
						t.setMaxWidth(400);
						t.setWrapText(true);
						tooltips.getKey().setTooltip(t);
					}
					
					//Horrible hack to set a reasonable default size on the columns.
					//Min width to the width of the header column.
					Font f = new Font("System Bold", 13.0);
					for (final TreeTableColumn<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>, ?> col : ttv_.getColumns())
					{
						for (TreeTableColumn<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>, ?> nCol : col.getColumns())
						{
							String text = (nCol.getGraphic() != null && nCol.getGraphic() instanceof Label ? ((Label)nCol.getGraphic()).getText() : nCol.getText());
							nCol.setMinWidth(Toolkit.getToolkit().getFontLoader().computeStringWidth(text, f) + 10);
						}
						
						if (col.getColumns().size() > 0)
						{
							FloatBinding binding = new FloatBinding()
							{
								{
									for (TreeTableColumn<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>, ?> nCol : col.getColumns())
									{
										bind(nCol.widthProperty());
										bind(nCol.visibleProperty());
									}
								}
								@Override
								protected float computeValue()
								{
									float temp = 0;
									for (TreeTableColumn<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>, ?> nCol : col.getColumns())
									{
										if (nCol.isVisible())
										{
											temp += nCol.getWidth();
										}
									}
									return temp;
								}
							};
							col.minWidthProperty().bind(binding);
						}
						else
						{
							if (col.getText().equalsIgnoreCase("Assemblage") || col.getText().equalsIgnoreCase("Component"))
							{
								col.setPrefWidth(250);
							}
							col.setMinWidth(Toolkit.getToolkit().getFontLoader().computeStringWidth(col.getText(), f) + 10);
						}
					}
					showStampColumns_.invalidate();
				});

				ArrayList<TreeItem<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>>> rowData;
				//Now add the data
				if (setFromType_.getComponentNid() != null)
				{
					rowData = getDataRows(setFromType_.getComponentBI(), null);
				}
				else
				{
					rowData = getDataRows(setFromType_.getAssemblyNid());
				}

				Platform.runLater(() ->
				{
					addButton_.setDisable(false);
					for (TreeItem<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>> rd : rowData)
					{
						treeRoot_.getChildren().add(rd);
					}
					checkForUncommittedRefexes(rowData);
					ttv_.setPlaceholder(placeholderText);
				});
			}
			catch (Exception e)
			{
				logger_.error("Unexpected error building the refex display", e);
				//null check, as the error may happen before the scene is visible
				AppContext.getCommonDialogs().showErrorDialog("Error", "There was an unexpected error building the refex display", e.getMessage(), 
						(rootNode_.getScene() == null ? null : rootNode_.getScene().getWindow()));
			}
		});
	}
	
	private ArrayList<TreeItem<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>>> getDataRows(ComponentChronicleBI<?> component, 
			TreeItem<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>> nestUnder) 
			throws IOException, ContradictionException
	{
		ArrayList<TreeItem<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>>> rowData = new ArrayList<>();

		if (component instanceof ConceptChronicleBI)
		{
			component = ((ConceptChronicleBI) component).getConceptAttributes();
		}
		
		if (component == null)
		{
			return (nestUnder == null ? rowData : null);
		}

		for (RefexDynamicChronicleBI<?> refexChronicle : component.getRefexesDynamic())
		{
			RefexDynamicVersionBI<?> refexVersionNewest = null;
			for (RefexDynamicVersionBI<?> refexVersion : refexChronicle.getVersions())
			{
				if (!showFullHistory_.get())
				{
					if (refexVersionNewest == null || refexVersion.getStamp() > refexVersionNewest.getStamp())
					{
						refexVersionNewest = refexVersion;
					}
				}
				else
				{
					if (showActiveOnly_.get() && !refexVersion.isActive())
					{
						continue;
					}
					TreeItem<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>> ti = new TreeItem<>();
					ti.setValue(refexVersion);
					//recurse
					getDataRows(refexVersion, ti);
					rowData.add(ti);
				}
			}
			if (!showFullHistory_.get() && refexVersionNewest != null 
					&& (!showActiveOnly_.get() || (showActiveOnly_.get() && refexVersionNewest.isActive())))
			{
				TreeItem<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>> ti = new TreeItem<>();
				ti.setValue(refexVersionNewest);
				//recurse
				getDataRows(refexVersionNewest, ti);
				rowData.add(ti);
			}
		}
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
	
	private void checkForUncommittedRefexes(List<TreeItem<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>>> items)
	{
		if (hasUncommitted_.get())
		{
			return;
		}
		if (items == null)
		{
			return;
		}
		for (TreeItem<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>> item : items)
		{
			if (item.getValue() != null && item.getValue().isUncommitted())
			{
				hasUncommitted_.set(true);
				return;
			}
			checkForUncommittedRefexes(item.getChildren());
		}
	}
	
	private HashSet<Integer> getAllAssemblageNids(List<TreeItem<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>>> items)
	{
		HashSet<Integer> results = new HashSet<Integer>();
		if (items == null)
		{
			return results;
		}
		for (TreeItem<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>> item : items)
		{
			if (item.getValue() != null)
			{
				RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>> refex = item.getValue();
				results.add(refex.getAssemblageNid());
			}
			results.addAll(getAllAssemblageNids(item.getChildren()));
		}
		return results;
	}
	
	private HashSet<Integer> getAllComponentNids(List<TreeItem<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>>> items)
	{
		HashSet<Integer> results = new HashSet<Integer>();
		if (items == null)
		{
			return results;
		}
		for (TreeItem<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>> item : items)
		{
			if (item.getValue() != null)
			{
				RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>> refex = item.getValue();
				results.add(refex.getReferencedComponentNid());
			}
			results.addAll(getAllComponentNids(item.getChildren()));
		}
		return results;
	}
	

	@SuppressWarnings("unchecked")
	private ArrayList<TreeItem<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>>> getDataRows(int assemblageNid) 
			throws IOException, ContradictionException, InterruptedException, NumberFormatException, ParseException
	{
		ArrayList<TreeItem<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>>> rowData = new ArrayList<>();
		Collection<RefexDynamicChronicleBI<?>> refexMembers;
		
		dr_ = null;
		
		ConceptVersionBI assemblageConceptFull = WBUtility.getConceptVersion(assemblageNid);
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
				List<SearchResult> results = ldri.queryAssemblageUsage(assemblageConceptFull.getNid(), Integer.MAX_VALUE, null);
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
						dr_ = dialog.showYesNoDialog("Scan for Annotation Refex entries?", "This is an annotation style Refex with no index."
								+ "  Without a supporting index, displaying the refex entries will take a long time.  Scan for entries?");
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
					if (newComponentHint != null)
					{
						ConceptChronicleBI c = ExtendedAppContext.getDataStore().getConcept(newComponentHint);
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

		for (RefexDynamicChronicleBI<?> refexChronicle: refexMembers)
		{
			RefexDynamicVersionBI<?> refexVersionNewest = null;
			for (RefexDynamicVersionBI<?> refexVersion : refexChronicle.getVersions())
			{
				if (!showFullHistory_.get())
				{
					if (refexVersionNewest == null || refexVersion.getStamp() > refexVersionNewest.getStamp())
					{
						refexVersionNewest = refexVersion;
					}
				}
				else
				{
					if (showActiveOnly_.get() && !refexVersion.isActive())
					{
						continue;
					}
					TreeItem<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>> ti = new TreeItem<>();
					ti.setValue(refexVersion);
					//recurse
					getDataRows(refexVersion, ti);
					rowData.add(ti);
				}
			}
			if (!showFullHistory_.get() && refexVersionNewest != null 
					&& (!showActiveOnly_.get() || (showActiveOnly_.get() && refexVersionNewest.isActive())))
			{
				TreeItem<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>> ti = new TreeItem<>();
				ti.setValue(refexVersionNewest);
				//recurse
				getDataRows(refexVersionNewest, ti);
				rowData.add(ti);
			}
		}
		if (rowData.size() == 0)
		{
			placeholderText.setText("No Dynamic Refexes were found using this Assemblage");
		}
		return rowData;
	}
}
