package gov.va.isaac.gui.mapping.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappingItemCommentDAO {

	private static final Logger LOG = LoggerFactory.getLogger(MappingItemCommentDAO.class);

	public static List<MappingItemComment> getComments(UUID mappingItemUUID) {
		List<MappingItemComment> comments = new ArrayList<MappingItemComment>();
		
		//TODO  DAN implement get comments for mapping item, populate comments list
		
		return comments;
	}
}
