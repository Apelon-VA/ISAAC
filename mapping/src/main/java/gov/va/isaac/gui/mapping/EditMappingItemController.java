package gov.va.isaac.gui.mapping;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.gui.mapping.data.MappingItem;
import gov.va.isaac.gui.mapping.data.MappingItemDAO;
import gov.va.isaac.gui.mapping.data.MappingUtils;
import gov.va.isaac.util.Utility;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

public class EditMappingItemController {

	private static final Logger LOG = LoggerFactory.getLogger(EditMappingItemController.class);

	@FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private GridPane infoGridPane;
    @FXML private Button cancelButton;
    @FXML private Label sourceLabel;
    @FXML private Label qualifierLabel;
    @FXML private Label targetLabel;
    @FXML private AnchorPane mainPane;
    @FXML private Button saveButton;
	@FXML private ComboBox<SimpleDisplayConcept> statusCombo;

    private MappingItem mappingItem_;
    
	public Region getRootNode() {
		return mainPane;
	}
	
	public StringProperty getTitle() {
		return new SimpleStringProperty("Edit Mapping Item Status");
	}
	
    @FXML
    void initialize() {
        assert infoGridPane != null : "fx:id=\"infoGridPane\" was not injected: check your FXML file 'EditMappingItem.fxml'.";
        assert cancelButton != null : "fx:id=\"cancelButton\" was not injected: check your FXML file 'EditMappingItem.fxml'.";
        assert sourceLabel != null : "fx:id=\"sourceLabel\" was not injected: check your FXML file 'EditMappingItem.fxml'.";
        assert qualifierLabel != null : "fx:id=\"qualifierLabel\" was not injected: check your FXML file 'EditMappingItem.fxml'.";
        assert targetLabel != null : "fx:id=\"targetLabel\" was not injected: check your FXML file 'EditMappingItem.fxml'.";
        assert mainPane != null : "fx:id=\"mainPane\" was not injected: check your FXML file 'EditMappingItem.fxml'.";
        assert saveButton != null : "fx:id=\"saveButton\" was not injected: check your FXML file 'EditMappingItem.fxml'.";

		statusCombo.setEditable(false);
		
		Utility.execute(() ->
		{
			try
			{
				List<SimpleDisplayConcept> statusList = MappingUtils.getStatusConcepts();
				statusList.add(0, new SimpleDisplayConcept("NO STATUS", Integer.MIN_VALUE));
				
				Platform.runLater(() ->
				{
					statusCombo.getItems().addAll(statusList);
					statusCombo.getSelectionModel().select(0);
				});
			}
			catch (Exception e1)
			{
				LOG.error("Unexpected error populating status combo fields", e1);
				AppContext.getCommonDialogs().showErrorDialog("Unexpected error configuring status options.  See logs.", e1);
			}
		});
		
		saveButton.setDefaultButton(true);
		saveButton.setOnAction((event) -> {
			boolean saved = false;
			try	{
				UUID statusUUID = (statusCombo.getSelectionModel().getSelectedItem().getNid() == Integer.MIN_VALUE ? null : 
					ExtendedAppContext.getDataStore().getUuidPrimordialForNid(statusCombo.getSelectionModel().getSelectedItem().getNid()));
					
				mappingItem_.setEditorStatusConcept(statusUUID);
				MappingItemDAO.updateMappingItem(mappingItem_);
				saved = true;
				
			} catch (Exception e)	{
				LOG.error("unexpected", e);
				AppContext.getCommonDialogs().showInformationDialog("Cannot Update Mapping Item", e.getMessage());
			}
			
			if (saved) {
				saveButton.getScene().getWindow().hide();
			} else {
				saveButton.getScene().getWindow().requestFocus();
			}
		});
		
		cancelButton.setCancelButton(true);
		cancelButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) { 
				mappingItem_ = null;
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
    
	public void setMappingItem(MappingItem mappingItem) {
		mappingItem_ = mappingItem;
		sourceLabel.setText(mappingItem.getSourceConceptProperty().getValueSafe());
		targetLabel.setText(mappingItem.getTargetConceptProperty().getValueSafe());
		qualifierLabel.setText(mappingItem.getQualifierConceptProperty().getValueSafe());
	}
	

}
