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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.isaacDbProcessingRules.loinc;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * {@link SpreadsheetReader}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class SpreadsheetReader
{
	ArrayList<String> columnHeaders = new ArrayList<>();
	ArrayList<ArrayList<Cell>> data = new ArrayList<>();
	
	public SpreadsheetReader()
	{
		
	}
	
	public List<RuleDefinition> readSpreadSheet(InputStream is) throws IOException
	{
		XSSFWorkbook ss = new XSSFWorkbook(is);
		
		Sheet sheet = ss.getSheetAt(0);
		
		{
			//row 0 is just a comment
			//row 1 has headers
			Row row = sheet.getRow(1);
			for (int i = 0; i < row.getLastCellNum(); i++)
			{
				Cell c = row.getCell(i);
				columnHeaders.add(c ==  null ? "" : toString(c));
			}
		}
		
		for (int i = 2; i <= sheet.getLastRowNum(); i++)
		{
			Row r = sheet.getRow(i);
			
			ArrayList<Cell> values = new ArrayList<>();
			
			for (int col = 0; col < columnHeaders.size(); col++)
			{
				values.add(r.getCell(col));
			}
			data.add(values);
		}

		//Have read the entire spreadsheet - now process into our 'rule' format
		
		ArrayList<RuleDefinition> result = new ArrayList<>();
		
		for (int rowNum = 0; rowNum <= data.size(); rowNum++)
		{
			RuleDefinition rd = new RuleDefinition();
			Integer id = readIntColumn(rowNum, "ID");
			if (id == null)
			{
				//blank row?
				continue;
			}
			rd.id = id;
			rd.date = readDateColumn(rowNum, "Date");
			rd.action = Action.parse(readStringColumn(rowNum, "Action"));
			rd.sctFSN = readStringColumn(rowNum, "SCT FSN");
			rd.sctID = readLongColumn(rowNum, "SCT ID");
			rd.author = readStringColumn(rowNum, "Author");
			rd.comments = readStringColumn(rowNum, "Comments");
			
			
			ArrayList<SelectionCriteria> criteria = new ArrayList<>();
			
			while (true)
			{
				SelectionCriteria sc = new SelectionCriteria();
				sc.operand = readOperand(rowNum);
				sc.type = SelectionCriteriaType.parse(readStringColumn(rowNum, "Type"));
				sc.value = readStringColumn(rowNum, "Value");
				sc.valueId = readStringColumn(rowNum, "Value ID");
				
				criteria.add(sc);
				//peak at the next row, see if it is an additional criteria, or a new rule
				Integer nextId = readIntColumn(rowNum + 1, "ID");  //if the next row has an id, its a new rule
				String nextType = readStringColumn(rowNum + 1, "Type");  //check to see if we hit the end of the rows
				if (nextId != null || nextType == null)
				{
					break;
				}
				else  //more criteria for this rule
				{
					rowNum++;
				}
			}
			
			rd.criteria = criteria;
			result.add(rd);
		}
		return result;
	}
	
	
	private List<Cell> findMatchingCellsOnRow(int row, String requestedColumnName)
	{
		ArrayList<Cell> result = new ArrayList<>();
		for (int i = 0; i < columnHeaders.size(); i++)
		{
			if (requestedColumnName.equalsIgnoreCase(columnHeaders.get(i)))
			{
				if (data.size() > row && data.get(row) != null)
				{
					Cell c = data.get(row).get(i);
					if (c != null && c.getCellType() != Cell.CELL_TYPE_BLANK)
					{
						result.add(c);
					}
				}
			}
		}
		return result;
	}
	
	private Operand readOperand(int row)
	{
		//There are two operand columns, but I would only expect one to be populated on a given row
		List<Cell> cells = findMatchingCellsOnRow(row, "Operand");
		Operand result = null;
		for (Cell c : cells)
		{
			String val = toString(c);
			if (val.length() > 0)
			{
				if (result == null)
				{
					result = Operand.parse(val);
				}
				else
				{
					throw new RuntimeException("Two operands on a single row!");
				}
			}
		}
		return result;
	}
	
	private Object readColumn(int row, String requestedColumnName, Function<Cell, Object> readFunction)
	{
		List<Cell> cells = findMatchingCellsOnRow(row, requestedColumnName);
		if (cells.size() > 1)
		{
			throw new RuntimeException("To many matching cells");
		}
		else if (cells.size() == 0 || cells.get(0).getCellType() == Cell.CELL_TYPE_BLANK)
		{
			return null;
		}
		else
		{
			return readFunction.apply(cells.get(0));
		}
	}
	
	private Integer readIntColumn(int row, String requestedColumnName)
	{
		return (Integer)readColumn(row, requestedColumnName, new Function<Cell, Object>()
		{
			@Override
			public Object apply(Cell cell)
			{
				return new Double(cell.getNumericCellValue()).intValue();
			}
		});
	}
	
	private long readDateColumn(int row, String requestedColumnName)
	{
		return ((Long)readColumn(row, requestedColumnName, new Function<Cell, Object>()
		{
			@Override
			public Object apply(Cell cell)
			{
				return cell.getDateCellValue().getTime();
			}
		})).longValue();
	}
	
	private Long readLongColumn(int row, String requestedColumnName)
	{
		return (Long)readColumn(row, requestedColumnName, new Function<Cell, Object>()
		{
			@Override
			public Object apply(Cell cell)
			{
				return new Double(cell.getNumericCellValue()).longValue();
			}
		});
	}
	
	private String readStringColumn(int row, String requestedColumnName)
	{
		return (String)readColumn(row, requestedColumnName, new Function<Cell, Object>()
		{
			@Override
			public Object apply(Cell cell)
			{
				return SpreadsheetReader.toString(cell);
			}
		});
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
	
	public static void main(String[] args) throws IOException
	{
		for (RuleDefinition rd : new SpreadsheetReader().readSpreadSheet(SpreadsheetReader.class.getResourceAsStream("/rules.xlsx")))
		{
			System.out.println(rd);
		}
	}
}
