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
package gov.va.isaac.gui.refexViews.refexEdit;

/**
 * {@link DynamicRefexColumnType}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public enum DynamicRefexColumnType
{
	STATUS_CONDENSED("s"), COMPONENT("Component"), ASSEMBLAGE("Assemblage"), STATUS_STRING("Status"), TIME("Time"), AUTHOR("Author"),
	MODULE("Module"), PATH("Path"), ATTACHED_DATA("Attached Data");
	
	private String niceName_;
	
	private DynamicRefexColumnType(String name)
	{
		niceName_ = name;
	}

	@Override
	public String toString()
	{
		return niceName_;
	}
}
