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
package gov.va.isaac.gui.dialog;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PopupControl;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/**
 * {@link BusyPopover}
 * Just a simple light-weight popup that shows an progress bar and a message.
 * Also consumes all events to the parent window until this view is hidden.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class BusyPopover extends PopupControl
{
	private Window parentWindow_;
	private WeakInvalidationListener listener_;
	private EventHandler<Event> eventHandler_;
	
	private BusyPopover(String busyText, Node aboveNode)
	{
		super();
		parentWindow_ = aboveNode.getScene().getWindow();
		VBox v = new VBox();
		Label l = new Label(busyText);
		l.getStyleClass().add("boldLabel");
		v.getChildren().add(l);
		ProgressBar pb = new ProgressBar(ProgressBar.INDETERMINATE_PROGRESS);
		pb.setPrefWidth(300);
		v.getChildren().add(pb);
		
		v.setPadding(new Insets(10, 10, 10, 10));
		v.getStyleClass().add("popupStyle");
		
		getScene().setRoot(v);
		getScene().getStylesheets().add(BusyPopover.class.getResource("/isaac-shared-styles.css").toString());
		
		listener_ = new WeakInvalidationListener(new InvalidationListener()
		{
			@Override
			public void invalidated(Observable observable)
			{
				position();
			}
		});
		
		eventHandler_ = new EventHandler<Event>()
		{
			@Override
			public void handle(Event event)
			{
				event.consume();
			}
		};
		
		setOnShowing((event) ->
		{
			
			parentWindow_.getScene().addEventFilter(MouseEvent.MOUSE_PRESSED, eventHandler_);
			parentWindow_.getScene().addEventFilter(KeyEvent.ANY, eventHandler_);
		});
		
		setOnHidden((event) ->
		{
			parentWindow_.xProperty().removeListener(listener_);
			parentWindow_.yProperty().removeListener(listener_);
			parentWindow_.widthProperty().removeListener(listener_);
			parentWindow_.heightProperty().removeListener(listener_);
			parentWindow_.getScene().removeEventFilter(MouseEvent.MOUSE_PRESSED, eventHandler_);
			parentWindow_.getScene().removeEventFilter(KeyEvent.ANY, eventHandler_);
			
		});
		
		showingProperty().addListener(listener_);
		parentWindow_.xProperty().addListener(listener_);
		parentWindow_.yProperty().addListener(listener_);
		parentWindow_.widthProperty().addListener(listener_);
		parentWindow_.heightProperty().addListener(listener_);
		
	}
	
	private void position()
	{
		double x = parentWindow_.getX() + (parentWindow_.getWidth() / 2) - (getWidth() / 2);
		double y = parentWindow_.getY() + (parentWindow_.getHeight() / 2) - (getHeight() / 2);
		setX(x);
		setY(y);
	}
	
	public static BusyPopover createBusyPopover(String busyText, Node aboveNode)
	{
		BusyPopover p = new BusyPopover(busyText, aboveNode);
		p.show(aboveNode.getScene().getWindow());
		return p;
	}
}
