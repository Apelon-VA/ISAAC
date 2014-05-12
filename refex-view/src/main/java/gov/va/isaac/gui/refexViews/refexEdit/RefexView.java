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
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.util.Callback;
import org.glassfish.hk2.api.PerLookup;
import org.ihtsdo.otf.tcc.api.refex.RefexAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.type_nid_string.RefexNidStringAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_string.RefexStringAnalogBI;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_string.NidStringMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_string.StringMember;
import org.jvnet.hk2.annotations.Service;

/**
 * 
 * Refset View
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */

@Service
@PerLookup
public class RefexView implements RefexViewI
{

	private Label l;
	
	private RefexView() throws IOException
	{
		// created by HK2
		l = new Label("I show a refset!");
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.DockedViewI#getView()
	 */
	@Override
	public Region getView() 
	{
		
		try
		{
			BorderPane bp = new BorderPane();
			HBox h = new HBox();
			h.getChildren().add(new Label("Concept: "));
			h.getChildren().add(l);
			h.getStyleClass().add("itemBorder");
			
			bp.setTop(h);
			
			TreeTableView<RefexChronicleBI<? extends RefexAnalogBI<?>>> ttv = new TreeTableView<>();
			
			ttv.setTableMenuButtonVisible(true);
			
			TreeTableColumn<RefexChronicleBI<? extends RefexAnalogBI<?>>, String> col1 = new TreeTableColumn<>();
			col1.setText("refset");
			col1.setSortable(true);
			col1.setResizable(true);
			col1.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<RefexChronicleBI<? extends RefexAnalogBI<?>>,String>, ObservableValue<String>>()
			{
				@Override
				public ObservableValue<String> call(CellDataFeatures<RefexChronicleBI<? extends RefexAnalogBI<?>>, String> param)
				{
					return new ReadOnlyStringWrapper(param.getValue().getValue().getPrimordialUuid().toString());
				}
			});
			
			TreeTableColumn<RefexChronicleBI<? extends RefexAnalogBI<?>>, String> col2 = new TreeTableColumn<>();
			col2.setText("Value");
			col2.setSortable(true);
			col2.setResizable(true);
			
			TreeTableColumn<RefexChronicleBI<? extends RefexAnalogBI<?>>, String> col2Nest1 = new TreeTableColumn<>();
			col2Nest1.setText("Value 1");
			col2Nest1.setSortable(true);
			col2Nest1.setResizable(true);
			col2Nest1.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<RefexChronicleBI<? extends RefexAnalogBI<?>>,String>, ObservableValue<String>>()
			{
				@Override
				public ObservableValue<String> call(CellDataFeatures<RefexChronicleBI<? extends RefexAnalogBI<?>>, String> param)
				{
					return convertValue(param.getValue().getValue(), 1);
				}
			});
			col2.getColumns().add(col2Nest1);
			
			TreeTableColumn<RefexChronicleBI<? extends RefexAnalogBI<?>>, String> col2Nest2 = new TreeTableColumn<>();
			col2Nest2.setText("Value 2");
			col2Nest2.setSortable(true);
			col2Nest2.setResizable(true);
			
			col2Nest2.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<RefexChronicleBI<? extends RefexAnalogBI<?>>,String>, ObservableValue<String>>()
			{
				@Override
				public ObservableValue<String> call(CellDataFeatures<RefexChronicleBI<? extends RefexAnalogBI<?>>, String> param)
				{
					return convertValue(param.getValue().getValue(), 2);
				}
			});
			col2.getColumns().add(col2Nest2);

			ttv.getColumns().add(col1);
			ttv.getColumns().add(col2);
			
			StringMember sm = new StringMember();
			sm.setPrimordialUuid(UUID.randomUUID());
			sm.setAssemblageNid(5);
			//sm.setAuthorNid(3);
			sm.setString1("hi there");
			
			TreeItem<RefexChronicleBI<? extends RefexAnalogBI<?>>> root = new TreeItem<>();
			root.setExpanded(true);
			ttv.setShowRoot(false);
			ttv.setRoot(root);
			
			TreeItem<RefexChronicleBI<? extends RefexAnalogBI<?>>> child = new TreeItem<>();
			child.setValue(sm);
			root.getChildren().add(child);

			NidStringMember nsm = new NidStringMember();
			nsm.setNid1(456);
			nsm.setString1("fred");
			TreeItem<RefexChronicleBI<? extends RefexAnalogBI<?>>> child2 = new TreeItem<>();
			child2.setValue(nsm);
			root.getChildren().add(child2);
			
			StringMember nested = new StringMember();
			nested.setPrimordialUuid(UUID.randomUUID());
			nested.setString1("A Nested Refex!");
			TreeItem<RefexChronicleBI<? extends RefexAnalogBI<?>>> childNested = new TreeItem<>();
			childNested.setValue(nested);
			child.getChildren().add(childNested);
			
			
			bp.setCenter(ttv);
			return bp;
		}
		catch (PropertyVetoException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
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
	 * @see gov.va.isaac.interfaces.gui.views.RefexViewI#setRefset(java.util.UUID)
	 */
	@Override
	public void setRefset(UUID conceptUUID)
	{
		// TODO Auto-generated method stub
		System.out.println("Refset passed in: " + conceptUUID);
		l.setText("My refset was set to " + conceptUUID);
	}
	
	private ObservableValue<String> convertValue(RefexChronicleBI<? extends RefexAnalogBI<?>> value, int dataColumn)
	{
		if (value.getRefexType() == RefexType.STR)
		{
			RefexStringAnalogBI<?> typedValue = (RefexStringAnalogBI<?>)value;
			if (dataColumn == 1)
			{
				return new ReadOnlyStringWrapper(typedValue.getString1());
			}
			else
			{
				return new ReadOnlyStringWrapper("");
			}
		}
		
		else if (value.getRefexType() == RefexType.CID_STR)
		{
			RefexNidStringAnalogBI<?> typedValue = (RefexNidStringAnalogBI<?>)value;
			if (dataColumn == 1)
			{
				return new ReadOnlyStringWrapper(typedValue.getString1());
			}
			else if (dataColumn == 2)
			{
				return new ReadOnlyStringWrapper(typedValue.getNid1() + "");
			}
			else
			{
				return new ReadOnlyStringWrapper("");
			}
		}
		return new ReadOnlyStringWrapper("-Not yet supported-");
	}
}
