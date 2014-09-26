package gov.va.isaac.models.owl.exporter;

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerHelper;
import gov.va.isaac.models.util.CommonBase;
import gov.va.isaac.util.WBUtility;
import org.ihtsdo.otf.tcc.api.concept.ConceptFetcherBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.*;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class OWLExporter extends CommonBase implements ProcessUnfetchedConceptDataBI {
    private static final Logger LOG = LoggerFactory.getLogger(OWLExporter.class);

    private static BdbTerminologyStore dataStore = ExtendedAppContext.getDataStore();

    private DataOutputStream dos;

    private int count = 0;

    private int pathNid;
    private static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    private static IRI snomedIRI = IRI.create("http://snomed.info/sct/900000000000207008");
    //todo: can I get this from somewhere?
    private static IRI snomedVersionIRI = IRI.create("http://snomed.info/sct/900000000000207008/version/20140131");
    private static OWLOntology snomed = null;
    private static OWLDataFactory factory = manager.getOWLDataFactory();
    private static PrefixManager pm = new DefaultPrefixManager("id/");
    private static OWLOntologyFormat format = new RDFXMLOntologyFormat();
    private Set<OWLAxiom> setOfAxioms = new HashSet<>();
    protected Logger getLogger() {
        return LOG;
    }

    public OWLExporter(OutputStream fileOutputStream) {
    		dos = new DataOutputStream(new BufferedOutputStream(fileOutputStream));

        try {
            if (snomed == null) {
                snomed = manager.createOntology(new OWLOntologyID(snomedIRI,snomedVersionIRI));
                format.setParameter("xml:base", "http://snomed.info/");
                UUID snomedRootUUID = Taxonomies.SNOMED.getUuids()[0];
                ConceptVersionBI snomedRootConcept = WBUtility.getConceptVersion(snomedRootUUID);
                for (DescriptionVersionBI desc : snomedRootConcept.getDescriptionsActive()) {
                    if (desc.getText().contains("Release")) {
                        manager.applyChange(new AddOntologyAnnotation(snomed, factory.getOWLAnnotation(factory
                                .getOWLAnnotationProperty(OWLRDFVocabulary.OWL_VERSION_INFO
                                        .getIRI()), factory.getOWLLiteral(desc.getText()))));
                    }
                    if (desc.getText().contains("IHTSDO")) {
                        manager.applyChange(new AddOntologyAnnotation(snomed, factory.getOWLAnnotation(factory
                                .getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT
                                        .getIRI()), factory.getOWLLiteral(desc.getText()))));
                    }
                }
                manager.applyChange(new AddOntologyAnnotation(snomed, factory.getOWLAnnotation(factory
                        .getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL
                                .getIRI()), factory.getOWLLiteral("SNOMED Clinical Terms, International Release, Stated Relationships in OWL RDF"))));
            }
        }catch(Exception e) {
            LOG.debug(e.toString());
        }
    }

    public void export(int pathNid) throws Exception {
   		this.pathNid = pathNid;
   		dataStore.iterateConceptDataInSequence(this);
        manager.addAxioms(snomed,setOfAxioms);
        manager.saveOntology(snomed, format,dos);
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
   		ConceptVersionBI concept = fetcher.fetch(WBUtility.getViewCoordinate());
   		if(concept.getPathNid() == pathNid) {
   			count++;
   			convertToOWLObjects(concept);
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

    public void convertToOWLObjects(ConceptVersionBI currentConcept) throws Exception {
        if (currentConcept == null) {
            return;
        }
        OWLClass currentConceptClass = createOWLAxiomsFromConceptVersionBI(setOfAxioms, factory, pm, currentConcept);

        for (DescriptionVersionBI desc : currentConcept.getDescriptionsActive()) {
            //.getSynonyms was not working, thus this work around
            if (desc.getTypeNid() == Snomed.SYNONYM_DESCRIPTION_TYPE.getNid() && !desc.equals(currentConcept.getPreferredDescription()) && isUSLanguageRefex(desc)) {
                OWLAnnotation synonymsTerm = factory.getOWLAnnotation(factory.getOWLAnnotationProperty(IRI.create("http://snomed.info/field/sctp:en-us.synonym")),
                        factory.getOWLLiteral(desc.getText(), desc.getLang()));
                setOfAxioms.add(factory.getOWLAnnotationAssertionAxiom(currentConceptClass.getIRI(),synonymsTerm));
            } else if (desc.getTypeNid() == Snomed.DEFINITION_DESCRIPTION_TYPE.getNid() && isUSLanguageRefex(desc)) {
                //TextDefinitions
                OWLAnnotation textDefinition = factory.getOWLAnnotation(factory.getOWLAnnotationProperty(IRI.create("http://snomed.info/field/sctf:TextDefinition.term")),
                        factory.getOWLLiteral(desc.getText(), desc.getLang()));
                setOfAxioms.add(factory.getOWLAnnotationAssertionAxiom(currentConceptClass.getIRI(),textDefinition));
            }
        }

        //Parent relationships
        Set<OWLClass> parentClasses = new HashSet<>();
        for (RelationshipVersionBI rel : currentConcept.getRelationshipsOutgoingActiveIsa()) {
            parentClasses.add(factory.getOWLClass(":" + getSnomedConceptID(WBUtility.getConceptVersion(rel.getDestinationNid())), pm));
        }

        //role group
        //todo: no idea where this is from in the snomed world, didn't see it in the owl input file, possibly relationship group active
        OWLObjectProperty groupPart = factory.getOWLObjectProperty(":69096000", pm);
        //relationships
        SortedMap<Integer,Set<OWLClassExpression>> relationshipGroups = new TreeMap<>();
        for (RelationshipVersionBI rel : currentConcept.getRelationshipsOutgoingActive()) {
            //Skip the is a relationship, these are taken care of in the parents relationships
            if (rel.getTypeNid() == Snomed.IS_A.getNid()) {
                continue;
            }

            OWLObjectProperty relationshipTypeProperty = createOWLPropertyAxiomsFromConceptVersionBI(setOfAxioms, factory, pm, WBUtility.getConceptVersion(rel.getTypeNid()));
            OWLClass destinationClass = factory.getOWLClass(":" + getSnomedConceptID(WBUtility.getConceptVersion(rel.getDestinationNid())), pm);

            OWLClassExpression owlRel = factory.getOWLObjectSomeValuesFrom(relationshipTypeProperty, destinationClass);
            if (relationshipGroups.containsKey(rel.getGroup())) {
                relationshipGroups.get(rel.getGroup()).add(owlRel);
            } else {
                Set<OWLClassExpression> relationships = new HashSet<>();
                relationships.add(owlRel);
                relationshipGroups.put(rel.getGroup(),relationships);
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
            for(int group : relationshipGroups.keySet()) {
                OWLClassExpression innerRelationship;
                if (relationshipGroups.get(group).size() > 1) {
                    innerRelationship = factory.getOWLObjectIntersectionOf(relationshipGroups.get(group));
                } else {
                    innerRelationship = relationshipGroups.get(group).iterator().next();
                }
                owlInnerRelationships.add(factory.getOWLObjectSomeValuesFrom(groupPart,innerRelationship));
            }
            owlRelationships = factory.getOWLObjectIntersectionOf(owlInnerRelationships);
        }

        if (owlRelationships != null) {
            if (currentConcept.getConceptAttributes().getVersion(WBUtility.getViewCoordinate()).isDefined()) {
                setOfAxioms.add(factory.getOWLEquivalentClassesAxiom(currentConceptClass, owlRelationships));
            } else {
                setOfAxioms.add(factory.getOWLSubClassOfAxiom(currentConceptClass, owlRelationships));
            }
        }
    }

    //Helper methods
    private OWLClass createOWLAxiomsFromConceptVersionBI(Set<OWLAxiom> setOfAxioms,  OWLDataFactory factory, PrefixManager pm, ConceptVersionBI conceptVersionBI) throws Exception{
        //Declaration
        OWLClass owlClass = factory.getOWLClass(":"+getSnomedConceptID(conceptVersionBI), pm);
        OWLDeclarationAxiom declarationAxiom = factory
                .getOWLDeclarationAxiom(owlClass);
        setOfAxioms.add(declarationAxiom);
        //Create annotation type axiom
        //Fully specified description
        OWLAnnotation labelAnnotation = factory.getOWLAnnotation(factory.getRDFSLabel(),
                factory.getOWLLiteral(conceptVersionBI.getFullySpecifiedDescription().getText(), conceptVersionBI.getFullySpecifiedDescription().getLang()));
        OWLAxiom labelAnnotationAxiom = factory.getOWLAnnotationAssertionAxiom(owlClass.getIRI(),
                labelAnnotation);
        setOfAxioms.add(labelAnnotationAxiom);

        //Preferred name
        OWLAnnotation preferredTerm = factory.getOWLAnnotation(factory.getOWLAnnotationProperty(IRI.create("http://snomed.info/field/sctp:en-us.preferred")),
                factory.getOWLLiteral(conceptVersionBI.getPreferredDescription().getText(), conceptVersionBI.getPreferredDescription().getLang()));
        OWLAxiom preferredTermAx = factory.getOWLAnnotationAssertionAxiom(owlClass.getIRI(),
                preferredTerm);
        setOfAxioms.add(preferredTermAx);

        return owlClass;
    }

    private OWLObjectProperty createOWLPropertyAxiomsFromConceptVersionBI(Set<OWLAxiom> setOfAxioms,  OWLDataFactory factory, PrefixManager pm, ConceptVersionBI conceptVersionBI) throws Exception{
        //Declaration
        OWLObjectProperty owlPropertyClass = factory.getOWLObjectProperty(":"+getSnomedConceptID(conceptVersionBI), pm);
        OWLDeclarationAxiom declarationAxiom = factory
                .getOWLDeclarationAxiom(owlPropertyClass);
        setOfAxioms.add(declarationAxiom);
        //Create annotation type axiom
        //Fully specified description
        OWLAnnotation labelAnnotation = factory.getOWLAnnotation(factory.getRDFSLabel(),
                factory.getOWLLiteral(conceptVersionBI.getFullySpecifiedDescription().getText(), conceptVersionBI.getFullySpecifiedDescription().getLang()));
        OWLAxiom labelAnnotationAxiom = factory.getOWLAnnotationAssertionAxiom(owlPropertyClass.getIRI(),
                labelAnnotation);
        setOfAxioms.add(labelAnnotationAxiom);

        //Preferred name
        OWLAnnotation preferredTerm = factory.getOWLAnnotation(factory.getOWLAnnotationProperty(IRI.create("http://snomed.info/field/sctp:en-us.preferred")),
                factory.getOWLLiteral(conceptVersionBI.getPreferredDescription().getText(), conceptVersionBI.getPreferredDescription().getLang()));
        OWLAxiom preferredTermAx = factory.getOWLAnnotationAssertionAxiom(owlPropertyClass.getIRI(),
                preferredTerm);
        setOfAxioms.add(preferredTermAx);

        //SubObjectPropertyOf
        //SubObjectPropertyOf(ObjectPropertyChain( <http://snomed.info/id/363701004> <http://snomed.info/id/127489000> ) <http://snomed.info/id/363701004>)
        for (RelationshipVersionBI rel : conceptVersionBI.getRelationshipsOutgoingActiveIsa()) {
            OWLObjectProperty parent = factory.getOWLObjectProperty(":" + getSnomedConceptID(WBUtility.getConceptVersion(rel.getDestinationNid())), pm);
            setOfAxioms.add(factory.getOWLSubObjectPropertyOfAxiom(owlPropertyClass,parent));
        }

        return owlPropertyClass;
    }

    private boolean isUSLanguageRefex(DescriptionVersionBI desc ) throws Exception{
        for(RefexVersionBI annotation : desc.getAnnotationsActive(WBUtility.getViewCoordinate())) {
            if (annotation.getAssemblageNid() == Snomed.US_LANGUAGE_REFEX.getNid()) {
                return true;
            }
        }
        return false;
    }

    private  String getSnomedConceptID(ConceptVersionBI conceptVersionBI) throws Exception {
        String id = ConceptViewerHelper.getSctId(conceptVersionBI).trim();
        if ("Unreleased".equalsIgnoreCase(id)) {
            return conceptVersionBI.getPrimordialUuid().toString();
        }
        return id;
    }

}
