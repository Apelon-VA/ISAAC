/**
 * Copyright 2011 International Health Terminology Standards Development Organisation.
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
package gov.va.isaac.drools.evaluators.facts;

import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;

/**
 * 
 * {@link RefexFact}
 *
 * @author kec
 * @author afurber
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefexFact extends ComponentFact<RefexVersionBI<?>>
{

	public RefexFact(Context context, RefexVersionBI<?> component, ViewCoordinate vc)
	{
		super(context, component, vc);
	}

	public RefexVersionBI<?> getRefex()
	{
		return component;
	}
}
