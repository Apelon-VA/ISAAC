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
package gov.va.isaac.gui.conceptCreation;

import gov.va.isaac.gui.conceptCreation.wizardPages.TermRow;
import gov.va.isaac.gui.conceptCreation.wizardPages.RelRow;
import gov.va.isaac.util.WBUtility;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.blueprint.ConceptCB;
import org.ihtsdo.otf.tcc.api.blueprint.DescriptionCAB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RelationshipCAB;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.lang.LanguageCode;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipType;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.model.cc.description.DescriptionRevision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * {@link WizardController}
 *
 * @author <a href="jefron@apelon.com">Jesse Efron</a>
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class WizardController {

	private static final Logger LOG = LoggerFactory.getLogger(WizardController.class);
	private String fsn;
	private String prefTerm;
	private List<ConceptVersionBI> parents;
	private boolean isPrimitive;
	private List<TermRow> syns;
	private List<RelRow> rels;
	private int acceptableTypeNid = 0;

	public void setConceptDefinitionVals(String fsn, String prefTerm,
			List<ConceptVersionBI> parents, boolean isPrimitive) {
		this.fsn = fsn;
		this.prefTerm = prefTerm;
		this.parents = parents;
		this.isPrimitive = isPrimitive;
	}
	
	public void setConceptComponents(List<TermRow> syns, List<RelRow> rels) {
		this.syns = syns;
		this.rels = rels;
	}

	public String getConceptFSN() {
		return fsn;
	}

	public String getConceptPT() {
		return prefTerm;
	}

	public String getConceptPrimDef() {
		if (isPrimitive) {
			return "Primitive";
		} else {
			return "Fully Defined";
		}
	}

	public List<ConceptVersionBI> getParents() {
		return parents;
	}

	public int getSynonymsCreated() {
		return syns.size();
	}

	public String getTerm(int i) {
		return syns.get(i).getTerm();
	}

	public int getTypeNid(int i) {
		return syns.get(i).getTypeNid();
	}

	public String getTypeString(int i) {
		return syns.get(i).getTypeString();
	}

	public String getCaseSensitivity(int i) {
		if (syns.get(i).isInitialCaseSig()) {
			return "True";
		} else {
			return "False";
		}
	}

	public String getLanguage(int i) {
		return LanguageCode.EN_US.getFormatedLanguageCode();
	}

	public int getRelationshipsCreated() {
		return rels.size();
	}

	public String getRelType(int i) {
		return WBUtility.getConPrefTerm(rels.get(i).getRelationshipNid());
	}
	
	public String getTarget(int i) {
		return WBUtility.getConPrefTerm(rels.get(i).getTargetNid());
	}
	
	//TODO make sure PT and FSN are case insensitive
	
	public String getQualRole(int i) {
		//TODO Why are we implementing a toString on Reltype?  ROLE isn't even an option in the enum
		if (RelationshipType.QUALIFIER == rels.get(i).getType()) {
			return "QUALIFIER";
		} else {
			return "ROLE";
		}
	}
	
	public String getGroup(int i) {
		return String.valueOf(rels.get(i).getGroup());
	}
	
	public ConceptChronicleBI createNewConcept() throws ValidationException, IOException, InvalidCAB, ContradictionException {
		String fsn = getConceptFSN();
		String prefTerm = getConceptPT();
		
		UUID isA = Snomed.IS_A.getUuids()[0];
		UUID parentCons[] = new UUID[parents.size()];
		for (int i = 0; i < parents.size(); i++) {
			parentCons[i] = parents.get(i).getPrimordialUuid();
		}

		IdDirective idDir = IdDirective.GENERATE_HASH;

		LanguageCode lc = LanguageCode.EN_US;
		UUID module = Snomed.CORE_MODULE.getLenient().getPrimordialUuid();
		
		ConceptCB newConCB = new ConceptCB(fsn, prefTerm, lc, isA, idDir, module, parentCons);
										
		ConceptChronicleBI newCon = WBUtility.getBuilder().construct(newConCB);
//		WBUtility.addUncommitted(newCon);

		return newCon;
	}
	
	public void createNewDescription(ConceptChronicleBI con, int i) throws IOException, InvalidCAB, ContradictionException {
		DescriptionCAB newDesc = new DescriptionCAB(con.getConceptNid(), 
													getTypeNid(i), 
													LanguageCode.EN_US, 
													syns.get(i).getTerm(), 
													syns.get(i).isInitialCaseSig(), 
													IdDirective.GENERATE_HASH);
		
		WBUtility.getBuilder().construct(newDesc);
//		WBUtility.addUncommitted(con);
	}
	
	public void createNewRelationship(ConceptChronicleBI con, int i) throws IOException, InvalidCAB, ContradictionException {
		RelationshipCAB newRel = new RelationshipCAB(con.getNid(), 
													 rels.get(i).getRelationshipNid(), 
													 rels.get(i).getTargetNid(),
													 rels.get(i).getGroup(),
													 rels.get(i).getType(), 
													 IdDirective.GENERATE_HASH);
		WBUtility.getBuilder().construct(newRel);
//		WBUtility.addUncommitted(con);
	}
}
