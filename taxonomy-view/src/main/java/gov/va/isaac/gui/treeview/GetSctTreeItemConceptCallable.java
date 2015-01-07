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

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.util.WBUtility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Callable;

import javafx.application.Platform;

import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.ddo.ComponentReference;
import org.ihtsdo.otf.tcc.ddo.TaxonomyReferenceWithConcept;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.relationship.RelationshipChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.relationship.RelationshipVersionDdo;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.RefexPolicy;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.RelationshipPolicy;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.VersionPolicy;
import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A concrete {@link Callable} for fetching concepts.
 *
 * @author ocarlsen
 * @author kec
 */
public class GetSctTreeItemConceptCallable implements Callable<Boolean> {
    private static final Logger LOG = LoggerFactory.getLogger(GetSctTreeItemConceptCallable.class);

    private final SctTreeItem treeItem;
    private final boolean addChildren;
    private final VersionPolicy versionPolicy;
    private final RefexPolicy refexPolicy;
    private final RelationshipPolicy relationshipPolicy;
    private final ArrayList<SctTreeItem> childrenToAdd = new ArrayList<>();

    private ConceptChronicleDdo concept;

    public GetSctTreeItemConceptCallable(SctTreeItem treeItem) {
        this(treeItem, true);
    }

    public GetSctTreeItemConceptCallable(SctTreeItem treeItem, boolean addChildren) {
        this(treeItem, addChildren, VersionPolicy.ACTIVE_VERSIONS,
                RefexPolicy.ANNOTATION_MEMBERS, RelationshipPolicy.ORIGINATING_AND_DESTINATION_TAXONOMY_RELATIONSHIPS);
    }

    public GetSctTreeItemConceptCallable(SctTreeItem treeItem, boolean addChildren,
            VersionPolicy versionPolicy, RefexPolicy refexPolicy, RelationshipPolicy relationshipPolicy) {
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

        BdbTerminologyStore dataStore = ExtendedAppContext.getDataStore();
        concept = dataStore.getFxConcept(reference,
                WBUtility.getViewCoordinate(),
                versionPolicy, refexPolicy, relationshipPolicy);

        if ((concept.getConceptAttributes() == null)
                || concept.getConceptAttributes().getVersions().isEmpty()
                || concept.getConceptAttributes().getVersions().get(0).isDefined()) {
            treeItem.setDefined(true);
        }

        if (concept.getOriginRelationships().size() > 1) {
        	//LOG.debug("Concept {} has {} origin relationships in {} mode", WBUtility.getDescription(concept), concept.getOriginRelationships().size(), WBUtility.getViewCoordinate().getRelationshipAssertionType());
            treeItem.setMultiParent(true);
        } else if (concept.getOriginRelationships().size() == 1) {
        	//LOG.debug("Concept {} has {} origin relationships in {} mode", WBUtility.getDescription(concept), concept.getOriginRelationships().size(), WBUtility.getViewCoordinate().getRelationshipAssertionType());
        } else if (concept.getOriginRelationships().size() == 0) {
        	// TODO (artf231888): remove this debug statement when this tracker is closed
        	LOG.debug("Concept {} has {} origin relationships in {} mode", WBUtility.getDescription(concept), concept.getOriginRelationships().size(), WBUtility.getViewCoordinate().getRelationshipAssertionType());
        }

        if (addChildren) {
            for (RelationshipChronicleDdo destRel : concept.getDestinationRelationships()) {
                if (SctTreeView.shutdownRequested) {
                    return false;
                }
                for (RelationshipVersionDdo rv : destRel.getVersions()) {
                    TaxonomyReferenceWithConcept taxRef = new TaxonomyReferenceWithConcept(rv);
                    SctTreeItem childItem = new SctTreeItem(taxRef, treeItem.getDisplayPolicies());
                    if (childItem.shouldDisplay()) {
                    	childrenToAdd.add(childItem);
                    }
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
