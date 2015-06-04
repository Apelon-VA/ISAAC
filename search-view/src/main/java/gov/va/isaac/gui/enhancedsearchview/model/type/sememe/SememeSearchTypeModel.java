package gov.va.isaac.gui.enhancedsearchview.model.type.sememe;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.gui.ConceptNode;
import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.gui.enhancedsearchview.SearchTypeEnums.ResultsType;
import gov.va.isaac.gui.enhancedsearchview.filters.SememeContentSearchTypeFilter;
import gov.va.isaac.gui.enhancedsearchview.model.SearchModel;
import gov.va.isaac.gui.enhancedsearchview.model.SearchTypeModel;
import gov.va.isaac.gui.enhancedsearchview.resulthandler.ResultsToTaxonomy;
import gov.va.isaac.refexDynamic.RefexDynamicUtil;
import gov.va.isaac.search.CompositeSearchResult;
import gov.va.isaac.search.SearchHandle;
import gov.va.isaac.search.SearchHandler;
import gov.va.isaac.util.Interval;
import gov.va.isaac.util.NumberUtilities;
import gov.va.isaac.util.TaskCompleteCallback;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.OTFUtility;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.ihtsdo.otf.query.lucene.LuceneDynamicRefexIndexer;
import org.ihtsdo.otf.query.lucene.LuceneDynamicRefexIndexerConfiguration;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.metadata.binding.RefexDynamic;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicUsageDescription;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicString;
import org.ihtsdo.otf.tcc.model.index.service.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.javafx.collections.ObservableListWrapper;

public class SememeSearchTypeModel extends SearchTypeModel implements TaskCompleteCallback {
	final static Logger logger = LoggerFactory.getLogger(SememeSearchTypeModel.class);

	private HBox searchInRefexHBox = new HBox();
	private FlowPane searchInColumnsHolder = new FlowPane();
	private ConceptNode searchInRefex;
	private Integer currentlyEnteredAssemblageNid = null;
	private TextField searchText;
	private Tooltip tooltip = new Tooltip();
	private SearchHandle ssh = null;
	private ObservableList<SimpleDisplayConcept> dynamicRefexList_ = new ObservableListWrapper<>(new ArrayList<>());
	private VBox optionsContentVBox;
	private Font boldFont = new Font("System Bold", 13.0);
	
	public SememeContentSearchTypeFilter getSearchType() {
		return new SememeContentSearchTypeFilter(searchText.getText(), searchInRefex != null ? searchInRefex.getConceptNoWait() : null);
	}
	public void setSearchType(SememeContentSearchTypeFilter filter) {
		ConceptVersionBI conceptFromSearchFilter = filter != null ? filter.getAssemblageConcept() : null;
		searchInRefex.set(conceptFromSearchFilter);
		searchText.setText(filter.getSearchParameter());
	}

