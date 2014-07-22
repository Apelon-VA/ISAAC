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
 *	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.gui.conceptViews;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerHelper;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerLabelHelper;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerTooltipHelper;
import gov.va.isaac.interfaces.gui.TaxonomyViewI;
import gov.va.isaac.interfaces.gui.views.ConceptViewMode;

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
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseConceptViewController {
	private static final Logger LOG = LoggerFactory.getLogger(BaseConceptViewController.class);

	@FXML protected AnchorPane parentPane;
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
	
	protected ConceptViewerLabelHelper labelHelper = new ConceptViewerLabelHelper();
	protected ConceptViewerTooltipHelper tooltipHelper = new ConceptViewerTooltipHelper();
	
	protected ConceptVersionBI con;
	private BooleanBinding prevButtonQueueFilled;
	private ConceptViewMode currentView;

	void setConcept(UUID currentCon, ConceptViewMode view, Stack<Integer> stack) {
		initializeWindow(stack, view);
		setConceptInfo(currentCon);
	}
	
	void setConcept(UUID currentCon, ConceptViewMode view) {
		intializePane(view);
		setConceptInfo(currentCon);
	}
	
	abstract void setConceptInfo(UUID currentCon);

	Rectangle createAnnotRectangle(VBox vbox, ComponentVersionBI comp) {
		Rectangle rec = new Rectangle(5, 5);
		rec.setFill(Color.BLACK);
		
		rec.setVisible(!ConceptViewerHelper.getAnnotations(comp).isEmpty());
		
		vbox.getChildren().add(rec);
		
		return rec;
	}

	void initializeWindow(Stack<Integer> stack, ConceptViewMode view) {
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
				AppContext.getService(EnhancedConceptView.class).setConcept(prevConNid);
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

		if (parentPane == null) {
			LOG.error("parentPane is null");
		}
		if (tooltipHelper == null) {
			LOG.error("tooltipHelper is null");
		}
		parentPane.setOnKeyPressed(tooltipHelper.getCtrlKeyPressEventHandler());
		parentPane.setOnKeyReleased(tooltipHelper.getCtrlKeyReleasedEventHandler());
		
		detailedRadio.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				AppContext.getService(EnhancedConceptView.class).setViewMode(ConceptViewMode.DETAIL_VIEW);
			}
		});

		basicRadio.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				AppContext.getService(EnhancedConceptView.class).setViewMode(ConceptViewMode.SIMPLE_VIEW);
			}
		});
	}
	
	private void commonInit(ConceptViewMode view) {
		// TODO (Until handled, make disabled)
		modifyButton.setDisable(true);
		historicalRadio.setDisable(true);
		
		Tooltip notYetImplTooltip = new Tooltip("Not Yet Implemented");
		modifyButton.setTooltip(notYetImplTooltip);
		historicalRadio.setTooltip(notYetImplTooltip);
		
		setViewType(view);
		labelHelper.setPane(parentPane);
	}

	void intializePane(ConceptViewMode view) {
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
	
	public void setViewType(ConceptViewMode view) {
		currentView = view;
//		labelHelper.setCurrentMode(view);
		
		if (view == ConceptViewMode.SIMPLE_VIEW) {
			basicRadio.setSelected(true);
		} else if (view == ConceptViewMode.DETAIL_VIEW) {
			detailedRadio.setSelected(true);
		}
		
	}

	public ConceptViewMode getViewMode() {
		return currentView;
	}
	
	public String getTitle() {
        return fsnLabel.getText();
    }
	
    public Region getRootNode()
    {
        return parentPane;
    }
}
