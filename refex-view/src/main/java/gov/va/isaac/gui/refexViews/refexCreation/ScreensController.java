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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
/**
 * 
 * {@link ScreensController}
 *
 * @author <a href="jefron@apelon.com">Jesse Efron</a>
 */
public class ScreensController extends StackPane {
	static private HashMap<String, Parent> screens = new HashMap<>();
	private int totalColumns;

	private ConceptChronicleBI refsetCon;

	private String refexName;
	private String refsetDescription;
	private int extendedFieldsCount;
	private boolean isAnnotated;
//	private boolean isReadOnly;
	
	private List<ConceptVersionBI> columnNids = new ArrayList<ConceptVersionBI>();
	private List<RefexDynamicDataType> columnTypeStrings = new ArrayList<RefexDynamicDataType>();
	private List<String> columnDefaultValues = new ArrayList<String>();
	private List<Boolean> columnIsMandatory = new ArrayList<Boolean>();
	private String parentConcept;
	private boolean isReadOnly;
	
	public static final String DEFINITION_SCREEN = "definition";
	public static final String DEFINITION_SCREEN_FXML = "wizardPages/definition.fxml";
	public static final String COLUMN_SCREEN = "column";
	public static final String COLUMN_SCREEN_FXML = "wizardPages/column.fxml";
	public static final String SUMMARY_SCREEN = "summary";
	public static final String SUMMARY_SCREEN_FXML = "wizardPages/summary.fxml";

	
	protected ScreensController()
	{
		loadScreen(DEFINITION_SCREEN, DEFINITION_SCREEN_FXML);
		loadScreen(COLUMN_SCREEN, COLUMN_SCREEN_FXML);
		loadScreen(SUMMARY_SCREEN, SUMMARY_SCREEN_FXML);
		setScreen(DEFINITION_SCREEN);
	}

	public void addScreen(String name, Parent screen) {
			screens.put(name, screen);
	}
	
	public boolean loadScreen(String name, String resource) {
		try {
			FXMLLoader myLoader = new FXMLLoader(getClass().getResource(resource));
			Parent loadScreen = (Parent) myLoader.load();
			PanelControllers myScreenControler = ((PanelControllers) myLoader.getController());

			myScreenControler.finishInit(this);
			addScreen(name, loadScreen);
			
			return true;
		}catch(Exception e) {
			e.printStackTrace();
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
			System.out.println("screen hasn't been loaded!\n");
			return false;
		} 
	}

	public boolean unloadScreen(String name) {
		if(screens.remove(name) == null) {
			System.out.println("Screen didn't exist");
			return false;
		} else {
			return true;
		}
	}

	public void setTotalColumnCount(int count) {
		totalColumns = count;
	}

	public int getTotalColumnCount() {
		return totalColumns;
	}

	public void setRefsetConcept(ConceptChronicleBI con) {
		refsetCon = con;
	}
	public ConceptChronicleBI getRefsetConcept() {
		return refsetCon;
	}

	public void setNewRefsetConceptVals(String name, String description, String parentConcept, int extendedFieldsCount, boolean isAnnotated, boolean isReadOnly) {
		this.refexName = name;
		this.refsetDescription = description;
		this.parentConcept = parentConcept;
		this.extendedFieldsCount = extendedFieldsCount;
		this.isAnnotated = isAnnotated;
		this.isReadOnly = isReadOnly;
	}

	public void setReferencedComponentVals(ConceptVersionBI colCon, RefexDynamicDataType type, String defaultValue, boolean isMandatory) {
		setColumnVals(colCon, type, defaultValue, isMandatory);
	}

	public void setColumnVals(ConceptVersionBI colCon, RefexDynamicDataType type, String defaultValue, boolean isMandatory) {
		columnNids.add(colCon);
		columnTypeStrings.add(type);
		columnDefaultValues.add(defaultValue);		
		columnIsMandatory.add(isMandatory);
	}

	public String getRefexName() {
		return refexName;
	}

	public String getRefexDescription() {
		return refsetDescription;
	}

	public String getParentConcept() {
		return parentConcept;
	}

	public boolean isReadOnlyRefex() {
		return isReadOnly;
	}

	public boolean isAnnotated() {
		return isAnnotated;
	}
	
	public int getExtendedFieldsCount() {
		return extendedFieldsCount;
	}

	public String getColumnDescription(int column) {
		try {
			return columnNids.get(column).getFullySpecifiedDescription().getText();
		} catch (Exception e) {
			e.printStackTrace();
			return "Not Accessible";
		}
	}

	public String getColumnType(int column) {
		return columnTypeStrings.get(column).getDisplayName();
	}

	public String getColumnDefaultValue(int column) {
		return columnDefaultValues.get(column);
	}

	public String getColumnIsMandatory(int column) {
		if (columnIsMandatory.get(column)) {
			return "Mandatory";
		} else {
			return "Optional";
		}
	}
}
