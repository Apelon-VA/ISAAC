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
package gov.va.isaac.gui;

import java.util.Comparator;

/**
 * {@link SimpleDisplayConceptComparator}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class SimpleDisplayConceptComparator implements Comparator<SimpleDisplayConcept>
{
	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(SimpleDisplayConcept o1, SimpleDisplayConcept o2)
	{
		if (o1 == null && o2 == null)
		{
			return 0;
		}
		else if (o1 == null)
		{
			return 1;
		}
		else if (o2 ==  null)
		{
			return -1;
		}
		else if (o1.getDescription() == null && o2.getDescription() == null)
		{
			return 0;
		}
		else if (o1.getDescription() == null)
		{
			return -1;
		}
		else if (o2.getDescription() == null)
		{
			return 1;
		}
		else
		{
			return o1.getDescription().compareTo(o2.getDescription());
		}
	}
}
