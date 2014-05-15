package gov.va.isaac.gui.refexViews.refexCreation;

import java.util.ArrayList;
import java.util.List;

import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.RefexDynamicData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WizardController {
	private ConceptChronicleBI refsetCon;

	private String refexName;
	private String refsetDescription;
	private int extendedFieldsCount;
	private boolean isAnnotated;
	private ConceptVersionBI parentConcept;
	
	private List<ConceptVersionBI> columnNids = new ArrayList<ConceptVersionBI>();
	private List<RefexDynamicDataType> columnTypes = new ArrayList<RefexDynamicDataType>();
	private List<Object> columnDefaultValues = new ArrayList<>();
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

	public void setColumnVals(int currentCol, ConceptVersionBI colCon, RefexDynamicDataType type, Object defaultValueObject, boolean isMandatory) {
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

	public String getColumnDescription(int column) {
		try {
			return columnNids.get(column).getFullySpecifiedDescription().getText().trim();
		} catch (Exception e) {
			logger.error("Unable to identify FSN of column #" + column, e);
			return "Not Accessible";
		}
	}

	public String getColumnType(int column) {
		return columnTypes.get(column).getDisplayName();
	}

	public Object getColumnDefaultValue(int column) {
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
			result[i] = new RefexDynamicColumnInfo(i, columnNids.get(i).getPrimordialUuid(), columnTypes.get(i), columnDefaultValues.get(i));
		}
		return result;
	}
	public String getColumnName(int column) {
		try {
			return columnNids.get(column).getPreferredDescription().getText().trim();
		} catch (Exception e) {
			logger.error("Unable to identify FSN of column #" + column, e);
			return "Not Accessible";
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
