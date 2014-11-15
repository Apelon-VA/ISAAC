package gov.va.isaac.gui.enhancedsearchview.model.type.text;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.ConceptNode;
import gov.va.isaac.gui.enhancedsearchview.SearchTypeEnums.ComponentSearchType;
import gov.va.isaac.gui.enhancedsearchview.SearchTypeEnums.FilterType;
import gov.va.isaac.gui.enhancedsearchview.SearchTypeEnums.SearchType;
import gov.va.isaac.gui.enhancedsearchview.filters.Invertable;
import gov.va.isaac.gui.enhancedsearchview.filters.IsAFilter;
import gov.va.isaac.gui.enhancedsearchview.filters.IsDescendantOfFilter;
import gov.va.isaac.gui.enhancedsearchview.filters.LuceneSearchTypeFilter;
import gov.va.isaac.gui.enhancedsearchview.filters.NonSearchTypeFilter;
import gov.va.isaac.gui.enhancedsearchview.filters.RegExpSearchTypeFilter;
import gov.va.isaac.gui.enhancedsearchview.filters.SearchTypeFilter;
import gov.va.isaac.gui.enhancedsearchview.filters.SingleNidFilter;
import gov.va.isaac.gui.enhancedsearchview.model.EnhancedSavedSearch;
import gov.va.isaac.gui.enhancedsearchview.model.SearchModel;
import gov.va.isaac.gui.enhancedsearchview.model.SearchTypeModel;
import gov.va.isaac.gui.enhancedsearchview.model.type.SearchTypeSpecificView;
import gov.va.isaac.util.WBUtility;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;

