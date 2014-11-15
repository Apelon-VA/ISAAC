package gov.va.isaac.gui.enhancedsearchview.model.type.sememe;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;

import gov.va.isaac.search.CompositeSearchResult;

public class SememeSearchResult extends CompositeSearchResult {

	private ConceptVersionBI assembCon;
	private String attachedData;

	public SememeSearchResult(ComponentVersionBI matchingComponent, ConceptVersionBI assembCon, String attachedData) {
		super(matchingComponent, 0);

		this.assembCon = assembCon;
		this.attachedData = attachedData;
	}

	public ConceptVersionBI getAssembCon() {
		return assembCon;
	}

	public String getAttachedData() {
		return attachedData;
	}
}
