package gov.va.isaac;

import gov.va.isaac.util.CommonMenus;

import org.ihtsdo.otf.query.lucene.LuceneIndexer;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.va.isaac.AppContext;
import gov.va.isaac.config.profiles.UserProfileManager;
import gov.va.isaac.config.users.InvalidUserException;
import gov.va.isaac.init.SystemInit;
import gov.va.isaac.interfaces.gui.constants.SharedServiceNames;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.ContentRequestHandlerI;
import gov.va.isaac.interfaces.utility.DialogResponse;
import gov.va.isaac.util.OTFUtility;
import gov.va.isaac.util.UuidGenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

import javax.inject.Named;

import org.glassfish.hk2.api.PerLookup;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class VasTestCodeRunner {
	private ViewCoordinate viewCoordinate;
	private static final Logger LOG = LoggerFactory.getLogger(CommonMenus.class);
	
	public static void main(String[] args) 
			throws Exception {

		IOException dataStoreLocationInitException = SystemInit.doBasicSystemInit(new File("../../ISAAC-PA/app/"));
		if (dataStoreLocationInitException != null)
		{
			System.err.println("Configuration of datastore path failed.  DB will not be able to start properly!  " + dataStoreLocationInitException);
			System.exit(-1);
		}
		
		AppContext.getService(UserProfileManager.class).configureAutomationMode(new File("profiles"));
		
		VasTestCodeRunner testRunner = new VasTestCodeRunner();
		
		//Set the view coordinate
		testRunner.viewCoordinate = OTFUtility.getViewCoordinate();
		testRunner.viewCoordinate.setAllowedStatus(EnumSet.of(Status.INACTIVE, Status.ACTIVE));

		//**ENTER CONCEPT NID HERE**
		int nid = -2146838903;
		ConceptChronicleBI concept = OTFUtility.getConceptVersion(nid);
		
		StringBuilder sb = new StringBuilder();

		//1A FSN
		sb.append("1A - FSN: " + testRunner.getFSN(concept));
		sb.append("\r\n");
		
		//1B Preferred Term
		sb.append("1B - Preferred Term: " + testRunner.getPreferredTerm(concept));
		sb.append("\r\n");
		
		//1C Description Object (to string'ed)
		sb.append("1C - Descriptions \r\n " + testRunner.getDescriptions(concept));
		sb.append("\r\n");
		
		//1D Get Relationships
		sb.append("1D - Relationships \r\n " + testRunner.getRelationships(concept));
		sb.append("\r\n");
		
		//1E Get Description Only
		sb.append("1E - Description Terms Only \r\n " + testRunner.getDescriptionOnly(concept));
		sb.append("\r\n");
		
		//1F 1GFSN isCapitalSignificance - F & G
		sb.append("1F 1G - Infarction of Heart");
		sb.append(" & ");
		sb.append("Fully Specified Name - isCapitalSignificance \r\n " + testRunner.getCapitalSignificance(concept));
		sb.append("\r\n");
		
		//1H Find Relationship types
		sb.append("1H - Relationship Types \r\n " + testRunner.getRelationshipTypes(concept));
		sb.append("\r\n");
		
		//1I Is a relationship types
		sb.append("2 - I:\r\n " + testRunner.getIsaRelationships(concept));
		sb.append("\r\n");
		
		//2J Finding Site relationship destinations
		sb.append("2 - J: \r\n	" + testRunner.getFindingSiteRelationships(concept));
		sb.append("\r\n");
		
		//2K Infarct Target Role Groups
		sb.append("2 - K: \r\n Infarct target Role Group: \r\n" + testRunner.getInfarctTargetRoleGroup(concept));
		sb.append("\r\n");
		
		//3A + 3B
		sb.append(" 3 - A: \r\n" + testRunner.getDescriptionVersions(concept, "Myocardial infarct"));
		sb.append("\r\n");
		
		//3D
		sb.append(" 3 - D: \r\n" + testRunner.getDescriptionVersions(concept, "Cardiac infarction"));
		sb.append("\r\n");
		
		//3E
		sb.append(" 3 - E: \r\n" + testRunner.threeE(concept));
		sb.append("\r\n");
		
		//6A
		sb.append("6 - A Part 1: \r\n" + testRunner.sixA(concept));
		sb.append("\r\n");
		sb.append("6 - A Part 2: \r\n" + testRunner.sixA2(concept));
		sb.append("\r\n");
		
		System.out.println(sb.toString());

		
		
	}
	
	private String getFSN2(ConceptChronicleBI concept) {
			String fsn = OTFUtility.getFullySpecifiedName(concept);
	return fsn;
	}
	
	public String getFSN(ConceptChronicleBI concept) throws ContradictionException {
	try {
		if (concept.getDescriptions() != null) {
			for (DescriptionChronicleBI desc : concept.getDescriptions()) {
				int versionCount = desc.getVersions().size();
				DescriptionVersionBI<?> descVer = desc.getVersions().toArray(new DescriptionVersionBI[versionCount])[versionCount - 1];

				DescriptionVersionBI descriptionVersion = desc.getVersion(this.viewCoordinate);
				
				if(descriptionVersion.getTypeNid() == Snomed.FULLY_SPECIFIED_DESCRIPTION_TYPE.getNid()) {
//					|| descVer.getTypeNid() == OTFUtility.getFsnRf1Nid()) {
					return descriptionVersion.getText();
				}
			}
		}
	} catch (IOException e) {
		// noop
	} 
	
	return null;
}
	
	private String getPreferredTerm(ConceptChronicleBI concept) {
		return OTFUtility.getConPrefTerm(concept.getNid());
	}
	
	private String getCapitalSignificance(ConceptChronicleBI concept) throws IOException, ContradictionException {
		
		StringBuilder sb = new StringBuilder();
		
		String fsnResult = null;
		String infarctionResult = null;
		int descriptionTypeNid = 0;
		int snomedFsnNid = Snomed.FULLY_SPECIFIED_DESCRIPTION_TYPE.getNid();
//		int snomedFsnNid2 = OTFUtility.getFsnRf1Nid();
		int snomedSynonymNid = Snomed.SYNONYM_DESCRIPTION_TYPE.getNid();
		
		// 3 types of descriptions: FSN, Synonym, Definition	
		for (DescriptionChronicleBI desc : concept.getDescriptions()) {
			DescriptionVersionBI<?> descVersion = desc.getVersion(this.viewCoordinate);
			
			if(descVersion != null) {
			descriptionTypeNid = descVersion.getTypeNid();
			//1F
				if(descriptionTypeNid == snomedFsnNid) {
//					|| descriptionTypeNid == snomedFsnNid2) {

					fsnResult	= Boolean.toString(descVersion.isInitialCaseSignificant());
					sb.append("FSN - isInitialCaseSignificant: " + fsnResult);
					sb.append("\r\n");
				}
					
				// 1G
				if(descriptionTypeNid == snomedSynonymNid) {
					if(descVersion.getText().equals("Infarction of heart")) {
						infarctionResult	= Boolean.toString(descVersion.isInitialCaseSignificant());
						sb.append("Synonym - Infarction of Heart isInitialCaseSignificant: " + infarctionResult);
						sb.append("\r\n");
					}
				}
			}
		}
		return sb.toString();
	}
	
	private String getDescriptionOnly(ConceptChronicleBI concept) throws IOException, ContradictionException {
		StringBuilder sb = new StringBuilder();
		
		sb.append("\r\n");
		int count = 1;
		for (DescriptionChronicleBI desc : concept.getDescriptions()) {
			for(DescriptionVersionBI<?> thisDescVersion : desc.getVersions()) {
				sb.append("Description Term " + count++ + ": " + thisDescVersion.getNid() + " - ");
				sb.append(thisDescVersion.getText());
				sb.append("\r\n");
			}
		}
		return sb.toString();
	}
	
	private String getDescriptions(ConceptChronicleBI concept) 
		throws ValidationException, IOException, ContradictionException {
		
		StringBuilder sb = new StringBuilder();
		int count = 1;
		for (DescriptionChronicleBI desc : concept.getDescriptions()) {
			sb.append("Description " + count++ + ": " + desc.getNid() + " - " + desc.toString());
			sb.append("\r\n");
		}
		
		return sb.toString();
	}	
	
	private String getRelationships(ConceptChronicleBI concept) throws ValidationException, IOException, ContradictionException {
		
	StringBuilder sb = new StringBuilder();
	int count = 1;
	List<String> parentIds = new ArrayList<>();
	for (RelationshipChronicleBI rel : concept.getRelationshipsOutgoing()) {
		RelationshipVersionBI<?> relVersion = rel.getVersion(OTFUtility.getViewCoordinate());
		
		sb.append("Relationship " + count++ + ": " + rel.toString());
		sb.append("\r\n");
	}
	return sb.toString();
}
	
	private String getRelationshipTypes(ConceptChronicleBI concept) throws ValidationException, IOException, ContradictionException {
		
		StringBuilder sb = new StringBuilder();
		int count = 1;
		for (RelationshipChronicleBI rel : concept.getRelationshipsOutgoing()) {
			RelationshipVersionBI<?> relVersion = rel.getVersion(OTFUtility.getViewCoordinate());
			
			if(relVersion != null) {
				sb.append("Relationship Type " + count++ + ": " + OTFUtility.getDescription(relVersion.getTypeNid()));
			}
			sb.append("\r\n");
		}
		
		return sb.toString();
	}
	
 private String getIsaRelationships(ConceptChronicleBI concept) throws ValidationException, IOException, ContradictionException {
		
		StringBuilder sb = new StringBuilder();
			
		for (RelationshipChronicleBI rel : concept.getRelationshipsOutgoing()) {
			RelationshipVersionBI<?> relVersion = rel.getVersion(OTFUtility.getViewCoordinate());
			
			if(relVersion != null) {
				if(relVersion.getTypeNid() == Snomed.IS_A.getNid()) {
					sb.append("IS-A Relationship: " + OTFUtility.getDescription(relVersion.getConceptNid()) + " \r\n");
				}
			}
		}
		
		return sb.toString();
	}
 
 private String getFindingSiteRelationships(ConceptChronicleBI concept) throws ValidationException, IOException, ContradictionException {
	 
	 StringBuilder sb = new StringBuilder();
	 int count = 1;
	 for (RelationshipChronicleBI rel : concept.getRelationshipsOutgoing()) {
		 RelationshipVersionBI<?> relVersion = rel.getVersion(OTFUtility.getViewCoordinate());
		 
		 if(relVersion != null) {
			 if(relVersion.getTypeNid() == Snomed.FINDING_SITE.getNid()) {
				 // Every relationship has a triple: source, destination and type
					sb.append("Finding Site Relationship (Destination) " + count + ": " + OTFUtility.getDescription(relVersion.getDestinationNid()) + " \r\n");
			 }
		 }
	 }
	return sb.toString();
 }
 
 private String getInfarctTargetRoleGroup(ConceptChronicleBI concept) throws ValidationException, IOException, ContradictionException {
	 
	 StringBuilder sb = new StringBuilder();
	 finished:
	 for (RelationshipChronicleBI rel : concept.getRelationshipsOutgoing()) {
		 RelationshipVersionBI<?> relVersion = rel.getVersion(OTFUtility.getViewCoordinate());

		 ConceptChronicleBI versionConcept = OTFUtility.getConceptVersion(relVersion.getDestinationNid());

		 Collection<? extends DescriptionChronicleBI> descriptions = versionConcept.getDescriptions(); // Get current, don't itterate versions of desc
		 for(DescriptionChronicleBI desc : descriptions) {
			 
			 DescriptionVersionBI<?> thisVersion = (DescriptionVersionBI<?>) desc.getVersion(OTFUtility.getViewCoordinate());
			 DescriptionVersionBI<?> descVersion = thisVersion;
			 
			 if(descVersion.getText().equals("Infarct")) {
			 sb.append("INFARCT TARGET ROLE GROUP: " + relVersion.getGroup());
			 sb.append("\r\n");
			 break finished;
			 }
		 }
		 relVersion.getGroup();
	 }
	 
	 return sb.toString(); 
 }
 
 private String threeA(ConceptChronicleBI concept) throws ValidationException, IOException, ContradictionException {
	 // 3A
	 StringBuilder sb = new StringBuilder();
	 sb.append("\r\n");
	 sb.append("Print Out Every Version of description Myocardial Infarct");
	 sb.append("\r\n");
	 	boolean foundInfarct = false;
	 for (DescriptionChronicleBI desc : concept.getDescriptions()) {
		 int count = 1;
		 for(DescriptionVersionBI<?> descVersion : desc.getVersions()) {
		 	if(descVersion.getText().equals("Myocardial infarct")) {
					foundInfarct = true;
					break;
		 	}

			}
		 
		 if(foundInfarct) {
			for(DescriptionVersionBI<?> descVersion : desc.getVersions()) {
			sb.append("Version " + count++ + ":" + descVersion.getNid());
			sb.append("\r\n");
			sb.append(descVersion.toString());
			sb.append("\r\n");
			}
			break;
		 }

		}

	 
	 return sb.toString(); 
 }
 
 private String getDescriptionVersions(ConceptChronicleBI concept, String description) throws ValidationException, IOException, ContradictionException {
	// 3A
	StringBuilder sb = new StringBuilder();
	sb.append("Print Out Every Version of description " + description);
	sb.append("\r\n");
	boolean foundCardiacInfarction = false;
	 for (DescriptionChronicleBI desc : concept.getDescriptions()) {
		int count = 1;
		for (DescriptionVersionBI<?> descVersion : desc.getVersions()) {
		 	if(descVersion.getText().equals(description)) {
		 		foundCardiacInfarction = true;
					break;
		 	}
			}
		 if(foundCardiacInfarction) {
			for(DescriptionVersionBI<?> descVersion : desc.getVersions()) {
			sb.append("Version " + count++ + ":" + descVersion.getNid() + " - ");
			sb.append(descVersion.toString());
			sb.append("\r\n");
			}
			break;
		 }
		}
	 
	 return sb.toString(); 
 }
 
 private String threeE(ConceptChronicleBI concept) throws ValidationException, IOException, ContradictionException {
	 
	 StringBuilder sb = new StringBuilder();
			
	 boolean ihdFound = false;
	 int versionCount = 1;
	 for (RelationshipChronicleBI rel : concept.getRelationshipsOutgoing()) { //Each Relationship
		 Collection<? extends RelationshipVersionBI<?>> relVersions = rel.getVersions();
		 for(RelationshipVersionBI<?> version : relVersions) { //Each Relationship Version
			 ConceptChronicleBI versionConcept = OTFUtility.getConceptVersion(version.getDestinationNid());
			 Collection<? extends DescriptionChronicleBI> descriptions = versionConcept.getDescriptions();
			 	for(DescriptionChronicleBI desc : descriptions) { //Each Description Chronicle
			 		Collection<? extends DescriptionVersionBI<?>> descVersions =	(Collection<? extends DescriptionVersionBI<?>>) desc.getVersions();
			 		for(DescriptionVersionBI<?> descVersion : descVersions) {
			 		if(descVersion.getText().equals("Ischemic heart disease")) {
							sb.append("Version " + versionCount++ + ": " + descVersion.toString());
							sb.append("\r\n");
							}
			 		}
					}
		 }
	}
	 return sb.toString();
 }
 
 private String sixA(ConceptChronicleBI concept) throws ValidationException, IOException, ContradictionException {
	 
	 String term = null;
	 int type = 0;
	 boolean descCase = false;
	 String langCode = null;
	 
	 boolean firstRun = true;
	 boolean foundDescription = false;
	 
		StringBuilder sb = new StringBuilder();
		int count = 1;
		for (DescriptionChronicleBI desc : concept.getDescriptions()) {
		Collection<? extends DescriptionVersionBI<?>> descVersions =	(Collection<? extends DescriptionVersionBI<?>>) desc.getVersions();
		for(DescriptionVersionBI<?> descVersion : descVersions) {
		if(descVersion.getText().equals("Vas's testing desc")) {
			foundDescription = true;
		}
		}
		
		if(foundDescription) {
			for(DescriptionVersionBI<?> descVersion : descVersions) {
				sb.append("Vas's testing desc Version " + count++ + ": ");
			
				if(descVersion.getText() != term && !firstRun) {
					sb.append("***Term: " + descVersion.getText() + "*** ");
				} else {
					sb.append("Term: " + term + " ");
				}
				
				if(descVersion.getTypeNid() != type && !firstRun) {
					sb.append("***Type: " + descVersion.getTypeNid() + "*** ");
				} else {
					sb.append("Type: " + type + " ");
				}
				
				if(descVersion.isInitialCaseSignificant() != descCase && !firstRun) {
					sb.append("***isInitialCase: " + descVersion.isInitialCaseSignificant() + "*** ");
				} else {
					sb.append("isInitialCase: " + descCase + " ");
				}
			
				if(!descVersion.getLang().equals(langCode) && !firstRun) {
					sb.append("***languageCode: " + descVersion.getLang() + "*** ");
				} else {
					sb.append("languageCode: " + langCode + " ");
				}
			
				if(firstRun) {
					firstRun = false;
				}
				
				term = descVersion.getText();
				type = descVersion.getTypeNid();
				descCase = descVersion.isInitialCaseSignificant();
				langCode = descVersion.getLang();
					
				sb.append("\r\n");
			}
			break;
		}
		}
		return sb.toString();
 }	
 
 private String sixA2(ConceptChronicleBI concept) throws ValidationException, IOException, ContradictionException {
	 
		String term = null;
		int type = 0;
		boolean descCase = false;
		String langCode = null;
		int role = 0;
		
		boolean firstRun = true;
		boolean descFound = false;
	 
		StringBuilder sb = new StringBuilder();
		int count = 1;
		for (RelationshipChronicleBI rel : concept.getRelationshipsOutgoing()) {
			Collection<? extends RelationshipVersionBI<?>> relVersions = rel.getVersions();
			for(RelationshipVersionBI<?> version : relVersions) { //Each Relationship Version
				ConceptChronicleBI versionConcept = OTFUtility.getConceptVersion(version.getDestinationNid());
				Collection<? extends DescriptionChronicleBI> descriptions = versionConcept.getDescriptions();
				for(DescriptionChronicleBI desc : descriptions) { //Each Description Chronicle
					Collection<? extends DescriptionVersionBI<?>> descVersions = (Collection<? extends DescriptionVersionBI<?>>) desc.getVersions();
					for(DescriptionVersionBI<?> descVersion : descVersions) {
						if(descVersion.getText().equals("Cardiac wall structure")) {
							descFound = true;
						}
					}
					
					if(descFound) {
						for(DescriptionVersionBI<?> descVersion : descVersions) {
							sb.append("Cardiac Wall Structure Version " + count++ + ": ");
						
							if(descVersion.getText() != term && !firstRun) {
								sb.append("***Term: " + descVersion.getText() + "*** ");
							} else {
								sb.append("Term: " + term + " ");
							}
							
							if(descVersion.getTypeNid() != type && !firstRun) {
								sb.append("***Type: " + descVersion.getTypeNid() + "*** ");
							} else {
								sb.append("Type: " + type + " ");
							}
							
							if(descVersion.isInitialCaseSignificant() != descCase && !firstRun) {
								sb.append("***isInitialCase: " + descVersion.isInitialCaseSignificant() + "*** ");
							} else {
								sb.append("isInitialCase: " + descCase + " ");
							}
						
							if(descVersion.getLang() != langCode && !firstRun) {
								sb.append("***languageCode: " + descVersion.getLang() + "*** ");
							} else {
								sb.append("languageCode: " + langCode + " ");
							}
							
							if(version.getGroup() != role && !firstRun) {
								sb.append("***rolegroup: " + version.getGroup() + "*** ");
							} else {
								sb.append("roleGroup: " + version.getGroup() + " ");
								role = version.getGroup();
							}
							
							if(firstRun) {
								firstRun = false;
							}
							
							term = descVersion.getText();
							type = descVersion.getTypeNid();
							descCase = descVersion.isInitialCaseSignificant();
							langCode = descVersion.getLang();
							
								sb.append("\r\n");
								}
							break;
						}
					}
	//			descFound = false;
			}
		}
		
		return sb.toString();
}

}
