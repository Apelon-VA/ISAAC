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

/**
 * {@link SelectionCriteria}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class SelectionCriteria
{
	protected Operand operand;
	protected SelectionCriteriaType type;
	protected String value;
	protected String valueId;
	/**
	 * @return the operand
	 */
	public Operand getOperand()
	{
		return operand;
	}
	/**
	 * @return the type
	 */
	public SelectionCriteriaType getType()
	{
		return type;
	}
	/**
	 * @return the value
	 */
	public String getValue()
	{
		return value;
	}
	/**
	 * @return the valueId
	 */
	public String getValueId()
	{
		return valueId;
	}
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "Operand: " + operand + ", type: " + type + ", value: " + value + ", valueId: " + valueId;
	}
}
