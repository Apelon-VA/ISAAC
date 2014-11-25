package gov.va.isaac.gui.refexViews.refexEdit;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.dialog.UserPrompt;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

public class RefexContentFilterPrompt extends UserPrompt {
    final ListView<String> selectedValues = new ListView<>();
	private String columnName;
	private List<String> allValues;

	final List<String> alreadySelectedValues = new ArrayList<>();

	protected RefexContentFilterPrompt(String columnName, List<String> allValues, List<?> alreadySelectedValues) {
		super("Apply");
		this.columnName = columnName;
		this.allValues = allValues;
		
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
		
		vb.getChildren().addAll(panelName, columnHBox, createListView());
		
		return vb;
	}
	private ListView<String> createListView() {
		ListView<String> listView = new ListView<>();
		listView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
			@Override
			public ListCell<String> call(ListView<String> param) {
				ListCell<String> cell = new ListCell<String>() {
					@Override
					public void updateItem(String item, boolean empty) {
						super.updateItem(item, empty);
						if (empty) {
							setText(null);
							setGraphic(null);
						} else {
							setText(null);
							Label label = new Label(item);
				        	label.setMaxWidth(280);
				        	label.setTooltip(new Tooltip(item));
							setGraphic(label);
						}
					}
				};
				
				return cell;
			}
		});
		listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		listView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<String>() {
			@Override
			public void onChanged(
					javafx.collections.ListChangeListener.Change<? extends String> c) {
				while (c.next()) {
					if (c.wasPermutated()) {
						// irrelevant
						//	                     for (int i = c.getFrom(); i < c.getTo(); ++i) {
						//	                          //permutate
						//	                     }
					} else if (c.wasUpdated()) {
						// irrelevant
					} else {
						for (String item : c.getRemoved()) {
							selectedValues.getItems().remove(item);
						}
						for (String item : c.getAddedSubList()) {
							selectedValues.getItems().add(item);
						}
					}
				}
			}	
		});
		
		for (String s : allValues)  {
			listView.getItems().add(s);
			if (alreadySelectedValues.contains(s)) {
				listView.getSelectionModel().select(s);
        	}
		}
       
        return listView;
    }
	private MenuButton createMenuBox() {
        final MenuButton choicesMenuBox = new MenuButton("Potential Values");
        List<CheckMenuItem> checkItems = new ArrayList<CheckMenuItem>();
        
        for (String s : allValues) {
        	Label label = new Label(s);
        	label.setMaxWidth(280);
        	label.setTooltip(new Tooltip(s));
        	CheckMenuItem item = new CheckMenuItem(null, label);
        	if (alreadySelectedValues.contains(s)) {
        		item.setSelected(true);
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
                        selectedValues.getItems().add(((Label)item.getGraphic()).getText());
                    } else {
                        selectedValues.getItems().remove(((Label)item.getGraphic()).getText());
                    }
                }
            });
            if (item.isSelected()) {
            	selectedValues.getItems().add(((Label)item.getGraphic()).getText());
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
		return true; //!selectedValues.getItems().isEmpty();
	}
	
	@Override
	protected void displayInvalidMessage() {
		AppContext.getCommonDialogs().showInformationDialog("No Filters Selected", "Must select at least one filter or select Cancel Button");
	}
}
