package gov.va.isaac.gui.mapping;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.mapping.data.MappingItem;
import gov.va.isaac.gui.mapping.data.MappingItemDAO;
import gov.va.isaac.gui.mapping.data.MappingSet;
import gov.va.isaac.gui.mapping.data.MappingSetDAO;
import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.utility.DialogResponse;
import gov.va.isaac.util.Utility;
import java.io.IOException;
import java.net.URL;
import javafx.application.Platform;
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
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
	@FXML private Button 		plusMappingSetButton;
	@FXML private Button 		minusMappingSetButton;
	@FXML private Button 		editMappingSetButton;
	@FXML private Label			mappingItemListTitleLabel;
	@FXML private Button 		plusMappingItemButton;
	@FXML private Button 		minusMappingItemButton;
	@FXML private Button 		commentButton;
	@FXML private Label			mappingSetSummaryLabel;
	@FXML private Label			mappingItemSummaryLabel;

	@FXML private TableView<MappingSet> 	 mappingSetTableView;
	@FXML private TableView<MappingItem> 	 mappingItemTableView;
	@FXML private TableColumn<MappingSet, ?> mappingSetRetiredTableColumn;
	@FXML private TableColumn<MappingSet, ?> mappingItemRetiredTableColumn;
	
	public static MappingController init() throws IOException {
		// Load from FXML.
		URL resource = MappingController.class.getResource("Mapping.fxml");
		FXMLLoader loader = new FXMLLoader(resource);
		loader.load();
		return loader.getController();
	}

	@SuppressWarnings("unchecked")
	@FXML
	public void initialize() {
		assert mainPane 			!= null : "fx:id=\"mainPane\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert mappingPane 			!= null : "fx:id=\"mappingPane\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert listPane 			!= null : "fx:id=\"listPane\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert activeOnlyToggle 	!= null : "fx:id=\"activeOnlyToggle\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert plusMappingSetButton 	!= null : "fx:id=\"plusMappingButton\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert minusMappingSetButton	!= null : "fx:id=\"minusMappingButton\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert editMappingSetButton 	!= null : "fx:id=\"editMappingButton\" was not injected: check your FXML file 'Mapping.fxml'.";

		assert mappingSetSummaryLabel 		!= null : "fx:id=\"mappingSummaryLabel\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert mappingSetTableView 			!= null : "fx:id=\"mappingTableView\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert mappingItemListTitleLabel 	!= null : "fx:id=\"listTitleLabel\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert mappingItemTableView 		!= null : "fx:id=\"listTableView\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert plusMappingItemButton 		!= null : "fx:id=\"plusListButton\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert minusMappingItemButton 		!= null : "fx:id=\"minusListButton\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert commentButton 				!= null : "fx:id=\"commentButton\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert mappingItemSummaryLabel 		!= null : "fx:id=\"listSummaryLabel\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert mappingSetRetiredTableColumn != null : "fx:id=\"mappingSetRetiredTableColumn\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert mappingItemRetiredTableColumn != null : "fx:id=\"mappingItemRetiredTableColumn\" was not injected: check your FXML file 'Mapping.fxml'.";
        
		mainPane.getStylesheets().add(MappingController.class.getResource("/isaac-shared-styles.css").toString());
		
		FxUtils.assignImageToButton(activeOnlyToggle, 		Images.FILTER_16.createImageView(), "Show Active Only / Show All");
		FxUtils.assignImageToButton(plusMappingSetButton, 	Images.PLUS.createImageView(), 		"Create Mapping Set");
		FxUtils.assignImageToButton(minusMappingSetButton, 	Images.MINUS.createImageView(), 	"Retire/Unretire Mapping Set");
		FxUtils.assignImageToButton(editMappingSetButton, 	Images.EDIT.createImageView(), 		"Edit Mapping Set");
		FxUtils.assignImageToButton(plusMappingItemButton, 	Images.PLUS.createImageView(), 		"Create Mapping");
		FxUtils.assignImageToButton(minusMappingItemButton, Images.MINUS.createImageView(), 	"Retire Mapping");
		FxUtils.assignImageToButton(commentButton, 			Images.BALLOON.createImageView(), 	"View Comments");
		
		ToggleGroup activeOnlyToggleGroup = new ToggleGroup();
		activeOnlyToggle.setToggleGroup(activeOnlyToggleGroup);
		activeOnlyToggle.setSelected(true);
		
		activeOnlyToggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			@Override
			public void changed(ObservableValue<? extends Toggle> ov, Toggle toggle, Toggle new_toggle) {
				refreshMappingSets();
			}
		});
		
		editMappingSetButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				MappingSet selectedMappingSet = getSelectedMappingSet();
				if (selectedMappingSet != null) {
					CreateMappingSetView cv = AppContext.getService(CreateMappingSetView.class);
					cv.setMappingSet(getSelectedMappingSet());
					cv.showView(null);
				}
			}
		});
		
		plusMappingSetButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				CreateMappingSetView cv = AppContext.getService(CreateMappingSetView.class);
				cv.showView(null);
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

		
		commentButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				MappingSet  selectedMappingSet  = getSelectedMappingSet();
				MappingItem selectedMappingItem = getSelectedMappingItem();
				if (selectedMappingItem != null && selectedMappingSet != null) {
					CommentDialogView commentView = AppContext.getService(CommentDialogView.class);
					commentView.setMappingSetAndItem(selectedMappingSet, selectedMappingItem);
					commentView.showView(null);
				}
			}
		});
		
		
		for (TableColumn<MappingSet, ?> x : mappingSetTableView.getColumns())
		{
			((TableColumn<MappingSet, String>)x).setCellValueFactory(new Callback<TableColumn.CellDataFeatures<MappingSet,String>, ObservableValue<String>>()
			{
				@Override
				public ObservableValue<String> call(CellDataFeatures<MappingSet, String> param)
				{
					SimpleStringProperty property = new SimpleStringProperty();
					MappingSet mappingSet = param.getValue();
					String columnName = param.getTableColumn().getText().trim();
					
					switch (columnName) {
					case "Name":
						property = new SimpleStringProperty(mappingSet.getName());
						break;
					case "Purpose":
						property = new SimpleStringProperty(mappingSet.getPurpose());
						break;
					case "Status":
						property = mappingSet.getEditorStatusConceptProperty();
						break;
					case "Description":
						property = new SimpleStringProperty(mappingSet.getDescription());
						break;
					case "Retired":
						property = new SimpleStringProperty((mappingSet.isActive())?"":"Y");
						break;
					default:
						System.out.println(param.getTableColumn().getText());
					}
					return property;
				}
			});
		}
		
		mappingSetTableView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<MappingSet>()
		{
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends MappingSet> c)
			{
				updateMappingItemsList(getSelectedMappingSet());
			}
		});
		
		for (TableColumn<MappingItem, ?> x : mappingItemTableView.getColumns())
		{
			((TableColumn<MappingItem, String>)x).setCellValueFactory(new Callback<TableColumn.CellDataFeatures<MappingItem,String>, ObservableValue<String>>()
			{
				@Override
				public ObservableValue<String> call(CellDataFeatures<MappingItem, String> param)
				{
					SimpleStringProperty property = new SimpleStringProperty();
					MappingItem mappingItem = param.getValue();
					String columnName = param.getTableColumn().getText().trim();
					
					switch (columnName) {
					case "Source Concept Name":
						property = mappingItem.getSourceConceptProperty(); 
						break;
					case "Target Concept Name":
						property = mappingItem.getTargetConceptProperty();
						break;
					case "Qualifier":
						property = mappingItem.getQualifierConceptProperty();
						break;
					case "Comments":
						property = mappingItem.getCommentsProperty();
						break;
					case "Status":
						property = mappingItem.getEditorStatusConceptProperty();
						break;
					case "Retired":
						property = new SimpleStringProperty((mappingItem.isActive())?"":"Y");
						break;
					default:
						System.out.println(param.getTableColumn().getText());
					}
					return property;
				}
			});
		}
		
		mappingItemTableView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<MappingItem>() {
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends MappingItem> c) {
				MappingItem selectedMappingItem = getSelectedMappingItem();
				if (c.getList().size() >= 1) {
					selectedMappingItem = (MappingItem) c.getList().get(0);
				} else {
					selectedMappingItem = null;
				}
				minusMappingItemButton.setDisable(selectedMappingItem == null);
				commentButton.setDisable(selectedMappingItem == null);
				
				mappingItemSummaryLabel.setText((selectedMappingItem == null)? "" : selectedMappingItem.getSummary());
			}
		});
		
		mappingItemTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
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
		return mappingSetTableView.getSelectionModel().getSelectedItem();
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
		
		//mappingSetRetiredTableColumn.setVisible(!activeOnly);
		//mappingItemRetiredTableColumn.setVisible(!activeOnly);
		
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
}
