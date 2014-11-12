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
package gov.va.isaac.gui.xmlView;

import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.XMLViewI;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.XMLUtils;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * {@link XMLDisplayWindow}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@PerLookup
public class XMLDisplayWindow extends Stage implements XMLViewI
{
	static Logger logger = LoggerFactory.getLogger(XMLDisplayWindow.class);
	private WebView wv_;
	ProgressIndicator pi_;

	private XMLDisplayWindow()
	{
		// created by HK2
		super(StageStyle.DECORATED);
		initModality(Modality.NONE);
	}
	
	

	/**
	 * @see gov.va.isaac.interfaces.gui.views.PopupViewI#showView(javafx.stage.Window)
	 */
	@Override
	public void showView(Window parent)
	{
		initOwner(parent);
		show();
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.XMLViewI#showView(Window, String, Supplier, int, int)
	 */
	@Override
	public void setParameters(String title, Supplier<String> xmlContent, int width, int height)
	{
		setWidth(width);
		setHeight(height);
		setTitle(title);
		getIcons().add(Images.XML_VIEW_16.getImage());
		getIcons().add(Images.XML_VIEW_32.getImage());

		BorderPane bp = new BorderPane();
		bp.setCursor(Cursor.WAIT);

		pi_ = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
		pi_.setMaxHeight(Region.USE_PREF_SIZE);
		pi_.setMaxWidth(Region.USE_PREF_SIZE);
		bp.setCenter(pi_);

		wv_ = new WebView();

		Scene scene = new Scene(bp);
		setScene(scene);

		addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent event)
			{
				wv_ = null;
			}
		});
		
		Utility.execute(() -> 
		{
			final StringBuilder formattedContent = new StringBuilder();
			
			try
			{
				formattedContent.append(XMLUtils.toHTML(xmlContent.get()));
			}
			catch (Exception e)
			{
				logger.error("There was an error formatting the lego as XML", e);
				formattedContent.append("There was an error formatting the XML for display");
			}
			
			Platform.runLater(() ->
			{
				WebView wv = wv_;
				if (wv != null)
				{
					wv_.getEngine().loadContent(formattedContent.toString());
					getScene().getRoot().setCursor(Cursor.DEFAULT);
					((BorderPane) getScene().getRoot()).setCenter(wv_);
					((BorderPane) getScene().getRoot()).getChildren().remove(pi_);
					pi_ = null;
				}
			});
		});
	}
}