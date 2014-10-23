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
package gov.va.isaac.config.users;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.config.generated.IsaacUserCreation;
import gov.va.isaac.config.generated.User;
import gov.va.isaac.config.profiles.UserProfileManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.UUID;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.ConceptCB;
import org.ihtsdo.otf.tcc.api.blueprint.DescriptionCAB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.lang.LanguageCode;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.uuid.UuidT5Generator;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * {@link GenerateUsers}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class GenerateUsers
{
	public static final UUID USER_LOGON_NAMESPACE = UUID.fromString("e3992850-7204-5159-af93-c0027a79d23c");
	private static Logger logger = LoggerFactory.getLogger(GenerateUsers.class);

	public static void generateUsers() throws InvalidUserException
	{
		generateUsers(AppContext.class.getResourceAsStream("/users.xml"));
	}

	public static void generateUsers(File sourceFile) throws FileNotFoundException, InvalidUserException
	{
		generateUsers(new FileInputStream(sourceFile));
	}

	public static void generateUsers(InputStream is) throws InvalidUserException
	{
		if (is == null)
		{
			throw new RuntimeException("Failure reading the users file!");
		}
		try
		{
			JAXBContext jaxbContext = JAXBContext.newInstance(IsaacUserCreation.class);
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = factory.newSchema(new StreamSource(AppContext.class.getResourceAsStream("/xsd/UserGenerationSchema.xsd")));
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			jaxbUnmarshaller.setSchema(schema);
			buildMissingUsers((IsaacUserCreation) jaxbUnmarshaller.unmarshal(is));
			is.close();
		}
		catch (JAXBException | SAXException | IOException e)
		{
			logger.error("Unexpected error processing users file", e);
			throw new RuntimeException("Unexpected error processing users file");
		}
	}

	private static void buildMissingUsers(IsaacUserCreation iuc) throws InvalidUserException
	{
		try
		{
			UserProfileManager upm = AppContext.getService(UserProfileManager.class);
			
			//This bit of hashing is to ensure that they didn't repeat any userLogin names (which can't be duplicated
			//for obvious reasons) and to make sure that the UniqueFullName is unique, which is used as the FSN, and as 
			//the basis for the computed UUID for the user concept (if they don't provide their own UUID)
			HashSet<String> uniqueFullNames = new HashSet<>();
			HashSet<String> uniqueLogonNames = new HashSet<>();
			HashSet<String> usersToSkip = new HashSet<>();
			for (User user : iuc.getUser())
			{
				if (!uniqueFullNames.add(user.getUniqueFullName()))
				{
					usersToSkip.add(user.getUniqueLogonName());
				}
				if (!uniqueLogonNames.add(user.getUniqueLogonName()))
				{
					usersToSkip.add(user.getUniqueLogonName());
				}
			}

			for (User user : iuc.getUser())
			{
				logger.debug("Checking user " + toString(user));
				
				if (usersToSkip.contains(user.getUniqueLogonName()))
				{
					logger.error("Skipping the user {} because the uniqueLogonName and/or the uniqueFullName is duplicated within the users file.", toString(user));
					continue;
				}
				
				//This also validates other rules about the incoming user, to make sure it can be created - throws an exception, if the user 
				//is invalid for whatever reason.  This also populates the UUID field (if necessary)
				if (alreadyExists(user))
				{
					logger.debug("User already exists in DB");
				}
				else
				{
					createUserConcept(user);
				}

				if (!upm.doesProfileExist(user.getUniqueLogonName()))
				{
					upm.createUserProfile(user, iuc.getNewUserDefaults());
				}
				else
				{
					logger.debug("User profile already exists");
				}
			}
		}

		catch (ContradictionException | IOException | InvalidCAB e)
		{
			logger.error("Unexpected error building the user concepts", e);
			throw new RuntimeException("Unexpected error building user concepts", e);
		}
		catch (InvalidUserException e)
		{
			throw e;
		}
	}
	
	/**
	 * Create a concept in the DB, for the specified user.  Only call this if {@link #alreadyExists(User)) return false
	 */
	public static void createUserConcept(User user) throws IOException, InvalidCAB, ContradictionException
	{
		logger.info("Creating user " + toString(user) + " in DB");
		AppContext.getRuntimeGlobals().disableAllCommitListeners();
		try
		{
			BdbTerminologyStore ts = ExtendedAppContext.getDataStore();
			String fsn = user.getUniqueFullName();
			String preferredName = user.getFullName();
			String logonName = user.getUniqueLogonName();
			UUID userUUID = UUID.fromString(user.getUUID());
	
			LanguageCode lc = LanguageCode.EN_US;
			UUID isA = Snomed.IS_A.getUuids()[0];
			IdDirective idDir = IdDirective.PRESERVE_CONCEPT_REST_HASH;
			UUID module = TermAux.TERM_AUX_MODULE.getUuids()[0];
			UUID parents[] = new UUID[] { TermAux.USER.getUuids()[0] };
	
			ConceptCB cab = new ConceptCB(fsn, preferredName, lc, isA, idDir, module, userUUID, parents);
	
			DescriptionCAB dCab = new DescriptionCAB(cab.getComponentUuid(), Snomed.SYNONYM_DESCRIPTION_TYPE.getUuids()[0], lc, logonName, true,
					IdDirective.GENERATE_HASH);
			dCab.getProperties().put(ComponentProperty.MODULE_ID, module);
	
			//Mark it as acceptable
			RefexCAB rCabAcceptable = new RefexCAB(RefexType.CID, dCab.getComponentUuid(), Snomed.US_LANGUAGE_REFEX.getUuids()[0], IdDirective.GENERATE_HASH,
					RefexDirective.EXCLUDE);
			rCabAcceptable.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, SnomedMetadataRf2.ACCEPTABLE_RF2.getUuids()[0]);
			rCabAcceptable.getProperties().put(ComponentProperty.MODULE_ID, module);
			dCab.addAnnotationBlueprint(rCabAcceptable);
	
			cab.addDescriptionCAB(dCab);
			
			//TODO store roles on the concept
	
			//Build this on the lowest level path, otherwise, other code that references this will fail (as it doesn't know about custom paths)
			ConceptChronicleBI newCon = ts.getTerminologyBuilder(
					new EditCoordinate(TermAux.USER.getLenient().getConceptNid(), TermAux.TERM_AUX_MODULE.getLenient().getNid(), TermAux.WB_AUX_PATH.getLenient()
							.getConceptNid()), StandardViewCoordinates.getWbAuxiliary()).construct(cab);
			ts.addUncommitted(newCon);
			ts.commit(newCon);
		}
		finally
		{
			AppContext.getRuntimeGlobals().enableAllCommitListeners();
		}
	}

	/**
	 * Check if the user already exists in the DB (return false) and if not, validate the incoming parameters, throwing an exception
	 * if anything is amiss with the user definition.
	 * 
	 * As a side effect, this populates the UUID field of the incoming user, if it was not yet populated.
	 * Also populates the password field (with the logon name) if it was not populated.
	 * Also populates the FullName field with the UniqueFullName field if it was not populated
	 * 
	 * @throws InvalidUserException for any issues with the values provided within the user
	 */
	public static boolean alreadyExists(User user) throws InvalidUserException
	{

		if (user.getUniqueFullName() == null || user.getUniqueFullName().length() == 0)
		{
			throw new InvalidUserException("The uniqueFullName value is required.", user);
		}

		if (user.getUniqueLogonName() == null || user.getUniqueLogonName().length() == 0)
		{
			throw new InvalidUserException("The uniqueLogonName value is required.", user);
		}

		UUID uuid;

		if (user.getUUID() == null || user.getUUID().length() == 0)
		{
			uuid = calculateUserUUID(user.getUniqueFullName());
			user.setUUID(uuid.toString());
		}
		else
		{
			try
			{
				uuid = UUID.fromString(user.getUUID());
			}
			catch (Exception e)
			{
				throw new InvalidUserException("The specified UUID is not a valid UUID", user);
			}
		}

		if (user.getFullName() == null || user.getFullName().length() == 0)
		{
			user.setFullName(user.getUniqueFullName());
		}

		if (user.getPassword() == null || user.getPassword().length() == 0)
		{
			user.setPassword(user.getUniqueLogonName());
		}

		return ExtendedAppContext.getDataStore().hasUuid(uuid);
	}

	public static UUID calculateUserUUID(String uniqueLogonName)
	{
		try
		{
			return UuidT5Generator.get(USER_LOGON_NAMESPACE, uniqueLogonName);
		}
		catch (NoSuchAlgorithmException | UnsupportedEncodingException e)
		{
			logger.error("Unexpected", e);
			throw new RuntimeException(e);
		}
	}

	static String toString(User user)
	{
		return "uniqueFullName: " + user.getUniqueFullName() + " uniqueLogonName: " + user.getUniqueLogonName() + " fullName: " + user.getFullName() + " UUID: "
				+ user.getUUID();
	}
}
