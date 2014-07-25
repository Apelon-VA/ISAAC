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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
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

	//TODO this class is really an info-model viewer - not a generic refset view.  We should move this code... somewhere... maybe into 
	//the infomodel package itself, which would fix some dependency issues.  And then get rid of the refset-view module - which is confusing.
	//Stick with just calling everything 'refexes' which I believe, is the current preferred terminology. 
	//@FXML private Slider hSlider;
	@FXML private TableView<RefsetInstance> refsetRows;
	@FXML private AnchorPane refsetAnchor;
	@FXML private Button addButton;
	@FXML private Button commitButton;
	@FXML private Label refsetLabel;
	
	ObservableList<RefsetInstance> data = FXCollections.observableArrayList();
	private boolean isAnnotation = false;
	private UUID refsetUUID_;
	private int refsetNid_;
	private UUID componentUUID_;
	private RefexType refsetType = RefexType.MEMBER;
	
	private boolean activeOnly_ = false;
	private RefsetTableHandler rth_ = null;

	public static RefsetViewController init() throws IOException {
		// Load from FXML.
		URL resource = RefsetViewController.class.getResource("RefsetView.fxml");
		FXMLLoader loader = new FXMLLoader(resource);
		loader.load();

		return loader.getController();
	}

	@FXML 
	void initialize() {
		rth_ = new RefsetTableHandler(refsetRows, this);
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
					reloadData();
				}
			});
	}
	
	public AnchorPane getRoot() {
		return refsetAnchor;
	}

	public void setRefsetAndComponent(UUID refsetUUID, UUID componentUUID)  {
		
		refsetUUID_ = refsetUUID;
		componentUUID_ = componentUUID;

//		try {
//			this.isAnnotation = refset.isAnnotationStyleRefex();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		String refsetFsn = WBUtility.getDescription(refsetUUID);
		refsetLabel.setText("Refset: " + refsetFsn);
		refsetLabel.setFont(new Font("Arial", 14));
		
		reloadData();
	}
	
	protected void reloadData()
	{
		Collection<? extends RefexChronicleBI<?>> members = new HashSet<>();
		ConceptVersionBI component = null;
		
		try {
//			if (!isAnnotation) {
//				members = refset.getRefsetMembersActive();
//			} else {
				component = WBUtility.getConceptVersion(componentUUID_);
				if (component == null)
				{
					System.err.println("Couldn't find component " + componentUUID_);
				}
				else
				{
					members = (activeOnly_ ? component.getAnnotationsActive(WBUtility.getViewCoordinate()) : component.getAnnotations());
				}
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		data.clear();
		ConceptVersionBI refset = WBUtility.getConceptVersion(refsetUUID_);
		refsetNid_ = refset.getNid();
		
		try {
			for (RefexChronicleBI<?> memChron : members) {
				List<RefexVersionBI<?>> memberVersion = new ArrayList<>();
				if (activeOnly_)
				{
					RefexVersionBI<?> version = memChron.getVersion(WBUtility.getViewCoordinate());
					
					if (version.isActive()) {
						memberVersion.add(version);
					}
				}
				else
				{
					memberVersion.addAll(memChron.getVersions());
				}
				RefexVersionBI<?> previousMember = null;
				
				for (RefexVersionBI<?> member : memberVersion)
				{
					ConceptVersionBI refCompCon;
					if (!isAnnotation) {
						refCompCon = WBUtility.getConceptVersion(member.getReferencedComponentNid());
					} else {
						refCompCon = component;
					}
	
					//TODO we shouldn't have any references to the CEM model here in the generic refset viewer.
					//If we want to enable special filtering, or something like that - then we should have an API that allows refset types to be passed in
					if (member.getAssemblageNid() == CEMMetadataBinding.CEM_COMPOSITION_REFSET.getNid() &&
						member.getAssemblageNid() == refset.getNid()) {
						handleComplexRefset(member, previousMember, refCompCon);
					} else {
						processMembers(member, previousMember, refCompCon);
					}
					
					previousMember = member;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		refsetRows.setItems(data);
	}


	private void handleComplexRefset(RefexVersionBI<?> member, RefexVersionBI<?> previousMember, ConceptVersionBI refCompCon) {
		if (!rth_.isSetupFinished() && member.getRefexType() != RefexType.MEMBER) {
			rth_.finishTableSetup(member, isAnnotation, refsetRows, refCompCon, member.getAssemblageNid());
			refsetType = member.getRefexType();
		}
		
		// Have needed member, add to data
		CEMCompositRefestInstance instance = (CEMCompositRefestInstance)RefsetInstanceAccessor.getInstance(refCompCon, member, previousMember, RefexType.UNKNOWN);
		
		data.add(instance);
	}

	private void processMembers(RefexVersionBI<?> member, RefexVersionBI<?> previousMember, ConceptVersionBI refCompCon) throws IOException, ContradictionException {
		if (member.getAssemblageNid() == refsetNid_) {
			// Setup if Necessary
			//TODO The current code only works if every member has the same RefexType (and there is _no_ guarantee about that in the APIs.  
			//The entire column display of the tables needs to be reworked, as the columns that are displayed needs to be dynamically detected 
			//from the data in the table, so it can take into account multiple refex types.
			if (!rth_.isSetupFinished() && member.getRefexType() != RefexType.MEMBER) {
				rth_.finishTableSetup(member, isAnnotation, refsetRows, refCompCon, member.getAssemblageNid());
				refsetType = member.getRefexType();
			}

			// Have needed member, add to data
			RefsetInstance instance = RefsetInstanceAccessor.getInstance(refCompCon, member, previousMember, refsetType);
			data.add(instance);
		}

		// Search for member's annotations
		Collection<? extends RefexChronicleBI<?>> refAnnots = (activeOnly_ ? member.getAnnotationsActive(WBUtility.getViewCoordinate()) : member.getAnnotations());
		for (RefexChronicleBI<?> annot : refAnnots) {
			List<RefexVersionBI<?>> annotVersions = new ArrayList<>();
			if (activeOnly_)
			{
				RefexVersionBI<?> version = (RefexVersionBI<?>) annot.getVersion(WBUtility.getViewCoordinate());
				if (version.isActive()) {
					annotVersions.add(version);
				}
			}
			else {
				annotVersions.addAll((Collection<? extends RefexVersionBI<?>>) annot.getVersions());
			}
			
			RefexVersionBI<?> prevAnnotVersion = null;
			for (RefexVersionBI<?> annotVersion : annotVersions)
			{
				processMembers(annotVersion, prevAnnotVersion, refCompCon);
				
				prevAnnotVersion = annotVersion;
			}
		}
	}
	
	public void setViewActiveOnly(boolean activeOnly)
	{
		activeOnly_ = activeOnly;
	}
}
