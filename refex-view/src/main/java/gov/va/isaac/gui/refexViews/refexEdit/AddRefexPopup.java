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
package gov.va.isaac.gui.refexViews.refexEdit;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.gui.ConceptNode;
import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.gui.dragAndDrop.DragRegistry;
import gov.va.isaac.gui.refexViews.util.RefexDataTypeFXNodeBuilder;
import gov.va.isaac.gui.refexViews.util.RefexDataTypeNodeDetails;
import gov.va.isaac.gui.util.CopyableLabel;
import gov.va.isaac.gui.util.ErrorMarkerUtils;
import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.gui.views.PopupViewI;
import gov.va.isaac.util.CommonlyUsedConcepts;
import gov.va.isaac.util.UpdateableBooleanBinding;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.ValidBooleanBinding;
import gov.va.isaac.util.WBUtility;
import java.util.ArrayList;
import java.util.UUID;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.runlevel.RunLevelException;
import org.ihtsdo.otf.query.lucene.LuceneDynamicRefexIndexer;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDynamicCAB;
import org.ihtsdo.otf.tcc.api.blueprint.TerminologyBuilderBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.metadata.ComponentType;
import org.ihtsdo.otf.tcc.api.metadata.binding.RefexDynamic;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicUsageDescription;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicValidatorType;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.RefexDynamicData;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.RefexDynamicUsageDescriptionBuilder;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicNid;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicString;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicUUID;
import org.ihtsdo.otf.tcc.model.index.service.IndexedGenerationCallable;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.javafx.collections.ObservableListWrapper;

/**
 * 
 * Refset View
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */

@Service
@PerLookup
public class AddRefexPopup extends Stage implements PopupViewI
{
	private DynamicRefexView callingView_;
	private InputType createRefexFocus_;
	private RefexDynamicGUI editRefex_;
	private Label unselectableComponentLabel_;;
	private ScrollPane sp_;
	//TODO (artf231426) improve 'ConceptNode' - this mess of Conceptnode or TextField will work for now, if they set a component type restriction
	//But if they don't set a component type restriction, we still need the field (conceptNode) to allow nids or UUIDs of other types of things.
	//both here, and in the GUI that creates the sememe - when specifying the default value.
	private ConceptNode selectableConcept_;
	private StackPane selectableComponentNode_;
	private ValidBooleanBinding selectableComponentNodeValid_;
	private TextField selectableComponent_;
	private boolean conceptNodeIsConceptType_ = false;
	private RefexDynamicUsageDescription assemblageInfo_;
	private SimpleBooleanProperty assemblageIsValid_ = new SimpleBooleanProperty(false);
	private Logger logger_ = LoggerFactory.getLogger(this.getClass());
	private UpdateableBooleanBinding allValid_;

	private ArrayList<ReadOnlyStringProperty> currentDataFieldWarnings_ = new ArrayList<>();
	private ArrayList<RefexDataTypeNodeDetails> currentDataFields_ = new ArrayList<>();
	private ObservableList<SimpleDisplayConcept> refexDropDownOptions = FXCollections.observableArrayList();
	private GridPane gp_;
	private Label title_;

