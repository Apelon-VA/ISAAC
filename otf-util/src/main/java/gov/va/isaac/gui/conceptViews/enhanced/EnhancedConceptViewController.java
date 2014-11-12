package gov.va.isaac.gui.conceptViews.enhanced;

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerLabelHelper;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerTooltipHelper;
import gov.va.isaac.gui.conceptViews.helpers.EnhancedConceptBuilder;
import gov.va.isaac.interfaces.gui.constants.ConceptViewMode;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.PopupConceptViewI;
import gov.va.isaac.util.UpdateableBooleanBinding;
import gov.va.isaac.util.WBUtility;
import java.io.IOException;
import java.util.UUID;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnhancedConceptViewController {
	@FXML protected AnchorPane enhancedConceptPane;

	// Descriptions & Relationships
	@FXML private VBox termVBox;
	@FXML private VBox relVBox;
	@FXML private VBox destVBox;
	@FXML private ScrollPane destScrollPane;
	
	// Top Labels
	@FXML protected Label releaseIdLabel;
	@FXML protected Label isPrimLabel;
	@FXML protected Label fsnLabel;
	@FXML protected VBox conAnnotVBox;
	@FXML protected VBox fsnAnnotVBox;
	
	// Radio Buttons
	@FXML protected ToggleGroup viewGroup;
	@FXML protected RadioButton  historicalRadio;
	@FXML protected RadioButton basicRadio;
	@FXML protected RadioButton detailedRadio;

	// Buttons
	@FXML protected Button closeButton;
	@FXML protected Button previousButton;
	@FXML protected Button commitButton;
	@FXML protected Button cancelButton;
	
	protected ConceptViewerLabelHelper labelHelper;
	protected ConceptViewerTooltipHelper tooltipHelper = new ConceptViewerTooltipHelper();
	
	protected ConceptVersionBI concept;
	private UpdateableBooleanBinding prevButtonQueueFilled;
	
	public PopupConceptViewI conceptView;
	private ConceptViewMode currentMode = ConceptViewMode.SIMPLE_VIEW;

	private EnhancedConceptBuilder creator;

	private boolean initialized = false;

	private static final Logger LOG = LoggerFactory.getLogger(EnhancedConceptViewController.class);

	AnchorPane getRootNode() {
		return enhancedConceptPane;
	}

	public void setConceptView(EnhancedConceptView enhancedConceptView) {
		conceptView = enhancedConceptView;		
	}

	void setConcept(UUID currentCon, ConceptViewMode mode, ObservableList<Integer> conceptHistoryStack) {
		if (!initialized ) {
			initialized = true;
			initializeWindow(conceptHistoryStack, mode);
		}
		concept = WBUtility.getConceptVersion(currentCon);
		labelHelper.setConcept(concept.getNid());
		clearContents();
		updateCommitButton();
		creator.setConceptValues(concept, mode);
	}
	
	void setConcept(UUID currentCon, ConceptViewMode mode) {
		if (!initialized ) {
			initialized = true;
			intializePane(mode);
		}
		
		concept = WBUtility.getConceptVersion(currentCon);
		labelHelper.setConcept(concept.getNid());
		clearContents();
		creator.setConceptValues(concept, mode);
	}

	void initializeWindow(ObservableList<Integer> conceptHistoryStack, ConceptViewMode view) {
		commonInit(view);
		
		labelHelper.setPrevConStack(conceptHistoryStack);
		labelHelper.setIsWindow(true);

		closeButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				((Stage)getRootNode().getScene().getWindow()).close();
			}
		});
		
		previousButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				int lastItemIdx = labelHelper.getPreviousConceptStack().size() - 1;
				int prevConNid = labelHelper.getPreviousConceptStack().remove(lastItemIdx);
				conceptView.setConcept(prevConNid);
			}
		});
		
		commitButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				try
				{
					WBUtility.commit(concept);
					clearContents();
					commitButton.setDisable(true);
					cancelButton.setDisable(true);
					creator.setConceptValues(concept, currentMode);
				}
				catch (IOException e)
				{
					LOG.error("Unexpected error during commit", e);
					ExtendedAppContext.getCommonDialogs().showErrorDialog("Error committing concept", e);
				}
			}
		});
		
		cancelButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				try {
					ExtendedAppContext.getDataStore().forget(concept.getChronicle());

					clearContents();
					commitButton.setDisable(true);
					cancelButton.setDisable(true);
					creator.setConceptValues(concept, currentMode);
				} catch (Exception e) {
					LOG.error("Unable to cancel concept: " + concept.getNid(), e);
				}
			}
		});
		
		prevButtonQueueFilled = new UpdateableBooleanBinding()
		{
			{
				addBinding(labelHelper.getPreviousConceptStack());
				setComputeOnInvalidate(true);
			}
			
			@Override
			protected boolean computeValue()
			{
				return !labelHelper.getPreviousConceptStack().isEmpty();
			}
		};
		previousButton.disableProperty().bind(prevButtonQueueFilled.not());

		if (getRootNode() == null) {
			LOG.error("getRootNode() is null");
		}
		if (tooltipHelper == null) {
			LOG.error("tooltipHelper is null");
		}
		getRootNode().setOnKeyPressed(tooltipHelper.getCtrlKeyPressEventHandler());
		getRootNode().setOnKeyReleased(tooltipHelper.getCtrlKeyReleasedEventHandler());
		
		detailedRadio.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				conceptView.setViewMode(ConceptViewMode.DETAIL_VIEW);
			}
		});

		basicRadio.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				conceptView.setViewMode(ConceptViewMode.SIMPLE_VIEW);
			}
		});

		historicalRadio.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				conceptView.setViewMode(ConceptViewMode.HISTORICAL_VIEW);
			}
		});
	}
	
	public void setViewMode(ConceptViewMode mode) {
		currentMode = mode;
		clearContents();
		creator.setConceptValues(concept, mode);
	}

	private void commonInit(ConceptViewMode mode) {
		creator = new EnhancedConceptBuilder(enhancedConceptPane, termVBox, relVBox, destVBox, destScrollPane, fsnAnnotVBox, conAnnotVBox, fsnLabel, releaseIdLabel, isPrimLabel);
		
		labelHelper = new ConceptViewerLabelHelper(conceptView);
		labelHelper.setPane(getRootNode());
		creator.setLabelHelper(labelHelper);
		
		setModeType(mode);
	}

	void intializePane(ConceptViewMode view) {
		commonInit(view);
		closeButton.setVisible(false);
		previousButton.setVisible(false);
	}

	private void updateCommitButton() {
		boolean isUncommitted = WBUtility.isUncommittened(concept);
		commitButton.setDisable(!isUncommitted);
		cancelButton.setDisable(!isUncommitted);
	}

	public void setModeType(ConceptViewMode mode) {
		currentMode = mode;
		
		if (mode == ConceptViewMode.SIMPLE_VIEW) {
			basicRadio.setSelected(true);
		} else if (mode == ConceptViewMode.DETAIL_VIEW) {
			detailedRadio.setSelected(true);
		} else if (mode == ConceptViewMode.HISTORICAL_VIEW) {
			historicalRadio.setSelected(true);
		}
	}

	public ConceptViewMode getViewMode() {
		return currentMode;
	}
	
	public String getTitle() {
        return fsnLabel.getText();
    }
	
	private void clearContents() {
		releaseIdLabel.setText("");
		isPrimLabel.setText("");
		fsnLabel.setText("");
		termVBox.getChildren().clear();
		destVBox.getChildren().clear();
		relVBox.getChildren().clear();
		conAnnotVBox.getChildren().clear();
		fsnAnnotVBox.getChildren().clear();
	}
}
