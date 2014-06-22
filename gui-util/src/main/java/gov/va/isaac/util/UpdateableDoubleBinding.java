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

import javafx.beans.Observable;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.sun.javafx.binding.BindingHelperObserver;

/**
 * {@link UpdateableDoubleBinding}
 * 
 * No idea why DoubleBinding has these variations of these methods that are protected and final... 
 * And the remove was implemented in such a way that you can't remove individual items.
 * (because they nulled themselves after a remove).  Copied code here, fixed to allow individual 
 * removals.
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public abstract class UpdateableDoubleBinding extends DoubleBinding
{
	private BindingHelperObserver observer;
	private ObservableList<Observable> listeningTo = FXCollections.observableArrayList();
	
	public final void addBinding(Observable... dependencies)
	{
		if ((dependencies != null) && (dependencies.length > 0))
		{
			if (observer == null)
			{
				observer = new BindingHelperObserver(this);
			}
			for (final Observable dep : dependencies)
			{
				dep.addListener(observer);
				listeningTo.add(dep);
			}
			invalidate();
		}
	}

	/**
	 * Stop observing the dependencies for changes.
	 * 
	 * @param dependencies
	 * the dependencies to stop observing
	 */
	public final void removeBinding(Observable... dependencies)
	{
		if (observer != null)
		{
			for (final Observable dep : dependencies)
			{
				dep.removeListener(observer);
				listeningTo.remove(dep);
			}
			if (listeningTo.size() == 0)
			{
				observer = null;
			}
			invalidate();
		}
	}
	
	public final void clearBindings()
	{
		while (listeningTo.size() > 0)
		{
			removeBinding(listeningTo.iterator().next());
		}
	}

	/**
	 * Currently registered bindings
	 * @see javafx.beans.binding.DoubleBinding#getDependencies()
	 */
	@Override
	public ObservableList<?> getDependencies()
	{
		return listeningTo;
	}
}
