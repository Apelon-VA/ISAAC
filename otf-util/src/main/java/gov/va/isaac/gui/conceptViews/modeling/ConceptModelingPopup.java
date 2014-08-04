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
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.gui.conceptViews.modeling;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.gui.util.ErrorMarkerUtils;
import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.util.UpdateableBooleanBinding;
import gov.va.isaac.util.WBUtility;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import javax.inject.Singleton;

import org.ihtsdo.otf.tcc.api.blueprint.ConceptAttributeAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeChronicleBI;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * ConceptModelingPopup
 * 
 * @author <a href="mailto:jefron@apelon.com">Jesse Efron</a>
 */

@Service
@Singleton
public class ConceptModelingPopup extends ModelingPopup
{
	
	private SimpleBooleanProperty definedIsValid_ = new SimpleBooleanProperty(true);
	private SimpleBooleanProperty modificationMade = new SimpleBooleanProperty(false);
	private SimpleStringProperty reasonSaveDisabled_ = new SimpleStringProperty();
	private Logger logger_ = LoggerFactory.getLogger(this.getClass());

	private GridPane gp_;
	private ChoiceBox<String> cb = new ChoiceBox<>();
	
	//TODO implement manditory columns
	//TODO use the word Sememe
	
	private ConceptModelingPopup()
	{
		super();
		BorderPane root = new BorderPane();

		VBox topItems = new VBox();
		topItems.setFillWidth(true);
		
		popupTitle = "Modify Concept's Attributes";
		Label title = new Label(popupTitle);
		title.getStyleClass().add("titleLabel");
		title.setAlignment(Pos.CENTER);
		title.prefWidthProperty().bind(topItems.widthProperty());
		topItems.getChildren().add(title);
		VBox.setMargin(title, new Insets(10, 10, 10, 10));

		gp_ = new GridPane();
		gp_.setHgap(10.0);
		gp_.setVgap(10.0);
		VBox.setMargin(gp_, new Insets(5, 5, 5, 5));
		topItems.getChildren().add(gp_);

		Label isFullyDefined = new Label("Is Fully Defined?");
		isFullyDefined.getStyleClass().add("boldLabel");
		gp_.add(isFullyDefined, 0, 0);

		cb.getItems().add("True");
		cb.getItems().add("False");
		cb.valueProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue ov, String oldVal, String newVal) {
				if (((ConceptAttributeVersionBI)origComp).isDefined() && newVal.equals("False") ||
					!((ConceptAttributeVersionBI)origComp).isDefined() && newVal.equals("True")) {
					modificationMade.set(true);
					
					if (!passesQA()) {
						definedIsValid_.set(false);;
						reasonSaveDisabled_.set("Failed QA");
					}
				} else {
					modificationMade.set(false);
					reasonSaveDisabled_.set("Cannot save unless original content changed");
				}
			}

			private boolean passesQA() {
				return true;
			}
		});

		gp_.add(cb, 1, 0);
				
		ColumnConstraints cc = new ColumnConstraints();
		cc.setHgrow(Priority.NEVER);
		cc.setMinWidth(FxUtils.calculateNecessaryWidthOfBoldLabel(isFullyDefined));
		gp_.getColumnConstraints().add(cc);

		cc = new ColumnConstraints();
		cc.setHgrow(Priority.ALWAYS);
		gp_.getColumnConstraints().add(cc);

		GridPane bottomRow = new GridPane();
		//spacer col
		bottomRow.add(new Region(), 0, 0);

		
		allValid_ = new UpdateableBooleanBinding()
		{
			{
				addBinding(definedIsValid_, modificationMade);
			}

			@Override
			protected boolean computeValue()
			{
				if (definedIsValid_.get() && modificationMade.get())
				{
					reasonSaveDisabled_.set("");
					return true;
				}

				return false;
			}
		};


		root.setTop(topItems);
		
		Button cancelButton = new Button("Cancel");
		cancelButton.setOnAction((action) -> {
			close();
		});
		GridPane.setMargin(cancelButton, new Insets(5, 20, 5, 0));
		GridPane.setHalignment(cancelButton, HPos.RIGHT);
		bottomRow.add(cancelButton, 1, 0);

		Button saveButton = new Button("Save");
		saveButton.disableProperty().bind(allValid_.not());
		saveButton.setOnAction((action) -> {
			doSave();
		});
		Node wrappedSave = ErrorMarkerUtils.setupDisabledInfoMarker(saveButton, reasonSaveDisabled_);
		GridPane.setMargin(wrappedSave, new Insets(5, 0, 5, 20));
		bottomRow.add(wrappedSave, 2, 0);

		//spacer col
		bottomRow.add(new Region(), 3, 0);

		cc = new ColumnConstraints();
		cc.setHgrow(Priority.SOMETIMES);
		cc.setFillWidth(true);
		bottomRow.getColumnConstraints().add(cc);
		cc = new ColumnConstraints();
		cc.setHgrow(Priority.NEVER);
		bottomRow.getColumnConstraints().add(cc);
		cc = new ColumnConstraints();
		cc.setHgrow(Priority.NEVER);
		bottomRow.getColumnConstraints().add(cc);
		cc = new ColumnConstraints();
		cc.setHgrow(Priority.SOMETIMES);
		cc.setFillWidth(true);
		bottomRow.getColumnConstraints().add(cc);
		root.setBottom(bottomRow);

		Scene scene = new Scene(root);
		scene.getStylesheets().add(ConceptModelingPopup.class.getResource("/isaac-shared-styles.css").toString());
		setScene(scene);
	}

	@Override
	void finishInit()
	{
		ConceptAttributeVersionBI attr = (ConceptAttributeVersionBI)origComp;
		
		if (attr.isDefined()) {
			cb.getSelectionModel().select("True");
		} else {
			cb.getSelectionModel().select("False");
		}
	}
	
	private void doSave()
	{
		try
		{
			boolean isDefined = (cb.getSelectionModel().getSelectedIndex() == 0); 
			ConceptAttributeAB cab = new ConceptAttributeAB(origComp.getConceptNid(), isDefined, RefexDirective.EXCLUDE);
			// Need to add a fix for storing isDefined 
			
			ConceptAttributeChronicleBI cabi = WBUtility.getBuilder().constructIfNotCurrent(cab);
			
			WBUtility.addUncommitted(cabi.getEnclosingConcept());

			if (callingView_ != null)
			{
				ExtendedAppContext.getDataStore().waitTillWritesFinished();
				callingView_.setConcept(origComp.getConceptNid());
			}
			close();
		}
		catch (Exception e)
		{
			logger_.error("Error saving refex", e);
			AppContext.getCommonDialogs().showErrorDialog("Unexpected error", "There was an error saving the refex", e.getMessage(), this);
		}
	}

	}