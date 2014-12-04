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
package gov.va.isaac.gui;

import gov.va.isaac.AppContext;
import gov.va.isaac.interfaces.gui.views.PopupViewI;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.WBUtility;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.ihtsdo.otf.query.lucene.LuceneDynamicRefexIndexerConfiguration;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicUsageDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ConfigureDynamicRefexIndexingView}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class ConfigureDynamicRefexIndexingView implements PopupViewI
{
	ConceptVersionBI assemblageConcept_;
	private BorderPane root_;
	private CheckBox indexMember_;
	private List<CheckBox> indexColumns_;
	private Logger logger_ = LoggerFactory.getLogger(this.getClass());

	public ConfigureDynamicRefexIndexingView(ConceptVersionBI assemblageConcept)
	{
		try
		{
			assemblageConcept_ = assemblageConcept;
			root_ = new BorderPane();
			root_.setPrefWidth(450);
			
			VBox titleBox = new VBox();
			
			Label title = new Label("Dynamic Sememe Index Configuration");
			title.getStyleClass().add("titleLabel");
			title.setAlignment(Pos.CENTER);
			title.setMaxWidth(Double.MAX_VALUE);
			titleBox.getChildren().add(title);
			
			title = new Label(WBUtility.getDescription(assemblageConcept));
			title.setAlignment(Pos.CENTER);
			title.setMaxWidth(Double.MAX_VALUE);
			titleBox.getChildren().add(title);
			
			titleBox.getStyleClass().add("headerBackground");
			titleBox.setPadding(new Insets(5, 5, 5, 5));
			root_.setTop(titleBox);
			
			indexColumns_ = new ArrayList<>();
			
			VBox vbox = new VBox();
			vbox.setPadding(new Insets(10));
			vbox.setSpacing(5.0);
			
			RefexDynamicUsageDescription rdud = RefexDynamicUsageDescription.read(assemblageConcept.getNid());
			Integer[] currentIndexConfig = LuceneDynamicRefexIndexerConfiguration.readIndexInfo(assemblageConcept.getNid());
			
			if (rdud.isAnnotationStyle())
			{
				indexMember_ = new CheckBox("Index sememe members");
				indexMember_.setTooltip(new Tooltip("Allows queries to find all components that have an instance of this sememe.\n"
						+ "Only applicable to Annotation-style sememes"));
				if (currentIndexConfig != null)
				{
					indexMember_.setSelected(true);
				}
				vbox.getChildren().add(indexMember_);
			}
			else if (rdud.getColumnInfo().length == 0)
			{
				vbox.getChildren().add(new Label("Member-style Sememes without columns are not indexable"));
			}
			
			for (RefexDynamicColumnInfo col : rdud.getColumnInfo())
			{
				CheckBox cb = new CheckBox("Index Attribute: " + col.getColumnName());
				cb.setTooltip(new Tooltip("Attribute Type: " + col.getColumnDataType().getDisplayName() + "\nAttribute Description: " + col.getColumnDescription()));
				
				
				if (!LuceneDynamicRefexIndexerConfiguration.isColumnTypeIndexable(col.getColumnDataType()))
				{
					//disabled check boxes don't show tooltips...
					StackPane silly = new StackPane();
					silly.getChildren().add(cb);
					cb.setDisable(true);
					cb.setMaxWidth(Double.MAX_VALUE);
					Tooltip.install(silly, new Tooltip("The datatype for this attribute isn't indexable"));
					vbox.getChildren().add(silly);
				}
				else
				{
					vbox.getChildren().add(cb);
				}
				indexColumns_.add(cb);
			}
			
			if (currentIndexConfig != null)
			{
				for (int i : currentIndexConfig)
				{
					indexColumns_.get(i).setSelected(true);
				}
			}
			
			ScrollPane sp = new ScrollPane();
			sp.setContent(vbox);
			root_.setCenter(sp);
			
			HBox buttons = new HBox();
			buttons.setMaxWidth(Double.MAX_VALUE);
			buttons.setAlignment(Pos.CENTER);
			buttons.setPadding(new Insets(5));
			buttons.setSpacing(30);

			Button cancel = new Button("Cancel");
			cancel.setOnAction((action) ->
			{
				cancel.getScene().getWindow().hide();
			});
			buttons.getChildren().add(cancel);

			Button ok = new Button("Ok");
			buttons.getChildren().add(ok);
			ok.minWidthProperty().bind(cancel.widthProperty());
			ok.setOnAction((action) ->
			{
				try
				{
					ArrayList<Integer> colsChecked = new ArrayList<>();
					for (int i = 0; i < indexColumns_.size(); i++)
					{
						if (indexColumns_.get(i).isSelected())
						{
							colsChecked.add(i);
						}
					}
					Task<Void> t = new Task<Void>()
					{
						@Override
						protected Void call() throws Exception
						{
							if (colsChecked.size() > 0 || (indexMember_ != null && indexMember_.isSelected()))
							{
								Integer[] toIndex = colsChecked.toArray(new Integer[colsChecked.size()]);
								if (Arrays.deepEquals(toIndex, LuceneDynamicRefexIndexerConfiguration.readIndexInfo(assemblageConcept_.getNid())))
								{
									logger_.info("Skipping reindex - no change detected.");
								}
								else
								{
									AppContext.getRuntimeGlobals().disableAllCommitListeners();
									try
									{
										LuceneDynamicRefexIndexerConfiguration.configureColumnsToIndex(assemblageConcept_.getNid(), toIndex);
									}
									finally
									{
										AppContext.getRuntimeGlobals().enableAllCommitListeners();
									}
								}
							}
							else
							{
								AppContext.getRuntimeGlobals().disableAllCommitListeners();
								try
								{
									LuceneDynamicRefexIndexerConfiguration.disableIndex(assemblageConcept_.getNid());
								}
								finally
								{
									AppContext.getRuntimeGlobals().enableAllCommitListeners();
								}
							}
							return null;
						}

						/**
						 * @see javafx.concurrent.Task#failed()
						 */
						@Override
						protected void failed()
						{
							AppContext.getCommonDialogs().showErrorDialog("Unexpected Error updating index configuration", getException());
						}
						
					};
					
					Utility.submit(t);
					AppContext.getCommonDialogs().showInformationDialog("Reindex running", "An index operation has begun in the background.\nIt may take some time to complete.", 
							root_.getScene().getWindow());
				}
				catch (Exception e)
				{
					logger_.error("Unexpected Error updating index configuration", e);
					AppContext.getCommonDialogs().showErrorDialog("Unexpected Error updating index configuration", e);
				}
				ok.getScene().getWindow().hide();
			});
			
			root_.setBottom(buttons);
			
		}
		catch (Exception e)
		{
			logger_.error("Unexpected Error reading index configuration", e);
			AppContext.getCommonDialogs().showErrorDialog("Unexpected Error reading index configuration", e);
		}
	}
	

	/**
	 * @see gov.va.isaac.interfaces.gui.views.PopupViewI#showView(javafx.stage.Window)
	 */
	@Override
	public void showView(Window parent)
	{
		Stage stage = new Stage(StageStyle.DECORATED);
		stage.initModality(Modality.NONE);
		stage.initOwner(parent);
		Scene scene = new Scene(root_);
		stage.setScene(scene);
		stage.setTitle("Configure Dynamic Sememe Indexing");
		stage.getScene().getStylesheets().add(ConfigureDynamicRefexIndexingView.class.getResource("/isaac-shared-styles.css").toString());
		stage.sizeToScene();
		stage.show();
	}
}