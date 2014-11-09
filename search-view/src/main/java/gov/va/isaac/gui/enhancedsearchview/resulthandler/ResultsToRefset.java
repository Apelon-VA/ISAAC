package gov.va.isaac.gui.enhancedsearchview.resulthandler;

import gov.va.isaac.search.CompositeSearchResult;
import gov.va.isaac.util.WBUtility;

import java.beans.PropertyVetoException;
import java.io.IOException;

import javafx.scene.control.TableView;
import javafx.stage.Stage;

import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDynamicCAB;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicUsageDescription;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.RefexDynamicUsageDescriptionBuilder;

public class ResultsToRefset {
	
	// Create Refset out of results in Search
	public static String resultsToRefset(Stage owner, TableView<CompositeSearchResult> tableView) throws IOException, ContradictionException, InvalidCAB, PropertyVetoException {
		// Prompt for name/Desc/iSAnnot/parent
		RefsetDefinitionPrompt.showContentGatheringDialog(owner, "Define Refset");
		
		// Create RefsetDynamic
		if (RefsetDefinitionPrompt.getButtonSelected() == RefsetDefinitionPrompt.Response.COMMIT) {
			RefexDynamicUsageDescription refset = 
					RefexDynamicUsageDescriptionBuilder.createNewRefexDynamicUsageDescriptionConcept(RefsetDefinitionPrompt.getNameTextField().getText(), 
																									 RefsetDefinitionPrompt.getNameTextField().getText(), 
																									 RefsetDefinitionPrompt.getDescTextField().getText(),
																									 new RefexDynamicColumnInfo[] {},
																									 RefsetDefinitionPrompt.getParentConcept().getConcept().getPrimordialUuid(),
																									 RefsetDefinitionPrompt.getAnnot().isSelected());
		    // Create a dynamic refex CAB for each result
			for (CompositeSearchResult con : tableView.getItems()) {
				RefexDynamicCAB refexBlueprint = new RefexDynamicCAB(con.getContainingConcept().getNid(), refset.getRefexUsageDescriptorNid());
				RefexDynamicChronicleBI<?> refex = WBUtility.getBuilder().construct(refexBlueprint);

				
				if (RefsetDefinitionPrompt.getAnnot().isSelected()) {
					WBUtility.addUncommitted(con.getContainingConcept());
				} 
			}
			
			if (!RefsetDefinitionPrompt.getAnnot().isSelected()) {
				WBUtility.addUncommitted(refset.getRefexUsageDescriptorNid());
			}
			
			WBUtility.commit();
			
			return RefsetDefinitionPrompt.getNameTextField().getText();
		}
		
		return null;
	}
}
