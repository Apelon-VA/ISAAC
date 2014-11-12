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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.ISVNStatusHandler;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusType;
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

//	/**
//	 * @throws AuthenticationException 
//	 * @see gov.va.isaac.interfaces.sync.ProfileSyncI#linkAndFetchFromRemote(java.io.File, java.lang.String, java.lang.String, java.lang.String)
//	 */
//	@Override
//	public void linkAndFetchFromRemote(String remoteAddress, String userName, String password) throws IllegalArgumentException, IOException, AuthenticationException
//	{
//		log.info("linkAndFetchFromRemote called - folder: {}, remoteAddress: {}, username: {}", localFolder_, remoteAddress, userName);
//		try
//		{
//			File svnFolder = new File(localFolder_, ".svn");
//
//			if (!svnFolder.isDirectory())
//			{
//				log.debug("Root folder does not contain a .git subfolder.  Creating new git repository.");
//				//TODO
//			}
//		}
//		catch (SVNException e)
//		{
//			log.error("Unexpected", e);
//			throw new IOException("Internal error", e);
//		}
//	}
//	
//	/**
//	 * @see gov.va.isaac.interfaces.sync.ProfileSyncI#relinkRemote(java.io.File, java.lang.String)
//	 */
//	@Override
//	public void relinkRemote(String remoteAddress) throws IllegalArgumentException, IOException
//	{
//		log.debug("Configuring remote URL and fetch defaults to {}", remoteAddress);
//		//TODO
//	}
//
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
			log.info("removeFiles Complete.  Current status: " +statusToString());
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
//
//	/**
//	 * @throws MergeFailure 
//	 * @throws AuthenticationException 
//	 * @see gov.va.isaac.interfaces.sync.ProfileSyncI#updateCommitAndPush(java.io.File, java.lang.String, java.lang.String, java.lang.String,
//	 * java.lang.String[])
//	 */
//	@Override
//	public Set<String> updateCommitAndPush(String commitMessage, String username, String password, MergeFailOption mergeFailOption, String... files)
//			throws IllegalArgumentException, IOException, MergeFailure, AuthenticationException
//	{
//		try
//		{
//			log.info("Commit Files called {}", (files == null ? "-null-" : Arrays.toString(files)));
//			Git git = getGit();
//			
//			if (git.status().call().getConflicting().size() > 0)
//			{
//				log.info("Previous merge failure not yet resolved");
//				throw new MergeFailure(git.status().call().getConflicting(), new HashSet<>());
//			}
//			
//			if (files == null)
//			{
//				files = git.status().call().getUncommittedChanges().toArray(new String[0]);
//				log.info("Will commit the uncommitted files {}", Arrays.toString(files));
//			}
//			
//			if (StringUtils.isEmptyOrNull(commitMessage) && files.length > 0)
//			{
//				throw new IllegalArgumentException("The commit message is required when files are specified");
//			}
//
//			if (files.length > 0)
//			{
//				CommitCommand commit = git.commit();
//				for (String file : files)
//				{
//					commit.setOnly(file);
//				}
//
//				commit.setAuthor(username, "42");
//				commit.setMessage(commitMessage);
//				RevCommit rv = commit.call();
//				log.debug("Local commit completed: " + rv.getFullMessage());
//			}
//
//			//need to merge origin/master into master now, prior to push
//			Set<String> result = updateFromRemote(username, password, mergeFailOption);
//
//			log.debug("Pushing");
//			CredentialsProvider cp = new SSHFriendlyUsernamePasswordCredsProvider(username, password);
//
//			Iterable<PushResult> pr = git.push().setCredentialsProvider(cp).call();
//			pr.forEach(new Consumer<PushResult>()
//			{
//				@Override
//				public void accept(PushResult t)
//				{
//					log.debug("Push Result Messages: " + t.getMessages());
//				}
//			});
//
//			log.info("commit and push complete.  Current status: " + statusToString(git.status().call()));
//			return result;
//		}
//		catch (TransportException te)
//		{
//			if (te.getMessage().contains("Auth fail"))
//			{
//				log.info("Auth fail", te);
//				throw new AuthenticationException("Auth fail");
//			}
//			else
//			{
//				log.error("Unexpected", te);
//				throw new IOException("Internal error", te);
//			}
//		}
//		catch (SVNException e)
//		{
//			log.error("Unexpected", e);
//			throw new IOException("Internal error", e);
//		}
//	}
//
	/**
	 * @throws MergeFailure
	 * @throws AuthenticationException 
	 * @see gov.va.isaac.interfaces.sync.ProfileSyncI#updateFromRemote(java.io.File, java.lang.String, java.lang.String,
	 * gov.va.isaac.interfaces.sync.MergeFailOption)
	 */
	@Override
	public Set<String> updateFromRemote(String username, String password, MergeFailOption mergeFailOption) throws IllegalArgumentException, IOException,
			MergeFailure, AuthenticationException
	{
		Set<String> filesChangedDuringPull = null;
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
//			
//			CredentialsProvider cp = new SSHFriendlyUsernamePasswordCredsProvider(username, password);
//			log.debug("Fetch Message" + git.fetch().setCredentialsProvider(cp).call().getMessages());
//			
//			ObjectId masterIdBeforeMerge = git.getRepository().getRef("master").getObjectId();
//			if (git.getRepository().getRef("refs/remotes/origin/master").getObjectId().getName().equals(masterIdBeforeMerge.getName()))
//			{
//				log.info("No changes to merge");
//				return new HashSet<String>();
//			}
//
//
//			{
//				log.debug("Merging from remotes/origin/master");
//				MergeResult mr = git.merge().include(git.getRepository().getRef("refs/remotes/origin/master")).call();
//				AnyObjectId headAfterMergeID = mr.getNewHead();
//				
//				if (!mr.getMergeStatus().isSuccessful())
//				{
//					if (mergeFailOption == null || MergeFailOption.FAIL == mergeFailOption)
//					{
//						addNote(NOTE_FAILED_MERGE_HAPPENED_ON_REMOTE + (stash == null ? ":NO_STASH" : STASH_MARKER + stash.getName()), git);
//						//We can use the status here - because we already stashed the stuff that they had uncommitted above.
//						throw new MergeFailure(mr.getConflicts().keySet(), git.status().call().getUncommittedChanges());
//					}
//					else if (MergeFailOption.KEEP_LOCAL == mergeFailOption || MergeFailOption.KEEP_REMOTE == mergeFailOption)
//					{
//						HashMap<String, MergeFailOption> resolutions = new HashMap<>();
//						for (String s : mr.getConflicts().keySet())
//						{
//							resolutions.put(s, mergeFailOption);
//						}
//						log.debug("Resolving merge failures with option {}", mergeFailOption);
//						filesChangedDuringPull = resolveMergeFailures(MergeFailType.REMOTE_TO_LOCAL, (stash == null ? null : stash.getName()), resolutions);
//					}
//					else
//					{
//						throw new IllegalArgumentException("Unexpected option");
//					}
//				}
//				else
//				{
//					//Conflict free merge - or perhaps, no merge at all.
//					if (masterIdBeforeMerge.getName().equals(headAfterMergeID.getName()))
//					{
//						log.debug("Merge didn't result in a commit - no incoming changes");
//						filesChangedDuringPull = new HashSet<>();
//					}
//					else
//					{
//						filesChangedDuringPull = listFilesChangedInCommit(git.getRepository(), masterIdBeforeMerge, headAfterMergeID);
//					}
//				}
//			}

		
			log.info("Files changed during updateFromRemote: {}", filesChangedDuringPull);
			return filesChangedDuringPull;
		}
		
		//TODO put SVN back
		catch (Exception e)
		{
			log.error("Unexpected", e);
			throw new IOException("Internal error", e);
		}
	}
	
