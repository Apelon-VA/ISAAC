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
package gov.va.legoEdit.model;

import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.Pncs;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * 
 * {@link LegoReference}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class LegoReference
{
	private String legoUUID;
	private String stampUUID;
	private XMLGregorianCalendar stampTime;
	private Pncs pncs;
	private boolean isNew = false;

	public LegoReference(Lego lego)
	{
		this.legoUUID = lego.getLegoUUID();
		this.stampUUID = lego.getStamp().getUuid();
		this.stampTime = lego.getStamp().getTime();
		this.pncs = lego.getPncs();
	}

	public String getLegoUUID()
	{
		return legoUUID;
	}

	public String getStampUUID()
	{
		return stampUUID;
	}

	public Pncs getPncs()
	{
		return pncs;
	}

	public XMLGregorianCalendar getStampTime()
	{
		return stampTime;
	}

	public String getUniqueId()
	{
		return ModelUtil.makeUniqueLegoID(legoUUID, stampUUID);
	}

	public void setIsNew(boolean isNew)
	{
		this.isNew = isNew;
	}

	public boolean isNew()
	{
		return isNew;
	}

	public static List<LegoReference> convert(Collection<Lego> legos, boolean unsaved)
	{
		List<LegoReference> result = convert(legos);
		if (unsaved)
		{
			for (LegoReference lr : result)
			{
				lr.setIsNew(true);
			}
		}
		return result;
	}

	public static List<LegoReference> convert(Collection<Lego> legos)
	{
		ArrayList<LegoReference> result = new ArrayList<>(legos.size());
		for (Lego l : legos)
		{
			result.add(new LegoReference(l));
		}
		return result;
	}
}
