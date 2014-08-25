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
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicValidatorType;

/**
 * 
 * {@link RefexData}
 *
 * @author <a href="jefron@apelon.com">Jesse Efron</a>
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefexData
{
	private String refexName_;
	private String refexDescription_;
	private boolean isAnnotatedStyle_;
	private ConceptVersionBI parentConcept_;
	private ArrayList<RefexDynamicColumnInfo> columnInfo_ = new ArrayList<>();

	public RefexData(String name, String description, ConceptVersionBI parentConcept, int extendedFieldsCount, boolean isAnnotatedStyle)
	{
		this.refexName_ = name;
		this.refexDescription_ = description;
		this.parentConcept_ = parentConcept;
		this.isAnnotatedStyle_ = isAnnotatedStyle;
		for (int i = 0; i < extendedFieldsCount; i++)
		{
			RefexDynamicColumnInfo rdci = new RefexDynamicColumnInfo();
			rdci.setColumnOrder(i);
			columnInfo_.add(rdci);
		}
	}

	public void setColumnVals(int currentCol, ConceptVersionBI colNameConcept, RefexDynamicDataType type, RefexDynamicDataBI defaultValueObject, boolean isMandatory,
			RefexDynamicValidatorType validatorType, RefexDynamicDataBI validatorData)
	{
		adjustColumnCount(currentCol);
		RefexDynamicColumnInfo rdci = columnInfo_.get(currentCol);
		rdci.setColumnDescriptionConcept(colNameConcept.getPrimordialUuid());
		rdci.setColumnDataType(type);
		rdci.setColumnDefaultData(defaultValueObject);
		rdci.setColumnRequired(isMandatory);
		rdci.setValidatorType(validatorType);
		rdci.setValidatorData(validatorData);
	}
	
	public void adjustColumnCount(int col)
	{
		while (col > columnInfo_.size())
		{
			RefexDynamicColumnInfo rdci = new RefexDynamicColumnInfo();
			rdci.setColumnOrder(columnInfo_.size());
			columnInfo_.add(rdci);
		}
		
		while (columnInfo_.size() > col)
		{
			columnInfo_.remove(columnInfo_.size() - 1);
		}
	}

	public String getRefexName()
	{
		return refexName_;
	}
	
	public void setRefexName(String refexName)
	{
		refexName_ = refexName;
	}

	public String getRefexDescription()
	{
		return refexDescription_;
	}
	
	public void setRefexDescription(String refexDescription)
	{
		refexDescription_ = refexDescription;
	}

	public ConceptVersionBI getParentConcept()
	{
		return parentConcept_;
	}
	
	public void setParentConcept(ConceptVersionBI parentConcept)
	{
		parentConcept_ = parentConcept;
	}

	public boolean isAnnotatedStyle()
	{
		return isAnnotatedStyle_;
	}
	
	public void setIsAnnotatedStyle(boolean annotatedStyle)
	{
		isAnnotatedStyle_ = annotatedStyle;
	}

	public int getExtendedFieldsCount()
	{
		return columnInfo_.size();
	}

	public List<RefexDynamicColumnInfo> getColumnInfo()
	{
		return columnInfo_;
	}
}
