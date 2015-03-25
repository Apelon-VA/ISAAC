package gov.va.isaac.mojos.export;

import gov.va.isaac.model.ExportType;
import gov.va.isaac.mojos.dbBuilder.MojoConceptSpec;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
//import org.apache.maven.execution.MavenSession;
//import org.apache.maven.plugin.MojoExecution;
//import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.InstantiationStrategy;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;


/**
 * Exports a jBin file type (an eConcept) .
 * @goal touch
 * @phase process-sources
 */
@Mojo( name = "Export")
public class Export extends AbstractMojo
{
	//REQUIRED
	@Parameter (name = "bdbFolderLocation", required = true)
	private File bdbFolderLocation;
	
	@Parameter (name = "outputFolder", required = true)
	private File outputFolder;
 
	@Parameter (name = "exportType", required = true)
	private ExportType[] exportType;
	
	@Parameter (name = "path", required = true)
	private MojoConceptSpec path;
	
	//OPTIONAL
	@Parameter (name="namespace")
	private String namespace;
	
	@Parameter (name="releaseDate", defaultValue = "${maven.build.timestamp}")
	private Date releaseDate;
	
	@Parameter (name = "pathFilter")
	private MojoConceptSpec[] pathFilter;

	
    public void execute() throws MojoExecutionException 
    {
    	//Check if requireed Parameters are Empty
    	// We can proably remove these
    	if(bdbFolderLocation == null) {
    		getLog().error("Missing bdbFolderLocation");
    		throw new MojoExecutionException("Missing bdbFolderLocation");
    	} else {
    		getLog().info("bdbFolderLocation: " + bdbFolderLocation);
    	}
    	if(outputFolder == null) {
    		getLog().error("missing outputFolder");
    		throw new MojoExecutionException("Missing outputFolder");
    	} else {
    		getLog().info("outputFolder: " + outputFolder);
    	}
    	if(exportType.length < 1) {
    		getLog().error("exportType array parameter is emnpty");
    		throw new MojoExecutionException("Missing exportType");
    	} else {
    		getLog().info("exportType: " + exportType);
    	}
    	if(path == null) {
    		getLog().error("Missing path");
    		throw new MojoExecutionException("Missing path");
    	} else {
    		getLog().info("path: " + path);
    	}
    	
    	
    	// [ARTF234122] Goes down here
    	
    	
    }
    
}