package gov.va.isaac.request.uscrs;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerHelper;
import gov.va.isaac.interfaces.gui.constants.SharedServiceNames;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.ContentRequestHandlerI;
import gov.va.isaac.interfaces.utility.DialogResponse;
import gov.va.isaac.request.ContentRequestHandler;
import gov.va.isaac.request.ContentRequestTrackingInfo;
import gov.va.isaac.request.uscrs.USCRSBatchTemplate.COLUMN;
import gov.va.isaac.request.uscrs.USCRSBatchTemplate.PICKLIST_Characteristic_Type;
import gov.va.isaac.request.uscrs.USCRSBatchTemplate.PICKLIST_Refinability;
import gov.va.isaac.request.uscrs.USCRSBatchTemplate.PICKLIST_Relationship_Type;
import gov.va.isaac.request.uscrs.USCRSBatchTemplate.PICKLIST_Semantic_Tag;
import gov.va.isaac.request.uscrs.USCRSBatchTemplate.PICKLIST_Source_Terminology;
import gov.va.isaac.request.uscrs.USCRSBatchTemplate.SHEET;
import gov.va.isaac.util.CommonMenus;
import gov.va.isaac.util.OTFUtility;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.stage.FileChooser;
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
	private static final Logger LOG = LoggerFactory.getLogger(CommonMenus.class);
	
	private int currentRequestId;

	@Override
	public UscrsContentRequestTrackingInfo submitContentRequest(int nid) throws Exception
	{
		LOG.debug("Submit content Request");

		ConceptChronicleBI concept = OTFUtility.getConceptVersion(nid);

		// Ideally this would connect to a request submission instance and dynamically create the request. In lieu
		// of that, we simply create a spreadsheet.

		// Create workbook
		USCRSBatchTemplate bt = new USCRSBatchTemplate(USCRSBatchTemplate.class.getResourceAsStream("/USCRS_Batch_Template-2015-01-27.xls"));

		currentRequestId = globalRequestCounter.getAndIncrement();
		
		// Handle new concept
		handleNewConcept(concept, bt);

		// Handle non-isa relationships
		handleNewRels(concept, bt);

		// Save the file
		LOG.info("Choose file to save");
		FileChooser fileChooser = new FileChooser();

		// Set extension filter.
		FileChooser.ExtensionFilter xmlFilter = new FileChooser.ExtensionFilter("Excel files", "*.xls");
		FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("All files (*.*)", "*.*");
		fileChooser.getExtensionFilters().addAll(xmlFilter, allFilter);
		fileChooser.setInitialFileName("USCRS_Export.xls");

		// Now determine
		UscrsContentRequestTrackingInfo info = new UscrsContentRequestTrackingInfo();
		info.setName(OTFUtility.getConPrefTerm(concept.getNid()));

		// Show save file dialog.
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
		{
			// Assume user cancelled
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
		LOG.debug("  Handle new concept tab");

		LOG.debug("    Add data for " + concept.toUserString());

		String fsn = OTFUtility.getFullySpecifiedName(concept);

		// PARENTS
		List<Double> parentIds = new ArrayList<>();
		for (RelationshipChronicleBI rel : concept.getRelationshipsOutgoing())
		{
			RelationshipVersionBI<?> relVersion = rel.getVersion(OTFUtility.getViewCoordinate());
			// check for "isa" relationship type
			if ((relVersion.getTypeNid() == Snomed.IS_A.getLenient().getNid()) && relVersion.isActive())
			{
				parentIds.add(Double.parseDouble(ConceptViewerHelper.getSctId(OTFUtility.getConceptVersion(relVersion.getDestinationNid())).trim()));
			}
		}
		LOG.debug("      parents = " + parentIds.size());
		if (parentIds.size() > 3)
		{
			throw new Exception("Cannot handle more than 3 parents");
		}

		//Synonyms
		List<String> synonyms = new ArrayList<>();
		for (DescriptionChronicleBI desc : concept.getDescriptions())
		{
			DescriptionVersionBI<?> descVersion = desc.getVersion(OTFUtility.getViewCoordinate());
			// find active, non FSN descriptions not matching the preferred name
			if (descVersion.isActive() && (descVersion.getTypeNid() != Snomed.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient().getNid())
					&& !descVersion.getText().equals(OTFUtility.getConPrefTerm(concept.getNid())))
			{
				synonyms.add(descVersion.getText());
			}
		}
		LOG.debug("      Synonym Count: {}", synonyms.size());

		bt.selectSheet(SHEET.New_Concept);
		bt.addRow();
		int colNumber = 0;
		for (COLUMN c : bt.getColumnsOfSheet(SHEET.New_Concept))
		{
			switch (c)
			{
				case Request_Id:
					bt.addNumericCell(colNumber++, currentRequestId);
					break;
				case Topic:
					// TODO: Topic - consider making the user enter this
					bt.addStringCell(colNumber++, "New concept");
					break;
				case Local_Code:
					bt.addStringCell(colNumber++, concept.getPrimordialUuid().toString());
					break;
				case Local_Term:
					bt.addStringCell(colNumber++, OTFUtility.getConPrefTerm(concept.getNid()));
					break;
				case Fully_Specified_Name:
					// Fully Specified Name (without the semantic tag)
					String fsnOnly = fsn;
					if (fsn.indexOf('(') != -1)
					{
						fsnOnly = fsn.substring(0, fsn.lastIndexOf('(') - 1);
					}
					bt.addStringCell(colNumber++, fsnOnly);
					break;
				case Semantic_Tag:
					PICKLIST_Semantic_Tag tag = null;
					if (fsn.indexOf('(') != -1)
					{
						String st = fsn.substring(fsn.lastIndexOf('(') + 1, fsn.lastIndexOf(')'));
						tag = PICKLIST_Semantic_Tag.find(st);
					}
					if (tag == null)
					{
						throw new Exception("Cannot submit a concept to USCRS without an FSN having a valid semantic tag.");
					}
					bt.addStringCell(colNumber++, tag.toString());
					break;
				case Preferred_Term:
					bt.addStringCell(colNumber++, OTFUtility.getConPrefTerm(concept.getNid()));
					break;
				case Terminology_1_:
				case Terminology_2_:
				case Terminology_3_:
					if (parentIds.size() == 0)
					{
						bt.addStringCell(colNumber++, "");
					}
					else
					{
						bt.addStringCell(colNumber++, PICKLIST_Source_Terminology.SNOMED_CT_International.toString());  //TODO this isn't a safe bet
					}
					break;
				case Parent_Concept_Id_1_:
				case Parent_Concept_Id_2_:
				case Parent_Concept__Id_3_:
					if (parentIds.size() == 0)
					{
						bt.addStringCell(colNumber++, "");
					}
					else
					{
						bt.addNumericCell(colNumber++, parentIds.remove(0));
					}
					break;
				case UMLS_CUI:
					bt.addStringCell(colNumber++, "");
					break;
				case Definition:
					// TODO: Definition - consider making the user enter this 
					bt.addStringCell(colNumber++, "See logical definition in relationships");
					break;
				case Proposed_Use:
					// TODO: Proposed Use - consider making the user enter this 
					bt.addStringCell(colNumber++, "");
					break;
				case Justification:
					// TODO: Justification - consider making the user enter this
					bt.addStringCell(colNumber++, "Developed as part of extension namespace " + AppContext.getAppConfiguration().getCurrentExtensionNamespace());
					break;
				case Note:
					StringBuilder sb = new StringBuilder();
					if (concept.getConceptAttributes().getVersion(OTFUtility.getViewCoordinate()).isDefined())
					{
						sb.append("NOTE: this concept is fully defined. ");
					}
					//First two synonyms have cols, any others go in Note
					boolean firstHasBeenSeen = false;
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
					bt.addStringCell(colNumber++, sb.toString());
					break;
				case Synonym:
					bt.addStringCell(colNumber++, (synonyms.size() > 0 ? synonyms.remove(0) : ""));
					break;
				default :
					throw new RuntimeException("Unexpected column type found in Sheet: " + c + " - " + SHEET.New_Concept);
			}
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
		LOG.debug("  Handle non-ISA rels");

		bt.selectSheet(SHEET.New_Relationship);

		for (RelationshipChronicleBI rel : concept.getRelationshipsOutgoing())
		{
			RelationshipVersionBI<?> relVersion = rel.getVersion(OTFUtility.getViewCoordinate());
			// find active, non-ISA relationships
			if (relVersion.isActive() && (relVersion.getTypeNid() != Snomed.IS_A.getLenient().getNid()))
			{
				bt.addRow();
				int colNumber = 0;
				for (COLUMN column : bt.getColumnsOfSheet(SHEET.New_Relationship))
				{
					LOG.debug("    Add rel " + relVersion.toUserString());
					switch (column)
					{
						case Topic:
							// TODO: Topic - consider making the user enter this
							bt.addStringCell(colNumber++, "See new concept request");
							break;
						case Source_Terminology:
							// Source Concept Id - aligns with Request Id from the new concept spreadsheet
							bt.addStringCell(colNumber++, PICKLIST_Source_Terminology.Current_Batch_Requests.toString());
							break;
						case Source_Concept_Id:
							bt.addNumericCell(colNumber++, currentRequestId);
							break;
						case Relationship_Type:
							bt.addStringCell(colNumber++, PICKLIST_Relationship_Type.find(OTFUtility.getConPrefTerm(relVersion.getTypeNid())).toString());
							break;
						case Destination_Terminology:
							// Destination Termionlogy - TODO: here we're only supporting things linked to SNOMED, in the future we may need to link
							// to things that have been previously created, but we need tracking info integration to do that properly.
							bt.addStringCell(colNumber++, PICKLIST_Source_Terminology.SNOMED_CT_International.toString());
							break;
						case Destination_Concept_Id:
							bt.addNumericCell(colNumber++, Double.parseDouble(ConceptViewerHelper.getSctId(
									OTFUtility.getConceptVersion(relVersion.getDestinationNid())).trim()));
							break;
						case Characteristic_Type:
							bt.addStringCell(colNumber++, PICKLIST_Characteristic_Type.Defining_relationship.toString());
							break;
						case Refinability:
							bt.addStringCell(colNumber++, PICKLIST_Refinability.Not_refinable.toString());
							break;
						case Relationship_Group:
							bt.addNumericCell(colNumber++, relVersion.getGroup());
							break;
						case Justification:
							bt.addStringCell(colNumber++, "Developed as part of extension namespace " + AppContext.getAppConfiguration().getCurrentExtensionNamespace());
							break;
						case Note:
							bt.addStringCell(colNumber++, "This is a defining relationship expressed for the corresponding new concept request in the other tab");
							break;
						default :
							throw new RuntimeException("Unexpected column type found in Sheet: " + column + " - " + SHEET.New_Relationship);
					}
				}
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
			e.printStackTrace();
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
