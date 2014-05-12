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
import java.io.IOException;
import java.util.UUID;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * RefsetViewRunner
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefexViewRunner extends Application
{
    UUID diastolicBP = UUID.fromString("215fd598-e21d-3e27-a0a2-8e23b1b36dfc");

	/**
	 * @see javafx.application.Application#start(javafx.stage.Stage)
	 */
	@Override
	public void start(Stage primaryStage) throws Exception
	{
		primaryStage.setTitle("Refex View");

		RefexView refsetView = AppContext.getService(RefexView.class);

		primaryStage.setScene(new Scene(refsetView.getView(), 800, 600));
		
		primaryStage.show();
	}

	public static void main(String[] args) throws ClassNotFoundException, IOException
	{
		AppContext.setup();
		launch(args);
	}

}
