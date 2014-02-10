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
package gov.va.isaac.gui;

import gov.va.isaac.gui.interfaces.ApplicationWindowI;
import javafx.stage.Stage;
import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;

/**
 * ApplicationWindowImpl
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Singleton
public class ApplicationWindowImpl implements ApplicationWindowI
{

	//TODO cleanup this hack of a class...
	private Stage primaryStage_;

	private ApplicationWindowImpl()
	{
		//HK2...
	}
	
	protected void init(Stage primaryStage)
	{
		if (primaryStage_ != null)
		{
			throw new RuntimeException("oops");
		}
		primaryStage_ = primaryStage;
	}

	/**
	 * @see gov.va.isaac.gui.interfaces.ApplicationWindowI#getPrimaryStage()
	 */
	@Override
	public Stage getPrimaryStage()
	{
		return primaryStage_;
	}

}
