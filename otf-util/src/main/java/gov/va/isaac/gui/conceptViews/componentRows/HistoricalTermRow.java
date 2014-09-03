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

import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;

public class HistoricalTermRow extends TermRow {

	public HistoricalTermRow(ConceptViewerLabelHelper labelHelper) {
		super(labelHelper);
	}

	@Override
	public void addTermRow(DescriptionVersionBI<?> desc, boolean isPrefTerm) {
		GridPane termGP = new GridPane();
		termGP.setHgap(3);
		
		Rectangle rec = createAnnotRectangle(desc);
		
		int termCounter = 0;
		Collection<? extends DescriptionVersionBI> versions = desc.getVersions();
		for (DescriptionVersionBI<?> dv : versions) {
			Label descLabel = labelHelper.createLabel(dv, dv.getText(), ComponentType.DESCRIPTION, 0);
			Label descTypeLabel = null;

			if (isPrefTerm) {
				descTypeLabel = labelHelper.createLabel(dv, prefTermTypeStr, ComponentType.DESCRIPTION, prefTermTypeNid);
			} else {
				descTypeLabel = labelHelper.createLabel(dv, WBUtility.getConPrefTerm(dv.getTypeNid()), ComponentType.DESCRIPTION, dv.getTypeNid());
			}
			Label descCaseLabel = labelHelper.createLabel(dv, getBooleanValue(dv.isInitialCaseSignificant()), ComponentType.DESCRIPTION, 0);
			Label descLangLabel = labelHelper.createLabel(dv, dv.getLang(), ComponentType.DESCRIPTION, 0);

			Label descStatusLabel = labelHelper.createLabel(dv, WBUtility.getStatusString(dv), ComponentType.DESCRIPTION, 0);
			Label descTimeLabel = labelHelper.createLabel(dv, WBUtility.getTimeString(dv), ComponentType.DESCRIPTION, 0);
			Label descAuthorLabel = labelHelper.createLabel(dv, WBUtility.getAuthorString(dv), ComponentType.DESCRIPTION, 0);
			Label descPathLabel = labelHelper.createLabel(dv, WBUtility.getPathString(dv), ComponentType.DESCRIPTION, 0);
			
			if (desc.isUncommitted()) {
				descLabel.setUnderline(true);
				descTypeLabel.setUnderline(true);
				descCaseLabel.setUnderline(true);
				descLangLabel.setUnderline(true);
			}

			//setConstraints(Node child, int columnIndex, int rowIndex, int columnspan, int rowspan, HPos halignment, 
			//				 VPos valignment, Priority hgrow, Priority vgrow, Insets margin)
			GridPane.setConstraints(rec,  0,  0,  1,  1,  HPos.CENTER,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);
			GridPane.setConstraints(descLabel,  1,  0,  1,  1,  HPos.LEFT,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);
			GridPane.setConstraints(descTypeLabel,  2,  0,  1,  1,  HPos.CENTER,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);
			GridPane.setConstraints(descCaseLabel,  3,  0,  1,  1,  HPos.CENTER,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);
			GridPane.setConstraints(descLangLabel,  4,  0,  1,  1,  HPos.CENTER,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);
			GridPane.setConstraints(descStatusLabel,  1,  0,  1,  1,  HPos.LEFT,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);
			GridPane.setConstraints(descTimeLabel,  2,  0,  1,  1,  HPos.CENTER,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);
			GridPane.setConstraints(descAuthorLabel,  3,  0,  1,  1,  HPos.CENTER,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);
			GridPane.setConstraints(descPathLabel,  4,  0,  1,  1,  HPos.CENTER,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);
			
			GridPane.setMargin(descLabel, new Insets(0, 20, 0, 20));
			GridPane.setMargin(descTypeLabel, new Insets(0, 20, 0, 20));
			GridPane.setMargin(descCaseLabel, new Insets(0, 20, 0, 20));
			GridPane.setMargin(descLangLabel, new Insets(0, 0, 0, 20));
			GridPane.setMargin(descStatusLabel, new Insets(0, 20, 0, 20));
			GridPane.setMargin(descTimeLabel, new Insets(0, 20, 0, 20));
			GridPane.setMargin(descAuthorLabel, new Insets(0, 20, 0, 20));
			GridPane.setMargin(descPathLabel, new Insets(0, 0, 0, 20));

			termGP.addRow(termCounter++, rec, descLabel, descTypeLabel, descCaseLabel, descLangLabel, descStatusLabel, descTimeLabel, descAuthorLabel, descPathLabel);
		}
		
		gp.addRow(counter++, termGP);
	}

	@Override
	public void createGridPane() {
		gp = new GridPane();
		gp.setHgap(3);
		gp.setGridLinesVisible(true);
		/*
		//ColumnConstraints(double minWidth, double prefWidth, double maxWidth, Priority hgrow, HPos halignment, boolean fillWidth) 
 		ColumnConstraints column1 = new ColumnConstraints(Control.USE_COMPUTED_SIZE);

		ColumnConstraints column2 = new ColumnConstraints(Control.USE_COMPUTED_SIZE);
		column2.setFillWidth(true);
		
		ColumnConstraints column3 = new ColumnConstraints(Control.USE_COMPUTED_SIZE);
		column3.setFillWidth(true);
	    
		ColumnConstraints column4 = new ColumnConstraints(Control.USE_COMPUTED_SIZE);
		column3.setFillWidth(false);

		ColumnConstraints column5 = new ColumnConstraints(Control.USE_COMPUTED_SIZE);
		column3.setFillWidth(false);

		gp.getColumnConstraints().addAll(column1, column2, column3, column4, column5); // first column gets any extra width
		*/		
	}
}
