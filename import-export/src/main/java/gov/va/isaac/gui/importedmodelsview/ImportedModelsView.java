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
package gov.va.isaac.gui.importedmodelsview;

import gov.va.isaac.interfaces.gui.MenuItemI;
import gov.va.isaac.interfaces.gui.views.DockedViewI;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Region;
import javafx.stage.Window;

import javax.inject.Singleton;

import org.jvnet.hk2.annotations.Service;

/**
 * A {@link DockedViewI} for displaying the "Imported Information Models" panel.
 *
 * @author ocarlsen
 */
@Service
@Singleton
public class ImportedModelsView implements DockedViewI {

    private final ImportedModelsViewController controller;

    private ImportedModelsView() throws IOException {

        // Load from FXML.
        URL resource = ImportedModelsView.class.getResource("ImportedModelsView.fxml");
        FXMLLoader loader = new FXMLLoader(resource);
        loader.load();
        controller = loader.getController();
    }

    /**
     * @see gov.va.isaac.interfaces.gui.views.DockedViewI#getView()
     */
    @Override
    public Region getView() {
        return controller.getRoot();
    }

    /**
     * @see gov.va.isaac.interfaces.gui.views.IsaacViewI#getMenuBarMenus()
     */
    @Override
    public List<MenuItemI> getMenuBarMenus() {
        // We don't currently have any custom menus with this view
        return new ArrayList<MenuItemI>();
    }

    /**
     * @see gov.va.isaac.interfaces.gui.views.DockedViewI#getMenuBarMenuToShowView()
     */
    @Override
    public MenuItemI getMenuBarMenuToShowView() {
        MenuItemI menuItem = new MenuItemI() {
            @Override
            public void handleMenuSelection(Window parent) {
                controller.setParent(parent);
            }

            @Override
            public int getSortOrder() {
                return 5;
            }

            @Override
            public String getParentMenuId() {
                return "panelsMenu";
            }

            @Override
            public String getMenuName() {
                return "Imported Information Models";
            }

            @Override
            public String getMenuId() {
                return "importedModelsPanelMenuItem";
            }

            @Override
            public boolean enableMnemonicParsing() {
                return false;
            }
        };
        return menuItem;
    }

    /**
     * @see gov.va.isaac.interfaces.gui.views.DockedViewI#getViewTitle()
     */
    @Override
    public String getViewTitle() {
        return "Imported Information Models";
    }
}
