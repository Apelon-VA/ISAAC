/**
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

/**
 * QueryBuilderHelper
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.gui.querybuilder;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.gui.ConceptNode;
import gov.va.isaac.gui.querybuilder.node.AssertionNode;
import gov.va.isaac.gui.querybuilder.node.InvertableNode;
import gov.va.isaac.gui.querybuilder.node.LogicalNode;
import gov.va.isaac.gui.querybuilder.node.NodeDraggable;
import gov.va.isaac.gui.querybuilder.node.RefsetContainsConcept;
import gov.va.isaac.gui.querybuilder.node.RefsetContainsKindOfConcept;
import gov.va.isaac.gui.querybuilder.node.RefsetContainsString;
import gov.va.isaac.gui.querybuilder.node.RelRestriction;
import gov.va.isaac.gui.querybuilder.node.RelType;
import gov.va.isaac.gui.querybuilder.node.SingleConceptAssertionNode;
import gov.va.isaac.gui.querybuilder.node.SingleStringAssertionNode;
import gov.va.isaac.interfaces.utility.DialogResponse;
import gov.va.isaac.search.CompositeSearchResult;
import gov.va.isaac.util.ComponentDescriptionHelper;
import gov.va.isaac.util.OTFUtility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.GridPane;

import org.ihtsdo.otf.query.implementation.Clause;
import org.ihtsdo.otf.query.implementation.Query;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetItrBI;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * QueryBuilderHelper
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public class QueryBuilderHelper {
	private final static Logger logger = LoggerFactory.getLogger(QueryBuilderHelper.class);
	
	private final static BdbTerminologyStore dataStore = ExtendedAppContext.getDataStore();

	private static NativeIdSetBI results;

	/**
	 * 
	 */
	private QueryBuilderHelper() {
	}

	public static boolean getUserConfirmToDeleteNodeIfNecessary(TreeItem<NodeDraggable> node) {
		boolean delete = true;
		if (node.getChildren().size() > 0
				|| (node.getValue() != null && (node.getValue() instanceof AssertionNode) && node.getValue().getIsValid())) {
			int numDescendants = QueryBuilderHelper.getDescendants(node).size();

			DialogResponse response = AppContext.getCommonDialogs().showYesNoDialog("Expression Deletion Confirmation", "Are you sure you want to delete expression\n" + node.getValue().getDescription() + "?" + (numDescendants > 0 ? ("\n\n" + numDescendants + " descendent expression(s) will also be deleted") : ""));
			if (response == DialogResponse.YES) {
				delete = true;
			} else {
				delete = false;
			}
		}
		
		return delete;
	}
	public static void initializeQueryNodeTreeView(TreeView<NodeDraggable> tree, GridPane nodeEditorGridPane, BooleanProperty queryNodeTreeViewIsValidProperty) {
		tree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

		tree.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<TreeItem<NodeDraggable>>() {
			@Override
			public void onChanged(
					javafx.collections.ListChangeListener.Change<? extends TreeItem<NodeDraggable>> c) {
				
				if (tree.getSelectionModel().getSelectedItems().size() == 1) {
					QueryBuilderHelper.populateNodeEditorGridPane(nodeEditorGridPane, tree.getSelectionModel().getSelectedItems().get(0).getValue());
				}
			}
		});

		tree.rootProperty().addListener(new ChangeListener<TreeItem<NodeDraggable>>() {
			@Override
			public void changed(
					ObservableValue<? extends TreeItem<NodeDraggable>> observable,
					TreeItem<NodeDraggable> oldValue,
					TreeItem<NodeDraggable> newValue) {
				if (newValue != null) {
					queryNodeTreeViewIsValidProperty.set(QueryBuilderHelper.isQueryNodeTreeViewValid(tree));
				} else {
					queryNodeTreeViewIsValidProperty.set(false);
				}
			}
		});
	}
	
	private static CheckBox createInversionCheckBox(InvertableNode node) {
		CheckBox inversionCheckBox = new CheckBox();
		inversionCheckBox.setText("Invert (NOT)");
		inversionCheckBox.setSelected(node.getInvert());

		inversionCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(
					ObservableValue<? extends Boolean> observable,
					Boolean oldValue,
					Boolean newValue) {
				node.setInvert(newValue);
			}
		});

		return inversionCheckBox;
	}
	public static void populateNodeEditorGridPane(GridPane nodeEditorGridPane, NodeDraggable draggableNode) {

		if (draggableNode == null) {
			String error = "populateNodeEditorGridPane() passed null node";
			Log.warn(error);
		} else {
			if (draggableNode instanceof LogicalNode) {
				LogicalNode logicalNode = (LogicalNode)draggableNode;
				nodeEditorGridPane.getChildren().clear();
				int rowIndex = 0;
				Label nodeEditorLabel = new Label(logicalNode.getNodeTypeName());
				nodeEditorGridPane.addRow(rowIndex++, nodeEditorLabel);
				nodeEditorGridPane.addRow(rowIndex++, new Label());

				CheckBox inversionCheckBox = createInversionCheckBox(logicalNode);

				nodeEditorGridPane.addRow(rowIndex++, inversionCheckBox);
			} else if (draggableNode instanceof SingleConceptAssertionNode) {
				SingleConceptAssertionNode singleConceptAssertionNode = (SingleConceptAssertionNode)draggableNode;
				nodeEditorGridPane.getChildren().clear();
				int rowIndex = 0;
				Label nodeEditorLabel = new Label(singleConceptAssertionNode.getNodeTypeName());
				nodeEditorGridPane.addRow(rowIndex++, nodeEditorLabel);
				nodeEditorGridPane.addRow(rowIndex++, new Label());
				ConceptVersionBI currentConcept = null;
				if (singleConceptAssertionNode.getNid() != null) {
					currentConcept = OTFUtility.getConceptVersion(singleConceptAssertionNode.getNid());
				}
				ConceptNode conceptNode = new ConceptNode(currentConcept, true);
				conceptNode.getConceptProperty().addListener(new ChangeListener<ConceptVersionBI>() {
					@Override
					public void changed(
							ObservableValue<? extends ConceptVersionBI> observable,
							ConceptVersionBI oldValue,
							ConceptVersionBI newValue) {
						if (newValue == null) {
							singleConceptAssertionNode.getNidProperty().set(0);
						} else {
							singleConceptAssertionNode.setNid(newValue.getConceptNid());
						}
					}
				});
				nodeEditorGridPane.addRow(rowIndex++, new Label("Concept"), conceptNode.getNode());

				CheckBox inversionCheckBox = createInversionCheckBox(singleConceptAssertionNode);

				nodeEditorGridPane.addRow(rowIndex++, inversionCheckBox);
			} else if (draggableNode instanceof SingleStringAssertionNode) {
				SingleStringAssertionNode singleStringAssertionNode = (SingleStringAssertionNode)draggableNode;
				nodeEditorGridPane.getChildren().clear();
				int rowIndex = 0;
				Label nodeEditorLabel = new Label(singleStringAssertionNode.getNodeTypeName());
				nodeEditorGridPane.addRow(rowIndex++, nodeEditorLabel);
				nodeEditorGridPane.addRow(rowIndex++, new Label());

				TextField stringNode = ((SingleStringAssertionNode)draggableNode).getStringInputField();

				if (singleStringAssertionNode != null) {
					stringNode.setText(singleStringAssertionNode.getString());
				}

				nodeEditorGridPane.addRow(rowIndex++, new Label("Concept"), stringNode);

				CheckBox inversionCheckBox = createInversionCheckBox(singleStringAssertionNode);

				nodeEditorGridPane.addRow(rowIndex++, inversionCheckBox);
			} else if (draggableNode instanceof RelRestriction) {
				RelRestriction relRestrictionNode = (RelRestriction)draggableNode;
				nodeEditorGridPane.getChildren().clear();
				int rowIndex = 0;
				Label nodeEditorLabel = new Label(relRestrictionNode.getNodeTypeName());
				nodeEditorGridPane.addRow(rowIndex++, nodeEditorLabel);
				nodeEditorGridPane.addRow(rowIndex++, new Label());
				
				{
					ConceptVersionBI currentRelRestrictionConcept = null;
					if (relRestrictionNode.getRelRestrictionConceptNid() != null) {
						currentRelRestrictionConcept = OTFUtility.getConceptVersion(relRestrictionNode.getRelRestrictionConceptNid());
					}
					ConceptNode relRestrictionConceptNode = new ConceptNode(currentRelRestrictionConcept, true);
					relRestrictionConceptNode.getConceptProperty().addListener(new ChangeListener<ConceptVersionBI>() {
						@Override
						public void changed(
								ObservableValue<? extends ConceptVersionBI> observable,
								ConceptVersionBI oldValue,
								ConceptVersionBI newValue) {
							if (newValue == null) {
								relRestrictionNode.setRelRestrictionConceptNid(0);
							} else {
								relRestrictionNode.setRelRestrictionConceptNid(newValue.getConceptNid());
							}
						}
					});
					nodeEditorGridPane.addRow(rowIndex++, new Label("Restriction Concept"), relRestrictionConceptNode.getNode());
				}
				{
					ConceptVersionBI currentRelTypeConcept = null;
					if (relRestrictionNode.getRelTypeConceptNid() != null) {
						currentRelTypeConcept = OTFUtility.getConceptVersion(relRestrictionNode.getRelTypeConceptNid());
					}
					ConceptNode relTypeConceptNode = new ConceptNode(currentRelTypeConcept, true);
					relTypeConceptNode.getConceptProperty().addListener(new ChangeListener<ConceptVersionBI>() {
						@Override
						public void changed(
								ObservableValue<? extends ConceptVersionBI> observable,
								ConceptVersionBI oldValue,
								ConceptVersionBI newValue) {
							if (newValue == null) {
								relRestrictionNode.setRelTypeConceptNid(0);
							} else {
								relRestrictionNode.setRelTypeConceptNid(newValue.getConceptNid());
							}
						}
					});
					nodeEditorGridPane.addRow(rowIndex++, new Label("RelType Concept"), relTypeConceptNode.getNode());
				}
				{
					ConceptVersionBI currentSourceConcept = null;
					if (relRestrictionNode.getSourceConceptNid() != null) {
						currentSourceConcept = OTFUtility.getConceptVersion(relRestrictionNode.getSourceConceptNid());
					}
					ConceptNode sourceConceptNode = new ConceptNode(currentSourceConcept, true);
					sourceConceptNode.getConceptProperty().addListener(new ChangeListener<ConceptVersionBI>() {
						@Override
						public void changed(
								ObservableValue<? extends ConceptVersionBI> observable,
								ConceptVersionBI oldValue,
								ConceptVersionBI newValue) {
							if (newValue == null) {
								relRestrictionNode.setSourceConceptNid(0);
							} else {
								relRestrictionNode.setSourceConceptNid(newValue.getConceptNid());
							}
						}
					});
					nodeEditorGridPane.addRow(rowIndex++, new Label("Target Concept"), sourceConceptNode.getNode());
				}
				{
					CheckBox destinationSubsumptionCheckBox = new CheckBox();
					destinationSubsumptionCheckBox.setText("Use Target Subsumption");
					destinationSubsumptionCheckBox.setSelected(relRestrictionNode.getUseDestinationSubsumption());

					destinationSubsumptionCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
						@Override
						public void changed(
								ObservableValue<? extends Boolean> observable,
								Boolean oldValue,
								Boolean newValue) {
							relRestrictionNode.setUseDestinationSubsumption(newValue);
						}
					});

					nodeEditorGridPane.addRow(rowIndex++, destinationSubsumptionCheckBox);
				}
				{
					CheckBox relTypeSubsumptionCheckBox = new CheckBox();
					relTypeSubsumptionCheckBox.setText("Use RelType Subsumption");
					relTypeSubsumptionCheckBox.setSelected(relRestrictionNode.getUseRelTypeSubsumption());

					relTypeSubsumptionCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
						@Override
						public void changed(
								ObservableValue<? extends Boolean> observable,
								Boolean oldValue,
								Boolean newValue) {
							relRestrictionNode.setUseRelTypeSubsumption(newValue);
						}
					});

					nodeEditorGridPane.addRow(rowIndex++, relTypeSubsumptionCheckBox);
				}

				CheckBox inversionCheckBox = createInversionCheckBox(relRestrictionNode);

				nodeEditorGridPane.addRow(rowIndex++, inversionCheckBox);
			} else if (draggableNode instanceof RelType) {
				RelType relTypeNode = (RelType)draggableNode;
				nodeEditorGridPane.getChildren().clear();
				int rowIndex = 0;
				Label nodeEditorLabel = new Label(relTypeNode.getNodeTypeName());
				nodeEditorGridPane.addRow(rowIndex++, nodeEditorLabel);
				nodeEditorGridPane.addRow(rowIndex++, new Label());

				{
					ConceptVersionBI currentRelTypeConcept = null;
					if (relTypeNode.getRelTypeConceptNid() != null) {
						currentRelTypeConcept = OTFUtility.getConceptVersion(relTypeNode.getRelTypeConceptNid());
					}
					ConceptNode relTypeConceptNode = new ConceptNode(currentRelTypeConcept, true);
					relTypeConceptNode.getConceptProperty().addListener(new ChangeListener<ConceptVersionBI>() {
						@Override
						public void changed(
								ObservableValue<? extends ConceptVersionBI> observable,
								ConceptVersionBI oldValue,
								ConceptVersionBI newValue) {
							if (newValue == null) {
								relTypeNode.setRelTypeConceptNid(0);
							} else {
								relTypeNode.setRelTypeConceptNid(newValue.getConceptNid());
							}
						}
					});
					nodeEditorGridPane.addRow(rowIndex++, new Label("RelType Concept"), relTypeConceptNode.getNode());
				}
				{
					ConceptVersionBI currentTargetConcept = null;
					if (relTypeNode.getTargetConceptNid() != null) {
						currentTargetConcept = OTFUtility.getConceptVersion(relTypeNode.getTargetConceptNid());
					}
					ConceptNode targetConceptNode = new ConceptNode(currentTargetConcept, true);
					targetConceptNode.getConceptProperty().addListener(new ChangeListener<ConceptVersionBI>() {
						@Override
						public void changed(
								ObservableValue<? extends ConceptVersionBI> observable,
								ConceptVersionBI oldValue,
								ConceptVersionBI newValue) {
							if (newValue == null) {
								relTypeNode.setTargetConceptNid(0);
							} else {
								relTypeNode.setTargetConceptNid(newValue.getConceptNid());
							}
						}
					});
					nodeEditorGridPane.addRow(rowIndex++, new Label("Target Concept"), targetConceptNode.getNode());
				}
				{
					CheckBox subsumptionCheckBox = new CheckBox();
					subsumptionCheckBox.setText("Use RelType Subsumption");
					subsumptionCheckBox.setSelected(relTypeNode.getUseSubsumption());

					subsumptionCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
						@Override
						public void changed(
								ObservableValue<? extends Boolean> observable,
								Boolean oldValue,
								Boolean newValue) {
							relTypeNode.setUseSubsumption(newValue);
						}
					});

					nodeEditorGridPane.addRow(rowIndex++, subsumptionCheckBox);
				}

				CheckBox inversionCheckBox = createInversionCheckBox(relTypeNode);

				nodeEditorGridPane.addRow(rowIndex++, inversionCheckBox);
			} else if (draggableNode instanceof RefsetContainsConcept) {
				RefsetContainsConcept refsetContainsConceptNode = (RefsetContainsConcept)draggableNode;
				nodeEditorGridPane.getChildren().clear();
				int rowIndex = 0;
				Label nodeEditorLabel = new Label(refsetContainsConceptNode.getNodeTypeName());
				nodeEditorGridPane.addRow(rowIndex++, nodeEditorLabel);
				nodeEditorGridPane.addRow(rowIndex++, new Label());
				
				{
					ConceptVersionBI currentRelTypeConcept = null;
					if (refsetContainsConceptNode.getRefsetConceptNid() != null) {
						currentRelTypeConcept = OTFUtility.getConceptVersion(refsetContainsConceptNode.getRefsetConceptNid());
					}
					ConceptNode relTypeConceptNode = new ConceptNode(currentRelTypeConcept, true);
					relTypeConceptNode.getConceptProperty().addListener(new ChangeListener<ConceptVersionBI>() {
						@Override
						public void changed(
								ObservableValue<? extends ConceptVersionBI> observable,
								ConceptVersionBI oldValue,
								ConceptVersionBI newValue) {
							if (newValue == null) {
								refsetContainsConceptNode.setRefsetConceptNid(0);
							} else {
								refsetContainsConceptNode.setRefsetConceptNid(newValue.getConceptNid());
							}
						}
					});
					nodeEditorGridPane.addRow(rowIndex++, new Label("Refset Concept"), relTypeConceptNode.getNode());
				}
				{
					ConceptVersionBI currentTargetConcept = null;
					if (refsetContainsConceptNode.getConceptNid() != null) {
						currentTargetConcept = OTFUtility.getConceptVersion(refsetContainsConceptNode.getConceptNid());
					}
					ConceptNode targetConceptNode = new ConceptNode(currentTargetConcept, true);
					targetConceptNode.getConceptProperty().addListener(new ChangeListener<ConceptVersionBI>() {
						@Override
						public void changed(
								ObservableValue<? extends ConceptVersionBI> observable,
								ConceptVersionBI oldValue,
								ConceptVersionBI newValue) {
							if (newValue == null) {
								refsetContainsConceptNode.setConceptNid(0);
							} else {
								refsetContainsConceptNode.setConceptNid(newValue.getConceptNid());
							}
						}
					});
					nodeEditorGridPane.addRow(rowIndex++, new Label("Concept"), targetConceptNode.getNode());
				}
				
				CheckBox inversionCheckBox = createInversionCheckBox(refsetContainsConceptNode);

				nodeEditorGridPane.addRow(rowIndex++, inversionCheckBox);
			} else if (draggableNode instanceof RefsetContainsKindOfConcept) {
				RefsetContainsKindOfConcept refsetContainsKindOfConceptNode = (RefsetContainsKindOfConcept)draggableNode;
				nodeEditorGridPane.getChildren().clear();
				int rowIndex = 0;
				Label nodeEditorLabel = new Label(refsetContainsKindOfConceptNode.getNodeTypeName());
				nodeEditorGridPane.addRow(rowIndex++, nodeEditorLabel);
				nodeEditorGridPane.addRow(rowIndex++, new Label());
				
				{
					ConceptVersionBI currentRelTypeConcept = null;
					if (refsetContainsKindOfConceptNode.getRefsetConceptNid() != null) {
						currentRelTypeConcept = OTFUtility.getConceptVersion(refsetContainsKindOfConceptNode.getRefsetConceptNid());
					}
					ConceptNode relTypeConceptNode = new ConceptNode(currentRelTypeConcept, true);
					relTypeConceptNode.getConceptProperty().addListener(new ChangeListener<ConceptVersionBI>() {
						@Override
						public void changed(
								ObservableValue<? extends ConceptVersionBI> observable,
								ConceptVersionBI oldValue,
								ConceptVersionBI newValue) {
							if (newValue == null) {
								refsetContainsKindOfConceptNode.setRefsetConceptNid(0);
							} else {
								refsetContainsKindOfConceptNode.setRefsetConceptNid(newValue.getConceptNid());
							}
						}
					});
					nodeEditorGridPane.addRow(rowIndex++, new Label("Refset Concept"), relTypeConceptNode.getNode());
				}
				{
					ConceptVersionBI currentTargetConcept = null;
					if (refsetContainsKindOfConceptNode.getConceptNid() != null) {
						currentTargetConcept = OTFUtility.getConceptVersion(refsetContainsKindOfConceptNode.getConceptNid());
					}
					ConceptNode targetConceptNode = new ConceptNode(currentTargetConcept, true);
					targetConceptNode.getConceptProperty().addListener(new ChangeListener<ConceptVersionBI>() {
						@Override
						public void changed(
								ObservableValue<? extends ConceptVersionBI> observable,
								ConceptVersionBI oldValue,
								ConceptVersionBI newValue) {
							if (newValue == null) {
								refsetContainsKindOfConceptNode.setConceptNid(0);
							} else {
								refsetContainsKindOfConceptNode.setConceptNid(newValue.getConceptNid());
							}
						}
					});
					nodeEditorGridPane.addRow(rowIndex++, new Label("Concept"), targetConceptNode.getNode());
				}
				
				CheckBox inversionCheckBox = createInversionCheckBox(refsetContainsKindOfConceptNode);

				nodeEditorGridPane.addRow(rowIndex++, inversionCheckBox);
			} else if (draggableNode instanceof RefsetContainsString) {
				RefsetContainsString refsetContainsKindOfConceptNode = (RefsetContainsString)draggableNode;
				nodeEditorGridPane.getChildren().clear();
				int rowIndex = 0;
				Label nodeEditorLabel = new Label(refsetContainsKindOfConceptNode.getNodeTypeName());
				nodeEditorGridPane.addRow(rowIndex++, nodeEditorLabel);
				nodeEditorGridPane.addRow(rowIndex++, new Label());
				
				{
					ConceptVersionBI refsetConcept = null;
					if (refsetContainsKindOfConceptNode.getRefsetConceptNid() != null) {
						refsetConcept = OTFUtility.getConceptVersion(refsetContainsKindOfConceptNode.getRefsetConceptNid());
					}
					ConceptNode refsetConceptNode = new ConceptNode(refsetConcept, true);
					refsetConceptNode.getConceptProperty().addListener(new ChangeListener<ConceptVersionBI>() {
						@Override
						public void changed(
								ObservableValue<? extends ConceptVersionBI> observable,
								ConceptVersionBI oldValue,
								ConceptVersionBI newValue) {
							if (newValue == null) {
								refsetContainsKindOfConceptNode.setRefsetConceptNid(0);
							} else {
								refsetContainsKindOfConceptNode.setRefsetConceptNid(newValue.getConceptNid());
							}
						}
					});
					nodeEditorGridPane.addRow(rowIndex++, new Label("Refset Concept"), refsetConceptNode.getNode());
				}
				{
					String queryText = refsetContainsKindOfConceptNode.getQueryText();
					
					TextField queryTextField = new TextField(queryText);
					queryTextField.textProperty().addListener(new ChangeListener<String>() {
						@Override
						public void changed(
								ObservableValue<? extends String> observable,
								String oldValue,
								String newValue) {
							refsetContainsKindOfConceptNode.setQueryText(newValue);;
						}
					});
					nodeEditorGridPane.addRow(rowIndex++, new Label("Query"), queryTextField);
				}
				
				CheckBox inversionCheckBox = createInversionCheckBox(refsetContainsKindOfConceptNode);

				nodeEditorGridPane.addRow(rowIndex++, inversionCheckBox);
			}
		}
	}

	public static boolean isQueryNodeTreeViewValid(TreeView<NodeDraggable> tree) {
		return isValidExpression(tree.getRoot());
	}
	public static boolean isValidExpression(TreeItem<NodeDraggable> treeNode) {
		if (treeNode == null) {
			return false;
		}
		if (treeNode.getValue() == null) {
			return false;
		}
		if (treeNode.getValue() instanceof AssertionNode) {
			return ((AssertionNode)treeNode.getValue()).getIsValid();
		} else if (treeNode.getValue() instanceof LogicalNode) {
			LogicalNode logicalNode = (LogicalNode)treeNode.getValue();
			int numChildren = treeNode.getChildren().size();
			if (numChildren > logicalNode.getMaxChildren() || numChildren < logicalNode.getMinimumChildren()) {
				return false;
			}
			Boolean childrenValid = null;
			for (TreeItem<NodeDraggable> childTreeNode : treeNode.getChildren()) {
				if (isValidExpression(childTreeNode)) {
					if (childrenValid == null) {
						childrenValid = true;
					}
				} else {
					childrenValid = false;
				}
			}
			
			return childrenValid != null ? childrenValid : false;
		} else {
			logger.warn("isValidExpression() encountered node {} of unexpected type {}.  Expected AssertionNode or LogicalNode.", treeNode.getValue().getDescription(), treeNode.getValue());
			return false;
		}
	}
	
	public static String getDescription(int nid) {
		String componentDescription = null;
		ComponentVersionBI component = OTFUtility.getComponentVersion(nid);
		if (component != null) {
			componentDescription = ComponentDescriptionHelper.getComponentDescription(component);
		}
		if (componentDescription == null) {
			componentDescription = OTFUtility.getDescriptionIfConceptExists(nid);
		}
		if (componentDescription == null) {
			try {
				componentDescription = OTFUtility.getConPrefTerm(nid);
			} catch (Exception e) {
				//
			}
		}
		
		return componentDescription;
	}
	
	public static List<TreeItem<NodeDraggable>> getDescendants(TreeItem<NodeDraggable> item) {
		List<TreeItem<NodeDraggable>> descendants = new ArrayList<>();

		for (TreeItem<NodeDraggable> childItem : item.getChildren()) {
			descendants.add(childItem);
			descendants.addAll(getDescendants(childItem));
		}
		
		return descendants;
	}
	public static void executeQuery(Query query, TreeView<NodeDraggable> tree) throws IOException, Exception {
		results = generateQuery(tree).compute();
	}

	public static Query generateQuery(TreeView<NodeDraggable> tree) {
		logger.debug("Generating Query...");

		// This should never happen if Build button is properly disabled
		if (! QueryBuilderHelper.isQueryNodeTreeViewValid(tree)) {
			String error = "Cannot generate Query from invalid clause tree";
			logger.error(error);
			throw new RuntimeException(error);
		}

		ViewCoordinate viewCoordinate = null;
		try {
			viewCoordinate = StandardViewCoordinates.getSnomedInferredLatest();
		} catch (IOException ex) {
			logger.error("Failed getting default ViewCoordinate. Caught {} \"{}\"", ex.getClass().getName(), ex.getLocalizedMessage());
		}

		Query syntheticQuery = new Query(viewCoordinate) {
			
			@Override
			protected NativeIdSetBI For() throws IOException {
				return dataStore.getAllConceptNids();
			}

			@Override
			public void Let() throws IOException {
			}

			@Override
			public Clause Where() {
				return ClauseFactory.createClause(this, tree.getRoot());
			}
		};
		
		return syntheticQuery;
	}

	public static Collection<CompositeSearchResult>  getResults() throws IOException {
		Set<CompositeSearchResult> collection = new HashSet<CompositeSearchResult>();
		
		if (results != null) {
			NativeIdSetItrBI itr = results.getSetBitIterator();
            while (itr.next()) {
            	int nid = itr.nid();
            	ConceptVersionBI con = OTFUtility.getConceptVersion(nid);
				CompositeSearchResult nidResult = new CompositeSearchResult(con, 0);
				collection.add(nidResult);
			}
        }

        return collection;
	}
}
