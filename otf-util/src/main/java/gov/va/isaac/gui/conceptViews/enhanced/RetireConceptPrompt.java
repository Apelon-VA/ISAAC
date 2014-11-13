package gov.va.isaac.gui.conceptViews.enhanced;

import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.util.WBUtility;

import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;

public class RetireConceptPrompt {
	public enum RetireConceptResponse { COMMIT, CANCEL };

	private static ComboBox<SimpleDisplayConcept> retirementConcepts = new ComboBox<SimpleDisplayConcept>();
	private static RetireConceptResponse buttonSelected = RetireConceptResponse.CANCEL;

	private static Font boldFont = new Font("System Bold", 13.0);

	static class Prompt extends Stage {
	
		public Prompt( String title, Stage owner, Scene scene) {
		    setTitle( title );
		    initStyle( StageStyle.UTILITY );
		    initModality( Modality.APPLICATION_MODAL );
		    initOwner( owner );
		    setResizable( false );
		    setScene( scene );
		}
		public void showDialog() {
		    sizeToScene();
		    centerOnScreen();
		    showAndWait();
		}
	}

	public static void retireConcept(Stage owner, String title) {
	    VBox vb = new VBox(10);
	    vb.setAlignment(Pos.CENTER);

	    Scene scene = new Scene( vb );
	    final Prompt prompt = new Prompt( title, owner, scene);
	    vb.setPadding( new Insets(10,10,10,10) );
	    vb.setSpacing( 10 );
	    
	    
	    Button commitButton = new Button( "Commit" );
	    commitButton.setOnAction((e) -> {
            prompt.close();
            buttonSelected = RetireConceptResponse.COMMIT;
	    } );

	    Button cancelButton = new Button( "Cancel" );
	    cancelButton.setOnAction((e) -> {
	            prompt.close();
	            buttonSelected = RetireConceptResponse.CANCEL;
	    } );
	    
	    HBox buttonHBox = new HBox(15);
	    buttonHBox.setPadding(new Insets(15));
	    buttonHBox.setAlignment( Pos.CENTER );
	    buttonHBox.getChildren().addAll(commitButton, cancelButton);
	    
	    vb.getChildren().addAll(createGridPane(), buttonHBox);
	    prompt.showDialog();
	}

	private static GridPane createGridPane() {
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


	private static Label createLabel(String str) {
		Label l = new Label(str);
		l.setFont(boldFont);

		return l;
	}

	public static int getRetirementConceptNid() {
		return retirementConcepts.getSelectionModel().getSelectedItem().getNid();
	}

	public static RetireConceptResponse getButtonSelected() {
		return buttonSelected;
	}
}
