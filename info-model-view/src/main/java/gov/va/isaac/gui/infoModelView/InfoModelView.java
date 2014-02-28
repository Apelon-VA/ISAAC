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
package gov.va.isaac.gui.infoModelView;

import gov.va.isaac.AppContext;
import gov.va.isaac.interfaces.gui.MenuItemI;
import gov.va.isaac.interfaces.gui.views.InfoModelViewI;
import gov.va.isaac.interfaces.gui.views.PopupViewI;
import gov.va.isaac.interfaces.gui.views.RefsetViewI;
import gov.va.isaac.models.cem.importer.CEMMetadataBinding;
import gov.va.isaac.util.WBUtility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.glassfish.hk2.api.PerLookup;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.jvnet.hk2.annotations.Service;

/**
 * 
 * Refset View
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */

@Service
@PerLookup
public class InfoModelView implements PopupViewI, InfoModelViewI
{
	private Stage stage;
	private VBox root = new VBox();
	private VBox refsetArea = new VBox();
	private Button configure = new Button("Configure...");
	private Popup popup = new Popup();
	private HashMap<String, Node> refsetsOnDisplay = new HashMap<>();
	private UUID conceptUUID;
	
	private InfoModelView() throws IOException
	{
		// created by HK2
		stage = new Stage();
		stage.initModality(Modality.NONE);
		stage.initStyle(StageStyle.UTILITY);
		stage.setScene(new Scene(root, 800, 600));
		stage.getScene().getStylesheets().add("/Style.css");
		stage.setTitle("Info Model View");
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.InfoModelViewI#setConcept(java.util.UUID)
	 */
	@Override
	public void setConcept(UUID conceptID)
	{
		this.conceptUUID = conceptID;
		
		HBox h = new HBox();
		h.getStyleClass().add("itemBorder");
		
		h.getChildren().add(new Label("Clinical Element Model"));
		Label l = new Label(WBUtility.getDescription(conceptUUID));
		HBox.setMargin(l, new Insets(0, 0, 0, 10));
		h.getChildren().add(l);
		
		configure.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				if (popup.isShowing())
				{
					popup.hide();
				}
				else
				{
					popup.show(stage, stage.getScene().getWindow().getX() + 220, stage.getScene().getWindow().getY() + 60);
				}
				
			}
		});
		HBox.setMargin(configure, new Insets(0, 10, 0, 10));
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		h.getChildren().add(spacer);
		h.getChildren().add(configure);
	
		BorderPane bp = new BorderPane();
		bp.getStyleClass().add("itemBorder");
		bp.setTop(new Label("Select Desired Model Elements"));
		BorderPane.setMargin(bp.getTop(), new Insets(5, 5, 5, 5));
		
		final ListView<String> list = new ListView<String>();
		list.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		for (ConceptSpec cs : CEMMetadataBinding.getAllRefsets())
		{
			list.getItems().add(cs.getDescription());
		}
		
		list.setPrefHeight(400);
		list.setPrefWidth(500);
		bp.setCenter(list);
		bp.setStyle("-fx-background-color: gainsboro");
		
		popup.getContent().add(bp);
		Button ok = new Button("Ok");
		ok.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				popup.hide();
			}
		});
		BorderPane.setMargin(ok, new Insets(5, 5, 5, 5));
		BorderPane.setAlignment(ok, Pos.CENTER);
		bp.setBottom(ok);
		
		
		root.getChildren().add(h);
		ScrollPane sp = new ScrollPane();
		sp.setPrefWidth(Double.MAX_VALUE);
		refsetArea.setFillWidth(true);
		sp.setContent(refsetArea);
		VBox.setVgrow(sp, Priority.ALWAYS);
		root.getChildren().add(sp);

		for (ConceptSpec cs : CEMMetadataBinding.getAllRefsets())
		{
			if (cs == CEMMetadataBinding.CEM_DATA_REFSET || cs == CEMMetadataBinding.CEM_TYPE_REFSET)
			{
				add(cs);
				list.getSelectionModel().select(cs.getDescription());
			}
		}
		
		list.getSelectionModel().getSelectedIndices().addListener(new ListChangeListener<Integer>()
		{
			@Override
			public void onChanged(Change<? extends Integer> c)
			{
				while (c.next())
				{
					for (Integer i : c.getRemoved())
					{
						if (i >= 0)
						{
							removeRefsetView(list.getItems().get(i));
						}
					}
					
					for (Integer i : c.getAddedSubList())
					{
						if (i >= 0)
						{
							addRefsetView(list.getItems().get(i));
						}
					}
				}
			}
		});
	}
	
	private void addRefsetView(String name)
	{
		for (ConceptSpec cs : CEMMetadataBinding.getAllRefsets())
		{
			if (cs.getDescription().equals(name))
			{
				add(cs);
				break;
			}
		}
	}
	
	private void removeRefsetView(String name)
	{
		refsetArea.getChildren().remove(refsetsOnDisplay.get(name));
	}
	
	private Region getRefsetView(ConceptSpec refset)
	{
		RefsetViewI rv = AppContext.getService(RefsetViewI.class);
		
		rv.setRefsetAndComponent(refset.getUuids()[0], conceptUUID);
		Region r =  rv.getView();
		r.getStyleClass().add("itemBorder");
		r.setPrefHeight(300);
		r.prefWidthProperty().bind(root.widthProperty().subtract(20));
		return r;
	}
	
	protected void add(ConceptSpec refset)
	{
		Node n = getRefsetView(refset);
		refsetsOnDisplay.put(refset.getDescription(), n);
		refsetArea.getChildren().add(n);
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
	 * @see gov.va.isaac.interfaces.gui.views.PopupViewI#showView(javafx.stage.Window)
	 */
	@Override
	public void showView(Window parent)
	{
		if (conceptUUID == null)
		{
			throw new RuntimeException("Must call setConcept(...) first");
		}
		stage.initOwner(parent);
		stage.show();
		
	}
}