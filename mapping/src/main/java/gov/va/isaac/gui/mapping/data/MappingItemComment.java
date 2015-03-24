package gov.va.isaac.gui.mapping.data;

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.constants.ISAAC;
import gov.va.isaac.util.OTFUtility;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDynamicCAB;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicString;

/**
 * {@link MappingItemComment}
 *
 * @author David Triglianos
 */
public class MappingItemComment
{
	private String commentText;
	private String commentContext;
	private UUID mappingItemUUID;
	private UUID primoridalUUID;
	private UUID authorUUID;
	private long creationDate;
	private boolean isActive;

	/**
	 * Create (and store to the DB) a new comment
	 * @param pMappingItemUUID - The item the comment is being added to
	 * @param pCommentText - The text of the comment
	 * @param commentContext - (optional) field for storing other arbitrary info about the comment.  An editor may wish to put certain keywords on 
	 * some comments - this field is indexed, so a search for comments could query this field.
	 * @throws IOException
	 */
	public MappingItemComment(UUID pMappingItemUUID, String pCommentText, String commentContext) throws IOException
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

			read((RefexDynamicVersionBI<?>) ExtendedAppContext.getDataStore().getComponentVersion(OTFUtility.getViewCoordinate(), rdc.getPrimordialUuid()));
		}
		catch (InvalidCAB | ContradictionException | PropertyVetoException e)
		{
			throw new IOException("Unexpected error", e);
		}
	}
	
	protected MappingItemComment(RefexDynamicVersionBI<?> comment) throws IOException
	{
		read(comment);
	}

	private void read(RefexDynamicVersionBI<?> commentRefex) throws IOException
	{
		commentText = commentRefex.getData()[0].getDataObject().toString();
		commentContext = ((commentRefex.getData().length > 1 && commentRefex.getData()[1] != null) ? commentRefex.getData()[1].getDataObject().toString() : null);
		mappingItemUUID = ExtendedAppContext.getDataStore().getUuidPrimordialForNid(commentRefex.getReferencedComponentNid());
		primoridalUUID = commentRefex.getPrimordialUuid();
		authorUUID = ExtendedAppContext.getDataStore().getUuidPrimordialForNid(commentRefex.getAuthorNid());
		creationDate = commentRefex.getTime();
		isActive = commentRefex.getStatus() == Status.ACTIVE;
	}

	/**
	 * @return the commentText
	 */
	public String getCommentText()
	{
		return commentText;
	}

	/**
	 * @param commentText the commentText to set
	 */
	public void setCommentText(String commentText)
	{
		this.commentText = commentText;
	}
	

	/**
	 * @return the commentContext
	 */
	public String getCommentContext()
	{
		return commentContext;
	}

	/**
	 * @param commentContext the commentContext to set - optional field on a comment used for arbitrary purposed by an editor.
	 */
	public void setCommentContext(String commentContext)
	{
		this.commentContext = commentContext;
	}

	/**
	 * @return the mappingItemUUID - which is the identifier of the thing that the comment is attached to
	 */
	public UUID getMappingItemUUID()
	{
		return mappingItemUUID;
	}

	/**
	 * @return the primoridalUUID - the identifier of this comment - which I'll note isn't globally unique - If you edit this comment,
	 * it will retain the same UUID - but the DB will now contain two versions of the comment - the old and the new - you would need
	 * this variable and the creationDate to be globally unique.
	 */
	public UUID getPrimoridalUUID()
	{
		return primoridalUUID;
	}

	/**
	 * @return the authorName - a UUID that identifies a concept that represents the Author
	 */
	public UUID getAuthorName()
	{
		return authorUUID;
	}

	/**
	 * @return the creationDate
	 */
	public long getCreationDate()
	{
		return creationDate;
	}

	/**
	 * @return the isActive
	 */
	public boolean isActive()
	{
		return isActive;
	}

	/**
	 * @param isActive the isActive to set
	 */
	public void setActive(boolean isActive)
	{
		this.isActive = isActive;
	}
}
