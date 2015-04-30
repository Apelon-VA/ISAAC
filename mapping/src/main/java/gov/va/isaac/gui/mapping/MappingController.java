package gov.va.isaac.gui.mapping;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.gui.mapping.data.MappingItem;
import gov.va.isaac.gui.mapping.data.MappingItemDAO;
import gov.va.isaac.gui.mapping.data.MappingObject;
import gov.va.isaac.gui.mapping.data.MappingSet;
import gov.va.isaac.gui.mapping.data.MappingSetDAO;
import gov.va.isaac.gui.mapping.data.StampedItem;
import gov.va.isaac.gui.util.CustomClipboard;
import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.utility.DialogResponse;
import gov.va.isaac.util.CommonMenus;
import gov.va.isaac.util.CommonMenusNIdProvider;
import gov.va.isaac.util.Utility;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
//import java.util.UUID;



import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.Callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Controller class for the Mapping View.
 *
 * @author dtriglianos
 * @author <a href="mailto:dtriglianos@apelon.com">David Triglianos</a>
 */

public class MappingController {
	private static final Logger LOG = LoggerFactory.getLogger(MappingController.class);

	@FXML private AnchorPane	mainPane;
	@FXML private AnchorPane	mappingPane;
	@FXML private AnchorPane	listPane;
	@FXML private ToggleButton 	activeOnlyToggle;
	@FXML private ToggleButton	stampToggle;
	@FXML private Button		refreshButton;
	@FXML private Button 		plusMappingSetButton;
	@FXML private Button 		minusMappingSetButton;
	@FXML private Button 		editMappingSetButton;
	@FXML private Label			mappingItemListTitleLabel;
	@FXML private Button 		plusMappingItemButton;
	@FXML private Button 		minusMappingItemButton;
    @FXML private Button 		editMappingItemButton;
	@FXML private Button 		commentButton;
	@FXML private Label			mappingSetSummaryLabel;
	@FXML private Label			mappingItemSummaryLabel;

	@FXML private TableView<MappingSet> 	 mappingSetTableView;
	@FXML private TableView<MappingItem> 	 mappingItemTableView;

    @FXML private TableColumn<MappingSet, MappingSet> mappingSetSTableColumn;
    @FXML private TableColumn<MappingSet, MappingSet> mappingSetNameTableColumn;
    @FXML private TableColumn<MappingSet, MappingSet> mappingSetDescriptionTableColumn;
    @FXML private TableColumn<MappingSet, MappingSet> mappingSetPurposeTableColumn;
    @FXML private TableColumn<MappingSet, MappingObject> mappingSetEditorStatusTableColumn;

    @FXML private TableColumn<MappingSet, ?> mappingSetSTAMPTableColumn;
    @FXML private TableColumn<MappingSet, StampedItem> mappingSetStatusTableColumn;
    @FXML private TableColumn<MappingSet, StampedItem> mappingSetTimeTableColumn;
    @FXML private TableColumn<MappingSet, StampedItem> mappingSetAuthorTableColumn;
    @FXML private TableColumn<MappingSet, StampedItem> mappingSetModuleTableColumn;
    @FXML private TableColumn<MappingSet, StampedItem> mappingSetPathTableColumn;
    
    @FXML private TableColumn<MappingItem, MappingItem> mappingItemSTableColumn;
    @FXML private TableColumn<MappingItem, MappingItem> mappingItemSourceTableColumn;
    @FXML private TableColumn<MappingItem, MappingItem> mappingItemTargetTableColumn;
    @FXML private TableColumn<MappingItem, MappingItem> mappingItemQualifierTableColumn;
    @FXML private TableColumn<MappingItem, MappingItem> mappingItemCommentsTableColumn;
    @FXML private TableColumn<MappingItem, MappingObject> mappingItemEditorStatusTableColumn;

