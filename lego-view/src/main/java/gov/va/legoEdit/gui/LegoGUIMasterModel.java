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
package gov.va.legoEdit.gui;

import gov.va.isaac.AppContext;
import gov.va.legoEdit.gui.legoListProperties.LegoListProperties;
import gov.va.legoEdit.gui.legoListView.LegoListView;
import gov.va.legoEdit.model.LegoListByReference;
import gov.va.legoEdit.model.LegoReference;
import gov.va.legoEdit.model.schemaModel.LegoList;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import gov.va.legoEdit.storage.DataStoreException;
import gov.va.legoEdit.storage.WriteException;
import javafx.event.Event;
import javafx.scene.control.TreeItem;
import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * {@link LegoGUIMasterModel} Generally convenience methods that touch multiple other parts of the API to do things
 * like add or remove Legos.  
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Singleton
public class LegoGUIMasterModel
{
	Logger logger = LoggerFactory.getLogger(LegoGUIMasterModel.class);


	private LegoGUIMasterModel()
	{
		//created by HK2
	}

	public void updateLegoLists()
	{
		AppContext.getService(LegoListView.class).updateListView();
	}
	
	public void importLegoList(LegoList ll) throws WriteException
	{
		BDBDataStoreImpl.getInstance().importLegoList(ll);
		//TODO (artf231850) [LEGO Edit]
		//LegoGUI.getInstance().getLegoGUIController().getCommonlyUsedConcept().rebuildDBStats();
		updateLegoLists();
	}
	
	public void updateLegoList(LegoListByReference llbr, TreeItem<String> ti, String name, String description, String comments) throws DataStoreException, WriteException
	{
		BDBDataStoreImpl.getInstance().updateLegoListMetadata(llbr.getLegoListUUID(), name, description, comments);
		llbr.setGroupName(name);
		llbr.setComments(comments);
		llbr.setDescription(description);
		if (ti != null)
		{
			Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
		}
	}
	
	public void showLegoListPropertiesDialog(LegoListByReference llbr, TreeItem<String> ti)
	{
		LegoListProperties llp = AppContext.getService(LegoListProperties.class);
		llp.show(llbr, ti);
	}

	public void removeLegoList(LegoListByReference legoListByReference) throws WriteException
	{
		//TODO (artf231850) [Lego Edit]
//		for (LegoReference lr : legoListByReference.getLegoReference())
//		{
//			LegoGUI.getInstance().getLegoGUIController().closeTabIfOpen(lr);
//			// clear the new list too.
//			LegoGUI.getInstance().getLegoGUIController().removeNewLego(lr.getUniqueId());
//		}
		BDBDataStoreImpl.getInstance().deleteLegoList(legoListByReference.getLegoListUUID());
		updateLegoLists();
	}

	public void removeLego(LegoListByReference legoListReference, LegoReference legoReference) throws WriteException
	{
		//TODO (artf231850) [Lego Edit]
//		LegoGUI.getInstance().getLegoGUIController().closeTabIfOpen(legoReference);
		BDBDataStoreImpl.getInstance().deleteLego(legoListReference.getLegoListUUID(), legoReference.getLegoUUID(), legoReference.getStampUUID());
//		LegoGUI.getInstance().getLegoGUIController().removeNewLego(legoReference.getUniqueId());
		updateLegoLists();
	}
}
