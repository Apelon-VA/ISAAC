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
 * DynamicRefexHelper
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.gui.enhancedsearchview;

import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicUsageDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DynamicRefexHelper
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public class DynamicRefexHelper {
	private static final Logger LOG = LoggerFactory.getLogger(DynamicRefexHelper.class);

	/**
	 * Do not instantiate
	 */
	private DynamicRefexHelper() {}
	
	public static void displayDynamicRefex(RefexDynamicVersionBI<?> refex) {
		displayDynamicRefex(refex, 0);
	}
	public static void displayDynamicRefex(RefexDynamicVersionBI<?> refex, int depth) {
		String indent = "";
		
		for (int i = 0; i < depth; ++i) {
			indent += "\t";
		}
		
		RefexDynamicUsageDescription dud = null;
		try {
			dud = refex.getRefexDynamicUsageDescription();
		} catch (IOException | ContradictionException e) {
			LOG.error("Failed executing getRefexDynamicUsageDescription().  Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
			e.printStackTrace();
			
			return;
		}
		RefexDynamicColumnInfo[] colInfo = dud.getColumnInfo();
		RefexDynamicDataBI[] data = refex.getData();
		LOG.debug(indent + "dynamic refex nid=" + refex.getNid() + ", uuid=" + refex.getPrimordialUuid());
		LOG.debug(indent + "dynamic refex name=\"" + dud.getRefexName() + "\": " + refex.toUserString() + " with " + colInfo.length + " columns:");
		for (int colIndex = 0; colIndex < colInfo.length; ++colIndex) {
			RefexDynamicColumnInfo currentCol = colInfo[colIndex];
			String name = currentCol.getColumnName();
			RefexDynamicDataType type = currentCol.getColumnDataType();
			UUID colUuid = currentCol.getColumnDescriptionConcept();
			RefexDynamicDataBI colData = data[colIndex];

			LOG.debug(indent + "\t" + "dynamic refex: " + refex.toUserString() + " col #" + colIndex + " (uuid=" + colUuid + ", type=" + type.getDisplayName() + "): " + name + "=" + colData.getDataObject());
		}
		
		Collection<? extends RefexDynamicVersionBI<?>> embeddedRefexes = null;
		try {
			embeddedRefexes = refex.getRefexesDynamicActive(WBUtility.getViewCoordinate());

			for (RefexDynamicVersionBI<?> embeddedRefex : embeddedRefexes) {
				displayDynamicRefex(embeddedRefex, depth + 1);
			}
		} catch (IOException e) {
			LOG.error("Failed executing getRefexesDynamicActive(WBUtility.getViewCoordinate()).  Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
			e.printStackTrace();
		}
	}
}
