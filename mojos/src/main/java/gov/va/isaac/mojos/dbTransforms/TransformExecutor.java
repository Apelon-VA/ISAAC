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
		try
		{
			getLog().info("Executing DB Transforms");
			// ASSUMES setup has run already
			TerminologyStoreDI store = AppContext.getService(TerminologyStoreDI.class);

			for (Transform t : transforms)
			{
				TransformI transformer = AppContext.getServiceLocator().getService(TransformI.class, t.getName());
				getLog().info("Executing transform " + transformer.getDescription());
				transformer.configure(t.getConfigFile());
				transformer.transform();
			}

			getLog().info("Finished executing transforms");
		}
		catch (Exception e)
		{
			throw new MojoExecutionException("Database transform failure", e);
		}
	}
}
