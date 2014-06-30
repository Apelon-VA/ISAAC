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
package gov.va.isaac.gui.conceptViews;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerHelper;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerHelper.ComponentType;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerLabelHelper;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerTooltipHelper;
import gov.va.isaac.interfaces.gui.TaxonomyViewI;
import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
*
* @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
* @author <a href="jefron@apelon.com">Jesse Efron</a>
*/

public class SimpleConceptViewController {
	@FXML private AnchorPane simpleConceptPane;
    
	// Top Labels
    @FXML private Label releaseIdLabel;
    @FXML private Label isPrimLabel;
    @FXML private Label fsnLabel;
    @FXML private VBox conAnnotVBox;
    @FXML private VBox fsnAnnotVBox;
    
    // Descriptions
    @FXML private VBox descriptionsBox;
    @FXML private HBox prefTermBox;
    @FXML private Label prefLabel;
    @FXML private Label prefTypeLabel;
    @FXML private VBox descLabelVBox;
    @FXML private VBox descTypeVBox;
    @FXML private VBox descAnnotVBox;
    
    // Relationships
    @FXML private VBox relationshipBox;
    @FXML private HBox isAChildBox;
    @FXML private Label isAChildLabel;
    @FXML private VBox relLabelVBox;
    @FXML private VBox relTypeVBox;
    @FXML private VBox relAnnotVBox;
    
    // Radio Buttons
    @FXML private ToggleGroup viewGroup;
    @FXML private RadioButton  historicalRadio;
    @FXML private RadioButton basicRadio;
    @FXML private RadioButton detailedRadio;

    // Buttons
    @FXML private Button closeButton;
    @FXML private Button taxonomyButton;
    @FXML private Button modifyButton;
    @FXML private Button previousButton;

	private ConceptViewerHelper viewerHelper = new ConceptViewerHelper();
	private ConceptViewerLabelHelper labelHelper = new ConceptViewerLabelHelper();
	private ConceptViewerTooltipHelper tooltipHelper = new ConceptViewerTooltipHelper();
	
	private static final Logger LOG = LoggerFactory.getLogger(SimpleConceptViewController.class);
	private ConceptVersionBI con;
	private BooleanBinding prevButtonQueueFilled;

	// Called on Window
	void setConcept(ConceptChronicleDdo concept, Stack<Integer> stack) {
		initializeWindow(stack);
		setupConceptDetails(concept);		
	}

	// Called on Panel
	public void setConcept(ConceptChronicleDdo concept)  {
		intializePane();
		setupConceptDetails(concept);		
	}
	
	private void setupConceptDetails(ConceptChronicleDdo concept) {
		con = WBUtility.getConceptVersion(concept.getPrimordialUuid());
		
    	try {
	        // FSN
    		labelHelper.initializeLabel(fsnLabel, con.getFullySpecifiedDescription(), ComponentType.DESCRIPTION, con.getFullySpecifiedDescription().getText(), false);
			createAnnotRectangle(fsnAnnotVBox, con.getFullySpecifiedDescription());

	    	// PT 
    		labelHelper.initializeLabel(prefLabel, con.getPreferredDescription(), ComponentType.DESCRIPTION, con.getPreferredDescription().getText(), false);
    		labelHelper.initializeLabel(prefTypeLabel, con.getPreferredDescription(), ComponentType.DESCRIPTION, prefTypeLabel.getText(), SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid(), true);
			createAnnotRectangle(descAnnotVBox, con.getPreferredDescription());

			ConceptAttributeVersionBI attr = viewerHelper.getConceptAttributes(con);
		
			// SCT Id
    		labelHelper.initializeLabel(releaseIdLabel, attr, ComponentType.CONCEPT, viewerHelper.getSctId(attr), false);
    		labelHelper.createIdsContextMenu(releaseIdLabel, con.getNid());
			createAnnotRectangle(conAnnotVBox, con);

    		// Defined Status
    		labelHelper.initializeLabel(isPrimLabel, attr, ComponentType.CONCEPT, viewerHelper.getPrimDef(attr), viewerHelper.getPrimDefNid(attr), true);
    	} catch (Exception e) {
    		LOG.error("Cannot access basic attributes for concept: " + con.getPrimordialUuid());
    	}
		

		// Descriptions
    	try {
	    	// Capture for sorting
	    	Map<Integer, Set<DescriptionVersionBI>> sortedDescs = new HashMap<>();

	    	for (DescriptionVersionBI desc : con.getDescriptionsActive()) {
	    		if (desc.getNid() != con.getFullySpecifiedDescription().getNid() &&
    				desc.getNid() != con.getPreferredDescription().getNid()) {
		    		if (!sortedDescs.containsKey(desc.getTypeNid())) {
		    			Set<DescriptionVersionBI> descs = new HashSet<>();
		    			sortedDescs.put(desc.getTypeNid(), descs);
		    		}

		    		sortedDescs.get(desc.getTypeNid()).add(desc);
	    		}
	    	}
	       	
	    	// Display
	    	for (Integer descType: sortedDescs.keySet()) {
	    		for (DescriptionVersionBI desc: sortedDescs.get(descType)) {
	    			if (desc.getNid() != con.getPreferredDescription().getNid()) {
	    				createAnnotRectangle(descAnnotVBox, desc);
	    				
			    		Label descLabel = labelHelper.createComponentLabel(desc, desc.getText(), ComponentType.DESCRIPTION, false);
			    		descLabelVBox.getChildren().add(descLabel);
		
			    		Label descTypeLabel = labelHelper.createComponentLabel(desc, WBUtility.getConPrefTerm(desc.getTypeNid()), ComponentType.DESCRIPTION, desc.getTypeNid(), true);
			    		descTypeVBox.getChildren().add(descTypeLabel);
		    		}
	    		}
	    	}
    	} catch (Exception e) {
    		LOG.error("Cannot access descriptions for concept: " + con.getPrimordialUuid());
    	}
    	
    	// Relationships
    	try {
        	// Capture for sorting (storing is-a in different collection
        	Map<Integer, Set<RelationshipVersionBI>> sortedRels = new HashMap<>();
        	Set<RelationshipVersionBI> isaRels = new HashSet<>();
			for (RelationshipVersionBI rel : con.getRelationshipsOutgoingActive()) {
				if (rel.getTypeNid() == Snomed.IS_A.getNid()) {
					isaRels.add(rel);
				} else {
					if (!sortedRels.containsKey(rel.getTypeNid())) {
						Set<RelationshipVersionBI> rels = new HashSet<>();
						sortedRels.put(rel.getTypeNid(), rels);
					}

					sortedRels.get(rel.getTypeNid()).add(rel);
				}
			}
	    	
	    	// Display IS-As
			addRels(isaRels);

			for (Integer relType: sortedRels.keySet()) {
				// Display non-IS-As
				addRels(sortedRels.get(relType));
			}
		} catch (IOException | ContradictionException e) {
    		LOG.error("Cannot access relationships for concept: " + con.getPrimordialUuid());
		}
    	
    }

