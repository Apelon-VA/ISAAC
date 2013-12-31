package gov.va.isaac.gui;

import gov.va.isaac.AppContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Callable;

import javafx.application.Platform;

import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.ddo.ComponentReference;
import org.ihtsdo.otf.tcc.ddo.TaxonomyReferenceWithConcept;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.relationship.RelationshipChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.relationship.RelationshipVersionDdo;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.RefexPolicy;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.RelationshipPolicy;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.VersionPolicy;

/**
 * A concrete {@link Callable} for fetching concepts.
 *
 * @author ocarlsen
 * @author kec
 */
public class GetSctTreeItemConceptCallable implements Callable<Boolean> {

    private final AppContext appContext;
    private final SctTreeItem treeItem;
    private final boolean addChildren;
    private final VersionPolicy versionPolicy;
    private final RefexPolicy refexPolicy;
    private final RelationshipPolicy relationshipPolicy;
    private final ArrayList<SctTreeItem> childrenToAdd = new ArrayList<>();

    private ConceptChronicleDdo concept;

    public GetSctTreeItemConceptCallable(AppContext appContext, SctTreeItem treeItem) {
        this(appContext, treeItem, true);
    }

    public GetSctTreeItemConceptCallable(AppContext appContext, SctTreeItem treeItem, boolean addChildren) {
        this(appContext, treeItem, addChildren, VersionPolicy.ACTIVE_VERSIONS,
                RefexPolicy.ANNOTATION_MEMBERS, RelationshipPolicy.ORIGINATING_AND_DESTINATION_TAXONOMY_RELATIONSHIPS);
    }

    public GetSctTreeItemConceptCallable(AppContext appContext, SctTreeItem treeItem, boolean addChildren,
            VersionPolicy versionPolicy, RefexPolicy refexPolicy, RelationshipPolicy relationshipPolicy) {
        this.appContext = appContext;
        this.treeItem = treeItem;
        this.addChildren = addChildren;
        this.versionPolicy = versionPolicy;
        this.refexPolicy = refexPolicy;
        this.relationshipPolicy = relationshipPolicy;
    }

    @Override
    public Boolean call() throws Exception {
        ComponentReference reference;

        if (addChildren) {
            reference = treeItem.getValue().getRelationshipVersion().getOriginReference();
        } else {
            reference = treeItem.getValue().getRelationshipVersion().getDestinationReference();
        }

        if (SctTreeView.shutdownRequested) {
            return false;
        }

        BdbTerminologyStore dataStore = appContext.getDataStore();
        concept = dataStore.getFxConcept(reference,
                StandardViewCoordinates.getSnomedInferredLatest(),
                versionPolicy, refexPolicy, relationshipPolicy);

        if ((concept.getConceptAttributes() == null)
                || concept.getConceptAttributes().getVersions().isEmpty()
                || concept.getConceptAttributes().getVersions().get(0).isDefined()) {
            treeItem.setDefined(true);
        }

        if (concept.getOriginRelationships().size() > 1) {
            treeItem.setMultiParent(true);
        }

        if (addChildren) {
            for (RelationshipChronicleDdo destRel : concept.getDestinationRelationships()) {
                if (SctTreeView.shutdownRequested) {
                    return false;
                }
                for (RelationshipVersionDdo rv : destRel.getVersions()) {
                    TaxonomyReferenceWithConcept taxRef = new TaxonomyReferenceWithConcept(rv);
                    SctTreeItem childItem = new SctTreeItem(appContext, taxRef);

                    childrenToAdd.add(childItem);
                }
            }
        }

        if (SctTreeView.shutdownRequested) {
            return false;
        }

        Collections.sort(childrenToAdd);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                TaxonomyReferenceWithConcept itemValue = treeItem.getValue();

                treeItem.setValue(null);
                treeItem.getChildren().clear();
                treeItem.computeGraphic();
                treeItem.getChildren().addAll(childrenToAdd);
                treeItem.setValue(itemValue);
                treeItem.getValue().conceptProperty().set(concept);
            }
        });

        return true;
    }
}
