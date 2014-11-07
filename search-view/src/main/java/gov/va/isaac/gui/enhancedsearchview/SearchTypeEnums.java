package gov.va.isaac.gui.enhancedsearchview;

import gov.va.isaac.gui.enhancedsearchview.filters.IsAFilter;
import gov.va.isaac.gui.enhancedsearchview.filters.IsDescendantOfFilter;
import gov.va.isaac.gui.enhancedsearchview.filters.NonSearchTypeFilter;

public class SearchTypeEnums  {

	public enum Tasks {
		SEARCH,
		WORKFLOW_EXPORT
	}

	public enum SearchType {
		COMPONENT_CONTENT("Component Based Search"),
		REFSET_SPEC("Refset Specification Search"),
		REFSET_CONTENT("Refset Based Search");

		private final String display;

		private SearchType(String display) {
			this.display = display;
		}

		@Override
		public String toString() {
			return display;
		}
	}

	public enum ComponentSearchType {
		LUCENE("Lucene"),
		REGEXP("RegExp"),
		CONCEPT("Concept Search");
		

		private final String display;

		private ComponentSearchType(String display) {
			this.display = display;
		}

		@Override
		public String toString() { return display; }
	}

	public enum ResultsType {
		CONCEPT("Concept"),
		DESCRIPTION("Description");

		private final String display;

		private ResultsType(String display) {
			this.display = display;
		}

		@Override
		public String toString() {
			return display;
		}
	}

	public enum TaxonomyViewMode {
		FILTERED,
		UNFILTERED
	}
	
	public enum FilterType {
		IS_DESCENDANT_OF(IsDescendantOfFilter.class),
		IS_A(IsAFilter.class);
		
		private final Class<? extends NonSearchTypeFilter<?>> clazz;
		
		private FilterType(Class<? extends NonSearchTypeFilter<?>> aClazz) {
			clazz = aClazz;
		}
		
		public Class<? extends NonSearchTypeFilter<?>> getClazz() { return clazz; }
		
		public static FilterType valueOf(Class<? extends NonSearchTypeFilter> filterType) {
			for (FilterType type : values()) {
				if (type.getClazz() == filterType) {
					return type;
				}
			}
			
			return null;
		}
	}
}
