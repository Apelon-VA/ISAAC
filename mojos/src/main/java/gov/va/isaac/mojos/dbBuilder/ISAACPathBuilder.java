package gov.va.isaac.mojos.dbBuilder;

import gov.va.isaac.AppContext;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.otf.mojo.GenerateMetadataEConcepts;
import org.ihtsdo.otf.query.lucene.LuceneIndexer;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.ConceptCB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.blueprint.TerminologyBuilderBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.io.FileIO;
import org.ihtsdo.otf.tcc.api.lang.LanguageCode;
import org.ihtsdo.otf.tcc.api.metadata.binding.RefexDynamic;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.ihtsdo.otf.tcc.datastore.BdbTermBuilder;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;
import org.ihtsdo.otf.tcc.model.index.service.IndexerBI;

/**
 * Goal which loads a database from an eConcept file, and generates the indexes.
 * 
 * @goal create-isaac-path-bdb
 * 
 * @phase process-sources
 */
public class ISAACPathBuilder extends AbstractMojo {

	private static ConceptSpec RELEASE = new ConceptSpec("release",
			UUID.fromString("88f89cc0-1d94-34a4-85ed-aa1949079314"));

	private static ConceptSpec LOINC = new ConceptSpec("LOINC Path",
			UUID.fromString("b2b1cc96-9ca6-5513-aad9-aa21e61ddc29"));

	private static ConceptSpec RXNORM = new ConceptSpec("RxNorm Path",
			UUID.fromString("d1cfff32-d25d-57e6-8cc4-fd433cd1096d"));

	private final UUID refsetIdentityUid = UUID
			.fromString("3e0cd740-2cc6-3d68-ace7-bad2eb2621da");

	/**
	 * Location of the file.
	 * 
	 * @parameter expression="${project.build.directory}/berkeley-db"
	 * @required
	 */
	private String bdbFolderLocation;

	private BdbTerminologyStore dataStore = null;
	
	@Override
	public void execute() throws MojoExecutionException {
		try {
			long startTime = System.currentTimeMillis();
			File bdbFolderFile = new File(bdbFolderLocation);

			System.setProperty(BdbTerminologyStore.BDB_LOCATION_PROPERTY,
					bdbFolderLocation);
			System.setProperty(LuceneIndexer.LUCENE_ROOT_LOCATION_PROPERTY,
					bdbFolderLocation);

			if(AppContext.getServiceLocator() == null)
				AppContext.setup();

			// TODO OTF fix: this needs to be fixed so I don't have to hack it
			// with reflection....
			// https://jira.ihtsdotools.org/browse/OTFISSUE-11
//			Field f = Hk2Looker.class.getDeclaredField("looker");
//			f.setAccessible(true);
//			f.set(null, AppContext.getServiceLocator());
//
//			TerminologyStoreDI store = AppContext
//					.getService(TerminologyStoreDI.class);
//			List<IndexerBI> indexers = AppContext.getServiceLocator()
//					.getAllServices(IndexerBI.class);
//
//			getLog().info("Located " + indexers.size() + " indexers");
//
//			if (!dbExists) {
//				getLog().info(
//						"DB did not exist - disabling indexers for batch index");
//				for (IndexerBI indexer : indexers) {
//					indexer.setEnabled(false);
//				}
//			}
//			store.loadEconFiles(econFileStrings);
//			if (loadDefaultMetadata) {
//				getLog().info("Creating and loading the metadata");
//				File metaData = File.createTempFile("WBMetaData-", ".jbin");
//				GenerateMetadataEConcepts gmc = new GenerateMetadataEConcepts(
//						metaData,
//						new String[] { "org.ihtsdo.otf.tcc.api.metadata.binding.RefexDynamic" },
//						new ConceptSpec[0], false);
//				gmc.execute();
//				store.loadEconFiles(new File[] { metaData });
//				metaData.delete();
//			}
//
//			if (!dbExists) {
//				for (IndexerBI indexer : indexers) {
//					indexer.setEnabled(true);
//				}
//				getLog().info("Batch Indexing");
//				store.index();
//			}
//
//			getLog().info("Shutting Down");
//
//			for (IndexerBI indexer : indexers) {
//				indexer.closeWriter();
//			}
//			store.shutdown();
			createISAACPath();

//			if (!dbExists && moveToReadOnly) {
//				getLog().info("moving mutable to read-only");
//				File readOnlyDir = new File(bdbFolderLocation, "read-only");
//				FileIO.recursiveDelete(readOnlyDir);
//				File mutableDir = new File(bdbFolderLocation, "mutable");
//				mutableDir.renameTo(readOnlyDir);
//			}
//			

			getLog().info(
					"Done building the DB.  Elapsed Time: "
							+ ((System.currentTimeMillis() - startTime) / 1000)
							+ " seconds");
		} catch (Exception e) {
			throw new MojoExecutionException("Database build failure", e);
		}
	}

