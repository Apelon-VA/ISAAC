package gov.va.isaac.gui;

import gov.va.isaac.AppContext;
import gov.va.isaac.util.Images;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

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
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
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

    @SuppressWarnings("unused")
    public void showConcept(final UUID conceptUUID, BooleanProperty setFalseWhenDone) {

        // Do work in background.
        Task<SctTreeItem> task = new Task<SctTreeItem>() {

            @SuppressWarnings("null")
            @Override
            protected SctTreeItem call() throws Exception {
                final ArrayList<UUID> pathToRoot = new ArrayList<>();
                pathToRoot.add(conceptUUID);

                // Walk up taxonomy to origin until no parent found.
                UUID current = conceptUUID;
                while (true) {

                    ConceptChronicleDdo concept = buildFxConcept(conceptUUID);
                    if (concept == null) {

                        // Must be a "pending concept".
                        // Not handled yet.
                        return null;
                    }

                    // Look for an IS_A relationship to origin.
                    boolean found = false;
                    for (RelationshipChronicleDdo chronicle : concept.getOriginRelationships()) {
                        RelationshipVersionDdo relationship = chronicle.getVersions().get(chronicle.getVersions().size() - 1);
                        if (relationship.getTypeReference().getUuid().equals(Snomed.IS_A)) {
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
                    SctTreeItem child = null;//TODO: findChild(currentTreeItem, pathToRoot.get(i), isLast);
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
             }

            @Override
            protected void failed() {
                Throwable ex = getException();
                LOG.warn("Unexpected error trying to find concept in Tree", ex);
            }
        };

        //TODO: Utility.tpe.execute(r);
    }

	/**
	 * Tell the tree to stop whatever threading operations it has running,
	 * since the application is exiting.
	 */
	public static void shutdown() {
		shutdownRequested = true;
	}

	/**
	 * The various {@link BdbTerminologyStore#getFxConcept()} APIs break if
     * you ask for a concept that doesn't exist.
     * Create a {@link ConceptChronicleDdo} manually here instead.
	 */
    @SuppressWarnings({ "unused", "null" })
    private ConceptChronicleDdo buildFxConcept(UUID conceptUUID)
            throws IOException, ContradictionException {

        ConceptVersionBI wbConcept = null;//TODO: WBUtility.lookupSnomedIdentifierAsCV(conceptUUID.toString());
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
