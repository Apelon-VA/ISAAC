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
package gov.va.isaac.drools.refexUtils;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.drools.helper.ResultsCollector;
import gov.va.isaac.drools.helper.ResultsItem;
import gov.va.isaac.drools.helper.TerminologyHelperDrools;
import gov.va.isaac.drools.manager.DroolsExecutor;
import gov.va.isaac.drools.manager.DroolsExecutorsManager;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.inject.Named;
import javax.inject.Singleton;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.ExternalValidatorBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicArrayBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicNidBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicStringBI;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicArray;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicString;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicUUID;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RefexDroolsValidator}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */

@Service
@Singleton
@Named("RefexDroolsValidator")
public class RefexDroolsValidator implements ExternalValidatorBI
{
	private static Logger logger = LoggerFactory.getLogger(RefexDroolsValidator.class);

	/**
	 * Returns true if the data is valid, otherwise, throws an error
	 * 
	 * @param droolsValidatorName - The validator to use. Must be present in {@link DroolsExecutorsManager#getLoadedExecutors()}
	 * @param dataToValidate - The data to pass into the validator.
	 * @return true if valid. Exception otherwise.
	 * @throws RuntimeException - if the validation fails. User-friendly message will be in the error.
	 */
	public static boolean validate(String droolsValidatorName, RefexDynamicDataBI dataToValidate)
	{
		try
		{
			DroolsExecutorsManager dem = AppContext.getService(DroolsExecutorsManager.class);

			DroolsExecutor de = dem.getDroolsExecutor(droolsValidatorName);

			if (de == null)
			{
				throw new RuntimeException("The requested drools validator '" + droolsValidatorName + "' is not available");
			}

			ArrayList<Object> facts = new ArrayList<>();

			if (dataToValidate.getRefexDataType() == RefexDynamicDataType.NID)
			{
				//switch it to a UUID, for drools purposes.
				UUID temp = ExtendedAppContext.getDataStore().getUuidsForNid(((RefexDynamicNidBI)dataToValidate).getDataNid()).get(0);
				facts.add(new RefexDynamicUUID(temp));
			}
			else
			{
				facts.add(dataToValidate);
			}

			Map<String, Object> globals = new HashMap<>();

			ResultsCollector rc = new ResultsCollector();

			globals.put("resultsCollector", rc);
			globals.put("terminologyHelper", new TerminologyHelperDrools());

			int fireCount = de.fireAllRules(globals, facts);

			if (fireCount > 0)
			{
				StringBuilder sb = new StringBuilder();
				for (ResultsItem r : rc.getResultsItems())
				{
					logger.debug("Drools rule fired during sememe validation with severity {}, error code {}, rule ID {}, message {}", r.getSeverity().getName(),
							r.getErrorCode(), r.getRuleUuid(), r.getMessage());
					sb.append(r.getMessage());
					sb.append(", ");
				}

				if (sb.length() > 2)
				{
					sb.setLength(sb.length() - 2);
				}
				else
				{
					logger.error("Oops.  Rule fired - but no message?");
					sb.append("Error - rule fired, but no message?");
				}
				throw new RuntimeException(sb.toString());
			}

		}
		catch (RuntimeException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			logger.error("Unexpected error validating with drools", e);
			throw new RuntimeException("Unexpected error validating with drools");
		}
		return true;
	}

	public static RefexDynamicArray<RefexDynamicString> createValidatorDefinitionData(RefexDroolsValidatorImplInfo rdvii)
	{
		try
		{
			return new RefexDynamicArray<RefexDynamicString>(
					new RefexDynamicString[]{new RefexDynamicString("RefexDroolsValidator"), new RefexDynamicString(rdvii.name())});
		}
		catch (PropertyVetoException e)
		{
			//not possible
			logger.error("oops");
			throw new RuntimeException(e);
		}
	}

	/**
	 * In our implementation - the validatorDefinitionData contains two things - the first - is the @name of this implementation of an
	 * {@link ExternalValidatorBI} - for example "RefexDroolsValidator" - the rest is corresponding name from the 
	 * {@link RefexDroolsValidatorImplInfo} enum String[]{"RefexDroolsValidator", "REFEX_STRING_RULES"}
	 * 
	 * @param validatorDefinitionData
	 * @throws RuntimeException if the input data can't be parsed as expected.
	 * @return - null, if input is null, or no drools impl is mapped to the 2nd part of the data. 
	 */
	public static RefexDroolsValidatorImplInfo readFromData(RefexDynamicDataBI validatorDefinitionData) throws RuntimeException
	{
		try
		{
			if (validatorDefinitionData == null)
			{
				return null;
			}
			@SuppressWarnings("unchecked")
			RefexDynamicStringBI[] validatorInfo = ((RefexDynamicArrayBI<RefexDynamicStringBI>)validatorDefinitionData).getDataArray();
			if (validatorInfo[0].getDataString().equals("RefexDroolsValidator"))
			{
				return RefexDroolsValidatorImplInfo.valueOf(validatorInfo[1].getDataString());
			}
			else
			{
				throw new RuntimeException("The name mapping for the validator does not match this class!");
			}
		}
		catch (ClassCastException | IndexOutOfBoundsException e)
		{
			throw new RuntimeException("The incoming value (" + validatorDefinitionData.getDataObject().toString() + " doesn't match the expected format");
		}
		catch (RuntimeException e)
		{
			throw e;
		}
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.ExternalValidatorBI#validate(org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI,
	 * org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicStringBI, org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate)
	 */
	@Override
	public boolean validate(RefexDynamicDataBI userData, RefexDynamicArrayBI<RefexDynamicStringBI> validatorDefinitionData, ViewCoordinate vc) throws RuntimeException
	{
		RefexDroolsValidatorImplInfo rdvi = readFromData(validatorDefinitionData);
		if (rdvi == null)
		{
			throw new RuntimeException("The specified validator is not mapped - cannot validate");
		}

		for (RefexDynamicDataType rddt : rdvi.getApplicableDataTypes())
		{
			if (userData.getRefexDataType() == rddt)
			{
				return RefexDroolsValidator.validate(rdvi.getDroolsPackageName(), userData);
			}
		}

		throw new RuntimeException("The selected drools validator doesn't apply to the datatype '" + userData.getRefexDataType().getDisplayName() + "'");
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.ExternalValidatorBI#validatorSupportsType(org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicStringBI, org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType)
	 */
	@Override
	public boolean validatorSupportsType(RefexDynamicArrayBI<RefexDynamicStringBI> validatorDefinitionData, RefexDynamicDataType dataType)
	{
		RefexDroolsValidatorImplInfo rdvi = readFromData(validatorDefinitionData);
		if (rdvi == null)
		{
			throw new RuntimeException("The specified validator is not mapped - cannot validate");
		}

		for (RefexDynamicDataType rddt : rdvi.getApplicableDataTypes())
		{
			if (rddt == dataType)
			{
				return true;
			}
		}
		return false;
	}
}
