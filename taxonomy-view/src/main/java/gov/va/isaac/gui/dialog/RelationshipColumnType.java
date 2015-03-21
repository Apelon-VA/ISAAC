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
package gov.va.isaac.gui.dialog;

/**
 * {@link RelationshipColumnType}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public enum RelationshipColumnType
{
	STATUS_CONDENSED("s"),
	SOURCE("Source"),
	TYPE("Type"),
	DESTINATION("Destination"),
	UUID("UUID"),
	GROUP("Group"),
	REFINEABILITY("Refineability"),
	CHARACTERISTIC("Characteristic"),
	STATUS_STRING("Status"),
	TIME("Time"),
	AUTHOR("Author"),
	MODULE("Module"),
	PATH("Path");
	
	private String niceName_;
	
	private RelationshipColumnType(String name)
	{
		niceName_ = name;
	}

	@Override
	public String toString()
	{
		return niceName_;
	}
}