    @FXML private TableColumn<MappingItem, ?> mappingItemSTAMPTableColumn;
    @FXML private TableColumn<MappingItem, StampedItem> mappingItemStatusTableColumn;
    @FXML private TableColumn<MappingItem, StampedItem> mappingItemTimeTableColumn;
    @FXML private TableColumn<MappingItem, StampedItem> mappingItemAuthorTableColumn;
    @FXML private TableColumn<MappingItem, StampedItem> mappingItemModuleTableColumn;
    @FXML private TableColumn<MappingItem, StampedItem> mappingItemPathTableColumn;
	
	
	public static MappingController init() throws IOException {
		// Load from FXML.
		URL resource = MappingController.class.getResource("Mapping.fxml");
		FXMLLoader loader = new FXMLLoader(resource);
		loader.load();
		return loader.getController();
	}

	@FXML
	public void initialize() {
        assert mappingSetTableView != null : "fx:id=\"mappingSetTableView\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert mappingSetNameTableColumn != null : "fx:id=\"mappingSetNameTableColumn\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert mappingSetSTableColumn != null : "fx:id=\"mappingSetSTableColumn\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert commentButton != null : "fx:id=\"commentButton\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert mappingSetModuleTableColumn != null : "fx:id=\"mappingSetModuleTableColumn\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert mappingSetDescriptionTableColumn != null : "fx:id=\"mappingSetDescriptionTableColumn\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert mappingItemAuthorTableColumn != null : "fx:id=\"mappingItemAuthorTableColumn\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert mappingItemSourceTableColumn != null : "fx:id=\"mappingItemSourceTableColumn\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert mappingSetStatusTableColumn != null : "fx:id=\"mappingSetActiveTableColumn\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert minusMappingSetButton != null : "fx:id=\"minusMappingSetButton\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert mappingSetEditorStatusTableColumn != null : "fx:id=\"mappingSetEditorStatusTableColumn\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert mappingSetPathTableColumn != null : "fx:id=\"mappingSetPathTableColumn\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert editMappingSetButton != null : "fx:id=\"editMappingSetButton\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert mappingItemListTitleLabel != null : "fx:id=\"mappingItemListTitleLabel\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert listPane != null : "fx:id=\"listPane\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert plusMappingSetButton != null : "fx:id=\"plusMappingSetButton\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert mappingItemTargetTableColumn != null : "fx:id=\"mappingItemTargetTableColumn\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert mappingItemEditorStatusTableColumn != null : "fx:id=\"mappingItemEditorStatusTableColumn\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert mappingSetTimeTableColumn != null : "fx:id=\"mappingSetTimeTableColumn\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert mappingItemTimeTableColumn != null : "fx:id=\"mappingItemTimeTableColumn\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert mappingItemTableView != null : "fx:id=\"mappingItemTableView\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert mappingSetSTAMPTableColumn != null : "fx:id=\"mappingSetSTAMPTableColumn\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert mappingItemSTableColumn != null : "fx:id=\"mappingItemSTableColumn\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert mappingItemModuleTableColumn != null : "fx:id=\"mappingItemModuleTableColumn\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert mappingSetAuthorTableColumn != null : "fx:id=\"mappingSetAuthorTableColumn\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert plusMappingItemButton != null : "fx:id=\"plusMappingItemButton\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert minusMappingItemButton != null : "fx:id=\"minusMappingItemButton\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert mappingItemSTAMPTableColumn != null : "fx:id=\"mappingItemSTAMPTableColumn\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert editMappingItemButton != null : "fx:id=\"editMappingItemButton\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert mappingItemStatusTableColumn != null : "fx:id=\"mappingItemActiveTableColumn\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert mappingSetSummaryLabel != null : "fx:id=\"mappingSetSummaryLabel\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert activeOnlyToggle != null : "fx:id=\"activeOnlyToggle\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert stampToggle != null : "fx:id=\"stampToggleToggle\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert mappingItemQualifierTableColumn != null : "fx:id=\"mappingItemQualifierTableColumn\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert mappingItemPathTableColumn != null : "fx:id=\"mappingItemPathTableColumn\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert mainPane != null : "fx:id=\"mainPane\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert mappingSetPurposeTableColumn != null : "fx:id=\"mappingSetPurposeTableColumn\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert mappingItemCommentsTableColumn != null : "fx:id=\"mappingItemCommentsTableColumn\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert mappingItemSummaryLabel != null : "fx:id=\"mappingItemSummaryLabel\" was not injected: check your FXML file 'Mapping.fxml'.";
        assert refreshButton != null : "fx:id=\"refreshButton\" was not injected: check your FXML file 'Mapping.fxml'.";

		mainPane.getStylesheets().add(MappingController.class.getResource("/isaac-shared-styles.css").toString());
		
		FxUtils.assignImageToButton(activeOnlyToggle, 		Images.FILTER_16.createImageView(), "Show Active Only / Show All");
		FxUtils.assignImageToButton(stampToggle, 			Images.STAMP.createImageView(), 	"Show/Hide STAMP Columns");
		FxUtils.assignImageToButton(refreshButton, 			Images.SYNC_GREEN.createImageView(), "Refresh");
		FxUtils.assignImageToButton(plusMappingSetButton, 	Images.PLUS.createImageView(), 		"Create Mapping Set");
		FxUtils.assignImageToButton(minusMappingSetButton, 	Images.MINUS.createImageView(), 	"Retire/Unretire Mapping Set");
		FxUtils.assignImageToButton(editMappingSetButton, 	Images.EDIT.createImageView(), 		"Edit Mapping Set");
		FxUtils.assignImageToButton(plusMappingItemButton, 	Images.PLUS.createImageView(), 		"Create Mapping");
		FxUtils.assignImageToButton(minusMappingItemButton, Images.MINUS.createImageView(), 	"Retire Mapping");
		FxUtils.assignImageToButton(editMappingItemButton, 	Images.EDIT.createImageView(), 		"Edit Mapping Item");
		FxUtils.assignImageToButton(commentButton, 			Images.BALLOON.createImageView(), 	"View Comments");
		
		setupColumnTypes();
		setupGlobalButtons();
		setupMappingSetButtons();
		setupMappingItemButtons();
		setupMappingSetTable();
		setupMappingItemTable();
		
		mappingSetSummaryLabel.setText("");
		mappingItemSummaryLabel.setText("");
		mappingItemListTitleLabel.setText("(no Mapping Set selected)");
		mappingSetTableView.setPlaceholder(new Label("There are no Mapping Sets in the database."));
		clearMappingItems();
	}
		
	
	private void updateMappingItemsList(MappingSet mappingSet)
	{
		clearMappingItems();
		if (mappingSet != null) {
			mappingItemTableView.setPlaceholder(new ProgressBar(-1.0));
			Utility.execute(() ->
			{
				ObservableList<MappingItem> mappingItems = FXCollections.observableList(mappingSet.getMappingItems(activeOnlyToggle.isSelected()));
				
				Platform.runLater(() ->
				{
					mappingItemTableView.setItems(mappingItems);
					mappingItemTableView.setPlaceholder(new Label("The selected Mapping Set contains no Mapping Items."));
					
					mappingItemListTitleLabel.setText(mappingSet.getName());
					plusMappingItemButton.setDisable(false);
					minusMappingSetButton.setDisable(false);
					editMappingSetButton.setDisable(false);
					mappingSetSummaryLabel.setText(mappingSet.getSummary(activeOnlyToggle.isSelected()));
				});
			});
		}
	}

