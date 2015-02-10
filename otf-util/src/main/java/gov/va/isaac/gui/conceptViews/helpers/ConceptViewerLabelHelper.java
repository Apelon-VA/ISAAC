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
 *	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.gui.conceptViews.helpers;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.gui.conceptViews.enhanced.EnhancedConceptView;
import gov.va.isaac.gui.conceptViews.enhanced.PreferredAcceptabilityPrompt;
import gov.va.isaac.gui.conceptViews.enhanced.RetireConceptPrompt;
import gov.va.isaac.gui.conceptViews.modeling.ConceptModelingPopup;
import gov.va.isaac.gui.conceptViews.modeling.DescriptionModelingPopup;
import gov.va.isaac.gui.conceptViews.modeling.ModelingPopup;
import gov.va.isaac.gui.conceptViews.modeling.RelationshipModelingPopup;
import gov.va.isaac.gui.dialog.UserPrompt.UserPromptResponse;
import gov.va.isaac.gui.util.CustomClipboard;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.PopupConceptViewI;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.WorkflowInitiationViewI;
import gov.va.isaac.util.OTFUtility;
import java.io.IOException;
import java.util.Collection;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.ihtsdo.otf.tcc.api.blueprint.ConceptAttributeAB;
import org.ihtsdo.otf.tcc.api.blueprint.DescriptionCAB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RelationshipCAB;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeChronicleBI;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.ComponentType;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipType;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
*
* @author <a href="jefron@apelon.com">Jesse Efron</a>
*/
public class ConceptViewerLabelHelper {
	
	private final Logger LOG = LoggerFactory.getLogger(this.getClass());
	private int conceptNid = 0;;
	
	private AnchorPane pane;
	private boolean isWindow = false;
	private ObservableList<Integer> previousConceptStack;
	
	private ConceptViewerTooltipHelper tooltipHelper = new ConceptViewerTooltipHelper();

	private PopupConceptViewI conceptView = null;

	public ConceptViewerLabelHelper(PopupConceptViewI conceptView) {
		this.conceptView = conceptView;
	}

	// Create Labels
	public Label createLabel(ComponentVersionBI comp, String txt, ComponentType type, int refConNid) {
		Label label = new Label();
		label.setFont(new Font(18));

		initializeLabel(label, comp, type, txt, refConNid);
		
		return label;
	}

	public void initializeLabel(Label label, ComponentVersionBI comp, ComponentType type, String txt, int refConNid) {
		label.setText(txt);
		
		if (refConNid != 0) {
			label.setTextFill(Color.BLUE);
		} else {
			label.setTextFill(Color.BLACK);
		}
		
		createContextMenu(label, txt, comp, refConNid, type);

		// Tooltip Handling
		tooltipHelper.setDefaultTooltip(label, comp, type);
		label.addEventHandler(MouseEvent.MOUSE_ENTERED, tooltipHelper.getCompTooltipEnterHandler(comp, type));
		label.addEventHandler(MouseEvent.MOUSE_EXITED, tooltipHelper.getCompTooltipExitHandler(comp, type));
	}

	
	
