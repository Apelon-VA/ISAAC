package gov.va.isaac.gui.enhancedsearchview.resulthandler;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.dialog.UserPrompt;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class SaveSearchPrompt extends UserPrompt {
	private TextField nameTextField = new TextField();
	private TextField descTextField = new TextField();

	public SaveSearchPrompt() {
		super("Save");
	}


	protected boolean isSelectedValuesValid() {
		return !nameTextField.getText().isEmpty() && 
			   !descTextField.getText().isEmpty() ;
	}

	protected Node createUserInterface() {
		GridPane gp = new GridPane();
		gp.setHgap(10);
		gp.setVgap(10);
		gp.setPadding(new Insets(15));
		
		Label name = createLabel("Name");
		Label desc = createLabel("Description");

		nameTextField.clear();
		descTextField.clear();
	    

		gp.setConstraints(currentMaxLabel,  0,  0,  1,  1,  HPos.RIGHT,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);
		gp.setConstraints(descTextField,  1,  0,  1,  1,  HPos.CENTER,  VPos.CENTER, Priority.ALWAYS, Priority.ALWAYS);

		gp.addRow(0, name, nameTextField);
		gp.addRow(1, desc, descTextField);

		return gp ;
	}


	public TextField getNameTextField() {
		return nameTextField;
	}

	public TextField getDescTextField() {
		return descTextField;
	}


	@Override
	protected void displayInvalidMessage() {
		AppContext.getCommonDialogs().showInformationDialog("Missing Information", "Must select value for each field");
	}
}
