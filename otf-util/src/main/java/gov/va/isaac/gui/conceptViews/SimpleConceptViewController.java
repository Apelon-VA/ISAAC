package gov.va.isaac.gui.conceptViews;

import gov.va.isaac.AppContext;
import gov.va.isaac.interfaces.gui.TaxonomyViewI;
import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
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
	public enum ComponentType {
		CONCEPT, DESCRIPTION, RELATIONSHIP;
	}
	
    public class CompTooltipExitHandler implements EventHandler {
		private ComponentVersionBI  comp;
		private ComponentType type;

		public CompTooltipExitHandler(ComponentVersionBI comp, ComponentType type) {
			this.comp = comp;
			this.type = type;
		}

		@Override
		public void handle(Event event) {
			Label l = (Label)event.getSource();
			if (type == ComponentType.CONCEPT) {
				l.getTooltip().setText("");
			} else if (type == ComponentType.DESCRIPTION) {
				l.getTooltip().setText(createDescTooltipText((DescriptionVersionBI)comp));
			} else {
				l.getTooltip().setText(createRelTooltipText((RelationshipVersionBI)comp));
			}

		}
    }

	public class CompTooltipHandler implements EventHandler {
		private ComponentVersionBI  comp;

		public CompTooltipHandler(ComponentVersionBI comp) {
			this.comp = comp;
		}

		@Override
		public void handle(Event event) {
			Label l = (Label)event.getSource();
			if (controlKeyPressed) {
				String tp = "There are no refsets for this component";

				try {
					Collection<? extends RefexVersionBI<?>> annots = comp.getAnnotationsActive(WBUtility.getViewCoordinate());
					
					for (RefexVersionBI annot : annots) {
						String refset = WBUtility.getConPrefTerm(annot.getAssemblageNid());
						String s = annot.toString();
						tp = "Assemblage: " + refset;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

				l.getTooltip().setText(tp);
			}
		}

	}

	private static final Logger LOG = LoggerFactory.getLogger(SimpleConceptViewController.class);
	private static int snomedAssemblageNid;
	private static boolean controlKeyPressed = false;
	private ConceptVersionBI con;

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
    @FXML private Button taxonomyButton;
    @FXML private Button historyButton;
    @FXML private Button detailedButton;
    @FXML private Button inferredButton;
    
    public SimpleConceptViewController() {
		snomedAssemblageNid = WBUtility.getConceptVersion(TermAux.SNOMED_IDENTIFIER.getUuids()[0]).getNid();
    }
    
    public void setConcept(ConceptChronicleDdo concept)  {
    	initialize();

		con = WBUtility.getConceptVersion(concept.getPrimordialUuid());

    	try {
	        // FSN
	    	fsnLabel.setText(con.getFullySpecifiedDescription().getText());
	    	addDescTooltip(fsnLabel, con.getFullySpecifiedDescription());
			fsnLabel.addEventHandler(MouseEvent.MOUSE_ENTERED, new CompTooltipHandler(con.getFullySpecifiedDescription()));
			fsnLabel.addEventHandler(MouseEvent.MOUSE_EXITED, new CompTooltipExitHandler(con.getFullySpecifiedDescription(), ComponentType.DESCRIPTION));
	    	
	    	// PT 
	    	prefTermLabel.setText(con.getPreferredDescription().getText());
	    	addDescTooltip(prefTermLabel, con.getPreferredDescription());
			prefTermLabel.addEventHandler(MouseEvent.MOUSE_ENTERED, new CompTooltipHandler(con.getPreferredDescription()));
			prefTermLabel.addEventHandler(MouseEvent.MOUSE_EXITED, new CompTooltipExitHandler(con.getPreferredDescription(), ComponentType.DESCRIPTION));
	    	
	        // SCT Id
	        String sctidString = "Unreleased";
            // Official approach found int AlternativeIdResource.class
            for (RefexChronicleBI<?> annotation : con.getAnnotations()) {
				if (annotation.getAssemblageNid() == snomedAssemblageNid) {
					RefexLongVersionBI sctid = (RefexLongVersionBI) annotation.getPrimordialVersion();
					sctidString = Long.toString(sctid.getLong1());
				}
			}
			releaseIdLabel.setText(sctidString);
			releaseIdLabel.addEventHandler(MouseEvent.MOUSE_ENTERED, new CompTooltipHandler(con.getConceptAttributesActive()));
			releaseIdLabel.addEventHandler(MouseEvent.MOUSE_EXITED, new CompTooltipExitHandler(con.getConceptAttributesActive(), ComponentType.CONCEPT));

			// Defined Status
			if (con.getConceptAttributesActive().isDefined()) {
				isPrimLabel.setText("Fully Defined");
			} else {
				isPrimLabel.setText("Primitive");
			}
			isPrimLabel.addEventHandler(MouseEvent.MOUSE_ENTERED, new CompTooltipHandler(con.getConceptAttributesActive()));
			isPrimLabel.addEventHandler(MouseEvent.MOUSE_EXITED, new CompTooltipExitHandler(con.getConceptAttributesActive(), ComponentType.CONCEPT));
			
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
			    		Label descLabel = getComponentLabel(desc.getText(), false);
			    		descLabelVBox.getChildren().add(descLabel);
		
			    		Label descTypeLabel = getComponentLabel(WBUtility.getConPrefTerm(desc.getTypeNid()), true);
			    		descTypeVBox.getChildren().add(descTypeLabel);
	
				    	addDescTooltip(descLabel, desc);
				    	addDescTooltip(descTypeLabel, desc);
				    	descLabel.addEventHandler(MouseEvent.MOUSE_ENTERED, new CompTooltipHandler(desc));
				    	descTypeLabel.addEventHandler(MouseEvent.MOUSE_ENTERED, new CompTooltipHandler(desc));
				    	descLabel.addEventHandler(MouseEvent.MOUSE_EXITED, new CompTooltipExitHandler(desc, ComponentType.DESCRIPTION));
				    	descTypeLabel.addEventHandler(MouseEvent.MOUSE_EXITED, new CompTooltipExitHandler(desc, ComponentType.DESCRIPTION));
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
			for (RelationshipVersionBI rel: isaRels) {
				if (!rel.isInferred()) {
		    		Label relLabel = getComponentLabel(WBUtility.getConPrefTerm(rel.getDestinationNid()), false);
		    		relLabelVBox.getChildren().add(relLabel);
	
		    		Label relTypeLabel = getComponentLabel(WBUtility.getConPrefTerm(rel.getTypeNid()), true);
		    		relTypeVBox.getChildren().add(relTypeLabel);
	
			    	addRelTooltip(relLabel, rel);
		    		addRelTooltip(relTypeLabel, rel);
			    	relLabel.addEventHandler(MouseEvent.MOUSE_ENTERED, new CompTooltipHandler(rel));
			    	relTypeLabel.addEventHandler(MouseEvent.MOUSE_ENTERED, new CompTooltipHandler(rel));
			    	relLabel.addEventHandler(MouseEvent.MOUSE_EXITED, new CompTooltipExitHandler(rel, ComponentType.RELATIONSHIP));
			    	relTypeLabel.addEventHandler(MouseEvent.MOUSE_EXITED, new CompTooltipExitHandler(rel, ComponentType.RELATIONSHIP));
	    		}
			}
				
			// Display non-IS-As
			for (Integer relType: sortedRels.keySet()) {
	    		for (RelationshipVersionBI rel: sortedRels.get(relType)) {
	    			if (!rel.isInferred()) {
		    			Label relLabel = getComponentLabel(WBUtility.getConPrefTerm(rel.getDestinationNid()), false);
			    		relLabelVBox.getChildren().add(relLabel);
		
			    		Label relTypeLabel = getComponentLabel(WBUtility.getConPrefTerm(rel.getTypeNid()), true);
			    		relTypeVBox.getChildren().add(relTypeLabel);
	
				    	addRelTooltip(relLabel, rel);
			    		addRelTooltip(relTypeLabel, rel);
				    	relLabel.addEventHandler(MouseEvent.MOUSE_ENTERED, new CompTooltipHandler(rel));
				    	relTypeLabel.addEventHandler(MouseEvent.MOUSE_ENTERED, new CompTooltipHandler(rel));
				    	relLabel.addEventHandler(MouseEvent.MOUSE_EXITED, new CompTooltipExitHandler(rel, ComponentType.RELATIONSHIP));
				    	relTypeLabel.addEventHandler(MouseEvent.MOUSE_EXITED, new CompTooltipExitHandler(rel, ComponentType.RELATIONSHIP));
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
			l.setBorder(new Border(new BorderStroke(Color.RED, null, null, new BorderWidths(2))));
		}
		
		return l;
	}

	public void handleOnKeyReleased(KeyEvent event)
	{
		if (event.getCode() == KeyCode.CONTROL)
		{
			controlKeyPressed = false;
		}
	}
	
	public void handleOnKeyPressed(KeyEvent event)
	{
		if (event.getCode() == KeyCode.CONTROL)
		{
			controlKeyPressed = true;
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
		
		if (simpleConceptPane.getScene() != null) {
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
	}
	private void addDescTooltip(Label node, DescriptionVersionBI desc) {
		final Tooltip tp = new Tooltip();
        tp.setFont(new Font(16));
        
        String txt = createDescTooltipText(desc);
		tp.setText(txt);
		node.setTooltip(tp);
	}

	private String createDescTooltipText(DescriptionVersionBI desc) {
		String lang = desc.getLang();
		String text = desc.getText();
		String type = WBUtility.getConPrefTerm(desc.getTypeNid());
		String caseSig = desc.isInitialCaseSignificant() ? "Is Case Significant" : "Not Case Significant";
		
		String status = WBUtility.getStatusString(desc);
		String time =  WBUtility.getTimeString(desc);
		String author = WBUtility.getAuthorString(desc); 
		String module = WBUtility.getModuleString(desc);
		String path = WBUtility.getPathString(desc);

		return "Term: " + text + "\nType: " + type + " Case Significant: " + caseSig + " Language: " + lang + " \nStatus" + status + " Time: " + time + " Author: " + author + " Module: " + module + " Path: " + path;
	}

	private void addRelTooltip(Label node, RelationshipVersionBI rel) {
		final Tooltip tp = new Tooltip();
        tp.setFont(new Font(16));
        
        String txt = createRelTooltipText(rel);
		tp.setText(txt);
		node.setTooltip(tp);
	}

	private String createRelTooltipText(RelationshipVersionBI rel) {

        
        String refinCharType = "";
		try {
			refinCharType = RelationshipType.getRelationshipType(rel.getRefinabilityNid(), rel.getCharacteristicNid()).toString();
		} catch (NullPointerException npe) {
			LOG.error("RelationshipType.getRelationshipType() doesn't handle AdditionalRelationship.  Tracker created");
			refinCharType = "AdditionalRelationshipType not handled properly";
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String group = Integer.toString(rel.getGroup());
		String type = WBUtility.getConPrefTerm(rel.getTypeNid());
		String target = WBUtility.getConPrefTerm(rel.getDestinationNid());
		String statInf = rel.isInferred() ? "False" : "True";

		String status = WBUtility.getStatusString(rel);
		String time =  WBUtility.getTimeString(rel);
		String author = WBUtility.getAuthorString(rel); 
		String module = WBUtility.getModuleString(rel);
		String path = WBUtility.getPathString(rel);

		return "Destination: " + target + " Type: " + type + "\nStated: " + statInf + " Relationship Type: " + refinCharType + " Role Group: " + group+ "\nStatus: " + status + " Time: " + time + " Author: " + author + " Module: " + module + " Path: " + path;
	}

	public String getTitle() {
		return "Simple Concept View";
	}
}
