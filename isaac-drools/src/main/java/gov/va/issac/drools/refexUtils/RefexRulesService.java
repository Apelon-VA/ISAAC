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

import gov.va.issac.drools.manager.DroolsExecutorsManager;
import javax.inject.Named;
import javax.inject.Singleton;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.ExternalValidatorBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.jvnet.hk2.annotations.Service;

/**
 * {@link RefexRulesService}
 *
 * The value 'RefexDroolsValidator||refex-rules' will be put into the refex column definition, 
 * so that the OTF code can lookup this particular service by name.  The portion after the | 
 * contains the string that must match to the specific drools validator we want to run against - 
 * it should match a value found in {@link DroolsExecutorsManager#getLoadedExecutors()}
 * 
 * But that is just a convenience - there is no code that enforces this.  In practice, 
 * we will just create a new Service (like this class) for any other drools rule package that we 
 * want to have as a separate entity.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Singleton
@Named("RefexDroolsValidator|refex-rules")
public class RefexRulesService implements ExternalValidatorBI
{
	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.ExternalValidatorBI#validate(org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI,
	 * org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate)
	 */
	@Override
	public boolean validate(RefexDynamicDataBI userData, ViewCoordinate vc) throws RuntimeException
	{
		return RefexDroolsValidator.validate("refex-rules", userData);
	}
}
