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
package gov.va.isaac.gui.querybuilder;

import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.QueryBuilderViewI;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.QueryNodeTypeI;
import java.io.IOException;
import java.net.URL;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * QueryBuilder
 *
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a> 
 */
@Service
@PerLookup
public class QueryBuilderView extends Stage implements QueryBuilderViewI
{
	private final Logger logger = LoggerFactory.getLogger(QueryBuilderView.class);

	private QueryBuilderViewController controller_;

	private boolean shown = false;
	
	private QueryBuilderView() throws IOException
	{
		super();

		URL resource = this.getClass().getResource("QueryBuilderView.fxml");
		FXMLLoader loader = new FXMLLoader(resource);
		Parent root = (Parent) loader.load();
		setScene(new Scene(root));
		getScene().getStylesheets().add(QueryBuilderView.class.getResource("/isaac-shared-styles.css").toString());
		getIcons().add(Images.SEARCH.getImage());

		controller_ = loader.getController();
		controller_.setStage(this);
		
		setTitle("Query Builder");
		setResizable(true);

		setWidth(600);
		setHeight(400);
	}

	/**
	 * 
	 * @see gov.va.isaac.interfaces.gui.views.PopupViewI#showView(javafx.stage.Window)
	 */
	@Override
	public void showView(Window parent)
	{
		if (! shown) {
			shown = true;

			initOwner(parent);
			initModality(Modality.NONE);
			initStyle(StageStyle.DECORATED);
			
			controller_.loadMenus();
		}

		logger.debug("Showing Query Builder View");
		show();
	}

	@Override
	public void setUnsupportedQueryNodeTypes(QueryNodeTypeI... nodeTypes) {
		controller_.setUnsupportedQueryNodeTypes(nodeTypes);
	}
}
