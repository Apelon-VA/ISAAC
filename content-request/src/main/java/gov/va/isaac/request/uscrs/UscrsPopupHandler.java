package gov.va.isaac.request.uscrs;

import gov.va.isaac.AppContext;
//import gov.va.isaac.gui.listview.operations.OperationResult;
import gov.va.isaac.interfaces.gui.constants.SharedServiceNames;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.ExportTaskHandlerI;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.ExportTaskViewI;
import gov.va.isaac.interfaces.utility.DialogResponse;
import gov.va.isaac.util.OTFUtility;
import gov.va.isaac.util.Utility;

import java.io.File;
import java.io.IOException;
import java.util.stream.IntStream;

import javafx.concurrent.Task;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

import javax.inject.Named;

import org.glassfish.hk2.api.PerLookup;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Named(value = SharedServiceNames.USCRS)
@PerLookup
public class UscrsPopupHandler implements ExportTaskViewI {
	
	IntStream conceptStream;
	
	private static final Logger LOG = LoggerFactory.getLogger(UscrsContentRequestHandler.class);
	
	@Override
	public IntStream getConcepts() {
		return conceptStream;
	}

	@Override
	public void setConcepts(IntStream conceptInput) {
		conceptStream = conceptInput;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.va.isaac.interfaces.gui.views.PopupViewI#showView(javafx.stage.Window)
	 */
	@Override
	public void showView(Window parent)
	{
		ConceptVersionBI firstConcept = OTFUtility.getConceptVersion(getConcepts().findFirst().getAsInt());
		
		String firstDesc = null;
		try {
			firstDesc = firstConcept.getDescriptions().stream().findFirst().toString();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		DialogResponse verifyConceptSelelction = AppContext.getCommonDialogs().showYesNoDialog("USCRS Content Request", "You are exporting " + firstDesc + " (nid " + firstConcept.getNid() + "). Would you like to continue?");
		
		if(verifyConceptSelelction == DialogResponse.NO) {
			return;
		}
		
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save USCRS Concept Request File");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Excel Files .xls .xlsx", "*.xls", "*.xlsx"));
		fileChooser.setInitialFileName("USCRS_Export.xls");
		File file = fileChooser.showSaveDialog(null);

		try
		{
			/* if ((firstConcept.getPathNid() != TermAux.SNOMED_CORE.getLenient().getNid())
					&& !OTFUtility.getConceptVersion(firstConcept.getPathNid()).getPrimordialUuid().toString().equals(AppContext.getAppConfiguration().getDefaultEditPathUuid())) */
			boolean isOnPath = true;
			if(isOnPath)
			{
				DialogResponse response = AppContext.getCommonDialogs().showYesNoDialog(
						"USCRS Content Request",
						"Concept Nid: " + firstConcept.getNid() + "The concept path is neither Snomed CORE nor " + AppContext.getAppConfiguration().getDefaultEditPathName()
								+ ". It is recommended that you only submit " + "concepts edited on one of these paths to USCRS.\n\n" + "Do you want to continue?");
				if (response == DialogResponse.NO)
				{
					return;
				}
			}
		}
		catch (Exception e)
		{
			AppContext.getCommonDialogs().showErrorDialog("USCRS Content Request", "Unable to load concepts for path comparison.", "This should never happen");
			return;
		}

		try
		{
			ExportTaskHandlerI uscrsExporter = AppContext.getService(ExportTaskHandlerI.class, SharedServiceNames.USCRS);
			if(uscrsExporter != null) {
				Task<Integer> task = uscrsExporter.createTask(conceptStream, file.toPath());
				Utility.execute(task);
				int count = task.get();
				AppContext.getCommonDialogs().showInformationDialog("USCRS Content Request", "Content request submission successful. Output: " + count + "\n\n Upload ");
			} else {
				throw new RuntimeException("The USCRS Content Request Handler is not available on the class path");
			}
		}
		catch (Exception e)
		{
			LOG.error("Unexpected error during submit", e);
			AppContext.getCommonDialogs().showErrorDialog("USCRS Content Request", "Unexpected error trying to submit request.", e.getMessage());
			return;
		}
	}

}
