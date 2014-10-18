package gov.va.isaac.ie.exporter;

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerHelper;
import gov.va.isaac.models.util.CommonBase;
import gov.va.isaac.util.ProgressEvent;
import gov.va.isaac.util.ProgressListener;
import gov.va.isaac.util.WBUtility;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.concept.ConceptFetcherBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.Taxonomies;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for exporting SNOMED to Owl. NOTE: this only works presently for
 * SNOMED and US English.
 *
 * @author Tim Kao
 */
public class OWLExporter extends CommonBase implements
   Exporter, ProcessUnfetchedConceptDataBI {
  

  /** Listeners */
  private List<ProgressListener> listeners = new ArrayList<>();

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(OWLExporter.class);

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

  /** The manager. */
  private static OWLOntologyManager manager = OWLManager
      .createOWLOntologyManager();

  /** The snomed ontology. */
  private static OWLOntology snomed = null;

  /** The owl data factory. */
  private static OWLDataFactory factory = manager.getOWLDataFactory();

  /** The pm. */
  private static PrefixManager pm = new DefaultPrefixManager("id/");

  /** The format. */
  private static OWLOntologyFormat format = new RDFXMLOntologyFormat();

  /** The set of axioms. */
  private Set<OWLAxiom> setOfAxioms = new HashSet<>();

  /**  The request cancel. */
  private boolean requestCancel = false;
  
  //
  // Hardcoded SNOMED info
  //

  /** The snomed iri. */
  private static IRI snomedIRI = IRI
      .create("http://snomed.info/sct/900000000000207008");

  // todo: can I get this from somewhere?
  /** The snomed version iri. */
  private static IRI snomedVersionIRI = IRI
      .create("http://snomed.info/sct/900000000000207008/version/20140131");

  /** The snomed namespace. */
  private static String snomedNamespace = "http://snomed.info/";

  /** The snomed ontology name. */
  private static String snomedOntologyName =
      "SNOMED Clinical Terms, International Release, Stated Relationships in OWL RDF";

  /** The snomed role group concept id. */
  private static String snomedRoleGroupConceptId = "69096000";

  /** The snomed en us preferred annotation. */
  private static String snomedEnUsPreferredAnnotation =
      "http://snomed.info/field/sctp:en-us.preferred";

  /** The snomed en us synonym annotation. */
  private static String snomedEnUsSynonymAnnotation =
      "http://snomed.info/field/sctp:en-us.synonym";

  /** The snomed text definition annotation. */
  private static String snomedTextDefinitionAnnotation =
      "http://snomed.info/field/sctf:TextDefinition.term";

  /** The top of the concept model attributs tree */
  /** do not render as a property or with sub properties */
  private static String snomedConceptAttributeModelConcept = "410662002";

  /** the count so far */
  private int progress = 0;

  /** the toal */
  private int progressMax = 0;

  /**
   * Returns the logger.
   *
   * @return the logger
   */
  @SuppressWarnings("static-method")
  protected Logger getLogger() {
    return LOG;
  }

  /**
   * Instantiates a {@link OWLExporter} from the specified parameters.
   *
   * @param fileOutputStream the file output stream
   * @throws Exception if something goes wrong in constructor
   */
  public OWLExporter(OutputStream fileOutputStream) throws Exception {
    dos = new DataOutputStream(new BufferedOutputStream(fileOutputStream));
    if (snomed == null) {
      // Create a new ontology
      snomed =
          manager
              .createOntology(new OWLOntologyID(snomedIRI, snomedVersionIRI));
      format.setParameter("xml:base", snomedNamespace);

      // Obtain SNOMED root concept
      // TODO: this needs to be generalized
      UUID snomedRootUUID = Taxonomies.SNOMED.getUuids()[0];
      ConceptVersionBI snomedRootConcept =
          WBUtility.getConceptVersion(snomedRootUUID);

      // Add annotation based on root concept
      for (DescriptionVersionBI<?> desc : snomedRootConcept
          .getDescriptionsActive()) {
        if (desc.getText().contains("Release")) {
          manager.applyChange(new AddOntologyAnnotation(snomed, factory
              .getOWLAnnotation(factory
                  .getOWLAnnotationProperty(OWLRDFVocabulary.OWL_VERSION_INFO
                      .getIRI()), factory.getOWLLiteral(desc.getText()))));
        }
        if (desc.getText().contains("IHTSDO")) {
          manager.applyChange(new AddOntologyAnnotation(snomed, factory
              .getOWLAnnotation(factory
                  .getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT
                      .getIRI()), factory.getOWLLiteral(desc.getText()))));
        }
      }
      manager.applyChange(new AddOntologyAnnotation(snomed, factory
          .getOWLAnnotation(factory
              .getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()),
              factory.getOWLLiteral(snomedOntologyName))));
    }
  }

  /**
   * Exports the indicated path nid.
   *
   * @param pathNid the path nid
   * @throws Exception the exception
   */
  @Override
  public void export(int pathNid) throws Exception {
    this.pathNid = pathNid;
    dataStore.iterateConceptDataInSequence(this);
    manager.addAxioms(snomed, setOfAxioms);
    manager.saveOntology(snomed, format, dos);
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
    if (Thread.currentThread().isInterrupted()) throw new InterruptedException();
    ConceptVersionBI concept = fetcher.fetch(WBUtility.getViewCoordinate());
    LOG.debug("Process concept " + concept.getPrimordialUuid());
    allCount++;
    if (Exporter.isQualifying(concept.getNid(),pathNid)) {
      count++;
      convertToOWLObjects(concept);
    }
    // Handle progress monitor
    if ((int) ((allCount * 100) / progressMax) > progress) {
      progress = (allCount * 100) / progressMax;
      fireProgressEvent(progress, progress + " % finished");
    }
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
   * Convert to owl objects.
   *
   * @param currentConcept the current concept
   * @throws Exception the exception
   */
  public void convertToOWLObjects(ConceptVersionBI currentConcept)
    throws Exception {

    // Bail on empty concepts
    if (currentConcept == null) {
      return;
    }

    // Create an Owl class for each concept
    OWLClass currentConceptClass =
        createOWLAxiomsFromConceptVersionBI(setOfAxioms, factory, pm,
            currentConcept);

    // Get labels
    for (DescriptionVersionBI<?> desc : currentConcept.getDescriptionsActive()) {
      // .getSynonyms was not working, thus this work around
      if (desc.getTypeNid() == Snomed.SYNONYM_DESCRIPTION_TYPE.getNid()
          && !desc.equals(currentConcept.getPreferredDescription())
          && isUSLanguageRefex(desc)) {
        OWLAnnotation synonymsTerm =
            factory.getOWLAnnotation(factory.getOWLAnnotationProperty(IRI
                .create(snomedEnUsSynonymAnnotation)), factory.getOWLLiteral(
                desc.getText(), desc.getLang()));
        setOfAxioms.add(factory.getOWLAnnotationAssertionAxiom(
            currentConceptClass.getIRI(), synonymsTerm));
      } else if (desc.getTypeNid() == Snomed.DEFINITION_DESCRIPTION_TYPE
          .getNid() && isUSLanguageRefex(desc)) {
        // TextDefinitions
        OWLAnnotation textDefinition =
            factory.getOWLAnnotation(factory.getOWLAnnotationProperty(IRI
                .create(snomedTextDefinitionAnnotation)), factory
                .getOWLLiteral(desc.getText(), desc.getLang()));
        setOfAxioms.add(factory.getOWLAnnotationAssertionAxiom(
            currentConceptClass.getIRI(), textDefinition));
      }
    }

    // Parent relationships
    Set<OWLClass> parentClasses = new HashSet<>();
    for (RelationshipVersionBI<?> rel : currentConcept
        .getRelationshipsOutgoingActiveIsa()) {
      parentClasses.add(factory.getOWLClass(
          ":"
              + getSnomedConceptID(WBUtility.getConceptVersion(rel
                  .getDestinationNid())), pm));
    }

    // Handle other relationships
    // role group
    OWLObjectProperty groupPart =
        factory.getOWLObjectProperty(":" + snomedRoleGroupConceptId, pm);
    // relationships
    SortedMap<Integer, Set<OWLClassExpression>> relationshipGroups =
        new TreeMap<>();
    Set<OWLClassExpression> neverGroupedRelationships = new HashSet<>();
    for (RelationshipVersionBI<?> rel : currentConcept
        .getRelationshipsOutgoingActive()) {
      // Skip the is a relationship, these are taken care of in the parents
      // relationships
      if (rel.getTypeNid() == Snomed.IS_A.getNid()) {
        continue;
      }

      OWLObjectProperty relationshipTypeProperty =
          createOWLPropertyAxiomsFromConceptVersionBI(setOfAxioms, factory, pm,
              WBUtility.getConceptVersion(rel.getTypeNid()));
      OWLClass destinationClass =
          factory.getOWLClass(
              ":"
                  + getSnomedConceptID(WBUtility.getConceptVersion(rel
                      .getDestinationNid())), pm);

      OWLClassExpression owlRel =
          factory.getOWLObjectSomeValuesFrom(relationshipTypeProperty,
              destinationClass);
      if (rel.getTypeNid() == Snomed.LATERALITY.getNid() || rel.getTypeNid() == Snomed.PART_OF.getNid()
        || rel.getTypeNid() == Snomed.HAS_ACTIVE_INGREDIENT.getNid() || rel.getTypeNid() == Snomed.HAS_DOSE_FORM.getNid()) {
          neverGroupedRelationships.add(owlRel);
      }
      if (relationshipGroups.containsKey(rel.getGroup())) {
        relationshipGroups.get(rel.getGroup()).add(owlRel);
      } else {
        Set<OWLClassExpression> relationships = new HashSet<>();
        relationships.add(owlRel);
        relationshipGroups.put(rel.getGroup(), relationships);
      }
    }
    OWLClassExpression owlRelationships = null;
    if (relationshipGroups.isEmpty()) {
      if (parentClasses.size() == 1) {
        owlRelationships = parentClasses.iterator().next();
      } else if (parentClasses.size() > 1) {
        owlRelationships = factory.getOWLObjectIntersectionOf(parentClasses);
      }
    } else {
      Set<OWLClassExpression> owlInnerRelationships = new HashSet<>();
      owlInnerRelationships.addAll(parentClasses);
      for (int group : relationshipGroups.keySet()) {
        OWLClassExpression innerRelationship = null;
        if (relationshipGroups.get(group).size() > 1) {
          //Separate into two sets, the nevergrouped and the normally grouped
          Set<OWLClassExpression> groupedRels = new HashSet<>();
          for(OWLClassExpression rel : relationshipGroups.get(group)) {
              if (neverGroupedRelationships.contains(rel)) {
                  owlInnerRelationships.add(rel);
              } else {
                groupedRels.add(rel);
              }
          }
          if (!groupedRels.isEmpty()) {
              innerRelationship =
                      factory.getOWLObjectIntersectionOf(groupedRels);
          }
        } else {
          OWLClassExpression singleRelationship = relationshipGroups.get(group).iterator().next();
          if (neverGroupedRelationships.contains(singleRelationship)) {
            owlInnerRelationships.add(singleRelationship);
          } else {
            innerRelationship = singleRelationship;
          }
        }

        if (innerRelationship != null) {
            owlInnerRelationships.add(factory.getOWLObjectSomeValuesFrom(groupPart,
                    innerRelationship));
        }
      }
      owlRelationships =
          factory.getOWLObjectIntersectionOf(owlInnerRelationships);
    }

    if (owlRelationships != null) {
      if (currentConcept.getConceptAttributes()
          .getVersion(WBUtility.getViewCoordinate()).isDefined()) {
        setOfAxioms.add(factory.getOWLEquivalentClassesAxiom(
            currentConceptClass, owlRelationships));
      } else {
        setOfAxioms.add(factory.getOWLSubClassOfAxiom(currentConceptClass,
            owlRelationships));
      }
    }
  }

  // Helper methods
  /**
   * Creates the owl axioms from concept version bi.
   *
   * @param setOfAxioms the set of axioms
   * @param factory the factory
   * @param pm the pm
   * @param conceptVersionBI the concept version bi
   * @return the OWL class
   * @throws Exception the exception
   */
  private OWLClass createOWLAxiomsFromConceptVersionBI(
    Set<OWLAxiom> setOfAxioms, OWLDataFactory factory, PrefixManager pm,
    ConceptVersionBI conceptVersionBI) throws Exception {
    // Declaration
    OWLClass owlClass =
        factory.getOWLClass(":" + getSnomedConceptID(conceptVersionBI), pm);
    OWLDeclarationAxiom declarationAxiom =
        factory.getOWLDeclarationAxiom(owlClass);
    setOfAxioms.add(declarationAxiom);
    // Create annotation type axiom
    // Fully specified description
    OWLAnnotation labelAnnotation =
        factory.getOWLAnnotation(factory.getRDFSLabel(), factory.getOWLLiteral(
            conceptVersionBI.getFullySpecifiedDescription().getText(),
            conceptVersionBI.getFullySpecifiedDescription().getLang()));
    OWLAxiom labelAnnotationAxiom =
        factory.getOWLAnnotationAssertionAxiom(owlClass.getIRI(),
            labelAnnotation);
    setOfAxioms.add(labelAnnotationAxiom);

    // Preferred name
    OWLAnnotation preferredTerm =
        factory.getOWLAnnotation(factory.getOWLAnnotationProperty(IRI
            .create(snomedEnUsPreferredAnnotation)), factory.getOWLLiteral(
            conceptVersionBI.getPreferredDescription().getText(),
            conceptVersionBI.getPreferredDescription().getLang()));
    OWLAxiom preferredTermAx =
        factory
            .getOWLAnnotationAssertionAxiom(owlClass.getIRI(), preferredTerm);
    setOfAxioms.add(preferredTermAx);

    return owlClass;
  }

  /**
   * Creates the owl property axioms from concept version bi.
   *
   * @param setOfAxioms the set of axioms
   * @param factory the factory
   * @param pm the pm
   * @param conceptVersionBI the concept version bi
   * @return the OWL object property
   * @throws Exception the exception
   */
  private OWLObjectProperty createOWLPropertyAxiomsFromConceptVersionBI(
    Set<OWLAxiom> setOfAxioms, OWLDataFactory factory, PrefixManager pm,
    ConceptVersionBI conceptVersionBI) throws Exception {
    // Declaration
    OWLObjectProperty owlPropertyClass =
        factory.getOWLObjectProperty(
            ":" + getSnomedConceptID(conceptVersionBI), pm);
    OWLDeclarationAxiom declarationAxiom =
        factory.getOWLDeclarationAxiom(owlPropertyClass);
    setOfAxioms.add(declarationAxiom);
    // Create annotation type axiom
    // Fully specified description
    OWLAnnotation labelAnnotation =
        factory.getOWLAnnotation(factory.getRDFSLabel(), factory.getOWLLiteral(
            conceptVersionBI.getFullySpecifiedDescription().getText(),
            conceptVersionBI.getFullySpecifiedDescription().getLang()));
    OWLAxiom labelAnnotationAxiom =
        factory.getOWLAnnotationAssertionAxiom(owlPropertyClass.getIRI(),
            labelAnnotation);
    setOfAxioms.add(labelAnnotationAxiom);

    // Preferred name
    OWLAnnotation preferredTerm =
        factory.getOWLAnnotation(factory.getOWLAnnotationProperty(IRI
            .create(snomedEnUsPreferredAnnotation)), factory.getOWLLiteral(
            conceptVersionBI.getPreferredDescription().getText(),
            conceptVersionBI.getPreferredDescription().getLang()));
    OWLAxiom preferredTermAx =
        factory.getOWLAnnotationAssertionAxiom(owlPropertyClass.getIRI(),
            preferredTerm);
    setOfAxioms.add(preferredTermAx);

    // SubObjectPropertyOf
    // SubObjectPropertyOf(ObjectPropertyChain(
    // <http://snomed.info/id/363701004> <http://snomed.info/id/127489000> )
    // <http://snomed.info/id/363701004>)
    for (RelationshipVersionBI<?> rel : conceptVersionBI
        .getRelationshipsOutgoingActiveIsa()) {
      String sctid =
          getSnomedConceptID(WBUtility.getConceptVersion(rel
              .getDestinationNid()));
      // Skip the root of the attributes tree
      if (!sctid.equals(snomedConceptAttributeModelConcept)) {
        OWLObjectProperty parent =
            factory.getOWLObjectProperty(":" + sctid, pm);
        setOfAxioms.add(factory.getOWLSubObjectPropertyOfAxiom(
            owlPropertyClass, parent));
      }
    }

    return owlPropertyClass;
  }

  /**
   * Indicates whether or not US language refex is the case.
   *
   * @param desc the desc
   * @return <code>true</code> if so, <code>false</code> otherwise
   * @throws Exception the exception
   */
  private boolean isUSLanguageRefex(DescriptionVersionBI<?> desc)
    throws Exception {
    for (RefexVersionBI<?> annotation : desc.getAnnotationsActive(WBUtility
        .getViewCoordinate())) {
      if (annotation.getAssemblageNid() == Snomed.US_LANGUAGE_REFEX.getNid()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the snomed concept id.
   *
   * @param conceptVersionBI the concept version bi
   * @return the snomed concept id
   * @throws Exception the exception
   */
  private String getSnomedConceptID(ConceptVersionBI conceptVersionBI)
    throws Exception {
    String id = ConceptViewerHelper.getSctId(conceptVersionBI).trim();
    if ("Unreleased".equalsIgnoreCase(id)) {
      return conceptVersionBI.getPrimordialUuid().toString();
    } else {
      // do nothing
    }
    return id;
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

  /**
   * Cancel.
   */
  @Override
  public void cancel() {
    requestCancel = true;    
  }

}
