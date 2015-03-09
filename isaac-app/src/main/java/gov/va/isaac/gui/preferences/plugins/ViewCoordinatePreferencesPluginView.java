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

/**
 * ViewCoordinatePreferencesPluginView
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 * @author <a href="mailto:vkaloidis@apelon.com">Vas kaloidis</a>
 */
package gov.va.isaac.gui.preferences.plugins;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.config.generated.StatedInferredOptions;
import gov.va.isaac.config.profiles.UserProfile;
import gov.va.isaac.config.profiles.UserProfileDefaults;
import gov.va.isaac.config.profiles.UserProfileManager;
import gov.va.isaac.config.users.InvalidUserException;
import gov.va.isaac.gui.util.TextErrorColorHelper;
import gov.va.isaac.util.OTFUtility;
import gov.va.isaac.util.ValidBooleanBinding;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javax.inject.Singleton;
import org.apache.commons.lang3.time.DateUtils;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Position;
import org.ihtsdo.otf.tcc.api.nid.NidSet;
import org.ihtsdo.otf.tcc.api.nid.NidSetBI;
import org.ihtsdo.otf.tcc.datastore.Bdb;
import org.ihtsdo.otf.tcc.datastore.stamp.StampBdb;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ViewCoordinatePreferencesPluginView
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */

@Service
@Singleton
public class ViewCoordinatePreferencesPluginView extends CoordinatePreferencesPluginView {
	private Logger logger = LoggerFactory.getLogger(ViewCoordinatePreferencesPluginView.class);
	
	protected TreeSet<Long> times = new TreeSet<Long>();
	protected DatePicker datePicker = null;
	protected ComboBox<Long> timeSelectCombo = null;
	protected HashSet<LocalDate> pathDatesList = new HashSet<LocalDate>();
	protected Date stampDate = null;
	protected SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
	protected DateTimeFormatter dtf = DateTimeFormatter.ofPattern("d/M/y");
	protected SimpleDateFormat regularDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	protected HashMap<Long, Long> truncTimeToFullTimeMap = new HashMap<Long, Long>();
	protected SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd hh:mm:ssa");
	protected SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm:ss a");
	protected LocalDate stampDateInstant = null;
	protected Long storedTimePref = null;
	protected UUID storedPathPref = null;
	
	protected boolean datePickerFirstRun = false; //This will probably need to go
	protected boolean pathComboFirstRun = false;

