package gov.va.isaac.gui.conceptViews.helpers;

import gov.va.isaac.gui.conceptViews.componentRows.DetailRelRow;
import gov.va.isaac.gui.conceptViews.componentRows.DetailTermRow;
import gov.va.isaac.gui.conceptViews.componentRows.RelRow;
import gov.va.isaac.gui.conceptViews.componentRows.SimpleRelRow;
import gov.va.isaac.gui.conceptViews.componentRows.SimpleTermRow;
import gov.va.isaac.gui.conceptViews.componentRows.TermRow;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerHelper.ComponentType;
import gov.va.isaac.interfaces.gui.views.ConceptViewMode;
import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import javax.validation.ValidationException;

import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnhancedConceptBuilder {
	
	private VBox termVBox;
	private VBox relVBox;
	private VBox destVBox;
	private ScrollPane destScrollPane;
	private VBox fsnAnnotVBox;
	private VBox conAnnotVBox;
	private ConceptVersionBI con;
	private Label fsnLabel;
	private Label releaseIdLabel;
	private Label isPrimLabel;

	private TermRow tr;
	private RelRow rr;
	private ConceptViewerLabelHelper labelHelper;

	private static final Logger LOG = LoggerFactory.getLogger(EnhancedConceptBuilder.class);

	public EnhancedConceptBuilder (VBox termVBox, VBox relVBox, VBox destVBox, ScrollPane destScrollPane, VBox fsnAnnotVBox, VBox conAnnotVBox, Label fsnLabel, Label releaseIdLabel, Label isPrimLabel) {
		this.termVBox = termVBox;
		this.relVBox = relVBox;
		this.destVBox = destVBox;
		this.destScrollPane = destScrollPane;
		
		this.fsnAnnotVBox = fsnAnnotVBox;
		this.conAnnotVBox = conAnnotVBox;
		this.fsnLabel = fsnLabel;
		this.releaseIdLabel = releaseIdLabel;
		this.isPrimLabel = isPrimLabel;
	}
	
	public void setConceptValues(UUID currentCon, ConceptViewMode mode) {
		setMode(mode);
		con = WBUtility.getConceptVersion(currentCon);
		
		executeConceptBuilder();
		executeTermBuilder();

		try {
			rr.createGridPane();
			executeRelBuilder(con.getRelationshipsOutgoingActive());
			relVBox.getChildren().add(rr.getGridPane());

			if (mode != ConceptViewMode.SIMPLE_VIEW) {
				rr.resetCounter();
				executeRelBuilder(con.getRelationshipsIncomingActive());
				GridPane gp = ((DetailRelRow)rr).getDestinationGridPane();

				if (!gp.getChildren().isEmpty()) {
					destVBox.getChildren().add(gp);
					destVBox.setVisible(true);
					destScrollPane.setVisible(true);
				}
			} else {
				destVBox.setVisible(false);
				destScrollPane.setVisible(false);
			}
		} catch (IOException | ContradictionException e) {
			LOG.error("Cannot access relationships for concept: " + con.getPrimordialUuid());
		}
	}
	

	private void executeConceptBuilder() {
		try {
			// FSN
			labelHelper.initializeLabel(fsnLabel, con.getFullySpecifiedDescription(), ComponentType.DESCRIPTION, con.getFullySpecifiedDescription().getText(), false);
			Rectangle fsnRec = AnnotationRectangle.create(con.getFullySpecifiedDescription());
			fsnAnnotVBox.getChildren().add(fsnRec);
			
			ConceptAttributeVersionBI attr = ConceptViewerHelper.getConceptAttributes(con);
		
			// SCT Id
			labelHelper.initializeLabel(releaseIdLabel, attr, ComponentType.CONCEPT, ConceptViewerHelper.getSctId(attr), false);
			labelHelper.createIdsContextMenu(releaseIdLabel, con.getNid());
			Rectangle conRec = AnnotationRectangle.create(con);
			conAnnotVBox.getChildren().add(conRec);

			// Defined Status
			labelHelper.initializeLabel(isPrimLabel, attr, ComponentType.CONCEPT, ConceptViewerHelper.getPrimDef(attr), ConceptViewerHelper.getPrimDefNid(attr), true);
		} catch (Exception e) {
			LOG.error("Cannot access basic attributes for concept: " + con.getPrimordialUuid());
		}

	}

	private void executeRelBuilder(Collection<? extends RelationshipVersionBI> rels) throws ValidationException, IOException {
		// Capture for sorting (storing is-a in different collection
		Map<Integer, Set<RelationshipVersionBI>> sortedRels = new HashMap<>();
		Set<RelationshipVersionBI> isaRels = new HashSet<>();
		sortRels(sortedRels, isaRels, rels);

		// Display IS-As
		addRels(isaRels);

		for (Integer relType: sortedRels.keySet()) {
			// Display non-IS-As
			addRels(sortedRels.get(relType));
		}
	}

	private void executeTermBuilder() {
		// Descriptions
		try {
			// Sort Descriptions filtering out FSN and special storage for PT
			Map<Integer, Set<DescriptionVersionBI>> sortedDescs = new HashMap<>();
			DescriptionVersionBI ptDesc = null;
			
			for (DescriptionVersionBI desc : con.getDescriptionsActive()) {
				if (desc.getNid() != con.getFullySpecifiedDescription().getNid()) {
					if (desc.getNid() == con.getPreferredDescription().getNid()) {
						ptDesc = desc;
					} else {
						if (!sortedDescs.containsKey(desc.getTypeNid())) {
							Set<DescriptionVersionBI> descs = new HashSet<>();
							sortedDescs.put(desc.getTypeNid(), descs);
						}
	
						sortedDescs.get(desc.getTypeNid()).add(desc);
					}
				}
			}
		   	
			// Create GridPane
			tr.createGridPane();

			// Add PT Row to GridPane
			tr.addTermRow(ptDesc);

			// Add other terms to GridPane
			for (Integer descType: sortedDescs.keySet()) {
				for (DescriptionVersionBI desc: sortedDescs.get(descType)) {
					if (desc.getNid() != con.getPreferredDescription().getNid()) {
						tr.addTermRow(desc);
					}
				}
			}
			
			// Add GridPane to VBox
			termVBox.getChildren().add(tr.getGridPane());
		} catch (Exception e) {
			LOG.error("Cannot access descriptions for concept: " + con.getPrimordialUuid());
		}		
	}

	private void setMode(ConceptViewMode mode) {
		if (mode == ConceptViewMode.SIMPLE_VIEW) {
			tr = new SimpleTermRow(labelHelper);
			rr = new SimpleRelRow(labelHelper);
		} else if (mode == ConceptViewMode.DETAIL_VIEW) {
			tr = new DetailTermRow(labelHelper);
			rr = new DetailRelRow(labelHelper);
		}
		
	}

	private void addRels(Set<RelationshipVersionBI> rels) {
		for (RelationshipVersionBI rel: rels) {
			rr.addRelRow(rel);
		}
	}

	private void sortRels(Map<Integer, Set<RelationshipVersionBI>> sortedRels, Set<RelationshipVersionBI> isaRels,
			Collection<? extends RelationshipVersionBI> relsToSort) throws ValidationException, IOException {
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

	public void setLabelHelper(ConceptViewerLabelHelper labelHelper) {
			this.labelHelper = labelHelper;
	}

}
