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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.va.isaac.gui.querybuilder.node.And;
import gov.va.isaac.gui.querybuilder.node.ConceptIs;
import gov.va.isaac.gui.querybuilder.node.ConceptIsChildOf;
import gov.va.isaac.gui.querybuilder.node.ConceptIsDescendantOf;
import gov.va.isaac.gui.querybuilder.node.DraggableNode;
import gov.va.isaac.gui.querybuilder.node.NodeDraggable;
import gov.va.isaac.gui.querybuilder.node.Or;
import gov.va.isaac.gui.querybuilder.node.Xor;

/**
 * QueryClauses
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public enum QueryNodeType {

	// Grouping
	AND(And.class),
	OR(Or.class),
	XOR(Xor.class),
	
	// Concept
	CONCEPT_IS(ConceptIs.class),
	CONCEPT_IS_CHILD_OF(ConceptIsChildOf.class),
	CONCEPT_IS_DESCENDANT_OF(ConceptIsDescendantOf.class);

	private final static Logger logger = LoggerFactory.getLogger(QueryNodeType.class);
	
	private final Class<? extends NodeDraggable> clazz;
	
	private QueryNodeType(Class<? extends NodeDraggable> clazz) {
		this.clazz = clazz;
	}
	
	public NodeDraggable construct() {
		try {
			return clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			logger.error("Failed constructing {} instance. Caught {} {}", clazz.getName(), e.getClass().getName(), e.getLocalizedMessage());
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
