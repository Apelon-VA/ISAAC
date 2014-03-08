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
package gov.va.isaac.gui;

import gov.va.isaac.gui.util.Images;
import gov.va.isaac.util.ConceptLookupCallback;
import gov.va.isaac.util.WBUtility;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ConceptNode}
 * 
 *  This class handles the GUI display of concepts with many other useful tidbits, 
 *  such as allowing users to enter UUIDs, SCTIDs, NIDS, or do type ahead searches.
 *  
 *  Validation lookups are background threaded.
 *  
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class ConceptNode implements ConceptLookupCallback
{
	private static Logger logger = LoggerFactory.getLogger(ConceptNode.class);

	private HBox hbox_;
	private ComboBox<SimpleDisplayConcept> cb_;
	private ProgressIndicator pi_;
	private ImageView lookupFailImage_;
	private ConceptVersionBI c_;
	private ObjectBinding<ConceptVersionBI> conceptBinding_;
	private SimpleDisplayConcept codeSetComboBoxConcept_ = null;
	private BooleanProperty isValid = new SimpleBooleanProperty(true);
	private StringProperty invalidToolTipText = new SimpleStringProperty("The specified concept was not found in the database.");
	private boolean flagAsInvalidWhenBlank_ = true;
	private volatile long lookupUpdateTime_ = 0;
	private AtomicInteger lookupsInProgress_ = new AtomicInteger();
	private BooleanBinding lookupInProgress = new BooleanBinding()
	{
		@Override
		protected boolean computeValue()
		{
			return lookupsInProgress_.get() > 0;
		}
	};

	public ConceptNode(ConceptVersionBI initialConcept, boolean flagAsInvalidWhenBlank)
	{
		c_ = initialConcept;
		conceptBinding_ = new ObjectBinding<ConceptVersionBI>()
		{

			@Override
			protected ConceptVersionBI computeValue()
			{
				return c_;
			}
		};
		
		flagAsInvalidWhenBlank_ = flagAsInvalidWhenBlank;
		cb_ = new ComboBox<>();
		cb_.setConverter(new StringConverter<SimpleDisplayConcept>()
		{
			@Override
			public String toString(SimpleDisplayConcept object)
			{
				return object.getDescription();
			}

			@Override
			public SimpleDisplayConcept fromString(String string)
			{
				return new SimpleDisplayConcept(string, 0);
			}
		});
		cb_.setEditable(true);
		cb_.setMaxWidth(Double.MAX_VALUE);
		cb_.setPrefWidth(ComboBox.USE_COMPUTED_SIZE);
		cb_.setMinWidth(200.0);
		cb_.setPromptText("Drop or select a concept");
		//TODO add a recently-used list of concepts API
		//cb_.setItems(FXCollections.observableArrayList(LegoGUI.getInstance().getLegoGUIController().getCommonlyUsedConcept().getSuggestions(cut)));
		cb_.setVisibleRowCount(11);

		updateGUI();
		
		new LookAheadConceptPopup(cb_);

		if (cb_.getValue().getNid() == 0 && flagAsInvalidWhenBlank_)
		{
			isValid.set(false);
			invalidToolTipText.set("Concept Required");
		}
		else
		{
			isValid.set(true);
		}

		cb_.valueProperty().addListener(new ChangeListener<SimpleDisplayConcept>()
		{
			@Override
			public void changed(ObservableValue<? extends SimpleDisplayConcept> observable, SimpleDisplayConcept oldValue, SimpleDisplayConcept newValue)
			{
				logger.debug("Combo Value Changed: {} {}", newValue.getDescription(), newValue.getNid());
				if (newValue.shouldIgnoreChange())
				{
					return;
				}
				//Whenever the focus leaves the combo box editor, a new combo box is generated.  But, the new box will have 0 for an id.  detect and ignore
				if (oldValue.getDescription().equals(newValue.getDescription()) && newValue.getNid() == 0)
				{
					return;
				}
				lookup();
			}
		});

		//TODO add drag and drop hooks
		//LegoGUI.getInstance().getLegoGUIController().addSnomedDropTarget(legoTreeView_.getLego(), cb_);

		pi_ = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
		pi_.visibleProperty().bind(lookupInProgress);
		pi_.setPrefHeight(16.0);
		pi_.setPrefWidth(16.0);
		pi_.setMaxWidth(16.0);
		pi_.setMaxHeight(16.0);

		lookupFailImage_ = Images.EXCLAMATION.createImageView();
		lookupFailImage_.visibleProperty().bind(isValid.not().and(lookupInProgress.not()));
		Tooltip t = new Tooltip();
		t.textProperty().bind(invalidToolTipText);
		Tooltip.install(lookupFailImage_, t);

		StackPane sp = new StackPane();
		sp.setMaxWidth(Double.MAX_VALUE);
		sp.getChildren().add(cb_);
		sp.getChildren().add(lookupFailImage_);
		sp.getChildren().add(pi_);
		StackPane.setAlignment(cb_, Pos.CENTER_LEFT);
		StackPane.setAlignment(lookupFailImage_, Pos.CENTER_RIGHT);
		StackPane.setMargin(lookupFailImage_, new Insets(0.0, 30.0, 0.0, 0.0));
		StackPane.setAlignment(pi_, Pos.CENTER_RIGHT);
		StackPane.setMargin(pi_, new Insets(0.0, 30.0, 0.0, 0.0));

		hbox_ = new HBox();
		hbox_.setSpacing(5.0);
		hbox_.setAlignment(Pos.CENTER_LEFT);

		hbox_.getChildren().add(sp);
		HBox.setHgrow(sp, Priority.SOMETIMES);
		//TODO drag and drop
//		cb_.getEditor().setOnDragDetected(new EventHandler<MouseEvent>()
//		{
//			public void handle(MouseEvent event)
//			{
//				/* drag was detected, start a drag-and-drop gesture */
//				/* allow any transfer mode */
//				if (c_ != null)
//				{
//					Dragboard db = cb_.startDragAndDrop(TransferMode.COPY);
//
//					/* Put a string on a dragboard */
//					String drag = null;
//					if (c_.getUuid() != null)
//					{
//						drag = c_.getUuid();
//					}
//					else if (c_.getSctid() != null)
//					{
//						drag = c_.getSctid() + "";
//					}
//					if (drag != null)
//					{
//						ClipboardContent content = new ClipboardContent();
//						content.putString(drag);
//						db.setContent(content);
//						LegoGUI.getInstance().getLegoGUIController().snomedDragStarted();
//						event.consume();
//					}
//				}
//			}
//		});
//
//		cb_.getEditor().setOnDragDone(new EventHandler<DragEvent>()
//		{
//			public void handle(DragEvent event)
//			{
//				LegoGUI.getInstance().getLegoGUIController().snomedDragCompleted();
//			}
//		});
	}

	private void updateGUI()
	{
		logger.debug("update gui - is concept null? {}", c_ == null);
		if (c_ == null)
		{
			//Keep the user entry, if it was invalid, so they can edit it.
			codeSetComboBoxConcept_ = new SimpleDisplayConcept((cb_.getValue() != null ? cb_.getValue().getDescription() : ""), 0, true);
			cb_.setTooltip(null);
		}
		else
		{
			codeSetComboBoxConcept_ = new SimpleDisplayConcept(WBUtility.getDescription(c_), c_.getNid(), true);
			
			//In case the description is too long, also put it in a tooltip
			Tooltip t = new Tooltip(codeSetComboBoxConcept_.getDescription());
			cb_.setTooltip(t);
		}
		cb_.setValue(codeSetComboBoxConcept_);
	}

	/**
	 * returns true if launched, false if skipped because it decided it wasn't necessary
	 */
	private synchronized void lookup()
	{
		lookupsInProgress_.incrementAndGet();
		lookupInProgress.invalidate();
		WBUtility.getConceptVersion(cb_.getValue().getNid(), this, null);
	}

	public Node getNode()
	{
		return hbox_;
	}

	public ConceptVersionBI getConcept()
	{
		//TODO this should block if a lookup is in progress
		return c_;
	}
	
	protected String getDisplayedText()
	{
		return cb_.getValue().getDescription();
	}

	protected void set(String newValue)
	{
		cb_.setValue(new SimpleDisplayConcept(newValue, 0));
	}
	
	public BooleanProperty isValid()
	{
		return isValid;
	}

	@Override
	public void lookupComplete(final ConceptVersionBI concept, final long submitTime, Integer callId)
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				logger.debug("lookupComplete - found '{}'", (concept == null  ? "-null-" : concept.toUserString()));
				lookupsInProgress_.decrementAndGet();
				lookupInProgress.invalidate();

				if (submitTime < lookupUpdateTime_)
				{
					// Throw it away, we already got back a newer lookup.
					logger.debug("throwing away a lookup");
					return;
				}
				else
				{
					lookupUpdateTime_ = submitTime;
				}

				if (concept != null)
				{
					c_ = concept;
					//TODO recent codes api
					//LegoGUI.getInstance().getLegoGUIController().updateRecentCodes(c_);
					isValid.set(true);
				}
				else
				{
					// lookup failed
					c_ = null;
					if (StringUtils.isNotBlank(cb_.getValue().getDescription()))
					{
						isValid.set(false);
						invalidToolTipText.set("The specified concept was not found in the database");
					}
					else if (flagAsInvalidWhenBlank_)
					{
						isValid.set(false);
						invalidToolTipText.set("Concept required");
					}
					else
					{
						isValid.set(true);
					}
				}
				updateGUI();
				conceptBinding_.invalidate();
			}
		});
	}
	
	public void setPromptText(String promptText)
	{
		cb_.setPromptText(promptText);
	}
	
	public ObjectBinding<ConceptVersionBI> getConceptProperty()
	{
		return conceptBinding_;
	}
	
	public void clear()
	{
		logger.debug("Clear called");
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				cb_.setValue(new SimpleDisplayConcept("", 0));
			}
		});
	}
}
