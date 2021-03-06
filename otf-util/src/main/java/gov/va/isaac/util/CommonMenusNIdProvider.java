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
 * {@link CommonMenusNIdProvider}
 *
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public abstract class CommonMenusNIdProvider
{
	private static final CommonMenusNIdProvider emptyCommonMenusNIdProvider =  new CommonMenusNIdProvider() {
		private final Collection<Integer> collection = Collections.unmodifiableCollection(new HashSet<>());
		
		@Override
		public Collection<Integer> getNIds() {
			return collection;
		}
	};
	public static CommonMenusNIdProvider getEmptyCommonMenusNIdProvider() { return emptyCommonMenusNIdProvider; }

	SimpleIntegerProperty nidCount = new SimpleIntegerProperty(0);

	public abstract Collection<Integer> getNIds();

	public IntegerExpression getObservableNidCount()
	{
		return nidCount;
	}

	public void invalidateAll()
	{
		Collection<Integer> nids = getNIds();
		nidCount.set(nids == null ? 0 : nids.size());
	}
}
