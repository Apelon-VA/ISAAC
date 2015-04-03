package gov.va.isaac.gui.mapping;

public enum MappingColumnType {
	STATUS_CONDENSED("Status Condensed"),
	NAME("Name"),
	DESCRIPTION("Description"),
	PURPOSE("Purpose"),
	EDITOR_STATUS("Editor Status", true),
	INVERSE_NAME("Inverse Name"),
	
	SOURCE("Source Concept", true),
	TARGET("Target Concept", true),
	QUALIFIER("Qualifier", true),
	COMMENTS("Comments"),
	
	CONCEPT("Concept", true),
	CODE_SYSTEM("Code System", true),

	STAMP("STAMP"),
	STATUS_STRING("Status"),
	TIME("Time"),
	AUTHOR("Author",true),
	MODULE("Module",true),
	PATH("Path",true);
	
	private String niceName_;
	private boolean isConcept_ = false;
	
	private MappingColumnType(String name) {
		this(name, false);
	}
	
	private MappingColumnType(String name, boolean isConcept)
	{
		niceName_ = name;
		isConcept_ = isConcept;
	}

	@Override
	public String toString()
	{
		return niceName_;
	}
	
	public boolean isConcept() {
		return isConcept_;
	}

}
