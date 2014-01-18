package gov.va.isaac.gui.importview;

import gov.va.isaac.gui.AppContext;
import gov.va.isaac.model.InformationModelType;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import com.google.common.base.Preconditions;

/**
 * A GUI for handling imports.
 *
 * @author ocarlsen
 */
public class ImportView extends GridPane {

    private final Label modelTypeLabel = new Label();
    private final Label fileNameLabel = new Label();

    @SuppressWarnings("unused") private InformationModelType modelType;
    @SuppressWarnings("unused") private String fileName;

    public ImportView(AppContext appContext) {
        super();

        // GUI placeholders.
        add(new Label("Information Model: "), 0, 0);
        add(modelTypeLabel, 1, 0);
        add(new Label("File Name: "), 0, 1);
        add(fileNameLabel, 1, 1);

        // Set minimum dimensions.
        setMinHeight(400);
        setMinWidth(400);
    }

    public void setVariables(InformationModelType modelType, String fileName) {
        this.modelType = Preconditions.checkNotNull(modelType);
        this.fileName = Preconditions.checkNotNull(fileName);

        // Update UI.
        modelTypeLabel.setText(modelType.getDisplayName());
        fileNameLabel.setText(fileName);
    }

    public void doWork() {
        // TODO: Implement by Alo/Dan.
        System.out.println("TODO");
    }
}