import org.apache.mahout.math.Arrays;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextSearchTypeView implements SearchTypeSpecificView {
	private static HBox componentSearchTypeControlsHbox = new HBox();
	private static ComboBox<ComponentSearchType> componentSearchTypeComboBox = new ComboBox<ComponentSearchType>();
	private static GridPane searchFilterGridPane = new GridPane();
	private static Button addIsDescendantOfFilterButton;
	private static boolean beenSet = false;
	private static ComponentSearchType previousSearchType = ComponentSearchType.LUCENE;
	
	private static final Logger LOG = LoggerFactory.getLogger(TextSearchTypeView.class);

	private static VBox componentContentParentPane = new VBox(5);
	SearchModel searchModel = new SearchModel();

	static {
		componentSearchTypeComboBox.setItems(FXCollections.observableArrayList(ComponentSearchType.LUCENE, ComponentSearchType.REGEXP));
		componentSearchTypeComboBox.getSelectionModel().select(ComponentSearchType.LUCENE);
	}

	public static ComponentSearchType getCurrentComponentSearchType() {
		return componentSearchTypeComboBox.getSelectionModel().getSelectedItem();
	}
	
	@Override
	public Pane setContents(SearchTypeModel typeModel) {
		if (!componentContentParentPane.getChildren().isEmpty()) {
			componentContentParentPane.getChildren().clear();
		}
		
		HBox controlAndTypePart = new HBox(5);

		controlAndTypePart.getChildren().add(componentSearchTypeControlsHbox);
		controlAndTypePart.getChildren().add(componentSearchTypeComboBox);
		
		componentContentParentPane.getChildren().add(controlAndTypePart);
		componentContentParentPane.getChildren().add(searchFilterGridPane);

		
		TextSearchTypeModel componentContentModel = (TextSearchTypeModel)typeModel;
		if (addIsDescendantOfFilterButton == null) {
			addIsDescendantOfFilterButton = new Button("Add Filter");
		}
		addIsDescendantOfFilterButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				IsDescendantOfFilter newFilter = new IsDescendantOfFilter();
				addSearchFilter(newFilter, componentContentModel);
				componentContentModel.getFilters().add(newFilter);
			}
		});
		
		
		// Force single selection
		componentSearchTypeComboBox.setCellFactory((p) -> {
			final ListCell<ComponentSearchType> cell = new ListCell<ComponentSearchType>() {
				@Override
				protected void updateItem(ComponentSearchType a, boolean bln) {
					super.updateItem(a, bln);

					if(a != null){
						setText(a.toString() + " Search");
					}else{
						setText(null);
					}
				}
			};

			return cell;
		});
		componentSearchTypeComboBox.setButtonCell(new ListCell<ComponentSearchType>() {
		@Override
			protected void updateItem(ComponentSearchType componentSearchType, boolean bln) {
				super.updateItem(componentSearchType, bln); 
				if (bln && !beenSet) {
					setText("");
					this.setGraphic(null);
					componentSearchTypeControlsHbox.getChildren().clear();
					componentSearchTypeControlsHbox.setUserData(null);
					componentContentModel.setSearchType(null);
				} else {
					if (componentSearchType == null) {
						componentSearchType = previousSearchType;
					}
					beenSet = true;
					setText(componentSearchType.toString() + " Search");
					this.setGraphic(null);

					componentSearchTypeControlsHbox.getChildren().clear();

					SearchTypeFilter<?> filter = null;

					componentSearchTypeControlsHbox.getChildren().add(addIsDescendantOfFilterButton);
					if (componentSearchType == ComponentSearchType.LUCENE) {
						LuceneSearchTypeFilter displayableLuceneFilter = null;

						Label searchParamLabel = new Label("Lucene Param");
						searchParamLabel.setPadding(new Insets(5.0));

						TextField searchParamTextField = new TextField();

						if (componentContentModel.getSearchType() != null && componentContentModel.getSearchType().getComponentSearchType() == componentSearchType) {
							searchParamTextField.setText(componentContentModel.getSearchType().getSearchParameterProperty().get());
							displayableLuceneFilter = ((LuceneSearchTypeFilter)componentContentModel.getSearchType());
						} else {
							displayableLuceneFilter = new LuceneSearchTypeFilter();
							filter = displayableLuceneFilter;
							componentContentModel.setSearchType(filter);
						}

						addAllSearchFilters(componentContentModel);
						
						searchParamTextField.textProperty().addListener(new ChangeListener<String>() {
							@Override
							public void changed(
									ObservableValue<? extends String> observable,
									String oldValue, String newValue) {
								componentContentModel.getSearchType().getSearchParameterProperty().set(newValue);
							}
						});

						searchParamTextField.setPadding(new Insets(5.0));
						searchParamTextField.setPromptText("Enter search text");
						if (displayableLuceneFilter.getSearchParameter() != null) {
							searchParamTextField.setText(displayableLuceneFilter.getSearchParameter());
						}

						componentSearchTypeControlsHbox.getChildren().addAll(searchParamLabel, searchParamTextField);
						
						EnhancedSavedSearch.refreshSavedSearchComboBox();
					} else if (componentSearchType == ComponentSearchType.REGEXP) {
						RegExpSearchTypeFilter displayableRegExpFilter = null;

						Label searchParamLabel = new Label("RegExp Param");
						searchParamLabel.setPadding(new Insets(5.0));

						TextField searchParamTextField = new TextField();

						if (componentContentModel.getSearchType() != null && componentContentModel.getSearchType().getComponentSearchType() == componentSearchType) {
							searchParamTextField.setText(ComponentSearchTypeHelper.stripAllSurroundingRegExpSlashes(componentContentModel.getSearchType().getSearchParameterProperty().get()));
							displayableRegExpFilter = ((RegExpSearchTypeFilter)componentContentModel.getSearchType());
						} else {
							displayableRegExpFilter = new RegExpSearchTypeFilter();
							filter = displayableRegExpFilter;
							componentContentModel.setSearchType(filter);
						}

						addAllSearchFilters(componentContentModel);
						
						searchParamTextField.textProperty().addListener(new ChangeListener<String>() {
							@Override
							public void changed(
									ObservableValue<? extends String> observable,
									String oldValue, String newValue) {
								componentContentModel.getSearchType().getSearchParameterProperty().set(ComponentSearchTypeHelper.addSurroundingRegExpSlashesIfNotAlreadyThere(ComponentSearchTypeHelper.stripAllSurroundingRegExpSlashes(newValue)));
							}
						});

						searchParamTextField.setPadding(new Insets(5.0));
						searchParamTextField.setPromptText("Enter regexp text");
						if (displayableRegExpFilter.getSearchParameter() != null) {
							searchParamTextField.setText(ComponentSearchTypeHelper.stripAllSurroundingRegExpSlashes(displayableRegExpFilter.getSearchParameter()));
						}

						componentSearchTypeControlsHbox.getChildren().addAll(searchParamLabel, searchParamTextField);						

						EnhancedSavedSearch.refreshSavedSearchComboBox();
					} 
					else {
						LOG.warn("Unsupported ComponentSearchType {}", componentSearchType);
//						throw new RuntimeException("Unsupported ComponentSearchType " + componentSearchType);
					}

					componentSearchTypeControlsHbox.setUserData(filter);
				}
			}
		});
		componentSearchTypeComboBox.setOnAction((event) -> {
			LOG.trace("searchModel.getResultsTypeComboBox() event (selected: " + searchModel.getResultsTypeComboBox().getSelectionModel().getSelectedItem() + ")");

			searchModel.getSearchResultsTable().getResults().getItems().clear();
			searchModel.getSearchResultsTable().initializeSearchResultsTable(SearchType.TEXT, searchModel.getResultsTypeComboBox().getSelectionModel().getSelectedItem());
			previousSearchType = componentSearchTypeComboBox.getSelectionModel().getSelectedItem();
		});

		return componentContentParentPane;
	}

	private void removeSearchFilter(final NonSearchTypeFilter<?> filter, TextSearchTypeModel model) {
		// Create temp save list of nodes from searchFilterGridPane
		List<Node> newNodes = new ArrayList<>(searchFilterGridPane.getChildren());

		HBox row = null;
		
		for (Node gridPaneNode : searchFilterGridPane.getChildren()) {
			if (gridPaneNode.getUserData() == filter) {
				row = (HBox)gridPaneNode;
			}
		}
		
		if (row == null) {
			LOG.error("Specified filter not found in searchFilterGridPane containing {} nodes: {}", searchFilterGridPane.getChildren().size(), filter);
		}
		
		// Remove this node from temp save list of nodes
		newNodes.remove(row);

		// Remove this filter from searchModel
		int preRemovalSize = model.getFilters().size();
		model.getFilters().remove(filter);
		int postRemovalSize = model.getFilters().size();
		// Check before/after list size because bugs could cause identical componentContentModel.getFilters() to be created that should be the same exact filter
		// which would otherwise silently prevent removal from list
		if (postRemovalSize >= preRemovalSize || model.getFilters().contains(filter)) {
			LOG.error("FAILED removing filter " + filter + " from searchModel NonSearchTypeFilter list: " + Arrays.toString(model.getFilters().toArray()));
		} else {
			LOG.debug("searchModel no longer contains filter " + filter + ": " + Arrays.toString(model.getFilters().toArray()));
		}
		
		// Remove all nodes from searchFilterGridPane
		removeAllSearchFilters();

		// Recreate and add each node to searchFilterGridPane
		for (NonSearchTypeFilter<?> filterToAdd : model.getFilters()) {
			addSearchFilter(filterToAdd, model);
		}
	}
	private void removeAllSearchFilters() {
		searchFilterGridPane.getChildren().clear();
	}
	public void addAllSearchFilters(TextSearchTypeModel model) {
		removeAllSearchFilters();
		
		for (NonSearchTypeFilter<?> filter : model.getFilters()) {
			addSearchFilter(filter, model);
		}
	}
	private void addSearchFilter(final NonSearchTypeFilter<?> filter, TextSearchTypeModel model) {
		final int index = searchFilterGridPane.getChildren().size();

		HBox row = new HBox();
		HBox.setMargin(row, new Insets(5, 5, 5, 5));
		row.setUserData(filter);

		// TODO: add binding to disable deletion of first filter in list containing other filter
		Button removeFilterButton = new Button("Remove");
		removeFilterButton.setMinWidth(55);
		removeFilterButton.setPadding(new Insets(5.0));
		removeFilterButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				removeSearchFilter(filter, model);
			}
		});
		row.getChildren().add(removeFilterButton);

		if (filter instanceof IsDescendantOfFilter) {
			IsDescendantOfFilter displayableIsDescendantOfFilter = (IsDescendantOfFilter)filter;

			Label searchParamLabel = new Label("Ancestor");
			searchParamLabel.setPadding(new Insets(5.0));
			searchParamLabel.setMinWidth(70);

			CheckBox excludeMatchesCheckBox = new CheckBox("Exclude Matches");
			excludeMatchesCheckBox.setPadding(new Insets(5.0));
			excludeMatchesCheckBox.setMinWidth(150);
			excludeMatchesCheckBox.setSelected(((IsDescendantOfFilter) filter).getInvert());
			excludeMatchesCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(
						ObservableValue<? extends Boolean> observable,
						Boolean oldValue,
						Boolean newValue) {
					((IsDescendantOfFilter) filter).setInvert(newValue);
				}});

			final ConceptNode cn = new ConceptNode(null, false);
			cn.setPromptText("Type, drop or select a concept to add");
			//HBox.setHgrow(cn.getNode(), Priority.SOMETIMES);
			//HBox.setMargin(cn.getNode(), new Insets(5, 5, 5, 5));

			cn.getConceptProperty().addListener(new ChangeListener<ConceptVersionBI>()
			{
				@Override
				public void changed(ObservableValue<? extends ConceptVersionBI> observable, ConceptVersionBI oldValue, ConceptVersionBI newValue)
				{
					if (newValue != null)
					{
						displayableIsDescendantOfFilter.setNid(newValue.getConceptNid());
						LOG.debug("isDescendantFilter should now contain concept with NID " + displayableIsDescendantOfFilter.getNid() + ": " + Arrays.toString(model.getFilters().toArray()));
					} else {
						displayableIsDescendantOfFilter.setNid(0);
					}
				}
			});
			
			
			if (filter.isValid()) {
				cn.set(WBUtility.getConceptVersion(((IsDescendantOfFilter) filter).getNid()));
			}

			row.getChildren().addAll(searchParamLabel, cn.getNode(), excludeMatchesCheckBox);
		} else if (filter instanceof IsAFilter) {
			IsAFilter displayableIsAFilter = (IsAFilter)filter;

			Label searchParamLabel = new Label("Match");
			searchParamLabel.setPadding(new Insets(5.0));
			searchParamLabel.setMinWidth(70);

			CheckBox excludeMatchesCheckBox = new CheckBox("Exclude Matches");
			excludeMatchesCheckBox.setPadding(new Insets(5.0));
			excludeMatchesCheckBox.setMinWidth(150);
			excludeMatchesCheckBox.setSelected(((IsAFilter) filter).getInvert());
			excludeMatchesCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(
						ObservableValue<? extends Boolean> observable,
						Boolean oldValue,
						Boolean newValue) {
					((IsAFilter) filter).setInvert(newValue);
				}});

			final ConceptNode cn = new ConceptNode(null, false);
			cn.setPromptText("Type, drop or select a concept to add");
			//HBox.setHgrow(cn.getNode(), Priority.SOMETIMES);
			//HBox.setMargin(cn.getNode(), new Insets(5, 5, 5, 5));

			cn.getConceptProperty().addListener(new ChangeListener<ConceptVersionBI>()
			{
				@Override
				public void changed(ObservableValue<? extends ConceptVersionBI> observable, ConceptVersionBI oldValue, ConceptVersionBI newValue)
				{
					if (newValue != null)
					{
						displayableIsAFilter.setNid(newValue.getConceptNid());
						LOG.debug("isAFilter should now contain concept with NID " + displayableIsAFilter.getNid() + ": " + Arrays.toString(model.getFilters().toArray()));
					} else {
						displayableIsAFilter.setNid(0);
					}
				}
			});
			
			
			if (filter.isValid()) {
				cn.set(WBUtility.getConceptVersion(((IsAFilter) filter).getNid()));
			}

			row.getChildren().addAll(searchParamLabel, cn.getNode(), excludeMatchesCheckBox);
		}
		else {
			String msg = "Failed creating DisplayableFilter GridPane cell for filter of unsupported type " + filter.getClass().getName();
			LOG.error(msg);
			throw new RuntimeException(msg);
		}

		ComboBox<FilterType> filterTypeComboBox = new ComboBox<>();
		filterTypeComboBox.setEditable(false);
		filterTypeComboBox.setItems(FXCollections.observableArrayList(FilterType.values()));
		filterTypeComboBox.getSelectionModel().select(FilterType.valueOf(filter.getClass()));

		filterTypeComboBox.setOnAction((event) -> {
			LOG.trace("filterTypeComboBox event (selected: " + filterTypeComboBox.getSelectionModel().getSelectedItem() + ")");

			if (model.getFilters().size() >= (index + 1)) {
				// Model already has filter at this index
				if (FilterType.valueOf(model.getFilters().get(index).getClass()) == filterTypeComboBox.getSelectionModel().getSelectedItem()) {
					// Same type as existing filter.  Do nothing.
				} else {
					try {
						NonSearchTypeFilter<?> newFilter = filterTypeComboBox.getSelectionModel().getSelectedItem().getClazz().newInstance();
						NonSearchTypeFilter<?> existingFilter = model.getFilters().get(index);

						// Attempt to retain existing filter parameters, if possible
						if ((existingFilter instanceof Invertable) && (newFilter instanceof Invertable)) {
							((Invertable)newFilter).setInvert(((Invertable)existingFilter).getInvert());
						}
						if ((existingFilter instanceof SingleNidFilter) && (newFilter instanceof SingleNidFilter)) {
							((SingleNidFilter)newFilter).setNid(((SingleNidFilter)existingFilter).getNid());
						}
						
						model.getFilters().set(index, newFilter);
					
						// Remove all nodes from searchFilterGridPane
						removeAllSearchFilters();

						// Recreate and add each node to searchFilterGridPane
						for (NonSearchTypeFilter<?> f : model.getFilters()) {
							addSearchFilter(f, model);
						}
					} catch (Exception e) {
						String msg = "Failed creating new " + filterTypeComboBox.getSelectionModel().getSelectedItem() + " filter at index " + index;

						String details = msg + ". Caught " + e.getClass().getName() + " \"" + e.getLocalizedMessage() + "\"";

						String title = "Failed creating new filter";
						AppContext.getCommonDialogs().showErrorDialog(title, msg, details, AppContext.getMainApplicationWindow().getPrimaryStage());

						e.printStackTrace();
					}
				}
			}
		});		
		
		row.getChildren().add(filterTypeComboBox);
		
		searchFilterGridPane.addRow(index, row);
		RowConstraints rowConstraints = new RowConstraints();
		rowConstraints.setVgrow(Priority.NEVER);
		searchFilterGridPane.getRowConstraints().add(index, rowConstraints);
	}

	/*
	 * This method adds new DisplayableFilter to both GridPane and searchModel,
	 * if DisplayableFilter not already in searchModel
	 * 
	 * It also adds a Remove button that removes the specified DisplayableFilter from
	 * both the GridPane and the searchModel
	 * 
	 */
}
