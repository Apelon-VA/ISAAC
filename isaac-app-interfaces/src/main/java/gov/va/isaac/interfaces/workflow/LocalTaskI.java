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

/**
 * LocalTaskI
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.interfaces.workflow;

import java.util.Comparator;
import java.util.Map;

/**
 * LocalTaskI
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public interface LocalTaskI {

	public static final Comparator<LocalTaskI> ID_COMPARATOR = (LocalTaskI o1, LocalTaskI o2) -> (Long.valueOf(o1.getId()).compareTo(Long.valueOf(o2.getId())));
	public static final Comparator<LocalTaskI> NAME_COMPARATOR = (LocalTaskI o1, LocalTaskI o2) -> o1.getName().compareTo(o2.getName());

	public abstract Long getId();

	public abstract void setId(Long id);

	public abstract String getName();

	public abstract void setName(String name);

	public abstract String getComponentId();

	public abstract void setComponentId(String componentId);

	public abstract String getComponentName();

	public abstract void setComponentName(String componentName);

	public abstract String getStatus();

	public abstract void setStatus(String status);

	public abstract String getOwner();

	public abstract void setOwner(String owner);

	public abstract String getAction();

	public abstract void setAction(String action);

	public abstract String getActionStatus();

	public abstract void setActionStatus(String actionStatus);

	public abstract Map<String, String> getInputVariables();

	public abstract void setInputVariables(Map<String, String> inputVariables);

	public abstract Map<String, String> getOutputVariables();

	public abstract void setOutputVariables(Map<String, String> outputVariables);

}