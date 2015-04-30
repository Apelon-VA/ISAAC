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
 * {@link Action}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public enum Action
{
	CHILD_OF("Child of"), SAME_AS("Same as");
	
	private String altName_;
	
	private Action(String altName)
	{
		this.altName_ = altName;
	}
	
	public static Action parse(String value)
	{
		for (Action a : Action.values())
		{
			if (a.altName_.equalsIgnoreCase(value))
			{
				return a;
			}
		}
		throw new RuntimeException("Couldn't parse action " + value);
	}
}
