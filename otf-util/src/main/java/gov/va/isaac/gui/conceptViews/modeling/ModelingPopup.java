package gov.va.isaac.gui.conceptViews.modeling;

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.gui.util.ErrorMarkerUtils;
import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.interfaces.gui.views.PopupViewI;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.PopupConceptViewI;
import gov.va.isaac.util.UpdateableBooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * ModelingPopup
 * 
 * @author <a href="mailto:jefron@apelon.com">Jesse Efron</a>
 */


public abstract class ModelingPopup extends Stage implements PopupViewI {

	protected String popupTitle;
	protected ComponentVersionBI origComp = null;
	protected UpdateableBooleanBinding allValid_;
	protected PopupConceptViewI callingView_;
	protected SimpleBooleanProperty modificationMade = new SimpleBooleanProperty(false);
	protected SimpleStringProperty reasonSaveDisabled_ = new SimpleStringProperty();
	protected Logger logger_ = LoggerFactory.getLogger(this.getClass());
	protected int row = 0;
	private Label maxLengthTitleLabel = new Label();
	private Label maxLengthOriginalLabel = new Label();
	protected GridPane gp_ = new GridPane();
	protected Label title;
	protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
	protected final String SELECT_VALUE = "<Select a Value>";
	protected int conceptNid;
	private Button saveButton;
	
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

		saveButton = new Button("Save");
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
	 * Call setReferencedComponent first
	 * 
	 * @see gov.va.isaac.interfaces.gui.views.PopupViewI#showView(javafx.stage.Window)
	 */
	@Override
	public void showView(Window parent)
	{
		String stageTitle = popupTitle.replace("Modify", "Modeling");
		setTitle(stageTitle);
		setResizable(true);

		initOwner(parent);
		initModality(Modality.NONE);
		initStyle(StageStyle.DECORATED);

		setWidth(600);
		setHeight(400);

		show();
	}


	public void finishInit(ComponentVersionBI comp, PopupConceptViewI callingView)
	{
		origComp = comp;
		conceptNid = comp.getConceptNid();
		callingView_ = callingView;
		
		finishInit();
		setupValidations();
		saveButton.disableProperty().bind(allValid_.not());
	}
	
	protected void doSave()
	{
		addNewVersion();

		if (callingView_ != null)
		{
			ExtendedAppContext.getDataStore().waitTillWritesFinished();
			callingView_.setConcept(conceptNid);
		}
		close();
	}
	
	protected void createTitleLabel(String title) {
		Label ltitle = new Label(title + ":");
		ltitle.getStyleClass().add("boldLabel");
		maxLengthTitleLabel = (FxUtils.calculateNecessaryWidthOfBoldLabel(ltitle) > FxUtils.calculateNecessaryWidthOfBoldLabel(maxLengthTitleLabel)) ? ltitle : maxLengthTitleLabel;
		gp_.add(ltitle, 0, row);		
	}

	protected void createOriginalLabel(String origValue) {
		Label lOrig = new Label(origValue);
		maxLengthOriginalLabel = (FxUtils.calculateNecessaryWidthOfBoldLabel(lOrig) > FxUtils.calculateNecessaryWidthOfBoldLabel(maxLengthOriginalLabel)) ? lOrig : maxLengthOriginalLabel;
		gp_.add(lOrig, 1, row);		
		
		row++;
	}

	protected void setupGridPaneConstraints() {
		ColumnConstraints cc = new ColumnConstraints();
		cc.setHgrow(Priority.NEVER);
		cc.setMinWidth(FxUtils.calculateNecessaryWidthOfBoldLabel(maxLengthTitleLabel));
		cc.setPrefWidth(FxUtils.calculateNecessaryWidthOfBoldLabel(maxLengthTitleLabel));
		gp_.getColumnConstraints().add(cc);

		cc = new ColumnConstraints();
		cc.setHgrow(Priority.NEVER);
		cc.setMinWidth(FxUtils.calculateNecessaryWidthOfLabel(maxLengthOriginalLabel));
		cc.setPrefWidth(FxUtils.calculateNecessaryWidthOfLabel(maxLengthOriginalLabel));
		gp_.getColumnConstraints().add(cc);

		cc = new ColumnConstraints();
		cc.setHgrow(Priority.ALWAYS);
		gp_.getColumnConstraints().add(cc);
	}

	protected void setupGridPane(VBox topItems) {
		title = new Label(popupTitle);
		title.getStyleClass().add("titleLabel");
		title.setAlignment(Pos.CENTER);
		title.prefWidthProperty().bind(topItems.widthProperty());
		topItems.getChildren().add(title);
		VBox.setMargin(title, new Insets(10, 10, 10, 10));

		gp_.setHgap(10.0);
		gp_.setVgap(10.0);
		VBox.setMargin(gp_, new Insets(5, 5, 5, 5));
		topItems.getChildren().add(gp_);
	}

	public void finishInit(int conceptNid, PopupConceptViewI callingView)
	{
		this.conceptNid = conceptNid;
		callingView_ = callingView;
		
		String newTitle = popupTitle.replace("Modify", "New");
		title.setText(newTitle);		
		
		setupValidations();
		saveButton.disableProperty().bind(allValid_.not());
	}
}
