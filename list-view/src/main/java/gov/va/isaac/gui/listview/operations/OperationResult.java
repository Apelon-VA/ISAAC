package gov.va.isaac.gui.listview.operations;

import gov.va.isaac.gui.SimpleDisplayConcept;
import java.util.Set;


public class OperationResult {

	private String operationMsg;
	private Set<SimpleDisplayConcept> modifiedConcepts;
	@SuppressWarnings("unused")
	private String title;

	public OperationResult(String title, boolean cancelRequested_) {
		if (!cancelRequested_) {
			operationMsg = title + "Missing content";
		} else {
			operationMsg = title + " was cancelled";
		}
	}

	public OperationResult(String title, Set<SimpleDisplayConcept> modifiedCons, String msgBuffer) {
		operationMsg = msgBuffer;
		this.title = title;
		this.modifiedConcepts = modifiedCons;
	}

	public OperationResult() {
		// TODO Auto-generated constructor stub
	}

	public String getOperationMsg() {
		return operationMsg;
	}
	
	public Set<SimpleDisplayConcept> getModifiedConcepts() {
		return modifiedConcepts;
	}
}
