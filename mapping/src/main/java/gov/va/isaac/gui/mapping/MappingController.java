package gov.va.isaac.gui.mapping;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.mapping.data.MappingItem;
import gov.va.isaac.gui.mapping.data.MappingSet;
import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.util.OTFUtility;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
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
    @FXML private Button 		plusMappingButton;
    @FXML private Button 		minusMappingButton;
    @FXML private Button 		editMappingButton;
	@FXML private Label			mappingSummaryLabel;
	@FXML private TableView<MappingSet> mappingTableView;
	@FXML private Label			listTitleLabel;
	@FXML private TableView<MappingItem> listTableView;
    @FXML private Button 		plusListButton;
    @FXML private Button 		minusListButton;
    @FXML private Button 		commentButton;
    @FXML private Label			listSummaryLabel;
    
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
		assert plusMappingButton 	!= null : "fx:id=\"plusMappingButton\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert minusMappingButton	!= null : "fx:id=\"minusMappingButton\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert editMappingButton 	!= null : "fx:id=\"editMappingButton\" was not injected: check your FXML file 'Mapping.fxml'.";

		assert mappingSummaryLabel 	!= null : "fx:id=\"mappingSummaryLabel\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert mappingTableView 	!= null : "fx:id=\"mappingTableView\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert listTitleLabel 		!= null : "fx:id=\"listTitleLabel\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert listTableView 		!= null : "fx:id=\"listTableView\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert plusListButton 		!= null : "fx:id=\"plusListButton\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert minusListButton 		!= null : "fx:id=\"minusListButton\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert commentButton 		!= null : "fx:id=\"commentButton\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert listSummaryLabel 	!= null : "fx:id=\"listSummaryLabel\" was not injected: check your FXML file 'Mapping.fxml'.";

		
		mainPane.getStylesheets().add(MappingController.class.getResource("/isaac-shared-styles.css").toString());
		
		FxUtils.assignImageToButton(activeOnlyToggle, 	Images.FILTER_16.createImageView(), "Show Active Only / Show All");
		FxUtils.assignImageToButton(plusMappingButton, 	Images.PLUS.createImageView(), 		"Create Mapping Set");
		FxUtils.assignImageToButton(minusMappingButton, Images.MINUS.createImageView(), 	"Retire Mapping Set");
		FxUtils.assignImageToButton(editMappingButton, 	Images.EDIT.createImageView(), 		"Edit Mapping Set");
		FxUtils.assignImageToButton(plusListButton, 	Images.PLUS.createImageView(), 		"Create Mapping");
		FxUtils.assignImageToButton(minusListButton, 	Images.MINUS.createImageView(), 	"Retire Mapping");
		FxUtils.assignImageToButton(commentButton, 		Images.BALLOON.createImageView(), 	"View Comments");
		
		activeOnlyToggle.setSelected(true);
		
		plusMappingButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				CreateMappingSetView cv = AppContext.getService(CreateMappingSetView.class);
				cv.setMapping("New Name", "New Desc", "New Purpose");
				cv.showView(null);
			}
		});
		
		plusListButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				CreateMappingItemView itemView = AppContext.getService(CreateMappingItemView.class);
				//itemView.setMapping();
				itemView.showView(null);
			}
		});
		
		commentButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				CommentDialogView commentView = AppContext.getService(CommentDialogView.class);
				//itemView.setMapping();
				commentView.showView(null);
			}
		});
		
		for (TableColumn<MappingSet, ?> x : mappingTableView.getColumns())
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
						return new SimpleStringProperty(param.getValue().getEditorStatus().toString().trim());
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
		
		mappingTableView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<MappingSet>()
		{
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends MappingSet> c)
			{
				if (c.getList().size() == 1)
				{
					updateItemsList((MappingSet) c.getList().get(0));
				}
			}
		});
		
		for (TableColumn<MappingItem, ?> x : listTableView.getColumns())
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
					else
					{
						System.out.println(param.getTableColumn().getText());
						return new SimpleStringProperty();
						
					}
				}
			});
		}
	}
		
	private void updateItemsList(MappingSet mappingSet)
	{
		ObservableList<MappingItem> mappingItems;
		try
		{
			//mappingItems = FXCollections.observableList(MappingDataAccess.getMappingItems(mappingSetId));
			mappingItems = FXCollections.observableList(mappingSet.getMappingItems());
		}
		catch (IOException e)
		{
			LOG.error("unexpected", e);
			//TODO GUI prompt;
			mappingItems = FXCollections.observableArrayList();
		}
		listTableView.setItems(mappingItems);
	}

	public AnchorPane getRoot()	{
		return mainPane;
	}
	
	protected void readData()
	{
		ObservableList<MappingSet> mappingSets;
		boolean activeOnly = false;
		//TODO retrieve active only flag
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
		mappingTableView.setItems(mappingSets);
	}
}
