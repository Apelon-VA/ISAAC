package gov.va.isaac.gui.mapping;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.ConceptNode;
import gov.va.isaac.gui.mapping.data.MappingItem;
import gov.va.isaac.gui.mapping.data.MappingItemDAO;
import gov.va.isaac.gui.mapping.data.MappingSet;
import gov.va.isaac.util.OTFUtility;
import gov.va.isaac.util.TaskCompleteCallback;
import java.util.UUID;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import gov.va.isaac.gui.IndexStatusListener;


/**
 * Controller class for the Create Mapping View.
 *
 * @author dtriglianos
 * @author <a href="mailto:dtriglianos@apelon.com">David Triglianos</a>
 */

public class CreateMappingItemController implements TaskCompleteCallback  {
	private static final Logger LOG = LoggerFactory.getLogger(CreateMappingItemController.class);

    @FXML private BorderPane 		mainPane;
    @FXML private Label				titleLabel;
    @FXML private GridPane			mainGridPane;
    
    @FXML private TextField 		criteriaText;
    @FXML private Button 			searchButton;

    @FXML private TableView<?> 		candidatesTableView;
    @FXML private TableColumn<?, ?> candidatesConceptColumn;
    @FXML private TableColumn<?, ?> candidatesCodeSystemColumn;
    @FXML private TableColumn<?, ?> candidatesStatusColumn;

    @FXML private ComboBox<?> 		codeSystemRestrictionCombo;
    @FXML private ComboBox<?> 		refsetRestrictionCombo;
    
    @FXML private RadioButton 		noRestrictionRadio;
    @FXML private RadioButton 		descriptionRestrictionRadio;
    @FXML private RadioButton 		synonymRestrictionRadio;
    @FXML private RadioButton 		fsnRestrictionRadio;
    
    @FXML private ComboBox<?> 		childRestrictionCombo;
    @FXML private ComboBox<?> 		descriptionRestrictionCombo;
    @FXML private ComboBox<?>		statusCombo;
    @FXML private ToggleGroup 		desc;    

    @FXML private Button 			clearRestrictionButton;
    @FXML private Button 			saveButton;
    @FXML private Button 			cancelButton;

	private ConceptNode 			sourceConceptNode = new ConceptNode(null, true);
	private ConceptNode				targetConceptNode = new ConceptNode(null, true);
	private ConceptNode				qualifierConceptNode = new ConceptNode(null, false);

	private MappingSet mappingSet_;

	public Region getRootNode() {
		//return region;
		return mainPane;
	}
	
	public StringProperty getTitle() {
		return titleLabel.textProperty();
	}
	
