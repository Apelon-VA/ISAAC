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
 * ClauseFactory
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.gui.querybuilder;

import gov.va.isaac.gui.querybuilder.node.Invertable;
import gov.va.isaac.gui.querybuilder.node.LogicalNode;
import gov.va.isaac.gui.querybuilder.node.NodeDraggable;
import gov.va.isaac.gui.querybuilder.node.RefsetContainsConcept;
import gov.va.isaac.gui.querybuilder.node.RefsetContainsKindOfConcept;
import gov.va.isaac.gui.querybuilder.node.RefsetContainsString;
import gov.va.isaac.gui.querybuilder.node.RelRestriction;
import gov.va.isaac.gui.querybuilder.node.RelType;
import gov.va.isaac.gui.querybuilder.node.SingleConceptAssertionNode;
import gov.va.isaac.gui.querybuilder.node.SingleStringAssertionNode;
import gov.va.isaac.util.WBUtility;
import javafx.scene.control.TreeItem;

import org.ihtsdo.otf.query.implementation.Clause;
import org.ihtsdo.otf.query.implementation.Query;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ClauseFactory
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public class ClauseFactory {
	private final static Logger logger = LoggerFactory.getLogger(ClauseFactory.class);

	/*
	 * And(Query enclosingQuery, Clause... clauses)
	 * Or(Query enclosingQuery, Clause... clauses)
	 * Xor(Query enclosingQuery, Clause... clauses)
	 * Not(Query enclosingQuery, Clause child)
	 *
	 * ChangedFromPreviousVersion(Query enclosingQuery, String previousViewCoordinateKey)
	 * ConceptForComponent(Query enclosingQuery, Clause child)
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

	private ClauseFactory() {}

	public static Clause createClause(Query query, TreeItem<NodeDraggable> treeItem) {
		QueryNodeType itemType = QueryNodeType.valueOf(treeItem.getValue());

		switch(itemType) {
		case AND:
		case OR: 
		case XOR: {
			LogicalNode node = (LogicalNode)treeItem.getValue();
			Clause[] subClauses = new Clause[treeItem.getChildren().size()];
			for (int i = 0; i < treeItem.getChildren().size(); ++i) {
				subClauses[i] = createClause(query, treeItem.getChildren().get(i));
			}
			Clause clause = null;
			
			if (itemType == QueryNodeType.AND) {
				clause = new org.ihtsdo.otf.query.implementation.And(query, subClauses);
			} else if (itemType == QueryNodeType.OR) {
				clause = new org.ihtsdo.otf.query.implementation.Or(query, subClauses);
			} else if (itemType == QueryNodeType.XOR) {
				clause = new org.ihtsdo.otf.query.implementation.Xor(query, subClauses);
			} else {
				// Programmer error.  Should never happen.
				throw new IllegalArgumentException("Unhandled LogicalNode QueryNodeType " + itemType);
			}

			logger.debug("Constructed {} clause with {} child subclauses", clause.getClass().getName(), subClauses.length);
			
			return addInversionClauseIfNecessary(query, node, clause);
		}

		case CONCEPT_IS: 
		case CONCEPT_IS_CHILD_OF:
		case CONCEPT_IS_DESCENDANT_OF: 
		case CONCEPT_IS_KIND_OF: {
			SingleConceptAssertionNode node = (SingleConceptAssertionNode)treeItem.getValue();
			ConceptVersionBI concept = WBUtility.getConceptVersion(node.getNid());
			final String conceptSpecKey = "UUIDKeyFor" + node.getTemporaryUniqueId();
			final String vcKey = "VCKeyFor" + node.getTemporaryUniqueId();
			query.getLetDeclarations().put(conceptSpecKey, new ConceptSpec(WBUtility.getDescription(concept), concept.getPrimordialUuid()));
			query.getLetDeclarations().put(vcKey, query.getViewCoordinate());
			Clause clause = null;
			
			if (itemType == QueryNodeType.CONCEPT_IS) {
				clause = new org.ihtsdo.otf.query.implementation.clauses.ConceptIs(query, conceptSpecKey, vcKey);
			} else if (itemType == QueryNodeType.CONCEPT_IS_CHILD_OF) {
				clause = new org.ihtsdo.otf.query.implementation.clauses.ConceptIsChildOf(query, conceptSpecKey, vcKey);
			} else if (itemType == QueryNodeType.CONCEPT_IS_DESCENDANT_OF) {
				clause = new org.ihtsdo.otf.query.implementation.clauses.ConceptIsDescendentOf(query, conceptSpecKey, vcKey);
			} else if (itemType == QueryNodeType.CONCEPT_IS_KIND_OF) {
				clause = new org.ihtsdo.otf.query.implementation.clauses.ConceptIsKindOf(query, conceptSpecKey, vcKey);
			} else {
				// Programmer error.  Should never happen.
				throw new IllegalArgumentException("Unhandled SingleConceptAssertionNode QueryNodeType " + itemType);
			}

			logger.debug("Constructed {} clause for {}", clause.getClass().getName(), node.getDescription());

			return addInversionClauseIfNecessary(query, node, clause);
		}

		case DESCRIPTION_CONTAINS:
		case DESCRIPTION_LUCENE_MATCH:
		case DESCRIPTION_REGEX_MATCH:  {
			SingleStringAssertionNode node = (SingleStringAssertionNode)treeItem.getValue();
			final String stringMatchKey = "StringMatchKeyFor" + node.getTemporaryUniqueId();
			final String vcKey = "VCKeyFor" + node.getTemporaryUniqueId();
			query.getLetDeclarations().put(stringMatchKey, node.getString());
			query.getLetDeclarations().put(vcKey, query.getViewCoordinate());
			
			Clause clause = null;
			if (itemType == QueryNodeType.DESCRIPTION_CONTAINS) {
				clause = new org.ihtsdo.otf.query.implementation.clauses.DescriptionActiveLuceneMatch(query, stringMatchKey, vcKey);
			} else if (itemType == QueryNodeType.DESCRIPTION_LUCENE_MATCH) {
				clause = new org.ihtsdo.otf.query.implementation.clauses.DescriptionLuceneMatch(query, stringMatchKey, vcKey);
			} else if (itemType == QueryNodeType.DESCRIPTION_REGEX_MATCH) {
				clause = new org.ihtsdo.otf.query.implementation.clauses.DescriptionRegexMatch(query, stringMatchKey, vcKey);
			} else {
				// Programmer error.  Should never happen.
				throw new IllegalArgumentException("Unhandled SingleStringAssertionNode QueryNodeType " + itemType);
			}
			
			logger.debug("Constructed {} clause for {}", clause.getClass().getName(), node.getDescription());

			clause = new org.ihtsdo.otf.query.implementation.clauses.ConceptForComponent(query, clause);
			
			return addInversionClauseIfNecessary(query, node, clause);
		}

		case REFSET_CONTAINS_CONCEPT: {
			RefsetContainsConcept node = (RefsetContainsConcept)treeItem.getValue();
			
			final ConceptVersionBI refsetConcept = WBUtility.getConceptVersion(node.getRefsetConceptNid());
			final String refsetConceptSpecKey = "RefsetConceptUUIDKeyFor" + node.getTemporaryUniqueId();
			query.getLetDeclarations().put(refsetConceptSpecKey, new ConceptSpec(WBUtility.getDescription(refsetConcept), refsetConcept.getPrimordialUuid()));

			final ConceptVersionBI targetConcept = WBUtility.getConceptVersion(node.getConceptNid());
			final String targetConceptSpecKey = "TargetConceptUUIDKeyFor" + node.getTemporaryUniqueId();
			query.getLetDeclarations().put(targetConceptSpecKey, new ConceptSpec(WBUtility.getDescription(targetConcept), targetConcept.getPrimordialUuid()));

			final String vcKey = "VCKeyFor" + node.getTemporaryUniqueId();
			query.getLetDeclarations().put(vcKey, query.getViewCoordinate());
			
			Clause clause = new org.ihtsdo.otf.query.implementation.clauses.RefsetContainsConcept(
				/* Query */		query,
				/* String */	refsetConceptSpecKey,
				/* String */	targetConceptSpecKey,
				/* String */	vcKey);

			logger.debug("Constructed {} clause for {}", clause.getClass().getName(), node.getDescription());

			return addInversionClauseIfNecessary(query, node, clause);
		}
		
		case REFSET_CONTAINS_KIND_OF_CONCEPT: {
			RefsetContainsKindOfConcept node = (RefsetContainsKindOfConcept)treeItem.getValue();
			
			final ConceptVersionBI refsetConcept = WBUtility.getConceptVersion(node.getRefsetConceptNid());
			final String refsetConceptSpecKey = "RefsetConceptUUIDKeyFor" + node.getTemporaryUniqueId();
			query.getLetDeclarations().put(refsetConceptSpecKey, new ConceptSpec(WBUtility.getDescription(refsetConcept), refsetConcept.getPrimordialUuid()));

			final ConceptVersionBI targetConcept = WBUtility.getConceptVersion(node.getConceptNid());
			final String targetConceptSpecKey = "TargetConceptUUIDKeyFor" + node.getTemporaryUniqueId();
			query.getLetDeclarations().put(targetConceptSpecKey, new ConceptSpec(WBUtility.getDescription(targetConcept), targetConcept.getPrimordialUuid()));

			final String vcKey = "VCKeyFor" + node.getTemporaryUniqueId();
			query.getLetDeclarations().put(vcKey, query.getViewCoordinate());
			
			Clause clause = new org.ihtsdo.otf.query.implementation.clauses.RefsetContainsKindOfConcept(
				/* Query */		query,
				/* String */	refsetConceptSpecKey,
				/* String */	targetConceptSpecKey,
				/* String */	vcKey);

			logger.debug("Constructed {} clause for {}", clause.getClass().getName(), node.getDescription());

			return addInversionClauseIfNecessary(query, node, clause);
		}
		case REFSET_CONTAINS_STRING: {
			RefsetContainsString node = (RefsetContainsString)treeItem.getValue();
			
			final ConceptVersionBI refsetConcept = WBUtility.getConceptVersion(node.getRefsetConceptNid());
			final String refsetConceptSpecKey = "RefsetConceptUUIDKeyFor" + node.getTemporaryUniqueId();
			query.getLetDeclarations().put(refsetConceptSpecKey, new ConceptSpec(WBUtility.getDescription(refsetConcept), refsetConcept.getPrimordialUuid()));

			final String queryText = node.getQueryText();
			final String queryTextKey = "QueryTextKeyFor" + node.getTemporaryUniqueId();
			query.getLetDeclarations().put(queryTextKey, queryText);

			final String vcKey = "VCKeyFor" + node.getTemporaryUniqueId();
			query.getLetDeclarations().put(vcKey, query.getViewCoordinate());
			
			Clause clause = new org.ihtsdo.otf.query.implementation.clauses.RefsetContainsString(
				/* Query */		query,
				/* String */	refsetConceptSpecKey,
				/* String */	queryTextKey,
				/* String */	vcKey);

			logger.debug("Constructed {} clause for {}", clause.getClass().getName(), node.getDescription());

			return addInversionClauseIfNecessary(query, node, clause);
		}

		case REL_RESTRICTION: {
			RelRestriction node = (RelRestriction)treeItem.getValue();
			
			final ConceptVersionBI relRestrictionConcept = WBUtility.getConceptVersion(node.getRelRestrictionConceptNid());
			final String relRestrictionConceptSpecKey = "RelRestrictionConceptUUIDKeyFor" + node.getTemporaryUniqueId();
			query.getLetDeclarations().put(relRestrictionConceptSpecKey, new ConceptSpec(WBUtility.getDescription(relRestrictionConcept), relRestrictionConcept.getPrimordialUuid()));
			
			final ConceptVersionBI relTypeConcept = WBUtility.getConceptVersion(node.getRelTypeConceptNid());
			final String relTypeConceptSpecKey = "RelTypeConceptUUIDKeyFor" + node.getTemporaryUniqueId();
			query.getLetDeclarations().put(relTypeConceptSpecKey, new ConceptSpec(WBUtility.getDescription(relTypeConcept), relTypeConcept.getPrimordialUuid()));

			final ConceptVersionBI sourceConcept = WBUtility.getConceptVersion(node.getSourceConceptNid());
			final String sourceConceptSpecKey = "SourceConceptUUIDKeyFor" + node.getTemporaryUniqueId();
			query.getLetDeclarations().put(sourceConceptSpecKey, new ConceptSpec(WBUtility.getDescription(sourceConcept), sourceConcept.getPrimordialUuid()));

			final String vcKey = "VCKeyFor" + node.getTemporaryUniqueId();
			query.getLetDeclarations().put(vcKey, query.getViewCoordinate());
			
			final String useDestinationSubsumptionKey = "UseDestinationSubsumptionBooleanKeyFor" + node.getTemporaryUniqueId();
			query.getLetDeclarations().put(useDestinationSubsumptionKey, node.getUseDestinationSubsumption());
			
			final String useRelTypeSubsumptionKey = "UseRelTypeSubsumptionBooleanKeyFor" + node.getTemporaryUniqueId();
			query.getLetDeclarations().put(useRelTypeSubsumptionKey, node.getUseRelTypeSubsumption());

			Clause clause = new org.ihtsdo.otf.query.implementation.clauses.RelRestriction(
				/* Query */		query,
				/* String */	relRestrictionConceptSpecKey,
				/* String */	relTypeConceptSpecKey,
				/* String */	sourceConceptSpecKey,
				/* String */	vcKey,
				/* String */	useDestinationSubsumptionKey,
				/* String */	useRelTypeSubsumptionKey);

			logger.debug("Constructed {} clause for {}", clause.getClass().getName(), node.getDescription());

			return addInversionClauseIfNecessary(query, node, clause);
		}

		case REL_TYPE: {
			RelType node = (RelType)treeItem.getValue();
			
			final ConceptVersionBI relTypeConcept = WBUtility.getConceptVersion(node.getRelTypeConceptNid());
			final String relTypeConceptSpecKey = "RelTypeConceptUUIDKeyFor" + node.getTemporaryUniqueId();
			query.getLetDeclarations().put(relTypeConceptSpecKey, new ConceptSpec(WBUtility.getDescription(relTypeConcept), relTypeConcept.getPrimordialUuid()));

			final ConceptVersionBI targetConcept = WBUtility.getConceptVersion(node.getTargetConceptNid());
			final String targetConceptSpecKey = "TargetConceptUUIDKeyFor" + node.getTemporaryUniqueId();
			query.getLetDeclarations().put(targetConceptSpecKey, new ConceptSpec(WBUtility.getDescription(targetConcept), targetConcept.getPrimordialUuid()));

			final String vcKey = "VCKeyFor" + node.getTemporaryUniqueId();
			query.getLetDeclarations().put(vcKey, query.getViewCoordinate());
			
			Clause clause = new org.ihtsdo.otf.query.implementation.clauses.RelType(
				/* Query */		query,
				/* String */	relTypeConceptSpecKey,
				/* String */	targetConceptSpecKey,
				/* String */	vcKey,
				/* Boolean */	node.getUseSubsumption());

			logger.debug("Constructed {} clause for {}", clause.getClass().getName(), node.getDescription());

			return addInversionClauseIfNecessary(query, node, clause);
		}

		default:
			throw new RuntimeException("Unsupported QueryNodeType " + itemType);
		}
	}

	private static Clause addInversionClauseIfNecessary(Query query, Invertable node, Clause clause) {
		if (! node.getInvert()) {
			return clause;
		} else {
			return new org.ihtsdo.otf.query.implementation.Not(query, clause);
		}
	}
}
