package gov.va.isaac.gui.mapping.data;

import gov.va.isaac.ExtendedAppContext;
import java.io.IOException;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;

/**
 * {@link MappingItemComment}
 *
 * @author David Triglianos
 */
public class MappingItemComment extends StampedItem
{
	private String commentText;
	private String commentContext;
	private UUID mappingItemUUID;
	private UUID primoridalUUID;


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
		readStampDetails(commentRefex);
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
	public UUID getPrimordialUUID()
	{
		return primoridalUUID;
	}
}
