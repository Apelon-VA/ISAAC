package gov.va.isaac.dialog;

import gov.va.isaac.AppContext;
import gov.va.isaac.util.Images;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;

/**
 * A {@link Stage} which can be used to show a SNOMED concept detail view .
 *
 * @author ocarlsen
 */
public class SnomedConceptView extends Stage {

	private final SnomedConceptViewController controller;

	public SnomedConceptView(AppContext appContext, Stage owner) throws IOException {
		super();

		initOwner(owner);
		initModality(Modality.NONE);
		initStyle(StageStyle.DECORATED);

		// Load from FXML.
        URL resource = this.getClass().getResource("SnomedConceptView.fxml");
		FXMLLoader loader = new FXMLLoader(resource);
        Parent root = (Parent) loader.load();
        setScene(new Scene(root));

        getIcons().add(Images.CONCEPT_VIEW.getImage());

		this.controller = loader.getController();
		controller.setAppContext(appContext);
	}

    public void setConcept(ConceptChronicleDdo concept) {
        controller.setConcept(concept);

        // Title will change after concept is set.
        this.setTitle(controller.getTitle());
    }
}
