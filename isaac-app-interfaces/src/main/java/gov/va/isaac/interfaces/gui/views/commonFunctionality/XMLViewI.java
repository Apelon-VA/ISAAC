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
package gov.va.isaac.interfaces.gui.views.commonFunctionality;

import gov.va.isaac.interfaces.gui.views.PopupViewI;
import java.util.function.Supplier;
import org.jvnet.hk2.annotations.Contract;


/**
 * {@link XMLViewI}
 *
 * Extends the IsaacViewI to provide a View which will pop up a new window on top of the parent 
 * window when the showView method is called.
 * 
 * Implementations are intended to simply display the specified XML in a window.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
@Contract
public interface XMLViewI extends PopupViewI
{
	/**
	 * Display this popup view to the user in front of the specified parent window.
	 * The PopupViewI implementation is responsible for showing itself when this method 
	 * is called. 
	 * 
	 * The content is passed in via the Supplier interface, so that if gathering the XML is expensive, 
	 * good implementations can background thread this job.
	 */
	public void setParameters(String title, Supplier<String> xmlContent, int width, int height);
}
