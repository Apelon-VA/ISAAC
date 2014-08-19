package gov.va.isaac.gui.conceptViews.componentRows;

import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerHelper.ComponentType;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerLabelHelper;
import gov.va.isaac.util.WBUtility;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.shape.Rectangle;

import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;

public class DetailTermRow extends TermRow {

	public DetailTermRow(ConceptViewerLabelHelper labelHelper) {
		super(labelHelper);
	}

	@Override
	public void addTermRow(DescriptionVersionBI<?> desc) {
		Rectangle rec = createAnnotRectangle(desc);
		
		Label descLabel = labelHelper.createLabel(desc, desc.getText(), ComponentType.DESCRIPTION, 0);
		Label descTypeLabel = labelHelper.createLabel(desc, WBUtility.getConPrefTerm(desc.getTypeNid()), ComponentType.DESCRIPTION, desc.getTypeNid());
		Label descCaseLabel = labelHelper.createLabel(desc, getBooleanValue(desc.isInitialCaseSignificant()), ComponentType.DESCRIPTION, 0);
		Label descLangLabel = labelHelper.createLabel(desc, desc.getLang(), ComponentType.DESCRIPTION, 0);
		
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
		
		GridPane.setMargin(descLabel, new Insets(0, 20, 0, 20));
		GridPane.setMargin(descTypeLabel, new Insets(0, 20, 0, 20));
		GridPane.setMargin(descCaseLabel, new Insets(0, 20, 0, 20));
		GridPane.setMargin(descLangLabel, new Insets(0, 0, 0, 20));

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