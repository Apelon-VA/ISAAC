package gov.va.isaac.gui.conceptViews;

import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.util.Collection;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderWidths;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_long.RefexLongVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipType;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConceptViewerHelper {
	private static boolean controlKeyPressed = false;
	private static final Logger LOG = LoggerFactory.getLogger(ConceptViewerHelper.class);
	private static int snomedAssemblageNid;

	ConceptViewerHelper() {
		snomedAssemblageNid = WBUtility.getConceptVersion(TermAux.SNOMED_IDENTIFIER.getUuids()[0]).getNid();
	}

	public enum ComponentType {
		CONCEPT, DESCRIPTION, RELATIONSHIP;
	}

	EventHandler getCompTooltipEnterHandler(ComponentVersionBI comp) {
		return new EventHandler<Event> () {
			@Override
			public void handle(Event event) {
				Label l = (Label)event.getSource();
				if (controlKeyPressed) {
					String tp = "There are no refsets for this component";
	
					try {
						Collection<? extends RefexVersionBI<?>> annots = comp.getAnnotationsActive(WBUtility.getViewCoordinate());
						
						for (RefexVersionBI annot : annots) {
							String refset = WBUtility.getConPrefTerm(annot.getAssemblageNid());
							String s = annot.toString();
							tp = "Assemblage: " + refset;
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
	
					l.getTooltip().setText(tp);
					l.getTooltip().setFont(new Font(16));
				}
			}
		};
	}


	EventHandler getCompTooltipExitHandler(ComponentVersionBI comp, ComponentType type) {
		return new EventHandler<Event>() {

			@Override
			public void handle(Event event) {
				Label l = (Label)event.getSource();
				setDefaultTooltip(l, comp, type);
			}
		};
	}

	private String createDescTooltipText(DescriptionVersionBI desc) {
		String lang = desc.getLang();
		String text = desc.getText();
		String type = WBUtility.getConPrefTerm(desc.getTypeNid());
		String caseSig = desc.isInitialCaseSignificant() ? "Is Case Significant" : "Not Case Significant";
		
		String status = WBUtility.getStatusString(desc);
		String time =  WBUtility.getTimeString(desc);
		String author = WBUtility.getAuthorString(desc); 
		String module = WBUtility.getModuleString(desc);
		String path = WBUtility.getPathString(desc);

		return "Term: " + text + "\nType: " + type + " Case Significant: " + caseSig + " Language: " + lang + " \nStatus" + status + " Time: " + time + " Author: " + author + " Module: " + module + " Path: " + path;
	}

	private String createConTooltipText(ConceptAttributeVersionBI attr) {
		String status = WBUtility.getStatusString(attr);
		String time =  WBUtility.getTimeString(attr);
		String author = WBUtility.getAuthorString(attr); 
		String module = WBUtility.getModuleString(attr);
		String path = WBUtility.getPathString(attr);

		return "SctId: " + getSctId(attr)+ " " + getPrimDef(attr)+ "\nStatus" + status + " Time: " + time + " Author: " + author + " Module: " + module + " Path: " + path;
	}
	
	private String createRelTooltipText(RelationshipVersionBI rel) {

        
        String refinCharType = "";
		try {
			refinCharType = RelationshipType.getRelationshipType(rel.getRefinabilityNid(), rel.getCharacteristicNid()).toString();
		} catch (NullPointerException npe) {
			LOG.error("RelationshipType.getRelationshipType() doesn't handle AdditionalRelationship.  Tracker created");
			refinCharType = "AdditionalRelationshipType not handled properly";
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String group = Integer.toString(rel.getGroup());
		String type = WBUtility.getConPrefTerm(rel.getTypeNid());
		String target = WBUtility.getConPrefTerm(rel.getDestinationNid());
		String statInf = rel.isInferred() ? "False" : "True";

		String status = WBUtility.getStatusString(rel);
		String time =  WBUtility.getTimeString(rel);
		String author = WBUtility.getAuthorString(rel); 
		String module = WBUtility.getModuleString(rel);
		String path = WBUtility.getPathString(rel);

		return "Destination: " + target + " Type: " + type + "\nStated: " + statInf + " Relationship Type: " + refinCharType + " Role Group: " + group+ "\nStatus: " + status + " Time: " + time + " Author: " + author + " Module: " + module + " Path: " + path;
	}

	public void handleOnKeyReleased(KeyEvent event)
	{
		if (event.getCode() == KeyCode.CONTROL)
		{
			controlKeyPressed = false;
		}
	}
	
	public void handleOnKeyPressed(KeyEvent event)
	{
		if (event.getCode() == KeyCode.CONTROL)
		{
			controlKeyPressed = true;
		}
	}


	public void initializeLabel(Label label, ComponentVersionBI comp, ComponentType type, String txt) {
		setDefaultTooltip(label, comp, type);
    	label.setText(txt);

		label.addEventHandler(MouseEvent.MOUSE_ENTERED, getCompTooltipEnterHandler(comp));
		label.addEventHandler(MouseEvent.MOUSE_EXITED, getCompTooltipExitHandler(comp, type));
	}

	private void setDefaultTooltip(Label node, ComponentVersionBI comp, ComponentType type) {
		final Tooltip tp = new Tooltip();
        
        String txt;
		if (type == ComponentType.CONCEPT) {
			txt = createConTooltipText((ConceptAttributeVersionBI)comp);
		} else if (type == ComponentType.DESCRIPTION) {
			txt = createDescTooltipText((DescriptionVersionBI)comp);
		} else {
			txt = createRelTooltipText((RelationshipVersionBI)comp);
		}

		tp.setText(txt);
        tp.setFont(new Font(16));
		node.setTooltip(tp);
	}


	public Label createComponentLabel(ComponentVersionBI comp, String txt, ComponentType type, boolean isTypeLabel) {
		Label l = new Label();
		l.setFont(new Font(18));
		l.setTextFill(Color.BLUE);
		
		if (isTypeLabel) {
			l.setBackground(new Background(new BackgroundFill(Color.YELLOW, null, null)));
			l.setBorder(new Border(new BorderStroke(Color.RED, null, null, new BorderWidths(2))));
		}
		
		initializeLabel(l, comp, type, txt);
		
		return l;
	}


	public String getSctId(ConceptAttributeVersionBI attr)  {
        String sctidString = "Unreleased";
        // Official approach found int AlternativeIdResource.class
        
        try {
	        for (RefexChronicleBI<?> annotation : attr.getAnnotations()) {
				if (annotation.getAssemblageNid() == snomedAssemblageNid) {
					RefexLongVersionBI sctid = (RefexLongVersionBI) annotation.getPrimordialVersion();
					sctidString = Long.toString(sctid.getLong1());
				}
			}
        } catch (Exception e) {
        	LOG.error("Could not access annotations for: " + attr.getPrimordialUuid());
        }
        return sctidString;
	}


	public String getPrimDef(ConceptAttributeVersionBI attr) {
        String status = "Primitive";
		if (attr.isDefined()) {
			status = "Fully Defined";
		}
		
        return status;
	}
}
