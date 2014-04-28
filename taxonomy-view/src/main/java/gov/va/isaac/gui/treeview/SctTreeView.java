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
package gov.va.isaac.gui.treeview;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.utility.ShutdownBroadcastListenerI;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.WBUtility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedRelType;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.ddo.TaxonomyReferenceWithConcept;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.relationship.RelationshipChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.relationship.RelationshipVersionDdo;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.RefexPolicy;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.RelationshipPolicy;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.VersionPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link TreeView} for browsing the SNOMED CT taxonomy.
 *
 * @author kec
 * @author ocarlsen
 */
public class SctTreeView implements ShutdownBroadcastListenerI {

    private static final Logger LOG = LoggerFactory.getLogger(SctTreeView.class);

    /** Package access for other classes. */
    static volatile boolean shutdownRequested = false;

    private Object lock_ = new Object();
    private boolean initialLoadComplete_ = false;
    private StackPane sp_;
    private TreeView<TaxonomyReferenceWithConcept> treeView_;

    protected SctTreeView() {
        treeView_ = new TreeView<>();
        AppContext.getMainApplicationWindow().registerShutdownListener(this);
        sp_ = new StackPane();
        ProgressIndicator pi = new ProgressIndicator();
        pi.setMaxHeight(100.0);
        pi.setMaxWidth(100.0);
        pi.getStyleClass().add("progressIndicator");
        StackPane.setAlignment(pi, Pos.CENTER);
        sp_.getChildren().add(pi);
    }
    
    public StackPane getView()
    {
        return sp_;
    }
    
    public void init(final UUID[] rootConcepts) {

        // Do work in background.
        Task<ConceptChronicleDdo[]> task = new Task<ConceptChronicleDdo[]>() {

            @Override
            protected ConceptChronicleDdo[] call() throws Exception {
                LOG.debug("Loading concepts {} as the root of a tree view", Arrays.toString(rootConcepts));
                ConceptChronicleDdo[] result = new ConceptChronicleDdo[rootConcepts.length];
                int i = 0;
                for (UUID uuid : rootConcepts)
                {
                    result[i++] = ExtendedAppContext.getDataStore().getFxConcept(
                            uuid,
                            WBUtility.getViewCoordinate(),
                            VersionPolicy.ACTIVE_VERSIONS,
                            RefexPolicy.REFEX_MEMBERS,
                            RelationshipPolicy.ORIGINATING_AND_DESTINATION_TAXONOMY_RELATIONSHIPS);
                }
                LOG.debug("Finished loading root concepts");
                return result;
            }

            @Override
            protected void succeeded() {
                ConceptChronicleDdo[] result = this.getValue();
                SctTreeView.this.finishTreeSetup(result);
            }

            @Override
            protected void failed() {
                Throwable ex = getException();
                String title = "Unexpected error loading root concept";
                String msg = ex.getClass().getName();
                LOG.error(title, ex);
                if (!shutdownRequested)
                {
                    AppContext.getCommonDialogs().showErrorDialog(title, msg, ex.getMessage());
                }
            }
        };

        Utility.execute(task);
    }
    
