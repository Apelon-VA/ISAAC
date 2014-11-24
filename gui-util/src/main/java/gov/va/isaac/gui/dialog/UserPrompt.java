package gov.va.isaac.gui.dialog;

import gov.va.isaac.gui.util.FxUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public abstract class UserPrompt {
	public enum UserPromptResponse { APPROVE, CANCEL };
	protected UserPromptResponse buttonSelected = UserPromptResponse.CANCEL;
	protected String cancelButtonText = "Cancel";
	
	private double currentMax = 0;
	protected Label currentMaxLabel = null;

	protected VBox vb;
	protected Prompt prompt;
	private String approvalString;
	
	protected static Font boldFont = new Font("System Bold", 13.0);

	protected UserPrompt(String string) {
		this.approvalString = string;
	}
	protected static class Prompt extends Stage {
		
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

	abstract protected boolean isSelectedValuesValid();
	abstract protected Node createUserInterface();
	abstract protected void displayInvalidMessage();
	
	public UserPromptResponse showUserPrompt(Stage owner, String title) {
		 	vb = new VBox(10);
		    vb.setAlignment(Pos.CENTER);

		    Scene scene = new Scene( vb );
		    prompt = new Prompt( title, owner, scene);
		    vb.setPadding( new Insets(10,10,10,10) );
		    vb.setSpacing( 10 );
		    
		    HBox buttonHBox = new HBox(15);
		    buttonHBox.setPadding(new Insets(15));
		    buttonHBox.setAlignment( Pos.CENTER );


			Button commitButton = new Button( approvalString );
		    commitButton.setOnAction((e) -> {
		    		if (isSelectedValuesValid()) {
			            prompt.close();
			            buttonSelected = UserPromptResponse.APPROVE;
		    		} else {
		    			displayInvalidMessage();
		    		}
		    } );

		    Button cancelButton = new Button(cancelButtonText);
		    cancelButton.setOnAction((e) -> {
		            prompt.close();
		            buttonSelected = UserPromptResponse.CANCEL;
		    } );
		    
		    buttonHBox.getChildren().addAll(commitButton, cancelButton);
		    vb.getChildren().addAll(createUserInterface(), buttonHBox);

		    prompt.showDialog();

		    return buttonSelected;
	}
	
	public UserPromptResponse getButtonSelected() {
		return buttonSelected;
	}

	protected Label createLabel(String str) {
		Label l = new Label(str);
		l.setFont(boldFont);
		
		if (FxUtils.calculateNecessaryWidthOfBoldLabel(l) > currentMax) {
			currentMax = FxUtils.calculateNecessaryWidthOfBoldLabel(l);
			currentMaxLabel = l;
		};
		
		return l;
	}

	protected Label createLabel(String str, int fontSize) {
		Label l = new Label(str);
		Font f = new Font("System Bold", fontSize);
		l.setFont(f);
		
		return l;
	}

}
