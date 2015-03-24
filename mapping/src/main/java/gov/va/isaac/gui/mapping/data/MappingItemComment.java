package gov.va.isaac.gui.mapping.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * {@link MappingItemComment}
 *
 * @author David Triglianos
 */
public class MappingItemComment {
	private String commentText;
	private UUID mappingItemUUID;
	
	public String getAuthorName()   { return "<author>";} //TODO  DAN read from stamp   }
	public Date   getCreatedDate()  { return new Date(); } //TODO  DAN read from stamp 
	public String getCommentText()  { return commentText; }
	public UUID   getMappingItemUUID() {return mappingItemUUID; }

	public void setCommentText(String commentText) { this.commentText = commentText; }

	public MappingItemComment(UUID pMappingItemUUID, String pCommentText) throws IOException {
		mappingItemUUID = pMappingItemUUID;
		commentText = pCommentText;
		
		//TODO   DAN Persist comment to DB
		
	}
	
	
	public void delete() {
		//TODO  DAN implement delete/retire
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
			//TODO  DAN retrieve comments for specified Mapping Item
			comments = new ArrayList<MappingItemComment>();

		//} catch (IOException e) {
		//	throw new RuntimeException("Unexpected error", e);
		//}
		
		return comments;
	}

}
