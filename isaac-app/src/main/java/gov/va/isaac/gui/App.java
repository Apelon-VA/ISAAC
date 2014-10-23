/**
 * Copyright Notice
 * 
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.gui;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.gui.dialog.CommonDialogs;
import gov.va.isaac.gui.download.DownloadDialog;
import gov.va.isaac.interfaces.gui.ApplicationWindowI;
import gov.va.isaac.interfaces.gui.views.DockedViewI;
import gov.va.isaac.util.DBLocator;
import gov.va.isaac.util.Utility;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.function.Consumer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.ihtsdo.otf.query.lucene.LuceneIndexer;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * ISAAC {@link Application} class.
 *
 * @author ocarlsen
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class App extends Application implements ApplicationWindowI{

    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    private AppController controller;
    private boolean shutdown = false;
    private Stage primaryStage_;
    private CommonDialogs commonDialog_;
    private static IOException dataStoreLocationInitException_ = null;

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage_ = primaryStage;
        ServiceLocatorUtilities.addOneConstant(AppContext.getServiceLocator(), this);
        //Set up the CommonDialogs class (which needs a references to primaryStage_ and gets it via injection)
        commonDialog_ = AppContext.getServiceLocator().getService(CommonDialogs.class);

        this.controller = new AppController();

        primaryStage.getIcons().add(new Image("/icons/application-icon.png"));
        String title = AppContext.getAppConfiguration().getApplicationTitle();
        primaryStage.setTitle(title);
        primaryStage.setScene(new Scene(controller.getRoot()));
        primaryStage.getScene().getStylesheets().add(App.class.getResource("/isaac-shared-styles.css").toString());
        primaryStage.getScene().getStylesheets().add(App.class.getResource("App.css").toString());

        // Set minimum dimensions.
        primaryStage.setMinHeight(400);
        primaryStage.setMinWidth(400);
        
        primaryStage.setHeight(768);
        primaryStage.setWidth(1024);

        // Handle window close event.
        primaryStage.setOnHiding(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent event) {
                shutdown();
            }
        });

        primaryStage.show();

        // Reduce size to fit in user's screen.
        // (Need to do after stage is shown, because otherwise
        // the primary stage width & height are NaN.)
        Screen screen = Screen.getPrimary();
        double screenW = screen.getVisualBounds().getWidth();
        double screenH = screen.getVisualBounds().getHeight();
        if (primaryStage.getWidth() > screenW) {
            LOG.debug("Resizing width to " + screenW);
            primaryStage.setWidth(screenW);
        }
        if (primaryStage.getHeight() > screenH) {
            LOG.debug("Resizing height to " + screenH);
            primaryStage.setHeight(screenH);
        }

        if (dataStoreLocationInitException_ == null)
        {
            // Kick off a thread to open the DB connection.
            loadDataStore();
        }
        else
        {
            new DownloadDialog(AppContext.getMainApplicationWindow().getPrimaryStage(), new Consumer<Boolean>()
            {
                @Override
                public void accept(Boolean t)
                {
                    if (t)
                    {
                        dataStoreLocationInitException_ = null;
                        try
                        {
                            configDataStorePaths(new File("berkeley-db"));
                        }
                        catch (IOException e)
                        {
                            //this should be impossible
                            LOG.error("Failed to find DB after download?", e);
                            // Close app since no DB to load.
                            // (The #shutdown method will be also invoked by
                            // the handler we hooked up with Stage#setOnHiding.)
                            primaryStage_.hide();
                        }
                        loadDataStore();
                    }
                    else
                    {
                        // Close app since no DB to load.
                        // (The #shutdown method will be also invoked by
                        // the handler we hooked up with Stage#setOnHiding.)
                        primaryStage_.hide();
                    }
                    
                }
            });
        }
    }

    private void loadDataStore() {

        // Do work in background.
        Task<BdbTerminologyStore> task = new Task<BdbTerminologyStore>() {

            @Override
            protected BdbTerminologyStore call() throws Exception {
                LOG.info("Opening Workbench database");
                BdbTerminologyStore dataStore = AppContext.getServiceLocator().getService(BdbTerminologyStore.class);
                LOG.info("Finished opening Workbench database");

                // Check if user shut down early.
                if (shutdown) {
                    dataStore.shutdown();
                    return null;
                }

                return dataStore;
            }

            @Override
            protected void succeeded() {
                controller.finishInit();
            }

            @Override
            protected void failed() {
                Throwable ex = getException();

                // Display helpful dialog to users.
                String title = "Unexpected error connecting to workbench database";
                String msg = ex.getClass().getName();
                String details = ex.getMessage();
                LOG.error(title, ex);
                commonDialog_.showErrorDialog(title, msg, details);
            }
        };

        Thread t = new Thread(task, "SCT_DB_Open");
        t.setDaemon(true);
        t.start();
    }

    private static void configDataStorePaths(File bdbFolder) throws IOException {

        // Default value if null.
        if (bdbFolder == null) {
            //do nothing... let the store use its own default
            //TODO OTF fix: note - the BDB defaults are not in sync with the Lucene defaults...  lucene will init in the wrong place and fail....
            //https://jira.ihtsdotools.org/browse/OTFISSUE-12
        }
        else
        {
            File localBDBLocation = DBLocator.findDBFolder(bdbFolder);
            File localLuceneLocation = DBLocator.findLuceneIndexFolder(localBDBLocation);
            
            if (!localBDBLocation.exists())
            {
                throw new FileNotFoundException("Couldn't find a bdb data store in '" + localBDBLocation.getAbsoluteFile().getParentFile().getAbsolutePath() + "'");
            }
            if (!localBDBLocation.isDirectory())
            {
                throw new IOException("The specified bdb data store: '" + localBDBLocation.getAbsolutePath() + "' is not a folder");
            }
            
            if (System.getProperty(BdbTerminologyStore.BDB_LOCATION_PROPERTY) == null)
            {
                System.setProperty(BdbTerminologyStore.BDB_LOCATION_PROPERTY, localBDBLocation.getCanonicalPath());
            }
            else
            {
                LOG.warn("The application specified '" + localBDBLocation.getCanonicalPath() + "' but the system property " + BdbTerminologyStore.BDB_LOCATION_PROPERTY 
                        + "is set to " + System.getProperty(BdbTerminologyStore.BDB_LOCATION_PROPERTY + " this will override the application path"));
            }
            if (System.getProperty(LuceneIndexer.LUCENE_ROOT_LOCATION_PROPERTY) == null)
            {
                System.setProperty(LuceneIndexer.LUCENE_ROOT_LOCATION_PROPERTY, localLuceneLocation.getCanonicalPath());
            }
            else
            {
                LOG.warn("The application specified '" + localLuceneLocation.getCanonicalPath() + "' but the system property " + LuceneIndexer.LUCENE_ROOT_LOCATION_PROPERTY 
                        + "is set to " + System.getProperty(LuceneIndexer.LUCENE_ROOT_LOCATION_PROPERTY) + " this will override the application path");
            }
            
        }
    }

    protected void shutdown() {
        LOG.info("Shutting down");
        shutdown = true;
        if (primaryStage_.isShowing())
        {
            primaryStage_.hide();
        }
        try {
            Utility.shutdownThreadPools();
            //TODO OTF fix note - the current BDB access model gives me no way to know if I should call shutdown, as I don't know if it was started.
            //If it wasn't started, calling shutdown tries to start the DB, because it inits in the constructor call.  https://jira.ihtsdotools.org/browse/OTFISSUE-13
            //don't bother with shutdown if we know the path was bad... other wise, this actually tries to init the DB.  Sigh.
            if (dataStoreLocationInitException_ == null)
            {
                ExtendedAppContext.getDataStore().shutdown();
            }
            controller.shutdown();
        } catch (Exception ex) {
            String message = "Trouble shutting down";
            LOG.warn(message, ex);
            commonDialog_.showErrorDialog("Oops!", message, ex.getMessage());
        }
        LOG.info("Finished shutting down");
        Platform.exit();
    }
    
    /**
     * @see gov.va.isaac.interfaces.gui.ApplicationWindowI#getPrimaryStage()
     */
    @Override
    public Stage getPrimaryStage()
    {
        return primaryStage_;
    }
    
    /**
     * @see gov.va.isaac.interfaces.gui.ApplicationWindowI#ensureDockedViewIsVisble(gov.va.isaac.interfaces.gui.views.DockedViewI)
     */
    @Override
    public void ensureDockedViewIsVisble(DockedViewI view)
    {
        controller.ensureDockedViewIsVisible(view);
    }

    /**
     * @see gov.va.isaac.interfaces.gui.ApplicationWindowI#browseURL(java.lang.String)
     */
    @Override
    public void browseURL(String url)
    {
        this.getHostServices().showDocument(url);
    }

    public static void main(String[] args) throws Exception {
        //Configure Java logging into logback
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        AppContext.setup();
        // TODO OTF fix: this needs to be fixed so I don't have to hack it with reflection.... https://jira.ihtsdotools.org/browse/OTFISSUE-11
        Field f = Hk2Looker.class.getDeclaredField("looker");
        f.setAccessible(true);
        f.set(null, AppContext.getServiceLocator());
        //This has to be done _very_ early, otherwise, any code that hits it via H2K will kick off the init process, on the wrong path
        //Which is made worse by the fact that the defaults in OTF are inconsistent between BDB and lucene...
        
        try
        {
            if (System.getProperty(BdbTerminologyStore.BDB_LOCATION_PROPERTY) == null)
            {
                configDataStorePaths(new File(BdbTerminologyStore.DEFAULT_BDB_LOCATION));
            } else {
                configDataStorePaths(new File(System.getProperty(BdbTerminologyStore.BDB_LOCATION_PROPERTY)));
            }

        }
        catch (IOException e)
        {
            System.err.println("Configuration of datastore path failed.  DB will not be able to start properly!  " + e);
            dataStoreLocationInitException_ = e;
        }
        Application.launch(args);
    }
}
