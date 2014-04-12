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
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.legoEdit.gui.legoTreeView;

/**
 * {@link LegoTreeNodeType}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public enum LegoTreeNodeType
{
	legoListByReference(1),
	legoReference(0),
	pncsValue(0), 
	pncsName(0),
	
	comment(0),
	status(2),
	assertion(3),
	expressionDiscernible(0),
	expressionQualifier(1),
	measurementEmpty(2), measurementPoint(2), measurementInterval(2), measurementBound(2), 
	value(3),
	assertionComponent(4),
	
	concept(0),
	
	expressionValue(2), expressionDestination(2), expressionOptional(2), 
	relation(3),
	relationshipGroup(4),

	assertionUUID(0),
	
	text(0), bool(0),

	blankLegoEndNode(50),
	blankLegoListEndNode(50);
	
	
	private int sortOrder_;
	
	private LegoTreeNodeType(int sortOrder)
	{
		sortOrder_ = sortOrder;
	}
	
	public int getSortOrder()
	{
		return sortOrder_;
	}
}
