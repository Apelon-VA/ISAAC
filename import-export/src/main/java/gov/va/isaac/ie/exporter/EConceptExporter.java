package gov.va.isaac.ie.exporter;

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.config.generated.StatedInferredOptions;
import gov.va.isaac.config.profiles.UserProfile;
import gov.va.isaac.models.util.CommonBase;
import gov.va.isaac.util.ProgressEvent;
import gov.va.isaac.util.ProgressListener;
import gov.va.isaac.util.OTFUtility;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptFetcherBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.otf.tcc.api.coordinate.Position;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.dto.ChronicleConverter;
import org.ihtsdo.otf.tcc.dto.TtkConceptChronicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for export to file in eConcept format.
 *
 * @author tnaing
 * @author bcarlsen
 */
public class EConceptExporter extends CommonBase implements
    Exporter, ProcessUnfetchedConceptDataBI {

	ViewCoordinate vc;
	
  /** Listeners */
  private List<ProgressListener> listeners = new ArrayList<>();

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory
      .getLogger(EConceptExporter.class);

  /** The data store. */
  private static BdbTerminologyStore dataStore = ExtendedAppContext
      .getDataStore();

  /** The dos. */
  private DataOutputStream dos;

  /** The count. */
  private int count = 0;

  /** The all concepts count. */
  private int allCount = 0;

  /** The path nid. */
  private int pathNid;

  /** the count so far */
  private int progress = 0;

  /** the toal */
  private int progressMax = 0;

  /**  The request cancel. */
  private boolean requestCancel = false;
  
  /**
   * Instantiates a {@link EConceptExporter} from the specified parameters.
   *
   * @param fileOutputStream the file output stream
   */
  public EConceptExporter(OutputStream fileOutputStream) {
    dos = new DataOutputStream(new BufferedOutputStream(fileOutputStream));
  }

  /**
   * Export.
   *
   * @param pathNid the path nid
   * @throws Exception the exception
   */
  @Override
  public void export(int pathNid) throws Exception {
    this.pathNid = pathNid;
    vc = makeViewCoordinate(pathNid);
    dataStore.iterateConceptDataInSequence(this);
    
    dos.flush();
    dos.close();
    LOG.info("Wrote " + count + " concepts.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.tcc.api.ContinuationTrackerBI#continueWork()
   */
  @Override
  public boolean continueWork() {
    return !requestCancel;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.tcc.api.concept.ProcessUnfetchedConceptDataBI#allowCancel()
   */
  @Override
  public boolean allowCancel() {
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.tcc.api.concept.ProcessUnfetchedConceptDataBI#
   * processUnfetchedConceptData(int,
   * org.ihtsdo.otf.tcc.api.concept.ConceptFetcherBI)
   */
  @SuppressWarnings("cast")
  @Override
  public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher)
    throws Exception {
    ConceptVersionBI concept = fetcher.fetch(vc);
    allCount++;
    if (LOG.isDebugEnabled())
    {
      if (concept.getPrimordialUuid().toString().equals("")){
        LOG.debug("Found a concept with no primoridial UUID: {}", concept);
        return;
      }
    }
    if (Exporter.isQualifying(concept.getNid(), pathNid, vc)) {
      count++;
      TtkConceptChronicle converted = ChronicleConverter.convert(concept);
      converted.writeExternal(dos);
    }
    // Handle progress monitor
    if ((int) ((allCount * 100) / progressMax) > progress) {
      progress = (allCount * 100) / progressMax;
      fireProgressEvent(progress, progress + " % finished");
    }
  }
  
	public static ViewCoordinate makeViewCoordinate(int pathInput) {
		ViewCoordinate vc = null;
		try {
			vc = StandardViewCoordinates.getSnomedInferredThenStatedLatest();

			//LOG.info("Using {} policy for view coordinate", policy);

			final Long time = Long.MAX_VALUE;
			final ConceptChronicleBI pathChronicle = dataStore.getConcept(pathInput);
			final int pathNid = pathChronicle.getNid();

			// Start with standard view coordinate and override the path setting to
			// use the preferred path
			Position position = dataStore.newPosition(dataStore.getPath(pathNid), time);

			vc.setViewPosition(position);

			//LOG.info("Using ViewCoordinate policy={}, path nid={}, uuid={}, desc={}", policy, pathNid, pathUuid, OTFUtility.getDescription(pathChronicle));
		} catch (NullPointerException e) {
			LOG.error("View path UUID does not exist", e);
		} catch (IOException e) {
			LOG.error("Unexpected error fetching view coordinates!", e);
		}

		return vc;
	}

  public int getCount() {
	  return count;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.tcc.api.concept.ProcessUnfetchedConceptDataBI#getNidSet()
   */
  @Override
  public NativeIdSetBI getNidSet() throws IOException {
    return dataStore.getAllConceptNids();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.tcc.api.concept.ProcessUnfetchedConceptDataBI#getTitle()
   */
  @Override
  public String getTitle() {
    return this.getClass().getName();
  }

  /**
   * Fires a {@link ProgressEvent}.
   * @param pct percent done
   * @param note progress note
   */
  public void fireProgressEvent(int pct, String note) {
    ProgressEvent pe = new ProgressEvent(this, pct, pct, note);
    for (int i = 0; i < listeners.size(); i++) {
      listeners.get(i).updateProgress(pe);
    }
  }

  /**
   * Adds a {@link ProgressListener}.
   * @param l thef{@link ProgressListener}
   */
  @Override
  public void addProgressListener(ProgressListener l) {
    listeners.add(l);
    progress = 0;
    try {
      progressMax = dataStore.getConceptCount();
    } catch (IOException e) {
      throw new IllegalStateException("This should never happen");
    }
  }

  /**
   * Removes a {@link ProgressListener}.
   * @param l thef{@link ProgressListener}
   */
  @Override
  public void removeProgressListener(ProgressListener l) {
    listeners.remove(l);
  }

  @Override
  public void cancel() {
    requestCancel = true;    
  }

}
