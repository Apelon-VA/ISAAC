package gov.va.isaac.gui.mapping.data;

import gov.va.isaac.util.OTFUtility;
import gov.va.isaac.util.Utility;

import java.util.HashMap;
import java.util.UUID;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;

public class MappingObject extends StampedItem {
	
	protected UUID editorStatusConcept;
	protected final SimpleStringProperty editorStatusConceptProperty = new SimpleStringProperty();
	protected HashMap<UUID, String> cachedValues = new HashMap<>();
	
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
		propertyLookup(editorStatusConcept, editorStatusConceptProperty);
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
					property.set(s);
				});
			}
		}
	}
}
