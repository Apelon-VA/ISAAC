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
package gov.va.isaac.associations;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.constants.ISAAC;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.queryparser.classic.ParseException;
import org.ihtsdo.otf.query.lucene.LuceneDynamicRefexIndexer;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicUsageDescription;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicString;
import org.ihtsdo.otf.tcc.model.index.service.SearchResult;

/**
 * {@link AssociationUtilities}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class AssociationUtilities
{
	private static int associationNid = Integer.MIN_VALUE;

	private static int getAssociationNid() throws ValidationException, IOException
	{
		if (associationNid == Integer.MIN_VALUE)
		{
			associationNid = ISAAC.ASSOCIATION_REFEX.getNid();
		}
		return associationNid;
	}

	public static List<Association> getSourceAssociations(ComponentChronicleBI<?> component, ViewCoordinate vc) throws IOException
	{
		ArrayList<Association> result = new ArrayList<>();
		for (RefexDynamicVersionBI<?> refex : component.getRefexesDynamicActive(vc))
		{
			ConceptVersionBI refexAssemblageType = ExtendedAppContext.getDataStore().getConceptVersion(vc, refex.getAssemblageNid());

			for (RefexDynamicChronicleBI<?> refexAttachedToAssemblage : refexAssemblageType.getRefexesDynamic())
			{
				if (refexAttachedToAssemblage.getAssemblageNid() == getAssociationNid())
				{
					result.add(new Association(refex));
					break;
				}
			}

		}
		return result;
	}

	public static List<Association> getTargetAssociations(ComponentChronicleBI<?> component, ViewCoordinate vc) throws IOException, ContradictionException, ParseException, PropertyVetoException
	{
		ArrayList<Association> result = new ArrayList<>();
		
		//TODO validate the concept is annotated for association?

		LuceneDynamicRefexIndexer indexer = AppContext.getService(LuceneDynamicRefexIndexer.class);
		if (indexer == null)
		{
			throw new RuntimeException("Required index is not available");
		}
		
		for (ConceptChronicleBI associationType : getAssociationTypes())
		{
			int colIndex = findTargetColumnIndex(associationType.getNid());
			List<SearchResult> refexes = indexer.query(new RefexDynamicString(component.getNid() + " OR " + component.getPrimordialUuid()),
					associationType.getNid(), false, new Integer[] {colIndex}, Integer.MAX_VALUE, null);
			for (SearchResult sr : refexes)
			{
				RefexDynamicChronicleBI<?> rc = (RefexDynamicChronicleBI<?>) ExtendedAppContext.getDataStore().getComponent(sr.getNid());
				RefexDynamicVersionBI<?> rv = rc.getVersion(vc);
				if (rv != null)
				{
					result.add(new Association(rv));
				}
			}
		}
		return result;
	}

	public static List<Association> getAssociationsOfType(ConceptChronicleBI concept, ViewCoordinate vc) throws NumberFormatException, IOException, ParseException, ContradictionException
	{
		ArrayList<Association> result = new ArrayList<>();

		//TODO validate the concept is annotated for association?

		LuceneDynamicRefexIndexer indexer = AppContext.getService(LuceneDynamicRefexIndexer.class);
		if (indexer == null)
		{
			throw new RuntimeException("Required index is not available");
		}
		List<SearchResult> refexes = indexer.queryAssemblageUsage(concept.getNid(), Integer.MAX_VALUE, null);
		for (SearchResult sr : refexes)
		{
			RefexDynamicChronicleBI<?> rc = (RefexDynamicChronicleBI<?>) ExtendedAppContext.getDataStore().getComponent(sr.getNid());
			RefexDynamicVersionBI<?> rv = rc.getVersion(vc);
			if (rv != null)
			{
				result.add(new Association(rv));
			}
		}

		return result;
	}

	public static List<ConceptChronicleBI> getAssociationTypes() throws NumberFormatException, ValidationException, IOException, ParseException
	{
		ArrayList<ConceptChronicleBI> result = new ArrayList<>();

		LuceneDynamicRefexIndexer indexer = AppContext.getService(LuceneDynamicRefexIndexer.class);
		if (indexer == null)
		{
			throw new RuntimeException("Required index is not available");
		}
		List<SearchResult> refexes = indexer.queryAssemblageUsage(ISAAC.ASSOCIATION_REFEX.getNid(), Integer.MAX_VALUE, null);
		for (SearchResult sr : refexes)
		{
			result.add(ExtendedAppContext.getDataStore().getComponent(sr.getNid()).getEnclosingConcept());
		}
		return result;
	}

	/**
	 * @param assemblageNid
	 * @throws ContradictionException 
	 * @throws IOException 
	 */
	public static int findTargetColumnIndex(int assemblageNid) throws IOException, ContradictionException
	{
		RefexDynamicUsageDescription rdud = RefexDynamicUsageDescription.read(assemblageNid);

		for (RefexDynamicColumnInfo rdci : rdud.getColumnInfo())
		{
			if (rdci.getColumnDescriptionConcept().equals(ISAAC.REFEX_COLUMN_TARGET_COMPONENT.getPrimodialUuid()))
			{
				return rdci.getColumnOrder();
			}
		}
		return Integer.MIN_VALUE;
	}
}
