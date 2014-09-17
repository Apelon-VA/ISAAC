/**
 * Copyright 2011 International Health Terminology Standards Development Organisation.
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

package gov.va.issac.drools.manager;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import org.drools.runtime.rule.Activation;
import org.drools.runtime.rule.ConsequenceExceptionHandler;
import org.drools.runtime.rule.WorkingMemory;

/**
 * 
 * {@link DroolsExceptionHandler}
 *
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DroolsExceptionHandler implements ConsequenceExceptionHandler, Externalizable
{
	@Override
	public void writeExternal(ObjectOutput oo) throws IOException
	{
		// nothing to do
	}

	@Override
	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException
	{
		// nothing to do. 
	}

	@Override
	public void handleException(Activation actvtn, WorkingMemory wm, Exception ex)
	{
		throw new DroolsException(ex, wm, actvtn);
	}
}
