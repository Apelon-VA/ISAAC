package gov.va.isaac.gui.conceptViews;

import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerHelper;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerHelper.ComponentType;
import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleConceptViewController extends BaseConceptViewController {

	// Descriptions
	@FXML private Label prefLabel;
	@FXML private Label prefTypeLabel;
	@FXML private VBox descLabelVBox;
	@FXML private VBox descTypeVBox;
	@FXML private VBox descAnnotVBox;
	
	// Relationships
	@FXML private VBox relLabelVBox;
	@FXML private VBox relTypeVBox;
	@FXML private VBox relAnnotVBox;
	
	private static final Logger LOG = LoggerFactory.getLogger(SimpleConceptViewController.class);
	
	@FXML
    void initialize() {
        assert releaseIdLabel != null : "fx:id=\"releaseIdLabel\" was not injected: check your FXML file 'SimpleView.fxml'.";
        assert descAnnotVBox != null : "fx:id=\"descAnnotVBox\" was not injected: check your FXML file 'SimpleView.fxml'.";
        assert previousButton != null : "fx:id=\"previousButton\" was not injected: check your FXML file 'SimpleView.fxml'.";
        assert historicalRadio != null : "fx:id=\"historicalRadio\" was not injected: check your FXML file 'SimpleView.fxml'.";
        assert taxonomyButton != null : "fx:id=\"taxonomyButton\" was not injected: check your FXML file 'SimpleView.fxml'.";
        assert fsnLabel != null : "fx:id=\"fsnLabel\" was not injected: check your FXML file 'SimpleView.fxml'.";
        assert prefLabel != null : "fx:id=\"prefLabel\" was not injected: check your FXML file 'SimpleView.fxml'.";
        assert prefTypeLabel != null : "fx:id=\"prefTypeLabel\" was not injected: check your FXML file 'SimpleView.fxml'.";
        assert basicRadio != null : "fx:id=\"basicRadio\" was not injected: check your FXML file 'SimpleView.fxml'.";
        assert fsnAnnotVBox != null : "fx:id=\"fsnAnnotVBox\" was not injected: check your FXML file 'SimpleView.fxml'.";
        assert viewGroup != null : "fx:id=\"viewGroup\" was not injected: check your FXML file 'SimpleView.fxml'.";
        assert relTypeVBox != null : "fx:id=\"relTypeVBox\" was not injected: check your FXML file 'SimpleView.fxml'.";
        assert modifyButton != null : "fx:id=\"modifyButton\" was not injected: check your FXML file 'SimpleView.fxml'.";
        assert closeButton != null : "fx:id=\"closeButton\" was not injected: check your FXML file 'SimpleView.fxml'.";
        assert conAnnotVBox != null : "fx:id=\"conAnnotVBox\" was not injected: check your FXML file 'SimpleView.fxml'.";
        assert relAnnotVBox != null : "fx:id=\"relAnnotVBox\" was not injected: check your FXML file 'SimpleView.fxml'.";
        assert descLabelVBox != null : "fx:id=\"descLabelVBox\" was not injected: check your FXML file 'SimpleView.fxml'.";
        assert parentPane != null : "fx:id=\"parentPane\" was not injected: check your FXML file 'SimpleView.fxml'.";
        assert isPrimLabel != null : "fx:id=\"isPrimLabel\" was not injected: check your FXML file 'SimpleView.fxml'.";
        assert relLabelVBox != null : "fx:id=\"relLabelVBox\" was not injected: check your FXML file 'SimpleView.fxml'.";
        assert descTypeVBox != null : "fx:id=\"descTypeVBox\" was not injected: check your FXML file 'SimpleView.fxml'.";
        assert detailedRadio != null : "fx:id=\"detailedRadio\" was not injected: check your FXML file 'SimpleView.fxml'.";
    }
	
	@Override
	void setConceptInfo(UUID currentCon) {	
		con = WBUtility.getConceptVersion(currentCon);
		
		try {
			// FSN
			labelHelper.initializeLabel(fsnLabel, con.getFullySpecifiedDescription(), ComponentType.DESCRIPTION, con.getFullySpecifiedDescription().getText(), false);
			createAnnotRectangle(fsnAnnotVBox, con.getFullySpecifiedDescription());

			// PT 
			labelHelper.initializeLabel(prefLabel, con.getPreferredDescription(), ComponentType.DESCRIPTION, con.getPreferredDescription().getText(), false);
			labelHelper.initializeLabel(prefTypeLabel, con.getPreferredDescription(), ComponentType.DESCRIPTION, prefTypeLabel.getText(), SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid(), true);
			createAnnotRectangle(descAnnotVBox, con.getPreferredDescription());

			ConceptAttributeVersionBI attr = ConceptViewerHelper.getConceptAttributes(con);
		
			// SCT Id
			labelHelper.initializeLabel(releaseIdLabel, attr, ComponentType.CONCEPT, ConceptViewerHelper.getSctId(attr), false);
			labelHelper.createIdsContextMenu(releaseIdLabel, con.getNid());
			createAnnotRectangle(conAnnotVBox, con);

			// Defined Status
			labelHelper.initializeLabel(isPrimLabel, attr, ComponentType.CONCEPT, ConceptViewerHelper.getPrimDef(attr), ConceptViewerHelper.getPrimDefNid(attr), true);
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
}
