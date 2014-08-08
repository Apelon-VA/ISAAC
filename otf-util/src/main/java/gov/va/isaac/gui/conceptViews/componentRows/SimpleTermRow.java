package gov.va.isaac.gui.conceptViews.componentRows;

import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerHelper.ComponentType;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerLabelHelper;
import gov.va.isaac.util.WBUtility;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;

import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;

public class SimpleTermRow extends TermRow  {
	public SimpleTermRow(ConceptViewerLabelHelper labelHelper) {
		super(labelHelper);
	}
	
	@Override
	public void addTermRow(DescriptionVersionBI desc) {
		Rectangle rec = createAnnotRectangle(desc);
		Label descLabel = labelHelper.createLabel(desc, desc.getText(), ComponentType.DESCRIPTION, 0);
		Label descTypeLabel = labelHelper.createLabel(desc, WBUtility.getConPrefTerm(desc.getTypeNid()), ComponentType.DESCRIPTION, desc.getTypeNid());
		
		if (desc.isUncommitted()) {
			descLabel.setUnderline(true);
			descTypeLabel.setUnderline(true);
		}

		
		//setConstraints(Node child, int columnIndex, int rowIndex, int columnspan, int rowspan, HPos halignment, 
		//				 VPos valignment, Priority hgrow, Priority vgrow, Insets margin)
		gp.setConstraints(rec,  0,  0,  1,  1,  HPos.CENTER,  VPos.CENTER);
		gp.setConstraints(descLabel,  1,  0,  1,  1,  HPos.LEFT,  VPos.CENTER);
		gp.setConstraints(descTypeLabel,  2,  0,  1,  1,  HPos.RIGHT,  VPos.CENTER);
		
		gp.setMargin(descLabel, new Insets(0, 20, 0, 0));
		gp.setMargin(descTypeLabel, new Insets(0, 0, 0, 20));
		gp.addRow(counter++, rec, descLabel, descTypeLabel);
	}

	@Override
	public void createGridPane() {
		gp = new GridPane();
		gp.setHgap(3);
		
//		ColumnConstraints(double minWidth, double prefWidth, double maxWidth, Priority hgrow, HPos halignment, boolean fillWidth) 
/*
 		ColumnConstraints column1 = new ColumnConstraints(Control.USE_COMPUTED_SIZE);
		ColumnConstraints column2 = new ColumnConstraints(10, 394, 404);
		ColumnConstraints column2 = new ColumnConstraints(Control.USE_COMPUTED_SIZE);
		column2.setFillWidth(true);
		
		ColumnConstraints column3 = new ColumnConstraints(10, 94, 120);
		ColumnConstraints column3 = new ColumnConstraints(Control.USE_COMPUTED_SIZE);
		column3.setFillWidth(true);
	    
		gp.getColumnConstraints().addAll(column1);//, column2, column3 ); // first column gets any extra width		
*/
	}
}
