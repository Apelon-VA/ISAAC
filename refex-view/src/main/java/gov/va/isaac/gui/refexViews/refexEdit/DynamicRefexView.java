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
import gov.va.isaac.interfaces.gui.MenuItemI;
import gov.va.isaac.interfaces.gui.views.RefexViewI;
import gov.va.isaac.interfaces.utility.DialogResponse;
import gov.va.isaac.util.UpdateableBooleanBinding;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.WBUtility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.FloatBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
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
import org.glassfish.hk2.api.PerLookup;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicUsageDescription;
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
	private ToggleButton stampButton_;
	private BooleanBinding removeButtonEnabled_;
	private UpdateableBooleanBinding showStampColumns_;
	private TreeTableColumn<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>, String> stampColumn_;
	private BooleanProperty hasUncommitted_ = new SimpleBooleanProperty(false);
	
	private Text placeholderText = new Text("No Dynamic Refexes were found associated with the component");
	
	private Logger logger_ = LoggerFactory.getLogger(this.getClass());

	private InputType setFromType_ = null;
	private Integer newComponentHint = null;  //Useful when viewing from the assemblage perspective, and they add a new component - we can't find it without an index.
	private DialogResponse dr_ = null;
	private final Object dialogThreadBlock_ = new Object();
	
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
			ttv_.setPlaceholder(new ProgressBar());
			
			rootNode_ = new VBox();
			rootNode_.setFillWidth(true);
			rootNode_.getChildren().add(ttv_);
			VBox.setVgrow(ttv_, Priority.ALWAYS);
			
			ToolBar t = new ToolBar();
			
			removeButtonEnabled_ = new BooleanBinding()
			{
				{
					bind(ttv_.getSelectionModel().getSelectedCells());
				}
				@Override
				protected boolean computeValue()
				{
					return ttv_.getSelectionModel().getSelectedCells().size() > 0;
				}
			};
			
			removeButton_ = new Button(null, Images.MINUS.createImageView());
			removeButton_.setTooltip(new Tooltip("Retire Selected Refex Extension(s)"));
			removeButton_.disableProperty().bind(removeButtonEnabled_.not());
			removeButton_.setOnAction((action) ->
			{
				try
				{
					YesNoDialog dialog = new YesNoDialog(rootNode_.getScene().getWindow());
					DialogResponse dr = dialog.showYesNoDialog("Retire?", "Do you want to retire the selected refex entries?");
					if (DialogResponse.YES == dr)
					{
						//TODO implement refex retire
						System.out.println("Do retire!");
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
			
			editButton_ = new Button(null, Images.EDIT.createImageView());
			editButton_.setTooltip(new Tooltip("Edit a Refex"));
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
			stampButton_.setTooltip(new Tooltip("Show/Hide Stamp Columns"));
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
			
			cancelButton_ = new Button("Cancel");
			cancelButton_.disableProperty().bind(hasUncommitted_.not());
			t.getItems().add(cancelButton_);
			cancelButton_.setOnAction((action) ->
			{
				try
				{
					//TODO [OTF API] I can't cancel individual items?  Seriously?
					ExtendedAppContext.getDataStore().cancel();
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
					//TODO [OTF-API] BUG! So - when you currently commit a change to a member-list style refset - and everything appears to work correctly 
					//in this session - if you stop and start the OTF backend - the member refset will vanish.  Something nasty going on here.
					//Definitely an OTF bug, possible in the dynamic refex code, possibly not.  Unsure at this point.
					
					HashSet<Integer> componentNids = getAllComponentNids(treeRoot_.getChildren());
					for (Integer i : componentNids)
					{
						//TODO how to handle cases where it isn't a concept?  Might be a description... we probably have to commit the parent concept?
						ConceptChronicleBI cc = ExtendedAppContext.getDataStore().getConcept(i);
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
			
			rootNode_.getChildren().add(t);
		}
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.DockedViewI#getView()
	 */
	@Override
	public Region getView()
	{
		initialInit();
		return rootNode_;
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.IsaacViewI#getMenuBarMenus()
	 */
	@Override
	public List<MenuItemI> getMenuBarMenus()
	{
		// We don't currently have any custom menus with this view
		return new ArrayList<MenuItemI>();
	}
	
	/**
	 * @see gov.va.isaac.interfaces.gui.views.RefexViewI#setComponent(int, boolean, javafx.beans.property.ReadOnlyBooleanProperty)
	 */
	@Override
	public void setComponent(int componentNid, ReadOnlyBooleanProperty showStampColumns)
	{
		initialInit();
		setFromType_ = new InputType(componentNid, false);
		handleStamp(showStampColumns);
		newComponentHint = null;
		refresh();
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.RefexViewI#setAssemblage(int, boolean, javafx.beans.property.ReadOnlyBooleanProperty)
	 */
	@Override
	public void setAssemblage(int assemblageConceptNid, ReadOnlyBooleanProperty showStampColumns)
	{
		initialInit();
		setFromType_ = new InputType(assemblageConceptNid, true);
		handleStamp(showStampColumns);
		newComponentHint = null;
		refresh();
	}
	
	private void handleStamp(ReadOnlyBooleanProperty showStampColumns)
	{
		showStampColumns_.clearBindings();
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
	}
	
	protected void setNewComponentHint(int componentNid)
	{
		newComponentHint = componentNid;
	}
	
	protected void refresh()
	{
		//This is stilly - throwing out the entire table...
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

		ttv_.setPlaceholder(new ProgressBar());
		
		loadData();
	}

	private void loadData()
	{
		Utility.execute(() -> {
			try
			{
				final ArrayList<TreeTableColumn<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>, String>> treeColumns = new ArrayList<>();
				
				TreeTableColumn<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>, String> ttCol;
				
				//Create columns for basic info
				if (setFromType_.getComponentNid() == null)
				{
					//If the component is null, the assemblage is always the same - don't show.
					ttCol = new TreeTableColumn<>();
					ttCol.setText("Component");
					ttCol.setSortable(true);
					ttCol.setResizable(true);
					ttCol.setCellValueFactory((callback) ->
					{
						return new ReadOnlyStringWrapper(WBUtility.getDescription(callback.getValue().getValue().getReferencedComponentNid()));
					});
					treeColumns.add(ttCol);
				}
				else
				{
					//if the assemblage is null, the component is always the same - don't show.
					ttCol = new TreeTableColumn<>();
					ttCol.setText("Assemblage");
					ttCol.setSortable(true);
					ttCol.setResizable(true);
					ttCol.setCellValueFactory((callback) ->
					{
						return new ReadOnlyStringWrapper(WBUtility.getDescription(callback.getValue().getValue().getAssemblageNid()));
					});
					treeColumns.add(ttCol);
				}
				
				ttCol = new TreeTableColumn<>();
				ttCol.setText("Attached Data");
				ttCol.setSortable(true);
				ttCol.setResizable(true);
				treeColumns.add(ttCol);
				
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
				
				//Create columns for every different type of data column we see in use
				for (Hashtable<UUID, List<RefexDynamicColumnInfo>> col : uniqueColumns.values())
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
						
						nestedCol.setCellFactory(new DataCellFactory(col, i));
						
						nestedCol.setCellValueFactory((callback) ->
						{
							return new ReadOnlyObjectWrapper<>(callback.getValue().getValue());
						}); 
						
						ttCol.getColumns().add(nestedCol);
					}
				}
				
				//Create the STAMP columns
				ttCol = new TreeTableColumn<>();
				ttCol.setText("STAMP");
				ttCol.setSortable(true);
				ttCol.setResizable(true);
				stampColumn_ = ttCol;
				treeColumns.add(ttCol);
				
				TreeTableColumn<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>, String> nestedCol = new TreeTableColumn<>();
				nestedCol.setText("Status");
				nestedCol.setSortable(true);
				nestedCol.setResizable(true);
				nestedCol.setCellValueFactory((callback) ->
				{
					return new ReadOnlyStringWrapper(callback.getValue().getValue().getStatus().toString());
				});
				ttCol.getColumns().add(nestedCol);
				

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
				ttCol.getColumns().add(nestedCol);
				
				nestedCol = new TreeTableColumn<>();
				nestedCol.setText("Author");
				nestedCol.setSortable(true);
				nestedCol.setResizable(true);
				nestedCol.setCellValueFactory((callback) ->
				{
					return new ReadOnlyStringWrapper(WBUtility.getDescription(callback.getValue().getValue().getAuthorNid()));
				});
				ttCol.getColumns().add(nestedCol);
				
				nestedCol = new TreeTableColumn<>();
				nestedCol.setText("Module");
				nestedCol.setSortable(true);
				nestedCol.setResizable(true);
				nestedCol.setVisible(false);
				nestedCol.setCellValueFactory((callback) ->
				{
					return new ReadOnlyStringWrapper(WBUtility.getDescription(callback.getValue().getValue().getModuleNid()));
				});
				ttCol.getColumns().add(nestedCol);
				
				nestedCol = new TreeTableColumn<>();
				nestedCol.setText("Path");
				nestedCol.setSortable(true);
				nestedCol.setResizable(true);
				nestedCol.setVisible(false);
				nestedCol.setCellValueFactory((callback) ->
				{
					return new ReadOnlyStringWrapper(WBUtility.getDescription(callback.getValue().getValue().getPathNid()));
				});
				ttCol.getColumns().add(nestedCol);

				Platform.runLater(() ->
				{
					for (TreeTableColumn<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>, String> tc : treeColumns)
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
						float nestedTotal = 0;
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
							col.setMinWidth(nestedTotal > 0 ? nestedTotal : Toolkit.getToolkit().getFontLoader().computeStringWidth(col.getText(), f) + 10);
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
			for (RefexDynamicVersionBI<?> refexVersion : refexChronicle.getVersions())
			{
				TreeItem<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>> ti = new TreeItem<>();
				ti.setValue(refexVersion);
				//recurse
				getDataRows(refexVersion, ti);
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
			throws IOException, ContradictionException, InterruptedException
	{
		ArrayList<TreeItem<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>>> rowData = new ArrayList<>();
		Collection<RefexDynamicChronicleBI<?>> refexMembers;
		boolean annotationStyle;
		boolean noIndex = true;
		
		ConceptVersionBI assemblageConceptFull = WBUtility.getConceptVersion(assemblageNid);
		annotationStyle = assemblageConceptFull.isAnnotationStyleRefex();
		if (!annotationStyle)
		{
			refexMembers = (Collection<RefexDynamicChronicleBI<?>>)assemblageConceptFull.getRefsetDynamicMembers();
		}
		else
		{
			//TODO see if there is an index, use it here.
			refexMembers = new ArrayList<>();
			
			//add in the newComponentHint
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
		
		dr_ = null;
		
		if (annotationStyle && noIndex)
		{
			Platform.runLater(() ->
			{
				synchronized (dialogThreadBlock_)
				{
					YesNoDialog dialog = new YesNoDialog(rootNode_.getScene().getWindow());
					dr_ = dialog.showYesNoDialog("Scan for Annotation Refex entries?", "This is an annotation style Refex."
							+ "  Without a supporting index, displaying the refex entries will take a long time.  Scan for entries?");
					if (dr_ == DialogResponse.NO)
					{
						placeholderText.setText("No index is available to fetch the entries");
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
				// TODO implement long scan
				Thread.sleep(5000);
			}
		}

		for (RefexDynamicChronicleBI<?> refexChronicle: refexMembers)
		{
			for (RefexDynamicVersionBI<?> refexVersion : refexChronicle.getVersions())
			{
				TreeItem<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>> ti = new TreeItem<>();
				ti.setValue(refexVersion);
				//recurse
				getDataRows(refexVersion, ti);
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
