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
package gov.va.isaac.gui.dragAndDrop;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.util.FxUtils;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.effect.Effect;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link DragRegistry}
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Singleton
public class DragRegistry
{
	private static Logger logger = LoggerFactory.getLogger(DragRegistry.class);
	private volatile long dragStartedAt = 0;
	private Set<Node> codeDropTargets = Collections.newSetFromMap(new WeakHashMap<Node, Boolean>());
	private WeakHashMap<Node, Effect> existingEffect = new WeakHashMap<Node, Effect>();

	private DragRegistry()
	{
		// created by HK2
		logger.debug("Drag Registry init");
	}

	private void addConceptDropTargetInternal(Node node)
	{
		codeDropTargets.add(node);
	}

	public void setupDragAndDrop(final ComboBox<?> n, ConceptIdProvider conceptIdProvider, boolean allowDrop)
	{
		logger.debug("Configure drag and drop for node {} - allow Drop {}", n, allowDrop);
		if (allowDrop)
		{
			addConceptDropTargetInternal(((Node) n));
			setDropShadows(n);
			n.setOnDragDropped(new EventHandler<DragEvent>()
			{
				public void handle(DragEvent event)
				{
					/* data dropped */
					Dragboard db = event.getDragboard();
					boolean success = false;
					try
					{
						if (db.hasString())
						{
							n.getEditor().setText(db.getString());
							//not sure why I have to do this...
							n.getEditor().fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, KeyEvent.CHAR_UNDEFINED, null, KeyCode.ENTER, false, false, false, false));
							n.getEditor().fireEvent(new KeyEvent(KeyEvent.KEY_RELEASED, KeyEvent.CHAR_UNDEFINED, null, KeyCode.ENTER, false, false, false, false));
							success = true;
							// It will have updated its effect upon the set - we don't want to restore an old one.
							existingEffect.remove(n);
						}
					}
					catch (Exception ex)
					{
						logger.error("Error dropping snomed concept", ex);
						AppContext.getCommonDialogs().showErrorDialog("Unexpected Error", "There was an unexpected error dropping the concept", ex.toString());
					}
					/*
					 * let the source know whether the string was successfully transferred and used
					 */
					event.setDropCompleted(success);
					event.consume();
				}
			});
		}

		n.setOnDragDetected(new DragDetectedEventHandeler(n, conceptIdProvider));
		n.setOnDragDone(new DragDoneEventHandeler());
	}

	public void setupDragAndDrop(final TextField n, ConceptIdProvider conceptIdProvider, boolean allowDrop)
	{
		logger.debug("Configure drag and drop for node {} - allow Drop {}", n, allowDrop);
		if (allowDrop)
		{
			addConceptDropTargetInternal((Node) n);
			setDropShadows(n);
			n.setOnDragDropped(new EventHandler<DragEvent>()
			{
				public void handle(DragEvent event)
				{
					/* data dropped */
					Dragboard db = event.getDragboard();
					boolean success = false;
					try
					{
						if (db.hasString())
						{
							n.setText(db.getString());
							//not sure why I have to do this...
							n.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, KeyEvent.CHAR_UNDEFINED, null, KeyCode.ENTER, false, false, false, false));
							n.fireEvent(new KeyEvent(KeyEvent.KEY_RELEASED, KeyEvent.CHAR_UNDEFINED, null, KeyCode.ENTER, false, false, false, false));
							success = true;
						}
					}
					catch (Exception ex)
					{
						logger.error("Error dropping snomed concept", ex);
						AppContext.getCommonDialogs().showErrorDialog("Unexpected Error", "There was an unexpected error dropping the concept", ex.toString());
					}
					/*
					 * let the source know whether the string was successfully transferred and used
					 */
					event.setDropCompleted(success);
					event.consume();
				}
			});
		}
		n.setOnDragDetected(new DragDetectedEventHandeler(n, conceptIdProvider));
		n.setOnDragDone(new DragDoneEventHandeler());
	}

	private void setDropShadows(final Node n)
	{
		n.setOnDragOver(new EventHandler<DragEvent>()
		{
			public void handle(DragEvent event)
			{
				logger.debug("Drag Over node {}" + n);
				/*
				 * data is dragged over the target accept it only if it is not dragged from the same node and if it has a string data
				 */
				if (event.getGestureSource() != n && event.getDragboard().hasString())
				{
					/* allow for both copying and moving, whatever user chooses */
					event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
				}
				event.consume();
			}
		});

		n.setOnDragEntered(new EventHandler<DragEvent>()
		{
			public void handle(DragEvent event)
			{
				logger.debug("Drag Entered node {}" + n);
				/* show to the user that it is an actual gesture target */
				n.setEffect(FxUtils.greenDropShadow);
				event.consume();
			}
		});

		n.setOnDragExited(new EventHandler<DragEvent>()
		{
			public void handle(DragEvent event)
			{
				logger.debug("Drag Exited node {}" + n);
				/* mouse moved away, remove the graphical cues */
				n.setEffect(FxUtils.lightGreenDropShadow);
				event.consume();
			}
		});
	}

	protected void conceptDragStarted()
	{
		logger.debug("Drag Started");
		// There is a bug in javafx with comboboxs - it seems to fire dragStarted events twice.
		// http://javafx-jira.kenai.com/browse/RT-28778
		if ((System.currentTimeMillis() - dragStartedAt) < 2000)
		{
			logger.debug("Ignoring duplicate drag event");
			return;
		}
		if (dragStartedAt > 0)
		{
			logger.warn("Unclosed drag event is still active while another was started!");
		}
		dragStartedAt = System.currentTimeMillis();
		for (Node n : codeDropTargets)
		{
			Effect existing = n.getEffect();
			if (existing != null)
			{
				existingEffect.put(n, existing);
			}
			n.setEffect(FxUtils.lightGreenDropShadow);
		}
	}

	protected void conceptDragCompleted()
	{
		logger.debug("Drag Completed");
		dragStartedAt = 0;
		for (Node n : codeDropTargets)
		{
			n.setEffect(existingEffect.remove(n));
		}
	}
}
