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
package gov.va.isaac.gui.classifier;

import gov.va.isaac.AppContext;
import gov.va.isaac.interfaces.gui.ApplicationMenus;
import gov.va.isaac.interfaces.gui.MenuItemI;
import gov.va.isaac.interfaces.gui.views.IsaacViewWithMenusI;
import gov.va.isaac.interfaces.gui.views.PopupViewI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.stage.Window;

import javax.inject.Singleton;

import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classifier view for actions menu
 * 
 * @author bcarlsenca
 */
@Service
@Singleton
@SuppressWarnings("restriction")
public class ClassifierDialogView implements PopupViewI, IsaacViewWithMenusI {

  /** The log. */
  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.va.isaac.interfaces.gui.views.IsaacViewWithMenusI#getMenuBarMenus()
   */
  @Override
  public List<MenuItemI> getMenuBarMenus() {
    ArrayList<MenuItemI> menus = new ArrayList<>();
    menus.add(new MenuItemI() {
      @Override
      public void handleMenuSelection(Window parent) {
        showView(parent);
      }

      @Override
      public int getSortOrder() {
        return 4;
      }

      @Override
      public String getParentMenuId() {
        return ApplicationMenus.ACTIONS.getMenuId();
      }

      @Override
      public String getMenuName() {
        return "Run Classifier";
      }

      @Override
      public String getMenuId() {
        return "runClassifierMenuItem";
      }

      @Override
      public boolean enableMnemonicParsing() {
        return false;
      }
    });
    return menus;
  }

  /**
   * Show view.
   *
   * @param parent the parent
   * @see gov.va.isaac.interfaces.gui.views.PopupViewI#showView(javafx.stage.Window)
   */
  @Override
  public void showView(Window parent) {
    try {
      ClassifierDialog dialog = new ClassifierDialog(parent);
      dialog.show();
    } catch (IOException ex) {
      String title = ex.getClass().getName();
      String msg = String.format("Unexpected error showing ClassifierDialog");
      LOG.error(msg, ex);
      AppContext.getCommonDialogs()
          .showErrorDialog(title, msg, ex.getMessage());
    }
  }
}
