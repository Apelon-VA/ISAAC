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

import gov.va.isaac.gui.querybuilder.node.And;
import gov.va.isaac.gui.querybuilder.node.ConceptIs;
import gov.va.isaac.gui.querybuilder.node.ConceptIsChildOf;
import gov.va.isaac.gui.querybuilder.node.ConceptIsDescendantOf;
import gov.va.isaac.gui.querybuilder.node.ConceptIsKindOf;
import gov.va.isaac.gui.querybuilder.node.DescriptionLuceneMatch;
import gov.va.isaac.gui.querybuilder.node.DescriptionRegexMatch;
import gov.va.isaac.gui.querybuilder.node.NodeDraggable;
import gov.va.isaac.gui.querybuilder.node.Or;
import gov.va.isaac.gui.querybuilder.node.RelType;
import gov.va.isaac.gui.querybuilder.node.SingleStringAssertionNode;
import gov.va.isaac.gui.querybuilder.node.Xor;
import gov.va.isaac.util.WBUtility;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.TreeItem;

import org.ihtsdo.otf.query.implementation.Clause;
import org.ihtsdo.otf.query.implementation.Query;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;

/**
 * ClauseFactory
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public class ClauseFactory {
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
		case AND: {
			And node = (And)treeItem.getValue();
			List<Clause> subClauses = new ArrayList<>();
			for (TreeItem<NodeDraggable> childClauseItem : treeItem.getChildren()) {
				subClauses.add(createClause(query, childClauseItem));
			}
			Clause newClause = new org.ihtsdo.otf.query.implementation.And(query, subClauses.toArray(new Clause[subClauses.size()]));
			if (! node.getInvert()) {
				return newClause;
			} else {
				return new org.ihtsdo.otf.query.implementation.Not(query, newClause);
			}
		}
		case OR: {
			Or node = (Or)treeItem.getValue();
			List<Clause> subClauses = new ArrayList<>();
			for (TreeItem<NodeDraggable> childClauseItem : treeItem.getChildren()) {
				subClauses.add(createClause(query, childClauseItem));
			}
			Clause newClause = new org.ihtsdo.otf.query.implementation.Or(query, subClauses.toArray(new Clause[subClauses.size()]));
			if (! node.getInvert()) {
				return newClause;
			} else {
				return new org.ihtsdo.otf.query.implementation.Not(query, newClause);
			}
		}
		case XOR: {
			Xor node = (Xor)treeItem.getValue();
			List<Clause> subClauses = new ArrayList<>();
			for (TreeItem<NodeDraggable> childClauseItem : treeItem.getChildren()) {
				subClauses.add(createClause(query, childClauseItem));
			}
			Clause newClause = new org.ihtsdo.otf.query.implementation.Xor(query, subClauses.toArray(new Clause[subClauses.size()]));
			if (! node.getInvert()) {
				return newClause;
			} else {
				return new org.ihtsdo.otf.query.implementation.Not(query, newClause);
			}
		}

		case CONCEPT_IS: {
			ConceptIs node = (ConceptIs)treeItem.getValue();
			ConceptVersionBI concept = WBUtility.getConceptVersion(node.getNid());
			final String conceptSpecKey = "UUIDKeyFor" + node.getTemporaryUniqueId();
			final String vcKey = "VCKeyFor" + node.getTemporaryUniqueId();
			query.getLetDeclarations().put(conceptSpecKey, new ConceptSpec(WBUtility.getDescription(concept), concept.getPrimordialUuid()));
			query.getLetDeclarations().put(vcKey, query.getViewCoordinate());
			Clause clause = new org.ihtsdo.otf.query.implementation.clauses.ConceptIs(query, conceptSpecKey, vcKey);
			if (! node.getInvert()) {
				return clause;
			} else {
				return new org.ihtsdo.otf.query.implementation.Not(query, clause);
			}
		}

		case CONCEPT_IS_CHILD_OF: {
			ConceptIsChildOf node = (ConceptIsChildOf)treeItem.getValue();
			ConceptVersionBI concept = WBUtility.getConceptVersion(node.getNid());
			final String conceptSpecKey = "UUIDKeyFor" + node.getTemporaryUniqueId();
			final String vcKey = "VCKeyFor" + node.getTemporaryUniqueId();
			query.getLetDeclarations().put(conceptSpecKey, new ConceptSpec(WBUtility.getDescription(concept), concept.getPrimordialUuid()));
			query.getLetDeclarations().put(vcKey, query.getViewCoordinate());
			Clause clause = new org.ihtsdo.otf.query.implementation.clauses.ConceptIsChildOf(query, conceptSpecKey, vcKey);
			if (! node.getInvert()) {
				return clause;
			} else {
				return new org.ihtsdo.otf.query.implementation.Not(query, clause);
			}
		}

		case CONCEPT_IS_DESCENDANT_OF: {
			ConceptIsDescendantOf node = (ConceptIsDescendantOf)treeItem.getValue();
			ConceptVersionBI concept = WBUtility.getConceptVersion(node.getNid());
			final String conceptSpecKey = "UUIDKeyFor" + node.getTemporaryUniqueId();
			final String vcKey = "VCKeyFor" + node.getTemporaryUniqueId();
			query.getLetDeclarations().put(conceptSpecKey, new ConceptSpec(WBUtility.getDescription(concept), concept.getPrimordialUuid()));
			query.getLetDeclarations().put(vcKey, query.getViewCoordinate());
			Clause clause = new org.ihtsdo.otf.query.implementation.clauses.ConceptIsDescendentOf(query, conceptSpecKey, vcKey);
			if (! node.getInvert()) {
				return clause;
			} else {
				return new org.ihtsdo.otf.query.implementation.Not(query, clause);
			}
		}

		case CONCEPT_IS_KIND_OF: {
			ConceptIsKindOf node = (ConceptIsKindOf)treeItem.getValue();
			ConceptVersionBI concept = WBUtility.getConceptVersion(node.getNid());
			final String conceptSpecKey = "UUIDKeyFor" + node.getTemporaryUniqueId();
			final String vcKey = "VCKeyFor" + node.getTemporaryUniqueId();
			query.getLetDeclarations().put(conceptSpecKey, new ConceptSpec(WBUtility.getDescription(concept), concept.getPrimordialUuid()));
			query.getLetDeclarations().put(vcKey, query.getViewCoordinate());
			Clause clause = new org.ihtsdo.otf.query.implementation.clauses.ConceptIsKindOf(query, conceptSpecKey, vcKey);
			if (! node.getInvert()) {
				return clause;
			} else {
				return new org.ihtsdo.otf.query.implementation.Not(query, clause);
			}
		}

		case DESCRIPTION_CONTAINS: {
			SingleStringAssertionNode node = (SingleStringAssertionNode)treeItem.getValue();
			final String stringMatchKey = "StringMatchKeyFor" + node.getTemporaryUniqueId();
			final String vcKey = "VCKeyFor" + node.getTemporaryUniqueId();
			query.getLetDeclarations().put(stringMatchKey, node.getString());
			query.getLetDeclarations().put(vcKey, query.getViewCoordinate());
			Clause clause = new org.ihtsdo.otf.query.implementation.clauses.DescriptionActiveLuceneMatch(query, stringMatchKey, vcKey);
			if (! node.getInvert()) {
				return clause;
			} else {
				return new org.ihtsdo.otf.query.implementation.Not(query, clause);
			}
		}
		case DESCRIPTION_LUCENE_MATCH: {
			DescriptionLuceneMatch node = (DescriptionLuceneMatch)treeItem.getValue();
			final String stringMatchKey = "StringMatchKeyFor" + node.getTemporaryUniqueId();
			final String vcKey = "VCKeyFor" + node.getTemporaryUniqueId();
			query.getLetDeclarations().put(stringMatchKey, node.getString());
			query.getLetDeclarations().put(vcKey, query.getViewCoordinate());
			Clause clause = new org.ihtsdo.otf.query.implementation.clauses.DescriptionLuceneMatch(query, stringMatchKey, vcKey);
			if (! node.getInvert()) {
				return clause;
			} else {
				return new org.ihtsdo.otf.query.implementation.Not(query, clause);
			}
		}
		
		case DESCRIPTION_REGEX_MATCH: {
			DescriptionRegexMatch node = (DescriptionRegexMatch)treeItem.getValue();
			final String stringMatchKey = "StringMatchKeyFor" + node.getTemporaryUniqueId();
			final String vcKey = "VCKeyFor" + node.getTemporaryUniqueId();
			query.getLetDeclarations().put(stringMatchKey, node.getString());
			query.getLetDeclarations().put(vcKey, query.getViewCoordinate());
			Clause clause = new org.ihtsdo.otf.query.implementation.clauses.DescriptionRegexMatch(query, stringMatchKey, vcKey);
			if (! node.getInvert()) {
				return clause;
			} else {
				return new org.ihtsdo.otf.query.implementation.Not(query, clause);
			}
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

			if (! node.getInvert()) {
				return clause;
			} else {
				return new org.ihtsdo.otf.query.implementation.Not(query, clause);
			}
		}

		default:
			throw new RuntimeException("Unsupported QueryNodeType " + itemType);
		}
	}
}
