package gov.va.isaac.classifier;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.classifier.model.Concept;
import gov.va.isaac.classifier.model.ConceptGroup;
import gov.va.isaac.classifier.model.EquivalentClasses;
import gov.va.isaac.classifier.model.Relationship;
import gov.va.isaac.classifier.model.RelationshipGroup;
import gov.va.isaac.classifier.model.RelationshipGroupList;
import gov.va.isaac.classifier.model.StringIDConcept;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerHelper;
import gov.va.isaac.util.OTFUtility;
import gov.va.isaac.util.ProgressEvent;
import gov.va.isaac.util.ProgressListener;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RelationshipCAB;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.nid.IntSet;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipType;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
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
 * <pre>
 * Configuration things to consider:
 * - Never group relationships, right identities
 * - which paths to include (e.g. edit path plus all origins? plus SNOMED?)
 * - the edit path for the inferred relationships
 * - commit strategy (commit once, commit every X?)
 * - progress monitoring for the classify step itself (needs changes to Snorocket)
 * -
 * </pre>
 * 
 * @author bcarlsenca
 */
public class SnomedSnorocketClassifier implements Classifier {

  /** The Constant LOG. */
  static final Logger LOG = LoggerFactory
      .getLogger(SnomedSnorocketClassifier.class);

  /** The cancel flag. */
  private boolean requestCancel = false;

  /** the count so far. */
  private int progress = 0;

  /** the total. */
  private int progressMax = 0;

  /** Listeners. */
  private List<ProgressListener> listeners = new ArrayList<>();

  /** The data store. */
  private static BdbTerminologyStore dataStore = ExtendedAppContext
      .getDataStore();

  /** The role group. */
  @SuppressWarnings("unused")
  private static String ROLE_GROUP = "19657985-279a-4713-83be-e3d1a884b50d";

  /** The concept attribute. */
  private static UUID CONCEPT_ATTRIBUTE = UUID
      .fromString("6155818b-09ed-388e-82ce-caa143423e99");

  /** The count. */
  // private int count = Integer.MIN_VALUE + 3;

  /** The progress count. */
  private int progressCount = 0;

  /** The edited snomed concepts. */
  private List<StringIDConcept> editedSnomedConcepts;

  /** The edit snomed rels. */
  private List<Relationship> editedSnomedRels;

  /** The c rocket sno rels. */
  private Map<Integer, List<Relationship>> snorocketRels;

  /** The prior inferred rels. */
  private static Map<Integer, List<Relationship>> previousInferredRels;

  /** The previous inferred rels ct. */
  private static int previousInferredRelsCt = 0;

  /** The concepts. Map of id->seen */
  private static Map<Integer, Boolean> cycleCheckConcepts;

  /** The isa relationships map. Map of sourceId->destinationIds */
  private static Map<Integer, List<Integer>> cycleCheckRelationships;

  /** The cycle check report. */
  private String cycleCheckReport = "cycleCheck.txt";

  /** The equivalent concepts report. */
  private String equivalentConceptsReport = "equivalentConcepts.txt";

  /** The incremental classification set. */
  private static IntSet incrementalClassificationSet;

  /** The valid paths. */
  private IntSet validPaths = new IntSet();

  /** An id map for resolving relationship ids later. */
  private Set<Integer> nidSeen;

  /** The rocket_123. */
  private static Snorocket_123 rocket_123 = null;

  /** The save cycle check report. */
  private boolean saveCycleCheckReport = true;

  /** The save equivalent concepts report. */
  private boolean saveEquivalentConceptsReport = true;

  /**
   * Instantiates an empty {@link SnomedSnorocketClassifier}.
   *
   * @throws Exception the exception
   */
  public SnomedSnorocketClassifier() throws Exception {
    // set valid paths to SNOMED CORE plus the edit path
    // TODO: this needs to suit more use cases, but WB_AUX and LOINC and other
    // things
    // are getting in the way
    validPaths.add(TermAux.SNOMED_CORE.getLenient().getNid());
    // when running in mojo mode, this may not be available
    if (AppContext.getAppConfiguration().getDefaultEditPathUuid() != null) {
      validPaths.add(OTFUtility.getConceptVersion(
          UUID.fromString(AppContext.getAppConfiguration()
              .getDefaultEditPathUuid())).getNid());
    }
  }

