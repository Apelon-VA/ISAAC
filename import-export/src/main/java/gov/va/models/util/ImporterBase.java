package gov.va.models.util;

import java.io.IOException;

import javax.inject.Inject;

import org.ihtsdo.otf.tcc.api.blueprint.TerminologyBuilderBI;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.datastore.BdbTermBuilder;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;

/**
 * Base class containing common methods for importing terminology objects.
 *
 * TODO: Factor out common logic with {@link ExporterBase}.
 *
 * @author ocarlsen
 */
public class ImporterBase {


    @Inject
    private BdbTerminologyStore dataStore;

    private final TerminologyBuilderBI builder;

    protected ImporterBase() throws ValidationException, IOException {
        super();
        Hk2Looker.get().inject(this);

        this.builder = new BdbTermBuilder(getEC(), getVC());
    }

    protected final BdbTerminologyStore getDataStore() {
        return dataStore;
    }

    protected final TerminologyBuilderBI getBuilder() {
        return builder;
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
