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
package gov.va.isaac.isaacDbProcessingRules.spreadsheet;

import java.util.ArrayList;
import org.apache.mahout.math.Arrays;

/**
 * {@link RuleDefinition}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class RuleDefinition
{
	protected int id;
	protected long date;
	protected ArrayList<SelectionCriteria> criteria;
	protected Action action;
	protected Long sctID;
	protected String sctFSN;
	protected String author;
	protected String comments;
	
	/**
	 * @return the id
	 */
	public int getId()
	{
		return id;
	}
	/**
	 * @return the date
	 */
	public long getDate()
	{
		return date;
	}
	/**
	 * @return the criteria
	 */
	public ArrayList<SelectionCriteria> getCriteria()
	{
		return criteria;
	}
	/**
	 * @return the action
	 */
	public Action getAction()
	{
		return action;
	}
	/**
	 * @return the sctID
	 */
	public Long getSctID()
	{
		return sctID;
	}
	/**
	 * @return the sctFSN
	 */
	public String getSctFSN()
	{
		return sctFSN;
	}
	/**
	 * @return the author
	 */
	public String getAuthor()
	{
		return author;
	}
	/**
	 * @return the comments
	 */
	public String getComments()
	{
		return comments;
	}
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "ID: " + id + " Criteria: " + Arrays.toString(criteria.toArray()) + " Action: " + action.name() + " SCT FSN:" + sctFSN + " SCT ID: " + sctID;
	}
}
