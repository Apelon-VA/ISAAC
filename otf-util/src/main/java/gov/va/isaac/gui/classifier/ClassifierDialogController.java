/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.gui.classifier;

import gov.va.isaac.AppContext;
import gov.va.isaac.classifier.SnomedClassifier;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;


/**
 * Controller class for {@link ClassifierDialog}.
 *
 * @author ocarlsen
 */
public class ClassifierDialogController {

    private static final String FOLDER_SELECTION_PROPERTY = "gov.va.isaac.gui.dialog.export-settings.folder-selection";

    private static final Logger LOG = LoggerFactory.getLogger(ClassifierDialogController.class);

    @FXML private ComboBox<ConceptChronicleBI> pathCombo;

    private Stage stage;

    public void setVariables(Stage stagel) {
        this.stage = stagel;
    }

    @FXML
    public void initialize() {

        // Populate pathCombo
        
        ObservableList<ConceptChronicleBI> paths = FXCollections.observableArrayList(new ArrayList<ConceptChronicleBI>());
        try {
        	List<ConceptChronicleBI> pathConcepts = WBUtility.getPathConcepts();
        	Iterators.removeIf(pathConcepts.iterator(), new Predicate<ConceptChronicleBI>() {

				@Override
				public boolean apply(ConceptChronicleBI arg0) {
					try {
						return arg0.getVersion(WBUtility.getViewCoordinate()).getPreferredDescription().getText().startsWith(TermAux.SNOMED_CORE.getDescription() + " ");
					} catch (IOException e) {
						e.printStackTrace();
					} catch (ContradictionException e) {
						e.printStackTrace();
					}
					return false;
        		}
        		
        	});
			paths.addAll(pathConcepts);
		} catch (ValidationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ContradictionException e) {
			e.printStackTrace();
		}
        pathCombo.setItems(paths);

    }

    /**
     * Handler for ok button.
     */
    public void handleOk() {
        int pathNid = pathCombo.getValue().getConceptNid();
        //Can only classify SNOMED at this time
        try {
			if (pathNid != Snomed.SNOMED_RELEASE_PATH.getNid()) {
			    throw new UnsupportedOperationException("Classifier only supports SNOMED path at this time.");
			}
			runClassifier(pathNid);
		} catch (ValidationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

    }

    /**
     * Handler for cancel button.
     */
    public void handleCancel() {
        stage.close();
    }

    private void runClassifier(int pathNid) {

        // Do work in background.
        Task<Boolean> task = new Task<Boolean>() {

            @Override
            protected Boolean call() throws Exception {
            	SnomedClassifier classifier = new SnomedClassifier();
            	classifier.classify(pathNid);
                return true;
            }

            @Override
            protected void succeeded() {
                @SuppressWarnings("unused")
                Boolean result = this.getValue();

                // Show confirmation dialog.
                String title = "Classifier Complete";
                String message = "Success";
                AppContext.getCommonDialogs().showInformationDialog(title, message);

                stage.close();
            }

            @Override
            protected void failed() {
                Throwable ex = getException();
                String title = ex.getClass().getName();
                String message = "Unexpected error performing classification";
                LOG.warn(message, ex);
                AppContext.getCommonDialogs().showErrorDialog(title, message, ex.getMessage());
            }
        };

        // Bind cursor to task state.
        ObjectBinding<Cursor> cursorBinding = Bindings.when(task.runningProperty()).then(Cursor.WAIT).otherwise(Cursor.DEFAULT);
        stage.getScene().cursorProperty().bind(cursorBinding);

        Utility.execute(task);
    }
}
