package gov.va.isaac.gui.importview;

import gov.va.isaac.gui.AppContext;
import gov.va.isaac.model.InformationModel;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import com.google.common.base.Preconditions;

/**
 * A GUI for handling imports.
 *
 * @author ocarlsen
 */
public class ImportView extends GridPane {

    private final Label informationModelLabel = new Label();
    private final Label fileNameLabel = new Label();

    private InformationModel informationModel;
    private String fileName;

    public ImportView(AppContext appContext) {
        super();

        // GUI placeholders.
        add(new Label("Information Model: "), 0, 0);
        add(informationModelLabel, 1, 0);
        add(new Label("File Name: "), 0, 1);
        add(fileNameLabel, 1, 1);

        // Set minimum dimensions.
        setMinHeight(400);
        setMinWidth(400);
    }

    public void setVariables(InformationModel informationModel, String fileName) {
        this.informationModel = Preconditions.checkNotNull(informationModel);
        this.fileName = Preconditions.checkNotNull(fileName);

        // Update UI.
        informationModelLabel.setText(informationModel.getDisplayName());
        fileNameLabel.setText(fileName);
    }

    public void doWork() {
        // TODO: Implement by Alo/Dan.
        System.out.println("TODO");
    }
}
