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
package gov.va.isaac.gui.dialog;

import gov.va.isaac.AppContext;
import gov.va.isaac.interfaces.utility.DialogResponse;
import gov.va.isaac.util.Utility;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.slf4j.LoggerFactory;

/**
 * {@link YesNoDialog}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class YesNoDialog
{
	private YesNoDialogController yndc_;
	private Stage yesNoStage_;

	public YesNoDialog(Window owner)
	{
		try
		{
			yesNoStage_ = new Stage();
			yesNoStage_.initModality(Modality.WINDOW_MODAL);
			yesNoStage_.initOwner(owner);
			yesNoStage_.initStyle(StageStyle.UTILITY);
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(YesNoDialogController.class.getResource("YesNoDialog.fxml"));
			Scene scene = new Scene((Parent) loader.load(YesNoDialogController.class.getResourceAsStream("YesNoDialog.fxml")));
			yndc_ = loader.getController();
			yesNoStage_.setScene(scene);
			
			//Problem on linux, where modal windows don't always stay on top...
			yesNoStage_.iconifiedProperty().addListener(new ChangeListener<Boolean>()
			{
				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
				{
					Platform.runLater(() -> {
						yesNoStage_.toFront();
					});
				}
			});
		}
		catch (Exception e)
		{
			LoggerFactory.getLogger(this.getClass()).error("Unexpected", e);
			AppContext.getCommonDialogs().showErrorDialog("Unexpected error showing Yes / No Dialog", e);
		}
	}

	public DialogResponse showYesNoDialog(String title, String question)
	{
		yndc_.init(question);
		yesNoStage_.setTitle(title);
		//Hack for linux, to try to make sure it gets on top
		Task<Void> t = new Task<Void>()
		{
			@Override
			protected Void call() throws Exception
			{
				Thread.sleep(100);
				return null;
			}

			/**
			 * @see javafx.concurrent.Task#succeeded()
			 */
			@Override
			protected void succeeded()
			{
				yesNoStage_.toFront();
			}
		};
		Utility.execute(t);
		
		yesNoStage_.showAndWait();
		return yndc_.getAnswer();
	}
}
