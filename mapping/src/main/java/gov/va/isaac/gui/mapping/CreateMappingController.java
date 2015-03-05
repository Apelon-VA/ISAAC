package gov.va.isaac.gui.mapping;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.gui.ConceptNode;
import gov.va.isaac.gui.ConfigureDynamicRefexIndexingView;
//import gov.va.isaac.gui.IndexStatusListener;
import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.gui.dragAndDrop.DragRegistry;
import gov.va.isaac.gui.dragAndDrop.SingleConceptIdProvider;
import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.search.CompositeSearchResult;
import gov.va.isaac.search.SearchHandle;
import gov.va.isaac.search.SearchHandler;
import gov.va.isaac.util.CommonMenuBuilderI;
import gov.va.isaac.util.CommonMenus;
import gov.va.isaac.util.CommonMenusDataProvider;
import gov.va.isaac.util.CommonMenusNIdProvider;
import gov.va.isaac.util.Interval;
import gov.va.isaac.util.NumberUtilities;
import gov.va.isaac.util.TaskCompleteCallback;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.ValidBooleanBinding;
import gov.va.isaac.util.OTFUtility;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.image.ImageView;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.util.Callback;
import javafx.util.StringConverter;

import org.ihtsdo.otf.query.lucene.LuceneDynamicRefexIndexer;
import org.ihtsdo.otf.query.lucene.LuceneDynamicRefexIndexerConfiguration;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.RefexDynamic;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicUsageDescription;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicString;
import org.ihtsdo.otf.tcc.model.index.service.IndexerBI;
import org.ihtsdo.otf.tcc.model.index.service.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.javafx.collections.ObservableListWrapper;


/**
 * Controller class for the Create Mapping View.
 *
 * @author dtriglianos
 * @author <a href="mailto:dtriglianos@apelon.com">David Triglianos</a>
 */

public class CreateMappingController {
	private static final Logger LOG = LoggerFactory.getLogger(CreateMappingController.class);

    @FXML private BorderPane 		mainPane;

    @FXML private TextField 		criteriaText;
    @FXML private Button 			searchButton;

    @FXML private TableView<?> 		candidatesTableView;
    @FXML private TableColumn<?, ?> candidatesConceptColumn;
    @FXML private TableColumn<?, ?> candidatesCodeSystemColumn;
    @FXML private TableColumn<?, ?> candidatesStatusColumn;
    @FXML private Label 			targetConceptLabel;

    @FXML private ComboBox<?> 		codeSystemRestrictionCombo;
    @FXML private ComboBox<?> 		refsetRestrictionCombo;
    
    @FXML private RadioButton 		noRestrictionRadio;
    @FXML private RadioButton 		descriptionRestrictionRadio;
    @FXML private RadioButton 		synonymRestrictionRadio;
    @FXML private RadioButton 		fsnRestrictionRadio;
    
    @FXML private ComboBox<?> 		childRestrictionCombo;
    @FXML private ComboBox<?> 		descriptionRestrictionCombo;
    @FXML private ComboBox<?> 		sourceConceptCombo;
    @FXML private ToggleGroup 		desc;    

    @FXML private Button 			clearRestrictionButton;
    @FXML private Button 			saveButton;
    @FXML private Button 			cancelButton;


	@FXML
	public void initialize() {

	    assert mainPane                    != null: "fx:id=\"mainPane\" was not injected. Check 'CreateMapping.fxml' file.";
	    assert criteriaText                != null: "fx:id=\"criteriaText\" was not injected. Check 'CreateMapping.fxml' file.";
	    assert searchButton                != null: "fx:id=\"searchButton\" was not injected. Check 'CreateMapping.fxml' file.";
	    assert candidatesTableView         != null: "fx:id=\"candidatesTableView\" was not injected. Check 'CreateMapping.fxml' file.";
	    assert candidatesConceptColumn     != null: "fx:id=\"candidatesConceptColumn\" was not injected. Check 'CreateMapping.fxml' file.";
	    assert candidatesCodeSystemColumn  != null: "fx:id=\"candidatesCodeSystemColumn\" was not injected. Check 'CreateMapping.fxml' file.";
	    assert candidatesStatusColumn      != null: "fx:id=\"candidatesStatusColumn\" was not injected. Check 'CreateMapping.fxml' file.";
	    assert targetConceptLabel          != null: "fx:id=\"targetConceptLabel\" was not injected. Check 'CreateMapping.fxml' file.";
	    assert codeSystemRestrictionCombo  != null: "fx:id=\"codeSystemRestrictionCombo\" was not injected. Check 'CreateMapping.fxml' file.";
	    assert refsetRestrictionCombo      != null: "fx:id=\"refsetRestrictionCombo\" was not injected. Check 'CreateMapping.fxml' file.";
	    assert noRestrictionRadio          != null: "fx:id=\"noRestrictionRadio\" was not injected. Check 'CreateMapping.fxml' file.";
	    assert descriptionRestrictionRadio != null: "fx:id=\"descriptionRestrictionRadio\" was not injected. Check 'CreateMapping.fxml' file.";
	    assert synonymRestrictionRadio     != null: "fx:id=\"synonymRestrictionRadio\" was not injected. Check 'CreateMapping.fxml' file.";
	    assert fsnRestrictionRadio         != null: "fx:id=\"fsnRestrictionRadio\" was not injected. Check 'CreateMapping.fxml' file.";
	    assert childRestrictionCombo       != null: "fx:id=\"childRestrictionCombo\" was not injected. Check 'CreateMapping.fxml' file.";
	    assert descriptionRestrictionCombo != null: "fx:id=\"descriptionRestrictionCombo\" was not injected. Check 'CreateMapping.fxml' file.";
	    assert sourceConceptCombo          != null: "fx:id=\"sourceConceptCombo\" was not injected. Check 'CreateMapping.fxml' file.";
	    assert clearRestrictionButton      != null: "fx:id=\"clearRestrictionButton\" was not injected. Check 'CreateMapping.fxml' file.";
	    assert saveButton                  != null: "fx:id=\"saveButton\" was not injected. Check 'CreateMapping.fxml' file.";
	    assert cancelButton                != null: "fx:id=\"cancelButton\" was not injected. Check 'CreateMapping.fxml' file.";

	}
    
}
