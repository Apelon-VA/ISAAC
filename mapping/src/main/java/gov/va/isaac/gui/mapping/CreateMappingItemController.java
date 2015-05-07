package gov.va.isaac.gui.mapping;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.gui.ConceptNode;
import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.gui.dragAndDrop.DragRegistry;
import gov.va.isaac.gui.dragAndDrop.SingleConceptIdProvider;
import gov.va.isaac.gui.mapping.data.MappingItem;
import gov.va.isaac.gui.mapping.data.MappingItemDAO;
import gov.va.isaac.gui.mapping.data.MappingSet;
import gov.va.isaac.gui.mapping.data.MappingUtils;
import gov.va.isaac.gui.util.CustomClipboard;
import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.refexDynamic.RefexDynamicUtil;
import gov.va.isaac.search.CompositeSearchResult;
import gov.va.isaac.search.SearchHandle;
import gov.va.isaac.util.CommonMenus;
import gov.va.isaac.util.CommonMenusNIdProvider;
import gov.va.isaac.util.OTFUtility;
import gov.va.isaac.util.Utility;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;

import org.ihtsdo.otf.query.lucene.LuceneDescriptionType;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller class for the Create Mapping View.
 *
 * @author dtriglianos
 * @author <a href="mailto:dtriglianos@apelon.com">David Triglianos</a>
 */

public class CreateMappingItemController {
	private static final Logger LOG = LoggerFactory.getLogger(CreateMappingItemController.class);
    
	private class SearchResultConcept {
		private ConceptVersionBI concept;
		private final SimpleStringProperty conceptNameProperty		= new SimpleStringProperty("-");
		private final SimpleStringProperty codeSystemNameProperty	= new SimpleStringProperty("-");
		private final SimpleStringProperty statusNameProperty		= new SimpleStringProperty("-");
		public SearchResultConcept(ConceptVersionBI concept) {
			this.concept = concept;
			try {
				statusNameProperty.set(concept.isActive()? "Active":"Inactive");	
			} catch (IOException e) {
				statusNameProperty.set("-error-");
				LOG.error("Error fetching isActive", e);
			}
			Utility.execute(() ->
			{
				String conceptName 	= OTFUtility.getDescription(concept);
				String pathName 	= OTFUtility.getDescription(concept.getPathNid());
				Platform.runLater(() -> {
					conceptNameProperty.set(conceptName);
					codeSystemNameProperty.set(pathName);
				});
			});
		}
		public ConceptVersionBI		getConcept()					{ return concept; }
		public SimpleStringProperty getConceptNameProperty() 		{ return conceptNameProperty; }
		public SimpleStringProperty getCodeSystemNameProperty() 	{ return codeSystemNameProperty; }
		public SimpleStringProperty getStatusNameProperty() 		{ return statusNameProperty; }
		
	}
	
    private static class SearchRestriction {
    	private LuceneDescriptionType descriptionType = null;
		private SimpleDisplayConcept advancedDescriptionType = null; 
		private SimpleDisplayConcept targetCodeSystemPath = null;
		private SimpleDisplayConcept memberOfRefset = null;
		private ConceptVersionBI     kindOf  = null;
		
		public LuceneDescriptionType getDescriptionType()         { return descriptionType;	        }
		public SimpleDisplayConcept  getAdvancedDescriptionType() { return advancedDescriptionType; }
		public SimpleDisplayConcept  getTargetCodeSystemPath()	  { return targetCodeSystemPath;    }
		public SimpleDisplayConcept  getMemberOfRefset()          { return memberOfRefset;          }
		public ConceptVersionBI      getKindOf()                  { return kindOf;                 }
		
		public UUID getAdvancedDescriptionTypeUUID() throws IOException { 
			UUID uuid = null;
			if (advancedDescriptionType != null) {
				uuid = ExtendedAppContext.getDataStore().getUuidPrimordialForNid(advancedDescriptionType.getNid()); 
			}
			return uuid; 
		}
		
		public Integer getTargetCodeSystemPathNid() { 
			Integer nid = null;
			if (targetCodeSystemPath != null) {
				nid = new Integer(targetCodeSystemPath.getNid());
			}
			return nid;
		}
		
		public Integer getMemberOfRefsetNid() { 
			Integer nid = null;
			if (memberOfRefset != null) {
				nid = new Integer(memberOfRefset.getNid());
			}
			return nid;
		}
		