	private void addRels(Set<RelationshipVersionBI> rels) {
		for (RelationshipVersionBI rel: rels) {
			if (!rel.isInferred()) {
				createAnnotRectangle(relAnnotVBox, rel);

				Label relLabel = labelHelper.createComponentLabel(rel, WBUtility.getConPrefTerm(rel.getDestinationNid()), ComponentType.RELATIONSHIP, rel.getDestinationNid(), true);
	    		relLabelVBox.getChildren().add(relLabel);

	    		Label relTypeLabel = labelHelper.createComponentLabel(rel, WBUtility.getConPrefTerm(rel.getTypeNid()), ComponentType.RELATIONSHIP, rel.getTypeNid(), true);
	    		relTypeVBox.getChildren().add(relTypeLabel);
    		}
		}
	}

	public String getTitle() {
		return "Simple Concept View";
	}

	private void commonInit() {
    	// TODO (Until handled, make disabled)
		modifyButton.setDisable(true);
		historicalRadio.setDisable(true);
		detailedRadio.setDisable(true);
		
		Tooltip notYetImplTooltip = new Tooltip("Not Yet Implemented");
		modifyButton.setTooltip(notYetImplTooltip);
		historicalRadio.setTooltip(notYetImplTooltip);
		detailedRadio.setTooltip(notYetImplTooltip);
		
		labelHelper.setPane(simpleConceptPane);
	}

	private void intializePane() {
		commonInit();
		closeButton.setVisible(false);
		previousButton.setVisible(false);
		modifyButton.setVisible(false);
		taxonomyButton.setVisible(false);
	}

	private void initializeWindow(Stack<Integer> stack) {
		commonInit();

		labelHelper.setPrevConStack(stack);
		labelHelper.setIsWindow(true);

		closeButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				((Stage)simpleConceptPane.getScene().getWindow()).close();
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
				AppContext.getService(SimpleConceptView.class).changeConcept(((Stage)simpleConceptPane.getScene().getWindow()), prevConNid);
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
		
		simpleConceptPane.setOnKeyPressed(tooltipHelper.getCtrlKeyPressEventHandler());
		simpleConceptPane.setOnKeyReleased(tooltipHelper.getCtrlKeyReleasedEventHandler());
	}
	
	Rectangle createAnnotRectangle(VBox vbox, ComponentVersionBI comp) {
		Rectangle rec = new Rectangle(5, 5);
		rec.setFill(Color.BLACK);
		
		rec.setVisible(!viewerHelper.getAnnotations(comp).isEmpty());
		
		vbox.getChildren().add(rec);
		
		return rec;
	}
}
