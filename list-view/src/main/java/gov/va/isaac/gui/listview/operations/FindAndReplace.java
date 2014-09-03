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
package gov.va.isaac.gui.listview.operations;

import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.gui.listview.operations.FindAndReplaceController.DescriptionType;
import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.beans.binding.BooleanExpression;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;

import org.apache.commons.lang.StringUtils;
import org.glassfish.hk2.api.PerLookup;
import org.ihtsdo.otf.tcc.api.blueprint.DescriptionCAB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FindAndReplace}
 * 
 * A Find / Replace operation, mostly ported from the SearchReplaceDialog in TK2
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@PerLookup
public class FindAndReplace extends Operation
{

	private FindAndReplaceController frc_;
	private Logger logger_ = LoggerFactory.getLogger(this.getClass());
	private Map<String, Set<String>> successCons = new HashMap<>();
	
	private FindAndReplace()
	{
		//For HK2 to init
	}
	@Override
	public void init(ObservableList<SimpleDisplayConcept> conceptList)
	{
		super.init(conceptList);
		try
		{
			URL resource = FindAndReplace.class.getResource("FindAndReplaceController.fxml");
			FXMLLoader loader = new FXMLLoader(resource);
			loader.load();
			frc_ = loader.getController();
			super.root_ = frc_.getRoot();
		}
		catch (IOException e)
		{
			logger_.error("Unexpected error building panel", e);
			throw new RuntimeException("Error building panel");
		}
	}

	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#getTitle()
	 */
	@Override
	public String getTitle()
	{
		return "Find and Replace";
	}

	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#conceptListChanged()
	 */
	@Override
	protected void conceptListChanged()
	{
		//noop
	}

	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#isValid()
	 */
	@Override
	public BooleanExpression isValid()
	{
		return frc_.allFieldsValid();
	}

	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#getOperationDescription()
	 */
	@Override
	public String getOperationDescription()
	{
		return "Perform a Find and Replace operation on content in the database";
	}

	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#createTask()
	 */
	@Override
	public CustomTask<OperationResult> createTask()
	{
		return new CustomTask<OperationResult>(FindAndReplace.this)
		{
			private Matcher matcher;

			@Override
			protected OperationResult call() throws Exception
			{
				double i = 0;
				successCons.clear();
				Set<SimpleDisplayConcept> modifiedConcepts = new HashSet<SimpleDisplayConcept>();
				for (SimpleDisplayConcept c : conceptList_)
				{
					if (cancelRequested_)
					{
						return new OperationResult(FindAndReplace.this.getTitle(), cancelRequested_);
					}

					String newTxt = null;
					Set<String> successMatches = new HashSet<>();

					updateProgress(i, conceptList_.size());
					updateMessage("Processing " + c.getDescription());
					

					// For each concept, filter the descriptions to be changed based on user selected DescType
					ConceptVersionBI con = WBUtility.getConceptVersion(c.getNid());
					Set<DescriptionVersionBI<?>> descsToChange= getDescsToChange(con);
					
					for (DescriptionVersionBI<?> desc : descsToChange) {
						// First see if text is found in desc before moving onward
						if (frc_.isRegExp() && hasRegExpMatch(desc) ||
							!frc_.isRegExp() && hasGenericMatch(desc)) { 

							// Now check if language is selected, that it exists in language refset
							if (frc_.getLanguageRefset().getNid() == 0 ||
								!desc.getAnnotationsActive(WBUtility.getViewCoordinate(), frc_.getLanguageRefset().getNid()).isEmpty()) {
						
								// Replace Text
								if (frc_.isRegExp()) {
									newTxt = replaceRegExp();
								} else {
									newTxt = replaceGeneric(desc);
								}
								
								DescriptionType descType = getDescType(con, desc);
								successMatches.add("    --> '" + desc.getText() + "' changed to '" + newTxt + "' ..... of type: " + descType);
								updateDescription(desc, newTxt);
							}
						}
					}
					if (successMatches.size() > 0) {
						modifiedConcepts.add(c);
						successCons.put(c.getDescription() + " --- " + con.getPrimordialUuid().toString(), successMatches);
					}
					
					updateProgress(++i, conceptList_.size());
				}
				
				return new OperationResult(FindAndReplace.this.getTitle(), modifiedConcepts, getMsgBuffer());
			}

			private boolean hasRegExpMatch(DescriptionVersionBI<?> desc) {
				Pattern pattern = Pattern.compile(frc_.getSearchText());
		        
				matcher = pattern.matcher(desc.getText());
		        
				if (matcher.find()) {
		        	return true;
		        } else {
		        	return false;
		        }
			}
			
			private String replaceRegExp() {
		        // Replace all occurrences of pattern in input
		        return matcher.replaceAll(frc_.getReplaceText());				
			}

			private String replaceGeneric(DescriptionVersionBI<?> desc) {
				String txt = desc.getText();
				
				if (frc_.isCaseSens()) {
					while (txt.contains(frc_.getSearchText())) {
						txt = txt.replace(frc_.getSearchText(), frc_.getReplaceText());
					}
				} else {
					int startIdx = StringUtils.indexOfIgnoreCase(txt, frc_.getSearchText());
					int endIdx = startIdx + frc_.getSearchText().length();
					
					while (startIdx >= 0) {
				        StringBuffer buf = new StringBuffer(txt);
				        buf.replace(startIdx, endIdx, frc_.getReplaceText());
				        txt = buf.toString();
				        
						startIdx = StringUtils.indexOfIgnoreCase(txt, frc_.getSearchText());
					}
				}
				
				return txt;
			}
			
			private boolean hasGenericMatch(DescriptionVersionBI<?> desc) {
				String txt = desc.getText();
				
				if (frc_.isCaseSens()) {
					if (!txt.contains(frc_.getSearchText())) {
						return false;
					}
				} else {
					if (!StringUtils.containsIgnoreCase(txt, frc_.getSearchText())) {
						return false;
					}
				}
				
				return true;
			}

			private void updateDescription(DescriptionVersionBI<?> desc, String newTxt) throws IOException, InvalidCAB, ContradictionException {
				DescriptionCAB dcab = desc.makeBlueprint(WBUtility.getViewCoordinate(), IdDirective.PRESERVE, RefexDirective.INCLUDE);
				dcab.setText(newTxt);
				DescriptionChronicleBI dcbi = WBUtility.getBuilder().constructIfNotCurrent(dcab);
				WBUtility.addUncommitted(dcbi.getEnclosingConcept());
			}

			private Set<DescriptionVersionBI<?>> getDescsToChange(ConceptVersionBI con) {
				Set<DescriptionVersionBI<?>> descsToChange = new HashSet<>();
				
				try {
					for (DescriptionVersionBI<?> desc : con.getDescriptionsActive()) {

						if (frc_.getSelectedDescTypes().contains(DescriptionType.FSN) &&
							con.getFullySpecifiedDescription().getNid() == desc.getNid()) {
							descsToChange.add(desc);
						} else if (frc_.getSelectedDescTypes().contains(DescriptionType.PT) &&
								   con.getPreferredDescription().getNid() == desc.getNid()) {
							descsToChange.add(desc);
						} else if (frc_.getSelectedDescTypes().contains(DescriptionType.SYNONYM) &&
								   con.getFullySpecifiedDescription().getNid() != desc.getNid() &&
								   con.getPreferredDescription().getNid() != desc.getNid()) {
							descsToChange.add(desc);
						}
					}
				} catch (IOException | ContradictionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				return descsToChange;
			}

			private DescriptionType getDescType(ConceptVersionBI con, DescriptionVersionBI<?> desc) throws IOException, ContradictionException {
				// TODO Auto-generated method stub
				if (con.getFullySpecifiedDescription().getNid() == desc.getNid()) {
					return DescriptionType.FSN;
				} else if ( con.getPreferredDescription().getNid() == desc.getNid()) {
					return DescriptionType.PT;
				} else {
					return DescriptionType.SYNONYM;
				}
			}
		};
	}

	protected String getMsgBuffer() {
		StringBuffer buf = new StringBuffer();
		
		if (successCons.keySet().size() == 1) {
			buf.append(this.getTitle() + " completed with changes to " + successCons.keySet().size() + " concept.");
		} else {
			buf.append(this.getTitle() + " completed with changes to " + successCons.keySet().size() + " concepts.");
		}
		
		for (String conStr : successCons.keySet()) {
			buf.append("\n\r\n\rConcept: " + conStr);
			
			for (String replace: successCons.get(conStr)) {
				buf.append("\n\r" + replace);	
			}
		}
		
		return buf.toString();
	}
}
