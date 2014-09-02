package gov.va.isaac.ie.exporter;

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.models.util.CommonBase;
import gov.va.isaac.util.WBUtility;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.ihtsdo.otf.tcc.api.concept.ConceptFetcherBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.dto.ChronicleConverter;
import org.ihtsdo.otf.tcc.dto.TtkConceptChronicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EConceptExporter extends CommonBase implements ProcessUnfetchedConceptDataBI {

    private static final Logger LOG = LoggerFactory.getLogger(EConceptExporter.class);

    private static BdbTerminologyStore dataStore = ExtendedAppContext.getDataStore();

	private DataOutputStream dos;

	private int count = 0;

	private int pathNid;

	public EConceptExporter(FileOutputStream fileOutputStream) {
		dos = new DataOutputStream(new BufferedOutputStream(fileOutputStream));
	}

	public void export(int pathNid) throws Exception {
		this.pathNid = pathNid;
		dataStore.iterateConceptDataInSequence(this);
		dos.flush();
		dos.close();
		LOG.info("Wrote " + count + " concepts.");
	}

	@Override
	public boolean continueWork() {
		return true;
	}

	@Override
	public boolean allowCancel() {
		return false;
	}

	@Override
	public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher)
			throws Exception {
		ConceptVersionBI concept = (ConceptVersionBI) fetcher.fetch(WBUtility.getViewCoordinate());
		if(concept.getPathNid() == pathNid) {
			count++;
			TtkConceptChronicle converted = ChronicleConverter.convert(concept);
			converted.writeExternal(dos);
		}
	}

	@Override
	public NativeIdSetBI getNidSet() throws IOException {
		return dataStore.getAllConceptNids();
	}

	@Override
	public String getTitle() {
		return this.getClass().getName();
	}

}
