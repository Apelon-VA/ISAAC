package gov.va.isaac.gui.conceptViews;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerHelper;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerLabelHelper;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerTooltipHelper;
import gov.va.isaac.interfaces.gui.TaxonomyViewI;
import gov.va.isaac.interfaces.gui.views.EnhancedConceptViewI.ViewType;

import java.util.Stack;
import java.util.UUID;

import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;

public abstract class BaseConceptViewController {
	@FXML private AnchorPane parentPane;
	@FXML private BorderPane baseConceptBorderPane;

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
    @FXML protected Button taxonomyButton;
    @FXML protected Button modifyButton;
    @FXML protected Button previousButton;
    
    protected ConceptViewerHelper viewerHelper = new ConceptViewerHelper();
    protected ConceptViewerLabelHelper labelHelper = new ConceptViewerLabelHelper();
    protected ConceptViewerTooltipHelper tooltipHelper = new ConceptViewerTooltipHelper();
	
    protected ConceptVersionBI con;
	private BooleanBinding prevButtonQueueFilled;
	private ViewType currentView;

    void setConcept(ConceptChronicleDdo concept, ViewType view, Stack<Integer> stack) {
		initializeWindow(stack, view);
		setConceptDetails(concept);
	}
	
	void setConcept(ConceptChronicleDdo concept, ViewType view) {
		intializePane(view);
		setConceptDetails(concept);
	}
	
	abstract void setConceptDetails(ConceptChronicleDdo concept);

	Rectangle createAnnotRectangle(VBox vbox, ComponentVersionBI comp) {
		Rectangle rec = new Rectangle(5, 5);
		rec.setFill(Color.BLACK);
		
		rec.setVisible(!viewerHelper.getAnnotations(comp).isEmpty());
		
		vbox.getChildren().add(rec);
		
		return rec;
	}

	void initializeWindow(Stack<Integer> stack, ViewType view) {
		commonInit(view);
		
		labelHelper.setPrevConStack(stack);
		labelHelper.setIsWindow(true);

		closeButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				((Stage)parentPane.getScene().getWindow()).close();
			}
		});
		
		taxonomyButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				UUID id = con.getPrimordialUuid();
				if (id != null)
				{
					AppContext.getService(TaxonomyViewI.class).locateConcept(id, null);
				}
				else
				{
					AppContext.getCommonDialogs().showInformationDialog("Invalid Concept", "Can't locate an invalid concept");
				}
			}
		});
		
		previousButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				
				int prevConNid = labelHelper.getPreviousConceptStack().pop();
				AppContext.getService(EnhancedConceptView.class).changeConcept(((Stage)parentPane.getScene().getWindow()), prevConNid, currentView);
			}
		});
		
		prevButtonQueueFilled = new BooleanBinding()
		{
			@Override
			protected boolean computeValue()
			{
				return !labelHelper.getPreviousConceptStack().isEmpty();
			}
		};
		previousButton.disableProperty().bind(prevButtonQueueFilled.not());
		
		parentPane.setOnKeyPressed(tooltipHelper.getCtrlKeyPressEventHandler());
		parentPane.setOnKeyReleased(tooltipHelper.getCtrlKeyReleasedEventHandler());
		
		detailedRadio.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				AppContext.getService(EnhancedConceptView.class).changeViewType(((Stage)parentPane.getScene().getWindow()), con.getNid(), ViewType.DETAIL_VIEW);
			}
		});

		basicRadio.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				AppContext.getService(EnhancedConceptView.class).changeViewType(((Stage)parentPane.getScene().getWindow()), con.getNid(), ViewType.SIMPLE_VIEW);
			}
		});
	}
	
	private void commonInit(ViewType view) {
    	// TODO (Until handled, make disabled)
		modifyButton.setDisable(true);
		historicalRadio.setDisable(true);
		
		Tooltip notYetImplTooltip = new Tooltip("Not Yet Implemented");
		modifyButton.setTooltip(notYetImplTooltip);
		historicalRadio.setTooltip(notYetImplTooltip);
		
		setViewType(view);
		labelHelper.setPane(parentPane);
	}

	void intializePane(ViewType view) {
		commonInit(view);
		closeButton.setVisible(false);
		previousButton.setVisible(false);
		modifyButton.setVisible(false);
		taxonomyButton.setVisible(false);
	}

	String getBooleanValue(boolean val) {
		if (val) {
			return "true";
		} else {
			return "false";
		}
	}
	
	public void setViewType(ViewType view) {
		currentView = view;
		labelHelper.setCurrentView(view);
		
		if (view == ViewType.SIMPLE_VIEW) {
			basicRadio.setSelected(true);
		} else if (view == ViewType.DETAIL_VIEW) {
			detailedRadio.setSelected(true);
		}
		
	}
}
