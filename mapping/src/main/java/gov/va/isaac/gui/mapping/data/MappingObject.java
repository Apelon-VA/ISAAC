package gov.va.isaac.gui.mapping.data;

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.util.OTFUtility;
import gov.va.isaac.util.Utility;

import java.util.HashMap;
import java.util.UUID;

import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;

public class MappingObject extends StampedItem {
	
	protected UUID editorStatusConcept = null;
	protected int editorStatusConceptNid = 0;
	protected final SimpleStringProperty editorStatusConceptProperty = new SimpleStringProperty();
	protected HashMap<UUID, String> cachedValues = new HashMap<>();
	
	protected static BdbTerminologyStore dataStore = ExtendedAppContext.getDataStore();
	
	/**
	 * @return the editorStatusConcept
	 */
	public UUID getEditorStatusConcept()
	{
		return editorStatusConcept;
	}

	/**
	 * @param editorStatusConcept the editorStatusConcept to set
	 */
	public void setEditorStatusConcept(UUID editorStatusConcept)
	{
		this.editorStatusConcept = editorStatusConcept;
		this.editorStatusConceptNid = getNidForUuidSafe(editorStatusConcept);
		propertyLookup(editorStatusConcept, editorStatusConceptProperty);
	}

	public int getEditorStatusConceptNid() {
		return editorStatusConceptNid;
	}

	public SimpleStringProperty getEditorStatusConceptProperty()
	{
		return editorStatusConceptProperty;
	}

	protected void propertyLookup(UUID uuid, SimpleStringProperty property)	{
		if (uuid == null) {
			property.set(null);
		} else {
			String cachedValue = cachedValues.get(uuid);
			if (cachedValue != null) {
				property.set(cachedValue);
			} else {
				property.set("-");
				Utility.execute(() -> {
					String s = OTFUtility.getDescription(uuid);
					cachedValues.put(uuid, s);
					Platform.runLater(() -> {
						property.set(s);
					});
				});
			}
		}
	}
	
	public static int getNidForUuidSafe(UUID uuid) {
		return (uuid == null)? 0 : dataStore.getNidForUuids(new UUID[] { uuid });
	}
}
