package gov.va.isaac.gui.enhancedsearchview.model;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerHelper;
import gov.va.isaac.gui.dragAndDrop.DragRegistry;
import gov.va.isaac.gui.dragAndDrop.SingleConceptIdProvider;
import gov.va.isaac.gui.enhancedsearchview.EnhancedSearchViewBottomPane;
import gov.va.isaac.gui.enhancedsearchview.SearchTypeEnums.ResultsType;
import gov.va.isaac.gui.enhancedsearchview.SearchTypeEnums.SearchType;
import gov.va.isaac.gui.enhancedsearchview.model.type.sememe.SememeSearchResult;
import gov.va.isaac.search.CompositeSearchResult;
import gov.va.isaac.search.CompositeSearchResultComparator;
import gov.va.isaac.search.DescriptionAnalogBITypeComparator;
import gov.va.isaac.util.CommonMenus;
import gov.va.isaac.util.CommonMenusDataProvider;
import gov.va.isaac.util.CommonMenusNIdProvider;
import gov.va.isaac.util.OTFUtility;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.description.DescriptionAnalogBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchResultsTable  {
	private final TableView<CompositeSearchResult> results = new TableView<CompositeSearchResult>();
	
	private static final Logger LOG = LoggerFactory.getLogger(SearchResultsTable.class);
	
	private ResultsType resultsType;
	private EnhancedSearchViewBottomPane bottomPane;

	private SearchType searchType;

	private TableColumn<CompositeSearchResult, String> statusCol = new TableColumn<>("Status");
	private TableColumn<CompositeSearchResult, Number> nidCol = new TableColumn<>("NID");
	private TableColumn<CompositeSearchResult, String> uuIdCol = new TableColumn<>("UUID");
	private TableColumn<CompositeSearchResult, String> sctIdCol = new TableColumn<>("SCTID");
	private TableColumn<CompositeSearchResult, String> matchingDescTypeCol = new TableColumn<>("Type");
	private TableColumn<CompositeSearchResult, String> matchingTextCol = new TableColumn<>("Text");
	private TableColumn<CompositeSearchResult, String> fsnCol = new TableColumn<>("FSN");
	private TableColumn<CompositeSearchResult, String> preferredTermCol = new TableColumn<>("Term");
	private TableColumn<CompositeSearchResult, Number> numMatchesCol = new TableColumn<>("Matches");
	private TableColumn<CompositeSearchResult, Number> scoreCol = new TableColumn<>("Score");


	private TableColumn<CompositeSearchResult, String> referencedComponentPrefTermCol = new TableColumn<>("Referenced Component");
	private TableColumn<CompositeSearchResult, String> assemblageConceptPrefTermCol = new TableColumn<>("Assemblage Concept");
	private TableColumn<CompositeSearchResult, String> attachedDataCol = new TableColumn<>("Attached Data");

	private List<CompositeSearchResult> resultsBackup;

	public void initializeSearchResultsTable(SearchType searchType, ResultsType resultsType) {
		if (searchType == null || resultsType == null) {
			return;
		}
		
		this.resultsType = resultsType;
		this.searchType = searchType;
		
		setupTableAttributes();

		setupColumns();

		// Default column ordering. May be changed within session
		addTypeBasedColumns();
		

		AppContext.getService(DragRegistry.class).setupDragOnly(results, new SingleConceptIdProvider() {
			@Override
			public String getConceptId()
			{
				CompositeSearchResult dragItem = results.getSelectionModel().getSelectedItem();
				if (dragItem != null)
				{
					LOG.debug("Dragging concept id " + dragItem.getContainingConcept().getNid());
					return dragItem.getContainingConcept().getNid() + "";
				}
				return null;
			}
		});

		Collections.sort(resultsBackup, new CompositeSearchResultComparator());
		results.getItems().addAll(resultsBackup);

		if (bottomPane != null) {
			bottomPane.refreshBottomPanel();
			bottomPane.refreshTotalResultsSelectedLabel();
		}
	}	
	
	private void addTypeBasedColumns() {
		if (searchType != SearchType.SEMEME) {
			if (searchType != SearchType.REFSET_SPEC) {
				results.getColumns().add(scoreCol);
			}
		
			results.getColumns().add(statusCol);
		
			results.getColumns().add(fsnCol);
			results.getColumns().add(preferredTermCol);
			
			if (resultsType == ResultsType.CONCEPT && searchType == SearchType.TEXT) {
				results.getColumns().add(numMatchesCol);
			}
			
			if (resultsType == ResultsType.DESCRIPTION) {
				results.getColumns().add(matchingTextCol);
				results.getColumns().add(matchingDescTypeCol);
			}
		} else {
			results.getColumns().add(referencedComponentPrefTermCol);
			results.getColumns().add(assemblageConceptPrefTermCol);
			results.getColumns().add(attachedDataCol);
		}
			
		results.getColumns().add(sctIdCol);
		results.getColumns().add(uuIdCol);
		results.getColumns().add(nidCol);
	}

	private void setupColumns() {
		initializeScoreColumn();
		initializeStatusColumn();
		initializeMatchesColumn();
		initializePrefTermColumn();
		initializeFsnColumn();
		initializeMatchingTextColumn();
		initializeTypeColumn();
		initializeNidColumn();
		initializeUuidColumn();
		initializeSctIdColumn();
		initializeRefCompColumn();
		initializeAssembConColumn();
		initializeAttachedDataColumn();

		results.getColumns().clear();
	}

	private void initializeNidColumn() {
		// NID set to invisible because largely for debugging purposes only
		nidCol.setCellValueFactory((param) -> new SimpleIntegerProperty(param.getValue().getContainingConcept().getNid()));
		nidCol.setCellFactory(new MyTableCellCallback<Number>());
		nidCol.setVisible(false);

	}	
	
	private void initializeUuidColumn() {
		// UUID set to invisible because largely for debugging purposes only
		uuIdCol.setCellValueFactory((param) -> new SimpleStringProperty(param.getValue().getContainingConcept().getPrimordialUuid().toString().trim()));
		uuIdCol.setVisible(false);
		uuIdCol.setCellFactory(new MyTableCellCallback<String>());
	}
	
	private void initializeSctIdColumn() {
		// Optional SCT ID
		sctIdCol.setCellValueFactory((param) -> new SimpleStringProperty(ConceptViewerHelper.getSctId(ConceptViewerHelper.getConceptAttributes(param.getValue().getContainingConcept())).trim()));
		sctIdCol.setCellFactory(new MyTableCellCallback<String>());
	}
	
	private void initializeTypeColumn() {

		// matchingDescTypeCol is string value type of matching description term displayed
		// Only meaningful for AggregationType DESCRIPTION
		// When AggregationTyppe is CONCEPT should always be type of first match
		matchingDescTypeCol.setCellValueFactory((param) -> new SimpleStringProperty(OTFUtility.getConPrefTerm(param.getValue().getMatchingDescriptionComponents().iterator().next().getTypeNid())));
		matchingDescTypeCol.setCellFactory(new MyTableCellCallback<String>());
		// matchingDescTypeCol defaults to invisible for anything but DESCRIPTION
		if (resultsType != ResultsType.DESCRIPTION) {
			matchingDescTypeCol.setVisible(false);
		}
	}

	private void initializeMatchingTextColumn() {
		// Matching description text.
		// If AggregationType is CONCEPT then arbitrarily picks first matching description
		matchingTextCol.setCellValueFactory((param) -> new SimpleStringProperty(param.getValue().getMatchingStrings().iterator().next().trim()));
		matchingTextCol.setCellFactory(new MyTableCellCallback<String>());
	}

	private void initializeFsnColumn() {

		// Fully Specified Name
		fsnCol.setCellFactory(new MyTableCellCallback<String>());
		fsnCol.setCellValueFactory((param) -> {
			try {
				return new SimpleStringProperty(param.getValue().getContainingConcept().getFullySpecifiedDescription().getText().trim());
			} catch (IOException | ContradictionException e) {
				String title = "Failed getting FSN";
				String msg = "Failed getting fully specified description";
				LOG.error(title);
				AppContext.getCommonDialogs().showErrorDialog(title, msg, "Encountered " + e.getClass().getName() + ": " + e.getLocalizedMessage(), AppContext.getMainApplicationWindow().getPrimaryStage());
				e.printStackTrace();
				return null;
			}
		});

	}

	private void initializePrefTermColumn() {
		// Preferred term
		preferredTermCol.setCellFactory(new MyTableCellCallback<String>());
		preferredTermCol.setCellValueFactory((param) -> {
			try {
				return new SimpleStringProperty(param.getValue().getContainingConcept().getPreferredDescription().getText().trim());
			} catch (IOException | ContradictionException e) {
				String title = "Failed getting preferred description";
				String msg = "Failed getting preferred description";
				LOG.error(title);
				AppContext.getCommonDialogs().showErrorDialog(title, msg, "Encountered " + e.getClass().getName() + ": " + e.getLocalizedMessage(), AppContext.getMainApplicationWindow().getPrimaryStage());
				e.printStackTrace();
				return null;
			}
		});
	}

	private void initializeMatchesColumn() {

		// numMatchesCol only meaningful for AggregationType CONCEPT
		// When AggregationTyppe is DESCRIPTION should always be 1
		numMatchesCol.setCellValueFactory((param) -> new SimpleIntegerProperty(param.getValue().getMatchingDescriptionComponents().size()));
		numMatchesCol.setCellFactory(new MyTableCellCallback<Number>() {
			@Override
			public TableCell<CompositeSearchResult, Number> modifyCell(TableCell<CompositeSearchResult, Number> cell) {

				cell.addEventFilter(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						TableCell<?, ?> c = (TableCell<?,?>) event.getSource();

						if (c != null && c.getIndex() < results.getItems().size()) {
							CompositeSearchResult result = results.getItems().get(c.getIndex());
							StringBuilder buffer = new StringBuilder();
							String fsn = null;
							try {
								fsn = result.getContainingConcept().getFullySpecifiedDescription().getText().trim();
							} catch (IOException | ContradictionException e) {
								String title = "Failed getting FSN";
								String msg = "Failed getting fully specified description";
								LOG.error(title);
								AppContext.getCommonDialogs().showErrorDialog(title, msg, "Encountered " + e.getClass().getName() + ": " + e.getLocalizedMessage(), AppContext.getMainApplicationWindow().getPrimaryStage());
								e.printStackTrace();
							}

							List<DescriptionAnalogBI<?>> matchingDescComponents = new ArrayList<DescriptionAnalogBI<?>>(result.getMatchingDescriptionComponents());
							Collections.sort(matchingDescComponents, new DescriptionAnalogBITypeComparator());
							for (DescriptionAnalogBI<?> descComp : matchingDescComponents) {
								String type = OTFUtility.getConPrefTerm(descComp.getTypeNid());
								buffer.append(type + ": " + descComp.getText() + "\n");
							}
							Tooltip tooltip = new Tooltip("Matching descriptions for \"" + fsn + "\":\n" + buffer.toString());

							Tooltip.install(cell, tooltip);
						}
					}
				});

				return cell;
			}
		});

	}

	private void initializeStatusColumn() {
		// Active status
		statusCol.setCellValueFactory((param) -> new SimpleStringProperty(param.getValue().getContainingConcept().getStatus().toString().trim()));
		statusCol.setCellFactory(new MyTableCellCallback<String>());
	}

	private void initializeScoreColumn() {
		// Match quality between 0 and 1
		scoreCol.setCellValueFactory((param) -> new SimpleDoubleProperty(param.getValue().getBestScore()));
		scoreCol.setCellFactory(new MyTableCellCallback<Number>());
		scoreCol.setCellFactory(new MyTableCellCallback<Number>() {
			@Override
			public TableCell<CompositeSearchResult, Number> createNewCell() {

				final DecimalFormat fmt = new DecimalFormat("#.####");

				TableCell<CompositeSearchResult, Number> cell = new TableCell<CompositeSearchResult, Number>() {
					@Override
					public void updateItem(Number item, boolean empty) {
						super.updateItem(item, empty);
						setText(empty ? null : getString());
						setGraphic(null);
					}

					private String getString() {	
						fmt.setRoundingMode(RoundingMode.HALF_UP);
						return getItem() == null ? "" : fmt.format(getItem().doubleValue());
					}
				};

				cell.addEventFilter(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						TableCell<?, ?> c = (TableCell<?,?>) event.getSource();

						if (c != null && c.getItem() != null) {
							Tooltip tooltip = new Tooltip(c.getItem().toString());
							Tooltip.install(cell, tooltip);
						}
					}
				});

				return cell;
			}
		});		
	}

	private void initializeRefCompColumn() {
		referencedComponentPrefTermCol.setCellFactory(new MyTableCellCallback<String>());
		referencedComponentPrefTermCol.setCellValueFactory((param) -> {
			try {
				SememeSearchResult sememeParam = (SememeSearchResult)param.getValue();
				return new SimpleStringProperty(sememeParam.getContainingConcept().getPreferredDescription().getText().trim());
			} catch (IOException | ContradictionException e) {
				String title = "Failed getting referenced component";
				String msg = "Failed getting referenced component";
				LOG.error(title);
				AppContext.getCommonDialogs().showErrorDialog(title, msg, "Encountered " + e.getClass().getName() + ": " + e.getLocalizedMessage(), AppContext.getMainApplicationWindow().getPrimaryStage());
				e.printStackTrace();
				return null;
			}
		});
	}
	
	private void initializeAssembConColumn() {
		assemblageConceptPrefTermCol.setCellFactory(new MyTableCellCallback<String>());
		assemblageConceptPrefTermCol.setCellValueFactory((param) -> {
			try {
				SememeSearchResult sememeParam = (SememeSearchResult)param.getValue();
				
				return new SimpleStringProperty(sememeParam.getAssembCon().getPreferredDescription().getText().trim());
			} catch (IOException | ContradictionException e) {
				String title = "Failed getting assemblage concept";
				String msg = "Failed getting assemblage concept";
				LOG.error(title);
				AppContext.getCommonDialogs().showErrorDialog(title, msg, "Encountered " + e.getClass().getName() + ": " + e.getLocalizedMessage(), AppContext.getMainApplicationWindow().getPrimaryStage());
				e.printStackTrace();
				return null;
			}
		});
		
	}

	private void initializeAttachedDataColumn() {
		attachedDataCol.setCellFactory(new MyTableCellCallback<String>());

		attachedDataCol.setCellValueFactory((param) -> {
			SememeSearchResult sememeParam = (SememeSearchResult)param.getValue();
			return new SimpleStringProperty(sememeParam.getAttachedData());
		});
		
	}

	
	private void setupTableAttributes() {
		// Enable selection of multiple rows.  Context menu handlers are coded to send collections.
		results.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		// Backup existing data in order to restore after reinitializing
		resultsBackup = new ArrayList<>(results.getItems());

		// Clear underlying data structure
		results.getItems().clear();

		// Enable optional menu to make visible columns invisible and currently invisible columns visible
		results.setTableMenuButtonVisible(true);

		// Disable editing of table data
		results.setEditable(false);
 	}

	// MyTableCellCallback adds hooks for double-click and/or other mouse actions to String cells
	private class MyTableCellCallback<T> implements Callback<TableColumn<CompositeSearchResult, T>, TableCell<CompositeSearchResult, T>> {
		/* (non-Javadoc)
		 * @see javafx.util.Callback#call(java.lang.Object)
		 */
		public TableCell<CompositeSearchResult, T> createNewCell() {
			TableCell<CompositeSearchResult, T> cell = new TableCell<CompositeSearchResult, T>() {
				@Override
				public void updateItem(T item, boolean empty) {
					super.updateItem(item, empty);
					setText(empty ? null : getString());
					setGraphic(null);
				}

				private String getString() {
					return getItem() == null ? "" : getItem().toString();
				}
			};

			return cell;
		}

		// This method can be overridden to customize cells
		public TableCell<CompositeSearchResult, T> modifyCell(TableCell<CompositeSearchResult, T> cell) {
			return cell;
		}

		@Override
		public TableCell<CompositeSearchResult, T> call(TableColumn<CompositeSearchResult, T> param) {
			TableCell<CompositeSearchResult, T> newCell = createNewCell();
			newCell.setUserData(param.getCellData(newCell.getIndex()));

			// This event filter adds a concept-specific context menu to all cells based on underlying concept
			// It is in this method because it should be common to all cells, even those overriding modifyCell()
			newCell.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					bottomPane.refreshTotalResultsSelectedLabel();

					if (event.getButton() == MouseButton.SECONDARY) {
						@SuppressWarnings("unchecked")
						TableCell<CompositeSearchResult, T> c = (TableCell<CompositeSearchResult, T>) event.getSource();

						if (c != null && c.getIndex() < c.getTableView().getItems().size()) {
							CommonMenusDataProvider dp = new CommonMenusDataProvider() {
								@Override
								public String[] getStrings() {
									List<String> items = new ArrayList<>();
									for (Integer index : c.getTableView().getSelectionModel().getSelectedIndices()) {
										items.add(c.getTableColumn().getCellData(index).toString());
									}

									String[] itemArray = items.toArray(new String[items.size()]);

									// TODO: determine why we are getting here multiple (2 or 3) times for each selection
									//System.out.println("Selected strings: " + Arrays.toString(itemArray));

									return itemArray;
								}
							};
							CommonMenusNIdProvider nidProvider = new CommonMenusNIdProvider() {
								@Override
								public Set<Integer> getNIds() {
									Set<Integer> nids = new HashSet<>();
									for (CompositeSearchResult r : (ObservableList<CompositeSearchResult>)c.getTableView().getSelectionModel().getSelectedItems()) {
										if (resultsType == ResultsType.CONCEPT) {
											nids.add(r.getContainingConcept().getNid());
										} else if (resultsType == ResultsType.DESCRIPTION) {
											nids.add(r.getMatchingDescriptionComponents().iterator().next().getNid());
										} else {
											LOG.error("Unexpected AggregationType value " + resultsType);
											nids.add(r.getContainingConcept().getNid());
										}
									}

									// TODO: determine why we are getting here multiple (2 or 3) times for each selection
									//System.out.println("Selected nids: " + Arrays.toString(nids.toArray()));

									return nids;
								}
							};

							ContextMenu cm = new ContextMenu();
							CommonMenus.addCommonMenus(cm, dp, nidProvider);

							c.setContextMenu(cm);
						}
					}
				}
			});

			return modifyCell(newCell);
		}	
	}

	public TableView<CompositeSearchResult> getResults() {
		return results;
	}

	public void setBottomPane(EnhancedSearchViewBottomPane pane) {
		bottomPane = pane;
		bottomPane.refreshBottomPanel();
		bottomPane.refreshTotalResultsSelectedLabel();
	}
}
