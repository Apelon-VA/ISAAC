package gov.va.isaac.gui.listview.operations;
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

import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.gui.util.ErrorMarkerUtils;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.ExportTaskHandlerI;
import gov.va.isaac.util.OTFUtility;
import gov.va.isaac.util.ValidBooleanBinding;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.IntStream;

import javafx.beans.binding.BooleanExpression;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import org.glassfish.hk2.api.PerLookup;
import org.ihtsdo.otf.tcc.dto.TtkConceptChronicle;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link EConceptExportOperation}
 * 
 * 
 * @author <a href="mailto:vkaloidis@apelon.com">Vas Kaloidis</a>
 */
@Service
public class EconceptExportOperation extends Operation
{
	private Logger logger_ = LoggerFactory.getLogger(this.getClass());
	private Map<String, Set<String>> successCons = new HashMap<>(); // we aren't returning anything
	
	private final FileChooser fileChooser = new FileChooser();
	private final Button openFileChooser = new Button(null, Images.FOLDER.createImageView());
	private GridPane root = new GridPane();
	private File file = null;
	private String fileName = "";
	private String filePath = "";
	private String fileExtension = null;
	private ToggleGroup toggleGroup = new ToggleGroup();
	private TextField outputField = new TextField();
	
	private ValidBooleanBinding allFieldsValid;
	private DataOutputStream dos_;
	private String toggleSelected = null;
	
	public enum ExportFileExtensionEnum
	{
		Econcept ("EConcept", "EConcept Files .jbin", ".jbin"),
		Changeset ("Changeset", "Changeset Files .eccs", ".eccs"),
		Xml ("Xml", "Xml Files .xml", ".xml");
		
		private final String name;
		private final String extensionDesc;
		private final String extensionFormat;
		
		ExportFileExtensionEnum(String name, 
				String extensionDesc, 
				String extensionFormat) 
		{
			this.name = name;
			this.extensionDesc = extensionDesc;
			this.extensionFormat = extensionFormat;
		}
	}
	
	private EconceptExportOperation()
	{
		//TODO: Implement Interfaces and HK2 Injection, remove dep's from POMS
		//For HK2 to init
	}
	
	public void setFileName(String fn)
	{
		fileName = fn;
	}
	
	public String getFileName()
	{
		return fileName;
	}
	
	@Override
	public void init(ObservableList<SimpleDisplayConcept> conceptList)
	{
		root.setHgap(10); 
		root.setVgap(10);
		
		super.init(conceptList);
		
		fileChooser.setTitle("Save EConcept Export File");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter(ExportFileExtensionEnum.Econcept.extensionDesc, "*" + ExportFileExtensionEnum.Econcept.extensionFormat));
		fileName = ExportFileExtensionEnum.Econcept.name + "_Export";
		fileExtension =	ExportFileExtensionEnum.Econcept.extensionFormat;
		fileChooser.setInitialFileName(fileName + fileExtension);
		toggleSelected = ExportFileExtensionEnum.Econcept.name;
		
		openFileChooser.setOnAction(
			new EventHandler<ActionEvent>() {
				@Override
				public void handle(final ActionEvent e) {
					file = fileChooser.showSaveDialog(null);
					if(file != null)
					{
						if(file.getAbsolutePath() != null && file.getAbsolutePath() != "")
						{
							fileName = file.getName();
							filePath = file.getAbsolutePath();
							outputField.setText(filePath);
						} 
					}
				}
			});

		allFieldsValid = new ValidBooleanBinding() {
			{
				bind(outputField.textProperty());
				setComputeOnInvalidate(true);
			}
			@Override
			protected boolean computeValue()
			{
				if(outputField.getText() != "" || !outputField.getText().trim().isEmpty()) 
				{
					if(!filePath.equals(""))
					{
						String path = filePath.substring(0, filePath.lastIndexOf(File.separator));
						logger_.debug("Output Directory: " + path);
						File f = new File(path);
						if(file.isFile()) {
							this.setInvalidReason("The file " + filePath + " already exists");
							return false;
						}
						else if(f.isDirectory()){
							return true;
						} else {
							this.setInvalidReason("Output Path is not a directory");
							return false;
						}
					} else {
						this.setInvalidReason("File Output Directory is not set - output field null");
						return false;
					}
				}
				else
				{
					this.setInvalidReason("Output field is empty, output directory is nto set");
					return false;
				}
				//TODO: Check to make sure outputField output is a valid file. Validate if parent folder exists for file & check if file selected is not a folder. Check if valid
			}
		};
		
