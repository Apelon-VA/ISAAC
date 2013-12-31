package gov.va.isaac;

import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;

/**
 * Encapsulates objects in application scope into one class for easy injection.
 *
 * @author ocarlsen
 */
public class AppContext {

    private final App app;
    private final BdbTerminologyStore dataStore;

    public AppContext(App app, BdbTerminologyStore dataStore) {
        super();
        this.app = app;
        this.dataStore = dataStore;
    }

    public App getApp() {
        return app;
    }

    public BdbTerminologyStore getDataStore() {
        return dataStore;
    }
}
