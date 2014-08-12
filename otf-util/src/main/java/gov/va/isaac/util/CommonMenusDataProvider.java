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
package gov.va.isaac.util;

import gov.va.isaac.util.CommonMenus.ObjectContainer;
import javafx.beans.binding.IntegerExpression;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Implementers should override getStrings(), getNumbers(), getObjectContainers() as appropriate
 * {@link CommonMenusDataProvider}
 *
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public abstract class CommonMenusDataProvider {
	private SimpleIntegerProperty stringCount = new SimpleIntegerProperty(0);
	private SimpleIntegerProperty numberCount = new SimpleIntegerProperty(0);
	private SimpleIntegerProperty objectCount = new SimpleIntegerProperty(0);
	
	public String[] getStrings() { return null; }

	public Number[] getNumbers() { return null; }

	public ObjectContainer[] getObjectContainers() { return null; }
	
	public IntegerExpression getObservableStringCount()
	{
		return stringCount;
	}
	public IntegerExpression getObservableObjectCount()
	{
		return objectCount;
	}
	public IntegerExpression getObservableNumberCount()
	{
		return numberCount;
	}
	public void invalidateAll()
	{
		String[] s = getStrings();
		stringCount.set(s == null ? 0 : s.length);
		
		Object[] o = getObjectContainers();
		objectCount.set(o == null ? 0 : o.length);
		
		Number[] n = getNumbers();
		numberCount.set(n == null ? 0 : n.length);
	}
}