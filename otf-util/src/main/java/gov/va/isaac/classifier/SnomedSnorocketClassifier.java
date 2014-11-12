package gov.va.isaac.classifier;

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.classifier.model.ConceptGroup;
import gov.va.isaac.classifier.model.EquivalentClasses;
import gov.va.isaac.classifier.model.Relationship;
import gov.va.isaac.classifier.model.StringIDConcept;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerHelper;
import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptFetcherBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.metadata.binding.Taxonomies;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.csiro.snorocket.core.IFactory_123;
import au.csiro.snorocket.snapi.I_Snorocket_123.I_Callback;
import au.csiro.snorocket.snapi.I_Snorocket_123.I_EquivalentCallback;
import au.csiro.snorocket.snapi.Snorocket_123;

public class SnomedSnorocketClassifier implements ProcessUnfetchedConceptDataBI {
	private static final Logger LOG = LoggerFactory
			.getLogger(SnomedSnorocketClassifier.class);

	private static String ROLE_GROUP = "19657985-279a-4713-83be-e3d1a884b50d";

	private static UUID CONCEPT_ATTRIBUTE = UUID
			.fromString("6155818b-09ed-388e-82ce-caa143423e99");

	private static BdbTerminologyStore dataStore = ExtendedAppContext
			.getDataStore();

	private int count = Integer.MIN_VALUE + 3;

	private int pathNid;

	/** The prev inferred rels. */
	private List<String> previousInferredRelationships;

	/** The edited snomed concepts. */
	private ArrayList<StringIDConcept> cEditSnoCons = new ArrayList<StringIDConcept>();;

	/** The edit snomed rels. */
	private ArrayList<Relationship> cEditRelationships = new ArrayList<Relationship>();;

	/** The logger. */
	private Logger logger;

	/** The c rocket sno rels. */
	private ArrayList<Relationship> cRocketRelationships;

	private String equivalencyReport = "equivalentConcepts.out";

	protected Logger getLogger() {
		return LOG;
	}

	public SnomedSnorocketClassifier() {
	}

	public void classify(int pathNid) throws Exception {
		this.pathNid = pathNid;
		dataStore.iterateConceptDataInSequence(this);
		int[] roles = getRoles(CONCEPT_ATTRIBUTE);
		int ridx = roles.length;
		if (roles.length > 100) {
			String errStr = "Role types exceeds 100. This will cause a memory issue. "
					+ "Please check that role root is set to 'Concept mode attribute'";
			logger.error(errStr);
			throw new ClassificationException(errStr);
		}
		final int reserved = 2;
		int cidx = reserved;
		int margin = cEditSnoCons.size() >> 2; // Add 50%
		int[] intArray = new int[cEditSnoCons.size() + margin + reserved];
		intArray[IFactory_123.TOP_CONCEPT] = IFactory_123.TOP;
		intArray[IFactory_123.BOTTOM_CONCEPT] = IFactory_123.BOTTOM;
		Collections.sort(cEditSnoCons);
		if (cEditSnoCons.get(0).id <= Integer.MIN_VALUE + reserved) {
			throw new ClassificationException(
					"::: SNOROCKET: TOP & BOTTOM nids NOT reserved");
		}
		for (StringIDConcept sc : cEditSnoCons) {
			intArray[cidx++] = sc.id;
		}
		// Fill array to make binary search work correctly.
		Arrays.fill(intArray, cidx, intArray.length, Integer.MAX_VALUE);
		// Obtain SNOMED root concept
		// TODO: this needs to be generalized
		UUID snomedRootUUID = Taxonomies.SNOMED.getUuids()[0];
		ConceptVersionBI snomedRootConcept = WBUtility
				.getConceptVersion(snomedRootUUID);
		int root = snomedRootConcept.getNid();
		Snorocket_123 rocket_123 = new Snorocket_123(intArray, cidx, roles,
				ridx, root);

		// SnomedMetadata :: ISA
		rocket_123.setIsaNid(TermAux.IS_A.getNid());

		// SnomedMetadata :: ROLE_ROOTS
		rocket_123.setRoleRoot(TermAux.IS_A.getNid(), true); // @@@
		UUID snomedRoleRootUUID = Taxonomies.SNOMED_ROLE_ROOT.getUuids()[0];
		ConceptVersionBI snomedRoleRootConcept = WBUtility
				.getConceptVersion(snomedRoleRootUUID);
		int roleRoot = snomedRoleRootConcept.getNid();
		rocket_123.setRoleRoot(roleRoot, false);

		// SET DEFINED CONCEPTS
		for (int i = 0; i < cEditSnoCons.size(); i++) {
			if (cEditSnoCons.get(i).isDefined) {
				rocket_123.setConceptIdxAsDefined(i + reserved);
			}
		}
		cEditSnoCons = null; // :MEMORY:

		// ADD RELATIONSHIPS
		Collections.sort(cEditRelationships);
		for (Relationship sr : cEditRelationships) {
			int err = rocket_123.addRelationship(sr.sourceId, sr.typeId,
					sr.destinationId, sr.group);
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
				logger.info("\r\n::: " + sb /* :!!!: + dumpSnoRelStr(sr) */);
			}
		}

		cEditRelationships = null; // :MEMORY:

		System.gc();

		// RUN CLASSIFIER
		long startTime = System.currentTimeMillis();
		logger.info("::: Starting Classifier... ");
		rocket_123.classify();
		logger.info("::: Time to classify (ms): "
				+ (System.currentTimeMillis() - startTime));

