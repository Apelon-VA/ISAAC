/**
 * Copyright 2013
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
package gov.va.legoEdit.storage;

import java.util.Iterator;

/**
 * An iterator that allows callers to call close() when they are finished with the iterator,
 * so that the data source for the iterator can release the underlying resources.
 * 
 * A runtime IteratorClosedException is thrown if you try to read a closed iterator.
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public interface CloseableIterator<E> extends Iterator<E>
{
	/**
	 * Clients of the iterator should call close() to release resources in the case where they
	 * don't iterate through all of the items in the iterator.
	 */
	public void close();

	@Override
	public E next() throws IteratorClosedException;

	@Override
	public boolean hasNext() throws IteratorClosedException;
}
