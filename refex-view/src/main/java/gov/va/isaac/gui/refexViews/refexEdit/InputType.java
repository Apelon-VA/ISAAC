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
package gov.va.isaac.gui.refexViews.refexEdit;

import gov.va.isaac.ExtendedAppContext;
import java.io.IOException;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;

public class InputType
{
	private Integer componentNid_;
	private Integer assemblyNid_;
	private RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>> refex_;
	
	private ComponentChronicleBI<?> componentCache_;
	
	protected InputType(RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>> refex)
	{
		refex_ = refex;
	}
	
	protected InputType(int nid, boolean isAssembly)
	{
		if (isAssembly)
		{
			assemblyNid_ = nid;
			componentNid_ = null;
		}
		else
		{
			assemblyNid_ = null;
			componentNid_ = nid;
		}
	}
	
	public Integer getComponentNid()
	{
		return componentNid_;
	}
	
	public ComponentChronicleBI<?> getComponentBI() throws IOException
	{
		if (componentNid_ == null)
		{
			return null;
		}
		if (componentCache_ == null)
		{
			componentCache_ = ExtendedAppContext.getDataStore().getComponent(componentNid_);
		}
		return componentCache_;
	}
	
	public Integer getAssemblyNid()
	{
		return assemblyNid_;
	}
	
	public RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>> getRefex()
	{
		return refex_;
	}
}