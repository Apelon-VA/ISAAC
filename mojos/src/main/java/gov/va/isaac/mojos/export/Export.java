package gov.va.isaac.mojos.export;

import gov.va.isaac.AppContext;
import gov.va.isaac.ie.exporter.EConceptExporter;
import gov.va.isaac.model.ExportType;
import gov.va.isaac.mojos.dbBuilder.MojoConceptSpec;
import gov.va.isaac.mojos.dbBuilder.Setup;
import gov.va.isaac.util.OTFUtility;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptFetcherBI;
import org.ihtsdo.otf.tcc.api.concept.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;


/**
 * Exports a jBin file type (an eConcept) .
 * @goal touch
 * @phase process-sources
 */
@Mojo( name = "Export")
public class Export extends AbstractMojo
{
	
	public enum ExportType {
		SNAPSHOT,
		DELTA,
		FULL
	}
	
	private int conCount = 0;
	private DataOutputStream dos_;
	
	//REQUIRED
	@Parameter (name = "bdbFolderLocation", required = true)
	private File bdbFolderLocation;
	
	@Parameter (name = "outputFolder", required = true)
	private File outputFolder;
 
	@Parameter (name = "exportType", required = true)
	private ExportType[] exportType;
	
	@Parameter (name = "path", required = true)
	private MojoConceptSpec path;
	
	@Parameter(name = "userProfileLocation", required = true)
	private File userProfileLocation;
	
	//OPTIONAL
	@Parameter (name="namespace")
	private String namespace;
	
	@Parameter (name="releaseDate", defaultValue = "${maven.build.timestamp}")
	private Date releaseDate;
	
	@Parameter (name = "pathFilter")
	private MojoConceptSpec[] pathFilter;

	
	public void execute() throws MojoExecutionException 
	{
		String fileName = "EXPORT";
				
		//Check if requireed Parameters are Empty
		// We can proably remove these
		if(bdbFolderLocation != null && bdbFolderLocation.exists()) {
			getLog().info("We found the bdbFolderLocation succesfully at " + bdbFolderLocation.getAbsolutePath());
		} else {
			getLog().error("Missing bdbFolderLocation");
			throw new MojoExecutionException("Missing bdbFolderLocation");
		}
		if(outputFolder != null) {
			if(outputFolder.exists()) {
				getLog().info("Output directory exists: " + outputFolder.getAbsolutePath());
			} else {
				outputFolder.mkdir();
				getLog().info("Output directory created: " + outputFolder.getAbsolutePath());
			}
			
		} else {
			getLog().error("Missing outputFolder parameter");
			throw new MojoExecutionException("Missing outputFolder");
		}
		
		if(exportType.length < 1) {
			getLog().error("exportType array parameter is emnpty");
			throw new MojoExecutionException("Missing exportType");
		} else {
			getLog().info("exportType: " + exportType);
		}
		
		
		if(namespace != null) {
			fileName = fileName + "-" + namespace;
		}
		if(releaseDate != null) {
			fileName = fileName + "-" + releaseDate;
		}
		
		try
		{
			//STARTUP
			Setup setup = new Setup();
			setup.setBdbFolderLocation(bdbFolderLocation.getAbsolutePath());
			setup.setUserProfileFolderLocation(userProfileLocation);
			setup.execute();
			
			getLog().info("Exporting the database " + fileName + " to " + outputFolder.getAbsolutePath());
			
			dos_ = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileName + ".jbin")));
			
			EConceptExporter eConExporter = new EConceptExporter(dos_);
			
			List<ConceptChronicleBI> allPaths = OTFUtility.getPathConcepts(); 
			
			boolean pathFound = false;
			for(ConceptChronicleBI thisPath : allPaths) {
				if(thisPath.getPrimordialUuid().equals(path.getConceptSpec().getPrimodialUuid())) {
					eConExporter.export(thisPath.getNid());
					pathFound = true;
					break;
				}
			}
			
			if(!pathFound){
				getLog().error("Could not find a matching path!");
				throw new MojoExecutionException("We could not find a matching path");
			}
			
			//SHUTDOWN
			TerminologyStoreDI store = AppContext.getService(TerminologyStoreDI.class);
			getLog().info("  Shutting Down");
			store.shutdown();
			
			dos_.close();
			
			getLog().info("Done exporting the DB - exported " + conCount + " concepts");
		}
		catch (Exception e)
		{
			throw new MojoExecutionException("Unexpected error exporting the DB", e);
		}
	}
	
	public static void main(String[] args) {
		
		Export export = new Export();
		export.bdbFolderLocation = new File("../../ISAAC-PA/app/solor-snomed-2015.03.06-active-only.bdb");
		export.outputFolder = new File("target/output");
		export.exportType[0]= ExportType.valueOf("snapshot");
		export.userProfileLocation = new File("../../ISAAC-PA/app/profiles");
		
		try {
	        export.execute();
        } catch (MojoExecutionException e) {
	        e.printStackTrace();
        }
		
		
	}
	

}