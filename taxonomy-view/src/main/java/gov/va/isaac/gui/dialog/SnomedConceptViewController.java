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
package gov.va.isaac.gui.dialog;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.treeview.SctTreeViewIsaacView;
import gov.va.isaac.gui.util.CopyableLabel;
import gov.va.isaac.gui.util.CustomClipboard;
import gov.va.isaac.util.WBUtility;
import java.util.ArrayList;
import java.util.UUID;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;
import org.ihtsdo.otf.tcc.api.metadata.binding.Taxonomies;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.attribute.ConceptAttributesChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.attribute.ConceptAttributesVersionDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.description.DescriptionChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.description.DescriptionVersionDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.identifier.IdentifierDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.relationship.RelationshipChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.relationship.RelationshipVersionDdo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller class for {@link SnomedConceptView}.
 *
 * @author ocarlsen
 */
public class SnomedConceptViewController {

    private static final Logger LOG = LoggerFactory.getLogger(SnomedConceptViewController.class);

    @FXML private AnchorPane anchorPane;
    @FXML private Label conceptDefinedLabel;
    @FXML private Label conceptStatusLabel;
    @FXML private TableView<StringWithRefList> descriptionsTable;
    @FXML private Label fsnLabel;
    @FXML private VBox idVBox;
    @FXML private TableView<StringWithRefList> relationshipsTable;
    @FXML private SplitPane splitPane;
    @FXML private VBox splitRight;
    @FXML private Label uuidLabel;
    @FXML private Button showInTreeButton;
    @FXML private ProgressIndicator treeViewProgress;

    private final BooleanProperty treeViewSearchRunning = new SimpleBooleanProperty(false);

    private SctTreeViewIsaacView sctTree;

