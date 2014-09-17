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
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;

public class IsParentMemberOfEvaluatorDefinition implements EvaluatorDefinition
{
	public static Operator IS_PARENT_MEMBER_OF = Operator.addOperatorToRegistry("isParentMemberOf", false);
	public static Operator NOT_IS_PARENT_MEMBER_OF = Operator.addOperatorToRegistry(IS_PARENT_MEMBER_OF.getOperatorString(), true);

	public static class IsParentMemberOfEvaluator extends BaseEvaluator
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

		public IsParentMemberOfEvaluator()
		{
			super();
			// No arg constructor for serialization. 
		}

		public IsParentMemberOfEvaluator(final ValueType type, final boolean isNegated)
		{
			super(type, isNegated ? IsParentMemberOfEvaluatorDefinition.NOT_IS_PARENT_MEMBER_OF : IsParentMemberOfEvaluatorDefinition.IS_PARENT_MEMBER_OF);
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
			try
			{

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
				}
				else if (ConceptFact.class.isAssignableFrom(value1.getClass()))
				{
					possibleMember = ((ConceptFact) value1).getConcept();
				}
				else
				{
					throw new UnsupportedOperationException("Can't convert: " + value1);
				}
				ViewCoordinate vc = possibleMember.getViewCoordinate();
				ConceptVersionBI possibleRefsetCV = null;
				ConceptSpec possibleRefset = null;
				Collection<? extends ConceptVersionBI> parents = possibleMember.getRelationshipsOutgoingDestinationsActiveIsa();

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

				return this.getOperator().isNegated() ^ (testParentOf(evalRefsetNid, parents, possibleMember));
			}
			catch (IOException e)
			{
				Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
				return this.getOperator().isNegated() ^ (false);
			}
			catch (ContradictionException e)
			{
				Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
				return this.getOperator().isNegated() ^ (false);
			}
		}

		private boolean testParentOf(int evalRefsetNid, Collection<? extends ConceptVersionBI> parents, ConceptVersionBI possibleMember)
		{
			boolean parentMember = false;
			try
			{
				for (ConceptVersionBI parent : parents)
				{
					parentMember = parent.isMember(evalRefsetNid);
					if (parentMember)
					{
						break;
					}
					else if (!parentMember && !parent.getRelationshipsOutgoingDestinationsActiveIsa().isEmpty())
					{
						parentMember = testParentOf(evalRefsetNid, parent.getRelationshipsOutgoingDestinationsActiveIsa(), possibleMember);
					}
				}
			}
			catch (IOException e)
			{
				Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "refset concept not found", e);
				return parentMember;
			}
			catch (ContradictionException e)
			{
				Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "refset concept not found", e);
				return parentMember;
			}

			return parentMember;
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
