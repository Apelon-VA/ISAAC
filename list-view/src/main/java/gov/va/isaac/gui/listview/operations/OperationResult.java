package gov.va.isaac.gui.listview.operations;

import java.util.Set;


public class OperationResult {

	private String operationMsg;
	private Set<Integer> modifiedConcepts;
	private String title;

	public OperationResult(String title, boolean cancelRequested_) {
		if (!cancelRequested_) {
			operationMsg = title + "Missing content";
		} else {
			operationMsg = title + " was cancelled";
		}
	}

	public OperationResult(String title, Set<Integer> modifiedConcepts, String msgBuffer) {
		operationMsg = msgBuffer;
		this.title = title;
		this.modifiedConcepts = modifiedConcepts;
	}

	public OperationResult() {
		// TODO Auto-generated constructor stub
	}

	public String getOperationMsg() {
		return operationMsg;
	}
	
	public Set<Integer> getModifiedConcepts() {
		return modifiedConcepts;
	}
}
