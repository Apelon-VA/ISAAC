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
package gov.va.isaac.gui.conceptViews;

import gov.va.isaac.AppContext;
import gov.va.isaac.config.profiles.UserProfileManager;
import gov.va.isaac.init.SystemInit;
import gov.va.isaac.interfaces.gui.constants.SharedServiceNames;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.PopupConceptViewI;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.UUID;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.ihtsdo.otf.query.lucene.LuceneIndexer;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;

/**
 * EnhancedConceptViewRunner
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 * @author <a href="mailto:jefron@apelon.com">Jesse Efron</a>
 */
public class EnhancedConceptViewRunner extends Application
{
	/**
	 * @see javafx.application.Application#start(javafx.stage.Stage)
	 */
	@Override
	public void start(Stage primaryStage) throws Exception
	{
		primaryStage.setTitle("New Concept Panel");

		PopupConceptViewI cv = AppContext.getService(PopupConceptViewI.class, SharedServiceNames.MODERN_STYLE);

		primaryStage.setScene(new Scene(new Label("hello world"), 200, 100));
		primaryStage.show();
		cv.setConcept(UUID.fromString("49064bb7-cda5-3cb3-b8f7-085139486fa8"));
		cv.showView(primaryStage);
	}

	public static void main(String[] args) throws Exception
	{
		IOException dataStoreLocationInitException = SystemInit.doBasicSystemInit(new File("../../ISAAC-PA-VA-Fork/app/"));
		if (dataStoreLocationInitException != null)
		{
			System.err.println("Configuration of datastore path failed.  DB will not be able to start properly!  " + dataStoreLocationInitException);
			System.exit(-1);
		}
		
		AppContext.getService(UserProfileManager.class).configureAutomationMode(new File("profiles"));
		
		launch(args);
	}

}
