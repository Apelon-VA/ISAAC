package gov.va.legoEdit.gui.legoListProperties;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.util.FxUtils;
import gov.va.legoEdit.gui.LegoGUIMasterModel;
import gov.va.legoEdit.model.LegoListByReference;
import gov.va.legoEdit.model.schemaModel.LegoList;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import gov.va.legoEdit.storage.WriteException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.UUID;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * 
 * {@link LegoListPropertiesController}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class LegoListPropertiesController implements Initializable
{
	@FXML// fx:id="rootPane"
	private AnchorPane rootPane; // Value injected by FXMLLoader
	@FXML//  fx:id="cancelButton"
	private Button cancelButton; // Value injected by FXMLLoader
	@FXML//  fx:id="legoListDescription"
	private TextField legoListDescription; // Value injected by FXMLLoader
	@FXML//  fx:id="legoListName"
	private TextField legoListName; // Value injected by FXMLLoader
	@FXML//  fx:id="legoListUUID"
	private TextField legoListUUID; // Value injected by FXMLLoader
	@FXML//  fx:id="okButton"
	private Button okButton; // Value injected by FXMLLoader
	@FXML//  fx:id="legoListComments"
	private TextArea legoListComments; // Value injected by FXMLLoader

	private TreeItem<String> ti_;
	LegoListByReference llbr_ = null;

	BooleanProperty nameValid = new SimpleBooleanProperty(false);
	BooleanProperty descValid = new SimpleBooleanProperty(false);
	BooleanBinding formValid;

	@Override
	// This method is called by the FXMLLoader when initialization is complete
	public void initialize(URL fxmlFileLocation, ResourceBundle resources)
	{
		assert cancelButton != null : "fx:id=\"cancelButton\" was not injected: check your FXML file 'LegoListProperties.fxml'.";
		assert legoListDescription != null : "fx:id=\"legoListDescription\" was not injected: check your FXML file 'LegoListProperties.fxml'.";
		assert legoListName != null : "fx:id=\"legoListName\" was not injected: check your FXML file 'LegoListProperties.fxml'.";
		assert legoListUUID != null : "fx:id=\"legoListUUID\" was not injected: check your FXML file 'LegoListProperties.fxml'.";
		assert okButton != null : "fx:id=\"okButton\" was not injected: check your FXML file 'LegoListProperties.fxml'.";

		// initialize your logic here: all @FXML variables will have been injected

		legoListName.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
			{
				//need to make sure it doesn't collide with something in the DB. Ok to collide with self.
				if (newValue.length() > 0
						&& ((llbr_ != null && llbr_.getGroupName().equals(newValue)) || BDBDataStoreImpl.getInstance().getLegoListByName(newValue) == null))
				{
					nameValid.set(true);
					legoListName.setEffect(null);
				}
				else
				{
					nameValid.set(false);
					legoListName.setEffect(FxUtils.redDropShadow);
				}
			}
		});

		legoListDescription.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
			{
				if (newValue.length() > 0)
				{
					descValid.set(true);
					legoListDescription.setEffect(null);
				}
				else
				{
					descValid.set(false);
					legoListDescription.setEffect(FxUtils.redDropShadow);
				}
			}
		});

		formValid = new BooleanBinding()
		{
			{
				bind(nameValid, descValid);
			}

			@Override
			protected boolean computeValue()
			{
				return nameValid.get() && descValid.get();
			}
		};

		cancelButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				((Stage) rootPane.getScene().getWindow()).close();
			}
		});

		okButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				if (llbr_ == null)
				{
					LegoList ll = new LegoList();
					ll.setGroupDescription(legoListDescription.getText());
					ll.setGroupName(legoListName.getText());
					ll.setLegoListUUID(legoListUUID.getText());
					ll.setComment(legoListComments.getText());
					try
					{
						AppContext.getService(LegoGUIMasterModel.class).importLegoList(ll);
					}
					catch (WriteException e)
					{
						AppContext.getCommonDialogs().showErrorDialog("Unexpected Error", "Error creating Lego List", e.toString());
					}
				}
				else
				{
					try
					{
						AppContext.getService(LegoGUIMasterModel.class).updateLegoList(llbr_, ti_, legoListName.getText(), legoListDescription.getText(),
								legoListComments.getText());
					}
					catch (WriteException e)
					{
						AppContext.getCommonDialogs().showErrorDialog("Unexpected Error", "Error updating Lego List", e.toString());
					}
				}
				((Stage) rootPane.getScene().getWindow()).close();
			}
		});

		okButton.disableProperty().bind(formValid.not());
	}

	public void setVariables(LegoListByReference llbr, TreeItem<String> ti)
	{
		ti_ = ti;
		llbr_ = llbr;
		legoListDescription.setText(llbr == null ? "" : llbr.getGroupDescription());
		legoListName.setText(llbr == null ? "" : llbr.getGroupName());
		legoListUUID.setText(llbr == null ? UUID.randomUUID().toString() : llbr.getLegoListUUID());
		legoListComments.setText(llbr == null ? "" : llbr.getComments());
	}
}
