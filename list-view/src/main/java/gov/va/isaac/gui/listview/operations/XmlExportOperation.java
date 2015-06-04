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

import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.gui.util.ErrorMarkerUtils;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.ExportTaskHandlerI;
import gov.va.isaac.request.uscrs.UscrsContentRequestHandler;
import gov.va.isaac.util.ValidBooleanBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.IntStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
* {@link EConceptExportOperation}
* 
* 
* @author <a href="mailto:vkaloidis@apelon.com">Vas Kaloidis</a>
*/
@Service
@PerLookup
public class XmlExportOperation extends Operation  implements ExportTaskHandlerI
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
	
	private XmlExportOperation()
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
	
	
	@Override
	public void init(ObservableList<SimpleDisplayConcept> conceptList)
	{
		root.setHgap(10); 
		root.setVgap(10);
		
		super.init(conceptList);
		
		String date = new SimpleDateFormat("MM-dd-yyyy").format(new Date());
		fileName = "VA_XML_Export_" + date;

		//this.chooseFileName(); TODO: Finish the file name system
		fileChooser.setTitle("Save XML Export");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("XML Files .xml", "*.xml"));
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
		return "XML Export Operation";
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
		return "Perform a XML Export on Selected Concepts";
	}

	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#createTask()
	 */
	@Override
	public CustomTask<OperationResult> createTask()
	{
		return new CustomTask<OperationResult>(XmlExportOperation.this)
		{
			@Override
			protected OperationResult call() throws Exception
			{
				
				StringBuffer sb = new StringBuffer();
				
				sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				
				IntStream nidStream = conceptList_.stream().mapToInt(c -> c.getNid());
				
				updateProgress(0, 3);
				updateMessage("Generating XML File");
				if (cancelRequested_)
				{
					return new OperationResult(XmlExportOperation.this.getTitle(), cancelRequested_);
				}
				
				if(file != null) {
					
					DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			 
					// root elements
					Document doc = docBuilder.newDocument();
					Element rootElement = doc.createElement("concepts");
					doc.appendChild(rootElement);
					
					for (SimpleDisplayConcept concept : conceptList_) {
						// staff elements
						Element conceptNode = doc.createElement("concept");
						rootElement.appendChild(conceptNode);
						conceptNode.setAttribute("nid", String.valueOf(concept.getNid()));

						doc.createElement("description");
						conceptNode.appendChild(doc.createTextNode(concept.getDescription()));
						
						doc.createElement("hashNode");
						conceptNode.appendChild(doc.createTextNode(String.valueOf(concept.hashCode())));
					}
				
					TransformerFactory transformerFactory = TransformerFactory.newInstance();
					Transformer transformer = transformerFactory.newTransformer();
					DOMSource source = new DOMSource(doc);
					StreamResult result = new StreamResult(file);
					
					transformer.transform(source,result);
					
					StreamResult sysOut = new StreamResult(System.out);
				}
				updateProgress(1, 3);
				updateMessage("Genearating USCRS Content Request Handler");
				if (cancelRequested_)
				{
					return new OperationResult(XmlExportOperation.this.getTitle(), cancelRequested_);
				}
				
				updateProgress(3, 3);
				
				String operationSuccess = "XML Exportted Succesfully in ";
				return new OperationResult(operationSuccess + file.getPath(), new HashSet<SimpleDisplayConcept>(), operationSuccess);
			}
		};
	}

	@Override
    public void setOptions(Properties options) {
	    // TODO Auto-generated method stub
	    
    }

	@Override
    public String getDescription() {
	    // TODO Auto-generated method stub
	    return null;
    }

	@Override
    public Task<Integer> createTask(IntStream nidList, Path file) {
	    // TODO Auto-generated method stub
	    return null;
    }

}
