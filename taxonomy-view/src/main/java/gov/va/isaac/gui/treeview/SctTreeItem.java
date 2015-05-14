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

import gov.va.isaac.constants.ISAAC;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.taxonomyView.SctTreeItemDisplayPolicies;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.taxonomyView.SctTreeItemI;
import gov.va.isaac.util.OTFUtility;
import gov.va.isaac.util.Utility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.ddo.ComponentReference;
import org.ihtsdo.otf.tcc.ddo.TaxonomyReferenceWithConcept;
import org.ihtsdo.otf.tcc.ddo.concept.component.relationship.RelationshipChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.relationship.RelationshipVersionDdo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link TreeItem} for modeling nodes in the SNOMED CT taxonomy.
 *
 * @author kec
 * @author ocarlsen
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
class SctTreeItem extends TreeItem<TaxonomyReferenceWithConcept> implements SctTreeItemI, Comparable<SctTreeItem> {

    private static final Logger LOG = LoggerFactory.getLogger(SctTreeItem.class);

    private final List<SctTreeItem> extraParents = new ArrayList<>();
    private CountDownLatch childrenLoadedLatch = new CountDownLatch(1);
    private DoubleProperty childLoadPercentComplete = new SimpleDoubleProperty(-1.0);
    private volatile boolean cancelLookup = false;
    private boolean defined = false;
    private boolean multiParent = false;
    private int multiParentDepth = 0;
    private boolean secondaryParentOpened = false;
    private SctTreeItemDisplayPolicies displayPolicies;

    private static TreeItem<TaxonomyReferenceWithConcept> getTreeRoot(TreeItem<TaxonomyReferenceWithConcept> item) {
        TreeItem<TaxonomyReferenceWithConcept> parent = item.getParent();
        
        if (parent == null) {
            return item;
        } else {
            return getTreeRoot(parent);
        }
    }
    
    SctTreeItem(TaxonomyReferenceWithConcept taxRef, SctTreeItemDisplayPolicies displayPolicies) {
        this(taxRef, displayPolicies, (Node) null);
    }

    SctTreeItem(TaxonomyReferenceWithConcept t, SctTreeItemDisplayPolicies displayPolicies, Node node) {
        super(t, node);
        this.displayPolicies = displayPolicies;
    }

    SctTreeItemDisplayPolicies getDisplayPolicies() {
        return displayPolicies;
    }
    
    void addChildren() {
        final TaxonomyReferenceWithConcept taxRef = getValue();
        if (! shouldDisplay()) {
            // Don't add children to something that shouldn't be displayed
            LOG.debug("this.shouldDisplay() == false: not adding children to " + this.getConceptUuid());
            childLoadComplete();
        } else if (taxRef == null || taxRef.getConcept() == null) {
            LOG.debug("addChildren(): taxRef={}, taxRef.getConcept()={}", taxRef, taxRef.getConcept());
            childLoadComplete();
        } else if (taxRef.getConcept() != null) {
            // Gather the children
            ArrayList<SctTreeItem> childrenToProcess = new ArrayList<>();

            for (RelationshipChronicleDdo r : taxRef.conceptProperty().get().getDestinationRelationships()) {
                for (RelationshipVersionDdo rv : r.getVersions()) {
                    if (cancelLookup) {
                        return;
                    }
                    try {
                        TaxonomyReferenceWithConcept fxtrc = new TaxonomyReferenceWithConcept(rv);
                        SctTreeItem childItem = new SctTreeItem(fxtrc, displayPolicies);
                        if (childItem.shouldDisplay()) {
                            childrenToProcess.add(childItem);
                        } else {
                            LOG.debug("item.shouldDisplay() == false: not adding " + childItem.getConceptUuid() + " as child of " + this.getConceptUuid());
                        }
                    } catch (IOException | ContradictionException ex) {
                        LOG.error(null, ex);
                    }
                }
            }

            Collections.sort(childrenToProcess);
            if (cancelLookup) {
                return;
            }
            
            Platform.runLater(() ->
            {
                getChildren().addAll(childrenToProcess);
                childLoadComplete();
            });
            

            //This loads the childrens children
            for (SctTreeItem child : childrenToProcess) {
                //TODO switch this to the blocking queue
                Utility.execute(new GetSctTreeItemConceptCallable(child));
            }
        }
    }

