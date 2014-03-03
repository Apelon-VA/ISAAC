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
import gov.va.isaac.gui.refsetview.RefsetInstanceAccessor.RefsetInstance;
import gov.va.isaac.models.cem.importer.CEMMetadataBinding;
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

/**
 * RefsetViewController
 *
 * @author <a href="jefron@apelon.com">Jesse Efron</a>
 */
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
		
		try {
			for (RefexChronicleBI<?> memChron : members) {
				RefexVersionBI member = memChron.getVersion(WBUtility.getViewCoordinate());
				
				ConceptVersionBI refCompCon;
				if (!isAnnotation) {
					refCompCon = WBUtility.lookupSnomedIdentifierAsCV(member.getReferencedComponentNid());
				} else {
					refCompCon = component;
				}

				if (member.getAssemblageNid() == CEMMetadataBinding.CEM_COMPOSITION_REFSET.getNid() &&
					member.getAssemblageNid() == refset.getNid()) {
					isSetup = handleComplexRefset(member, refCompCon, isSetup);
				} else {
					isSetup = processMembers(member, refCompCon, isSetup);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		refsetRows.setItems(data);
	}


	private boolean handleComplexRefset(RefexVersionBI member, ConceptVersionBI refCompCon, boolean isSetup) {
		if (!isSetup && member.getRefexType() != RefexType.MEMBER) {
			RefsetTableHandler.setupTable(member, isAnnotation, refsetRows, refCompCon);
			refsetType = member.getRefexType();
			isSetup = true;
		}
		
		// Have needed member, add to data
		CEMCompositRefestInstance instance = (CEMCompositRefestInstance)RefsetInstanceAccessor.getInstance(refCompCon, member, RefexType.UNKNOWN);
		
		data.add(instance);

		return isSetup;
	}

	private boolean processMembers(RefexVersionBI member, ConceptVersionBI refCompCon, boolean isSetup) throws IOException {
		if (member.getAssemblageNid() == refset.getNid()) {
			// Setup if Necessary
			if (!isSetup && member.getRefexType() != RefexType.MEMBER) {
				RefsetTableHandler.setupTable(member, isAnnotation, refsetRows, refCompCon);
				refsetType = member.getRefexType();
				isSetup = true;
			}

			// Have needed member, add to data
			RefsetInstance instance = RefsetInstanceAccessor.getInstance(refCompCon, member, refsetType);
			data.add(instance);
		}

		// Search for member's annotations
		Collection<? extends RefexVersionBI<?>> refAnnots = member.getAnnotationsActive(WBUtility.getViewCoordinate());
		for (RefexVersionBI annot : refAnnots) {
			isSetup = processMembers(annot, refCompCon, isSetup);
		}

		return isSetup;
	}
}