  /**
   * Classify.
   *
   * @param rootNid the root nid
   * @throws Exception the exception
   */
  @Override
  public void classify(int rootNid) throws Exception {
    // Reset data structures
    editedSnomedConcepts = new ArrayList<>();
    editedSnomedRels = new ArrayList<>();
    previousInferredRels = new HashMap<>();
    previousInferredRelsCt = 0;
    cycleCheckConcepts = new HashMap<>(20000);
    cycleCheckRelationships = new HashMap<>(20000);
    incrementalClassificationSet = new IntSet();
    nidSeen = new HashSet<>();
    rocket_123 = null;

    // Set up progress monitoring
    progress = 0;
    // set this to the approximate snomed concept count,
    // once computed, the static variable will retain the count
    // for future runs. Not sure how to quickly determine
    // the total number of concepts in scope
    if (progressMax == 0) {
      // approximate size of SNOMED + extension
      progressMax = 320000;
    }

    fireProgressEvent(0, "Preparing data");
    IntSet sctDescendants = new IntSet();
    getAllDescendants(rootNid, rootNid, sctDescendants);
    if (!continueWork()) {
      return;
    }
    LOG.info("  concepts = " + editedSnomedConcepts.size());
    LOG.info("  stated rels = " + editedSnomedRels.size());
    LOG.info("  inferred rels = " + previousInferredRelsCt);

    // Cycle check - at this point data structures are loaded
    fireProgressEvent(50, "Begin cycle check");
    CycleCheck cycleCheck = new CycleCheck();
    cycleCheck.setConcepts(cycleCheckConcepts);
    cycleCheck.setIsarelationships(cycleCheckRelationships);
    saveCycleCheckReport(cycleCheck.getConceptInLoop());
    if (!continueWork()) {
      return;
    }

    // Prepare classifier
    fireProgressEvent(55, "Setup classifier data structures");

    // load roles - this is an array of role concept ids
    int[] roleRefArray = getRoles(CONCEPT_ATTRIBUTE);
    nidSeen = null;
    int roleCount = roleRefArray.length;
    // cannot handle > 100 roles
    if (roleCount > 100) {
      String errStr =
          "Role types exceeds 100. This will cause a memory issue. "
              + "Please check that role root is set to 'Concept mode attribute'";
      LOG.error(errStr);
      throw new ClassificationException(errStr);
    }

    // Set up data structures in memory efficient way
    fireProgressEvent(56, "Setup classifier data structures - concepts");
    final int reserved = 2;
    int conceptCount = reserved;
    int margin = editedSnomedConcepts.size() >> 2; // Add 50%
    int[] conceptRefArray =
        new int[editedSnomedConcepts.size() + margin + reserved];
    conceptRefArray[IFactory_123.TOP_CONCEPT] = IFactory_123.TOP;
    conceptRefArray[IFactory_123.BOTTOM_CONCEPT] = IFactory_123.BOTTOM;
    Collections.sort(editedSnomedConcepts);
    if (editedSnomedConcepts.get(0).id <= Integer.MIN_VALUE + reserved) {
      throw new ClassificationException(
          "SNOROCKET: TOP & BOTTOM nids NOT reserved.");
    }
    // Add concept ids to the array
    for (StringIDConcept sc : editedSnomedConcepts) {
      // This is the id based on "count"
      conceptRefArray[conceptCount++] = sc.id;
    }

    // Fill array to make binary search work correctly.
    Arrays.fill(conceptRefArray, conceptCount, conceptRefArray.length,
        Integer.MAX_VALUE);

    // Instantiate classifier
    rocket_123 =
        new Snorocket_123(conceptRefArray, conceptCount, roleRefArray,
            roleCount, rootNid);

    // Set isa nid
    rocket_123.setIsaNid(Snomed.IS_A.getNid());

    // Set role roots NID.
    rocket_123.setRoleRoot(Snomed.IS_A.getNid(), true);
    int roleRoot = OTFUtility.getConceptVersion(CONCEPT_ATTRIBUTE).getNid();
    rocket_123.setRoleRoot(roleRoot, false);

    // Non-grouping roles (from owl perl script)
    // $nevergrouped{"123005000"} = "T"; # part-of is never grouped
    // $nevergrouped{"272741003"} = "T"; # laterality is never grouped
    // $nevergrouped{"127489000"} = "T"; # has-active-ingredient is never
    // grouped
    // $nevergrouped{"411116001"} = "T"; # has-dose-form is never grouped
    rocket_123.setRoleNeverGrouped(Snomed.PART_OF.getNid());
    rocket_123.setRoleNeverGrouped(Snomed.LATERALITY.getNid());
    rocket_123.setRoleNeverGrouped(Snomed.HAS_ACTIVE_INGREDIENT.getNid());
    rocket_123.setRoleNeverGrouped(Snomed.HAS_DOSE_FORM.getNid());

    // right identities (from owl perl script)
    // $rightid{"363701004"} = "127489000"; # direct-substance o
    // has-active-ingredient -> direct-substance
    rocket_123.addRoleComposition(new int[] {
        Snomed.DIRECT_SUBSTANCE.getNid(), Snomed.HAS_ACTIVE_INGREDIENT.getNid()
    }, Snomed.HAS_ACTIVE_INGREDIENT.getNid());

    fireProgressEvent(57, "Setup classifier data structures - defined");
    // Indicate defined concepts
    for (int i = 0; i < editedSnomedConcepts.size(); i++) {
      if (editedSnomedConcepts.get(i).isDefined) {
        rocket_123.setConceptIdxAsDefined(i + reserved);
      }
    }
    if (!continueWork()) {
      return;
    }

    // Clear reference for memory
    editedSnomedConcepts = null;

    // Add relationships
    fireProgressEvent(58, "Setup classifier data structures - defined");
    progressCount = 0;
    progress = 0;
    int relsCt = editedSnomedRels.size();
    Collections.sort(editedSnomedRels);
    for (Relationship sr : editedSnomedRels) {
      progressCount++;
      int err =
          rocket_123.addRelationship(sr.sourceId, sr.typeId, sr.destinationId,
              sr.group);
      if (!continueWork()) {
        return;
      }
      if (err > 0) {
        StringBuilder sb = new StringBuilder();
        if ((err & 1) == 1) {
          sb.append(" --UNDEFINED_C1-- " + sr.sourceId);
        }
        if ((err & 2) == 2) {
          sb.append(" --UNDEFINED_ROLE-- " + sr.typeId);
        }
        if ((err & 4) == 4) {
          sb.append(" --UNDEFINED_C2-- " + sr.destinationId);
        }
        LOG.info(sb.toString());
      }
      // Handle progress monitor - consider this part 58%-70%% of the process
      @SuppressWarnings("cast")
      final int currentProgress = (int) ((progressCount * 100) / relsCt);
      if (currentProgress > progress) {
        progress = currentProgress;
        // 12 percentage points starting at 58
        fireProgressEvent(((int) (progress * .12) + 58),
            "Loading relationships");

      }
    }
    if (!continueWork()) {
      return;
    }

    // clear reference for memory
    editedSnomedRels = null;

    // Run garbage collector
    System.gc();
    System.gc();

    // Classify
    fireProgressEvent(71, "Classify");
    long startTime = System.currentTimeMillis();
    rocket_123.classify();
    if (!continueWork()) {
      return;
    }
    LOG.info("  time to classify (ms): "
        + (System.currentTimeMillis() - startTime));

    // Handle equivalents
    fireProgressEvent(85, "Handle equivalents");
    startTime = System.currentTimeMillis();
    ProcessEquivalents pe = new ProcessEquivalents();
    rocket_123.getEquivalents(pe);
    if (!continueWork()) {
      return;
    }
    LOG.info("  count=" + pe.countConSet + ", time= "
        + toStringLapseSec(startTime));
    pe.getEquivalentClasses();
    if (saveEquivalentConceptsReport) {
      saveEquivalentConceptsReport(pe.getEquivalentClasses(),
          equivalentConceptsReport);
    }
    pe = null;

    // Get distribution form of relationships and write back
    fireProgressEvent(90, "Compare relationships");
    snorocketRels = new HashMap<>();
    startTime = System.currentTimeMillis();
    ProcessResults pr = new ProcessResults(snorocketRels);
    rocket_123.getDistributionFormRelationships(pr);
    if (!continueWork()) {
      return;
    }
    LOG.info("  count=" + pr.countRel + ", time= "
        + toStringLapseSec(startTime));

    // Clear data structures to save memory
    // there is an opportunity to reconstitute rocket_123 here rather than
    // saving it for later
    // to clear the inferred view within
    pr = null;
    // save for incremental classification
    // rocket_123 = null;
    System.gc();
    System.gc();

    // Write back results - disable commit listeners during this
    try {
      AppContext.getRuntimeGlobals().disableAllCommitListeners();
      startTime = System.currentTimeMillis();
      fireProgressEvent(93, "Write relationship changes");
      List<Relationship> prevInferredRelsList = new ArrayList<>();
      for (List<Relationship> rels : previousInferredRels.values()) {
        prevInferredRelsList.addAll(rels);
      }

      List<Relationship> snorocketRelsList = new ArrayList<>();
      for (List<Relationship> rels : snorocketRels.values()) {
        snorocketRelsList.addAll(rels);
      }
      LOG.info("start comparing");
      LOG.info(compareAndWriteBack(prevInferredRelsList, snorocketRelsList));
      dataStore.commit();
    } catch (Exception e) {
      throw e;
    } finally {
      AppContext.getRuntimeGlobals().enableAllCommitListeners();
    }
    // Clear memory
    startTime = System.currentTimeMillis();
    editedSnomedRels = null;
    editedSnomedConcepts = null;
    nidSeen = null;

    fireProgressEvent(100, "Finished");
    LOG.info("  total elapsed time = " + toStringLapseSec(startTime));

  }

