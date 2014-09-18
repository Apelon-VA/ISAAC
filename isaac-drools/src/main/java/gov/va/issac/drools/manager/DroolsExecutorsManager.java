/**
 * Copyright 2011 International Health Terminology Standards Development Organisation.
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
package gov.va.issac.drools.manager;

import gov.va.isaac.util.Utility;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * {@link DroolsExecutorsManager}
 *
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Singleton
public class DroolsExecutorsManager
{
	private final File droolsRulesFolder_ = new File("drools-rules");
	private final File compiledRuleDirectory_ = new File(droolsRulesFolder_, "compiled-drools-rules");
	
	private Logger logger = LoggerFactory.getLogger(DroolsExecutorsManager.class);
	
	private HashMap<String, DroolsExecutor> loadedExecutors_ = new HashMap<>();
	
	private CountDownLatch cdl_ = new CountDownLatch(1);
	
	public static enum ExtraEvaluators
	{
		IS_KIND_OF, SAFISFIES_CONSTRAINT, IS_MEMBER_OF, IS_PARENT_MEMBER_OF, IS_MISSING_DESC_FOR, IS_GB_MEMBER_TYPE_OF, IS_US_MEMBER_TYPE_OF, IS_SYNONYM_MEMBER_TYPE_OF,
		IS_MEMBER_OF_WITH_TYPE;
	}

	private DroolsExecutorsManager()
	{
		//For HK2 to call
		Utility.execute(() -> init());
	}

	private void init()
	{
		logger.info("Configuring the Drools Rules Engine");
		try
		{
			//First, load any previously compiled rules
			compiledRuleDirectory_.mkdirs();
			
			logger.debug("Checking for compiled rules in " + compiledRuleDirectory_.getAbsolutePath());
			
			if (compiledRuleDirectory_.exists())
			{
				for (File f : compiledRuleDirectory_.listFiles())
				{
					if (f.isFile() && f.getName().toLowerCase().endsWith(".kpkgs"))
					{
						try
						{
							logger.info("Loading Compiled Rule " + f.getAbsolutePath());
	
							String name = nameFromFile(f);
							
							DroolsExecutor de = new DroolsExecutor(name, f);
							loadedExecutors_.put(name, de);
						}
						catch (Exception e)
						{
							logger.error("Problem reading compiled rule file - skipping", e);
						}
					}
				}
			}
			
			logger.debug("Checking for drools rules in " + droolsRulesFolder_.getAbsolutePath());
			
			if (droolsRulesFolder_.exists())
			{
				for (File f : droolsRulesFolder_.listFiles())
				{
					if (f.isFile() && f.getName().toLowerCase().endsWith(".drl"))
					{
						logger.info("Loading Rule " + f.getAbsolutePath());
	
						String name = nameFromFile(f);
						DroolsExecutor alreadyLoaded = loadedExecutors_.get(name);
						if (alreadyLoaded != null)
						{
							//If the drl file on disk is older than the compiled file - skip
							if (f.lastModified() <= alreadyLoaded.getCompileTime())
							{
								logger.debug("Skipping " + f + " because it is already compiled");
								continue;
							}
						}
						
						try
						{
							//otherwise - compile
							DroolsExecutor de = new DroolsExecutor(name, EnumSet.allOf(ExtraEvaluators.class), f);
							loadedExecutors_.put(name, de);
							
							File compiledTo = new File(compiledRuleDirectory_, name + ".kpkgs");
							
							//serialize
							ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(compiledTo));
							out.writeObject(de.getKnowledgePackages());
							out.close();
						}
						catch (Exception e)
						{
							logger.error("Problem compiling drools file " + f.getAbsolutePath(), e);
						}
					}
				}
			}
			else
			{
				logger.info("No drools rules folder was found at " + droolsRulesFolder_.getAbsolutePath());
			}
		}
		finally
		{
			cdl_.countDown();
		}
	}

	private String nameFromFile(File f)
	{
		String name = sanatizeName(f.getName());
		int index = name.indexOf('.'); 
		if (index > 0)
		{
			name = name.substring(0, index);
		}
		return name;
	}

	private String sanatizeName(String name)
	{
		if (name.contains(":"))
		{
			name = name.replace(':', '.');
		}
		if (name.contains("/"))
		{
			name = name.replace('/', '.');
		}
		if (name.contains("*"))
		{
			name = name.replace('*', '.');
		}
		if (name.contains("\\"))
		{
			name = name.replace('\\', '.');
		}
		return name;
	}
	
	/**
	 * Get the named drools executor.  Blocks until the initial init (which runs in a background thread) completes.
	 * 
	 * @return the executor, or null, if no executor is available with this name.
	 *
	 * @throws InterruptedException - if interrupted while waiting for the initial init.
	 */
	public DroolsExecutor getDroolsExecutor(String name) throws InterruptedException
	{
		cdl_.await();
		return loadedExecutors_.get(sanatizeName(name));
	}
	
	/**
	 * Create, cache and serialize a new executor with the specified name, evaulatorsList, and drools files.
	 * 
	 * If an executor already exists with the same name, it will be overwritten.
	 * 
	 * @param name
	 * @param extraEvaluators
	 * @param kbFiles
	 * @return The newly created executor
	 * @throws RuntimeException
	 * @throws IOException
	 */
	public DroolsExecutor createDroolsExecutor(String name, EnumSet<ExtraEvaluators> extraEvaluators, File... kbFiles) throws RuntimeException, IOException
	{
		DroolsExecutor de = new DroolsExecutor(name, EnumSet.allOf(ExtraEvaluators.class), kbFiles);
		loadedExecutors_.put(name, de);
		
		File compiledTo = new File(compiledRuleDirectory_, name + ".kpkgs");
		
		//serialize
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(compiledTo));
		out.writeObject(de.getKnowledgePackages());
		out.close();
		return de;
	}
}
