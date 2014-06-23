package gov.va.isaac.gui.conceptViews;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.conceptViews.ConceptViewerHelper.ComponentType;
import gov.va.isaac.interfaces.gui.TaxonomyViewI;
import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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

	private ConceptViewerHelper helper = new ConceptViewerHelper();
	
	private static final Logger LOG = LoggerFactory.getLogger(SimpleConceptViewController.class);
	private ConceptVersionBI con;

    @FXML private Label releaseIdLabel;
    @FXML private Label isPrimLabel;
    @FXML private Label fsnLabel;
    
    @FXML private VBox descriptionsBox;
    @FXML private HBox prefTermBox;
    @FXML private Label prefTermLabel;
    @FXML private Label prefTypeLabel;
    @FXML private VBox descLabelVBox;
    @FXML private VBox descTypeVBox;
    
    @FXML private VBox relationshipBox;
    @FXML private HBox isAChildBox;
    @FXML private Label isAChildLabel;
    @FXML private VBox relLabelVBox;
    @FXML private VBox relTypeVBox;


    @FXML private AnchorPane simpleConceptPane;
    @FXML private Button closeButton;
    @FXML private Button taxonomyButton;
    @FXML private Button historyButton;
    @FXML private Button detailedButton;
    @FXML private Button inferredButton;
    
    public SimpleConceptViewController() {
		helper = new ConceptViewerHelper();
    }
    
    public void setConcept(ConceptChronicleDdo concept)  {
    	initialize();

		con = WBUtility.getConceptVersion(concept.getPrimordialUuid());
		
    	try {
	        // FSN
    		helper.initializeLabel(fsnLabel, con.getFullySpecifiedDescription(), ComponentType.DESCRIPTION, con.getFullySpecifiedDescription().getText());
	    	
	    	// PT 
    		helper.initializeLabel(prefTermLabel, con.getPreferredDescription(), ComponentType.DESCRIPTION, con.getPreferredDescription().getText());
    		helper.initializeLabel(prefTypeLabel, con.getPreferredDescription(), ComponentType.DESCRIPTION, con.getPreferredDescription().getText());

    		ConceptAttributeVersionBI attr = con.getConceptAttributesActive();

    		// SCT Id
    		helper.initializeLabel(releaseIdLabel, attr, ComponentType.CONCEPT, helper.getSctId(attr));

    		// Defined Status
    		helper.initializeLabel(isPrimLabel, attr, ComponentType.CONCEPT, helper.getPrimDef(attr));
    	} catch (Exception e) {
    		LOG.error("Cannot access basic attributes for concept: " + con.getPrimordialUuid());
    	}
		

		// Descriptions
    	try {
	    	// Capture for sorting
	    	Map<Integer, Set<DescriptionVersionBI>> sortedDescs = new HashMap<>();

	    	for (DescriptionVersionBI desc : con.getDescriptionsActive()) {
	    		if (desc.getTypeNid() != SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getNid()) {
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
			    		Label descLabel = helper.createComponentLabel(desc, desc.getText(), ComponentType.DESCRIPTION, false);
			    		descLabelVBox.getChildren().add(descLabel);
		
			    		Label descTypeLabel = helper.createComponentLabel(desc, WBUtility.getConPrefTerm(desc.getTypeNid()), ComponentType.DESCRIPTION, true);
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
				if (rel.getNid() == Snomed.IS_A.getNid()) {
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }

	private void addRels(Set<RelationshipVersionBI> rels) {
		for (RelationshipVersionBI rel: rels) {
			if (!rel.isInferred()) {
	    		Label relLabel = helper.createComponentLabel(rel, WBUtility.getConPrefTerm(rel.getDestinationNid()), ComponentType.RELATIONSHIP, false);
	    		relLabelVBox.getChildren().add(relLabel);

	    		Label relTypeLabel = helper.createComponentLabel(rel, WBUtility.getConPrefTerm(rel.getTypeNid()), ComponentType.RELATIONSHIP, true);
	    		relTypeVBox.getChildren().add(relTypeLabel);
    		}
		}
	}

	private void initialize() {
		closeButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				((Stage)simpleConceptPane.getScene().getWindow()).close();
		}});
		
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
		
/*		if (simpleConceptPane.getScene() != null) {
			simpleConceptPane.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {

				@Override
				public void handle(KeyEvent event) {
					int a = 2;
					if (event.getCode() == KeyCode.CONTROL)
					{
						controlKeyPressed = true;
					}
				}
				
			});
		}
*/	}

	public String getTitle() {
		return "Simple Concept View";
	}
}
