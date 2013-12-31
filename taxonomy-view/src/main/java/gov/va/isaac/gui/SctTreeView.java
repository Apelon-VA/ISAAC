package gov.va.isaac.gui;

import gov.va.isaac.AppContext;
import gov.va.isaac.util.Images;
import javafx.event.EventHandler;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

import org.ihtsdo.otf.tcc.ddo.TaxonomyReferenceWithConcept;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;

/**
 * A {@link TreeView} for browsing the SNOMED CT taxonomy.
 *
 * @author kec
 * @author ocarlsen
 */
public class SctTreeView extends TreeView<TaxonomyReferenceWithConcept> {

	private final AppContext appContext;

	private SctTreeItem visibleRootItem;

	protected static volatile boolean shutdownRequested = false;

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

		visibleRootItem = new SctTreeItem(appContext, visibleRootConcept, Images.ROOT.createImageView());

		hiddenRootItem.getChildren().add(visibleRootItem);
		visibleRootItem.addChildren();

		// put this event handler on the root
		visibleRootItem.addEventHandler(TreeItem.branchCollapsedEvent(),
				new EventHandler<TreeItem.TreeModificationEvent<Object>>() {
					@Override
					public void handle(TreeItem.TreeModificationEvent<Object> t) {
						// remove grandchildren
						SctTreeItem sourceTreeItem = (SctTreeItem) t
								.getSource();
						sourceTreeItem.removeGrandchildren();
					}
				});

		visibleRootItem.addEventHandler(TreeItem.branchExpandedEvent(),
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
	 * rebuild the tree from the root down. Useful when the requested description type changes.
	 */
	public void rebuild() {
		getRoot().getChildren().get(0).getChildren().clear();
		((SctTreeItem) getRoot().getChildren().get(0)).addChildren();
	}

	/**
	 * Tell the sim tree to stop whatever threading operations it has running, as the application is exiting.
	 */
	public static void shutdown() {
		shutdownRequested = true;
	}
}
