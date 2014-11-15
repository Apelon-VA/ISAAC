package gov.va.isaac.gui.conceptViews.componentRows;

import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerHelper.ComponentType;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerLabelHelper;
import gov.va.isaac.util.WBUtility;

import java.util.Collection;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;

public class HistoricalRelRow extends RelRow {
	
	public HistoricalRelRow(ConceptViewerLabelHelper labelHelper) {
		super(labelHelper);
	}

	@Override
	public void addRelRow(RelationshipVersionBI<?> rel) {
		GridPane relGP = new GridPane();
		relGP.setHgap(3);

		int termCounter = 0;
		Collection<? extends RelationshipVersionBI> versions = rel.getVersions();
		for (RelationshipVersionBI<?> rv : versions) {

			Rectangle rec = createAnnotRectangle(rv);
			Label relLabel;
			if (isDetailed) {
				relLabel = labelHelper.createLabel(rv, WBUtility.getConPrefTerm(rv.getConceptNid()), ComponentType.RELATIONSHIP, rv.getConceptNid());
			} else {
				relLabel = labelHelper.createLabel(rv, WBUtility.getConPrefTerm(rv.getDestinationNid()), ComponentType.RELATIONSHIP, rv.getDestinationNid());
			}
			Label relTypeLabel = labelHelper.createLabel(rv, WBUtility.getConPrefTerm(rv.getTypeNid()), ComponentType.RELATIONSHIP, rv.getTypeNid());
			Label relGroupLabel = labelHelper.createLabel(rv, String.valueOf(rv.getGroup()), ComponentType.RELATIONSHIP, 0);
			Label relCharLabel = labelHelper.createLabel(rv, WBUtility.getConPrefTerm(rv.getCharacteristicNid()), ComponentType.RELATIONSHIP, rv.getCharacteristicNid());
			Label relRefLabel = labelHelper.createLabel(rv, WBUtility.getConPrefTerm(rv.getRefinabilityNid()), ComponentType.RELATIONSHIP, rv.getRefinabilityNid());
			
			Label relStatusLabel = labelHelper.createLabel(rv, WBUtility.getStatusString(rv), ComponentType.RELATIONSHIP, 0);
			Label relTimeLabel = labelHelper.createLabel(rv, WBUtility.getTimeString(rv), ComponentType.RELATIONSHIP, rv.getTypeNid());
			Label relAuthorLabel = labelHelper.createLabel(rv, WBUtility.getAuthorString(rv), ComponentType.RELATIONSHIP, 0);
			Label relPathLabel = labelHelper.createLabel(rv, WBUtility.getPathString(rv), ComponentType.RELATIONSHIP, 0);

			if (rel.isUncommitted()) {
				if (rel.getVersions().size() == 1) {
					Font f = relLabel.getFont();
					relLabel.setFont(Font.font(f.getFamily(), FontPosture.ITALIC, f.getSize()));

					f = relGroupLabel.getFont();
					relGroupLabel.setFont(Font.font(f.getFamily(), FontPosture.ITALIC, f.getSize()));

					f = relRefLabel.getFont();
					relRefLabel.setFont(Font.font(f.getFamily(), FontPosture.ITALIC, f.getSize()));

					f = relCharLabel.getFont();
					relCharLabel.setFont(Font.font(f.getFamily(), FontPosture.ITALIC, f.getSize()));

					f = relTypeLabel.getFont();
					relTypeLabel.setFont(Font.font(f.getFamily(), FontPosture.ITALIC, f.getSize()));
				} else {
					ComponentChronicleBI<?> chronicle = rel.getChronicle();
					RelationshipVersionBI<?> origVersion = (RelationshipVersionBI<?>) WBUtility.getLastCommittedVersion(chronicle);
	
					if (!relLabel.getText().equals(WBUtility.getConPrefTerm(origVersion.getDestinationNid()))) {
						relLabel.setUnderline(true);
					}
					
					if (!relTypeLabel.getText().equals(WBUtility.getConPrefTerm(origVersion.getTypeNid()))) {
						relTypeLabel.setUnderline(true);
					}
		
					if (!relCharLabel.getText().equals(WBUtility.getConPrefTerm(origVersion.getCharacteristicNid()))) {
						relCharLabel.setUnderline(true);
					}
					
					if (!relRefLabel.getText().equals(WBUtility.getConPrefTerm(origVersion.getRefinabilityNid()))) {
						relRefLabel.setUnderline(true);
					}
					
					if (!relGroupLabel.getText().equals(origVersion.getGroup())) {
						relGroupLabel.setUnderline(true);
					}
				}
			}
				
			//setConstraints(Node child, int columnIndex, int rowIndex, int columnspan, int rowspan, HPos halignment, 
			//				 VPos valignment, Priority hgrow, Priority vgrow, Insets margin)
			GridPane.setConstraints(rec,  0,  0,  1,  1,  HPos.CENTER,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);
			GridPane.setConstraints(relLabel,  1,  0,  1,  1,  HPos.LEFT,  VPos.CENTER, Priority.SOMETIMES, Priority.ALWAYS);
			GridPane.setConstraints(relTypeLabel,  3,  0,  1,  1,  HPos.CENTER,  VPos.CENTER, Priority.SOMETIMES, Priority.ALWAYS);
			GridPane.setConstraints(relGroupLabel,  4,  0,  1,  1,  HPos.CENTER,  VPos.CENTER, Priority.SOMETIMES, Priority.ALWAYS);
			GridPane.setConstraints(relCharLabel,  5,  0,  1,  1,  HPos.CENTER,  VPos.CENTER, Priority.SOMETIMES, Priority.ALWAYS);
			GridPane.setConstraints(relRefLabel,  6,  0,  1,  1,  HPos.CENTER,  VPos.CENTER, Priority.SOMETIMES, Priority.ALWAYS);
			GridPane.setConstraints(relStatusLabel,  1,  0,  1,  1,  HPos.LEFT,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);
			GridPane.setConstraints(relTimeLabel,  2,  0,  1,  1,  HPos.CENTER,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);
			GridPane.setConstraints(relAuthorLabel,  3,  0,  1,  1,  HPos.CENTER,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);
			GridPane.setConstraints(relPathLabel,  4,  0,  1,  1,  HPos.CENTER,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);

			GridPane.setMargin(relLabel, new Insets(0, 20, 0, 20));
			GridPane.setMargin(relTypeLabel, new Insets(0, 20, 0, 20));
			GridPane.setMargin(relGroupLabel, new Insets(0, 20, 0, 20));
			GridPane.setMargin(relCharLabel, new Insets(0, 20, 0, 20));
			GridPane.setMargin(relRefLabel, new Insets(0, 0, 0, 20));
			GridPane.setMargin(relStatusLabel, new Insets(0, 0, 0, 20));
			GridPane.setMargin(relTimeLabel, new Insets(0, 0, 0, 20));
			GridPane.setMargin(relAuthorLabel, new Insets(0, 0, 0, 20));
			GridPane.setMargin(relPathLabel, new Insets(0, 0, 0, 20));
	
			relGP.addRow(termCounter++, rec, relLabel, relTypeLabel, relGroupLabel, relCharLabel, relRefLabel, relStatusLabel, relTimeLabel, relAuthorLabel, relPathLabel);
		}
		
		currentPane.addRow(counter++, relGP);
	}

	@Override
	public void createGridPane() {
		gp = new GridPane();
		gp.setHgap(3);
		gp.setGridLinesVisible(true);
		currentPane = gp;
		
		dgp = new GridPane();
		dgp.setHgap(3);
		dgp.setGridLinesVisible(true);
		
		//ColumnConstraints(double minWidth, double prefWidth, double maxWidth, Priority hgrow, HPos halignment, boolean fillWidth) 
/*		ColumnConstraints column1 = new ColumnConstraints(10);

		ColumnConstraints column2 = new ColumnConstraints(10, 394, 404);
		column2.setFillWidth(true);
		
		ColumnConstraints column3 = new ColumnConstraints(10, 94, 120);
		column3.setFillWidth(true);
	    
		gp.getColumnConstraints().addAll(column1, column2, column3 ); // first column gets any extra width		
*/	
	}
}
