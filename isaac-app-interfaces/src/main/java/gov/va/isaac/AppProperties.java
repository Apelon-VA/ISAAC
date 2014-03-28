package gov.va.isaac;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Functionality to provide properties to the ISAAC application and its modules.
 * Reads properties from a file called "app.properties",
 * which is typically in a project-specific "config" project.
 */
public class AppProperties {

    private final Properties properties;

    public AppProperties() throws IOException {
        super();
        this.properties = new Properties();

        // Load from file in classpath.
        InputStream in = getClass().getResourceAsStream("/app.properties");
        properties.load(in);
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}