	public SememeSearchTypeModel() {
		setupSearchText();
		populateDynamicRefexList();
		
		Label rootExp = new Label("Search Assemblage");
		rootExp.setFont(boldFont);

		searchInRefex = new ConceptNode(null, false, dynamicRefexList_, null);
		searchInRefexHBox.getChildren().addAll(rootExp, searchInRefex.getNode());

		searchInRefex.getConceptProperty().addListener((ChangeListener<ConceptVersionBI>) (observable, oldValue, newValue) -> 
		{
			if (newValue != null)
			{
				searchInColumnsHolder.getChildren().clear();
				try
				{
					RefexDynamicUsageDescription rdud = RefexDynamicUsageDescription.read(newValue.getNid());
					currentlyEnteredAssemblageNid = rdud.getRefexUsageDescriptorNid();
					Integer[] indexedColumns = LuceneDynamicRefexIndexerConfiguration.readIndexInfo(currentlyEnteredAssemblageNid);
					if (indexedColumns == null || indexedColumns.length == 0)
					{
						searchInRefex.isValid().setInvalid("Sememe searches can only be performed on indexed columns in the sememe.  The selected "
								+ "sememe does not contain any indexed data columns.  Please configure the indexes to search this sememe.");
						optionsContentVBox.getChildren().remove(searchInColumnsHolder);
					}
					else
					{
						Label l = new Label("Search in Columns");
						searchInColumnsHolder.getChildren().add(l);
						l.minWidthProperty().bind(((Label)searchInRefexHBox.getChildren().get(0)).widthProperty());
						RefexDynamicColumnInfo[] rdci = rdud.getColumnInfo();
						if (rdci.length > 0)
						{
							Arrays.sort(rdci);  //We will depend on them being in the correct order later.
							HashSet<Integer> indexedColumnsSet = new HashSet<>(Arrays.asList(indexedColumns));
							int indexNumber = 0;
							for (RefexDynamicColumnInfo ci : rdci)
							{
								StackPane cbStack = new StackPane();
								CheckBox cb = new CheckBox(ci.getColumnName());
								if (ci.getColumnDataType() == RefexDynamicDataType.BYTEARRAY || !indexedColumnsSet.contains(indexNumber))
								{
									cb.setDisable(true);  //No index on this column... not searchable
									Tooltip.install(cbStack, new Tooltip("Column Datatype: " + ci.getColumnDataType().getDisplayName() + " is not indexed"));
								}
								else
								{
									cb.setSelected(true);
									cb.setTooltip(new Tooltip("Column Datatype: " + ci.getColumnDataType().getDisplayName()));
								}
								cbStack.getChildren().add(cb);
								searchInColumnsHolder.getChildren().add(cbStack);
								indexNumber++;
							}
							optionsContentVBox.getChildren().add(searchInColumnsHolder);
						}
						else
						{
							searchInRefex.isValid().setInvalid("Sememe searches can only be performed on the data in the sememe.  The selected "
									+ "sememe does not contain any data columns.");
							optionsContentVBox.getChildren().remove(searchInColumnsHolder);
						}
					}
				}
				catch (Exception e1)
				{
					searchInRefex.isValid().setInvalid("Sememe searches can only be limited to valid Dynamic Sememe Assemblage concept types."
							+ "  The current value is not a Dynamic Sememe Assemblage concept.");
					currentlyEnteredAssemblageNid = null;
					optionsContentVBox.getChildren().remove(searchInColumnsHolder);
					searchInColumnsHolder.getChildren().clear();
				}
			}
			else
			{
				currentlyEnteredAssemblageNid = null;
				optionsContentVBox.getChildren().remove(searchInColumnsHolder);
				searchInColumnsHolder.getChildren().clear();
			}
		});
		
		viewCoordinateProperty.addListener(new ChangeListener<ViewCoordinate>() {
			@Override
			public void changed(
					ObservableValue<? extends ViewCoordinate> observable,
					ViewCoordinate oldValue, ViewCoordinate newValue) {	
				isSearchTypeRunnableProperty.set(isValidSearch());
				isSearchTypeSavableProperty.set(isSavableSearch());
			}
		});
		searchText.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable,
					String oldValue, String newValue) {
				isSearchTypeRunnableProperty.set(isValidSearch());
				isSearchTypeSavableProperty.set(isSavableSearch());
			}
		});
		
		isSearchTypeRunnableProperty.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable,
					Boolean oldValue, Boolean newValue) {
				if (SearchModel.getSearchTypeSelector().getTypeSpecificModel() == SememeSearchTypeModel.this) {
					SearchModel.isSearchRunnableProperty().set(newValue);
				}
			}
		});
		isSearchTypeRunnableProperty.set(isValidSearch());

		isSearchTypeSavableProperty.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable,
					Boolean oldValue, Boolean newValue) {
				if (SearchModel.getSearchTypeSelector().getTypeSpecificModel() == SememeSearchTypeModel.this) {
					SearchModel.isSearchSavableProperty().set(newValue);
				}
			}
		});
		isSearchTypeSavableProperty.set(isSavableSearch());
	}
	
	private void setupSearchText() {
		tooltip.setText("Enter the description text to search for.  Advanced query syntax such as 'AND', 'NOT' is supported.  You may also enter UUIDs for concepts.");
		tooltip.setWrapText(true);
		tooltip.setMaxWidth(600);
		searchText = new TextField();
		searchText.setTooltip(tooltip);
	}

	public Integer getCurrentlyEnteredAssemblageNid() {
		return currentlyEnteredAssemblageNid;
	}

	public void setCurrentlyEnteredAssemblageNid(
			Integer currentlyEnteredAssemblageNid) {
		this.currentlyEnteredAssemblageNid = currentlyEnteredAssemblageNid;
	}

	public ConceptNode getSearchInRefex() {
		return searchInRefex;
	}

	public void setSearchInRefex(ConceptNode searchInRefex) {
		this.searchInRefex = searchInRefex;
	}


	private Integer[] getSearchColumns()
	{
		if (searchInColumnsHolder.getChildren().size() > 1)
		{
			ArrayList<Integer> result = new ArrayList<>();
			int deselectedCount = 0;
			for (int i = 1; i < searchInColumnsHolder.getChildren().size(); i++)
			{
				CheckBox cb = ((CheckBox)((StackPane)searchInColumnsHolder.getChildren().get(i)).getChildren().get(0));
				if (cb.isSelected())
				{
					result.add(i - 1);
				}
				else if (!cb.isDisable())
				{
					deselectedCount++;
				}
			}
			//If they didn't uncheck any, its more efficient to query without the column filter.
			return deselectedCount == 0 ? null : result.toArray(new Integer[0]);
		}
		return null;
	}

	
	@Override
	public void typeSpecificCopy(SearchTypeModel other) {
	}

	@Override
	public String getModelDisplayString() {
		return ", ";
	}

	@Override
	public void executeSearch(ResultsType resultsType, String modelMaxResults) {
		try
		{
			String searchString = searchText.getText().trim();
			try
			{
				RefexDynamicDataBI data = NumberUtilities.wrapIntoRefexHolder(NumberUtilities.parseNumber(searchString));
				LOG.debug("Doing a sememe search with a numeric value");
				ssh = SearchHandler.dynamicRefexSearch((indexer) ->
				{
					try
					{
						return indexer.query(data, currentlyEnteredAssemblageNid, false, getSearchColumns(), Integer.parseInt(modelMaxResults), null);
					}
					catch (Exception e)
					{
						throw new RuntimeException(e);
					}
				}, this, null, null, null, true);
			}
			catch (NumberFormatException e)
			{
				//Not a number...  is it a valid interval?
				try
				{
					Interval interval = new Interval(searchString);
					LOG.debug("Doing a sememe search with an interval value");
					ssh = SearchHandler.dynamicRefexSearch((indexer) ->
					{
						try
						{
							return indexer.queryNumericRange(NumberUtilities.wrapIntoRefexHolder(interval.getLeft()), interval.isLeftInclusive(), 
									NumberUtilities.wrapIntoRefexHolder(interval.getRight()), interval.isRightInclusive(), 
									currentlyEnteredAssemblageNid, getSearchColumns(), Integer.parseInt(modelMaxResults), null);
						}
						catch (Exception e1)
						{
							throw new RuntimeException(e1);
						}
					}, this, null, null, null, true);
				}
				catch (NumberFormatException e1) 
				{
					//run it as a string search
					LOG.debug("Doing a sememe search as a string search");
					ssh = SearchHandler.dynamicRefexSearch((indexer) ->
					{
						try
						{
							return indexer.query(new RefexDynamicString(searchText.getText()), currentlyEnteredAssemblageNid, false, 
									getSearchColumns(), Integer.parseInt(modelMaxResults), null);
						}
						catch (Exception e2)
						{
							throw new RuntimeException(e2);
						}
					}, this, null, null, null, true);
				}
			}
		}
		catch (Exception e)
		{
			LOG.error("Search imploded unexpectedly...", e);
			ssh = null;  //force a null ptr in taskComplete, so an error is displayed.
			taskComplete(0, null);
		}
	}

	public void taskComplete(long taskStartTime, Integer taskId)
	{

		// Run on JavaFX thread.
		Platform.runLater(() -> 
		{
			try
			{
				if (!ssh.isCancelled())
				{
					Collection<CompositeSearchResult> results = createSingleEntryPerResults(ssh.getResults());
					setResults(results);

					/*
					SearchModel.getSearchResultsTable().getResults().getItems().clear();
					SearchModel.getSearchResultsTable().getResults().setItems(FXCollections.observableArrayList(results));

					bottomPane.refreshBottomPanel();
					bottomPane.refreshTotalResultsSelectedLabel();
					
					if (splitPane.getItems().contains(taxonomyPane)) {
						ResultsToTaxonomy.resultsToSearchTaxonomy();
					}
					*/
				}
			} catch (Exception ex) {
				getSearchRunning().set(false);
				String title = "Unexpected Search Error";
				LOG.error(title, ex);
				AppContext.getCommonDialogs().showErrorDialog(title,
						"There was an unexpected error running the search",
						ex.toString(), AppContext.getMainApplicationWindow().getPrimaryStage());
				//searchResultsTable.getItems().clear();
				bottomPane.refreshBottomPanel();
				bottomPane.refreshTotalResultsSelectedLabel();
			} finally {
				getSearchRunning().set(false);
			}
		});
	}

	private Collection<CompositeSearchResult> createSingleEntryPerResults(
			Collection<CompositeSearchResult> results) {
		List<CompositeSearchResult> retList = new ArrayList<CompositeSearchResult>();
		
		for (CompositeSearchResult refConResult : results) {
			ConceptVersionBI refCon = refConResult.getContainingConcept();
			
			for (ComponentVersionBI c : refConResult.getMatchingComponents())
			{
				if (c instanceof RefexDynamicVersionBI<?>)
				{
					RefexDynamicVersionBI<?> rv = (RefexDynamicVersionBI<?>)c;
					ConceptVersionBI assembCon = OTFUtility.getConceptVersion(rv.getAssemblageNid());	
					
					try
					{
						RefexDynamicColumnInfo[] ci = rv.getRefexDynamicUsageDescription().getColumnInfo();
						int i = 0;
						
						for (RefexDynamicDataBI data : rv.getData())
						{
							if (data == null)  //might be an unset column, if the col is optional
							{
								continue;
							}

							String attachedData;
							if (RefexDynamicDataType.BYTEARRAY == data.getRefexDataType()) {
								attachedData = ci[i].getColumnName()+  " - [Binary]";
							} else {
								attachedData = ci[i].getColumnName() + " - " + data.getDataObject().toString();
							}	

							SememeSearchResult result = new SememeSearchResult(refCon, assembCon, attachedData);
							retList.add(result);
							i++;
						}
					}
					catch (Exception e)
					{
						LOG.error("Unexpected error reading sememe info", e);
					}
				} else {
					/*
					concept;
					for (String s : item.getMatchingStrings())
					{
						if (s.equals(preferredText))
						{
							continue;
						}
						Label matchString = new Label(s);
						VBox.setMargin(matchString, new Insets(0.0, 0.0, 0.0, 10.0));
						box.getChildren().add(matchString);
					}
					*/
				}
			}
		}
		
		return retList;
	}

	private void populateDynamicRefexList()
	{
		Task<Void> t = new Task<Void>()
		{
			HashSet<SimpleDisplayConcept> dynamicRefexAssemblages = new HashSet<>();

			@Override
			protected Void call() throws Exception
			{
				dynamicRefexAssemblages = new HashSet<>();
				dynamicRefexAssemblages.addAll(RefexDynamicUtil.getAllRefexDefinitions());
				return null;
			}

			/**
			 * @see javafx.concurrent.Task#succeeded()
			 */
			@Override
			protected void succeeded()
			{
				dynamicRefexList_.clear();
				dynamicRefexList_.addAll(dynamicRefexAssemblages);
				dynamicRefexList_.sort(new Comparator<SimpleDisplayConcept>()
				{
					@Override
					public int compare(SimpleDisplayConcept o1, SimpleDisplayConcept o2)
					{
						return o1.getDescription().compareToIgnoreCase(o2.getDescription());
					}
				});
			}
		};
		
		Utility.execute(t);
	}

	@Override
	public String getValidationFailureMessage() {
		if (viewCoordinateProperty.get() == null) {
			return "View Coordinate is unset";
		} else if (searchText.getText().length() == 0) {
			return "Text parameter is unset or too short";
		} else {
			return null;
		}
	}

	public TextField getSearchText() {
		return searchText;
	}

	public HBox getSearchInRefexHBox() {
		return searchInRefexHBox;
	}

	public VBox getOptionsContentVBox() {
		return optionsContentVBox;		
	}

	public void setOptionsContentVBox(VBox vBox) {
		optionsContentVBox = vBox;
	}
}
