package gov.va.isaac.gui.refsetview;


import gov.va.isaac.gui.refsetview.RefsetInstanceAccessor.RefsetInstance;
import gov.va.isaac.gui.refsetview.RefsetInstanceAccessor.StrExtRefsetInstance;
import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.UUID;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;

import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_string.RefexStringVersionBI;

public class RefsetViewController {

	@FXML private ResourceBundle resources;
	@FXML private URL location;
	@FXML private Slider hSlider;
	@FXML private TableView<RefsetInstance> refsetRows;
	@FXML private AnchorPane refsetAnchor;
	@FXML private Button addButton;
	@FXML private Button removeButton;
	@FXML private Label refsetLabel;
	
	static ViewCoordinate vc = null;
	
	ObservableList<RefsetInstance> data = FXCollections.observableArrayList();
	private boolean isAnnotation = false;
	private ConceptVersionBI refset;
	private RefexType refsetType = RefexType.MEMBER;
		

	public static RefsetViewController init() throws IOException {
		// Load from FXML.
		URL resource = RefsetViewController.class.getResource("RefsetView.fxml");
		FXMLLoader loader = new FXMLLoader(resource);
		loader.load();

		return loader.getController();
	}

	@FXML 
	void initialize() {
		 vc = WBUtility.getViewCoordinate();

		TableColumn memberCol = new TableColumn("Reference Component");	
		memberCol.setCellValueFactory(
				new PropertyValueFactory<RefsetInstance,String>("refConFsn")
		);
		memberCol.setSortType(TableColumn.SortType.DESCENDING);

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
		addButton.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent e) {
					RefsetInstance newInstance = RefsetInstanceAccessor.createNewInstance(refsetType);
					data.add(newInstance);
//					newInstance.clear();
				}
			});
		}
	
	public AnchorPane getRoot() {
		return refsetAnchor;
	}

	public void setRefset(UUID refsetUUID) {
		refset = WBUtility.lookupSnomedIdentifierAsCV(refsetUUID);

//		try {
//			this.isAnnotation = refset.isAnnotationStyleRefex();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		String refsetFsn = WBUtility.getDescription(refsetUUID);
		refsetLabel.setText("Refset: " + refsetFsn);
		refsetLabel.setFont(new Font("Arial", 14));
	}
	
	public void setComponent(UUID componentUUID)  {
		Collection<? extends RefexChronicleBI<?>> members = new HashSet<>();
		ConceptVersionBI component = null;
		
		try {
//			if (!isAnnotation) {
//				members = refset.getRefsetMembersActive();
//			} else {
				component = WBUtility.lookupSnomedIdentifierAsCV(componentUUID);
				if (component == null)
				{
					System.err.println("Couldn't find component " + componentUUID);
				}
				else
				{
					members = component.getAnnotationsActive(vc);
				}
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		data.clear();
		boolean isSetup = false;
		
		for (RefexChronicleBI<?> member : members) {
			ConceptVersionBI refCon;
//			if (!isAnnotation) {
//				refCon = WBUtility.lookupSnomedIdentifierAsCV(member.getReferencedComponentNid());
//				if (member.getAssemblageNid() != refset.getNid()) {
//					continue;
//				}
//			} else {
				refCon = component;
				
				if (member.getAssemblageNid() != refset.getNid()) {
					continue;
				}
//			}


			if (member.getRefexType() != RefexType.MEMBER && !isSetup) {
					setupTable(member);
			}

			RefsetInstance instance = createRow(refCon, member);
			data.add(instance);
		}
		
		refsetRows.setItems(data);
	}

	@SuppressWarnings("unchecked")
	private void setupTable(RefexChronicleBI<?> member) {
		refsetType  = member.getRefexType();
		
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
						
						int nid = instance.getMemberNid();
						RefexStringVersionBI refex = (RefexStringVersionBI)WBUtility.getRefsetMember(nid);
						RefexCAB bp;
						try {
							bp = refex.makeBlueprint(vc,  IdDirective.PRESERVE, RefexDirective.INCLUDE);
						
							// Change String
							bp.put(ComponentProperty.STRING_EXTENSION_1, t.getNewValue());

							RefexChronicleBI<?> cabi = WBUtility.getBuilder().constructIfNotCurrent(bp);
							
							ConceptVersionBI refCon;
							if (isAnnotation) {
								refCon = WBUtility.lookupSnomedIdentifierAsCV(refex.getReferencedComponentNid());
							} else {
								refCon = WBUtility.lookupSnomedIdentifierAsCV(refex.getAssemblageNid());
							}
							
							WBUtility.commit(refCon);
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
			
			refsetRows.getColumns().addAll(col);
		} else if (refsetType == RefexType.CID_CID) {
			TableColumn col1 = new TableColumn("Component1");	
			col1.setCellValueFactory(
					new PropertyValueFactory<RefsetInstance,String>("cidExtFsn")
			);
			
			refsetRows.getColumns().addAll(col1);

			TableColumn col2 = new TableColumn("Component2");	
			col2.setCellValueFactory(
					new PropertyValueFactory<RefsetInstance,String>("cid2ExtFsn")
			);
			
			refsetRows.getColumns().addAll(col2);
		}
	}

	private RefsetInstance createRow(ConceptVersionBI refCon, RefexChronicleBI<?> memberChron) {
		try {
			RefexVersionBI member = memberChron.getVersion(vc);
	
			return RefsetInstanceAccessor.getInstance(refCon, member, refsetType);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

}
