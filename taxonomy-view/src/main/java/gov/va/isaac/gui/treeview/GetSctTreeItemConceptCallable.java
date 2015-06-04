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
import gov.va.isaac.util.OTFUtility;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.ddo.ComponentReference;
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
 * A concrete {@link Callable} for fetching concepts.
 *
 * @author ocarlsen
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class GetSctTreeItemConceptCallable extends Task<Boolean> {
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
        try
        {
            ComponentReference reference;
    
            if (treeItem == null || treeItem.getValue() == null || treeItem.getValue().getRelationshipVersion() == null)
            {
                return false;
            }
            
            if (SctTreeView.wasGlobalShutdownRequested() || treeItem.isCancelRequested()) {
                return false;
            }
            
            if (addChildren) {
                reference = treeItem.getValue().getRelationshipVersion().getOriginReference();
            } else {
                reference = treeItem.getValue().getRelationshipVersion().getDestinationReference();
            }
    
            if (SctTreeView.wasGlobalShutdownRequested() || treeItem.isCancelRequested()|| reference == null) {
                return false;
            }
    
            BdbTerminologyStore dataStore = ExtendedAppContext.getDataStore();
            concept = dataStore.getFxConcept(reference,
                    OTFUtility.getViewCoordinate(),
                    versionPolicy, refexPolicy, relationshipPolicy);
    
            if ((concept.getConceptAttributes() == null)
                    || concept.getConceptAttributes().getVersions().isEmpty()
                    || concept.getConceptAttributes().getVersions().get(0).isDefined()) {
                treeItem.setDefined(true);
            }
            
            if (SctTreeView.wasGlobalShutdownRequested() || treeItem.isCancelRequested()) {
                return false;
            }
    
            if (concept.getOriginRelationships().size() > 1) {
                treeItem.setMultiParent(true);
            } 
    
            if (addChildren) {
                //TODO it would be nice to show progress here, by binding this status to the 
                //progress indicator in the SctTreeItem - However -that progress indicator displays at 16x16,
                //and ProgressIndicator has a bug, that is vanishes for anything other than indeterminate for anything less than 32x32
                //need a progress indicator that works at 16x16
                for (RelationshipChronicleDdo destRel : concept.getDestinationRelationships()) {
                    if (SctTreeView.wasGlobalShutdownRequested() || treeItem.isCancelRequested()) {
                        return false;
                    }
                    for (RelationshipVersionDdo rv : destRel.getVersions()) {
                        TaxonomyReferenceWithConcept taxRef = new TaxonomyReferenceWithConcept(rv);
                        SctTreeItem childItem = new SctTreeItem(taxRef, treeItem.getDisplayPolicies());
                        if (childItem.shouldDisplay()) {
                            childrenToAdd.add(childItem);
                        }
                        if (SctTreeView.wasGlobalShutdownRequested() || treeItem.isCancelRequested()) {
                            return false;
                        }
                    }
                }
                Collections.sort(childrenToAdd);
            }
            
            CountDownLatch temp = new CountDownLatch(1);
    
            Platform.runLater(() -> 
            {
                TaxonomyReferenceWithConcept itemValue = treeItem.getValue();

                treeItem.setValue(null);
                if (addChildren)
                {
                    treeItem.getChildren().clear();
                    treeItem.getChildren().addAll(childrenToAdd);
                }
                treeItem.setValue(itemValue);
                treeItem.getValue().conceptProperty().set(concept);
                temp.countDown();
            });
            temp.await();
            
            return true;
        }
        catch (Exception e)
        {
            LOG.error("Unexpected", e);
            throw e;
        }
        finally
        {
            if (!SctTreeView.wasGlobalShutdownRequested() && !treeItem.isCancelRequested()) 
            {
                treeItem.childLoadComplete();
            }
        }
    }
}
