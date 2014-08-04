package gov.va.isaac.gui.conceptViews.modeling;

import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerHelper.ComponentType;
import gov.va.isaac.interfaces.gui.MenuItemI;
import gov.va.isaac.interfaces.gui.views.PopupConceptViewI;
import gov.va.isaac.interfaces.gui.views.PopupViewI;
import gov.va.isaac.util.UpdateableBooleanBinding;

import java.util.ArrayList;
import java.util.List;

import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import org.glassfish.hk2.runlevel.RunLevelException;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;

public abstract class ModelingPopup extends Stage implements PopupViewI {

	protected String popupTitle;
	protected ComponentVersionBI origComp = null;
	protected UpdateableBooleanBinding allValid_;
	protected PopupConceptViewI callingView_;

	abstract void finishInit();
	
	/**
	 * @see gov.va.isaac.interfaces.gui.views.IsaacViewI#getMenuBarMenus()
	 */
	@Override
	public List<MenuItemI> getMenuBarMenus()
	{
		return new ArrayList<>();
	}


	/**
	 * Call setReferencedComponent first
	 * 
	 * @see gov.va.isaac.interfaces.gui.views.PopupViewI#showView(javafx.stage.Window)
	 */
	@Override
	public void showView(Window parent)
	{
		if (origComp == null)
		{
			throw new RunLevelException("referenced component nid must be set first");
		}
		setTitle(popupTitle);
		setResizable(true);

		initOwner(parent);
		initModality(Modality.NONE);
		initStyle(StageStyle.DECORATED);

		setWidth(600);
		setHeight(400);

		show();
	}


	public void finishInit(ComponentVersionBI comp, ComponentType type, PopupConceptViewI callingView)
	{
		origComp = comp;
		callingView_ = callingView;
		
		finishInit();
	}

}
