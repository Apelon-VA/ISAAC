package gov.va.isaac.gui.conceptViews.enhanced;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.gui.conceptViews.helpers.EnhancedConceptBuilder;
import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.otf.tcc.model.cc.ReferenceConcepts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreferredAcceptabilityPrompt {
	public enum PrefAcceptResponse { COMMIT, CANCEL };

	private static PrefAcceptResponse buttonSelected = PrefAcceptResponse.CANCEL;

	private static Font boldFont = new Font("System Bold", 13.0);

	private static ChoiceBox<SimpleDisplayConcept> langDropDown = new ChoiceBox<SimpleDisplayConcept>();
	private static ConceptVersionBI usLangRefex; 

	
	private static int prefDescNid;
	private static RefexNidVersionBI<?> prefRefexMember;
	private static Map<Integer, RadioButton> prefMap = new HashMap<Integer, RadioButton>();

	
	private static Map<Integer, CheckBox> acceptMap = new HashMap<Integer, CheckBox>();
	private static Map<Integer, RefexNidVersionBI<?>> acceptRefexMap = new HashMap<Integer, RefexNidVersionBI<?>>();


	private static final Logger LOG = LoggerFactory.getLogger(EnhancedConceptBuilder.class);

	static class Prompt extends Stage {
	
		public Prompt( String title, Stage owner, Scene scene) {
		    setTitle( title );
		    initStyle( StageStyle.UTILITY );
		    initModality( Modality.APPLICATION_MODAL );
		    initOwner( owner );
		    setResizable( false );
		    setScene( scene );
		}
		public void showDialog() {
		    sizeToScene();
		    centerOnScreen();
		    showAndWait();
		}
	}

	public static void definePrefAcceptConcept(Stage owner, String title, ConceptVersionBI con) {
	    VBox vb = new VBox(15);
	    vb.setAlignment(Pos.CENTER);
	    vb.setPadding( new Insets(10,10,10,10) );
	    vb.setSpacing( 10 );

	    Scene scene = new Scene( vb );
	    final Prompt prompt = new Prompt( title, owner, scene);

	    HBox languageSelectionHBox = setupLangugaeSelection();

	    GridPane gp = createGridPane(con);

	    HBox buttonHBox = setupButtons(prompt, con);
	    
	    vb.getChildren().addAll(languageSelectionHBox, gp, buttonHBox);
	    prompt.showDialog();
	}

	private static HBox setupButtons(Prompt prompt, ConceptVersionBI con) {
	    Button commitButton = new Button( "Commit" );
	    commitButton.setOnAction((e) -> {
            prompt.close();
            try {
				commitChanges(con);
			} catch (Exception e1) {
				LOG.error("Failure to commit selected changes", e);
			}
            buttonSelected = PrefAcceptResponse.COMMIT;
	    } );

	    Button cancelButton = new Button( "Cancel" );
	    cancelButton.setOnAction((e) -> {
	            prompt.close();
	            buttonSelected = PrefAcceptResponse.CANCEL;
	    } );
	    
	    HBox buttonHBox = new HBox(15);
	    buttonHBox.setPadding(new Insets(15));
	    buttonHBox.setAlignment( Pos.CENTER );
	    buttonHBox.getChildren().addAll(commitButton, cancelButton);
	    
		return buttonHBox;
	}

	private static void commitChanges(ConceptVersionBI con) throws IOException, InvalidCAB, ContradictionException {
		
		ConceptVersionBI langRefex = PreferredAcceptabilityPrompt.getLangRefex();
		
		if (PreferredAcceptabilityPrompt.getOldPrefDescNid() != PreferredAcceptabilityPrompt.getNewPrefDescNid()) {
			// retire old Pref member
			RefexNidVersionBI<?> oldPrefMember = PreferredAcceptabilityPrompt.getOldPrefMember();
			retireRefexMember(oldPrefMember);
			
			// if new Pref already Acceptable, retire acceptable
			int newPrefDescNid = PreferredAcceptabilityPrompt.getNewPrefDescNid();

			if (PreferredAcceptabilityPrompt.isOldAcceptDesc(newPrefDescNid)) {
				RefexNidVersionBI<?> oldAcceptMember = PreferredAcceptabilityPrompt.getOldAcceptMember(newPrefDescNid);
				retireRefexMember(oldAcceptMember);
			}
			
			// add new Pref member
			DescriptionVersionBI<?> d = (DescriptionVersionBI<?>)WBUtility.getComponentVersion(newPrefDescNid);
			addLangRefexMember(langRefex, d, SnomedMetadataRf2.PREFERRED_RF2.getUuids()[0]);
		}

		for (DescriptionVersionBI<?> d : con.getDescriptionsActive()) {
			if (d.getNid() != con.getFullySpecifiedDescription().getNid()) {
				if (PreferredAcceptabilityPrompt.isNewAcceptDesc(d.getNid()) && !PreferredAcceptabilityPrompt.isOldAcceptDesc(d.getNid())) {
					// Add as Acceptable member on d
					addLangRefexMember(langRefex, d, SnomedMetadataRf2.ACCEPTABLE_RF2.getUuids()[0]);

				} else if (!PreferredAcceptabilityPrompt.isNewAcceptDesc(d.getNid()) && PreferredAcceptabilityPrompt.isOldAcceptDesc(d.getNid())) {
					// Retire previous Acceptable member
					RefexNidVersionBI<?> oldAcceptMember = PreferredAcceptabilityPrompt.getOldAcceptMember(d.getNid());
					retireRefexMember(oldAcceptMember);
				}
			}
		}

		try {
	        AppContext.getRuntimeGlobals().disableAllCommitListeners();
			WBUtility.commit();
		} catch (Exception e) {
	        LOG.error("Coudn't commit selected preferred/acceptability changes", e);
		} finally {
	        AppContext.getRuntimeGlobals().enableAllCommitListeners();
		}
	}


	private static void retireRefexMember(RefexNidVersionBI<?> member) {
		try {
			RefexCAB bp = member.makeBlueprint(WBUtility.getViewCoordinate(),  IdDirective.PRESERVE, RefexDirective.INCLUDE);
			if (bp.getMemberUUID() == null) {
				bp.setMemberUuid(member.getPrimordialUuid());
			}
			bp.setStatus(Status.INACTIVE);
			WBUtility.getBuilder().constructIfNotCurrent(bp);
			ConceptVersionBI refCon = WBUtility.getConceptVersion(member.getConceptNid());
	
			WBUtility.addUncommitted(refCon);
		} catch (Exception e) {
			AppContext.getCommonDialogs().showErrorDialog("Failed to retire member: " + member, e);
		}
	}


	private static void addLangRefexMember(ConceptVersionBI assemblageCon, DescriptionVersionBI<?> description, UUID typeUUId) throws IOException, InvalidCAB, ContradictionException {

		RefexCAB newMember = new RefexCAB(RefexType.CID, description.getNid(),  assemblageCon.getNid(), IdDirective.GENERATE_HASH, RefexDirective.INCLUDE);

		newMember.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, typeUUId);
		
		 WBUtility.getBuilder().construct(newMember);

		WBUtility.addUncommitted(description.getConceptNid());
	}
	private static HBox setupLangugaeSelection() {
	    HBox languageSelectionHBox = new HBox(10);
	    Label langSelect = createLabel("Select Language Dialect to Define");
	    
	    ObservableList<SimpleDisplayConcept> langConsList = FXCollections.observableArrayList(new ArrayList<SimpleDisplayConcept>());

	    try {
		    //bca0a686-3516-3daf-8fcf-fe396d13cfad is US Eng Language type reference set
	    	usLangRefex = WBUtility.getConceptVersion(UUID.fromString("bca0a686-3516-3daf-8fcf-fe396d13cfad"));
	    	String desc = usLangRefex.getFullySpecifiedDescription().getText();
			int endIdx = desc.indexOf("language reference set");
			String displayStr = desc.substring(0, endIdx).trim();
			SimpleDisplayConcept sdc = new SimpleDisplayConcept(displayStr, usLangRefex.getNid());
			langConsList.add(sdc);
	    } catch (Exception e) {
	    	LOG.error("Unable to identify the available languages", e);
	    }
	    langDropDown.setItems(langConsList);
	    langDropDown.getSelectionModel().select(0);
	    
	    languageSelectionHBox.getChildren().addAll(langSelect, langDropDown);
	    
		return languageSelectionHBox;
	}

	private static GridPane createGridPane(ConceptVersionBI con) {
		GridPane gp = new GridPane();
		gp.setHgap(10);
		gp.setVgap(10);
		gp.setPadding(new Insets(15));
//		gp.setGridLinesVisible(true);
		
		Label desc = createLabel("Concept's Descriptions");
		Label preferred = createLabel("Preferability");
		Label accept = createLabel("Acceptability");

		String maxDesc = "";
	    try {
			for (DescriptionVersionBI<?> d : con.getDescriptionsActive()) {
				if (maxDesc.length() < d.getText().length()) {
					maxDesc = d.getText();
				}
			}
			
			gp.setConstraints(new Label(maxDesc),  0,  0,  1,  1,  HPos.LEFT,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);
			gp.setConstraints(preferred,  1,  0,  1,  1,  HPos.CENTER,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);
			gp.setConstraints(accept,  2,  0,  1,  1,  HPos.CENTER,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);
	
			gp.addRow(0, desc, preferred, accept);
			
			int i = 0;
			ToggleGroup prefTermGroup = new ToggleGroup();
			
			for (DescriptionVersionBI<?> d : con.getDescriptionsActive()) {
				if (d.getNid() != con.getFullySpecifiedDescription().getNid()) {
					CheckBox cb = new CheckBox();
				    
					RadioButton rb = new RadioButton();
					rb.setToggleGroup(prefTermGroup);
					GridPane.setHalignment(cb, HPos.CENTER);
					GridPane.setHalignment(rb, HPos.CENTER);
					
					
					if (con.getPreferredDescription().getNid() == d.getNid()) {
						rb.setSelected(true);
						prefDescNid = d.getNid();
						prefRefexMember = gertPrefMember(d);
						prefMap.put(d.getNid(), rb);
					} else {
						rb.setSelected(false);
						prefMap.put(d.getNid(), rb);
					}
	
					
					if (isAcceptableTerm(d)) {
						cb.setSelected(true);
						acceptMap.put(d.getNid(), cb);
					} else {
						cb.setSelected(false);
						acceptMap.put(d.getNid(), cb);
					}

					gp.addRow(++i, new Label(d.getText()), rb, cb);
				}
			}
	    } catch (Exception e) {
	    	LOG.error("Unable to identify all languages", e);
	    }
	    
		return gp ;
	}


	private static boolean isAcceptableTerm(DescriptionVersionBI<?> d) {
		try {
	        for (RefexVersionBI<?> refex : d.getRefexMembersActive(WBUtility.getViewCoordinate())) {
	            if (refex.getAssemblageNid() == langDropDown.getSelectionModel().getSelectedItem().getNid()) {
	                RefexNidVersionBI<?> langRefex = (RefexNidVersionBI<?>) refex;
	
	                if ((langRefex.getNid1() == ReferenceConcepts.ACCEPTABLE_ACCEPTABILITY.getNid()) || 
	            		(langRefex.getNid1() == SnomedMetadataRf2.ACCEPTABLE_RF2.getNid())) {
						acceptRefexMap .put(d.getNid(), langRefex);
	                	return true;
	                }
	            }
	        }
		} catch (Exception e) {
			AppContext.getCommonDialogs().showErrorDialog("Can't identify Preferred/Acceptable value for description: " + d.getText(), e);
		}
        return false;
	}

	private static RefexNidVersionBI<?> gertPrefMember(DescriptionVersionBI<?> d) {
		try {
	        for (RefexVersionBI<?> refex : d.getRefexMembersActive(WBUtility.getViewCoordinate())) {
	            if (refex.getAssemblageNid() == langDropDown.getSelectionModel().getSelectedItem().getNid()) {
	                RefexNidVersionBI<?> langRefex = (RefexNidVersionBI<?>) refex;
	
	                if ((langRefex.getNid1() == ReferenceConcepts.PREFERRED_ACCEPTABILITY_RF1.getNid()) || 
	            		(langRefex.getNid1() == SnomedMetadataRf2.PREFERRED_RF2.getNid())) {
	                	return langRefex;
	                }
	            }
	        }
		} catch (Exception e) {
			AppContext.getCommonDialogs().showErrorDialog("Can't identify Preferred/Acceptable value for description: " + d.getText(), e);
		}
        return null;
	}

	private static Label createLabel(String str) {
		Label l = new Label(str);
		l.setFont(boldFont);

		return l;
	}

	public static PrefAcceptResponse getButtonSelected() {
		return buttonSelected;
	}


	public static ConceptVersionBI getLangRefex() {
		return usLangRefex;
	}

	public static RefexNidVersionBI<?> getOldPrefMember() {
		return prefRefexMember;		
	}

	public static int getOldPrefDescNid() {
		return prefDescNid;		
	}

	public static int getNewPrefDescNid() {
		for (Integer descNid : prefMap.keySet()) {
			if (prefMap.get(descNid).isSelected()) {
				return descNid;
			}
		}
		
		return 0;
	}

	public static RefexNidVersionBI<?> getOldAcceptMember(int nid) {
		return acceptRefexMap.get(nid);
	}

	public static boolean isNewAcceptDesc(int nid) {
		return acceptMap.get(nid).isSelected();
	}

	public static boolean isOldAcceptDesc(int nid) {
		return acceptRefexMap.keySet().contains(nid);
	}
}
