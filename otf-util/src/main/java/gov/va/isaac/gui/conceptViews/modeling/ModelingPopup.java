package gov.va.isaac.gui.conceptViews.modeling;

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerHelper.ComponentType;
import gov.va.isaac.gui.util.ErrorMarkerUtils;
import gov.va.isaac.interfaces.gui.MenuItemI;
import gov.va.isaac.interfaces.gui.views.PopupConceptViewI;
import gov.va.isaac.interfaces.gui.views.PopupViewI;
import gov.va.isaac.util.UpdateableBooleanBinding;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import org.glassfish.hk2.runlevel.RunLevelException;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ModelingPopup extends Stage implements PopupViewI {

	protected String popupTitle;
	protected ComponentVersionBI origComp = null;
	protected UpdateableBooleanBinding allValid_;
	protected PopupConceptViewI callingView_;
	protected SimpleBooleanProperty modificationMade = new SimpleBooleanProperty(false);
	protected SimpleStringProperty reasonSaveDisabled_ = new SimpleStringProperty();
	protected Logger logger_ = LoggerFactory.getLogger(this.getClass());

	abstract protected void finishInit();
	abstract protected void setupTopItems(VBox topItems);
	abstract protected void setupValidations();
	abstract protected void addNewVersion();
	abstract protected boolean passesQA();

	ModelingPopup() {
		BorderPane root = new BorderPane();

		VBox topItems = new VBox();
		topItems.setFillWidth(true);
		
		// Component Specific
		setupTopItems(topItems);
		setupValidations();
		root.setTop(topItems);
		
		// Buttons
		GridPane bottomRow = new GridPane();
		//spacer col
		bottomRow.add(new Region(), 0, 0);
		
		Button cancelButton = new Button("Cancel");
		cancelButton.setOnAction((action) -> {
			close();
		});
		GridPane.setMargin(cancelButton, new Insets(5, 20, 5, 0));
		GridPane.setHalignment(cancelButton, HPos.RIGHT);
		bottomRow.add(cancelButton, 1, 0);

		Button saveButton = new Button("Save");
		saveButton.disableProperty().bind(allValid_.not());
		saveButton.setOnAction((action) -> {
			doSave();
		});
		Node wrappedSave = ErrorMarkerUtils.setupDisabledInfoMarker(saveButton, reasonSaveDisabled_);
		GridPane.setMargin(wrappedSave, new Insets(5, 0, 5, 20));
		bottomRow.add(wrappedSave, 2, 0);

		//spacer col
		bottomRow.add(new Region(), 3, 0);

		ColumnConstraints cc = new ColumnConstraints();
		cc.setHgrow(Priority.SOMETIMES);
		cc.setFillWidth(true);
		bottomRow.getColumnConstraints().add(cc);
		cc = new ColumnConstraints();
		cc.setHgrow(Priority.NEVER);
		bottomRow.getColumnConstraints().add(cc);
		cc = new ColumnConstraints();
		cc.setHgrow(Priority.NEVER);
		bottomRow.getColumnConstraints().add(cc);
		cc = new ColumnConstraints();
		cc.setHgrow(Priority.SOMETIMES);
		cc.setFillWidth(true);
		bottomRow.getColumnConstraints().add(cc);
		root.setBottom(bottomRow);

		Scene scene = new Scene(root);
		scene.getStylesheets().add(ConceptModelingPopup.class.getResource("/isaac-shared-styles.css").toString());
		setScene(scene);

	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.IsaacViewI#getMenuBarMenus()
	 */
	@Override
	public List<MenuItemI> getMenuBarMenus()
	{
		return new ArrayList<>();
	}


	/**
	 * Call setReferencedComponent first
	 * 
	 * @see gov.va.isaac.interfaces.gui.views.PopupViewI#showView(javafx.stage.Window)
	 */
	@Override
	public void showView(Window parent)
	{
		if (origComp == null)
		{
			throw new RunLevelException("Component must be set first");
		}
		setTitle(popupTitle);
		setResizable(true);

		initOwner(parent);
		initModality(Modality.NONE);
		initStyle(StageStyle.DECORATED);

		setWidth(600);
		setHeight(400);

		show();
	}


	public void finishInit(ComponentVersionBI comp, ComponentType type, PopupConceptViewI callingView)
	{
		origComp = comp;
		callingView_ = callingView;
		
		finishInit();
	}
	
	protected void doSave()
	{
		addNewVersion();

		if (callingView_ != null)
		{
			ExtendedAppContext.getDataStore().waitTillWritesFinished();
			callingView_.setConcept(origComp.getConceptNid());
		}
		close();
	}
}
