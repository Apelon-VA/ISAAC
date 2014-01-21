package gov.va.isaac.gui.treeview;

import gov.va.isaac.gui.AppContext;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.gui.util.WBUtility;
import gov.va.isaac.util.Utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
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
public class SctTreeView extends TreeView<TaxonomyReferenceWithConcept> {

    private static final Logger LOG = LoggerFactory.getLogger(SctTreeView.class);

    /** Package access for other classes. */
    static volatile boolean shutdownRequested = false;

    private final AppContext appContext;

    private SctTreeItem rootTreeItem;

    public SctTreeView(AppContext appContext, ConceptChronicleDdo rootConcept) {
        super();
        this.appContext = appContext;

        getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        setCellFactory(new Callback<TreeView<TaxonomyReferenceWithConcept>, TreeCell<TaxonomyReferenceWithConcept>>() {
            @Override
            public TreeCell<TaxonomyReferenceWithConcept> call(TreeView<TaxonomyReferenceWithConcept> p) {
                return new SctTreeCell(SctTreeView.this.appContext);
            }
        });

        TaxonomyReferenceWithConcept hiddenRootConcept = new TaxonomyReferenceWithConcept();
        SctTreeItem hiddenRootItem = new SctTreeItem(appContext, hiddenRootConcept);
        setShowRoot(false);
        setRoot(hiddenRootItem);

        TaxonomyReferenceWithConcept visibleRootConcept = new TaxonomyReferenceWithConcept();
        visibleRootConcept.setConcept(rootConcept);

        rootTreeItem = new SctTreeItem(appContext, visibleRootConcept, Images.ROOT.createImageView());

        hiddenRootItem.getChildren().add(rootTreeItem);
        rootTreeItem.addChildren();

        // put this event handler on the root
        rootTreeItem.addEventHandler(TreeItem.branchCollapsedEvent(),
                new EventHandler<TreeItem.TreeModificationEvent<Object>>() {
                    @Override
                    public void handle(TreeItem.TreeModificationEvent<Object> t) {
                        // remove grandchildren
                        SctTreeItem sourceTreeItem = (SctTreeItem) t
                                .getSource();
                        sourceTreeItem.removeGrandchildren();
                    }
                });

        rootTreeItem.addEventHandler(TreeItem.branchExpandedEvent(),
                new EventHandler<TreeItem.TreeModificationEvent<Object>>() {
                    @Override
                    public void handle(TreeItem.TreeModificationEvent<Object> t) {
                        // add grandchildren
                        SctTreeItem sourceTreeItem = (SctTreeItem) t.getSource();
                        ProgressIndicator p2 = new ProgressIndicator();

                        p2.setSkin(new TaxonomyProgressIndicatorSkin(p2));
                        p2.setPrefSize(16, 16);
                        p2.setProgress(-1);
                        sourceTreeItem.setProgressIndicator(p2);
                        sourceTreeItem.addChildrenConceptsAndGrandchildrenItems(p2);
                    }
                });
    }

    /**
     * Tell the tree to stop whatever threading operations it has running,
     * since the application is exiting.
     */
    public static void shutdown() {
        shutdownRequested = true;
    }

    public void showConcept(final UUID conceptUUID, final BooleanProperty workingIndicator) {

        // Do work in background.
        Task<SctTreeItem> task = new Task<SctTreeItem>() {

            @Override
            protected SctTreeItem call() throws Exception {
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

                SctTreeItem currentTreeItem = rootTreeItem;

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
                    int row = getRow(lastItemFound);
                    scrollTo(row);
                    getSelectionModel().clearAndSelect(row);
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
                        scrollTo(getRow(answer));
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

        ConceptVersionBI wbConcept = WBUtility.lookupSnomedIdentifierAsCV(conceptUUID);
        if (wbConcept == null) {
            return null;
        }

        BdbTerminologyStore dataStore = appContext.getDataStore();
        ViewCoordinate viewCoordinate = StandardViewCoordinates.getSnomedInferredLatest();
        TerminologySnapshotDI snapshot = dataStore.getSnapshot(viewCoordinate);

        return new ConceptChronicleDdo(
                snapshot,
                wbConcept,
                VersionPolicy.ACTIVE_VERSIONS,
                RefexPolicy.REFEX_MEMBERS,
                RelationshipPolicy.ORIGINATING_RELATIONSHIPS);
    }
}
