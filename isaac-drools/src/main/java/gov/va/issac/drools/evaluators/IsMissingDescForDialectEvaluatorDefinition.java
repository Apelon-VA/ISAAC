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
package gov.va.issac.drools.evaluators;

import gov.va.issac.drools.dialect.DialectHelper;
import gov.va.issac.drools.dialect.UnsupportedDialectOrLanguage;
import gov.va.issac.drools.evaluators.facts.ConceptFact;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;

/**
 * 
 * {@link IsMissingDescForDialectEvaluatorDefinition}
 *
 * @author kec
 * @author afurber
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class IsMissingDescForDialectEvaluatorDefinition implements EvaluatorDefinition
{
	public static Operator IS_MISSING_DESC_FOR = Operator.addOperatorToRegistry("isMissingDescFor", false);
	public static Operator NOT_IS_MISSING_DESC_FOR = Operator.addOperatorToRegistry(IS_MISSING_DESC_FOR.getOperatorString(), true);

	public static class IsMissingDescForEvaluator extends BaseEvaluator
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

		public IsMissingDescForEvaluator()
		{
			super();
			// No arg constructor for serialization. 
		}

		public IsMissingDescForEvaluator(final ValueType type, final boolean isNegated)
		{
			super(type, isNegated ? IsMissingDescForDialectEvaluatorDefinition.NOT_IS_MISSING_DESC_FOR : IsMissingDescForDialectEvaluatorDefinition.IS_MISSING_DESC_FOR);
		}

		@Override
		public boolean evaluate(InternalWorkingMemory workingMemory, InternalReadAccessor extractor, InternalFactHandle factHandle, FieldValue value)
		{
			return testMissingDescForDialect(factHandle, value.getValue());
		}

		@Override
		public boolean evaluate(InternalWorkingMemory workingMemory, InternalReadAccessor leftExtractor, InternalFactHandle left, InternalReadAccessor rightExtractor,
				InternalFactHandle right)
		{
			final Object value1 = leftExtractor.getValue(workingMemory, left);
			final Object value2 = rightExtractor.getValue(workingMemory, right);

			return testMissingDescForDialect(value1, value2);
		}

		/**
		 * 
		 * @param value1 Concept to test descriptions for dialects
		 * @param value2 Concept representing the dialect to check
		 * @return
		 */
		private boolean testMissingDescForDialect(final Object value1, final Object value2)
		{

			//value1 this could be concept VersionBI or conceptFact
			//value2 this could be concept VersionBI or conceptFact

			ConceptVersionBI conceptToTest = null;
			if (ConceptVersionBI.class.isAssignableFrom(value1.getClass()))
			{
				conceptToTest = (ConceptVersionBI) value1;
			}
			else if (ConceptFact.class.isAssignableFrom(value1.getClass()))
			{
				conceptToTest = ((ConceptFact) value1).getConcept();
			}
			else
			{
				throw new UnsupportedOperationException("Can't convert: " + value1);
			}
			ViewCoordinate coordinate = conceptToTest.getViewCoordinate();

			ConceptVersionBI dialectCV = null;

			if (ConceptVersionBI.class.isAssignableFrom(value2.getClass()))
			{
				dialectCV = (ConceptVersionBI) value2;
			}
			else if (ConceptSpec.class.isAssignableFrom(value2.getClass()))
			{
				try
				{
					dialectCV = ((ConceptSpec) value2).getStrict(coordinate);
				}
				catch (IOException ex)
				{
					Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
					return false;
				}
			}
			else if (ConceptFact.class.isAssignableFrom(value2.getClass()))
			{
				ConceptFact fact = (ConceptFact) value2;
				dialectCV = (ConceptVersionBI) fact.getConcept();
			}
			int dialectNid = dialectCV.getNid();
			try
			{
				boolean missingDescForDialect = false;
				for (DescriptionVersionBI<?> desc : conceptToTest.getDescriptionsActive())
				{
					if (DialectHelper.isMissingDescForDialect(desc, dialectNid, coordinate))
					{
						missingDescForDialect = true;
						break;
					}
				}
				return this.getOperator().isNegated() ^ (missingDescForDialect);
			}
			catch (UnsupportedDialectOrLanguage ex)
			{
				Logger.getLogger(IsMissingDescForDialectEvaluatorDefinition.class.getName()).log(Level.SEVERE,
						"Cannot test secondary to " + "unsupported dialect: " + dialectCV, ex);
				return false;
			}
			catch (ContradictionException ex)
			{
				Logger.getLogger(IsMissingDescForDialectEvaluatorDefinition.class.getName()).log(Level.SEVERE, "Cannot test secondary to contradiction", ex);
				return false;
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		@Override
		public boolean evaluateCachedLeft(InternalWorkingMemory workingMemory, VariableContextEntry context, InternalFactHandle right)
		{
			return testMissingDescForDialect(((ObjectVariableContextEntry) context).left, right);
		}

		@Override
		public boolean evaluateCachedRight(InternalWorkingMemory workingMemory, VariableContextEntry context, InternalFactHandle left)
		{
			return testMissingDescForDialect(left, ((ObjectVariableContextEntry) context).right);
		}

		@Override
		public String toString()
		{
			return "IsMissingDescForDialect isMissingDescForDialect";
		}
	}
}
