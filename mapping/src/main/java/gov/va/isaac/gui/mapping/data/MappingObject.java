package gov.va.isaac.gui.mapping.data;

import gov.va.isaac.util.OTFUtility;
import gov.va.isaac.util.Utility;

import java.util.HashMap;
import java.util.UUID;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;

public class MappingObject extends StampedItem {
	protected HashMap<UUID, SimpleStringProperty> cachedValues = new HashMap<>();
	
	protected SimpleStringProperty propertyLookup(UUID uuid)
	{
		if (uuid == null)
		{
			return new SimpleStringProperty("");
		}
		SimpleStringProperty ssp = cachedValues.get(uuid);
		if (ssp == null)
		{
			ssp = new SimpleStringProperty("-");
			cachedValues.put(uuid, ssp);
		}
		
		SimpleStringProperty ssp2 = cachedValues.get(uuid);
		
		if (ssp.get().equals("-"))
		{
			Utility.execute(() ->
			{
				String s = OTFUtility.getDescription(uuid);
				Platform.runLater(() ->
				{
					ssp2.set(s);
				});
			});
		}
		return ssp2;
	}
}
