package gov.va.isaac.models.util;

import java.io.IOException;

import org.ihtsdo.otf.tcc.api.blueprint.TerminologyBuilderBI;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.datastore.BdbTermBuilder;

/**
 * Base class containing common methods for importing information models.
 *
 * @author ocarlsen
 */
public class ImporterBase extends CommonBase {

    private final TerminologyBuilderBI builder;

    protected ImporterBase() throws ValidationException, IOException {
        super();

        this.builder = new BdbTermBuilder(getEC(), getVC());
    }

    protected final TerminologyBuilderBI getBuilder() {
        return builder;
    }
}
