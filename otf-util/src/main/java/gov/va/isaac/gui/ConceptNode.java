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

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.dragAndDrop.ConceptIdProvider;
import gov.va.isaac.gui.dragAndDrop.DragRegistry;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.util.CommonlyUsedConcepts;
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
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
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
	private AtomicInteger lookupsCurrentlyInProgress_ = new AtomicInteger();
	private BooleanBinding isLookupInProgress_ = new BooleanBinding()
	{
		@Override
		protected boolean computeValue()
		{
			return lookupsCurrentlyInProgress_.get() > 0;
		}
	};
	
	private ListChangeListener<SimpleDisplayConcept> listChangeListener_;
	private volatile boolean disableChangeListener_ = false;

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
				return object == null ? "" : object.getDescription();
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
		//We can't simply use the ObservableList from the CommonlyUsedConcepts, because it infinite loops - there doesn't seem to be a way 
		//to change the items in the drop down without changing the selection.  So, we have this hack instead.
		ObservableList<SimpleDisplayConcept> items = AppContext.getService(CommonlyUsedConcepts.class).getObservableConcepts();
		listChangeListener_ = new ListChangeListener<SimpleDisplayConcept>()
		{
			@Override
			public void onChanged(Change<? extends SimpleDisplayConcept> c)
			{
				logger.debug("updating recently used dropdown");
				disableChangeListener_ = true;
				SimpleDisplayConcept temp = cb_.getValue();
				cb_.setItems(FXCollections.observableArrayList(AppContext.getService(CommonlyUsedConcepts.class).getObservableConcepts()));
				cb_.setValue(temp);
				cb_.getSelectionModel().select(temp);
				disableChangeListener_ = false;
			}
		};
		
		items.addListener(new WeakListChangeListener<SimpleDisplayConcept>(listChangeListener_));
		
		cb_.setItems(FXCollections.observableArrayList(items));
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
				if (newValue == null)
				{
					logger.debug("Combo Value Changed - null entry");
				}
				else
				{
					logger.debug("Combo Value Changed: {} {}", newValue.getDescription(), newValue.getNid());
				}
				
				if (disableChangeListener_)
				{
					logger.debug("change listener disabled");
					return;
				}
				if (newValue.shouldIgnoreChange())
				{
					logger.debug("One time change ignore");
					return;
				}
				//Whenever the focus leaves the combo box editor, a new combo box is generated.  But, the new box will have 0 for an id.  detect and ignore
				if (oldValue.getDescription().equals(newValue.getDescription()) && newValue.getNid() == 0)
				{
					logger.debug("Not a real change, ignore");
					newValue.setNid(oldValue.getNid());
					return;
				}
				lookup();
			}
		});

		AppContext.getService(DragRegistry.class).setupDragAndDrop(cb_, new ConceptIdProvider()
		{
			@Override
			public String getConceptId()
			{
				return cb_.getValue().getNid() + "";
			}
		}, true);
		
		pi_ = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
		pi_.visibleProperty().bind(isLookupInProgress_);
		pi_.setPrefHeight(16.0);
		pi_.setPrefWidth(16.0);
		pi_.setMaxWidth(16.0);
		pi_.setMaxHeight(16.0);

		lookupFailImage_ = Images.EXCLAMATION.createImageView();
		lookupFailImage_.visibleProperty().bind(isValid.not().and(isLookupInProgress_.not()));
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

	private synchronized void lookup()
	{
		lookupsCurrentlyInProgress_.incrementAndGet();
		isLookupInProgress_.invalidate();
		if (cb_.getValue().getNid() != 0)
		{
			WBUtility.getConceptVersion(cb_.getValue().getNid(), this, null);
		}
		else
		{
			WBUtility.lookupIdentifier(cb_.getValue().getDescription(), this, null);
		}
	}

	public Node getNode()
	{
		return hbox_;
	}

	public ConceptVersionBI getConcept()
	{
		if (isLookupInProgress_.get())
		{
			synchronized (lookupsCurrentlyInProgress_)
			{
				while (lookupsCurrentlyInProgress_.get() > 0)
				{
					try
					{
						lookupsCurrentlyInProgress_.wait();
					}
					catch (InterruptedException e)
					{
						// noop
					}
				}
			}
		}
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
				synchronized (lookupsCurrentlyInProgress_)
				{
					lookupsCurrentlyInProgress_.decrementAndGet();
					isLookupInProgress_.invalidate();
					lookupsCurrentlyInProgress_.notifyAll();
				}
				
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
					AppContext.getService(CommonlyUsedConcepts.class).addConcept(new SimpleDisplayConcept(c_));
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
