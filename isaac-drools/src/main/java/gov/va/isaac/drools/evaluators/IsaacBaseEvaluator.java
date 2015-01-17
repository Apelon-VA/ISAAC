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
package gov.va.isaac.drools.evaluators;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import org.drools.core.base.BaseEvaluator;
import org.drools.core.base.ValueType;
import org.drools.core.base.evaluators.Operator;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.rule.VariableRestriction.ObjectVariableContextEntry;
import org.drools.core.rule.VariableRestriction.VariableContextEntry;
import org.drools.core.spi.FieldValue;
import org.drools.core.spi.InternalReadAccessor;

/**
 * {@link IsaacBaseEvaluator}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public abstract class IsaacBaseEvaluator extends BaseEvaluator
{
	private static final long serialVersionUID = 1L;
	protected int dataVersion = 1;
	
	abstract protected boolean test(final Object value1, final Object value2);
	
	protected IsaacBaseEvaluator()
	{
		// No arg for serialization
		super();
	}
	
	protected IsaacBaseEvaluator(ValueType type, Operator operator)
	{
		super(type, operator);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		super.readExternal(in);
		int objDataVersion = in.readInt();
		if (objDataVersion == dataVersion)
		{
			// Nothing to do
		}
		else
		{
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		super.writeExternal(out);
		out.writeInt(dataVersion);
	}
	
	@Override
	public boolean evaluate(InternalWorkingMemory workingMemory, InternalReadAccessor extractor, InternalFactHandle factHandle, FieldValue value)
	{
		return test(factHandle, value.getValue());
	}

	@Override
	public boolean evaluate(InternalWorkingMemory workingMemory, InternalReadAccessor leftExtractor, InternalFactHandle left, InternalReadAccessor rightExtractor,
			InternalFactHandle right)
	{
		final Object value1 = leftExtractor.getValue(workingMemory, left);
		final Object value2 = rightExtractor.getValue(workingMemory, right);

		return test(value1, value2);
	}
	
	@Override
	public boolean evaluateCachedLeft(InternalWorkingMemory workingMemory, VariableContextEntry context, InternalFactHandle right)
	{
		return test(((ObjectVariableContextEntry) context).left, right);
	}

	@Override
	public boolean evaluateCachedRight(InternalWorkingMemory workingMemory, VariableContextEntry context, InternalFactHandle left)
	{
		return test(left, ((ObjectVariableContextEntry) context).right);
	}
}
