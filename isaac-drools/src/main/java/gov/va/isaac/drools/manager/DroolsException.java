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
package gov.va.isaac.drools.manager;

import java.io.PrintStream;
import java.util.Collection;
import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.runtime.rule.Match;
import org.kie.api.runtime.rule.RuleRuntime;

/**
 * 
 * {@link DroolsException}
 *
 * Based on example at:
 * http://members.inode.at/w.laun/drools/CustomConsequenceExceptionHandlingHowTo.html
 * 
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DroolsException extends RuntimeException
{
	private static final long serialVersionUID = 1L;
	private RuleRuntime workingMemory;
	private Match activation;

	public DroolsException(Match match, RuleRuntime workingMemory, Exception exception)
	{
		super(exception);
		this.workingMemory = workingMemory;
		this.activation = match;
	}

	@Override
	public String getMessage()
	{
		StringBuilder sb = new StringBuilder("Exception executing consequence for ");
		Rule rule = null;

		if (activation != null && (rule = activation.getRule()) != null)
		{
			String packageName = rule.getPackageName();
			String ruleName = rule.getName();
			sb.append("rule \"").append(ruleName).append("\" in ").append(packageName);
		}
		else
		{
			sb.append("rule, name unknown");
		}
		sb.append(": ").append(super.getMessage());
		return sb.toString();
	}

	public void printFactDump()
	{
		printFactDump(System.err);
	}

	public void printFactDump(PrintStream pStream)
	{
		Collection<? extends FactHandle> handles = activation.getFactHandles();
		for (FactHandle handle : handles)
		{
			Object object = workingMemory.getObject(handle);
			if (object != null)
			{
				pStream.println("   Fact " + object.getClass().getSimpleName() + ": " + object.toString());
			}
		}
	}

	@Override
	public String toString()
	{
		return getMessage();
	}
}
