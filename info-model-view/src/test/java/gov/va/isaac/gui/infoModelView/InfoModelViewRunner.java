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

import java.io.IOException;
import gov.va.isaac.AppContext;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * InfoModelViewRunner
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class InfoModelViewRunner extends Application
{
	/**
	 * @see javafx.application.Application#start(javafx.stage.Stage)
	 */
	@Override
	public void start(Stage primaryStage) throws Exception
	{
		primaryStage.setTitle("Refset View");

		InfoModelView infoModelView = AppContext.getService(InfoModelView.class);

		primaryStage.setScene(new Scene(infoModelView.getView(), 800, 600));
		
		primaryStage.show();
	}

	public static void main(String[] args) throws ClassNotFoundException, IOException
	{
		AppContext.setup();
		launch(args);
	}

}
