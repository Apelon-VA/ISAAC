package gov.va.isaac.gui.mapping.data;

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.constants.ISAAC;
import gov.va.isaac.util.OTFUtility;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDynamicCAB;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicString;
import org.ihtsdo.otf.tcc.model.index.service.SearchResult;

public class MappingItemCommentDAO extends MappingDAO 
{
	/**
	 * Create (and store to the DB) a new comment
	 * @param pMappingItemUUID - The item the comment is being added to
	 * @param pCommentText - The text of the comment
	 * @param commentContext - (optional) field for storing other arbitrary info about the comment.  An editor may wish to put certain keywords on 
	 * some comments - this field is indexed, so a search for comments could query this field.
	 * @throws IOException
	 */
	public static MappingItemComment createMappingItemComment(UUID pMappingItemUUID, String pCommentText, String commentContext) throws IOException
	{
		if (pMappingItemUUID == null)
		{
			throw new IOException("UUID of component to attach the comment to is required");
		}
		if (StringUtils.isBlank(pCommentText))
		{
			throw new IOException("The comment is required");
		}

		try
		{
			RefexDynamicCAB commentAnnotation = new RefexDynamicCAB(pMappingItemUUID, ISAAC.COMMENT_ATTRIBUTE.getPrimodialUuid());
			commentAnnotation.setData(new RefexDynamicDataBI[] { 
					new RefexDynamicString(pCommentText),
					(StringUtils.isBlank(commentContext) ? null : new RefexDynamicString(commentContext))}, null);

			commentAnnotation.computeMemberUuid();

			if (ExtendedAppContext.getDataStore().hasUuid(commentAnnotation.getComponentUuid()))
			{
				throw new IOException("A comment of that value already exists on that item.");
			}

			RefexDynamicChronicleBI<?> rdc = OTFUtility.getBuilder().construct(commentAnnotation);

			ConceptChronicleBI cc = ExtendedAppContext.getDataStore().getConcept(rdc.getConceptNid());
			ExtendedAppContext.getDataStore().addUncommitted(cc);
			ExtendedAppContext.getDataStore().commit(cc);

			return new MappingItemComment((RefexDynamicVersionBI<?>) ExtendedAppContext.getDataStore().getComponentVersion(OTFUtility.getViewCoordinate(), rdc.getPrimordialUuid()));
		}
		catch (InvalidCAB | ContradictionException | PropertyVetoException e)
		{
			throw new IOException("Unexpected error", e);
		}
	}

	/**
	 * Read all comments for a particular mapping item (which could be a mapping set, or a mapping item)
	 * @param mappingUUID - The UUID of a MappingSet or a MappingItem
	 * @param activeOnly - when true, only return active comments
	 * @return
	 * @throws IOException
	 */
	public static List<MappingItemComment> getComments(UUID mappingUUID, boolean activeOnly) throws IOException {
		List<MappingItemComment> comments = new ArrayList<MappingItemComment>();
		
		try
		{
			for (SearchResult sr : search(ISAAC.COMMENT_ATTRIBUTE.getPrimodialUuid()))
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
		catch (ContradictionException e)
		{
			LOG.error("Unexpected error reading comments", e);
			throw new IOException("internal error reading comments");
		}
		return comments;
	}
	
	/**
	 * @param commentPrimordialUUID - The ID of the comment to be re-activated
	 * @throws IOException
	 */
	public void unRetireComment(UUID commentPrimordialUUID) throws IOException 
	{
		setRefexStatus(commentPrimordialUUID, Status.ACTIVE);
	}
	
	/**
	 * @param commentPrimordialUUID - The ID of the comment to be retired
	 * @throws IOException
	 */
	public void retireComment(UUID commentPrimordialUUID) throws IOException 
	{
		setRefexStatus(commentPrimordialUUID, Status.INACTIVE);
	}
	
	/**
	 * Store the values passed in as a new revision of a comment (the old revision remains in the DB)
	 * @param comment - The MappingItemComment with revisions (contains fields where the setters have been called)
	 * @throws IOException
	 */
	public void updateComment(MappingItemComment comment) throws IOException 
	{
		try
		{
			RefexDynamicVersionBI<?> rdv = readCurrentRefex(comment.getPrimordialUUID());
			RefexDynamicCAB commentCab = rdv.makeBlueprint(OTFUtility.getViewCoordinate(), IdDirective.PRESERVE, RefexDirective.EXCLUDE);
			commentCab.getData()[0] = new RefexDynamicString(comment.getCommentText());
			commentCab.getData()[1] = (StringUtils.isNotBlank(comment.getCommentContext()) ? new RefexDynamicString(comment.getCommentContext()) : null);
			RefexDynamicChronicleBI<?> rdc = OTFUtility.getBuilder().construct(commentCab);

			ConceptChronicleBI cc = ExtendedAppContext.getDataStore().getConcept(rdc.getConceptNid());
			ExtendedAppContext.getDataStore().addUncommitted(cc);
			ExtendedAppContext.getDataStore().commit(cc);
		}
		catch (InvalidCAB | ContradictionException | PropertyVetoException e)
		{
			LOG.error("Unexpected!", e);
			throw new IOException("Internal error");
		}
	}
}
