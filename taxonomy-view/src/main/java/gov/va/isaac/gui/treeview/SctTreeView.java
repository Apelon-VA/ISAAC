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
import gov.va.isaac.constants.ISAAC;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.treeview.SctTreeItemDisplayPolicies;
import gov.va.isaac.interfaces.utility.ShutdownBroadcastListenerI;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.WBUtility;
import java.io.IOException;
import java.util.ArrayList;
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

    private final static SctTreeItemDisplayPolicies defaultDisplayPolicies = new DefaultSctTreeItemDisplayPolicies();

    /** Package access for other classes. */
    static volatile boolean shutdownRequested = false;

    private Object lock_ = new Object();
    private boolean initRun = false;
    private boolean initialLoadComplete_ = false;
    private StackPane sp_;
    private SctTreeItem rootTreeItem;
    private TreeView<TaxonomyReferenceWithConcept> treeView_;
    private SctTreeItemDisplayPolicies displayPolicies = defaultDisplayPolicies;

    protected SctTreeView() {
        treeView_ = new TreeView<>();
        AppContext.getRuntimeGlobals().registerShutdownListener(this);
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
    	if (! initRun) {
    		LOG.warn("getView() called before init() run");
    	}
        return sp_;
    }

    public void refresh() {
    	if (! initRun) {
    		init();
    	}

    	Task<Object> task = new Task<Object>() {
            @Override
            protected Object call() throws Exception {
                LOG.debug("Ensuring init completed");
                
                synchronized (lock_)
                {
                    if (!initialLoadComplete_)
                    {
                    	try {
                    		LOG.info("Waiting for init() to complete...");
                    		lock_.wait();
                    		LOG.info("Completed waiting for init() to complete");
                    	} catch (Exception e) {
                    		LOG.error("Caught " + e.getClass().getName() + " \"" + e.getLocalizedMessage() + "\" while waiting for init() to complete");
                    	}
                    }
                }
                
                LOG.debug("Finished ensuring init completed");
                return new Object();
            }

            @Override
            protected void succeeded() {
            	LOG.debug("Succeeded waiting for init() to complete");

            	if (rootTreeItem.getChildren().size() > 0) {
                	LOG.debug("Removing existing grandchildren...");
            		rootTreeItem.removeGrandchildren();
                	LOG.debug("Removed existing grandchildren.");
            	}
            	
            	LOG.debug("Removing existing children...");
            	rootTreeItem.getChildren().clear();
            	LOG.debug("Removed existing children.");
            	
            	LOG.debug("Re-adding children...");
            	rootTreeItem.addChildren();
            	LOG.debug("Re-added children.");
            }

            @Override
            protected void failed() {
                Throwable ex = getException();
                String title = "Unexpected error waiting for init() to complete";
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
    
    public void init() {
        init(ISAAC.ISAAC_ROOT.getUuids()[0]);
    }

    private void init(final UUID rootConcept) {
    	initRun = true;
    	
        // Do work in background.
        Task<ConceptChronicleDdo> task = new Task<ConceptChronicleDdo>() {

            @Override
            protected ConceptChronicleDdo call() throws Exception {
                LOG.debug("Loading concept {} as the root of a tree view", rootConcept);
                ConceptChronicleDdo rootConceptCC = ExtendedAppContext.getDataStore().getFxConcept(
                        rootConcept,
                        WBUtility.getViewCoordinate(),
                        VersionPolicy.ACTIVE_VERSIONS,
                        RefexPolicy.REFEX_MEMBERS,
                        RelationshipPolicy.ORIGINATING_AND_DESTINATION_TAXONOMY_RELATIONSHIPS);
                LOG.debug("Finished loading root concept");
                return rootConceptCC;
            }

            @Override
            protected void succeeded() {
            	LOG.debug("getFxConcept() (called by init()) succeeded");

                ConceptChronicleDdo result = this.getValue();
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
    
    private void finishTreeSetup(ConceptChronicleDdo rootConcept) {
    	LOG.debug("Running finishTreeSetup()...");
    	
        treeView_.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        treeView_.setCellFactory(new Callback<TreeView<TaxonomyReferenceWithConcept>, TreeCell<TaxonomyReferenceWithConcept>>() {
            @Override
            public TreeCell<TaxonomyReferenceWithConcept> call(TreeView<TaxonomyReferenceWithConcept> p) {
                return new SctTreeCell();
            }
        });

        TaxonomyReferenceWithConcept hiddenRootConcept = new TaxonomyReferenceWithConcept();
        SctTreeItem hiddenRootItem = new SctTreeItem(hiddenRootConcept, displayPolicies);
        treeView_.setShowRoot(false);
        treeView_.setRoot(hiddenRootItem);

        TaxonomyReferenceWithConcept visibleRootConcept = new TaxonomyReferenceWithConcept();
        visibleRootConcept.setConcept(rootConcept);

        rootTreeItem = new SctTreeItem(visibleRootConcept, displayPolicies, Images.ROOT.createImageView());

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
                        if (sourceTreeItem.shouldDisplay()) {
                        	sourceTreeItem.addChildrenConceptsAndGrandchildrenItems(p2);
                        }
                    }
                });
        sp_.getChildren().add(treeView_);
        sp_.getChildren().remove(0);  //remove the progress indicator
        synchronized (lock_)
        {
            initialLoadComplete_ = true;
            lock_.notifyAll();
        }
    }

    public void showConcept(final UUID conceptUUID, final BooleanProperty workingIndicator) {
		if (! initRun) {
    		init();
    	}

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

                SctTreeItem currentTreeItem = rootTreeItem;

                // Walk down path from root.
                boolean isLast = true;
                for (int i = pathToRoot.size() - 1; i >= 0; i--) {
                    SctTreeItem child = findChild(currentTreeItem, pathToRoot.get(i), isLast);
                    if (child == null) {
                        break;
                    }
                    currentTreeItem = child;
                    isLast = false;
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
                        answer.setExpanded(true);
                        if (! isLast) {
                            // Start fetching the next level.
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

	public void setDisplayPolicies(SctTreeItemDisplayPolicies policies) {
		this.displayPolicies = policies;
	}

	public static SctTreeItemDisplayPolicies getDefaultDisplayPolicies() {
		return defaultDisplayPolicies;
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
