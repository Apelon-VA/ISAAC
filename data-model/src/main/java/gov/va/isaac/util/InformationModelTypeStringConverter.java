package gov.va.isaac.util;

import gov.va.isaac.model.InformationModelType;
import javafx.util.StringConverter;

/**
 * A {@link StringConverter} for the {@link InformationModelType} enum.
 *
 * @author ocarlsen
 */
public class InformationModelTypeStringConverter extends StringConverter<InformationModelType> {

    @Override
    public String toString(InformationModelType modelType) {
        return modelType.getDisplayName();
    }

    @Override
    public InformationModelType fromString(String modelTypeName) {
        return InformationModelType.valueOf(modelTypeName);
    }
}