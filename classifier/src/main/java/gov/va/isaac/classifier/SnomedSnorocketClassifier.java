package gov.va.isaac.classifier;

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.classifier.model.Concept;
import gov.va.isaac.classifier.model.ConceptGroup;
import gov.va.isaac.classifier.model.EquivalentClasses;
import gov.va.isaac.classifier.model.Relationship;
import gov.va.isaac.classifier.model.StringIDConcept;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerHelper;
import gov.va.isaac.util.ProgressEvent;
import gov.va.isaac.util.ProgressListener;
import gov.va.isaac.util.WBUtility;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptFetcherBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.Taxonomies;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.csiro.snorocket.core.IFactory_123;
import au.csiro.snorocket.snapi.I_Snorocket_123.I_Callback;
import au.csiro.snorocket.snapi.I_Snorocket_123.I_EquivalentCallback;
import au.csiro.snorocket.snapi.Snorocket_123;

/**
 * The older version Snorocket classifier.
 *
 * @author bcarlsenca
 */
public class SnomedSnorocketClassifier implements
    ProcessUnfetchedConceptDataBI, Classifier {

  /** The Constant LOG. */
  static final Logger LOG = LoggerFactory
      .getLogger(SnomedSnorocketClassifier.class);

  /** The cancel flag. */
  private boolean requestCancel = false;

  /** the count so far */
  private int progress = 0;

  /** the toal */
  private int progressMax = 0;

  /** Listeners */
  private List<ProgressListener> listeners = new ArrayList<>();

  /** The role group. */
  @SuppressWarnings("unused")
  private static String ROLE_GROUP = "19657985-279a-4713-83be-e3d1a884b50d";

  /** The concept attribute. */
  private static UUID CONCEPT_ATTRIBUTE = UUID
      .fromString("6155818b-09ed-388e-82ce-caa143423e99");

  /**  The snomed root nid. */
  private static ConceptSpec SNOMED_ROOT_NID = Taxonomies.SNOMED;
  
  /** The data store. */
  private static BdbTerminologyStore dataStore = ExtendedAppContext
      .getDataStore();

  /** The count. */
  private int count = Integer.MIN_VALUE + 3;

  /** The all concepts count. */
  private int allCount = count;

  /** The path nid. */
  private int pathNid;

  /** The prev inferred rels. */
  @SuppressWarnings("unused")
  private List<String> f;

  /** The edited snomed concepts. */
  private ArrayList<StringIDConcept> editedSnomedConcepts = new ArrayList<>();

  /** The edit snomed rels. */
  private ArrayList<Relationship> editedSnomedRels = new ArrayList<>();

  /** The c rocket sno rels. */
  private ArrayList<Relationship> snorocketRels;

  /** The concepts. Map of id->seen */
  private Map<Long, Boolean> cycleCheckConcepts = new HashMap<>(20000);

  /** The isa relationships map. Map of sourceId->destinationIds */
  private Map<Long, List<Long>> cycleCheckRelationships = new HashMap<>(20000);

  /** The cycle check report. */
  private String cycleCheckReport = "cycleCheck.txt";

  /** The equivalent concepts report. */
  private String equivalentConcepts = "equivalentConcepts.txt";

  /**
   * Instantiates an empty {@link SnomedSnorocketClassifier}.
   */
  public SnomedSnorocketClassifier() {
    // do nothing
  }

  /**
   * Classify.
   *
   * @param pathNid the path nid
   * @throws Exception the exception
   */
  @Override
  public void classify(int pathNid) throws Exception {
    this.pathNid = pathNid;

    progress = 0;
    try {
      progressMax = dataStore.getConceptCount();
    } catch (IOException e) {
      throw new IllegalStateException("This should never happen");
    }

    // Build ontology model
    dataStore.iterateConceptDataInSequence(this);

    // Cycle check - at this point data structures are loaded
    fireProgressEvent(80, "Begin cycle check");
    CycleCheck cycleCheck = new CycleCheck();
    cycleCheck.setConcepts(cycleCheckConcepts);
    cycleCheck.setIsarelationships(cycleCheckRelationships);
    if (cycleCheck.cycleDetected()) {
      // write out cycles to cycle check file
      saveCycleCheckReport(cycleCheck.getConceptInLoop());
    }

    // Classification run
    fireProgressEvent(90, "Begin classification, prepare data structures");
    // load roles
    int[] roles = getRoles(CONCEPT_ATTRIBUTE);
    int ridx = roles.length;
    if (roles.length > 100) {
      String errStr =
          "Role types exceeds 100. This will cause a memory issue. "
              + "Please check that role root is set to 'Concept mode attribute'";
      LOG.error(errStr);
      throw new ClassificationException(errStr);
    }
    final int reserved = 2;
    int cidx = reserved;
    int margin = editedSnomedConcepts.size() >> 2; // Add 50%
    int[] intArray = new int[editedSnomedConcepts.size() + margin + reserved];
    intArray[IFactory_123.TOP_CONCEPT] = IFactory_123.TOP;
    intArray[IFactory_123.BOTTOM_CONCEPT] = IFactory_123.BOTTOM;
    Collections.sort(editedSnomedConcepts);
    if (editedSnomedConcepts.get(0).id <= Integer.MIN_VALUE + reserved) {
      throw new ClassificationException(
          "::: SNOROCKET: TOP & BOTTOM nids NOT reserved");
    }
    for (StringIDConcept sc : editedSnomedConcepts) {
      intArray[cidx++] = sc.id;
    }
    // Fill array to make binary search work correctly.
    Arrays.fill(intArray, cidx, intArray.length, Integer.MAX_VALUE);
    // Obtain SNOMED root concept
    // TODO: this needs to be generalized
    UUID snomedRootUUID = Taxonomies.SNOMED.getUuids()[0];
    ConceptVersionBI snomedRootConcept =
        WBUtility.getConceptVersion(snomedRootUUID);
    int root = snomedRootConcept.getNid();

    Snorocket_123 rocket_123 =
        new Snorocket_123(intArray, cidx, roles, ridx, root);
    // SnomedMetadata :: ISA
    rocket_123.setIsaNid(TermAux.IS_A.getNid());
    // SnomedMetadata :: ROLE_ROOTS
    rocket_123.setRoleRoot(TermAux.IS_A.getNid(), true); // @@@
    UUID snomedRoleRootUUID = Taxonomies.SNOMED_ROLE_ROOT.getUuids()[0];
    ConceptVersionBI snomedRoleRootConcept =
        WBUtility.getConceptVersion(snomedRoleRootUUID);
    int roleRoot = snomedRoleRootConcept.getNid();
    rocket_123.setRoleRoot(roleRoot, false);
    // SET DEFINED CONCEPTS
    for (int i = 0; i < editedSnomedConcepts.size(); i++) {
      if (editedSnomedConcepts.get(i).isDefined) {
        rocket_123.setConceptIdxAsDefined(i + reserved);
      }
    }
    editedSnomedConcepts = null; // :MEMORY:
    // ADD RELATIONSHIPS
    Collections.sort(editedSnomedRels);
    for (Relationship sr : editedSnomedRels) {
      int err =
          rocket_123.addRelationship(sr.sourceId, sr.typeId, sr.destinationId,
              sr.group);
      if (err > 0) {
        StringBuilder sb = new StringBuilder();
        if ((err & 1) == 1) {
          sb.append(" --UNDEFINED_C1-- ");
        }
        if ((err & 2) == 2) {
          sb.append(" --UNDEFINED_ROLE-- ");
        }
        if ((err & 4) == 4) {
          sb.append(" --UNDEFINED_C2-- ");
        }
        LOG.info("\r\n::: " + sb /* :!!!: + dumpSnoRelStr(sr) */);
      }
    }

    editedSnomedRels = null; // :MEMORY:

    System.gc();

    // RUN CLASSIFIER
    fireProgressEvent(91, "Begin classification, classify");
    long startTime = System.currentTimeMillis();
    LOG.info("::: Starting Classifier... ");
    rocket_123.classify();
    LOG.info("::: Time to classify (ms): "
        + (System.currentTimeMillis() - startTime));

    // Handle equivalents
    fireProgressEvent(91, "Begin classification, classify");
    LOG.info("::: GET EQUIVALENT CONCEPTS...");
    startTime = System.currentTimeMillis();
    ProcessEquiv pe = new ProcessEquiv();
    rocket_123.getEquivalents(pe);
    LOG.info("\r\n::: [SnorocketMojo] ProcessEquiv() count=" + pe.countConSet
        + " time= " + toStringLapseSec(startTime));
    pe.getEquivalentClasses();
    saveEquivalentConceptsReport(pe.getEquivalentClasses(), equivalentConcepts);

    // Get distribution form of relationships and write back
    snorocketRels = new ArrayList<>();
    LOG.info("::: GET CLASSIFIER RESULTS...");
    startTime = System.currentTimeMillis();
    ProcessResults pr = new ProcessResults(snorocketRels);
    rocket_123.getDistributionFormRelationships(pr);
    LOG.info("\r\n::: [SnorocketMojo] GET CLASSIFIER RESULTS count="
        + pr.countRel + " time= " + toStringLapseSec(startTime));

    pr = null; // :MEMORY:
    rocket_123 = null; // :MEMORY:
    System.gc();
    System.gc();

    // GET CLASSIFIER_PATH RELS
    startTime = System.currentTimeMillis();
    editedSnomedRels = new ArrayList<>();
    editedSnomedConcepts = new ArrayList<>();
    editedSnomedConcepts = null;

    // WRITEBACK RESULTS
    // startTime = System.currentTimeMillis();
    // if (previousInferredRelationships == null
    // || !previousInferredRelationships.isEmpty()) {
    // writeInferredRel(cRocketRelationships);
    // } else {
    //
    // LOG.info(compareAndWriteBack(cEditRelationships,
    // cRocketRelationships));
    //
    // LOG.info("\r\n::: *** WRITEBACK *** LAPSED TIME =\t"
    // + toStringLapseSec(startTime) + "\t ***");
    //
    // consolidateRels();
    //
    // }

    LOG.info("\r\n::: *** WROTE *** LAPSED TIME =\t"
        + toStringLapseSec(startTime) + "\t ***");

  }

  /**
   * Save cycle check report.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void saveCycleCheckReport(Set<Long> conceptInLoop) throws IOException {
    FileOutputStream fos = new FileOutputStream(cycleCheckReport);
    OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
    BufferedWriter bw = new BufferedWriter(osw);
    bw.append("conceptId");
    bw.append("\r\n");
    for (Long concept : conceptInLoop) {
      bw.append(concept.toString());
      bw.append("\r\n");
    }
    bw.close();
    bw = null;
    fos = null;
    osw = null;

  }

  /**
   * Save equivalent concepts report.
   *
   * @param equivalentClasses the equivalent classes
   * @param fName the f name
   */
  public static void saveEquivalentConceptsReport(
    EquivalentClasses equivalentClasses, String fName) {
    BufferedWriter bw = null;
    try {
      bw = new BufferedWriter(new FileWriter(fName));
      // "COMPARE" UUIDs, //NIDs, Initial Text
      int setNumber = 1;
      for (ConceptGroup eqc : equivalentClasses) {
        for (Concept sc : eqc) {
          bw.write(sc.id + "\tset=\t" + setNumber + "\t");
          bw.write("\r\n");
        }
        setNumber++;
      }
      bw.flush();
      bw.close();
    } catch (IOException ex) {
      LOG.error(null, ex);
    } finally {
      try {
        bw.close();
      } catch (IOException ex) {
        LOG.error(null, ex);
      }
    }
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
    ConceptVersionBI concept = fetcher.fetch(WBUtility.getViewCoordinate());
    allCount++;
    if (concept.getPathNid() == pathNid) {
      count++;
      convertToOntologyObjects(concept);

    }
    // Handle progress monitor - consider this part 80% of the process
    if ((int) ((allCount * 100) / progressMax) > progress) {
      progress = (allCount * 100) / progressMax;
      fireProgressEvent((int) (progress * .8), progress + " % finished");
    }

  }

  /**
   * Convert to ontology objects.
   *
   * @param concept the concept
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ContradictionException the contradiction exception
   */
  private void convertToOntologyObjects(ConceptVersionBI concept)
    throws IOException, ContradictionException {
    ConceptAttributeVersionBI<?> attributes =
        ConceptViewerHelper.getConceptAttributes(concept);

    // Add for classification
    StringIDConcept conStr =
        new StringIDConcept(count, String.valueOf(concept.getNid()),
            attributes.isDefined());
    editedSnomedConcepts.add(conStr);

    // Add for cycle check
    long nid = concept.getNid();
    cycleCheckConcepts.put(nid, false);
    List<Long> destIds = new ArrayList<>();
    if (cycleCheckRelationships.containsKey(nid)) {
      destIds = cycleCheckRelationships.get(nid);
    }
    cycleCheckRelationships.put(nid, destIds);

    Collection<? extends RelationshipVersionBI<?>> relationships =
        concept.getRelationshipsOutgoingActive();
    for (RelationshipVersionBI<?> relationship : relationships) {

      // add for classification
      Relationship rel =
          new Relationship(relationship.getConceptNid(),
              relationship.getDestinationNid(), relationship.getTypeNid(),
              relationship.getGroup(), String.valueOf(relationship.getNid()));
      editedSnomedRels.add(rel);

      // add for cycle check
      if (relationship.getTypeNid() == Snomed.IS_A.getLenient().getNid()) {
        destIds.add((long) relationship.getDestinationNid());

      }
    }

  }

  /**
   * Returns the roles.
   *
   * @param parent the parent
   * @return the roles
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ContradictionException the contradiction exception
   */
  private int[] getRoles(UUID parent) throws IOException,
    ContradictionException {
    ArrayList<ConceptVersionBI> children =
        WBUtility.getAllChildrenOfConcept(WBUtility.getConceptVersion(parent),
            false);
    int[] roles = new int[children.size() + 1];
    roles[0] = TermAux.IS_A.getNid();
    int i = 1;
    for (ConceptVersionBI child : children) {
      roles[i++] = child.getNid();
    }
    return roles;
  }

  /**
   * To string lapse sec.
   *
   * @param startTime the start time
   * @return the string
   */
  private String toStringLapseSec(long startTime) {
    StringBuilder s = new StringBuilder();
    long stopTime = System.currentTimeMillis();
    long lapseTime = stopTime - startTime;
    s.append((float) lapseTime / 1000).append(" (seconds)");
    return s.toString();
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
   * The Class ProcessResults.
   *
   * @author ${author}
   */
  private class ProcessResults implements I_Callback {

    /** The snorels. */
    private List<Relationship> snorels;

    /** The count rel. */
    int countRel = 0; // STATISTICS COUNTER

    /**
     * Instantiates a new process results.
     *
     * @param snorels the snorels
     */
    public ProcessResults(List<Relationship> snorels) {
      this.snorels = snorels;
      this.countRel = 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * au.csiro.snorocket.snapi.I_Snorocket_123.I_Callback#addRelationship(int,
     * int, int, int)
     */
    @Override
    public void addRelationship(int conceptId1, int roleId, int conceptId2,
      int group) {
      countRel++;
      Relationship relationship =
          new Relationship(conceptId1, conceptId2, roleId, group);
      snorels.add(relationship);
      if (countRel % 25000 == 0) {
        // ** GUI: ProcessResults
        LOG.info("rels processed " + countRel);
      }
    }
  }

  /**
   * The Class ProcessEquiv.
   *
   * @author ${author}
   */
  private class ProcessEquiv implements I_EquivalentCallback {

    /** The count con set. */
    int countConSet = 0; // STATISTICS COUNTER

    /** The equiv concept. */
    private EquivalentClasses equivalentClasses;

    /**
     * Instantiates a new process equiv.
     */
    public ProcessEquiv() {
      equivalentClasses = new EquivalentClasses();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * au.csiro.snorocket.snapi.I_Snorocket_123.I_EquivalentCallback#equivalent
     * (java.util.ArrayList)
     */
    @Override
    public void equivalent(ArrayList<Integer> equivalentConcepts) {
      equivalentClasses.add(new ConceptGroup(equivalentConcepts));
      countConSet += 1;
    }

    /**
     * Gets the equiv concept.
     *
     * @return the equiv concept
     */
    public EquivalentClasses getEquivalentClasses() {
      return equivalentClasses;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.va.isaac.util.ProgressReporter#addProgressListener(gov.va.isaac.util
   * .ProgressListener)
   */
  @Override
  public void addProgressListener(ProgressListener l) {
    listeners.add(l);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.va.isaac.util.ProgressReporter#removeProgressListener(gov.va.isaac.
   * util.ProgressListener)
   */
  @Override
  public void removeProgressListener(ProgressListener l) {
    listeners.remove(l);
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
   * Cancel operation
   */
  @Override
  public void cancel() {
    requestCancel = true;
  }

}
