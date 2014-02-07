package gov.va.models.util;

import gov.va.isaac.gui.AppContext;

import java.io.IOException;

import org.ihtsdo.otf.tcc.api.blueprint.TerminologyBuilderBI;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.datastore.BdbTermBuilder;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;

/**
 * Base class containing common methods for building terminology objects.
 *
 * @author ocarlsen
 */
public class TerminologyBuilderBase {

    private final BdbTerminologyStore dataStore;
    private final TerminologyBuilderBI builder;

    protected TerminologyBuilderBase(AppContext appContext) throws ValidationException, IOException {
        super();
        this.dataStore = appContext.getDataStore();
        this.builder = new BdbTermBuilder(getEC(), getVC());
    }

    protected BdbTerminologyStore getDataStore() {
        return dataStore;
    }

    protected TerminologyBuilderBI getBuilder() {
        return builder;
    }

    protected EditCoordinate getEC() throws ValidationException, IOException {
        int authorNid = TermAux.USER.getLenient().getConceptNid();
        int module = Snomed.CORE_MODULE.getLenient().getNid();
        int editPathNid = TermAux.SNOMED_CORE.getLenient().getConceptNid();

        return new EditCoordinate(authorNid, module, editPathNid);
    }

    protected ViewCoordinate getVC() throws IOException {
        return StandardViewCoordinates.getSnomedStatedLatest();
    }
}
