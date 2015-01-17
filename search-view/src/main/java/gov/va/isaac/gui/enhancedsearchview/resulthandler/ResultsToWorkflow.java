package gov.va.isaac.gui.enhancedsearchview.resulthandler;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.enhancedsearchview.SearchTypeEnums.ResultsType;
import gov.va.isaac.gui.enhancedsearchview.model.SearchModel;
import gov.va.isaac.interfaces.utility.DialogResponse;
import gov.va.isaac.interfaces.workflow.ComponentWorkflowServiceI;
import gov.va.isaac.interfaces.workflow.ProcessInstanceCreationRequestI;
import gov.va.isaac.interfaces.workflow.WorkflowProcess;
import gov.va.isaac.search.CompositeSearchResult;

import java.util.HashMap;
import java.util.Map;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResultsToWorkflow {
	private static final Logger LOG = LoggerFactory.getLogger(ResultsToWorkflow.class);
	private static ComponentWorkflowServiceI conceptWorkflowService;
	private static SearchModel searchModel = new SearchModel();

	// TODO (artf23141) This doesn't make sense here.  Should be exported to listView, then Workflow
	public static void multipleResultsToWorkflow() {
		initializeWorkflowServices();

		// Use HashSet to ensure that only one workflow is created for each concept
		Map<Integer, ComponentVersionBI> conceptsOrComponents = new HashMap<>();
		if (searchModel.getResultsTypeComboBox().getSelectionModel().getSelectedItem() == ResultsType.CONCEPT) {
			for (CompositeSearchResult result : searchModel.getSearchResultsTable().getResults().getItems()) {
				ConceptVersionBI conceptVersion = result.getContainingConcept();
				int nid = conceptVersion.getNid();
				
				if (! conceptsOrComponents.containsKey(nid)) {
					conceptsOrComponents.put(nid, conceptVersion);
				}
			}
		} else if (searchModel.getResultsTypeComboBox().getSelectionModel().getSelectedItem() == ResultsType.DESCRIPTION) {
			for (CompositeSearchResult result : searchModel.getSearchResultsTable().getResults().getItems()) {
				ComponentVersionBI componentVersion = result.getMatchingComponents().iterator().next();
				int nid = componentVersion.getNid();
				
				if (! conceptsOrComponents.containsKey(nid)) {
					conceptsOrComponents.put(nid, componentVersion);
				}
			}
		} else {
			String title = "Failed exporting search results to workflow";
			String msg = "Unsupported AggregationType " + searchModel.getResultsTypeComboBox().getSelectionModel().getSelectedItem();
			LOG.error(title + ". " + msg);
			AppContext.getCommonDialogs().showErrorDialog(title, msg, title + ". " + msg, AppContext.getMainApplicationWindow().getPrimaryStage());
		}

		if (conceptsOrComponents.size() > 0) {
			DialogResponse response = AppContext.getCommonDialogs().showYesNoDialog("Bulk Workflow Export Confirmation", "Are you sure that you want to generate " + conceptsOrComponents.size() + " new Workflow instance(s)?");

			if (response == DialogResponse.YES) {
				for (ComponentVersionBI conceptOrComponent : conceptsOrComponents.values()) {
					singleResultToWorkflow(conceptOrComponent);
				}

				conceptWorkflowService.synchronizeWithRemote();
			}
		}
	}


	private static void singleResultToWorkflow(ComponentVersionBI componentOrConceptVersion) {
		initializeWorkflowServices();

		final WorkflowProcess process = WorkflowProcess.REVIEW3;
		String preferredDescription = null;
		try {
			if (componentOrConceptVersion instanceof ConceptVersionBI) {
				DescriptionVersionBI<?> desc = ((ConceptVersionBI)componentOrConceptVersion).getPreferredDescription();
				preferredDescription = desc.getText();
			} else {
				preferredDescription = componentOrConceptVersion.toUserString();
			}
			
			LOG.debug("Invoking createNewConceptWorkflowRequest(preferredDescription=\"" + preferredDescription + "\", conceptUuid=\"" 
					+ componentOrConceptVersion.getPrimordialUuid().toString() + "\", processName=\"" + process.getText() + "\")");
			ProcessInstanceCreationRequestI createdRequest = conceptWorkflowService.createNewComponentWorkflowRequest(preferredDescription, 
					componentOrConceptVersion.getPrimordialUuid(), process.getText(), new HashMap<String,String>());
			LOG.debug("Created ProcessInstanceCreationRequestI: " + createdRequest);
			
		} catch (Exception e1) {
			String title = "Error sending component to workflow";
			String msg = title + ". Unexpected error while sending the component (nid=" + componentOrConceptVersion.getNid() + ", uuid=" 
					+ componentOrConceptVersion.getPrimordialUuid().toString() + "): caught " + e1.getClass().getName() + " " + e1.getLocalizedMessage();
			LOG.error(msg, e1);
			AppContext.getCommonDialogs().showErrorDialog(title, msg, e1.getMessage(), AppContext.getMainApplicationWindow().getPrimaryStage());
			
			return;
		}

	}

	private static void initializeWorkflowServices() {
		if (conceptWorkflowService == null) {
			conceptWorkflowService = AppContext.getService(ComponentWorkflowServiceI.class);
		}

		assert conceptWorkflowService != null;
	}
}
