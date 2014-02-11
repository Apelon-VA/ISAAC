package gov.va.models.util;

import java.io.IOException;

import javax.inject.Inject;

import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;

/**
 * Base class containing common methods for exporting terminology objects.
 *
 * TODO: Factor out common logic with {@link ImporterBase}.
 *
 * @author ocarlsen
 */
public class ExporterBase {

    @Inject
    private BdbTerminologyStore dataStore;

    protected ExporterBase() throws ValidationException, IOException {
        super();
        Hk2Looker.get().inject(this);
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
