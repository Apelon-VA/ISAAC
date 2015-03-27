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
package gov.va.isaac.refexDynamic;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import javafx.application.Platform;
import javafx.scene.control.ProgressIndicator;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptFetcherBI;
import org.ihtsdo.otf.tcc.api.concept.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.otf.tcc.datastore.Bdb;
/**
 * 
 * {@link RefexAnnotationSearcher}
 * @author jefron
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefexAnnotationSearcher implements ProcessUnfetchedConceptDataBI
{
	private volatile boolean continue_ = true;
	private Set<RefexDynamicChronicleBI<?>> refexResults_ = new ConcurrentSkipListSet<>();
	private Function<RefexDynamicChronicleBI<?>, Boolean> matchFunction_;
	private int totalToProcess_;
	private AtomicInteger totalProcessed_ = new AtomicInteger();
	private ProgressIndicator progressIndicator_;

	public RefexAnnotationSearcher(Function<RefexDynamicChronicleBI<?>, Boolean> matchFunction, ProgressIndicator indicatorToUpdate) throws IOException
	{
		matchFunction_ = matchFunction;
		totalToProcess_ = Bdb.getConceptDb().getCount();
		progressIndicator_ = indicatorToUpdate;
	}
	
	/**
	 * Call this to request the background processing threads cease
	 */
	public void requestStop()
	{
		continue_ = false;
	}

	@Override
	public boolean continueWork()
	{
		return continue_;
	}

	@Override
	public boolean allowCancel()
	{
		return true;
	}

	@Override
	public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher) throws Exception
	{
		if (progressIndicator_ != null)
		{
			int count = totalProcessed_.incrementAndGet();
			if (count % 1000 == 0)
			{
				Platform.runLater(() -> {progressIndicator_.setProgress((double)count / (double)totalToProcess_);});
			}
		}
		ConceptChronicleBI cc = fetcher.fetch();
		for (RefexDynamicChronicleBI<?> annot : cc.getRefexDynamicAnnotations())
		{
			process(annot);
		}
		for (DescriptionChronicleBI desc : cc.getDescriptions())
		{
			for (RefexDynamicChronicleBI<?> annot : desc.getRefexDynamicAnnotations())
			{
				process(annot);
			}
		}
		for (RelationshipChronicleBI rels : cc.getRelationshipsOutgoing())
		{
			for (RefexDynamicChronicleBI<?> annot : rels.getRefexDynamicAnnotations())
			{
				process(annot);
			}
		}
	}
	
	private void process(RefexDynamicChronicleBI<?> refex) throws IOException
	{
		if (matchFunction_.apply(refex))
		{
			refexResults_.add(refex);
		}
		for (RefexDynamicChronicleBI<?> nested : refex.getRefexDynamicAnnotations())
		{
			process(nested);
		}
	}

	@Override
	public NativeIdSetBI getNidSet() throws IOException
	{
		return null;
	}

	@Override
	public String getTitle()
	{
		return "Find concepts with sememe annotation as specified";
	}

	/**
	 * Return the results.  Will not return null
	 * @return
	 */
	public Set<RefexDynamicChronicleBI<?>> getResults()
	{
		return refexResults_;
	}

}