package gov.va.legoEdit.storage;

import gov.va.isaac.util.WBUtility;
import gov.va.legoEdit.gui.legoFilterPane.LegoFilterPaneController;
import gov.va.legoEdit.model.LegoReference;
import gov.va.legoEdit.model.schemaModel.Assertion;
import gov.va.legoEdit.model.schemaModel.AssertionComponent;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.model.schemaModel.Destination;
import gov.va.legoEdit.model.schemaModel.Expression;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.Relation;
import gov.va.legoEdit.model.schemaModel.RelationGroup;
import gov.va.legoEdit.model.schemaModel.Type;
import gov.va.legoEdit.storage.wb.LegoWBUtility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * {@link AdvancedLegoFilter}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class AdvancedLegoFilter
{
	static Logger logger = LoggerFactory.getLogger(AdvancedLegoFilter.class);
		
	static private class LRUCache<A, B> extends LinkedHashMap<A, B>
	{
		private static final long serialVersionUID = 1L;
		private final int maxEntries;
		
		public LRUCache(int maxEntries)
		{
			super(maxEntries + 1, 1.0f, true);
			this.maxEntries = maxEntries;
		}
		@Override
		protected boolean removeEldestEntry(Entry<A, B> eldest)
		{
			return super.size() > maxEntries;
		}
	}
	
	static LRUCache<String, HashMap<String, Boolean>> hierarchyCache = new LRUCache<>(1000);

	public static ArrayList<LegoReference> findMatchingRelTypes(CloseableIterator<Lego> legos, Concept relTypeFilter, Concept relDestFilter, String destModifier, String relAppliesToLegoSection)
	{
		logger.debug("Applying Advanced Lego Filter - at start: - all legos: Params: {} : {} : {} : {}", (relTypeFilter == null ? null : relTypeFilter.getDesc()),
				(relDestFilter == null ? null : relDestFilter.getDesc()), destModifier, relAppliesToLegoSection);
		ArrayList<LegoReference> result = new ArrayList<>();
		while (legos.hasNext())
		{
			Lego l = legos.next();
			if ((relTypeFilter == null && relDestFilter == null) || legoPassesFilter(l, relTypeFilter, relDestFilter, destModifier, relAppliesToLegoSection))
			{
				result.add(new LegoReference(l));
			}
		}
		
		logger.debug("Finished Applying Advanced Lego Filter - at end: {}", result.size());
		return result;
	}
	
	public static void removeNonMatchingRelType(ArrayList<LegoReference> legos, Concept relTypeFilter, Concept relDestFilter, String destModifier,
			String relAppliesToLegoSection)
	{
		if (relTypeFilter == null && relDestFilter == null)
		{
			return;
		}
		logger.debug("Applying Advanced Lego Filter - at start: {} Params: {} : {} : {} : {}", legos.size(), (relTypeFilter == null ? null : relTypeFilter.getDesc()),
				(relDestFilter == null ? null : relDestFilter.getDesc()), destModifier, relAppliesToLegoSection);

		Iterator<LegoReference> legoIter = legos.iterator();
		while (legoIter.hasNext())
		{
			LegoReference lr = legoIter.next();
			if (!legoPassesFilter(BDBDataStoreImpl.getInstance().getLego(lr.getLegoUUID(), lr.getStampUUID()), relTypeFilter, relDestFilter, destModifier, relAppliesToLegoSection))
			{
				legoIter.remove();
			}
		}
		logger.debug("Finished Applying Advanced Lego Filter - at end: {}", legos.size());
	}

	static private boolean legoPassesFilter(Lego l, Concept relTypeFilter, Concept relDestFilter, String destModifier, String relAppliesToLegoSection)
	{
		for (Assertion a : l.getAssertion())
		{
			if (relAppliesToLegoSection == null && relDestFilter == null)
			{
				for (AssertionComponent ac : a.getAssertionComponent())
				{
					if (typeMatches(ac.getType(), relTypeFilter))
					{
						return true;
					}
				}
			}
			
			if ((relAppliesToLegoSection == null || relAppliesToLegoSection.equals(LegoFilterPaneController.DISCERNIBLE)) && a.getDiscernible() != null 
					&& typeMatches(a.getDiscernible().getExpression(), relTypeFilter) 
					&& destMatches(a.getDiscernible().getExpression(), relDestFilter, destModifier, false))
			{
					return true;
			}
			if ((relAppliesToLegoSection == null || relAppliesToLegoSection.equals(LegoFilterPaneController.QUALIFIER)) && a.getQualifier() != null 
					&& typeMatches(a.getQualifier().getExpression(), relTypeFilter) 
					&& destMatches(a.getQualifier().getExpression(), relDestFilter, destModifier, false))
			{
					return true;
			}
			
			if ((relAppliesToLegoSection == null || relAppliesToLegoSection.equals(LegoFilterPaneController.VALUE)) && a.getValue() != null 
					&& typeMatches(a.getValue().getExpression(), relTypeFilter) 
					&& destMatches(a.getValue().getExpression(), relDestFilter, destModifier, false))
			{
					return true;
			}
			
		}
		//If we haven't returned yet, none of the assertions matched both the type and dest filters.
		return false;
	}
	
	static private boolean destMatches(Expression e, Concept relDestFilter, String destModifier, boolean isDestConcept)
	{
		if (relDestFilter == null)
		{
			return true;
		}
		if (e == null)
		{
			return false;
		}
		
		if (isDestConcept)
		{
			if ((LegoFilterPaneController.IS.equals(destModifier) && conceptMatches(relDestFilter, e.getConcept()))
					|| (LegoFilterPaneController.CHILD_OF.equals(destModifier) && conceptHierarchyMatches(relDestFilter, e.getConcept())))
			{
				return true;
			}
		}
		
		for (Expression e1 : e.getExpression())
		{
			if (destMatches(e1, relDestFilter, destModifier, isDestConcept))
			{
				return true;
			}
		}
		for (Relation r : e.getRelation())
		{
			if (destMatches(r, relDestFilter, destModifier))
			{
				return true;
			}
		}
		
		for (RelationGroup rg : e.getRelationGroup())
		{
			for (Relation r : rg.getRelation())
			{
				if (destMatches(r, relDestFilter, destModifier))
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	static private boolean destMatches(Relation r, Concept relDestFilter, String destModifier)
	{
		if (relDestFilter == null)
		{
			return true;
		}
		if (r == null)
		{
			return false;
		}
		if (destMatches(r.getDestination(), relDestFilter, destModifier))
		{
			return true;
		}
		return false;
	}
	
	static private boolean destMatches(Destination d, Concept relDestFilter, String destModifier)
	{
		if (relDestFilter == null)
		{
			return true;
		}
		if (d == null)
		{
			return false;
		}
		if (destMatches(d.getExpression(), relDestFilter, destModifier, true))
		{
			return true;
		}
		return false;
	}
	
	static private boolean typeMatches(Expression e, Concept relTypeFilter)
	{
		if (relTypeFilter == null)
		{
			return true;
		}
		if (e == null)
		{
			return false;
		}
		
		for (Expression e1 : e.getExpression())
		{
			if (typeMatches(e1, relTypeFilter))
			{
				return true;
			}
		}
		
		for (Relation r : e.getRelation())
		{
			if (typeMatches(r, relTypeFilter))
			{
				return true;
			}
		}
		
		for (RelationGroup rg : e.getRelationGroup())
		{
			for (Relation r : rg.getRelation())
			{
				if (typeMatches(r, relTypeFilter))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	static private boolean typeMatches(Relation r, Concept relTypeFilter)
	{
		if (relTypeFilter == null)
		{
			return true;
		}
		if (r == null)
		{
			return false;
		}
		if (typeMatches(r.getType(), relTypeFilter))
		{
			return true;
		}
		if (typeMatches(r.getDestination(), relTypeFilter))
		{
			return true;
		}
		return false;
	}
	
	static private boolean typeMatches(Destination d, Concept relTypeFilter)
	{
		if (relTypeFilter == null)
		{
			return true;
		}
		if (d == null)
		{
			return false;
		}
		if (typeMatches(d.getExpression(), relTypeFilter))
		{
			return true;
		}
		return false;
	}
	
	static private boolean typeMatches(Type t, Concept relTypeFilter)
	{
		if (relTypeFilter == null)
		{
			return true;
		}
		if (t != null)
		{
			return conceptMatches(relTypeFilter, t.getConcept());
		}
		return false;
	}
	
	static private boolean conceptMatches(Concept filter, Concept concept)
	{
		if (concept == null)
		{
			return false;
		}
		if (filter == null)
		{
			return true;
		}
		
		if (filter.getUuid().equals(concept.getUuid()) || filter.getSctid() == concept.getSctid())
		{
			return true;
		}
		return false;
	}
	
	static private String makeKey(Concept concept)
	{
		return concept.getUuid() + ":" + concept.getSctid();
	}
	
	static private boolean conceptHierarchyMatches(Concept filter, Concept concept)
	{
		if (concept == null)
		{
			return false;
		}
		
		//this checks filter null
		if (conceptMatches(filter, concept))
		{
			return true;
		}
		
		//The cache I'm keeping is a mapping of the concept, to the desired filter concept, to the answer (yes or no)
		String conceptKey = makeKey(concept);
		String filterKey = makeKey(filter);
		HashMap<String, Boolean> map = hierarchyCache.get(conceptKey);
		if (map != null)
		{
			Boolean result = map.get(filterKey);
			if (result != null)
			{
				logger.debug("Cache hit - cache said " + result);
				return result;
			}
		}
		
		if (map == null)
		{
			map = new HashMap<String, Boolean>();
			hierarchyCache.put(conceptKey, map);
		}
		
		ConceptVersionBI cv = WBUtility.lookupIdentifier(concept.getUuid());
		if (cv == null)
		{
			cv = WBUtility.lookupIdentifier(concept.getSctid() + "");
		}
		if (cv == null)
		{
			return false;
		}
		else
		{
			try
			{
				Collection<List<Integer>> pathsToRoot = cv.getNidPathsToRoot();
				for (List<Integer> list : pathsToRoot)
				{
					for (Integer i : list)
					{
						Concept c = LegoWBUtility.convertConcept(WBUtility.getConceptVersion(i));
						if (conceptMatches(filter, c))
						{
							map.put(filterKey, true);
							return true;
						}
					}
				}
			}
			catch (IOException e)
			{
				logger.error("Unexpeted error during AdvancedLegoFilter", e);
			}
		}
		map.put(filterKey, false);
		return false;
	}
}
