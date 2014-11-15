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
package gov.va.isaac.sync.view;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.config.profiles.UserProfile;
import gov.va.isaac.config.profiles.UserProfileManager;
import gov.va.isaac.gui.users.CredentialsPromptDialog;
import gov.va.isaac.gui.util.CopyableLabel;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.gui.ApplicationMenus;
import gov.va.isaac.interfaces.gui.MenuItemI;
import gov.va.isaac.interfaces.gui.views.IsaacViewWithMenusI;
import gov.va.isaac.interfaces.gui.views.PopupViewI;
import gov.va.isaac.interfaces.sync.MergeFailOption;
import gov.va.isaac.interfaces.sync.MergeFailure;
import gov.va.isaac.interfaces.sync.ProfileSyncI;
import gov.va.isaac.util.Utility;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javax.inject.Singleton;
import javax.naming.AuthenticationException;
import org.apache.commons.lang3.StringUtils;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.model.cs.ChangeSetReader;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link SyncView}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Singleton
public class SyncView implements PopupViewI, IsaacViewWithMenusI
{
	private static Logger log = LoggerFactory.getLogger(SyncView.class);
	private BorderPane root_;
	private ProfileSyncI syncService_ = null;
	private ProgressBar pb_;
	private TextArea summary_;
	private String url_;
	private SimpleBooleanProperty running_ = new SimpleBooleanProperty(false);
	private boolean cancelRequested_ = false;
	String commitMessage_ = null;
	
	private SyncView()
	{
		//For HK2
	}
	
	private void initGui()
	{
		root_ = new BorderPane();
		root_.setPrefWidth(550);
		
		VBox titleBox = new VBox();
		
		Label title = new Label("Datastore Synchronization");
		title.getStyleClass().add("titleLabel");
		title.setAlignment(Pos.CENTER);
		title.setMaxWidth(Double.MAX_VALUE);
		title.setPadding(new Insets(10));
		titleBox.getChildren().add(title);
		titleBox.getStyleClass().add("headerBackground");
	
		url_ = AppContext.getAppConfiguration().getChangeSetUrl();
		String urlType = AppContext.getAppConfiguration().getChangeSetUrlTypeName();
		
		String syncUsername = ExtendedAppContext.getCurrentlyLoggedInUserProfile().getSyncUsername();
		if (StringUtils.isBlank(syncUsername))
		{
			syncUsername = ExtendedAppContext.getCurrentlyLoggedInUser();
		}
		
		url_ = syncService_.substituteURL(url_, syncUsername);
		
		Label info = new CopyableLabel("Sync using " + urlType + ": " + url_);
		info.setTooltip(new Tooltip(url_));
		
		titleBox.getChildren().add(info);
		
		titleBox.setPadding(new Insets(5, 5, 5, 5));
		root_.setTop(titleBox);
		
		
		VBox centerContent = new VBox();
		centerContent.setFillWidth(true);
		centerContent.setPrefWidth(Double.MAX_VALUE);
		centerContent.setPadding(new Insets(10));
		centerContent.getStyleClass().add("itemBorder");
		centerContent.setSpacing(10.0);
		
		
		centerContent.getChildren().add(new Label("Status:"));
		
		summary_ = new TextArea();
		summary_.setWrapText(true);
		summary_.setEditable(false);
		summary_.setMaxWidth(Double.MAX_VALUE);
		summary_.setMaxHeight(Double.MAX_VALUE);
		summary_.setPrefHeight(150.0);
		
		centerContent.getChildren().add(summary_);
		VBox.setVgrow(summary_, Priority.ALWAYS);
		
		pb_ = new ProgressBar(0.0);
		pb_.setPrefHeight(20);
		pb_.setMaxWidth(Double.MAX_VALUE);
		
		centerContent.getChildren().add(pb_);
		
		root_.setCenter(centerContent);
		
		
		//Bottom buttons
		HBox buttons = new HBox();
		buttons.setMaxWidth(Double.MAX_VALUE);
		buttons.setAlignment(Pos.CENTER);
		buttons.setPadding(new Insets(5));
		buttons.setSpacing(30);

		Button cancel = new Button("Close");
		cancel.setOnAction((action) ->
		{
			if (running_.get())
			{
				addLine("Cancelling...");
				cancel.setDisable(true);
				cancelRequested_ = true;
			}
			else
			{
				cancel.getScene().getWindow().hide();
				root_ = null;
			}
		});
		buttons.getChildren().add(cancel);

		Button action = new Button("Synchronize");
		action.disableProperty().bind(running_);
		action.setOnAction((theAction) ->
		{
			summary_.setText("");
			pb_.setProgress(-1.0);
			running_.set(true);
			Utility.execute(() -> sync());
		});
		buttons.getChildren().add(action);
		
		cancel.minWidthProperty().bind(action.widthProperty());
		
		running_.addListener(change ->
		{
			if (running_.get())
			{
				cancel.setText("Cancel");
			}
			else
			{
				cancel.setText("Close");
			}
			cancel.setDisable(false);
		});
		
		root_.setBottom(buttons);
	}
	
