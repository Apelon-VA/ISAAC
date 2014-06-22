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
package gov.va.isaac.gui.conceptCreation;

import java.util.HashMap;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 
 * {@link ScreensController}
 *
 * @author <a href="jefron@apelon.com">Jesse Efron</a>
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class ScreensController extends StackPane {
	static private HashMap<String, Parent> screens = new HashMap<>();

	public static final String DEFINITION_SCREEN = "definition";
	public static final String DEFINITION_SCREEN_FXML = "wizardPages/definition.fxml";
	public static final String COMPONENTS_SCREEN = "components";
	public static final String COMPONENTS_SCREEN_FXML = "wizardPages/components.fxml";
	public static final String SUMMARY_SCREEN = "summary";
	public static final String SUMMARY_SCREEN_FXML = "wizardPages/summary.fxml";
	
	private static final Logger logger = LoggerFactory.getLogger(ScreensController.class);

	public final WizardController wizard = new WizardController();
	
	protected ScreensController()
	{
		loadScreen(DEFINITION_SCREEN, DEFINITION_SCREEN_FXML);
		loadScreen(COMPONENTS_SCREEN, COMPONENTS_SCREEN_FXML);
		setScreen(DEFINITION_SCREEN);
	}

	public void addScreen(String name, Parent screen) {
			screens.put(name, screen);
	}
	
	public void loadSummaryScreen() {
		loadScreen(SUMMARY_SCREEN, SUMMARY_SCREEN_FXML);
	}

	public boolean loadScreen(String name, String resource) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));
			Parent loadScreen = (Parent) loader.load();
			PanelControllers processController = ((PanelControllers) loader.getController());
			processController.finishInit(this);
			addScreen(name, loadScreen);
			
			return true;
		}catch(Exception e) {
			logger.error("Unable to load new screen: " + name, e);
			return false;
		}
	} 

	public boolean setScreen(final String name) {
		if(screens.get(name) != null) { //screen loaded
			final DoubleProperty opacity = opacityProperty();

			//Is there is more than one screen
			if(!getChildren().isEmpty()){
				Timeline fade = new Timeline(
						new KeyFrame(Duration.ZERO, new KeyValue(opacity,1.0)),
						new KeyFrame(new Duration(1000), (e) -> {
							//remove displayed screen
							getChildren().remove(0);
							//add new screen
							getChildren().add(0, screens.get(name));
							Timeline fadeIn = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(opacity, 0.0)), 
									new KeyFrame(new Duration(800), new KeyValue(opacity, 1.0)));
							fadeIn.play();
						}, new KeyValue(opacity, 0.0))); 
				fade.play();
			} else {
				//no one else been displayed, then just show
				setOpacity(0.0);
				getChildren().add(screens.get(name));
				Timeline fadeIn = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(opacity, 0.0)), 
						new KeyFrame(new Duration(2500), new KeyValue(opacity, 1.0)));
				fadeIn.play();
			}
			return true;
		} else {
			logger.warn("screen hasn't been loaded!");
			return false;
		} 
	}

	public boolean unloadScreen(String name) {
		if(screens.remove(name) == null) {
			logger.warn("Screen didn't exist");
			return false;
		} else {
			return true;
		}
	}
	
	public WizardController getWizard() {
		return wizard;
	}
}
