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
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.WBUtility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.FloatBinding;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
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
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
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
	private VBox rootNode_;
	private UUID componentUUID_;
	private int componentNid_;
	private TreeTableView<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>> ttv_;
	private TreeItem<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>> treeRoot_;
	private Button removeButton_, addButton_, commitButton_, cancelButton_;
	private BooleanBinding removeButtonEnabled_;
	private ReadOnlyBooleanProperty showStampColumns_;
	TreeTableColumn<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>, String> stampColumn_;
	private Logger logger_ = LoggerFactory.getLogger(this.getClass());

	private DynamicRefexView() throws IOException
	{
		// created by HK2
		ttv_ = new TreeTableView<>();
		ttv_.setTableMenuButtonVisible(true);

		treeRoot_ = new TreeItem<>();
		treeRoot_.setExpanded(true);
		ttv_.setShowRoot(false);
		ttv_.setRoot(treeRoot_);
		
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
			arp.finishInit(componentNid_, this);
			arp.showView(rootNode_.getScene().getWindow());
		});
		
		addButton_.setDisable(true);
		t.getItems().add(addButton_);
		
		//fill to right
		Region r = new Region();
		HBox.setHgrow(r, Priority.ALWAYS);
		t.getItems().add(r);
		
		//TODO implement tossUncommitted
		cancelButton_ = new Button("Cancel");
		cancelButton_.setDisable(true);
		t.getItems().add(cancelButton_);
		
		commitButton_ = new Button("Commit");
		commitButton_.setDisable(true);
		t.getItems().add(commitButton_);
		
		rootNode_.getChildren().add(t);
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.DockedViewI#getView()
	 */
	@Override
	public Region getView()
	{
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
	 * @see gov.va.isaac.interfaces.gui.views.RefexViewI#setComponent(java.util.UUID)
	 */
	@Override
	public void setComponent(UUID componentUUID, ReadOnlyBooleanProperty showStampColumns)
	{
		componentUUID_ = componentUUID;
		treeRoot_.getChildren().clear();
		ttv_.getColumns().clear();
		ttv_.setPlaceholder(new ProgressBar());
		if (showStampColumns != showStampColumns_)
		{
			showStampColumns_ = showStampColumns;
			showStampColumns_.addListener((listener, oldV, newV) ->
			{
				setStampColVisibility();
			});
		}
		init();
	}
	
	protected void refresh()
	{
		setComponent(componentUUID_, showStampColumns_);
	}

	private void init()
	{
		Utility.execute(() -> {
			try
			{
				ComponentChronicleBI<?> component = ExtendedAppContext.getDataStore().getComponent(componentUUID_);
				componentNid_ = component.getNid();
				final ArrayList<TreeTableColumn<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>, String>> treeColumns = new ArrayList<>();
				
				//Create columns for basic info
				TreeTableColumn<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>, String> ttCol = new TreeTableColumn<>();
				ttCol.setText("Assemblage");
				ttCol.setSortable(true);
				ttCol.setResizable(true);
				ttCol.setCellValueFactory((callback) ->
				{
					return new ReadOnlyStringWrapper(WBUtility.getDescription(callback.getValue().getValue().getAssemblageNid()));
				});
				
				treeColumns.add(ttCol);
				
				
				ttCol = new TreeTableColumn<>();
				ttCol.setText("Attached Data");
				ttCol.setSortable(true);
				ttCol.setResizable(true);
				treeColumns.add(ttCol);
				
				//Create columns for every different type of data column we see in use
				for (Hashtable<UUID, RefexDynamicColumnInfo> col : getUniqueColumns(component).values())
				{
					//TODO figure out how to put a tooltip on the header
					TreeTableColumn<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>, RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>> nestedCol = 
							new TreeTableColumn<>();
					nestedCol.setText(col.values().iterator().next().getColumnName());
					nestedCol.setSortable(true);
					nestedCol.setResizable(true);
					
					nestedCol.setCellFactory((callback) -> 
					{
						return new DataCell(col.values());
					});
					
					nestedCol.setCellValueFactory((callback) ->
					{
						return new ReadOnlyObjectWrapper<>(callback.getValue().getValue());
					}); 
					
					ttCol.getColumns().add(nestedCol);
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
					//Horrible hack to set a reasonable default size on the columns.
					//Min width to the width of the header column.
					Font f = new Font("System Bold", 13.0);
					for (final TreeTableColumn<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>, ?> col : ttv_.getColumns())
					{
						float nestedTotal = 0;
						for (TreeTableColumn<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>, ?> nCol : col.getColumns())
						{
							nCol.setMinWidth(Toolkit.getToolkit().getFontLoader().computeStringWidth(nCol.getText(), f) + 10);
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
					setStampColVisibility();
				});

				
				//Now add the data
				final ArrayList<TreeItem<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>>> rowData = getDataRows(component, null);
				Platform.runLater(() ->
				{
					addButton_.setDisable(false);
					for (TreeItem<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>> rd : rowData)
					{
						treeRoot_.getChildren().add(rd);
					}
					ttv_.setPlaceholder(new Text("No content in table"));
					//JavaFX bug hacking...
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
	 * will be the same.
	 */
	private Hashtable<UUID, Hashtable<UUID, RefexDynamicColumnInfo>> getUniqueColumns(ComponentChronicleBI<?> component) throws IOException, ContradictionException
	{
		//ComponentChronicleBI<?> component = ExtendedAppContext.getDataStore().getComponent(componentUUID);
		Hashtable<UUID, Hashtable<UUID, RefexDynamicColumnInfo>> columns = new Hashtable<>();

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
			for (RefexDynamicColumnInfo column : refex.getRefexDynamicUsageDescription().getColumnInfo())
			{
				Hashtable<UUID, RefexDynamicColumnInfo> inner = columns.get(column.getColumnDescriptionConcept());
				if (inner == null)
				{
					inner = new Hashtable<>();
					columns.put(column.getColumnDescriptionConcept(), inner);
				}
				inner.put(column.getAssemblageConcept(), column);
			}
			
			for (RefexDynamicChronicleBI<?> refexNested: refex.getRefexesDynamic())
			{
				//recurse
				Hashtable<UUID, Hashtable<UUID, RefexDynamicColumnInfo>> nested = getUniqueColumns(refexNested);
				for (Entry<UUID, Hashtable<UUID, RefexDynamicColumnInfo>> nestedItem : nested.entrySet())
				{
					if (columns.get(nestedItem.getKey()) == null)
					{
						columns.put(nestedItem.getKey(), nestedItem.getValue());
					}
					else
					{
						columns.get(nestedItem.getKey()).putAll(nestedItem.getValue());
					}
				}
			}
		}
		return columns;
	}
	
	private void setStampColVisibility()
	{
		boolean visible = showStampColumns_.get();
		stampColumn_.setVisible(visible);
		for (TreeTableColumn<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>, ?> nested : stampColumn_.getColumns())
		{
			nested.setVisible(visible);
		}
	}
}
