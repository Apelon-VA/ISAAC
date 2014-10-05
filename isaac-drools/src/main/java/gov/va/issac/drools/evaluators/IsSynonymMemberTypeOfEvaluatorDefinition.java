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
package gov.va.issac.drools.evaluators;

import gov.va.issac.drools.evaluators.facts.DescFact;
import java.io.IOException;
import java.util.Collection;
import org.drools.core.base.BaseEvaluator;
import org.drools.core.base.ValueType;
import org.drools.core.base.evaluators.Operator;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRfx;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.kie.api.runtime.rule.EvaluatorDefinition;

/**
 * 
 * {@link IsSynonymMemberTypeOfEvaluatorDefinition}
 *
 * @author kec
 * @author afurber
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class IsSynonymMemberTypeOfEvaluatorDefinition extends IsaacBaseEvaluatorDefinition implements EvaluatorDefinition
{
	public static Operator IS_SYNONYM_MEMBER_TYPE_OF = Operator.addOperatorToRegistry("isSynonymMemberTypeOf", false);
	public static Operator NOT_IS_SYNONYM_MEMBER_TYPE_OF = Operator.addOperatorToRegistry(IS_SYNONYM_MEMBER_TYPE_OF.getOperatorString(), true);

	public static class IsSynonymMemberTypeOfEvaluator extends IsaacBaseEvaluator
	{
		public IsSynonymMemberTypeOfEvaluator()
		{
			super();
			// No arg constructor for serialization. 
		}

		public IsSynonymMemberTypeOfEvaluator(final ValueType type, final boolean isNegated)
		{
			super(type, isNegated ? IsSynonymMemberTypeOfEvaluatorDefinition.NOT_IS_SYNONYM_MEMBER_TYPE_OF
					: IsSynonymMemberTypeOfEvaluatorDefinition.IS_SYNONYM_MEMBER_TYPE_OF);
		}

		@Override
		protected boolean test(final Object value1, final Object value2)
		{

			boolean isMemberType = false;
			DescFact fact;
			//value1 (concept) must be in form of DescFact
			//value2 (refset member type) must be a type as a concept fact (descRefsetMemberTypes.java)

			if (DescFact.class.isAssignableFrom(value1.getClass()))
			{
				fact = (DescFact) value1;
			}
			else
			{
				throw new UnsupportedOperationException("Can't convert: " + value1);
			}
			try
			{
				DescriptionVersionBI<?> desc = fact.getComponent();
				ViewCoordinate vc = fact.getVc();
				ConceptSpec possibleType = null;
				int evalRefsetNid = SnomedMetadataRfx.getSYNONYMY_REFEX_NID();
				int typeNid = 0;

				if (ConceptSpec.class.isAssignableFrom(value2.getClass()))
				{
					possibleType = (ConceptSpec) value2;
					typeNid = possibleType.getStrict(vc).getNid();
				}
				else
				{
					throw new UnsupportedOperationException("Can't convert: " + value1);
				}

				Collection<? extends RefexChronicleBI<?>> refexes = desc.getRefexMembersActive(vc);

				if (refexes != null)
				{
					for (RefexChronicleBI<?> refex : refexes)
					{
						if (refex.getAssemblageNid() == evalRefsetNid)
						{
							//test member type
							if (RefexVersionBI.class.isAssignableFrom(refex.getClass()))
							{
								RefexVersionBI<?> rv = (RefexVersionBI<?>) refex;

								if (RefexNidVersionBI.class.isAssignableFrom(rv.getClass()))
								{
									int cnid = ((RefexNidVersionBI<?>) rv).getNid1();
									if (cnid == typeNid)
									{
										isMemberType = true;
									}
								}
								else
								{
									throw new UnsupportedOperationException("Can't convert: RefexCnidVersionBI:  " + value1);
								}
							}
							else
							{
								throw new UnsupportedOperationException("Can't convert: RefexVersionBI:  " + value1);
							}
						}
					}
				}

			}
			catch (IOException e)
			{
				e.printStackTrace();
				return false;
			}

			return this.getOperator().isNegated() ^ (isMemberType);

		}

		@Override
		public String toString()
		{
			return "IsSynonymMemberTypeOf isSynonymMemberTypeOf";
		}
	}

	/**
	 * @see gov.va.issac.drools.evaluators.IsaacBaseEvaluatorDefinition#getId()
	 */
	@Override
	protected String getId()
	{
		return IS_SYNONYM_MEMBER_TYPE_OF.getOperatorString();
	}

	/**
	 * @see gov.va.issac.drools.evaluators.IsaacBaseEvaluatorDefinition#buildEvaluator(org.drools.core.base.ValueType, boolean, String)
	 */
	@Override
	protected BaseEvaluator buildEvaluator(ValueType type, boolean isNegated, String parameterText)
	{
		return new IsSynonymMemberTypeOfEvaluator(type, isNegated);
	}
}
