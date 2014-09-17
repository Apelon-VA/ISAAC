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
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
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
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRfx;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;

/**
 * 
 * {@link IsGbMemberTypeOfEvaluatorDefinition}
 *
 * @author kec
 * @author afurber
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class IsGbMemberTypeOfEvaluatorDefinition implements EvaluatorDefinition
{
	public static Operator IS_GB_MEMBER_TYPE_OF = Operator.addOperatorToRegistry("isGbMemberTypeOf", false);
	public static Operator NOT_IS_GB_MEMBER_TYPE_OF = Operator.addOperatorToRegistry(IS_GB_MEMBER_TYPE_OF.getOperatorString(), true);

	public static class IsGbMemberTypeOfEvaluator extends BaseEvaluator
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

		public IsGbMemberTypeOfEvaluator()
		{
			super();
			// No arg constructor for serialization. 
		}

		public IsGbMemberTypeOfEvaluator(final ValueType type, final boolean isNegated)
		{
			super(type, isNegated ? IsGbMemberTypeOfEvaluatorDefinition.NOT_IS_GB_MEMBER_TYPE_OF : IsGbMemberTypeOfEvaluatorDefinition.IS_GB_MEMBER_TYPE_OF);
		}

		@Override
		public boolean evaluate(InternalWorkingMemory workingMemory, InternalReadAccessor extractor, InternalFactHandle factHandle, FieldValue value)
		{
			return testMemberTypeOf(factHandle, value.getValue());
		}

		@Override
		public boolean evaluate(InternalWorkingMemory workingMemory, InternalReadAccessor leftExtractor, InternalFactHandle left, InternalReadAccessor rightExtractor,
				InternalFactHandle right)
		{
			final Object value1 = leftExtractor.getValue(workingMemory, left);
			final Object value2 = rightExtractor.getValue(workingMemory, right);

			return testMemberTypeOf(value1, value2);
		}

		private boolean testMemberTypeOf(final Object value1, final Object value2)
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
				int evalRefsetNid = SnomedMetadataRfx.getGB_DIALECT_REFEX_NID();
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

				//TODO not sure if this is the right change (from trek where it was getRefexesActive(vc))
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
				return false;
			}

			return this.getOperator().isNegated() ^ (isMemberType);

		}

		@Override
		public boolean evaluateCachedLeft(InternalWorkingMemory workingMemory, VariableContextEntry context, InternalFactHandle right)
		{
			return testMemberTypeOf(((ObjectVariableContextEntry) context).left, right);
		}

		@Override
		public boolean evaluateCachedRight(InternalWorkingMemory workingMemory, VariableContextEntry context, InternalFactHandle left)
		{
			return testMemberTypeOf(left, ((ObjectVariableContextEntry) context).right);
		}

		@Override
		public String toString()
		{
			return "IsGbMemberTypeOf isGbMemberTypeOf";
		}
	}
}
