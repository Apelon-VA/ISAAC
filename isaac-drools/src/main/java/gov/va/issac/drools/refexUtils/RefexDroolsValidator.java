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
package gov.va.issac.drools.refexUtils;

import gov.va.isaac.AppContext;
import gov.va.issac.drools.helper.ResultsCollector;
import gov.va.issac.drools.helper.ResultsItem;
import gov.va.issac.drools.manager.DroolsExecutor;
import gov.va.issac.drools.manager.DroolsExecutorsManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicStringBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RefexDroolsValidator}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */

public class RefexDroolsValidator
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

			if (dataToValidate instanceof RefexDynamicStringBI)
			{
				RefexDynamicStringBI stringValue = (RefexDynamicStringBI) dataToValidate;
				facts.add(stringValue);
			}
			else
			{
				throw new RuntimeException("No implementation exists for validating the datatype " + dataToValidate.getClass().getName());
			}

			Map<String, Object> globals = new HashMap<>();

			ResultsCollector rc = new ResultsCollector();

			globals.put("resultsCollector", rc);

			int fireCount = de.fireAllRules(globals, facts);

			if (fireCount > 0)
			{
				StringBuilder sb = new StringBuilder();
				for (ResultsItem r : rc.getResultsItems())
				{
					logger.debug("Drools rule fired during refex validation with severity {}, error code {}, rule ID {}, message {}", r.getSeverity().getName(),
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
			throw new RuntimeException("Unexpected error validating with drools");
		}
		return true;
	}
}
