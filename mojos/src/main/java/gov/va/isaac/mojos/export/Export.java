package gov.va.isaac.mojos.export;

import gov.va.isaac.AppContext;
import gov.va.isaac.ie.exporter.EConceptExporter;
import gov.va.isaac.mojos.dbBuilder.MojoConceptSpec;
import gov.va.isaac.mojos.dbBuilder.Setup;
import gov.va.isaac.util.OTFUtility;
import gov.va.isaac.util.ProgressEvent;
import gov.va.isaac.util.ProgressListener;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;

/**
 * Exports a jBin file type (an eConcept) .
 */
@Mojo( name = "Export")
public class Export extends AbstractMojo
{
	private EConceptExporter eConExporter;
	private DataOutputStream dos_;
	
	//REQUIRED
	@Parameter (name = "outputFolder", required = true)
	private File outputFolder;
 
	@Parameter (name = "exportType", required = true)
	private ExportReleaseType[] exportType;
	
	@Parameter (name = "path", required = true)
	private MojoConceptSpec path;
	
	//OPTIONAL
	@Parameter (name="namespace")
	private String namespace;
	
	@Parameter (name="releaseDate", defaultValue = "${maven.build.timestamp}")
	private Date releaseDate;
	
	@Parameter (name = "pathFilter")
	private MojoConceptSpec[] pathFilter;
	
	@Parameter (name="skipExportAssembly", defaultValue="false")
	private boolean skipExportAssembly;

	
	@Override
	public void execute() throws MojoExecutionException 
	{
		if(skipExportAssembly)
		{
			getLog().info("Content Export Mojo will not run due to 'skipExportAssembly' being set to true.  Set skipExportAssembly=false to enable the Content Export" );
		}
		else
		{
			getLog().info("Executing Content Export" );
			String fileName = "SOLOR_Snapshot_" + path.getFsn() + "";
					
			//Check if requireed Parameters are Empty
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
				fileName = fileName + "_[" + namespace + "]";
			}
			if(releaseDate != null) {
				fileName = fileName + "_[" + releaseDate + "]";
			}
			
			try
			{
				getLog().info("Exporting the database " + fileName + " to " + outputFolder.getAbsolutePath());
				
				dos_ = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(outputFolder, fileName + ".jbin"))));
				eConExporter = new EConceptExporter(dos_);
				List<ConceptChronicleBI> allPaths = OTFUtility.getPathConcepts(); 
				
				ProgressListener pListener = new ProgressListener() {
					@Override
					public void updateProgress(ProgressEvent pe) {
						long percent = pe.getPercent();
						if((percent % 10) == 0){
							getLog().info(percent + "% complete - " + eConExporter.getCount() + " concepts exported");
						} else if(percent == 1) {
							getLog().info("Export started succesfully (1% complete)");
						}
					}
				};
				
				eConExporter.addProgressListener(pListener);
				
				boolean pathFound = false;
				for(ConceptChronicleBI thisPath : allPaths) {
					try {
						if(thisPath.getPrimordialUuid().equals(path.getConceptSpec().getPrimodialUuid())) {
							int thisNid = thisPath.getNid();
							try {
								eConExporter.export(thisNid);
							} catch (Exception e) {
								e.printStackTrace();
							}
							pathFound = true;
							break;
						}
					} catch (Exception e) {
						getLog().error("Exception Error");
						e.printStackTrace();
					}
				}
					
				if(!pathFound){
					getLog().error("Could not find a matching path!");
					//throw new MojoExecutionException("We could not find a matching path");
				}
			}
			catch (Exception e) {
				getLog().error("Error exporting DB");
				throw new MojoExecutionException("Unexpected error exporting the DB", e);
			} finally {
				
				//CLOSE DATA OUTPUT STREAM
				try {
					if(dos_ != null) {
						dos_.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				getLog().info("Done exporting the DB - exported " + eConExporter.getCount() + " concepts");
			}
		}
	}
	
	public static void main(String[] args) {
		
		Setup setup = new Setup();
		setup.setBdbFolderLocation("../../ISAAC-PA/app/solor-snomed-2015.03.06-active-only.bdb");
		setup.setUserProfileFolderLocation( new File("../../ISAAC-PA/app/profiles"));
		
		try {
			setup.execute();
		} catch (MojoExecutionException e1) {
			e1.printStackTrace();
		}
		
		Export export = new Export();
//		export.bdbFolderLocation = new File("../../ISAAC-PA/app/solor-snomed-2015.03.06-active-only.bdb");
		export.outputFolder = new File("target/output");
		export.exportType = new ExportReleaseType[]{ExportReleaseType.SNAPSHOT};
		export.skipExportAssembly = false;
//		export.userProfileLocation = new File("../../ISAAC-PA/app/profiles");
		
		MojoConceptSpec mojoConceptSpec = new MojoConceptSpec();
//		mojoConceptSpec.setFsn("ISAAC development path origin");
//		mojoConceptSpec.setUuid("83637d62-a85f-5ce5-b6b3-b9bcf6262abb");
		
		mojoConceptSpec.setFsn("ISAAC development path");
		mojoConceptSpec.setUuid("f5c0a264-15af-5b94-a964-bb912ea5634f");
		
		export.path = mojoConceptSpec;
		
		try {
			export.execute();
		} catch (MojoExecutionException e) {
			e.printStackTrace();
		}
		
		//SHUTDOWN
		AppContext.getService(TerminologyStoreDI.class).shutdown();
		
	}
	

}