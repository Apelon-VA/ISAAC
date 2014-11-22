package gov.va.isaac.gui.enhancedsearchview.resulthandler;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.ConceptNode;
import gov.va.isaac.gui.dialog.UserPrompt;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class RefsetCreationPrompt extends UserPrompt {
	private TextField nameTextField = new TextField();
	private TextField descTextField = new TextField();
	private RadioButton annot = new RadioButton ("Annotation");
	private ConceptNode parentConcept;

	protected RefsetCreationPrompt() {
		super("Commit");
	}

	protected boolean isSelectedValuesValid() {
		return !nameTextField.getText().isEmpty() && 
			   !descTextField.getText().isEmpty() &&
			   parentConcept.isValid().get();
	}

	protected Node createUserInterface() {
		GridPane gp = new GridPane();
		gp.setHgap(10);
		gp.setVgap(10);
		gp.setPadding(new Insets(15));
		
		Label name = createLabel("Name");
		Label desc = createLabel("Description");
		Label type = createLabel("Type");
		Label parent = createLabel("Sememe Parent Concept");

		nameTextField.clear();
		descTextField.clear();
	    parentConcept = new ConceptNode(null, true);
	    
		ToggleGroup typeGroup = new ToggleGroup();
		RadioButton memberList = new RadioButton("Member List");
		annot.setToggleGroup(typeGroup);
		memberList.setToggleGroup(typeGroup);
		memberList.setSelected(true);
		HBox typeHBox = new HBox(5);
		typeHBox.getChildren().addAll(annot, memberList);

		gp.setConstraints(currentMaxLabel,  0,  0,  1,  1,  HPos.RIGHT,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);
		gp.setConstraints(descTextField,  1,  0,  1,  1,  HPos.CENTER,  VPos.CENTER, Priority.ALWAYS, Priority.ALWAYS);

		gp.addRow(0, name, nameTextField);
		gp.addRow(1, desc, descTextField);
		gp.addRow(2, type, typeHBox);
		gp.addRow(3, parent, parentConcept.getNode());

		return gp ;
	}


	public TextField getNameTextField() {
		return nameTextField;
	}

	public TextField getDescTextField() {
		return descTextField;
	}

	public RadioButton getAnnot() {
		return annot;
	}

	public ConceptNode getParentConcept() {
		return parentConcept;
	}

	@Override
	protected void displayInvalidMessage() {
		AppContext.getCommonDialogs().showInformationDialog("Missing Information", "Must select value for each field");
	}
}
