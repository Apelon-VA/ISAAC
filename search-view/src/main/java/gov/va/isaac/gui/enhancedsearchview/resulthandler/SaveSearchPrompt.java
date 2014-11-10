package gov.va.isaac.gui.enhancedsearchview.resulthandler;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.util.FxUtils;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SaveSearchPrompt {
	public enum Response { SAVE, CANCEL };

	private static TextField nameTextField = new TextField();
	private static TextField descTextField = new TextField();

	private static Response buttonSelected = Response.CANCEL;

	private static Font boldFont = new Font("System Bold", 13.0);
	private static double currentMax = 0;
	private static Label currentMaxLabel = null;

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

	


	public static Response showContentGatheringDialog( Stage owner, String title ) {
	    VBox vb = new VBox();
	    Scene scene = new Scene( vb );
	    final Prompt prompt = new Prompt( title, owner, scene);
	    vb.setPadding( new Insets(10,10,10,10) );
	    vb.setSpacing( 10 );
	    
	    
	    Button commitButton = new Button( "Save" );
	    commitButton.setOnAction((e) -> {
	    		if (allValuesFilledIn()) {
		            prompt.close();
		            buttonSelected = Response.SAVE;
	    		} else {
	    			AppContext.getCommonDialogs().showInformationDialog("Missing Information", "Must select value for each field");
	    		}
	    } );

	    Button cancelButton = new Button( "Cancel" );
	    cancelButton.setOnAction((e) -> {
	            prompt.close();
	            buttonSelected = Response.CANCEL;
	    } );
	    
	    HBox buttonHBox = new HBox(15);
	    buttonHBox.setPadding(new Insets(15));
	    buttonHBox.setAlignment( Pos.CENTER );
	    buttonHBox.getChildren().addAll(commitButton, cancelButton);
	    
	    vb.getChildren().addAll(createGridPane(), buttonHBox);
	    prompt.showDialog();
	    
	    return buttonSelected;
	}
	
	private static boolean allValuesFilledIn() {
		return !nameTextField.getText().isEmpty() && 
			   !descTextField.getText().isEmpty() ;
	}

	private static GridPane createGridPane() {
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


	private static Label createLabel(String str) {
		Label l = new Label(str);
		l.setFont(boldFont);
		
		if (FxUtils.calculateNecessaryWidthOfBoldLabel(l) > currentMax) {
			currentMax = FxUtils.calculateNecessaryWidthOfBoldLabel(l);
			currentMaxLabel = l;
		};
		
		return l;
	}

	public static TextField getNameTextField() {
		return nameTextField;
	}

	public static TextField getDescTextField() {
		return descTextField;
	}

	public static Response getButtonSelected() {
		return buttonSelected;
	}
}
