package gov.va.isaac.gui.conceptViews;

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
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.type_long.RefexLongVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipType;
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

    private static final Logger LOG = LoggerFactory.getLogger(SimpleConceptViewController.class);
	private static UUID snomedAssemblageUuid;

    @FXML private Label releaseIdLabel;
    @FXML private Label isPrimLabel;
    @FXML private Label fsnLabel;
    
    @FXML private VBox descriptionsBox;
    @FXML private HBox prefTermBox;
    @FXML private Label prefTermLabel;
    @FXML private VBox descLabelVBox;
    @FXML private VBox descTypeVBox;
    
    @FXML private VBox relationshipBox;
    @FXML private HBox isAChildBox;
    @FXML private Label isAChildLabel;
    @FXML private VBox relLabelVBox;
    @FXML private VBox relTypeVBox;


    @FXML private AnchorPane simpleConceptPane;
    @FXML private Button closeButton;
    @FXML private Button historyButton;
    @FXML private Button detailedButton;
    @FXML private Button inferredButton;
    
    public SimpleConceptViewController() {
		snomedAssemblageUuid = TermAux.SNOMED_IDENTIFIER.getUuids()[0];
    }
    
    public void setConcept(ConceptChronicleDdo concept)  {
    	initialize();

		final ConceptVersionBI con = WBUtility.getConceptVersion(concept.getPrimordialUuid());

    	try {
	        // FSN
	    	fsnLabel.setText(con.getFullySpecifiedDescription().getText());
	    	addDescTooltip(fsnLabel, con.getFullySpecifiedDescription());
	    	
	    	// PT 
	    	prefTermLabel.setText(con.getPreferredDescription().getText());
	    	addDescTooltip(prefTermLabel, con.getPreferredDescription());
	    	
	        // SCT Id
	        String sctidString = "Undefined";
			for (RefexChronicleBI<?> annotation : con.getAnnotations()) {
				if (annotation.getPrimordialUuid().equals(snomedAssemblageUuid)) {
					RefexLongVersionBI sctid = (RefexLongVersionBI) annotation.getPrimordialVersion();
					sctidString = Long.toString(sctid.getLong1());
				}
			}
			releaseIdLabel.setText(sctidString);

			// Defined Status
			if (con.getConceptAttributesActive().isDefined()) {
				isPrimLabel.setText("Fully Defined");
			} else {
				isPrimLabel.setText("Primitive");
			}
    	} catch (Exception e) {
    		LOG.error("Cannot access basic attributes for concept: " + con.getPrimordialUuid());
    	}
		

		// Descriptions
    	try {
	    	// Capture for sorting
	    	Map<Integer, Set<DescriptionVersionBI>> sortedDescs = new HashMap<>();

	    	for (DescriptionVersionBI desc : con.getDescriptionsActive()) {
	    		if (desc.getTypeNid() != SnomedMetadataRf2.PREFERRED_RF2.getNid() &&
    				desc.getTypeNid() != SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getNid()) {
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
		    		Label descLabel = getComponentLabel(desc.getText(), false);
		    		descLabelVBox.getChildren().add(descLabel);
	
		    		Label descTypeLabel = getComponentLabel(WBUtility.getConPrefTerm(desc.getTypeNid()), true);
		    		descTypeVBox.getChildren().add(descTypeLabel);

			    	addDescTooltip(descLabel, desc);
			    	addDescTooltip(descTypeLabel, desc);
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
			for (RelationshipVersionBI rel: isaRels) {
				if (!rel.isInferred()) {
		    		Label relLabel = getComponentLabel(WBUtility.getConPrefTerm(rel.getConceptNid()), false);
		    		relLabelVBox.getChildren().add(relLabel);
	
		    		Label relTypeLabel = getComponentLabel(WBUtility.getConPrefTerm(rel.getTypeNid()), true);
		    		relTypeVBox.getChildren().add(relTypeLabel);
	
			    	addRelTooltip(relLabel, rel);
		    		addRelTooltip(relTypeLabel, rel);
	    		}
			}
				
			// Display non-IS-As
			for (Integer relType: sortedRels.keySet()) {
	    		for (RelationshipVersionBI rel: sortedRels.get(relType)) {
	    			if (!rel.isInferred()) {
		    			Label relLabel = getComponentLabel(WBUtility.getConPrefTerm(rel.getConceptNid()), false);
			    		relLabelVBox.getChildren().add(relLabel);
		
			    		Label relTypeLabel = getComponentLabel(WBUtility.getConPrefTerm(rel.getTypeNid()), true);
			    		relTypeVBox.getChildren().add(relTypeLabel);
	
				    	addRelTooltip(relLabel, rel);
			    		addRelTooltip(relTypeLabel, rel);
		    		}
	    		}
	    	}
		} catch (IOException | ContradictionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }

	private Label getComponentLabel(String txt, boolean isType) {
		Label l = new Label(txt);
		l.setFont(new Font(18));
		l.setTextFill(Color.BLUE);
		
		if (isType) {
			l.setBackground(new Background(new BackgroundFill(Color.YELLOW, null, null)));
		}
		
		return null;
	}

	private void initialize() {
		closeButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				((Stage)simpleConceptPane.getScene().getWindow()).close();
		}});
	
	}
	private void addDescTooltip(Label node, DescriptionVersionBI desc) {
		final Tooltip tp = new Tooltip();
        tp.setFont(new Font("Arial", 14));
        
		String lang = desc.getLang();
		String text = desc.getText();
		String type = WBUtility.getConPrefTerm(desc.getTypeNid());
		String caseSig = desc.isInitialCaseSignificant() ? "Is Case Significant" : "Not Case Significant";
		
		String status = WBUtility.getStatusString(desc);
		String time =  WBUtility.getTimeString(desc);
		String author = WBUtility.getAuthorString(desc); 
		String module = WBUtility.getModuleString(desc);
		String path = WBUtility.getPathString(desc);

		tp.setText(text + " " + type + " " + caseSig + " " + lang + " " + status + " " + time + " " + author + " " + module + " " + path);
		node.setTooltip(tp);
	}

	private void addRelTooltip(Label node, RelationshipVersionBI rel) {
		final Tooltip tp = new Tooltip();
        tp.setFont(new Font("Arial", 14));

        
        String refinCharType = "";
		try {
			refinCharType = RelationshipType.getRelationshipType(rel.getRefinabilityNid(), rel.getCharacteristicNid()).toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String group = Integer.toString(rel.getGroup());
		String type = WBUtility.getConPrefTerm(rel.getTypeNid());
		String target = WBUtility.getConPrefTerm(rel.getDestinationNid());
		String statInf = rel.isInferred() ? "Inferred" : "Stated";

		String status = WBUtility.getStatusString(rel);
		String time =  WBUtility.getTimeString(rel);
		String author = WBUtility.getAuthorString(rel); 
		String module = WBUtility.getModuleString(rel);
		String path = WBUtility.getPathString(rel);

		tp.setText(target + " " + type + " " + " " + statInf + " " + refinCharType + " " + group+ " " + status + " " + time + " " + author + " " + module + " " + path);

		node.setTooltip(tp);
	}

	public String getTitle() {
		return "Simple Concept View";
	}
}