	private AddRefexPopup()
	{
		super();
		BorderPane root = new BorderPane();

		VBox topItems = new VBox();
		topItems.setFillWidth(true);

		title_ = new Label("Create new sememe instance");
		title_.getStyleClass().add("titleLabel");
		title_.setAlignment(Pos.CENTER);
		title_.prefWidthProperty().bind(topItems.widthProperty());
		topItems.getChildren().add(title_);
		VBox.setMargin(title_, new Insets(10, 10, 10, 10));

		gp_ = new GridPane();
		gp_.setHgap(10.0);
		gp_.setVgap(10.0);
		VBox.setMargin(gp_, new Insets(5, 5, 5, 5));
		topItems.getChildren().add(gp_);

		Label referencedComponent = new Label("Referenced Component");
		referencedComponent.getStyleClass().add("boldLabel");
		gp_.add(referencedComponent, 0, 0);

		unselectableComponentLabel_ = new CopyableLabel();
		unselectableComponentLabel_.setWrapText(true);
		AppContext.getService(DragRegistry.class).setupDragOnly(unselectableComponentLabel_, () -> 
		{
			if (editRefex_ == null)
			{
				if (createRefexFocus_.getComponentNid() != null)
				{
					return createRefexFocus_.getComponentNid() + "";
				}
				else
				{
					return createRefexFocus_.getAssemblyNid() + "";
				}
			}
			else
			{
				return editRefex_.getRefex().getAssemblageNid() + "";
			}
		});
		//delay adding till we know which row

		Label assemblageConceptLabel = new Label("Assemblage Concept");
		assemblageConceptLabel.getStyleClass().add("boldLabel");
		gp_.add(assemblageConceptLabel, 0, 1);

		selectableConcept_ = new ConceptNode(null, true, refexDropDownOptions, null);

		selectableConcept_.getConceptProperty().addListener(new ChangeListener<ConceptVersionBI>()
		{
			@Override
			public void changed(ObservableValue<? extends ConceptVersionBI> observable, ConceptVersionBI oldValue, ConceptVersionBI newValue)
			{
				if (createRefexFocus_ != null && createRefexFocus_.getComponentNid() != null)
				{
					if (selectableConcept_.isValid().get())
					{
						//Its a valid concept, but is it a valid assemblage concept?
						try
						{
							assemblageInfo_ = RefexDynamicUsageDescriptionBuilder.readRefexDynamicUsageDescriptionConcept(selectableConcept_.getConceptNoWait().getNid());
							assemblageIsValid_.set(true);
							if (assemblageInfo_.getReferencedComponentTypeRestriction() != null)
							{
								String result = RefexDynamicValidatorType.COMPONENT_TYPE.passesValidatorStringReturn(new RefexDynamicNid(createRefexFocus_.getComponentNid()), 
										new RefexDynamicString(assemblageInfo_.getReferencedComponentTypeRestriction().name()), null);  //component type validator doesn't use vc, so null is ok
								if (result.length() > 0)
								{
									selectableConcept_.isValid().setInvalid("The selected assemblage requires the component type to be " 
											+ assemblageInfo_.getReferencedComponentTypeRestriction().toString() + ", which doesn't match the referenced component.");
									logger_.info("The selected assemblage requires the component type to be " 
											+ assemblageInfo_.getReferencedComponentTypeRestriction().toString() + ", which doesn't match the referenced component.");
									assemblageIsValid_.set(false);
								}
							}
						}
						catch (Exception e)
						{
							selectableConcept_.isValid().setInvalid("The selected concept is not properly constructed for use as an Assemblage concept");
							logger_.info("Concept not a valid concept for a sememe assemblage", e);
							assemblageIsValid_.set(false);
						}
					}
					else
					{
						assemblageInfo_ = null;
						assemblageIsValid_.set(false);
					}
					buildDataFields(assemblageIsValid_.get(), null);
				}
			}
		});

		selectableComponent_ = new TextField();
		
		selectableComponentNodeValid_ = new ValidBooleanBinding()
		{
			{
				setComputeOnInvalidate(true);
				bind(selectableComponent_.textProperty());
				invalidate();
			}
			@Override
			protected boolean computeValue()
			{
				if (createRefexFocus_ != null && createRefexFocus_.getAssemblyNid() != null && !conceptNodeIsConceptType_)
				{
					//If the assembly nid was what was set - the component node may vary - validate if we are using the text field 
					String value = selectableComponent_.getText().trim();
					if (value.length() > 0)
					{
						try
						{
							if (Utility.isUUID(value))
							{
								String result = RefexDynamicValidatorType.COMPONENT_TYPE.passesValidatorStringReturn(new RefexDynamicUUID(UUID.fromString(value)), 
										new RefexDynamicString(assemblageInfo_.getReferencedComponentTypeRestriction().name()), null);  //component type validator doesn't use vc, so null is ok
								if (result.length() > 0)
								{
									setInvalidReason(result);
									logger_.info(result);
									return false;
								}
							}
							else if (Utility.isInt(value))
							{
								String result = RefexDynamicValidatorType.COMPONENT_TYPE.passesValidatorStringReturn(new RefexDynamicNid(Integer.parseInt(value)), 
										new RefexDynamicString(assemblageInfo_.getReferencedComponentTypeRestriction().name()), null);  //component type validator doesn't use vc, so null is ok
								if (result.length() > 0)
								{
									setInvalidReason(result);
									logger_.info(result);
									return false;
								}
							}
							else
							{
								setInvalidReason("Value cannot be parsed as a component identifier.  Must be a UUID or a valid NID");
								return false;
							}
						}
						catch (Exception e)
						{
							logger_.error("Error checking component type validation", e);
							setInvalidReason("Unexpected error validating entry");
							return false;
						}
					}
					else
					{
						setInvalidReason("Component identifier is required");
						return false;
					}
				}
				clearInvalidReason();
				return true;
			}
		};
		
		selectableComponentNode_ = ErrorMarkerUtils.setupErrorMarker(selectableComponent_, null, selectableComponentNodeValid_);
		
		//delay adding concept / component till we know if / where
		ColumnConstraints cc = new ColumnConstraints();
		cc.setHgrow(Priority.NEVER);
		cc.setMinWidth(FxUtils.calculateNecessaryWidthOfBoldLabel(referencedComponent));
		gp_.getColumnConstraints().add(cc);

		cc = new ColumnConstraints();
		cc.setHgrow(Priority.ALWAYS);
		gp_.getColumnConstraints().add(cc);

		Label l = new Label("Data Fields");
		l.getStyleClass().add("boldLabel");
		VBox.setMargin(l, new Insets(5, 5, 5, 5));
		topItems.getChildren().add(l);

		root.setTop(topItems);

		sp_ = new ScrollPane();
		sp_.visibleProperty().bind(assemblageIsValid_);
		sp_.setFitToHeight(true);
		sp_.setFitToWidth(true);
		root.setCenter(sp_);

		allValid_ = new UpdateableBooleanBinding()
		{
			{
				addBinding(assemblageIsValid_, selectableConcept_.isValid(), selectableComponentNodeValid_);
			}

			@Override
			protected boolean computeValue()
			{
				if (assemblageIsValid_.get() && (conceptNodeIsConceptType_ ? selectableConcept_.isValid().get() : selectableComponentNodeValid_.get()))
				{
					boolean allDataValid = true;
					for (ReadOnlyStringProperty ssp : currentDataFieldWarnings_)
					{
						if (ssp.get().length() > 0)
						{
							allDataValid = false;
							break;
						}
					}
					if (allDataValid)
					{
						clearInvalidReason();
						return true;
					}
				}
				setInvalidReason("All errors must be corrected before save is allowed");
				return false;
			}
		};

		GridPane bottomRow = new GridPane();
		//spacer col
		bottomRow.add(new Region(), 0, 0);

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
		Node wrappedSave = ErrorMarkerUtils.setupDisabledInfoMarker(saveButton, allValid_.getReasonWhyInvalid());
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
		scene.getStylesheets().add(AddRefexPopup.class.getResource("/isaac-shared-styles.css").toString());
		setScene(scene);
	}
	
