package gov.va.isaac.gui.conceptViews.enhanced;

import gov.va.isaac.AppContext;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.RefexViewI;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class EnhancedConceptDynamicRefexPopup {
	static class Prompt extends Stage {
		public Prompt( String title, Stage owner, Scene scene) {
		    setTitle( title );
		    initStyle( StageStyle.UTILITY );
		    initModality( Modality.APPLICATION_MODAL );
		    initOwner( owner );
		    setResizable( true );
		    setScene( scene );
		}
		public void showDialog() {
		    sizeToScene();
		    centerOnScreen();
		    showAndWait();
		}
	}

	public static void showDynamicRefexForConcept( Stage owner, String title, int conNid ) {
        VBox vb = new VBox(10);
        vb.setAlignment(Pos.CENTER);
        Scene scene = new Scene( vb );
	    final Prompt prompt = new Prompt( title, owner, scene);
		
        RefexViewI v = AppContext.getService(RefexViewI.class, "DynamicRefexView");
        v.setComponent(conNid, null, null, null, true);
        v.getView().setMinHeight(200.0);
        v.getView().setMinWidth(200.0);

        VBox.setVgrow(v.getView(), Priority.ALWAYS);
        VBox.setVgrow(v.getView(), Priority.ALWAYS);

	    Button closeButton = new Button( "Close" );
	    closeButton.setPadding(new Insets(10));
	    closeButton.setOnAction((e) -> {
	            prompt.close();
	    } );
	    
	    vb.getChildren().addAll(v.getView(), closeButton);
	    prompt.showDialog();
	}
	
}
