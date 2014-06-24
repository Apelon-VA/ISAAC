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
package gov.va.isaac.gui.conceptViews.helpers;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.conceptViews.SimpleConceptView;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerHelper.ComponentType;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.util.WBUtility;

import java.util.Stack;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
*
* @author <a href="jefron@apelon.com">Jesse Efron</a>
*/
public class ConceptViewerLabelHelper {
	
	private static final Logger LOG = LoggerFactory.getLogger(ConceptViewerLabelHelper.class);
	
	private AnchorPane pane;
	private boolean isWindow = false;
	private Stack<Integer> previousConceptStack;
	
	private ConceptViewerTooltipHelper tooltipHelper = new ConceptViewerTooltipHelper();
	ConceptViewerHelper viewerHelper = new ConceptViewerHelper();


	// Create/Initialize without refNid
	public void initializeLabel(Label label, ComponentVersionBI comp, ComponentType type, String txt) {
		tooltipHelper.setDefaultTooltip(label, comp, type);
    	label.setText(txt);

		label.addEventHandler(MouseEvent.MOUSE_ENTERED, tooltipHelper.getCompTooltipEnterHandler(comp, type));
		label.addEventHandler(MouseEvent.MOUSE_EXITED, tooltipHelper.getCompTooltipExitHandler(comp, type));

		createConceptContextMenu(label, txt);
	}

	public Label createComponentLabel(ComponentVersionBI comp, String txt, ComponentType type, boolean isTypeLabel) {
		Label l = new Label();
		l.setFont(new Font(18));
//		l.setTextFill(Color.BLUE);
		
		if (isTypeLabel) {
//			l.setTextFill(Color.BLUE);
//			l.setStyle("-fx-background-color: YELLOW; -fx-border-color: GREEN; -fx-border-width: 2");
		}
		
		initializeLabel(l, comp, type, txt);
		
		return l;
	}

	private void createConceptContextMenu(Label label, String txt) {
		final ContextMenu rtClickMenu = new ContextMenu();

		MenuItem copyTextItem = new MenuItem("Copy Text");
		copyTextItem.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				viewerHelper.copyToClipboard(txt);
	        }
		});
				
		MenuItem copyContentItem = new MenuItem("Copy Content");
		copyContentItem.setGraphic(Images.COPY.createImageView());
		copyContentItem.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				viewerHelper.copyToClipboard(label.getTooltip().getText());
			}
		});
		rtClickMenu.getItems().add(copyContentItem);
		
		rtClickMenu.getItems().add(copyTextItem);

		label.setContextMenu(rtClickMenu);
	}


	// Create/Initialize with refNid
	public void initializeLabel(Label label, ComponentVersionBI comp, ComponentType type, String txt, int refNid) {
		initializeLabel(label, comp, type, txt);
		createConceptContextMenu(label, refNid, comp.getConceptNid());
	}

	public Label createComponentLabel(ComponentVersionBI comp, String txt, ComponentType type, boolean isTypeLabel, int refNid) {
		Label label = createComponentLabel(comp, txt, type, isTypeLabel);
		createConceptContextMenu(label, refNid, comp.getConceptNid());

		return label;
	}
	
	public void createIdsContextMenu(Label label, int refNid) {
		ContextMenu rtClickMenu = label.getContextMenu();

		Menu copyIdMenu = new Menu("Copy Ids");
		MenuItem sctIdItem = new MenuItem("SctId");
		MenuItem uuidItem = new MenuItem("UUID");
		MenuItem nidItem = new MenuItem("Native Id");
		copyIdMenu.getItems().addAll(sctIdItem, uuidItem, nidItem);
		
		sctIdItem.setGraphic(Images.COPY.createImageView());
		sctIdItem.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				ConceptVersionBI con = WBUtility.getConceptVersion(refNid);
				ConceptAttributeVersionBI attr = viewerHelper.getConceptAttributes(con);
				viewerHelper.copyToClipboard(viewerHelper.getSctId(attr));
			}
		});

		uuidItem.setGraphic(Images.COPY.createImageView());
		uuidItem.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				ConceptVersionBI con = WBUtility.getConceptVersion(refNid);
				viewerHelper.copyToClipboard(con.getPrimordialUuid().toString());
			}
		});

		nidItem.setGraphic(Images.COPY.createImageView());
		nidItem.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				ConceptVersionBI con = WBUtility.getConceptVersion(refNid);
				viewerHelper.copyToClipboard(Integer.toString(con.getNid()));
			}
		});
		
		rtClickMenu.getItems().addAll(copyIdMenu);
		label.setContextMenu(rtClickMenu);
	}
	
	private void createConceptContextMenu(Label label, int refNid, int currentConceptNid) {
		ContextMenu rtClickMenu = label.getContextMenu();
		
		if (isWindow) {
			MenuItem viewItem = new MenuItem("View Concept");
			viewItem.setGraphic(Images.CONCEPT_VIEW.createImageView());
			viewItem.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent event)
				{
					previousConceptStack.push(currentConceptNid);
					AppContext.getService(SimpleConceptView.class).changeConcept(((Stage)pane.getScene().getWindow()), refNid);
				}
			});
			
			rtClickMenu.getItems().add(0, viewItem);
		}
		
		MenuItem viewNewItem = new MenuItem("View Concept New Panel");
		viewNewItem.setGraphic(Images.COPY.createImageView());
		viewNewItem.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				AppContext.getService(SimpleConceptView.class).setConcept(refNid);
			}
		});
		
		rtClickMenu.getItems().add(1, viewNewItem);
		
		label.setContextMenu(rtClickMenu);
		createIdsContextMenu(label, refNid);
	}


	// Setters&Getters
	public void setPane(AnchorPane simpleConceptPane) {
		pane = simpleConceptPane;
	}

	public void setIsWindow(boolean isWindow) {
		this.isWindow = isWindow;
	}

	public Stack<Integer> getPreviousConceptStack() {
		return previousConceptStack;
	}

	public void setPrevConStack(Stack<Integer> stack) {
		previousConceptStack = stack;
	}
}
