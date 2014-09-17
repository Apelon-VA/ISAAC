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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import javafx.beans.binding.IntegerExpression;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * {@link CommonMenusTaskIdProvider}
 *
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
public abstract class CommonMenusTaskIdProvider
{
	private static final CommonMenusTaskIdProvider emptyCommonMenusTaskIdProvider =  new CommonMenusTaskIdProvider() {
		private final Collection<Long> collection = Collections.unmodifiableCollection(new HashSet<>());
		
		@Override
		public Collection<Long> getTaskIds() {
			return collection;
		}
	};
	public static CommonMenusTaskIdProvider getEmptyCommonMenusTaskIdProvider() { return emptyCommonMenusTaskIdProvider; }

	SimpleIntegerProperty taskIdCount = new SimpleIntegerProperty(0);

	public abstract Collection<Long> getTaskIds();

	public IntegerExpression getObservableTaskIdCount()
	{
		return taskIdCount;
	}

	public void invalidateAll()
	{
		Collection<Long> taskIds = getTaskIds();
		taskIdCount.set(taskIds == null ? 0 : taskIds.size());
	}
}
