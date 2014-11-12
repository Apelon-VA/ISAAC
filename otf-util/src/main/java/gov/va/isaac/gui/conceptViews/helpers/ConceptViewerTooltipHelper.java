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
 *	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.gui.conceptViews.helpers;

import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerHelper.ComponentType;
import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.util.Collection;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_long.RefexLongVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_string.RefexStringVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipType;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
*
* @author <a href="jefron@apelon.com">Jesse Efron</a>
*/
public class ConceptViewerTooltipHelper {
	private static final Logger LOG = LoggerFactory.getLogger(ConceptViewerTooltipHelper.class);
	private static boolean controlKeyPressed = false;
	private static boolean shiftKeyPressed = false;
	private EnhancedConceptViewerTooltipCache tooltipCache = new EnhancedConceptViewerTooltipCache();

	EventHandler<Event> getCompTooltipEnterHandler(ComponentVersionBI comp, ComponentType type) {
		return new EventHandler<Event> () {
			@Override
			public void handle(Event event) {
				Label l = (Label)event.getSource();
				if (shiftKeyPressed) {
					boolean errorFound = true;
					StringBuffer tpText = new StringBuffer();
	
					try {
						Collection<? extends RefexVersionBI<?>> annots = comp.getAnnotationsActive(WBUtility.getViewCoordinate());
						
						for (RefexVersionBI<?> annot : annots) {
							if (annot.getAssemblageNid() != ConceptViewerHelper.getSnomedAssemblageNid()) {
								try {
									tpText.append(getRefsetTooltip(annot));
										tpText.append("\n\n");
								} catch (Exception e) {
									LOG.error("Unable to access annotations", e);
									tpText = new StringBuffer("Unable to access annotations");
									errorFound = true;
								}
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
	
					if (!errorFound && tpText.toString().trim().isEmpty()) {
						l.getTooltip().setText("There are no refsets for this component");
					} else {
						l.getTooltip().setText(tpText.toString().trim());
					}
				} else if (controlKeyPressed){
					setDefaultTooltip(l, comp, type);
				} else {
					l.setTooltip(null);
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

	void setDefaultTooltip(Label node, ComponentVersionBI comp, ComponentType type) {
		int compNid = comp.getNid();
		long lastCommitTime = comp.getTime();
		
		if (tooltipCache.hasLatestTooltip(compNid, lastCommitTime)) {
			node.setTooltip(tooltipCache.getTooltip(compNid));
		} else {
			final Tooltip tp = createComponentTooltip(type, comp);
			node.setTooltip(tp);
			tooltipCache.updateCache(compNid, lastCommitTime, tp);
		}
	}


	private String getRefsetTooltip(RefexVersionBI<?> annot) throws IOException, ContradictionException {
		int annotNid = annot.getNid();
		long lastCommitTime = annot.getTime();
		
		if (tooltipCache.hasLatestTooltip(annotNid, lastCommitTime)) {
			return tooltipCache.getRefsetTooltip(annotNid);
		} else {
			final String refsetTooltipString = createRefsetTooltip(annot);
			tooltipCache.updateRefsetCache(annotNid, lastCommitTime, refsetTooltipString);

			return refsetTooltipString;
		}
	}

	private String createRefsetTooltip(RefexVersionBI<?> annot) throws IOException, ContradictionException {
		String refset = WBUtility.getConPrefTerm(annot.getAssemblageNid());
		StringBuffer strBuf = new StringBuffer();

		strBuf.append(refset + " is a ");
		
		if (annot.getRefexType() == RefexType.MEMBER) {
			strBuf.append("Member Annotation ");
		} else {
			if (annot.getRefexType() == RefexType.CID) {
				strBuf.append("Component Annotation");
			} else if (annot.getRefexType() == RefexType.STR) {
				strBuf.append("String Annotation");
			} else if (annot.getRefexType() == RefexType.LONG) {
				strBuf.append("Long Annotation");
			}

			strBuf.append(" with extension content:\n");

			if (annot.getRefexType() == RefexType.CID) {
				int nidExt = ((RefexNidVersionBI<?>)annot).getNid1();
				String compExt = WBUtility.getConceptVersion(nidExt).getPreferredDescription().getText();
				strBuf.append("Component #1: " + compExt);
			} else if (annot.getRefexType() == RefexType.STR) {
				String strExt = ((RefexStringVersionBI<?>)annot).getString1();
				strBuf.append("String #1: " + strExt);
			} else if (annot.getRefexType() == RefexType.LONG) {
				String longExt = Long.toString(((RefexLongVersionBI<?>)annot).getLong1());
				strBuf.append("String #1: " + longExt);
			}
		}
		
		strBuf.append(getStampTooltip(annot));
		return strBuf.toString();
	}

	private String createDescTooltipText(DescriptionVersionBI<?> desc) {
		String lang = desc.getLang();
		String text = desc.getText();
		String type = WBUtility.getConPrefTerm(desc.getTypeNid());
		String caseSig = desc.isInitialCaseSignificant() ? "Is Case Significant" : "Not Case Significant";

		return "Term: " + text + "\nType: " + type + "  Case Significant: " + caseSig + "  Language: " + lang + getStampTooltip(desc);
	}

	private String createConTooltipText(ConceptAttributeVersionBI<?> attr) {
		return "SctId: " + ConceptViewerHelper.getSctId(attr)+ " " + ConceptViewerHelper.getPrimDef(attr) + getStampTooltip(attr);
	}
	
	private String createRelTooltipText(RelationshipVersionBI<?> rel) {

		
		String refinCharType = "";
		try {
			refinCharType = RelationshipType.getRelationshipType(rel.getRefinabilityNid(), rel.getCharacteristicNid()).toString();
		} catch (NullPointerException npe) {
			LOG.error("RelationshipType.getRelationshipType() doesn't handle Additional OR RF1 Relationship Types.  Tracker created");
			refinCharType = "Additional and RF1 relationship type not handled properly";
		} catch (Exception e) {
			LOG.error("Unknown error in identifying relationship type");
		}
		String group = Integer.toString(rel.getGroup());
		String type = WBUtility.getConPrefTerm(rel.getTypeNid());
		String target = WBUtility.getConPrefTerm(rel.getDestinationNid());
		String statInf = rel.isInferred() ? "False" : "True";

		return "Destination: " + target + "  Type: " + type + "\nStated: " + statInf + "  Relationship Type: " + refinCharType + "  Role Group: " + group + getStampTooltip(rel);
	}

	private Tooltip createComponentTooltip(ComponentType type, ComponentVersionBI comp) {
		final Tooltip tp = new Tooltip();
		
		String txt;
		if (type == ComponentType.CONCEPT) {
			txt = createConTooltipText((ConceptAttributeVersionBI<?>)comp);
		} else if (type == ComponentType.DESCRIPTION) {
			txt = createDescTooltipText((DescriptionVersionBI<?>)comp);
		} else {
			txt = createRelTooltipText((RelationshipVersionBI<?>)comp);
		}

		tp.setText(txt);

		return tp;
	}

	// Control Handling Functionality
	public EventHandler<KeyEvent> getCtrlKeyReleasedEventHandler(){
		return new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.CONTROL)
				{
					controlKeyPressed = false;
				} 

				if (event.getCode() == KeyCode.SHIFT)
				{
					shiftKeyPressed = false;
				}
			}
		};
	}

	public EventHandler<KeyEvent> getCtrlKeyPressEventHandler(){
		return new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.CONTROL)
				{
					controlKeyPressed = true;
				} 

				if (event.getCode() == KeyCode.SHIFT){
					shiftKeyPressed = true;
				}
			}
		};
	}

	private String getStampTooltip(ComponentVersionBI comp) {
		String status = WBUtility.getStatusString(comp);
		String time =  WBUtility.getTimeString(comp);
		String author = WBUtility.getAuthorString(comp); 
		String module = WBUtility.getModuleString(comp);
		String path = WBUtility.getPathString(comp);

		return "\nStatus: " + status + "  Time: " + time + "  Author: " + author + "  Module: " + module + "  Path: " + path;
	}
}
