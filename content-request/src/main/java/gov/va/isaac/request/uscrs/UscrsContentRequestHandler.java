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

package gov.va.isaac.request.uscrs;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerHelper;
import gov.va.isaac.interfaces.gui.constants.SharedServiceNames;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.ExportTaskHandlerI;
import gov.va.isaac.request.ContentRequestHandler;
import gov.va.isaac.request.uscrs.USCRSBatchTemplate.COLUMN;
import gov.va.isaac.request.uscrs.USCRSBatchTemplate.PICKLIST_Case_Significance;
import gov.va.isaac.request.uscrs.USCRSBatchTemplate.PICKLIST_Refinability;
import gov.va.isaac.request.uscrs.USCRSBatchTemplate.PICKLIST_Relationship_Type;
import gov.va.isaac.request.uscrs.USCRSBatchTemplate.PICKLIST_Semantic_Tag;
import gov.va.isaac.request.uscrs.USCRSBatchTemplate.PICKLIST_Source_Terminology;
import gov.va.isaac.request.uscrs.USCRSBatchTemplate.SHEET;
import gov.va.isaac.util.OTFUtility;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import javafx.concurrent.Task;

import javax.inject.Named;
import javax.management.RuntimeErrorException;

import org.glassfish.hk2.api.PerLookup;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.Position;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.relationship.RelAssertionType;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.ihtsdo.otf.tcc.datastore.Bdb;
import org.ihtsdo.otf.tcc.datastore.stamp.StampBdb;
import org.jfree.util.Log;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * USCRS implementation of a {@link ContentRequestHandler}.
 *
 * @author bcarlsenca
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 * @author <a href="mailto:vkaloidis@apelon.com">Vas Kaloidis</a>
 */
@Service
@Named(value = SharedServiceNames.USCRS)
@PerLookup
public class UscrsContentRequestHandler implements ExportTaskHandlerI
{
	Properties prop = new Properties();
	boolean filter = false;
	Date date;
	Date setDate;
	
	private UscrsContentRequestHandler() {
		//hk2
	}
	
	//TODO: Document these options in JavaDoc VERY well because ppl just look at javadocs
	//TODO: Specify all instructions here, because this is how to config everything
	//TODO: Throw an exception if an option was passed in that is unknown. Same with a 
	//		required option, if not passed in, then throw an error again.
	@Override
	public void setOptions(Properties options) {
		prop = options;
		prop.getProperty("date", "none"); //This is a long
		prop.getProperty("mode", "none"); //IE: ListView, BatchExport
		//IE: If an author property is set, use an author filter. Everytying can be optional
		//IE: Path
		// TODO: If nothing is set then just go with default, export everything
		
		if(!prop.get("date").equals("none")) { 
			filter = true; 
			setDate = new Date(Integer.parseInt(prop.getProperty("date"))); 
			}
//		if(!prop.get("mode").equals("none")) { filter = true; }
		
	}

	@Override
	public String getTitle() {
		return "USCRS Content Request Handler";
	}

	@Override
	public String getDescription() {
		return "Exports a USCRS Content Request Excel file";
	}
	
	/** The request id counter. */
	private static AtomicInteger globalRequestCounter = new AtomicInteger(1);

	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(UscrsContentRequestHandler.class);
	
	private int currentRequestId;
	private LinkedHashMap<UUID, Integer> currentRequestUuidMap = new LinkedHashMap<UUID, Integer>();
	private USCRSBatchTemplate bt = null;
	private int count = 0;
	
	private IntStream conceptStream;
	
	public static void main(String[] args) {
		UscrsContentRequestHandler ucrh = new UscrsContentRequestHandler();
		
		IntStream is = IntStream.of(-2147374144, 2143494493, -2147483620, -2147418042);
		Properties p = new Properties();
		p.setProperty("date", String.valueOf(System.currentTimeMillis()));
		
		ucrh.createTask(is, new File("C:\\Users\\vkaloidis\\Desktop\\").toPath());
		
	}
	
