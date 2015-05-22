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
package gov.va.isaac.request.uscrs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * {@link USCRSBatchTemplate}
 * 
 * This class has enum constants that correspond to the USCRS Excel template file (included in the resources folder) from
 * https://uscrs.nlm.nih.gov/main.xhtml
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class USCRSBatchTemplate
{
	/**
	 * All of the following code, down through the end comment - is generated automatically by executing the main(String[] args) method
	 * which is part of this class. That method reads the excel template, and creates this set of enums.
	 * 
	 * At runtime, the constructor for this class reads the supplied excel templates, and validates that the template still aligns with
	 * the enums hard-coded here. In the future, if the template changes - rerun the main method to regenerate these enum constants.
	 */

	public enum SHEET
	{
		Help, New_Concept, New_Synonym, Add_Parent, Change_Description, Change_Parent, Change_Relationship, New_Relationship, Retire_Concept, Retire_Description,
		Retire_Relationship, Other, metadata
	};

	public enum COLUMN
	{
		Terminology_3_, Local_Term, UMLS_CUI, Terminology, Description, Source_Concept_Id, Change_Description_Status_To, Terminology_1_, Child_Concept_Id,
		Parent_Concept_Id_2_, Parent_Concept__Id_3_, Semantic_Tag, Request_Id, Change_Concept_Status_To, Description_Id, Characteristic_Type, Preferred_Term,
		Proposed_Use, Relationship_Type, New_Parent_Terminology, Case_Significance, Justification, Relationship_Group, Topic, Destination_Terminology,
		Source_Terminology, Destination_Concept_Id, Parent_Concept_Id_1_, Fully_Specified_Name, Terminology_2_, Definition, Term, Duplicate_Concept_Id, Relationship_Id,
		Parent_Concept_Id, Concept_Id, Note, New_Parent_Concept_Id, Local_Code, Synonym, Refinability
	};

	public enum PICKLIST_Case_Significance
	{
		Entire_term_case_sensitive("Entire term case sensitive"), Entire_term_case_insensitive("Entire term case insensitive"), Only_initial_character_case_insensitive(
				"Only initial character case insensitive");
		private String value;

		private PICKLIST_Case_Significance(String pickListValue)
		{
			value = pickListValue;
		}

		@Override
		public String toString()
		{
			return value;
		}

		public static PICKLIST_Case_Significance find(String value)
		{
			return PICKLIST_Case_Significance.valueOf(enumSafeCharExchange(value));
		}
	};

	public enum PICKLIST_Source_Terminology
	{
		SNOMED_CT_International("SNOMED CT International"), SNOMED_CT_National_US("SNOMED CT National US"), New_Concept_Requests("New Concept Requests"),
		Current_Batch_Requests("Current Batch Requests");
		private String value;

		private PICKLIST_Source_Terminology(String pickListValue)
		{
			value = pickListValue;
		}

		@Override
		public String toString()
		{
			return value;
		}

		public static PICKLIST_Source_Terminology find(String value)
		{
			return PICKLIST_Source_Terminology.valueOf(enumSafeCharExchange(value));
		}
	};

	public enum PICKLIST_Relationship_Type
	{
		Is_a("Is a"), Access("Access"), Associated_finding("Associated finding"), Associated_morphology("Associated morphology"), Associated_procedure(
				"Associated procedure"), Associated_with("Associated with"), After("After"), Causative_agent("Causative agent"), Due_to("Due to"), Clinical_course(
				"Clinical course"), Component("Component"), Direct_substance("Direct substance"), Episodicity("Episodicity"), Finding_context("Finding context"),
		Finding_informer("Finding informer"), Finding_method("Finding method"), Finding_site("Finding site"), Has_active_ingredient("Has active ingredient"),
		Has_definitional_manifestation("Has definitional manifestation"), Has_dose_form("Has dose form"), Has_focus("Has focus"), Has_intent("Has intent"),
		Has_interpretation("Has interpretation"), Has_specimen("Has specimen"), Interprets("Interprets"), Laterality("Laterality"), Measurement_method(
				"Measurement method"), Method("Method"), Occurrence("Occurrence"), Part_of("Part of"), Pathological_process("Pathological process"),
		Priority("Priority"), Procedure_context("Procedure context"), Procedure_device("Procedure device"), Direct_device("Direct device"), Indirect_device(
				"Indirect device"), Using_device("Using device"), Using_access_device("Using access device"), Procedure_morphology("Procedure morphology"),
		Direct_morphology("Direct morphology"), Indirect_morphology("Indirect morphology"), Procedure_site("Procedure site"), Procedure_site___Direct(
				"Procedure site - Direct"), Procedure_site___Indirect("Procedure site - Indirect"), Property("Property"), Recipient_category("Recipient category"),
		Revision_status("Revision status"), Route_of_administration("Route of administration"), Scale_type("Scale type"), Severity("Severity"), Specimen_procedure(
				"Specimen procedure"), Specimen_source_identity("Specimen source identity"), Specimen_source_morphology("Specimen source morphology"),
		Specimen_source_topography("Specimen source topography"), Specimen_substance("Specimen substance"), Subject_of_information("Subject of information"),
		Subject_relationship_context("Subject relationship context"), Surgical_approach("Surgical approach"), Temporal_context("Temporal context"), Time_aspect(
				"Time aspect"), Using_energy("Using energy");
		private String value;

		private PICKLIST_Relationship_Type(String pickListValue)
		{
			value = pickListValue;
		}

		@Override
		public String toString()
		{
			return value;
		}

		public static PICKLIST_Relationship_Type find(String value)
		{
			return PICKLIST_Relationship_Type.valueOf(enumSafeCharExchange(value));
		}
	};

	public enum PICKLIST_Characteristic_Type
	{
		Defining_relationship("Defining relationship"), Qualifying_relationship("Qualifying relationship"), Additional_relationship("Additional relationship");
		private String value;

		private PICKLIST_Characteristic_Type(String pickListValue)
		{
			value = pickListValue;
		}

		@Override
		public String toString()
		{
			return value;
		}

		public static PICKLIST_Characteristic_Type find(String value)
		{
			return PICKLIST_Characteristic_Type.valueOf(enumSafeCharExchange(value));
		}
	};

	public enum PICKLIST_Refinability
	{
		Not_refinable("Not refinable"), Optional("Optional"), Mandatory("Mandatory");
		private String value;

		private PICKLIST_Refinability(String pickListValue)
		{
			value = pickListValue;
		}

		@Override
		public String toString()
		{
			return value;
		}

		public static PICKLIST_Refinability find(String value)
		{
			return PICKLIST_Refinability.valueOf(enumSafeCharExchange(value));
		}
	};

	public enum PICKLIST_Change_Concept_Status_To
	{
		Retired("Retired"), Duplicate("Duplicate"), Outdated("Outdated"), Ambiguous("Ambiguous"), Erroneous("Erroneous"), Limited("Limited"), Moved_elsewhere(
				"Moved elsewhere"), Pending_move("Pending move");
		private String value;

		private PICKLIST_Change_Concept_Status_To(String pickListValue)
		{
			value = pickListValue;
		}

		@Override
		public String toString()
		{
			return value;
		}

		public static PICKLIST_Change_Concept_Status_To find(String value)
		{
			return PICKLIST_Change_Concept_Status_To.valueOf(enumSafeCharExchange(value));
		}
	};

	public enum PICKLIST_Semantic_Tag
	{
		administrative_concept("administrative concept"), assessment_scale("assessment scale"), attribute("attribute"), body_structure("body structure"), cell("cell"),
		cell_structure("cell structure"), disorder("disorder"), environment("environment"), environment___location("environment / location"),
		ethnic_group("ethnic group"), event("event"), finding("finding"), geographic_location("geographic location"), inactive_concept("inactive concept"), life_style(
				"life style"), link_assertion("link assertion"), linkage_concept("linkage concept"), morphologic_abnormality("morphologic abnormality"),
		namespace_concept("namespace concept"), navigational_concept("navigational concept"), observable_entity("observable entity"), occupation("occupation"), organism(
				"organism"), person("person"), physical_force("physical force"), physical_object("physical object"), procedure("procedure"), product("product"),
		qualifier_value("qualifier value"), racial_group("racial group"), record_artifact("record artifact"), regime_therapy("regime/therapy"), religion_philosophy(
				"religion/philosophy"), situation("situation"), social_concept("social concept"), special_concept("special concept"), specimen("specimen"),
		staging_scale("staging scale"), substance("substance"), tumor_staging("tumor staging");
		private String value;

		private PICKLIST_Semantic_Tag(String pickListValue)
		{
			value = pickListValue;
		}

		@Override
		public String toString()
		{
			return value;
		}

		public static PICKLIST_Semantic_Tag find(String value)
		{
			return PICKLIST_Semantic_Tag.valueOf(enumSafeCharExchange(value));
		}
	};

	/**
	 * END OF GENERATED CODE
	 */

	private HashMap<SHEET, Integer> sheetNamePositionMap = new HashMap<>();
	private HashMap<SHEET, LinkedHashMap<COLUMN, Integer>> columnNamePositionMap = new HashMap<>();
	private Workbook wb;
	private CreationHelper ch;
	private Sheet editSheet = null;
	private SHEET editSheetEnum = null;
	private int editSheetRowNum = Integer.MIN_VALUE;
	private Row currentEditRow = null;

	public USCRSBatchTemplate(InputStream spreadsheetTemplate) throws IOException
	{
		wb = new HSSFWorkbook(spreadsheetTemplate);
		ch = wb.getCreationHelper();

		for (int i = 0; i < wb.getNumberOfSheets(); i++)
		{
			Sheet s = wb.getSheetAt(i);
			SHEET sheetEnum = SHEET.valueOf(enumSafeCharExchange(s.getSheetName()));
			if (sheetEnum == null)
			{
				throw new RuntimeException("No enum type found for sheet " + s.getSheetName() + " - code out of sync with template");
			}
			else
			{
				sheetNamePositionMap.put(sheetEnum, i);
			}

			if (s.getSheetName().equals("Help"))
			{
				continue;
			}

			LinkedHashMap<COLUMN, Integer> colList = new LinkedHashMap<>();
			columnNamePositionMap.put(sheetEnum, colList);

			wb.getSheetAt(i)
					.getRow(0)
					.forEach(headerCell -> {
						if (s.getSheetName().equals("metadata") && headerCell.getCellType() == Cell.CELL_TYPE_NUMERIC)
						{
							//SKIP - the metadata tab has a cell that is just a number - likely the release date
						}
						else
						{
							COLUMN colEnum = COLUMN.valueOf(enumSafeCharExchange(toString(headerCell)));
							if (colEnum == null)
							{
								throw new RuntimeException("No enum type found for colum " + toString(headerCell) + " on sheet " + s.getSheetName()
										+ " - code out of sync with template");
							}
							else
							{
								colList.put(colEnum, headerCell.getColumnIndex());
							}
						}

					});
		}
	}

	private Sheet getSheet(SHEET sheet)
	{
		return wb.getSheetAt(sheetNamePositionMap.get(sheet));
	}

	public List<COLUMN> getColumnsOfSheet(SHEET sheet)
	{
		return new ArrayList<COLUMN>(columnNamePositionMap.get(sheet).keySet());
	}

	public void selectSheet(SHEET sheet)
	{
		editSheetEnum = sheet;
		editSheet = getSheet(sheet);
		currentEditRow = null;
		editSheetRowNum = editSheet.getLastRowNum() + 1;
	}

	public void addRow()
	{
		if (editSheet == null)
		{
			throw new RuntimeException("Select a sheet first!");
		}
		currentEditRow = editSheet.createRow(editSheetRowNum++);
	}

	public void addStringCell(COLUMN column, String value)
	{
		if (currentEditRow == null)
		{
			throw new RuntimeException("Call addRow() first");
		}
		
		Integer cellPos = columnNamePositionMap.get(editSheetEnum).get(column);
		if (cellPos == null)
		{
			throw new RuntimeException("Couldn't find the correct cell position for column " + column + " in sheet " + editSheetEnum);
		}
		
		Cell cell = currentEditRow.createCell(cellPos, Cell.CELL_TYPE_STRING);
		cell.setCellValue(ch.createRichTextString(value));
	}

	public void addNumericCell(COLUMN column, double value)
	{
		if (currentEditRow == null)
		{
			throw new RuntimeException("Call addRow() first");
		}
		Integer cellPos = columnNamePositionMap.get(editSheetEnum).get(column);
		if (cellPos == null)
		{
			throw new RuntimeException("Couldn't find the correct cell position for column " + column + " in sheet " + editSheetEnum);
		}
		Cell cell = currentEditRow.createCell(cellPos, Cell.CELL_TYPE_NUMERIC);
		cell.setCellValue(value);
	}

	public void saveFile(File writeTo) throws IOException
	{
		FileOutputStream out = new FileOutputStream(writeTo);
		wb.write(out);
		out.flush();
		out.close();
	}
	
	public Workbook getExcel() {
		return wb;
	}

	private static String toString(Cell cell)
	{
		switch (cell.getCellType())
		{
			case Cell.CELL_TYPE_BLANK:
				return "";

			case Cell.CELL_TYPE_BOOLEAN:
				return cell.getBooleanCellValue() + "";

			case Cell.CELL_TYPE_NUMERIC:
				return cell.getNumericCellValue() + "";

			case Cell.CELL_TYPE_STRING:
				return cell.getStringCellValue();

			case Cell.CELL_TYPE_ERROR:
				return "_ERROR_ " + cell.getErrorCellValue();

			case Cell.CELL_TYPE_FORMULA:
				return cell.getCellFormula() + "";
			default :
				throw new RuntimeException("No toString is available for the cell type!");
		}
	}

	private static String enumSafeCharExchange(String s)
	{
		s = s.replaceAll(" ", "_");
		s = s.replaceAll("\\(", "_");
		s = s.replaceAll("\\)", "_");
		s = s.replaceAll("/", "_");
		s = s.replaceAll("-", "_");
		return s;
	}

	/**
	 * Generate the enums and constants from a template file for use at the top of this class
	 */
	public static void main(String[] args) throws IOException
	{
		//USCRSBatchTemplate b = new USCRSBatchTemplate(USCRSBatchTemplate.class.getResourceAsStream("/USCRS_Batch_Template-2015-01-27.xls"));

		Workbook wb = new HSSFWorkbook(USCRSBatchTemplate.class.getResourceAsStream("/USCRS_Batch_Template-2015-01-27.xls"));

		ArrayList<String> sheets = new ArrayList<>();
		HashSet<String> columns = new HashSet<>();

		LinkedHashMap<String, ArrayList<String>> pickLists = new LinkedHashMap<>();

		for (int i = 0; i < wb.getNumberOfSheets(); i++)
		{
			Sheet sheet = wb.getSheetAt(i);
			String sheetName = sheet.getSheetName();
			sheets.add(sheetName);
			if (sheetName.equals("Help"))
			{
				continue;
			}

			sheet.getRow(0).forEach(headerCell -> {
				if (sheetName.equals("metadata") && headerCell.getCellType() == Cell.CELL_TYPE_NUMERIC)
				{
					//SKIP - the metadata tab has a cell that is just a number - likely the release date
				}
				else
				{
					String stringValue = toString(headerCell);
					columns.add(stringValue);
					if (sheetName.equals("metadata"))
					{
						pickLists.put(stringValue, new ArrayList<>());
						for (int row = 1; row < sheet.getLastRowNum(); row++)
						{
							Cell valueCell = sheet.getRow(row).getCell(headerCell.getColumnIndex());
							if (valueCell != null)
							{
								String s = toString(valueCell);
								if (s.length() > 0)
								{
									pickLists.get(stringValue).add(s);
								}
							}
						}
					}
				}
			});
		}

		String eol = System.getProperty("line.separator");

		StringBuilder sb = new StringBuilder();
		int i = 0;
		sb.append("public enum SHEET {");
		for (String s : sheets)
		{
			sb.append(enumSafeCharExchange(s));
			sb.append(", ");
			i++;
			if (i % 8 == 0)
			{
				i = 0;
				sb.append(eol);
			}
		}
		sb.setLength(sb.length() - (i == 0 ? 3 : 2));
		sb.append("};");
		System.out.println(sb);
		System.out.println();

		sb.setLength(0);
		i = 0;
		sb.append("public enum COLUMN {");
		for (String c : columns)
		{
			sb.append(enumSafeCharExchange(c));
			sb.append(", ");
			i++;
			if (i % 8 == 0)
			{
				i = 0;
				sb.append(eol);
			}
		}
		sb.setLength(sb.length() - (i == 0 ? 3 : 2));
		sb.append("};");
		System.out.println(sb);
		sb.setLength(0);
		i = 0;

		for (Entry<String, ArrayList<String>> x : pickLists.entrySet())
		{
			sb.append("public enum PICKLIST_");
			sb.append(enumSafeCharExchange(x.getKey()));
			sb.append(" {");
			for (String s : x.getValue())
			{
				sb.append(enumSafeCharExchange(s));
				sb.append("(\"");
				sb.append(s);
				sb.append("\")");
				sb.append(", ");
				i++;
				if (i % 2 == 0)
				{
					i = 0;
					sb.append(eol);
				}
			}
			sb.setLength(sb.length() - (i == 0 ? 3 : 2));
			sb.append(";" + eol);
			sb.append("\tprivate String value;" + eol + eol);
			sb.append("\tprivate PICKLIST_" + enumSafeCharExchange(x.getKey()) + " (String pickListValue)" + eol);
			sb.append("\t{" + eol);
			sb.append("\t\tvalue = pickListValue;" + eol);
			sb.append("\t}" + eol);

			sb.append("" + eol);
			sb.append("\t@Override" + eol);
			sb.append("\tpublic String toString()" + eol);
			sb.append("\t{" + eol);
			sb.append("\t\treturn value;" + eol);
			sb.append("\t}" + eol);

			sb.append("" + eol);
			sb.append("\tpublic static PICKLIST_" + enumSafeCharExchange(x.getKey()) + " find(String value)" + eol);
			sb.append("\t{" + eol);
			sb.append("\t\treturn PICKLIST_" + enumSafeCharExchange(x.getKey()) + ".valueOf(enumSafeCharExchange(value));" + eol);
			sb.append("\t}" + eol);
			sb.append("};");

			System.out.println(sb);
			sb.setLength(0);
			i = 0;
			System.out.println();
		}
	}
}
