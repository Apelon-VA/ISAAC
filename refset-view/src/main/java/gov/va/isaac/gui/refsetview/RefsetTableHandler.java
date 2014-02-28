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
package gov.va.isaac.gui.refsetview;

import gov.va.isaac.gui.refsetview.RefsetInstanceAccessor.CEMCompositRefestInstance;
import gov.va.isaac.gui.refsetview.RefsetInstanceAccessor.NidExtRefsetInstance;
import gov.va.isaac.gui.refsetview.RefsetInstanceAccessor.NidStrExtRefsetInstance;
import gov.va.isaac.gui.refsetview.RefsetInstanceAccessor.RefsetInstance;
import gov.va.isaac.gui.refsetview.RefsetInstanceAccessor.StrExtRefsetInstance;
import gov.va.isaac.models.cem.importer.CEMMetadataBinding;
import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.util.UUID;

import javafx.event.EventHandler;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_string.RefexStringVersionBI;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;

/**
 * RefsetViewRunner
 *
 * @author <a href="jefron@apelon.com">Jesse Efron</a>
 */
public class RefsetTableHandler {
	private static RefexType refsetType;
	private static boolean isAnnotation;
	private static ConceptVersionBI refCompCon;
	
	static void setupTable(RefexChronicleBI<?> member, final boolean annotationValue, final TableView<RefsetInstance> refsetRows, ConceptVersionBI refCompSetupCon) {
		refsetType = member.getRefexType();
		isAnnotation = annotationValue;
		refCompCon = refCompSetupCon;
		
		try {
			if (member.getAssemblageNid() == CEMMetadataBinding.CEM_COMPOSITION_REFSET.getNid()) {
				refsetType = RefexType.UNKNOWN;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (refsetType == RefexType.STR) {
			createStringInstanceTable(refsetRows);
		} else if (refsetType == RefexType.CID) {
			createCidInstanceTable(refsetRows);
		} else if (refsetType == RefexType.CID_STR) {
			createCidStrInstanceTable(refsetRows);
		} else if (refsetType == RefexType.UNKNOWN) {
			createComponentRefsetInstanceTable(refsetRows);
		}
	}

	@SuppressWarnings("unchecked")
	private static void createCidInstanceTable(TableView<RefsetInstance> refsetRows) {
		TableColumn col = createCidColumn("Component", "cidExtFsn", 1);
		refsetRows.getColumns().addAll(col);
	}

	@SuppressWarnings("unchecked")
	private static void createStringInstanceTable(TableView<RefsetInstance> refsetRows) {
		TableColumn col1 = createStrColumn("String", "strExt", 1);
		refsetRows.getColumns().addAll(col1);
	}

	@SuppressWarnings("unchecked")
	private static void createCidStrInstanceTable(TableView<RefsetInstance> refsetRows) {
		TableColumn col1 = createCidColumn("Component", "cidExtFsn", 1);
		TableColumn col2 = createStrColumn("String", "strExt", 2);

		refsetRows.getColumns().addAll(col1);
		refsetRows.getColumns().addAll(col2);
	}

	@SuppressWarnings("unchecked")
	private static void createComponentRefsetInstanceTable(TableView<RefsetInstance> refsetRows) {
		TableColumn col1 = createCidColumn("Component", "cidExtFsn", 1);
		TableColumn col2 = createStrColumn("String", "strExt", 2);

		// COL 3 (CONSTR PATH Refset Value)
		TableColumn col3 = createStrColumn("Constraint Type", "constraintExt", 3);

		// COL 3 (CONSTR VAL Refset Value)
		TableColumn col4 = createStrColumn("Constraint Value", "constraintValExt", 4);

		// COL 3 ( VAL Refset Value)
		TableColumn col5 = createStrColumn("Attribute's Value", "valueExt", 5);

		refsetRows.getColumns().addAll(col1);
		refsetRows.getColumns().addAll(col2);
		refsetRows.getColumns().addAll(col3);
		refsetRows.getColumns().addAll(col4);
		refsetRows.getColumns().addAll(col5);
	}
	
	@SuppressWarnings("unchecked")
	private static TableColumn createStrColumn(String columnTitle, String associatedValue, final int columnNumber) {
		TableColumn col = new TableColumn(columnTitle);	
		col.setCellValueFactory(new PropertyValueFactory<RefsetInstance,String>(associatedValue));
		col.setCellFactory(TextFieldTableCell.forTableColumn());

		col.setOnEditCommit(new EventHandler<CellEditEvent<RefsetInstance, String>>() {
			@Override
			public void handle(CellEditEvent<RefsetInstance, String> t) {
				RefexCAB bp = null;
				
				try {
					if (columnNumber == 1) {
						StrExtRefsetInstance instance = (StrExtRefsetInstance) t.getTableView().getItems().get(t.getTablePosition().getRow());
						instance.setStrExt(t.getNewValue());
						
						bp = createBlueprint(instance.getMemberNid());
					} else if (columnNumber == 2) {
						NidStrExtRefsetInstance instance = (NidStrExtRefsetInstance) t.getTableView().getItems().get(t.getTablePosition().getRow());
						instance.setStrExt(t.getNewValue());
						
						bp = createBlueprint(instance.getMemberNid());
					} else if (columnNumber == 3) {
						CEMCompositRefestInstance instance = (CEMCompositRefestInstance) t.getTableView().getItems().get(t.getTablePosition().getRow());
						instance.setConstraintExt(t.getNewValue());

						bp = createBlueprint(instance.getConstraintMemberNid());
					} else if (columnNumber == 4) {
						CEMCompositRefestInstance instance = (CEMCompositRefestInstance) t.getTableView().getItems().get(t.getTablePosition().getRow());
						instance.setConstraintValExt(t.getNewValue());

						bp = createBlueprint(instance.getConstraintValMemberNid());
					} else if (columnNumber == 5) {
						CEMCompositRefestInstance instance = (CEMCompositRefestInstance) t.getTableView().getItems().get(t.getTablePosition().getRow());
						instance.setValueExt(t.getNewValue());

						if (WBUtility.getRefsetMember(instance.getValueMemberNid()) == null) {
							ComponentChronicleBI constraintMember = instance.getConstraintMember();
							RefexCAB newMember = new RefexCAB(RefexType.STR, constraintMember.getNid(),  instance.getValueMemberNid(), IdDirective.GENERATE_HASH, RefexDirective.INCLUDE);
							newMember.put(ComponentProperty.STRING_EXTENSION_1, t.getNewValue());
							
							RefexChronicleBI<?> newMemChron = WBUtility.getBuilder().construct(newMember);
			                
							constraintMember.addAnnotation(newMemChron);
							
							WBUtility.addUncommitted(refCompCon);
							return;
						} else {
							bp = createBlueprint(instance.getValueMemberNid());
						}
					}

					// None of Comp are Str-Str. . . so simple solution to this
					bp.put(ComponentProperty.STRING_EXTENSION_1, t.getNewValue());

					commitUpdate(bp, isAnnotation);
				} catch (ContradictionException | InvalidCAB | IOException e) {
					e.printStackTrace();
				}
			}
		});

		return col;
	}

	@SuppressWarnings("unchecked")
	private static TableColumn createCidColumn(String columnTitle, String associatedValue, final int columnNumber) {
		TableColumn col = new TableColumn(columnTitle);	
		col.setCellValueFactory(new PropertyValueFactory<RefsetInstance,String>(associatedValue));
		col.setCellFactory(TextFieldTableCell.forTableColumn());

		col.setOnEditCommit(new EventHandler<CellEditEvent<RefsetInstance, String>>() {
			@Override
			public void handle(CellEditEvent<RefsetInstance, String> t) {
				RefsetInstance genericInstance = (RefsetInstance) t.getTableView().getItems().get(t.getTablePosition().getRow());
				if (genericInstance.getMemberNid() != 0) {
					// TODO Raise dialog box saying cannot change existing RefComp
					t.getTableView().getItems().get(t.getTablePosition().getRow()).setRefCompConFsn(genericInstance.getRefCompConFsn());
				} else {
					
					ConceptVersionBI comp = WBUtility.lookupSnomedIdentifierAsCV(t.getNewValue());
					if (comp == null) {
						// TODO Raise dialog box saying cannot locate Component
					} else {
						try {
							RefexCAB bp = null;
							
							if (refsetType == RefexType.CID ||
								refsetType == RefexType.CID_STR ||
								refsetType == RefexType.UNKNOWN)
							{
								if (columnNumber == 1) {
									NidExtRefsetInstance instance = (NidExtRefsetInstance) t.getTableView().getItems().get(t.getTablePosition().getRow());
									instance.setCidExtFsn(comp.getFullySpecifiedDescription().getText());
									instance.setCidExtUuid(comp.getPrimordialUuid());
	
									bp = createBlueprint(instance.getMemberNid());
									bp.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, comp.getPrimordialUuid());
								}
							}
							
							commitUpdate(bp, isAnnotation);

						} catch (ContradictionException | InvalidCAB | IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});

		return col;
	}


	@SuppressWarnings("unchecked")
	public static void initializeTable(final TableView<RefsetInstance> refsetRows) {
		TableColumn memberCol = new TableColumn("Reference Component");	
		memberCol.setCellValueFactory(new PropertyValueFactory<RefsetInstance,String>("refCompConFsn"));
		
		memberCol.setCellFactory(TextFieldTableCell.forTableColumn());
		memberCol.setOnEditCommit(new EventHandler<CellEditEvent<RefsetInstance, String>>() {
					@Override
					public void handle(CellEditEvent<RefsetInstance, String> t) {
						RefsetInstance instance = (RefsetInstance) t.getTableView().getItems().get(t.getTablePosition().getRow());
						
						if (instance.getMemberNid() != 0) {
							// TODO Raise dialog box saying cannot change existing RefComp
							t.getTableView().getItems().get(t.getTablePosition().getRow()).setRefCompConFsn(instance.getRefCompConFsn());
						} else {
							ConceptVersionBI comp = WBUtility.lookupSnomedIdentifierAsCV(t.getNewValue());
							if (comp == null) {
								// TODO Raise dialog box saying cannot locate Component
							} else {
								try {
									instance.setRefCompConFsn(comp.getFullySpecifiedDescription().getText());
									instance.setRefCompConUuid(comp.getPrimordialUuid());
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}						
					}
				}
			);
		
		
		refsetRows.getColumns().clear();
		refsetRows.setEditable(true);
		refsetRows.getColumns().addAll(memberCol);

/*		
		// Lock Column Ordering (better solution not available in API
		refsetRows.getColumns().addListener(new ListChangeListener() {
			@Override
			public void onChanged(Change change) {
			  change.next();
			  if(change.wasReplaced()) {
				  ObservableList<TableColumn<RefsetInstance, ?>> origCols = FXCollections.observableArrayList();
				  FXCollections.copy(refsetRows.getColumns(), origCols);

				  refsetRows.getColumns().clear();
				  refsetRows.getColumns().addAll(origCols);
			  }
			}
		});
*/		 
	}

	private static RefexCAB createBlueprint(int nid) throws ContradictionException, InvalidCAB, IOException {
		RefexStringVersionBI refex = (RefexStringVersionBI)WBUtility.getRefsetMember(nid);
		
		return refex.makeBlueprint(WBUtility.getViewCoordinate(),  IdDirective.PRESERVE, RefexDirective.INCLUDE);
	
	}

	private static void commitUpdate(RefexCAB member, boolean isAnnotation) throws IOException, InvalidCAB, ContradictionException {
		RefexVersionBI refex = (RefexVersionBI)WBUtility.getRefsetMember(member.getComponentNid());
		
		RefexChronicleBI<?> cabi = WBUtility.getBuilder().constructIfNotCurrent(member);
		ConceptVersionBI refCompCon;
		if (!isAnnotation) {
			refCompCon = WBUtility.lookupSnomedIdentifierAsCV(refex.getReferencedComponentNid());
		} else {
			refCompCon = WBUtility.lookupSnomedIdentifierAsCV(refex.getAssemblageNid());
		}
		
		WBUtility.addUncommitted(refCompCon);
	}
}
