package gov.va.isaac.gui.enhancedsearchview.model;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerHelper;
import gov.va.isaac.gui.dragAndDrop.DragRegistry;
import gov.va.isaac.gui.dragAndDrop.SingleConceptIdProvider;
import gov.va.isaac.gui.enhancedsearchview.EnhancedSearchViewBottomPane;
import gov.va.isaac.gui.enhancedsearchview.SearchTypeEnums.ResultsType;
import gov.va.isaac.gui.enhancedsearchview.SearchTypeEnums.SearchType;
import gov.va.isaac.search.CompositeSearchResult;
import gov.va.isaac.search.CompositeSearchResultComparator;
import gov.va.isaac.search.DescriptionAnalogBITypeComparator;
import gov.va.isaac.util.CommonMenus;
import gov.va.isaac.util.CommonMenusDataProvider;
import gov.va.isaac.util.CommonMenusNIdProvider;
import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
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
	private TableView<CompositeSearchResult> results = new TableView<CompositeSearchResult>();
	
	private static final Logger LOG = LoggerFactory.getLogger(SearchResultsTable.class);
	
	private ResultsType selectedResultsType;
	private EnhancedSearchViewBottomPane bottomPane;

	private SearchType selectedSearchType;

	public void initializeSearchResultsTable(SearchType selectedSearchType, ResultsType selectedResultsType) {
		this.selectedResultsType = selectedResultsType;
		this.selectedSearchType = selectedSearchType;
		
		// Enable selection of multiple rows.  Context menu handlers are coded to send collections.
		results.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		// Backup existing data in order to restore after reinitializing
		List<CompositeSearchResult> resultsBackup = new ArrayList<>(results.getItems());

		// Clear underlying data structure
		results.getItems().clear();

		// Enable optional menu to make visible columns invisible and currently invisible columns visible
		results.setTableMenuButtonVisible(true);

		// Disable editing of table data
		results.setEditable(false);

		// Match quality between 0 and 1
		TableColumn<CompositeSearchResult, Number> scoreCol = new TableColumn<>("Score");
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

		// Active status
		TableColumn<CompositeSearchResult, String> statusCol = new TableColumn<>("Status");
		statusCol.setCellValueFactory((param) -> new SimpleStringProperty(param.getValue().getContainingConcept().getStatus().toString().trim()));
		statusCol.setCellFactory(new MyTableCellCallback<String>());

		// numMatchesCol only meaningful for AggregationType CONCEPT
		// When AggregationTyppe is DESCRIPTION should always be 1
		TableColumn<CompositeSearchResult, Number> numMatchesCol = new TableColumn<>("Matches");
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
								String type = WBUtility.getConPrefTerm(descComp.getTypeNid());
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

		// Preferred term
		TableColumn<CompositeSearchResult, String> preferredTermCol = new TableColumn<>("Term");
		preferredTermCol.setCellFactory(new MyTableCellCallback<String>());
		preferredTermCol.setPrefWidth(100);
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

		// Fully Specified Name
		TableColumn<CompositeSearchResult, String> fsnCol = new TableColumn<>("FSN");
		fsnCol.setCellFactory(new MyTableCellCallback<String>());
		fsnCol.setPrefWidth(100);
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

		// Matching description text.
		// If AggregationType is CONCEPT then arbitrarily picks first matching description
		TableColumn<CompositeSearchResult, String> matchingTextCol = new TableColumn<>("Text");
		matchingTextCol.setCellValueFactory((param) -> new SimpleStringProperty(param.getValue().getMatchingStrings().iterator().next().trim()));
		matchingTextCol.setCellFactory(new MyTableCellCallback<String>());

		// matchingDescTypeCol is string value type of matching description term displayed
		// Only meaningful for AggregationType DESCRIPTION
		// When AggregationTyppe is CONCEPT should always be type of first match
		TableColumn<CompositeSearchResult, String> matchingDescTypeCol = new TableColumn<>("Type");
		matchingDescTypeCol.setCellValueFactory((param) -> new SimpleStringProperty(WBUtility.getConPrefTerm(param.getValue().getMatchingDescriptionComponents().iterator().next().getTypeNid())));
		matchingDescTypeCol.setCellFactory(new MyTableCellCallback<String>());
		// matchingDescTypeCol defaults to invisible for anything but DESCRIPTION
		if (selectedResultsType != ResultsType.DESCRIPTION) {
			matchingDescTypeCol.setVisible(false);
		}

		// NID set to invisible because largely for debugging purposes only
		TableColumn<CompositeSearchResult, Number> nidCol = new TableColumn<>("NID");
		nidCol.setCellValueFactory((param) -> new SimpleIntegerProperty(param.getValue().getContainingConcept().getNid()));
		nidCol.setCellFactory(new MyTableCellCallback<Number>());
		nidCol.setVisible(false);

		// UUID set to invisible because largely for debugging purposes only
		TableColumn<CompositeSearchResult, String> uuIdCol = new TableColumn<>("UUID");
		uuIdCol.setCellValueFactory((param) -> new SimpleStringProperty(param.getValue().getContainingConcept().getPrimordialUuid().toString().trim()));
		uuIdCol.setVisible(false);
		uuIdCol.setCellFactory(new MyTableCellCallback<String>());

		// Optional SCT ID
		TableColumn<CompositeSearchResult, String> sctIdCol = new TableColumn<>("SCTID");
		sctIdCol.setCellValueFactory((param) -> new SimpleStringProperty(ConceptViewerHelper.getSctId(ConceptViewerHelper.getConceptAttributes(param.getValue().getContainingConcept())).trim()));
		sctIdCol.setCellFactory(new MyTableCellCallback<String>());

		results.getColumns().clear();

		// Default column ordering. May be changed within session
		results.getColumns().add(scoreCol);
		results.getColumns().add(statusCol);
		results.getColumns().add(fsnCol);
		results.getColumns().add(preferredTermCol);
		if (selectedResultsType == ResultsType.CONCEPT) {
			results.getColumns().add(numMatchesCol);
		}
		if (selectedResultsType == ResultsType.DESCRIPTION) {
			results.getColumns().add(matchingTextCol);
			results.getColumns().add(matchingDescTypeCol);
		}
		results.getColumns().add(sctIdCol);
		results.getColumns().add(uuIdCol);
		results.getColumns().add(nidCol);

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
										if (selectedResultsType == ResultsType.CONCEPT) {
											nids.add(r.getContainingConcept().getNid());
										} else if (selectedResultsType == ResultsType.DESCRIPTION) {
											nids.add(r.getMatchingDescriptionComponents().iterator().next().getNid());
										} else {
											LOG.error("Unexpected AggregationType value " + selectedResultsType);
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
