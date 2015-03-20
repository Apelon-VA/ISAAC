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
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.config.profiles.UserProfileBindings;
import gov.va.isaac.gui.refexViews.refexEdit.DynamicRefexView;
import gov.va.isaac.gui.util.CustomClipboard;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.util.OTFUtility;
import gov.va.isaac.util.UpdateableBooleanBinding;
import gov.va.isaac.util.Utility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;
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
import org.ihtsdo.otf.tcc.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.javafx.tk.Toolkit;

/**
 * {@link RelationshipTableView}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RelationshipTableView
{
	//TODO there is lots of duplicate / copy-paste code that should be condensed / reused between rel view, description view, and the dynamic refex view code.
	private static final Logger LOG = LoggerFactory.getLogger(RelationshipTableView.class);
	
	private TableView<RelationshipVersion> relationshipsTable = new TableView<>();
	private UUID conceptUUID_;
	private BooleanProperty showActiveOnly_, showHistory_, showStampColumns_;
	
	ArrayList<TableColumn<RelationshipVersion, RelationshipVersion>> stampColumns = new ArrayList<>();
	
	@SuppressWarnings("unused")
	private UpdateableBooleanBinding refreshRequiredListenerHack;

	public RelationshipTableView(BooleanProperty showStampColumns, BooleanProperty showHistory, BooleanProperty showActiveOnly)
	{
		relationshipsTable.setTableMenuButtonVisible(true);
		relationshipsTable.setMaxHeight(Double.MAX_VALUE);
		showActiveOnly_ = showActiveOnly;
		showHistory_ = showHistory;
		showStampColumns_ = showStampColumns;
		
		showStampColumns.addListener((change, oldV, newV) -> 
		{
			for (TableColumn<RelationshipVersion, RelationshipVersion> c : stampColumns)
			{
				c.setVisible(newV.booleanValue());
			}
		});
		
		refreshRequiredListenerHack = new UpdateableBooleanBinding()
		{
			{
				setComputeOnInvalidate(true);
				addBinding(AppContext.getService(UserProfileBindings.class).getViewCoordinatePath(),
						AppContext.getService(UserProfileBindings.class).getDisplayFSN(),
						AppContext.getService(UserProfileBindings.class).getStatedInferredPolicy(),
						showActiveOnly,
						showHistory);
			}

			@Override
			protected boolean computeValue()
			{
				LOG.info("Kicking off refresh() due to change of an observed user property}");
				try
				{
					refresh(null);
				}
				catch (Exception e)
				{
					LOG.error("Unexpected");
				}
				return false;
			}
		};
		
		Callback<TableColumn<RelationshipVersion, RelationshipVersion>, TableCell<RelationshipVersion, RelationshipVersion>> cellFactory =
			new Callback<TableColumn<RelationshipVersion, RelationshipVersion>, TableCell<RelationshipVersion, RelationshipVersion>>()
		{
			@Override
			public TableCell<RelationshipVersion, RelationshipVersion> call(TableColumn<RelationshipVersion, RelationshipVersion> param)
			{
				return new TableCell<RelationshipVersion, RelationshipVersion>()
				{
					@Override
					public void updateItem(final RelationshipVersion ref, boolean empty)
					{
						super.updateItem(ref, empty);

						if (!isEmpty() && ref != null)
						{
							Node graphic = null;
							ContextMenu cm = new ContextMenu();
							setContextMenu(cm);
							switch((RelationshipColumnType)getTableColumn().getUserData())
							{
								case STATUS_CONDENSED:
									
									String tooltipText = "";
									StackPane sp = new StackPane();
									sp.setPrefSize(25, 25);

									try
									{
										if (ref.getRelationshipVersion().isActive())
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
										
										if (ref.getRelationshipVersion().getTime() == Long.MAX_VALUE)
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
												drv.setComponent(ref.getRelationshipVersion().getNid(), null, null, null, true);
												
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
								case TYPE: case AUTHOR: case MODULE: case PATH: case CHARACTERISTIC: case DESTINATION: case REFINEABILITY: case SOURCE:
									graphic = backgroundLookup(this, (RelationshipColumnType)getTableColumn().getUserData(), ref);
									MenuItem mi = new MenuItem("Copy " + ((RelationshipColumnType)getTableColumn().getUserData()).toString() + " UUID");
									mi.setOnAction(new EventHandler<ActionEvent>()
									{
										@Override
										public void handle(ActionEvent ignored)
										{
											try
											{
												CustomClipboard.set(ExtendedAppContext.getDataStore().getUuidPrimordialForNid(
														ref.getNidFetcher((RelationshipColumnType)getTableColumn().getUserData()).applyAsInt(ref.getRelationshipVersion()))
														.toString());
											}
											catch (IOException e)
											{
												LOG.error("Error getting UUID for nid", e);
											}
										}
									});
									mi.setGraphic(Images.COPY.createImageView());
									cm.getItems().add(mi);
									
									mi = new MenuItem("View " + ((RelationshipColumnType)getTableColumn().getUserData()).toString() + " Concept");
									mi.setOnAction(new EventHandler<ActionEvent>()
									{
										@Override
										public void handle(ActionEvent ignored)
										{
											AppContext.getCommonDialogs().showConceptDialog(ref.getNidFetcher((RelationshipColumnType)getTableColumn().getUserData())
													.applyAsInt(ref.getRelationshipVersion()));
										}
									});
									mi.setGraphic(Images.CONCEPT_VIEW.createImageView());
									cm.getItems().add(mi);

									break;
								case TIME: case STATUS_STRING: case UUID: case GROUP:
									graphic = new Text(ref.getDisplayStrings((RelationshipColumnType)getTableColumn().getUserData()).getKey());
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
		addTableColumns(new RelationshipColumnType[] {RelationshipColumnType.STATUS_CONDENSED, RelationshipColumnType.UUID, RelationshipColumnType.SOURCE, 
				RelationshipColumnType.TYPE, RelationshipColumnType.DESTINATION, RelationshipColumnType.CHARACTERISTIC, RelationshipColumnType.GROUP, 
				RelationshipColumnType.REFINEABILITY}, 
				false, cellFactory);
		
		addTableColumns(new RelationshipColumnType[] {RelationshipColumnType.STATUS_STRING,  RelationshipColumnType.TIME, RelationshipColumnType.AUTHOR, 
				RelationshipColumnType.MODULE, RelationshipColumnType.PATH}, true, cellFactory);

		relationshipsTable.setPrefHeight(relationshipsTable.getMinHeight() + (20.0 * relationshipsTable.getItems().size()));
		relationshipsTable.setPlaceholder(new Label());
		
		for (TableColumn<RelationshipVersion, RelationshipVersion> c : stampColumns)
		{
			c.setVisible(showStampColumns_.get());
		}
		
		//Horrible hack to set a reasonable default size on the columns.
		//Min width to the width of the header column.
		Font f = new Font("System Bold", 13.0);
		for (final TableColumn<RelationshipVersion, ?> col : relationshipsTable.getColumns())
		{
			//min widths to nested col titles across the board
			for (TableColumn<RelationshipVersion, ?> nCol : col.getColumns())
			{
				nCol.setMinWidth(Toolkit.getToolkit().getFontLoader().computeStringWidth(nCol.getText(), f) + 70);
			}
			
			//and now the parent column
			if (col.getColumns().size() > 0)
			{
				FloatBinding binding = new FloatBinding()
				{
					{
						for (TableColumn<RelationshipVersion, ?> nCol : col.getColumns())
						{
							bind(nCol.widthProperty());
							bind(nCol.visibleProperty());
						}
					}
					@Override
					protected float computeValue()
					{
						float temp = 0;
						for (TableColumn<RelationshipVersion, ?> nCol : col.getColumns())
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
				
				if (text.equalsIgnoreCase(RelationshipColumnType.UUID.toString()))
				{
					col.setPrefWidth(300);
				}
				if (text.equalsIgnoreCase(RelationshipColumnType.STATUS_CONDENSED.toString()))
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
	
	private Node backgroundLookup(final TableCell<RelationshipVersion, RelationshipVersion> cell, RelationshipColumnType type, RelationshipVersion ref)
	{
		Utility.execute(() ->
		{
			String value = ref.getDisplayStrings(type).getKey();
			Platform.runLater(() ->
			{
				if (cell.getContextMenu() == null)
				{
					cell.setContextMenu(new ContextMenu());
				}
				Text text = new Text(value);
				if (cell.isEmpty() || cell.getItem() == null)
				{
					//We are updating a cell that should no longer be populated - stop!
					return;
				}
				cell.setGraphic(text);
				text.wrappingWidthProperty().bind(cell.getTableColumn().widthProperty());
				
				// Menu item to copy cell text.
				//for some reason, we get called multiple times - only add the Copy Value method if it doesn't exit
				boolean found = false;
				for (MenuItem mi : cell.getContextMenu().getItems())
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
					
					cell.getContextMenu().getItems().add(mi);
				}
			});
		});
		ProgressBar pb = new ProgressBar();
		pb.prefWidthProperty().bind(cell.widthProperty());
		return pb;
	}
	
	private void addTableColumns(RelationshipColumnType[] columns, boolean isStampColumnSet, 
			Callback<TableColumn<RelationshipVersion, RelationshipVersion>, TableCell<RelationshipVersion, RelationshipVersion>> cellFactory)
	{
		TableColumn<RelationshipVersion, RelationshipVersion> nestingParent = null;
		if (isStampColumnSet)
		{
			nestingParent = new TableColumn<>("Stamp Fields");
			relationshipsTable.getColumns().add(nestingParent);
			stampColumns.add(nestingParent);
		}
		for (RelationshipColumnType col : columns)
		{
			TableColumn<RelationshipVersion, RelationshipVersion> tc = new TableColumn<RelationshipVersion, RelationshipVersion>(col.toString());
			tc.setUserData(col);
			tc.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<RelationshipVersion, RelationshipVersion>, ObservableValue<RelationshipVersion>>()
			{
				@Override
				public ObservableValue<RelationshipVersion> call(CellDataFeatures<RelationshipVersion, RelationshipVersion> param)
				{
					return new SimpleObjectProperty<RelationshipVersion>(param.getValue());
				}
			});
			tc.setCellFactory(cellFactory);

			//off by default
			if (col == RelationshipColumnType.UUID || col == RelationshipColumnType.SOURCE)
			{
				tc.setVisible(false);
			}

			if (nestingParent == null)
			{
				relationshipsTable.getColumns().add(tc);
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
			ArrayList<RelationshipVersionBI<?>> allRelationships = new ArrayList<>();
			try
			{
				ConceptVersionBI localConcept = (concept == null ? OTFUtility.getConceptVersion(conceptUUID_) : concept);
				
				for (RelationshipChronicleBI chronicle : localConcept.getRelationshipsOutgoing())
				{
					for (RelationshipVersionBI<?> rv : chronicle.getVersions())
					{
						allRelationships.add(rv);
					}
				}	
				
				//Sort the newest to the top per UUID
				Collections.sort(allRelationships, new Comparator<RelationshipVersionBI<?>>()
				{
					@Override
					public int compare(RelationshipVersionBI<?> o1, RelationshipVersionBI<?> o2)
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
					
					relationshipsTable.getItems().clear();
					for (RelationshipVersionBI<?> r : allRelationships)
					{
						if (!showHistory_.get() && r.getPrimordialUuid().equals(lastSeenRefex))
						{
							//Only want the newest one per UUID if history isn't requested
							continue;
						}
						if (showActiveOnly_.get() == false || r.isActive())
						{
							//first one we see with a new UUID is current, others are historical
							RelationshipVersion newRelationshipVersion = new RelationshipVersion(r, !r.getPrimordialUuid().equals(lastSeenRefex));  
							relationshipsTable.getItems().add(newRelationshipVersion);
						}
						lastSeenRefex = r.getPrimordialUuid();
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
		return relationshipsTable;
	}
}
