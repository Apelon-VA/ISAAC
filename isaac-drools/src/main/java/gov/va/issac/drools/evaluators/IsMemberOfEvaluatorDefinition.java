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

import gov.va.isaac.AppContext;
import gov.va.issac.drools.evaluators.facts.ConceptFact;
import gov.va.issac.drools.evaluators.facts.DescFact;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
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
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;

/**
 * 
 * {@link IsMemberOfEvaluatorDefinition}
 * 
 * @author kec
 * @author afurber
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class IsMemberOfEvaluatorDefinition implements EvaluatorDefinition
{
	public static Operator IS_MEMBER_OF = Operator.addOperatorToRegistry("isMemberOf", false);
	public static Operator NOT_IS_MEMBER_OF = Operator.addOperatorToRegistry(IS_MEMBER_OF.getOperatorString(), true);

	public static class IsMemberOfEvaluator extends BaseEvaluator
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

		public IsMemberOfEvaluator()
		{
			super();
			// No arg constructor for serialization. 
		}

		public IsMemberOfEvaluator(final ValueType type, final boolean isNegated)
		{
			super(type, isNegated ? IsMemberOfEvaluatorDefinition.NOT_IS_MEMBER_OF : IsMemberOfEvaluatorDefinition.IS_MEMBER_OF);
		}

		@Override
		public boolean evaluate(InternalWorkingMemory workingMemory, InternalReadAccessor extractor, InternalFactHandle factHandle, FieldValue value)
		{
			return testMemberOf(factHandle, value.getValue());
		}

		@Override
		public boolean evaluate(InternalWorkingMemory workingMemory, InternalReadAccessor leftExtractor, InternalFactHandle left, InternalReadAccessor rightExtractor,
				InternalFactHandle right)
		{
			final Object value1 = leftExtractor.getValue(workingMemory, left);
			final Object value2 = rightExtractor.getValue(workingMemory, right);

			return testMemberOf(value1, value2);
		}

		private boolean testMemberOf(final Object value1, final Object value2)
		{

			boolean isMember = false;
			//value1 (concept): this could be concept VersionBI or conceptFact
			//value2 (refset): this will be put in Refset.java (tk-arena-rules) as a ConceptSpec
			ConceptSpec refexConcept = (ConceptSpec) value2;
			try
			{
				if (!AppContext.getService(TerminologyStoreDI.class).hasUuid(refexConcept.getLenient().getPrimordialUuid()))
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

			ConceptVersionBI possibleMember = null;
			if (ConceptVersionBI.class.isAssignableFrom(value1.getClass()))
			{
				possibleMember = (ConceptVersionBI) value1;
				isMember = testConcept(possibleMember, value2);
			}
			else if (ConceptFact.class.isAssignableFrom(value1.getClass()))
			{
				possibleMember = ((ConceptFact) value1).getConcept();
				isMember = testConcept(possibleMember, value2);
			}
			else if (DescFact.class.isAssignableFrom(value1.getClass()))
			{
				DescFact dFact = (DescFact) value1;
				isMember = testDesc(dFact, value2);
			}
			else
			{
				throw new UnsupportedOperationException("Can't convert: " + value1);
			}

			return this.getOperator().isNegated() ^ (isMember);

		}

		private boolean testDesc(DescFact dFact, final Object value2)
		{
			boolean member = false;

			try
			{
				DescriptionVersionBI<?> desc = dFact.getComponent();
				ViewCoordinate vc = dFact.getVc();
				ConceptVersionBI possibleRefsetCV = null;
				ConceptSpec possibleRefset = null;

				int evalRefsetNid = 0;

				if (ConceptVersionBI.class.isAssignableFrom(value2.getClass()))
				{
					possibleRefsetCV = (ConceptVersionBI) value2;
					evalRefsetNid = possibleRefsetCV.getNid();
				}
				else if (ConceptSpec.class.isAssignableFrom(value2.getClass()))
				{
					possibleRefset = (ConceptSpec) value2;
					try
					{
						evalRefsetNid = possibleRefset.getStrict(vc).getNid();
					}
					catch (ValidationException ve)
					{
						return false;
					}
				}
				else if (ConceptFact.class.isAssignableFrom(value2.getClass()))
				{
					ConceptFact fact = (ConceptFact) value2;
					possibleRefsetCV = (ConceptVersionBI) fact.getConcept();
					evalRefsetNid = possibleRefsetCV.getNid();
				}

				//TODO not sure about this change 
				Collection<? extends RefexChronicleBI<?>> refexes = desc.getRefexMembersActive(vc);

				if (refexes != null)
				{
					for (RefexChronicleBI<?> refex : refexes)
					{
						if (refex.getAssemblageNid() == evalRefsetNid)
						{
							member = true;
						}
					}
				}

				return member;
			}
			catch (IOException e)
			{
				Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "refset concept not found", e);
				return member;
			}
		}

		private boolean testConcept(ConceptVersionBI possibleMember, final Object value2)
		{
			boolean member = false;
			try
			{
				ViewCoordinate vc = possibleMember.getViewCoordinate();
				ConceptVersionBI possibleRefsetCV = null;
				ConceptSpec possibleRefset = null;

				int evalRefsetNid = 0;

				if (ConceptVersionBI.class.isAssignableFrom(value2.getClass()))
				{
					possibleRefsetCV = (ConceptVersionBI) value2;
					evalRefsetNid = possibleRefsetCV.getNid();
				}
				else if (ConceptSpec.class.isAssignableFrom(value2.getClass()))
				{

					possibleRefset = (ConceptSpec) value2;
					try
					{
						evalRefsetNid = possibleRefset.getStrict(vc).getNid();
					}
					catch (ValidationException ve)
					{
						return false;
					}

				}
				else if (ConceptFact.class.isAssignableFrom(value2.getClass()))
				{
					ConceptFact fact = (ConceptFact) value2;
					possibleRefsetCV = (ConceptVersionBI) fact.getConcept();
					evalRefsetNid = possibleRefsetCV.getNid();
				}

				member = possibleMember.isMember(evalRefsetNid);

				return member;
			}
			catch (IOException e)
			{
				Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "refset concept not found", e);
				return member;
			}
		}

		@Override
		public boolean evaluateCachedLeft(InternalWorkingMemory workingMemory, VariableContextEntry context, InternalFactHandle right)
		{
			return testMemberOf(((ObjectVariableContextEntry) context).left, right);
		}

		@Override
		public boolean evaluateCachedRight(InternalWorkingMemory workingMemory, VariableContextEntry context, InternalFactHandle left)
		{
			return testMemberOf(left, ((ObjectVariableContextEntry) context).right);
		}

		@Override
		public String toString()
		{
			return "IsMemberOf isMemberOf";
		}
	}
}
