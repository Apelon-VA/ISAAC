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

/**
 * QueryClauses
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.gui.querybuilder;

import gov.va.isaac.gui.querybuilder.node.And;
import gov.va.isaac.gui.querybuilder.node.ConceptIs;
import gov.va.isaac.gui.querybuilder.node.ConceptIsChildOf;
import gov.va.isaac.gui.querybuilder.node.ConceptIsDescendantOf;
import gov.va.isaac.gui.querybuilder.node.ConceptIsKindOf;
import gov.va.isaac.gui.querybuilder.node.DescriptionContains;
import gov.va.isaac.gui.querybuilder.node.DescriptionLuceneMatch;
import gov.va.isaac.gui.querybuilder.node.DescriptionRegexMatch;
import gov.va.isaac.gui.querybuilder.node.NodeDraggable;
import gov.va.isaac.gui.querybuilder.node.Or;
import gov.va.isaac.gui.querybuilder.node.RelType;
import gov.va.isaac.gui.querybuilder.node.Xor;

import org.ihtsdo.otf.query.implementation.Clause;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * QueryClauses
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public enum QueryNodeType {
	/*
	 * And(Query enclosingQuery, Clause... clauses)
	 * Or(Query enclosingQuery, Clause... clauses)
	 * Xor(Query enclosingQuery, Clause... clauses)
	 * Not(Query enclosingQuery, Clause child)
	 *
	 * ChangedFromPreviousVersion(Query enclosingQuery, String previousViewCoordinateKey)
	 * ConceptForComponent(Query enclosingQuery, Clause child)
	 * 
	 * ConceptIs(Query enclosingQuery, String conceptSpec, String viewCoordinateKey)
	 * ConceptIsChildOf(Query enclosingQuery, String kindOfSpecKey, String viewCoordinateKey)
	 * ConceptIsDescendentOf(Query enclosingQuery, String kindOfSpecKey, String viewCoordinateKey)
	 * ConceptIsKindOf(Query enclosingQuery, String kindOfSpecKey, String viewCoordinateKey)
	 * 
	 * DescriptionActiveLuceneMatch(Query enclosingQuery, String luceneMatchKey, String viewCoordinateKey)
	 * DescriptionActiveRegexMatch(Query enclosingQuery, String regexKey, String viewCoordinateKey)
	 * DescriptionLuceneMatch(Query enclosingQuery, String luceneMatchKey, String viewCoordinateKey)
	 * DescriptionRegexMatch(Query enclosingQuery, String regexKey, String viewCoordinateKey)
	 * 
	 * RefsetContainsConcept(Query enclosingQuery, String refsetSpecKey, String conceptSpecKey, String viewCoordinateKey)
	 * RefsetContainsKindOfConcept(Query enclosingQuery, String refsetSpecKey, String conceptSpecKey, String viewCoordinateKey)
	 * RefsetContainsString(Query enclosingQuery, String refsetSpecKey, String queryText, String viewCoordinateKey)
	 * RefsetLuceneMatch(Query enclosingQuery, String luceneMatchKey, String viewCoordinateKey)
	 * RelRestriction(Query enclosingQuery, String relRestrictionSpecKey, String relTypeKey, String sourceSpecKey,
	 *		String viewCoordinateKey, String destinationSubsumptionKey, String relTypeSubsumptionKey)
	 * RelType(Query enclosingQuery, String relTypeSpecKey, String targetSpecKey, String viewCoordinateKey, Boolean relTypeSubsumption)
	 * 
	 */

	// Grouping
	AND(And.class, org.ihtsdo.otf.query.implementation.And.class),
	OR(Or.class, org.ihtsdo.otf.query.implementation.Or.class),
	XOR(Xor.class, org.ihtsdo.otf.query.implementation.Xor.class),
	NOT(null, org.ihtsdo.otf.query.implementation.Not.class), // No Not LogicalNode

	// Concept
	CONCEPT_IS(ConceptIs.class, org.ihtsdo.otf.query.implementation.clauses.ConceptIs.class),
	CONCEPT_IS_CHILD_OF(ConceptIsChildOf.class, org.ihtsdo.otf.query.implementation.clauses.ConceptIsChildOf.class),
	CONCEPT_IS_DESCENDANT_OF(ConceptIsDescendantOf.class, org.ihtsdo.otf.query.implementation.clauses.ConceptIsDescendentOf.class),
	CONCEPT_IS_KIND_OF(ConceptIsKindOf.class, org.ihtsdo.otf.query.implementation.clauses.ConceptIsKindOf.class),
	
	// String
		// DESCRIPTION_CONTAINS id associated with DescriptionActiveLuceneMatch
	DESCRIPTION_CONTAINS(DescriptionContains.class, org.ihtsdo.otf.query.implementation.clauses.DescriptionActiveLuceneMatch.class),
		// Currently unsupported
	DESCRIPTION_LUCENE_MATCH(DescriptionLuceneMatch.class, org.ihtsdo.otf.query.implementation.clauses.DescriptionLuceneMatch.class),
		// Currently unsupported
	DESCRIPTION_REGEX_MATCH(DescriptionRegexMatch.class, org.ihtsdo.otf.query.implementation.clauses.DescriptionRegexMatch.class),
	
	REL_TYPE(RelType.class, org.ihtsdo.otf.query.implementation.clauses.RelType.class);

	private final static Logger logger = LoggerFactory.getLogger(QueryNodeType.class);
	
	private final Class<? extends NodeDraggable> nodeClass;
	private final Class<? extends Clause> clauseClass;
	
	private QueryNodeType(Class<? extends NodeDraggable> nodeClass, Class<? extends Clause> clauseClass) {
		this.nodeClass = nodeClass;
		this.clauseClass = clauseClass;
	}
	
	public Class<? extends NodeDraggable> getNodeClass() { return nodeClass; }
	
	public NodeDraggable constructNode() {
		try {
			return nodeClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			logger.error("Failed constructing {} instance. Caught {} {}", nodeClass.getName(), e.getClass().getName(), e.getLocalizedMessage());
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public static QueryNodeType valueOf(Clause clause) {
		for (QueryNodeType type : values()) {
			if (type.clauseClass == clause.getClass()) {
				return type;
			}
		}
		
		throw new IllegalArgumentException("Unexpected Clause type " + clause.getClass().getName());
	}
	public static QueryNodeType valueOf(NodeDraggable draggableNode) {
		for (QueryNodeType type : values()) {
			if (type.nodeClass != null && type.nodeClass == draggableNode.getClass()) {
				return type;
			}
		}
		
		throw new IllegalArgumentException("Unexpected NodeDraggable type " + draggableNode.getClass().getName());
	}
}