    void addChildrenConceptsAndGrandchildrenItems() {
        ArrayList<SctTreeItem> grandChildrenToProcess = new ArrayList<>();

        if (! shouldDisplay()) {
            // Don't add children to something that shouldn't be displayed
            LOG.debug("this.shouldDisplay() == false: not adding children concepts and grandchildren items to " + this.getConceptUuid());
            childLoadComplete();
        } else {
            for (TreeItem<TaxonomyReferenceWithConcept> child : getChildren()) {
                if (cancelLookup) {
                    return;
                }
                if (((SctTreeItem)child).shouldDisplay()) {
                    if (child.getChildren().isEmpty() && (child.getValue().getConcept() != null)) {
                        if (child.getValue().getConcept().getDestinationRelationships().isEmpty()) {
                            TaxonomyReferenceWithConcept value = child.getValue();
                            child.setValue(null);
                            SctTreeItem noChildItem = (SctTreeItem) child;
                            noChildItem.computeGraphic();
                            noChildItem.setValue(value);
                        } else {
                            ArrayList<SctTreeItem> grandChildrenToAdd = new ArrayList<>();

                            for (RelationshipChronicleDdo r : child.getValue().conceptProperty().get().getDestinationRelationships()) {
                                if (cancelLookup) {
                                    return;
                                }
                                for (RelationshipVersionDdo rv : r.getVersions()) {
                                    try {
                                        TaxonomyReferenceWithConcept taxRef = new TaxonomyReferenceWithConcept(rv);
                                        SctTreeItem grandChildItem = new SctTreeItem(taxRef, displayPolicies);

                                        if (grandChildItem.shouldDisplay()) {
                                            grandChildrenToProcess.add(grandChildItem);
                                            grandChildrenToAdd.add(grandChildItem);
                                        } else {
                                            LOG.debug("grandChildItem.shouldDisplay() == false: not adding " + grandChildItem.getConceptUuid() + " as child of " + ((SctTreeItem)child).getConceptUuid());
                                        }
                                    } catch (IOException | ContradictionException ex) {
                                        LOG.error(null, ex);
                                    }
                                }
                            }

                            Collections.sort(grandChildrenToAdd);
                            if (cancelLookup) {
                                return;
                            }
                            
                            Platform.runLater(() ->
                            {
                                child.getChildren().addAll(grandChildrenToAdd);
                                ((SctTreeItem)child).childLoadComplete();
                            });
                        }
                    } else if (child.getValue().getConcept() == null) {
                        grandChildrenToProcess.add((SctTreeItem) child);
                    }
                } else {
                    LOG.debug("childItem.shouldDisplay() == false: not adding " + ((SctTreeItem)child).getConceptUuid() + " as child of " + this.getConceptUuid());
                }
            }
            
            if (cancelLookup) {
                return;
            }

            //This loads the childrens children
            for (SctTreeItem childsChild : grandChildrenToProcess) {
                //TODO switch this to the blocking queue
                Utility.execute(new GetSctTreeItemConceptCallable(childsChild));
            }
        }
    }

    @Override
    public int compareTo(SctTreeItem o) {
        return this.toString().compareTo(o.toString());
    }

    public UUID getConceptUuid() {
        TaxonomyReferenceWithConcept ref = getValue();
        
        if (ref != null && ref.getConceptFromRelationshipOrConceptProperties() != null) {
            return getValue().getConceptFromRelationshipOrConceptProperties().getUuid();
        } else {
            return null;
        }
    }
    @Override
    public Integer getConceptNid() {
        return getConceptNid(getValue());
    }
    private static Integer getConceptNid(TreeItem<TaxonomyReferenceWithConcept> item) {
        return getConceptNid(item.getValue());
    }
    private static Integer getConceptNid(TaxonomyReferenceWithConcept ref) {
        if (ref != null && ref.getConceptFromRelationshipOrConceptProperties() != null) {
            return ref.getConceptFromRelationshipOrConceptProperties().getNid();
        } else {
            return null;
        }
    }
    
    @Override
    public boolean isRoot() {
        TaxonomyReferenceWithConcept ref = getValue();

        if (ISAAC.ISAAC_ROOT.getPrimodialUuid().equals(this.getConceptUuid())) {
            return true;
        } else if (ref != null && ref.getRelationshipVersion() == null) {
            return true;
        } else if (this.getParent() == null) {
            return true;
        } else {
            TreeItem<TaxonomyReferenceWithConcept> root = getTreeRoot(this);

            if (this == root) {
                return true;
            } else if (getConceptNid(root) == getConceptNid()) {
                return true;
            }
            else {
                return false;
            }
        }
    }
    
