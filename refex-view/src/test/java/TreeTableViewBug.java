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


import java.io.IOException;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.stage.Stage;

/**
 * TreeTableViewBug
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class TreeTableViewBug extends Application
{
	/**
	 * @see javafx.application.Application#start(javafx.stage.Stage)
	 */
	@Override
	public void start(Stage primaryStage) throws Exception
	{
		primaryStage.setTitle("TreeTableViewBug");

		TreeTableView<String> ttv = new TreeTableView<>();
		
		ttv.setTableMenuButtonVisible(true);
		
		TreeTableColumn<String, String> col1 = new TreeTableColumn<>();
		col1.setText("refset");
		col1.setSortable(true);
		col1.setResizable(true);
		
		
		TreeTableColumn<String, String> col2 = new TreeTableColumn<>();
		col2.setText("Value");
		col2.setSortable(true);
		col2.setResizable(true);
		
		TreeTableColumn<String, String> col2Nest1 = new TreeTableColumn<>();
		col2Nest1.setText("Value 1");
		col2Nest1.setSortable(true);
		col2Nest1.setResizable(true);
		
		col2.getColumns().add(col2Nest1);
		
		TreeTableColumn<String, String> col2Nest2 = new TreeTableColumn<>();
		col2Nest2.setText("Value 2");
		col2Nest2.setSortable(true);
		col2Nest2.setResizable(true);
		
		col2.getColumns().add(col2Nest2);

		ttv.getColumns().add(col1);
		ttv.getColumns().add(col2);
		

		primaryStage.setScene(new Scene(ttv, 800, 600));
		
		primaryStage.show();
	}

	public static void main(String[] args) throws ClassNotFoundException, IOException
	{
		launch(args);
	}

}
