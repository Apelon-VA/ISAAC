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
package gov.va.isaac.mojos.dbBuilder;

import gov.va.isaac.AppContext;
import gov.va.isaac.config.users.GenerateUsers;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.otf.tcc.api.concept.ConceptFetcherBI;
import org.ihtsdo.otf.tcc.api.concept.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.ihtsdo.otf.tcc.dto.TtkConceptChronicle;

/**
 * Goal which processes a users.xml file which is formatted according to the UserGenerationSchema.xsd
 * stored in otf-util.  See {@link GenerateUsers} for more details.
 * 
 * @goal export-to-econcept
 * 
 * @phase process-sources
 */
public class ExportDatabaseToEConceptMojo extends AbstractMojo implements ProcessUnfetchedConceptDataBI
{

	/**
	 * The filename to use for the output.  Typically, this file would 
	 * end with an extension of .jbin
	 * 
	 * @parameter
	 * @required
	 */
	File outputFile = null;
	
	private int conCount = 0;
	private DataOutputStream dos_;

	/**
	 * To execute this mojo, you need to first have run the "Setup" mojo against
	 * the same database. Here, we assume the data store is ready to go and we can
	 * acquire it simply as shown in the createPath method below.
	 * 
	 * If not yet initialized, this will fail.
	 */
	@Override
	public void execute() throws MojoExecutionException
	{
		try
		{
			getLog().info("Exporting the database to " + outputFile.getAbsolutePath());
			TerminologyStoreDI dataStore = AppContext.getService(TerminologyStoreDI.class);
			dos_ = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
			
			dataStore.iterateConceptDataInSequence(this);
			dos_.close();
			
			getLog().info("Done exporting the DB - exported " + conCount + " concepts");
		}
		catch (Exception e)
		{
			throw new MojoExecutionException("Unexpected error exporting the DB", e);
		}
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.ContinuationTrackerBI#continueWork()
	 */
	@Override
	public boolean continueWork()
	{
		return true;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.concept.ProcessUnfetchedConceptDataBI#allowCancel()
	 */
	@Override
	public boolean allowCancel()
	{
		return false;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.concept.ProcessUnfetchedConceptDataBI#processUnfetchedConceptData(int, org.ihtsdo.otf.tcc.api.concept.ConceptFetcherBI)
	 */
	@Override
	public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher) throws Exception
	{
		new TtkConceptChronicle(fetcher.fetch()).writeExternal(dos_);
		conCount++;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.concept.ProcessUnfetchedConceptDataBI#getNidSet()
	 */
	@Override
	public NativeIdSetBI getNidSet() throws IOException
	{
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.concept.ProcessUnfetchedConceptDataBI#getTitle()
	 */
	@Override
	public String getTitle()
	{
		return "Exporter";
	}	
}
