package gov.va.isaac.gui.conceptViews.enhanced;

import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.gui.dialog.UserPrompt;

import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class RetireConceptPrompt extends UserPrompt {
	private ComboBox<SimpleDisplayConcept> retirementConcepts = new ComboBox<SimpleDisplayConcept>();

	public RetireConceptPrompt() {
		super("Commit");
	}
	
	protected Node createUserInterface() {
		GridPane gp = new GridPane();
		gp.setHgap(10);
		gp.setVgap(10);
		gp.setPadding(new Insets(15));
		
		Label instruction = createLabel("Select Parent for Concept Being Retire");
		
	    ObservableList<SimpleDisplayConcept> retirementConceptsList = FXCollections.observableArrayList(new ArrayList<SimpleDisplayConcept>());
	    retirementConceptsList.add(new SimpleDisplayConcept("Ambiguous Concept", -2141626726));
	    retirementConceptsList.add(new SimpleDisplayConcept("Duplicate Concept", -2146404342));
	    retirementConceptsList.add(new SimpleDisplayConcept("Erroneous Concept", -2145165252));
	    retirementConceptsList.add(new SimpleDisplayConcept("Limited status Concept", -2143673639));
	    retirementConceptsList.add(new SimpleDisplayConcept("Outdated Concept", -2145045405));
	    retirementConceptsList.add(new SimpleDisplayConcept("Reason not stated Concept", -2146545390));

		retirementConcepts.setItems(retirementConceptsList);
		
	    
		gp.setConstraints(instruction,  0,  0,  1,  1,  HPos.CENTER,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);

		gp.addRow(0, instruction);
		gp.addRow(1, retirementConcepts);
		
		return gp ;
	}


	public int getRetirementConceptNid() {
		return retirementConcepts.getSelectionModel().getSelectedItem().getNid();
	}
	

	@Override
	protected boolean isSelectedValuesValid() {
		// Because always returns true, if a test ever changes, need to update displayInvalidMessage() accordingly
		return true;
	}
	
	@Override
	protected void displayInvalidMessage() {
		// Empty Method as always returns True
	}
}