  /**
   * Compare and write back.
   *
   * @param snorelA the snorel a
   * @param snorelB the snorel b
   * @return the string
   * @throws Exception the exception
   */
  @SuppressWarnings("null")
  private String compareAndWriteBack(List<Relationship> snorelA,
    List<Relationship> snorelB) throws Exception {

    int relsCt = snorelA.size() + snorelB.size();

    // STATISTICS COUNTERS
    int countConceptsSeen = 0;
    int countSame = 0;
    int countSameISA = 0;
    int countA_Diff = 0;
    int countA_DiffISA = 0;
    int countA_Total = 0;
    int countB_Diff = 0;
    int countB_DiffISA = 0;
    int countB_Total = 0;

    long startTime = System.currentTimeMillis();
    Collections.sort(snorelA);
    Collections.sort(snorelB);

    // Typically, A is the Classifier Path (for previously inferred)
    // Typically, B is the SnoRocket Results Set (for newly inferred)
    Iterator<Relationship> itA = snorelA.iterator();
    Iterator<Relationship> itB = snorelB.iterator();
    Relationship rel_A = null;
    boolean done_A = false;
    if (itA.hasNext()) {
      rel_A = itA.next();
    } else {
      done_A = true;
    }
    Relationship rel_B = null;
    boolean done_B = false;
    if (itB.hasNext()) {
      rel_B = itB.next();
    } else {
      done_B = true;
    }

    LOG.info("  previousInferredRelationships.size() = " + snorelA.size());
    LOG.info("  snorocketRels.size() = " + snorelB.size());

    // BY SORT ORDER, LOWER NUMBER ADVANCES FIRST
    while (!done_A && !done_B) {
      if (!continueWork()) {
        return "cancelled";
      }

      // Handle progress monitor - consider this part 93%-100%% of the process
      @SuppressWarnings("cast")
      final int currentProgress = (int) ((progressCount * 100) / relsCt);
      if (currentProgress > progress) {
        progress = currentProgress;
        // 7 percentage points starting at 93
        fireProgressEvent(((int) (progress * .07) + 93),
            "Loading relationships");

      }
      if (++countConceptsSeen % 25000 == 0) {
        LOG.info("  count = " + countConceptsSeen);
      }

      if (rel_A.sourceId == rel_B.sourceId) {
        // COMPLETELY PROCESS ALL C1 FOR BOTH IN & OUT
        // PROCESS C1 WITH GROUP == 0
        int thisC1 = rel_A.sourceId;

        // PROCESS WHILE BOTH HAVE GROUP 0
        while (rel_A.sourceId == thisC1 && rel_B.sourceId == thisC1
            && rel_A.group == 0 && rel_B.group == 0 && !done_A && !done_B) {

          // PROGESS GROUP ZERO
          switch (compareRelationships(rel_A, rel_B)) {
            case 1: // SAME
              // GATHER STATISTICS
              countA_Total++;
              countB_Total++;
              countSame++;
              // NOTHING TO WRITE IN THIS CASE
              if (rel_A.typeId == Snomed.IS_A.getNid()) {
                countSameISA++;
              }
              if (itA.hasNext()) {
                rel_A = itA.next();
              } else {
                done_A = true;
              }
              if (itB.hasNext()) {
                rel_B = itB.next();
              } else {
                done_B = true;
              }
              break;

            case 2: // REL_A > REL_B -- B has extra stuff
              // WRITEBACK REL_B (Classifier Results) AS CURRENT
              countB_Diff++;
              countB_Total++;
              if (rel_B.typeId == Snomed.IS_A.getNid()) {
                countB_DiffISA++;
              }

              writeRel(rel_B, false, countConceptsSeen, "A");

              if (itB.hasNext()) {
                rel_B = itB.next();
              } else {
                done_B = true;
              }
              break;

            case 3: // REL_A < REL_B -- A has extra stuff
              // WRITEBACK REL_A (Classifier Input) AS RETIRED
              // GATHER STATISTICS
              countA_Diff++;
              countA_Total++;
              if (rel_A.typeId == Snomed.IS_A.getNid()) {
                countA_DiffISA++;
              }
              writeRel(rel_A, true, countConceptsSeen, "B");
              if (itA.hasNext()) {
                rel_A = itA.next();
              } else {
                done_A = true;
              }
              break;
            default:
              throw new Exception("Unexpected condition.");
          } // switch
        }

        // REMAINDER LIST_A GROUP 0 FOR C1
        while (rel_A.sourceId == thisC1 && rel_A.group == 0 && !done_A) {

          countA_Diff++;
          countA_Total++;
          if (rel_A.typeId == Snomed.IS_A.getNid()) {
            countA_DiffISA++;
          }
          writeRel(rel_A, true, countConceptsSeen, "C");
          if (itA.hasNext()) {
            rel_A = itA.next();
          } else {
            done_A = true;
            break;
          }
        }

        // REMAINDER LIST_B GROUP 0 FOR C1
        while (rel_B.sourceId == thisC1 && rel_B.group == 0 && !done_B) {
          countB_Diff++;
          countB_Total++;
          if (rel_B.typeId == Snomed.IS_A.getNid()) {
            countB_DiffISA++;
          }
          writeRel(rel_B, false, countConceptsSeen, "D");
          if (itB.hasNext()) {
            rel_B = itB.next();
          } else {
            done_B = true;
            break;
          }
        }

        // ** SEGMENT GROUPS **
        RelationshipGroupList groupList_A = new RelationshipGroupList();
        RelationshipGroupList groupList_B = new RelationshipGroupList();
        RelationshipGroup groupA = null;
        RelationshipGroup groupB = null;

        // SEGMENT GROUPS IN LIST_A
        int prevGroup = Integer.MIN_VALUE;
        while (rel_A.sourceId == thisC1 && !done_A) {
          if (rel_A.group != prevGroup) {
            groupA = new RelationshipGroup();
            groupList_A.add(groupA);
          }

          groupA.add(rel_A);

          prevGroup = rel_A.group;
          if (itA.hasNext()) {
            rel_A = itA.next();
          } else {
            done_A = true;
          }
        }
        // SEGMENT GROUPS IN LIST_B
        prevGroup = Integer.MIN_VALUE;
        while (rel_B.sourceId == thisC1 && !done_B) {
          if (rel_B.group != prevGroup) {
            groupB = new RelationshipGroup();
            groupList_B.add(groupB);
          }

          groupB.add(rel_B);

          prevGroup = rel_B.group;
          if (itB.hasNext()) {
            rel_B = itB.next();
          } else {
            done_B = true;
          }
        }

        // FIND GROUPS IN GROUPLIST_A WITHOUT AN EQUAL IN GROUPLIST_B
        // WRITE THESE GROUPED RELS AS "RETIRED"
        RelationshipGroupList groupList_NotEqual;
        if (groupList_A.size() > 0) {
          groupList_NotEqual = groupList_A.whichNotEqual(groupList_B);
          for (RelationshipGroup sg : groupList_NotEqual) {
            for (Relationship sr_A : sg) {
              writeRel(sr_A, true, countConceptsSeen, "E");
            }
          }
          countA_Total += groupList_A.countRels();
          countA_Diff += groupList_NotEqual.countRels();
        }

        // FIND GROUPS IN GROUPLIST_B WITHOUT AN EQUAL IN GROUPLIST_A
        // WRITE THESE GROUPED RELS AS "NEW, CURRENT"
        int rgNum = 0; // USED TO DETERMINE "AVAILABLE" ROLE GROUP NUMBERS
        if (groupList_B.size() > 0) {
          groupList_NotEqual = groupList_B.whichNotEqual(groupList_A);
          for (RelationshipGroup sg : groupList_NotEqual) {
            if (sg.get(0).group != 0) {
              rgNum = nextRoleGroupNumber(groupList_A, rgNum);
              for (Relationship sr_B : sg) {
                sr_B.group = rgNum;
                writeRel(sr_B, false, countConceptsSeen, "F");
              }
            } else {
              for (Relationship sr_B : sg) {
                writeRel(sr_B, false, countConceptsSeen, "G");
              }
            }
          }
          countB_Total += groupList_A.countRels();
          countB_Diff += groupList_NotEqual.countRels();
        }
      } else if (rel_A.sourceId > rel_B.sourceId) {
        // CASE 2: LIST_B HAS CONCEPT NOT IN LIST_A
        // COMPLETELY *ADD* ALL THIS C1 FOR REL_B AS NEW, CURRENT
        int thisC1 = rel_B.sourceId;
        while (rel_B.sourceId == thisC1) {
          countB_Diff++;
          countB_Total++;
          if (rel_B.typeId == Snomed.IS_A.getNid()) {
            countB_DiffISA++;
          }
          writeRel(rel_B, false, countConceptsSeen, "H");
          if (itB.hasNext()) {
            rel_B = itB.next();
          } else {
            done_B = true;
            break;
          }
        }

      } else {
        // CASE 3: LIST_A HAS CONCEPT NOT IN LIST_B
        // COMPLETELY *RETIRE* ALL THIS C1 FOR REL_A
        int thisC1 = rel_A.sourceId;
        while (rel_A.sourceId == thisC1) {
          countA_Diff++;
          countA_Total++;
          if (rel_A.typeId == Snomed.IS_A.getNid()) {
            countA_DiffISA++;
          }
          writeRel(rel_A, true, countConceptsSeen, "I");
          if (itA.hasNext()) {
            rel_A = itA.next();
          } else {
            done_A = true;
            break;
          }
        }
      }
    }

    // AT THIS POINT, THE PREVIOUS C1 HAS BE PROCESSED COMPLETELY
    // AND, EITHER REL_A OR REL_B HAS BEEN COMPLETELY PROCESSED
    // AND, ANY REMAINDER IS ONLY ON REL_LIST_A OR ONLY ON REL_LIST_B
    // AND, THAT REMAINDER HAS A "STANDALONE" C1 VALUE
    // THEREFORE THAT REMAINDER WRITEBACK COMPLETELY
    // AS "NEW CURRENT" OR "OLD RETIRED"
    //
    // LASTLY, IF .NOT.DONE_A THEN THE NEXT REL_A IN ALREADY IN PLACE
    while (!done_A) {
      countA_Diff++;
      countA_Total++;
      if (rel_A.typeId == Snomed.IS_A.getNid()) {
        countA_DiffISA++;
      }
      // COMPLETELY UPDATE ALL REMAINING REL_A AS RETIRED
      writeRel(rel_A, true, countA_Diff, "J");
      if (itA.hasNext()) {
        rel_A = itA.next();
      } else {
        done_A = true;
        break;
      }
    }

    while (!done_B) {
      countB_Diff++;
      countB_Total++;
      if (rel_B.typeId == Snomed.IS_A.getNid()) {
        countB_DiffISA++;
      }
      // COMPLETELY UPDATE ALL REMAINING REL_B AS NEW, CURRENT
      writeRel(rel_B, false, countB_Diff, "K");
      if (itB.hasNext()) {
        rel_B = itB.next();
      } else {
        done_B = true;
        break;
      }
    }

    StringBuilder s = new StringBuilder();
    s.append("\r\n::: [Snorocket] compareAndWriteBack()");
    long lapseTime = System.currentTimeMillis() - startTime;
    s.append("\r\n::: [Time] Sort/Compare Input & Output: \t")
        .append(lapseTime);
    s.append("\t(mS)\t").append(((float) lapseTime / 1000) / 60)
        .append("\t(min)");
    s.append("\r\n");
    s.append("\r\n::: ");
    s.append("\r\n::: countSame:     \t").append(countSame);
    s.append("\r\n::: countSameISA:  \t").append(countSameISA);
    s.append("\r\n::: A == Classifier Output Path");
    s.append("\r\n::: countA_Diff:   \t").append(countA_Diff);
    s.append("\r\n::: countA_DiffISA:\t").append(countA_DiffISA);
    s.append("\r\n::: countA_Total:  \t").append(countA_Total);
    s.append("\r\n::: B == Classifier Solution Set");
    s.append("\r\n::: countB_Diff:   \t").append(countB_Diff);
    s.append("\r\n::: countB_DiffISA:\t").append(countB_DiffISA);
    s.append("\r\n::: countB_Total:  \t").append(countB_Total);
    s.append("\r\n::: ");

    return s.toString();
  }

