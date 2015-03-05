package gov.va.isaac.gui.mapping;

import gov.va.isaac.gui.util.ErrorMarkerUtils;
import gov.va.isaac.util.TaskCompleteCallback;
import gov.va.isaac.util.ValidBooleanBinding;

import java.io.IOException;
import java.net.URL;

import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller class for the Create Mapping View.
 *
 * @author vkaloidis
 * 
 */

public class CreateMappingSetController implements TaskCompleteCallback {
	private static final Logger LOG = LoggerFactory.getLogger(MappingController.class);
	
	@FXML private BorderPane	mainPane;
	@FXML private TextField		nameInput;
	@FXML private TextArea		descInput;
	@FXML private TextField		purposeInput;
	@FXML private Button		createButton;
	@FXML private Button		cancelButton;
	
	private Region region = new Region();
	private Label title = new Label();
	
	public Region getRootNode() {
		return mainPane;
	}
	
	public StringProperty getTitle() {
		return title.textProperty();
	}
	
	@FXML
	public void initialize() {
		assert mainPane 			!= null : "fx:id=\"mainPane\" was not injected: check your FXML file 'CreateMapping.fxml'.";
		assert nameInput 			!= null : "fx:id=\"nameInput\" was not injected: check your FXML file 'CreateMapping.fxml'.";
		assert descInput 			!= null : "fx:id=\"descInput\" was not injected: check your FXML file 'CreateMapping.fxml'.";
		assert purposeInput 		!= null : "fx:id=\"purposeInput\" was not injected: check your FXML file 'CreateMapping.fxml'.";
		assert createButton 		!= null : "fx:id=\"createButton\" was not injected: check your FXML file 'CreateMapping.fxml'.";
		assert cancelButton 		!= null : "fx:id=\"cancelButton\" was not injected: check your FXML file 'CreateMapping.fxml'.";
		

//		nameInput.setPromptText("CURRENT NAME"); TODO: vk EDIT MAPPING FILLS THIS OUT
//		descInput.setPromptText("CURRENT DESC"); TODO: VK EDIT MAPPING FILLS THS OUT
//		promptInput.setPromptText("CURRENT PROMPT") TODO: VK EDIT MAPPING FILLS THIS OUT 
		
		StackPane nameStack = new StackPane();
		StackPane descStack = new StackPane();
		title.setText("Create new Mapping Refset");;
		
		final ValidBooleanBinding nameInputEmpty = new ValidBooleanBinding() {
			{
				bind(nameInput.textProperty());
				setComputeOnInvalidate(true);
			}
			@Override
			protected boolean computeValue() {
				if (nameInput.getText().length() == 0) {
					setInvalidReason("You must fill out the Name Input and Description Input");
					return false;
				}
				clearInvalidReason();
				return true;
			}
		};
		
		final ValidBooleanBinding descInputEmpty = new ValidBooleanBinding() {
			{
				bind(descInput.textProperty());
				setComputeOnInvalidate(true);
			}
			@Override
			protected boolean computeValue() {
				if (descInput.getText().length() == 0) {
					setInvalidReason("You must fill out the Name Input and Description Input");
					return false;
				}
				clearInvalidReason();
				return true;
			}
		};
		ErrorMarkerUtils.setupErrorMarker(nameInput, nameStack, nameInputEmpty);
		ErrorMarkerUtils.setupErrorMarker(descInput, descStack, descInputEmpty);
		
		createButton.setDefaultButton(true);
		createButton.setPadding(new Insets(5, 20, 5, 20));
		createButton.disableProperty().bind(nameInputEmpty.not());
		createButton.disableProperty().bind(descInputEmpty.not());
		createButton.setOnAction((event) -> {
			// TODO: vk CREATE LOGIC TO PASS ON TO NEXT STEP
		});
		
		HBox.setHgrow(region, Priority.ALWAYS); //TODO: vk maybe get rid of this bc its done in fxml file
		cancelButton.setPadding(new Insets(5, 20, 5, 20));
		cancelButton.setCancelButton(true);
		cancelButton.setOnKeyPressed(new EventHandler<KeyEvent>()  {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.ENTER) {
					event.consume();
					cancelButton.fire();
				}
			}
		});
		
		HBox buttons = new HBox();
		buttons.setMaxWidth(Double.MAX_VALUE);
		buttons.setPadding(new Insets(10));
		buttons.setSpacing(5);
		buttons.getChildren().add(cancelButton);
		buttons.getChildren().add(createButton);
		
	}

	@Override
    public void taskComplete(long taskStartTime, Integer taskId) {
	    // TODO Auto-generated method stub
	    
    }
}


