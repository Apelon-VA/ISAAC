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
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.legoEdit.gui.legoFilterPane;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.LookAheadConceptPopup;
import gov.va.isaac.gui.dragAndDrop.DragRegistry;
import gov.va.isaac.gui.util.ExpandedNode;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.util.AlphanumComparator;
import gov.va.isaac.util.ConceptLookupCallback;
import gov.va.isaac.util.TaskCompleteCallback;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.WBUtility;
import gov.va.legoEdit.gui.legoListTreeView.LegoListTreeItem;
import gov.va.legoEdit.gui.legoListTreeView.LegoListTreeView;
import gov.va.legoEdit.gui.legoListView.LegoListView;
import gov.va.legoEdit.gui.legoTreeView.LegoTreeItem;
import gov.va.legoEdit.gui.legoTreeView.LegoTreeItemComparator;
import gov.va.legoEdit.gui.legoTreeView.LegoTreeNodeType;
import gov.va.legoEdit.model.LegoListByReference;
import gov.va.legoEdit.model.LegoReference;
import gov.va.legoEdit.model.ModelUtil;
import gov.va.legoEdit.model.SchemaToString;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.LegoList;
import gov.va.legoEdit.model.schemaModel.Pncs;
import gov.va.legoEdit.storage.AdvancedLegoFilter;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import gov.va.legoEdit.storage.CloseableIterator;
import gov.va.legoEdit.storage.wb.LegoWBUtility;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import javafx.scene.control.IndexedCell;

