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
package gov.va.isaac.gui;

import gov.va.isaac.gui.interfaces.DockedViewI;
import gov.va.isaac.gui.interfaces.IsaacViewI;
import gov.va.isaac.gui.interfaces.MenuItemI;
import gov.va.isaac.gui.util.FxUtils;
import java.util.Hashtable;
import java.util.TreeSet;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javax.inject.Inject;
import org.glassfish.hk2.api.IterableProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller class for {@link App}.
 *
 * @author ocarlsen
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class AppController {

    private static final Logger LOG = LoggerFactory.getLogger(AppController.class);

    @FXML private Menu importExportMenu;
    @FXML private Menu panelsMenu;
    @FXML private SplitPane mainSplitPane;
    @FXML private BorderPane appBorderPane;
    @FXML private MenuBar menuBar;

    @Inject
    private IterableProvider<IsaacViewI> moduleViews_;
    @Inject
    private IterableProvider<DockedViewI> dockedViews_;

    //Just a hashed view of all of the menus
    private final Hashtable<String, Menu> allMenus_ = new Hashtable<>();

    @FXML
    public void initialize() {

        AppContext.getServiceLocator().inject(this);

        //index these for ease in adding module menus
        for (Menu menu : menuBar.getMenus())
        {
            allMenus_.put(menu.getId(), menu);
        }

        //Sort them...
        TreeSet<MenuItemI> menusToAdd = new TreeSet<>();
        for (IsaacViewI view : moduleViews_)
        {
            for (MenuItemI menuItem : view.getMenuBarMenus())
            {
                menusToAdd.add(menuItem);
            }
        }

        for (final MenuItemI menuItemsToCreate : menusToAdd)
        {
            //TODO make an enumeration of master menu names, and put it into the interfaces module, so these don't have to be hard-coded strings...
            Menu parentMenu = allMenus_.get(menuItemsToCreate.getParentMenuId());
            if (parentMenu == null)
            {
                LOG.error("Cannot add module menu '" + menuItemsToCreate.getMenuId() + "' because the specified parent menu doesn't exist");
            }
            else
            {
                MenuItem menuItem = new MenuItem();
                menuItem.setId(menuItemsToCreate.getMenuId());
                menuItem.setText(menuItemsToCreate.getMenuName());
                menuItem.setMnemonicParsing(menuItemsToCreate.enableMnemonicParsing());
                menuItem.setOnAction(new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent arg0)
                    {
                        menuItemsToCreate.handleMenuSelection(appBorderPane.getScene().getWindow());
                    }
                });
                parentMenu.getItems().add(menuItem);
            }
        }
        
        for (final DockedViewI dv : dockedViews_)
        {
            try
            {
                Menu parentMenu = allMenus_.get(dv.getMenuBarMenuToShowView().getParentMenuId());
                if (parentMenu == null)
                {
                    LOG.error("Cannot add module menu '" + dv.getMenuBarMenuToShowView().getMenuId() + "' because the specified parent menu doesn't exist");
                }
                else
                {
                    final BorderPane bp = buildPanelForView(dv);
                    //TODO this isn't honoring sort order... need to sort all of the menus from the DockedViewI at once....
                    MenuItem mi = new MenuItem();
                    mi.setText(dv.getMenuBarMenuToShowView().getMenuName());
                    mi.setId(dv.getMenuBarMenuToShowView().getMenuId());
                    mi.setMnemonicParsing(dv.getMenuBarMenuToShowView().enableMnemonicParsing());
                    mi.setOnAction(new EventHandler<ActionEvent>()
                    {
                        @Override
                        public void handle(ActionEvent arg0)
                        {
                            //This is a convenience call... not expected to actually show the view.
                            dv.getMenuBarMenuToShowView().handleMenuSelection(appBorderPane.getScene().getWindow());

                            if (!mainSplitPane.getItems().contains(bp))
                            {
                                bp.setVisible(true);
                                mainSplitPane.getItems().add(bp);
                            }
                        }

                    });
                    mi.disableProperty().bind(bp.visibleProperty());
                    parentMenu.getItems().add(mi);
                }
            }
            catch (Exception e)
            {
                LOG.error("Unexpected error configuring DockedViewI " + (dv == null ? "?" : dv.getViewTitle()), e);
            }
        }
    }

    public void finishInit() {
        // Make sure in application thread.
        FxUtils.checkFxUserThread();

        // Enable the menus.
        importExportMenu.setDisable(false);
        panelsMenu.setDisable(false);
    }



    private BorderPane buildPanelForView(DockedViewI dockedView)
    {
        final BorderPane bp = new BorderPane();
        bp.setVisible(false);
        AnchorPane ap = new AnchorPane();
        ap.getStyleClass().add("headerBackground");

        Label l = new Label(dockedView.getViewTitle());
        AnchorPane.setLeftAnchor(l, 5.0);
        AnchorPane.setTopAnchor(l, 5.0);
        ap.getChildren().add(l);

        Button b = new Button();
        b.setMnemonicParsing(false);
        b.setStyle("-fx-cursor:hand");
        b.getStyleClass().add("tab-close-button");
        b.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                hidePanelView(bp);
            }
        });
        AnchorPane.setTopAnchor(b, 5.0);
        AnchorPane.setRightAnchor(b, 3.0);
        ap.getChildren().add(b);

        bp.setTop(ap);
        bp.setCenter(dockedView.getView());
        return bp;
    }

    private void hidePanelView(BorderPane bp)
    {
        bp.setVisible(false);
        mainSplitPane.getItems().remove(bp);
    }
}
