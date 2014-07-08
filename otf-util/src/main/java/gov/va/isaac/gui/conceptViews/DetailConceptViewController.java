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

import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerHelper;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerHelper.ComponentType;
import gov.va.isaac.util.WBUtility;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
*
* @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
* @author <a href="jefron@apelon.com">Jesse Efron</a>
*/

public class DetailConceptViewController  extends BaseConceptViewController{
	@FXML private AnchorPane detailedConceptPane;
    
    // Descriptions
    @FXML private Label prefLabel;
    @FXML private Label prefTypeLabel;
    @FXML private Label prefCaseLabel;
    @FXML private Label prefLangLabel;
    
    @FXML private VBox descLabelVBox;
    @FXML private VBox descTypeVBox;
    @FXML private VBox descAnnotVBox;
    @FXML private VBox descCaseVbox;
    @FXML private VBox descLangVbox;

    // Relationships
    @FXML private VBox relAnnotVBox;
    @FXML private VBox relLabelVBox;
    @FXML private VBox relTypeVBox;
    @FXML private VBox relRefVBox;
    @FXML private VBox relCharVBox;
    
    // Destinations
    @FXML private VBox destAnnotVBox;
    @FXML private VBox destLabelVBox;
    @FXML private VBox destTypeVBox;
    @FXML private VBox destRefVBox;
    @FXML private VBox destCharVBox;

	private static final Logger LOG = LoggerFactory.getLogger(DetailConceptViewController.class);
	

	@Override
	void setConceptDetails(ConceptChronicleDdo concept) {
		con = WBUtility.getConceptVersion(concept.getPrimordialUuid());
		
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

			    		Label descCaseLabel = labelHelper.createComponentLabel(desc, getBooleanValue(desc.isInitialCaseSignificant()), ComponentType.DESCRIPTION, false);
			    		descCaseVbox.getChildren().add(descCaseLabel);

			    		Label descLangLabel = labelHelper.createComponentLabel(desc, desc.getLang(), ComponentType.DESCRIPTION, false);
			    		descLangVbox.getChildren().add(descLangLabel);
	    			}
	    		}
	    	}
    	} catch (Exception e) {
    		LOG.error("Cannot access descriptions for concept: " + con.getPrimordialUuid());
    	}
    	
    	// Source Relationships
    	try {
        	// Capture for sorting (storing is-a in different collection
        	Map<Integer, Set<RelationshipVersionBI>> sortedRels = new HashMap<>();
        	Set<RelationshipVersionBI> isaRels = new HashSet<>();
        	sortRels(sortedRels, isaRels, con.getRelationshipsOutgoingActive());

        	// Display IS-As
			addRels(isaRels, relAnnotVBox, relLabelVBox, relTypeVBox, relCharVBox, relRefVBox);

			for (Integer relType: sortedRels.keySet()) {
				// Display non-IS-As
				addRels(sortedRels.get(relType), relAnnotVBox, relLabelVBox, relTypeVBox, relCharVBox, relRefVBox);
			}
		} catch (IOException | ContradictionException e) {
    		LOG.error("Cannot access relationships for concept: " + con.getPrimordialUuid());
		}
    	
    	
    	// Destination Relationships
    	try {
        	// Capture for sorting (storing is-a in different collection
        	Map<Integer, Set<RelationshipVersionBI>> sortedRels = new HashMap<>();
        	Set<RelationshipVersionBI> isaRels = new HashSet<>();
        	
        	sortRels(sortedRels, isaRels, con.getRelationshipsIncomingActive());

        	// Display IS-As
			addRels(isaRels, destAnnotVBox, destLabelVBox, destTypeVBox, destCharVBox, destRefVBox);

			for (Integer relType: sortedRels.keySet()) {
				// Display non-IS-As
				addRels(sortedRels.get(relType), destAnnotVBox, destLabelVBox, destTypeVBox, destCharVBox, destRefVBox);
			}
		} catch (IOException | ContradictionException e) {
    		LOG.error("Cannot access destinations for concept: " + con.getPrimordialUuid());
		}
    }

	
	
	
	
	
	
	private void sortRels(Map<Integer, Set<RelationshipVersionBI>> sortedRels,
						  Set<RelationshipVersionBI> isaRels,
						  Collection<? extends RelationshipVersionBI> relsToSort) throws ValidationException, IOException 
	{			
		for (RelationshipVersionBI rel : relsToSort) {
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
	}

	private void addRels(Set<RelationshipVersionBI> rels, VBox annotVbox, VBox labelVBox, VBox typeVBox, VBox charVBox, VBox refVBox) {
		for (RelationshipVersionBI rel: rels) {
			if (!rel.isInferred()) {
				createAnnotRectangle(annotVbox, rel);

				Label relLabel = labelHelper.createComponentLabel(rel, WBUtility.getConPrefTerm(rel.getDestinationNid()), ComponentType.RELATIONSHIP, rel.getDestinationNid(), true);
	    		labelVBox.getChildren().add(relLabel);

	    		Label relTypeLabel = labelHelper.createComponentLabel(rel, WBUtility.getConPrefTerm(rel.getTypeNid()), ComponentType.RELATIONSHIP, rel.getTypeNid(), true);
	    		typeVBox.getChildren().add(relTypeLabel);
	    		
	    		Label relCharLabel = labelHelper.createComponentLabel(rel, WBUtility.getConPrefTerm(rel.getCharacteristicNid()), ComponentType.RELATIONSHIP, rel.getCharacteristicNid(), true);
	    		charVBox.getChildren().add(relCharLabel);

	    		Label relRefLabel = labelHelper.createComponentLabel(rel, WBUtility.getConPrefTerm(rel.getRefinabilityNid()), ComponentType.RELATIONSHIP, rel.getRefinabilityNid(), true);
	    		refVBox.getChildren().add(relRefLabel);
    		}
		}
	}
}