/**
 * 
 * {@link LegoFilterPaneController}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@SuppressWarnings("restriction")
public class LegoFilterPaneController implements Initializable, ConceptLookupCallback, TaskCompleteCallback
{
	public static String ANY = "-ANY-";
	public static String DISCERNIBLE = "Discernible";
	public static String QUALIFIER = "Qualifier";
	public static String VALUE = "Value";
	public static String IS = "is";
	public static String CHILD_OF = "Child of";
	
	private final Integer CONCEPT_LOOKUP = 0;
	private final Integer CONCEPT_REL_TYPE_LOOKUP = 1;
	private final Integer CONCEPT_REL_DEST_LOOKUP = 2;
	
	@FXML// fx:id="advancedButton"
	private ToggleButton advancedButton; // Value injected by FXMLLoader
	@FXML// fx:id="advancedDestConcept"
	private TextField advancedDestConcept; // Value injected by FXMLLoader
	@FXML// fx:id="advancedDestConceptStack"
	private StackPane advancedDestConceptStack; // Value injected by FXMLLoader
	@FXML// fx:id="advancedDestMatchType"
	private ComboBox<String> advancedDestMatchType; // Value injected by FXMLLoader
	@FXML// fx:id="advancedPanel"
	private VBox advancedPanel; // Value injected by FXMLLoader
	@FXML// fx:id="advancedRelType"
	private TextField advancedRelType; // Value injected by FXMLLoader
	@FXML// fx:id="advancedRelTypeStack"
	private StackPane advancedRelTypeStack; // Value injected by FXMLLoader
	@FXML// fx:id="advancedTypeLegoPart"
	private ComboBox<String> advancedTypeLegoPart; // Value injected by FXMLLoader
	@FXML// fx:id="borderPane"
	private BorderPane borderPane; // Value injected by FXMLLoader
	@FXML// fx:id="borderPaneTopVbox"
	private VBox borderPaneTopVbox; // Value injected by FXMLLoader
	@FXML// fx:id="clearButton"
	private Button clearButton; // Value injected by FXMLLoader
	@FXML// fx:id="pncsItem"
	private ComboBox<PncsItem> pncsItem; // Value injected by FXMLLoader
	@FXML// fx:id="pncsNameOrId"
	private ComboBox<String> pncsNameOrId; // Value injected by FXMLLoader
	@FXML// fx:id="pncsValue"
	private ComboBox<String> pncsValue; // Value injected by FXMLLoader
	@FXML// fx:id="snomedId"
	private TextField snomedId; // Value injected by FXMLLoader
	@FXML// fx:id="snomedIdStack"
	private StackPane snomedIdStack; // Value injected by FXMLLoader
	@FXML// fx:id="labelPncsValue"
	private Label labelPncsValue; // Value injected by FXMLLoader
	@FXML// fx:id="labelRelLocation"
	private Label labelRelLocation; // Value injected by FXMLLoader
	@FXML// fx:id="labelAdvancedActive"
	private Label labelAdvancedActive; // Value injected by FXMLLoader
	@FXML// fx:id="labelNoResults"
	private Label labelNoResults; // Value injected by FXMLLoader
	@FXML //  fx:id="listUpdatePI"
	private ProgressIndicator listUpdatePI; // Value injected by FXMLLoader

	private LegoListTreeView lltv_;
	
	private ConceptGUIInfo[] conceptInfo_ = new ConceptGUIInfo[3]; 

	private volatile AtomicInteger updateDisabled = new AtomicInteger(0);  // Update will only run when this is 0
	private AtomicInteger updateRunningCount = new AtomicInteger(0);
	private BooleanBinding isUpdateRunning = new BooleanBinding()
	{
		@Override
		protected boolean computeValue()
		{
			return updateRunningCount.get() > 0;
		}
	};

	Logger logger = LoggerFactory.getLogger(LegoFilterPaneController.class);
	
	private class ConceptGUIInfo
	{
		TextField tf;
		Concept concept;
		Tooltip tooltip;
		BooleanProperty isValid;
		ProgressIndicator pi;
		volatile long lookupUpdateTime;
		AtomicInteger lookupsInProgress = new AtomicInteger();
		BooleanBinding isLookupInProgress = new BooleanBinding()
		{
			@Override
			protected boolean computeValue()
			{
				return lookupsInProgress.get() > 0;
			}
		};
	}

	public static LegoFilterPaneController init()
	{
		try
		{
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(LegoFilterPaneController.class.getResource("LegoFilterPane.fxml"));
			loader.load(LegoFilterPaneController.class.getResourceAsStream("LegoFilterPane.fxml"));
			return loader.getController();
		}
		catch (IOException e)
		{
			throw new RuntimeException("Unexpected", e);
		}
	}

	@Override
	// This method is called by the FXMLLoader when initialization is complete
	public void initialize(URL fxmlFileLocation, ResourceBundle resources)
	{
		assert advancedButton != null : "fx:id=\"advancedButton\" was not injected: check your FXML file 'LegoFilterPane.fxml'.";
		assert advancedDestConcept != null : "fx:id=\"advancedDestConcept\" was not injected: check your FXML file 'LegoFilterPane.fxml'.";
		assert advancedDestConceptStack != null : "fx:id=\"advancedDestConceptStack\" was not injected: check your FXML file 'LegoFilterPane.fxml'.";
		assert advancedDestMatchType != null : "fx:id=\"advancedDestMatchType\" was not injected: check your FXML file 'LegoFilterPane.fxml'.";
		assert advancedPanel != null : "fx:id=\"advancedPanel\" was not injected: check your FXML file 'LegoFilterPane.fxml'.";
		assert advancedRelType != null : "fx:id=\"advancedRelType\" was not injected: check your FXML file 'LegoFilterPane.fxml'.";
		assert advancedRelTypeStack != null : "fx:id=\"advancedRelTypeStack\" was not injected: check your FXML file 'LegoFilterPane.fxml'.";
		assert advancedTypeLegoPart != null : "fx:id=\"advancedTypeLegoPart\" was not injected: check your FXML file 'LegoFilterPane.fxml'.";
		assert borderPane != null : "fx:id=\"borderPane\" was not injected: check your FXML file 'LegoFilterPane.fxml'.";
		assert borderPaneTopVbox != null : "fx:id=\"borderPaneTopVbox\" was not injected: check your FXML file 'LegoFilterPane.fxml'.";
		assert clearButton != null : "fx:id=\"clearButton\" was not injected: check your FXML file 'LegoFilterPane.fxml'.";
		assert pncsItem != null : "fx:id=\"pncsItem\" was not injected: check your FXML file 'LegoFilterPane.fxml'.";
		assert pncsNameOrId != null : "fx:id=\"pncsNameOrId\" was not injected: check your FXML file 'LegoFilterPane.fxml'.";
		assert pncsValue != null : "fx:id=\"pncsValue\" was not injected: check your FXML file 'LegoFilterPane.fxml'.";
		assert snomedId != null : "fx:id=\"snomedId\" was not injected: check your FXML file 'LegoFilterPane.fxml'.";
		assert snomedIdStack != null : "fx:id=\"snomedIdStack\" was not injected: check your FXML file 'LegoFilterPane.fxml'.";

		// initialize your logic here: all @FXML variables will have been injected
		
		borderPane.getStylesheets().add(LegoFilterPaneController.class.getResource("/isaac-shared-styles.css").toString());

		lltv_ = new LegoListTreeView();
		((StackPane)borderPane.getCenter()).getChildren().add(0, lltv_);
		AnchorPane.setBottomAnchor(borderPane, 0.0);
		AnchorPane.setTopAnchor(borderPane, 0.0);
		AnchorPane.setLeftAnchor(borderPane, 0.0);
		AnchorPane.setRightAnchor(borderPane, 0.0);

		pncsValue.setDisable(true);
		labelPncsValue.setDisable(true);

		pncsNameOrId.getItems().add("Name");
		pncsNameOrId.getItems().add("Id");
		pncsNameOrId.getSelectionModel().select(0);

		pncsNameOrId.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				updateDisabled.incrementAndGet();
				// UGLY HACK cause I can't figure out how to tell the combo box that the value of the items it is displaying changed.
				PncsItem selected = pncsItem.getSelectionModel().getSelectedItem();
				ObservableList<PncsItem> items = FXCollections.observableArrayList();
				items.addAll(pncsItem.getItems());
				pncsItem.getItems().clear();
				pncsItem.getItems().addAll(items);
				FXCollections.sort(pncsItem.getItems(), new PncsItemComparator());
				pncsItem.getSelectionModel().select(selected);
				updateDisabled.decrementAndGet();
			}
		});

		loadPncs();

		pncsItem.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				PncsItem item = pncsItem.getValue();
				if (item != null)
				{
					updateDisabled.incrementAndGet();
					if (item.getName().equals(ANY))
					{
						labelPncsValue.setDisable(true);
						pncsValue.setDisable(true);
						pncsValue.getItems().clear();
					}
					else
					{
						labelPncsValue.setDisable(false);
						pncsValue.setDisable(false);
						pncsValue.getItems().clear();
						List<Pncs> items = BDBDataStoreImpl.getInstance().getPncs(pncsItem.getValue().getId());
						for (Pncs pncs : items)
						{
							pncsValue.getItems().add(pncs.getValue());
						}
						FXCollections.sort(pncsValue.getItems(), new AlphanumComparator(true));
						pncsValue.getItems().add(0, ANY);
						pncsValue.getSelectionModel().select(0);
					}
					updateDisabled.decrementAndGet();
					updateLegoList();
				}
			}
		});

		pncsValue.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				updateLegoList();
			}
		});

		setupConceptField(snomedId, snomedIdStack, CONCEPT_LOOKUP);
		setupConceptField(advancedRelType, advancedRelTypeStack, CONCEPT_REL_TYPE_LOOKUP);
		setupConceptField(advancedDestConcept, advancedDestConceptStack, CONCEPT_REL_DEST_LOOKUP);		

		clearButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				clearFilter();
			}
		});
	
		advancedButton.setSelected(false);
		advancedButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				toggleAdvanced();
			}
		});
		toggleAdvanced();
		
		advancedTypeLegoPart.getItems().add(ANY);
		advancedTypeLegoPart.getItems().add(DISCERNIBLE);
		advancedTypeLegoPart.getItems().add(QUALIFIER);
		advancedTypeLegoPart.getItems().add(VALUE);
		advancedTypeLegoPart.getSelectionModel().select(0);
		
		advancedDestMatchType.getItems().add(IS);
		advancedDestMatchType.getItems().add(CHILD_OF);
		advancedDestMatchType.getSelectionModel().select(0);
		
		BooleanBinding enableAdvancedLegoRelLocation = new BooleanBinding()
		{
			{
				bind(advancedDestConcept.textProperty(), advancedRelType.textProperty());
			}
			
			@Override
			protected boolean computeValue()
			{
				if (advancedDestConcept.textProperty().get().length() > 0 || advancedRelType.textProperty().get().length() > 0)
				{
					return true;
				}
				else
				{
					return false;
				}
			}
		};

		advancedDestMatchType.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				updateLegoList();
			}
		});
		
		advancedTypeLegoPart.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				updateLegoList();
			}
		});

		labelRelLocation.disableProperty().bind(enableAdvancedLegoRelLocation.not());
		advancedTypeLegoPart.disableProperty().bind(enableAdvancedLegoRelLocation.not());
		listUpdatePI.visibleProperty().bind(isUpdateRunning);
		labelAdvancedActive.setText(SchemaToString.rightArrow);
		labelAdvancedActive.visibleProperty().bind(enableAdvancedLegoRelLocation);

		isUpdateRunning.addListener(new InvalidationListener()
		{
			@Override
			public void invalidated(Observable observable)
			{
				if (isUpdateRunning.get())
				{
					labelNoResults.setVisible(false);
				}
				else if (lltv_.getRoot().getChildren().size() == 1) //there is always a blank node tacked on the end
				{
					labelNoResults.setVisible(true);
				}
				else
				{
					labelNoResults.setVisible(false);
				}
			}
		});
		
		updateLegoList();
	}
	
	private void setupConceptField(final TextField textField, StackPane stack, final Integer conceptId)
	{
		final ConceptGUIInfo info = new ConceptGUIInfo();
		conceptInfo_[conceptId] = info;
		
		info.concept = null;
		info.isValid = new SimpleBooleanProperty(true);
		info.lookupUpdateTime = 0;
		info.pi = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
		info.pi.visibleProperty().bind(info.isLookupInProgress);
		info.pi.setPrefHeight(16.0);
		info.pi.setPrefWidth(16.0);
		info.pi.setMaxWidth(16.0);
		info.pi.setMaxHeight(16.0);
		info.tf = textField;
		info.tooltip = new Tooltip("The specified concept was not found in the Snomed Database.");
		
		ImageView lookupFailImage = Images.EXCLAMATION.createImageView();
		lookupFailImage.visibleProperty().bind(info.isValid.not());
		Tooltip.install(lookupFailImage, info.tooltip);
		
		stack.getChildren().add(info.pi);
		StackPane.setAlignment(info.pi, Pos.CENTER_RIGHT);
		StackPane.setMargin(info.pi, new Insets(0.0, 5.0, 0.0, 0.0));
		stack.getChildren().add(lookupFailImage);
		StackPane.setAlignment(lookupFailImage, Pos.CENTER_RIGHT);
		StackPane.setMargin(lookupFailImage, new Insets(0.0, 5.0, 0.0, 0.0));
		
		AppContext.getService(DragRegistry.class).setupDragAndDrop(textField, () -> info.concept.getUuid(), true);
		
		textField.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
			{
				if (newValue.length() == 0)
				{
					info.isValid.set(true);
					info.tf.setTooltip(null);
					info.concept = null;
					info.lookupUpdateTime = System.currentTimeMillis();
					updateLegoList();
				}
				else if (newValue.length() > 0)
				{
					if (updateDisabled.get() > 0)
					{
						return;
					}
					info.lookupsInProgress.incrementAndGet();
					info.isLookupInProgress.invalidate();
					Object o = info.tf.getUserData();
					info.tf.setUserData(null);  //Clear it back out.
					if (o != null && o instanceof Integer)
					{
						WBUtility.getConceptVersion((Integer)o, LegoFilterPaneController.this, conceptId);
					}
					else
					{
						WBUtility.lookupIdentifier(newValue, LegoFilterPaneController.this, conceptId);
					}
				}
			}
		});
		new LookAheadConceptPopup(textField);
	}

	private void toggleAdvanced()
	{
		if (advancedButton.isSelected())
		{
			if (borderPaneTopVbox.getChildren().get(1) != advancedPanel)
			{
				borderPaneTopVbox.getChildren().add(1, advancedPanel);
			}
		}
		else if (borderPaneTopVbox.getChildren().get(1) == advancedPanel)
		{
			borderPaneTopVbox.getChildren().remove(1);
		}
	}

	public synchronized void filterOnConcept(String conceptId)
	{
		updateDisabled.incrementAndGet();
		clearFilter();
		updateDisabled.decrementAndGet();
		snomedId.setText(conceptId);
		AppContext.getMainApplicationWindow().ensureDockedViewIsVisble(AppContext.getService(LegoListView.class));
	}
	
	public synchronized void filterOnPncs(String pncsName, String pncsValue)
	{
		updateDisabled.incrementAndGet();
		clearFilter();
		pncsNameOrId.getSelectionModel().select(0);
		for (PncsItem item : pncsItem.getItems())
		{
			if (item.name.equals(pncsName))
			{
				pncsItem.getSelectionModel().select(item);
				break;
			}
		}
		if (pncsValue != null && !this.pncsValue.isDisable())
		{
			for (String s : this.pncsValue.getItems())
			{
				if (s.equals(pncsValue))
				{
					this.pncsValue.getSelectionModel().select(s);
					break;
				}
			}
		}
		updateDisabled.decrementAndGet();
		updateLegoList();
		AppContext.getMainApplicationWindow().ensureDockedViewIsVisble(AppContext.getService(LegoListView.class));
	}

	private void clearFilter()
	{
		updateDisabled.incrementAndGet();
		pncsItem.getSelectionModel().select(0);
		snomedId.setText("");
		snomedId.setTooltip(null);
		advancedTypeLegoPart.getSelectionModel().select(0);
		advancedDestMatchType.getSelectionModel().select(0);
		advancedRelType.setText("");
		advancedDestConcept.setText("");
		updateDisabled.decrementAndGet();
		updateLegoList();
	}

	public void updateLegoList()
	{
		if (updateDisabled.get() > 0)
		{
			return;
		}

		updateRunningCount.incrementAndGet();
		isUpdateRunning.invalidate();
		Integer pncsFilterId = null;
		String pncsFilterValue = null;
		if (!pncsItem.getSelectionModel().getSelectedItem().getName().equals(ANY))
		{
			pncsFilterId = pncsItem.getSelectionModel().getSelectedItem().getId();
		}
		if (!pncsValue.isDisable() && !pncsValue.getSelectionModel().getSelectedItem().equals(ANY))
		{
			pncsFilterValue = pncsValue.getSelectionModel().getSelectedItem();
		}
		
		String advancedRelFilterLegoPart = null;
		if (!advancedTypeLegoPart.getSelectionModel().getSelectedItem().equals(ANY))
		{
			advancedRelFilterLegoPart = advancedTypeLegoPart.getSelectionModel().getSelectedItem();
		}
		String advancedDestMatchTypePart = advancedDestMatchType.getSelectionModel().getSelectedItem();

		lltv_.getSelectionModel().clearSelection();
		initializeLegoListNames(pncsFilterId, pncsFilterValue, conceptInfo_[CONCEPT_LOOKUP].concept, 
				advancedRelFilterLegoPart, conceptInfo_[CONCEPT_REL_TYPE_LOOKUP].concept, advancedDestMatchTypePart, 
				conceptInfo_[CONCEPT_REL_DEST_LOOKUP].concept, this);
	}
	
	public LegoTreeItem getCurrentSelection()
	{
		return (LegoTreeItem)lltv_.getSelectionModel().getSelectedItem();
	}

	public BorderPane getBorderPane()
	{
		return borderPane;
	}

	public void reloadOptions()
	{
		updateDisabled.incrementAndGet();
		loadPncs();
		updateDisabled.decrementAndGet();
	}

	private void loadPncs()
	{
		CloseableIterator<Pncs> iterator = BDBDataStoreImpl.getInstance().getPncs();
		HashMap<String, PncsItem> unique = new HashMap<>();
		while (iterator.hasNext())
		{
			Pncs pncs = iterator.next();
			unique.put(pncs.getName() + pncs.getId(), new PncsItem(pncs, pncsNameOrId.valueProperty()));
		}
		PncsItem selectedName = pncsItem.getSelectionModel().getSelectedItem();
		String selectedValue = pncsValue.getSelectionModel().getSelectedItem();
		pncsItem.getItems().clear();
		pncsItem.getItems().addAll(new PncsItem(ANY, -1, pncsNameOrId.valueProperty()));
		pncsItem.getItems().addAll(unique.values());
		FXCollections.sort(pncsItem.getItems(), new PncsItemComparator());
		if (selectedName != null)
		{
			pncsItem.getSelectionModel().select(selectedName);
			if (selectedValue != null)
			{
				pncsValue.getSelectionModel().select(selectedValue);
			}
		}
		else
		{
			pncsItem.getSelectionModel().select(0);
		}
	}

	@Override
	public void lookupComplete(final ConceptVersionBI concept, final long submitTime, final Integer callId)
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				conceptInfo_[callId].lookupsInProgress.decrementAndGet();
				conceptInfo_[callId].isLookupInProgress.invalidate();

				if (submitTime < conceptInfo_[callId].lookupUpdateTime)
				{
					// Throw it away, we already got back a newer lookup.
					logger.debug("throwing away a lookup");
					return;
				}
				else
				{
					conceptInfo_[callId].lookupUpdateTime = submitTime;
				}

				conceptInfo_[callId].concept = (concept ==  null ? null : LegoWBUtility.convertConcept(concept));
				
				if (concept != null)
				{
					updateDisabled.incrementAndGet();
					conceptInfo_[callId].tf.setText(conceptInfo_[callId].concept.getDesc());
					conceptInfo_[callId].tf.setTooltip(new Tooltip(conceptInfo_[callId].concept.getDesc() + " " 
							+ (conceptInfo_[callId].concept.getSctid() != null ? conceptInfo_[callId].concept.getSctid() : "")));
					updateDisabled.decrementAndGet();
					conceptInfo_[callId].isValid.set(true);
					updateLegoList();
				}
				else
				{
					conceptInfo_[callId].isValid.set(false);
					conceptInfo_[callId].tooltip.setText("The specified concept was not found in the Snomed Database, and this value is not being used in the filter.");
				}
			}
		});
	}

	@Override
	public void taskComplete(long startTime, Integer taskId)
	{
		updateRunningCount.decrementAndGet();
		isUpdateRunning.invalidate();
	}
	
	/**
	 * 
	 * Assumed to be called on the FXThread, but does the work in the background.  
	 */
	public void initializeLegoListNames(final Integer pncsFilterId, final String pncsFilterValue, final Concept conceptFilter, 
			final String relAppliesToLegoSection, final Concept relTypeFilter, final String destTypeFilter, final Concept destFilter, 
			final TaskCompleteCallback callback)
	{
		final long startTime = System.currentTimeMillis();
		final ExpandedNode before = ExpandedNode.buildExpandedNodeHierarchy(lltv_.getRoot());
		ObservableList<TreeItem<String>> legoLists =  lltv_.getRoot().getChildren();
		legoLists.clear();
		
		int targetScrollPos = 0;
		//Now this is a hack.... https://javafx-jira.kenai.com/browse/RT-18965
		if (lltv_.getChildrenUnmodifiable().size() > 0)
		{
			VirtualFlow<?> vf = (VirtualFlow<?>) lltv_.getChildrenUnmodifiable().get(0);
			IndexedCell<?> first = vf.getFirstVisibleCell();
			if (first != null)
			{
				targetScrollPos = first.getIndex();
			}
		}
		
		final int scrollTo = (targetScrollPos > 0 ? targetScrollPos : 0);
		
		Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				ArrayList<LegoReference> legos = new ArrayList<>();
				final HashMap<String, LegoListByReference> legoListMap = new HashMap<>();
		
				if (pncsFilterId == null)
				{
					// no concept filter
					if (conceptFilter == null && relTypeFilter == null && destFilter == null)
					{
						// No usable filter - get all - shortcircut the lego processing, just get the lists directly.
						Iterator<LegoList> ll = BDBDataStoreImpl.getInstance().getLegoLists();
						while (ll.hasNext())
						{
							// Throw away the legos, just keep the refs.
							LegoList legoList = ll.next();
							legoListMap.put(legoList.getLegoListUUID(), new LegoListByReference(legoList, false));
						}
					}
					else
					{
						boolean alreadyProcessed = false;
						if (conceptFilter != null)
						{
							legos.addAll(LegoReference.convert(BDBDataStoreImpl.getInstance().getLegosContainingConceptIdentifiers(conceptFilter.getSctid() + "",
								conceptFilter.getUuid())));
							
						}
						else if (relTypeFilter != null)
						{
							//start by matching legos anywhere, narrow down below.
							legos.addAll(LegoReference.convert(BDBDataStoreImpl.getInstance().getLegosContainingConceptIdentifiers(relTypeFilter.getSctid() + "",
									relTypeFilter.getUuid())));
						}
						else
						{
							if (LegoFilterPaneController.IS.equals(destTypeFilter))
							{
								//start by matching legos anywhere, narrow down below.
								legos.addAll(LegoReference.convert(BDBDataStoreImpl.getInstance().getLegosContainingConceptIdentifiers(destFilter.getSctid() + "",
									destFilter.getUuid())));
							}
							else
							{
								//with only a child-of, we don't have a shortcut for the starting point.  Just have to get all legos.
								alreadyProcessed = true;
								legos.addAll(AdvancedLegoFilter.findMatchingRelTypes(BDBDataStoreImpl.getInstance().getLegos(), relTypeFilter, destFilter, 
										destTypeFilter, relAppliesToLegoSection));
							}
						}
						
						//Now apply each of the relation filters, removing things that don't match.
						if (!alreadyProcessed)
						{
							AdvancedLegoFilter.removeNonMatchingRelType(legos, relTypeFilter, destFilter, destTypeFilter, relAppliesToLegoSection);
						}
					}
				}
				else
				{
					// we have a pncs filter - start there.
					if (pncsFilterValue == null)
					{
						// id filter, no value
						legos.addAll(LegoReference.convert(BDBDataStoreImpl.getInstance().getLegosForPncs(pncsFilterId)));
					}
					else
					{
						// id filter and value
						legos.addAll(LegoReference.convert(BDBDataStoreImpl.getInstance().getLegosForPncs(pncsFilterId, pncsFilterValue)));
					}
		
					if (conceptFilter != null)
					{
						// Need to remove any legos that don't match the concept filter
						HashSet<String> conceptLegoKeys = new HashSet<>();
						List<Lego> conceptLegos = BDBDataStoreImpl.getInstance().getLegosContainingConceptIdentifiers(conceptFilter.getSctid() + "", conceptFilter.getUuid());
						for (Lego l : conceptLegos)
						{
							conceptLegoKeys.add(ModelUtil.makeUniqueLegoID(l));
						}
						Iterator<LegoReference> iterator = legos.iterator();
						while (iterator.hasNext())
						{
							if (!conceptLegoKeys.contains(iterator.next().getUniqueId()))
							{
								iterator.remove();
							}
						}
					}
					AdvancedLegoFilter.removeNonMatchingRelType(legos, relTypeFilter, destFilter, destTypeFilter, relAppliesToLegoSection);
				}
				// Don't filter unsaved legos - always include them.
				//TODO [Lego edit]
//				UnsavedLegos unsavedLegos = LegoGUI.getInstance().getLegoGUIController().getUnsavedLegos();
//				legos.addAll(LegoReference.convert(unsavedLegos.getLegos(), true));

				if (legos.size() > 0)
				{
					// Need to work backwards from the legos now, and get the legoListRefs - and wire them back together.
					assert legoListMap.size() == 0;

					for (LegoReference lr : legos)
					{
						// Could be more than one, usually only one in practice, however
						List<String> legoListIds = BDBDataStoreImpl.getInstance().getLegoListByLego(lr.getLegoUUID());

						// Might also be from the new list...
//						String id = unsavedLegos.getLegoListIdForLego(lr.getUniqueId());
//						if (id != null)
//						{
//							legoListIds.add(id);
//						}

						for (String legoListId : legoListIds)
						{
							LegoListByReference llbr = legoListMap.get(legoListId);
							if (llbr == null)
							{
								llbr = new LegoListByReference(BDBDataStoreImpl.getInstance().getLegoListByID(legoListId), true);
								legoListMap.put(legoListId, llbr);
							}
							llbr.getLegoReference().add(lr);
						}
					}
				}
					
				Platform.runLater(new Runnable ()
				{
					@Override
					public void run()
					{
						for (LegoListByReference llbr : legoListMap.values())
						{
							legoLists.add(new LegoListTreeItem(llbr));
						}

						legoLists.add(new LegoListTreeItem(LegoTreeNodeType.blankLegoListEndNode));
						FXCollections.sort(legoLists, new LegoTreeItemComparator(true));
						//TODO [Lego Edit]
//						LegoGUI.getInstance().getLegoGUIController().showLegosForAllOpenTabs();
						if (callback != null)
						{
							callback.taskComplete(startTime, null);
						}
						ExpandedNode.setExpandedStates(before, lltv_.getRoot());
						lltv_.scrollTo(scrollTo);
					}
				});
			}
		};
		Utility.execute(r);
	}
}
