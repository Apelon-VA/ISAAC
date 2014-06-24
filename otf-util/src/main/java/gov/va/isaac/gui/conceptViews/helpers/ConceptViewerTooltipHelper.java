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
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import javafx.scene.text.Font;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
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
	ConceptViewerHelper viewerHelper = new ConceptViewerHelper();

	EventHandler getCompTooltipEnterHandler(ComponentVersionBI comp) {
		return new EventHandler<Event> () {
			@Override
			public void handle(Event event) {
				Label l = (Label)event.getSource();
				if (viewerHelper.getControlKeyPressed()) {
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

		return "SctId: " + viewerHelper.getSctId(attr)+ " " + viewerHelper.getPrimDef(attr)+ "\nStatus" + status + " Time: " + time + " Author: " + author + " Module: " + module + " Path: " + path;
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

	void setDefaultTooltip(Label node, ComponentVersionBI comp, ComponentType type) {
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
}
