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
import gov.va.isaac.gui.querybuilder.node.DescriptionRegexMatch;
import gov.va.isaac.gui.querybuilder.node.NodeDraggable;
import gov.va.isaac.gui.querybuilder.node.Or;
import gov.va.isaac.gui.querybuilder.node.Xor;

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
	AND(And.class),
	OR(Or.class),
	XOR(Xor.class),
	
	// Concept
	CONCEPT_IS(ConceptIs.class),
	CONCEPT_IS_CHILD_OF(ConceptIsChildOf.class),
	CONCEPT_IS_DESCENDANT_OF(ConceptIsDescendantOf.class),
	CONCEPT_IS_KIND_OF(ConceptIsKindOf.class),
	
	// String
	DESCRIPTION_REGEX_MATCH(DescriptionRegexMatch.class);

	private final static Logger logger = LoggerFactory.getLogger(QueryNodeType.class);
	
	private final Class<? extends NodeDraggable> clazz;
	
	private QueryNodeType(Class<? extends NodeDraggable> clazz) {
		this.clazz = clazz;
	}
	
	public NodeDraggable constructNode() {
		try {
			return clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			logger.error("Failed constructing {} instance. Caught {} {}", clazz.getName(), e.getClass().getName(), e.getLocalizedMessage());
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public static QueryNodeType valueOf(NodeDraggable draggableNode) {
		for (QueryNodeType type : values()) {
			if (type.clazz == draggableNode.getClass()) {
				return type;
			}
		}
		
		throw new IllegalArgumentException("Unexpected NodeDraggable type " + draggableNode.getClass().getName());
	}
}
