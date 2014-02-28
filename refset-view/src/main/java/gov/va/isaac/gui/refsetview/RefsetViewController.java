package gov.va.isaac.gui.refsetview;


import gov.va.isaac.gui.refsetview.RefsetInstanceAccessor.RefsetInstance;
import gov.va.isaac.util.WBUtility;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;

public class RefsetViewController {

	//@FXML private Slider hSlider;
	@FXML private TableView<RefsetInstance> refsetRows;
	@FXML private AnchorPane refsetAnchor;
	@FXML private Button addButton;
	@FXML private Button commitButton;
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

		 RefsetTableHandler.initializeTable(refsetRows);
		 addButton.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent e) {
					RefsetInstance newInstance = RefsetInstanceAccessor.createNewInstance(refsetType);
					data.add(newInstance);
				}
			});
		 
		 commitButton.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent e) {
					WBUtility.commit();
				}
		 	});
		}
	
	public AnchorPane getRoot() {
		return refsetAnchor;
	}

	public void setRefsetAndComponent(UUID refsetUUID, UUID componentUUID)  {
		
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
				RefsetTableHandler.setupTable(member, isAnnotation, refsetRows);
				refsetType = member.getRefexType();
			}

			RefsetInstance instance = createRow(refCon, member);
			data.add(instance);
		}
		
		refsetRows.setItems(data);
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