//	
//	/**
//	 * @throws MergeFailure 
//	 * @throws NoWorkTreeException 
//	 * @see gov.va.isaac.interfaces.sync.ProfileSyncI#resolveMergeFailures(java.io.File, java.util.Map)
//	 */
//	@Override
//	public Set<String> resolveMergeFailures(Map<String, MergeFailOption> resolutions) throws IllegalArgumentException, IOException, NoWorkTreeException, MergeFailure
//	{
//		log.info("resolve merge failures called - resolutions: {}", resolutions);
//		try
//		{
//			Git git = getGit();
	
//	Set<String> conflicting = git.status().call().getConflicting();
//	if (conflicting.size() == 0)
//	{
//		throw new IllegalArgumentException("You do not appear to have any conflicting files");
//	}
//	if (conflicting.size() !=  resolutions.size())
//	{
//		throw new IllegalArgumentException("You must provide a resolution for each conflicting file.  Files in conflict: " + conflicting);
//	}
//	for (String s : conflicting)
//	{
//		if (!resolutions.containsKey(s))
//		{
//			throw new IllegalArgumentException("No conflit resolution specified for file " + s + ".  Resolutions must be specified for all files");
//		}
//	}

//			for (Entry<String, MergeFailOption> r : resolutions.entrySet())
//			{
//				if (MergeFailOption.FAIL == r.getValue())
//				{
//					throw new IllegalArgumentException("MergeFailOption.FAIL is not a valid option");
//				}
//				else if (MergeFailOption.KEEP_LOCAL == r.getValue())
//				{
//					log.debug("Keeping our local file for conflict {}", r.getKey());
//					git.checkout().addPath(r.getKey()).setStage(MergeFailType.REMOTE_TO_LOCAL == mergeFailType ? Stage.OURS : Stage.THEIRS).call();
//				}
//				else if (MergeFailOption.KEEP_REMOTE == r.getValue())
//				{
//					log.debug("Keeping remote file for conflict {}", r.getKey());
//					git.checkout().addPath(r.getKey()).setStage(MergeFailType.REMOTE_TO_LOCAL == mergeFailType ? Stage.THEIRS : Stage.OURS).call();
//				}
//				else
//				{
//					throw new IllegalArgumentException("MergeFailOption is required");
//				}
//				
//				log.debug("calling add to mark merge resolved");
//				git.add().addFilepattern(r.getKey()).call();
//			}
//			
//			if (mergeFailType == MergeFailType.STASH_TO_LOCAL)
//			{
//				//clean up the stash
//				log.debug("Dropping stash");
//				git.stashDrop().call();
//			}
//			
//			
//			RevWalk walk = new RevWalk(git.getRepository());
//			Ref head = git.getRepository().getRef("refs/heads/master");
//			RevCommit commitWithPotentialNote = walk.parseCommit(head.getObjectId());
//			
//			log.info("resolve merge failures Complete.  Current status: " + statusToString(git.status().call()));
//			
//			RevCommit rc = git.commit().setMessage("Merging with user specified merge failure resolution for files " + resolutions.keySet()).call();
//			
//			git.notesRemove().setObjectId(commitWithPotentialNote).call();
//			Set<String> filesChangedInCommit = listFilesChangedInCommit(git.getRepository(), commitWithPotentialNote.getId(), rc);
//			
//			//When we auto resolve to KEEP_REMOTE - these will have changed - make sure they are in the list.
//			//TODO seems like this shouldn't really be necessary - need to look into the listFilesChangedInCommit algorithm closer.
//			//this might already be fixed by the rework on 11/12/14, but no time to validate at the moment.
//			for (Entry<String, MergeFailOption> r : resolutions.entrySet())
//			{
//				if (MergeFailOption.KEEP_REMOTE == r.getValue())
//				{
//					filesChangedInCommit.add(r.getKey());
//				}
//				if (MergeFailOption.KEEP_LOCAL == r.getValue())
//				{
//					filesChangedInCommit.remove(r.getKey());
//				}
//			}
//			
//			if (!StringUtils.isEmptyOrNull(stashIDToApply))
//			{
//				log.info("Replaying stash identified in note");
//				try
//				{
//					git.stashApply().setStashRef(stashIDToApply).call();
//					log.debug("stash applied cleanly, dropping stash");
//					git.stashDrop().call();
//				}
//				catch (StashApplyFailureException e)
//				{
//					log.debug("Stash failed to merge");
//					addNote(NOTE_FAILED_MERGE_HAPPENED_ON_STASH, git);
//					throw new MergeFailure(git.status().call().getConflicting(), filesChangedInCommit);
//				}
//			}
//			
//			return filesChangedInCommit;
//		}
//		catch (SVNException e)
//		{
//			log.error("Unexpected", e);
//			throw new IOException("Internal error", e);
//		}
//	}
//
//	private HashSet<String> listFilesChangedInCommit(Repository repository, AnyObjectId beforeID, AnyObjectId afterID) throws MissingObjectException, IncorrectObjectTypeException, IOException
//	{
//		log.info("calculating files changed in commit");
//		HashSet<String> result = new HashSet<>();
//		RevWalk rw = new RevWalk(repository);
//		RevCommit commitBefore = rw.parseCommit(beforeID);
//		RevCommit commitAfter = rw.parseCommit(afterID);
//		DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
//		df.setRepository(repository);
//		df.setDiffComparator(RawTextComparator.DEFAULT);
//		df.setDetectRenames(true);
//		List<DiffEntry> diffs = df.scan(commitBefore.getTree(), commitAfter.getTree());
//		for (DiffEntry diff : diffs)
//		{
//			result.add(diff.getNewPath());
//		}
//		log.debug("Files changed between commits commit: {} and {} - {}", beforeID.getName(), afterID, result);
//		return result;
//	}
//
	private SVNClientManager getSvn() throws IOException, IllegalArgumentException
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
	
			if (!svnFolder.isDirectory())
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
		
		//TODO handle getting 'lastUser.txt' into the SVN ignore properties
		return result;
	}

	/**
	 * @see gov.va.isaac.interfaces.sync.ProfileSyncI#substituteURL(java.lang.String, java.lang.String)
	 * 
	 *  
	 *  Otherwise, returns URL.
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
		try
		{
			HashSet<String> result = new HashSet<>();
			getSvn().getStatusClient().doStatus(localFolder_, SVNRevision.BASE, SVNDepth.INFINITY, false, false, false, false, new ISVNStatusHandler()
			{
				
				@Override
				public void handleStatus(SVNStatus status) throws SVNException
				{
					//TODO any others I need to check?
					if (status.getNodeStatus() == SVNStatusType.STATUS_MODIFIED || status.getNodeStatus() == SVNStatusType.STATUS_ADDED
							|| status.getNodeStatus() == SVNStatusType.STATUS_UNVERSIONED || status.getNodeStatus() == SVNStatusType.MERGED)
					{
						result.add(status.getRepositoryRelativePath());
					}
				}
			}, new ArrayList<String>());
			
			return result.size();
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

	
	/**
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	
	/**
	 * @see gov.va.isaac.interfaces.sync.ProfileSyncI#linkAndFetchFromRemote(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void linkAndFetchFromRemote(String remoteAddress, String username, String password) throws IllegalArgumentException, IOException, AuthenticationException
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see gov.va.isaac.interfaces.sync.ProfileSyncI#relinkRemote(java.lang.String)
	 */
	@Override
	public void relinkRemote(String remoteAddress) throws IllegalArgumentException, IOException
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see gov.va.isaac.interfaces.sync.ProfileSyncI#updateCommitAndPush(java.lang.String, java.lang.String, java.lang.String, gov.va.isaac.interfaces.sync.MergeFailOption, java.lang.String[])
	 */
	@Override
	public Set<String> updateCommitAndPush(String commitMessage, String username, String password, MergeFailOption mergeFailOption, String... files)
			throws IllegalArgumentException, IOException, MergeFailure, AuthenticationException
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see gov.va.isaac.interfaces.sync.ProfileSyncI#resolveMergeFailures(java.util.Map)
	 */
	@Override
	public Set<String> resolveMergeFailures(Map<String, MergeFailOption> resolutions) throws IllegalArgumentException, IOException, MergeFailure
	{
		// TODO Auto-generated method stub
		return null;
	}

}
