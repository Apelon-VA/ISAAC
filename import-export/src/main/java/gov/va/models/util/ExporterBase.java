package gov.va.models.util;

import gov.va.isaac.gui.AppContext;

import java.io.IOException;

import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;

/**
 * Base class containing common methods for exporting terminology objects.
 *
 * TODO: Factor out common logic with {@link ImporterBase}.
 *
 * @author ocarlsen
 */
public class ExporterBase {

    private final BdbTerminologyStore dataStore;

    protected ExporterBase(AppContext appContext) throws ValidationException, IOException {
        super();
        this.dataStore = appContext.getDataStore();
    }

    protected final BdbTerminologyStore getDataStore() {
        return dataStore;
    }

    protected final EditCoordinate getEC() throws ValidationException, IOException {
        int authorNid = TermAux.USER.getLenient().getConceptNid();
        int module = Snomed.CORE_MODULE.getLenient().getNid();
        int editPathNid = TermAux.SNOMED_CORE.getLenient().getConceptNid();

        return new EditCoordinate(authorNid, module, editPathNid);
    }

    protected final ViewCoordinate getVC() throws IOException {
        return StandardViewCoordinates.getSnomedStatedLatest();
    }
}
