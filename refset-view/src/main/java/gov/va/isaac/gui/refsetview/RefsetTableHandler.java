package gov.va.isaac.gui.refsetview;

import gov.va.isaac.gui.refsetview.RefsetInstanceAccessor.NidExtRefsetInstance;
import gov.va.isaac.gui.refsetview.RefsetInstanceAccessor.NidNidExtRefsetInstance;
import gov.va.isaac.gui.refsetview.RefsetInstanceAccessor.RefsetInstance;
import gov.va.isaac.gui.refsetview.RefsetInstanceAccessor.StrExtRefsetInstance;
import gov.va.isaac.util.WBUtility;

import java.io.IOException;

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
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.type_string.RefexStringVersionBI;

public class RefsetTableHandler {
	private static RefexType refsetType;

	@SuppressWarnings("unchecked")
	static void setupTable(RefexChronicleBI<?> member, final boolean isAnnotation, final TableView<RefsetInstance> refsetRows) {
		refsetType = member.getRefexType();
		
		if (refsetType == RefexType.STR) {
			TableColumn col = new TableColumn("String");	
			col.setCellValueFactory(
					new PropertyValueFactory<RefsetInstance,String>("strExt")
			);

			col.setCellFactory(TextFieldTableCell.forTableColumn());
			col.setOnEditCommit(
				new EventHandler<CellEditEvent<StrExtRefsetInstance, String>>() {
					@Override
					public void handle(CellEditEvent<StrExtRefsetInstance, String> t) {
						StrExtRefsetInstance instance = (StrExtRefsetInstance) t.getTableView().getItems().get(t.getTablePosition().getRow());
						instance.setStrExt(t.getNewValue());

						try {
							RefexCAB bp = createBlueprint(instance);

							bp.put(ComponentProperty.STRING_EXTENSION_1, t.getNewValue());

							commitUpdate(bp, isAnnotation);

						} catch (ContradictionException | InvalidCAB | IOException e) {
							e.printStackTrace();
						}
					}
				}
			);
		
			refsetRows.getColumns().addAll(col);
		} else if (refsetType == RefexType.CID) {
			TableColumn col = new TableColumn("Component");	
			col.setCellValueFactory(
					new PropertyValueFactory<RefsetInstance,String>("cidExtFsn")
			);
			
			col.setCellFactory(TextFieldTableCell.forTableColumn());
			col.setOnEditCommit(
				new EventHandler<CellEditEvent<NidExtRefsetInstance, String>>() {
					@Override
					public void handle(CellEditEvent<NidExtRefsetInstance, String> t) {
						NidExtRefsetInstance instance = (NidExtRefsetInstance) t.getTableView().getItems().get(t.getTablePosition().getRow());

						if (instance.getMemberNid() != 0) {
							// TODO Raise dialog box saying cannot change existing RefComp
							t.getTableView().getItems().get(t.getTablePosition().getRow()).setRefConFsn(instance.getRefConFsn());
						} else {
							ConceptVersionBI comp = WBUtility.lookupSnomedIdentifierAsCV(t.getNewValue());
							if (comp == null) {
								// TODO Raise dialog box saying cannot locate Component
							} else {
								try {
									instance.setCidExtFsn(comp.getFullySpecifiedDescription().getText());
									instance.setCidExtUuid(comp.getPrimordialUuid());

									RefexCAB bp = createBlueprint(instance);

									bp.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, comp.getPrimordialUuid());

									commitUpdate(bp, isAnnotation);
		
								} catch (ContradictionException | InvalidCAB | IOException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
			);

			refsetRows.getColumns().addAll(col);
		} else if (refsetType == RefexType.CID_CID) {
			TableColumn col1 = new TableColumn("Component1");	
			col1.setCellValueFactory(
					new PropertyValueFactory<RefsetInstance,String>("cidExtFsn")
			);
			
			col1.setCellFactory(TextFieldTableCell.forTableColumn());
			col1.setOnEditCommit(
				new EventHandler<CellEditEvent<NidExtRefsetInstance, String>>() {
					@Override
					public void handle(CellEditEvent<NidExtRefsetInstance, String> t) {
						NidExtRefsetInstance instance = (NidExtRefsetInstance) t.getTableView().getItems().get(t.getTablePosition().getRow());

						if (instance.getMemberNid() != 0) {
							// TODO Raise dialog box saying cannot change existing RefComp
							t.getTableView().getItems().get(t.getTablePosition().getRow()).setRefConFsn(instance.getRefConFsn());
						} else {
							ConceptVersionBI comp = WBUtility.lookupSnomedIdentifierAsCV(t.getNewValue());
							if (comp == null) {
								// TODO Raise dialog box saying cannot locate Component
							} else {
								try {
									instance.setCidExtFsn(comp.getFullySpecifiedDescription().getText());
									instance.setCidExtUuid(comp.getPrimordialUuid());
									RefexCAB bp = createBlueprint(instance);

									bp.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, comp.getPrimordialUuid());

									commitUpdate(bp, isAnnotation);

								} catch (ContradictionException | InvalidCAB | IOException e) {
									e.printStackTrace();
								}
							}
						}						
					}
				}
			);

			refsetRows.getColumns().addAll(col1);

			TableColumn col2 = new TableColumn("Component2");	
			col2.setCellValueFactory(
					new PropertyValueFactory<RefsetInstance,String>("cid2ExtFsn")
			);
			
			col2.setCellFactory(TextFieldTableCell.forTableColumn());
			col2.setOnEditCommit(
				new EventHandler<CellEditEvent<NidNidExtRefsetInstance, String>>() {
					@Override
					public void handle(CellEditEvent<NidNidExtRefsetInstance, String> t) {
						NidNidExtRefsetInstance instance = (NidNidExtRefsetInstance) t.getTableView().getItems().get(t.getTablePosition().getRow());

						if (instance.getMemberNid() != 0) {
							// TODO Raise dialog box saying cannot change existing RefComp
							t.getTableView().getItems().get(t.getTablePosition().getRow()).setRefConFsn(instance.getRefConFsn());
						} else {
							ConceptVersionBI comp = WBUtility.lookupSnomedIdentifierAsCV(t.getNewValue());
							if (comp == null) {
								// TODO Raise dialog box saying cannot locate Component
							} else {
								try {
									instance.setCid2ExtFsn(comp.getFullySpecifiedDescription().getText());
									instance.setCid2ExtUuid(comp.getPrimordialUuid());

									RefexCAB bp = createBlueprint(instance);

									bp.put(ComponentProperty.COMPONENT_EXTENSION_2_ID, comp.getPrimordialUuid());

									commitUpdate(bp, isAnnotation);
								} catch (ContradictionException | InvalidCAB | IOException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
			);
			refsetRows.getColumns().addAll(col2);
		}
	}

	@SuppressWarnings("unchecked")
	public static void initializeTable(final TableView<RefsetInstance> refsetRows) {
		TableColumn memberCol = new TableColumn("Reference Component");	
		memberCol.setCellValueFactory(
				new PropertyValueFactory<RefsetInstance,String>("refConFsn")
		);
		memberCol.setSortType(TableColumn.SortType.DESCENDING);
		memberCol.setCellFactory(TextFieldTableCell.forTableColumn());
		memberCol.setOnEditCommit(
				new EventHandler<CellEditEvent<RefsetInstance, String>>() {
					@Override
					public void handle(CellEditEvent<RefsetInstance, String> t) {
						RefsetInstance instance = (RefsetInstance) t.getTableView().getItems().get(t.getTablePosition().getRow());
						
						if (instance.getMemberNid() != 0) {
							// TODO Raise dialog box saying cannot change existing RefComp
							t.getTableView().getItems().get(t.getTablePosition().getRow()).setRefConFsn(instance.getRefConFsn());
						} else {
							ConceptVersionBI comp = WBUtility.lookupSnomedIdentifierAsCV(t.getNewValue());
							if (comp == null) {
								// TODO Raise dialog box saying cannot locate Component
							} else {
								try {
									instance.setRefConFsn(comp.getFullySpecifiedDescription().getText());
									instance.setRefConUuid(comp.getPrimordialUuid());
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

	private static RefexCAB createBlueprint(RefsetInstance instance) throws ContradictionException, InvalidCAB, IOException {
		int nid = instance.getMemberNid();
		RefexStringVersionBI refex = (RefexStringVersionBI)WBUtility.getRefsetMember(nid);
		
		return refex.makeBlueprint(WBUtility.getViewCoordinate(),  IdDirective.PRESERVE, RefexDirective.INCLUDE);
	
	}

	private static void commitUpdate(RefexCAB bp, boolean isAnnotation) throws IOException, InvalidCAB, ContradictionException {
		RefexStringVersionBI refex = (RefexStringVersionBI)WBUtility.getRefsetMember(bp.getComponentNid());
		
		RefexChronicleBI<?> cabi = WBUtility.getBuilder().constructIfNotCurrent(bp);
		ConceptVersionBI refCon;
		if (!isAnnotation) {
			refCon = WBUtility.lookupSnomedIdentifierAsCV(refex.getReferencedComponentNid());
		} else {
			refCon = WBUtility.lookupSnomedIdentifierAsCV(refex.getAssemblageNid());
		}
		
		WBUtility.addUncommitted(refCon);
	}
}