	private void addLine(String line)
	{
		Runnable work = new Runnable()
		{
			@Override
			public void run()
			{
				summary_.setText(summary_.getText() + line + "\n");
			}
		};
		if (Platform.isFxApplicationThread())
		{
			work.run();
		}
		else
		{
			Platform.runLater(work);
		}
	}
	
	private void sync()
	{
		cancelRequested_ = false;
		try
		{
			UserProfile up = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
			if (syncService_.isRootLocationConfiguredForSCM())
			{
				addLine("Setting Remote Address");
				
				boolean successful = false;
				while (!successful)
				{
					try
					{
						syncService_.relinkRemote(url_, up.getSyncUsername(), up.getSyncPassword());
						successful = true;
					}
					catch (AuthenticationException ae)
					{
						try
						{
							promptUserForCreds(up);
						}
						catch (InterruptedException e)
						{
							//noop
						}
						
						if (cancelRequested_)
						{
							return;
						}
					}
					catch (Exception e)
					{
						log.error("Sync failure", e);
						AppContext.getCommonDialogs().showErrorDialog("Sync Error", "Sync error setting up remote address", e.getMessage());
						return;
					}
				}
			}
			else
			{
				//Note, this operation is not expected to be run by real end users - hence, no attempt to gather a new password if necessary.
				//this is mostly for test purposes.  This is typically done during bundle build.
				addLine("Initial Sync Beginning");
				try
				{
					syncService_.linkAndFetchFromRemote(url_, up.getSyncUsername(), up.getSyncPassword());
					addLine("*****************");
					addLine("INITIAL SYNC PERFORMED - PLEASE RESTART ISAAC AFTER THIS OPERATION COMPLETES!");
				}
				catch (Exception e)
				{
					log.error("Sync failure", e);
					AppContext.getCommonDialogs().showErrorDialog("Sync Error", "Sync error performing initial sync", e.getMessage());
					return;
				}
			}
			
			if (cancelRequested_)
			{
				addLine("Cancelled");
				return;
			}
			
			Set<String> changedFiles = new HashSet<>();
			
			//recover from a previous merge fail state that didn't get resolved for whatever reason
			try
			{
				Set<String> conflictFiles = syncService_.getFilesInMergeConflict();
				if (conflictFiles.size() > 0)
				{
					MergeFailure mf = new MergeFailure(conflictFiles, new HashSet<String>());
					changedFiles.addAll(resolveMergeFailure(mf));
				}
			}
			catch (Exception e)
			{
				log.error("Sync failure", e);
				AppContext.getCommonDialogs().showErrorDialog("Sync Error", "Error checking for aborted merge conflicts", e.getMessage());
				return;
			}
			
			if (cancelRequested_)
			{
				addLine("Cancelled");
				return;
			}
			addLine("Adding new local files");
			
			try
			{
				syncService_.addUntrackedFiles();
			}
			catch (Exception e)
			{
				log.error("Sync failure", e);
				AppContext.getCommonDialogs().showErrorDialog("Sync Error", "Error adding new files", e.getMessage());
				return;
			}
			
			if (cancelRequested_)
			{
				addLine("Cancelled");
				return;
			}
			addLine("Performing remote sync");
			try
			{
				int modifiedFileCount = syncService_.getLocallyModifiedFileCount();
				addLine(modifiedFileCount + " local modifications to be sent");
				
				commitMessage_ = null;
				
				if (modifiedFileCount > 0)
				{
					CountDownLatch await = new CountDownLatch(1);
					
					
					Platform.runLater(() ->
					{
						new CommitMessage(root_.getScene().getWindow()).getMessage(result ->
						{
							commitMessage_ = result;
							await.countDown();
						});
					});
					
					await.await();
					
					if (StringUtils.isBlank(commitMessage_))
					{
						addLine("Commit message is required.  Cancelling.");
						return;
					}
				}
				
				boolean successful = false;
				while (!successful)
				{
					try
					{
						//If we didn't collect a commit message above, we had no outgoing changes - just sync down
						if (commitMessage_ == null)
						{
							//in theory, it should be impossible to get a merge failure here - but if we do - just run them through the normal 
							//resolution process.
							addLine("No local changes - checking for changes on server");
							changedFiles.addAll(syncService_.updateFromRemote(up.getSyncUsername(), up.getSyncPassword(), MergeFailOption.FAIL));
							successful = true;
						}
						else
						{
							//sync down and push back
							addLine("Sending changes and checking for changes on server");
							changedFiles.addAll(syncService_.updateCommitAndPush(commitMessage_, up.getSyncUsername(), up.getSyncPassword(), 
									MergeFailOption.FAIL, (String[])null));
							successful = true;
						}
					}
					catch (MergeFailure mf)
					{
						if (commitMessage_ == null)
						{
							//We (somehow) got a merge failure when only doing an update, even though we had no changes to push.
							//put in a commit message, so on the next loop of the code, we do a push as well, to push the resolution.
							commitMessage_ = "Merge Failure Resolution";
						}
						changedFiles.addAll(resolveMergeFailure(mf));
					}
					catch (AuthenticationException ae)
					{
						promptUserForCreds(up);
						
						if (cancelRequested_)
						{
							return;
						}
					}
				}
			}
			catch (Exception e)
			{
				log.error("Sync failure", e);
				AppContext.getCommonDialogs().showErrorDialog("Sync Error", "Error adding new files", e.getMessage());
				return;
			}
			
			
			//Process the changed files list
			addLine("Processing the changed files (" + changedFiles.size() + ")");
			StringBuilder errorsDuringProcess = new StringBuilder();
			try
			{
				AppContext.getService(BdbTerminologyStore.class).suspendChangeNotifications();
				for (String s : changedFiles)
				{
					log.debug("Post processing {} after change during sync", s);
					File f = new File(syncService_.getRootLocation(), s);
					if (f.getName().equals(UserProfileManager.PREFS_FILE_NAME) && f.getParentFile().getName().equals(ExtendedAppContext.getCurrentlyLoggedInUser()))
					{
						addLine("Rereading current user profile");
						try
						{
							AppContext.getService(UserProfileManager.class).rereadProfile();
						}
						catch (IOException e)
						{
							log.error("Error rereading changed user profile!", e);
							AppContext.getCommonDialogs().showErrorDialog("Unexpected error reading updated user profile", e);
						}
					}
					else if (f.getName().toLowerCase().endsWith(".eccs"))
					{
						try
						{
							addLine("Processing changeset " + f.getName());
							ChangeSetReader csr = new ChangeSetReader();
							csr.setChangeSetFile(f);
							Set<ConceptChronicleBI> indexedAnnotationConcepts = new HashSet<>();
							csr.read(indexedAnnotationConcepts);
							if (indexedAnnotationConcepts.size() > 0)
							{
								log.info("Dan doesn't know what to do with this after change set processing: {}", indexedAnnotationConcepts);
							}
						}
	
						catch (Exception e)
						{
							log.error("Error processing change set file " + f.getAbsolutePath(), e);
							errorsDuringProcess.append("Error processing change set file " + f.getName() + "/r");
						}
					}
					else
					{
						log.info("No processing done for changed file {}", f.getAbsolutePath());
					}
				}
			}
			finally
			{
				AppContext.getService(BdbTerminologyStore.class).resumeChangeNotifications();
			}
			if (errorsDuringProcess.length() > 0)
			{
				AppContext.getCommonDialogs().showErrorDialog("Errors processing changesets", "Errors processing changesets:", errorsDuringProcess.toString());
			}
			addLine("Syncronization complete!");
		}
		finally
		{
			Platform.runLater(() ->
			{
				pb_.setProgress(0.0);
				running_.set(false);
			});
		}
	}
	