		public Integer getKindOfNid() { 
			Integer nid = null;
			if (kindOf != null) {
				nid = new Integer(kindOf.getNid());
			}
			return nid;
		}
		
		public void setDescriptionType(LuceneDescriptionType descriptionType)                { this.descriptionType = descriptionType; }
		public void setAdvancedDescriptionType(SimpleDisplayConcept advancedDescriptionType) { this.advancedDescriptionType = advancedDescriptionType; }
		public void setTargetCodeSystemPath(SimpleDisplayConcept targetCodeSystemPath)       { this.targetCodeSystemPath = targetCodeSystemPath; }
		public void setMemberOfRefset(SimpleDisplayConcept memberOfRefset)                   { this.memberOfRefset = memberOfRefset; }
		public void setKindOf(ConceptVersionBI kindOf)                                       { this.kindOf = kindOf; }
		
		public void clear() {
	    	descriptionType         = null;
			advancedDescriptionType = null; 
			targetCodeSystemPath    = null;
			memberOfRefset          = null;
			kindOf                  = null;
		}
    }

    private static final SearchRestriction searchRestriction = new SearchRestriction();
    
    private final Label LABEL_NO_RESULTS = new Label("Search returned no results");
	private final Label LABEL_SEARCHING  = new Label("Searching...");

	@FXML private BorderPane 		mainPane;
	@FXML private Label				titleLabel;
	@FXML private GridPane			mainGridPane;
	
	@FXML private TextField 		criteriaText;
	@FXML private Button 			searchButton;

	@FXML private TableView<SearchResultConcept> candidatesTableView;
	@FXML private TableColumn<SearchResultConcept, SearchResultConcept> candidatesConceptColumn;
	@FXML private TableColumn<SearchResultConcept, SearchResultConcept> candidatesCodeSystemColumn;
	@FXML private TableColumn<SearchResultConcept, SearchResultConcept> candidatesStatusColumn;

	@FXML private VBox				filterVBox;
    @FXML private ToggleGroup 		showFilterToggleGroup;
    @FXML private ToggleButton 		showFilterToggle;
	
	@FXML private VBox              kindOfRestrictionVBox;
	@FXML private RadioButton 		noRestrictionRadio;
	@FXML private RadioButton 		descriptionRestrictionRadio;
	@FXML private RadioButton 		synonymRestrictionRadio;
	@FXML private RadioButton 		fsnRestrictionRadio;
	@FXML private ToggleGroup 		desc;	
	
	@FXML private ComboBox<SimpleDisplayConcept> 	refsetRestrictionCombo;
	@FXML private ComboBox<SimpleDisplayConcept> 	codeSystemRestrictionCombo;
	@FXML private ComboBox<SimpleDisplayConcept> 	descriptionRestrictionCombo;
	@FXML private ComboBox<SimpleDisplayConcept>	statusCombo;
	@FXML private ComboBox<SimpleDisplayConcept>	qualifierCombo;

    @FXML private Button 			applyRestrictionButton;
	@FXML private Button 			clearRestrictionButton;
	@FXML private Button 			saveButton;
	@FXML private Button 			cancelButton;
	
	private ConceptNode 			sourceConceptNode = new ConceptNode(null, true);
	private ConceptNode				targetConceptNode = new ConceptNode(null, false);
	private ConceptNode				kindOfRestrictionConceptNode;

	private MappingSet mappingSet_;
	private Object searchObject_;
	private boolean restrictionsInitialized = false;
	
	public Region getRootNode() {
		return mainPane;
	}
	
	public StringProperty getTitle() {
		return titleLabel.textProperty();
	}
	
