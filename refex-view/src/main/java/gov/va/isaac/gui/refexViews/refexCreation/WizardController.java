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
package gov.va.isaac.gui.refexViews.refexCreation;

import java.util.ArrayList;
import java.util.List;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * {@link WizardController}
 *
 * @author <a href="jefron@apelon.com">Jesse Efron</a>
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class WizardController {
	private ConceptChronicleBI refsetCon;

	private String refexName;
	private String refsetDescription;
	private int extendedFieldsCount;
	private boolean isAnnotated;
	private ConceptVersionBI parentConcept;
	
	private List<ConceptVersionBI> columnNids = new ArrayList<ConceptVersionBI>();
	private List<RefexDynamicDataType> columnTypes = new ArrayList<RefexDynamicDataType>();
	private List<RefexDynamicDataBI> columnDefaultValues = new ArrayList<>();
	private List<Boolean> columnIsMandatory = new ArrayList<Boolean>();

	private static final Logger logger = LoggerFactory.getLogger(WizardController.class);

	public void setRefsetConcept(ConceptChronicleBI con) {
		refsetCon = con;
	}
	public ConceptChronicleBI getRefsetConcept() {
		return refsetCon;
	}

	public void setNewRefsetConceptVals(String name, String description, ConceptVersionBI parentConcept, int extendedFieldsCount, boolean isAnnotated) {
		this.refexName = name;
		this.refsetDescription = description;
		this.parentConcept = parentConcept;
		this.extendedFieldsCount = extendedFieldsCount;
		this.isAnnotated = isAnnotated;
	}

	public void setColumnVals(int currentCol, ConceptVersionBI colCon, RefexDynamicDataType type, RefexDynamicDataBI defaultValueObject, boolean isMandatory) {
		if (previouslyFilledOut(currentCol)) {
			columnNids.set(currentCol, colCon);
			columnTypes.set(currentCol, type);
			columnDefaultValues.set(currentCol, defaultValueObject);		
			columnIsMandatory.set(currentCol, isMandatory);
		} else {
			columnNids.add(colCon);
			columnTypes.add(type);
			columnDefaultValues.add(defaultValueObject);		
			columnIsMandatory.add(isMandatory);
		}
	}
	
	public String getRefexName() {
		return refexName;
	}

	public String getRefexDescription() {
		return refsetDescription;
	}

	public String getParentConceptFsn() {
		return getConceptFsn(parentConcept);
	}

	public ConceptVersionBI getParentConcept() {
		return parentConcept;
	}

	private String getConceptFsn(ConceptVersionBI con) {
		try {
			if (con == null) {
				return "";
			}
			return con.getFullySpecifiedDescription().getText().trim();
		} catch (Exception e) {
			logger.error("Unable to identify FSN of concept" + con.getPrimordialUuid().toString(), e);
			return "Not Accessible";
		}
	}

	public boolean isAnnotated() {
		return isAnnotated;
	}
	
	public int getExtendedFieldsCount() {
		return extendedFieldsCount;
	}

	public String getColumnType(int column) {
		return columnTypes.get(column).getDisplayName();
	}

	public RefexDynamicDataBI getColumnDefaultValue(int column) {
		return columnDefaultValues.get(column);
	}

	public String getColumnIsMandatory(int column) {
		if (columnIsMandatory.get(column)) {
			return "True";
		} else {
			return "False";
		}
	}

	public RefexDynamicColumnInfo[] getColumnInfo()
	{
		if (columnTypes.size() == 0) {
			return null;
		}
		
		RefexDynamicColumnInfo[] result = new RefexDynamicColumnInfo[columnTypes.size()];
		for (int i = 0; i < result.length; i++)
		{
			//TODO add GUI for selecting validator details
			result[i] = new RefexDynamicColumnInfo(i, columnNids.get(i).getPrimordialUuid(), columnTypes.get(i), columnDefaultValues.get(i), columnIsMandatory.get(i), null ,null);
		}
		return result;
	}
	public ConceptVersionBI getColumnName(int column) {
		try {
			return columnNids.get(column);
		} catch (Exception e) {
			logger.error("Unable to identify FSN of column #" + column, e);
			return null;
		}
	}
	public boolean isColumnMandatory(int column) {
		return columnIsMandatory.get(column);
	}
	public RefexDynamicDataType getColumnTypeToken(int column) {
		return columnTypes.get(column);
	}
	public boolean previouslyFilledOut(int column) {
		return columnTypes.size() > column;
	}
}
