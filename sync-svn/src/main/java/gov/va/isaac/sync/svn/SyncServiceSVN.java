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
package gov.va.isaac.sync.svn;

import gov.va.isaac.interfaces.sync.MergeFailOption;
import gov.va.isaac.interfaces.sync.MergeFailure;
import gov.va.isaac.interfaces.sync.ProfileSyncI;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.naming.AuthenticationException;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNPropertyValue;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.util.SVNEncodingUtil;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;
import org.tmatesoft.svn.core.wc.ISVNStatusHandler;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNConflictChoice;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.SVNEventAction;
import org.tmatesoft.svn.core.wc.SVNPropertyData;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusType;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCClient;

/**
 * {@link SyncServiceGIT}
 * 
 * A GIT implementation of {@link ProfileSyncI}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service(name = "SVN")
@PerLookup
public class SyncServiceSVN implements ProfileSyncI
{
	private static Logger log = LoggerFactory.getLogger(SyncServiceSVN.class);

	private final String eol = System.getProperty("line.separator");

	private File localFolder_ = null;
	private SVNClientManager scm_;

	//TODO figure out how we handle prompts for things.  GUI vs no GUI... etc.
	public SyncServiceSVN(File localFolder)
	{
		this();
		setRootLocation(localFolder);
	}

	private SyncServiceSVN()
	{
		//For HK2
	}

	/**
	 * @see gov.va.isaac.interfaces.sync.ProfileSyncI#setRootLocation(java.io.File)
	 */
	@Override
	public void setRootLocation(File localFolder) throws IllegalArgumentException
	{
		if (localFolder == null)
		{
			throw new IllegalArgumentException("The localFolder is required");
		}
		if (!localFolder.isDirectory())
		{
			log.error("The passed in local folder '{}' didn't exist", localFolder);
			throw new IllegalArgumentException("The localFolder must be a folder, and must exist");
		}
		this.localFolder_ = localFolder;
	}

	/**
	 * @see gov.va.isaac.interfaces.sync.ProfileSyncI#getRootLocation()
	 */
	@Override
	public File getRootLocation()
	{
		return this.localFolder_;
	}

	/**
	 * @throws AuthenticationException 
	 * @see gov.va.isaac.interfaces.sync.ProfileSyncI#linkAndFetchFromRemote(java.io.File, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void linkAndFetchFromRemote(String remoteAddress, String userName, String password) throws IllegalArgumentException, IOException, AuthenticationException
	{
		log.info("linkAndFetchFromRemote called - folder: {}, remoteAddress: {}, username: {}", localFolder_, remoteAddress, userName);
		try
		{
			File svnFolder = new File(localFolder_, ".svn");

			
			if (svnFolder.isDirectory())
			{
				log.debug("Root folder already contains a .svn folder.  Checking to see if it can be relinked to remote.");
				
				String id = getSvn().getWCClient().doInfo(SVNURL.parseURIEncoded(SVNEncodingUtil.autoURIEncode(remoteAddress)), SVNRevision.HEAD, SVNRevision.HEAD)
						.getRepositoryUUID();
				
				log.debug("Remote repository ID: {}", id);
				
				String localId = getSvn().getStatusClient().doStatus(localFolder_, false).getRepositoryUUID();
				log.debug("Local repository ID: {}", localId);
				
				if (id.equals(localId))
				{
					log.info("Ok to re-link local to remote - they have the same ID");
					relinkRemote(remoteAddress);
					
					//But, if we have any pre-existing merge conflicts.....
					Set<String> conflicts = getFilesInMergeConflict();
					if (conflicts.size() > 0)
					{
						log.info("Local repository was left in a conflicted state.  Will discared local repository information, and check out clean (preserving local files)");
						
						for (String s : conflicts)
						{
							File mine = new File(localFolder_, s + ".mine");
							File conflicted = new File(localFolder_, s);
							
							if (mine.isFile() && conflicted.isFile())
							{
								log.info("Deleting pre-merged conflicted file " + conflicted.getAbsolutePath() + " replacing with " + mine.getAbsolutePath());
								if (conflicted.delete())
								{
									mine.renameTo(conflicted);
								}
							}
							else
							{
								log.error("Unresolved conflict being left - user will have to manually resolve before next commit!");
							}
						}
						
						deleteRecursive(svnFolder.toPath());
					}
				}
				else
				{
					log.info("Local and remote are not the same repository.  Deleting local repository information - will check out clean (preserving local files).");
					deleteRecursive(svnFolder.toPath());
				}
			}
			
			//we might have deleted it above, so this isn't a simple else case.
			if (!svnFolder.isDirectory())
			{
				log.debug("Root folder does not contain a .svn subfolder.  Will checkout from remote.");
				long rev = getSvn(true).getUpdateClient().doCheckout(SVNURL.parseURIEncoded(SVNEncodingUtil.autoURIEncode(remoteAddress)),
						localFolder_, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY, true);
				log.info("Checkout out revision " + rev);
			}
			else
			{
				//Just do an update
				try
				{
					updateFromRemote(userName, password, MergeFailOption.KEEP_LOCAL);
				}
				catch (MergeFailure e)
				{
					// merge failure should be impossible with the settings above.  We have already cleared any pre-existing local conflicts above, with a 
					//metadata delete, and keep_local prevents conflicts on update.
					throw new IOException("Should be impossible", e);
				}
			}
			
			List<String> modifiedFiles = makeInitialFilesAsNecessary(localFolder_);

			ArrayList<File> filesToCommit = new ArrayList<>();
			for (String s : modifiedFiles)
			{
				File f = new File(localFolder_, s);
				if (f.isFile())
				{
					addFiles(f.getName());
					filesToCommit.add(f);
				}
			}
			
			//See if ignore is set up property for 'lastUser.txt'
			SVNPropertyData prop = getSvn().getWCClient().doGetProperty(localFolder_, "svn:ignore", SVNRevision.HEAD, SVNRevision.HEAD);
			if (prop == null || prop.getValue() == null || prop.getValue().getString() == null || prop.getValue().getString().length() == 0)
			{
				log.debug("Configuring initial ignore settings");
				getSvn().getWCClient().doSetProperty(localFolder_, "svn:ignore", SVNPropertyValue.create("lastUser.txt"), false, SVNDepth.EMPTY,
						null, null);
				filesToCommit.add(localFolder_);
			}
			
			if (filesToCommit.size() > 0)
			{
				log.debug("Committing initial repository files");
				getSvn().getCommitClient().doCommit(filesToCommit.toArray(new File[filesToCommit.size()]), false, "Adding initial repository files", 
						null, null, false, true, SVNDepth.EMPTY);
			}
			
			log.info("Status after init: " + statusToString());
			
		}
		catch (SVNException e)
		{
			log.error("Unexpected", e);
			throw new IOException("Internal error", e);
		}
	}

	/**
	 * @see gov.va.isaac.interfaces.sync.ProfileSyncI#relinkRemote(java.io.File, java.lang.String)
	 */
	@Override
	public void relinkRemote(String remoteAddress) throws IllegalArgumentException, IOException
	{

		log.debug("Configuring remote URL and fetch defaults to {}", remoteAddress);
		try
		{
			getSvn().getUpdateClient().doRelocate(localFolder_, null, SVNURL.parseURIEncoded(SVNEncodingUtil.autoURIEncode(remoteAddress)), true);
		}
		catch (SVNException e)
		{
			log.error("Unexpected", e);
			throw new IOException("Internal error", e);
		}

	}

	/**
	 * @see gov.va.isaac.interfaces.sync.ProfileSyncI#addFiles(java.io.File, java.util.Set)
	 */
	@Override
	public void addFiles(String... files) throws IllegalArgumentException, IOException
	{
		try
		{
			log.info("Add Files called {}", Arrays.toString(files));
			if (files.length == 0)
			{
				log.debug("No files to add");
			}
			else
			{
				SVNWCClient client = getSvn().getWCClient();
				for (String file : files)
				{
					client.doAdd(new File(localFolder_, file), false, false, false, SVNDepth.INFINITY, false, true);
				}
			}
			log.info("addFiles Complete.  Current status: " + statusToString());
		}
		catch (SVNException e)
		{
			log.error("Unexpected", e);
			throw new IOException("Internal error", e);
		}
	}

	/**
	 * @see gov.va.isaac.interfaces.sync.ProfileSyncI#removeFiles(java.io.File, java.util.Set)
	 */
	@Override
	public void removeFiles(String... files) throws IllegalArgumentException, IOException
	{
		try
		{
			log.info("Remove Files called {}", Arrays.toString(files));
			if (files.length == 0)
			{
				log.debug("No files to remove");
			}
			else
			{
				SVNWCClient client = getSvn().getWCClient();
				for (String file : files)
				{
					client.doDelete(new File(localFolder_, file), true, false);
				}
			}
			log.info("removeFiles Complete.  Current status: " + statusToString());
		}
		catch (SVNException e)
		{
			log.error("Unexpected", e);
			throw new IOException("Internal error", e);
		}
	}

	/**
	 * @see gov.va.isaac.interfaces.sync.ProfileSyncI#addUntrackedFiles(java.io.File)
	 */
	@Override
	public void addUntrackedFiles() throws IllegalArgumentException, IOException
	{
		log.info("Add Untracked files called");
		try
		{
			HashSet<String> result = new HashSet<>();
			HashSet<String> conflicts = new HashSet<>();
			getSvn().getStatusClient().doStatus(localFolder_, SVNRevision.BASE, SVNDepth.INFINITY, false, false, false, false, new ISVNStatusHandler()
			{

				@Override
				public void handleStatus(SVNStatus status) throws SVNException
				{
					if (status.getNodeStatus() == SVNStatusType.STATUS_UNVERSIONED)
					{
						result.add(getPathRelativeToRoot(status.getFile()));
					}
					else if (status.getNodeStatus() == SVNStatusType.STATUS_CONFLICTED)
					{
						conflicts.add(getPathRelativeToRoot(status.getFile()));
					}
				}
			}, new ArrayList<String>());

			//don't try to add conflict marker files
			for (String s : conflicts)
			{
				Iterator<String> toAdd = result.iterator();
				while (toAdd.hasNext())
				{
					String item = toAdd.next();
					if (item.equals(s + ".mine") || item.startsWith(s + ".r"))
					{
						toAdd.remove();
					}
				}
			}

			addFiles(result.toArray(new String[result.size()]));
		}
		catch (SVNException e)
		{
			log.error("Unexpected", e);
			throw new IOException("Internal error", e);
		}
	}

	/**
	 * @throws MergeFailure
	 * @throws AuthenticationException
	 * @see gov.va.isaac.interfaces.sync.ProfileSyncI#updateCommitAndPush(java.io.File, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String[])
	 */
	@Override
	public Set<String> updateCommitAndPush(String commitMessage, String username, String password, MergeFailOption mergeFailOption, String... fileNamesToCommit)
			throws IllegalArgumentException, IOException, MergeFailure, AuthenticationException
	{
		try
		{
			log.info("Commit Files called {}", (fileNamesToCommit == null ? "-null-" : Arrays.toString(fileNamesToCommit)));

			Set<String> conflicting = getFilesInMergeConflict();
			if (conflicting.size() > 0)
			{
				log.info("Previous merge failure not yet resolved");
				throw new MergeFailure(conflicting, new HashSet<>());
			}

			if (fileNamesToCommit == null)
			{
				fileNamesToCommit = getLocallyModifiedFiles().toArray(new String[0]);
				log.info("Will commit the uncommitted files {}", Arrays.toString(fileNamesToCommit));
			}

			if (commitMessage == null || commitMessage.trim().length() == 0 && fileNamesToCommit.length > 0)
			{
				throw new IllegalArgumentException("The commit message is required when files are specified");
			}

			//perform update
			Set<String> updatedFiles = updateFromRemote(username, password, mergeFailOption);

			if (fileNamesToCommit.length > 0)
			{
				File[] filesToCommit = new File[fileNamesToCommit.length];
				for (int i = 0; i < fileNamesToCommit.length; i++)
				{
					filesToCommit[i] = new File(localFolder_, fileNamesToCommit[i]);
				}

				SVNCommitInfo result = getSvn().getCommitClient().doCommit(filesToCommit, false, commitMessage, null, null, false, false, SVNDepth.EMPTY);

				if (result.getErrorMessage() != null)
				{
					//I don't know if/when this would actually ever happen - seems most things result in a SVNException being thrown.
					log.error("Error during commit! " + result.getErrorMessage().toString());
					throw new IOException("Failed to commit.  Try updating first.");
				}

				log.debug("Commit completed - now at " + result.getNewRevision());
			}

			log.info("commit and push complete.  Current status: " + statusToString());
			return updatedFiles;
		}

		//TODO handle auth issues
		catch (SVNException e)
		{
			if (e.getErrorMessage() != null && SVNErrorCode.WC_NOT_UP_TO_DATE == e.getErrorMessage().getErrorCode())
			{
				log.error("Local checkout not up to date", e);
				throw new IOException("Local copy out of date with server.  Please update again");
			}
			log.error("Unexpected", e);
			throw new IOException("Internal error", e);
		}
	}

	/**
	 * @throws MergeFailure
	 * @throws AuthenticationException
	 * @see gov.va.isaac.interfaces.sync.ProfileSyncI#updateFromRemote(java.io.File, java.lang.String, java.lang.String,
	 * gov.va.isaac.interfaces.sync.MergeFailOption)
	 */
	@Override
	public Set<String> updateFromRemote(String username, String password, MergeFailOption mergeFailOption) throws IllegalArgumentException, IOException, MergeFailure,
			AuthenticationException
	{
		final Set<String> filesChangedDuringPull = new HashSet<>();
		try
		{
			log.info("update from remote called ");

			log.debug("Fetching from remote");

			Set<String> mergeConflicts = getFilesInMergeConflict();
			if (mergeConflicts.size() > 0)
			{
				log.info("Previous merge failure not yet resolved");
				throw new MergeFailure(mergeConflicts, new HashSet<>());
			}

			SVNUpdateClient client = getSvn().getUpdateClient();
			client.setEventHandler(new ISVNEventHandler()
			{

				@Override
				public void checkCancelled() throws SVNCancelException
				{
					// noop
				}

				@Override
				public void handleEvent(SVNEvent event, double progress) throws SVNException
				{
					//dont care about directory updates
					if (!event.getFile().isDirectory())
					{
						if (event.getAction() == SVNEventAction.UPDATE_UPDATE || event.getAction() == SVNEventAction.UPDATE_ADD
								|| event.getAction() == SVNEventAction.UPDATE_DELETE || event.getAction() == SVNEventAction.UPDATE_REPLACE)
						{
							filesChangedDuringPull.add(getPathRelativeToRoot(event.getFile()));
						}
						else
						{
							log.error("Unhandeled update action! {}", event.getAction());
						}
					}
				}
			});

			log.debug("Running Update");
			client.doUpdate(localFolder_, SVNRevision.HEAD, SVNDepth.INFINITY, false, false);

			System.out.println("After update:" + statusToString());

			Set<String> conflicted = getFilesInMergeConflict();

			if (conflicted.size() > 0)
			{
				if (mergeFailOption == null || MergeFailOption.FAIL == mergeFailOption)
				{
					throw new MergeFailure(conflicted, filesChangedDuringPull);
				}
				else if (MergeFailOption.KEEP_LOCAL == mergeFailOption || MergeFailOption.KEEP_REMOTE == mergeFailOption)
				{
					HashMap<String, MergeFailOption> resolutions = new HashMap<>();
					for (String s : conflicted)
					{
						resolutions.put(s, mergeFailOption);
					}
					log.debug("Resolving merge failures with option {}", mergeFailOption);
					filesChangedDuringPull.addAll(resolveMergeFailures(resolutions));
				}
				else
				{
					throw new IllegalArgumentException("Unexpected option");
				}
			}

			log.info("Files changed during updateFromRemote: {}", filesChangedDuringPull);
			return filesChangedDuringPull;
		}

		catch (SVNException e)
		{
			log.error("Unexpected", e);
			throw new IOException("Internal error", e);
		}
	}

	/**
	 * @throws MergeFailure
	 * @throws NoWorkTreeException
	 * @see gov.va.isaac.interfaces.sync.ProfileSyncI#resolveMergeFailures(java.io.File, java.util.Map)
	 */
	@Override
	public Set<String> resolveMergeFailures(Map<String, MergeFailOption> resolutions) throws IllegalArgumentException, IOException, MergeFailure
	{
		log.info("resolve merge failures called - resolutions: {}", resolutions);
		try
		{
			Set<String> conflicting = getFilesInMergeConflict();
			if (conflicting.size() == 0)
			{
				throw new IllegalArgumentException("You do not appear to have any conflicting files");
			}
			if (conflicting.size() != resolutions.size())
			{
				throw new IllegalArgumentException("You must provide a resolution for each conflicting file.  Files in conflict: " + conflicting);
			}
			for (String s : conflicting)
			{
				if (!resolutions.containsKey(s))
				{
					throw new IllegalArgumentException("No conflit resolution specified for file " + s + ".  Resolutions must be specified for all files");
				}
			}

			ArrayList<File> filesToCommit = new ArrayList<>();

			for (Entry<String, MergeFailOption> r : resolutions.entrySet())
			{
				if (MergeFailOption.FAIL == r.getValue())
				{
					throw new IllegalArgumentException("MergeFailOption.FAIL is not a valid option");
				}
				else if (MergeFailOption.KEEP_LOCAL == r.getValue())
				{
					log.debug("Keeping our local file for conflict {}", r.getKey());
					getSvn().getWCClient().doResolve(new File(localFolder_, r.getKey()), SVNDepth.EMPTY, SVNConflictChoice.MINE_FULL);
				}
				else if (MergeFailOption.KEEP_REMOTE == r.getValue())
				{
					log.debug("Keeping remote file for conflict {}", r.getKey());
					getSvn().getWCClient().doResolve(new File(localFolder_, r.getKey()), SVNDepth.EMPTY, SVNConflictChoice.THEIRS_FULL);
				}
				else
				{
					throw new IllegalArgumentException("MergeFailOption is required");
				}

				filesToCommit.add(new File(localFolder_, r.getKey()));
			}

			log.info("resolve merge failures Complete.  Current status: " + statusToString());

			return resolutions.keySet();
		}
		catch (SVNException e)
		{
			log.error("Unexpected", e);
			throw new IOException("Internal error", e);
		}
	}

	
	private SVNClientManager getSvn() throws IOException, IllegalArgumentException
	{
		return getSvn(false);
	}
	private SVNClientManager getSvn(boolean missingLocalOk) throws IOException, IllegalArgumentException
	{
		if (scm_ == null)
		{
			if (localFolder_ == null)
			{
				throw new IllegalArgumentException("localFolder has not yet been set - please call setRootLocation(...)");
			}
			if (!localFolder_.isDirectory())
			{
				log.error("The passed in local folder '{}' didn't exist", localFolder_);
				throw new IllegalArgumentException("The localFolder must be a folder, and must exist");
			}

			File svnFolder = new File(localFolder_, ".svn");

			if (!missingLocalOk && !svnFolder.isDirectory())
			{
				log.error("The passed in local folder '{}' does not appear to be a svn repository", localFolder_);
				throw new IllegalArgumentException("The localFolder does not appear to be a svn repository");
			}
			scm_ = SVNClientManager.newInstance();  //TODO authenticator
		}
		return scm_;
	}

	private String statusToString() throws IllegalArgumentException, SVNException, IOException
	{
		StringBuilder sb = new StringBuilder();

		HashMap<SVNStatusType, ArrayList<String>> result = new HashMap<>();
		getSvn().getStatusClient().doStatus(localFolder_, SVNRevision.BASE, SVNDepth.INFINITY, false, true, true, false, new ISVNStatusHandler()
		{

			@Override
			public void handleStatus(SVNStatus status) throws SVNException
			{
				ArrayList<String> temp = result.get(status.getNodeStatus());
				if (temp == null)
				{
					temp = new ArrayList<>();
					result.put(status.getNodeStatus(), temp);
				}
				String path = status.getRepositoryRelativePath();
				if (path == null || path.isEmpty())
				{
					path = getPathRelativeToRoot(status.getFile());
					if (path.isEmpty())
					{
						path = "{repo root}";
					}
				}
				temp.add(path);
			}
		}, new ArrayList<String>());

		for (Entry<SVNStatusType, ArrayList<String>> x : result.entrySet())
		{
			sb.append(x.getKey().toString() + " - " + x.getValue() + eol);
		}
		return sb.toString();
	}

	private String getPathRelativeToRoot(File file)
	{
		Path full = file.getAbsoluteFile().toPath();
		Path base = localFolder_.getAbsoluteFile().toPath();
		Path relative = base.relativize(full);
		return relative.toString();
	}

	/**
	 * returns a list of newly created files and files that were modified.
	 */
	private List<String> makeInitialFilesAsNecessary(File containingFolder) throws IOException
	{
		ArrayList<String> result = new ArrayList<>();
		File readme = new File(containingFolder, "README.md");
		if (!readme.isFile())
		{
			log.debug("Creating {}", readme.getAbsolutePath());
			Files.write(readme.toPath(), new String("ISAAC Profiles Storage \r" + "=== \r" + "This is a repository for storing ISAAC profiles and changesets.\r"
					+ "It is highly recommended that you do not make changes to this repository manually - ISAAC interfaces with this.").getBytes(),
					StandardOpenOption.CREATE_NEW);
			result.add(readme.getName());
		}
		else
		{
			log.debug("README.md already exists");
		}
		return result;
	}

	/**
	 * @see gov.va.isaac.interfaces.sync.ProfileSyncI#substituteURL(java.lang.String, java.lang.String)
	 * 
	 * 
	 * Otherwise, returns URL.
	 */
	@Override
	public String substituteURL(String url, String username)
	{
		//TODO need any sub patterns?
		return url;
	}

	/**
	 * @see gov.va.isaac.interfaces.sync.ProfileSyncI#isRootLocationConfiguredForSCM()
	 */
	@Override
	public boolean isRootLocationConfiguredForSCM()
	{
		return new File(localFolder_, ".svn").isDirectory();
	}

	/**
	 * @throws IOException
	 * @see gov.va.isaac.interfaces.sync.ProfileSyncI#getLocallyModifiedFileCount()
	 */
	@Override
	public int getLocallyModifiedFileCount() throws IOException
	{
		return getLocallyModifiedFiles().size();
	}

	private Set<String> getLocallyModifiedFiles() throws IOException
	{
		try
		{
			HashSet<String> result = new HashSet<>();
			getSvn().getStatusClient().doStatus(localFolder_, SVNRevision.BASE, SVNDepth.INFINITY, false, false, false, false, new ISVNStatusHandler()
			{

				@Override
				public void handleStatus(SVNStatus status) throws SVNException
				{
					if (status.getNodeStatus() == SVNStatusType.STATUS_MODIFIED || status.getNodeStatus() == SVNStatusType.STATUS_ADDED
							|| status.getNodeStatus() == SVNStatusType.STATUS_UNVERSIONED || status.getNodeStatus() == SVNStatusType.MERGED
							|| status.getNodeStatus() == SVNStatusType.STATUS_DELETED || status.getNodeStatus() == SVNStatusType.STATUS_REPLACED
							|| status.getNodeStatus() == SVNStatusType.MERGED)
					{
						result.add(getPathRelativeToRoot(status.getFile()));
					}
				}
			}, new ArrayList<String>());

			return result;
		}
		catch (Exception e)
		{
			log.error("Unexpected", e);
			throw new IOException("Internal error", e);
		}
	}

	/**
	 * @throws IOException
	 * @see gov.va.isaac.interfaces.sync.ProfileSyncI#getFilesInMergeConflict()
	 */
	@Override
	public Set<String> getFilesInMergeConflict() throws IOException
	{
		try
		{
			HashSet<String> result = new HashSet<>();
			getSvn().getStatusClient().doStatus(localFolder_, SVNRevision.BASE, SVNDepth.INFINITY, false, false, false, false, new ISVNStatusHandler()
			{

				@Override
				public void handleStatus(SVNStatus status) throws SVNException
				{
					if (status.isConflicted())
					{
						result.add(status.getRepositoryRelativePath());
					}
				}
			}, new ArrayList<String>());

			return result;
		}
		catch (Exception e)
		{
			log.error("Unexpected", e);
			throw new IOException("Internal error", e);
		}
	}
	
	private static void deleteRecursive(Path path) throws IOException
	{
		Files.walkFileTree(path, new SimpleFileVisitor<Path>()
		{
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
			{
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException
			{
				// try to delete the file anyway, even if its attributes could not be read, since delete-only access is theoretically possible
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
			{
				if (exc == null)
				{
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}
				else
				{
					// directory iteration failed; propagate exception
					throw exc;
				}
			}
		});
	}
}
