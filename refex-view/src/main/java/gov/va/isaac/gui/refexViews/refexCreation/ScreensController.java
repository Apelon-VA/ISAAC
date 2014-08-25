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
package gov.va.isaac.gui.refexViews.refexCreation;

import gov.va.isaac.gui.refexViews.refexCreation.wizardPages.ColumnController;
import gov.va.isaac.gui.refexViews.refexCreation.wizardPages.DefinitionController;
import gov.va.isaac.gui.refexViews.refexCreation.wizardPages.SummaryController;
import java.io.IOException;
import java.util.function.Consumer;
import javafx.animation.FadeTransition;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Window;
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
public class ScreensController extends StackPane 
{
	private static final Logger logger = LoggerFactory.getLogger(ScreensController.class);

	private final String DEFINITION_SCREEN_FXML = "wizardPages/definition.fxml";
	private final String COLUMN_SCREEN_FXML = "wizardPages/column.fxml";
	private final String SUMMARY_SCREEN_FXML = "wizardPages/summary.fxml";
	
	private DefinitionController definitionController_;
	private ColumnController columnController_;
	private SummaryController summaryController_;
	
	private PanelControllersI currentlyShowingScreen = null;
	private int currentlyShowingColumnNumber = 0;
	
	private SimpleBooleanProperty canGoNext = new SimpleBooleanProperty(true);
	private SimpleBooleanProperty canGoBack = new SimpleBooleanProperty(false);
	
	protected ScreensController() throws IOException
	{
		definitionController_ = (DefinitionController)loadScreen(DEFINITION_SCREEN_FXML);
		summaryController_ = (SummaryController)loadScreen(SUMMARY_SCREEN_FXML);
		columnController_ = (ColumnController)loadScreen(COLUMN_SCREEN_FXML);
	}

	
	private PanelControllersI loadScreen(String resource) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));
		Region loadScreen = (Region) loader.load();
		loadScreen.setOpacity(0);
		loadScreen.setVisible(false);
		getChildren().add(loadScreen);
		PanelControllersI processController = ((PanelControllersI) loader.getController());
		processController.finishInit(this, loadScreen);
		return processController;
	}
	
	public ReadOnlyBooleanProperty getCanGoBack()
	{
		return ReadOnlyBooleanProperty.readOnlyBooleanProperty(canGoBack);
	}
	
	public ReadOnlyBooleanProperty getCanGoNext()
	{
		return ReadOnlyBooleanProperty.readOnlyBooleanProperty(canGoNext);
	}
	
	public void showFirstScreen()
	{
		transitionToScreen(definitionController_);
		canGoNext.set(true);
		canGoBack.set(false);
	}
	
	public void showNextScreen()
	{
		if (currentlyShowingScreen == null)
		{
			transitionToScreen(definitionController_);
		}
		else if (currentlyShowingScreen == definitionController_)
		{
			canGoBack.set(true);
			if (getWizardData().getColumnInfo().size() > 0)
			{
				currentlyShowingColumnNumber = 0;
				transitionToScreen(columnController_, (Void) -> columnController_.setColumnNumber(currentlyShowingColumnNumber));
			}
			else
			{
				canGoNext.set(false);
				summaryController_.updateValues(getWizardData());
				transitionToScreen(summaryController_);
			}
		}
		else if (currentlyShowingScreen == columnController_)
		{
			if (getWizardData().getColumnInfo().size() > (currentlyShowingColumnNumber + 1))
			{
				currentlyShowingColumnNumber++;
				transitionToScreen(columnController_, (Void) -> columnController_.setColumnNumber(currentlyShowingColumnNumber));
			}
			else
			{
				canGoNext.set(false);
				summaryController_.updateValues(getWizardData());
				transitionToScreen(summaryController_);
			}
		}
		else
		{
			logger.error("Design failure!");
		}
	}
	
	public void showPreviousScreen()
	{
		if (currentlyShowingScreen == summaryController_)
		{
			canGoNext.set(true);
			if (getWizardData().getColumnInfo().size() > 0)
			{
				transitionToScreen(columnController_, (Void) -> columnController_.setColumnNumber(currentlyShowingColumnNumber));
			}
			else
			{
				canGoBack.set(false);
				transitionToScreen(definitionController_);
			}
		}
		else if (currentlyShowingScreen == columnController_)
		{
			if (currentlyShowingColumnNumber > 0)
			{
				currentlyShowingColumnNumber--;
				transitionToScreen(columnController_, (Void) -> columnController_.setColumnNumber(currentlyShowingColumnNumber));
			}
			else
			{
				canGoBack.set(false);
				transitionToScreen(definitionController_);
			}
		}
		else
		{
			logger.error("Design failure!");
		}
	}
	
	public void transitionToScreen(PanelControllersI newScreen) {
		transitionToScreen(newScreen, null);
	}

	public void transitionToScreen(PanelControllersI newScreen, Consumer<Void> callBeforeSetVisible) {
		FadeTransition fadeIn = new FadeTransition();
		fadeIn.setAutoReverse(false);
		fadeIn.setCycleCount(1);
		fadeIn.setDuration(new Duration(350));
		fadeIn.setFromValue(0.0);
		fadeIn.setToValue(1.0);
		fadeIn.setNode(newScreen.getParent());
		
		FadeTransition fadeOut = null;
		
		if (currentlyShowingScreen != null)
		{
			Node oldNode = currentlyShowingScreen.getParent();
			fadeOut = new FadeTransition();
			fadeOut.setAutoReverse(false);
			fadeOut.setCycleCount(1);
			fadeOut.setDuration(new Duration(350));
			fadeOut.setFromValue(1.0);
			fadeOut.setToValue(0.0);
			fadeOut.setNode(oldNode);
			fadeOut.setOnFinished((event) ->
			{
				oldNode.setVisible(false);
				if (callBeforeSetVisible != null)
				{
					callBeforeSetVisible.accept(null);
				}
				fadeIn.getNode().setVisible(true);
				currentlyShowingScreen = newScreen;
				resize(currentlyShowingScreen);
				fadeIn.play();
			});
		}
		if (fadeOut == null)
		{
			if (callBeforeSetVisible != null)
			{
				callBeforeSetVisible.accept(null);
			}
			fadeIn.getNode().setVisible(true);
			currentlyShowingScreen = newScreen;
			resize(currentlyShowingScreen);
			fadeIn.play();
		}
		else 
		{
			fadeOut.play();
			//fadeOut does the necessary work for fadeIn, when complete.
		}
	}
	
	public RefexData getWizardData() 
	{
		return definitionController_.getWizardData();
	}
	
	private void resize(PanelControllersI panel)
	{
		Window w = getScene().getWindow();
		if (w.getWidth() != panel.getParent().getPrefWidth() || w.getHeight() != panel.getParent().getPrefHeight())
		{
			//side borders are 4 pixels
			double desiredWidth = panel.getParent().getPrefWidth() + 4 > Screen.getPrimary().getVisualBounds().getWidth() ? Screen.getPrimary().getVisualBounds().getWidth() :
				panel.getParent().getPrefWidth() + 4;
			//top border is 25, bottom is 2 pixels
			double desiredHeight =  panel.getParent().getPrefHeight() + 27 > Screen.getPrimary().getVisualBounds().getHeight() ? Screen.getPrimary().getVisualBounds().getHeight() :
				 panel.getParent().getPrefHeight() + 27;
			
			w.setWidth(desiredWidth);
			w.setHeight(desiredHeight);
		}
	}
}