	//TODO: Document EVERYTHING
	//TODO: Check everything passed in, and 
	@Override
	public Task<Integer> createTask(IntStream intStream, Path file) 
	{
		return new Task<Integer>() {
			
			@Override
			protected Integer call() throws Exception {
				updateTitle("Beginning Uscrs Content Request Export Operation");
				
				conceptStream = intStream;
				currentRequestId = globalRequestCounter.getAndIncrement();
				
				try {
					bt = new USCRSBatchTemplate(USCRSBatchTemplate.class.getResourceAsStream("/USCRS_Batch_Template-2015-01-27.xls"));
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				conceptStream
					.forEach( nid -> {
						if(prop.equals("none")) {
							//Modify this based on what options are passed in above
							if(count % 50 == 0) {
								updateTitle("Uscrs Content Request Exported " + count + " components");
							}
						}
						
						
						if(isCancelled()) {
							LOG.info("User canceled Uscrs Export Operation");
							throw new RuntimeException("User canceled operation");
						}
						
						ConceptChronicleBI concept = OTFUtility.getConceptVersion(nid); 
						
						ArrayList<RelationshipVersionBI> exportRels = null;
						try {
							exportRels.addAll(handleNewConcept(concept, bt));
							
						} catch (Exception e1) {
							LOG.error("Could not export concept " + nid);
							e1.printStackTrace();
						} 
						
						//Check if concept fits this criteria
						//First check if the concept is valid
						// If the date was passed in, we check if this is on that date
						// Look at all the components of the concept, and all the versions of 
						//		the concept. If any versions of the concept are newer than the date, t
						//		then we export. We also go throug the descriptions to see if the desc
						//		is newer than the date then we export that.
						//	Also look at the relationships of the concept to see if any newer than the 
						//		date passed in. If yes then export.
						// If a description has been edited, then edited again, we are not sure if we
						// 		put both in the export  or if we just want to export the latest version.
						
						
						
						if(filter) {
							try {
								Collection<? extends DescriptionChronicleBI> descriptions = concept.getDescriptions();
								for(DescriptionChronicleBI d : descriptions) {
									if(!prop.getProperty("date").equals("none")) {
										StampBdb stampDb = Bdb.getStampDb();
										
										DescriptionVersionBI<?> dv = d.getVersion(OTFUtility.getViewCoordinate());
										
										ViewCoordinate vc = new ViewCoordinate();
										
										
										
										Integer stamp = dv.getStamp();
										Position stampPosition = stampDb.getPosition(stamp);
										date = new Date(stampPosition.getTime());
										
										if(date.before(setDate) && !date.equals(setDate)) {
											
										}
										
									
									//TODO: After this, check if the desc.getVersion(OTF.GetVC(release date VC)), check to see if they differ
										//	to see if you have any changes between the two. If not then no problem. If so then ask Dan..?
									}
								
								}
								
							} catch (Exception e) {
								LOG.error("Error retreiving the descriptions: " + e.getMessage());
								e.printStackTrace();
							}
								
							try {
								Collection<? extends RelationshipChronicleBI> relsIncoming = concept.getRelationshipsIncoming();
							} catch (Exception e) {
								LOG.error("Error retreiving the incoming relationshios: " + e.getMessage());
								e.printStackTrace();
							}
							try {
								Collection<? extends RelationshipChronicleBI> relsOutgoing = concept.getRelationshipsOutgoing();
							} catch (Exception e) {
								LOG.error("Error retreiving the incoming relationshios: " + e.getMessage());
								e.printStackTrace();
							}
						}
						

						
						try {
							handleNewParent(exportRels, bt);
							handleNewRels(exportRels, bt);
							//TODO: THURSDAY TODO: **MAKE THIS DESC TAKE AN ARRAY LIST AND ADD TO IT. CHANGE THESE***
							for (DescriptionChronicleBI desc : concept.getDescriptions())
							{
								DescriptionVersionBI<?> thisDesc = desc.getVersion(OTFUtility.getViewCoordinate());
								handleNewSyn(thisDesc, bt); 
							}
						} catch(Exception e) {
							LOG.error(e.getMessage());
							throw new RuntimeException("Export failed on component nid " + nid);//isnt that bad?
						}
						count++;
				});
				
				if(isCancelled()) {
					return count;
				}
				
				//TODO: Update the return messages and the progress. Do it update every ~20 concepts
				
				//info.setName(OTFUtility.getConPrefTerm(concept.getNid()));
				
				LOG.info("  file = " + file);
				if (file != null)
				{
					bt.saveFile(file.toFile());
//					return new OperationResult(" " + file.getPath(), new HashSet<SimpleDisplayConcept>(), "The concepts were succesfully exported");
				}
				else
				{ 
					LOG.error("File object is null, could not proceed");
					throw new RuntimeException("The Operation could not be completed because the file is null");
				}
				return count;
			}
		};
	}
	

	/**
	 * Handle new concept spreadsheet tab.
	 *
	 * @param concept the concept
	 * @param bt the wb
	 * @return extra relationships (if more than 3)
	 * @throws Exception the exception
	 */
	private ArrayList<RelationshipVersionBI> handleNewConcept(ConceptChronicleBI concept, USCRSBatchTemplate bt) throws Exception
	{
		ArrayList<RelationshipVersionBI> extraRels = new ArrayList<RelationshipVersionBI>();
		// PARENTS
		LinkedList<Integer> parentsSct = new LinkedList<Integer>();
		LinkedList<Integer> parentsPathNid = new LinkedList<Integer>();
		LinkedList<String> definitions = new LinkedList<String>();
		
		int count = 0;
		for (RelationshipChronicleBI rel : concept.getRelationshipsOutgoing())
		{
			ViewCoordinate vc;
			vc = OTFUtility.getViewCoordinate();
			vc.setRelationshipAssertionType(RelAssertionType.STATED);
			//RelationshipVersionBI<?> relVersion = rel.getVersion(vc); //TODO: This was leading to possible issues. Needs more testing..
			
			RelationshipVersionBI<?> relVersion = rel.getVersion(OTFUtility.getViewCoordinate());
			
			if(relVersion != null) {
				if(relVersion.isActive()) 
				{
					if ((relVersion.getTypeNid() == Snomed.IS_A.getLenient().getNid()))
					{
						int relDestNid = relVersion.getDestinationNid();
						int parentSctId = this.getSct(relDestNid);
						
						parentsSct.add(count, parentSctId);
						int pathNid = OTFUtility.getConceptVersion(relDestNid).getPathNid();
						parentsPathNid.add(count, pathNid);
						if(count > 2 && relVersion != null) {
							extraRels.add(relVersion);
						}
						count++;
					} else {
						extraRels.add(relVersion);
					}
				}
			}
			
		}

		//Synonyms
		List<String> synonyms = new ArrayList<>();
		for (DescriptionChronicleBI desc : concept.getDescriptions())
		{
			DescriptionVersionBI<?> descVersion = desc.getVersion(OTFUtility.getViewCoordinate());
			// Synonyms: find active, non FSN descriptions not matching the preferred name
			if (descVersion.isActive() && (descVersion.getTypeNid() != Snomed.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient().getNid())
					&& !descVersion.getText().equals(OTFUtility.getConPrefTerm(concept.getNid())))
			{
				synonyms.add(descVersion.getText());
			}
			//Definition
			if(descVersion.getTypeNid() == Snomed.DEFINITION_DESCRIPTION_TYPE.getLenient().getNid()
					&& descVersion.isActive()){
				definitions.add(descVersion.getText());
			}
		}

		bt.selectSheet(SHEET.New_Concept);
		bt.addRow();
		for (COLUMN column : bt.getColumnsOfSheet(SHEET.New_Concept))
		{
			switch (column)
			{
				case Request_Id:
					bt.addNumericCell(column, currentRequestId);
					break;
				case Topic:
					bt.addStringCell(column, ""); //User Input
					break;
				case Local_Code:
					bt.addStringCell(column, concept.getPrimordialUuid().toString());
					break;
				case Local_Term: 
					bt.addStringCell(column, OTFUtility.getConPrefTerm(concept.getNid()));
					break;
				case Fully_Specified_Name:
					bt.addStringCell(column, this.getFsnWithoutSemTag(concept));
					break;
				case Semantic_Tag:
					bt.addStringCell(column, this.getSemanticTag(concept));
					break;
				case Preferred_Term:
					bt.addStringCell(column, OTFUtility.getConPrefTerm(concept.getNid()));
					break;
				case Terminology_1_:
				case Terminology_2_:
				case Terminology_3_:
					if (parentsPathNid.size() >= 1)
					{
						bt.addStringCell(column, this.getTerminology(parentsPathNid.remove(0)));
					}
					else
					{
						bt.addStringCell(column, "");
					}
					break;
				case Parent_Concept_Id_1_:
				case Parent_Concept_Id_2_:
				case Parent_Concept__Id_3_:
					if(parentsSct.size() >= 1) 
					{
						bt.addNumericCell(column, parentsSct.remove(0));
						
					} else 
					{
						bt.addStringCell(column, "");
					}
					break;
				case UMLS_CUI:
					bt.addStringCell(column, ""); //Not in API
					break;
				case Definition:
					if(definitions.size() > 0) 
					{
						bt.addStringCell(column, definitions.remove(0));
					} else {
						bt.addStringCell(column, "");
					}
					
					break;
				case Proposed_Use:
					bt.addStringCell(column, ""); //User Input
					break;
				case Justification:
					//Probably not correct because justification needs to be specific to that row
					bt.addStringCell(column, "Developed as part of extension namespace " + ExtendedAppContext.getCurrentlyLoggedInUserProfile().getExtensionNamespace());
					break;
				case Note:
					StringBuilder sb = new StringBuilder();
					
					//sb.append("SCT ID:" + this.getSct(-2143244556));
					
					
					if (concept.getConceptAttributes().getVersion(OTFUtility.getViewCoordinate()).isDefined())
					{
						sb.append("NOTE: this concept is fully defined. ");
					}
					
					boolean firstDef = false;
					
					//Extra Definitions
					if(definitions.size() > 0) {
						sb.append("Note: This concept has multiple definitions: ");
					}
					boolean firstSyn = false;
					while(definitions.size() > 0) 
					{
						if(firstDef) 
						{
							sb.append(", ");
						}
					}
					
					sb.append("Relationship Count: " + parentsSct.size());
					
					//Extra Synonyms
					if(synonyms.size() > 2) 
					{
						sb.append("NOTE: this concept also has the following synonyms: ");
					}
					while (synonyms.size() > 2)
					{
						if (firstSyn)
						{
							sb.append(", ");
						}
						sb.append(synonyms.remove(0));
						firstSyn = true;
					}
					bt.addStringCell(column, sb.toString());
					break;
				case Synonym:
					bt.addStringCell(column, (synonyms.size() > 0 ? synonyms.remove(0) : ""));
					break;
				default :
					throw new RuntimeException("Unexpected column type found in Sheet: " + column + " - " + SHEET.New_Concept);
			}
		}
		return extraRels;
	}
	

	/**
	 * Handle new Synonyms spreadsheet tab
	 *
	 * @param concept the concept
	 * @param bt the wb
	 * @throws Exception the exception
	 */
	private void handleNewSyn(DescriptionVersionBI<?> descVersion, USCRSBatchTemplate bt) throws Exception
	{	
		bt.selectSheet(SHEET.New_Synonym);
		bt.addRow();

		ConceptChronicleBI concept = OTFUtility.getConceptVersion(descVersion.getConceptNid());
		
		for (COLUMN column : bt.getColumnsOfSheet(SHEET.New_Synonym)) {
			switch(column)
			{
			case Topic:
				bt.addStringCell(column, ""); //User Input
				break;
			case Terminology:
				bt.addStringCell(column, this.getTerminology(descVersion.getPathNid()));
				break;
			case Concept_Id:
				bt.addNumericCell(column, this.getSct(concept.getNid()));
				break;
			case Term:
				bt.addStringCell(column, descVersion.getText());
				break;
			case Case_Significance:
				bt.addStringCell(column, this.getCaseSig(descVersion.isInitialCaseSignificant()));
				break;
			case Justification:
				bt.addStringCell(column, ""); //User Input
				break;
			case Note: 
				bt.addStringCell(column, "Description UUID: " + descVersion.getUUIDs().get(0).toString()); 
				break;
			default :
				throw new RuntimeException("Unexpected column type found in Sheet: " + column + " - " + SHEET.New_Synonym);
			}
		}
	}
	
	/**
	 * Handle Change parent spreadsheet tab
	 *
	 * @param concept the concept
	 * @param bt the wb
	 * @throws Exception the exception
	 */
	@SuppressWarnings({ })
	private void handleChangeParent(ConceptChronicleBI concept, RelationshipVersionBI<?> relVersion, USCRSBatchTemplate bt) throws Exception
	{	
		bt.selectSheet(SHEET.Change_Parent);
		bt.addRow();
		
		ConceptVersionBI targetConcept = OTFUtility.getConceptVersion(relVersion.getConceptNid());
		ConceptVersionBI thisConcept = OTFUtility.getConceptVersion(concept.getConceptNid());
		
		for (COLUMN column : bt.getColumnsOfSheet(SHEET.Change_Parent)) {
			switch(column)
			{
				case Topic:
					bt.addStringCell(column, ""); //User Input
					break;
				case Source_Terminology:
					bt.addStringCell(column,this.getTerminology(thisConcept.getPathNid()));
					break;
				case Concept_Id:
					bt.addNumericCell(column, this.getSct(concept.getNid()));
					break;
				case New_Parent_Concept_Id:
					bt.addNumericCell(column, this.getSct(targetConcept.getNid()));
					break;
				case New_Parent_Terminology:
					bt.addStringCell(column, this.getTerminology(targetConcept.getPathNid()));
					break;
				case Justification:
					bt.addStringCell(column, ""); //User Input
					break;
				case Note:
					bt.addStringCell(column, "");
					break;
				default :
					throw new RuntimeException("Unexpected column type found in Sheet: " + column + " - " + SHEET.Change_Parent);
			}
		}
	}
	
	private String getSemanticTag(ConceptChronicleBI concept) throws Exception {
		String fsn = OTFUtility.getFullySpecifiedName(concept);
		if (fsn.indexOf('(') != -1)
		{
			String st = fsn.substring(fsn.lastIndexOf('(') + 1, fsn.lastIndexOf(')'));
			return PICKLIST_Semantic_Tag.find(st).toString();
		} else {
			return null;
		}
	}
	
	private String getFsnWithoutSemTag(ConceptChronicleBI concept) throws Exception {
		String fsn = null;
		fsn = OTFUtility.getFullySpecifiedName(concept);
		
		String fsnOnly;
		if(fsn == null) {
			throw new Exception("FSN Could not be retreived");
		} else {
		
			fsnOnly = fsn;
			if (fsn.indexOf('(') != -1)
			{
				fsnOnly = fsn.substring(0, fsn.lastIndexOf('(') - 1);
			}
		}
		return fsnOnly;
	}
	
	private String getRelType(int nid) {
		return PICKLIST_Relationship_Type.find(OTFUtility.getConPrefTerm(nid)).toString();
	}
	
	private String getCharType(int nid) {
		String characteristic = OTFUtility.getConPrefTerm(nid); 
		//TODO: vk - Discuss the API / Spreadsheet mixup with the characteristic type
		// For now we are just going to return the preffered description retreived from
		// the characteristic type Nid until we can discuss with NLM or Jackie how to 
		// handle this problem. Inferred and stated relationships are not in the ENUM
//		return PICKLIST_Characteristic_Type.find(characteristic).toString(); // We will use this once we find a solution
		return characteristic; //But this works temporarily
	}
	
	private String getRefinability(int nid) {
		
		String desc = OTFUtility.getConPrefTerm(nid);
		String descToPicklist = desc;
		
		//Map the words optional and mandatory to their equal ENUMS b/c of API limitations
		if(desc.equals("Optional refinability")) {
			descToPicklist = "Optional";
		} else if(desc.equals("Mandatory refinability")) {
			descToPicklist = "Mandatory";
		} else {
			descToPicklist = desc;
		}
			
		return PICKLIST_Refinability.find(descToPicklist).toString();
	}
	
	private String getCaseSig(boolean caseSig) {
		if(caseSig) {
			return PICKLIST_Case_Significance.Entire_term_case_sensitive.toString();
		} else {
			return PICKLIST_Case_Significance.Entire_term_case_insensitive.toString();
		}
	}

	/**
	 * Handle new rels spreadsheet tab
	 *
	 * @param concept the concept
	 * @param bt the wb
	 * @throws Exception the exception
	 */
	@SuppressWarnings("rawtypes")
	public void handleNewRels(ArrayList<RelationshipVersionBI> extraRels, USCRSBatchTemplate bt) throws Exception {
		bt.selectSheet(SHEET.New_Relationship);
		for(RelationshipVersionBI rel : extraRels) {
			if (rel.isActive() && (rel.getTypeNid() != Snomed.IS_A.getLenient().getNid())) 
			{
				int destNid = rel.getDestinationNid();
				ConceptVersionBI destConcept = OTFUtility.getConceptVersion(destNid);
				ConceptVersionBI concept = OTFUtility.getConceptVersion(rel.getConceptNid());
				bt.addRow();
				for (COLUMN column : bt.getColumnsOfSheet(SHEET.New_Relationship)) {
					switch (column) {
						case Topic:
							bt.addStringCell(column, ""); //User Input
							break;
						case Source_Terminology:
							bt.addStringCell(column, this.getTerminology(concept.getPathNid()));
							break;
						case Source_Concept_Id:
							bt.addNumericCell(column, this.getSct(concept.getNid()));
							break;
						case Relationship_Type:
							bt.addStringCell(column, this.getRelType(rel.getTypeNid()));
							break;
						case Destination_Terminology:
							bt.addStringCell(column, this.getTerminology(destConcept.getPathNid()));
							break;
						case Destination_Concept_Id:
							bt.addNumericCell(column, this.getSct(destNid));
							break;
						case Characteristic_Type:
							bt.addStringCell(column, this.getCharType(rel.getCharacteristicNid()));
							break;
						case Refinability:
							bt.addStringCell(column, this.getRefinability(rel.getRefinabilityNid()));
							break;
						case Relationship_Group:
							bt.addNumericCell(column, rel.getGroup());
							break;
						case Justification:
							bt.addStringCell(column, "Developed as part of extension namespace " + ExtendedAppContext.getCurrentlyLoggedInUserProfile().getExtensionNamespace());
							break;
						case Note:
							bt.addStringCell(column, "This is a defining relationship expressed for the corresponding new concept request in the other tab");
							break;
						default :
							throw new RuntimeException("Unexpected column type found in Sheet: " + column + " - " + SHEET.New_Relationship);
					}
				}
			}
		}
	}
	
	private String getTerminology(int pathNid) throws Exception {
		ConceptVersionBI path = OTFUtility.getConceptVersion(pathNid);
		if(path != null) {
			UUID pathUUID = path.getPrimordialUuid();
			UUID snomedCtInternational = Snomed.CORE_MODULE.getUuids()[0];
			if(pathUUID.equals(snomedCtInternational) 
					|| pathNid == TermAux.SNOMED_CORE.getLenient().getNid()
					|| OTFUtility.getConceptVersion(pathNid).getPrimordialUuid().toString()
							.equals(AppContext.getAppConfiguration().getDefaultEditPathUuid())
					) {
				return PICKLIST_Source_Terminology.SNOMED_CT_International.toString();
			} else {
				LOG.error("Terminology Lib Error - NOT Snomed CT Core");
				return "Not SNOMED CT Core";
				//throw new RuntimeException("TERMINOLOGY LIB ERROR - NOT SNOMED CT CORE");
			}
		} else {
			throw new RuntimeException("Could not create a concept version from PATH NID: " + pathNid);
		}
	}
	
	
	private int getSct(int nid) {
		
		int descIdAttempt = 0, relIdAttempt = 0;
		try {
			descIdAttempt = Integer.parseInt(ConceptViewerHelper.getSctId(OTFUtility.getComponentChronicle(nid).getVersion(OTFUtility.getViewCoordinate())));
		} catch(Exception e) {
			//Eat it
			//TODO - no - you shouldn't be taking an error here.  the poorly written ConceptViewerHelper is going to return the String "Unreleased" if it 
			//doesn't find one - we should fix the API to be proper.  It should return an int and throw a checked exception if none is available, or perhaps, 
			//return an Integer, and return null if none is available.
		}
		
		try {
			relIdAttempt = Integer.parseInt(ConceptViewerHelper.getSctId(OTFUtility.getComponentVersion(nid)));
		} catch(Exception e) {
			//Eat it
		}
		
		try 
		{
			//TODO get rid of these sysouts - put in log statements if you want debug output
			if(OTFUtility.getComponentChronicle(nid).getVersion(OTFUtility.getViewCoordinate()) != null && descIdAttempt != 0) { //Description
				System.out.println("Fetching SCT of a Description");
				return Integer.parseInt(ConceptViewerHelper.getSctId(OTFUtility.getComponentChronicle(nid).getVersion(OTFUtility.getViewCoordinate())));
				
			} else if(OTFUtility.getComponentVersion(nid) != null && relIdAttempt != 0) { //Relationship
				System.out.println("Fetching SCT of a Relationship");
				return Integer.parseInt(ConceptViewerHelper.getSctId(OTFUtility.getComponentVersion(nid)));
			} else { 
				System.out.println("Fetching SCT of a Concept (Probably)");
				if(Integer.parseInt(ConceptViewerHelper.getSctId(OTFUtility.getConceptVersion(nid))) != 0) { //Concept
					return Integer.parseInt(ConceptViewerHelper.getSctId(OTFUtility.getConceptVersion(nid)));
				} else {
					return currentRequestUuidMap.get(OTFUtility.getConceptVersion(nid).getPrimordialUuid());
				}
			}
			
			
		} catch(Exception e) 
		{ 
			Log.error("We could not get the SCT from the Given NID");
			e.printStackTrace();  //TODO nope
			return 0;  //document the failure behavior - is 0 an appropriate thing to return if no sctid could be found?
		}
		
	}
	
	/**
	 * Handle Change Relationships spreadsheet tab
	 *
	 * @param concept the concept
	 * @param bt the wb
	 * @throws Exception the exception
	 */
	private void handleChangeRels(ConceptChronicleBI concept, RelationshipVersionBI<?> relVersion, USCRSBatchTemplate bt) throws Exception
	{
		bt.selectSheet(SHEET.Change_Relationship);
		ConceptVersionBI destConcept = OTFUtility.getConceptVersion(relVersion.getDestinationNid());
		ConceptVersionBI sourceConcept = OTFUtility.getConceptVersion(concept.getConceptNid());
		bt.addRow();
		for (COLUMN column : bt.getColumnsOfSheet(SHEET.Change_Relationship))
		{
			switch (column)
			{
				case Topic:
					bt.addStringCell(column, "");
					break;
				case Source_Concept_Id:
					bt.addNumericCell(column, this.getSct(sourceConcept.getNid()));
					break;
				case Relationship_Id:  
					bt.addNumericCell(column, this.getSct(relVersion.getNid()));
					break;
				case Relationship_Type: 
					bt.addStringCell(column, this.getRelType(relVersion.getTypeNid()));
					break;
				case Source_Terminology:
					bt.addStringCell(column, this.getTerminology(sourceConcept.getPathNid()));
					break;
				case Destination_Concept_Id:
					bt.addNumericCell(column, this.getSct(relVersion.getDestinationNid()));
					break;
				case Destination_Terminology:
					bt.addStringCell(column, this.getTerminology(destConcept.getPathNid()));
					break;
				case Characteristic_Type:
					bt.addStringCell(column, this.getCharType(relVersion.getCharacteristicNid()));
					break;
				case Refinability:
					bt.addStringCell(column, this.getRefinability(relVersion.getRefinabilityNid()));
					break;
				case Relationship_Group:
					bt.addNumericCell(column, relVersion.getGroup());
					break;
				case Justification:
					bt.addStringCell(column, ""); ///User Input
					break;
				case Note:
					bt.addStringCell(column, ""); //User Input
					break;
				default :
					throw new RuntimeException("Unexpected column type found in Sheet: " + column + " - " + SHEET.Change_Relationship);
			}

		}
	}
	
	/**
	 * Handle Change Description spreadsheet tab
	 *
	 * @param concept the concept
	 * @param bt the wb
	 * @throws Exception the exception
	 */
	private void handleChangeDesc(DescriptionVersionBI<?> descVersion, USCRSBatchTemplate bt) throws Exception
	{
		bt.selectSheet(SHEET.Change_Description);
		bt.addRow();
		for (COLUMN column : bt.getColumnsOfSheet(SHEET.Change_Description))
		{
			switch (column)
			{
				case Topic:
					bt.addStringCell(column, "");
					break;
				case Terminology:
					bt.addStringCell(column, this.getTerminology(descVersion.getPathNid()));
					break;
				case Concept_Id:
					bt.addNumericCell(column, this.getSct(descVersion.getConceptNid()));
					break;
				case Description_Id:
					bt.addNumericCell(column, this.getSct(descVersion.getNid()));
					break;
				case Term: 
					//TODO nope - can't use OTF Utility to get this - you need to get the text from the description version passed in
					bt.addStringCell(column, OTFUtility.getConPrefTerm(descVersion.getConceptNid()));
					break;
				case Case_Significance:
					bt.addStringCell(column, this.getCaseSig(descVersion.isInitialCaseSignificant()));
					break;
				case Justification:
					bt.addStringCell(column, "");
					break;
				case Note:
					bt.addStringCell(column, "");
					break;
				default :
					throw new RuntimeException("Unexpected column type found in Sheet: " + column + " - " + SHEET.Change_Description);
			}
		}

	}
	
	/**
	 * Handle Retire Concept spreadsheet tab
	 *
	 * @param concept the concept
	 * @param bt the wb
	 * @throws Exception the exception
	 */
	private void handleRetireConcept(ConceptChronicleBI concept, USCRSBatchTemplate bt) throws Exception
	{
		bt.selectSheet(SHEET.Retire_Concept);
		bt.addRow();
		ConceptVersionBI conceptVersion = OTFUtility.getConceptVersion(concept.getNid());
		for (COLUMN column : bt.getColumnsOfSheet(SHEET.Retire_Concept))
		{
			switch (column)
			{
				case Topic:
					break;
				case Terminology:
					bt.addStringCell(column, this.getTerminology(conceptVersion.getPathNid()));
					break;
				case Concept_Id:
					bt.addNumericCell(column, this.getSct(concept.getNid()));
					break;
				case Change_Concept_Status_To: 
					bt.addStringCell(column, "");
					break;
				case Duplicate_Concept_Id:  //TODO: vk - - possibly userinput - if they are deactivating because it is a dupe, we need the SCTID of the dupe here
					bt.addStringCell(column, "");
					break;
				case Justification:
					bt.addStringCell(column, "");
					break;
				case Note:
					bt.addStringCell(column, "");
					break;
				default :
					throw new RuntimeException("Unexpected column type found in Sheet: " + column + " - " + SHEET.Retire_Concept);
			}
		}
	}
	
	/**
	 * Handle Retire Description spreadsheet tab
	 *
	 * @param concept the concept
	 * @param bt the wb
	 * @throws Exception the exception
	 */
	private void handleRetireDescription(DescriptionVersionBI<?> descVersion, USCRSBatchTemplate bt) throws Exception
	{
		bt.selectSheet(SHEET.Retire_Description);
		bt.addRow();
		ConceptVersionBI conceptVersion = OTFUtility.getConceptVersion(descVersion.getConceptNid());
		for (COLUMN column : bt.getColumnsOfSheet(SHEET.Retire_Description))
		{
			switch (column)
			{
				case Topic:
					bt.addStringCell(column, ""); //User Input
					break;
				case Terminology:
					bt.addStringCell(column, this.getTerminology(conceptVersion.getPathNid()));
					break;
				case Concept_Id:
					bt.addNumericCell(column, this.getSct(conceptVersion.getNid()));
					break;
				case Description_Id:
					bt.addNumericCell(column, this.getSct(descVersion.getNid()));
					break;
				case Change_Description_Status_To:  //TODO talk to Jaqui / NLM - same status question as above
					break;
				case Justification:
					bt.addStringCell(column, ""); //User Input
					break;
				case Note:
					bt.addStringCell(column, ""); //User Input
					break;
				default :
					throw new RuntimeException("Unexpected column type found in Sheet: " + column + " - " + SHEET.Retire_Description);
			}
		}

	}

	/**
	 * Handle Retire Relationship spreadsheet tab
	 *
	 * @param concept the concept
	 * @param bt the wb
	 * @throws Exception the exception
	 */
	@SuppressWarnings({"rawtypes" })
	private void handleRetireRelationship(RelationshipVersionBI<?> relVersion, USCRSBatchTemplate bt) throws Exception
	{
		bt.selectSheet(SHEET.Retire_Relationship);
		bt.addRow();
		ConceptVersionBI destConcept = OTFUtility.getConceptVersion(relVersion.getDestinationNid());
		ConceptVersionBI sourceConcept = OTFUtility.getConceptVersion(relVersion.getConceptNid());
		for (COLUMN column : bt.getColumnsOfSheet(SHEET.Retire_Relationship))
		{
			switch (column)
			{
				case Topic:
					bt.addStringCell(column, ""); //User Input
					break;
				case Source_Terminology:
					bt.addStringCell(column, this.getTerminology(sourceConcept.getPathNid()));
					break;
				case Source_Concept_Id:
					bt.addNumericCell(column, this.getSct(sourceConcept.getNid()));
					break;
				case Relationship_Id:  
					bt.addNumericCell(column, this.getSct(relVersion.getNid()));
					break;
				case Destination_Terminology:
					bt.addStringCell(column, this.getTerminology(destConcept.getPathNid()));
					break;
				case Destination_Concept_Id:
					bt.addNumericCell(column, this.getSct(destConcept.getNid()));
					break;
				case Relationship_Type:
					bt.addStringCell(column, this.getRelType(relVersion.getTypeNid()));
					break;
				case Justification:
					bt.addStringCell(column, ""); //User Input
					break;
				case Note:
					bt.addStringCell(column, ""); //User Input
					break;
				default :
					throw new RuntimeException("Unexpected column type found in Sheet: " + column + " - " + SHEET.Retire_Relationship);
			}
		}

	}
	
	/**
	 * Handle Add Parent spreadsheet tab
	 *
	 * @param concept the concept
	 * @param bt the wb
	 * @throws Exception the exception
	 */
	private void handleNewParent(ArrayList<RelationshipVersionBI> extraRels, USCRSBatchTemplate bt) throws Exception
	{
		bt.selectSheet(SHEET.Add_Parent);
		for(RelationshipVersionBI rel : extraRels) {
			if (rel.isActive() && (rel.getTypeNid() == Snomed.IS_A.getLenient().getNid())) 
			{
				int destNid = rel.getDestinationNid();
				ConceptVersionBI destConcept = OTFUtility.getConceptVersion(destNid);
				ConceptVersionBI concept = OTFUtility.getConceptVersion(rel.getConceptNid());
				bt.addRow();
				for (COLUMN column : bt.getColumnsOfSheet(SHEET.Add_Parent))
				{
					switch (column)
					{
						case Topic:
							bt.addStringCell(column, "");
							break;
						case Source_Terminology: 
							bt.addStringCell(column, this.getTerminology(OTFUtility.getConceptVersion(concept.getConceptNid()).getPathNid()));
							break;
						case Child_Concept_Id:
							bt.addNumericCell(column, this.getSct(concept.getNid()));
							break;
						case Destination_Terminology:
							bt.addStringCell(column, this.getTerminology(OTFUtility.getConceptVersion(destConcept.getConceptNid()).getPathNid()));
							break;
						case Parent_Concept_Id:  
							bt.addNumericCell(column, this.getSct(destConcept.getConceptNid()));
							break;
						case Justification:
							bt.addStringCell(column, "");
							break;
						case Note:
							bt.addStringCell(column, "");
							break;
						default :
							throw new RuntimeException("Unexpected column type found in Sheet: " + column + " - " + SHEET.Add_Parent);
					}
				}
			}
		}
	}
	
	/**
	 * Handle Other spreadsheet tab
	 *
	 * @param concept the concept
	 * @param bt the wb
	 * @throws Exception the exception
	 */
	@SuppressWarnings("unused")
	private void handleOther(ConceptChronicleBI concept, USCRSBatchTemplate bt) throws Exception
	{
		bt.selectSheet(SHEET.Other);
		bt.addRow();
		for (COLUMN column : bt.getColumnsOfSheet(SHEET.Other))
		{
			switch (column)
			{
				case Topic:
					bt.addStringCell(column, ""); //User Input
					break;
				case Description:
					break;
				case Justification:
					bt.addStringCell(column, ""); //User Input
					break;
				case Note:
					bt.addStringCell(column, ""); //User Input
					break;
				default :
					throw new RuntimeException("Unexpected column type found in Sheet: " + column + " - " + SHEET.Other);
			}
		}
	}

}