    private void finishTreeSetup(ConceptChronicleDdo[] rootConcepts) {
        treeView_.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        treeView_.setCellFactory(new Callback<TreeView<TaxonomyReferenceWithConcept>, TreeCell<TaxonomyReferenceWithConcept>>() {
            @Override
            public TreeCell<TaxonomyReferenceWithConcept> call(TreeView<TaxonomyReferenceWithConcept> p) {
                return new SctTreeCell();
            }
        });

        TaxonomyReferenceWithConcept hiddenRootConcept = new TaxonomyReferenceWithConcept();
        SctTreeItem hiddenRootItem = new SctTreeItem(hiddenRootConcept);
        treeView_.setShowRoot(false);
        treeView_.setRoot(hiddenRootItem);

        
        for (ConceptChronicleDdo rootConcept : rootConcepts)
        {
            TaxonomyReferenceWithConcept visibleRootConcept = new TaxonomyReferenceWithConcept();
            visibleRootConcept.setConcept(rootConcept);
    
            SctTreeItem rootTreeItem = new SctTreeItem(visibleRootConcept, Images.ROOT.createImageView());
    
            hiddenRootItem.getChildren().add(rootTreeItem);

            rootTreeItem.addChildren();
    
            // put this event handler on the root
            rootTreeItem.addEventHandler(TreeItem.<TaxonomyReferenceWithConcept>branchCollapsedEvent(),
                    new EventHandler<TreeItem.TreeModificationEvent<TaxonomyReferenceWithConcept>>() {
                        @Override
                        public void handle(TreeItem.TreeModificationEvent<TaxonomyReferenceWithConcept> t) {
                            // remove grandchildren
                            SctTreeItem sourceTreeItem = (SctTreeItem) t
                                    .getSource();
                            sourceTreeItem.removeGrandchildren();
                        }
                    });
    
            rootTreeItem.addEventHandler(TreeItem.<TaxonomyReferenceWithConcept>branchExpandedEvent(),
                    new EventHandler<TreeItem.TreeModificationEvent<TaxonomyReferenceWithConcept>>() {
                        @Override
                        public void handle(TreeItem.TreeModificationEvent<TaxonomyReferenceWithConcept> t) {
                            // add grandchildren
                            SctTreeItem sourceTreeItem = (SctTreeItem) t.getSource();
                            ProgressIndicator p2 = new ProgressIndicator();
    
                            //TODO figure out what to do with the progress indicator
    //                        p2.setSkin(new TaxonomyProgressIndicatorSkin(p2));
                            p2.setPrefSize(16, 16);
                            p2.setProgress(-1);
                            sourceTreeItem.setProgressIndicator(p2);
                            sourceTreeItem.addChildrenConceptsAndGrandchildrenItems(p2);
                        }
                    });
        }
        sp_.getChildren().add(treeView_);
        sp_.getChildren().remove(0);  //remove the progress indicator
        synchronized (lock_)
        {
            initialLoadComplete_ = true;
            lock_.notifyAll();
        }
    }

    public void showConcept(final UUID conceptUUID, final BooleanProperty workingIndicator) {

        // Do work in background.
        Task<SctTreeItem> task = new Task<SctTreeItem>() {

            @Override
            protected SctTreeItem call() throws Exception {
                synchronized (lock_)
                {
                    if (!initialLoadComplete_)
                    {
                        lock_.wait();
                    }
                }
                final ArrayList<UUID> pathToRoot = new ArrayList<>();
                pathToRoot.add(conceptUUID);

                // Walk up taxonomy to origin until no parent found.
                UUID current = conceptUUID;
                while (true) {

                    ConceptChronicleDdo concept = buildFxConcept(current);
                    if (concept == null) {

                        // Must be a "pending concept".
                        // Not handled yet.
                        return null;
                    }

                    // Look for an IS_A relationship to origin.
                    boolean found = false;
                    for (RelationshipChronicleDdo chronicle : concept.getOriginRelationships()) {
                        RelationshipVersionDdo relationship = chronicle.getVersions().get(chronicle.getVersions().size() - 1);
                        UUID isaRelTypeUUID = SnomedRelType.IS_A.getUuids()[0];
                        if (relationship.getTypeReference().getUuid().equals(isaRelTypeUUID)) {
                            UUID parentUUID = relationship.getDestinationReference().getUuid();
                            pathToRoot.add(parentUUID);
                            current = parentUUID;
                            found = true;
                            break;
                        }
                    }

                    // No parent IS_A relationship found, stop looking.
                    if (! found) {
                        break;
                    }
                }

                //TODO fix this bad  hack - need to redo this algoritm to handle multiple roots.
                //currently  assumes SCT is the first node.
                SctTreeItem currentTreeItem = (SctTreeItem)treeView_.getRoot().getChildren().get(0);

                // Walk down path from root.
                for (int i = pathToRoot.size() - 1; i >= 0; i--) {
                    boolean isLast = (i == 0);
                    SctTreeItem child = findChild(currentTreeItem, pathToRoot.get(i), isLast);
                    if (child == null) {
                        break;
                    }
                    currentTreeItem = child;
                }

                return currentTreeItem;
            }

            @Override
            protected void succeeded() {
                final SctTreeItem lastItemFound = this.getValue();

                // Expand tree to last item found.
                if (lastItemFound != null) {
                    int row = treeView_.getRow(lastItemFound);
                    treeView_.scrollTo(row);
                    treeView_.getSelectionModel().clearAndSelect(row);
                }

                // Turn off progress indicator.
                if (workingIndicator != null) {
                    workingIndicator.set(false);
                }
            }

            @Override
            protected void failed() {
                Throwable ex = getException();
                LOG.warn("Unexpected error trying to find concept in Tree", ex);

                // Turn off progress indicator.
                if (workingIndicator != null) {
                    workingIndicator.set(false);
                }
            }
        };

        Utility.execute(task);
    }

