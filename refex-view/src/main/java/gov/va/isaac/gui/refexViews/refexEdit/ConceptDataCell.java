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
package gov.va.isaac.gui.refexViews.refexEdit;

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.util.CommonMenus;
import gov.va.isaac.util.CommonMenusNIdProvider;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.WBUtility;
import java.util.Arrays;
import java.util.Collection;
import javafx.concurrent.Task;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TreeTableCell;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ConceptDataCell}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class ConceptDataCell extends TreeTableCell<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>, Integer>
{
	private static Logger logger_ = LoggerFactory.getLogger(ConceptDataCell.class);
	

	/**
	 * @see javafx.scene.control.Cell#updateItem(java.lang.Object, boolean)
	 */
	@Override
	protected void updateItem(Integer item, boolean empty)
	{
		super.updateItem(item, empty);
		
		if (empty || item == null)
		{
			setText("");
			setGraphic(null);
		}
		else if (item != null)
		{
			conceptLookup(item);
			
		}
	}
	
	private void conceptLookup(int nid)
	{
		setGraphic(new ProgressBar());
		setText(null);
		Task<Void> t = new Task<Void>()
		{
			String text;
			ContextMenu cm = new ContextMenu();
			
			@Override
			protected Void call() throws Exception
			{
				try
				{
					ConceptVersionBI c = WBUtility.getConceptVersion(nid);
					if (c == null) 
					{
						//This may be a different component - like a description, or another refex... need to handle.
						ComponentVersionBI cv = ExtendedAppContext.getDataStore().getComponentVersion(WBUtility.getViewCoordinate(), nid);
						System.out.println("The component type " + cv + " is not handled yet!");
						//TODO implement
						text = cv.toUserString();
					}
					else
					{
						CommonMenus.addCommonMenus(cm, new CommonMenusNIdProvider()
						{
							
							@Override
							public Collection<Integer> getNIds()
							{
								return Arrays.asList(new Integer[] {nid});
							}
						});
						text = WBUtility.getDescription(c);
					}
				}
				catch (Exception e)
				{
					logger_.error("Unexpected error", e);
					text= "-ERROR-";
				}
				return null;
			}

			/**
			 * @see javafx.concurrent.Task#succeeded()
			 */
			@Override
			protected void succeeded()
			{
				setText(text);
				if (cm.getItems().size() > 0)
				{
					setContextMenu(cm);
				}
				setGraphic(null);
			}
		};
		Utility.execute(t);
	}
}
