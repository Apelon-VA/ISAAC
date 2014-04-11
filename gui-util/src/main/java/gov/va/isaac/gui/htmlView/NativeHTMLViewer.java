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
package gov.va.isaac.gui.htmlView;

import gov.va.isaac.AppContext;
import gov.va.isaac.util.Utility;
import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.util.function.Supplier;

/**
 * {@link NativeHTMLViewer} A utility class that opens HTML in the system default web browser
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class NativeHTMLViewer
{
	/**
	 * View the passed in content in the system-native webbrowser.  htmlContent is fetched in a background thread, written to a temp file, 
	 * and then opened.
	 * @param htmlContent
	 */
	public static void viewInBrowser(Supplier<String> htmlContent)
	{
		Utility.execute(() -> 
		{
			File f = null;
			try
			{
				f = File.createTempFile("isaacHTMLView-", ".html");
				BufferedWriter w = new BufferedWriter(new FileWriter(f));
				w.write(htmlContent.get());
				w.close();
				viewInBrowser(f.toURI());
				// Sleep long enough for the browser to render, then delete the file
				Thread.sleep(10000);
			}
			catch (InterruptedException e)
			{
				//noop
			}
			catch (Exception e)
			{
				AppContext.getCommonDialogs().showErrorDialog("Error launching browser", "There was an error launching the web browser", null);
			}
			finally
			{
				if (f != null)
				{
					f.delete();
				}
			}
		});
	}

	/**
	 * Just open the URL in the system native browser
	 * @param address
	 */
	public static void viewInBrowser(URI address)
	{
		try
		{
			Desktop.getDesktop().browse(address);
		}
		catch (Exception e)
		{
			AppContext.getCommonDialogs().showErrorDialog("Error launching browser", "There was an error launching the web browser", null);
		}
	}
}