	public void finishInit(RefexDynamicGUI refexToEdit, DynamicRefexView viewToRefresh)
	{
		callingView_ = viewToRefresh;
		createRefexFocus_ = null;
		editRefex_ = refexToEdit;
		
		title_.setText("Edit existing sememe instance");
		
		gp_.add(unselectableComponentLabel_, 1, 1);
		unselectableComponentLabel_.setText(WBUtility.getDescription(editRefex_.getRefex().getAssemblageNid()));
		
		//don't actually put this in the view
		selectableConcept_.set(WBUtility.getConceptVersion(editRefex_.getRefex().getReferencedComponentNid()));
		
		Label refComp = new CopyableLabel(WBUtility.getDescription(editRefex_.getRefex().getReferencedComponentNid()));
		refComp.setWrapText(true);
		AppContext.getService(DragRegistry.class).setupDragOnly(refComp, () -> {return editRefex_.getRefex().getReferencedComponentNid() + "";});
		gp_.add(refComp, 1, 0);
		refexDropDownOptions.clear();
		try
		{
			assemblageInfo_ = RefexDynamicUsageDescriptionBuilder.readRefexDynamicUsageDescriptionConcept(editRefex_.getRefex().getAssemblageNid());
			assemblageIsValid_.set(true);
			buildDataFields(true, editRefex_.getRefex().getData());
		}
		catch (Exception e)
		{
			logger_.error("Unexpected", e);
			AppContext.getCommonDialogs().showErrorDialog("Unexpected Error reading Assembly concept", e);
		}
	}

