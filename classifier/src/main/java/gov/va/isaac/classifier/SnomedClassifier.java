package gov.va.isaac.classifier;

import java.io.IOException;

import org.ihtsdo.otf.tcc.api.concept.ConceptFetcherBI;
import org.ihtsdo.otf.tcc.api.concept.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;

/**
 * SnomedClassifier.
 */
public class SnomedClassifier implements ProcessUnfetchedConceptDataBI {

  @Override
  public boolean continueWork() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean allowCancel() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public NativeIdSetBI getNidSet() throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getTitle() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void processUnfetchedConceptData(int arg0, ConceptFetcherBI arg1)
    throws Exception {
    // TODO Auto-generated method stub

  }
  /**
   * For later private static final Logger LOG = LoggerFactory
   * .getLogger(SnomedClassifier.class);
   * 
   * private static String ROLE_GROUP = "19657985-279a-4713-83be-e3d1a884b50d";
   * 
   * private static UUID CONCEPT_ATTRIBUTE =
   * UUID.fromString("6155818b-09ed-388e-82ce-caa143423e99");
   * 
   * private static BdbTerminologyStore dataStore = ExtendedAppContext
   * .getDataStore();
   * 
   * private Map<String, Concept> concepts = new HashMap<String, Concept>();
   * 
   * private Map<String, Role> roles = new HashMap<String, Role>();
   * 
   * private Set<Axiom> axioms = new HashSet<Axiom>();
   * 
   * private int count = 0;
   * 
   * private int pathNid;
   * 
   * protected Logger getLogger() { return LOG; }
   * 
   * public SnomedClassifier() { }
   * 
   * public void classify(int pathNid) throws Exception { this.pathNid =
   * pathNid; getRole(ROLE_GROUP); LOG.info("Building axioms ");
   * dataStore.iterateConceptDataInSequence(this); LOG.info("Axioms " +
   * axioms.size()); addRoleAxioms(CONCEPT_ATTRIBUTE); LOG.info("Total Axioms "
   * + axioms.size()); IReasoner reasoner = new SnorocketReasoner();
   * reasoner.loadAxioms(axioms); long startTime = System.currentTimeMillis();
   * LOG.info("Running Classifier"); reasoner.classify();
   * LOG.info("Time to classify (ms): " + (System.currentTimeMillis() -
   * startTime)); Ontology ontology = reasoner.getClassifiedOntology();
   * Map<String,Set<String>> equivalentMap = new HashMap<String, Set<String>>();
   * for(String id : ontology.getNodeMap().keySet()) { Node node =
   * ontology.getNode(id); if(node.getEquivalentConcepts().size() == 1 &&
   * node.getEquivalentConcepts().contains(id)) { continue; }
   * equivalentMap.put(id, node.getEquivalentConcepts()); } for(String id :
   * equivalentMap.keySet()) { LOG.warn(id + " equivalents " +
   * equivalentMap.get(id)); } LOG.info("Inferred Axioms " +
   * ontology.getInferredAxioms().size()); for(Axiom axiom :
   * ontology.getInferredAxioms()) { LOG.info(axiom.toString()); } }
   * 
   * private void addRoleAxioms(UUID parent) throws IOException,
   * ContradictionException { ArrayList<ConceptVersionBI> children =
   * WBUtility.getAllChildrenOfConcept(WBUtility.getConceptVersion(parent),
   * false); if(children.isEmpty()) return; Role[] rlhs = new
   * Role[children.size()]; int i=0; for(ConceptVersionBI child : children) {
   * addRoleAxioms(child.getPrimordialUuid()); rlhs[i++] =
   * getRole(child.getPrimordialUuid().toString()); }
   * axioms.add(Factory.createRoleInclusion(rlhs, getRole(parent.toString())));
   * }
   * @Override public boolean continueWork() { return true; }
   * @Override public boolean allowCancel() { return false; }
   * @Override public void processUnfetchedConceptData(int cNid,
   *           ConceptFetcherBI fetcher) throws Exception { ConceptVersionBI
   *           concept = fetcher.fetch(WBUtility.getViewCoordinate()); if
   *           (concept.getPathNid() == pathNid) { count++;
   *           convertToOntologyObjects(concept); } }
   * 
   *           private void convertToOntologyObjects(ConceptVersionBI concept)
   *           throws IOException, ContradictionException {
   *           ConceptAttributeVersionBI<?> attributes =
   *           ConceptViewerHelper.getConceptAttributes(concept); //
   *           Set<Integer> relGroups Collection<? extends
   *           RelationshipVersionBI<?>> relationships =
   *           concept.getRelationshipsOutgoingActive(); Map<Integer, Concept>
   *           mapRoleGroup = mapToRoleGroup(relationships); List<Concept>
   *           parentConcepts = getParentConcepts(relationships); // Root
   *           Concept ? if (parentConcepts.isEmpty()) return; if
   *           (mapRoleGroup.isEmpty() && parentConcepts.size() == 1) { //
   *           Single Parent and no role relationships
   *           axioms.add(Factory.createConceptInclusion(getConcept(concept
   *           .getPrimordialUuid().toString()), parentConcepts.get(0))); } else
   *           { List<Concept> definition = new ArrayList<Concept>();
   *           definition.addAll(parentConcepts);
   *           definition.addAll(mapRoleGroup.values());
   *           axioms.add(Factory.createConceptInclusion(getConcept(concept
   *           .getPrimordialUuid().toString()), Factory
   *           .createConjunction((definition.toArray(new Concept[0]))))); if
   *           (attributes.isDefined()) {
   *           axioms.add(Factory.createConceptInclusion(
   *           Factory.createConjunction(definition.toArray(new Concept[0])),
   *           getConcept(concept.getPrimordialUuid().toString()))); }
   * 
   *           }
   * 
   *           }
   * 
   *           private List<Concept> getParentConcepts( Collection<? extends
   *           RelationshipVersionBI<?>> relationships) throws
   *           ValidationException, IOException { List<Concept> parentConcepts =
   *           new ArrayList<Concept>(); for (RelationshipVersionBI<?>
   *           relationship : relationships) { if (relationship.getTypeNid() ==
   *           Snomed.IS_A.getNid()) { parentConcepts.add(getConcept(WBUtility
   *           .getConceptVersion(relationship.getDestinationNid())
   *           .getPrimordialUuid().toString())); } } return parentConcepts; }
   * 
   *           private Map<Integer, Concept> mapToRoleGroup( Collection<?
   *           extends RelationshipVersionBI<?>> relationships) throws
   *           ValidationException, IOException { Map<Integer, List<Concept>>
   *           map = new HashMap<>(); Map<Concept, Integer> conceptToRoleNid =
   *           new HashMap<>(); // map roleGroup -> Concepts for
   *           (RelationshipVersionBI<?> relationship : relationships) { if
   *           (relationship.getGroup() == Snomed.IS_A.getNid()) continue;
   * 
   *           List<Concept> concepts = new ArrayList<Concept>();
   * 
   *           if (map.containsKey(relationship.getGroup())) concepts =
   *           map.get(relationship.getGroup()); else
   *           map.put(relationship.getGroup(), concepts);
   * 
   *           Concept existentialConcept = Factory.createExistential(
   *           getRole(WBUtility.getConceptVersion(relationship.getTypeNid())
   *           .getPrimordialUuid().toString()), getConcept(WBUtility
   *           .getConceptVersion(relationship.getDestinationNid())
   *           .getPrimordialUuid().toString()));
   *           concepts.add(existentialConcept);
   *           conceptToRoleNid.put(existentialConcept,
   *           relationship.getTypeNid()); } // map roleGroup ->
   *           Conjunction/Concept Map<Integer, Concept> roles = new
   *           HashMap<Integer, Concept>(); for (Integer group : map.keySet()) {
   *           List<Concept> concepts = map.get(group); if (concepts.size() ==
   *           1) { if (group != 0) throw new
   *           IOException("Unexpected single grouped rel."); if
   *           (isNonGroupingRole(conceptToRoleNid.get(concepts.get(0)))) {
   *           roles.put(group, concepts.get(0)); } else { roles.put(group,
   *           Factory.createExistential(getRole(ROLE_GROUP), concepts.get(0)));
   *           } } else { if (group == 0) { for (Concept concept : concepts) {
   *           if (isNonGroupingRole(conceptToRoleNid.get(concept))) {
   *           roles.put(group, concept); } else { roles.put(group,
   *           Factory.createExistential(getRole(ROLE_GROUP),concept)); } } }
   *           else { roles.put(group,
   *           Factory.createExistential(getRole(ROLE_GROUP),
   *           Factory.createConjunction(concepts.toArray(new Concept[0])))); }
   *           } } return roles; }
   * 
   *           private boolean isNonGroupingRole(int nid) throws
   *           ValidationException, IOException { // $nevergrouped{"123005000"}
   *           = "T"; # part-of is never grouped // //
   *           $nevergrouped{"272741003"} = "T"; # laterality is never grouped
   *           // // $nevergrouped{"127489000"} = "T"; # has-active-ingredient
   *           is never // grouped // // $nevergrouped{"411116001"} = "T"; #
   *           has-dose-form is never grouped return (nid ==
   *           Snomed.PART_OF.getNid() || nid == Snomed.LATERALITY.getNid()
   *           ||nid == Snomed.HAS_ACTIVE_INGREDIENT.getNid() || nid ==
   *           Snomed.HAS_DOSE_FORM.getNid()); }
   * 
   *           private Concept getConcept(String id) { if
   *           (!concepts.containsKey(id)) { concepts.put(id,
   *           Factory.createNamedConcept(id)); } return concepts.get(id); }
   * 
   *           private Role getRole(String id) { if (!roles.containsKey(id)) {
   *           roles.put(id, Factory.createNamedRole(id)); } return
   *           roles.get(id); }
   * @Override public NativeIdSetBI getNidSet() throws IOException { return
   *           dataStore.getAllConceptNids(); }
   * @Override public String getTitle() { return this.getClass().getName(); }
   **/
}
