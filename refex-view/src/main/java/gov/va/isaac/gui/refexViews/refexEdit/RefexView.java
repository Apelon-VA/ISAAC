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

import gov.va.isaac.interfaces.gui.MenuItemI;
import gov.va.isaac.interfaces.gui.views.RefexViewI;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javax.inject.Named;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * 
 * Refset View
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */

@Service
@Named (value="RefexView")
@PerLookup
public class RefexView implements RefexViewI
{
	private int conceptNid_;

	private RefexView() throws IOException
	{
		// created by HK2
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.DockedViewI#getView()
	 */
	@Override
	public Region getView()
	{
		//TODO maybe implement a viewer for old style refexes
		return new Label("Not implemented");
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.IsaacViewI#getMenuBarMenus()
	 */
	@Override
	public List<MenuItemI> getMenuBarMenus()
	{
		// We don't currently have any custom menus with this view
		return new ArrayList<MenuItemI>();
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.RefexViewI#setComponent(int, boolean, javafx.beans.property.ReadOnlyBooleanProperty)
	 */
	@Override
	public void setComponent(int componentNid, ReadOnlyBooleanProperty showStampColumns)
	{
		this.conceptNid_ = componentNid;
		
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.RefexViewI#setAssemblage(int, boolean, javafx.beans.property.ReadOnlyBooleanProperty)
	 */
	@Override
	public void setAssemblage(int assemblageConceptNid, ReadOnlyBooleanProperty showStampColumns)
	{
		throw new RuntimeException("Not implemented");
	}
	

//	private ObservableValue<String> convertValue(RefexChronicleBI<? extends RefexAnalogBI<?>> value, int dataColumn)
//	{
//		if (value.getRefexType() == RefexType.STR)
//		{
//			RefexStringAnalogBI<?> typedValue = (RefexStringAnalogBI<?>) value;
//			if (dataColumn == 1)
//			{
//				return new ReadOnlyStringWrapper(typedValue.getString1());
//			}
//			else
//			{
//				return new ReadOnlyStringWrapper("");
//			}
//		}
//
//		else if (value.getRefexType() == RefexType.CID_STR)
//		{
//			RefexNidStringAnalogBI<?> typedValue = (RefexNidStringAnalogBI<?>) value;
//			if (dataColumn == 1)
//			{
//				return new ReadOnlyStringWrapper(typedValue.getString1());
//			}
//			else if (dataColumn == 2)
//			{
//				return new ReadOnlyStringWrapper(typedValue.getNid1() + "");
//			}
//			else
//			{
//				return new ReadOnlyStringWrapper("");
//			}
//		}
//		return new ReadOnlyStringWrapper("-Not yet supported-");
//	}
}