	public void finishInit(InputType setFromType, DynamicRefexView viewToRefresh)
	{
		callingView_ = viewToRefresh;
		createRefexFocus_ = setFromType;
		editRefex_ = null;
		
		title_.setText("Create new sememe instance");

		if (createRefexFocus_.getComponentNid() != null)
		{
			gp_.add(unselectableComponentLabel_, 1, 0);
			unselectableComponentLabel_.setText(WBUtility.getDescription(createRefexFocus_.getComponentNid()));
			gp_.add(selectableConcept_.getNode(), 1, 1);
			refexDropDownOptions.clear();
			refexDropDownOptions.addAll(buildAssemblageConceptList());
		}
		else
		{
			try
			{
				assemblageInfo_ = RefexDynamicUsageDescriptionBuilder.readRefexDynamicUsageDescriptionConcept(createRefexFocus_.getAssemblyNid());
				gp_.add(unselectableComponentLabel_, 1, 1);
				unselectableComponentLabel_.setText(WBUtility.getDescription(createRefexFocus_.getAssemblyNid()));
				if (assemblageInfo_.getReferencedComponentTypeRestriction() != null 
						&& ComponentType.CONCEPT != assemblageInfo_.getReferencedComponentTypeRestriction() 
						&& ComponentType.CONCEPT_ATTRIBUTES != assemblageInfo_.getReferencedComponentTypeRestriction())
				{
					conceptNodeIsConceptType_ = false;
					gp_.add(selectableComponentNode_, 1, 0);
					selectableComponent_.setPromptText("UUID or NID of a " + assemblageInfo_.getReferencedComponentTypeRestriction().toString());
					selectableComponent_.setTooltip(new Tooltip("UUID or NID of a " + assemblageInfo_.getReferencedComponentTypeRestriction().toString()));
					selectableComponentNodeValid_.invalidate();
				}
				else
				{
					conceptNodeIsConceptType_ = true;
					gp_.add(selectableConcept_.getNode(), 1, 0);
				}
				refexDropDownOptions.clear();
				refexDropDownOptions.addAll(AppContext.getService(CommonlyUsedConcepts.class).getObservableConcepts());
				assemblageIsValid_.set(true);
				buildDataFields(true, null);
			}
			catch (Exception e)
			{
				logger_.error("Unexpected", e);
				AppContext.getCommonDialogs().showErrorDialog("Unexpected Error reading Assembly concept", e);
			}
		}
	}
	
