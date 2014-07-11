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
package gov.va.isaac.gui.enhancedsearchview.filter;

import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.gui.enhancedsearchview.model.SearchResultsFilterI;
import javafx.beans.binding.BooleanExpression;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;

import org.jvnet.hk2.annotations.Contract;

/**
 * {@link FilterView}
 * 
 * The interface that serves as the basis for batch operations.  Extend this to create a new operation.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
@Contract
public abstract class FilterView
{
	protected ObservableList<SearchResultsFilterI> filterList_;

	protected GridPane root_;
	private boolean initRun = false;
	
	protected FilterView()
	{
		//For HK2 to create
	}
	
	public synchronized final void initIfNot(ObservableList<SearchResultsFilterI> filterList)
	{
		if(!initRun)
		{
			initRun = true;
			init(filterList);
		}
	}
	
	public void init(ObservableList<SearchResultsFilterI> conceptList)
	{
		root_ = new GridPane();
		root_.setMaxWidth(Double.MAX_VALUE);
		root_.setHgap(5.0);
		root_.setVgap(5.0);
		
		filterList_.addListener(new ListChangeListener<SearchResultsFilterI>()
		{
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends SearchResultsFilterI> c)
			{
				filterListChanged();
			}
		});
	}
	
	public Node getNode()
	{
		return root_;
	}
	
	/**
	 * The title of the operation, which will be displayed to the user.
	 * @return
	 */
	public abstract String getTitle();
	
	/**
	 * The description of the operation, which will be displayed to the user
	 * @return
	 */
	public abstract String getFilterDescription();
	
	/**
	 * A convenience method which will be called when the filterList changes.
	 */
	protected abstract void filterListChanged();
	
	/**
	 * Implementers should return a BooleanExpression which evaluates to true when all necessary values
	 * for task execution have been provided.
	 */
	public abstract BooleanExpression isValid();
}
