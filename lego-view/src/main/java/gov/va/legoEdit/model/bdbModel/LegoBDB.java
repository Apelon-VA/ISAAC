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
package gov.va.legoEdit.model.bdbModel;

import gov.va.legoEdit.model.ModelUtil;
import gov.va.legoEdit.model.schemaModel.Assertion;
import gov.va.legoEdit.model.schemaModel.AssertionComponent;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.model.schemaModel.Expression;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.Measurement;
import gov.va.legoEdit.model.schemaModel.Relation;
import gov.va.legoEdit.model.schemaModel.RelationGroup;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import gov.va.legoEdit.storage.DataStoreException;
import gov.va.legoEdit.storage.WriteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

/**
 * This class handles the storage of the LEGO object into the BerkeleyDB.
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Entity
public class LegoBDB
{
	@PrimaryKey private String uniqueId;
	@SecondaryKey(relate = Relationship.MANY_TO_ONE) protected String legoUUID;
	protected String stampId;
	protected String comment;
	@SecondaryKey(relate = Relationship.MANY_TO_ONE) protected String pncsId;
	protected List<Assertion> assertions;
	@SecondaryKey(relate = Relationship.MANY_TO_MANY) protected Set<String> usedSCTIdentifiers;
	// This is the list of assertions defined within this lego.
	@SecondaryKey(relate = Relationship.MANY_TO_MANY) protected Set<String> usedAssertionUUIDs;
	// This is the list of assertions linked to by this lego in the form of composite assertions.
	@SecondaryKey(relate = Relationship.MANY_TO_MANY) protected Set<String> compositeAssertionUUIDs;

	// not stored
	private transient PncsBDB pncsBDBRef;
	private transient StampBDB stampBDBRef;

	@SuppressWarnings("unused")
	private LegoBDB()
	{
		// required by BDB
	}

	public LegoBDB(Lego lego) throws WriteException
	{
		legoUUID = lego.getLegoUUID();
		pncsBDBRef = new PncsBDB(lego.getPncs());
		pncsId = pncsBDBRef.getUniqueId();
		comment = lego.getComment();
		stampBDBRef = new StampBDB(lego.getStamp());
		stampId = stampBDBRef.getStampId();
		assertions = new ArrayList<>();
		usedAssertionUUIDs = new HashSet<>();
		compositeAssertionUUIDs = new HashSet<>();
		usedSCTIdentifiers = new HashSet<>();
		for (Assertion a : lego.getAssertion())
		{
			assertions.add(a);
			checkAndUpdateAssertionList(a);
			indexExpression(a.getDiscernible().getExpression());
			indexExpression(a.getQualifier().getExpression());
			indexMeasurement(a.getTiming());
			indexExpression(a.getValue().getExpression());
			indexMeasurement(a.getValue().getMeasurement());

			for (AssertionComponent ac : a.getAssertionComponent())
			{
				compositeAssertionUUIDs.add(ac.getAssertionUUID());
			}
		}

		this.uniqueId = ModelUtil.makeUniqueLegoID(legoUUID, stampId);
	}

	private void indexConcept(Concept c)
	{
		if (c != null)
		{
			if (c.getSctid() != null)
			{
				usedSCTIdentifiers.add(c.getSctid() + "");
			}
			if (c.getUuid() != null && c.getUuid().length() > 0)
			{
				usedSCTIdentifiers.add(c.getUuid());
			}
		}
	}

	private void indexExpression(Expression expression)
	{
		if (expression != null)
		{
			indexConcept(expression.getConcept());

			if (expression.getExpression() != null)
			{
				for (Expression e : expression.getExpression())
				{
					indexExpression(e);
				}
			}

			if (expression.getRelation() != null)
			{
				for (Relation r : expression.getRelation())
				{
					indexRelation(r);
				}
			}
			if (expression.getRelationGroup() != null)
			{
				for (RelationGroup rg : expression.getRelationGroup())
				{
					for (Relation r : rg.getRelation())
					{
						indexRelation(r);
					}
				}
			}
		}
	}

	private void indexRelation(Relation r)
	{
		if (r.getType() != null)
		{
			indexConcept(r.getType().getConcept());
		}
		if (r.getDestination() != null)
		{
			indexExpression(r.getDestination().getExpression());
			indexMeasurement(r.getDestination().getMeasurement());
		}
	}

	private void indexMeasurement(Measurement m)
	{
		if (m != null && m.getUnits() != null)
		{
			indexConcept(m.getUnits().getConcept());
		}
	}

	public String getLegoUUID()
	{
		return legoUUID;
	}

	public String getComment()
	{
		return comment;
	}

	public String getStampId()
	{
		return stampId;
	}

	public StampBDB getStampBDB()
	{
		if (stampBDBRef == null)
		{
			stampBDBRef = ((BDBDataStoreImpl) BDBDataStoreImpl.getInstance()).getStampByUniqueId(stampId);
		}
		return stampBDBRef;
	}

	public PncsBDB getPncsBDB()
	{
		return pncsBDBRef;
	}

	public String getPncsId()
	{
		return pncsId;
	}

	public void addAssertion(Assertion assertion) throws WriteException
	{
		if (assertions == null)
		{
			assertions = new ArrayList<>();
		}
		assertions.add(assertion);
		checkAndUpdateAssertionList(assertion);

		for (AssertionComponent ac : assertion.getAssertionComponent())
		{
			compositeAssertionUUIDs.add(ac.getAssertionUUID());
		}

		indexExpression(assertion.getDiscernible().getExpression());
		indexExpression(assertion.getQualifier().getExpression());
		indexMeasurement(assertion.getTiming());
		indexExpression(assertion.getValue().getExpression());
		indexMeasurement(assertion.getValue().getMeasurement());
	}

	/**
	 * Note, this returns a copy (you can't add to this list)
	 */
	public List<Assertion> getAssertions()
	{
		ArrayList<Assertion> result = new ArrayList<>();
		if (assertions == null)
		{
			return result;
		}
		for (Assertion a : assertions)
		{
			result.add(a);
		}
		return result;
	}

	public String getUniqueId()
	{
		return uniqueId;
	}

	public Lego toSchemaLego()
	{
		Lego l = new Lego();
		l.setLegoUUID(legoUUID);
		l.setComment(comment);
		l.setStamp(getStampBDB().toSchemaStamp());
		l.setPncs(((BDBDataStoreImpl) BDBDataStoreImpl.getInstance()).getPncsByUniqueId(pncsId));
		if (l.getPncs() == null)
		{
			throw new DataStoreException("Pncs should be present!");
		}
		l.getAssertion().addAll(assertions);
		return l;
	}

	private void checkAndUpdateAssertionList(Assertion a) throws WriteException
	{
		if (!usedAssertionUUIDs.add(a.getAssertionUUID()))
		{
			throw new WriteException("Each assertion within a Lego must have a unique UUID");
		}
		Set<String> legosUsingAssertionUUID = ((BDBDataStoreImpl) BDBDataStoreImpl.getInstance()).getLegoUUIDsContainingAssertion(a.getAssertionUUID());
		legosUsingAssertionUUID.remove(legoUUID);
		if (legosUsingAssertionUUID.size() > 0)
		{
			throw new WriteException("The assertion UUID '" + a.getAssertionUUID() + "' is already in use by the lego '" + legosUsingAssertionUUID.iterator().next()
					+ "'.  Assertion UUIDs should be unique");
		}
	}
}
