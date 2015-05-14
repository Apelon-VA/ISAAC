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
import gov.va.isaac.config.generated.StatedInferredOptions;
import gov.va.isaac.config.profiles.UserProfile;
import gov.va.isaac.config.profiles.UserProfileBindings;
import gov.va.isaac.config.profiles.UserProfileManager;
import gov.va.isaac.constants.ISAAC;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.taxonomyView.SctTreeItemDisplayPolicies;
import gov.va.isaac.util.OTFUtility;
import gov.va.isaac.util.UpdateableBooleanBinding;
import gov.va.isaac.util.Utility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.apache.mahout.math.Arrays;
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
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
class SctTreeView {

    private static final Logger LOG = LoggerFactory.getLogger(SctTreeView.class);

    private final static SctTreeItemDisplayPolicies defaultDisplayPolicies = new DefaultSctTreeItemDisplayPolicies();

    private static volatile boolean shutdownRequested = false;

    // initializationCountDownLatch_ begins with count of 2, indicating init() not yet run
    // initializationCountDownLatch_ count is decremented to 1 during init, indicating that init() started
    // initializationCountDownLatch_ count is decremented to 0 upon completion of init
    //
    // Calls to init() while count is less than 2 return immediately
    // Methods requiring that init() be completed must run init() if count > 1 and block on await()
    private final CountDownLatch initializationCountDownLatch_ = new CountDownLatch(2);

    private BorderPane bp_;
    private StackPane sp_;
    private ToolBar tb_ = new ToolBar();
    private SctTreeItem rootTreeItem;
    private TreeView<TaxonomyReferenceWithConcept> treeView_;
    private SctTreeItemDisplayPolicies displayPolicies = defaultDisplayPolicies;
    
    @SuppressWarnings("unused")
    private UpdateableBooleanBinding refreshRequiredListenerHack;

    private Optional<UUID> selectedItem_ = Optional.empty();
    private ArrayList<UUID> expandedUUIDs_ = new ArrayList<>();
    
