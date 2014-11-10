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

import gov.va.isaac.interfaces.sync.MergeFailOption;
import gov.va.isaac.interfaces.sync.MergeFailure;
import gov.va.isaac.interfaces.sync.ProfileSyncI;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CheckoutCommand.Stage;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.StashApplyFailureException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.LargeObjectException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.notes.Note;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.util.StringUtils;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jcraft.jsch.JSch;

/**
 * {@link SyncServiceGIT}
 * 
 * A GIT implementation of {@link ProfileSyncI}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service(name = "GIT")
@PerLookup
public class SyncServiceGIT implements ProfileSyncI
{
	private static Logger log = LoggerFactory.getLogger(SyncServiceGIT.class);

	private final String eol = System.getProperty("line.separator");
	private final String NOTE_FAILED_MERGE_HAPPENED_ON_REMOTE = "Conflicted merge happened during remote merge";
	private final String NOTE_FAILED_MERGE_HAPPENED_ON_STASH = "Conflicted merge happened during stash merge";
	private final String STASH_MARKER = ":STASH-";
	
	private File localFolder = null;
	
	//TODO figure out how we handle prompts for things.  GUI vs no GUI... etc.
	public SyncServiceGIT(File localFolder)
	{	
		this();
		setRootLocation(localFolder);
	}

	private SyncServiceGIT()
	{
		//For HK2
		JSch.setLogger(new com.jcraft.jsch.Logger()
		{
			private HashMap<Integer, Consumer<String>> logMap = new HashMap<>();
			private HashMap<Integer, BooleanSupplier> enabledMap = new HashMap<>();
			
			{
				logMap.put(com.jcraft.jsch.Logger.DEBUG, log::debug);
				logMap.put(com.jcraft.jsch.Logger.ERROR, log::error);
				logMap.put(com.jcraft.jsch.Logger.FATAL, log::error);
				logMap.put(com.jcraft.jsch.Logger.INFO, log::info);
				logMap.put(com.jcraft.jsch.Logger.WARN, log::warn);
				
				enabledMap.put(com.jcraft.jsch.Logger.DEBUG, log::isDebugEnabled);
				enabledMap.put(com.jcraft.jsch.Logger.ERROR, log::isErrorEnabled);
				enabledMap.put(com.jcraft.jsch.Logger.FATAL, log::isErrorEnabled);
				enabledMap.put(com.jcraft.jsch.Logger.INFO, log::isInfoEnabled);
				enabledMap.put(com.jcraft.jsch.Logger.WARN, log::isWarnEnabled);
			}
			@Override
			public void log(int level, String message)
			{
				logMap.get(level).accept(message);
			}
			
			@Override
			public boolean isEnabled(int level)
			{
				return enabledMap.get(level).getAsBoolean();
			}
		});
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
		this.localFolder = localFolder;
	}

	/**
	 * @see gov.va.isaac.interfaces.sync.ProfileSyncI#linkAndFetchFromRemote(java.io.File, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void linkAndFetchFromRemote(String remoteAddress, String userName, String password) throws IllegalArgumentException, IOException
	{
		log.info("linkAndFetchFromRemote called - folder: {}, remoteAddress: {}, username: {}", localFolder, remoteAddress, userName);
		try
		{
			File gitFolder = new File(localFolder, ".git");
			Repository r = new FileRepository(gitFolder);

			if (!gitFolder.isDirectory())
			{
				log.debug("Root folder does not contain a .git subfolder.  Creating new git repository.");
				r.create();
			}

			relinkRemote(remoteAddress);

			Git git = new Git(r);

			CredentialsProvider cp = new SSHFriendlyUsernamePasswordCredsProvider(userName, password);

			log.debug("Fetching");
			FetchResult fr = git.fetch().setCheckFetchedObjects(true).setCredentialsProvider(cp).call();
			log.debug("Fetch messages: {}", fr.getMessages());

			boolean remoteHasMaster = false;
			Collection<Ref> refs = git.lsRemote().setCredentialsProvider(cp).call();
			for (Ref ref : refs)
			{
				if ("refs/heads/master".equals(ref.getName()))
				{
					remoteHasMaster = true;
					log.debug("Remote already has 'heads/master'");
					break;
				}
			}

			if (remoteHasMaster)
			{
				//we need to fetch and (maybe) merge - get onto origin/master.

				log.debug("Fetching from remote");
				String fetchResult = git.fetch().setCredentialsProvider(cp).call().getMessages();
				log.debug("Fetch Result: {}", fetchResult);

				log.debug("Resetting to origin/master");
				git.reset().setMode(ResetType.MIXED).setRef("origin/master").call();

				//Get the files from master that we didn't have in our working folder
				log.debug("Checking out missing files from origin/master");
				for (String missing : git.status().call().getMissing())
				{
					log.debug("Checkout {}", missing);
					git.checkout().addPath(missing).call();
				}

				for (String newFile : makeInitialFilesAsNecessary(localFolder))
				{
					log.debug("Adding and committing {}", newFile);
					git.add().addFilepattern(newFile).call();
					git.commit().setMessage("Adding " + newFile).setAuthor(userName, "42").call();

					for (PushResult pr : git.push().setCredentialsProvider(cp).call())
					{
						log.debug("Push Message: {}", pr.getMessages());
					}
				}
			}
			else
			{
				//just push
				//make sure we have something to push
				for (String newFile : makeInitialFilesAsNecessary(localFolder))
				{
					log.debug("Adding and committing {}", newFile);
					git.add().addFilepattern(newFile).call();
					git.commit().setMessage("Adding readme file").setAuthor(userName, "42").call();
				}

				log.debug("Pushing repository");
				for (PushResult pr : git.push().setCredentialsProvider(cp).call())
				{
					log.debug("Push Result: {}", pr.getMessages());
				}
			}

			log.info("linkAndFetchFromRemote Complete.  Current status: " + statusToString(git.status().call()));
		}
		catch (GitAPIException e)
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
		StoredConfig sc = getGit().getRepository().getConfig();
		sc.setString("remote", "origin", "url", remoteAddress);
		sc.setString("remote", "origin", "fetch", "+refs/heads/*:refs/remotes/origin/*");
		sc.save();
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
			Git git = getGit();
			if (files.length == 0)
			{
				log.debug("No files to add");
			}
			else
			{
				AddCommand ac = git.add();
				for (String file : files)
				{
					ac.addFilepattern(file);
				}
				ac.call();
			}
			log.info("addFiles Complete.  Current status: " + statusToString(git.status().call()));
		}
		catch (GitAPIException e)
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
			Git git = getGit();
			if (files.length == 0)
			{
				log.debug("No files to remove");
			}
			else
			{
				RmCommand rm = git.rm();
				for (String file : files)
				{
					rm.addFilepattern(file);
				}
				rm.call();
			}
			log.info("removeFiles Complete.  Current status: " + statusToString(git.status().call()));
		}
		catch (GitAPIException e)
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
			Git git = getGit();
			Status s = git.status().call();

			addFiles(s.getUntracked().toArray(new String[s.getUntracked().size()]));
		}
		catch (GitAPIException e)
		{
			log.error("Unexpected", e);
			throw new IOException("Internal error", e);
		}
	}

	/**
	 * @throws MergeFailure 
	 * @see gov.va.isaac.interfaces.sync.ProfileSyncI#updateCommitAndPush(java.io.File, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String[])
	 */
	@Override
	public Set<String> updateCommitAndPush(String commitMessage, String username, String password, MergeFailOption mergeFailOption, String... files)
			throws IllegalArgumentException, IOException, MergeFailure
	{
		try
		{
			log.info("Commit Files called {}", (files == null ? "-null-" : Arrays.toString(files)));
			Git git = getGit();
			
			if (git.status().call().getConflicting().size() > 0)
			{
				log.info("Previous merge failure not yet resolved");
				throw new MergeFailure(git.status().call().getConflicting(), new HashSet<>());
			}
			
			if (files == null)
			{
				files = git.status().call().getUncommittedChanges().toArray(new String[0]);
				log.info("Will commit the uncommitted files {}", Arrays.toString(files));
			}
			
			if (StringUtils.isEmptyOrNull(commitMessage) && files.length > 0)
			{
				throw new IllegalArgumentException("The commit message is required when files are specified");
			}

			if (files.length > 0)
			{
				CommitCommand commit = git.commit();
				for (String file : files)
				{
					commit.setOnly(file);
				}

				commit.setAuthor(username, "42");
				commit.setMessage(commitMessage);
				RevCommit rv = commit.call();
				log.debug("Local commit completed: " + rv.getFullMessage());
			}

			//need to merge origin/master into master now, prior to push
			Set<String> result = updateFromRemote(username, password, mergeFailOption);

			log.debug("Pushing");
			CredentialsProvider cp = new SSHFriendlyUsernamePasswordCredsProvider(username, password);

			Iterable<PushResult> pr = git.push().setCredentialsProvider(cp).call();
			pr.forEach(new Consumer<PushResult>()
			{
				@Override
				public void accept(PushResult t)
				{
					log.debug("Push Result Messages: " + t.getMessages());
				}
			});

			log.info("commit and push complete.  Current status: " + statusToString(git.status().call()));
			return result;
		}
		catch (GitAPIException e)
		{
			log.error("Unexpected", e);
			throw new IOException("Internal error", e);
		}
	}

	/**
	 * @throws MergeFailure
	 * @see gov.va.isaac.interfaces.sync.ProfileSyncI#updateFromRemote(java.io.File, java.lang.String, java.lang.String,
	 * gov.va.isaac.interfaces.sync.MergeFailOption)
	 */
	@Override
	public Set<String> updateFromRemote(String username, String password, MergeFailOption mergeFailOption) throws IllegalArgumentException, IOException,
			MergeFailure
	{
		Set<String> filesChangedDuringPull;
		try
		{
			log.info("update from remote called ");

			Git git = getGit();
			
			log.debug("Fetching from remote");
			
			if (git.status().call().getConflicting().size() > 0)
			{
				log.info("Previous merge failure not yet resolved");
				throw new MergeFailure(git.status().call().getConflicting(), new HashSet<>());
			}
			
			CredentialsProvider cp = new SSHFriendlyUsernamePasswordCredsProvider(username, password);
			log.debug("Fetch Message" + git.fetch().setCredentialsProvider(cp).call().getMessages());
			
			ObjectId masterIdBeforeMerge = git.getRepository().getRef("master").getObjectId();
			if (git.getRepository().getRef("refs/remotes/origin/master").getObjectId().getName().equals(masterIdBeforeMerge.getName()))
			{
				log.info("No changes to merge");
				return new HashSet<String>();
			}

			RevCommit stash = null;
			if (git.status().call().getUncommittedChanges().size() > 0)
			{
				log.info("Stashing uncommitted changes");
				stash = git.stashCreate().call();
			}

			{
				log.debug("Merging from remotes/origin/master");
				MergeResult mr = git.merge().include(git.getRepository().getRef("refs/remotes/origin/master")).call();
				AnyObjectId headAfterMergeID = mr.getNewHead();
				
				if (!mr.getMergeStatus().isSuccessful())
				{
					if (mergeFailOption == null || MergeFailOption.FAIL == mergeFailOption)
					{
						addNote(NOTE_FAILED_MERGE_HAPPENED_ON_REMOTE + (stash == null ? ":NO_STASH" : STASH_MARKER + stash.getName()), git);
						//We can use the status here - because we already stashed the stuff that they had uncommitted above.
						throw new MergeFailure(mr.getConflicts().keySet(), git.status().call().getUncommittedChanges());
					}
					else if (MergeFailOption.KEEP_LOCAL == mergeFailOption || MergeFailOption.KEEP_REMOTE == mergeFailOption)
					{
						HashMap<String, MergeFailOption> resolutions = new HashMap<>();
						for (String s : mr.getConflicts().keySet())
						{
							resolutions.put(s, mergeFailOption);
						}
						log.debug("Resolving merge failures with option {}", mergeFailOption);
						filesChangedDuringPull = resolveMergeFailures(MergeFailType.REMOTE_TO_LOCAL, (stash == null ? null : stash.getName()), resolutions);
					}
					else
					{
						throw new IllegalArgumentException("Unexpected option");
					}
				}
				else
				{
					//Conflict free merge - or perhaps, no merge at all.
					if (masterIdBeforeMerge.getName().equals(headAfterMergeID.getName()))
					{
						log.debug("Merge didn't result in a commit - no incoming changes");
						filesChangedDuringPull = new HashSet<>();
					}
					else
					{
						filesChangedDuringPull = listFilesChangedInCommit(git.getRepository(), headAfterMergeID);
					}
				}
			}

			if (stash != null)
			{
				log.info("Replaying stash");
				try
				{
					git.stashApply().setStashRef(stash.getName()).call();
					log.debug("stash applied cleanly, dropping stash");
					git.stashDrop().call();
				}
				catch (StashApplyFailureException e)
				{
					log.debug("Stash failed to merge");
					if (mergeFailOption == null || MergeFailOption.FAIL == mergeFailOption)
					{
						addNote(NOTE_FAILED_MERGE_HAPPENED_ON_STASH, git);
						throw new MergeFailure(git.status().call().getConflicting(), filesChangedDuringPull);
					}
					
					else if (MergeFailOption.KEEP_LOCAL == mergeFailOption || MergeFailOption.KEEP_REMOTE == mergeFailOption)
					{
						HashMap<String, MergeFailOption> resolutions = new HashMap<>();
						for (String s : git.status().call().getConflicting())
						{
							resolutions.put(s, mergeFailOption);
						}
						log.debug("Resolving stash apply merge failures with option {}", mergeFailOption);
						resolveMergeFailures(MergeFailType.STASH_TO_LOCAL, null, resolutions);
						//When we auto resolve to KEEP_LOCAL - these files won't have really changed, even though we recorded a change above.
						for (Entry<String, MergeFailOption> r : resolutions.entrySet())
						{
							if (MergeFailOption.KEEP_LOCAL == r.getValue())
							{
								filesChangedDuringPull.remove(r.getKey());
							}
						}
					}
					else
					{
						throw new IllegalArgumentException("Unexpected option");
					}
				}
			}
			log.info("Files changed during updateFromRemote: {}", filesChangedDuringPull);
			return filesChangedDuringPull;
		}
		catch (CheckoutConflictException e)
		{
			log.error("Unexpected", e);
			throw new IOException("A local file exists (but is not yet added to source control) which conflicts with a file from the server."
					+ "  Either delete the local file, or call addFile(...) on the offending file prior to attempting to update from remote.", e);
		}
		catch (GitAPIException e)
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
	public Set<String> resolveMergeFailures(Map<String, MergeFailOption> resolutions) throws IllegalArgumentException, IOException, NoWorkTreeException, MergeFailure
	{
		log.info("resolve merge failures called - resolutions: {}", resolutions);
		try
		{
			Git git = getGit();
			
			List<Note> notes = git.notesList().call();
			
			Set<String> conflicting = git.status().call().getConflicting();
			if (conflicting.size() == 0)
			{
				throw new IllegalArgumentException("You do not appear to have any conflicting files");
			}
			if (conflicting.size() !=  resolutions.size())
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
			
			if (notes == null || notes.size() == 0)
			{
				throw new IllegalArgumentException("The 'note' that is required for tracking state is missing.  This merge failure must be resolved on the command line");
			}
			
			String noteValue = new String(git.getRepository().open(notes.get(0).getData()).getBytes());
			
			MergeFailType mergeFailType;
			if (noteValue.startsWith(NOTE_FAILED_MERGE_HAPPENED_ON_REMOTE))
			{
				mergeFailType = MergeFailType.REMOTE_TO_LOCAL;
			}
			else if (noteValue.startsWith(NOTE_FAILED_MERGE_HAPPENED_ON_STASH))
			{
				mergeFailType = MergeFailType.STASH_TO_LOCAL;
			}
			else
			{
				throw new IllegalArgumentException("The 'note' that is required for tracking state contains an unexpected value of '" + noteValue + "'");
			}
			String stashIdToApply = null;
			if (noteValue.contains(STASH_MARKER))
			{
				stashIdToApply = noteValue.substring(noteValue.indexOf(STASH_MARKER) + STASH_MARKER.length());
			}
			
			return resolveMergeFailures(mergeFailType, stashIdToApply, resolutions);
		}
		catch (GitAPIException | LargeObjectException e)
		{
			log.error("Unexpected", e);
			throw new IOException("Internal error", e);
		}
	}

	
	private Set<String> resolveMergeFailures(MergeFailType mergeFailType, String stashIDToApply, Map<String, MergeFailOption> resolutions) 
			throws IllegalArgumentException, IOException, MergeFailure
	{
		log.debug("resolve merge failures called - mergeFailType: {} stashIDToApply: {} resolutions: {}", mergeFailType, stashIDToApply, resolutions);
		try
		{
			Git git = getGit();
			
			//We unfortunately, must know the mergeFailType option, because the resolution mechanism here uses OURS and THEIRS - but the 
			//meaning of OURS and THEIRS reverse, depending on if you are recovering from a merge failure, or a stash apply failure.
			
			for (Entry<String, MergeFailOption> r : resolutions.entrySet())
			{
				if (MergeFailOption.FAIL == r.getValue())
				{
					throw new IllegalArgumentException("MergeFailOption.FAIL is not a valid option");
				}
				else if (MergeFailOption.KEEP_LOCAL == r.getValue())
				{
					log.debug("Keeping our local file for conflict {}", r.getKey());
					git.checkout().addPath(r.getKey()).setStage(MergeFailType.REMOTE_TO_LOCAL == mergeFailType ? Stage.OURS : Stage.THEIRS).call();
				}
				else if (MergeFailOption.KEEP_REMOTE == r.getValue())
				{
					log.debug("Keeping remote file for conflict {}", r.getKey());
					git.checkout().addPath(r.getKey()).setStage(MergeFailType.REMOTE_TO_LOCAL == mergeFailType ? Stage.THEIRS : Stage.OURS).call();
				}
				else
				{
					throw new IllegalArgumentException("MergeFailOption is required");
				}
				
				log.debug("calling add to mark merge resolved");
				git.add().addFilepattern(r.getKey()).call();
			}
			
			if (mergeFailType == MergeFailType.STASH_TO_LOCAL)
			{
				//clean up the stash
				log.debug("Dropping stash");
				git.stashDrop().call();
			}
			
			
			RevWalk walk = new RevWalk(git.getRepository());
			Ref head = git.getRepository().getRef("refs/heads/master");
			RevCommit commitWithPotentialNote = walk.parseCommit(head.getObjectId());
			
			log.info("resolve merge failures Complete.  Current status: " + statusToString(git.status().call()));
			
			RevCommit rc = git.commit().setMessage("Merging with user specified merge failure resolution for files " + resolutions.keySet()).call();
			
			git.notesRemove().setObjectId(commitWithPotentialNote).call();
			Set<String> filesChangedInCommit = listFilesChangedInCommit(git.getRepository(), rc);
			
			//When we auto resolve to KEEP_REMOTE - these will have changed - make sure they are in the list.
			//TODO seems like this shouldn't really be necessary - need to look into the listFilesChangedInCommit algorithm closer.
			for (Entry<String, MergeFailOption> r : resolutions.entrySet())
			{
				if (MergeFailOption.KEEP_REMOTE == r.getValue())
				{
					filesChangedInCommit.add(r.getKey());
				}
				if (MergeFailOption.KEEP_LOCAL == r.getValue())
				{
					filesChangedInCommit.remove(r.getKey());
				}
			}
			
			if (!StringUtils.isEmptyOrNull(stashIDToApply))
			{
				log.info("Replaying stash identified in note");
				try
				{
					git.stashApply().setStashRef(stashIDToApply).call();
					log.debug("stash applied cleanly, dropping stash");
					git.stashDrop().call();
				}
				catch (StashApplyFailureException e)
				{
					log.debug("Stash failed to merge");
					addNote(NOTE_FAILED_MERGE_HAPPENED_ON_STASH, git);
					throw new MergeFailure(git.status().call().getConflicting(), filesChangedInCommit);
				}
			}
			
			return filesChangedInCommit;
		}
		catch (GitAPIException e)
		{
			log.error("Unexpected", e);
			throw new IOException("Internal error", e);
		}
	}
	
	private void addNote(String message, Git git) throws IOException, GitAPIException
	{
		RevWalk walk = new RevWalk(git.getRepository());
		Ref head = git.getRepository().getRef("refs/heads/master");
		RevCommit commit = walk.parseCommit(head.getObjectId());
		git.notesAdd().setObjectId(commit).setMessage(message).call();
		
	}

	private HashSet<String> listFilesChangedInCommit(Repository repository, AnyObjectId commitId) throws MissingObjectException, IncorrectObjectTypeException, IOException
	{
		log.info("calculating files changed in commit");
		HashSet<String> result = new HashSet<>();
		RevWalk rw = new RevWalk(repository);
		RevCommit commit = rw.parseCommit(commitId);
		RevCommit parent = rw.parseCommit(commit.getParent(0).getId());
		DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
		df.setRepository(repository);
		df.setDiffComparator(RawTextComparator.DEFAULT);
		df.setDetectRenames(true);
		List<DiffEntry> diffs = df.scan(parent.getTree(), commit.getTree());
		for (DiffEntry diff : diffs)
		{
			result.add(diff.getNewPath());
		}
		log.debug("Files changed in commit: {} - {}", commitId.getName(), result);
		return result;
	}

	private Git getGit() throws IOException, IllegalArgumentException
	{
		if (localFolder == null)
		{
			throw new IllegalArgumentException("localFolder has not yet been set - please call setRootLocation(...)");
		}
		if (!localFolder.isDirectory())
		{
			log.error("The passed in local folder '{}' didn't exist", localFolder);
			throw new IllegalArgumentException("The localFolder must be a folder, and must exist");
		}

		File gitFolder = new File(localFolder, ".git");

		if (!gitFolder.isDirectory())
		{
			log.error("The passed in local folder '{}' does not appear to be a git repository", localFolder);
			throw new IllegalArgumentException("The localFolder does not appear to be a git repository");
		}
		return new Git(new FileRepository(gitFolder));
	}

	private String statusToString(Status status)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Is clean: " + status.isClean() + eol);
		sb.append("Changed: " + status.getChanged() + eol);
		sb.append("Added: " + status.getAdded() + eol);
		sb.append("Conflicting: " + status.getConflicting() + eol);
		sb.append("Ignored, unindexed: " + status.getIgnoredNotInIndex() + eol);
		sb.append("Missing: " + status.getMissing() + eol);
		sb.append("Modified: " + status.getModified() + eol);
		sb.append("Removed: " + status.getRemoved() + eol);
		sb.append("UncomittedChanges: " + status.getUncommittedChanges() + eol);
		sb.append("Untracked: " + status.getUntracked() + eol);
		sb.append("UntrackedFolders: " + status.getUntrackedFolders() + eol);
		return sb.toString();
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
		
		File ignore = new File(containingFolder, ".gitignore");
		if (!ignore.isFile())
		{
			log.debug("Creating {}", ignore.getAbsolutePath());
			Files.write(ignore.toPath(), new String("lastUser.txt\r\n").getBytes(), StandardOpenOption.CREATE_NEW);
			result.add(ignore.getName());
		}
		else
		{
			log.debug(".gitignore already exists");
			
			if (!new String(Files.readAllBytes(ignore.toPath())).contains("lastUser.txt"))
			{
				log.debug("Appending onto existing .gitignore file");
				Files.write(ignore.toPath(), new String("\r\nlastUser.txt\r\n").getBytes(), StandardOpenOption.APPEND);
				result.add(ignore.getName());
			}
		}
		return result;
	}
	
	/**
	 * @see gov.va.isaac.interfaces.sync.ProfileSyncI#substituteURL(java.lang.String, java.lang.String)
	 * 
	 * Turns 
	 *  ssh://someuser@csfe.aceworkspace.net:29418/... into
	 *  ssh://username.toString()@csfe.aceworkspace.net:29418/...
	 *  
	 *  Otherwise, returns URL.
	 */
	@Override
	public String substituteURL(String url, String username)
	{
		if (url.startsWith("ssh://") && url.contains("@"))
		{
			int index = url.indexOf("@");
			url = "ssh://" + username + url.substring(index);
		}
		return url;
	}
}
