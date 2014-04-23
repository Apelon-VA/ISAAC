package gov.va.isaac.gui.refexViews.refexCreation;

import java.util.ArrayList;
import java.util.List;

import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;

public class WizardController {
	private int totalColumns;

	private ConceptChronicleBI refsetCon;

	private String refexName;
	private String refsetDescription;
	private int extendedFieldsCount;
	private boolean isAnnotated;
//	private boolean isReadOnly;
	
	private List<ConceptVersionBI> columnNids = new ArrayList<ConceptVersionBI>();
	private List<RefexDynamicDataType> columnTypeStrings = new ArrayList<RefexDynamicDataType>();
	private List<String> columnDefaultValues = new ArrayList<String>();
	private List<Boolean> columnIsMandatory = new ArrayList<Boolean>();
	private ConceptVersionBI parentConcept;
	private boolean isReadOnly;
	

	public void setRefsetConcept(ConceptChronicleBI con) {
		refsetCon = con;
	}
	public ConceptChronicleBI getRefsetConcept() {
		return refsetCon;
	}

	public void setNewRefsetConceptVals(String name, String description, ConceptVersionBI parentConcept, int extendedFieldsCount, boolean isAnnotated, boolean isReadOnly) {
		this.refexName = name;
		this.refsetDescription = description;
		this.parentConcept = parentConcept;
		this.extendedFieldsCount = extendedFieldsCount + 1;
		this.isAnnotated = isAnnotated;
		this.isReadOnly = isReadOnly;
	}

	public void setReferencedComponentVals(ConceptVersionBI colCon, RefexDynamicDataType type, String defaultValue, boolean isMandatory) {
		setColumnVals(colCon, type, defaultValue, isMandatory);
	}

	public void setColumnVals(ConceptVersionBI colCon, RefexDynamicDataType type, String defaultValue, boolean isMandatory) {
		columnNids.add(colCon);
		columnTypeStrings.add(type);
		columnDefaultValues.add(defaultValue);		
		columnIsMandatory.add(isMandatory);
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

	private String getConceptFsn(ConceptVersionBI con) {
		try {
			if (con == null) {
				return "";
			}
			return con.getFullySpecifiedDescription().getText().trim();
		} catch (Exception e) {
			e.printStackTrace();
			return "Not Accessible";
		}
	}

	public boolean isReadOnlyRefex() {
		return isReadOnly;
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
			e.printStackTrace();
			return "Not Accessible";
		}
	}

	public String getColumnType(int column) {
		return columnTypeStrings.get(column).getDisplayName();
	}

	public String getColumnDefaultValue(int column) {
		return columnDefaultValues.get(column);
	}

	public String getColumnIsMandatory(int column) {
		if (columnIsMandatory.get(column)) {
			return "Mandatory";
		} else {
			return "Optional";
		}
	}

	public String getRefCompDesc() {
		return getConceptFsn(columnNids.get(0));
	}


}
