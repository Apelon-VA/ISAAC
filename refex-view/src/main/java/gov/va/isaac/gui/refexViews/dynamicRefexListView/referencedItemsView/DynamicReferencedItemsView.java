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
package gov.va.isaac.gui.refexViews.dynamicRefexListView.referencedItemsView;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.gui.refexViews.refexEdit.DynamicRefexView;
import gov.va.isaac.interfaces.gui.views.PopupViewI;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;

/**
 * {@link DynamicReferencedItemsView}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class DynamicReferencedItemsView implements PopupViewI
{
	private SimpleDisplayConcept assemblageConcept_;
	ConceptVersionBI assemblageConceptFull_;
	private BorderPane root_;
	private DynamicRefexView drv_;

	public DynamicReferencedItemsView(SimpleDisplayConcept assemblageConcept)
	{
		assemblageConcept_ = assemblageConcept;
		root_ = new BorderPane();
		
		Label title = new Label("Dynamic Refex Usage - " + assemblageConcept.getDescription());
		title.getStyleClass().add("titleLabel");
		title.setAlignment(Pos.CENTER);
		title.setMaxWidth(Double.MAX_VALUE);
		title.setPadding(new Insets(5, 5, 5, 5));
		root_.setTop(title);
		
		drv_ = AppContext.getService(DynamicRefexView.class, "DynamicRefexView");
		root_.setCenter(drv_.getView());
	}
	

	/**
	 * @see gov.va.isaac.interfaces.gui.views.PopupViewI#showView(javafx.stage.Window)
	 */
	@Override
	public void showView(Window parent)
	{
		Stage stage = new Stage(StageStyle.DECORATED);
		stage.initModality(Modality.NONE);
		stage.initOwner(parent);
		Scene scene = new Scene(root_);
		stage.setScene(scene);
		stage.setTitle("Dynamic Refex Usage - " + assemblageConcept_.getDescription());
		stage.getScene().getStylesheets().add(DynamicReferencedItemsView.class.getResource("/isaac-shared-styles.css").toString());
		stage.setWidth(800);
		stage.setHeight(600);
		stage.show();
		drv_.setAssemblage(assemblageConcept_.getNid(), null);
	}
}