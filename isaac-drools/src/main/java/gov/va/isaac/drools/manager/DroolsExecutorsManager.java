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
package gov.va.isaac.drools.manager;

import eu.infomas.annotation.ClassPathScanner;
import gov.va.isaac.interfaces.utility.ServicesToPreloadI;
import gov.va.isaac.util.Utility;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * {@link DroolsExecutorsManager}
 * 
 * Initial code came from the ExecutionManager in trek... but is had many many issues, and didn't support 
 * several use cases, so this has been quite rewritten.  
 * 
 * This looks for any .drl files on the classpath - and then writes them out to a "drools-rules" folder 
 * relative to the JVM startup location - typically, whatever folder the application is installed to.
 * 
 * Any .drl files which already exist in that folder will be overwritten by files from the classpath
 * if the names clash.
 * 
 * Rules are compiled to the folder "drools-rules/compiled-drools-rules".  
 * 
 * Any pre-compiled rules (files with a .kpkgs extension) will be loaded first.
 * 
 * Then, all .drl files are checked (including files written from the classpath) and any .drl file
 * that doesn't have a corresponding .kpkgs file is compiled.  Also, any drl file that is newer than
 * the corresponding .kpkgs file is recompiled.
 * 
 * Each individual .drl file that is found gets configured into its own DroolsExecutor instance.
 * 
 * To create a DroolsExecutor instance with multiple .drl files, see the {@link #createDroolsExecutor(String, EnumSet, File...)} 
 * method.
 *
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Singleton
public class DroolsExecutorsManager implements ServicesToPreloadI
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
		//runs in a background thread
		Utility.execute(() -> init());
	}

	private void init()
	{
		logger.info("Configuring the Drools Rules Engine");
		try
		{
			//First, load any previously compiled rules
			compiledRuleDirectory_.mkdirs();
			
			writeDRLFilesFromClassPath();
			
			
			logger.debug("Checking for compiled rules in " + compiledRuleDirectory_.getAbsolutePath());
			
			if (compiledRuleDirectory_.exists())
			{
				loadFromFolder(compiledRuleDirectory_, "");
			}
			
			logger.debug("Checking for drools rules in " + droolsRulesFolder_.getAbsolutePath());
			
			if (droolsRulesFolder_.exists())
			{
				compileFromFolder(droolsRulesFolder_, "");
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
	
	private void loadFromFolder(File folder, String relativeParentPortion)
	{
		for (File f : folder.listFiles())
		{
			if (f.isFile() && f.getName().toLowerCase().endsWith(".kpkgs"))
			{
				try
				{
					logger.info("Loading Compiled Rule " + f.getAbsolutePath());

					String name = relativeParentPortion + nameFromFile(f);
					
					DroolsExecutor de = new DroolsExecutor(name, f);
					loadedExecutors_.put(name, de);
				}
				catch (Exception e)
				{
					logger.error("Problem reading compiled rule file - skipping", e);
				}
			}
			else if (f.isDirectory())
			{
				loadFromFolder(f, relativeParentPortion + f.getName() + ".");
			}
		}
	}
	
	private void compileFromFolder(File folder, String relativeParentPortion)
	{
		for (File f : folder.listFiles())
		{
			if (f.isFile() && f.getName().toLowerCase().endsWith(".drl"))
			{
				logger.info("Checking Rule " + f.getAbsolutePath());

				String name = relativeParentPortion + nameFromFile(f);
				DroolsExecutor alreadyLoaded = loadedExecutors_.get(name);
				if (alreadyLoaded != null)
				{
					//If the drl file on disk is older than the compiled file - skip
					if (f.lastModified() <= alreadyLoaded.getCompileTime())
					{
						logger.debug("Skipping " + f + " because it is already compiled");
						continue;
					}
					else
					{
						logger.debug("Drools file " + f + " is newer than compiled package - will recompile");
					}
				}
				else
				{
					logger.info("Compiling Rule " + f);
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
			else if (f.isDirectory())
			{
				compileFromFolder(f, relativeParentPortion + f.getName() + ".");
			}
		}
	}

	private String nameFromFile(File f)
	{
		String name = sanatizeName(f.getName());
		int index = name.lastIndexOf('.'); 
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
	 * Only the compiled knowledge package is written to disk for future use - the kbFiles are not stored.
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
		String localName = sanatizeName(name);
		DroolsExecutor de = new DroolsExecutor(localName, EnumSet.allOf(ExtraEvaluators.class), kbFiles);
		loadedExecutors_.put(localName, de);
		
		File compiledTo = new File(compiledRuleDirectory_, localName + ".kpkgs");
		
		//serialize
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(compiledTo));
		out.writeObject(de.getKnowledgePackages());
		out.close();
		return de;
	}
	
	/**
	 * Get the currently loaded executor names.  Waits for init to complete, if necessary.
	 * @return
	 * @throws InterruptedException if interrupted while waiting for init to complete.
	 */
	public Set<String> getLoadedExecutors() throws InterruptedException
	{
		cdl_.await();
		return loadedExecutors_.keySet();
	}
	
	private void writeDRLFilesFromClassPath()
	{
		logger.debug("Scanning classpath for .drl files");
		try
		{
			//only copy stuff from the package "builtin" - though note - this code has a bug, and only filters 
			//packages from inside jar files it won't filter from the file-based classpath (which is ok, 99.9% of the time)
			ClassPathScanner cps = new ClassPathScanner(new String[] {"builtin"}, ".drl");
			InputStream is = cps.next();
			while (is != null)
			{
				try
				{
					String name = cps.getName();
					
					//If we read it from the classpath - it was probably in the target/classes folder - just keep the part after that.
					
					int index = name.indexOf("target/classes");
					if (index < 0)
					{
						index = name.indexOf("target\\classes");
					}
					if (index > 0)
					{
						name = name.substring(index + "target/classes".length(), name.length());
					}
					
					File output = new File(droolsRulesFolder_, name);
					output.getParentFile().mkdirs();
					
					logger.info("Found " + name + " on the classpath - writing out to drools folder");
					Files.copy(is, output.toPath(), StandardCopyOption.REPLACE_EXISTING);
					Files.setLastModifiedTime(output.toPath(), FileTime.fromMillis(cps.getModifyTime()));
					is.close();
					is = cps.next();
				}
				catch (Exception e)
				{
					logger.error("Error copying file from classpath - skipped", e);
					is = cps.next();
				}
			}
		}
		catch (Exception e)
		{
			logger.error("Unexpected error during classpath scanning", e);
		}
	}

	/**
	 * @see gov.va.isaac.interfaces.utility.ServicesToPreloadI#loadRequested()
	 */
	@Override
	public void loadRequested()
	{
		// nothing to do, we already kick off init in the constructor.
	}
	
	/**
	 * @see gov.va.isaac.interfaces.utility.ServicesToPreloadI#shutdown()
	 */
	@Override
	public void shutdown()
	{
		// noop
	}
	
	
}
