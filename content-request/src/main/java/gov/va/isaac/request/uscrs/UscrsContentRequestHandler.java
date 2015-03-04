package gov.va.isaac.request.uscrs;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerHelper;
import gov.va.isaac.interfaces.gui.constants.SharedServiceNames;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.ContentRequestHandlerI;
import gov.va.isaac.interfaces.utility.DialogResponse;
import gov.va.isaac.request.ContentRequestHandler;
import gov.va.isaac.request.ContentRequestTrackingInfo;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;
import javax.inject.Named;
import org.glassfish.hk2.api.PerLookup;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.jfree.util.Log;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * USCRS implementation of a {@link ContentRequestHandler}.
 *
 * @author bcarlsenca
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Named(value = SharedServiceNames.USCRS)
@PerLookup
public class UscrsContentRequestHandler implements ContentRequestHandler, ContentRequestHandlerI
{
	/** The request id counter. */
	private static AtomicInteger globalRequestCounter = new AtomicInteger(1);

	/** The nid. */
	private int nid;

	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(UscrsContentRequestHandler.class);
	
	private int currentRequestId;
	private LinkedHashMap<UUID, Integer> currentRequestUuidMap = new LinkedHashMap<UUID, Integer>();
	
	private ConceptChronicleBI concept;

	@Override
	public UscrsContentRequestTrackingInfo submitContentRequest(int nid) throws Exception
	{
		this.concept = OTFUtility.getConceptVersion(nid); //TODO Maybe get rid of this.concept
		ConceptChronicleBI concept = this.concept;
		USCRSBatchTemplate bt = new USCRSBatchTemplate(USCRSBatchTemplate.class.getResourceAsStream("/USCRS_Batch_Template-2015-01-27.xls"));

		//Map current Request to the concept.getPrimorialUUID 
		// as the key and the value is the currentRequestID that was just assigned to the new Concept
		currentRequestId = globalRequestCounter.getAndIncrement();
		UUID primorialUuid = concept.getPrimordialUuid();
		this.currentRequestUuidMap.put(primorialUuid, currentRequestId);
		
		//TODO start changing the API on these handle methods to be more what we need, eventually.
		//changeX need to take in the thing that is being changed - say - a rel or description, rather than a concept
		//changeParent need to take in the new parent concept
		//retire rel / retire desc should take in the thing being retired.
		
		//'hack' code for now, to arbitrarily pick something to be passed in, can remain here.
		
		handleNewConcept(concept, bt);
		handleNewRels(concept, bt);
		handleNewSyn(concept, bt);
		handleAddParent(concept, bt);
		
		handleChangeParent(concept, bt);
		handleChangeRels(concept, bt);
		handleChangeDesc(concept, bt);
		
		handleRetireConcept(concept, bt);
		handleRetireRelationship(concept, bt);
		handleRetireDescription(concept, bt);

		// TODO: Fix extension filter
		// TODO [Vas] what is broken?  Or is the to do above just invalid?
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save USCRS Concept Request File");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Excel Files", ".xls", ".xlsx"));
		fileChooser.setInitialFileName("USCRS_Export.xls");

		UscrsContentRequestTrackingInfo info = new UscrsContentRequestTrackingInfo();
		info.setName(OTFUtility.getConPrefTerm(concept.getNid()));

		File file = fileChooser.showSaveDialog(null);
		LOG.info("  file = " + file);
		if (file != null)
		{
			bt.saveFile(file);
			info.setIsSuccessful(true);
			info.setFile(file.toString());
			info.setDetail("Batch USCRS submission spreadsheet successfully created.");
		}
		else
		{ // User cancelled dialog
			info.setIsSuccessful(false);
			info.setDetail("Submission cancelled.");
		}
		return info;
	}

	/**
	 * Handle new concept spreadsheet tab.
	 *
	 * @param concept the concept
	 * @param bt the wb
	 * @throws Exception the exception
	 */
	private void handleNewConcept(ConceptChronicleBI concept, USCRSBatchTemplate bt) throws Exception
	{
		// PARENTS
		LinkedList<Integer> parentsSct = new LinkedList<Integer>();
		LinkedList<Integer> parentsPathNid = new LinkedList<Integer>();
		
		int count = 0;
		for (RelationshipChronicleBI rel : concept.getRelationshipsOutgoing())
		{
			RelationshipVersionBI<?> relVersion = rel.getVersion(OTFUtility.getViewCoordinate());
			if ((relVersion.getTypeNid() == Snomed.IS_A.getLenient().getNid()) && relVersion.isActive())
			{
				int relDestNid = relVersion.getDestinationNid();
				int parentSctId = this.getSct(relDestNid);
				
				parentsSct.add(count, parentSctId);
				int pathNid = OTFUtility.getConceptVersion(relDestNid).getPathNid();
				parentsPathNid.add(count, pathNid);
				count++;
			}
		}
		if (parentsSct.size() > 3)
		{
			throw new Exception("Cannot handle more than 3 parents");
		}

		//Synonyms
		List<String> synonyms = new ArrayList<>();
		String definition = null;
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
			//TODO what do we do with the other descriptions if there are more than 1?  Why aren't we checking for isActive?
			if(descVersion.getTypeNid() == Snomed.DEFINITION_DESCRIPTION_TYPE.getLenient().getNid()){
				definition = descVersion.getText();
			}
		}
		LOG.debug("      Synonym Count: {}", synonyms.size());

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
				case Local_Term:  //TODO find out what this is supposed to be - not sure why we would do the same thing as preferred term - Jaqui question
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
					//Change to LinkedHM or regular HashMap Key: Nid SctID: Value
					// Use a get for terminology and a remove for parent_concept
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
					//TODO Just leave blank - don't write stuff here
					bt.addStringCell(column, "Not Available in API");
					break;
				case Definition:
					//TODO definition was init'ed as null - need to make sure this is at least, an empty string, not 'null'
					bt.addStringCell(column, definition);
					break;
				case Proposed_Use:
					bt.addStringCell(column, ""); //User Input
					break;
				case Justification:
					//Probably not correct because justification needs to be specific to that row
					bt.addStringCell(column, "Developed as part of extension namespace " + AppContext.getAppConfiguration().getCurrentExtensionNamespace());
					break;
				case Note:
					StringBuilder sb = new StringBuilder();
					if (concept.getConceptAttributes().getVersion(OTFUtility.getViewCoordinate()).isDefined())
					{
						sb.append("NOTE: this concept is fully defined. ");
					}
					boolean firstHasBeenSeen = false;
					//TODO should also add extra definitions here, if there is more than one definition
					while (synonyms.size() > 2)
					{
						sb.append("NOTE: this concept also has the following synonyms: ");
						if (firstHasBeenSeen)
						{
							sb.append(", ");
						}
						sb.append(synonyms.remove(0));
						firstHasBeenSeen = true;
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
	}
	

	/**
	 * Handle new Synonyms spreadsheet tab
	 *
	 * @param concept the concept
	 * @param bt the wb
	 * @throws Exception the exception
	 */
	@SuppressWarnings({ })
    private void handleNewSyn(ConceptChronicleBI concept, USCRSBatchTemplate bt) throws Exception
	{	
		bt.selectSheet(SHEET.New_Synonym);
		bt.addRow();
		for (DescriptionChronicleBI desc : concept.getDescriptions())
		{
			DescriptionVersionBI<?> descVersion = desc.getVersion(OTFUtility.getViewCoordinate());
			
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
					//TODO need to handle the case where this concept does not have a SCTID - which would be the case if this is a new concept - 
					//in which case - you should be able to find a request ID in the currentRequestUuidMap 
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
				case Note:  //TODO until further clarified with NLM, we should probably put the UUID of the description here.
					bt.addStringCell(column, ""); //User Input
					break;
				default :
					throw new RuntimeException("Unexpected column type found in Sheet: " + column + " - " + SHEET.New_Concept);
				}
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
	private void handleChangeParent(ConceptChronicleBI concept, USCRSBatchTemplate bt) throws Exception
	{	
		bt.selectSheet(SHEET.Change_Parent);
		bt.addRow();
		for (DescriptionChronicleBI desc : concept.getDescriptions())
		{
			DescriptionVersionBI<?> descVersion = desc.getVersion(OTFUtility.getViewCoordinate());
			for (COLUMN column : bt.getColumnsOfSheet(SHEET.Change_Parent)) {
				switch(column)
				{
					case Topic:
						bt.addStringCell(column, ""); //User Input
						break;
					case Source_Terminology:
						bt.addStringCell(column,this.getTerminology(descVersion.getPathNid()));
						break;
					case Concept_Id:
						bt.addNumericCell(column, this.getSct(concept.getNid()));
						break;
					case New_Parent_Concept_Id:
						bt.addStringCell(column, ""); //User Input
						break;
					case New_Parent_Terminology:
						bt.addStringCell(column, ""); //User Input
						break;
						//TODO this column doesn't exist here
					case Case_Significance:
						bt.addStringCell(column, this.getCaseSig(descVersion.isInitialCaseSignificant()));
						break;
					case Justification:
						bt.addStringCell(column, ""); //User Input
						break;
					case Note:
						bt.addStringCell(column, "Notes");//TODO leave blank if no notes
						break;
					default :
						throw new RuntimeException("Unexpected column type found in Sheet: " + column + " - " + SHEET.New_Concept);
				}
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
		//TODO: Discuss the API / Spreadsheet mixup with the characteristic type
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
	private void handleNewRels(ConceptChronicleBI concept, USCRSBatchTemplate bt) throws Exception
	{
		bt.selectSheet(SHEET.New_Relationship);
		for (RelationshipChronicleBI rel : concept.getRelationshipsOutgoing())
		{
			RelationshipVersionBI<?> relVersion = rel.getVersion(OTFUtility.getViewCoordinate());
			if (relVersion.isActive() && (relVersion.getTypeNid() != Snomed.IS_A.getLenient().getNid()))
			{
				int destNid = relVersion.getDestinationNid();
				ConceptVersionBI destConcept = OTFUtility.getConceptVersion(destNid);
				
				bt.addRow();
				for (COLUMN column : bt.getColumnsOfSheet(SHEET.New_Relationship))
				{
					switch (column)
					{
						case Topic:
							bt.addStringCell(column, ""); //User Input
							break;
						case Source_Terminology:
							//TODO: Get source terminology
							// What goes in source depends on whether this a new concept or not
							//If the SctID exists - use it - in the future this will probably need to change to a lookup 
							// Create a hashmap that maps UUID's to the integer request ID. If there is no Sct ID then I access the HashMap created earlier
							// The hashmap created when a new concept is created is from the incremented current request ID to the UUID
							//You pass in the UUID to the hashmap created earlier to get the request ID
							
							//TODO - no, this should be the source terminology of the source concept - not of the relationship
							bt.addStringCell(column, this.getTerminology(relVersion.getPathNid()));
							break;
						case Source_Concept_Id:
							//TODO this needs to be the SCTID of the source concept, if it has one - otherwise - look up the correct request ID from the UUID / request ID hashmap
//							bt.addNumericCell(column, Double.parseDouble(ConceptViewerHelper.getSctId(OTFUtility.getConceptVersion(relVersion.getNid())).trim()));
							bt.addNumericCell(column, currentRequestId);
							break;
						case Relationship_Type:
							bt.addStringCell(column, this.getRelType(relVersion.getTypeNid()));
							break;
						case Destination_Terminology:
							bt.addStringCell(column, this.getTerminology(destConcept.getPathNid()));
							break;
						case Destination_Concept_Id:
							//TODO - if no SCTID, this needs to be looked up in the UUID to request ID map
							bt.addNumericCell(column, this.getSct(destNid));
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
							bt.addStringCell(column, "Developed as part of extension namespace " + AppContext.getAppConfiguration().getCurrentExtensionNamespace());
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
				throw new RuntimeException("TERMINOLOGY LIB ERROR - NOT SNOMED CT CORE");
			}
		} else {
			throw new RuntimeException("Could not create a concept version from PATH NID: " + pathNid);
		}
	}
	
	
	private int getSct(int nid) {
		
		int returnNid = 0;
		
		try {
			returnNid = Integer.parseInt(ConceptViewerHelper.getSctId(OTFUtility.getConceptVersion(nid)));
		} catch(Exception e) {  //TODO use the map to check new requests
			//TODO: Add error logging everywhere
			Log.error("We could not get the SCT from the Given NID");
		}
		
		return returnNid;
	}
	
	/**
	 * Handle Change Relationships spreadsheet tab
	 *
	 * @param concept the concept
	 * @param bt the wb
	 * @throws Exception the exception
	 */
	private void handleChangeRels(ConceptChronicleBI concept, USCRSBatchTemplate bt) throws Exception
	{
		bt.selectSheet(SHEET.Change_Relationship);
		for (RelationshipChronicleBI rel : concept.getRelationshipsOutgoing())
		{
			RelationshipVersionBI<?> relVersion = rel.getVersion(OTFUtility.getViewCoordinate());
			if (relVersion.isActive() && (relVersion.getTypeNid() != Snomed.IS_A.getLenient().getNid()))
			{
				ConceptVersionBI destConcept = OTFUtility.getConceptVersion(relVersion.getDestinationNid());
				bt.addRow();
				for (COLUMN column : bt.getColumnsOfSheet(SHEET.Change_Relationship))
				{
					switch (column)
					{
						case Topic:
							bt.addStringCell(column, "");
							break;
							//TODO all of the same issues as previous methods with ids for source and target concepts
						case Source_Concept_Id:
							bt.addNumericCell(column, this.getSct(relVersion.getConceptNid()));
							break;
						case Relationship_Id:  //TODO - no - never nid.  The rel should have an SCTID - nids _never_ leave the environment - they are meaningless
							bt.addNumericCell(column, rel.getNid());
							break;
						case Relationship_Type: 
							bt.addStringCell(column, this.getRelType(relVersion.getTypeNid()));
							break;
						case Source_Terminology:
							//TODO no, concept source con terminlogy, not rel terminlogy
							bt.addStringCell(column, this.getTerminology(relVersion.getPathNid()));
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
							throw new RuntimeException("Unexpected column type found in Sheet: " + column + " - " + SHEET.New_Concept);
					}
				}
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
	private void handleChangeDesc(ConceptChronicleBI concept, USCRSBatchTemplate bt) throws Exception
	{
		bt.selectSheet(SHEET.Change_Description);
		bt.addRow();
		for (DescriptionChronicleBI desc : concept.getDescriptions())
		{
			DescriptionVersionBI<?> descVersion = desc.getVersion(OTFUtility.getViewCoordinate());
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
					case Concept_Id:  //TODO - no, never nid - sctid or request id
						bt.addNumericCell(column, this.getSct(concept.getNid()));
						break;
					case Description_Id:   //TODO no, not UUID - either SCTID, or, we talk to NLM about what this is.
						bt.addStringCell(column, descVersion.getUUIDs().get(0).toString());
						break;
					case Term:  //TODO - no, this should be the description that is being modified - the handleChangeDesc method API should take in a description, not a concept
						bt.addStringCell(column, OTFUtility.getConPrefTerm(concept.getNid()));
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
						throw new RuntimeException("Unexpected column type found in Sheet: " + column + " - " + SHEET.New_Concept);
				}
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
				case Change_Concept_Status_To:  //TODO need to talk to Jaqui / NLM - this needs to be 'inactive' or some such - there used to be many different types of 'inactive', now there may only be one?
					bt.addStringCell(column, "");
					break;
				case Duplicate_Concept_Id:  //TODO - possibly userinput - if they are deactivating because it is a dupe, we need the SCTID of the dupe here
					bt.addStringCell(column, "");
					break;
				case Justification:
					bt.addStringCell(column, "");
					break;
				case Note:
					bt.addStringCell(column, "");
					break;
				default :
					throw new RuntimeException("Unexpected column type found in Sheet: " + column + " - " + SHEET.New_Concept);
					
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
	private void handleRetireDescription(ConceptChronicleBI concept, USCRSBatchTemplate bt) throws Exception
	{
		bt.selectSheet(SHEET.Retire_Description);
		for (DescriptionChronicleBI desc : concept.getDescriptions())
		{
			bt.addRow();
			DescriptionVersionBI<?> descVersion = desc.getVersion(OTFUtility.getViewCoordinate());
			ConceptVersionBI conceptVersion = OTFUtility.getConceptVersion(concept.getNid());
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
					case Concept_Id:  //TODO not nid
						bt.addNumericCell(column, this.getSct(concept.getNid()));
						break;
					case Description_Id:  //TODO not UUID
						bt.addStringCell(column, descVersion.getUUIDs().get(0).toString());
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
						throw new RuntimeException("Unexpected column type found in Sheet: " + column + " - " + SHEET.New_Concept);
				}
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
	private void handleRetireRelationship(ConceptChronicleBI concept, USCRSBatchTemplate bt) throws Exception
	{
		bt.selectSheet(SHEET.Retire_Relationship);
		for (RelationshipChronicleBI rel : concept.getRelationshipsOutgoing())
		{
			RelationshipVersionBI<?> relVersion = rel.getVersion(OTFUtility.getViewCoordinate());
			if (relVersion.isActive() && (relVersion.getTypeNid() != Snomed.IS_A.getLenient().getNid())) //NOT IS-A Rels
			{
				ConceptVersionBI destConcept = OTFUtility.getConceptVersion(relVersion.getDestinationNid());
				bt.addRow();
				for (COLUMN column : bt.getColumnsOfSheet(SHEET.Retire_Relationship))
				{
					switch (column)
					{
						case Topic:
							bt.addStringCell(column, ""); //User Input
							break;
						case Source_Terminology:
							//TODO source concept path, not rel
							bt.addStringCell(column, this.getTerminology(relVersion.getPathNid()));
							break;
						case Source_Concept_Id:  //TODO same SCTID issue
							bt.addNumericCell(column, this.getSct(concept.getNid()));
							break;
						case Relationship_Id:  //TODO no nid - either SCTID , or need to talk to NLM / Jaqui
							bt.addNumericCell(column, relVersion.getNid()); //No UUID or Sct available for this
//							bt.addStringCell(column, relVersion.getUUIDs().get(0).toString());
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
							throw new RuntimeException("Unexpected column type found in Sheet: " + column + " - " + SHEET.New_Concept);
					}
				}
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
	private void handleAddParent(ConceptChronicleBI concept, USCRSBatchTemplate bt) throws Exception
	{
		bt.selectSheet(SHEET.Add_Parent);
		for (RelationshipChronicleBI rel : concept.getRelationshipsOutgoing())
		{
			RelationshipVersionBI<?> relVersion = rel.getVersion(OTFUtility.getViewCoordinate());
			//TODO - no - this is parent - so we ARE doing IS_A - we wouldn't do this loop anyway - the new child concept needs to be a parameter to this method
			if (relVersion.isActive() && (relVersion.getTypeNid() != Snomed.IS_A.getLenient().getNid())) //NOT IS-A Rels
			{
				ConceptVersionBI destConcept = OTFUtility.getConceptVersion(relVersion.getDestinationNid());
				
				bt.addRow();
				for (COLUMN column : bt.getColumnsOfSheet(SHEET.Add_Parent))
				{
					switch (column)
					{
						case Topic:
							bt.addStringCell(column, "");
							break;
						case Source_Terminology:  //TODO not rel path
							bt.addStringCell(column, this.getTerminology(relVersion.getPathNid()));
							break;
						case Child_Concept_Id:
							bt.addNumericCell(column, this.getSct(concept.getNid()));
//							bt.addStringCell(column, relVersion.getUUIDs().get(0).toString());
							break;
						case Destination_Terminology:
							bt.addStringCell(column, this.getTerminology(destConcept.getPathNid()));
							break;
						case Parent_Concept_Id:  //TODO - no, needs to be input into this method
							bt.addStringCell(column, ""); //User Input
//							bt.addStringCell(column, OTFUtility.getConceptVersion(relVersion.getDestinationNid()).getUUIDs().get(0).toString());
							break;
						case Justification:
							bt.addStringCell(column, "");
							break;
						case Note:
							bt.addStringCell(column, "");
							break;
						default :
							throw new RuntimeException("Unexpected column type found in Sheet: " + column + " - " + SHEET.New_Concept);
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
					throw new RuntimeException("Unexpected column type found in Sheet: " + column + " - " + SHEET.New_Concept);
			}
		}
	}

	@Override
	public ContentRequestTrackingInfo getContentRequestStatus(ContentRequestTrackingInfo info)
	{
		// TODO: placeholder
		throw new UnsupportedOperationException("PLACEHOLDER for future functionality");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.va.isaac.interfaces.gui.views.PopupViewI#showView(javafx.stage.Window)
	 */
	@Override
	public void showView(Window parent)
	{
		// No view, per se is needed, though we could
		// put a warning here if the request won't make sense
		ConceptVersionBI concept = OTFUtility.getConceptVersion(nid);
		if (concept == null)
		{
			AppContext.getCommonDialogs().showErrorDialog("USCRS Content Request", "Unable to load concept for " + nid, "This should never happen");
			return;
		}

		try
		{
			if ((concept.getPathNid() != TermAux.SNOMED_CORE.getLenient().getNid())
					&& !OTFUtility.getConceptVersion(concept.getPathNid()).getPrimordialUuid().toString()
							.equals(AppContext.getAppConfiguration().getDefaultEditPathUuid()))
			{
				DialogResponse response = AppContext.getCommonDialogs().showYesNoDialog(
						"USCRS Content Request",
						"The concept path is neither Snomed CORE nor " + AppContext.getAppConfiguration().getDefaultEditPathName()
								+ ". It is recommended that you only submit " + "concepts edited on one of these paths to USCRS.\n\n" + "Do you want to continue?");
				if (response == DialogResponse.NO)
				{
					return;
				}
			}
		}
		catch (IOException e)
		{
			AppContext.getCommonDialogs().showErrorDialog("USCRS Content Request", "Unable to load concepts for path comparison.", "This should never happen");
			return;
		}

		try
		{
			UscrsContentRequestTrackingInfo info = submitContentRequest(nid);
			if (info.isSuccessful())
			{
				AppContext.getCommonDialogs().showInformationDialog("USCRS Content Request",
						"Content request submission successful.\n\nUpload " + info.getFile() + " to here: " + info.getUrl());

			}
		}
		catch (Exception e)
		{
			LOG.error("Unexpected error during submit", e);
			AppContext.getCommonDialogs().showErrorDialog("USCRS Content Request", "Unexpected error trying to submit request.", e.getMessage());
			return;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.
	 * UscrsContentRequestHandlerI#setConcept(int)
	 */
	@Override
	public void setConcept(int conceptNid)
	{
		this.nid = conceptNid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.
	 * UscrsContentRequestHandlerI#getConceptNid()
	 */
	@Override
	public int getConceptNid()
	{
		return nid;
	}

}
