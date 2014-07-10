package gov.va.isaac.gui.enhancedsearchview;

import gov.va.isaac.AppContext;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import javax.inject.Singleton;

import org.ihtsdo.otf.query.lucene.LuceneIndexer;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;
import org.jvnet.hk2.annotations.Service;

/**
 * EnhancedSearchViewController
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
@Service
@Singleton
public class EnhancedSearchViewRunner extends Application {
	final private EnhancedSearchView view;

    private Stage primaryStage;
    private Region rootLayout;
    
	public EnhancedSearchViewRunner() throws IOException {
		view = new EnhancedSearchView();
	}

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("EnhancedSearchView");

        initRootLayout();
    }

    /**
     * Initializes the root layout.
     */
    public void initRootLayout() {
    	// Load root layout from fxml file.
    	rootLayout = view.getView();

    	// Show the scene containing the root layout
    	primaryStage.setScene(new Scene(rootLayout));
    	primaryStage.show();
    }

    public static void main(String[] args) throws ClassNotFoundException, IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        AppContext.setup();
        
		// TODO OTF fix: this needs to be fixed so I don't have to hack it with reflection.... https://jira.ihtsdotools.org/browse/OTFISSUE-11
        Field f = Hk2Looker.class.getDeclaredField("looker");
		f.setAccessible(true);
		f.set(null, AppContext.getServiceLocator());

		System.setProperty(BdbTerminologyStore.BDB_LOCATION_PROPERTY, new File("../../ISAAC-PA/app/berkeley-db").getCanonicalPath());
		System.setProperty(LuceneIndexer.LUCENE_ROOT_LOCATION_PROPERTY, new File("../../ISAAC-PA/app/berkeley-db").getCanonicalPath());

        launch(args);
    }
}