	private MappingSet getSelectedMappingSet() {
		return (MappingSet)mappingSetTableView.getSelectionModel().getSelectedItem();
	}
	
	private MappingItem getSelectedMappingItem() {
		return mappingItemTableView.getSelectionModel().getSelectedItem();
	}
	
	private ObservableList<MappingItem> getSelectedMappingItems() {
		return mappingItemTableView.getSelectionModel().getSelectedItems();
	}
	
	public AnchorPane getRoot()	{
		return mainPane;
	}
	
	protected void refreshMappingSets()
	{
		ObservableList<MappingSet> mappingSets;
		boolean activeOnly = activeOnlyToggle.isSelected();
		try
		{
			mappingSets = FXCollections.observableList(MappingSetDAO.getMappingSets(activeOnly));
		}
		catch (IOException e)
		{
			LOG.error("unexpected", e);
			//TODO GUI prompt;
			mappingSets = FXCollections.observableArrayList();
		}
		mappingSetTableView.setItems(mappingSets);
		// TODO maybe come up with a way to preserve the selection, if possible.
		mappingSetTableView.getSelectionModel().clearSelection();
		
		refreshMappingItems();
	}

	protected void refreshMappingItems() {
		MappingSet selectedMappingSet = getSelectedMappingSet();
		updateMappingItemsList(selectedMappingSet);
	}
	
