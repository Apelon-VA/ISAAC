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
package gov.va.isaac.gui.listview.operations;

import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.gui.util.FxUtils;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;

/**
 * {@link FindAndReplaceController}
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class FindAndReplaceController
{

	@FXML private CheckBox regexp;
	@FXML private TextField findText;
	@FXML private TextField replaceText;
	@FXML private ToggleGroup sdt;
	@FXML private RadioButton descriptionTypeAll;
	@FXML private RadioButton descriptionTypeSelected;
	@FXML private CheckBox descriptionTypeFSN;
	@FXML private CheckBox descriptionTypePT;
	@FXML private CheckBox descriptionTypeSynonym;
	@FXML private CheckBox caseSensitive;
	@FXML private ComboBox<SimpleDisplayConcept> retireAs;
	@FXML private ComboBox<SimpleDisplayConcept> searchInLanguage;
	@FXML private GridPane root;
	@FXML private GridPane optionsGridPane;
	@FXML private TitledPane optionsTitledPane;

	@FXML
	void initialize()
	{
		assert regexp != null : "fx:id=\"regexp\" was not injected: check your FXML file 'FindAndReplaceController.fxml'.";
		assert findText != null : "fx:id=\"findText\" was not injected: check your FXML file 'FindAndReplaceController.fxml'.";
		assert replaceText != null : "fx:id=\"replaceText\" was not injected: check your FXML file 'FindAndReplaceController.fxml'.";
		assert sdt != null : "fx:id=\"sdt\" was not injected: check your FXML file 'FindAndReplaceController.fxml'.";
		assert descriptionTypeAll != null : "fx:id=\"descriptionTypeAll\" was not injected: check your FXML file 'FindAndReplaceController.fxml'.";
		assert descriptionTypeFSN != null : "fx:id=\"descriptionTypeFSN\" was not injected: check your FXML file 'FindAndReplaceController.fxml'.";
		assert descriptionTypePT != null : "fx:id=\"descriptionTypePT\" was not injected: check your FXML file 'FindAndReplaceController.fxml'.";
		assert descriptionTypeSynonym != null : "fx:id=\"descriptionTypeSynonym\" was not injected: check your FXML file 'FindAndReplaceController.fxml'.";
		assert descriptionTypeSelected != null : "fx:id=\"descriptionTypeSelected\" was not injected: check your FXML file 'FindAndReplaceController.fxml'.";
		assert caseSensitive != null : "fx:id=\"caseSensitive\" was not injected: check your FXML file 'FindAndReplaceController.fxml'.";
		assert retireAs != null : "fx:id=\"retireAs\" was not injected: check your FXML file 'FindAndReplaceController.fxml'.";
		assert searchInLanguage != null : "fx:id=\"searchInLanguage\" was not injected: check your FXML file 'FindAndReplaceController.fxml'.";

		FxUtils.preventColCollapse(root, 0);
		FxUtils.preventColCollapse(optionsGridPane, 0);
		FxUtils.preventColCollapse(optionsGridPane, 1);
		FxUtils.preventColCollapse(optionsGridPane, 2);

		//Sigh - TitlePane doesn't advertise its size properly, unless you manually set the min sizes.
		//Add a couple listeners, do a bunch of hacking to work around it.
		//TODO file bug
		optionsTitledPane.expandedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
			{
				//If open, bind it to its children content.  If closed, 26 is the value it needs for just the header.
				if (newValue)
				{
					optionsTitledPane.minHeightProperty().bind(optionsGridPane.heightProperty().add(26));
				}
				else
				{
					optionsTitledPane.minHeightProperty().unbind();
					optionsTitledPane.setMinHeight(26);
				}
			}
		});
		optionsTitledPane.heightProperty().addListener(new ChangeListener<Number>()
		{
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
			{
				//When the height first changes from 0, bind it to the height of its child.
				if (optionsTitledPane.isExpanded())
				{
					optionsTitledPane.minHeightProperty().bind(optionsGridPane.heightProperty().add(26));
				}
				else
				{
					optionsTitledPane.setMinHeight(26);
				}
				//Don't need to listen to this anymore
				optionsTitledPane.heightProperty().removeListener(this);

				//Need to bounce it open and closed, to get the right initial sizes.  Not sure why.
				if (optionsTitledPane.isExpanded())
				{
					Platform.runLater(new Runnable()
					{
						@Override
						public void run()
						{
							optionsTitledPane.setExpanded(false);
							optionsTitledPane.setExpanded(true);
						}
					});
				}
			}
		});
	}

	protected GridPane getRoot()
	{
		return root;
	}
}
