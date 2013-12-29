package gov.va.isaac.gui;

import gov.va.isaac.util.Images;
import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;

import org.ihtsdo.otf.tcc.api.concurrency.FutureHelper;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.ddo.TaxonomyReferenceWithConcept;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.relationship.RelationshipChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.relationship.RelationshipVersionDdo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link TreeCell} for rendering {@link TaxonomyReferenceWithConcept} objects.
 *
 * @author kec
 * @author ocarlsen
 */
public final class SctTreeCell extends TreeCell<TaxonomyReferenceWithConcept> {

	private static final Logger LOG = LoggerFactory.getLogger(SctTreeCell.class);

	private final BdbTerminologyStore dataStore;

    public SctTreeCell(BdbTerminologyStore dataStore) {
    	super();
        this.dataStore = dataStore;

        setOnMouseClicked(new ClickListener());
    }

    //~--- methods -------------------------------------------------------------
    private void openOrCloseParent(SctTreeItem treeItem) throws IOException, ContradictionException {
        TaxonomyReferenceWithConcept value = treeItem.getValue();
        ConceptChronicleDdo c = value.getConcept();

        if (c != null) {
            treeItem.setValue(null);

            SctTreeItem parentItem = (SctTreeItem) treeItem.getParent();
            ObservableList<TreeItem<TaxonomyReferenceWithConcept>> siblings = parentItem.getChildren();

            if (treeItem.isSecondaryParentOpened()) {
                removeExtraParents(treeItem, siblings);
            } else {
                ArrayList<RelationshipChronicleDdo> extraParents = new ArrayList<>(c.getOriginRelationships());

                extraParents.remove(value.getRelationshipVersion().getChronicle());

                ArrayList<SctTreeItem> extraParentItems = new ArrayList<>(extraParents.size());

                for (RelationshipChronicleDdo extraParent : extraParents) {
                    for (RelationshipVersionDdo extraParentVersion : extraParent.getVersions()) {
                        SctTreeItem extraParentItem =
                                new SctTreeItem(new TaxonomyReferenceWithConcept(extraParentVersion,
                                TaxonomyReferenceWithConcept.WhichConcept.DESTINATION), dataStore);
                        ProgressIndicator indicator = new ProgressIndicator();

                        indicator.setSkin(new TaxonomyProgressIndicatorSkin(indicator));
                        indicator.setPrefSize(16, 16);
                        indicator.setProgress(-1);
                        extraParentItem.setGraphic(indicator);
                        extraParentItem.setMultiParentDepth(treeItem.getMultiParentDepth() + 1);
                        extraParentItems.add(extraParentItem);
                    }
                }

                Collections.sort(extraParentItems);
                Collections.reverse(extraParentItems);

                int startIndex = siblings.indexOf(treeItem);

                for (SctTreeItem extraParent : extraParentItems) {
                    parentItem.getChildren().add(startIndex++, extraParent);
                    treeItem.getExtraParents().add(extraParent);
                    GetSctTreeItemConceptCallable fetcher = new GetSctTreeItemConceptCallable(extraParent, false, dataStore);
                    FutureHelper.addFuture(SctTreeItem.conceptFetcherService.submit(fetcher));
                }
            }

            treeItem.setValue(value);
            treeItem.setSecondaryParentOpened(!treeItem.isSecondaryParentOpened());
            treeItem.computeGraphic();
        }
    }

    @Override
    protected void updateItem(TaxonomyReferenceWithConcept t, boolean bln) {
        super.updateItem(t, bln);
        double opacity = 0.0;

        if (t != null) {
            final SctTreeItem treeItem = (SctTreeItem) getTreeItem();

            if (treeItem.getMultiParentDepth() > 0) {
                if (treeItem.isLeaf()) {
                    BorderPane graphicBorderPane = new BorderPane();
                    int multiParentInset = treeItem.getMultiParentDepth() * 16;
                    Rectangle leftRect =
                            RectangleBuilder.create().width(multiParentInset).height(16).build();

                    leftRect.setOpacity(opacity);
                    graphicBorderPane.setLeft(leftRect);
                    graphicBorderPane.setCenter(treeItem.computeGraphic());
                    setGraphic(graphicBorderPane);
                }

                setText(WBUtility.getDescription(t.getConcept()));

                return;
            }

            BorderPane disclosureBorderPane = new BorderPane();

            if (treeItem.isExpanded()) {
                ImageView iv = Images.TAXONOMY_CLOSE.createImageView();

                if (treeItem.getProgressIndicator() != null) {
                    disclosureBorderPane.setCenter(treeItem.getProgressIndicator());
                } else {
                    disclosureBorderPane.setCenter(iv);
                }

                setDisclosureNode(disclosureBorderPane);
            } else {
                ImageView iv = Images.TAXONOMY_OPEN.createImageView();

                if (treeItem.getProgressIndicator() != null) {
                    disclosureBorderPane.setCenter(treeItem.getProgressIndicator());
                } else {
                    disclosureBorderPane.setCenter(iv);
                }

                setDisclosureNode(disclosureBorderPane);
            }

            if (t.getConcept() == null)
            {
                setText(t.toString());
            }
            else
            {
                setText(WBUtility.getDescription(t.getConcept()));
            }

            BorderPane graphicBorderPane = new BorderPane();

            if (treeItem.isLeaf()) {
                int multiParentInset = treeItem.getMultiParentDepth() * 16;
                Rectangle leftRect =
                        RectangleBuilder.create().width(multiParentInset).height(16).build();

                leftRect.setOpacity(opacity);
                graphicBorderPane.setLeft(leftRect);
                    graphicBorderPane.setCenter(treeItem.computeGraphic());
                setGraphic(graphicBorderPane);
            } else {

                Rectangle leftRect = RectangleBuilder.create().width(6).height(16).build();

                leftRect.setOpacity(opacity);
                graphicBorderPane.setLeft(leftRect);
                    graphicBorderPane.setCenter(treeItem.computeGraphic());
                setGraphic(graphicBorderPane);
            }
        }
    }

    private void removeExtraParents(SctTreeItem treeItem, ObservableList<TreeItem<TaxonomyReferenceWithConcept>> siblings) {
        for (SctTreeItem extraParent : treeItem.getExtraParents()) {
            removeExtraParents(extraParent, siblings);
            siblings.remove(extraParent);
        }
    }

	/**
	 * Listens for mouse clicks to expand/collapse node.
	 */
    private final class ClickListener implements EventHandler<MouseEvent> {

        @Override
        public void handle(MouseEvent t) {
            if (getItem() != null) {
                if (getGraphic().getBoundsInParent().contains(t.getX(), t.getY())) {
                    SctTreeItem item = (SctTreeItem) getTreeItem();

                    if (item.isMultiParent() || item.getMultiParentDepth() > 0) {
                        try {
                            openOrCloseParent(item);
                        } catch (ContradictionException | IOException ex) {
                            LOG.error(null, ex);
                        }
                    }
                }
            }
        }
    }
}