	private Long overrideTimestamp;
	/**
	 * 
	 */
	public ViewCoordinatePreferencesPluginView() {
		super();
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.PreferencesPluginViewI#getName()
	 */
	@Override
	public String getName() {
		return "View Coordinate";
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.PreferencesPluginViewI#save()
	 */
	@Override
	public void save() throws IOException {
		
			logger.debug("Saving ViewCoordinatePreferencesPluginView data");
			UserProfile loggedIn = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
			
			//Path Property
			logger.debug("Setting stored VC path (currently \"{}\") to {}", loggedIn.getViewCoordinatePath(), currentPathProperty().get()); 
			loggedIn.setViewCoordinatePath(currentPathProperty().get());
			
			//Stated/Inferred Policy
			logger.debug("Setting stored VC StatedInferredPolicy (currently \"{}\") to {}", loggedIn.getStatedInferredPolicy(), currentStatedInferredOptionProperty().get()); 
			loggedIn.setStatedInferredPolicy(currentStatedInferredOptionProperty().get());
			
			//Time Coordinate Property
			logger.debug("Setting stored VC time to :" + currentViewCoordinateTimeProperty().get());
			loggedIn.setViewCoordinateTime(currentViewCoordinateTimeProperty().get());
			
			if (overrideTimestamp != null) {
				loggedIn.setViewCoordinateTime(overrideTimestamp);
			}
		try {
			AppContext.getService(UserProfileManager.class).saveChanges(loggedIn);
		} catch (InvalidUserException e) {
			String msg = "Caught " + e.getClass().getName() + " " + e.getLocalizedMessage() + " attempting to save UserProfile for " + getName();
			
			logger.error(msg, e);
			throw new IOException(msg, e);
		}
	}
	
	public static Date getEndOfDay(Date date) {
	    return DateUtils.addMilliseconds(DateUtils.ceiling(date, Calendar.DATE), -1);
	}

	public static Date getStartOfDay(Date date) {
	    return DateUtils.truncate(date, Calendar.DATE);
	}
	
	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.PreferencesPluginViewI#getRegion()
	 */
	@Override
	public Region getContent() {
		if (hBox == null) {
			VBox statedInferredToggleGroupVBox = new VBox();
			statedInferredToggleGroupVBox.setSpacing(4.0);
			
			//Instantiate Everything
			pathComboBox = new ComboBox<>(); //Path
			statedInferredToggleGroup = new ToggleGroup(); //Stated / Inferred
			List<RadioButton> statedInferredOptionButtons = new ArrayList<>();
			datePicker = new DatePicker(); //Date
			timeSelectCombo = new ComboBox<Long>(); //Time
			
			//Radio buttons
			for (StatedInferredOptions option : StatedInferredOptions.values()) {
				RadioButton optionButton = new RadioButton();
				if (option == StatedInferredOptions.STATED)
				{
					optionButton.setText("Stated");
				}
				else if (option == StatedInferredOptions.INFERRED_THEN_STATED)
				{
					optionButton.setText("Inferred Then Stated");
				}
				else if (option == StatedInferredOptions.INFERRED)
				{
					optionButton.setText("Inferred");
				}
				else
				{
					throw new RuntimeException("oops");
				}
				optionButton.setUserData(option);
				optionButton.setTooltip(new Tooltip("Default StatedInferredOption is " + getDefaultStatedInferredOption()));
				statedInferredToggleGroup.getToggles().add(optionButton);
				statedInferredToggleGroupVBox.getChildren().add(optionButton);
				statedInferredOptionButtons.add(optionButton);
			}
			statedInferredToggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
				@Override
				public void changed(
						ObservableValue<? extends Toggle> observable,
						Toggle oldValue, Toggle newValue) {
					currentStatedInferredOptionProperty.set((StatedInferredOptions)newValue.getUserData());
				}	
			});
			
			
			//Path Combo Box
			pathComboBox.setCellFactory(new Callback<ListView<UUID>, ListCell<UUID>> () {
				@Override
				public ListCell<UUID> call(ListView<UUID> param) {
					final ListCell<UUID> cell = new ListCell<UUID>() {
						@Override
						protected void updateItem(UUID c, boolean emptyRow) {
							super.updateItem(c, emptyRow);
							if(c == null) {
								setText(null);
							}else {
								String desc = OTFUtility.getDescription(c);
								setText(desc);
							}
						}
					};
					return cell;
				}
			});

			pathComboBox.setButtonCell(new ListCell<UUID>() { // Don't know why this should be necessary, but without this the UUID itself is displayed
				@Override
				protected void updateItem(UUID c, boolean emptyRow) {
					super.updateItem(c, emptyRow); 
					if (emptyRow) {
						setText("");
					} else {
						String desc = OTFUtility.getDescription(c);
						setText(desc);
					}
				}
			});
			pathComboBox.setOnAction((event)-> {
				if(!pathComboFirstRun) {
					UUID selectedPath = pathComboBox.getSelectionModel().getSelectedItem();
					if(selectedPath != null) {
						int path = OTFUtility.getConceptVersion(selectedPath).getPathNid();
						
						StampBdb stampDb = Bdb.getStampDb();
						NidSet nidSet = new NidSet();
						nidSet.add(path);
						//TODO: Make this multi-threaded and possibly implement setTimeOptions() here also
						NidSetBI stamps = stampDb.getSpecifiedStamps(nidSet, Long.MIN_VALUE, Long.MAX_VALUE); 
						
						pathDatesList.clear(); 
//						disableTimeCombo(true);
						timeSelectCombo.setValue(Long.MAX_VALUE);
					
						for(Integer thisStamp : stamps.getAsSet()) {
							try {
								Position stampPosition = stampDb.getPosition(thisStamp);
								this.stampDate = new Date(stampPosition.getTime());
								stampDateInstant = stampDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
								this.pathDatesList.add(stampDateInstant); //Build DatePicker
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						datePicker.setValue(LocalDate.now());
					}
				} else {
					pathComboFirstRun = false;
				}
			});
			
			pathComboBox.setTooltip(new Tooltip("Default path is \"" + OTFUtility.getDescription(getDefaultPath()) + "\""));
			
			//Calendar Date Picker
			final Callback<DatePicker, DateCell> dayCellFactory = 
				new Callback<DatePicker, DateCell>() {
					@Override
					public DateCell call(final DatePicker datePicker) {
						return new DateCell() {
							@Override
							public void updateItem(LocalDate thisDate, boolean empty) {
								super.updateItem(thisDate, empty);
								if(pathDatesList != null) {
									if(pathDatesList.contains(thisDate)) { 
										setDisable(false); 
									} else {
										setDisable(true);
									}
								}
							}
						};
					}
				};
			datePicker.setDayCellFactory(dayCellFactory);
			datePicker.setOnAction((event) -> {
				if(!datePickerFirstRun) {
					UUID selectedPath = pathComboBox.getSelectionModel().getSelectedItem();
					
					Instant instant = Instant.from(datePicker.getValue().atStartOfDay(ZoneId.systemDefault()));
					Long dateSelected = Date.from(instant).getTime();
					
					if(selectedPath != null && dateSelected != 0) {
						
						int path = OTFUtility.getConceptVersion(selectedPath).getPathNid();
						setTimeOptions(path, dateSelected);
						try {
							timeSelectCombo.setValue(times.first()); //Default Dropdown Value
						} catch(Exception e) {
							// Eat it.. like a sandwich! TODO: Create Read Only Property Conditional for checking if Time Combo is disabled
							// Right now, Sometimes Time Combo is disabled, so we catch this and eat it
							// Otherwise make a conditional from the Read Only Boolean Property to check first
						}
					} else {
						disableTimeCombo(false);
						logger.debug("The path isn't set or the date isn't set. Both are needed right now");
					}
				} else {
					datePickerFirstRun = false;
				}
			});
			
			//Commit-Time ComboBox
			timeSelectCombo.setMinWidth(200);
			timeSelectCombo.setCellFactory(new Callback<ListView<Long>, ListCell<Long>> () {
				@Override
				public ListCell<Long> call(ListView<Long> param) {
					final ListCell<Long> cell = new ListCell<Long>() {
						@Override
						protected void updateItem(Long item, boolean emptyRow) {
							super.updateItem(item, emptyRow);
							if(item == null) {
								setText("");
							} else {
								if(item == Long.MAX_VALUE) {
									setText("LATEST TIME");
								} else {
									setText(timeFormatter.format(new Date(item)));
								}
							}
						}
					};
					return cell;
				}
			});
			timeSelectCombo.setButtonCell(new ListCell<Long>() {
				@Override
				protected void updateItem(Long item, boolean emptyRow) {
					super.updateItem(item, emptyRow); 
					if(item == null) {
						setText("");
					} else {
						if(item == Long.MAX_VALUE) {
							setText("LATEST TIME");
						} else {
							setText(timeFormatter.format(new Date(item)));
						}
					}
				}
			});
			
			try { 
				currentPathProperty.bind(pathComboBox.getSelectionModel().selectedItemProperty()); //Set Path Property
				currentTimeProperty.bind(timeSelectCombo.getSelectionModel().selectedItemProperty());
			} catch(Exception e) {
				e.printStackTrace();
			}

			// DEFAULT VALUES
			UserProfile loggedIn = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
			storedTimePref = loggedIn.getViewCoordinateTime();
			storedPathPref = loggedIn.getViewCoordinatePath();
			
			if(storedPathPref != null) {
				pathComboBox.getItems().clear(); //Set the path Dates by default
				pathComboBox.getItems().addAll(getPathOptions());
				final UUID storedPath = getStoredPath();
				if(storedPath != null) {
					pathComboBox.getSelectionModel().select(storedPath);
				}
				
				if(storedTimePref != null) {
					final Long storedTime = loggedIn.getViewCoordinateTime();
					Calendar cal = Calendar.getInstance();
					cal.setTime(new Date(storedTime));
					cal.set(Calendar.MILLISECOND, 0); //Strip milliseconds
					Long storedTruncTime = cal.getTimeInMillis();
					
					if(!storedTime.equals(Long.MAX_VALUE)) { //***** FIX THIS, not checking default vc time value
						int path = OTFUtility.getConceptVersion(storedPathPref).getPathNid();
						setTimeOptions(path, storedTimePref);
						timeSelectCombo.setValue(storedTruncTime);
//						timeSelectCombo.getItems().addAll(getTimeOptions()); //The correct way, but doesen't work
						
						Date storedDate = new Date(storedTime);
						datePicker.setValue(storedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
					} else {
						datePicker.setValue(LocalDate.now());
						timeSelectCombo.getItems().addAll(Long.MAX_VALUE); //The correct way, but doesen't work
						timeSelectCombo.setValue(Long.MAX_VALUE);
//						disableTimeCombo(false);
					}
				} else { //Stored Time Pref == null
					logger.error("ERROR: Stored Time Preference = null");
				}
			} else { //Stored Path Pref == null
				logger.error("We could not load a stored path, ISAAC cannot run");
				throw new Error("No stored PATH could be found. ISAAC can't run without a path");
			}

			GridPane gridPane = new GridPane();
			gridPane.setHgap(10);
			gridPane.setVgap(10);
			
			Label pathLabel = new Label("View Coordinate Path");
			gridPane.add(pathLabel, 0, 0); //Path Label - Row 0
			GridPane.setHalignment(pathLabel, HPos.LEFT);
			gridPane.add(statedInferredToggleGroupVBox, 1, 0, 1, 2);  //--Row 0, span 2
			
			gridPane.add(pathComboBox, 0, 1); //Path Combo box - Row 2
			GridPane.setValignment(pathComboBox, VPos.TOP);
			
			Label datePickerLabel = new Label("View Coordinate Dates");
			gridPane.add(datePickerLabel, 0, 2); //Row 3
			GridPane.setHalignment(datePickerLabel, HPos.LEFT);
			gridPane.add(datePicker, 0, 3); //Row 4
			
			Label timeSelectLabel = new Label("View Coordinate Times");
			gridPane.add(timeSelectLabel, 1, 2); //Row 3
			GridPane.setHalignment(timeSelectLabel, HPos.LEFT);
			gridPane.add(timeSelectCombo, 1, 3); //Row 4
			
			// FOR DEBUGGING CURRENTLY SELECTED PATH, TIME AND POLICY
/*			
			UserProfile userProfile = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
			StatedInferredOptions chosenPolicy = userProfile.getStatedInferredPolicy();
			UUID chosenPathUuid = userProfile.getViewCoordinatePath();
			Long chosenTime = userProfile.getViewCoordinateTime();
			
			Label printSelectedPathLabel = new Label("Path: " + OTFUtility.getDescription(chosenPathUuid));
			gridPane.add(printSelectedPathLabel, 0, 4);
			GridPane.setHalignment(printSelectedPathLabel, HPos.LEFT);
			Label printSelectedTimeLabel = null;
			if(chosenTime != getDefaultTime()) {
				printSelectedTimeLabel = new Label("Time: " + dateFormat.format(new Date(chosenTime)));
			} else {
				printSelectedTimeLabel = new Label("Time: LONG MAX VALUE");
			}
			gridPane.add(printSelectedTimeLabel, 1, 4);
			GridPane.setHalignment(printSelectedTimeLabel, HPos.LEFT);
			Label printSelectedPolicyLabel = new Label("Policy: " + chosenPolicy);
			gridPane.add(printSelectedPolicyLabel, 2, 4);
			GridPane.setHalignment(printSelectedPolicyLabel, HPos.LEFT);
			*/
			hBox = new HBox();
			hBox.getChildren().addAll(gridPane);

			allValid_ = new ValidBooleanBinding() {
				{
					bind(currentStatedInferredOptionProperty, currentPathProperty, currentTimeProperty);
					setComputeOnInvalidate(true);
				}

				@Override
				protected boolean computeValue() {
					if (currentStatedInferredOptionProperty.get() == null) {
						this.setInvalidReason("Null/unset/unselected StatedInferredOption");
						for (RadioButton button : statedInferredOptionButtons) {
							TextErrorColorHelper.setTextErrorColor(button);
						}
						return false;
					} else {
						for (RadioButton button : statedInferredOptionButtons) {
							TextErrorColorHelper.clearTextErrorColor(button);
						}
					}
					if (currentPathProperty.get() == null) {
						this.setInvalidReason("Null/unset/unselected path");
						TextErrorColorHelper.setTextErrorColor(pathComboBox);

						return false;
					} else {
						TextErrorColorHelper.clearTextErrorColor(pathComboBox);
					}
					if (OTFUtility.getConceptVersion(currentPathProperty.get()) == null) {
						this.setInvalidReason("Invalid path");
						TextErrorColorHelper.setTextErrorColor(pathComboBox);

						return false;
					} else {
						TextErrorColorHelper.clearTextErrorColor(pathComboBox);
					}
//					if(currentTimeProperty.get() == null && currentTimeProperty.get() != Long.MAX_VALUE)
//					{
//						this.setInvalidReason("View Coordinate Time is unselected");
//						TextErrorColorHelper.setTextErrorColor(timeSelectCombo);
//						return false;
//					}
					this.clearInvalidReason();
					return true;
				}
			};
		}
//		createButton.disableProperty().bind(saveButtonValid.not()));

		
		
		// Reload persisted values every time
		final StatedInferredOptions storedStatedInferredOption = getStoredStatedInferredOption();
		for (Toggle toggle : statedInferredToggleGroup.getToggles()) {
			if (toggle.getUserData() == storedStatedInferredOption) {
				toggle.setSelected(true);
			}
		}

//		pathComboBox.setButtonCell(new ListCell<UUID>() {
//			@Override
//			protected void updateItem(UUID c, boolean emptyRow) {
//				super.updateItem(c, emptyRow); 
//				if (emptyRow) {
//					setText("");
//				} else {
//					String desc = OTFUtility.getDescription(c);
//					setText(desc);
//				}
//			}
//		});
//		timeSelectCombo.setButtonCell(new ListCell<Long>() {
//			@Override
//			protected void updateItem(Long item, boolean emptyRow) {
//				super.updateItem(item, emptyRow); 
//				if (emptyRow) {
//					setText("");
//				} else {
//					setText(timeFormatter.format(new Date(item)));
//				}
//			}
//		});
		
//		datePickerFirstRun = false;
//		pathComboFirstRun = false;
		
		return hBox;
	}
	
	
	/**
	 *  Disables the Time Combo Box by changing opacity, bg color and disabling GUI object.
	 *  
	 * @param clear boolean whether or not to clear the Time Combo Values as well as disabling it
	 */
	protected void disableTimeCombo(boolean clear) {
		timeSelectCombo.setStyle("-fx-opacity: .5; -fx-background-color: gray");
		timeSelectCombo.setDisable(true);
		if(clear) {
			timeSelectCombo.getItems().clear();
		}
		
	}
	
	/**
	 * Enables the Time Combo Box by changing opacity to full, and changing bg color to white
	 * 	as well as enabling the GUI object
	 * 
	 * @param clear boolean whether or not to clear the Time Combo Values after enabling it.
	 */
	protected void enableTimeCombo(boolean clear) {
		timeSelectCombo.setStyle("-fx-opacity: 1; -fx-background-color: white");
		timeSelectCombo.setDisable(false);
		if(clear) {
			timeSelectCombo.getItems().clear();
		}
	}
	
	/**
	 * 
	 * @param path int of the path to get the Time Options for
	 * @param storedTimePref Long of anytime during the specific day that we want to return times for
	 * @return populates the "times" TreeSet (time longs truncated at the "the seconds" position) 
	 * 			which populates Time Combo box, the truncTimeToFullTimeMap which maps the truncated times
	 * 			im times TreeSet to each times full Long value. The truncTimeToFullTimeMap chooses each time
	 * 			up to the second and maps it to the greatest equivalent time up to the milliseconds.
	 * 			
	 */
	protected void setTimeOptions(int path, Long storedTimePref) {
		try {
			timeSelectCombo.getItems().clear();
			overrideTimestamp = null;
			
			Date startDate = null, finishDate = null;
			if(storedTimePref != null) {
				StampBdb stampDb = Bdb.getStampDb();
				NidSet nidSet = new NidSet(); 
				nidSet.add(path); 
				
				NidSetBI stamps = null;
				if(!storedTimePref.equals(getDefaultTime())) {
					startDate = getStartOfDay(new Date(storedTimePref)); 
					finishDate = getEndOfDay(new Date(storedTimePref));
					stamps = stampDb.getSpecifiedStamps(nidSet, startDate.getTime(), finishDate.getTime());
				} else {
					stamps = stampDb.getSpecifiedStamps(nidSet, Long.MIN_VALUE, Long.MAX_VALUE);
				}
				
				truncTimeToFullTimeMap.clear();
				times.clear();

				HashSet<Integer> stampSet = stamps.getAsSet();
				
				Date d = new Date(storedTimePref);
				if (dateIsLocalDate(d)) {
					// Get stamps of day
					Date todayStartDate = getStartOfDay(new Date()); 
					Date todayFinishDate = getEndOfDay(new Date());
					NidSetBI todayStamps = stampDb.getSpecifiedStamps(nidSet, todayStartDate.getTime(), todayFinishDate.getTime());
					
					// If have stamps, no action, if not, show Latest and set stamps to latest stamp we have in stampset
					if (todayStamps.size() == 0) {
//						timeSelectCombo.getItems().add(Long.MAX_VALUE);
						NidSetBI allStamps = stampDb.getSpecifiedStamps(nidSet, Long.MIN_VALUE, Long.MAX_VALUE);
						HashSet<Integer> allStampSet = allStamps.getAsSet();
						SortedSet<Integer> s = new TreeSet<Integer>(allStampSet);
						if (!s.isEmpty()) {
							Integer stampToSet = s.last();
							overrideTimestamp = stampDb.getPosition(stampToSet).getTime();
							timeSelectCombo.getItems().add(Long.MAX_VALUE);
							timeSelectCombo.setValue(Long.MAX_VALUE);
						}
					}
				}
				
				this.pathDatesList.add(LocalDate.now());
				if (overrideTimestamp == null) {
					if(!stampSet.isEmpty()) {
						enableTimeCombo(true);
						for(Integer thisStamp : stampSet) {
							Long fullTime = null;
							Date stampDate;
							LocalDate stampInstant = null;
							try {
								fullTime = stampDb.getPosition(thisStamp).getTime();
								stampDate = new Date(fullTime);
								stampInstant = stampDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
							} catch (Exception e) {
								 e.printStackTrace();
							}
							Calendar cal = Calendar.getInstance();
							cal.setTime(new Date(fullTime));
							cal.set(Calendar.MILLISECOND, 0); //Strip milliseconds
							Long truncTime = cal.getTimeInMillis();
							
							this.pathDatesList.add(stampInstant); //Build DatePicker
							times.add(truncTime); //This can probably go, we don't populate hashmap like this at initialization
							timeSelectCombo.getItems().add(truncTime);
							
							if(truncTimeToFullTimeMap.containsKey(truncTime)) { //Build Truncated Time to Full Time HashMap
								//If truncTimeToFullTimeMap has this key, is the value the newest time in milliseconds?
								if(new Date(truncTimeToFullTimeMap.get(truncTime)).before(new Date(fullTime))) {
									truncTimeToFullTimeMap.put(truncTime, fullTime);
								}
							} else {
								truncTimeToFullTimeMap.put(truncTime, fullTime);
							}
						}
					} else {
	//					disableTimeCombo(true);
//						timeSelectCombo.getItems().add(Long.MAX_VALUE);
						timeSelectCombo.setValue(Long.MAX_VALUE);
						enableTimeCombo(true);
	//					logger.error("Could not retreive any Stamps");
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error setting the default Time Dropdown");
			e.printStackTrace();
		}
	}

	private boolean dateIsLocalDate(Date d) {
		Month ldMonth = LocalDate.now().atStartOfDay().getMonth();
		int ldDate = LocalDate.now().atStartOfDay().getDayOfMonth();
		int ldYear = LocalDate.now().atStartOfDay().getYear();
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);

		if (cal.get(Calendar.YEAR) == ldYear &&
			cal.get(Calendar.DAY_OF_MONTH) == ldDate &&
			cal.get(Calendar.MONTH) == (ldMonth.getValue() -1)) {
			return true;
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.gui.preferences.plugins.CoordinatePreferencesPluginView#getPathOptions()
	 */
	@Override
	protected Collection<UUID> getPathOptions() {
		List<UUID> list = new ArrayList<>();

		try {
			List<ConceptChronicleBI> pathConcepts = OTFUtility.getPathConcepts();
			for (ConceptChronicleBI cc : pathConcepts) {
				list.add(cc.getPrimordialUuid());
			}
		} catch (IOException | ContradictionException e) {
			logger.error("Failed loading path concepts. Caught {} {}", e.getClass().getName(), e.getLocalizedMessage());
			e.printStackTrace();
		}
		// Add currently-stored value to list of options, if not already there
		UUID storedPath = getStoredPath();
		if (storedPath != null && ! list.contains(storedPath)) {
			list.add(storedPath);
		}

		//Collections.sort(list);
		return list;
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.gui.preferences.plugins.CoordinatePreferencesPluginView#getStoredTime()
	 */
	@Override
	protected Long getStoredTime() {
		UserProfile loggedIn = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
		return loggedIn.getViewCoordinateTime();
	}
	
	/* (non-Javadoc)
	 * @see gov.va.isaac.gui.preferences.plugins.CoordinatePreferencesPluginView#getStoredPath()
	 */
	@Override
	protected UUID getStoredPath() {
		UserProfile loggedIn = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
		return loggedIn.getViewCoordinatePath();
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.gui.preferences.plugins.CoordinatePreferencesPluginView#getStoredStatedInferredOption()
	 */
	@Override
	protected StatedInferredOptions getStoredStatedInferredOption() {
		UserProfile loggedIn = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
		return loggedIn.getStatedInferredPolicy();
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.gui.preferences.plugins.CoordinatePreferencesPluginView#getDefaultPath()
	 */
	@Override
	protected UUID getDefaultPath() {
		return UserProfileDefaults.getDefaultViewCoordinatePath();
	}
	
	/* (non-Javadoc)
	 * @see gov.va.isaac.gui.preferences.plugins.CoordinatePreferencesPluginView#getDefaultPath()
	 */
	@Override
	protected Long getDefaultTime() {
		return Long.MAX_VALUE;
//		return UserProfileDefaults.getDefaultViewCoordinateTime();
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.gui.preferences.plugins.CoordinatePreferencesPluginView#getDefaultStatedInferredOption()
	 */
	@Override
	protected StatedInferredOptions getDefaultStatedInferredOption() {
		return UserProfileDefaults.getDefaultStatedInferredPolicy();
	}
	
	/**
	 * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.PreferencesPluginViewI#getTabOrder()
	 */
	@Override
	public int getTabOrder()
	{
		return 10;
	}

}
