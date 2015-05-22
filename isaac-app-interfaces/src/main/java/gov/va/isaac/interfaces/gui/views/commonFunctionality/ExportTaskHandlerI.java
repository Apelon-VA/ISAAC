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

import java.nio.file.Path;
import java.util.Properties;
import java.util.stream.IntStream;

import javafx.concurrent.Task;

import org.jvnet.hk2.annotations.Contract;

@Contract
public interface ExportTaskHandlerI {
	
	/**
	 * Set the configuration properties for this specific exporter 
	 * 
	 * @param options
	 */
	public void setOptions(Properties options);
	
	/**
	 * Exporter Title
	 * 
	 * @return String the title
	 */
	public String getTitle();
	
	// protected void conceptListChanged(); - possibly not
		
	public String getDescription();
	
	/**
	 * The integer returned should equal the number of components exported.
	 * 
	 * @param nidList
	 * @param filePath
	 * @return
	 */
	public Task<Integer> createTask(IntStream nidList, Path file);
	

}
