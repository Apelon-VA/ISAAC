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
package gov.va.issac.drools.evaluators;

import gov.va.issac.drools.evaluators.facts.ConceptFact;
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
import org.drools.runtime.rule.EvaluatorDefinition;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.constraint.ConstraintBI;
import org.ihtsdo.otf.tcc.api.constraint.ConstraintCheckType;

/**
 * 
 * {@link SatisfiesConstraintEvaluatorDefinition}
 * 
 * @author kec
 * @author afurber
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class SatisfiesConstraintEvaluatorDefinition implements EvaluatorDefinition
{
	public static final Operator SATISFIES_CONSTRAINT = Operator.addOperatorToRegistry("satisfiesConstraint", false);
	public static final Operator NOT_SATISFIES_CONSTRAINT = Operator.addOperatorToRegistry(SATISFIES_CONSTRAINT.getOperatorString(), true);

	private static final String DEFAULT_PARAMETERS = "x,e,e";

	public static class SatisfiesConstraintEvaluator extends BaseEvaluator
	{
		private static final long serialVersionUID = 1L;
		private static final int dataVersion = 1;

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
		{
			super.readExternal(in);
			int objDataVersion = in.readInt();
			if (objDataVersion == dataVersion)
			{
				subjectCheck = (ConstraintCheckType) in.readObject();
				propertyCheck = (ConstraintCheckType) in.readObject();
				valueCheck = (ConstraintCheckType) in.readObject();
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
			out.writeObject(subjectCheck);
			out.writeObject(propertyCheck);
			out.writeObject(valueCheck);
		}
		private ConstraintCheckType subjectCheck;
		private ConstraintCheckType propertyCheck;
		private ConstraintCheckType valueCheck;

		public SatisfiesConstraintEvaluator()
		{
			super();
			// No arg constructor for serialization. 
		}

		public SatisfiesConstraintEvaluator(final ValueType type, final boolean isNegated, String parameterText)
		{
			super(type, isNegated ? IsKindOfEvaluatorDefinition.NOT_IS_KIND_OF : IsKindOfEvaluatorDefinition.IS_KIND_OF);
			if (parameterText == null)
			{
				parameterText = DEFAULT_PARAMETERS;
			}
			String[] params = parameterText.toLowerCase().replace(',', ' ').split("\\s+");
			subjectCheck = ConstraintCheckType.get(params[0]);
			propertyCheck = ConstraintCheckType.get(params[1]);
			valueCheck = ConstraintCheckType.get(params[2]);
		}

		@Override
		public boolean evaluate(InternalWorkingMemory workingMemory, InternalReadAccessor extractor, InternalFactHandle factHandle, FieldValue value)
		{
			return testSatisfiesConstraint(factHandle, value.getValue());
		}

		@Override
		public boolean evaluate(InternalWorkingMemory workingMemory, InternalReadAccessor leftExtractor, InternalFactHandle left, InternalReadAccessor rightExtractor,
				InternalFactHandle right)
		{
			final Object value1 = leftExtractor.getValue(workingMemory, left);
			final Object value2 = rightExtractor.getValue(workingMemory, right);

			return testSatisfiesConstraint(value1, value2);
		}

		private boolean testSatisfiesConstraint(final Object value1, final Object value2)
		{
			try
			{
				ConceptVersionBI conceptVersion = null;
				if (ConceptVersionBI.class.isAssignableFrom(value1.getClass()))
				{
					conceptVersion = (ConceptVersionBI) value1;
				}
				else if (ConceptFact.class.isAssignableFrom(value1.getClass()))
				{
					conceptVersion = ((ConceptFact) value1).getConcept();
				}
				else
				{
					throw new UnsupportedOperationException("Can't convert: " + value1);
				}
				ConstraintBI constraint = (ConstraintBI) value2;
				return this.getOperator().isNegated() ^ (conceptVersion.satisfies(constraint, subjectCheck, propertyCheck, valueCheck));
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}

		@Override
		public boolean evaluateCachedLeft(InternalWorkingMemory workingMemory, VariableContextEntry context, InternalFactHandle right)
		{
			return testSatisfiesConstraint(((ObjectVariableContextEntry) context).left, right);
		}

		@Override
		public boolean evaluateCachedRight(InternalWorkingMemory workingMemory, VariableContextEntry context, InternalFactHandle left)
		{
			return testSatisfiesConstraint(left, ((ObjectVariableContextEntry) context).right);
		}

		@Override
		public String toString()
		{
			return "SatisfiesConstraint satisfiesConstraint";
		}

		public ConstraintCheckType getSubjectCheck()
		{
			return subjectCheck;
		}

		public void setSubjectCheck(ConstraintCheckType subjectCheck)
		{
			this.subjectCheck = subjectCheck;
		}

		public ConstraintCheckType getPropertyCheck()
		{
			return propertyCheck;
		}

		public void setPropertyCheck(ConstraintCheckType propertyCheck)
		{
			this.propertyCheck = propertyCheck;
		}

		public ConstraintCheckType getValueCheck()
		{
			return valueCheck;
		}

		public void setValueCheck(ConstraintCheckType valueCheck)
		{
			this.valueCheck = valueCheck;
		}
	}

}
