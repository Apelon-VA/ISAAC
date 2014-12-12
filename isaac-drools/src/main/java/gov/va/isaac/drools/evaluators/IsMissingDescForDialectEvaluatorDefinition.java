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
package gov.va.isaac.drools.evaluators;

import gov.va.isaac.drools.dialect.DialectHelper;
import gov.va.isaac.drools.dialect.UnsupportedDialectOrLanguage;
import gov.va.isaac.drools.evaluators.facts.ConceptFact;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.drools.core.base.BaseEvaluator;
import org.drools.core.base.ValueType;
import org.drools.core.base.evaluators.Operator;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.kie.api.runtime.rule.EvaluatorDefinition;

/**
 * 
 * {@link IsMissingDescForDialectEvaluatorDefinition}
 *
 * @author kec
 * @author afurber
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class IsMissingDescForDialectEvaluatorDefinition extends IsaacBaseEvaluatorDefinition implements EvaluatorDefinition
{
	public static Operator IS_MISSING_DESC_FOR = Operator.addOperatorToRegistry("isMissingDescFor", false);
	public static Operator NOT_IS_MISSING_DESC_FOR = Operator.addOperatorToRegistry(IS_MISSING_DESC_FOR.getOperatorString(), true);

	public static class IsMissingDescForEvaluator extends IsaacBaseEvaluator
	{
		public IsMissingDescForEvaluator()
		{
			super();
			// No arg constructor for serialization. 
		}

		public IsMissingDescForEvaluator(final ValueType type, final boolean isNegated)
		{
			super(type, isNegated ? IsMissingDescForDialectEvaluatorDefinition.NOT_IS_MISSING_DESC_FOR : IsMissingDescForDialectEvaluatorDefinition.IS_MISSING_DESC_FOR);
		}

		/**
		 * 
		 * @param value1 Concept to test descriptions for dialects
		 * @param value2 Concept representing the dialect to check
		 * @return
		 */
		@Override
		protected boolean test(final Object value1, final Object value2)
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
		public String toString()
		{
			return "IsMissingDescForDialect isMissingDescForDialect";
		}
	}

	/**
	 * @see gov.va.isaac.drools.evaluators.IsaacBaseEvaluatorDefinition#getId()
	 */
	@Override
	protected String getId()
	{
		return IS_MISSING_DESC_FOR.getOperatorString();
	}

	/**
	 * @see gov.va.isaac.drools.evaluators.IsaacBaseEvaluatorDefinition#buildEvaluator(org.drools.core.base.ValueType, boolean, String)
	 */
	@Override
	protected BaseEvaluator buildEvaluator(ValueType type, boolean isNegated, String parameterText)
	{
		return new IsMissingDescForEvaluator(type, isNegated);
	}
}
