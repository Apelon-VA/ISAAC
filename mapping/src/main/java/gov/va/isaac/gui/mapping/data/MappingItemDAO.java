package gov.va.isaac.gui.mapping.data;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.lucene.queryparser.classic.ParseException;
import org.ihtsdo.otf.query.lucene.LuceneDynamicRefexIndexer;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.model.index.service.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappingItemDAO {

	private static final Logger LOG = LoggerFactory.getLogger(MappingItemDAO.class);

	public static List<MappingItem> getMappingItems(UUID mappingSetID) throws IOException
	{
		try
		{
			ArrayList<MappingItem> result = new ArrayList<>();
			
			LuceneDynamicRefexIndexer indexer = AppContext.getService(LuceneDynamicRefexIndexer.class);
			if (indexer == null)
			{
				throw new RuntimeException("Required index is not available");
			}
			List<SearchResult> refexes = indexer.queryAssemblageUsage(ExtendedAppContext.getDataStore().getNidForUuids(mappingSetID), Integer.MAX_VALUE, null);
			for (SearchResult sr : refexes)
			{
				RefexDynamicChronicleBI<?> rc = (RefexDynamicChronicleBI<?>) ExtendedAppContext.getDataStore().getComponent(sr.getNid());
				result.add(new MappingItem(rc));
			}
			
			return result;
		}
		catch (NumberFormatException | ParseException e)
		{
			LOG.error("Unexpected error reading mappings", e);
			throw new IOException("Error reading mappings", e);
		}
	}
	
}