    public Node computeGraphic() {
        return displayPolicies.computeGraphic(this);
    }
    
    public boolean shouldDisplay() {
        return displayPolicies.shouldDisplay(this);
    }

    /**
     * @see javafx.scene.control.TreeItem#toString()
     * WARNING: toString is currently used in compareTo()
     */
    @Override
    public String toString() {
        return toString(this);
    }
    
    public static String toString(SctTreeItem item) {
        try {
            if (item.getValue().getRelationshipVersion() != null) {
                if (item.getMultiParentDepth() > 0) {
                    ComponentReference destRef = item.getValue().getRelationshipVersion().getDestinationReference();
                    String temp = OTFUtility.getDescription(destRef.getUuid());
                    if (temp == null) {
                        return destRef.getText();
                    } else {
                        return temp;
                    }
                } else {
                    ComponentReference originRef = item.getValue().getRelationshipVersion().getOriginReference();
                    String temp = OTFUtility.getDescription(originRef.getUuid());
                    if (temp == null) {
                        return originRef.getText();
                    } else {
                        return temp;
                    }
                }
            }

            if (item.getValue().conceptProperty().get() != null) {
                return OTFUtility.getDescription(item.getValue().conceptProperty().get());
            }

            return "root";
        } catch (RuntimeException re) {
            LOG.error("Caught {} \"{}\"", re.getClass().getName(), re.getLocalizedMessage());
            throw re;
        } catch (Error e) {
            LOG.error("Caught {} \"{}\"", e.getClass().getName(), e.getLocalizedMessage());
            throw e;
        }
    }

    public List<SctTreeItem> getExtraParents() {
        return extraParents;
    }

    @Override
    public int getMultiParentDepth() {
        return multiParentDepth;
    }

    /**
     * returns -1 when not yet started, otherwise, a value between 0 and 1 (1 when complete)
     */
    public DoubleProperty getChildLoadPercentComplete() {
        return childLoadPercentComplete;
    }

    @Override
    public boolean isDefined() {
        return defined;
    }

    @Override
    public boolean isLeaf() {
        if (multiParentDepth > 0) {
            return true;
        }

        return super.isLeaf();
    }

    @Override
    public boolean isMultiParent() {
        if (multiParent) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isSecondaryParentOpened() {
        if (secondaryParentOpened) {
            return true;
        } else {
            return false;
        }
    }

    public void setDefined(boolean defined) {
        this.defined = defined;
    }

    public void setMultiParent(boolean multiParent) {
        this.multiParent = multiParent;
    }

    public void setMultiParentDepth(int multiParentDepth) {
        this.multiParentDepth = multiParentDepth;
    }

    public void setSecondaryParentOpened(boolean secondaryParentOpened) {
        this.secondaryParentOpened = secondaryParentOpened;
    }

    public void blockUntilChildrenReady() throws InterruptedException {
        childrenLoadedLatch.await();
    }
    
    public void clearChildren()
    {
        cancelLookup = true;
        for (TreeItem<TaxonomyReferenceWithConcept> child : getChildren())
        {
            ((SctTreeItem)child).clearChildren();
        }
        getChildren().clear();
    }
    
    protected void resetChildrenCalculators()
    {
        CountDownLatch cdl = new CountDownLatch(1);
        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
                cancelLookup = false;
                childLoadPercentComplete.set(-1);
                childrenLoadedLatch = new CountDownLatch(1);
                cdl.countDown();
            }
        };
        if (Platform.isFxApplicationThread())
        {
            r.run();
        }
        else
        {
            Platform.runLater(r);
        }
        try
        {
            cdl.await();
        }
        catch (InterruptedException e)
        {
            LOG.error("unexpected interrupt", e);
        }
    }
    
    public void removeGrandchildren() {
        for (TreeItem<TaxonomyReferenceWithConcept> child : getChildren()) {
           ((SctTreeItem)child).clearChildren();
           ((SctTreeItem)child).resetChildrenCalculators();
        }
    }
    
    /**
     * Can be called on either a background or the FX thread
     */
    protected void childLoadComplete()
    {
        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
                childLoadPercentComplete.set(1.0);
                childrenLoadedLatch.countDown();
            }
        };
        if (Platform.isFxApplicationThread())
        {
            r.run();
        }
        else
        {
            Platform.runLater(r);
        }
    }
    
    protected boolean isCancelRequested()
    {
        return cancelLookup;
    }
}
