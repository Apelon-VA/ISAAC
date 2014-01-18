package gov.va.isaac.model;

import javafx.collections.ObservableList;

import com.sun.javafx.collections.ImmutableObservableList;


/**
 * An enumerated type representing various information models.
 *
 * @author ocarlsen
 */
public enum InformationModel {

    FHIM("Federal Health Information Model"),
    CEM("Clinical Element Model"),
    CIMI("Clinical Information Model Initiative"),
    HeD("Health eDecision");

    private static final ImmutableObservableList<InformationModel> VALUES =
            new ImmutableObservableList<>(InformationModel.values());

    private final String displayName;

    private InformationModel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public static ObservableList<InformationModel> asObservableList() {
        return VALUES;
    }
}
