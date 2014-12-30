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
package gov.va.isaac.gui.refexViews.refexCreation.wizardPages;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.ConceptNode;
import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.gui.SimpleDisplayConceptComparator;
import gov.va.isaac.gui.refexViews.refexCreation.PanelControllersI;
import gov.va.isaac.gui.refexViews.refexCreation.ScreensController;
import gov.va.isaac.gui.refexViews.util.RefexDataTypeFXNodeBuilder;
import gov.va.isaac.gui.refexViews.util.RefexDataTypeNodeDetails;
import gov.va.isaac.gui.refexViews.util.RefexValidatorTypeFXNodeBuilder;
import gov.va.isaac.gui.refexViews.util.RefexValidatorTypeNodeDetails;
import gov.va.isaac.gui.util.ErrorMarkerUtils;
import gov.va.isaac.util.UpdateableBooleanBinding;
import gov.va.isaac.util.WBUtility;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.function.Function;
import javafx.application.Platform;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.RefexDynamic;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicValidatorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.javafx.collections.ObservableListWrapper;

/**
 * 
 * {@link ColumnController}
 *
 * @author <a href="jefron@apelon.com">Jesse Efron</a>
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class ColumnController implements PanelControllersI {
	@FXML private ResourceBundle resources;
	@FXML private Button nextButton;
	@FXML private Button cancelButton;
	@FXML private Button startOverButton;
	@FXML private HBox defaultValueHolder;
	@FXML private ChoiceBox<RefexDynamicDataType> typeOption;
	@FXML private Button backButton;
	@FXML private BorderPane columnDefinitionPane;
	@FXML private Label columnTitle;
	@FXML private HBox columnNameHolder;
	@FXML private TextArea columnDescription;
	@FXML private Button newColNameButton;
	@FXML private CheckBox isMandatory;
	@FXML private GridPane gridPane;
	@FXML private ChoiceBox<RefexDynamicValidatorType> validatorType;
	@FXML private HBox validatorDataHolder;
	
	private RefexValidatorTypeNodeDetails validatorTypeNode = new RefexValidatorTypeNodeDetails();

	private ScreensController processController_;
	private Region sceneParent_;
	private int columnNumber_;
	private ConceptNode columnNameSelection_;
	
	private UpdateableBooleanBinding allValid_;
	private StringBinding typeValueInvalidReason_;
	private SimpleStringProperty defaultValueInvalidReason_ = new SimpleStringProperty("");
	private RefexDataTypeNodeDetails currentDefaultNodeDetails_;

	private ObservableList<SimpleDisplayConcept> columnNameChoices = new ObservableListWrapper<>(new ArrayList<SimpleDisplayConcept>());
	private Function<ConceptVersionBI, String> colNameReader_ = (conceptVersion) -> 
	{
		RefexDynamicColumnInfo rdc = new RefexDynamicColumnInfo();
		rdc.setColumnDescriptionConcept(conceptVersion.getPrimordialUuid());
		return rdc.getColumnName();
	};


	private static final Logger logger = LoggerFactory.getLogger(ColumnController.class);

	@Override
	public void initialize() {		
		assert nextButton != null : "fx:id=\"nextButton\" was not injected: check your FXML file 'column.fxml'.";
		assert cancelButton != null : "fx:id=\"cancelButton\" was not injected: check your FXML file 'column.fxml'.";
		assert newColNameButton != null : "fx:id=\"newColNameButton\" was not injected: check your FXML file 'column.fxml'.";
		assert defaultValueHolder != null : "fx:id=\"defaultValue\" was not injected: check your FXML file 'column.fxml'.";
		assert typeOption != null : "fx:id=\"typeOption\" was not injected: check your FXML file 'column.fxml'.";
		assert backButton != null : "fx:id=\"backButton\" was not injected: check your FXML file 'column.fxml'.";
		assert columnNameHolder != null : "fx:id=\"columnNameHolder\" was not injected: check your FXML file 'column.fxml'.";
		assert columnTitle != null : "fx:id=\"columnTitle\" was not injected: check your FXML file 'column.fxml'.";
		assert isMandatory != null : "fx:id=\"isMandatory\" was not injected: check your FXML file 'column.fxml'.";
		assert columnDescription != null : "fx:id=\"columnDescription\" was not injected: check your FXML file 'column.fxml'.";
		assert columnDefinitionPane != null : "fx:id=\"columnDefinitionPane\" was not injected: check your FXML file 'column.fxml'.";
		assert gridPane != null : "fx:id=\"gridPane\" was not injected: check your FXML file 'column.fxml'.";

		columnDescription.setEditable(false);
		
		columnNameSelection_ = new ConceptNode(null, true, columnNameChoices.sorted(new SimpleDisplayConceptComparator()), colNameReader_);
		
		columnNameHolder.getChildren().add(columnNameSelection_.getNode());
		HBox.setHgrow(columnNameSelection_.getNode(), Priority.ALWAYS);
		
		typeValueInvalidReason_ = new StringBinding()
		{
			{
				bind(typeOption.valueProperty());
			}
			@Override
			protected String computeValue()
			{
				if (typeOption.getValue() == RefexDynamicDataType.UNKNOWN)
				{
					return "You must select the attribute type";
				}
				return "";
			}
		};

		initializeTypeConcepts();
		
		allValid_ = new UpdateableBooleanBinding()
		{
			{
				bind(columnNameSelection_.getConceptProperty(), defaultValueInvalidReason_, typeValueInvalidReason_);
			}
			@Override
			protected boolean computeValue()
			{
				if (columnNameSelection_.isValid().get() && defaultValueInvalidReason_.get().length() == 0 && typeValueInvalidReason_.get().length() == 0)
				{
					if (currentDefaultNodeDetails_ != null)
					{
						for (ReadOnlyStringProperty sp : currentDefaultNodeDetails_.getBoundToAllValid())
						{
							if (sp.get().length() > 0)
							{
								return false;
							}
						}
					}
					for (ReadOnlyStringProperty sp : validatorTypeNode.getBoundToAllValid())
					{
						if (sp.get().length() > 0)
						{
							return false;
						}
					}
					return true;
				}
				else
				{
					return false;
				}
			}
		};
		
		cancelButton.setOnAction(e -> 
		{
			((Stage) columnDefinitionPane.getScene().getWindow()).close();
		});
		
		nextButton.disableProperty().bind(allValid_.not());

		nextButton.setOnAction(e -> 
		{
			try
			{
				storeValues();
				processController_.showNextScreen();
			}
			catch (Exception e1)
			{
				logger.error("Unexpeted error storing screen data", e1);
				AppContext.getCommonDialogs().showErrorDialog("Unexpected error storing screen data", e1);
			}
		});

		backButton.setOnAction(e -> 
		{
			try
			{
				storeValues();
				processController_.showPreviousScreen();
			}
			catch (Exception e1)
			{
				logger.error("Unexpeted error storing screen data", e1);
				AppContext.getCommonDialogs().showErrorDialog("Unexpected error storing screen data", e1);
			}
		});
		
		startOverButton.setOnAction(e -> 
		{
			try
			{
				storeValues();
				processController_.showFirstScreen();;
			}
			catch (Exception e1)
			{
				logger.error("Unexpeted error storing screen data", e1);
				AppContext.getCommonDialogs().showErrorDialog("Unexpected error storing screen data", e1);
			}
		});
		
		newColNameButton.setOnAction(e -> createNewColumnConcept());
		
		columnNameSelection_.getConceptProperty().addListener(new ChangeListener<ConceptVersionBI>()
		{
			@Override
			public void changed(ObservableValue<? extends ConceptVersionBI> observable, ConceptVersionBI oldValue, ConceptVersionBI newValue)
			{
				RefexDynamicColumnInfo rdci = processController_.getWizardData().getColumnInfo().get(columnNumber_);
				if (newValue != null)
				{
					rdci.setColumnDescriptionConcept(newValue.getPrimordialUuid());
					columnDescription.setText(rdci.getColumnDescription());
				}
				else
				{
					rdci.setColumnDescriptionConcept(null);
					columnDescription.setText("");
				}
			}
		});
		

		typeOption.setConverter(new StringConverter<RefexDynamicDataType>()
		{
			
			@Override
			public String toString(RefexDynamicDataType object)
			{
				if (object == RefexDynamicDataType.UNKNOWN)
				{
					return "- Make Selection - ";
				}
				else
				{
					return object.getDisplayName();
				}
			}
			
			@Override
			public RefexDynamicDataType fromString(String string)
			{
				throw new RuntimeException("unecessary");
			}
		});
		
		
		typeOption.valueProperty().addListener(new ChangeListener<RefexDynamicDataType>()
		{
			@Override
			public void changed(ObservableValue<? extends RefexDynamicDataType> observable, RefexDynamicDataType oldValue, RefexDynamicDataType newValue)
			{
				defaultValueHolder.getChildren().clear();
				if (currentDefaultNodeDetails_ != null)
				{
					for (ReadOnlyStringProperty binding : currentDefaultNodeDetails_.getBoundToAllValid())
					{
						allValid_.removeBinding(binding);
					}
				}
				currentDefaultNodeDetails_ = null;
				if (newValue == RefexDynamicDataType.POLYMORPHIC)
				{
					Label l = new Label("No defaults allowed for polymorphic data");
					l.setAlignment(Pos.CENTER_LEFT);
					l.setMinHeight(25.0);
					defaultValueHolder.getChildren().add(l);
				
					validatorType.getSelectionModel().select(RefexDynamicValidatorType.UNKNOWN);
					validatorType.setDisable(true);
				}
				else if (newValue == RefexDynamicDataType.UNKNOWN)
				{
					validatorType.getSelectionModel().select(RefexDynamicValidatorType.UNKNOWN);
					validatorType.setDisable(true);
				}
				else
				{
					if (newValue == RefexDynamicDataType.BOOLEAN || newValue == RefexDynamicDataType.BYTEARRAY)
					{
						validatorType.getSelectionModel().select(RefexDynamicValidatorType.UNKNOWN);
						validatorType.setDisable(true);
					}
					else
					{
						validatorType.setDisable(false);
						updateValidationValues(newValue);
					}
					
					currentDefaultNodeDetails_ = RefexDataTypeFXNodeBuilder.buildNodeForType(newValue, null, 
							processController_.getWizardData().getColumnInfo().get(columnNumber_).getDefaultColumnValue(), null, null, null, allValid_,
							validatorType.valueProperty(), validatorTypeNode.getValidatorDataProperty());
					defaultValueHolder.getChildren().add(currentDefaultNodeDetails_.getNodeForDisplay());
					HBox.setHgrow(currentDefaultNodeDetails_.getNodeForDisplay(), Priority.ALWAYS);
				}
			}
		});
		
		validatorType.setDisable(true);
		validatorType.setConverter(new StringConverter<RefexDynamicValidatorType>()
		{
			
			@Override
			public String toString(RefexDynamicValidatorType object)
			{
				if (object == RefexDynamicValidatorType.UNKNOWN)
				{
					return "No Validator";
				}
				if (object == RefexDynamicValidatorType.EXTERNAL)
				{
					return "Drools";
				}
				else
				{
					return object.getDisplayName();
				}
			}
			
			@Override
			public RefexDynamicValidatorType fromString(String string)
			{
				throw new RuntimeException("unecessary");
			}
		});
		
		validatorType.valueProperty().addListener((change) ->
		{
			validatorDataHolder.getChildren().clear();
			
			for (ReadOnlyStringProperty binding : validatorTypeNode.getBoundToAllValid())
			{
				allValid_.removeBinding(binding);
			}
			
			if (validatorType.getValue() !=  RefexDynamicValidatorType.UNKNOWN)
			{
				if (processController_.getWizardData().getColumnInfo().get(columnNumber_).getValidator() != validatorType.getValue())
				{
					//If the validator type has changed, clear the stored value.
					processController_.getWizardData().getColumnInfo().get(columnNumber_).setValidatorData(null);
				}
				validatorTypeNode.update(RefexValidatorTypeFXNodeBuilder.buildNodeForType(validatorType.getSelectionModel().getSelectedItem(), 
						processController_.getWizardData().getColumnInfo().get(columnNumber_).getValidatorData(),
						typeOption.valueProperty(), allValid_));
				validatorDataHolder.getChildren().add(validatorTypeNode.getNodeForDisplay());
				HBox.setHgrow(validatorTypeNode.getNodeForDisplay(), Priority.ALWAYS);
			}
			else
			{
				validatorTypeNode.update(null);
			}
			allValid_.invalidate();
		});
		
		StackPane sp = new StackPane();
		ErrorMarkerUtils.swapGridPaneComponents(typeOption, sp, gridPane);
		ErrorMarkerUtils.setupErrorMarker(typeOption, sp, typeValueInvalidReason_);
	}
	
	
	private void updateValidationValues(RefexDynamicDataType dType) 
	{
		for (RefexDynamicValidatorType type : RefexDynamicValidatorType.values()) 
		{
			// TODO (artf231424) replace RegExp for UUID with Type3/5 validators that are hardwired to validate via RegExp
			if (type.validatorSupportsType(dType) && (!((dType == RefexDynamicDataType.UUID || dType == RefexDynamicDataType.NID) && type == RefexDynamicValidatorType.REGEXP)))
			{
				if (!validatorType.getItems().contains(type))
				{
					validatorType.getItems().add(type);
				}
			}
			else if (type != RefexDynamicValidatorType.UNKNOWN)  //always keep UNKNOWN
			{
				validatorType.getItems().remove(type);
			}
		}
		validatorType.getSelectionModel().select(RefexDynamicValidatorType.UNKNOWN);
	}

	private void initializeTypeConcepts() {
		for (RefexDynamicDataType type : RefexDynamicDataType.values()) {
			typeOption.getItems().add(type);
		}
		typeOption.getSelectionModel().select(RefexDynamicDataType.UNKNOWN);
		
		validatorType.getItems().add(RefexDynamicValidatorType.UNKNOWN);
		validatorType.getSelectionModel().select(RefexDynamicValidatorType.UNKNOWN);
	}

	private void createNewColumnConcept() {
		try {
			NewColumnDialog dialog = new NewColumnDialog(processController_.getScene().getWindow());
			dialog.showAndWait();

			ConceptChronicleBI newCon = dialog.getNewColumnConcept();
			
			if (newCon != null) {
				
				SimpleDisplayConcept sdc = new SimpleDisplayConcept(newCon, colNameReader_); 
				columnNameChoices.add(sdc);
				columnNameSelection_.set(sdc);
			}
		} catch (IOException e) {
			logger.error("Unexpected error creating new attribute concept", e);
			AppContext.getCommonDialogs().showErrorDialog("Unexpected error creating attribute concept.  Please see logs.", e);
		}
	}

	private void initializeColumnConcepts() {
		try {
			ConceptVersionBI colCon = WBUtility.getConceptVersion(RefexDynamic.REFEX_DYNAMIC_COLUMNS.getNid());
			ArrayList<ConceptVersionBI> colCons = WBUtility.getAllChildrenOfConcept(colCon, false);

			for (ConceptVersionBI col : colCons) {
				columnNameChoices.add(new SimpleDisplayConcept(col, colNameReader_));
			}
			columnNameSelection_.set(columnNameChoices.get(0));
		} catch (Exception e1) {
			logger.error("Unable to access attribute concepts", e1);
		}
	}

	private void updatePreviousContent() {
		
		RefexDynamicColumnInfo rdci = processController_.getWizardData().getColumnInfo().get(columnNumber_);
		
		//TODO (artf231425) this is currently triggering an infinite loop in Dans ConceptNode code... Dan needs to finish debugging this... 
		//The Platform.runLater should _NOT_ be necessary, but it seems to prevent the infinite loop for now.
		if (rdci.getColumnDescriptionConcept() != null)
		{
			Platform.runLater(() -> {columnNameSelection_.set(new SimpleDisplayConcept(rdci.getColumnDescriptionConcept().toString()));});
		}
		else
		{
			columnNameSelection_.clear();
		}
		if (rdci.getColumnDataType() != null)
		{
			typeOption.getSelectionModel().select(rdci.getColumnDataType());
		}
		else
		{
			typeOption.getSelectionModel().select(RefexDynamicDataType.UNKNOWN);
		}
		defaultValueHolder.getChildren().clear();
		if (typeOption.getSelectionModel().getSelectedItem() != RefexDynamicDataType.UNKNOWN)
		{
			if (typeOption.getSelectionModel().getSelectedItem() == RefexDynamicDataType.POLYMORPHIC)
			{
				Label l = new Label("No defaults allowed for polymorphic data");
				l.setAlignment(Pos.CENTER_LEFT);
				l.setMinHeight(25.0);
				defaultValueHolder.getChildren().add(l);
			}
			else 
			{
				currentDefaultNodeDetails_ = RefexDataTypeFXNodeBuilder.buildNodeForType(typeOption.getSelectionModel().getSelectedItem(), null, rdci.getDefaultColumnValue(), 
					null, null, null, allValid_, validatorType.valueProperty(), validatorTypeNode.getValidatorDataProperty());
				defaultValueHolder.getChildren().add(currentDefaultNodeDetails_.getNodeForDisplay());
				HBox.setHgrow(currentDefaultNodeDetails_.getNodeForDisplay(), Priority.ALWAYS);
			}
		}
		isMandatory.setSelected(rdci.isColumnRequired());
		if (rdci.getValidator() != null)
		{
			validatorType.getSelectionModel().select(rdci.getValidator());
		}
		else
		{
			validatorType.getSelectionModel().select(RefexDynamicValidatorType.UNKNOWN);
		}
		validatorDataHolder.getChildren().clear();
		if (validatorType.getSelectionModel().getSelectedItem() != RefexDynamicValidatorType.UNKNOWN)
		{
			validatorTypeNode.update(RefexValidatorTypeFXNodeBuilder.buildNodeForType(validatorType.getSelectionModel().getSelectedItem(), 
					rdci.getValidatorData(), 
					typeOption.valueProperty(), allValid_));
			validatorDataHolder.getChildren().add(validatorTypeNode.getNodeForDisplay());
			HBox.setHgrow(validatorTypeNode.getNodeForDisplay(), Priority.ALWAYS);
		}
	}

	public void storeValues() throws PropertyVetoException {
		RefexDynamicColumnInfo rdci = processController_.getWizardData().getColumnInfo().get(columnNumber_);
		
		rdci.setColumnDescriptionConcept(columnNameSelection_.isValid().get() ? columnNameSelection_.getConcept().getPrimordialUuid() : null);
		rdci.setColumnDataType(typeOption.getSelectionModel().getSelectedItem());
		rdci.setColumnDefaultData(null);//make sure it doesn't read this when parsing below
		if (currentDefaultNodeDetails_ != null)
		{
			rdci.setColumnDefaultData(RefexDataTypeFXNodeBuilder.getDataForType(currentDefaultNodeDetails_.getDataField(), rdci));
		}
		rdci.setColumnRequired(isMandatory.isSelected());
		rdci.setValidatorType((validatorType.getValue() == RefexDynamicValidatorType.UNKNOWN ? null : validatorType.getSelectionModel().getSelectedItem()));
		if (rdci.getValidator() == null)
		{
			rdci.setValidatorData(null);
		}
		else
		{
			rdci.setValidatorData(validatorTypeNode.getValidatorDataProperty().get());
		}
	}

	public void setColumnNumber(int colNum) {
		columnNumber_ = colNum;
		columnTitle.setText("Attribute #" + (columnNumber_ + 1) + " Definition");
		//don't do this slow task during JavaFX init.
		if (columnNameChoices.size() == 0)
		{
			initializeColumnConcepts();
		}
		updatePreviousContent();
	}

	/**
	 * @see gov.va.isaac.gui.refexViews.refexCreation.PanelControllersI#finishInit(gov.va.isaac.gui.refexViews.refexCreation.ScreensController, javafx.scene.Parent)
	 */
	@Override
	public void finishInit(ScreensController screenController, Region parent)
	{
		processController_ = screenController;
		sceneParent_ = parent;
	}

	/**
	 * @see gov.va.isaac.gui.refexViews.refexCreation.PanelControllersI#getParent()
	 */
	@Override
	public Region getParent()
	{
		return sceneParent_;
	}
}