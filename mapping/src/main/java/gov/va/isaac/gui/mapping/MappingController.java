package gov.va.isaac.gui.mapping;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.util.TaskCompleteCallback;

import java.io.IOException;
import java.net.URL;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Controller class for the Mapping View.
 *
 * @author dtriglianos
 * @author <a href="mailto:dtriglianos@apelon.com">David Triglianos</a>
 */

public class MappingController implements TaskCompleteCallback {
	private static final Logger LOG = LoggerFactory.getLogger(MappingController.class);

    @FXML private AnchorPane	mainPane;
    @FXML private AnchorPane	mappingPane;
    @FXML private AnchorPane	listPane;
    @FXML private ToggleButton 	activeOnlyToggle;
    @FXML private Button 		plusMappingButton;
    @FXML private Button 		minusMappingButton;
    @FXML private Button 		editMappingButton;
	@FXML private Label			mappingSummaryLabel;
	@FXML private TableView		mappingTableView;
	@FXML private Label			listTitleLabel;
	@FXML private TableView		listTableView;
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
				CreateMappingItemView itemView = AppContext.getService(CreateMappingItemView.class);
				//itemView.setMapping();
				itemView.showView(null);
			}
		});
		
		plusListButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				CreateMappingSetView cv = AppContext.getService(CreateMappingSetView.class);
				cv.setMapping("New Name", "New Desc", "New Purpose");
				cv.showView(null);
			}
		});
		
	}

	public AnchorPane getRoot()	{
		return mainPane;
	}

	@Override
	public void taskComplete(long taskStartTime, Integer taskId) {

		// Run on JavaFX thread.
		Platform.runLater(() -> 
		{
			try
			{
			}
			catch (Exception ex)
			{
				String title = "Unexpected Search Error";
				LOG.error(title, ex);
				AppContext.getCommonDialogs().showErrorDialog(title, "There was an unexpected error", ex.toString());
			}
			finally
			{
			}
		});
	}

}
