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
package gov.va.isaac.gui.mapping;

import gov.va.isaac.AppContext;
import gov.va.isaac.config.profiles.UserProfileManager;
import gov.va.isaac.init.SystemInit;
import java.io.File;
import java.io.IOException;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * EnhancedConceptViewRunner
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 * @author <a href="mailto:jefron@apelon.com">Jesse Efron</a>
 */
public class MappingViewRunner extends Application
{
	/**
	 * @see javafx.application.Application#start(javafx.stage.Stage)
	 */
	@Override
	public void start(Stage primaryStage) throws Exception
	{
		

		Mapping m = AppContext.getService(Mapping.class);

		primaryStage.setScene(new Scene(m.getView(), 800, 600));
		primaryStage.setTitle(m.getViewTitle());
		primaryStage.show();
	}

	public static void main(String[] args) throws Exception
	{
		IOException dataStoreLocationInitException = SystemInit.doBasicSystemInit(new File("../../isaac-pa/app/"));
		if (dataStoreLocationInitException != null)
		{
			System.err.println("Configuration of datastore path failed.  DB will not be able to start properly!  " + dataStoreLocationInitException);
			System.exit(-1);
		}
		AppContext.getService(UserProfileManager.class).configureAutomationMode(new File("profiles"));
		
//		BdbTerminologyStore dataStore = AppContext.getServiceLocator().getService(BdbTerminologyStore.class);
//		Thread.sleep(3000);
//		
//		MappingDataAccess.createMappingSet("test mapping2", "inverse another", "purpose 2", "description 2", UUID.fromString("d481125e-b8ca-537c-b688-d09d626e5ff9"));
//		
//		UUID allergy = UUID.fromString("022665ef-8bf5-5972-805a-c3f71ea31f3d");
//		UUID chem = UUID.fromString("48bff044-2d3d-5a99-b005-d39ea2d94a54");
//		MappingDataAccess.createMapping(allergy, UUID.fromString("45cadb52-d8e3-57ab-9a19-dc649177e197"), chem, 
//				UUID.fromString("8aa6421d-4966-5230-ae5f-aca96ee9c2c1"), UUID.fromString("d481125e-b8ca-537c-b688-d09d626e5ff9"));
//		
//		MappingDataAccess.createMapping(allergy, UUID.fromString("45cadb52-d8e3-57ab-9a19-dc649177e197"), chem, 
//				UUID.fromString("8aa6421d-4966-5230-ae5f-aca96ee9c2c1"), UUID.fromString("d481125e-b8ca-537c-b688-d09d626e5ff9"));
		
//		for (ConceptChronicleBI x : AssociationUtilities.getAssociationTypes())
//		{
//			for (Association y : AssociationUtilities.getAssociationsOfType(x))
//			{
//				System.out.println(y.toString());
//			}
//		}
		
//		for (Association x : AssociationUtilities.getSourceAssociations(ExtendedAppContext.getDataStore().getComponent(UUID.fromString("1daa10e3-6876-5650-b9fa-1f036076acc6")), 
//				OTFUtility.getViewCoordinate()))
//		{
//			System.out.println(x.toString());
//		}
//		
//		for (Association x : AssociationUtilities.getTargetAssociations(ExtendedAppContext.getDataStore().getComponent(UUID.fromString("80208468-e25d-5e03-a1b2-99b6da12a386")), 
//				OTFUtility.getViewCoordinate()))
//		{
//			System.out.println(x.toString());
//		}
		
		launch(args);
	}
	

}