	// Create Context Menus
	private void createContextMenu(Label label, String txt, ComponentVersionBI comp, int refConNid, ComponentType type) {
		final ContextMenu rtClickMenu = new ContextMenu();

		Menu copytoClipboardItem = new Menu("Copy to Clipboard");
		MenuItem copyTextItem = new MenuItem("Text");
		copyTextItem.setGraphic(Images.COPY.createImageView());
		copyTextItem.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				CustomClipboard.set(txt);
			}
		});
				
		MenuItem copyContentItem = new MenuItem("Full Component Content");
		copyContentItem.setGraphic(Images.COPY.createImageView());
		copyContentItem.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				CustomClipboard.set(label.getTooltip().getText());
			}
		});
		
		copytoClipboardItem.getItems().addAll(copyTextItem, copyContentItem);
		rtClickMenu.getItems().add(copytoClipboardItem);

		
		// Enable copying of component's various Ids
		if (comp != null) {
			if (type != ComponentType.CONCEPT && 
				(!(type == ComponentType.RELATIONSHIP && conceptNid != ((RelationshipVersionBI<?>)comp).getOriginNid()))) { 
				Menu modifyComponentMenu = addModifyMenus(comp, type);
				rtClickMenu.getItems().add(modifyComponentMenu);
			}

			Menu copyIdMenu = addIdMenus(comp, type);
			copytoClipboardItem.getItems().add(copyIdMenu);
		}

		if (comp != null) {
			MenuItem initiateWorkflowItem = new MenuItem("Initiate Workflow on " + type);
			initiateWorkflowItem.setGraphic(Images.INBOX.createImageView());
			initiateWorkflowItem.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent event)
				{
					WorkflowInitiationViewI view = AppContext.getService(WorkflowInitiationViewI.class);
					if (view == null) {
						LOG.error("HK2 FAILED to provide requested service: " + WorkflowInitiationViewI.class);
					}
					if (type == ComponentType.CONCEPT) {
						view.setComponent(comp.getConceptNid());
					} else {
						view.setComponent(comp.getNid());
					}
					view.showView(AppContext.getMainApplicationWindow().getPrimaryStage());
				}
			});
			
			rtClickMenu.getItems().add(0, initiateWorkflowItem);
		}
		
		
		// Enable changing concept to Reference Concept
		if (refConNid != 0) {
			assert(comp != null);
			
			if (isWindow) {
				MenuItem viewItem = new MenuItem("Open Concept");
				viewItem.setGraphic(Images.CONCEPT_VIEW.createImageView());
				viewItem.setOnAction(new EventHandler<ActionEvent>()
				{
					@Override
					public void handle(ActionEvent event)
					{
						previousConceptStack.add(comp.getConceptNid());
						if (conceptView == null) {
							AppContext.getService(EnhancedConceptView.class).setConcept(refConNid);
						} else {
							conceptView.setConcept(refConNid);
						}
					}
				});
				
				rtClickMenu.getItems().add(0, viewItem);
			}
	
			MenuItem viewNewItem = new MenuItem("Open Concept in New Panel");
			viewNewItem.setGraphic(Images.CONCEPT_VIEW.createImageView());
			viewNewItem.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent event)
				{
					EnhancedConceptView cv = AppContext.getService(EnhancedConceptView.class);
				
					cv.setConcept(refConNid);
					cv.showView(pane.getScene().getWindow());
				}
			});
			
			rtClickMenu.getItems().add(1, viewNewItem);
		}

		rtClickMenu.getItems().add(addCreateNewComponent());
		
		label.setContextMenu(rtClickMenu);
	}

	Menu addModifyMenus(ComponentVersionBI comp, ComponentType type) {
		Menu modifyComponentMenu = new Menu("Modify " + type);
		MenuItem editComponentMenu = new MenuItem("Edit");
		MenuItem retireComponentMenu = new MenuItem("Retire");
		MenuItem undoComponentMenu = new MenuItem("Undo");
		
		modifyComponentMenu.getItems().addAll(editComponentMenu, retireComponentMenu, undoComponentMenu);
		
		editComponentMenu.setGraphic(Images.EDIT.createImageView());
		editComponentMenu.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				ModelingPopup popup = null;
				if (type == ComponentType.CONCEPT) {
					popup = AppContext.getService(ConceptModelingPopup.class);
				} else if (type == ComponentType.DESCRIPTION) {
					popup = AppContext.getService(DescriptionModelingPopup.class);
				} else if (type == ComponentType.RELATIONSHIP) {
					popup = AppContext.getService(RelationshipModelingPopup.class);
					if (conceptNid != ((RelationshipVersionBI<?>)comp).getOriginNid()) { 
						((RelationshipModelingPopup)popup).setDestination(true);
					}
				}

				popup.finishInit(comp, conceptView);
				popup.showView(pane.getScene().getWindow());
			}
		});

		retireComponentMenu.setGraphic(Images.DELETE.createImageView());
		retireComponentMenu.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				try {
					if (type == ComponentType.CONCEPT) {
						ConceptVersionBI con = OTFUtility.getConceptVersion(comp.getConceptNid());

						if (!OTFUtility.getAllChildrenOfConcept(con, false).isEmpty()) {
							AppContext.getCommonDialogs().showInformationDialog("Retire Concept Failure", "Cannot retire concept until it has no children");
						} else {
							RetireConceptPrompt prompt = new RetireConceptPrompt();
							
							prompt.showUserPrompt((Stage)pane.getScene().getWindow(), "Retire Concept: " + OTFUtility.getConPrefTerm(comp.getNid()));
							
							if (prompt.getButtonSelected() == UserPromptResponse.APPROVE) {
								// Retire Stated Parent Rels
								Collection<? extends RelationshipVersionBI<?>> rels = con.getRelationshipsOutgoingActiveIsa();
								for (RelationshipVersionBI<?> r : rels) {
									if (r.getCharacteristicNid() == SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getNid()) {
										retireRelationship(r);
									}
								}
								
								// Add new Rel
								int retirementConceptNid = prompt.getRetirementConceptNid();
								OTFUtility.createNewParent(conceptNid, retirementConceptNid);

								// Retire Con
								ConceptAttributeAB cab = new ConceptAttributeAB(con.getConceptNid(), con.getConceptAttributesActive().isDefined(), RefexDirective.EXCLUDE);
								cab.setStatus(Status.INACTIVE);
								
								ConceptAttributeChronicleBI cabi = OTFUtility.getBuilder().constructIfNotCurrent(cab);
								
								OTFUtility.addUncommitted(cabi.getEnclosingConcept());
								
								// Commit
								OTFUtility.commit(con);
							}
						}
					} else if (type == ComponentType.DESCRIPTION) {
						DescriptionVersionBI<?> desc = (DescriptionVersionBI<?>)comp;

						if (desc.isUncommitted()) {
							ExtendedAppContext.getDataStore().forget(desc);
						}

						DescriptionCAB dcab = desc.makeBlueprint(OTFUtility.getViewCoordinate(),  IdDirective.PRESERVE, RefexDirective.EXCLUDE);
						dcab.setStatus(Status.INACTIVE);
						
						DescriptionChronicleBI dcbi = OTFUtility.getBuilder().constructIfNotCurrent(dcab);
						
						OTFUtility.addUncommitted(dcbi.getEnclosingConcept());
	
					} else if (type == ComponentType.RELATIONSHIP) {
						RelationshipVersionBI<?> rel = (RelationshipVersionBI<?>)comp;

						if (rel.isUncommitted()) {
							ExtendedAppContext.getDataStore().forget(rel);
						}

						retireRelationship(rel);
					}
					
					conceptView.setConcept(comp.getConceptNid());
				} catch (Exception e) {
					LOG.error("Failure in retiring comp: " + comp.getPrimordialUuid(), e);
				}
			}

			private void retireRelationship(RelationshipVersionBI<?> rel) throws ValidationException, IOException, InvalidCAB, ContradictionException {
				RelationshipCAB rcab = new RelationshipCAB(rel.getConceptNid(), rel.getTypeNid(), rel.getDestinationNid(), rel.getGroup(), RelationshipType.getRelationshipType(rel.getRefinabilityNid(), rel.getCharacteristicNid()), rel, OTFUtility.getViewCoordinate(), IdDirective.PRESERVE, RefexDirective.EXCLUDE);

				rcab.setStatus(Status.INACTIVE);
				
				RelationshipChronicleBI rcbi = OTFUtility.getBuilder().constructIfNotCurrent(rcab);
				
				OTFUtility.addUncommitted(rcbi.getEnclosingConcept());
			}
		});

		if (comp.isUncommitted() && comp.getChronicle().getVersions().size() == 1) {
			retireComponentMenu.setDisable(true);
		}
		
		undoComponentMenu.setGraphic(Images.CANCEL.createImageView());
		undoComponentMenu.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				try {
					if (type == ComponentType.CONCEPT) {
						ExtendedAppContext.getDataStore().forget((ConceptAttributeVersionBI<?>)comp);
					} else if (type == ComponentType.DESCRIPTION) {
						ExtendedAppContext.getDataStore().forget((DescriptionVersionBI<?>)comp);
					} else if (type == ComponentType.RELATIONSHIP) {
						ExtendedAppContext.getDataStore().forget((RelationshipVersionBI<?>)comp);
					}
					conceptView.setConcept(comp.getConceptNid());
				} catch (Exception e) {
					LOG.error("Unable to cancel comp: " + comp.getNid(), e);
				}
			}
		});
		
		if (!comp.isUncommitted()) {
			undoComponentMenu.setDisable(true);
		}


		return modifyComponentMenu;
	}

	Menu addCreateNewComponent() {
		Menu createComponentMenu = new Menu("Create New Component");
		MenuItem newDescriptionMenu = new MenuItem("Create New Description");
		MenuItem newRelationshipMenu = new MenuItem("Create New Relationship");
		createComponentMenu.getItems().addAll(newDescriptionMenu, newRelationshipMenu);

		newDescriptionMenu.setGraphic(Images.EDIT.createImageView());
		newDescriptionMenu.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				DescriptionModelingPopup popup = AppContext.getService(DescriptionModelingPopup.class);
				popup.finishInit(conceptNid, conceptView);
				popup.showView(pane.getScene().getWindow());
			}
		});

		newRelationshipMenu.setGraphic(Images.EDIT.createImageView());
		newRelationshipMenu.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				RelationshipModelingPopup popup = AppContext.getService(RelationshipModelingPopup.class);
				popup.finishInit(conceptNid, conceptView);
				popup.showView(pane.getScene().getWindow());
			}
		});

		return createComponentMenu;
	}
	
	Menu addIdMenus(ComponentVersionBI comp, ComponentType type) {
		Menu copyIdMenu = new Menu("Copy " + type + " Ids");
		MenuItem sctIdItem = new MenuItem("SctId");
		MenuItem uuidItem = new MenuItem("UUID");
		MenuItem nidItem = new MenuItem("Native Id");
		copyIdMenu.getItems().addAll(sctIdItem, uuidItem, nidItem);
		
		sctIdItem.setGraphic(Images.COPY.createImageView());
		sctIdItem.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				CustomClipboard.set(ConceptViewerHelper.getSctId(comp));
			}
		});

		uuidItem.setGraphic(Images.COPY.createImageView());
		uuidItem.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				CustomClipboard.set(comp.getPrimordialUuid().toString());
			}
		});

		nidItem.setGraphic(Images.COPY.createImageView());
		nidItem.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				CustomClipboard.set(Integer.toString(comp.getNid()));
			}
		});
		
		return copyIdMenu;		
	}

	// Setters&Getters
	public void setPane(AnchorPane simpleConceptPane) {
		pane = simpleConceptPane;
	}

	public void setIsWindow(boolean isWindow) {
		this.isWindow = isWindow;
	}

	public ObservableList<Integer> getPreviousConceptStack() {
		return previousConceptStack;
	}

	public void setPrevConStack(ObservableList<Integer> conceptHistoryStack) {
		previousConceptStack = conceptHistoryStack;
	}

	public void setConcept(int nid) {
		conceptNid = nid;		
	}

	public MenuItem addPrefAcceptModMenu(ConceptVersionBI con) {
		MenuItem prefAccModificationMenu = new MenuItem("Define Preferred and Acceptibility");
		prefAccModificationMenu.setOnAction((e) -> {
			try {
				PreferredAcceptabilityPrompt.definePrefAcceptConcept((Stage)pane.getScene().getWindow(), "Select Preferability and Acceptability for Concept: " + con.getPreferredDescription().getText(), con);
				conceptView.setConcept(con.getNid());
			} catch (Exception ex) {
				LOG.error("Pref Accept Modification Panel Error", ex);
			}
			
		});
		
		return prefAccModificationMenu;		
	}

}