	private void promptUserForCreds(UserProfile up) throws InterruptedException
	{
		CountDownLatch awaitCreds = new CountDownLatch(1);
		
		Platform.runLater(() ->
		{
			AppContext.getService(CredentialsPromptDialog.class).showView(up.getSyncUsername(), up.getSyncPassword(), 
					"Please provide the Sync credentials", credentials ->
			{
				if (credentials == null)
				{
					addLine("Cancelling");
					cancelRequested_ = true;
				}
				else
				{
					
					if (!up.getSyncUsername().equals(credentials.getUsername()))
					{
						try
						{
							syncService_.relinkRemote(syncService_.substituteURL(AppContext.getAppConfiguration().getChangeSetUrl(), 
									credentials.getUsername()), credentials.getUsername(), credentials.getPassword());
						}
						catch (AuthenticationException e)
						{
							log.info("Sync credentials still incorrect", e);
						}
						catch (Exception e)
						{
							//highly unlikely.. don't care, it will just fail - will work next time.
							log.error("Unexpected", e);
						}
					}
					up.setSyncUsername(credentials.getUsername());
					up.setSyncPassword(credentials.getPassword());
					
					try
					{
						AppContext.getService(UserProfileManager.class).saveChanges(up);
					}
					catch (Exception e)
					{
						//doesn't really matter - just a pw change, no big deal if we can't save it.
						log.error("Unexpected error changing profile change", e);
					}
				}
				awaitCreds.countDown();
			});
		});
		
		awaitCreds.await();
	}
	
