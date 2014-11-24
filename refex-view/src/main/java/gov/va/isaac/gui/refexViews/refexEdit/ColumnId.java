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

package gov.va.isaac.gui.refexViews.refexEdit;

import java.util.UUID;

/**
 * ColumnId
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
public class ColumnId {	
	private final String columnDescriptionUuidOrColumnType;
	private final Integer columnOrder;

	public static ColumnId getInstance(DynamicRefexColumnType aDynamicRefexColumnType) {
		return getInstance(aDynamicRefexColumnType.toString(), null);
	}
	public static ColumnId getInstance(UUID columnDescriptionUuid, Integer columnOrder) {
		return getInstance((Object)columnDescriptionUuid, columnOrder);
	}
	private static ColumnId getInstance(Object columnDescriptionUuidOrColumnType, Integer columnOrder) {
		return new ColumnId(columnDescriptionUuidOrColumnType.toString(), columnOrder);
	}

	/**
	 * @param columnDescriptionUuidOrColumnType
	 * @param columnOrder
	 */
	private ColumnId(String columnDescriptionUuidOrColumnType, Integer columnOrder) {
		super();
		this.columnDescriptionUuidOrColumnType = columnDescriptionUuidOrColumnType;
		this.columnOrder = columnOrder;
	}
	/**
	 * @return the columnDescriptionUuidOrColumnType
	 */
	public String getColumnDescriptionUuidOrColumnType() {
		return columnDescriptionUuidOrColumnType;
	}
	/**
	 * @return the columnOrder
	 */
	public Integer getColumnOrder() {
		return columnOrder;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((columnDescriptionUuidOrColumnType == null) ? 0
						: columnDescriptionUuidOrColumnType.hashCode());
		result = prime * result
				+ ((columnOrder == null) ? 0 : columnOrder.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ColumnId other = (ColumnId) obj;
		if (columnDescriptionUuidOrColumnType == null) {
			if (other.columnDescriptionUuidOrColumnType != null)
				return false;
		} else if (!columnDescriptionUuidOrColumnType
				.equals(other.columnDescriptionUuidOrColumnType))
			return false;
		if (columnOrder == null) {
			if (other.columnOrder != null)
				return false;
		} else if (!columnOrder.equals(other.columnOrder))
			return false;
		return true;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return columnDescriptionUuidOrColumnType + (columnOrder != null ? (":" + columnOrder) : "");
	}
}