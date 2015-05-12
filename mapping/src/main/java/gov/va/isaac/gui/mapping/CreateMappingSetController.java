package gov.va.isaac.gui.mapping;

import java.util.List;
import java.util.UUID;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.gui.mapping.data.MappingSet;
import gov.va.isaac.gui.mapping.data.MappingSetDAO;
import gov.va.isaac.gui.mapping.data.MappingUtils;
import gov.va.isaac.gui.util.ErrorMarkerUtils;
//import gov.va.isaac.util.TaskCompleteCallback;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.ValidBooleanBinding;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
//import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
//import javafx.scene.layout.HBox;
//import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller class for the Create Mapping View.
 *
 * @author vkaloidis
 * 
 */

public class CreateMappingSetController {
	private static final Logger LOG = LoggerFactory.getLogger(MappingController.class);
	
	@FXML private BorderPane	mainPane;
	@FXML private TextField		nameInput;
	@FXML private TextArea		descInput;
	@FXML private TextField		purposeInput;
	@FXML private Button		createButton;
	@FXML private Button		cancelButton;
	@FXML private GridPane		gridPane;
	@FXML private ComboBox<SimpleDisplayConcept>	statusCombo;
	
	private Label title = new Label();
	private MappingSet mappingSet_ = null;
	
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
		assert statusCombo 			!= null : "fx:id=\"statusCombo\" was not injected: check your FXML file 'CreateMapping.fxml'.";
		
		title.setText("Create Mapping Set");;

		statusCombo.setEditable(false);
		Utility.execute(() ->
		{
			try
			{
				List<SimpleDisplayConcept> status = MappingUtils.getStatusConcepts();
				status.add(0, new SimpleDisplayConcept("No Status", Integer.MIN_VALUE));
				
				Platform.runLater(() ->
				{
					statusCombo.getItems().addAll(status);
					if (mappingSet_ != null) {
						MappingController.setComboSelection(statusCombo, mappingSet_.getEditorStatusConceptProperty().getValue(), 0); 	
					} else {
						statusCombo.getSelectionModel().select(0);
					}
					
				});
			}
			catch (Exception e1)
			{
				LOG.error("Unexpected error populating qualifier and/or status combo fields", e1);
				AppContext.getCommonDialogs().showErrorDialog("Unexpected error configuring status options.  See logs.", e1);
			}
		});
		
		final ValidBooleanBinding nameInputValid = new ValidBooleanBinding() {
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
		
		final ValidBooleanBinding descInputValid = new ValidBooleanBinding() {
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
		ErrorMarkerUtils.setupErrorMarkerAndSwap(nameInput, gridPane, nameInputValid);
		ErrorMarkerUtils.setupErrorMarkerAndSwap(descInput, gridPane, descInputValid);
		
		createButton.setDefaultButton(true);
		createButton.disableProperty().bind(nameInputValid.not().or(descInputValid.not()));
		createButton.setOnAction((event) -> {
			try
			{
				UUID statusUUID = (statusCombo.getSelectionModel().getSelectedItem().getNid() == Integer.MIN_VALUE ? null : 
					ExtendedAppContext.getDataStore().getUuidPrimordialForNid(statusCombo.getSelectionModel().getSelectedItem().getNid()));
				
				if (mappingSet_ == null) {
					mappingSet_ = MappingSetDAO.createMappingSet(nameInput.getText(), null, purposeInput.getText(), descInput.getText(), statusUUID);
					//TODO need a proper wait on index update here... - platform run later helps...
					Platform.runLater(() -> AppContext.getService(Mapping.class).refreshMappingSets());

				} else {
					// Edit mapping set
					mappingSet_.setName(nameInput.getText());
					mappingSet_.setPurpose(purposeInput.getText());
					mappingSet_.setDescription(descInput.getText());
					mappingSet_.setEditorStatusConcept(statusUUID);
					MappingSetDAO.updateMappingSet(mappingSet_);
				}
				
				createButton.getScene().getWindow().hide();
			}
			catch (Exception e)
			{
				//TODO fix this...
				e.printStackTrace();
			}
			
			
		});
		
		cancelButton.setCancelButton(true);
		cancelButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) { 
				mappingSet_ = null;
				cancelButton.getScene().getWindow().hide();
			}
		});
		cancelButton.setOnKeyPressed(new EventHandler<KeyEvent>()  {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.ENTER) {
					event.consume();
					cancelButton.fire();
				}
			}
		});
	}
	
	public void setMappingSet(MappingSet mappingSet) {
		title.setText("Edit Mapping Set");;
		mappingSet_ = mappingSet;
		nameInput.setText(mappingSet.getName());
		purposeInput.setText(mappingSet.getPurpose());
		descInput.setText(mappingSet.getDescription());
	}

}