	private Set<String> resolveMergeFailure(MergeFailure mf) throws IllegalArgumentException, IOException
	{
		Set<String> changedFiles = mf.getFilesChangedDuringMergeAttempt();
		
		CountDownLatch cdl = new CountDownLatch(1);
		HashMap<String, MergeFailOption> resolutions = new HashMap<String, MergeFailOption>();

		Platform.runLater(() ->
		{
			new ResolveConflicts(root_.getScene().getWindow(), mf.getMergeFailures(), new Consumer<HashMap<String, MergeFailOption>>()
			{
				@Override
				public void accept(HashMap<String, MergeFailOption> t)
				{
					resolutions.putAll(t);
					cdl.countDown();
				}
			});
		});
		
		try
		{
			cdl.await();
		}
		catch (InterruptedException e)
		{
			log.info("Interrupted during wait for resolutions");
		}
		
		try
		{
			syncService_.resolveMergeFailures(resolutions);
		}
		catch (MergeFailure nestedMF)
		{
			changedFiles.addAll(resolveMergeFailure(nestedMF));
		}
		
		return changedFiles;
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.PopupViewI#showView(javafx.stage.Window)
	 */
	@Override
	public void showView(Window parent)
	{
		initGui();
		Stage stage = new Stage(StageStyle.DECORATED);
		stage.initModality(Modality.NONE);
		stage.initOwner(parent);
		Scene scene = new Scene(root_);
		stage.setScene(scene);
		stage.setTitle("Datastore Synchronization");
		stage.getScene().getStylesheets().add(SyncView.class.getResource("/isaac-shared-styles.css").toString());
		stage.sizeToScene();
		stage.show();
		stage.setOnCloseRequest(windowEvent -> 
		{
			if (running_.get())
			{
				windowEvent.consume();
			}
		});
	}
	
	private void syncRequested()
	{
		//Check and see if we have a sync implementation present
		ProfileSyncI syncImpl = AppContext.getService(ProfileSyncI.class);
		if (syncImpl == null)
		{
			log.error("No implementation of ProfileSyncI is available on the classpath.  Datastore sync is not available.");
			AppContext.getCommonDialogs().showInformationDialog("Sync not installed in bundle", 
					"This ISAAC bundle has not been configured for Sync.\nPlease consult with the administrators for this bundle.");
		}
		else
		{
			String url = AppContext.getAppConfiguration().getChangeSetUrl();
			String urlType = AppContext.getAppConfiguration().getChangeSetUrlTypeName();
			
			if (StringUtils.isBlank(url) || StringUtils.isBlank(urlType))
			{
				log.error("Sync URL and/or Sync URL Type is missing or blank.  Cannot sync.");
				AppContext.getCommonDialogs().showInformationDialog("Sync not configured correctly in bundle", 
						"This ISAAC bundle has not been configured for Sync.\nPlease consult with the administrators for this bundle.");
			}
			else
			{
				syncService_ = AppContext.getService(ProfileSyncI.class, urlType);
				
				if (syncService_ == null)
				{
					log.error("Sync implementation for {} is not available. Cannot sync.", urlType);
					AppContext.getCommonDialogs().showInformationDialog("Sync not configured correctly in bundle", 
							"This ISAAC bundle has not been configured for Sync using " + urlType + ".\nPlease consult with the administrators for this bundle.");
				}
				else
				{
					syncService_.setRootLocation(AppContext.getService(UserProfileManager.class).getProfilesFolder());
					showView(AppContext.getMainApplicationWindow().getPrimaryStage());
				}
			}
		}
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.IsaacViewWithMenusI#getMenuBarMenus()
	 */
	@Override
	public List<MenuItemI> getMenuBarMenus()
	{
		ArrayList<MenuItemI> menus = new ArrayList<>();

		menus.add(new MenuItemI()
		{
			@Override
			public void handleMenuSelection(Window parent)
			{
				syncRequested();
			}

			@Override
			public int getSortOrder()
			{
				return 16;
			}

			@Override
			public String getParentMenuId()
			{
				return ApplicationMenus.ACTIONS.getMenuId();
			}

			@Override
			public String getMenuName()
			{
				return "Synchronize Datastore...";
			}

			@Override
			public String getMenuId()
			{
				return "synchronizeDatastore";
			}

			@Override
			public boolean enableMnemonicParsing()
			{
				return false;
			}

			/**
			 * @see gov.va.isaac.interfaces.gui.MenuItemI#getImage()
			 */
			@Override
			public Image getImage()
			{
				return Images.SYNC_GREEN.getImage();
			}
		});
		return menus;
	}
}
