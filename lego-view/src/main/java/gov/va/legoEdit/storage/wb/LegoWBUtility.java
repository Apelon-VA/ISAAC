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
package gov.va.legoEdit.storage.wb;

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.WBUtility;
import gov.va.legoEdit.model.schemaModel.Assertion;
import gov.va.legoEdit.model.schemaModel.AssertionComponent;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.model.schemaModel.Expression;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.LegoList;
import gov.va.legoEdit.model.schemaModel.Measurement;
import gov.va.legoEdit.model.schemaModel.Relation;
import gov.va.legoEdit.model.schemaModel.RelationGroup;
import gov.va.legoEdit.model.schemaModel.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.id.IdBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * WBUtility
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 * Copyright 2013
 */
public class LegoWBUtility
{
	private static Logger logger = LoggerFactory.getLogger(LegoWBUtility.class);
	private static UUID snomedIdType = TermAux.SNOMED_IDENTIFIER.getUuids()[0]; // SNOMED integer id
	private static Integer snomedIdTypeNid = null;

	public static Concept convertConcept(ConceptChronicleBI concept)
	{
		Concept c = null;
		if (concept != null && concept.getUUIDs() != null && concept.getUUIDs().size() > 0)
		{
			c = new Concept();
			c.setDesc(WBUtility.getDescription(concept));
			c.setUuid(concept.getUUIDs().get(0).toString());
			try
			{
				if (concept.getAdditionalIds() != null)
				{
					for (IdBI x : concept.getAdditionalIds())
					{
						if (x.getAuthorityNid() == getSnomedIdTypeNid() && Utility.isLong(x.getDenotation().toString()))
						{
							c.setSctid(Long.parseLong(x.getDenotation().toString()));
							break;
						}
					}
				}
			}
			catch (Exception e)
			{
				logger.warn("Oops", e);
			}
			if (c.getSctid() == null)
			{
				logger.debug("Couldn't find SCTID for concept " + c.getDesc() + " " + c.getUuid());
			}
		}
		return c;
	}

	private static int getSnomedIdTypeNid()
	{
		if (snomedIdTypeNid == null)
		{
			snomedIdTypeNid = ExtendedAppContext.getDataStore().getNidForUuids(snomedIdType);
		}
		return snomedIdTypeNid;
	}

	/**
	 * Updates (in place) all of the concepts within the supplied LegoList with
	 * the results from a WB lookup. Concepts which fail lookup are returned in
	 * the result list.
	 */
	public static List<Concept> lookupAllConcepts(LegoList ll)
	{
		ArrayList<Concept> failures = new ArrayList<>();

		// walk through the legolist, and to a lookup on each concept, flagging
		// errors on the ones that failed lookup.
		for (Lego l : ll.getLego())
		{
			for (Assertion a : l.getAssertion())
			{
				failures.addAll(lookupAll(a.getDiscernible().getExpression()));
				failures.addAll(lookupAll(a.getQualifier().getExpression()));
				failures.addAll(lookupAll(a.getValue().getExpression()));
				if (a.getValue() != null && a.getValue().getMeasurement() != null)
				{
					failures.addAll(lookupAll(a.getValue().getMeasurement()));
				}
				for (AssertionComponent ac : a.getAssertionComponent())
				{
					failures.addAll(lookupAll(ac.getType()));
				}
			}
		}
		return failures;
	}

	private static List<Concept> lookupAll(Expression e)
	{
		ArrayList<Concept> failures = new ArrayList<>();
		if (e == null)
		{
			return failures;
		}
		if (e.getConcept() != null)
		{
			Concept result = convertConcept(WBUtility.lookupIdentifier(e.getConcept().getUuid()));
			if (result == null)
			{
				result = convertConcept(WBUtility.lookupIdentifier(e.getConcept().getSctid() + ""));
			}
			if (result != null)
			{
				e.setConcept(result);
			}
			else
			{
				failures.add(e.getConcept());
			}
		}
		for (Expression e1 : e.getExpression())
		{
			failures.addAll(lookupAll(e1));
		}
		for (Relation r : e.getRelation())
		{
			failures.addAll(lookupAll(r));
		}
		for (RelationGroup rg : e.getRelationGroup())
		{
			for (Relation r : rg.getRelation())
			{
				failures.addAll(lookupAll(r));
			}
		}
		return failures;
	}

	private static List<Concept> lookupAll(Relation r)
	{
		ArrayList<Concept> failures = new ArrayList<>();
		if (r.getType() != null && r.getType().getConcept() != null)
		{
			failures.addAll(lookupAll(r.getType()));
		}
		if (r.getDestination() != null)
		{
			failures.addAll(lookupAll(r.getDestination().getExpression()));
			failures.addAll(lookupAll(r.getDestination().getMeasurement()));
		}
		return failures;
	}

	private static List<Concept> lookupAll(Type t)
	{
		ArrayList<Concept> failures = new ArrayList<Concept>();
		if (t == null || t.getConcept() == null)
		{
			return failures;
		}
		Concept result = convertConcept(WBUtility.lookupIdentifier(t.getConcept().getUuid()));
		if (result == null)
		{
			result = convertConcept(WBUtility.lookupIdentifier(t.getConcept().getSctid() + ""));
		}
		if (result != null)
		{
			t.setConcept(result);
		}
		else
		{
			failures.add(t.getConcept());
		}
		return failures;
	}

	private static List<Concept> lookupAll(Measurement m)
	{
		ArrayList<Concept> failures = new ArrayList<>();
		if (m == null || m.getUnits() == null || m.getUnits().getConcept() == null)
		{
			return failures;
		}
		Concept result = convertConcept(WBUtility.lookupIdentifier(m.getUnits().getConcept().getUuid()));
		if (result == null)
		{
			result = convertConcept(WBUtility.lookupIdentifier(m.getUnits().getConcept().getSctid() + ""));
		}
		if (result != null)
		{
			m.getUnits().setConcept(result);
		}
		else
		{
			failures.add(m.getUnits().getConcept());
		}
		return failures;
	}
}
