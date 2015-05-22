package gov.va.isaac.gui.listview.operations;

/*
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

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.gui.util.ErrorMarkerUtils;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.gui.constants.SharedServiceNames;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.ExportTaskHandlerI;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.ValidBooleanBinding;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import org.glassfish.hk2.api.PerLookup;
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
@PerLookup
public class UscrsExportOperation extends Operation
{
	protected final ObjectProperty<File> fileProperty = new SimpleObjectProperty<>();
	protected final ObjectProperty<TextField> outputProperty = new SimpleObjectProperty<>();
	
	private Logger logger_ = LoggerFactory.getLogger(this.getClass());
	
	private Map<String, Set<String>> successCons = new HashMap<>();
	
	private final FileChooser fileChooser = new FileChooser();
	private Button openFileChooser = new Button(null, Images.FOLDER.createImageView());
	private GridPane root = new GridPane();
	public File file = null;
	private String filePath = "";
	private TextField outputField = new TextField();
	
	private ValidBooleanBinding allFieldsValid;
//	private DataOutputStream dos_;
	public String fileName = "";
	
	private UscrsExportOperation()
	{
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
	
	public void chooseFileName() {
		String date = new SimpleDateFormat("MM-dd-yyyy").format(new Date());
		if(conceptList_.size() > 1) {
			fileName = "VA_USCRS_Submission_File_Multiple_Concepts_" + date + "";
		} else if(conceptList_.size() == 1) {
			String singleConceptName = conceptList_.get(0).getDescription();
			fileName = "VA_USCRS_Submission_File_" + singleConceptName  + "_" + date; 
		} else {
			fileName = "VA_USCRS_Submission_File_" + date;
		}
	}
	
	@Override
	public void init(ObservableList<SimpleDisplayConcept> conceptList)
	{
		root.setHgap(10); 
		root.setVgap(10);
		
		super.init(conceptList);
		
		String date = new SimpleDateFormat("MM-dd-yyyy").format(new Date());
		fileName = "VA_USCRS_Submission_File_" + date;

		//this.chooseFileName(); TODO: Finish the file name system
		fileChooser.setTitle("Save USCRS Concept Request File");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Excel Files .xls .xlsx", "*.xls", "*.xlsx"));
		fileChooser.setInitialFileName(fileName);
//		outputProperty.set(outputField.textProperty());
		
		openFileChooser.setOnAction(
			new EventHandler<ActionEvent>() {
				@Override
				public void handle(final ActionEvent e) {
					file = fileChooser.showSaveDialog(null);
					if(file != null)
					{
						if(file.getAbsolutePath() != null && file.getAbsolutePath() != "")
						{
							outputField.setText(file.getAbsolutePath());
							filePath = file.getAbsolutePath();
							logger_.info("File Path Changed: " + filePath);
						} 
					}
				}
			});
		outputField.setOnAction( //TODO: So you can manually type in a file path and it validates (break this out)
			new EventHandler<ActionEvent>() {
				@Override
				public void handle(final ActionEvent e) {
					filePath = outputField.getText();
					file = new File(filePath);
				}
			});
		
		allFieldsValid = new ValidBooleanBinding() {
			{
				bind(outputField.textProperty());
				setComputeOnInvalidate(true);
			}
			/* (non-Javadoc)
			 * @see javafx.beans.binding.BooleanBinding#computeValue()
			 */
			@Override
			protected boolean computeValue()
			{
				if(outputField.getText() != "" || !outputField.getText().trim().isEmpty()) 
				{
					String fieldOutput = outputField.getText();
					if(filePath != null && !fieldOutput.isEmpty() && file != null) //fieldOutput is repetetive but necessary
					{
						int lastSeperatorPosition = outputField.getText().lastIndexOf(File.separator);
						String path = "";
						if(lastSeperatorPosition > 0) {
							path = outputField.getText().substring(0, lastSeperatorPosition);
						} else {
							path = outputField.getText();
						}
						
						logger_.debug("Output Directory: " + path);
						File f = new File(path);
						if(file.isFile()) { //If we want to prevent file overwrite
							this.setInvalidReason("The file " + filePath + " already exists");
							return false;
						} else if(f.isDirectory()) {
							return true;
						} else { 
							this.setInvalidReason("Output Path is not a directory");
							return false;
						}
					} else {
						this.setInvalidReason("File Output Directory is not set - output field null!!");
						return false;
					}
				}
				else
				{
					this.setInvalidReason("Output field is empty, output directory is not set");
					return false;
				} 
			}
		};

		root.add(openFileChooser, 2, 0); //Path Label - Row 2
		GridPane.setHalignment(openFileChooser, HPos.LEFT);
		
		Label outputLocationLabel = new Label("Output Location");
		root.add(outputLocationLabel, 0, 0);
		GridPane.setHalignment(outputLocationLabel, HPos.LEFT);
		
		StackPane sp = ErrorMarkerUtils.setupErrorMarker(outputField, null, allFieldsValid);
		root.add(sp, 1, 0);
		GridPane.setHalignment(sp, HPos.LEFT);
		
		super.root_ = root;
	}
	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#getTitle()
	 */
	@Override
	public String getTitle()
	{
		return "USCRS Content Request Export";
	}

	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#conceptListChanged()
	 */
	@Override
	protected void conceptListChanged()
	{
		//noop
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
		return "Performa  USCRS Content Export to an Excel File";
	}

	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#createTask()
	 */
	@Override
	public CustomTask<OperationResult> createTask()
	{
		return new CustomTask<OperationResult>(UscrsExportOperation.this)
		{
			@Override
			protected OperationResult call() throws Exception
			{
				IntStream nidStream = conceptList_.stream().mapToInt(c -> c.getNid());
				
				ExportTaskHandlerI uscrsExporter = AppContext.getService(ExportTaskHandlerI.class, SharedServiceNames.USCRS);
				int count = 0;
				if(uscrsExporter != null) {
					Task<Integer> task = uscrsExporter.createTask(nidStream, file.toPath());
					Utility.execute(task);
					count = task.get();
					//TODO: Fix all the customTask stuff here...
					return new OperationResult("The USCRS Content request was succesfully generated in: " + file.getPath(), new HashSet<SimpleDisplayConcept>(), "The concepts were succesfully exported");
				} else {
					throw new RuntimeException("The USCRS Content Request Handler is not available on the class path");
				}
			}
		};
	}


}
