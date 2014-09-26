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
package gov.va.isaac.util;

import java.beans.PropertyVetoException;
import java.math.BigDecimal;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicDouble;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicFloat;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicInteger;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicLong;

/**
 * {@link NumberUtilities}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class NumberUtilities
{
	/**
	 * Return the wrapped number, or throw an exception if not parseable as an integer, long, float or double
	 */
	public static RefexDynamicDataBI wrapIntoRefexHolder(Number value) throws NumberFormatException
	{
		if (value == null)
		{
			return null;
		}
		try
		{
			if (value instanceof Integer)
			{
				return new RefexDynamicInteger(value.intValue());
			}
			else if (value instanceof Long)
			{
				return new RefexDynamicLong(value.longValue());
			}
			else if (value instanceof Float)
			{
				return new RefexDynamicFloat(value.floatValue());
			}
			else if (value instanceof Double)
			{
				return new RefexDynamicDouble(value.doubleValue());
			}
			else
			{
				throw new NumberFormatException("The value must be a numeric value of type int, long, float or double.");
			}
		}
		catch (PropertyVetoException e)
		{
			throw new RuntimeException("Should have been impossible", e);
		}
	}
	
	public static Number parseNumber(String value)
	{
		try
		{
			return Integer.parseInt(value);
		}
		catch (Exception e)
		{
			//noop
		}
		try
		{
			return Long.parseLong(value);
		}
		catch (Exception e)
		{
			//noop
		}
		try
		{
			return Float.parseFloat(value);
		}
		catch (Exception e)
		{
			//noop
		}
		try
		{
			return Double.parseDouble(value);
		}
		catch (Exception e)
		{
			throw new NumberFormatException("Not a number (" + value + ")");
		}
	}
	
	/**
	 * Compare two numbers of arbitrary data types
	 */
	public static int compare(final Number x, final Number y)
	{
		if (isSpecial(x) || isSpecial(y))
		{
			return Double.compare(x.doubleValue(), y.doubleValue());
		}
		else
		{
			return toBigDecimal(x).compareTo(toBigDecimal(y));
		}
	}

	private static boolean isSpecial(final Number x)
	{
		boolean specialDouble = x instanceof Double && (Double.isNaN((Double) x) || Double.isInfinite((Double) x));
		boolean specialFloat = x instanceof Float && (Float.isNaN((Float) x) || Float.isInfinite((Float) x));
		return specialDouble || specialFloat;
	}

	private static BigDecimal toBigDecimal(final Number number)
	{
		if (number instanceof Integer || number instanceof Long)
		{
			return new BigDecimal(number.longValue());
		}
		else if (number instanceof Float || number instanceof Double)
		{
			return new BigDecimal(number.doubleValue());
		}
		else
		{
			throw new RuntimeException("Unexpected data type passed in to toBigDecimal (" + number.getClass() + ")");
		}
	}
}
