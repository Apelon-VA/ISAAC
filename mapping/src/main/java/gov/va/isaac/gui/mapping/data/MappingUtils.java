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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.gui.mapping.data;

import gov.va.isaac.constants.MappingConstants;
import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.search.SearchHandle;
import gov.va.isaac.search.SearchHandler;
import gov.va.isaac.util.OTFUtility;
import gov.va.isaac.util.TaskCompleteCallback;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MappingUtils}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class MappingUtils
{
	//TODO DAN write methods for description search criteria
	protected static final Logger LOG = LoggerFactory.getLogger(MappingUtils.class);
	
	public static List<SimpleDisplayConcept> getStatusConcepts() throws IOException
	{
		ArrayList<SimpleDisplayConcept> result = new ArrayList<>();
		try
		{
			for (ConceptVersionBI cv : OTFUtility.getAllChildrenOfConcept(MappingConstants.MAPPING_STATUS.getNid(), true))
			{
				result.add(new SimpleDisplayConcept(cv));
			}
		}
		catch (ContradictionException e)
		{
			LOG.error("Unexpected", e);
			throw new IOException("Unexpected error");
		}
		
		return result;
	}
	
	public static List<SimpleDisplayConcept> getQualifierConcepts() throws IOException
	{
		ArrayList<SimpleDisplayConcept> result = new ArrayList<>();
		try
		{
			for (ConceptVersionBI cv : OTFUtility.getAllChildrenOfConcept(MappingConstants.MAPPING_QUALIFIERS.getNid(), true))
			{
				result.add(new SimpleDisplayConcept(cv));
			}
		}
		catch (ContradictionException e)
		{
			LOG.error("Unexpected", e);
			throw new IOException("Unexpected error");
		}
		
		return result;
	}
	
	public static SearchHandle search(String searchString, TaskCompleteCallback callback) throws IOException
	{
		return SearchHandler.descriptionSearch(searchString, Integer.MAX_VALUE, callback, true);
	}
}
