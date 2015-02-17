package gov.va.isaac.gui.conceptViews.componentRows;

import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerLabelHelper;
import gov.va.isaac.util.OTFUtility;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import org.ihtsdo.otf.tcc.api.metadata.ComponentType;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;

public class SimpleRelRow extends RelRow {

	public SimpleRelRow(ConceptViewerLabelHelper labelHelper) {
		super(labelHelper);
	}

	@Override
	public void addRelRow(RelationshipVersionBI<?> rel) {
		Rectangle rec = createAnnotRectangle(rel);
		Label relLabel = labelHelper.createLabel(rel, OTFUtility.getConPrefTerm(rel.getDestinationNid()), ComponentType.RELATIONSHIP, rel.getDestinationNid());
		Label relTypeLabel = labelHelper.createLabel(rel, OTFUtility.getConPrefTerm(rel.getTypeNid()), ComponentType.RELATIONSHIP, rel.getTypeNid());
		
		if (rel.isUncommitted()) {
			if (rel.getVersions().size() == 1) {
				Font f = relLabel.getFont();
				relLabel.setFont(Font.font(f.getFamily(), FontPosture.ITALIC, f.getSize()));

				f = relTypeLabel.getFont();
				relTypeLabel.setFont(Font.font(f.getFamily(), FontPosture.ITALIC, f.getSize()));
			} else {
				relLabel.setUnderline(true);
				relTypeLabel.setUnderline(true);
			}
		}
		
		//setConstraints(Node child, int columnIndex, int rowIndex, int columnspan, int rowspan, HPos halignment, 
		//				 VPos valignment, Priority hgrow, Priority vgrow, Insets margin)
		GridPane.setConstraints(rec,  0,  0,  1,  1,  HPos.CENTER,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);
		GridPane.setConstraints(relLabel,  1,  0,  1,  1,  HPos.LEFT,  VPos.CENTER, Priority.SOMETIMES, Priority.ALWAYS);
		GridPane.setConstraints(relTypeLabel,  2,  0,  1,  1,  HPos.RIGHT,  VPos.CENTER, Priority.SOMETIMES, Priority.ALWAYS);
		
		GridPane.setMargin(relLabel, new Insets(0, 20, 0, 0));
		GridPane.setMargin(relTypeLabel, new Insets(0, 0, 0, 20));

		gp.addRow(counter++, rec, relLabel, relTypeLabel);
	}

	@Override
	public void createGridPane() {
		gp = new GridPane();
		gp.setHgap(3);
		
		//ColumnConstraints(double minWidth, double prefWidth, double maxWidth, Priority hgrow, HPos halignment, boolean fillWidth) 
/*		ColumnConstraints column1 = new ColumnConstraints(10);

		ColumnConstraints column2 = new ColumnConstraints(10, 394, 404);
		column2.setFillWidth(true);
		
		ColumnConstraints column3 = new ColumnConstraints(10, 94, 120);
		column3.setFillWidth(true);
	    
		gp.getColumnConstraints().addAll(column1);//, column2, column3 ); // first column gets any extra width		
*/
	}
}
