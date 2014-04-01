package gov.va.isaac;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Functionality to provide properties to the ISAAC application and its modules.
 * Reads properties from a file called "app.properties",
 * which is typically in a project-specific "config" project.
 */
public class AppProperties {

    private static final Logger LOG = LoggerFactory.getLogger(AppProperties.class);
    private static final String PROPERTIES_FILE = "/app.properties";

    private final Properties properties;

    public AppProperties() {
        super();
        this.properties = new Properties();

        // Load from file in classpath.
        try {
            InputStream in = getClass().getResourceAsStream(PROPERTIES_FILE);
            if (in == null) {
                throw new FileNotFoundException(PROPERTIES_FILE);
            }
            properties.load(in);
        } catch (IOException ex) {
            LOG.warn("Unable to load app properties.", ex);
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
}