	private void buildDataFields(boolean assemblageValid, RefexDynamicDataBI[] currentValues)
	{
		if (assemblageValid)
		{
			for (ReadOnlyStringProperty ssp : currentDataFieldWarnings_)
			{
				allValid_.removeBinding(ssp);
			}
			currentDataFieldWarnings_.clear();
			for (RefexDataTypeNodeDetails nd : currentDataFields_)
			{
				nd.cleanupListener();
			}
			currentDataFields_.clear();

			GridPane gp = new GridPane();
			gp.setHgap(10.0);
			gp.setVgap(10.0);
			gp.setStyle("-fx-padding: 5;");
			int row = 0;
			boolean extraInfoColumnIsRequired = false;
			for (RefexDynamicColumnInfo ci : assemblageInfo_.getColumnInfo())
			{
				SimpleStringProperty valueIsRequired = (ci.isColumnRequired() ? new SimpleStringProperty("") : null);
				SimpleStringProperty defaultValueTooltip = ((ci.getDefaultColumnValue() == null && ci.getValidator() == null) ? null : new SimpleStringProperty());
				ComboBox<RefexDynamicDataType> polymorphicType = null;
				
				Label l = new Label(ci.getColumnName());
				l.getStyleClass().add("boldLabel");
				l.setMinWidth(FxUtils.calculateNecessaryWidthOfBoldLabel(l));
				Tooltip.install(l, new Tooltip(ci.getColumnDescription()));
				int col = 0;
				gp.add(l, col++, row);
				
				if (ci.getColumnDataType() == RefexDynamicDataType.POLYMORPHIC)
				{
					polymorphicType = new ComboBox<>();
					polymorphicType.setEditable(false);
					polymorphicType.setConverter(new StringConverter<RefexDynamicDataType>()
					{
						
						@Override
						public String toString(RefexDynamicDataType object)
						{
							return object.getDisplayName();
						}
						
						@Override
						public RefexDynamicDataType fromString(String string)
						{
							throw new RuntimeException("unecessary");
						}
					});
					for (RefexDynamicDataType type : RefexDynamicDataType.values())
					{
						if (type == RefexDynamicDataType.POLYMORPHIC || type == RefexDynamicDataType.UNKNOWN)
						{
							continue;
						}
						else
						{
							polymorphicType.getItems().add(type);
						}
					}
					polymorphicType.getSelectionModel().select((currentValues == null ? RefexDynamicDataType.STRING : currentValues[row].getRefexDataType()));
				}
				
				RefexDataTypeNodeDetails nd = RefexDataTypeFXNodeBuilder.buildNodeForType(ci.getColumnDataType(), ci.getDefaultColumnValue(), 
						(currentValues == null ? null : currentValues[row]),valueIsRequired, defaultValueTooltip, 
						(polymorphicType == null ? null : polymorphicType.getSelectionModel().selectedItemProperty()), allValid_,
						new SimpleObjectProperty<>(ci.getValidator()), new SimpleObjectProperty<>(ci.getValidatorData()));
				
				currentDataFieldWarnings_.addAll(nd.getBoundToAllValid());
				if (ci.getColumnDataType() == RefexDynamicDataType.POLYMORPHIC)
				{
					nd.addUpdateParentListListener(currentDataFieldWarnings_);
				}
				
				currentDataFields_.add(nd);
				
				gp.add(nd.getNodeForDisplay(), col++, row);
				if (ci.isColumnRequired() || ci.getDefaultColumnValue() != null || ci.getValidator() != null)
				{
					extraInfoColumnIsRequired = true;
					
					StackPane stackPane = new StackPane();
					stackPane.setMaxWidth(Double.MAX_VALUE);
					
					if (ci.getDefaultColumnValue() != null || ci.getValidator() != null)
					{
						ImageView information = Images.INFORMATION.createImageView();
						Tooltip tooltip = new Tooltip();
						tooltip.textProperty().bind(defaultValueTooltip);
						Tooltip.install(information, tooltip);
						tooltip.setAutoHide(true);
						information.setOnMouseClicked(event -> tooltip.show(information, event.getScreenX(), event.getScreenY()));
						stackPane.getChildren().add(information);
					}
					
					if (ci.isColumnRequired())
					{
						ImageView exclamation = Images.EXCLAMATION.createImageView();

						final BooleanProperty showExclamation = new SimpleBooleanProperty(StringUtils.isNotBlank(valueIsRequired.get()));
						valueIsRequired.addListener((ChangeListener<String>) (observable, oldValue, newValue) -> showExclamation.set(StringUtils.isNotBlank(newValue)));

						exclamation.visibleProperty().bind(showExclamation);
						Tooltip tooltip = new Tooltip();
						tooltip.textProperty().bind(valueIsRequired);
						Tooltip.install(exclamation, tooltip);
						tooltip.setAutoHide(true);
						
						exclamation.setOnMouseClicked(event -> tooltip.show(exclamation, event.getScreenX(), event.getScreenY()));
						stackPane.getChildren().add(exclamation);
					}

					gp.add(stackPane, col++, row);
				}
				Label colType = new Label(ci.getColumnDataType().getDisplayName());
				colType.setMinWidth(FxUtils.calculateNecessaryWidthOfLabel(colType));
				gp.add((polymorphicType == null ? colType : polymorphicType), col++, row++);
			}

			ColumnConstraints cc = new ColumnConstraints();
			cc.setHgrow(Priority.NEVER);
			gp.getColumnConstraints().add(cc);

			cc = new ColumnConstraints();
			cc.setHgrow(Priority.ALWAYS);
			gp.getColumnConstraints().add(cc);

			if (extraInfoColumnIsRequired)
			{
				cc = new ColumnConstraints();
				cc.setHgrow(Priority.NEVER);
				gp.getColumnConstraints().add(cc);
			}
			
			cc = new ColumnConstraints();
			cc.setHgrow(Priority.NEVER);
			gp.getColumnConstraints().add(cc);

			if (row == 0)
			{
				sp_.setContent(new Label("This assemblage does not allow data fields"));
			}
			else
			{
				sp_.setContent(gp);
			}
			allValid_.invalidate();
		}
		else
		{
			sp_.setContent(null);
		}
	}