	@FXML
	public void initialize() {

	    assert mainPane                    != null: "fx:id=\"mainPane\" was not injected. Check 'CreateMapping.fxml' file.";
	    assert mainGridPane                != null: "fx:id=\"mainGridPane\" was not injected. Check 'CreateMapping.fxml' file.";
	    assert criteriaText                != null: "fx:id=\"criteriaText\" was not injected. Check 'CreateMapping.fxml' file.";
	    assert searchButton                != null: "fx:id=\"searchButton\" was not injected. Check 'CreateMapping.fxml' file.";
	    assert candidatesTableView         != null: "fx:id=\"candidatesTableView\" was not injected. Check 'CreateMapping.fxml' file.";
	    assert candidatesConceptColumn     != null: "fx:id=\"candidatesConceptColumn\" was not injected. Check 'CreateMapping.fxml' file.";
	    assert candidatesCodeSystemColumn  != null: "fx:id=\"candidatesCodeSystemColumn\" was not injected. Check 'CreateMapping.fxml' file.";
	    assert candidatesStatusColumn      != null: "fx:id=\"candidatesStatusColumn\" was not injected. Check 'CreateMapping.fxml' file.";
	    assert codeSystemRestrictionCombo  != null: "fx:id=\"codeSystemRestrictionCombo\" was not injected. Check 'CreateMapping.fxml' file.";
	    assert refsetRestrictionCombo      != null: "fx:id=\"refsetRestrictionCombo\" was not injected. Check 'CreateMapping.fxml' file.";
	    assert noRestrictionRadio          != null: "fx:id=\"noRestrictionRadio\" was not injected. Check 'CreateMapping.fxml' file.";
	    assert descriptionRestrictionRadio != null: "fx:id=\"descriptionRestrictionRadio\" was not injected. Check 'CreateMapping.fxml' file.";
	    assert synonymRestrictionRadio     != null: "fx:id=\"synonymRestrictionRadio\" was not injected. Check 'CreateMapping.fxml' file.";
	    assert fsnRestrictionRadio         != null: "fx:id=\"fsnRestrictionRadio\" was not injected. Check 'CreateMapping.fxml' file.";
	    assert childRestrictionCombo       != null: "fx:id=\"childRestrictionCombo\" was not injected. Check 'CreateMapping.fxml' file.";
	    assert descriptionRestrictionCombo != null: "fx:id=\"descriptionRestrictionCombo\" was not injected. Check 'CreateMapping.fxml' file.";
	    assert statusCombo          	   != null: "fx:id=\"statusCombo\" was not injected. Check 'CreateMapping.fxml' file.";
	    assert clearRestrictionButton      != null: "fx:id=\"clearRestrictionButton\" was not injected. Check 'CreateMapping.fxml' file.";
	    assert saveButton                  != null: "fx:id=\"saveButton\" was not injected. Check 'CreateMapping.fxml' file.";
	    assert cancelButton                != null: "fx:id=\"cancelButton\" was not injected. Check 'CreateMapping.fxml' file.";

	    mainGridPane.add(sourceConceptNode.getNode(), 1, 0);
	    mainGridPane.add(targetConceptNode.getNode(), 1, 4);
	    mainGridPane.add(qualifierConceptNode.getNode(), 1, 5);
	    //TODO status
	    
		saveButton.setDefaultButton(true);
		saveButton.setOnAction((event) -> {
			MappingItem mi = null;
			try	{
				ConceptVersionBI sourceConcept = sourceConceptNode.getConcept();
				ConceptVersionBI targetConcept = targetConceptNode.getConcept();
				
				if (sourceConcept == null || targetConcept == null) {
					AppContext.getCommonDialogs().showInformationDialog("Cannot Create Mapping Item", "Source and Target Concepts must be specified.");
				} else {
					UUID qualifierUUID = (qualifierConceptNode.getConcept() == null)? null : qualifierConceptNode.getConcept().getPrimordialUuid();
					UUID statusUUID = null; // TODO status UUID.fromString("d481125e-b8ca-537c-b688-d09d626e5ff9")
					
					mi = MappingItemDAO.createMappingItem(sourceConcept.getPrimordialUuid(), 
														  mappingSet_.getPrimordialUUID(), 
														  targetConcept.getPrimordialUuid(),
														  qualifierUUID,
														  statusUUID);
				}
			} catch (Exception e)	{
				AppContext.getCommonDialogs().showInformationDialog("Cannot Create Mapping Item", e.getMessage());
			}
			
			if (mi != null) {
				saveButton.getScene().getWindow().hide();
				AppContext.getService(Mapping.class).refreshMappingItems();
			} else {
				saveButton.getScene().getWindow().requestFocus();
			}
		});
		
		cancelButton.setCancelButton(true);
		cancelButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) { 
				mappingSet_ = null;
				cancelButton.getScene().getWindow().hide();
			}
		});
		cancelButton.setOnKeyPressed(new EventHandler<KeyEvent>()  {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.ENTER) {
					event.consume();
					cancelButton.fire();
				}
			}
		});
	    
	    
	}
    
	@Override
    public void taskComplete(long taskStartTime, Integer taskId) {
	    // TODO Auto-generated method stub
		// No idea what this needs to be - DT
    }
	
	public void setMappingSet(MappingSet mappingSet) {
		mappingSet_ = mappingSet;
	}
	
	public void setSourceConcept(UUID sourceConceptID) {
		//TODO set source concept node
		ConceptVersionBI sourceConcept = OTFUtility.getConceptVersion(sourceConceptID);
		if (sourceConcept != null) {
			sourceConceptNode.set(sourceConcept);
		}
	}
}
