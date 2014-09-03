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
package gov.va.isaac.gui.refsetview;

import gov.va.isaac.gui.refsetview.RefsetInstanceAccessor.RefsetInstance;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;

/**
 * {@link RefsetTableEditingCell}
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefsetTableEditingCell extends TableCell<RefsetInstance, String>
{

	private TextField textField;

	public static Callback<TableColumn<RefsetInstance, String>, TableCell<RefsetInstance, String>> create()
	{
		return new Callback<TableColumn<RefsetInstance, String>, TableCell<RefsetInstance, String>>()
		{
			@Override
			public TableCell<RefsetInstance, String> call(TableColumn<RefsetInstance, String> p)
			{
				return new RefsetTableEditingCell();
			}
		};
	}

	private RefsetTableEditingCell()
	{
	}

	@Override
	public void startEdit()
	{
		super.startEdit();

		if (textField == null)
		{
			createTextField();
		}
		setText(null);
		setGraphic(textField);
		textField.selectAll();
	}

	@Override
	public void cancelEdit()
	{
		super.cancelEdit();
		setText((String) getItem());
		setGraphic(null);
	}

	@Override
	public void updateItem(String item, boolean empty)
	{
		super.updateItem(item, empty);
		// super.updateItem(item, false);
		if (empty)
		{
			setText(null);
			setGraphic(null);
		}
		else
		{
			if (isEditing())
			{
				if (textField != null)
				{
					textField.setText(getString());
				}
				setText(null);
				setGraphic(textField);
			}
			else
			{
				setText(getString());
				setGraphic(null);
			}
		}
	}

	private void createTextField()
	{
		textField = new TextField(getString());
		textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
		textField.focusedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2)
			{
				if (!arg2)
				{
					commitEdit(textField.getText());
				}
			}
		});

		textField.setOnKeyReleased(new EventHandler<KeyEvent>()
		{
			@Override
			public void handle(KeyEvent t)
			{
				if (t.getCode() == KeyCode.ENTER)
				{
					String value = textField.getText();
					if (value != null)
					{
						commitEdit(value);
					}
					else
					{
						commitEdit(null);
					}
				}
				else if (t.getCode() == KeyCode.ESCAPE)
				{
					cancelEdit();
				}
			}
		});
	}

	private String getString()
	{
		return getItem() == null ? "" : getItem().toString();
	}
}
