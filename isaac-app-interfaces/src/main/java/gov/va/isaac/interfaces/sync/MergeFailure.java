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
package gov.va.isaac.interfaces.sync;

import java.util.Set;

/**
 * {@link MergeFailure}
 * Thrown when an operation encounters a merge failure that requires user instruction to resolve.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class MergeFailure extends Exception
{
	private static final long serialVersionUID = 1L;
	private Set<String> filesWithMergeFailures_;
	private Set<String> filesChangedDuringMergeAttempt_;
	
	public MergeFailure(Set<String> filesWithMergeFailures, Set<String> filesChangedDuringMergeAttempt)
	{
		super("Merge Failure");
		filesWithMergeFailures_ = filesWithMergeFailures;
		filesChangedDuringMergeAttempt_ = filesChangedDuringMergeAttempt;
	}
	
	/**
	 * @return The files that were left in a conflicted, unusable state - much be corrected with a call to resolveMergeFailures.
	 */
	public Set<String> getMergeFailures()
	{
		return filesWithMergeFailures_;
	}
	
	/**
	 * @return All files that were changed (successfully or not) during the merge.
	 */
	public Set<String> getFilesChangedDuringMergeAttempt()
	{
		return filesChangedDuringMergeAttempt_;
	}

	/**
	 * @see java.lang.Throwable#getLocalizedMessage()
	 */
	@Override
	public String getLocalizedMessage()
	{
		return super.getLocalizedMessage() + " on " + filesWithMergeFailures_ + " while updating the files " + filesChangedDuringMergeAttempt_;
	}
}
