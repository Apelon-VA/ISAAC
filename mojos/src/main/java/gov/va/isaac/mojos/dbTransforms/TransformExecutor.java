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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.mojos.dbTransforms;

import gov.va.isaac.AppContext;
import java.io.File;
import java.nio.file.Files;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;

/**
 * Goal which executes arbitrary transforms.
 * 
 * @goal execute-transforms
 * 
 * @phase process-sources
 */
public class TransformExecutor extends AbstractMojo
{

	/**
	 * The transforms and their configuration that are to be executed
	 * 
	 * @parameter
	 * @required
	 */
	private Transform[] transforms;

	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	@Override
	public void execute() throws MojoExecutionException
	{
		StringBuilder summaryInfo = new StringBuilder();
		try
		{
			getLog().info("Executing DB Transforms");
			// ASSUMES setup has run already
			TerminologyStoreDI store = AppContext.getService(TerminologyStoreDI.class);

			for (Transform t : transforms)
			{
				long start = System.currentTimeMillis();
				//TODO rework API to allow any transforms that iterate all concepts to do so in parallel, with one iteration, instead of one per transform
				TransformI transformer = AppContext.getServiceLocator().getService(TransformI.class, t.getName());
				if (transformer == null)
				{
					throw new MojoExecutionException("Could not locate a TransformI implementation with the name '" + t.getName() + "'.");
				}
				getLog().info("Executing transform " + transformer.getDescription());
				transformer.configure(t.getConfigFile());
				transformer.transform(store);
				String summary = "Transformer " + t.getName() + " compleated:  " + transformer.getWorkResultSummary() + " in " + (System.currentTimeMillis() - start) + "ms";
				getLog().info(summary);
				summaryInfo.append(summary);
				summaryInfo.append(System.getProperty("line.separator"));
			}

			Files.write(new File("target/transformsSummary.txt").toPath(), summaryInfo.toString().getBytes());
			
			getLog().info("Finished executing transforms");
		}
		catch (Exception e)
		{
			throw new MojoExecutionException("Database transform failure", e);
		}
	}
}
