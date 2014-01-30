package gov.va.isaac.gui.importview;

import gov.va.isaac.gui.AppContext;
import gov.va.isaac.model.InformationModelType;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import gov.va.models.cem.importer.CEMImporter;
import java.io.File;

/**
 * A GUI for handling imports.
 *
 * @author ocarlsen
 */
public class ImportView extends GridPane {

    private static final Logger LOG = LoggerFactory.getLogger(ImportView.class);

    private final Label modelTypeLabel = new Label();
    private final Label fileNameLabel = new Label();

    private InformationModelType modelType;
    private String fileName;

    public ImportView() {
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

    public void doImport(final AppContext appContext, InformationModelType modelType, final String fileName) {
        this.modelType = Preconditions.checkNotNull(modelType);
        this.fileName = Preconditions.checkNotNull(fileName);

        // Update UI.
        modelTypeLabel.setText(modelType.getDisplayName());
        fileNameLabel.setText(fileName);

        // Do work in background.
        Task<String> task = new Task<String>() {

            @Override
            protected String call() throws Exception {
                @SuppressWarnings("unused")
                BdbTerminologyStore dataStore = appContext.getDataStore();

                // In Process: Implement by Alo.
                CEMImporter ci = new CEMImporter();
                ci.ImportCEMModel(new File(fileName), appContext);
                
                return "Ended import of: " + ImportView.this.modelType;
            }

            @Override
            protected void succeeded() {
                String result = this.getValue();

                // TODO: Implement by Alo/Dan.
                System.out.println(result);
            }

            @Override
            protected void failed() {
                Throwable ex = getException();
                String title = ex.getClass().getName();
                String msg = String.format("Unexpected error importing from file \"%s\"", ImportView.this.fileName);
                LOG.error(msg, ex);
                appContext.getAppUtil().showErrorDialog(title, msg, ex.getMessage());
            }
        };

        Thread t = new Thread(task, "Importer_" + modelType);
        t.setDaemon(true);
        t.start();
    }
}
