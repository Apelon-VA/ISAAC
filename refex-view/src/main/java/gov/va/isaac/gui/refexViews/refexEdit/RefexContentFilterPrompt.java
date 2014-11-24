package gov.va.isaac.gui.refexViews.refexEdit;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.dialog.UserPrompt;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class RefexContentFilterPrompt extends UserPrompt {
    final ListView<String> selectedValues = new ListView<>();
	private String columnName;
	private List<String> allValues;

	final List<String> alreadySelectedValues = new ArrayList<>();

//	protected RefexContentFilterPrompt(String columnName, List<String> allValues) {
//		this(columnName, allValues, null);
//	}
	protected RefexContentFilterPrompt(String columnName, List<String> allValues, List<?> alreadySelectedValues) {
		super("Filter");
		this.columnName = columnName;
		this.allValues = allValues;
		this.cancelButtonText = "Clear";
		
		for (Object obj : alreadySelectedValues) {
			if (obj != null) {
				this.alreadySelectedValues.add(obj.toString());
			}
		}
	}

	protected Node createUserInterface() {
		VBox vb = new VBox(10);
		vb.setAlignment(Pos.CENTER);
		vb.setPadding(new Insets(15));

		Label panelName = createLabel("Filter Selection", 16);
		
		HBox columnHBox = new HBox(10);
		columnHBox.setAlignment(Pos.CENTER);
		Label columnAttrLabel= createLabel("Column:");
		Label columnValLabel = new Label(columnName);
		columnHBox.getChildren().addAll(columnAttrLabel, columnValLabel);
		
		vb.getChildren().addAll(panelName, columnHBox, createMenuBox());
		
		return vb;
	}

	private MenuButton createMenuBox() {
        final MenuButton choicesMenuBox = new MenuButton("Potential Values");
        List<CheckMenuItem> checkItems = new ArrayList<CheckMenuItem>();
        
        for (String s : allValues) {
        	CheckMenuItem item = new CheckMenuItem(s);
        	if (alreadySelectedValues.contains(s)) {
        		item.isSelected();
        	}
    		checkItems.add(item);
        }
        
        choicesMenuBox.getItems().addAll(checkItems);

          // Keep track of selected items
        
        for (final CheckMenuItem item : checkItems) {
            item.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> obs,
                        Boolean wasPreviouslySelected, Boolean isNowSelected) {
                    if (isNowSelected) {
                        selectedValues.getItems().add(item.getText());
                    } else {
                        selectedValues.getItems().remove(item.getText());
                    }
                }
            });
            if (item.isSelected()) {
            	selectedValues.getItems().add(item.getText());
            }
        }	
        
        return choicesMenuBox;       
        
    }

	public ObservableList<String> getSelectedValues() {
		return selectedValues.getItems();
	}
	
	@Override
	protected boolean isSelectedValuesValid() {
		// Because always returns true, if a test ever changes, need to update displayInvalidMessage() accordingly
		return !selectedValues.getItems().isEmpty();
	}
	
	@Override
	protected void displayInvalidMessage() {
		AppContext.getCommonDialogs().showInformationDialog("No Filters Selected", "Must select at least one filter or select Cancel Button");
	}
}
