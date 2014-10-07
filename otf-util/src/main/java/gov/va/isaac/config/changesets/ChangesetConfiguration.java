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
package gov.va.isaac.config.changesets;

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.util.Utility;
import java.io.File;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.otf.tcc.api.changeset.ChangeSetGeneratorBI;
import org.ihtsdo.otf.tcc.model.cs.ChangeSetLogWriter;
import org.ihtsdo.otf.tcc.model.cs.ChangeSetWriterHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ChangesetConfiguration}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class ChangesetConfiguration
{
	private static Logger logger = LoggerFactory.getLogger(ChangesetConfiguration.class);
	
	/**
	 * Typically called immediately after a user logs in, in order to configure the changeset writers for the user.
	 * Also, horribly slow - so it runs in a background thread.
	 */
	public static void configureChangeSetWriter()
	{
		Utility.execute(() ->
		{
			String userName = ExtendedAppContext.getCurrentlyLoggedInUser().getUserLogonName();
			logger.info("Configuring changset writer for {}", userName);
			File changeSetRoot = new File(new File(new File("profiles"), userName), "changesets");
			
			changeSetRoot.mkdirs();
			
			if (!changeSetRoot.exists() || !changeSetRoot.isDirectory())
			{
				throw new RuntimeException("Could not create the change set root directory " + changeSetRoot.getAbsolutePath());
			}
			
			int fileIncrement = -1;
			
			for (File f : changeSetRoot.listFiles())
			{
				if (f.isFile() && f.getName().endsWith(".eccs"))
				{
					String[] temp = f.getName().split("#");
					if (temp.length >= 2)
					{
						try 
						{
							int i = Integer.parseInt(temp[1]);
							if (i > fileIncrement)
							{
								fileIncrement = i;
							}
						}
						catch (NumberFormatException e)
						{
							//noop
						}
					}
				}
			}
			fileIncrement++;
			
			String changeSetFileName = userName + '#' + fileIncrement + "#" + UUID.randomUUID() + ".eccs";
			File changeSetFile = new File(changeSetRoot, changeSetFileName);
			
			logger.info("ChangeSet writer will be configured for " + changeSetFile.getAbsolutePath());
			
			ChangeSetGeneratorBI csEccs = ExtendedAppContext.getDataStore().createDtoChangeSetGenerator(
					changeSetFile, new File(changeSetRoot, "." + changeSetFileName), ChangeSetGenerationPolicy.COMPREHENSIVE);  //TODO what should this be?
			ChangeSetWriterHandler.addWriter(userName + ".eccs", csEccs);
			
			ChangeSetLogWriter cslw = new ChangeSetLogWriter(new File(changeSetRoot, "commitLog.tsv"), new File(changeSetRoot, "." + "commitLog.xls"));
			ChangeSetWriterHandler.addWriter(userName + ".commitLog.tsv", cslw);
			logger.info("Finished configuring changset writer for {}", userName);
		});
	}
}
