package gov.va.isaac.gui.mapping;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.gui.ConceptNode;
import gov.va.isaac.gui.ConfigureDynamicRefexIndexingView;
//import gov.va.isaac.gui.IndexStatusListener;
import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.gui.dragAndDrop.DragRegistry;
import gov.va.isaac.gui.dragAndDrop.SingleConceptIdProvider;
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
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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
 * Controller class for the Mapping View.
 *
 * @author dtriglianos
 * @author <a href="mailto:dtriglianos@apelon.com">David Triglianos</a>
 */

public class MappingController implements TaskCompleteCallback
{
	private static final Logger LOG = LoggerFactory.getLogger(MappingController.class);

    @FXML private AnchorPane	mainPane;
    @FXML private AnchorPane	mappingPane;
    @FXML private AnchorPane	listPane;
    @FXML private ToggleButton 	activeOnlyToggle;
    @FXML private ToggleButton 	plusMappingToggle;
    @FXML private ToggleButton 	minusMappingToggle;
    @FXML private Button 		editMappingButton;
	@FXML private Label			mappingSummaryLabel;
	@FXML private TableView		mappingTableView;
	@FXML private Label			listTitleLabel;
	@FXML private TableView		listTableView;
    @FXML private ToggleButton 	plusListToggle;
    @FXML private ToggleButton 	minusListToggle;
    @FXML private Button 		commentButton;
    @FXML private Label			listSummaryLabel;
    
	public static MappingController init() throws IOException
	{
		// Load from FXML.
		URL resource = MappingController.class.getResource("Mapping.fxml");
		FXMLLoader loader = new FXMLLoader(resource);
		loader.load();
		return loader.getController();
	}

	@FXML
	public void initialize()
	{
		assert mainPane 			!= null : "fx:id=\"mainPane\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert mappingPane 			!= null : "fx:id=\"mappingPane\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert listPane 			!= null : "fx:id=\"listPane\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert activeOnlyToggle 	!= null : "fx:id=\"activeOnlyToggle\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert plusMappingToggle 	!= null : "fx:id=\"plusMappingToggle\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert minusMappingToggle	!= null : "fx:id=\"minusMappingToggle\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert editMappingButton 	!= null : "fx:id=\"editMappingButton\" was not injected: check your FXML file 'Mapping.fxml'.";

		assert mappingSummaryLabel 	!= null : "fx:id=\"mappingSummaryLabel\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert mappingTableView 	!= null : "fx:id=\"xx\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert listTitleLabel 		!= null : "fx:id=\"xx\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert listTableView 		!= null : "fx:id=\"xx\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert plusListToggle 		!= null : "fx:id=\"xx\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert minusListToggle 		!= null : "fx:id=\"xx\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert commentButton 		!= null : "fx:id=\"xx\" was not injected: check your FXML file 'Mapping.fxml'.";
		assert listSummaryLabel 	!= null : "fx:id=\"xx\" was not injected: check your FXML file 'Mapping.fxml'.";

		
		mainPane.getStylesheets().add(MappingController.class.getResource("/isaac-shared-styles.css").toString());
		
		assignImageToButton(activeOnlyToggle, 	Images.FILTER_16.createImageView(), "Show Active Only / Show All");
		
		assignImageToButton(plusMappingToggle, 	Images.PLUS.createImageView(), "Show Active Only / Show All");
		assignImageToButton(minusMappingToggle, Images.MINUS.createImageView(), "Show Active Only / Show All");
		assignImageToButton(editMappingButton, 	Images.EDIT.createImageView(), "Show Active Only / Show All");
		assignImageToButton(plusListToggle, 	Images.PLUS.createImageView(), "Show Active Only / Show All");
		assignImageToButton(minusListToggle, 	Images.MINUS.createImageView(), "Show Active Only / Show All");
		assignImageToButton(commentButton, 		Images.BALLOON.createImageView(), "Show Active Only / Show All");
		
		activeOnlyToggle.setSelected(true);
        

	}

	public AnchorPane getRoot()
	{
		return mainPane;
	}

	@Override
	public void taskComplete(long taskStartTime, Integer taskId)
	{

		// Run on JavaFX thread.
		Platform.runLater(() -> 
		{
			try
			{
			}
			catch (Exception ex)
			{
				String title = "Unexpected Search Error";
				LOG.error(title, ex);
				AppContext.getCommonDialogs().showErrorDialog(title, "There was an unexpected error", ex.toString());
			}
			finally
			{
			}
		});
	}

	private void assignImageToButton(ButtonBase button, ImageView imageView, String tooltip) {
        button.setText("");
        button.setGraphic(imageView);
        button.setTooltip(new Tooltip(tooltip));

	}

}