  /**
   * Compare relationships.
   *
   * @param in the in relationship
   * @param out the out relationship
   * @return the int indicating the nature of the change
   */
  private static int compareRelationships(Relationship in, Relationship out) {
    if ((in.sourceId == out.sourceId) && (in.group == out.group)
        && (in.typeId == out.typeId) && (in.destinationId == out.destinationId)) {
      return 1; // SAME
    } else if (in.sourceId > out.sourceId) {
      return 2; // ADDED
    } else if ((in.sourceId == out.sourceId) && (in.group > out.group)) {
      return 2; // ADDED
    } else if ((in.sourceId == out.sourceId) && (in.group == out.group)
        && (in.typeId > out.typeId)) {
      return 2; // ADDED
    } else if ((in.sourceId == out.sourceId) && (in.group == out.group)
        && (in.typeId == out.typeId) && (in.destinationId > out.destinationId)) {
      return 2; // ADDED
    } else {
      return 3; // DROPPED
    }
  }

  /**
   * Write relationship.
   *
   * @param relationship the relationship
   * @param retired the retired
   * @param countConceptsSeen the count concepts seen
   * @param label the label
   * @throws Exception the exception
   */
  private void writeRel(Relationship relationship, boolean retired,
    int countConceptsSeen, String label) throws Exception {

    // add concept to commit list
    dataStore.addUncommitted(OTFUtility
        .getConceptVersion(relationship.sourceId));

    // add rel
    if (!retired) {
      LOG.debug("ADD : " + relationship + " " + label);
      // using generate random to work around an issue where the
      // constructor thinks this object is a RelGroupChronicle
      // that error was only sometimes being triggered when trying to add a new
      // rel
      final RelationshipCAB relCAB =
          new RelationshipCAB(relationship.sourceId, relationship.typeId,
              relationship.destinationId, relationship.group,
              RelationshipType.INFERRED_ROLE, IdDirective.GENERATE_RANDOM);

      OTFUtility.getBuilder().construct(relCAB);

      // Add to previousInferredRels for incremental classification
      addPreviousInferredRel(relationship.sourceId, relationship);
    }

    // retire rel
    else {
      LOG.debug("RETIRE REL : " + relationship + " " + relationship.getRelId()
          + " " + label);
      final RelationshipVersionBI<?> rel =
          (RelationshipVersionBI<?>) dataStore.getComponent(Integer
              .parseInt(relationship.getRelId()));
      final RelationshipCAB rcab =
          new RelationshipCAB(rel.getConceptNid(), rel.getTypeNid(),
              rel.getDestinationNid(), 1, RelationshipType.getRelationshipType(
                  rel.getRefinabilityNid(), rel.getCharacteristicNid()), rel,
              OTFUtility.getViewCoordinate(), IdDirective.PRESERVE,
              RefexDirective.EXCLUDE);
      rcab.setStatus(Status.INACTIVE);
      OTFUtility.getBuilder().constructIfNotCurrent(rcab);
      // Remove from previously inferred rels for incremental classification
      removePreviousInferredRel(relationship.sourceId, relationship);
    }

    // Commit periodically (for full case)
    // if (countConceptsSeen % 5000 == 0) {
    // LOG.info("    commit = " + countConceptsSeen);
    // dataStore.commit();
    // }
  }

