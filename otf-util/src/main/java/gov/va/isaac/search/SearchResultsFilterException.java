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
 * SearchResultsFilterException
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.search;

import java.util.List;
import java.util.function.Function;

/**
 * SearchResultsFilterException
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public class SearchResultsFilterException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final Function<List<CompositeSearchResult>, List<CompositeSearchResult>> failedFilter;
	/**
	 * 
	 */
	public SearchResultsFilterException(Function<List<CompositeSearchResult>, List<CompositeSearchResult>> failedFilter) {
		this.failedFilter = failedFilter;
	}

	/**
	 * @param message
	 */
	public SearchResultsFilterException(Function<List<CompositeSearchResult>, List<CompositeSearchResult>> failedFilter, String message) {
		super(message);
		this.failedFilter = failedFilter;
	}

	/**
	 * @param cause
	 */
	public SearchResultsFilterException(SearchResultsFilterException cause) {
		super(cause);
		this.failedFilter = cause.failedFilter;
	}
	/**
	 * @param cause
	 */
	public SearchResultsFilterException(Function<List<CompositeSearchResult>, List<CompositeSearchResult>> failedFilter, Throwable cause) {
		super(cause);
		this.failedFilter = failedFilter;
	}

	/**
	 * @param message
	 * @param cause
	 */
	public SearchResultsFilterException(Function<List<CompositeSearchResult>, List<CompositeSearchResult>> failedFilter, String message, Throwable cause) {
		super(message, cause);
		this.failedFilter = failedFilter;
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public SearchResultsFilterException(
			Function<List<CompositeSearchResult>, List<CompositeSearchResult>> failedFilter,
			String message, 
			Throwable cause,
			boolean enableSuppression, 
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.failedFilter = failedFilter;
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public SearchResultsFilterException(
			String message, 
			SearchResultsFilterException cause,
			boolean enableSuppression, 
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.failedFilter = cause.failedFilter;
	}
}