    /**
     * Ugly nasty threading code to try to get a handle on waiting until
     * children are populated before requesting them. The first call you make to
     * this should pass in the root node, and its children should already be
     * populated. After that you can call it repeatedly to walk down the tree.
     *
     * @return the found child, or null, if not found. found child will have
     *         already been told to expand and fetch its children.
     */
    private SctTreeItem findChild(final SctTreeItem item,
            final UUID targetChildUUID, final boolean isLast) {

        // This will hold
        final ArrayList<SctTreeItem> answers = new ArrayList<>(1);

        Runnable finder = new Runnable() {

            @Override
            public void run() {
                synchronized (answers) {
                    if (item.getValue().getConcept().getPrimordialUuid().equals(targetChildUUID)) {
                        // Found it.
                        answers.add(item);
                    } else {

                        // Iterate through children and look for child with target UUID.
                        for (TreeItem<TaxonomyReferenceWithConcept> child : item.getChildren()) {
                            if (child != null
                                    && child.getValue() != null
                                    && child.getValue().getConcept() != null
                                    && child.getValue().getConcept().getPrimordialUuid().equals(targetChildUUID)) {

                                // Found it.
                                answers.add((SctTreeItem) child);
                                break;
                            }
                        }
                    }

                    if (answers.size() == 0) {
                        answers.add(null);
                    } else {
                        SctTreeItem answer = answers.get(0);
                        treeView_.scrollTo(treeView_.getRow(answer));
                        if (! isLast) {
                            // Start fetching the next level.
                            answer.setExpanded(true);
                            answer.addChildren();
                        }
                    }

                    answers.notify();
                }
            }
        };

        item.blockUntilChildrenReady();

        synchronized (answers) {
            Platform.runLater(finder);

            // Wait until finder done.
            while (answers.size() == 0) {
                try {
                    answers.wait();
                } catch (InterruptedException e) {
                    // No-op.
                }
            }
        }

        return answers.get(0);
    }

    /**
     * The various {@link BdbTerminologyStore#getFxConcept()} APIs break if
     * you ask for a concept that doesn't exist.
     * This method creates a {@link ConceptChronicleDdo} manually instead.
     */
    private ConceptChronicleDdo buildFxConcept(UUID conceptUUID)
            throws IOException, ContradictionException {

        //TODO see if this is still the case... we should be using the Fx APIs directly....
        ConceptVersionBI wbConcept = WBUtility.getConceptVersion(conceptUUID);
        if (wbConcept == null) {
            return null;
        }

        BdbTerminologyStore dataStore = ExtendedAppContext.getDataStore();
        ViewCoordinate viewCoordinate = WBUtility.getViewCoordinate();
        TerminologySnapshotDI snapshot = dataStore.getSnapshot(viewCoordinate);

        return new ConceptChronicleDdo(
                snapshot,
                wbConcept,
                VersionPolicy.ACTIVE_VERSIONS,
                RefexPolicy.REFEX_MEMBERS,
                RelationshipPolicy.ORIGINATING_RELATIONSHIPS);
    }

    /**
     * Tell the tree to stop whatever threading operations it has running,
     * since the application is exiting.
     * @see gov.va.isaac.interfaces.utility.ShutdownBroadcastListenerI#shutdown()
     */
    @Override
    public void shutdown()
    {
        shutdownRequested = true;
    }
}