	private void doSave()
	{
		try
		{
			RefexDynamicDataBI[] data = new RefexDynamicData[assemblageInfo_.getColumnInfo().length];
			int i = 0;
			for (RefexDynamicColumnInfo ci : assemblageInfo_.getColumnInfo())
			{
				data[i] = RefexDataTypeFXNodeBuilder.getDataForType(currentDataFields_.get(i++).getDataField(), ci);
			}
			
			int componentNid;
			int assemblageNid;
			RefexDynamicCAB cab;
			if (createRefexFocus_ != null)
			{
				if (createRefexFocus_.getComponentNid() == null)
				{
					if (conceptNodeIsConceptType_)
					{
						componentNid = selectableConcept_.getConcept().getNid();
					}
					else
					{
						String value = selectableComponent_.getText().trim();
						if (Utility.isUUID(value))
						{
							componentNid = ExtendedAppContext.getDataStore().getNidForUuids(UUID.fromString(value));
						}
						else
						{
							componentNid = Integer.parseInt(value);
						}
					}
					
					assemblageNid = createRefexFocus_.getAssemblyNid();
				}
				else
				{
					componentNid = createRefexFocus_.getComponentNid();
					assemblageNid =  selectableConcept_.getConcept().getNid();
				}
				cab = new RefexDynamicCAB(componentNid, assemblageNid);
			}
			else
			{
				componentNid = editRefex_.getRefex().getReferencedComponentNid();
				assemblageNid = editRefex_.getRefex().getAssemblageNid();
				cab = editRefex_.getRefex().makeBlueprint(WBUtility.getViewCoordinate(),IdDirective.PRESERVE, RefexDirective.INCLUDE);
				//If they are editing, we assume that they want it back active, if it is retired.
				cab.setStatus(Status.ACTIVE);
			}
			
			cab.setData(data, WBUtility.getViewCoordinate());
			TerminologyBuilderBI builder = ExtendedAppContext.getDataStore().getTerminologyBuilder(WBUtility.getEditCoordinate(), WBUtility.getViewCoordinate());
			RefexDynamicChronicleBI<?> rdc = builder.construct(cab);
			
			boolean isAnnotationStyle = WBUtility.getConceptVersion(assemblageNid).isAnnotationStyleRefex();
			IndexedGenerationCallable indexGen = null;
			
			//In order to make sure we can wait for the index to have this entry, we need a latch...
			if (isAnnotationStyle)
			{
				indexGen = AppContext.getService(LuceneDynamicRefexIndexer.class).getIndexedGenerationCallable(rdc.getNid());
			}
			
			ExtendedAppContext.getDataStore().addUncommitted(ExtendedAppContext.getDataStore().getConceptForNid(componentNid));
			if (!isAnnotationStyle)
			{
				ExtendedAppContext.getDataStore().addUncommitted(WBUtility.getConceptVersion(assemblageNid));
			}
			if (callingView_ != null)
			{
				ExtendedAppContext.getDataStore().waitTillWritesFinished();
				callingView_.setNewComponentHint(componentNid, indexGen);
				callingView_.refresh();
			}
			close();
		}
		catch (Exception e)
		{
			logger_.error("Error saving sememe", e);
			AppContext.getCommonDialogs().showErrorDialog("Unexpected error", "There was an error saving the sememe", e.getMessage(), this);
		}
	}

	/**
	 * Call setReferencedComponent first
	 * 
	 * @see gov.va.isaac.interfaces.gui.views.PopupViewI#showView(javafx.stage.Window)
	 */
	@Override
	public void showView(Window parent)
	{
		if (createRefexFocus_ == null && editRefex_ == null)
		{
			throw new RunLevelException("referenced component nid must be set first");
		}
		setTitle("Create new Sememe");
		setResizable(true);

		initOwner(parent);
		initModality(Modality.NONE);
		initStyle(StageStyle.DECORATED);

		setWidth(600);
		setHeight(400);

		show();
	}
	
	private ObservableList<SimpleDisplayConcept> buildAssemblageConceptList()
	{
		ObservableList<SimpleDisplayConcept> assemblageConcepts = new ObservableListWrapper<>(new ArrayList<SimpleDisplayConcept>());
		try
		{
			ConceptVersionBI colCon = WBUtility.getConceptVersion(RefexDynamic.REFEX_DYNAMIC_IDENTITY.getNid());
			ArrayList<ConceptVersionBI> colCons = WBUtility.getAllChildrenOfConcept(colCon, false);

			for (ConceptVersionBI col : colCons) {
				assemblageConcepts.add(new SimpleDisplayConcept(col));
			}
		}
		catch (Exception e)
		{
			logger_.error("Unexpected error reading existing assemblage concepts", e);
		}

		return assemblageConcepts;
	}
}