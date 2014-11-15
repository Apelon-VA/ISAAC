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
 * ComponentSearchTypeHelper
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.gui.enhancedsearchview.model.type.text;

/**
 * ComponentSearchTypeHelper
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
class ComponentSearchTypeHelper {
	static boolean hasSurroundingRegExpSlashes(String str) {
		return str != null ? str.trim().matches("^/.*/$"): false;
	}

	static String stripSurroundingRegExpSlashes(String regexp) {
		return regexp != null ? regexp.trim().replaceAll("^/|/$", "").trim() : null;
	}

	static String stripAllSurroundingRegExpSlashes(String regexp) {
		while (hasSurroundingRegExpSlashes(regexp)) {
			regexp = stripSurroundingRegExpSlashes(regexp);
		}
		return regexp;
	}

	static String addSurroundingRegExpSlashes(String str) {
		return "/" + (str != null ? str.trim() : "") + "/";
	}

	static String addSurroundingRegExpSlashesIfNotAlreadyThere(String str) {
		return hasSurroundingRegExpSlashes(str) ? str : addSurroundingRegExpSlashes(str);
	}
}