	@FXML
	public void initialize() {

		assert mainPane						!= null: "fx:id=\"mainPane\" was not injected. Check 'CreateMapping.fxml' file.";
		assert mainGridPane					!= null: "fx:id=\"mainGridPane\" was not injected. Check 'CreateMapping.fxml' file.";
		assert criteriaText					!= null: "fx:id=\"criteriaText\" was not injected. Check 'CreateMapping.fxml' file.";
		assert searchButton					!= null: "fx:id=\"searchButton\" was not injected. Check 'CreateMapping.fxml' file.";
		assert candidatesTableView			!= null: "fx:id=\"candidatesTableView\" was not injected. Check 'CreateMapping.fxml' file.";
		assert candidatesConceptColumn		!= null: "fx:id=\"candidatesConceptColumn\" was not injected. Check 'CreateMapping.fxml' file.";
		assert candidatesCodeSystemColumn	!= null: "fx:id=\"candidatesCodeSystemColumn\" was not injected. Check 'CreateMapping.fxml' file.";
		assert candidatesStatusColumn		!= null: "fx:id=\"candidatesStatusColumn\" was not injected. Check 'CreateMapping.fxml' file.";
		assert codeSystemRestrictionCombo	!= null: "fx:id=\"codeSystemRestrictionCombo\" was not injected. Check 'CreateMapping.fxml' file.";
		assert refsetRestrictionCombo		!= null: "fx:id=\"refsetRestrictionCombo\" was not injected. Check 'CreateMapping.fxml' file.";
		assert noRestrictionRadio			!= null: "fx:id=\"noRestrictionRadio\" was not injected. Check 'CreateMapping.fxml' file.";
		assert descriptionRestrictionRadio	!= null: "fx:id=\"descriptionRestrictionRadio\" was not injected. Check 'CreateMapping.fxml' file.";
		assert synonymRestrictionRadio		!= null: "fx:id=\"synonymRestrictionRadio\" was not injected. Check 'CreateMapping.fxml' file.";
		assert fsnRestrictionRadio			!= null: "fx:id=\"fsnRestrictionRadio\" was not injected. Check 'CreateMapping.fxml' file.";
		assert descriptionRestrictionCombo	!= null: "fx:id=\"descriptionRestrictionCombo\" was not injected. Check 'CreateMapping.fxml' file.";
		assert statusCombo					!= null: "fx:id=\"statusCombo\" was not injected. Check 'CreateMapping.fxml' file.";
		assert qualifierCombo				!= null: "fx:id=\"qualifierCombo\" was not injected. Check 'CreateMapping.fxml' file.";
		assert clearRestrictionButton		!= null: "fx:id=\"clearRestrictionButton\" was not injected. Check 'CreateMapping.fxml' file.";
		assert saveButton					!= null: "fx:id=\"saveButton\" was not injected. Check 'CreateMapping.fxml' file.";
		assert cancelButton					!= null: "fx:id=\"cancelButton\" was not injected. Check 'CreateMapping.fxml' file.";
        assert applyRestrictionButton 		!= null : "fx:id=\"applyRestrictionButton\" was not injected: check your FXML file 'CreateMappingItem.fxml'.";
        assert showFilterToggle 			!= null : "fx:id=\"showFilterToggle\" was not injected: check your FXML file 'CreateMappingItem.fxml'.";
        assert showFilterToggleGroup 		!= null : "fx:id=\"showFilterToggleGroup\" was not injected: check your FXML file 'CreateMappingItem.fxml'.";
        assert filterVBox 					!= null : "fx:id=\"filterVBox\" was not injected: check your FXML file 'CreateMappingItem.fxml'.";

		FxUtils.assignImageToButton(showFilterToggle, Images.FILTER_16.createImageView(), "Show/Hide Candidate Restrictions");

		setupColumnTypes();
		
		mainGridPane.add(sourceConceptNode.getNode(), 1, 0);
		mainGridPane.add(targetConceptNode.getNode(), 1, 4);
		
		statusCombo.setEditable(false);
		qualifierCombo.setEditable(false);
		descriptionRestrictionCombo.setEditable(false);
		codeSystemRestrictionCombo.setEditable(false);
		refsetRestrictionCombo.setEditable(false);
		
		showFilterToggle.setVisible(false);
		showFilterToggle.setDisable(true);
		/*
		 * Keep this around in case we want it later
		 *  
		showFilterToggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			@Override
			public void changed(ObservableValue<? extends Toggle> ov, Toggle toggle, Toggle new_toggle) {
				showFilterPane(showFilterToggle.isSelected());
			}
		});
		showFilterToggle.setSelected(false);
		showFilterPane(false);
		 */

		Utility.execute(() ->
		{
			try
			{
				List<SimpleDisplayConcept> qualifiers = MappingUtils.getQualifierConcepts();
				qualifiers.add(0, new SimpleDisplayConcept("No Qualifier", Integer.MIN_VALUE));
				
				List<SimpleDisplayConcept> status = MappingUtils.getStatusConcepts();
				status.add(0, new SimpleDisplayConcept("No Status", Integer.MIN_VALUE));
				
				List<SimpleDisplayConcept> advancedDescriptionTypes = MappingUtils.getExtendedDescriptionTypes();
				advancedDescriptionTypes.add(0, new SimpleDisplayConcept("No Restriction", Integer.MIN_VALUE));
				
				List<SimpleDisplayConcept> codeSystems = MappingUtils.getCodeSystems();
				codeSystems.add(0, new SimpleDisplayConcept("No Restriction", Integer.MIN_VALUE));

				List<SimpleDisplayConcept> refsetRestrictions = RefexDynamicUtil.getAllRefexDefinitions(); 
				refsetRestrictions.add(0, new SimpleDisplayConcept("No Restriction", Integer.MIN_VALUE));
				
				Platform.runLater(() ->
				{
					statusCombo.getItems().addAll(status);
					statusCombo.getSelectionModel().select(0);
					
					qualifierCombo.getItems().addAll(qualifiers);
					qualifierCombo.getSelectionModel().select(0);
					
					descriptionRestrictionCombo.getItems().addAll(advancedDescriptionTypes);
					descriptionRestrictionCombo.getSelectionModel().select(0);
					
					codeSystemRestrictionCombo.getItems().addAll(codeSystems);
					codeSystemRestrictionCombo.getSelectionModel().select(0);
					
					refsetRestrictionCombo.getItems().addAll(refsetRestrictions);
					refsetRestrictionCombo.getSelectionModel().select(0);
					
					paintSearchRestriction();

				});
			}
			catch (Exception e1)
			{
				LOG.error("Unexpected error populating combo fields", e1);
				AppContext.getCommonDialogs().showErrorDialog("Unexpected error populating combo fields.  See logs.", e1);
			}
		});

		saveButton.setDefaultButton(true);
		saveButton.setOnAction((event) -> {
			MappingItem mi = null;
			try	{
				ConceptVersionBI sourceConcept = sourceConceptNode.getConcept();
				ConceptVersionBI targetConcept = targetConceptNode.getConcept();
				
				//if (sourceConcept == null || targetConcept == null) {
				//	AppContext.getCommonDialogs().showInformationDialog("Cannot Create Mapping Item", "Source and Target Concepts must be specified.");
				if (sourceConcept == null) {
					AppContext.getCommonDialogs().showInformationDialog("Cannot Create Mapping Item", "Source Concept must be specified.");
				} else {
					UUID qualifierUUID = (qualifierCombo.getSelectionModel().getSelectedItem().getNid() == Integer.MIN_VALUE ? null : 
						ExtendedAppContext.getDataStore().getUuidPrimordialForNid(qualifierCombo.getSelectionModel().getSelectedItem().getNid()));
					UUID statusUUID = (statusCombo.getSelectionModel().getSelectedItem().getNid() == Integer.MIN_VALUE ? null : 
						ExtendedAppContext.getDataStore().getUuidPrimordialForNid(statusCombo.getSelectionModel().getSelectedItem().getNid()));
					
					mi = MappingItemDAO.createMappingItem(sourceConcept, 
														  mappingSet_.getPrimordialUUID(), 
														  targetConcept,
														  qualifierUUID,
														  statusUUID);
				}
			} catch (Exception e)	{
				LOG.error("unexpected", e);
				AppContext.getCommonDialogs().showInformationDialog("Cannot Create Mapping Item", e.getMessage());
			}
			
			if (mi != null) {
				saveButton.getScene().getWindow().hide();
				//TODO need a proper wait for index update here... runLater helps
				Platform.runLater(() -> AppContext.getService(Mapping.class).refreshMappingItems());
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
		
		sourceConceptNode.getConceptProperty().addListener(new ChangeListener<ConceptVersionBI>() {
			@Override
			public void changed(ObservableValue<? extends ConceptVersionBI> observable, ConceptVersionBI oldValue, ConceptVersionBI newValue) {
				if (newValue != null) {
					//criteriaText.setText(OTFUtility.getDescription(newValue));
					doSearch(newValue);
				}
			}
		});
		
		searchButton.setOnAction((event) -> {
			doSearch();
		});

		clearRestrictionButton.setOnAction((event) -> {
			clearSearchRestriction();
			doSearch();
		});
		
		applyRestrictionButton.setOnAction((event) -> {
			scrapeSearchRestriction();
			doSearch();
		});

		candidatesTableView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<SearchResultConcept>()
		{
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends SearchResultConcept> c)
			{
				SearchResultConcept srConcept = candidatesTableView.getSelectionModel().getSelectedItem(); 
				if (srConcept != null) {
					targetConceptNode.set(srConcept.getConcept());
				}
			}
		});

	}
	
	// intended to re-run last search when restrictions are applied
	private void doSearch() {
		String searchString = criteriaText.getText().trim();
		
		if (searchString != null && !searchString.equals("")) {
			doSearch(searchString);
		
		} else if (searchObject_ != null) {
			doSearch(searchObject_);
		}
	}
	
	private void doSearch(Object searchObject) {
		try {
			searchObject_ = searchObject;
			SearchHandle searchHandle = null;
			scrapeSearchRestriction();
			
			if (searchObject instanceof ConceptVersionBI) {
				searchHandle = MappingUtils.search(
						((ConceptVersionBI)searchObject).getNid(), 
						null, 
						searchRestriction.getDescriptionType(), 
						searchRestriction.getAdvancedDescriptionTypeUUID(), 
						searchRestriction.getTargetCodeSystemPathNid(), 
						searchRestriction.getMemberOfRefsetNid(), 
						searchRestriction.getKindOfNid()
				);
				
			} else if (searchObject instanceof String) {
				searchHandle = MappingUtils.search(
						(String)searchObject, 
						null, 
						searchRestriction.getDescriptionType(), 
						searchRestriction.getAdvancedDescriptionTypeUUID(), 
						searchRestriction.getTargetCodeSystemPathNid(), 
						searchRestriction.getMemberOfRefsetNid(), 
						searchRestriction.getKindOfNid()
				);
			}
			if (searchHandle != null) {
				populateSearchResult(searchHandle);	
			}
		} catch (IOException e) {
			LOG.error("Error performing search", e);
		}
	}
	
	public void setMappingSet(MappingSet mappingSet) {
		mappingSet_ = mappingSet;
	}
	
	public void setSourceConcept(UUID sourceConceptID) {
		ConceptVersionBI sourceConcept = OTFUtility.getConceptVersion(sourceConceptID);
		if (sourceConcept != null) {
			sourceConceptNode.set(sourceConcept);
		}
	}
	
	private void populateSearchResult(SearchHandle searchHandle) {
		candidatesTableView.getItems().clear();
		candidatesTableView.setPlaceholder(LABEL_SEARCHING);
		Utility.execute(() ->
		{
			ObservableList<SearchResultConcept> resultList = FXCollections.observableArrayList(); 
			try {
				Collection<CompositeSearchResult> results = searchHandle.getResults();	
				for (CompositeSearchResult result : results) {
					ConceptVersionBI concept = result.getContainingConcept();
					if (concept != null) {
						SearchResultConcept src = new SearchResultConcept(concept);
						resultList.add(src);
					}
				}
			} catch (Exception e) {
				LOG.error("Error populating search results", e);
				AppContext.getCommonDialogs().showErrorDialog("There was an error populating search results", e);
			}

			Platform.runLater(() -> {			
				if (resultList.size() == 0) {
					candidatesTableView.setPlaceholder(LABEL_NO_RESULTS);
				} else {
					candidatesTableView.getItems().addAll(resultList);	
				}
			});
		});
	}

	private Callback<TableColumn.CellDataFeatures<SearchResultConcept, SearchResultConcept>, ObservableValue<SearchResultConcept>> conceptCellValueFactory = 
			new Callback<TableColumn.CellDataFeatures<SearchResultConcept, SearchResultConcept>, ObservableValue<SearchResultConcept>>()	{
		@Override
		public ObservableValue<SearchResultConcept> call(CellDataFeatures<SearchResultConcept, SearchResultConcept> param) {
			return new SimpleObjectProperty<SearchResultConcept>(param.getValue());
		}
	};
	
	private Callback<TableColumn<SearchResultConcept, SearchResultConcept>, TableCell<SearchResultConcept, SearchResultConcept>> conceptCellFactory =
			new Callback<TableColumn<SearchResultConcept, SearchResultConcept>, TableCell<SearchResultConcept, SearchResultConcept>>() {

		@Override
		public TableCell<SearchResultConcept, SearchResultConcept> call(TableColumn<SearchResultConcept, SearchResultConcept> param) {
			return new TableCell<SearchResultConcept, SearchResultConcept>() {
				@Override
				public void updateItem(final SearchResultConcept concept, boolean empty) {
					super.updateItem(concept, empty);
					updateCell(this, concept);
				}
			};
		}
	};

	private void setupColumnTypes() {
		candidatesConceptColumn.setUserData(MappingColumnType.CONCEPT);
		candidatesCodeSystemColumn.setUserData(MappingColumnType.CODE_SYSTEM);
		candidatesStatusColumn.setUserData(MappingColumnType.STATUS_STRING);
		
		candidatesConceptColumn.setCellFactory(conceptCellFactory);
		candidatesCodeSystemColumn.setCellFactory(conceptCellFactory);
		candidatesStatusColumn.setCellFactory(conceptCellFactory);

		candidatesConceptColumn.setCellValueFactory(conceptCellValueFactory);
		candidatesCodeSystemColumn.setCellValueFactory(conceptCellValueFactory);
		candidatesStatusColumn.setCellValueFactory(conceptCellValueFactory);
		
		candidatesConceptColumn.prefWidthProperty().bind(candidatesTableView.widthProperty().multiply(0.6)); 
		candidatesCodeSystemColumn.prefWidthProperty().bind(candidatesTableView.widthProperty().multiply(0.2)); 
		candidatesStatusColumn.prefWidthProperty().bind(candidatesTableView.widthProperty().multiply(0.2)); 		

		candidatesConceptColumn.setComparator(new Comparator<SearchResultConcept>() {
			@Override
			public int compare(SearchResultConcept o1, SearchResultConcept o2) {
				return Utility.compareStringsIgnoreCase(o1.getConceptNameProperty().get(), o2.getConceptNameProperty().get());
			}
		});
		
		candidatesCodeSystemColumn.setComparator(new Comparator<SearchResultConcept>() {
			@Override
			public int compare(SearchResultConcept o1, SearchResultConcept o2) {
				return Utility.compareStringsIgnoreCase(o1.getCodeSystemNameProperty().get(), o2.getCodeSystemNameProperty().get());
			}
		});
		
		candidatesStatusColumn.setComparator(new Comparator<SearchResultConcept>() {
			@Override
			public int compare(SearchResultConcept o1, SearchResultConcept o2) {
				return Utility.compareStringsIgnoreCase(o1.getStatusNameProperty().get(), o2.getStatusNameProperty().get());
			}
		});
	}
	
	private void updateCell(TableCell<?, ?> cell, SearchResultConcept srConcept) {
		if (!cell.isEmpty() && srConcept != null) {
			ContextMenu cm = new ContextMenu();
			cell.setContextMenu(cm);
			SimpleStringProperty property = null;
			int conceptNid  = 0;
			MappingColumnType columnType = (MappingColumnType) cell.getTableColumn().getUserData();

			cell.setText(null);
			cell.setGraphic(null);

			switch (columnType) {
			case CONCEPT:
				property = srConcept.getConceptNameProperty();
				conceptNid = srConcept.getConcept().getNid();
				break;
				
			case CODE_SYSTEM:
				property = srConcept.getCodeSystemNameProperty();
				conceptNid = srConcept.getConcept().getPathNid();
				break;
				
			case STATUS_STRING:
				property = srConcept.getStatusNameProperty();
				break;
			default:
				// Nothing
			}
			
			if (property.get() != null) {
				Text text = new Text();
				text.textProperty().bind(property);
				text.wrappingWidthProperty().bind(cell.getTableColumn().widthProperty());
				cell.setGraphic(text);
	
				MenuItem mi = new MenuItem("Copy Value");
				mi.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent arg0) {
						CustomClipboard.set(((Text)cell.getGraphic()).getText());
					}
				});
				mi.setGraphic(Images.COPY.createImageView());
				cm.getItems().add(mi);
	
				if (columnType.isConcept() && conceptNid != 0) {
					final int nid = conceptNid;
					CommonMenus.addCommonMenus(cm, new CommonMenusNIdProvider() {
						@Override
						public Collection<Integer> getNIds() {
						   return Arrays.asList(new Integer[] {nid});
						}
					});
				}

				AppContext.getService(DragRegistry.class).setupDragOnly(text, new SingleConceptIdProvider() {
					@Override
					public String getConceptId() {
						return Integer.toString(srConcept.getConcept().getNid());
					}
				});
}
		} else {
			cell.setText(null);
			cell.setGraphic(null);
		}
	}

	private void scrapeSearchRestriction() {
		if (restrictionsInitialized) {
			// Description Type
			if (descriptionRestrictionRadio.selectedProperty().get()) {
				searchRestriction.setDescriptionType(LuceneDescriptionType.DEFINITION);
			
			} else if (synonymRestrictionRadio.selectedProperty().get()) {
				searchRestriction.setDescriptionType(LuceneDescriptionType.SYNONYM);
			
			} else if (fsnRestrictionRadio.selectedProperty().get()) {
				searchRestriction.setDescriptionType(LuceneDescriptionType.FSN);
			
			} else {
				searchRestriction.setDescriptionType(null);
			}
			
			// Advanced Description Type
			SimpleDisplayConcept selectedAdvancedDescription = descriptionRestrictionCombo.getSelectionModel().getSelectedItem();
			if (selectedAdvancedDescription != null && selectedAdvancedDescription.getNid() != Integer.MIN_VALUE) {
				searchRestriction.setAdvancedDescriptionType(selectedAdvancedDescription);
			} else {
				searchRestriction.setAdvancedDescriptionType(null);
			}
			

			// Code System
			SimpleDisplayConcept selectedCodeSystem = codeSystemRestrictionCombo.getSelectionModel().getSelectedItem();
			if (selectedCodeSystem != null && selectedCodeSystem.getNid() != Integer.MIN_VALUE) {
				searchRestriction.setTargetCodeSystemPath(selectedCodeSystem);
			} else {
				searchRestriction.setTargetCodeSystemPath(null);
			}

			// Refset Member
			SimpleDisplayConcept selectedRefset = refsetRestrictionCombo.getSelectionModel().getSelectedItem();
			if (selectedRefset != null && selectedRefset.getNid() != Integer.MIN_VALUE) {
				searchRestriction.setMemberOfRefset(selectedRefset);
			} else {
				searchRestriction.setMemberOfRefset(null);
			}
			
			// Kind of concept
			searchRestriction.setKindOf(kindOfRestrictionConceptNode.getConcept());
			
			
		}
	}
	
	private void paintSearchRestriction() {
		// Description Type
		if (searchRestriction.descriptionType == null) {
			noRestrictionRadio.setSelected(true);
		} else {
			switch (searchRestriction.descriptionType) {
			case DEFINITION:
				descriptionRestrictionRadio.setSelected(true);
				break;
			case SYNONYM:
				synonymRestrictionRadio.setSelected(true);
				break;
			case FSN:
				fsnRestrictionRadio.setSelected(true);
				break;
			default:
				noRestrictionRadio.setSelected(true);
			}
		}
		
		MappingController.setComboSelection(descriptionRestrictionCombo, searchRestriction.getAdvancedDescriptionType(), 0);
		
		MappingController.setComboSelection(codeSystemRestrictionCombo, searchRestriction.getTargetCodeSystemPath(), 0);

		MappingController.setComboSelection(refsetRestrictionCombo, searchRestriction.getMemberOfRefset(), 0);

		resetKindOfRestriction();
		
		restrictionsInitialized = true;
	}
	
	private void clearSearchRestriction() {
		searchRestriction.clear();
		paintSearchRestriction();
	}

	private void resetKindOfRestriction() {
		kindOfRestrictionConceptNode = new ConceptNode(null, false);
		kindOfRestrictionVBox.getChildren().clear();
		kindOfRestrictionVBox.getChildren().add(kindOfRestrictionConceptNode.getNode());
		if (searchRestriction.getKindOf() != null) {
			kindOfRestrictionConceptNode.set(searchRestriction.getKindOf());
		}
	}
	

	@SuppressWarnings("unused")
	private void showFilterPane(boolean show) {
		filterVBox.setPrefWidth((show) ? Region.USE_COMPUTED_SIZE : 0);
	}
	
}