	protected void clearMappingItems() {
		mappingItemTableView.getItems().clear();
		mappingItemTableView.setPlaceholder(new Label("No Mapping Set is selected."));
	}
	
	private void setupMappingSetTable() {
		
		setMappingSetTableFactories(mappingSetTableView.getColumns());
		
		mappingSetNameTableColumn.setComparator(MappingSet.nameComparator);
		mappingSetPurposeTableColumn.setComparator(MappingSet.purposeComparator);
		mappingSetDescriptionTableColumn.setComparator(MappingSet.descriptionComparator);
		mappingSetEditorStatusTableColumn.setComparator(MappingObject.editorStatusComparator);

	    mappingSetStatusTableColumn.setComparator(StampedItem.statusComparator);
	    mappingSetTimeTableColumn.setComparator(StampedItem.timeComparator);
	    mappingSetAuthorTableColumn.setComparator(StampedItem.authorComparator);
	    mappingSetModuleTableColumn.setComparator(StampedItem.moduleComparator);
	    mappingSetPathTableColumn.setComparator(StampedItem.pathComparator);

		mappingSetTableView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<MappingSet>()
		{
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends MappingSet> c)
			{
				updateMappingItemsList(getSelectedMappingSet());
			}
		});
		
		mappingSetSTAMPTableColumn.setVisible(false);
		
	}

	private void setupMappingItemTable() {

		setMappingItemTableFactories(mappingItemTableView.getColumns());

	    mappingItemSourceTableColumn.setComparator(MappingItem.sourceComparator);
	    mappingItemTargetTableColumn.setComparator(MappingItem.targetComparator);
	    mappingItemQualifierTableColumn.setComparator(MappingItem.qualifierComparator);
	    mappingItemCommentsTableColumn.setComparator(MappingItem.commentsComparator);
		mappingItemEditorStatusTableColumn.setComparator(MappingObject.editorStatusComparator);

		mappingItemStatusTableColumn.setComparator(StampedItem.statusComparator);
	    mappingItemTimeTableColumn.setComparator(StampedItem.timeComparator);
	    mappingItemAuthorTableColumn.setComparator(StampedItem.authorComparator);
	    mappingItemModuleTableColumn.setComparator(StampedItem.moduleComparator);
	    mappingItemPathTableColumn.setComparator(StampedItem.pathComparator);

		mappingItemTableView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<MappingItem>() {
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends MappingItem> c) {
				MappingItem selectedMappingItem = getSelectedMappingItem();
				int selectedItemCount = c.getList().size(); 
				if (selectedItemCount >= 1) {
					selectedMappingItem = (MappingItem) c.getList().get(0);
				} else {
					selectedMappingItem = null;
				}
				minusMappingItemButton.setDisable(selectedItemCount == 0);
				editMappingItemButton.setDisable(selectedItemCount != 1);
				commentButton.setDisable(selectedItemCount == 0);
				
				String summary = "";
				switch (selectedItemCount) {
				case 0:
					break;
				case 1:
					summary = selectedMappingItem.getSummary();
					break;
				default:
					summary = "Multiple items selected";
				}
				mappingItemSummaryLabel.setText(summary);
			}
		});
		
		mappingItemTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		mappingItemSTAMPTableColumn.setVisible(false);

	}
	
	@SuppressWarnings("unchecked")
	private void setMappingSetTableFactories(ObservableList<TableColumn<MappingSet,?>> tableColumns) {
		for (TableColumn<MappingSet, ?> tableColumn : tableColumns) {
			TableColumn<MappingSet, MappingSet> mappingItemTableColumn = (TableColumn<MappingSet, MappingSet>)tableColumn;
			mappingItemTableColumn.setCellValueFactory(mappingSetCellValueFactory);
			mappingItemTableColumn.setCellFactory(mappingSetCellFactory);
			
			ObservableList<TableColumn<MappingSet,?>> nestedTableColumns = mappingItemTableColumn.getColumns();
			if (nestedTableColumns.size() > 0) {
				setMappingSetTableFactories(nestedTableColumns);
			}
		}
		
	}

	@SuppressWarnings("unchecked")
	private void setMappingItemTableFactories(ObservableList<TableColumn<MappingItem,?>> tableColumns) {
		for (TableColumn<MappingItem, ?> tableColumn : tableColumns) {
			TableColumn<MappingItem, MappingItem> mappingItemTableColumn = (TableColumn<MappingItem, MappingItem>)tableColumn;
			mappingItemTableColumn.setCellValueFactory(mappingItemCellValueFactory);
			mappingItemTableColumn.setCellFactory(mappingItemCellFactory);
			
			ObservableList<TableColumn<MappingItem,?>> nestedTableColumns = mappingItemTableColumn.getColumns();
			if (nestedTableColumns.size() > 0) {
				setMappingItemTableFactories(nestedTableColumns);
			}
		}
		
	}

	private void updateCell(TableCell<?, ?> cell, MappingObject mappingObject) {
		if (!cell.isEmpty() && mappingObject != null) {
			ContextMenu cm = new ContextMenu();
			cell.setContextMenu(cm);
			SimpleStringProperty property = null;
			int  conceptNid  = 0;
			MappingColumnType columnType = (MappingColumnType) cell.getTableColumn().getUserData();

			cell.setText(null);
			cell.setGraphic(null);

			switch (columnType) {
			case STATUS_CONDENSED:
				StackPane sp = new StackPane();
				sp.setPrefSize(25, 25);
				String tooltipText = mappingObject.isActive()? "Active" : "Inactive";
				ImageView image    = mappingObject.isActive()? Images.BLACK_DOT.createImageView() : Images.GREY_DOT.createImageView();
				sizeAndPosition(image, sp, Pos.CENTER);
				cell.setTooltip(new Tooltip(tooltipText));
				cell.setGraphic(sp);
				break;
				
			case NAME:
				property = ((MappingSet)mappingObject).getNameProperty(); 
				break;
				
			case PURPOSE:
				property = ((MappingSet)mappingObject).getPurposeProperty(); 
				break;
				
			case DESCRIPTION:
				property = ((MappingSet)mappingObject).getDescriptionProperty(); 
				break;
				
			case SOURCE:
				property = ((MappingItem)mappingObject).getSourceConceptProperty();
				conceptNid = ((MappingItem)mappingObject).getSourceConceptNid();
				break;
			case TARGET:
				property = ((MappingItem)mappingObject).getTargetConceptProperty();
				conceptNid = ((MappingItem)mappingObject).getTargetConceptNid();
				break;
			case QUALIFIER:
				property = ((MappingItem)mappingObject).getQualifierConceptProperty();
				conceptNid = ((MappingItem)mappingObject).getQualifierConceptNid();
				break;
			case COMMENTS:
				property = ((MappingItem)mappingObject).getCommentsProperty();
				break;
			case EDITOR_STATUS:
				property = mappingObject.getEditorStatusConceptProperty();
				conceptNid = mappingObject.getEditorStatusConceptNid();
				break;
			case STATUS_STRING:
				property = mappingObject.getStatusProperty();
				break;
			case TIME:
				property = mappingObject.getTimeProperty();
				break;
			case AUTHOR:
				property = mappingObject.getAuthorProperty();
				conceptNid = mappingObject.getAuthorNid();
				break;
			case MODULE:
				property = mappingObject.getModuleProperty();
				conceptNid = mappingObject.getModuleNid();
				break;
			case PATH:
				property = mappingObject.getPathProperty();
				conceptNid = mappingObject.getPathNid();
				break;
			default:
				// Nothing
			}
			
			if (property != null) {
				Text text = new Text();
				text.textProperty().bind(property);
				text.wrappingWidthProperty().bind(cell.getTableColumn().widthProperty());
				cell.setGraphic(text);
	
				MenuItem mi = new MenuItem("Copy Value");
				mi.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent arg0) {
						CustomClipboard.set(((Text)cell.getGraphic()).getText());
					}
				});
				mi.setGraphic(Images.COPY.createImageView());
				cm.getItems().add(mi);
	
				if (columnType.isConcept() && conceptNid != 0) {
					final int nid = conceptNid;
					CommonMenus.addCommonMenus(cm, new CommonMenusNIdProvider() {
						@Override
						public Collection<Integer> getNIds() {
						   return Arrays.asList(new Integer[] {nid});
						}
					});
				}
			}
		} else {
			cell.setText(null);
			cell.setGraphic(null);
		}
	}
	
	
	private void setupMappingSetButtons() {
		editMappingSetButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				MappingSet selectedMappingSet = getSelectedMappingSet();
				if (selectedMappingSet != null) {
					CreateMappingSetView cv = AppContext.getService(CreateMappingSetView.class);
					cv.setMappingSet(selectedMappingSet);
					cv.showView(getRoot().getScene().getWindow());
				}
			}
		});
		
		plusMappingSetButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				CreateMappingSetView cv = AppContext.getService(CreateMappingSetView.class);
				cv.showView(getRoot().getScene().getWindow());
			}
		});
		
		minusMappingSetButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				MappingSet selectedMappingSet = getSelectedMappingSet();
				if (selectedMappingSet != null) {
					String verb = (selectedMappingSet.isActive())? "retire" : "unretire";
					DialogResponse response = AppContext.getCommonDialogs().showYesNoDialog("Please Confirm", "Are you sure you want to " + verb + " " + selectedMappingSet.getName() + "?");
					if (response == DialogResponse.YES) {
						try
						{
							if (selectedMappingSet.isActive()) {
								MappingSetDAO.retireMappingSet(selectedMappingSet.getPrimordialUUID());
							} else {
								MappingSetDAO.unRetireMappingSet(selectedMappingSet.getPrimordialUUID());
							}
						}
						catch (IOException e1)
						{
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						refreshMappingSets();
					}
				}
			}
		});
		
	}

	private void setupMappingItemButtons() {
		plusMappingItemButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				MappingSet selectedMappingSet = getSelectedMappingSet();
				if (selectedMappingSet != null) {
					MappingItem selectedMappingItem = getSelectedMappingItem();

					CreateMappingItemView itemView = AppContext.getService(CreateMappingItemView.class);
					itemView.setMappingSet(selectedMappingSet);
					if (selectedMappingItem != null) {
						itemView.setSourceConcept(selectedMappingItem.getSourceConcept());
					}
					itemView.showView(null);
				}
			}
		});
		
		minusMappingItemButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				ObservableList<MappingItem> selectedMappingItems = getSelectedMappingItems();
				if (selectedMappingItems.size() >= 0) {
					if (selectedMappingItems.size() == 1 && !selectedMappingItems.get(0).isActive()) {
						// One inactive item selected; unretire
						DialogResponse response = AppContext.getCommonDialogs().showYesNoDialog("Please Confirm", "Are you sure you want to unretire this Mapping Item?");
						if (response == DialogResponse.YES) {
							try {
								MappingItemDAO.unRetireMappingItem(selectedMappingItems.get(0).getPrimordialUUID());
							} catch (IOException e1) {
								//TODO prompt
								e1.printStackTrace();
							}
							updateMappingItemsList(getSelectedMappingSet());
						}
					} else {
						String clause = (selectedMappingItems.size() == 1) ? "this Mapping Item" : "these " + Integer.toString(selectedMappingItems.size()) + " Mapping Items";
						DialogResponse response = AppContext.getCommonDialogs().showYesNoDialog("Please Confirm", "Are you sure you want to retire " + clause + "?");
						if (response == DialogResponse.YES) {
							for (MappingItem mappingItem : selectedMappingItems) {
								if (mappingItem.isActive()) {
									// Don't bother trying to retire inactive items
									try {
										MappingItemDAO.retireMappingItem(mappingItem.getPrimordialUUID());
									
									} catch (IOException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
								}
							}
							updateMappingItemsList(getSelectedMappingSet());
						}
					}
				}
			}
		});

		editMappingItemButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				MappingItem selectedMappingItem = getSelectedMappingItem();
				if (selectedMappingItem != null) {
					EditMappingItemView cv = AppContext.getService(EditMappingItemView.class);
					cv.setMappingItem(selectedMappingItem);
					cv.showView(getRoot().getScene().getWindow());
				}
			}
		});
		
		commentButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				MappingSet  selectedMappingSet  = getSelectedMappingSet();
				ObservableList<MappingItem> selectedMappingItems = getSelectedMappingItems();
				if (selectedMappingItems.size() > 0) {
					CommentDialogView commentView = AppContext.getService(CommentDialogView.class);
					commentView.setMappingSetAndItems(selectedMappingSet, selectedMappingItems);
					commentView.showView(getRoot().getScene().getWindow());
				}
			}
		});
	}

	private void setupGlobalButtons() {
		ToggleGroup activeOnlyToggleGroup = new ToggleGroup();
		activeOnlyToggle.setToggleGroup(activeOnlyToggleGroup);
		activeOnlyToggle.setSelected(true);
		
		activeOnlyToggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			@Override
			public void changed(ObservableValue<? extends Toggle> ov, Toggle toggle, Toggle new_toggle) {
				refreshMappingSets();
			}
		});
		
		ToggleGroup showStampToggleGroup = new ToggleGroup();
		stampToggle.setToggleGroup(showStampToggleGroup);
		stampToggle.setSelected(false);
		
		showStampToggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			@Override
			public void changed(ObservableValue<? extends Toggle> ov, Toggle toggle, Toggle new_toggle) {
				boolean showStampFields = stampToggle.isSelected();
				mappingSetSTAMPTableColumn.setVisible(showStampFields);
				mappingItemSTAMPTableColumn.setVisible(showStampFields);
			}
		});		
		
		refreshButton.setOnAction((event) -> {
			refreshMappingSets();
		});
		
	}
	
	private void setupColumnTypes() {
		mappingSetSTableColumn.setUserData(MappingColumnType.STATUS_CONDENSED);
		mappingSetNameTableColumn.setUserData(MappingColumnType.NAME);
		mappingSetDescriptionTableColumn.setUserData(MappingColumnType.DESCRIPTION);
		mappingSetPurposeTableColumn.setUserData(MappingColumnType.PURPOSE);
		mappingSetEditorStatusTableColumn.setUserData(MappingColumnType.EDITOR_STATUS);

		mappingSetSTAMPTableColumn.setUserData(MappingColumnType.STAMP);
		mappingSetStatusTableColumn.setUserData(MappingColumnType.STATUS_STRING);
		mappingSetTimeTableColumn.setUserData(MappingColumnType.TIME);
		mappingSetAuthorTableColumn.setUserData(MappingColumnType.AUTHOR);
		mappingSetModuleTableColumn.setUserData(MappingColumnType.MODULE);
		mappingSetPathTableColumn.setUserData(MappingColumnType.PATH);

		mappingItemSTableColumn.setUserData(MappingColumnType.STATUS_CONDENSED);
		mappingItemSourceTableColumn.setUserData(MappingColumnType.SOURCE);
		mappingItemTargetTableColumn.setUserData(MappingColumnType.TARGET);
		mappingItemQualifierTableColumn.setUserData(MappingColumnType.QUALIFIER);
		mappingItemCommentsTableColumn.setUserData(MappingColumnType.COMMENTS);
		mappingItemEditorStatusTableColumn.setUserData(MappingColumnType.EDITOR_STATUS);

		mappingItemSTAMPTableColumn.setUserData(MappingColumnType.STAMP);
		mappingItemStatusTableColumn.setUserData(MappingColumnType.STATUS_STRING);
		mappingItemTimeTableColumn.setUserData(MappingColumnType.TIME);
		mappingItemAuthorTableColumn.setUserData(MappingColumnType.AUTHOR);
		mappingItemModuleTableColumn.setUserData(MappingColumnType.MODULE);
		mappingItemPathTableColumn.setUserData(MappingColumnType.PATH);
		
	}

	public static void sizeAndPosition(Node node, StackPane sp, Pos position)
	{
		if (node instanceof ImageView)
		{
			((ImageView)node).setFitHeight(12);
			((ImageView)node).setFitWidth(12);
		}
		Insets insets;
		switch (position) {
		case TOP_LEFT:
			insets = new Insets(0,0,0,0);
			break;
		case TOP_RIGHT:
			insets = new Insets(0,0,0,13);
			break;
		case BOTTOM_LEFT:
			insets = new Insets(13,0,0,0);
			break;
		case BOTTOM_RIGHT:
			insets = new Insets(13,0,0,13);
			break;
		case CENTER:
			insets = new Insets(5,0,0,5);
			break;
		default:
			insets = new Insets(0,0,0,0);
		}
		StackPane.setMargin(node, insets);
		sp.getChildren().add(node);
		StackPane.setAlignment(node, Pos.TOP_LEFT);
	}

	private Callback<TableColumn.CellDataFeatures<MappingItem, MappingItem>, ObservableValue<MappingItem>> mappingItemCellValueFactory = 
			new Callback<TableColumn.CellDataFeatures<MappingItem, MappingItem>, ObservableValue<MappingItem>>()	{
		@Override
		public ObservableValue<MappingItem> call(CellDataFeatures<MappingItem, MappingItem> param) {
			return new SimpleObjectProperty<MappingItem>(param.getValue());
		}
	};
	
	private Callback<TableColumn.CellDataFeatures<MappingSet, MappingSet>, ObservableValue<MappingSet>> mappingSetCellValueFactory = 
			new Callback<TableColumn.CellDataFeatures<MappingSet, MappingSet>, ObservableValue<MappingSet>>()	{
		@Override
		public ObservableValue<MappingSet> call(CellDataFeatures<MappingSet, MappingSet> param) {
			return new SimpleObjectProperty<MappingSet>(param.getValue());
		}
	};
	
	private Callback<TableColumn<MappingItem, MappingItem>, TableCell<MappingItem, MappingItem>> mappingItemCellFactory =
			new Callback<TableColumn<MappingItem, MappingItem>, TableCell<MappingItem, MappingItem>>() {

		@Override
		public TableCell<MappingItem, MappingItem> call(TableColumn<MappingItem, MappingItem> param) {
			return new TableCell<MappingItem, MappingItem>() {
				@Override
				public void updateItem(final MappingItem mappingItem, boolean empty) {
					super.updateItem(mappingItem, empty);
					updateCell(this, mappingItem);
				}
			};
		}
	};

	private Callback<TableColumn<MappingSet, MappingSet>, TableCell<MappingSet, MappingSet>> mappingSetCellFactory =
			new Callback<TableColumn<MappingSet, MappingSet>, TableCell<MappingSet, MappingSet>>() {

		@Override
		public TableCell<MappingSet, MappingSet> call(TableColumn<MappingSet, MappingSet> param) {
			return new TableCell<MappingSet, MappingSet>() {
				@Override
				public void updateItem(final MappingSet mappingSet, boolean empty) {
					super.updateItem(mappingSet, empty);
					updateCell(this, mappingSet);
				}
			};
		}
	};

	
	public static void setComboSelection(ComboBox<SimpleDisplayConcept> combo, SimpleDisplayConcept selectConcept, int defaultIndex) {
		if (selectConcept == null) {
			combo.getSelectionModel().select(defaultIndex);
		} else {
			setComboSelection(combo, selectConcept.getDescription(), defaultIndex);
		}
	}
	
	public static void setComboSelection(ComboBox<SimpleDisplayConcept> combo, String selectValue, int defaultIndex) {
		boolean found = false;
		if (selectValue != null && !selectValue.trim().equals("")) {
			for (SimpleDisplayConcept sdc : combo.getItems()) {
				if (sdc.getDescription().equals(selectValue)) {
					combo.getSelectionModel().select(sdc);
					found = true;
					break;
				}
			}
		}
		if (!found && defaultIndex >= 0 && defaultIndex < combo.getItems().size()) {
			combo.getSelectionModel().select(defaultIndex);
		}
	}

	
}
