package gov.va.isaac.gui.enhancedsearchview.resulthandler;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.enhancedsearchview.SearchTypeEnums.SearchType;
import gov.va.isaac.gui.enhancedsearchview.filters.IsDescendantOfFilter;
import gov.va.isaac.gui.enhancedsearchview.model.SearchModel;
import gov.va.isaac.gui.enhancedsearchview.model.type.text.TextSearchTypeModel;
import gov.va.isaac.gui.util.CustomClipboard;
import gov.va.isaac.util.WBUtility;

import java.util.UUID;

public class ResultsToDrools {

	public static void createDroolsOnClipboard(SearchModel searchModel) {
		if (!handleExclusions(searchModel)) {
			// Check for EMPTY with isKindOf
			String resultStr = null;
			
			if (1 == 1) {
				resultStr = isKindOfBuild(searchModel);
			}
			
			if (resultStr != null) {
				CustomClipboard.set(resultStr);
			}
		}
	}
	
	private static boolean handleExclusions(SearchModel searchModel) {
		if (searchModel.getSearchTypeSelector().getCurrentType() != SearchType.TEXT) {
			AppContext.getCommonDialogs().showInformationDialog("Drools From Search Failure", "Unable to create drools rules from search type: " + searchModel.getSearchTypeSelector().getCurrentType());
			return true;
		} else {
			TextSearchTypeModel model = (TextSearchTypeModel)searchModel.getSearchTypeSelector().getTypeSpecificModel();
		
			if (model.getSearchType().getSearchParameter() != null && !model.getSearchType().getSearchParameter().isEmpty()) {
				AppContext.getCommonDialogs().showInformationDialog("Drools From Search Failure", "Current implementation only allows for creation of drools rule based on empty search string \rsuch that it can be added to nightly QA: " + model.getSearchType().getSearchParameter());
				return true;
			} else if (model.getFilters().size() != 1) {
				AppContext.getCommonDialogs().showInformationDialog("Drools From Search Failure", "Current implementation only allows for creation of drools rule based on one search filter");
				return true;
			} else if (!(model.getFilters().get(0) instanceof IsDescendantOfFilter) ) {
				AppContext.getCommonDialogs().showInformationDialog("Drools From Search Failure", "Current implementation only allows for creation of drools rule based on isDescenedant search filter");
				return true;
			}
		}
		
		return false;		
	}

	private static String isKindOfBuild(SearchModel searchModel) {
		TextSearchTypeModel model = (TextSearchTypeModel)searchModel.getSearchTypeSelector().getTypeSpecificModel();

		StringBuilder strBld = new StringBuilder();
		strBld.append(getPackage());
		strBld.append("import gov.va.issac.drools.testmodel.DrConcept\r");
		strBld.append("import gov.va.issac.drools.helper.templates.ConceptTemplate\r\r");
		strBld.append(getGenericImports());
		strBld.append("\r");
		strBld.append(getGlobals());
		strBld.append("\r");
		strBld.append(setupRule());
		
		// Define Variable
		strBld.append("\t\t$c : ConceptVersionBI( )\r");
		
		// Setup Eval
		strBld.append("\t\teval(terminologyHelper.isParentOf( ");
		
		UUID id = WBUtility.getConceptVersion(((IsDescendantOfFilter)model.getFilters().get(0)).getNidProperty().get()).getPrimordialUuid();
		
		strBld.append("\"" + id.toString() + "\"");
		strBld.append(", $c.dataUUID.toString))\r");
		
		// Placeholder for then
		strBld.append("\tthen\r");
		
		return strBld.toString();
	}

	private static Object setupRule() {
		return "rule <RULE_NAME>\r" +
			   "\twhen\r";
	}

	private static Object getGlobals() {
		return "global gov.va.issac.drools.helper.ResultsCollector resultsCollector\r" +
			   "global gov.va.issac.drools.helper.TerminologyHelperDrools terminologyHelper\r";
	}

	private static Object getGenericImports() {
		return "import gov.va.issac.drools.helper.ResultsItemError\r" + 
			   "import gov.va.issac.drools.helper.TerminologyHelperDrools\r" + 
			   "import java.util.UUID\r";
	}

	private static String getPackage() {
		return "package gov.va.isaac.drools.rules\r\r";
	}

}
