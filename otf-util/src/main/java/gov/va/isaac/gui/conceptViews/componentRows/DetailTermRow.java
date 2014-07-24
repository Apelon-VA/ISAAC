package gov.va.isaac.gui.conceptViews.componentRows;

import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerHelper.ComponentType;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerLabelHelper;
import gov.va.isaac.util.WBUtility;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.shape.Rectangle;

import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;

public class DetailTermRow extends TermRow {

	public DetailTermRow(ConceptViewerLabelHelper labelHelper) {
		super(labelHelper);
	}

	@Override
	public void addTermRow(DescriptionVersionBI desc) {
		Rectangle rec = createAnnotRectangle(desc);
		Label descLabel = labelHelper.createComponentLabel(desc, desc.getText(), ComponentType.DESCRIPTION, false);
		Label descTypeLabel = labelHelper.createComponentLabel(desc, WBUtility.getConPrefTerm(desc.getTypeNid()), ComponentType.DESCRIPTION, desc.getTypeNid(), true);
		Label descCaseLabel = labelHelper.createComponentLabel(desc, getBooleanValue(desc.isInitialCaseSignificant()), ComponentType.DESCRIPTION, false);
		Label descLangLabel = labelHelper.createComponentLabel(desc, desc.getLang(), ComponentType.DESCRIPTION, false);

		//setConstraints(Node child, int columnIndex, int rowIndex, int columnspan, int rowspan, HPos halignment, 
		//				 VPos valignment, Priority hgrow, Priority vgrow, Insets margin)
		gp.setConstraints(rec,  0,  0,  1,  1,  HPos.CENTER,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);
		gp.setConstraints(descLabel,  1,  0,  1,  1,  HPos.LEFT,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);
		gp.setConstraints(descTypeLabel,  2,  0,  1,  1,  HPos.CENTER,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);
		gp.setConstraints(descCaseLabel,  3,  0,  1,  1,  HPos.CENTER,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);
		gp.setConstraints(descLangLabel,  4,  0,  1,  1,  HPos.CENTER,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);
		
		gp.setMargin(descLabel, new Insets(0, 20, 0, 20));
		gp.setMargin(descTypeLabel, new Insets(0, 20, 0, 20));
		gp.setMargin(descCaseLabel, new Insets(0, 20, 0, 20));
		gp.setMargin(descLangLabel, new Insets(0, 0, 0, 20));

		gp.addRow(counter++, rec, descLabel, descTypeLabel, descCaseLabel, descLangLabel);
	}

	@Override
	public void createGridPane() {
		gp = new GridPane();
		gp.setHgap(3);
		
		//ColumnConstraints(double minWidth, double prefWidth, double maxWidth, Priority hgrow, HPos halignment, boolean fillWidth) 
/*		
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
