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
import gov.va.isaac.gui.util.ErrorMarkerUtils;
import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.gui.MenuItemI;
import gov.va.isaac.interfaces.gui.views.PopupViewI;
import gov.va.isaac.util.CommonlyUsedConcepts;
import gov.va.isaac.util.UpdateableBooleanBinding;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.WBUtility;
import java.beans.PropertyVetoException;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.runlevel.RunLevelException;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDynamicCAB;
import org.ihtsdo.otf.tcc.api.blueprint.TerminologyBuilderBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.RefexDynamic;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicUsageDescription;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicBooleanBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicDoubleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicFloatBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicIntegerBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicLongBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicNidBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicStringBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicUUIDBI;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.RefexDynamicData;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.RefexDynamicUsageDescriptionBuilder;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicBoolean;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicByteArray;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicDouble;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicFloat;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicInteger;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicLong;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicNid;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicString;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicUUID;
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
	private InputType inputType_;
	private Label unselectableComponentLabel_;;
	private ScrollPane sp_;
	private ConceptNode selectableConcept_;
	private RefexDynamicUsageDescription assemblageInfo_;
	private SimpleBooleanProperty assemblageIsValid_ = new SimpleBooleanProperty(false);
	private Logger logger_ = LoggerFactory.getLogger(this.getClass());
	private UpdateableBooleanBinding allValid_;

	private ArrayList<ReadOnlyStringProperty> currentDataFieldWarnings_ = new ArrayList<>();
	private ArrayList<Object> currentDataFields_ = new ArrayList<>();
	private ObservableList<SimpleDisplayConcept> refexDropDownOptions = FXCollections.observableArrayList();
	private GridPane gp_;

	//TODO use the word Sememe
	
	private AddRefexPopup()
	{
		super();
		BorderPane root = new BorderPane();

		VBox topItems = new VBox();
		topItems.setFillWidth(true);

		Label title = new Label("Create new refex instance");
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

		Label referencedComponent = new Label("Referenced Component");
		referencedComponent.getStyleClass().add("boldLabel");
		gp_.add(referencedComponent, 0, 0);

		unselectableComponentLabel_ = new Label();
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
				if (inputType_.getComponentNid() != null)
				{
					if (selectableConcept_.isValid().get())
					{
						//Its a valid concept, but is it a valid assemblage concept?
						try
						{
							assemblageInfo_ = RefexDynamicUsageDescriptionBuilder.readRefexDynamicUsageDescriptionConcept(selectableConcept_.getConceptNoWait().getNid());
							assemblageIsValid_.set(true);
						}
						catch (Exception e)
						{
							selectableConcept_.isValid().setInvalid("The selected concept is not properly constructed for use as an Assemblage concept");
							logger_.info("Concept not a valid concept for a refex assemblage", e);
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

		//delay adding concept till we know where
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
				addBinding(assemblageIsValid_, selectableConcept_.isValid());
			}

			@Override
			protected boolean computeValue()
			{
				if (assemblageIsValid_.get() && selectableConcept_.isValid().get())
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

	/**
	 * @see gov.va.isaac.interfaces.gui.views.IsaacViewI#getMenuBarMenus()
	 */
	@Override
	public List<MenuItemI> getMenuBarMenus()
	{
		return new ArrayList<>();
	}
	
	public void finishInit(RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>> refex, DynamicRefexView viewToRefresh)
	{
		finishInit(new InputType(refex), viewToRefresh);
	}

	public void finishInit(InputType setFromType, DynamicRefexView viewToRefresh)
	{
		callingView_ = viewToRefresh;
		inputType_ = setFromType;
		
		if (inputType_.getRefex() != null)
		{
			gp_.add(unselectableComponentLabel_, 1, 1);
			unselectableComponentLabel_.setText(WBUtility.getDescription(inputType_.getRefex().getAssemblageNid()));
			
			//don't actually put this in the view
			selectableConcept_.set(WBUtility.getConceptVersion(inputType_.getRefex().getReferencedComponentNid()));
			
			gp_.add(new Label(WBUtility.getDescription(inputType_.getRefex().getReferencedComponentNid())), 1, 0);
			refexDropDownOptions.clear();
			try
			{
				assemblageInfo_ = RefexDynamicUsageDescriptionBuilder.readRefexDynamicUsageDescriptionConcept(inputType_.getRefex().getAssemblageNid());
				assemblageIsValid_.set(true);
				buildDataFields(true, inputType_.getRefex().getData());
			}
			catch (Exception e)
			{
				logger_.error("Unexpected", e);
				AppContext.getCommonDialogs().showErrorDialog("Unexpected Error reading Assembly concept", e);
			}
		}
		else
		{
			if (inputType_.getComponentNid() != null)
			{
				gp_.add(unselectableComponentLabel_, 1, 0);
				unselectableComponentLabel_.setText(WBUtility.getDescription(inputType_.getComponentNid()));
				gp_.add(selectableConcept_.getNode(), 1, 1);
				refexDropDownOptions.clear();
				refexDropDownOptions.addAll(buildAssemblageConceptList());
			}
			else
			{
				gp_.add(unselectableComponentLabel_, 1, 1);
				unselectableComponentLabel_.setText(WBUtility.getDescription(inputType_.getAssemblyNid()));
				gp_.add(selectableConcept_.getNode(), 1, 0);
				refexDropDownOptions.clear();
				refexDropDownOptions.addAll(AppContext.getService(CommonlyUsedConcepts.class).getObservableConcepts());
				try
				{
					assemblageInfo_ = RefexDynamicUsageDescriptionBuilder.readRefexDynamicUsageDescriptionConcept(inputType_.getAssemblyNid());
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
				SimpleStringProperty defaultValueTooltip = (ci.getDefaultColumnValue() == null ? null : new SimpleStringProperty());
				ComboBox<RefexDynamicDataType> polymorphicType = null;
				
				Label l = new Label(ci.getColumnName());
				l.getStyleClass().add("boldLabel");
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
				
				Node n = buildNodeForType(ci.getColumnDataType(), ci.getDefaultColumnValue(), (currentValues == null ? null : currentValues[row]), 
						valueIsRequired, defaultValueTooltip, (polymorphicType == null ? null : polymorphicType.getSelectionModel().selectedItemProperty()),
						currentDataFieldWarnings_, currentDataFields_, true);
				gp.add(n, col++, row);
				if (ci.isColumnRequired() || ci.getDefaultColumnValue() != null)
				{
					extraInfoColumnIsRequired = true;
					
					StackPane stackPane = new StackPane();
					stackPane.setMaxWidth(Double.MAX_VALUE);
					
					if (ci.getDefaultColumnValue() != null)
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
				gp.add((polymorphicType == null ? new Label(ci.getColumnDataType().getDisplayName()) : polymorphicType), col++, row++);
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

	/**
	 * when valueIsRequired is null, it is understood to be optional.  If it is not null, need to tie it in to the listeners on the field - setting
	 * an appropriate message if the field is empty.
	 */
	private Node buildNodeForType(RefexDynamicDataType dt, RefexDynamicDataBI defaultValue, RefexDynamicDataBI currentValue, SimpleStringProperty valueIsRequired,
			SimpleStringProperty defaultValueTooltip, ReadOnlyObjectProperty<RefexDynamicDataType> polymorphicSelection, 
			ArrayList<ReadOnlyStringProperty> currentDataFieldWarningsProxy, ArrayList<Object> currentDataFieldsProxy, boolean isFirstLevel)
	{
		Node returnValue = null;
		if (RefexDynamicDataType.BOOLEAN == dt)
		{
			ChoiceBox<String> cb = new ChoiceBox<>();
			cb.getItems().add("No Value");
			cb.getItems().add("True");
			cb.getItems().add("False");
			
			if (valueIsRequired != null)
			{
				cb.getSelectionModel().selectedIndexProperty().addListener((change) ->
				{
					if (cb.getSelectionModel().getSelectedIndex() == 0)
					{
						valueIsRequired.set("You must select True or False");
					}
					else
					{
						valueIsRequired.set("");
					}
				});
			}
			
			if (currentValue == null)
			{
				if (defaultValue != null)
				{
					if (((RefexDynamicBooleanBI)defaultValue).getDataBoolean())
					{
						cb.getSelectionModel().select(1);
					}
					else
					{
						cb.getSelectionModel().select(2);
					}
				}
				else
				{
					cb.getSelectionModel().select(0);
				}
			}
			else
			{
				if (((RefexDynamicBooleanBI)currentValue).getDataBoolean())
				{
					cb.getSelectionModel().select(1);
				}
				{
					cb.getSelectionModel().select(2);
				}
			}
			if (defaultValue != null)
			{
				defaultValueTooltip.set("The default value for this field is '" + defaultValue.getDataObject().toString() + "'");
			}
			currentDataFieldsProxy.add(cb);
			returnValue = cb;
		}
		else if (RefexDynamicDataType.BYTEARRAY == dt)
		{
			HBox hbox = new HBox();
			hbox.setMaxWidth(Double.MAX_VALUE);
			Label choosenFile = new Label("- no data attached -");
			if (valueIsRequired != null)
			{
				valueIsRequired.set("You must select a file to attach");
			}
			choosenFile.setAlignment(Pos.CENTER_LEFT);
			choosenFile.setMaxWidth(Double.MAX_VALUE);
			choosenFile.setMaxHeight(Double.MAX_VALUE);
			Tooltip tt = new Tooltip("Select a file to attach to the refex");
			Tooltip.install(choosenFile, tt);
			Button fileChooser = new Button("Choose File...");
			final ByteArrayDataHolder dataHolder = new ByteArrayDataHolder();
			
			if (currentValue != null)
			{
				dataHolder.data = ((RefexDynamicByteArray)currentValue).getData();
				choosenFile.setText("Currently has " + dataHolder.data.length + " bytes attached");
				if (valueIsRequired != null)
				{
					valueIsRequired.set("");
				}
			}
			else if (defaultValue != null)
			{
				dataHolder.data = ((RefexDynamicByteArray)defaultValue).getData();
				choosenFile.setText("Will attach the default value of " + dataHolder.data.length + " bytes");
				if (valueIsRequired != null)
				{
					valueIsRequired.set("");
				}
			}
			
			fileChooser.setOnAction((event) -> 
			{
				FileChooser fc = new FileChooser();
				fc.setTitle("Select a file to attach to the refex");
				File selectedFile = fc.showOpenDialog(getScene().getWindow());
				if (selectedFile != null && selectedFile.isFile())
				{
					
					try
					{
						dataHolder.data = Files.readAllBytes(selectedFile.toPath());
						Platform.runLater(() -> 
						{
							choosenFile.setText(selectedFile.getName());
							tt.setText(selectedFile.getAbsolutePath());
							if (valueIsRequired != null)
							{
								valueIsRequired.set("");
							}
						});
					}
					catch (Exception e)
					{
						AppContext.getCommonDialogs().showErrorDialog("Error reading the selected file", e);
					}
				}
				else
				{
					Platform.runLater(() -> 
					{
						if (defaultValue != null)
						{
							dataHolder.data = ((RefexDynamicByteArray)defaultValue).getData();
							choosenFile.setText("Will attach the default value of " + dataHolder.data.length + " bytes");
							if (valueIsRequired != null)
							{
								valueIsRequired.set("");
							}
						}
						else
						{
							choosenFile.setText("- no data attached -");
							tt.setText("Select a file to attach that file to the refex");
							if (valueIsRequired != null)
							{
								valueIsRequired.set("You must select a file to attach");
							}
						}
					});
				}
			});
			
			currentDataFieldsProxy.add(dataHolder);
			
			hbox.getChildren().add(choosenFile);
			hbox.getChildren().add(fileChooser);
			HBox.setHgrow(choosenFile, Priority.ALWAYS);
			HBox.setHgrow(fileChooser, Priority.NEVER);
			returnValue = hbox;
			if (defaultValue != null)
			{
				defaultValueTooltip.set("If no file is selected, the default value of " + ((RefexDynamicByteArray)defaultValue).getData().length +  " bytes will be used");
			}
		}
		else if (RefexDynamicDataType.DOUBLE == dt || RefexDynamicDataType.FLOAT == dt || RefexDynamicDataType.INTEGER == dt || RefexDynamicDataType.LONG == dt
				|| RefexDynamicDataType.STRING == dt || RefexDynamicDataType.UUID == dt)
		{
			TextField tf = new TextField();
			currentDataFieldsProxy.add(tf);

			if (defaultValue != null)
			{
				tf.setPromptText(defaultValue.getDataObject().toString());
			}
			SimpleStringProperty valueInvalidReason = new SimpleStringProperty("");
			currentDataFieldWarningsProxy.add(valueInvalidReason);
			allValid_.addBinding(valueInvalidReason);
			Node n = ErrorMarkerUtils.setupErrorMarker(tf, valueInvalidReason);

			tf.textProperty().addListener(new ChangeListener<String>()
			{
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
				{
					if (newValue.length() == 0)
					{
						valueInvalidReason.setValue("");
						if (valueIsRequired != null && defaultValue == null)
						{
							valueIsRequired.set("You must specify a value for this field");
						}
					}
					else if (RefexDynamicDataType.DOUBLE == dt)
					{
						try
						{
							if (valueIsRequired != null)
							{
								valueIsRequired.set("");
							}
							Double.parseDouble(tf.getText());
							valueInvalidReason.set("");
						}
						catch (Exception e)
						{
							valueInvalidReason.set("The value (if present) must be a double");
						}

					}
					else if (RefexDynamicDataType.FLOAT == dt)
					{
						try
						{
							if (valueIsRequired != null)
							{
								valueIsRequired.set("");
							}
							Float.parseFloat(tf.getText());
							valueInvalidReason.set("");
						}
						catch (Exception e)
						{
							valueInvalidReason.set("The value (if present) must be a float");
						}
					}
					else if (RefexDynamicDataType.INTEGER == dt)
					{
						try
						{
							if (valueIsRequired != null)
							{
								valueIsRequired.set("");
							}
							Integer.parseInt(tf.getText());
							valueInvalidReason.set("");
						}
						catch (Exception e)
						{
							valueInvalidReason.set("The value (if present) must be an integer");
						}
					}
					else if (RefexDynamicDataType.LONG == dt)
					{
						try
						{
							if (valueIsRequired != null)
							{
								valueIsRequired.set("");
							}
							Long.parseLong(tf.getText());
							valueInvalidReason.set("");
						}
						catch (Exception e)
						{
							valueInvalidReason.set("The value (if present) must be a long");
						}
					}
					else if (RefexDynamicDataType.STRING == dt)
					{
						if (valueIsRequired != null)
						{
							valueIsRequired.set("");
						}
						valueInvalidReason.set("");
					}
					else if (RefexDynamicDataType.UUID == dt)
					{
						if (valueIsRequired != null)
						{
							valueIsRequired.set("");
						}
						if (Utility.isUUID(tf.getText()))
						{
							valueInvalidReason.set("");
						}
						else
						{
							valueInvalidReason.set("The value (if present) must be a properly formatted UUID");
						}
					}
				}
			});
			
			if (currentValue != null)
			{
				tf.setText(currentValue.getDataObject().toString());
			}
			if (currentValue == null && valueIsRequired != null && defaultValue == null)
			{
				valueIsRequired.set("You must specify a value for this field");
			}
			returnValue = n;
			if (defaultValue != null)
			{
				defaultValueTooltip.set("If no value is specified the default value of '" +defaultValue.getDataObject().toString()+  "' will be used");
			}
		}
		else if (RefexDynamicDataType.NID == dt)
		{
			ConceptNode cn = new ConceptNode(null, false);
			currentDataFieldsProxy.add(cn);
			currentDataFieldWarningsProxy.add(cn.isValid().getReasonWhyInvalid());
			allValid_.addBinding(cn.isValid().getReasonWhyInvalid());
			
			if (currentValue != null)
			{
				//TODO this doesn't work, if the nid isn't a concept nid.  We need a NidNode, rather than a ConceptNode
				cn.set(WBUtility.getConceptVersion(((RefexDynamicNidBI)currentValue).getDataNid()));
			}
			
			if (valueIsRequired != null && defaultValue == null)
			{
				if (currentValue == null)
				{
					valueIsRequired.set("You must specify a value for this field");
				}
				cn.getConceptProperty().addListener((change) ->
				{
					if (cn.getConceptProperty().getValue() == null)
					{
						valueIsRequired.set("You must specify a value for this field");
					}
					else
					{
						valueIsRequired.set("");
					}
				});
			}

			returnValue = cn.getNode();
			if (defaultValue != null)
			{
				defaultValueTooltip.set("If no value is specified the default value of '" +defaultValue.getDataObject().toString()+  "' will be used");
			}
		}
		else if (RefexDynamicDataType.POLYMORPHIC == dt)
		{
			//a slick little bit of recursion... but a bit tricky to keep the validators aligned properly...
			HBox hBox = new HBox();
			hBox.setMaxWidth(Double.MAX_VALUE);
			NestedPolymorphicData nestedData = new NestedPolymorphicData();
			
			ArrayList<ReadOnlyStringProperty> currentDataFieldWarningsNested = new ArrayList<>();
			ArrayList<Object> currentDataFieldsNested = new ArrayList<>();
			hBox.getChildren().add(buildNodeForType(polymorphicSelection.get(), null, currentValue, valueIsRequired, defaultValueTooltip, null,
					currentDataFieldWarningsNested, currentDataFieldsNested, false));
			HBox.setHgrow(hBox.getChildren().get(0), Priority.ALWAYS);
			
			nestedData.userData = currentDataFieldsNested.get(0);
			nestedData.dataType = polymorphicSelection.get();
			currentDataFieldsProxy.add(nestedData);
			if (currentDataFieldWarningsNested.size() == 1)
			{
				currentDataFieldWarnings_.add(currentDataFieldWarningsNested.get(0));
				allValid_.invalidate(); //the new binding will already be made to allValid_ during the recursion - but it may have computed without ths full list...
			}
			
			polymorphicSelection.addListener((change) ->
			{
				hBox.getChildren().remove(0);
				if (currentDataFieldWarningsNested.size() == 1)
				{
					currentDataFieldsProxy.remove(currentDataFieldWarningsNested.get(0));
					allValid_.removeBinding(currentDataFieldWarningsNested.get(0));
				}
				currentDataFieldWarningsNested.clear();
				currentDataFieldsNested.clear();
				hBox.getChildren().add(buildNodeForType(polymorphicSelection.get(), null, currentValue, valueIsRequired, defaultValueTooltip, null, 
						currentDataFieldWarningsNested, currentDataFieldsNested, false));
				HBox.setHgrow(hBox.getChildren().get(0), Priority.ALWAYS);
				nestedData.userData = currentDataFieldsNested.get(0);
				nestedData.dataType = polymorphicSelection.get();
				if (currentDataFieldWarningsNested.size() == 1)
				{
					currentDataFieldsProxy.add(currentDataFieldWarningsNested.get(0));
					allValid_.invalidate(); //the new binding will already be made to allValid_ during the recursion - but it may have computed without ths full list...
				}
			});
			returnValue = hBox;
		}
		else
		{
			throw new RuntimeException("Unexpected datatype " + dt);
		}
		if (isFirstLevel && valueIsRequired != null)
		{
			currentDataFieldWarnings_.add(valueIsRequired);
			allValid_.addBinding(valueIsRequired);
		}
		return returnValue;
	}

	private void doSave()
	{
		try
		{
			RefexDynamicDataBI[] data = new RefexDynamicData[assemblageInfo_.getColumnInfo().length];
			int i = 0;
			for (RefexDynamicColumnInfo ci : assemblageInfo_.getColumnInfo())
			{
				data[i] = getDataForType(currentDataFields_.get(i++), ci);
			}
			
			int componentNid;
			int assemblageNid;
			if (inputType_.getRefex() == null)
			{
				if (inputType_.getComponentNid() == null)
				{
					componentNid = selectableConcept_.getConcept().getNid();
					assemblageNid = inputType_.getAssemblyNid();
				}
				else
				{
					componentNid = inputType_.getComponentNid();
					assemblageNid =  selectableConcept_.getConcept().getNid();
				}
			}
			else
			{
				componentNid = inputType_.getRefex().getReferencedComponentNid();
				assemblageNid = inputType_.getRefex().getAssemblageNid();
			}
			
			RefexDynamicCAB cab;
			if (inputType_.getRefex() == null)
			{
				cab = new RefexDynamicCAB(componentNid, assemblageNid);
			}
			else
			{
				cab = inputType_.getRefex().makeBlueprint(WBUtility.getViewCoordinate(),IdDirective.PRESERVE, RefexDirective.INCLUDE);
			}
			cab.setData(data, WBUtility.getViewCoordinate());
			TerminologyBuilderBI builder = ExtendedAppContext.getDataStore().getTerminologyBuilder(WBUtility.getEC(), WBUtility.getViewCoordinate());
			builder.construct(cab);
			
			ExtendedAppContext.getDataStore().addUncommitted(WBUtility.getConceptVersion(componentNid));
			if (!WBUtility.getConceptVersion(assemblageNid).isAnnotationStyleRefex())
			{
				ExtendedAppContext.getDataStore().addUncommitted(WBUtility.getConceptVersion(assemblageNid));
			}
			if (callingView_ != null)
			{
				ExtendedAppContext.getDataStore().waitTillWritesFinished();
				callingView_.setNewComponentHint(componentNid);
				callingView_.refresh();
			}
			close();
		}
		catch (Exception e)
		{
			logger_.error("Error saving refex", e);
			AppContext.getCommonDialogs().showErrorDialog("Unexpected error", "There was an error saving the refex", e.getMessage(), this);
		}
	}

	private RefexDynamicDataBI getDataForType(Object data, RefexDynamicColumnInfo ci) throws PropertyVetoException
	{
		if (RefexDynamicDataType.BOOLEAN == ci.getColumnDataType())
		{
			@SuppressWarnings("unchecked")
			ChoiceBox<String> cb = (ChoiceBox<String>) data;

			Boolean value = null;
			if (cb.getSelectionModel().getSelectedItem().equals("True"))
			{
				value = true;
			}
			else if (cb.getSelectionModel().getSelectedItem().equals("False"))
			{
				value = false;
			}
			else if (ci.getDefaultColumnValue() != null)
			{
				value =  ((RefexDynamicBooleanBI) ci.getDefaultColumnValue()).getDataBoolean();
			}
			return (value == null ? null : new RefexDynamicBoolean(value));
		}
		else if (RefexDynamicDataType.BYTEARRAY == ci.getColumnDataType())
		{
			if (data == null && ci.getDefaultColumnValue() == null)
			{
				return null;
			}
			ByteArrayDataHolder holder = (ByteArrayDataHolder) data;
			if (holder == null || holder.data == null)
			{
				return (RefexDynamicByteArray)ci.getDefaultColumnValue();
			}
			return new RefexDynamicByteArray(holder.data);
		}
		else if (RefexDynamicDataType.DOUBLE == ci.getColumnDataType() || RefexDynamicDataType.FLOAT == ci.getColumnDataType()
				|| RefexDynamicDataType.INTEGER == ci.getColumnDataType() || RefexDynamicDataType.LONG == ci.getColumnDataType()
				|| RefexDynamicDataType.STRING == ci.getColumnDataType() || RefexDynamicDataType.UUID == ci.getColumnDataType())
		{
			TextField tf = (TextField) data;
			String text = tf.getText();
			if (text.length() == 0 && ci.getDefaultColumnValue() == null)
			{
				return null;
			}
			if (RefexDynamicDataType.DOUBLE == ci.getColumnDataType())
			{
				return (text.length() > 0 ? new RefexDynamicDouble(Double.parseDouble(text)) : (RefexDynamicDoubleBI)ci.getDefaultColumnValue());
			}
			else if (RefexDynamicDataType.FLOAT == ci.getColumnDataType())
			{
				return (text.length() > 0 ? new RefexDynamicFloat(Float.parseFloat(text)) : (RefexDynamicFloatBI)ci.getDefaultColumnValue());
			}
			else if (RefexDynamicDataType.INTEGER == ci.getColumnDataType())
			{
				return (text.length() > 0 ? new RefexDynamicInteger(Integer.parseInt(text)) : (RefexDynamicIntegerBI)ci.getDefaultColumnValue());
			}
			else if (RefexDynamicDataType.LONG == ci.getColumnDataType())
			{
				return (text.length() > 0 ? new RefexDynamicLong(Long.parseLong(text)) : (RefexDynamicLongBI)ci.getDefaultColumnValue());
			}
			else if (RefexDynamicDataType.STRING == ci.getColumnDataType())
			{
				return (text.length() > 0 ? new RefexDynamicString(text) : (RefexDynamicStringBI)ci.getDefaultColumnValue());
			}
			else if (RefexDynamicDataType.UUID == ci.getColumnDataType())
			{
				return (text.length() > 0 ? new RefexDynamicUUID(UUID.fromString(text)) : (RefexDynamicUUIDBI)ci.getDefaultColumnValue());
			}
			else
			{
				throw new RuntimeException("oops");
			}
		}
		else if (RefexDynamicDataType.NID == ci.getColumnDataType())
		{
			ConceptNode cn = (ConceptNode)data;
			if (cn.getConcept() == null)
			{
				return (RefexDynamicNid)ci.getDefaultColumnValue();
			}
			return new RefexDynamicNid(cn.getConcept().getNid());
		}
		else if (RefexDynamicDataType.POLYMORPHIC == ci.getColumnDataType())
		{
			NestedPolymorphicData nestedData = (NestedPolymorphicData)data;
			//HACK - only need the data type field... but this is the type we want.
			//override datatype, and default (default value isn't allowed for polymorphic)
			RefexDynamicColumnInfo nestedCI = new RefexDynamicColumnInfo(ci.getColumnOrder(), ci.getColumnDescriptionConcept(), nestedData.dataType, null, 
					ci.isColumnRequired(), ci.getValidator(), ci.getValidatorData());
			return getDataForType(nestedData.userData, nestedCI);
		}
		else
		{
			throw new RuntimeException("Unexpected datatype " + ci.getColumnDataType());
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
		if (inputType_ == null)
		{
			throw new RunLevelException("referenced component nid must be set first");
		}
		setTitle("Create new Refex");
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
	
	private class ByteArrayDataHolder
	{
		byte[] data;
	}
	
	private class NestedPolymorphicData
	{
		RefexDynamicDataType dataType;
		Object userData;
	}
}