	private void createISAACPath() {
		try {
			getLog().info("Creating ISAAC Path");
			dataStore = AppContext.getServiceLocator()
					.getService(BdbTerminologyStore.class);
			ConceptChronicleBI bpConcept = dataStore.getConcept(RELEASE
					.getLenient().getPrimordialUuid());
			ConceptVersionBI con = bpConcept.getVersion(getVC());
			String fsn = "ISAAC development path";
			String prefTerm = "ISAAC development path";
			LanguageCode lc = LanguageCode.EN_US;
			UUID isA = Snomed.IS_A.getUuids()[0];
			IdDirective idDir = IdDirective.GENERATE_HASH;
			UUID module = Snomed.CORE_MODULE.getLenient().getPrimordialUuid();
			UUID parents[] = new UUID[1];
			parents[0] = con.getPrimordialUuid();

			ConceptCB newConCB = new ConceptCB(fsn, prefTerm, lc, isA, idDir,
					module, parents);

			ConceptChronicleBI refCon = getBuilder().construct(newConCB);
			dataStore.addUncommitted(refCon);
			ConceptChronicleBI refComp = dataStore
					.getConcept(TermAux.PATH_REFSET.getLenient()
							.getPrimordialUuid());
			addMember(refCon, refComp);
			addExtensionMember(refCon, TermAux.SNOMED_CORE);
			addExtensionMember(refCon, LOINC);
			addExtensionMember(refCon, RXNORM);
			dataStore.commit();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidCAB e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ContradictionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void addMember(ConceptChronicleBI refCon, ConceptChronicleBI refComp) throws IOException, InvalidCAB, ContradictionException {
		RefexCAB newMember = new RefexCAB(RefexType.MEMBER,
				refComp.getConceptNid(), refCon.getNid(),
				IdDirective.GENERATE_HASH, RefexDirective.INCLUDE);

		RefexChronicleBI<?> newMemChron = getBuilder().construct(newMember);

		if (!refCon.isAnnotationStyleRefex()) {
			dataStore.addUncommitted(refCon);
		} else {
			dataStore.addUncommitted(refComp);
		}
		
	}
	private void addExtensionMember(ConceptChronicleBI refCon, ConceptSpec con) {
		try {
			ConceptChronicleBI refComp = dataStore
					.getConcept(TermAux.PATH_REFSET.getLenient()
							.getPrimordialUuid());
			RefexCAB newMember;
			newMember = new RefexCAB(RefexType.CID, refComp.getConceptNid(),
					refCon.getNid(), IdDirective.GENERATE_HASH,
					RefexDirective.INCLUDE);
			int parentRefsetFSN = getRefsetIdentity().getVersion(getVC())
					.getFullySpecifiedDescription().getNid();
			newMember.put(ComponentProperty.COMPONENT_EXTENSION_1_ID,
					con.getNid());

			RefexChronicleBI<?> newMemChron = getBuilder().construct(newMember);
			if (!refCon.isAnnotationStyleRefex()) {
				dataStore.addUncommitted(refCon);
			} else {
				dataStore.addUncommitted(refComp);
			}
		} catch (IOException | InvalidCAB | ContradictionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public ConceptVersionBI getRefsetIdentity() throws ContradictionException, IOException {
		return dataStore.getConcept(refsetIdentityUid).getVersion(getVC());
	}

	public TerminologyBuilderBI getBuilder() {
		BdbTermBuilder builder = null;
		try {
			builder = new BdbTermBuilder(getEC(), getVC());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return builder;
	}

	public ViewCoordinate getVC() {
		ViewCoordinate vc = null;
		try {
			vc = StandardViewCoordinates.getSnomedStatedLatest();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return vc;

	}

	EditCoordinate getEC() throws ValidationException, IOException {
		int authorNid = TermAux.USER.getLenient().getConceptNid();
		int module = Snomed.CORE_MODULE.getLenient().getNid();
		int editPathNid = TermAux.SNOMED_CORE.getLenient().getConceptNid();

		return new EditCoordinate(authorNid, module, editPathNid);
	}

	public static void main(String[] args) throws Exception {
		ISAACPathBuilder foo = new ISAACPathBuilder();
		foo.bdbFolderLocation = "target/berkeley-db";
		FileIO.recursiveDelete(new File(foo.bdbFolderLocation));
		foo.execute();
	}
}
