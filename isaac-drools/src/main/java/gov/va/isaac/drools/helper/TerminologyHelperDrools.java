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
package gov.va.isaac.drools.helper;

import gov.va.isaac.drools.testmodel.DrComponent;
import gov.va.isaac.drools.testmodel.DrConcept;
import gov.va.isaac.drools.testmodel.DrDescription;
import gov.va.isaac.drools.testmodel.DrRelationship;
import gov.va.isaac.util.OTFUtility;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * {@link TerminologyHelperDrools}
 *
 * @author kec
 * @author afurber
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class TerminologyHelperDrools
{
	private static final Logger LOG = LoggerFactory.getLogger(TerminologyHelperDrools.class);
	private Hashtable<String, String> usspelling;
	private Hashtable<String, String> ukspelling;
	private String strSplitChars;
	private boolean propsLoaded;
	private InputStream fis;
	private BufferedReader in;

	private Hashtable<String, String> words;
	private boolean wordsLoaded;
	private InputStream fisW;

	// These properties are only used to support enumerations for the DSL, should not be used from Java
	private String conceptDomain;
	private String roleRange;
	private String groupCondition;

	// End of DSL enum properties

	public TerminologyHelperDrools()
	{
		// Spelling terms data are commented out
		//		fis=(InputStream) TerminologyHelperDrools.class.getResourceAsStream("GB-US-spellingdiffs.txt");
		//		usspelling = new Hashtable<String,String>();
		//		ukspelling = new Hashtable<String,String>();
		//		propsLoaded=false;
		//		for (int i = 2; i < 8; i++) {
		//			for (int j = 0; j < 10; j++) {
		//				FnF[i][j] = FnF[i - 1][FnF[1][j]];
		//			}
		//		}
		//
		//		fisW=(InputStream) TerminologyHelperDrools.class.getResourceAsStream("IcsWords.txt");
		//		words = new Hashtable<String,String>();
		//		wordsLoaded=false;
	}

	public boolean checkSameInitialWord(String term, String term2)
	{
		String fWord = "";
		String fWord2 = "";
		if (term.indexOf(" ") > 0)
			fWord = term.substring(0, term.indexOf(" "));
		else
			fWord = term;
		if (term2.indexOf(" ") > 0)
			fWord2 = term2.substring(0, term2.indexOf(" "));
		else
			fWord2 = term2;

		return fWord.equals(fWord2);
	}

	public String getICSCategory(String term)
	{
		String retString = "0";
		String word;
		if (term.indexOf(" ") > 0)
			word = term.substring(0, term.indexOf(" "));
		else
			word = term;
		try
		{
			String aLine = null;

			if (!wordsLoaded)
			{
				in = new BufferedReader(new InputStreamReader(fisW, "UTF-8"));

				//Read in GB-US-spellingdiffs.txt UK<space>US
				while ((aLine = in.readLine()) != null)
				{
					aLine.trim();
					String[] line = aLine.split(" ");
					words.put(line[0], line[1]);
				}
				wordsLoaded = true;
				in.close();
			}

			if (words.containsKey(word))
			{
				retString = (String) words.get(word);
			}
		}
		catch (Exception e)
		{
			LOG.warn("Exception in getICSCatetory", e);
		}
		return retString;
	}

	public enum NAMESPACE
	{
		NEHTA("1000036"), NHS("1999999"), CORE("");
		private String digits;

		private NAMESPACE(String digits)
		{
			this.digits = digits;
		}

		public String getDigits()
		{
			return digits;
		}
	};

	public enum TYPE
	{
		CONCEPT("00"), DESCRIPTION("01"), RELATIONSHIP("02"), EXTENSION_CONCEPT("10"), EXTENSION_DESCRIPTION("11"), EXTENSION_RELATIONSHIP("12"), UNKNOW("XX");
		private String digits;

		private TYPE(String digits)
		{
			this.digits = digits;
		}

		public String getDigits()
		{
			return digits;
		}
	}

	private int[][] FnF = { 
			{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }, 
			{ 1, 5, 7, 6, 2, 8, 3, 0, 9, 4 }, 
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, 
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, 
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, 
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, 
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }
			};

	private int[][] Dihedral = { 
			{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }, 
			{ 1, 2, 3, 4, 0, 6, 7, 8, 9, 5 }, 
			{ 2, 3, 4, 0, 1, 7, 8, 9, 5, 6 }, 
			{ 3, 4, 0, 1, 2, 8, 9, 5, 6, 7 },
			{ 4, 0, 1, 2, 3, 9, 5, 6, 7, 8 }, 
			{ 5, 9, 8, 7, 6, 0, 4, 3, 2, 1 }, 
			{ 6, 5, 9, 8, 7, 1, 0, 4, 3, 2 }, 
			{ 7, 6, 5, 9, 8, 2, 1, 0, 4, 3 },
			{ 8, 7, 6, 5, 9, 3, 2, 1, 0, 4 }, 
			{ 9, 8, 7, 6, 5, 4, 3, 2, 1, 0 } 
			};

	private int[] InverseD5 = { 0, 4, 3, 2, 1, 5, 6, 7, 8, 9 };

	public boolean checkDigit(DrComponent component, String SCTIDasString)
	{
		TYPE type = TYPE.UNKNOW;
		if (component instanceof DrConcept)
		{
			type = TYPE.CONCEPT;
		}
		if (component instanceof DrDescription)
		{
			type = TYPE.DESCRIPTION;
		}
		if (component instanceof DrRelationship)
		{
			type = TYPE.RELATIONSHIP;
		}
		if (SCTIDasString.length() < 6)
			return false;

		String partition = SCTIDasString.substring(SCTIDasString.length() - 3, SCTIDasString.length() - 1);

		if (!partition.equals(type.getDigits()))
			return false;

		return verhoeffCheck(SCTIDasString);
	}

	public String generate(long sequence, NAMESPACE namespace, TYPE type)
	{
		if (sequence <= 0)
		{
			throw new RuntimeException("sequence must be > 0");
		}
		String mergedid = Long.toString(sequence) + namespace.digits + type.digits;
		return mergedid + verhoeffCompute(mergedid);
	}

	public String generate(long sequence, int namespaceId, TYPE type)
	{
		if (sequence <= 0)
		{
			throw new RuntimeException("sequence must be > 0");
		}

		String mergedid = Long.toString(sequence) + namespaceId + type.digits;

		return mergedid + verhoeffCompute(mergedid);
	}

	public boolean verhoeffCheck(String idAsString)
	{
		int check = 0;

		for (int i = idAsString.length() - 1; i >= 0; i--)
		{
			check = Dihedral[check][FnF[(idAsString.length() - i - 1) % 8][new Integer(new String(new char[] { idAsString.charAt(i) }))]];
		}
		if (check != 0)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	public long verhoeffCompute(String idAsString)
	{
		int check = 0;
		for (int i = idAsString.length() - 1; i >= 0; i--)
		{
			check = Dihedral[check][FnF[((idAsString.length() - i) % 8)][new Integer(new String(new char[] { idAsString.charAt(i) }))]];

		}
		return InverseD5[check];
	}

	public boolean checkTermSpelling(String term, String language)
	{
		try
		{
			if (!propsLoaded)
			{
				if (!loadProperties())
					return false;
			}
			String GB = "en-gb";
			String EN = "en";
			String US = "en-us";

			String origvalue = term.trim();

			int index = origvalue.lastIndexOf("(");
			if (index > -1)
			{
				origvalue = origvalue.substring(0, index);
				origvalue = origvalue.trim();
			}

			StringBuffer usstring = new StringBuffer();
			StringBuffer ukstring = new StringBuffer();

			createDialectTerms(origvalue, usstring, ukstring, strSplitChars);

			if (!origvalue.equals(usstring.toString().trim()) && (language.toLowerCase().equals(EN) || language.toLowerCase().equals(US)))
			{
				return false;
			}
			else if (!origvalue.equals(ukstring.toString().trim()) && language.toLowerCase().equals(GB))
			{
				return false;
			}

		}
		catch (Exception e)
		{
			LOG.warn("Exception in checkTermSpelling", e);
		}
		return true;
	}

	public String getSpellingTerm(String term, String language)
	{
		try
		{
			if (!propsLoaded)
			{
				if (!loadProperties())
					return "";
			}
			String GB = "en-gb";
			String EN = "en";
			String US = "en-us";

			String origvalue = term.trim();

			int index = origvalue.lastIndexOf("(");
			if (index > -1)
			{
				origvalue = origvalue.substring(0, index);
				origvalue = origvalue.trim();
			}

			StringBuffer usstring = new StringBuffer();
			StringBuffer ukstring = new StringBuffer();

			createDialectTerms(origvalue, usstring, ukstring, strSplitChars);
			if (!usstring.toString().equals(ukstring.toString()))
			{
				if (origvalue.equals(usstring.toString().trim()) && (language.toLowerCase().equals(EN) || language.toLowerCase().equals(US)))
				{
					return ukstring.toString();
				}
				else if (origvalue.equals(ukstring.toString().trim()) && language.toLowerCase().equals(GB))
				{
					return usstring.toString();
				}
				else
				{
					return "";
				}
			}

		}
		catch (Exception e)
		{
			LOG.warn("Exception in getSpellingTerms", e);
		}
		return "";
	}

	public boolean loadProperties()
	{
		try
		{
			String aLine = null;

			in = new BufferedReader(new InputStreamReader(fis, "UTF-8"));

			strSplitChars = " ,-()";

			//Read in GB-US-spellingdiffs.txt UK<space>US
			while ((aLine = in.readLine()) != null)
			{
				aLine.trim();
				String[] line = aLine.split(" ");
				if (line.length > 1)
				{
					usspelling.put(line[1], line[0]);
					ukspelling.put(line[0], line[1]);
				}
			}
			in.close();
		}
		catch (Exception e)
		{
			LOG.warn("Exception in loadProperties", e);
			propsLoaded = false;
			return false;
		}
		propsLoaded = true;
		return true;
	}

	private void createDialectTerms(String strToSplit, StringBuffer UsString, StringBuffer UkString, String SplitChars)
	{
		String strtmp = "";
		String initialLowerStr;
		String initialStr;
		String endStr;
		String gettedStr;

		if (SplitChars.length() > 0)
		{
			String Charac = SplitChars.substring(0, 1);
			//                Charac="\\" + Charac;
			String nextSplitChars = SplitChars.substring(1);
			String[] line = strToSplit.split("\\" + Charac);
			int j;
			//Process GB spelling
			for (int i = 0; i < line.length; i++)
			{
				if (line[i].length() > 0)
				{
					initialLowerStr = line[i].substring(0, 1).toLowerCase();
					initialStr = line[i].substring(0, 1);
					if (line[i].length() > 1)
						endStr = line[i].substring(1);
					else
						endStr = "";
					strtmp = initialLowerStr + endStr;

					if (usspelling.containsKey(strtmp))
					{
						gettedStr = (String) usspelling.get(strtmp);
						if (initialLowerStr.equals(initialStr))
							UkString.append(gettedStr);
						else if (gettedStr.length() > 1)
							UkString.append(gettedStr.substring(0, 1).toUpperCase() + gettedStr.substring(1));
						else
							UkString.append(gettedStr.substring(0, 1).toUpperCase());

						UsString.append(line[i]);
					}
					else if (ukspelling.containsKey(strtmp))
					{
						gettedStr = (String) ukspelling.get(strtmp);
						if (initialLowerStr.equals(initialStr))
							UsString.append(gettedStr);
						else if (gettedStr.length() > 1)
							UsString.append(gettedStr.substring(0, 1).toUpperCase() + gettedStr.substring(1));
						else
							UsString.append(gettedStr.substring(0, 1).toUpperCase());

						UkString.append(line[i]);
					}
					else
					{
						if (nextSplitChars.length() > 0)
						{
							for (j = 0; j < nextSplitChars.length(); j++)
							{
								if (line[i].indexOf(nextSplitChars.charAt(j)) > -1)
									break;
							}
							if (j < nextSplitChars.length())
							{
								createDialectTerms(line[i], UsString, UkString, nextSplitChars.substring(j));
							}
							else
							{
								UsString.append(line[i]);
								UkString.append(line[i]);
							}
						}
						else
						{
							UsString.append(line[i]);
							UkString.append(line[i]);
						}
					}
				}
				if (i < line.length - 1)
				{
					UsString.append(Charac);
					UkString.append(Charac);
				}
			}
			if (strToSplit.endsWith(Charac))
			{
				UsString.append(Charac);
				UkString.append(Charac);
			}
		}
	}

	public String getConceptDomain()
	{
		return conceptDomain;
	}

	public void setConceptDomain(String conceptDomain)
	{
		this.conceptDomain = conceptDomain;
	}

	public String getRoleRange()
	{
		return roleRange;
	}

	public void setRoleRange(String roleRange)
	{
		this.roleRange = roleRange;
	}

	public String getGroupCondition()
	{
		return groupCondition;
	}

	public void setGroupCondition(String groupCondition)
	{
		this.groupCondition = groupCondition;
	}

	public boolean areParenthesesBalanced(String text)
	{
		MatchParen mp = new MatchParen();
		mp.add(text);
		return mp.isMatching();
	}

	public boolean isMemberOf(String conceptUUID, String refsetUUID) throws Exception
	{
		ConceptVersionBI concept = OTFUtility.getConceptVersion(UUID.fromString(conceptUUID));
		if (concept == null)
		{
			return false;
		}

		ConceptVersionBI refset = OTFUtility.getConceptVersion(UUID.fromString(refsetUUID));
		if (refset == null)
		{
			return false;
		}
		
		if (refset.isAnnotationStyleRefex())
		{
			for (RefexVersionBI<?> r : concept.getAnnotationsActive(OTFUtility.getViewCoordinate()))
			{
				if (r.getAssemblageNid() == refset.getNid())
				{
					return true;
				}
			}
		}
		else
		{
			for (RefexVersionBI<?> r : refset.getRefsetMembersActive())
			{
				if (r.getReferencedComponentNid() == concept.getNid())
				{
					return true;
				}
			}
		}
		return false;
	}

	public boolean isParentOf(String parentUUID, String subtypeUUID) throws Exception
	{
		ConceptVersionBI parentConcept = OTFUtility.getConceptVersion(UUID.fromString(parentUUID));
		if (parentConcept == null)
		{
			return false;
		}

		ConceptVersionBI subtypeConcept = OTFUtility.getConceptVersion(UUID.fromString(subtypeUUID));
		if (subtypeConcept == null)
		{
			return false;
		}
		return subtypeConcept.isKindOf(parentConcept);
	}

	public boolean isParentOfOrEqualTo(String parent, String subtype) throws Exception
	{
		return subtype.equals(parent) || isParentOf(parent, subtype);
	}
}
