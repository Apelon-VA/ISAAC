package gov.va.isaac.gui.mapping;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.mapping.data.MappingItem;
import gov.va.isaac.gui.mapping.data.MappingItemComment;
import gov.va.isaac.gui.mapping.data.MappingSet;
import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.utility.DialogResponse;
import gov.va.isaac.util.OTFUtility;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

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
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;

import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
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
	@FXML private TableView<MappingSet> mappingSetTableView;
	@FXML private Label			mappingItemListTitleLabel;
	@FXML private TableView<MappingItem> mappingItemTableView;
    @FXML private Button 		plusMappingItemButton;
    @FXML private Button 		minusMappingItemButton;
    @FXML private Button 		commentButton;
	@FXML private Label			mappingSetSummaryLabel;
    @FXML private Label			mappingItemSummaryLabel;
    
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

		assert mappingSetSummaryLabel 	!= null : "fx:id=\"mappingSummaryLabel\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert mappingSetTableView 	!= null : "fx:id=\"mappingTableView\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert mappingItemListTitleLabel 		!= null : "fx:id=\"listTitleLabel\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert mappingItemTableView != null : "fx:id=\"listTableView\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert plusMappingItemButton 		!= null : "fx:id=\"plusListButton\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert minusMappingItemButton 		!= null : "fx:id=\"minusListButton\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert commentButton 		!= null : "fx:id=\"commentButton\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert mappingItemSummaryLabel 	!= null : "fx:id=\"listSummaryLabel\" was not injected: check your FXML file 'Mapping.fxml'.";

		
		mainPane.getStylesheets().add(MappingController.class.getResource("/isaac-shared-styles.css").toString());
		
		FxUtils.assignImageToButton(activeOnlyToggle, 		Images.FILTER_16.createImageView(), "Show Active Only / Show All");
		FxUtils.assignImageToButton(plusMappingSetButton, 	Images.PLUS.createImageView(), 		"Create Mapping Set");
		FxUtils.assignImageToButton(minusMappingSetButton, 	Images.MINUS.createImageView(), 	"Retire Mapping Set");
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
				ObservableList<MappingSet> selectedMappingSets = getSelectedMappingSets();
				if (selectedMappingSets.size() >= 0) {
					String clause = (selectedMappingSets.size() == 1) ? selectedMappingSets.get(0).getName() : "these " + Integer.toString(selectedMappingSets.size()) + " Mapping Sets";
					DialogResponse response = AppContext.getCommonDialogs().showYesNoDialog("Please Confirm", "Are you sure you want to retire " + clause + "?");

					if (response == DialogResponse.YES) {
						for (MappingSet mappingSet : selectedMappingSets) {
							mappingSet.retire();
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
					String clause = (selectedMappingItems.size() == 1) ? "this Mapping Item" : "these " + Integer.toString(selectedMappingItems.size()) + " Mapping Items";
					DialogResponse response = AppContext.getCommonDialogs().showYesNoDialog("Please Confirm", "Are you sure you want to retire " + clause + "?");

					if (response == DialogResponse.YES) {
						for (MappingItem mappingItem : selectedMappingItems) {
							mappingItem.retire();
						}
						updateMappingItemsList(getSelectedMappingSet());
					}
				}
			}
		});

		
		commentButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				CommentDialogView commentView = AppContext.getService(CommentDialogView.class);
				commentView.showView(null);
			}
		});
		
		for (TableColumn<MappingSet, ?> x : mappingSetTableView.getColumns())
		{
			((TableColumn<MappingSet, String>)x).setCellValueFactory(new Callback<TableColumn.CellDataFeatures<MappingSet,String>, ObservableValue<String>>()
			{
				@Override
				public ObservableValue<String> call(CellDataFeatures<MappingSet, String> param)
				{
					if (param.getTableColumn().getText().equals("Name"))
					{
						return new SimpleStringProperty(param.getValue().getName());
					}
					else if (param.getTableColumn().getText().equals("Purpose"))
					{
						return new SimpleStringProperty(param.getValue().getPurpose());
					}
					else if (param.getTableColumn().getText().equals("Status"))
					{
						UUID editorStatus = param.getValue().getEditorStatus();
						if (editorStatus == null) {
							return new SimpleStringProperty("");
						} else {
							return new SimpleStringProperty(editorStatus.toString().trim());
						}
					}
					else if (param.getTableColumn().getText().equals("Description"))
					{
						return new SimpleStringProperty(param.getValue().getDescription());
					}
					else
					{
						System.out.println(param.getTableColumn().getText());
						return new SimpleStringProperty();
						
					}
				}
			});
		}
		
		mappingSetTableView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<MappingSet>()
		{
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends MappingSet> c)
			{
				MappingSet selectedMappingSet = getSelectedMappingSet();
				if (selectedMappingSet != null) {
					updateMappingItemsList(selectedMappingSet);
				}
			}
		});
		
		mappingSetTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
		for (TableColumn<MappingItem, ?> x : mappingItemTableView.getColumns())
		{
			((TableColumn<MappingItem, String>)x).setCellValueFactory(new Callback<TableColumn.CellDataFeatures<MappingItem,String>, ObservableValue<String>>()
			{
				@Override
				public ObservableValue<String> call(CellDataFeatures<MappingItem, String> param)
				{
					if (param.getTableColumn().getText().equals("Source Concept Name"))
					{
						return new SimpleStringProperty(OTFUtility.getDescription(param.getValue().getSourceConcept()));
					}
					else if (param.getTableColumn().getText().equals("Target Concept Name"))
					{
						return new SimpleStringProperty(OTFUtility.getDescription(param.getValue().getTargetConcept()));
					}
					else if (param.getTableColumn().getText().equals("Qualifier"))
					{
						return new SimpleStringProperty(OTFUtility.getDescription(param.getValue().getQualifierConcept()));
					}
					else if (param.getTableColumn().getText().equals("Comments"))
					{
						String commentValue = "";
						List<MappingItemComment> comments = param.getValue().getComments();
						if (comments.size() > 0) {
							commentValue = comments.get(0).getCommentText();
						}
						if (comments.size() > 1) {
							commentValue += " (+" + Integer.toString(comments.size() - 1) + " more)";
						}
						return new SimpleStringProperty(commentValue);
					}
					else if (param.getTableColumn().getText().equals("Status"))
					{
						UUID editorStatus = param.getValue().getEditorStatus();
						String statusString = (editorStatus == null) ? "" : editorStatus.toString().trim(); 
						return new SimpleStringProperty(statusString);
					}
					else
					{
						System.out.println(param.getTableColumn().getText());
						return new SimpleStringProperty();
						
					}
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
		mappingItemTableView.setPlaceholder(new Label("No Mapping Set is selected."));
	}
		
	
	private void updateMappingItemsList(MappingSet mappingSet)
	{
		ObservableList<MappingItem> mappingItems;
		mappingItems = FXCollections.observableList(mappingSet.getMappingItems());
		mappingItemTableView.setItems(mappingItems);
		if (mappingItems.size() == 0) {
			mappingItemTableView.setPlaceholder(new Label("The selected Mapping Set contains no Mapping Items."));
		}
		
		mappingItemListTitleLabel.setText(mappingSet.getName());
		plusMappingItemButton.setDisable(false);
		minusMappingSetButton.setDisable(false);
		editMappingSetButton.setDisable(false);
		mappingSetSummaryLabel.setText(mappingSet.getSummary());
	}

	private MappingSet getSelectedMappingSet() {
		return mappingSetTableView.getSelectionModel().getSelectedItem();
	}
	
	private MappingItem getSelectedMappingItem() {
		return mappingItemTableView.getSelectionModel().getSelectedItem();
	}
	
	private ObservableList<MappingSet> getSelectedMappingSets() {
		return mappingSetTableView.getSelectionModel().getSelectedItems();
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
			mappingSets = FXCollections.observableList(MappingSet.getMappingSets(activeOnly));
		}
		catch (IOException | ContradictionException e)
		{
			LOG.error("unexpected", e);
			//TODO GUI prompt;
			mappingSets = FXCollections.observableArrayList();
		}
		mappingSetTableView.setItems(mappingSets);
	}

	protected void refreshMappingItems() {
		MappingSet selectedMappingSet = getSelectedMappingSet();
		if (selectedMappingSet != null) {
			updateMappingItemsList(selectedMappingSet);
		}
	}
}
