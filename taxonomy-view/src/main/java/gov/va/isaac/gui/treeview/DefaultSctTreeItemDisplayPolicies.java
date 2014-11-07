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
 * DefaultSctTreeItemDisplayPolicies
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.gui.treeview;

import javafx.scene.Node;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.taxonomyView.SctTreeItemDisplayPolicies;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.taxonomyView.SctTreeItemI;

/**
 * DefaultSctTreeItemDisplayPolicies
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public class DefaultSctTreeItemDisplayPolicies implements SctTreeItemDisplayPolicies {
	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.treeview.SctTreeItemDisplayPolicies#computeGraphic(gov.va.isaac.interfaces.treeview.SctTreeItemI)
	 */
	@Override
	public Node computeGraphic(SctTreeItemI item) {
		if (item.isRoot()) {
			return Images.ROOT.createImageView();
		} else if (item.isDefined() && (item.isMultiParent() || item.getMultiParentDepth() > 0)) {
			if (item.isSecondaryParentOpened()) {
				return Images.DEFINED_MULTI_PARENT_OPEN.createImageView();
			} else {
				return Images.DEFINED_MULTI_PARENT_CLOSED.createImageView();
			}
		} else if (!item.isDefined() && (item.isMultiParent() || item.getMultiParentDepth() > 0)) {
			if (item.isSecondaryParentOpened()) {
				return Images.PRIMITIVE_MULTI_PARENT_OPEN.createImageView();
			} else {
				return Images.PRIMITIVE_MULTI_PARENT_CLOSED.createImageView();
			}
		} else if (item.isDefined() && !item.isMultiParent()) {
			return Images.DEFINED_SINGLE_PARENT.createImageView();
		}
		return Images.PRIMITIVE_SINGLE_PARENT.createImageView();
	}
}
