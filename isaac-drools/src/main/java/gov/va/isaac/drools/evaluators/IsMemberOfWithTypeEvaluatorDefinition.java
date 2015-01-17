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
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.drools.core.base.BaseEvaluator;
import org.drools.core.base.ValueType;
import org.drools.core.base.evaluators.Operator;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_boolean.RefexBooleanVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_int.RefexIntVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_string.RefexStringVersionBI;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.kie.api.runtime.rule.EvaluatorDefinition;

/**
 * 
 * {@link IsMemberOfWithTypeEvaluatorDefinition}
 *
 * @author kec
 * @author afurber
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class IsMemberOfWithTypeEvaluatorDefinition extends IsaacBaseEvaluatorDefinition implements EvaluatorDefinition
{
	public static Operator IS_MEMBER_OF_WITH_TYPE = Operator.addOperatorToRegistry("isMemberOfWithType", false);
	public static Operator NOT_IS_MEMBER_OF_WITH_TYPE = Operator.addOperatorToRegistry(IS_MEMBER_OF_WITH_TYPE.getOperatorString(), true);

	public static class IsMemberOfWithTypeEvaluator extends IsaacBaseEvaluator
	{
		public IsMemberOfWithTypeEvaluator()
		{
			super();
			// No arg constructor for serialization. 
		}

		public IsMemberOfWithTypeEvaluator(final ValueType type, final boolean isNegated)
		{
			super(type, isNegated ? IsMemberOfWithTypeEvaluatorDefinition.NOT_IS_MEMBER_OF_WITH_TYPE : IsMemberOfWithTypeEvaluatorDefinition.IS_MEMBER_OF_WITH_TYPE);
		}
		
		@Override
		protected boolean test(final Object value1, final Object value2)
		{

			boolean isMember = false;
			/*
			 * value1 (concept): this could be concept VersionBI, ConceptFact, or DescriptionFact
			 * value2 (refset, type): this is an array of concept specs
			 * where ConceptSpec[0] = the concept spec of the refex
			 * and ConceptSpec[1] = the concept spec of the type being tested
			 */

			Object[] refexTypeArray = (Object[]) value2;
			Object refexThing = refexTypeArray[0];
			ConceptSpec refexConceptSpec = null;
			if (ConceptSpec.class.isAssignableFrom(refexThing.getClass()))
			{
				refexConceptSpec = (ConceptSpec) refexTypeArray[0];
			}
			else
			{
				throw new UnsupportedOperationException("The first object in the array must be a ConceptSpec.");
			}
			Object typeThing = refexTypeArray[1];

			try
			{
				if (!AppContext.getService(TerminologyStoreDI.class).hasUuid(refexConceptSpec.getLenient().getPrimordialUuid()))
				{
					return false;
				}
			}
			catch (ValidationException ex)
			{
				//do nothing
			}
			catch (IOException ex)
			{
				//do nothing
			}

			if (ConceptFact.class.isAssignableFrom(value1.getClass()))
			{
				isMember = testConcept((ConceptFact) value1, refexConceptSpec, typeThing);
			}
			else if (DescFact.class.isAssignableFrom(value1.getClass()))
			{
				DescFact dFact = (DescFact) value1;
				isMember = testDesc(dFact, refexConceptSpec, typeThing);
			}
			else
			{
				throw new UnsupportedOperationException("Can't convert: " + value1);
			}

			return this.getOperator().isNegated() ^ (isMember);

		}

		private boolean testDesc(DescFact dFact, ConceptSpec refexConceptSpec, final Object typeThing)
		{
			boolean member = false;

			try
			{
				DescriptionVersionBI<?> desc = dFact.getComponent();
				ViewCoordinate vc = dFact.getVc();

				int evalRefsetNid = refexConceptSpec.getStrict(vc).getNid();

				Collection<? extends RefexVersionBI<?>> refexes = desc.getRefexMembersActive(vc, evalRefsetNid);

				if (refexes != null && refexes.size() > 0)
				{
					member = true;
				}

				if (member)
				{

					if (ConceptSpec.class.isAssignableFrom(typeThing.getClass()))
					{
						ConceptSpec conceptSpecType = (ConceptSpec) typeThing;
						for (RefexVersionBI<?> refex : refexes)
						{
							RefexNidVersionBI<?> conceptRefex = (RefexNidVersionBI<?>) refex;
							if (conceptRefex.getNid1() == conceptSpecType.getStrict(vc).getConceptNid())
							{
								return true;
							}
						}
					}
					else if (String.class.isAssignableFrom(typeThing.getClass()))
					{
						String stringType = (String) typeThing;
						for (RefexVersionBI<?> refex : refexes)
						{
							RefexStringVersionBI<?> conceptRefex = (RefexStringVersionBI<?>) refex;
							if (conceptRefex.getString1().equals(stringType))
							{
								return true;
							}
						}
					}
					else if (Integer.class.isAssignableFrom(typeThing.getClass()))
					{
						Integer integerType = (Integer) typeThing;
						for (RefexVersionBI<?> refex : refexes)
						{
							RefexIntVersionBI<?> conceptRefex = (RefexIntVersionBI<?>) refex;
							if (conceptRefex.getInt1() == integerType.intValue())
							{
								return true;
							}
						}
					}
					else if (Boolean.class.isAssignableFrom(typeThing.getClass()))
					{
						Boolean booleanType = (Boolean) typeThing;
						for (RefexVersionBI<?> refex : refexes)
						{
							RefexBooleanVersionBI<?> conceptRefex = (RefexBooleanVersionBI<?>) refex;
							if (conceptRefex.getBoolean1() == booleanType.booleanValue())
							{
								return true;
							}
						}
					}
					else
					{
						throw new UnsupportedOperationException("Can only handle sememe type of concept, string, integer, and boolean. "
								+ "Given type did not match any of these");
					}
				}

				return false;
			}
			catch (IOException e)
			{
				Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "refset concept not found", e);
				return member;
			}
		}

		private boolean testConcept(ConceptFact conceptFact, ConceptSpec refexConceptSpec, final Object typeThing)
		{
			boolean member = false;

			try
			{
				ConceptVersionBI conceptVersion = conceptFact.getConcept();
				ViewCoordinate vc = conceptFact.getVc();

				int evalRefsetNid = refexConceptSpec.getStrict(vc).getNid();

				Collection<? extends RefexVersionBI<?>> refexes = conceptVersion.getRefexMembersActive(vc, evalRefsetNid);

				if (refexes != null)
				{
					member = true;
				}

				if (member)
				{

					if (ConceptSpec.class.isAssignableFrom(typeThing.getClass()))
					{
						ConceptSpec conceptSpecType = (ConceptSpec) typeThing;
						for (RefexVersionBI<?> refex : refexes)
						{
							RefexNidVersionBI<?> conceptRefex = (RefexNidVersionBI<?>) refex;
							if (conceptRefex.getNid1() == conceptSpecType.getStrict(vc).getConceptNid())
							{
								return true;
							}
						}
					}
					else if (String.class.isAssignableFrom(typeThing.getClass()))
					{
						String stringType = (String) typeThing;
						for (RefexVersionBI<?> refex : refexes)
						{
							RefexStringVersionBI<?> conceptRefex = (RefexStringVersionBI<?>) refex;
							if (conceptRefex.getString1().equals(stringType))
							{
								return true;
							}
						}
					}
					else if (Integer.class.isAssignableFrom(typeThing.getClass()))
					{
						Integer integerType = (Integer) typeThing;
						for (RefexVersionBI<?> refex : refexes)
						{
							RefexIntVersionBI<?> conceptRefex = (RefexIntVersionBI<?>) refex;
							if (conceptRefex.getInt1() == integerType.intValue())
							{
								return true;
							}
						}
					}
					else if (Boolean.class.isAssignableFrom(typeThing.getClass()))
					{
						Boolean booleanType = (Boolean) typeThing;
						for (RefexVersionBI<?> refex : refexes)
						{
							RefexBooleanVersionBI<?> conceptRefex = (RefexBooleanVersionBI<?>) refex;
							if (conceptRefex.getBoolean1() == booleanType.booleanValue())
							{
								return true;
							}
						}
					}
					else
					{
						throw new UnsupportedOperationException("Can only handle sememe type of concept, string, integer, and boolean. "
								+ "Given type did not match any of these");
					}
				}

				return false;
			}
			catch (IOException e)
			{
				Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "refset concept not found", e);
				return member;
			}
		}

		@Override
		public String toString()
		{
			return "IsMemberOfWithType isMemberOfWithType";
		}
	}

	/**
	 * @see gov.va.isaac.drools.evaluators.IsaacBaseEvaluatorDefinition#getId()
	 */
	@Override
	protected String getId()
	{
		return IS_MEMBER_OF_WITH_TYPE.getOperatorString();
	}

	/**
	 * @see gov.va.isaac.drools.evaluators.IsaacBaseEvaluatorDefinition#buildEvaluator(org.drools.core.base.ValueType, boolean)
	 */
	@Override
	protected BaseEvaluator buildEvaluator(ValueType type, boolean isNegated, String parameterText)
	{
		return new IsMemberOfWithTypeEvaluator(type, isNegated);
	}
}
