package gov.va.isaac.gui.mapping;

import java.io.IOException;
import java.net.URL;

import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.gui.views.PopupViewI;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@PerLookup
public class CreateMappingItemView implements PopupViewI{

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private CreateMappingItemController controller;

	public CreateMappingItemView() throws IOException {
		super();
		URL resource = MappingController.class.getResource("CreateMappingItem.fxml");
		FXMLLoader loader = new FXMLLoader(resource);
		loader.load();
		controller = loader.getController();
	}
	
	@Override
    public void showView(Window parent) {
		Stage s = new Stage();
		s.initOwner(parent);
		s.initModality(Modality.NONE);
		s.initStyle(StageStyle.DECORATED);

		s.setScene(new Scene(controller.getRootNode()));
		s.getIcons().add(Images.CONCEPT_VIEW.getImage());
		s.getScene().getStylesheets().add(MappingController.class.getResource("/isaac-shared-styles.css").toString());
		
		// Title will change after concept is set.
		s.titleProperty().bind(controller.getTitle());
		s.show();
		//doesn't come to the front unless you do this (on linux, at least)
		Platform.runLater(() -> {s.toFront();});
	    
    }
	
	
	public void setMappingItem() {
		// Function TBD
		
		//Catch exceptions and throw up a dialog box
	}
	
}
