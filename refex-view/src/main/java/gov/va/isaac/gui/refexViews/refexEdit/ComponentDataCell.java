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

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.gui.ConfigureDynamicRefexIndexingView;
import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.gui.dragAndDrop.DragRegistry;
import gov.va.isaac.gui.dragAndDrop.SingleConceptIdProvider;
import gov.va.isaac.gui.refexViews.dynamicRefexListView.referencedItemsView.DynamicReferencedItemsView;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.util.CommonMenuBuilderI;
import gov.va.isaac.util.CommonMenus;
import gov.va.isaac.util.CommonMenus.CommonMenuItem;
import gov.va.isaac.util.CommonMenusNIdProvider;
import gov.va.isaac.util.OTFUtility;
import gov.va.isaac.util.Utility;
import java.util.Arrays;
import java.util.Collection;
import javafx.concurrent.Task;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TreeTableCell;
import javafx.scene.text.Text;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ComponentDataCell}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class ComponentDataCell extends TreeTableCell<RefexDynamicGUI, RefexDynamicGUI>
{
	private static Logger logger_ = LoggerFactory.getLogger(ComponentDataCell.class);
	
	private DynamicRefexColumnType type_;
	
	protected ComponentDataCell(DynamicRefexColumnType type)
	{
		type_ = type;
	}

	/**
	 * @see javafx.scene.control.Cell#updateItem(java.lang.Object, boolean)
	 */
	@Override
	protected void updateItem(RefexDynamicGUI item, boolean empty)
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
	
	private void conceptLookup(RefexDynamicGUI item)
	{
		ProgressBar pb = new ProgressBar();
		pb.setMaxWidth(Double.MAX_VALUE);
		setGraphic(pb);
		
		setText(null);
		Task<Void> t = new Task<Void>()
		{
			String text;
			boolean setStyle = false;
			boolean configureDragAndDrop = false;
			ContextMenu cm = new ContextMenu();
			int nid = item.getNidFetcher(type_, null).applyAsInt(item.getRefex());
			
			@Override
			protected Void call() throws Exception
			{
				try
				{
					text = item.getDisplayStrings(type_, null).getKey();
					ConceptVersionBI c = OTFUtility.getConceptVersion(nid);
					if (c == null) 
					{
						//This may be a different component - like a description, or another refex... need to handle.
						ComponentVersionBI cv = ExtendedAppContext.getDataStore().getComponentVersion(OTFUtility.getViewCoordinate(), nid);
						
						if (cv instanceof DescriptionVersionBI<?>)
						{
							DescriptionVersionBI<?> dv = (DescriptionVersionBI<?>) cv;
							CommonMenuBuilderI menuBuilder = CommonMenus.CommonMenuBuilder.newInstance();
							menuBuilder.setMenuItemsToExclude(CommonMenuItem.COPY_SCTID);
							
							CommonMenus.addCommonMenus(cm, menuBuilder, new CommonMenusNIdProvider()
							{
								
								@Override
								public Collection<Integer> getNIds()
								{
									return Arrays.asList(new Integer[] {dv.getNid()});
								}
							});
						}
						else if (cv instanceof RelationshipVersionBI<?>)
						{
							RelationshipVersionBI<?> rv = (RelationshipVersionBI<?>) cv;
							
							CommonMenuBuilderI menuBuilder = CommonMenus.CommonMenuBuilder.newInstance();
							menuBuilder.setMenuItemsToExclude(CommonMenuItem.COPY_SCTID);
							
							CommonMenus.addCommonMenus(cm, menuBuilder, new CommonMenusNIdProvider()
							{
								
								@Override
								public Collection<Integer> getNIds()
								{
									return Arrays.asList(new Integer[] {rv.getNid()});
								}
							});
						}
						else if (cv instanceof RefexDynamicVersionBI<?>)
						{
							RefexDynamicVersionBI<?> rdv = (RefexDynamicVersionBI<?>) cv;
							CommonMenuBuilderI menuBuilder = CommonMenus.CommonMenuBuilder.newInstance();
							menuBuilder.setMenuItemsToExclude(CommonMenuItem.COPY_SCTID);
							
							CommonMenus.addCommonMenus(cm, menuBuilder, new CommonMenusNIdProvider()
							{
								
								@Override
								public Collection<Integer> getNIds()
								{
									return Arrays.asList(new Integer[] {rdv.getNid()});
								}
							});
						}
						else if (cv instanceof RefexVersionBI<?>)
						{
							RefexVersionBI<?> rv = (RefexVersionBI<?>) cv;
							
							CommonMenuBuilderI menuBuilder = CommonMenus.CommonMenuBuilder.newInstance();
							menuBuilder.setMenuItemsToExclude(CommonMenuItem.COPY_SCTID);
							
							CommonMenus.addCommonMenus(cm, menuBuilder, new CommonMenusNIdProvider()
							{
								
								@Override
								public Collection<Integer> getNIds()
								{
									return Arrays.asList(new Integer[] {rv.getNid()});
								}
							});
						}
					}
					else
					{
						if (DynamicRefexColumnType.ASSEMBLAGE == type_)
						{
							MenuItem mi = new MenuItem("View Sememe Assemblage Usage");
							mi.setOnAction((action) ->
							{
								SimpleDisplayConcept sdc = new SimpleDisplayConcept(c);
								DynamicReferencedItemsView driv = new DynamicReferencedItemsView(sdc);
								driv.showView(null);
							});
							mi.setGraphic(Images.SEARCH.createImageView());
							cm.getItems().add(mi);
							
							mi = new MenuItem("Configure Sememe Indexing");
							mi.setOnAction((action) ->
							{
								new ConfigureDynamicRefexIndexingView(c).showView(null);
							});
							mi.setGraphic(Images.CONFIGURE.createImageView());
							cm.getItems().add(mi);
						}
						
						configureDragAndDrop = true;

						CommonMenus.addCommonMenus(cm, new CommonMenusNIdProvider()
						{
							@Override
							public Collection<Integer> getNIds()
							{
								return Arrays.asList(new Integer[] {item.getNidFetcher(type_, null).applyAsInt(item.getRefex())});
							}
						});
						setStyle = true;
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
				//default text is a label, which doesn't wrap properly.
				setText(null);
				Text textHolder = new Text(text);
				textHolder.wrappingWidthProperty().bind(widthProperty().subtract(10));
				if (cm.getItems().size() > 0)
				{
					setContextMenu(cm);
				}
				setGraphic(textHolder);
				if (setStyle)
				{
					if (item.isCurrent())
					{
						getTreeTableRow().getStyleClass().removeAll("historical");
					}
					else
					{
						if (!getTreeTableRow().getStyleClass().contains("historical"))
						{
							getTreeTableRow().getStyleClass().add("historical");
						}
					}
				}
				if (configureDragAndDrop)
				{
					AppContext.getService(DragRegistry.class).setupDragOnly(textHolder, new SingleConceptIdProvider()
					{
						@Override
						public String getConceptId()
						{
							return nid + "";
						}
					});
				}
			}
		};
		Utility.execute(t);
	}
}
