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
package gov.va.isaac.gui;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import javax.inject.Singleton;
import org.ihtsdo.otf.tcc.model.index.service.IndexStatusListenerBI;
import org.ihtsdo.otf.tcc.model.index.service.IndexerBI;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link IndexStatusListener}
 *
 * A simple service that lets any ISAAC code tie in an action to index change events.
 * 
 * Simply pass a consumer into one of the on* methods.
 * 
 * Make sure you maintain a reference to the consumer - this class uses weak references, and 
 * will not hold them from the garbage collector.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Singleton
public class IndexStatusListener implements IndexStatusListenerBI
{
	private static final Logger LOG = LoggerFactory.getLogger(IndexStatusListener.class);

	Set<Consumer<IndexerBI>> callbackOnIndexConfigChanged = Collections.newSetFromMap(new WeakHashMap<Consumer<IndexerBI>, Boolean>());
	Set<Consumer<IndexerBI>> callbackOnReindexStarted = Collections.newSetFromMap(new WeakHashMap<Consumer<IndexerBI>, Boolean>());
	Set<Consumer<IndexerBI>> callbackOnReindexComplete = Collections.newSetFromMap(new WeakHashMap<Consumer<IndexerBI>, Boolean>());

	private IndexStatusListener()
	{
		//for HK2
	}

	/**
	 * This uses weak references - you must hold onto your own consumer reference.
	 * @return the passed in reference, for convenience.
	 */
	public Consumer<IndexerBI> onIndexConfigChanged(Consumer<IndexerBI> callback)
	{
		callbackOnIndexConfigChanged.add(callback);
		return callback;
	}
	
	/**
	 * This uses weak references - you must hold onto your own consumer reference.
	 * @return the passed in reference, for convenience.
	 */
	public void onReindexStarted(Consumer<IndexerBI> callback)
	{
		callbackOnReindexStarted.add(callback);
	}
	
	/**
	 * This uses weak references - you must hold onto your own consumer reference.
	 * @return the passed in reference, for convenience.
	 */
	public void onReindexCompleted(Consumer<IndexerBI> callback)
	{
		callbackOnReindexComplete.add(callback);
	}

	/**
	 * @see org.ihtsdo.otf.tcc.model.index.service.IndexStatusListenerBI#indexConfigurationChanged(org.ihtsdo.otf.tcc.model.index.service.IndexerBI)
	 */
	@Override
	public void indexConfigurationChanged(IndexerBI indexConfigurationThatChanged)
	{
		LOG.info("Index config changed {}", indexConfigurationThatChanged.getIndexerName());
		for (Consumer<IndexerBI> callback : callbackOnIndexConfigChanged)
		{
			callback.accept(indexConfigurationThatChanged);
		}
	}

	/**
	 * @see org.ihtsdo.otf.tcc.model.index.service.IndexStatusListenerBI#reindexBegan(org.ihtsdo.otf.tcc.model.index.service.IndexerBI)
	 */
	@Override
	public void reindexBegan(IndexerBI index)
	{
		LOG.info("Reindex Began {}", index.getIndexerName());
		for (Consumer<IndexerBI> callback : callbackOnReindexStarted)
		{
			callback.accept(index);
		}
	}

	/**
	 * @see org.ihtsdo.otf.tcc.model.index.service.IndexStatusListenerBI#reindexCompleted(org.ihtsdo.otf.tcc.model.index.service.IndexerBI)
	 */
	@Override
	public void reindexCompleted(IndexerBI index)
	{
		LOG.info("Reindex Completed {}", index.getIndexerName());
		for (Consumer<IndexerBI> callback : callbackOnReindexComplete)
		{
			callback.accept(index);
		}
	}
}