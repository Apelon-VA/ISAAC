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
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptFetcherBI;
import org.ihtsdo.otf.tcc.api.concept.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
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
	
	/**
	 * The folder where any summary output files should be written
	 * 
	 * @parameter
	 * @required
	 */
	private File summaryOutputFolder;

	
	
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
			
			ArrayList<TransformConceptIterateI> iterateTransforms = new ArrayList<>();
			
			// ASSUMES setup has run already
			TerminologyStoreDI store = AppContext.getService(TerminologyStoreDI.class);

			for (Transform t : transforms)
			{
				long start = System.currentTimeMillis();
				TransformI transformer = AppContext.getServiceLocator().getService(TransformI.class, t.getName());
				if (transformer == null)
				{
					throw new MojoExecutionException("Could not locate a TransformI implementation with the name '" + t.getName() + "'.");
				}
				if (transformer instanceof TransformArbitraryI)
				{
					getLog().info("Executing arbitrary transform " + transformer.getName() + " - " + transformer.getDescription());
					transformer.configure(t.getConfigFile(), store);
					
					((TransformArbitraryI)transformer).transform(store);
					String summary = "Transformer " + t.getName() + " completed:  " + transformer.getWorkResultSummary() + " in " + (System.currentTimeMillis() - start) + "ms";
					getLog().info(summary);
					summaryInfo.append(summary);
					summaryInfo.append(System.getProperty("line.separator"));
				}
				else if (transformer instanceof TransformConceptIterateI)
				{
					iterateTransforms.add((TransformConceptIterateI)transformer);
					transformer.configure(t.getConfigFile(), store);
				}
				else
				{
					throw new MojoExecutionException("Unhandled transform subtype");
				}
			}
			
			if (iterateTransforms.size() > 0)
			{
				getLog().info("Executing concept iterate transforms:");
				for (TransformConceptIterateI it : iterateTransforms)
				{
					getLog().info(it.getName() + " - " + it.getDescription());
				}
				
				AtomicInteger parallelChangeCount = new AtomicInteger();
				long start = System.currentTimeMillis();
				
				//Start a process to iterate all concepts in the DB.
				store.iterateConceptDataInParallel(new ProcessUnfetchedConceptDataBI()
				{
					@Override
					public boolean continueWork()
					{
						return true;
					}

					@Override
					public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher) throws Exception
					{
						ConceptChronicleBI cc = fetcher.fetch();
						
						for (TransformConceptIterateI it : iterateTransforms)
						{
							if (it.transform(store, cc))
							{
								int last = parallelChangeCount.getAndIncrement();
								//commit every 2000 changes (this is running in parallel)
								if (last % 2000 == 0)
								{
									store.commit();
								}
							}
						}
					}

					@Override
					public String getTitle()
					{
						return "Iterate All Concepts for Transform";
					}

					@Override
					public NativeIdSetBI getNidSet() throws IOException
					{
						return null;
					}

					@Override
					public boolean allowCancel()
					{
						return false;
					}
				});
				
				store.commit();
				getLog().info("Parallel concept iterate completed in " + (System.currentTimeMillis() - start) + "ms.");
				summaryInfo.append("Parallel concept iterate completed in " + (System.currentTimeMillis() - start) + "ms.");
				for (TransformConceptIterateI it : iterateTransforms)
				{
					String summary = "Transformer " + it.getName() + " completed:  " + it.getWorkResultSummary();
					getLog().info(summary);
					getLog().info(it.getWorkResultDocBookTable());
					summaryInfo.append(summary);
					summaryInfo.append(it.getWorkResultDocBookTable());
					summaryInfo.append(System.getProperty("line.separator"));
				}
			}
			
			Files.write(new File(summaryOutputFolder, "transformsSummary.txt").toPath(), summaryInfo.toString().getBytes());
			
			getLog().info("Finished executing transforms");
		}
		catch (Exception e)
		{
			throw new MojoExecutionException("Database transform failure", e);
		}
	}
}
