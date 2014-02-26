package gov.va.isaac.models.util;

import java.io.IOException;

import org.ihtsdo.otf.tcc.api.spec.ValidationException;

/**
 * Base class containing common methods for exporting information models.
 *
 * @author ocarlsen
 */
public class ExporterBase extends CommonBase {

    protected ExporterBase() throws ValidationException, IOException {
        super();
    }
}