    SctTreeView() {
        long startTime = System.currentTimeMillis();
        treeView_ = new TreeView<>();
        bp_ = new BorderPane();
        
        Button descriptionType = new Button();
        descriptionType.setPadding(new Insets(2.0));
        ImageView displayFsn = Images.DISPLAY_FSN.createImageView();
        Tooltip.install(displayFsn, new Tooltip("Displaying the Fully Specified Name - click to display the Preferred Term"));
        displayFsn.visibleProperty().bind(AppContext.getService(UserProfileBindings.class).getDisplayFSN());
        ImageView displayPreferred = Images.DISPLAY_PREFERRED.createImageView();
        displayPreferred.visibleProperty().bind(AppContext.getService(UserProfileBindings.class).getDisplayFSN().not());
        Tooltip.install(displayPreferred, new Tooltip("Displaying the Preferred Term - click to display the Fully Specified Name"));
        descriptionType.setGraphic(new StackPane(displayFsn, displayPreferred));
        descriptionType.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                try
                {
                    UserProfile up = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
                    up.setDisplayFSN(AppContext.getService(UserProfileBindings.class).getDisplayFSN().not().get());
                    ExtendedAppContext.getService(UserProfileManager.class).saveChanges(up);
                }
                catch (Exception e)
                {
                    LOG.error("Unexpected error storing pref change", e);
                }
            }
        });
        
        tb_.getItems().add(descriptionType);
        
        Button taxonomyViewMode = new Button();
        taxonomyViewMode.setPadding(new Insets(2.0));
        ImageView taxonomyInferred = Images.TAXONOMY_INFERRED.createImageView();
        taxonomyInferred.visibleProperty().bind(AppContext.getService(UserProfileBindings.class).getStatedInferredPolicy().isEqualTo(StatedInferredOptions.INFERRED));
        Tooltip.install(taxonomyInferred, new Tooltip("Displaying the Inferred view- click to display the Inferred then Stated view"));
        ImageView taxonomyStated = Images.TAXONOMY_STATED.createImageView();
        taxonomyStated.visibleProperty().bind(AppContext.getService(UserProfileBindings.class).getStatedInferredPolicy().isEqualTo(StatedInferredOptions.STATED));
        Tooltip.install(taxonomyStated, new Tooltip("Displaying the Stated view- click to display the Inferred view"));
        ImageView taxonomyInferredThenStated = Images.TAXONOMY_INFERRED_THEN_STATED.createImageView();
        taxonomyInferredThenStated.visibleProperty().bind(AppContext.getService(UserProfileBindings.class).getStatedInferredPolicy().isEqualTo(StatedInferredOptions.INFERRED_THEN_STATED));
        Tooltip.install(taxonomyInferredThenStated, new Tooltip("Displaying the Inferred then Stated view- click to display the Stated view"));
        taxonomyViewMode.setGraphic(new StackPane(taxonomyInferred, taxonomyStated, taxonomyInferredThenStated));
        taxonomyViewMode.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                try
                {
                    UserProfile up = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
                    StatedInferredOptions sip = null;
                    if (AppContext.getService(UserProfileBindings.class).getStatedInferredPolicy().get() == StatedInferredOptions.STATED)
                    {
                        sip = StatedInferredOptions.INFERRED;
                    }
                    else if (AppContext.getService(UserProfileBindings.class).getStatedInferredPolicy().get() == StatedInferredOptions.INFERRED)
                    {
                        sip = StatedInferredOptions.INFERRED_THEN_STATED;
                    }
                    else if (AppContext.getService(UserProfileBindings.class).getStatedInferredPolicy().get() == StatedInferredOptions.INFERRED_THEN_STATED)
                    {
                        sip = StatedInferredOptions.STATED;
                    }
                    else
                    {
                        LOG.error("Unexpected error!");
                        return;
                    }
                    up.setStatedInferredPolicy(sip);
                    ExtendedAppContext.getService(UserProfileManager.class).saveChanges(up);
                }
                catch (Exception e)
                {
                    LOG.error("Unexpected error storing pref change", e);
                }
            }
        });
        tb_.getItems().add(taxonomyViewMode);
        
        bp_.setTop(tb_);
        
        sp_ = new StackPane();
        bp_.setCenter(sp_);
        ProgressIndicator pi = new ProgressIndicator();
        pi.setMaxHeight(100.0);
        pi.setMaxWidth(100.0);
        pi.getStyleClass().add("progressIndicator");
        StackPane.setAlignment(pi, Pos.CENTER);
        sp_.getChildren().add(pi);
        LOG.debug("Tree View construct time: {}", System.currentTimeMillis() - startTime);
    }
    
    public SctTreeItem getRoot() {
        return rootTreeItem;
    }
    
    public BorderPane getView()
    {
        if (initializationCountDownLatch_.getCount() > 1) {
            LOG.debug("getView() called before initial init() started");
        } else if (initializationCountDownLatch_.getCount() > 0) {
            LOG.debug("getView() called before initial init() completed");
        }
        return bp_;
    }
    
    /**
     * Convenience method for other code to add buttons, etc to the tool bar displayed above
     * the tree view 
     * @param node
     */
    public void addToToolBar(Node node)
    {
        tb_.getItems().add(node);
    }

    public void refresh() {
        if (initializationCountDownLatch_.getCount() > 1) {
            // called before initial init() run, so run init()
            init();
        }

        Task<Object> task = new Task<Object>() {
            @Override
            protected Object call() throws Exception {
                // Waiting to ensure that init() completed
                initializationCountDownLatch_.await();
                return new Object();
            }

            @Override
            protected void succeeded() {
                LOG.debug("Succeeded waiting for init() to complete");

             // record which items are expanded
                saveExpanded();
                
                LOG.debug("Removing existing children...");
                rootTreeItem.clearChildren();
                rootTreeItem.resetChildrenCalculators();
                LOG.debug("Removed existing children.");
                
                LOG.debug("Re-adding children...");
                Utility.execute(() -> rootTreeItem.addChildren());
                restoreExpanded();
                
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

    private synchronized void init(final UUID rootConcept) {
        if (initializationCountDownLatch_.getCount() == 0) {
            LOG.debug("Ignoring call to init({}) after previous init() already completed", rootConcept);
            return;
        } else if (initializationCountDownLatch_.getCount() <= 1) {
            LOG.debug("Ignoring call to init({}) while initial init() still running", rootConcept);
            return;
        } else if (initializationCountDownLatch_.getCount() == 2) {
            initializationCountDownLatch_.countDown();
            LOG.debug("Performing initial init({})", rootConcept);
        } else {
            // this should never happen
            throw new RuntimeException("SctTreeView initializationCountDownLatch_ has unexpected count " + initializationCountDownLatch_.getCount() + " which is not 0, 1 or 2");
        }

        // Do work in background.
        Task<ConceptChronicleDdo> task = new Task<ConceptChronicleDdo>() {

            @Override
            protected ConceptChronicleDdo call() throws Exception {
                LOG.debug("Loading concept {} as the root of a tree view", rootConcept);
                ConceptChronicleDdo rootConceptCC = ExtendedAppContext.getDataStore().getFxConcept(
                        rootConcept,
                        OTFUtility.getViewCoordinate(),
                        VersionPolicy.ACTIVE_VERSIONS,
                        RefexPolicy.REFEX_MEMBERS,
                        RelationshipPolicy.ORIGINATING_AND_DESTINATION_TAXONOMY_RELATIONSHIPS);
                LOG.debug("Finished loading root concept");
                
                if (rootConceptCC.getDestinationRelationships().size() == 0) {
                    LOG.warn("ROOT CONCEPT {} HAS NO DESTINATION RELATIONSHIPS.  MAY BE A PROBLEM WITH VIEWCOORDINATE RELATIONSHIP ASSERTION TYPE ({})", OTFUtility.getDescription(rootConceptCC), OTFUtility.getViewCoordinate().getRelationshipAssertionType());
                }
                return rootConceptCC;
            }

            @Override
            protected void succeeded()
            {
                LOG.debug("getFxConcept() (called by init()) succeeded");

                ConceptChronicleDdo result = this.getValue();
                SctTreeView.this.finishTreeSetup(result);

                refreshRequiredListenerHack = new UpdateableBooleanBinding()
                {
                    private volatile boolean enabled = false;
                    private volatile AtomicBoolean refreshQueued = new AtomicBoolean(false);
                    {
                        setComputeOnInvalidate(true);
                        addBinding(AppContext.getService(UserProfileBindings.class).getViewCoordinatePath(), 
                                AppContext.getService(UserProfileBindings.class).getDisplayFSN(), 
                                AppContext.getService(UserProfileBindings.class).getStatedInferredPolicy());
                        enabled = true;
                    }

                    @Override
                    protected boolean computeValue()
                    {
                        if (!enabled)
                        {
                            LOG.debug("Skip initial spurious refresh calls");
                            return false;
                        }
                        synchronized (refreshQueued)
                        {
                            if (refreshQueued.get())
                            {
                                LOG.info("Skip tree refresh() due to pending refresh");
                                return false;
                            }
                            else
                            {
                                refreshQueued.set(true);
                                LOG.debug("Kicking off tree refresh() due to change of an observed user property");
                                Utility.schedule(() -> 
                                {
                                    Platform.runLater(() -> 
                                    {
                                        try
                                        {
                                            synchronized (refreshQueued)
                                            {
                                                refreshQueued.set(false);
                                            }
                                            
                                            SctTreeView.this.refresh();
                                        }
                                        catch (Exception e)
                                        {
                                            LOG.error("Unexpected error running refresh", e);
                                        }
                                    });
                                }, 10, TimeUnit.MILLISECONDS);
                            }
                        }
                        return false;
                    }
                };
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
    
    /**
     * @param rootConcept
     * 
     * This method should be called only by init() and only a single time.
     * The only reason this is its own method is to make the init() more readable.
     * 
     */
    private void finishTreeSetup(ConceptChronicleDdo rootConcept) {
        LOG.debug("Running finishTreeSetup()...");
        
        treeView_.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        treeView_.setCellFactory(new Callback<TreeView<TaxonomyReferenceWithConcept>, TreeCell<TaxonomyReferenceWithConcept>>() {
            @Override
            public TreeCell<TaxonomyReferenceWithConcept> call(TreeView<TaxonomyReferenceWithConcept> p) {
                return new SctTreeCell();
            }
        });

        TaxonomyReferenceWithConcept visibleRootConcept = new TaxonomyReferenceWithConcept();
        visibleRootConcept.setConcept(rootConcept);

        rootTreeItem = new SctTreeItem(visibleRootConcept, displayPolicies, Images.ROOT.createImageView());

        treeView_.setRoot(rootTreeItem);
        Utility.execute(() -> rootTreeItem.addChildren());

        // put this event handler on the root
        rootTreeItem.addEventHandler(TreeItem.<TaxonomyReferenceWithConcept>branchCollapsedEvent(),
                new EventHandler<TreeItem.TreeModificationEvent<TaxonomyReferenceWithConcept>>() {
                    @Override
                    public void handle(TreeItem.TreeModificationEvent<TaxonomyReferenceWithConcept> t) {
                        // remove grandchildren
                        ((SctTreeItem) t.getSource()).removeGrandchildren();
                    }
                });

        rootTreeItem.addEventHandler(TreeItem.<TaxonomyReferenceWithConcept>branchExpandedEvent(),
                new EventHandler<TreeItem.TreeModificationEvent<TaxonomyReferenceWithConcept>>() {
                    @Override
                    public void handle(TreeItem.TreeModificationEvent<TaxonomyReferenceWithConcept> t) {
                        // add grandchildren
                        SctTreeItem sourceTreeItem = (SctTreeItem) t.getSource();
                        Utility.execute(() -> sourceTreeItem.addChildrenConceptsAndGrandchildrenItems());
                    }
                });
        sp_.getChildren().add(treeView_);
        sp_.getChildren().remove(0);  //remove the progress indicator

        // Final decrement of initializationCountDownLatch_ to 0,
        // indicating that initial init() is complete
        initializationCountDownLatch_.countDown();
    }

    public void showConcept(final UUID conceptUUID, final BooleanProperty workingIndicator) {
        if (initializationCountDownLatch_.getCount() > 1) {
            // Called before initial init() run, so run init().
            // showConcept Task will internally await() init() completion.

            init();
        }

        // Do work in background.
        Task<SctTreeItem> task = new Task<SctTreeItem>() {

            @Override
            protected SctTreeItem call() throws Exception {
                // await() init() completion.
                initializationCountDownLatch_.await();
                
                LOG.debug("Looking for concept {} in tree", conceptUUID);

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
                
                LOG.debug("Calculated root path {}", Arrays.toString(pathToRoot.toArray()));

                SctTreeItem currentTreeItem = rootTreeItem;

                // Walk down path from root.
                for (int i = pathToRoot.size() - 1; i >= 0; i--) {
                    SctTreeItem child = findChild(currentTreeItem, pathToRoot.get(i));
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
     * The first call you make to this should pass in the root node.
     * 
     * After that you can call it repeatedly to walk down the tree (you need to know the path first)
     * This will handle the waiting for each node to open, before moving on to the next node.
     * 
     * This should be called on a background thread.
     *
     * @return the found child, or null, if not found. found child will have
     *         already been told to expand and fetch its children.
     * @throws InterruptedException 
     */
    private SctTreeItem findChild(final SctTreeItem item, final UUID targetChildUUID) throws InterruptedException {
        
        LOG.debug("Looking for {}", targetChildUUID);
        SimpleObjectProperty<SctTreeItem> found = new SimpleObjectProperty<SctTreeItem>(null);
        if (item.getValue().getConcept().getPrimordialUuid().equals(targetChildUUID)) {
            // Found it.
            found.set(item);
        }
        else 
        {
            item.blockUntilChildrenReady();
            // Iterate through children and look for child with target UUID.
            for (TreeItem<TaxonomyReferenceWithConcept> child : item.getChildren()) {
                if (child != null && child.getValue() != null && child.getValue().getConceptFromRelationshipOrConceptProperties() != null
                        && child.getValue().getConceptFromRelationshipOrConceptProperties().getUuid().equals(targetChildUUID)) {

                    // Found it.
                    found.set((SctTreeItem) child);
                    break;
                }
            }
        }
        if (found.get() != null)
        {
            found.get().blockUntilChildrenReady();
            CountDownLatch cdl = new CountDownLatch(1);
            Platform.runLater(() ->
            {
                treeView_.scrollTo(treeView_.getRow(found.get()));
                found.get().setExpanded(true);
                cdl.countDown();
            });
            cdl.await();
        }
        else
        {
            LOG.debug("Find child failed to find {}", targetChildUUID);
        }
        return found.get();
    }

    /**
     * The various {@link BdbTerminologyStore#getFxConcept()} APIs break if
     * you ask for a concept that doesn't exist.
     * This method creates a {@link ConceptChronicleDdo} manually instead.
     */
    private ConceptChronicleDdo buildFxConcept(UUID conceptUUID)
            throws IOException, ContradictionException {

        ConceptVersionBI wbConcept = OTFUtility.getConceptVersion(conceptUUID);
        if (wbConcept == null) {
            return null;
        }

        BdbTerminologyStore dataStore = ExtendedAppContext.getDataStore();
        ViewCoordinate viewCoordinate = OTFUtility.getViewCoordinate();
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
    public static void globalShutdownRequested()
    {
        shutdownRequested = true;
        LOG.info("Global Tree shutdown called!");
    }
    
    protected void shutdownInstance()
    {
        LOG.info("Shutdown taxonomy instance");
        if (rootTreeItem != null)
        {
            rootTreeItem.clearChildren();  //This recursively cancels any active lookups
        }
    }
    
    protected static boolean wasGlobalShutdownRequested()
    {
        return shutdownRequested;
    }
    
    private void saveExpanded() {
        TreeItem<TaxonomyReferenceWithConcept> selected = treeView_.getSelectionModel().getSelectedItem();
        selectedItem_ = Optional.ofNullable(selected == null ? null : selected.getValue().getConceptFromRelationshipOrConceptProperties().getUuid());
        expandedUUIDs_.clear();
        saveExpanded(rootTreeItem);
        LOG.debug("Saved {} expanded nodes", expandedUUIDs_.size());
    }

    private void saveExpanded(SctTreeItem item) {
        if (!item.isLeaf() && item.isExpanded()) {
            expandedUUIDs_.add(item.getConceptUuid());
            if (!item.isLeaf()) {
                for (TreeItem<TaxonomyReferenceWithConcept> child : item.getChildren()) {
                    saveExpanded((SctTreeItem) child);
                }
            }
        }
    }
    
    private void restoreExpanded() {
        treeView_.getSelectionModel().clearSelection();
        
        Utility.execute(() -> 
        {
            try
            {
                SimpleObjectProperty<SctTreeItem> scrollTo = new SimpleObjectProperty<SctTreeItem>();
                restoreExpanded(rootTreeItem, scrollTo);
                
                expandedUUIDs_.clear();
                selectedItem_ = Optional.empty();
                if (scrollTo.get() != null)
                {
                    Platform.runLater(() -> 
                    {
                        treeView_.scrollTo(treeView_.getRow(scrollTo.get()));
                        treeView_.getSelectionModel().select(scrollTo.get());
                    });
                }
            }
            catch (InterruptedException e)
            {
                LOG.info("Interrupted while looking restoring expanded items");
            }
        });
        
    }
    
    private void restoreExpanded(SctTreeItem item, SimpleObjectProperty<SctTreeItem> scrollTo) throws InterruptedException {
        if (expandedUUIDs_.contains(item.getConceptUuid())) {
            item.blockUntilChildrenReady();
            Platform.runLater(() -> item.setExpanded(true));
            for (TreeItem<TaxonomyReferenceWithConcept> child : item.getChildren()) {
                restoreExpanded((SctTreeItem) child, scrollTo);
            }
        }
        if (selectedItem_.isPresent() && selectedItem_.get().equals(item.getConceptUuid())) {
            scrollTo.set(item);
        }
    }
}
