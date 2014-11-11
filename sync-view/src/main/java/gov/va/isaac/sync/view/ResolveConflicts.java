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
package gov.va.isaac.sync.view;

import gov.va.isaac.interfaces.sync.MergeFailOption;
import gov.va.isaac.util.ValidBooleanBinding;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * 
 * {@link ResolveConflicts}
 *
 * A Simple dialog to the user decision for handeling merge conflicts
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */

public class ResolveConflicts extends Stage
{
	private BorderPane root_;
	private Consumer<HashMap<String, MergeFailOption>> sendResultTo_;
	private ValidBooleanBinding allValid ;
	private HashMap<String, ToggleGroup> selections = new HashMap<>();
	
	protected ResolveConflicts(Window parent, Set<String> filesWithConflicts, Consumer<HashMap<String, MergeFailOption>> sendResultTo)
	{
		super();
		sendResultTo_ = sendResultTo;
		setTitle("Resolve Merge Conflicts");
		setResizable(true);

		initOwner(parent);
		initModality(Modality.WINDOW_MODAL);
		initStyle(StageStyle.UTILITY);

		setOnCloseRequest((event) -> {
			event.consume();
		});

		setupGUI(filesWithConflicts);
		
		Scene scene = new Scene(root_);
		scene.getStylesheets().add(ResolveConflicts.class.getResource("/isaac-shared-styles.css").toString());
		setScene(scene);
		sizeToScene();
		show();
	}

	private void setupGUI(Set<String> filesWithConflicts)
	{
		VBox top = new VBox();
		top.setAlignment(Pos.CENTER);
		Label title = new Label("Resolve Merge Conflicts");
		title.getStyleClass().add("headerBackground");
		title.getStyleClass().add("titleLabel");
		title.setMaxWidth(Double.MAX_VALUE);
		title.setPrefHeight(40.0);
		title.setAlignment(Pos.CENTER);
		
		top.getChildren().add(title);
		
		Label details = new Label("Please select an action for each conflict");
		details.setPadding(new Insets(5.0));
		top.getChildren().add(details);
		
		root_ = new BorderPane();
		root_.setPrefWidth(500);
		
		root_.setTop(top);
		
		
		GridPane gp = new GridPane();
		gp.setPadding(new Insets(5.0));
		gp.setMaxWidth(Double.MAX_VALUE);
		gp.setHgap(10.0);
		gp.setVgap(5.0);
		
		int row = 0;
		for (String s : filesWithConflicts)
		{
			Label l = new Label(s);
			GridPane.setHgrow(l, Priority.ALWAYS);
			gp.add(l, 0, row);
			
			ToggleGroup tg = new ToggleGroup();
			RadioButton local = new RadioButton("Keep Local Version");
			local.setUserData(MergeFailOption.KEEP_LOCAL);
			local.setToggleGroup(tg);
			
			gp.add(local, 1, row);
			
			RadioButton remote = new RadioButton("Use Server Version");
			remote.setUserData(MergeFailOption.KEEP_REMOTE);
			remote.setToggleGroup(tg);
			
			gp.add(remote, 2, row++);
			
			selections.put(s, tg);
		}
		
		ScrollPane sp = new ScrollPane(gp);
		sp.setFitToWidth(true);
		root_.setCenter(sp);
		
		allValid = new ValidBooleanBinding()
		{
			{
				setComputeOnInvalidate(true);
				for (Entry<String, ToggleGroup> tg : selections.entrySet())
				{
					for (Toggle t : tg.getValue().getToggles())
					{
						bind(t.selectedProperty());
					}
				}
			}
			
			@Override
			protected boolean computeValue()
			{
				for (Entry<String, ToggleGroup> tg : selections.entrySet())
				{
					if (tg.getValue().getSelectedToggle() == null)
					{
						return false;
					}
				}
				return true;
			}
		};
		
		VBox vbox = new VBox();
		vbox.setAlignment(Pos.CENTER);
		vbox.setMaxWidth(Double.MAX_VALUE);
		vbox.setFillWidth(true);
		vbox.setPadding(new Insets(10.0));
		
		Button ok = new Button("Ok");
		ok.disableProperty().bind(allValid.not());
		ok.setOnAction(event ->
		{
			HashMap<String, MergeFailOption> result = new HashMap<>();
			
			for (Entry<String, ToggleGroup> tg : selections.entrySet())
			{
				result.put(tg.getKey(), (MergeFailOption)tg.getValue().getSelectedToggle().getUserData());
			}
			
			if (sendResultTo_ != null)
			{
				sendResultTo_.accept(result);
			}
			this.hide();
		});
		
		vbox.getChildren().add(ok);
		
		root_.setBottom(vbox);
	}
}
