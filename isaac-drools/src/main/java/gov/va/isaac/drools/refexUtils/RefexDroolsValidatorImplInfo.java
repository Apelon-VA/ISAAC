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
package gov.va.isaac.drools.refexUtils;

import gov.va.isaac.drools.manager.DroolsExecutorsManager;

import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;

/**
 * {@link RefexDroolsValidatorImplInfo}
 * 
 * Stores various information and mappings about the known .drl files that are shipped with the application, so that the
 * GUI can dynamically do useful things with them.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public enum RefexDroolsValidatorImplInfo
{
	REFEX_STRING_RULES("builtin.refex-string-rules", "Drools Rules for String values", RefexDynamicDataType.STRING),
	REFEX_CONCEPT_RULES("builtin.refex-concept-rules", "Drools Rules for Concept values", RefexDynamicDataType.UUID, RefexDynamicDataType.NID);
	
	private String droolsPackageName_, displayName_;
	private RefexDynamicDataType[] applicableDataTypes_;
	
	private RefexDroolsValidatorImplInfo(String droolsPackageName, String displayName, RefexDynamicDataType ... applicableDataTypes)
	{
		displayName_ = displayName;
		droolsPackageName_ = droolsPackageName;
		applicableDataTypes_ = applicableDataTypes;
	}
	
	public static RefexDroolsValidatorImplInfo getByDroolsPackageName(String droolsPackageName)
	{
		for (RefexDroolsValidatorImplInfo rdv : RefexDroolsValidatorImplInfo.values())
		{
			if (rdv.getDroolsPackageName().equals(droolsPackageName))
			{
				return rdv;
			}
		}
		return null;
	}

	/**
	 * @return the droolsPackageName - used for mapping to {@link DroolsExecutorsManager#getDroolsExecutor(String)}
	 */
	public String getDroolsPackageName()
	{
		return droolsPackageName_;
	}

	/**
	 * @return the displayName - A user friendly name for GUI useage
	 */
	public String getDisplayName()
	{
		return displayName_;
	}

	/**
	 * @return the applicableDataTypes - which data type will the drools package run on (drools packages ignore datatypes they don't know about)
	 */
	public RefexDynamicDataType[] getApplicableDataTypes()
	{
		return applicableDataTypes_;
	}
}
