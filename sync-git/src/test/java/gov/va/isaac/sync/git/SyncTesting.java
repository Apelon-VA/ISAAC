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
package gov.va.isaac.sync.git;

import gov.va.isaac.AppContext;
import gov.va.isaac.interfaces.sync.MergeFailOption;
import gov.va.isaac.interfaces.sync.ProfileSyncI;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * {@link SyncTesting}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class SyncTesting
{
	public static void main(String[] args) throws Exception
	{
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		AppContext.setup();
		ProfileSyncI ssg = AppContext.getService(SyncServiceGIT.class);
		File localFolder = new File("/mnt/SSD/scratch/gitTesting");
		
		String username = "username";
		String password = "password";

		ssg.linkAndFetchFromRemote(localFolder, new URL("https://github.com/" + username + "/test.git"), username, password);
		ssg.addUntrackedFiles(localFolder);
		System.out.println("UpdateCommitAndPush result: " + ssg.updateCommitAndPush(localFolder, "mergetest2", username, password, MergeFailOption.FAIL, (String[])null));

		ssg.removeFiles(localFolder, "b");

		System.out.println("Update from remote result: " + ssg.updateFromRemote(localFolder, username, password, MergeFailOption.FAIL));

		HashMap<String, MergeFailOption> resolutions = new HashMap<>();
		resolutions.put("d", MergeFailOption.KEEP_LOCAL);
		System.out.println("resolve merge failures result: " + ssg.resolveMergeFailures(localFolder, resolutions));
		System.out.println("UpdateCommitAndPush result: " + ssg.updateCommitAndPush(localFolder, "mergetest2", username, password, MergeFailOption.FAIL, (String[])null));
	}
}
