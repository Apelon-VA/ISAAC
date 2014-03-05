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
package gov.va.legoEdit.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * 
 * TimeConvert
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 * Copyright 2013
 */
public class TimeConvert
{
	private static DatatypeFactory datatypeFactory_;
	private static DateFormat dateFormat_ = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	static
	{
		try
		{
			datatypeFactory_ = DatatypeFactory.newInstance();
		}
		catch (Exception e)
		{
			throw new RuntimeException("Expected to be impossible", e);
		}
	}

	public static XMLGregorianCalendar convert(long time)
	{
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(time);
		return datatypeFactory_.newXMLGregorianCalendar(gc);
	}

	public static long convert(XMLGregorianCalendar gc)
	{
		return gc.toGregorianCalendar().getTimeInMillis();
	}

	public static String format(XMLGregorianCalendar gc)
	{
		return format(gc.toGregorianCalendar().getTimeInMillis());
	}

	public static String format(long time)
	{
		return dateFormat_.format(new Date(time));
	}
}
