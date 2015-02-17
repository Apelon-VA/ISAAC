/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
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

import gov.va.isaac.AppContext;
import gov.va.isaac.drools.evaluators.facts.ConceptFact;
import gov.va.isaac.drools.evaluators.facts.DescFact;

import java.io.IOException;

import org.drools.core.base.BaseEvaluator;
import org.drools.core.base.ValueType;
import org.drools.core.base.evaluators.EvaluatorDefinition;
import org.drools.core.base.evaluators.Operator;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.RelAssertionType;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;

/**
 * 
 * {@link IsKindOfEvaluatorDefinition}
 *
 * Only includes stated view.
 * 
 * @author kec
 * @author afurber
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class IsKindOfEvaluatorDefinition extends IsaacBaseEvaluatorDefinition implements EvaluatorDefinition
{
	public static Operator IS_KIND_OF = Operator.addOperatorToRegistry("isKindOf", false);
	public static Operator NOT_IS_KIND_OF = Operator.addOperatorToRegistry(IS_KIND_OF.getOperatorString(), true);

	public static class IsKindOfEvaluator extends IsaacBaseEvaluator
	{
		public IsKindOfEvaluator()
		{
			super();
			// No arg constructor for serialization. 
		}

		public IsKindOfEvaluator(final ValueType type, final boolean isNegated)
		{
			super(type, isNegated ? IsKindOfEvaluatorDefinition.NOT_IS_KIND_OF : IsKindOfEvaluatorDefinition.IS_KIND_OF);
		}

		
		@Override
		protected boolean test(final Object value1, final Object value2)
		{
			try
			{
				ConceptVersionBI possibleKind = null;
				if (ConceptVersionBI.class.isAssignableFrom(value1.getClass()))
				{
					possibleKind = (ConceptVersionBI) value1;
				}
				else if (ConceptFact.class.isAssignableFrom(value1.getClass()))
				{
					possibleKind = ((ConceptFact) value1).getConcept();
				}
				else if (DescFact.class.isAssignableFrom(value1.getClass()))
				{
					DescriptionVersionBI<?> dv = ((DescFact) value1).getDesc();
					ViewCoordinate vc = ((DescFact) value1).getVc();
					possibleKind = AppContext.getService(TerminologyStoreDI.class).getConceptVersion(vc, dv.getConceptNid());
				}
				else
				{
					throw new UnsupportedOperationException("Can't convert: " + value1);
				}
				ViewCoordinate coordinate = possibleKind.getViewCoordinate();
				coordinate.setRelationshipAssertionType(RelAssertionType.STATED);
				possibleKind = possibleKind.getVersion(coordinate);
				ConceptVersionBI parentKind = null;

				if (ConceptVersionBI.class.isAssignableFrom(value2.getClass()))
				{
					parentKind = (ConceptVersionBI) value2;
				}
				else if (ConceptSpec.class.isAssignableFrom(value2.getClass()))
				{
					try
					{
						parentKind = ((ConceptSpec) value2).getStrict(coordinate);
					}
					catch (ValidationException ex)
					{
						return false;
					}
				}
				else if (ConceptFact.class.isAssignableFrom(value2.getClass()))
				{
					ConceptFact fact = (ConceptFact) value2;
					parentKind = (ConceptVersionBI) fact.getConcept();
				}
				return this.getOperator().isNegated() ^ (possibleKind.isKindOf(parentKind));
			}
			catch (IOException e)
			{
				return false;
			}
			catch (ContradictionException e)
			{
				return false;
			}
		}

		@Override
		public String toString()
		{
			return "IsKindOf isKindOf";
		}
	}

	/**
	 * @see gov.va.isaac.drools.evaluators.IsaacBaseEvaluatorDefinition#getId()
	 */
	@Override
	protected String getId()
	{
		return IS_KIND_OF.getOperatorString();
	}

	/**
	 * @see gov.va.isaac.drools.evaluators.IsaacBaseEvaluatorDefinition#buildEvaluator(org.drools.core.base.ValueType, boolean, String)
	 */
	@Override
	protected BaseEvaluator buildEvaluator(ValueType type, boolean isNegated, String parameterText)
	{
		return new IsKindOfEvaluator(type, isNegated);
	}
}
