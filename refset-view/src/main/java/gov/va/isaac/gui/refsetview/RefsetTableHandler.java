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

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.refsetview.RefsetInstanceAccessor.CEMCompositRefestInstance;
import gov.va.isaac.gui.refsetview.RefsetInstanceAccessor.NidExtRefsetInstance;
import gov.va.isaac.gui.refsetview.RefsetInstanceAccessor.NidStrExtRefsetInstance;
import gov.va.isaac.gui.refsetview.RefsetInstanceAccessor.RefsetInstance;
import gov.va.isaac.gui.refsetview.RefsetInstanceAccessor.StrExtRefsetInstance;
import gov.va.isaac.models.cem.importer.CEMMetadataBinding;
import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.util.UUID;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.text.Font;
import javafx.util.Callback;

import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.javafx.tk.Toolkit;

/**
 * RefsetViewRunner
 *
 * @author <a href="jefron@apelon.com">Jesse Efron</a>
 */
public class RefsetTableHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(RefsetTableHandler.class);
	private RefexType refsetType;
	private boolean isAnnotation;
	private RefsetViewController rvc_;
	private boolean isSetupFinished_ = false;
	private int refsetNid ;
	
	protected void finishTableSetup(RefexChronicleBI<?> member, final boolean annotationValue, final TableView<RefsetInstance> refsetRows, ConceptVersionBI refCompSetupCon, int rNid) {
		isSetupFinished_ = true;
		refsetType = member.getRefexType();
		isAnnotation = annotationValue;
		refsetNid = rNid;
		
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
		
		//TODO we need some refactoring - don't know why we have a setupTable and an initializeTable method, which are sometimes both called, sometimes only one called
		//would be easier if they were both always called, or if they were merged...
		setColSizes(refsetRows);
	}
	
	protected boolean isSetupFinished()
	{
		return isSetupFinished_;
	}
	
	private void setColSizes(TableView<RefsetInstance> refsetRows)
	{
		//Horrible hack to move the stamp column to the end
		//TODO remove this hack after code gets refactored to combine these various init methods
		for (int i = 0; i < refsetRows.getColumns().size(); i++)
		{
			if (refsetRows.getColumns().get(i).getText().equals("STAMP"))
			{
				refsetRows.getColumns().add(refsetRows.getColumns().remove(i));
				break;
			}
		}
		
		//Horrible hack to set a reasonable default size on the columns.
		//Min width to the with of the header column.  Preferred width - divide space out evenly.
		Font f = new Font("System Bold", 13.0);
		float prefColWidthPercentage = 1.0f / (float) refsetRows.getColumns().size();
		for (TableColumn<RefsetInstance, ?> col : refsetRows.getColumns())
		{
			for (TableColumn<RefsetInstance, ?> nestedCol : col.getColumns())
			{
				nestedCol.setMinWidth(Toolkit.getToolkit().getFontLoader().computeStringWidth(nestedCol.getText(), f) + 20);
				nestedCol.prefWidthProperty().bind(refsetRows.widthProperty().subtract(5.0).multiply(prefColWidthPercentage).divide(col.getColumns().size()));
			}
			col.setMinWidth(Toolkit.getToolkit().getFontLoader().computeStringWidth(col.getText(), f) + 20);
			col.prefWidthProperty().bind(refsetRows.widthProperty().subtract(5.0).multiply(prefColWidthPercentage));
		}
	}

	@SuppressWarnings("unchecked")
	private void createCidInstanceTable(TableView<RefsetInstance> refsetRows) {
		TableColumn<RefsetInstance, String> col = createCidColumn("Component", "cidExtFsn", 1);
		refsetRows.getColumns().addAll(col);
	}

	@SuppressWarnings("unchecked")
	private void createStringInstanceTable(TableView<RefsetInstance> refsetRows) {
		TableColumn<RefsetInstance, String> col1 = createStrColumn("String", "strExt", 1);
		refsetRows.getColumns().addAll(col1);
	}

	@SuppressWarnings("unchecked")
	private void createCidStrInstanceTable(TableView<RefsetInstance> refsetRows) {
		TableColumn<RefsetInstance, String> col1 = createCidColumn("Component", "cidExtFsn", 1);
		TableColumn<RefsetInstance, String> col2 = createStrColumn("String", "strExt", 2);

		refsetRows.getColumns().addAll(col1);
		refsetRows.getColumns().addAll(col2);
	}

	@SuppressWarnings("unchecked")
	private void createComponentRefsetInstanceTable(TableView<RefsetInstance> refsetRows) {
		TableColumn<RefsetInstance, String> col1 = createCidColumn("Component", "cidExtFsn", 1);
		TableColumn<RefsetInstance, String> col2 = createStrColumn("String", "strExt", 2);

		// COL 3 (CONSTR PATH Refset Value)
		TableColumn<RefsetInstance, String> col3 = createStrColumn("Constraint Type", "constraintPathExt", 3);

		// COL 3 (CONSTR VAL Refset Value)
		TableColumn<RefsetInstance, String> col4 = createStrColumn("Constraint Value", "constraintValExt", 4);

		// COL 3 ( VAL Refset Value)
		TableColumn<RefsetInstance, String> col5 = createStrColumn("Attribute's Value", "valueExt", 5);

		refsetRows.getColumns().addAll(col1);
		refsetRows.getColumns().addAll(col2);
		refsetRows.getColumns().addAll(col3);
		refsetRows.getColumns().addAll(col4);
		refsetRows.getColumns().addAll(col5);
	}
	
	private TableColumn<RefsetInstance, String> createStrColumn(String columnTitle, String associatedValue, final int columnNumber) {
		TableColumn<RefsetInstance, String> col = new TableColumn<>(columnTitle);	
		col.setCellValueFactory(new PropertyValueFactory<RefsetInstance,String>(associatedValue));
		col.setCellFactory(RefsetTableEditingCell.create());

		col.setOnEditCommit(new EventHandler<CellEditEvent<RefsetInstance, String>>() {
			@Override
			public void handle(CellEditEvent<RefsetInstance, String> t) {
				RefexCAB bp = null;
				
				try {
					if (columnNumber == 1) {
						StrExtRefsetInstance instance = (StrExtRefsetInstance) t.getTableView().getItems().get(t.getTablePosition().getRow());
						instance.setStrExt(t.getNewValue());
						
						if (WBUtility.getRefsetMember(instance.getMemberNid()) == null) {
							RefexCAB newMember = new RefexCAB(RefexType.STR, instance.getRefCompConNid(), refsetNid, IdDirective.GENERATE_RANDOM, RefexDirective.EXCLUDE);

							newMember.put(ComponentProperty.STRING_EXTENSION_1, t.getNewValue());
							
							RefexChronicleBI<?> newMemChron = WBUtility.getBuilder().construct(newMember);
							instance.setMemberNid(newMemChron.getNid());
							
							
							ConceptVersionBI refCompCon;
							if (!isAnnotation) {
								refCompCon = WBUtility.getConceptVersion(instance.getRefCompConNid());
							} else {
								refCompCon = WBUtility.getConceptVersion(refsetNid);
							}
							refCompCon.addAnnotation(newMemChron);
							
							WBUtility.addUncommitted(instance.getRefCompConNid());
							rvc_.reloadData();
							return;
						} else {
							bp = createBlueprint(instance.getMemberNid());
						}
					} else if (columnNumber == 2) {
						NidStrExtRefsetInstance instance = (NidStrExtRefsetInstance) t.getTableView().getItems().get(t.getTablePosition().getRow());
						instance.setStrExt(t.getNewValue());
						
						bp = createBlueprint(instance.getMemberNid());
					} else if (columnNumber == 3) {
						CEMCompositRefestInstance instance = (CEMCompositRefestInstance) t.getTableView().getItems().get(t.getTablePosition().getRow());
						instance.setConstraintPathExt(t.getNewValue());

						bp = createBlueprint(instance.getConstraintPathMemberNid());
					} else if (columnNumber == 4) {
						CEMCompositRefestInstance instance = (CEMCompositRefestInstance) t.getTableView().getItems().get(t.getTablePosition().getRow());
						instance.setConstraintValExt(t.getNewValue());

						bp = createBlueprint(instance.getConstraintValMemberNid());
					} else if (columnNumber == 5) {
						CEMCompositRefestInstance instance = (CEMCompositRefestInstance) t.getTableView().getItems().get(t.getTablePosition().getRow());
						instance.setValueExt(t.getNewValue());

						if (WBUtility.getRefsetMember(instance.getValueMemberNid()) == null) {
							ComponentChronicleBI compositeMember = WBUtility.getRefsetMember(instance.getCompositeMemberNid());
							
							RefexCAB newMember = new RefexCAB(RefexType.STR, compositeMember.getNid(), CEMMetadataBinding.CEM_VALUE_REFSET.getNid(), IdDirective.GENERATE_RANDOM, RefexDirective.EXCLUDE);

							newMember.put(ComponentProperty.STRING_EXTENSION_1, t.getNewValue());
							
							RefexChronicleBI<?> newMemChron = WBUtility.getBuilder().construct(newMember);
							instance.setValueMemberNid(newMemChron.getNid());
							
							compositeMember.addAnnotation(newMemChron);
							
							WBUtility.addUncommitted(instance.getRefCompConNid());
							rvc_.reloadData();
							return;
						} else {
							bp = createBlueprint(instance.getValueMemberNid());
						}
					}

					// None of Comp are Str-Str. . . so simple solution to this
					bp.put(ComponentProperty.STRING_EXTENSION_1, t.getNewValue());

					commitUpdate(bp, isAnnotation);
					rvc_.reloadData();
				}
				catch (Exception e)
				{
					logger.error("Unexpected error handling edit", e);
				}
			}
		});

		return col;
	}

	private TableColumn<RefsetInstance, String> createCidColumn(String columnTitle, String associatedValue, final int columnNumber) {
		TableColumn<RefsetInstance, String> col = new TableColumn<>(columnTitle);	
		col.setCellValueFactory(new PropertyValueFactory<RefsetInstance,String>(associatedValue));
		col.setCellFactory(RefsetTableEditingCell.create());

		col.setOnEditCommit(new EventHandler<CellEditEvent<RefsetInstance, String>>() {
			@Override
			public void handle(CellEditEvent<RefsetInstance, String> t) {
				try
				{
					try {
						UUID value = UUID.fromString(t.getNewValue());
					} catch (Exception e) {
						AppContext.getCommonDialogs().showErrorDialog("Invalid UUID", "Value requested is not a valid UUID", t.getNewValue());
						return;
					}
	
					ConceptVersionBI comp = WBUtility.lookupIdentifier(t.getNewValue());
					if (comp == null) {
						AppContext.getCommonDialogs().showErrorDialog("UUID Not Found", "Could not find the UUID in the database", t.getNewValue());
					} else {
						RefexCAB bp = null;
						
						if (refsetType == RefexType.CID ||
							refsetType == RefexType.CID_STR ||
							refsetType == RefexType.UNKNOWN)
						{
							if (columnNumber == 1) {
								NidExtRefsetInstance instance = (NidExtRefsetInstance) t.getTableView().getItems().get(t.getTablePosition().getRow());
								if (WBUtility.getRefsetMember(instance.getMemberNid()) == null) {
									RefexCAB newMember = new RefexCAB(RefexType.CID, instance.getRefCompConNid(), refsetNid, IdDirective.GENERATE_RANDOM, RefexDirective.EXCLUDE);

									newMember.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, comp.getPrimordialUuid());
									
									RefexChronicleBI<?> newMemChron = WBUtility.getBuilder().construct(newMember);
									instance.setMemberNid(newMemChron.getNid());
									
									
									ConceptVersionBI refCompCon;
									if (!isAnnotation) {
										refCompCon = WBUtility.getConceptVersion(instance.getRefCompConNid());
									} else {
										refCompCon = WBUtility.getConceptVersion(refsetNid);
									}
									refCompCon.addAnnotation(newMemChron);
									
									WBUtility.addUncommitted(instance.getRefCompConNid());
									rvc_.reloadData();
									return;
								} else {
									instance.setCidExtFsn(comp.getFullySpecifiedDescription().getText());
									instance.setCidExtUuid(comp.getPrimordialUuid());
	
									bp = createBlueprint(instance.getMemberNid());
									bp.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, comp.getPrimordialUuid());
								}
							}
						}

						commitUpdate(bp, isAnnotation);
						rvc_.reloadData();
					}
				}
				catch (Exception e)
				{
					logger.error("Unexpected error handling edit", e);
				}
			}
		});

		return col;
	}


	@SuppressWarnings("unchecked")
	protected RefsetTableHandler(final TableView<RefsetInstance> refsetRows, RefsetViewController rvc) {
		rvc_ = rvc;
		TableColumn<RefsetInstance, String> memberCol = new TableColumn<>("Reference Component");	
		memberCol.setCellValueFactory(new PropertyValueFactory<RefsetInstance,String>("refCompConFsn"));
		
		memberCol.setCellFactory(RefsetTableEditingCell.create());
		memberCol.setOnEditCommit(new EventHandler<CellEditEvent<RefsetInstance, String>>() {
			//TODO this doesn't seem to update the WB DB
			@Override
			public void handle(CellEditEvent<RefsetInstance, String> t) {
				try
				{
					try {
						UUID value = UUID.fromString(t.getNewValue());
					} catch (Exception e) {
						AppContext.getCommonDialogs().showErrorDialog("Invalid UUID", "Value requested is not a valid UUID", t.getNewValue());
						return;
					}

					RefsetInstance instance = (RefsetInstance) t.getTableView().getItems().get(t.getTablePosition().getRow());
					
					if (instance.getMemberNid() != 0) {
						AppContext.getCommonDialogs().showErrorDialog("Illegal Operation", "Cannot modify the reference component of an existing refset member", "");
					} else {
						ConceptVersionBI comp = WBUtility.lookupIdentifier(t.getNewValue());
						if (comp == null) {
							AppContext.getCommonDialogs().showErrorDialog("UUID Not Found", "Could not find the UUID in the database", t.getNewValue());
						} else {
							try {
								instance.setRefCompConFsn(comp.getFullySpecifiedDescription().getText());
								instance.setRefCompConUuid(comp.getPrimordialUuid());
								instance.setRefCompConNid(comp.getNid());
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
				catch (Exception e)
				{
					logger.error("Unexpected error handling edit", e);
				}
			}
		});
		
		refsetRows.getColumns().clear();
		refsetRows.setEditable(true);
		refsetRows.getColumns().addAll(memberCol);
		refsetRows.getColumns().add(createStampColumn());
		setColSizes(refsetRows);

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
	
	private TableColumn<RefsetInstance, ?> createStampColumn() {
		final ObservableList<Status> statusList = FXCollections.observableArrayList(Status.ACTIVE, Status.INACTIVE);

		TableColumn<RefsetInstance, String> col = new TableColumn<>("STAMP");
		
		TableColumn<RefsetInstance, Status> status = new TableColumn<>("Status");
		status.setCellValueFactory(new PropertyValueFactory<RefsetInstance,Status>("status"));
		status.setCellFactory(ComboBoxTableCell.<RefsetInstance, Status>forTableColumn(statusList));
		status.setOnEditCommit(new EventHandler<CellEditEvent<RefsetInstance, Status>>() {
			@Override
			public void handle(CellEditEvent<RefsetInstance, Status> t) {
				RefsetInstance instance = (RefsetInstance) t.getTableView().getItems().get(t.getTablePosition().getRow());
				try {
					if (!isLatestVersion(instance)) {
						// TODO throw dialog
					} else {
						instance.setStatus(t.getNewValue());
						RefexCAB bp = createBlueprint(instance.getMemberNid());
						bp.setStatus(t.getNewValue());
						commitUpdate(bp, isAnnotation);
						rvc_.reloadData();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			private boolean isLatestVersion(RefsetInstance instance) throws ContradictionException {
 				RefexChronicleBI refChron = WBUtility.getAllVersionsRefsetMember(instance.getMemberNid());
				RefexVersionBI refVersion = WBUtility.getRefsetMember(instance.getMemberNid());
				
				if (refChron.getVersion(WBUtility.getViewCoordinate()).getStamp() == refVersion.getStamp()) {
					return true;
				}
				
				return false;
			}
		});
		col.getColumns().add(status);
		
		TableColumn<RefsetInstance, String> time = new TableColumn<>("Time");
		time.setCellValueFactory(new PropertyValueFactory<RefsetInstance,String>("time"));
		time.setCellFactory(TextFieldTableCell.<RefsetInstance>forTableColumn());
		time.setEditable(false);
		col.getColumns().add(time);
		
		TableColumn<RefsetInstance, String> author = new TableColumn<>("Author");
		author.setCellValueFactory(new PropertyValueFactory<RefsetInstance,String>("author"));
		author.setCellFactory(TextFieldTableCell.<RefsetInstance>forTableColumn());
		author.setEditable(false);
		col.getColumns().add(author);
		
		TableColumn<RefsetInstance, String> module = new TableColumn<>("Module");
		module.setCellValueFactory(new PropertyValueFactory<RefsetInstance,String>("module"));
		module.setCellFactory(TextFieldTableCell.<RefsetInstance>forTableColumn());
		module.setEditable(false);
		col.getColumns().add(module);
		
		TableColumn<RefsetInstance, String> path = new TableColumn<>("Path");
		path.setCellValueFactory(new PropertyValueFactory<RefsetInstance,String>("path"));
		path.setCellFactory(TextFieldTableCell.<RefsetInstance>forTableColumn());
		path.setEditable(false);
		col.getColumns().add(path);
		
		
		return col;
	}

	private RefexCAB createBlueprint(int nid) throws ContradictionException, InvalidCAB, IOException {
		RefexVersionBI refex = (RefexVersionBI)WBUtility.getRefsetMember(nid);
		
		return refex.makeBlueprint(WBUtility.getViewCoordinate(),  IdDirective.PRESERVE, RefexDirective.INCLUDE);
	
	}

	private void commitUpdate(RefexCAB member, boolean isAnnotation) throws IOException, InvalidCAB, ContradictionException {
		RefexVersionBI refex = (RefexVersionBI)WBUtility.getRefsetMember(member.getComponentNid());
		
		//TODO - make sense of this magical API.  Why on earth do we have to look up, and addUncommitted, on something other than what the 
		//Builder returned to us?  Here be dragons.
		//Also, rename this method... it doesn't commit.
		RefexChronicleBI<?> cabi = WBUtility.getBuilder().constructIfNotCurrent(member);
		ConceptVersionBI refCompCon;
		if (!isAnnotation) {
			refCompCon = WBUtility.getConceptVersion(refex.getReferencedComponentNid());
		} else {
			refCompCon = WBUtility.getConceptVersion(refex.getAssemblageNid());
		}
		
		WBUtility.addUncommitted(refCompCon);
	}
}