		root.add(openFileChooser, 3, 0); //Path Label - Row 2
		GridPane.setHalignment(openFileChooser, HPos.LEFT);
		
		Label outputLocationLabel = new Label("Output Location");
		root.add(outputLocationLabel, 0, 0);
		GridPane.setHalignment(outputLocationLabel, HPos.LEFT);
		
		StackPane sp = ErrorMarkerUtils.setupErrorMarker(outputField, null, allFieldsValid);
		root.add(sp, 1, 0);
		GridPane.setHalignment(sp, HPos.LEFT);
		
		Label eConChangesetLabel = new Label("EConcept / Changeset");
		root.add(eConChangesetLabel, 0, 1); //Path Label - Row 3
		GridPane.setHalignment(eConChangesetLabel, HPos.LEFT);
		
		VBox EconceptChangesetRadioVbox = new VBox();
		EconceptChangesetRadioVbox.setSpacing(4.0);
		
		RadioButton eConceptRadioButton = new RadioButton();
		eConceptRadioButton.setText("EConcept");
		eConceptRadioButton.setSelected(true);
		eConceptRadioButton.setTooltip(new Tooltip("Export selected concepts as an EConcept (.jbin) file"));
		eConceptRadioButton.setToggleGroup(toggleGroup);
		EconceptChangesetRadioVbox.getChildren().add(eConceptRadioButton);
		
		RadioButton changesetRadioButton = new RadioButton();
		changesetRadioButton.setText("Changeset");
		changesetRadioButton.setToggleGroup(toggleGroup);
		changesetRadioButton.setTooltip(new Tooltip("Export selected concepts as a Changeset (.eccs) file. Difference: export time placed in-between each concept exported."));
		EconceptChangesetRadioVbox.getChildren().add(changesetRadioButton);
		
		RadioButton xmlRadioButton = new RadioButton();
		xmlRadioButton.setText("XML");
		xmlRadioButton.setToggleGroup(toggleGroup);
		xmlRadioButton.setTooltip(new Tooltip("Export selected concepts as an XML (.xml) file."));
		EconceptChangesetRadioVbox.getChildren().add(xmlRadioButton);
		root.add(EconceptChangesetRadioVbox, 1, 1);
		
		toggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			@Override
			public void changed( ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
				if(newValue != oldValue)
				{
					RadioButton selectedRadio = (RadioButton)newValue.getToggleGroup().getSelectedToggle();
					String theSelectedValue = selectedRadio.getText().trim();
					String plainFilePath = filePath.substring(0, filePath.lastIndexOf("."));
					fileChooser.getExtensionFilters().clear();
					if(theSelectedValue.equalsIgnoreCase("econcept"))
					{
						fileChooser.getExtensionFilters().addAll(new ExtensionFilter(ExportFileExtensionEnum.Econcept.extensionDesc, ExportFileExtensionEnum.Econcept.extensionFormat));
						fileName = ExportFileExtensionEnum.Econcept.name + "_Export";
						fileExtension =	ExportFileExtensionEnum.Econcept.extensionFormat;
						fileChooser.setInitialFileName(fileName);
						filePath = plainFilePath +  ExportFileExtensionEnum.Econcept.extensionFormat;
						file = new File(filePath);
						outputField.clear();
						outputField.setText(filePath);
						
						toggleSelected = ExportFileExtensionEnum.Econcept.name;
						logger_.info("Export File Type Changed to EConcept");
					} 
					else if(theSelectedValue.equalsIgnoreCase("changeset")) 
					{
						fileChooser.getExtensionFilters().addAll(new ExtensionFilter(ExportFileExtensionEnum.Changeset.extensionDesc, ExportFileExtensionEnum.Changeset.extensionFormat));
						fileName = ExportFileExtensionEnum.Changeset.name + "_Export";
						fileExtension =	ExportFileExtensionEnum.Changeset.extensionFormat;
						fileChooser.setInitialFileName(fileName);
						filePath = plainFilePath +  ExportFileExtensionEnum.Changeset.extensionFormat;
						file = new File(filePath);
						outputField.clear();
						outputField.setText(filePath);
						toggleSelected = ExportFileExtensionEnum.Changeset.name;
						logger_.info("Export File Type Changed to Changeset");
					}
					else if(theSelectedValue.equalsIgnoreCase("xml")) 
					{
						fileChooser.getExtensionFilters().addAll(new ExtensionFilter(ExportFileExtensionEnum.Xml.extensionDesc, ExportFileExtensionEnum.Xml.extensionFormat));
						fileName = ExportFileExtensionEnum.Xml.name + "_Export";
						fileExtension =	ExportFileExtensionEnum.Xml.extensionFormat;
						fileChooser.setInitialFileName(fileName);
						filePath = plainFilePath +  ExportFileExtensionEnum.Xml.extensionFormat;
						file = new File(filePath);
						outputField.clear();
						outputField.setText(filePath);
						toggleSelected = ExportFileExtensionEnum.Xml.name;
						logger_.info("Export File Type Changed to Changeset");
					}
					
					
					if(file == null) {
						logger_.error("Could not create a file object, this is fatal");
					}
				}
			}
		});
		
		super.root_ = root;
	}
	
	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#getTitle()
	 */
	@Override
	public String getTitle()
	{
		return "EConcept / Changeset Export";
	}

	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#conceptListChanged()
	 */
	@Override
	protected void conceptListChanged()
	{
		//TODO: DO we want to also populate this list with exported concepts ?
	}

	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#isValid()
	 */
	@Override
	public BooleanExpression isValid()
	{
		return allFieldsValid;
	}

	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#getOperationDescription()
	 */
	@Override
	public String getOperationDescription()
	{
		return "Performa	EConcept or Changeset Content Export to an Excel File";
	}
		
	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#createTask()
	 */
	@Override
	public CustomTask<OperationResult> createTask()
	{
		return new CustomTask<OperationResult>(EconceptExportOperation.this)
		{
			@Override
			protected OperationResult call() throws Exception
			{
				if(file != null) 
				{
					double i = 0;
					boolean firstValue = true;
					dos_ = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
					for (SimpleDisplayConcept concept : conceptList_)
					{
						try
						{
							logger_.info("Exporting the database " + file.getAbsolutePath());
							
							if (cancelRequested_)
							{
								return new OperationResult(EconceptExportOperation.this.getTitle(), cancelRequested_);
							}
							updateProgress(i, conceptList_.size());
							if (i % 100 == 0) {
								updateMessage("Creating Data Ouput Stream for Export: " + concept.getDescription());
							}
							
							new TtkConceptChronicle(OTFUtility.getConceptVersion(concept.getNid())).writeExternal(dos_);
							
							if(toggleSelected.equalsIgnoreCase("changeset")) {
								new TtkConceptChronicle(OTFUtility.getConceptVersion(concept.getNid())).writeExternal(dos_);
								dos_.writeLong(System.currentTimeMillis());
							} else if(toggleSelected.equalsIgnoreCase("econcept")) {
								new TtkConceptChronicle(OTFUtility.getConceptVersion(concept.getNid())).writeExternal(dos_);
							} else {
								//new TtkConceptChronicle(OTFUtility.getConceptVersion(concept.getNid())).toXml(firstValue);
								if(firstValue) {
									firstValue = false;
								}
							}

							if (cancelRequested_) {
								return new OperationResult(  EconceptExportOperation.this.getTitle(), cancelRequested_);
							}
							if (i % 100 == 0) {
								updateMessage("Exporting Database to EConcepts " + concept.getDescription());
							}

							if (cancelRequested_) {
								return new OperationResult(  EconceptExportOperation.this.getTitle(), cancelRequested_);
							}
							updateProgress(++i, conceptList_.size());
						} catch (Exception e) {
							logger_.error(e.getMessage());
							e.printStackTrace();
						}
					}
					if (dos_ != null) {
						dos_.close();
					}
				} else {
					throw new Exception( "File Location path was not set correctly");
				}
				return new OperationResult("The export was completed succesfully", new HashSet<SimpleDisplayConcept>(), "The USCRS content request was succesfully generated");
			}
		};
	}
}
