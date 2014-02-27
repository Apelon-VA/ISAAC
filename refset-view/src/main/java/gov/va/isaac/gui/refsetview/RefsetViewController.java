package gov.va.isaac.gui.refsetview;


import gov.va.isaac.gui.refsetview.RefsetInstanceAccessor.RefsetInstance;
import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.UUID;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;

import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;

public class RefsetViewController {

    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private Slider hSlider;
    @FXML private TableView<RefsetInstance> refsetRow;
    @FXML private AnchorPane refsetAnchor;
    
    static ViewCoordinate vc = null;
    
    ObservableList<RefsetInstance> data = FXCollections.observableArrayList();
	private boolean isAnnotation = false;
	private ConceptVersionBI refset;
		

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

		refsetRow.getColumns().clear();

		TableColumn memberCol = new TableColumn("Reference Component");	
		memberCol.setCellValueFactory(
			    new PropertyValueFactory<RefsetInstance,String>("refConFsn")
		);
		
		refsetRow.getColumns().addAll(memberCol);
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
		Label refsetName = new Label("Refset: " + refsetFsn);
		refsetName.setFont(new Font("Arial", 14));

		refsetAnchor.getChildren().add(0, refsetName);
	}
	
	public void setComponent(UUID componentUUID)  {
		Collection<? extends RefexChronicleBI<?>> members = new HashSet<>();
		ConceptVersionBI component = null;
		
		try {
//			if (!isAnnotation) {
//				members = refset.getRefsetMembersActive();
//			} else {
				component = WBUtility.lookupSnomedIdentifierAsCV(componentUUID);
				members = component.getAnnotationsActive(vc);
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
		
		refsetRow.setItems(data);
	}

	private void setupTable(RefexChronicleBI<?> member) {
		if (member.getRefexType() == RefexType.STR) {
			TableColumn col = new TableColumn("String");	
			col.setCellValueFactory(
				    new PropertyValueFactory<RefsetInstance,String>("strExt")
			);
			
			refsetRow.getColumns().addAll(col);
		} else if (member.getRefexType() == RefexType.CID) {
			TableColumn col = new TableColumn("Component");	
			col.setCellValueFactory(
				    new PropertyValueFactory<RefsetInstance,String>("cidExtFsn")
			);
			
			refsetRow.getColumns().addAll(col);
		} else if (member.getRefexType() == RefexType.CID_CID) {
			TableColumn col1 = new TableColumn("Component1");	
			col1.setCellValueFactory(
				    new PropertyValueFactory<RefsetInstance,String>("cidExtFsn")
			);
			
			refsetRow.getColumns().addAll(col1);

			TableColumn col2 = new TableColumn("Component2");	
			col2.setCellValueFactory(
				    new PropertyValueFactory<RefsetInstance,String>("cid2ExtFsn")
			);
			
			refsetRow.getColumns().addAll(col2);
		}
	}

	private RefsetInstance createRow(ConceptVersionBI refCon, RefexChronicleBI<?> memberChron) {
		try {
			RefexVersionBI member = memberChron.getVersion(vc);
	
			return RefsetInstanceAccessor.getInstance(refCon, member);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

}
