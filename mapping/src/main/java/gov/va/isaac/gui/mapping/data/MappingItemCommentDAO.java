package gov.va.isaac.gui.mapping.data;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.constants.ISAAC;
import gov.va.isaac.util.OTFUtility;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.ihtsdo.otf.query.lucene.LuceneDynamicRefexIndexer;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDynamicCAB;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicString;
import org.ihtsdo.otf.tcc.model.index.service.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappingItemCommentDAO {

	private static final Logger LOG = LoggerFactory.getLogger(MappingItemCommentDAO.class);

	public static List<MappingItemComment> getComments(UUID mappingItemUUID, boolean activeOnly) throws IOException {
		List<MappingItemComment> comments = new ArrayList<MappingItemComment>();
		
		LuceneDynamicRefexIndexer indexer = AppContext.getService(LuceneDynamicRefexIndexer.class);
		if (indexer == null)
		{
			LOG.error("No index found on comment refex!");
			throw new RuntimeException("Required index is not available");
		}
		try
		{
			List<SearchResult> refexes = indexer.queryAssemblageUsage(ISAAC.COMMENT_ATTRIBUTE.getNid(), 5000, Long.MAX_VALUE);
			for (SearchResult sr : refexes)
			{
				RefexDynamicVersionBI<?> rc = (RefexDynamicVersionBI<?>) ExtendedAppContext.getDataStore().
						getComponentVersion(OTFUtility.getViewCoordinate(), sr.getNid());
				if (rc != null)
				{
					if (activeOnly && !rc.isActive())
					{
						continue;
					}
					comments.add(new MappingItemComment(rc));
				}
			}
		}
		catch (Exception e)
		{
			LOG.error("Unexpected error reading comment refexex", e);
			throw new RuntimeException("Unexpected error reading comment refexes");
		}
		return comments;
	}
	
	public void unRetireComment(UUID commentPrimordialUUID) throws IOException 
	{
		setStatus(commentPrimordialUUID, Status.ACTIVE);
	}
	
	public void retireComment(UUID commentPrimordialUUID) throws IOException 
	{
		setStatus(commentPrimordialUUID, Status.INACTIVE);
	}
	
	private void setStatus(UUID commentPrimordialUUID, Status status) throws IOException
	{
		try
		{
			RefexDynamicVersionBI<?> rdv = readCurrent(commentPrimordialUUID);
			if (rdv.getStatus() == status)
			{
				LOG.warn("Tried set the status to the value is already has.  Doing nothing");
			}
			else
			{
				RefexDynamicCAB commentCab = rdv.makeBlueprint(OTFUtility.getViewCoordinate(), IdDirective.PRESERVE, RefexDirective.EXCLUDE);
				commentCab.setStatus(status == Status.ACTIVE ? Status.INACTIVE : Status.ACTIVE);
				RefexDynamicChronicleBI<?> rdc = OTFUtility.getBuilder().construct(commentCab);

				ConceptChronicleBI cc = ExtendedAppContext.getDataStore().getConcept(rdc.getConceptNid());
				ExtendedAppContext.getDataStore().addUncommitted(cc);
				ExtendedAppContext.getDataStore().commit(cc);
			}
		}
		catch (InvalidCAB | ContradictionException e)
		{
			LOG.error("Unexpected!", e);
			throw new RuntimeException("Internal error");
		}
	}
	public void updateComment(MappingItemComment comment) throws IOException 
	{
		try
		{
			RefexDynamicVersionBI<?> rdv = readCurrent(comment.getPrimoridalUUID());
			RefexDynamicCAB commentCab = rdv.makeBlueprint(OTFUtility.getViewCoordinate(), IdDirective.PRESERVE, RefexDirective.EXCLUDE);
			commentCab.getData()[0] = new RefexDynamicString(comment.getCommentText());
			if (StringUtils.isNotBlank(comment.getCommentContext()))
			{
				commentCab.getData()[1] = new RefexDynamicString(comment.getCommentContext());
			}
			RefexDynamicChronicleBI<?> rdc = OTFUtility.getBuilder().construct(commentCab);

			ConceptChronicleBI cc = ExtendedAppContext.getDataStore().getConcept(rdc.getConceptNid());
			ExtendedAppContext.getDataStore().addUncommitted(cc);
		}
		catch (InvalidCAB | ContradictionException | PropertyVetoException e)
		{
			LOG.error("Unexpected!", e);
			throw new RuntimeException("Internal error");
		}
	}
	
	private RefexDynamicVersionBI<?>  readCurrent(UUID commentUUID) throws IOException, ContradictionException
	{
		return (RefexDynamicVersionBI<?>) ExtendedAppContext.getDataStore().getComponentVersion(OTFUtility.getViewCoordinate(), commentUUID);
	}
}
