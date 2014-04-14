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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.legoEdit.gui.legoListProperties;

import gov.va.isaac.AppContext;
import gov.va.legoEdit.model.LegoListByReference;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;

/**
 * {@link LegoListProperties}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
@Service
@Singleton
public class LegoListProperties
{
	private Stage legoListPropertiesStage_;
	private LegoListPropertiesController llpc_;
	
	private LegoListProperties() throws IOException
	{
		//init by HK2
		legoListPropertiesStage_ = new Stage();
		legoListPropertiesStage_.initModality(Modality.WINDOW_MODAL);
		legoListPropertiesStage_.initOwner(AppContext.getMainApplicationWindow().getPrimaryStage());
		legoListPropertiesStage_.initStyle(StageStyle.UTILITY);
		FXMLLoader loader = new FXMLLoader();
		Scene scene = new Scene((Parent) loader.load(LegoListProperties.class.getResourceAsStream("LegoListProperties.fxml")));
		llpc_ = loader.getController();
		scene.getStylesheets().add(LegoListProperties.class.getResource("/isaac-shared-styles.css").toString());
		legoListPropertiesStage_.setScene(scene);
	}
	
	public void show(LegoListByReference llbr, TreeItem<String> ti)
	{
		llpc_.setVariables(llbr, ti);
		legoListPropertiesStage_.show();
	}
}
