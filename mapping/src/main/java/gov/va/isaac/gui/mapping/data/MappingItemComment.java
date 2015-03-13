package gov.va.isaac.gui.mapping.data;

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.constants.MappingConstants;
import gov.va.isaac.util.OTFUtility;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDynamicCAB;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.api.uuid.UuidT5Generator;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicUUID;

/**
 * {@link MappingItemComment}
 *
 * @author David Triglianos
 */
public class MappingItemComment {
	private String authorName;
	private Date createdDate;
	private String commentText;
	private UUID mappingItemUUID;
	
	public String getAuthorName()   { return authorName;  }
	public Date   getCreatedDate()  { return createdDate; }
	public String getCommentText()  { return commentText; }
	public UUID   getMappingItemUUID() {return mappingItemUUID; }

	public void setAuthorName(String authorName)   { this.authorName = authorName;   }
	public void setCreatedDate(Date createdDate)   { this.createdDate = createdDate; }
	public void setCommentText(String commentText) { this.commentText = commentText; }

	public MappingItemComment(UUID pMappingItemUUID, String pAuthorName, String pCommentText, Date pCreatedDate) throws IOException {
		mappingItemUUID = pMappingItemUUID;
		createdDate = pCreatedDate;
		authorName = pAuthorName;
		commentText = pCommentText;
		
		//TODO Persist comment to DB
		
	}
	
	
	public void delete() {
		//TODO implement delete/retire
	}
	
	/**
	 * 
	 * @param mappingItemUUID
	 * @return
	 * @throws IOException
	 */
	public static List<MappingItemComment> getMappingItemComments(UUID mappingItemUUID) throws IOException {
		List<MappingItemComment> comments;
		
		//try {
			//TODO retreive comments for specified Mapping Item
			comments = new ArrayList<MappingItemComment>();

		//} catch (IOException e) {
		//	throw new RuntimeException("Unexpected error", e);
		//}
		
		return comments;
	}

}