    public void setConcept(ConceptChronicleDdo concept) {

        // Update text of labels.
        ConceptAttributesChronicleDdo attributeChronicle = concept.getConceptAttributes();
        final ConceptAttributesVersionDdo conceptAttributes = attributeChronicle.getVersions().get(attributeChronicle.getVersions().size() - 1);
        conceptDefinedLabel.setText(conceptAttributes.isDefined() + "");
        conceptStatusLabel.setText(conceptAttributes.getStatus().name());
        fsnLabel.setText(WBUtility.getDescription(concept));
        CopyableLabel.addCopyMenu(fsnLabel);
        uuidLabel.setText(concept.getPrimordialUuid().toString());
        CopyableLabel.addCopyMenu(uuidLabel);

        // Update action handlers.
        showInTreeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                treeViewSearchRunning.set(true);
                sctTree.showConcept(conceptAttributes.getConcept().getPrimordialUuid(), treeViewSearchRunning);
            }
        });

        Callback<TableColumn.CellDataFeatures<StringWithRefList, StringWithRef>, ObservableValue<StringWithRef>> cellValueFactory =
                new Callback<TableColumn.CellDataFeatures<StringWithRefList, StringWithRef>, ObservableValue<StringWithRef>>() {

            @Override
            public ObservableValue<StringWithRef> call(CellDataFeatures<StringWithRefList, StringWithRef> param) {
                int index = Integer.parseInt(param.getTableColumn().getId());
                StringWithRefList refList = param.getValue();
                StringWithRef ref = refList.get(index);
                return new SimpleObjectProperty<SnomedConceptViewController.StringWithRef>(ref);
            }
        };

        Callback<TableColumn<StringWithRefList, StringWithRef>, TableCell<StringWithRefList, StringWithRef>> cellFactory =
                new Callback<TableColumn<StringWithRefList, StringWithRef>, TableCell<StringWithRefList, StringWithRef>>() {

            @Override
            public TableCell<StringWithRefList, StringWithRef> call(TableColumn<StringWithRefList, StringWithRef> param) {
                return new TableCell<StringWithRefList, StringWithRef>() {

                    @Override
                    public void updateItem(final StringWithRef ref, boolean empty) {
                        super.updateItem(ref, empty);

                        if (! isEmpty()) {
                            Text text = new Text(ref.text);
                            text.wrappingWidthProperty().bind(getTableColumn().widthProperty());
                            setGraphic(text);

                            ContextMenu cm = new ContextMenu();
                            setContextMenu(cm);

                            // Menu item to copy cell text.
                            MenuItem mi0 = new MenuItem("Copy");
                            mi0.setOnAction(new EventHandler<ActionEvent>() {

                                @Override
                                public void handle(ActionEvent arg0) {
                                    CustomClipboard.set(ref.text);
                                }
                            });
                            cm.getItems().add(mi0);

                            // Menu item to view concept.
                            if (ref.uuid != null) {
                                MenuItem mi1 = new MenuItem("View Concept");
                                mi1.setOnAction(new EventHandler<ActionEvent>() {

                                    @Override
                                    public void handle(ActionEvent ignored) {
                                        AppContext.getCommonDialogs().showSnomedConceptDialog(ref.uuid);
                                    }
                                });
                                cm.getItems().add(mi1);
                            }
                        }
                    }
                };
            }
        };

        // Populate description table data model.
        for (DescriptionChronicleDdo chronicle : concept.getDescriptions()) {
            DescriptionVersionDdo description = chronicle.getVersions().get(chronicle.getVersions().size() - 1);
            StringWithRef typeRef = new StringWithRef(description.getTypeReference().getText(), description.getTypeReference().getUuid());
            StringWithRef textRef = new StringWithRef(description.getText());
            StringWithRefList row = new StringWithRefList(typeRef, textRef);
            descriptionsTable.getItems().add(row);
        }

        setupTable(new String[] { "Type", "Text" }, descriptionsTable,
                cellValueFactory, cellFactory);

        // Add context menu items for additional identifiers.
        for (final IdentifierDdo id : attributeChronicle.getAdditionalIds()) {

            MenuItem mi = new MenuItem("View Concept");
            mi.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent ignore) {
                    AppContext.getCommonDialogs().showSnomedConceptDialog(id.getAuthorityRef().getUuid());
                }
            });

            CopyableLabel l = new CopyableLabel(id.getAuthorityRef().getText());
            l.getContextMenu().getItems().add(mi);
            l.getStyleClass().add("boldLabel");

            HBox hbox = new HBox();
            hbox.getChildren().add(l);
            hbox.getChildren().add(new CopyableLabel(id.getDenotation().toString()));
            hbox.setSpacing(5.0);

            idVBox.getChildren().add(hbox);
        }

        // Populate relationship table data model.
        for (RelationshipChronicleDdo chronicle : concept.getOriginRelationships()) {
            RelationshipVersionDdo relationship = chronicle.getVersions().get(chronicle.getVersions().size() - 1);
            StringWithRef typeRef = new StringWithRef(relationship.getTypeReference().getText(), relationship.getTypeReference().getUuid());
            StringWithRef destRef = new StringWithRef(relationship.getDestinationReference().getText(), relationship.getDestinationReference().getUuid());
            StringWithRefList row = new StringWithRefList(typeRef, destRef);
            relationshipsTable.getItems().add(row);
        }

        setupTable(new String[] { "Type", "Destination" }, relationshipsTable,
                cellValueFactory, cellFactory);

        treeViewProgress.visibleProperty().bind(treeViewSearchRunning);

        // Load the inner tree view.
        try {
            sctTree = AppContext.getService(SctTreeViewIsaacView.class); 
            sctTree.init(Taxonomies.SNOMED.getUuids()[0]);
            Region r = sctTree.getView();
            splitRight.getChildren().add(r);
            VBox.setVgrow(r, Priority.ALWAYS);
            treeViewSearchRunning.set(true);
            sctTree.showConcept(concept.getPrimordialUuid(), treeViewSearchRunning);
        } catch (Exception ex) {
            LOG.error("Error creating tree view", ex);
            splitRight.getChildren().add(new Label("Unexpected error building tree"));
        }
    }

    public String getTitle() {
        return fsnLabel.getText();
    }

    private void setupTable(String[] columns, TableView<StringWithRefList> tableView,
            Callback<TableColumn.CellDataFeatures<StringWithRefList, StringWithRef>, ObservableValue<StringWithRef>> cellValueFactory,
            Callback<TableColumn<StringWithRefList, StringWithRef>, TableCell<StringWithRefList, StringWithRef>> cellFactory) {

        // Configure table columns.
        for (int i = 0; i < columns.length; i++) {
            TableColumn<StringWithRefList, StringWithRef> tc =
                    new TableColumn<StringWithRefList, StringWithRef>(columns[i]);
            tc.setId(i + "");
            tc.setCellValueFactory(cellValueFactory);
            tc.setCellFactory(cellFactory);

            // Bind preferred column width to function of column count.
            float colWidth = 1.0f / columns.length;
            tc.prefWidthProperty().bind(tableView.widthProperty().multiply(colWidth).subtract(5.0));

            tableView.getColumns().add(tc);
        }

        tableView.setPrefHeight(tableView.getMinHeight() + (20.0 * tableView.getItems().size()));
        tableView.setPlaceholder(new Label());
    }

    /**
     * A class encapsulating a List of StringWithRef objects.
     */
    private static final class StringWithRefList {

        private final ArrayList<StringWithRef> items = new ArrayList<StringWithRef>();

        private StringWithRefList(StringWithRef... items) {
            for (StringWithRef item : items) {
                this.items.add(item);
            }
        }

        public StringWithRef get(int index) {
            return items.get(index);
        }
    }

    /**
     * A class encapsulating text and a UUID.
     */
    private static final class StringWithRef {

        private final String text;
        private final UUID uuid;

        private StringWithRef(String text) {
            this(text, null);
        }

        private StringWithRef(String text, UUID uuid) {
            this.text = text;
            this.uuid = uuid;
        }
    }
}
