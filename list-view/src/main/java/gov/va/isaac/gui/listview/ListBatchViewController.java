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
package gov.va.isaac.gui.listview;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.gui.ConceptNode;
import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.gui.util.ErrorMarkerUtils;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.util.UpdateableBooleanBinding;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.WBUtility;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ListBatchViewController}
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class ListBatchViewController
{
	@FXML private Button loadListButton;
	@FXML private VBox operationsList;
	@FXML private Button clearOperationsButton;
	@FXML private Tab batchResultsTab;
	@FXML private TableView<SimpleDisplayConcept> conceptTable;
	@FXML private HBox conceptTableFooter;
	@FXML private Button addUncommittedListButton;
	@FXML private Button addOperationButton;
	@FXML private Button clearListButton;
	@FXML private Button executeOperationsButton;
	@FXML private ToolBar executeOperationsToolbar;
	@FXML private Button saveListButton;
	@FXML private AnchorPane rootPane;

	private UpdateableBooleanBinding allOperationsReady_;
	StringProperty reasonWhyExecuteDisabled_ = new SimpleStringProperty();
	private Logger logger_ = LoggerFactory.getLogger(this.getClass());

	protected static ListBatchViewController init() throws IOException
	{
		// Load from FXML.
		URL resource = ListBatchViewController.class.getResource("ListBatchView.fxml");
		FXMLLoader loader = new FXMLLoader(resource);
		loader.load();
		return loader.getController();
	}

	@FXML
	public void initialize()
	{
		operationsList.getChildren().add(new OperationNode(this));
		
		final ConceptNode cn = new ConceptNode(null, false);
		cn.setPromptText("Type or select concept to add");
		HBox.setHgrow(cn.getNode(), Priority.SOMETIMES);
		HBox.setMargin(cn.getNode(), new Insets(6, 5, 6, 5));
		conceptTableFooter.getChildren().add(cn.getNode());
		
		cn.getConceptProperty().addListener(new ChangeListener<ConceptVersionBI>()
		{
			@Override
			public void changed(ObservableValue<? extends ConceptVersionBI> observable, ConceptVersionBI oldValue, ConceptVersionBI newValue)
			{
				if (newValue != null)
				{
					conceptTable.getItems().add(new SimpleDisplayConcept(newValue));
					cn.clear();
				}
			}
		});

		conceptTable.setPlaceholder(new Label("Drop Concepts Here"));
		TableColumn<SimpleDisplayConcept, String> col1 = new TableColumn<SimpleDisplayConcept, String>();
		col1.setText("Concept");
		col1.prefWidthProperty().bind(conceptTable.widthProperty().subtract(3.0));
		col1.setCellValueFactory(new PropertyValueFactory<SimpleDisplayConcept, String>("description"));
		conceptTable.getColumns().add(col1);
		conceptTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		conceptTable.setOnKeyReleased(new EventHandler<KeyEvent>()
		{
			@Override
			public void handle(KeyEvent event)
			{
				if (event.getCode() == KeyCode.DELETE)
				{
					for (SimpleDisplayConcept sdc : conceptTable.getSelectionModel().getSelectedItems())
					{
						conceptTable.getItems().remove(sdc);
					}
					conceptTable.getSelectionModel().clearSelection();
				}
			}
		});
		
		conceptTable.setRowFactory(new Callback<TableView<SimpleDisplayConcept>, TableRow<SimpleDisplayConcept>>()
		{
			@Override
			public TableRow<SimpleDisplayConcept> call(TableView<SimpleDisplayConcept> param)
			{
				final TableRow<SimpleDisplayConcept> row = new TableRow<SimpleDisplayConcept>();
				final ContextMenu rowMenu = new ContextMenu();
				MenuItem viewItem = new MenuItem("View Concept");
				viewItem.setGraphic(Images.CONCEPT_VIEW.createImageView());
				viewItem.setOnAction(new EventHandler<ActionEvent>()
				{
					@Override
					public void handle(ActionEvent event)
					{
						AppContext.getCommonDialogs().showConceptDialog(row.getItem().getNid());
					}
				});
				MenuItem removeItem = new MenuItem("Delete");
				removeItem.setGraphic(Images.DELETE.createImageView());
				removeItem.setOnAction(new EventHandler<ActionEvent>()
				{
					@Override
					public void handle(ActionEvent event)
					{
						if (conceptTable.getSelectionModel().getSelectedItems().size() > 1)
						{
							for (SimpleDisplayConcept sdc : conceptTable.getSelectionModel().getSelectedItems())
							{
								conceptTable.getItems().remove(sdc);
							}
							conceptTable.getSelectionModel().clearSelection();
						}
						else
						{
							conceptTable.getItems().remove(row.getItem());
						}
					}
				});
				rowMenu.getItems().addAll(viewItem, removeItem);

				// only display context menu for non-null items:
				row.contextMenuProperty().bind(Bindings.when(Bindings.isNotNull(row.itemProperty())).then(rowMenu).otherwise((ContextMenu) null));
				return row;
			}
		});

		//remove it, wrap it, readd it.
		executeOperationsToolbar.getItems().remove(executeOperationsButton);
		executeOperationsToolbar.getItems().add(ErrorMarkerUtils.setupDisabledInfoMarker(executeOperationsButton, reasonWhyExecuteDisabled_));
		
		executeOperationsButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				//TODO implement
				logger_.error("Not yet implemented");
			}
		});

		addOperationButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				OperationNode operationNode = new OperationNode(ListBatchViewController.this);
				operationsList.getChildren().add(operationNode);
				allOperationsReady_.addBinding(operationNode.isReadyForExecution());
				allOperationsReady_.invalidate();
			}
		});

		clearOperationsButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				operationsList.getChildren().clear();
				allOperationsReady_.clearBindings();
				allOperationsReady_.invalidate();
			}
		});

		clearListButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				conceptTable.getItems().clear();
			}
		});

		saveListButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				//TODO implement
				logger_.error("Not yet implemented");
			}
		});
		
		loadListButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				//TODO implement
				logger_.error("Not yet implemented");
			}
		});
		
		addUncommittedListButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				addUncommittedListButton.setDisable(true);
				Runnable r = new Runnable()
				{
					@Override
					public void run()
					{
						final ArrayList<SimpleDisplayConcept> concepts = new ArrayList<>();
						for (ConceptChronicleBI c : ExtendedAppContext.getDataStore().getUncommittedConcepts())
						{
							try
							{
								concepts.add(new SimpleDisplayConcept(WBUtility.getDescription(c.getVersion(WBUtility.getViewCoordinate())), c.getNid()));
							}
							catch (ContradictionException e)
							{
								logger_.error("error adding uncommited concept '" + c + "'", e);
							}
						}
						Platform.runLater(new Runnable()
						{
							
							@Override
							public void run()
							{
								conceptTable.getItems().addAll(concepts);
								addUncommittedListButton.setDisable(false);
							}
						});
					}
				};
				Utility.execute(r);
			}
		});
		
		allOperationsReady_ = new UpdateableBooleanBinding()
		{
			@Override
			protected boolean computeValue()
			{
				for (Node n : operationsList.getChildren())
				{
					if (n instanceof OperationNode)
					{
						OperationNode on = (OperationNode)n;
						if (!on.isReadyForExecution().get())
						{
							reasonWhyExecuteDisabled_.set("All errors must be corrected in the Operations List before execution");
							return false;
						}
					}
					else
					{
						logger_.error("Design error - Node isn't an operationNode!");
					}
				}
				if (operationsList.getChildren().size() == 0)
				{
					reasonWhyExecuteDisabled_.set("At least one operation must be specified");
					return false;
				}
				return true;
			}
		};
		
		for (Node n : operationsList.getChildren())
		{
			if (n instanceof OperationNode)
			{
				allOperationsReady_.addBinding(((OperationNode) n).isReadyForExecution());
			}
			else
			{
				logger_.error("Design error - Node isn't an operationNode!");
			}
		}
		allOperationsReady_.invalidate();
		
		executeOperationsButton.disableProperty().bind(allOperationsReady_.not());
	}

	protected void remove(OperationNode node)
	{
		if (!operationsList.getChildren().remove(node))
		{
			logger_.error("Unexpected error removing operation item");
		}
		allOperationsReady_.removeBinding(node.isReadyForExecution());
		allOperationsReady_.invalidate();
	}
	
	protected ObservableList<SimpleDisplayConcept> getConceptList()
	{
		return conceptTable.getItems();
	}

	public AnchorPane getRoot()
	{
		return rootPane;
	}
}
