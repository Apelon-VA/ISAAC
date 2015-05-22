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
package gov.va.isaac.gui.dialog;

import gov.va.isaac.AppContext;
import gov.va.isaac.config.profiles.UserProfileBindings;
import gov.va.isaac.gui.dragAndDrop.DragRegistry;
import gov.va.isaac.gui.dragAndDrop.SingleConceptIdProvider;
import gov.va.isaac.gui.refexViews.refexEdit.DynamicRefexView;
import gov.va.isaac.gui.util.CustomClipboard;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.util.CommonMenus;
import gov.va.isaac.util.CommonMenusNIdProvider;
import gov.va.isaac.util.OTFUtility;
import gov.va.isaac.util.UpdateableBooleanBinding;
import gov.va.isaac.util.Utility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import javafx.beans.binding.FloatBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Callback;
import org.controlsfx.control.PopOver;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.javafx.tk.Toolkit;

/**
 * {@link DescriptionTableView}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DescriptionTableView
{
	private static final Logger LOG = LoggerFactory.getLogger(DescriptionTableView.class);
	
	private TableView<DescriptionVersion> descriptionsTable = new TableView<>();
	private UUID conceptUUID_;
	private BooleanProperty showActiveOnly_, showHistory_, showStampColumns_;
	
	ArrayList<TableColumn<DescriptionVersion, DescriptionVersion>> stampColumns = new ArrayList<>();
	
	@SuppressWarnings("unused")
	private UpdateableBooleanBinding refreshRequiredListenerHack;

	public DescriptionTableView(BooleanProperty showStampColumns, BooleanProperty showHistory, BooleanProperty showActiveOnly)
	{
		descriptionsTable.setTableMenuButtonVisible(true);
		descriptionsTable.setMaxHeight(Double.MAX_VALUE);
		showActiveOnly_ = showActiveOnly;
		showHistory_ = showHistory;
		showStampColumns_ = showStampColumns;
		
		showStampColumns.addListener((change, oldV, newV) -> 
		{
			for (TableColumn<DescriptionVersion, DescriptionVersion> c : stampColumns)
			{
				c.setVisible(newV.booleanValue());
			}
		});
		
		refreshRequiredListenerHack = new UpdateableBooleanBinding()
		{
			private volatile AtomicBoolean refreshQueued = new AtomicBoolean(false);
			{
				setComputeOnInvalidate(true);
				addBinding(AppContext.getService(UserProfileBindings.class).getViewCoordinatePath(),
						AppContext.getService(UserProfileBindings.class).getDisplayFSN(),
						showActiveOnly,
						showHistory);
			}

			@Override
			protected boolean computeValue()
			{
				synchronized (refreshQueued)
				{
					if (refreshQueued.get())
					{
						LOG.info("Skip description refresh() due to pending refresh");
						return false;
					}
					else
					{
						refreshQueued.set(true);
						LOG.debug("Desc refresh() due to change of an observed user property in " + DescriptionTableView.this);
						Utility.schedule(() ->
						{
							Platform.runLater(() ->
							{
								try
								{
									synchronized (refreshQueued)
									{
										refreshQueued.set(false);
									}
									refresh(null);
								}
								catch (Exception e)
								{
									LOG.error("Unexpected error running refresh", e);
								}
							});
						}, 10, TimeUnit.MILLISECONDS);
					}
				}
				return false;
			}
		};
		
		Callback<TableColumn<DescriptionVersion, DescriptionVersion>, TableCell<DescriptionVersion, DescriptionVersion>> cellFactory =
			new Callback<TableColumn<DescriptionVersion, DescriptionVersion>, TableCell<DescriptionVersion, DescriptionVersion>>()
		{
			@Override
			public TableCell<DescriptionVersion, DescriptionVersion> call(TableColumn<DescriptionVersion, DescriptionVersion> param)
			{
				return new TableCell<DescriptionVersion, DescriptionVersion>()
				{

					@Override
					public void updateItem(final DescriptionVersion ref, boolean empty)
					{
						super.updateItem(ref, empty);

						if (!isEmpty() && ref != null)
						{
							Node graphic = null;
							ContextMenu cm = new ContextMenu();
							setContextMenu(cm);
							switch((DescriptionColumnType)getTableColumn().getUserData())
							{
								case STATUS_CONDENSED:
									
									String tooltipText = "";
									StackPane sp = new StackPane();
									sp.setPrefSize(25, 25);

									try
									{
										if (ref.getDescriptionVersion().isActive())
										{
											sizeAndPosition(Images.BLACK_DOT.createImageView(), sp, Pos.TOP_LEFT);
											tooltipText += "Active";
										}
										else
										{
											sizeAndPosition(Images.GREY_DOT.createImageView(), sp, Pos.TOP_LEFT);
											tooltipText += "Inactive";
										}
										
										if (!ref.isCurrent())
										{
											sizeAndPosition(Images.HISTORICAL.createImageView(), sp, Pos.BOTTOM_LEFT);
											tooltipText += " and Historical";
										}
										else
										{
											tooltipText += " and Current";
										}
										
										if (ref.getDescriptionVersion().getTime() == Long.MAX_VALUE)
										{
											sizeAndPosition(Images.YELLOW_DOT.createImageView(), sp, Pos.TOP_RIGHT);
											tooltipText += " - Uncommitted";
										}
										if (ref.hasDynamicRefex())
										{
											//I can't seem to get just and image view to pick up mouse clicks
											//but it works in a button... sigh.
											Button b = new Button();
											b.setPadding(new Insets(0));
											b.setPrefHeight(12.0);
											b.setPrefWidth(12.0);
											ImageView iv = Images.ATTACH.createImageView();
											iv.setFitHeight(12.0);
											iv.setFitWidth(12.0);
											b.setGraphic(iv);
											b.setOnAction((event) ->
											{
												DynamicRefexView drv = AppContext.getService(DynamicRefexView.class);
												drv.setComponent(ref.getDescriptionVersion().getNid(), null, null, null, true);
												
												BorderPane bp = new BorderPane();
												
												Label title = new Label("Sememes attached to Description ");
												title.setMaxWidth(Double.MAX_VALUE);
												title.setAlignment(Pos.CENTER);
												title.setPadding(new Insets(10));
												title.getStyleClass().add("boldLabel");
												title.getStyleClass().add("headerBackground");
												
												bp.setTop(title);
												bp.setCenter(drv.getView());
												
												
												PopOver po = new PopOver();
												po.setContentNode(bp);
												po.setAutoHide(true);
												po.detachedTitleProperty().set("Sememes attached to Description");
												po.detachedProperty().addListener((change) ->
												{
													po.setAutoHide(false);
												});
												
												Point2D point = b.localToScreen(b.getWidth(), -32);
												po.show(b.getScene().getWindow(), point.getX(), point.getY());
											});
											sizeAndPosition(b, sp, Pos.BOTTOM_RIGHT);
										}
									}
									catch (Exception e)
									{
										LOG.error("Unexpected", e);
									}
									graphic = sp;
									setTooltip(new Tooltip(tooltipText));

									
									//TODO Only want to set the style once per row - note - this breaks if they turn off this row :(
									if (ref.isCurrent())
									{
										getTableRow().getStyleClass().removeAll("historical");
									}
									else
									{
										if (!getTableRow().getStyleClass().contains("historical"))
										{
											getTableRow().getStyleClass().add("historical");
										}
									}
									
									break;
								case TYPE: case AUTHOR: case MODULE: case PATH:
									graphic = backgroundLookup(this, (DescriptionColumnType)getTableColumn().getUserData(), ref);
									break;
								case LANGUAGE: 
									graphic = backgroundLookup(this, (DescriptionColumnType)getTableColumn().getUserData(), ref);
									break;
								case TIME: case STATUS_STRING: case UUID: case DESCRIPTION:
									graphic = new Text(ref.getDisplayStrings((DescriptionColumnType)getTableColumn().getUserData()).getKey());
									break;
								default :
									throw new RuntimeException("Unhandeled column");
							}
							
							if (graphic != null && graphic instanceof Text)
							{
								((Text)graphic).wrappingWidthProperty().bind(getTableColumn().widthProperty());
								// Menu item to copy cell text.
								MenuItem mi = new MenuItem("Copy Value");
								mi.setOnAction(new EventHandler<ActionEvent>()
								{
									@Override
									public void handle(ActionEvent arg0)
									{
										CustomClipboard.set(((Text)getGraphic()).getText());
									}
								});
								mi.setGraphic(Images.COPY.createImageView());
								cm.getItems().add(mi);
							}
							setGraphic(graphic);
							setText(null);
						}
						else
						{
							setText(null);
							setGraphic(null);
						}
					}
				};
			}
		};
		
		// Configure table columns.
		stampColumns.clear();
		addTableColumns(new DescriptionColumnType[] {DescriptionColumnType.STATUS_CONDENSED,  DescriptionColumnType.DESCRIPTION, DescriptionColumnType.UUID,
				DescriptionColumnType.TYPE, DescriptionColumnType.LANGUAGE}, false, cellFactory);
		
		addTableColumns(new DescriptionColumnType[] {DescriptionColumnType.STATUS_STRING,  DescriptionColumnType.TIME, DescriptionColumnType.AUTHOR, 
				DescriptionColumnType.MODULE, DescriptionColumnType.PATH}, true, cellFactory);

		descriptionsTable.setPrefHeight(descriptionsTable.getMinHeight() + (20.0 * descriptionsTable.getItems().size()));
		descriptionsTable.setPlaceholder(new Label());
		
		for (TableColumn<DescriptionVersion, DescriptionVersion> c : stampColumns)
		{
			c.setVisible(showStampColumns_.get());
		}
		
		//Horrible hack to set a reasonable default size on the columns.
		//Min width to the width of the header column.
		Font f = new Font("System Bold", 13.0);
		for (final TableColumn<DescriptionVersion, ?> col : descriptionsTable.getColumns())
		{
			//min widths to nested col titles across the board
			for (TableColumn<DescriptionVersion, ?> nCol : col.getColumns())
			{
				nCol.setMinWidth(Toolkit.getToolkit().getFontLoader().computeStringWidth(nCol.getText(), f) + 70);
			}
			
			//and now the parent column
			if (col.getColumns().size() > 0)
			{
				FloatBinding binding = new FloatBinding()
				{
					{
						for (TableColumn<DescriptionVersion, ?> nCol : col.getColumns())
						{
							bind(nCol.widthProperty());
							bind(nCol.visibleProperty());
						}
					}
					@Override
					protected float computeValue()
					{
						float temp = 0;
						for (TableColumn<DescriptionVersion, ?> nCol : col.getColumns())
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
				//no nested cols
				String text = col.getText();
				
				if (text.equalsIgnoreCase(DescriptionColumnType.DESCRIPTION.toString()) || text.equalsIgnoreCase(DescriptionColumnType.UUID.toString()))
				{
					col.setPrefWidth(300);
				}
				if (text.equalsIgnoreCase(DescriptionColumnType.STATUS_CONDENSED.toString()))
				{
					//TODO it would be nice to adjust the with of this column when disclosure nodes open and close...
					col.setPrefWidth(40);
					col.setMinWidth(40);
				}
				else
				{
					col.setMinWidth(Toolkit.getToolkit().getFontLoader().computeStringWidth(text, f) + 70);
				}
			}
		}
	}
	
	private void sizeAndPosition(Node node, StackPane sp, Pos position)
	{
		if (node instanceof ImageView)
		{
			((ImageView)node).setFitHeight(12);
			((ImageView)node).setFitWidth(12);
		}
		
		if (position == Pos.TOP_LEFT)
		{
			StackPane.setMargin(node, new Insets(0, 0, 0, 0));
		}
		else if (position == Pos.TOP_RIGHT)
		{
			StackPane.setMargin(node, new Insets(0, 0, 0, 13));
		}
		else if (position == Pos.BOTTOM_LEFT)
		{
			StackPane.setMargin(node, new Insets(13, 0, 0, 0));
		}
		else if (position == Pos.BOTTOM_RIGHT)
		{
			StackPane.setMargin(node, new Insets(13, 0, 0, 13));
		}
		else
		{
			throw new RuntimeException("Unsupported Position!");
		}
		sp.getChildren().add(node);
		StackPane.setAlignment(node, Pos.TOP_LEFT);
	}
	
	private Node backgroundLookup(final TableCell<DescriptionVersion, DescriptionVersion> cell, DescriptionColumnType type, DescriptionVersion ref)
	{
		Utility.execute(() ->
		{
			String value = ref.getDisplayStrings(type).getKey();
			Platform.runLater(() ->
			{
				if (cell.isEmpty() || cell.getItem() == null)
				{
					//We are updating a cell that should no longer be populated - stop!
					return;
				}
				ContextMenu cm = cell.getContextMenu();
				if (cm == null)
				{
					cm = new ContextMenu();
					cell.setContextMenu(cm);
				}
				
				Text text = new Text(value);
				cell.setGraphic(text);
				text.wrappingWidthProperty().bind(cell.getTableColumn().widthProperty());

				
				if (type != DescriptionColumnType.LANGUAGE)
				{
					CommonMenus.addCommonMenus(cm, new CommonMenusNIdProvider()
					{
						@Override
						public Collection<Integer> getNIds()
						{
							int nid = ref.getNidFetcher(type).applyAsInt(ref.getDescriptionVersion());
	
							ArrayList<Integer> nids = new ArrayList<>();
							if (nid != 0)
							{
								nids.add(nid);
							}
							return nids;
						}
					});
				}
				
				// Menu item to copy cell text.
				//for some reason, we get called multiple times - only add the Copy Value method if it doesn't exit
				boolean found = false;
				for (MenuItem mi : cm.getItems())
				{
					if (mi.getText().equals("Copy Value"))
					{
						found = true;
						break;
					}
				}
				
				if (!found)
				{
					// Menu item to copy cell text.
					MenuItem mi = new MenuItem("Copy Value");
					mi.setOnAction(new EventHandler<ActionEvent>()
					{
						@Override
						public void handle(ActionEvent arg0)
						{
							CustomClipboard.set(text.getText());
						}
					});
					mi.setGraphic(Images.COPY.createImageView());
					
					cm.getItems().add(mi);
				}
				
				if (type != DescriptionColumnType.LANGUAGE)
				{
					AppContext.getService(DragRegistry.class).setupDragOnly(text, new SingleConceptIdProvider()
					{
						@Override
						public String getConceptId()
						{
							return ref.getNidFetcher(type).applyAsInt(ref.getDescriptionVersion()) +"";
						}
					});
				}
			});
		});
		ProgressBar pb = new ProgressBar();
		pb.prefWidthProperty().bind(cell.widthProperty());
		return pb;
	}
	
	private void addTableColumns(DescriptionColumnType[] columns, boolean isStampColumnSet, 
			Callback<TableColumn<DescriptionVersion, DescriptionVersion>, TableCell<DescriptionVersion, DescriptionVersion>> cellFactory)
	{
		TableColumn<DescriptionVersion, DescriptionVersion> nestingParent = null;
		if (isStampColumnSet)
		{
			nestingParent = new TableColumn<>("Stamp Fields");
			descriptionsTable.getColumns().add(nestingParent);
			stampColumns.add(nestingParent);
		}
		for (DescriptionColumnType col : columns)
		{
			TableColumn<DescriptionVersion, DescriptionVersion> tc = new TableColumn<DescriptionVersion, DescriptionVersion>(col.toString());
			tc.setUserData(col);
			tc.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DescriptionVersion, DescriptionVersion>, ObservableValue<DescriptionVersion>>()
			{
				@Override
				public ObservableValue<DescriptionVersion> call(CellDataFeatures<DescriptionVersion, DescriptionVersion> param)
				{
					return new SimpleObjectProperty<DescriptionVersion>(param.getValue());
				}
			});
			tc.setCellFactory(cellFactory);
			
			//TODO Dan forgot to put comparators on these columns

			//off by default
			if (col == DescriptionColumnType.UUID)
			{
				tc.setVisible(false);
			}

			if (nestingParent == null)
			{
				descriptionsTable.getColumns().add(tc);
			}
			else
			{
				nestingParent.getColumns().add(tc);
				stampColumns.add(tc);
			}
		}
	}

	public void setConcept(ConceptVersionBI concept) throws IOException, ContradictionException
	{
		// Populate description table data model.
		conceptUUID_ = concept.getPrimordialUuid();
		refresh(concept);
	}
	
	private void refresh(ConceptVersionBI concept)
	{
		if (concept == null && conceptUUID_ == null)
		{
			LOG.info("refesh called while concept null");
			return;
		}
		Utility.execute(() ->
		{
			ArrayList<DescriptionVersionBI<?>> allDescriptions = new ArrayList<>();
			
			try
			{
				ConceptVersionBI localConcept = (concept == null ? OTFUtility.getConceptVersion(conceptUUID_) : concept);
				
				for (DescriptionChronicleBI chronicle : localConcept.getDescriptions())
				{
					for (DescriptionVersionBI<?> dv : chronicle.getVersions())
					{
						allDescriptions.add(dv);
					}
				}	
				
				//Sort the newest to the top per UUID
				Collections.sort(allDescriptions, new Comparator<DescriptionVersionBI<?>>()
				{
					@Override
					public int compare(DescriptionVersionBI<?> o1, DescriptionVersionBI<?> o2)
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
			}
			catch (Exception e)
			{
				AppContext.getCommonDialogs().showErrorDialog("Error reading relationships", e);
				LOG.error("Unexpected error reading relationships", e);
			}
			
			Platform.runLater(() ->
			{
				try
				{
					UUID lastSeenRefex = null;
					
					descriptionsTable.getItems().clear();
					for (DescriptionVersionBI<?> d : allDescriptions)
					{
						if (!showHistory_.get() && d.getPrimordialUuid().equals(lastSeenRefex))
						{
							//Only want the newest one per UUID if history isn't requested
							continue;
						}
						if (showActiveOnly_.get() == false || d.isActive())
						{
							//first one we see with a new UUID is current, others are historical
							DescriptionVersion newDescriptionVersion = new DescriptionVersion(d, !d.getPrimordialUuid().equals(lastSeenRefex));  
							descriptionsTable.getItems().add(newDescriptionVersion);
						}
						lastSeenRefex = d.getPrimordialUuid();
					}
				}
				catch (Exception e)
				{
					AppContext.getCommonDialogs().showErrorDialog("Error reading relationships", e);
					LOG.error("Unexpected error reading relationships", e);
				}
			});
		});
	}
	
	public Node getNode()
	{
		return descriptionsTable;
	}
}
