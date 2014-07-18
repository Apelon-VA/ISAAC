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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.gui.searchview;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.dragAndDrop.ConceptIdProvider;
import gov.va.isaac.gui.dragAndDrop.DragRegistry;
import gov.va.isaac.search.CompositeSearchResult;
import gov.va.isaac.search.SearchHandle;
import gov.va.isaac.search.SearchHandler;
import gov.va.isaac.util.CommonMenus;
import gov.va.isaac.util.TaskCompleteCallback;
import gov.va.isaac.util.WBUtility;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller class for the Search View.
 * <p>
 * Logic has been mostly copied from LEGO {@code SnomedSearchController}.
 * Original author comments are in "quotes".
 *
 * @author ocarlsen
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */

public class SearchViewController implements TaskCompleteCallback {

    private static final Logger LOG = LoggerFactory.getLogger(SearchViewController.class);

    @FXML private Button searchButton;
    @FXML private ProgressIndicator searchProgress;
    @FXML private TextField searchText;
    @FXML private ListView<CompositeSearchResult> searchResults;
    @FXML private BorderPane borderPane;

    private final BooleanProperty searchRunning = new SimpleBooleanProperty(false);
    private SearchHandle ssh = null;


    public static SearchViewController init() throws IOException {
        // Load from FXML.
        URL resource = SearchViewController.class.getResource("SearchView.fxml");
        FXMLLoader loader = new FXMLLoader(resource);
        loader.load();
        return loader.getController();
    }

    @FXML
    public void initialize() {
        borderPane.getStylesheets().add(SearchViewController.class.getResource("/isaac-shared-styles.css").toString());
    
        searchResults.setCellFactory(new Callback<ListView<CompositeSearchResult>, ListCell<CompositeSearchResult>>() {
            @Override
            public ListCell<CompositeSearchResult> call(ListView<CompositeSearchResult> arg0) {
                return new ListCell<CompositeSearchResult>() {
                    @Override
                    protected void updateItem(final CompositeSearchResult item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty) {
                            VBox box = new VBox();
                            box.setFillWidth(true);
                            final ConceptVersionBI wbConcept = item.getConcept();
                            String preferredText = (wbConcept != null ? WBUtility.getDescription(wbConcept) : "error - see log");
                            Label concept = new Label(preferredText);
                            concept.getStyleClass().add("boldLabel");
                            box.getChildren().add(concept);

                            for (String s : item.getMatchStrings()) {
                                if (s.equals(preferredText)) {
                                    continue;
                                }
                                Label matchString = new Label(s);
                                VBox.setMargin(matchString, new Insets(0.0, 0.0, 0.0, 10.0));
                                box.getChildren().add(matchString);
                            }
                            setGraphic(box);

                            ContextMenu cm = new ContextMenu();

                            CommonMenus.addCommonMenus(cm, null, new ConceptIdProvider()
                            {
                                @Override
                                public String getConceptId()
                                {
                                    return item.getConceptNid() + "";
                                }

                                /**
                                 * @see gov.va.isaac.gui.dragAndDrop.ConceptIdProvider#getConceptUUID()
                                 */
                                @Override
                                public UUID getConceptUUID()
                                {
                                    return item.getConcept().getPrimordialUuid();
                                }

                                /**
                                 * @see gov.va.isaac.gui.dragAndDrop.ConceptIdProvider#getNid()
                                 */
                                @Override
                                public Integer getNid()
                                {
                                    return item.getConceptNid();
                                }
                            });

                            setContextMenu(cm);

                            // Also show concept details on double-click.
                            setOnMouseClicked(new EventHandler<MouseEvent>() {
                                @Override
                                public void handle(MouseEvent mouseEvent) {
                                    if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                                        if (mouseEvent.getClickCount() == 2) {
                                            AppContext.getCommonDialogs().showConceptDialog(
                                                    wbConcept.getUUIDs().get(0));
                                        }
                                    }
                                }
                            });
                        }
                        else
                        {
                            setText("");
                            setGraphic(null);
                        }
                    }
                };
            }
        });

        AppContext.getService(DragRegistry.class).setupDragOnly(searchResults, new ConceptIdProvider()
        {
            @Override
            public String getConceptId()
            {
                CompositeSearchResult dragItem = searchResults.getSelectionModel().getSelectedItem();
                if (dragItem != null)
                {
                    return dragItem.getConceptNid() + "";
                }
                return null;
            }
        });

        final BooleanProperty searchTextValid = new SimpleBooleanProperty(false);
        searchProgress.visibleProperty().bind(searchRunning);
        searchButton.disableProperty().bind(searchTextValid.not());

        // Perform search or cancel when button pressed.
        searchButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (searchRunning.get() && ssh != null) {
                    ssh.cancel();
                } else {
                    search();
                }
            }
        });

        // Change button text while search running.
        searchRunning.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable,
                    Boolean oldValue, Boolean newValue) {
                if (searchRunning.get()) {
                    searchButton.setText("Cancel");
                } else {
                    searchButton.setText("Search");
                }

            }
        });

        // Perform search on Enter keypress.
        searchText.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (searchTextValid.getValue() && ! searchRunning.get()) {
                    search();
                }
            }
        });

        // Search text must be greater than one character.
        searchText.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                if (newValue.length() > 1) {
                    searchTextValid.set(true);
                } else {
                    searchTextValid.set(false);
                }
            }
        });
    }
    
    public BorderPane getRoot() {
        return borderPane;
    }

    @Override
    public void taskComplete(long taskStartTime, Integer taskId) {

        // Run on JavaFX thread.
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    if (! ssh.isCancelled()) {
                        searchResults.getItems().addAll(ssh.getResults());
                    }
                } catch (Exception ex) {
                    String title = "Unexpected Search Error";
                    LOG.error(title, ex);
                    AppContext.getCommonDialogs().showErrorDialog(title,
                            "There was an unexpected error running the search",
                            ex.toString());
                    searchResults.getItems().clear();
                } finally {
                    searchRunning.set(false);
                }
            }
        });
    }

    private synchronized void search() {
        // Sanity check if search already running.
        if (searchRunning.get()) {
            return;
        }

        searchRunning.set(true);
        searchResults.getItems().clear();
        // "we get called back when the results are ready."
        ssh = SearchHandler.conceptSearch(searchText.getText(), this);
    }
}
