/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.va.isaac.drools.dialect;

import gov.va.isaac.AppContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.Language;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.type_string.RefexStringVersionBI;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.api.spec.DescriptionSpec;
import org.ihtsdo.otf.tcc.api.spec.SpecFactory;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.ihtsdo.otf.tcc.api.uuid.UuidT5Generator;

/**
 * The Class DialectHelper. Loads the lists of dialect variant words from the
 * dialect specific refset into a map of the dialect type and the associated
 * dialect variant word.
 *
 * {@link DialectHelper}
 * 
 * @author kec
 * @author afurber
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DialectHelper
{

	private static Map<Integer, Map<String, String>> variantMap = null;
	private static Map<Integer, Set<String>> variantSetMap = null;
	private static Lock initLock = new ReentrantLock();

	/**
	 * Imports the dialect variant words into a map of the dialect to the
	 * dialect variant word. Checks to see if the map is empty before loading,
	 * and will only load the words if the map is found to be empty.
	 *
	 * @param dialectOrLanguageNid the dialect or language nid
	 * @throws UnsupportedDialectOrLanguage indicates an unsupported dialect or
	 * language
	 * @throws IOException signals that an I/O exception has occurred
	 */
	private static void lazyInit(int dialectOrLanguageNid) throws UnsupportedDialectOrLanguage, IOException
	{
		if (variantMap == null)
		{
			initLock.lock();
			try
			{
				if (variantMap == null)
				{
					HashMap<Integer, Map<String, String>> initialVariantMap = new HashMap<Integer, Map<String, String>>();
					variantSetMap = new HashMap<Integer, Set<String>>();
					ViewCoordinate vc = AppContext.getService(TerminologyStoreDI.class).getMetadataVC();
					TerminologySnapshotDI ts = AppContext.getService(TerminologyStoreDI.class).getSnapshot(vc);

					ConceptVersionBI enVariantTextRefsetC = Language.EN_VARIANT_TEXT.getStrict(AppContext.getService(TerminologyStoreDI.class).getMetadataVC());
					Collection<? extends RefexChronicleBI<?>> enVariants = enVariantTextRefsetC.getRefexes();
					Set<String> variantSet = new HashSet<String>();
					for (RefexChronicleBI<?> refex : enVariants)
					{
						if (RefexStringVersionBI.class.isAssignableFrom(refex.getClass()))
						{
							RefexStringVersionBI<?> variantText = (RefexStringVersionBI<?>) refex.getVersion(vc);
							if (variantText != null)
							{
								variantSet.add(variantText.getString1());
							}
						}
					}
					variantSetMap.put(Language.EN.getStrict(vc).getNid(), variantSet);

					addDialect(Language.EN_AU, vc, Language.EN_AU_TEXT_VARIANTS, ts, initialVariantMap);
					addDialect(Language.EN_CA, vc, Language.EN_CA_TEXT_VARIANTS, ts, initialVariantMap);
					addDialect(Language.EN_NZ, vc, Language.EN_NZ_TEXT_VARIANTS, ts, initialVariantMap);
					addDialect(Language.EN_UK, vc, Language.EN_UK_TEXT_VARIANTS, ts, initialVariantMap);
					addDialect(Language.EN_US, vc, Language.EN_US_TEXT_VARIANTS, ts, initialVariantMap);
					DialectHelper.variantMap = initialVariantMap;
				}
			}
			catch (ContradictionException ex)
			{
				throw new IOException(ex);
			}
			finally
			{
				initLock.unlock();
			}
		}
		if (!variantMap.containsKey(dialectOrLanguageNid) && !variantSetMap.containsKey(dialectOrLanguageNid))
		{
			throw new UnsupportedDialectOrLanguage("nid: " + dialectOrLanguageNid);
		}
	}

	/**
	 * Adds a dialect to the <code>initialVariantMap</code>.
	 *
	 * @param dialectSpec the <code>ConceptSpec</code> representing the dialect
	 * concept
	 * @param viewCoordinate the view coordinate specifying which versions are
	 * active or inactive
	 * @param varientsSpec the <code>ConceptSpec</code> representing the dialect
	 * refex concept
	 * @param terminologySnapshot the terminologySnapshot to use for getting
	 * component versions
	 * @param initialVariantMap the map to udpate
	 * @throws ContradictionException if more than one version is found for a
	 * given position or view coordinate
	 * @throws IOException signals that an I/O exception has occurred
	 */
	private static void addDialect(ConceptSpec dialectSpec, ViewCoordinate viewCoordinate, ConceptSpec varientsSpec, TerminologySnapshotDI terminologySnapshot,
			HashMap<Integer, Map<String, String>> initialVariantMap) throws ContradictionException, IOException
	{
		ConceptVersionBI dialectC = dialectSpec.getStrict(viewCoordinate);
		ConceptVersionBI variantTextRefsetC = varientsSpec.getStrict(viewCoordinate);

		Collection<? extends RefexChronicleBI<?>> dialectVarients = variantTextRefsetC.getRefexMembersActive(viewCoordinate);
		Map<String, String> variantDialectMap = new HashMap<String, String>();
		for (RefexChronicleBI<?> refex : dialectVarients)
		{
			if (RefexStringVersionBI.class.isAssignableFrom(refex.getClass()))
			{
				RefexStringVersionBI<?> dialectText = (RefexStringVersionBI<?>) refex.getVersion(viewCoordinate);
				if (dialectText != null)
				{
					RefexStringVersionBI<?> variantText = (RefexStringVersionBI<?>) terminologySnapshot.getComponentVersion(dialectText.getReferencedComponentNid());
					variantDialectMap.put(variantText.getString1(), dialectText.getString1());
				}
			}
		}
		initialVariantMap.put(dialectC.getNid(), variantDialectMap);
	}

	/**
	 * Checks if the enclosing concept of the given <code>description</code> is
	 * has any descriptions in the specified dialect, <code>dialectNid</code>.
	 * Uses the given <code>viewCoordinate</code> to determine which version of
	 * the descriptions to use.
	 *
	 * @param description the description containing the text to check
	 * @param dialectNid the dialect nid specifying the desired dialect
	 * @param viewCoordinate the view coordinate specifying which version of the
	 * description to use
	 * @return <code>true</code>, if no description is found in the specified
	 * dialect
	 * @throws IOException signals that an I/O exception has occurred
	 * @throws ContradictionException if more than one version is found for the
	 * given view coordinate
	 * @throws UnsupportedDialectOrLanguage indicates an unsupported dialect or
	 * language
	 */
	public static boolean isMissingDescForDialect(DescriptionVersionBI<?> description, int dialectNid, ViewCoordinate viewCoordinate) throws IOException,
			ContradictionException, UnsupportedDialectOrLanguage
	{
		lazyInit(dialectNid);
		if (!description.getLang().equals("en"))
		{
			return false;
		}
		if (isTextForDialect(description.getText(), dialectNid))
		{
			return false;
		}
		String dialectText = makeTextForDialect(description.getText(), dialectNid);
		ConceptVersionBI concept = AppContext.getService(TerminologyStoreDI.class).getConceptVersion(viewCoordinate, description.getConceptNid());
		for (DescriptionVersionBI<?> d : concept.getDescriptionsActive())
		{
			if (d.getText().toLowerCase().equals(dialectText.toLowerCase()))
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if the given <code>text</code> has dialect variants in the
	 * specified <code>language</code>.
	 *
	 * @param text the string to check for variants
	 * @param languageNid the nid representing the desired language
	 * @return <code>true</code>, if the text has any dialect variants in the
	 * specified language
	 * @throws UnsupportedDialectOrLanguage indicates an unsupported dialect or
	 * language
	 * @throws IOException signals that an I/O exception has occurred
	 */
	public static boolean hasDialectVariants(String text, int languageNid) throws UnsupportedDialectOrLanguage, IOException
	{
		lazyInit(languageNid);
		String[] tokens = text.split("\\s+");
		Set<String> dialectVariants = variantSetMap.get(languageNid);
		for (String token : tokens)
		{
			if (token.length() >= 1)
			{
				if (!token.substring(token.length() - 1, token.length()).matches("\\w"))
				{
					token = token.substring(0, token.length() - 1);
				}
				if (dialectVariants.contains(token.toLowerCase()))
				{
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks if the given<code>text</code> is written in the specified dialect.
	 *
	 * @param text the string to check
	 * @param dialectNid the nid associated with the other dialect. Use GB nid
	 * to test for US dialect, use US nid to test for GB dialect.
	 * @return <code>true</code>, if the text is written in the specified
	 * dialect
	 * @throws UnsupportedDialectOrLanguage indicates an unsupported dialect or
	 * language
	 * @throws IOException signals that an I/O exception has occurred
	 * @deprecated need to correct naming or implementation
	 */
	public static boolean isTextForDialect(String text, int dialectNid) throws UnsupportedDialectOrLanguage, IOException
	{
		lazyInit(dialectNid);
		String[] tokens = text.split("\\s+");
		Map<String, String> dialectVariants = variantMap.get(dialectNid);
		for (String token : tokens)
		{
			if (token.length() >= 1)
			{
				if (!token.substring(token.length() - 1, token.length()).matches("\\w"))
				{
					token = token.substring(0, token.length() - 1);
				}
				if (dialectVariants.containsKey(token.toLowerCase()))
				{
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Generates a String representing the given <code>text</code> re-written in
	 * specified dialect.
	 *
	 * @param text the string to re-write
	 * @param dialectNid the nid representing the desired dialect
	 * @return the string representing the given text re-written in the
	 * specified dialect
	 * @throws UnsupportedDialectOrLanguage indicates an unsupported dialect or
	 * language
	 * @throws IOException signals that an I/O exception has occurred
	 */
	public static String makeTextForDialect(String text, int dialectNid) throws UnsupportedDialectOrLanguage, IOException
	{
		lazyInit(dialectNid);
		String[] tokens = text.split("\\s+");
		Map<String, String> dialectVariants = variantMap.get(dialectNid);
		for (int i = 0; i < tokens.length; i++)
		{
			String word = tokens[i];
			String punctuation = null;
			if (word.length() >= 1)
			{
				if (!word.substring(word.length() - 1, word.length()).matches("\\w"))
				{
					punctuation = word.substring(word.length() - 1);
					word = word.substring(0, word.length() - 1);
				}
			}
			if (dialectVariants.containsKey(word.toLowerCase()))
			{
				boolean upperCase = Character.isUpperCase(word.charAt(0));
				if (punctuation != null)
				{
					tokens[i] = dialectVariants.get(word.toLowerCase()) + punctuation;
				}
				else
				{
					tokens[i] = dialectVariants.get(word.toLowerCase());
				}
				if (upperCase)
				{
					if (Character.isLowerCase(tokens[i].charAt(0)))
					{
						tokens[i] = Character.toUpperCase(tokens[i].charAt(0)) + tokens[i].substring(1);
					}
				}
			}
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < tokens.length; i++)
		{
			sb.append(tokens[i]);
			if (i < tokens.length - 1)
			{
				sb.append(' ');
			}
		}
		return sb.toString();
	}

	/**
	 * Gets a description spec representing the <code>description</code> in the
	 * dialect specified by the <code>dialectSpec</code>.
	 *
	 * @param description the description to represent
	 * @param dialectSpec specifying the dialect of the description spec
	 * @param viewCoordinate specifying which version of the description to use
	 * @return the generated description spec for the specified dialect
	 * @throws UnsupportedDialectOrLanguage indicates an unsupported dialect or
	 * language
	 * @throws IOException signals that an I/O exception has occurred
	 */
	public static DescriptionSpec getDescriptionSpecForDialect(DescriptionVersionBI<?> description, ConceptSpec dialectSpec, ViewCoordinate viewCoordinate)
			throws UnsupportedDialectOrLanguage, IOException
	{
		return getDescriptionSpecForDialect(description, dialectSpec.getStrict(viewCoordinate).getNid(), viewCoordinate);
	}

	/**
	 * Gets a description spec representing the <code>description</code> in the
	 * dialect specified by the <code>dialectNid</code>.
	 *
	 * @param description the description to represent
	 * @param dialectNid specifying the dialect of the description spec
	 * @param viewCoordinate specifying which version of the description to use
	 * @return the generated description spec for the specified dialect
	 * @throws UnsupportedDialectOrLanguage indicates an unsupported dialect or
	 * language
	 * @throws IOException signals that an I/O exception has occurred
	 */
	public static DescriptionSpec getDescriptionSpecForDialect(DescriptionVersionBI<?> description, int dialectNid, ViewCoordinate viewCoordinate)
			throws UnsupportedDialectOrLanguage, IOException
	{
		try
		{
			lazyInit(dialectNid);
			String variantText = makeTextForDialect(description.getText(), dialectNid);

			UUID descUuid = UuidT5Generator.getDescUuid(description.getText(),
					AppContext.getService(TerminologyStoreDI.class).getConcept(dialectNid).getPrimordialUuid(), AppContext.getService(TerminologyStoreDI.class)
							.getConcept(description.getConceptNid()).getPrimordialUuid());

			DescriptionSpec ds = new DescriptionSpec(new UUID[] { descUuid }, SpecFactory.get(
					AppContext.getService(TerminologyStoreDI.class).getConcept(description.getConceptNid()), viewCoordinate), SpecFactory.get(
					AppContext.getService(TerminologyStoreDI.class).getConcept(description.getTypeNid()), viewCoordinate), variantText);
			ds.setLangText(description.getLang());
			return ds;
		}
		catch (NoSuchAlgorithmException ex)
		{
			throw new IOException(ex);
		}
		catch (UnsupportedEncodingException ex)
		{
			throw new IOException(ex);
		}
	}
}