		// GET CLASSIFER EQUIVALENTS
		logger.info("::: GET EQUIVALENT CONCEPTS...");
		startTime = System.currentTimeMillis();
		ProcessEquiv pe = new ProcessEquiv();
		rocket_123.getEquivalents(pe);
		logger.info("\r\n::: [SnorocketMojo] ProcessEquiv() count="
				+ pe.countConSet + " time= " + toStringLapseSec(startTime));
		pe.getEquivalentClasses();
		EquivalentClasses.writeEquivConcept(pe.getEquivalentClasses(),
				equivalencyReport );

		// GET CLASSIFER RESULTS
		cRocketRelationships = new ArrayList<Relationship>();
		logger.info("::: GET CLASSIFIER RESULTS...");
		startTime = System.currentTimeMillis();
		ProcessResults pr = new ProcessResults(cRocketRelationships);
		rocket_123.getDistributionFormRelationships(pr);
		logger.info("\r\n::: [SnorocketMojo] GET CLASSIFIER RESULTS count="
				+ pr.countRel + " time= " + toStringLapseSec(startTime));

		pr = null; // :MEMORY:
		rocket_123 = null; // :MEMORY:
		System.gc();
		System.gc();

		// GET CLASSIFIER_PATH RELS
		startTime = System.currentTimeMillis();
		cEditRelationships = new ArrayList<Relationship>();

		cEditSnoCons = new ArrayList<StringIDConcept>();
		cEditSnoCons = null;

		// WRITEBACK RESULTS
//		startTime = System.currentTimeMillis();
//		if (previousInferredRelationships == null
//				|| !previousInferredRelationships.isEmpty()) {
//			writeInferredRel(cRocketRelationships);
//		} else {
//
//			logger.info(compareAndWriteBack(cEditRelationships,
//					cRocketRelationships));
//
//			logger.info("\r\n::: *** WRITEBACK *** LAPSED TIME =\t"
//					+ toStringLapseSec(startTime) + "\t ***");
//
//			consolidateRels();
//
//		}

		logger.info("\r\n::: *** WROTE *** LAPSED TIME =\t"
				+ toStringLapseSec(startTime) + "\t ***");

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
		if (concept.getPathNid() == pathNid) {
			count++;
			convertToOntologyObjects(concept);
		}
	}

	private void convertToOntologyObjects(ConceptVersionBI concept)
			throws IOException, ContradictionException {
		ConceptAttributeVersionBI<?> attributes = ConceptViewerHelper
				.getConceptAttributes(concept);

		StringIDConcept conStr = new StringIDConcept(count,
				String.valueOf(concept.getNid()), attributes.isDefined());
		cEditSnoCons.add(conStr);

		Collection<? extends RelationshipVersionBI<?>> relationships = concept
				.getRelationshipsOutgoingActive();
		for (RelationshipVersionBI<?> relationship : relationships) {
			Relationship rel = new Relationship(relationship.getConceptNid(),
					relationship.getDestinationNid(),
					relationship.getTypeNid(), relationship.getGroup(),
					String.valueOf(relationship.getNid()));
			cEditRelationships.add(rel);
		}

	}

	private int[] getRoles(UUID parent) throws IOException,
			ContradictionException {
		ArrayList<ConceptVersionBI> children = WBUtility
				.getAllChildrenOfConcept(WBUtility.getConceptVersion(parent),
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
	 * @param startTime
	 *            the start time
	 * @return the string
	 */
	private String toStringLapseSec(long startTime) {
		StringBuilder s = new StringBuilder();
		long stopTime = System.currentTimeMillis();
		long lapseTime = stopTime - startTime;
		s.append((float) lapseTime / 1000).append(" (seconds)");
		return s.toString();
	}

	@Override
	public NativeIdSetBI getNidSet() throws IOException {
		return dataStore.getAllConceptNids();
	}

	@Override
	public String getTitle() {
		return this.getClass().getName();
	}

	/**
	 * The Class ProcessResults.
	 */
	private class ProcessResults implements I_Callback {

		/** The snorels. */
		private List<Relationship> snorels;

		/** The count rel. */
		private int countRel = 0; // STATISTICS COUNTER

		/**
		 * Instantiates a new process results.
		 *
		 * @param snorels the snorels
		 */
		public ProcessResults(List<Relationship> snorels) {
			this.snorels = snorels;
			this.countRel = 0;
		}

		/* (non-Javadoc)
		 * @see au.csiro.snorocket.snapi.I_Snorocket_123.I_Callback#addRelationship(int, int, int, int)
		 */
		public void addRelationship(int conceptId1, int roleId, int conceptId2, int group) {
			countRel++;
			Relationship relationship = new Relationship(conceptId1, conceptId2, roleId, group);
			snorels.add(relationship);
			if (countRel % 25000 == 0) {
				// ** GUI: ProcessResults
				logger.info("rels processed " + countRel);
			}
		}
	}

	/**
	 * The Class ProcessEquiv.
	 */
	private class ProcessEquiv implements I_EquivalentCallback {

		/** The count con set. */
		private int countConSet = 0; // STATISTICS COUNTER

		/** The equiv concept. */
		private EquivalentClasses equivalentClasses;

		/**
		 * Instantiates a new process equiv.
		 */
		public ProcessEquiv() {
			equivalentClasses =new EquivalentClasses();
		}

		/* (non-Javadoc)
		 * @see au.csiro.snorocket.snapi.I_Snorocket_123.I_EquivalentCallback#equivalent(java.util.ArrayList)
		 */
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

}
