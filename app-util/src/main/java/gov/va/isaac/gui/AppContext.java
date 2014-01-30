package gov.va.isaac.gui;

import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;

/**
 * Encapsulates objects in application scope into one class for easy injection.
 *
 * @author ocarlsen
 */
public class AppContext {

    private final AppUtil appUtil;
    private final BdbTerminologyStore dataStore;

    public AppContext(AppUtil appUtil, BdbTerminologyStore dataStore) {
        super();
        this.appUtil = appUtil;
        this.dataStore = dataStore;
    }

    public AppUtil getAppUtil() {
        return appUtil;
    }

    public BdbTerminologyStore getDataStore() {
        return dataStore;
    }
}
