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

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.gui.ConceptNode;
import gov.va.isaac.gui.util.ErrorMarkerUtils;
import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.interfaces.gui.MenuItemI;
import gov.va.isaac.interfaces.gui.views.PopupViewI;
import gov.va.isaac.util.UpdateableBooleanBinding;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.WBUtility;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.runlevel.RunLevelException;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDynamicCAB;
import org.ihtsdo.otf.tcc.api.blueprint.TerminologyBuilderBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicBuilderBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicUsageDescription;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.RefexDynamicData;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.RefexDynamicUsageDescriptionBuilder;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexBoolean;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexByteArray;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDouble;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexFloat;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexInteger;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexLong;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexNid;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexString;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexUUID;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private int referencedComponentNid_ = 0;
	private Label referencedComponentValue;
	private ScrollPane sp_;
	ConceptNode assemblageConcept_;
	private RefexDynamicUsageDescription assemblageInfo_;
	private SimpleBooleanProperty assemblageIsValid_ = new SimpleBooleanProperty(false);
	private SimpleStringProperty reasonSaveDisabled_ = new SimpleStringProperty();
	private Logger logger_ = LoggerFactory.getLogger(this.getClass());
	private UpdateableBooleanBinding allValid_;

	private ArrayList<StringProperty> currentDataFieldWarnings_ = new ArrayList<>();
	private ArrayList<Object> currentDataFields_ = new ArrayList<>();

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

		GridPane gp = new GridPane();
		gp.setHgap(10.0);
		gp.setVgap(10.0);
		VBox.setMargin(gp, new Insets(5, 5, 5, 5));
		topItems.getChildren().add(gp);

		Label referencedComponent = new Label("Referenced Component");
		referencedComponent.getStyleClass().add("boldLabel");
		gp.add(referencedComponent, 0, 0);

		referencedComponentValue = new Label();
		gp.add(referencedComponentValue, 1, 0);

		Label assemblageConceptLabel = new Label("Assemblage Concept");
		assemblageConceptLabel.getStyleClass().add("boldLabel");
		gp.add(assemblageConceptLabel, 0, 1);

		assemblageConcept_ = new ConceptNode(null, true);

		assemblageConcept_.getConceptProperty().addListener(new ChangeListener<ConceptVersionBI>()
		{
			@Override
			public void changed(ObservableValue<? extends ConceptVersionBI> observable, ConceptVersionBI oldValue, ConceptVersionBI newValue)
			{
				if (assemblageConcept_.isValid().get())
				{
					//Its a valid concept, but is it a valid assemblage concept?
					try
					{
						assemblageInfo_ = RefexDynamicUsageDescriptionBuilder.readRefexDynamicUsageDescriptionConcept(assemblageConcept_.getConceptNoWait().getNid());
						assemblageIsValid_.set(true);
					}
					catch (Exception e)
					{
						assemblageConcept_.isValid().set(false);
						assemblageConcept_.getInvalidReason().set("The selected concept is not properly constructed for use as an Assemblage concept");
						logger_.info("Concept not a valid concept for a refex assemblage", e);
						assemblageIsValid_.set(false);
					}
				}
				else
				{
					assemblageInfo_ = null;
					assemblageIsValid_.set(false);
				}
			}
		});

		gp.add(assemblageConcept_.getNode(), 1, 1);

		ColumnConstraints cc = new ColumnConstraints();
		cc.setHgrow(Priority.NEVER);
		cc.setMinWidth(FxUtils.calculateNecessaryWidthOfBoldLabel(referencedComponent));
		gp.getColumnConstraints().add(cc);

		cc = new ColumnConstraints();
		cc.setHgrow(Priority.ALWAYS);
		gp.getColumnConstraints().add(cc);

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
				addBinding(assemblageIsValid_);
			}

			@Override
			protected boolean computeValue()
			{
				if (assemblageIsValid_.get())
				{
					boolean allDataValid = true;
					for (StringProperty ssp : currentDataFieldWarnings_)
					{
						if (ssp.get().length() > 0)
						{
							allDataValid = false;
							break;
						}
					}
					if (allDataValid)
					{
						reasonSaveDisabled_.set("");
						return true;
					}
				}
				reasonSaveDisabled_.set("All errors must be corrected before save is allowed");
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

	public void finishInit(int referencedComponentNid, DynamicRefexView viewToRefresh)
	{
		callingView_ = viewToRefresh;
		referencedComponentNid_ = referencedComponentNid;

		referencedComponentValue.setText(WBUtility.getDescription(referencedComponentNid_));

		assemblageIsValid_.addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
			{
				if (newValue)
				{
					for (StringProperty ssp : currentDataFieldWarnings_)
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
					for (RefexDynamicColumnInfo ci : assemblageInfo_.getColumnInfo())
					{
						Label l = new Label(ci.getColumnName());
						l.getStyleClass().add("boldLabel");
						Tooltip.install(l, new Tooltip(ci.getColumnDescription()));
						gp.add(l, 0, row);
						Node n = buildNodeForType(ci.getColumnDataType(), ci.getDefaultColumnValue());
						gp.add(n, 1, row++);
					}

					ColumnConstraints cc = new ColumnConstraints();
					cc.setHgrow(Priority.NEVER);
					gp.getColumnConstraints().add(cc);

					cc = new ColumnConstraints();
					cc.setHgrow(Priority.ALWAYS);
					gp.getColumnConstraints().add(cc);

					if (row == 0)
					{
						sp_.setContent(new Label("This assemblage does not allow data fields"));
					}
					else
					{
						sp_.setContent(gp);
					}
				}
				else
				{
					sp_.setContent(null);
				}
			}
		});
	}

	private Node buildNodeForType(RefexDynamicDataType dt, Object defaultValue)
	{
		if (RefexDynamicDataType.BOOLEAN == dt)
		{
			ChoiceBox<String> cb = new ChoiceBox<>();
			cb.getItems().add("No Value");
			cb.getItems().add("True");
			cb.getItems().add("False");
			cb.getSelectionModel().select(0);
			currentDataFields_.add(cb);
			return cb;
		}
		else if (RefexDynamicDataType.BYTEARRAY == dt)
		{
			currentDataFields_.add(null);
			//TODO potentially let them pick a file to be read and included
			return new Label("Byte Array not yet supported in the GUI");

		}
		else if (RefexDynamicDataType.DOUBLE == dt || RefexDynamicDataType.FLOAT == dt || RefexDynamicDataType.INTEGER == dt || RefexDynamicDataType.LONG == dt
				|| RefexDynamicDataType.STRING == dt || RefexDynamicDataType.UUID == dt)
		{
			TextField tf = new TextField();
			currentDataFields_.add(tf);

			if (defaultValue != null)
			{
				tf.setPromptText(defaultValue.toString());
			}
			SimpleStringProperty valueInvalidReason = new SimpleStringProperty("");
			currentDataFieldWarnings_.add(valueInvalidReason);
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
					}
					else if (RefexDynamicDataType.DOUBLE == dt)
					{
						try
						{
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
						valueInvalidReason.set("");
					}
					else if (RefexDynamicDataType.UUID == dt)
					{
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

			return n;
		}
		else if (RefexDynamicDataType.NID == dt)
		{
			ConceptNode cn = new ConceptNode(null, false);
			currentDataFields_.add(cn);
			currentDataFieldWarnings_.add(cn.getInvalidReason());
			allValid_.addBinding(cn.getInvalidReason());

			return cn.getNode();
		}
		else if (RefexDynamicDataType.POLYMORPHIC == dt)
		{
			//TODO tricky one....
			HBox hBox = new HBox();
			currentDataFields_.add(null);
			hBox.getChildren().add(new Label("Polymorphic is not yet supported in the GUI"));
			return hBox;
		}
		else
		{
			throw new RuntimeException("Unexpected datatype " + dt);
		}
	}

	private void doSave()
	{
		try
		{
			RefexDynamicData[] data = new RefexDynamicData[assemblageInfo_.getColumnInfo().length];
			int i = 0;
			for (RefexDynamicColumnInfo ci : assemblageInfo_.getColumnInfo())
			{
				data[i] = getDataForType(currentDataFields_.get(i++), ci);
			}
			
			RefexDynamicCAB cab = new RefexDynamicCAB(referencedComponentNid_, assemblageConcept_.getConcept().getNid());
			cab.setData(data);
			TerminologyBuilderBI builder = ExtendedAppContext.getDataStore().getTerminologyBuilder(WBUtility.getEC(), WBUtility.getViewCoordinate());
			builder.construct(cab);
			
			ExtendedAppContext.getDataStore().addUncommitted(WBUtility.getConceptVersion(referencedComponentNid_));
			if (!assemblageConcept_.getConcept().isAnnotationStyleRefex())
			{
				ExtendedAppContext.getDataStore().addUncommitted(assemblageConcept_.getConcept());
			}
			if (callingView_ != null)
			{
				callingView_.refresh();
			}
			close();
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private RefexDynamicData getDataForType(Object data, RefexDynamicColumnInfo ci) throws PropertyVetoException
	{
		if (RefexDynamicDataType.BOOLEAN == ci.getColumnDataType())
		{
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
			else
			{
				value = (Boolean) ci.getDefaultColumnValue();
			}
			return new RefexBoolean(value, ci.getColumnName());
		}
		else if (RefexDynamicDataType.BYTEARRAY == ci.getColumnDataType())
		{
			//TODO implement
			return new RefexByteArray(new byte[] {}, ci.getColumnName());
		}
		else if (RefexDynamicDataType.DOUBLE == ci.getColumnDataType() || RefexDynamicDataType.FLOAT == ci.getColumnDataType()
				|| RefexDynamicDataType.INTEGER == ci.getColumnDataType() || RefexDynamicDataType.LONG == ci.getColumnDataType()
				|| RefexDynamicDataType.STRING == ci.getColumnDataType() || RefexDynamicDataType.UUID == ci.getColumnDataType())
		{
			TextField tf = (TextField) data;
			String text = tf.getText();
			if (RefexDynamicDataType.DOUBLE == ci.getColumnDataType())
			{
				return new RefexDouble(text.length() > 0 ? Double.parseDouble(text) : (Double)ci.getDefaultColumnValue(), ci.getColumnName());
			}
			else if (RefexDynamicDataType.FLOAT == ci.getColumnDataType())
			{
				return new RefexFloat(text.length() > 0 ? Float.parseFloat(text) : (Float)ci.getDefaultColumnValue(), ci.getColumnName());
			}
			else if (RefexDynamicDataType.INTEGER == ci.getColumnDataType())
			{
				return new RefexInteger(text.length() > 0 ? Integer.parseInt(text) : (Integer)ci.getDefaultColumnValue(), ci.getColumnName());
			}
			else if (RefexDynamicDataType.LONG == ci.getColumnDataType())
			{
				return new RefexLong(text.length() > 0 ? Long.parseLong(text) : (Long)ci.getDefaultColumnValue(), ci.getColumnName());
			}
			else if (RefexDynamicDataType.STRING == ci.getColumnDataType())
			{
				return new RefexString(text.length() > 0 ? text : (String)ci.getDefaultColumnValue(), ci.getColumnName());
			}
			else if (RefexDynamicDataType.UUID == ci.getColumnDataType())
			{
				return new RefexUUID(text.length() > 0 ? UUID.fromString(text) : (UUID)ci.getDefaultColumnValue(), ci.getColumnName());
			}
			else
			{
				throw new RuntimeException("oops");
			}
		}
		else if (RefexDynamicDataType.NID == ci.getColumnDataType())
		{
			ConceptNode cn = (ConceptNode)data;
			return new RefexNid(cn.getConcept().getNid(), ci.getColumnName());
		}
		else if (RefexDynamicDataType.POLYMORPHIC == ci.getColumnDataType())
		{
			//TODO tricky one....
			return new RefexString("", ci.getColumnName());
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
		if (referencedComponentNid_ == 0)
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
}