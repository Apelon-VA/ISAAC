package gov.va.isaac.gui.enhancedsearchview;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.enhancedsearchview.SearchTypeEnums.SearchType;
import gov.va.isaac.gui.enhancedsearchview.model.SearchModel;
import gov.va.isaac.gui.enhancedsearchview.model.type.text.TextSearchTypeModel;

import org.apache.mahout.math.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchValidator {
	private static final Logger LOG = LoggerFactory.getLogger(SearchValidator.class);

	static public boolean validateComponentSearchViewModel(SearchModel model, String errorDialogTitle) {
		if (model.getSearchTypeSelector().getTypeSpecificModel().getViewCoordinate() == null) {
			String details = "View coordinate is null: " + model;
			LOG.warn("Invalid search model (name=" + model.getSearchTypeSelector().getTypeSpecificModel().getName() + "). " + details);

			if (errorDialogTitle != null) {
				AppContext.getCommonDialogs().showErrorDialog(errorDialogTitle, errorDialogTitle, details, AppContext.getMainApplicationWindow().getPrimaryStage());
			}

			return false;
		} else if (model.getSearchTypeSelector().getCurrentType() == SearchType.TEXT) {
			TextSearchTypeModel typeModel = (TextSearchTypeModel)model.getSearchTypeSelector().getTypeSpecificModel();
			if (typeModel.getSearchType() == null) {
				String details = "No SearchTypeFilter specified: " + model;
				LOG.warn("Invalid search model (name=" + model.getSearchTypeSelector().getTypeSpecificModel().getName() + "). " + details);

				if (errorDialogTitle != null) {
					AppContext.getCommonDialogs().showErrorDialog(errorDialogTitle, errorDialogTitle, details, AppContext.getMainApplicationWindow().getPrimaryStage());
				}

				return false;
			} else if (typeModel.getInvalidFilters().size() > 0) {
				String details = "Found " + typeModel.getInvalidFilters().size() + " invalid filter: " + Arrays.toString(typeModel.getFilters().toArray());
				LOG.warn("Invalid filter in search model (name=" + typeModel.getName() + "). " + details);
	
				if (errorDialogTitle != null) {
					AppContext.getCommonDialogs().showErrorDialog(errorDialogTitle, errorDialogTitle, details, AppContext.getMainApplicationWindow().getPrimaryStage());
				}
	
				return false;
			}
		}
		
		return true;
	}


}