  /**
   * Next role group number.
   *
   * @param relGroupList the sgl
   * @param gnum the gnum
   * @return the int
   */
  private static int nextRoleGroupNumber(RelationshipGroupList relGroupList,
    int gnum) {

    int testNum = gnum + 1;
    int sglSize = relGroupList.size();
    int trial = 0;
    while (trial <= sglSize) {

      boolean exists = false;
      for (int i = 0; i < sglSize; i++) {
        if (relGroupList.get(i).get(0).group == testNum) {
          exists = true;
        }
      }

      if (exists == false) {
        return testNum;
      } else {
        testNum++;
        trial++;
      }
    }

    return testNum;
  }

  /**
   * Returns the all descendants.
   *
   * @param topNid the top level nid
   * @param nid the root nid
   * @param descendants the descendants
   * @throws Exception the exception
   */
  @SuppressWarnings("cast")
  public void getAllDescendants(int topNid, int nid, final IntSet descendants)
    throws Exception {
    // return if cancelled
    if (!continueWork()) {
      return;
    }

    // return if seen already
    if (descendants.contains(nid)) {
      return;
    }

    // Process this concept
    ConceptVersionBI concept = OTFUtility.getConceptVersion(nid);

    // return if inactive
    if (!concept.isActive()) {
      return;
    }

    // return if the path of the concept is not valid
    if (concept.getNid() != topNid
        && !validPaths.contains(concept.getPathNid())) {
      return;
    }

    progressCount++;

    // Support for multiple threads
    synchronized (this) {
      descendants.add(nid);
    }
    convertToOntologyObjects(topNid, concept);

    // Handle progress monitor - consider this part 0%-50% of the process
    final int currentProgress = (int) ((progressCount * 100) / progressMax);
    if (currentProgress > progress) {
      progress = currentProgress;
      fireProgressEvent((int) (progress * .5), "Preparing data");
    }
    Set<Thread> threads = new HashSet<>();
    final boolean[] b = new boolean[1];
    final Exception[] exceptions = new Exception[1];
    b[0] = false;
    for (RelationshipVersionBI<?> r : concept
        .getRelationshipsIncomingActiveIsa()) {
      if (r.isStated() && r.isActive()) {
        // Spin off separate threads for top level things
        if (descendants.size() == 1) {
          Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
              LOG.info("Create THREAD for " + r.getOriginNid());
              try {
                getAllDescendants(topNid, r.getOriginNid(), descendants);
              } catch (Exception e) {
                e.printStackTrace();
                b[0] = true;
                exceptions[0] = e;
              }
            }
          });
          threads.add(t);
        } else {
          getAllDescendants(topNid, r.getOriginNid(), descendants);
        }
      }
    }
    // If top-level call, wait for threads
    if (threads.size() > 0) {
      for (Thread t : threads) {
        LOG.info("Start THREAD " + t);
        t.start();
      }
      for (Thread t : threads) {
        LOG.info("Join THREAD " + t);
        t.join();
      }
    }
  }

  /**
   * Adds the previous inferred rel.
   *
   * @param nid the nid
   * @param rel the rel
   */
  @SuppressWarnings("static-method")
  private void addPreviousInferredRel(int nid, Relationship rel) {
    List<Relationship> rels = null;
    previousInferredRelsCt++;
    if (previousInferredRels.containsKey(nid)) {
      rels = previousInferredRels.get(nid);
    } else {
      rels = new ArrayList<>();
      previousInferredRels.put(nid, rels);
    }
    previousInferredRels.get(nid).add(rel);
  }

  /**
   * Removes the previous inferred rel.
   *
   * @param nid the nid
   * @param rel the rel
   */
  @SuppressWarnings("static-method")
  private void removePreviousInferredRel(int nid, Relationship rel) {
    previousInferredRelsCt--;
    if (previousInferredRels.containsKey(nid)) {
      previousInferredRels.get(nid).remove(rel);
    }
  }

  /**
   * Returns the role descendants.
   *
   * @param topNid the top nid
   * @param nid the nid
   * @param descendants the descendants
   * @throws Exception the exception
   */
  public void getRoleDescendants(int topNid, int nid, IntSet descendants)
    throws Exception {

    // return if seen already
    if (descendants.contains(nid)) {
      return;
    }

    // Process this concept
    ConceptVersionBI concept = OTFUtility.getConceptVersion(nid);

    // return if inactive
    if (!concept.isActive()) {
      return;
    }

    // return if the path of the concept is not valid
    if (!validPaths.contains(concept.getPathNid())) {
      return;
    }

    if (nid != topNid) {
      descendants.add(nid);
    }
    for (RelationshipVersionBI<?> r : concept
        .getRelationshipsIncomingActiveIsa()) {
      if (r.isStated() && r.isActive()) {
        getRoleDescendants(topNid, r.getOriginNid(), descendants);
      }
    }
  }

  /**
   * Save cycle check report.
   *
   * @param conceptInLoop the concept in loop
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void saveCycleCheckReport(Set<Integer> conceptInLoop)
    throws IOException {
    if (!saveCycleCheckReport) {
      return;
    }
    if (conceptInLoop == null) {
      return;
    }
    FileOutputStream fos = new FileOutputStream(cycleCheckReport);
    OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
    BufferedWriter bw = new BufferedWriter(osw);
    bw.append("conceptId");
    bw.append("\r\n");
    for (Integer concept : conceptInLoop) {
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
  @SuppressWarnings("null")
  public void saveEquivalentConceptsReport(EquivalentClasses equivalentClasses,
    String fName) {
    if (!saveEquivalentConceptsReport) {
      return;
    }
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

  /**
   * Continue work.
   *
   * @return true, if successful
   */
  public boolean continueWork() {
    return !requestCancel;
  }

  /**
   * Convert to ontology objects.
   *
   * @param topNid the top nid
   * @param concept the concept
   * @throws Exception the exception
   */
  private synchronized void convertToOntologyObjects(int topNid,
    ConceptVersionBI concept) throws Exception {

    // Add concept
    StringIDConcept stringIdConcept =
        new StringIDConcept(concept.getNid(), String.valueOf(concept.getNid()),
            concept.getConceptAttributes()
                .getVersion(OTFUtility.getViewCoordinate()).isDefined());

    // save so we can map nids to their classifier ids.
    nidSeen.add(concept.getNid());
    editedSnomedConcepts.add(stringIdConcept);

    // Add concept for cycle check
    int nid = concept.getNid();
    cycleCheckConcepts.put(nid, false);
    List<Integer> parentIds = new ArrayList<>();
    if (cycleCheckRelationships.containsKey(nid)) {
      parentIds = cycleCheckRelationships.get(nid);
    }
    cycleCheckRelationships.put(nid, parentIds);

    // Do not process relationships for the top nid (e.g. SNOMED root)
    if (concept.getNid() == topNid) {
      return;
    }

    // Iterate through relationships
    for (RelationshipVersionBI<?> relationship : concept
        .getRelationshipsOutgoingActive()) {

      // skip inactive
      if (!relationship.isActive()) {
        continue;
      }

      // return if the path of the rel is not valid
      if (!validPaths.contains(relationship.getPathNid())) {
        return;
      }

      Relationship rel =
          new Relationship(relationship.getConceptNid(),
              relationship.getDestinationNid(), relationship.getTypeNid(),
              relationship.getGroup(), String.valueOf(relationship.getNid()));
      if (relationship.isStated()) {
        // add stated rels for classification
        editedSnomedRels.add(rel);

        // add stated rel for cycle check
        if (relationship.getTypeNid() == Snomed.IS_A.getLenient().getNid()) {
          parentIds.add(relationship.getDestinationNid());

        }
      } else if (relationship.isInferred()) {
        // Add prior inferred rels
        addPreviousInferredRel(relationship.getConceptNid(), rel);
      } else {
        // these are the "additional" relationships, ignore them
      }
    }
    cycleCheckRelationships.put(nid, parentIds);

  }

  /**
   * Returns the roles of the parent concept UUID.
   *
   * @param parent the parent concept UUID
   * @return the roles
   * @throws Exception the exception
   */
  private int[] getRoles(UUID parent) throws Exception {
    LOG.info("  Get Roles - " + parent);

    // Get descendants of the parent UUID passed in
    // do not include concept for this UUID
    IntSet childNids = new IntSet();
    getRoleDescendants(OTFUtility.getConceptVersion(parent).getNid(),
        OTFUtility.getConceptVersion(parent).getNid(), childNids);

    // save where concepts have been found
    IntSet children = new IntSet();
    for (int childNid : childNids.getSetValues()) {
      // Keep only entries for which concepts exist,
      // others are likely in different paths
      // this also sort uniques any duplicates from previous call
      if (nidSeen.contains(childNid)) {
        children.add(childNid);
      }
    }

    // Create the roles array we will return with the appropriate size
    int[] roles = new int[children.size() + 1];

    // Add "isa"
    LOG.info("    find role - "
        + OTFUtility.getConPrefTerm(Snomed.IS_A.getNid()));

    roles[0] = Snomed.IS_A.getNid();

    // Add remaining roles given the concept ids they were assigned in
    // converToOntologyObjects
    int i = 1;
    for (int childNid : children.getSetValues()) {
      LOG.info("    find role - " + childNid + ", "
          + OTFUtility.getConPrefTerm(childNid));
      roles[i++] = childNid;
    }
    return roles;
  }

  /**
   * To string lapse sec.
   *
   * @param startTime the start time
   * @return the string
   */
  @SuppressWarnings("static-method")
  private String toStringLapseSec(long startTime) {
    StringBuilder s = new StringBuilder();
    long stopTime = System.currentTimeMillis();
    long lapseTime = stopTime - startTime;
    s.append((float) lapseTime / 1000).append(" (seconds)");
    return s.toString();
  }

  /**
   * Processes Results.
   */
  private class ProcessResults implements I_Callback {

    /** The snorels. */
    private Map<Integer, List<Relationship>> snorels;

    /** The count rel. */
    int countRel = 0; // STATISTICS COUNTER

    /**
     * Instantiates a new process results.
     *
     * @param snorels the snorels
     */
    public ProcessResults(Map<Integer, List<Relationship>> snorels) {
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
      List<Relationship> rels = snorels.get(conceptId1);
      if (rels == null) {
        rels = new ArrayList<>();
        snorels.put(conceptId1, rels);
      }
      rels.add(relationship);
      if (countRel % 25000 == 0) {
        LOG.info("rels processed " + countRel);
      }
    }
  }

  /**
   * For processing equivalencies.
   */
  private class ProcessEquivalents implements I_EquivalentCallback {

    /** The count con set. */
    int countConSet = 0; // STATISTICS COUNTER

    /** The equiv concept. */
    private EquivalentClasses equivalentClasses;

    /**
     * Instantiates a new process equiv.
     */
    public ProcessEquivalents() {
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
    LOG.info(note + " ... " + pct + "% finished");
    ProgressEvent pe =
        new ProgressEvent(this, pct, pct, note + " ... " + pct + "% finished");
    for (int i = 0; i < listeners.size(); i++) {
      listeners.get(i).updateProgress(pe);
    }
  }

  /**
   * Cancel operation.
   */
  @Override
  public void cancel() {
    requestCancel = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.va.isaac.classifier.Classifier#incrementalClassify(org.ihtsdo.otf.tcc
   * .api.nid.IntSet)
   */
  @Override
  public void incrementalClassify() throws Exception {

    // Full classification is needed
    if (rocket_123 == null) {
      LOG.warn("Full classification must be done before incremental classification can be used.");
      return;
    }

    // add concepts - add to cycle check data structures too
    // add relationships - add to cycle check data structures too
    boolean classifiableConceptFound = false;
    for (int nid : incrementalClassificationSet.getSetValues()) {

      ConceptVersionBI concept = OTFUtility.getConceptVersion(nid);
      // return if the path of the concept is not valid
      if (!validPaths.contains(concept.getPathNid())) {
        continue;
      }

      // Indicate defined
      ConceptAttributeVersionBI<?> attributes =
          ConceptViewerHelper.getConceptAttributes(concept);
      classifiableConceptFound = true;
      rocket_123.addConcept(nid, attributes.isDefined());

      // Add cycle check info
      cycleCheckConcepts.put(nid, false);
      List<Integer> parentIds = new ArrayList<>();
      if (cycleCheckRelationships.containsKey(nid)) {
        parentIds = cycleCheckRelationships.get(nid);
      }
      cycleCheckRelationships.put(nid, parentIds);

      // Iterate through relationships
      for (RelationshipVersionBI<?> relationship : concept
          .getRelationshipsOutgoingActive()) {

        if (relationship.isStated()) {
          // add stated rels for classification
          rocket_123.addRelationship(relationship.getConceptNid(),
              relationship.getTypeNid(), relationship.getDestinationNid(),
              relationship.getGroup());

          // add stated rel for cycle check
          if (relationship.getTypeNid() == Snomed.IS_A.getLenient().getNid()) {
            parentIds.add(relationship.getDestinationNid());

          }
        } else if (relationship.isInferred()) {
          // Add inferred rels to previous inferred rels
          Relationship rel =
              new Relationship(relationship.getConceptNid(),
                  relationship.getDestinationNid(), relationship.getTypeNid(),
                  relationship.getGroup(),
                  String.valueOf(relationship.getNid()));
          // Add prior inferred rels
          addPreviousInferredRel(relationship.getConceptNid(), rel);
        }
      }
      cycleCheckRelationships.put(nid, parentIds);

    }

    // Bail if no classifiable concepts found
    if (!classifiableConceptFound) {
      return;
    }

    // Cycle check - at this point data structures are loaded
    CycleCheck cycleCheck = new CycleCheck();
    cycleCheck.setConcepts(cycleCheckConcepts);
    cycleCheck.setIsarelationships(cycleCheckRelationships);
    if (cycleCheck.getConceptInLoop() != null) {
      throw new Exception("A cycle has been detected.");
    }

    // classify
    rocket_123.classify();

    // Handle equivalents
    ProcessEquivalents pe = new ProcessEquivalents();
    rocket_123.getEquivalents(pe);
    saveEquivalentConceptsReport(pe.getEquivalentClasses(),
        equivalentConceptsReport);

    // handle inferred relationship changes
    // Get distribution form of relationships and write back
    snorocketRels = new HashMap<>();
    ProcessResults pr = new ProcessResults(snorocketRels);
    rocket_123.getDistributionFormRelationships(pr);

    // Write back results - disable commit listeners during this
    try {
      AppContext.getRuntimeGlobals().disableAllCommitListeners();
      List<Relationship> prevInferredRelsList = new ArrayList<>();
      List<Relationship> snorocketRelsList = new ArrayList<>();
      for (int nid : incrementalClassificationSet.getSetValues()) {
        prevInferredRelsList.addAll(previousInferredRels.get(nid));
        snorocketRelsList.addAll(snorocketRels.get(nid));
      }
      LOG.info(compareAndWriteBack(prevInferredRelsList, snorocketRelsList));
      dataStore.commit();
    } catch (Exception e) {
      throw e;
    } finally {
      AppContext.getRuntimeGlobals().disableAllCommitListeners();
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.va.isaac.classifier.Classifier#addToIncrementalClassificationSet(org
   * .ihtsdo.otf.tcc.api.nid.IntSet)
   */
  @Override
  public void addToIncrementalClassificationSet(IntSet conceptSet) {
    // Skip if full classification hasn't been run
    if (rocket_123 != null) {
      for (int nid : conceptSet.getSetValues()) {
        incrementalClassificationSet.add(nid);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.classifier.Classifier#isIncrementalClassifyReady()
   */
  @Override
  public boolean isIncrementalClassifyReady() {
    // If classifier data structures are populated, we're ready
    return rocket_123 != null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.classifier.Classifier#clearStaticState()
   */
  @Override
  public void clearStaticState() {
    // Essentially reset the classifier for a new full classification
    previousInferredRels = new HashMap<>();
    previousInferredRelsCt = 0;
    cycleCheckConcepts = new HashMap<>(20000);
    cycleCheckRelationships = new HashMap<>(20000);
    incrementalClassificationSet = new IntSet();
    rocket_123 = null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.classifier.Classifier#setSaveCycleCheckReport(boolean)
   */
  @Override
  public void setSaveCycleCheckReport(boolean flag) {
    saveCycleCheckReport = flag;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.va.isaac.classifier.Classifier#setSaveEquivalentConceptsReport(boolean)
   */
  @Override
  public void setSaveEquivalentConceptsReport(boolean flag) {
    saveEquivalentConceptsReport = flag;
  }

}
