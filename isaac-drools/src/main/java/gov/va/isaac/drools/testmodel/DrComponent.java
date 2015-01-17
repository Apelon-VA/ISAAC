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
package gov.va.isaac.drools.testmodel;

import gov.va.isaac.AppContext;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;

/**
 * 
 * {@link DrComponent}
 *
 * @author kec
 * @author afurber
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public abstract class DrComponent
{

	private String statusUuid;
	private String pathUuid;
	private String authorUuid;
	private Long time;

	private String factContextName;

	// Inferred properties
	private boolean published = false;
	private boolean extensionComponent = false;
	private String extensionId = "";
	private boolean active = false;

	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer("");
		try
		{
			try
			{
				ConceptChronicleBI status = AppContext.getService(TerminologyStoreDI.class).getConcept(UUID.fromString(statusUuid));
				sb.append(" Status: " + status + " (" + statusUuid + "),");
			}
			catch (Exception ex)
			{
			}

			try
			{
				ConceptChronicleBI path = AppContext.getService(TerminologyStoreDI.class).getConcept(UUID.fromString(pathUuid));
				sb.append(" Path: " + path + " (" + pathUuid + "),");
			}
			catch (Exception ex)
			{
			}

			try
			{
				ConceptChronicleBI author = AppContext.getService(TerminologyStoreDI.class).getConcept(UUID.fromString(authorUuid));
				sb.append(" Author: " + author + " (" + authorUuid + "),");
			}
			catch (Exception ex)
			{
			}

			sb.append(" Time: " + time + ",");
			sb.append(" Fact Context Name: " + factContextName + ",");
			sb.append(" Published: " + published + ",");
			sb.append(" Extension Component: " + extensionComponent + ",");
			sb.append(" Active: " + active);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return sb.toString();
	}

	public String getStatusUuid()
	{
		return statusUuid;
	}

	public void setStatusUuid(String statusUuid)
	{
		this.statusUuid = statusUuid;
	}

	public String getPathUuid()
	{
		return pathUuid;
	}

	public void setPathUuid(String pathUuid)
	{
		this.pathUuid = pathUuid;
	}

	public String getAuthorUuid()
	{
		return authorUuid;
	}

	public void setAuthorUuid(String authorUuid)
	{
		this.authorUuid = authorUuid;
	}

	public Long getTime()
	{
		return time;
	}

	public void setTime(Long time)
	{
		this.time = time;
	}

	public String getFactContextName()
	{
		return factContextName;
	}

	public void setFactContextName(String factContextName)
	{
		this.factContextName = factContextName;
	}

	public boolean isPublished()
	{
		return published;
	}

	public void setPublished(boolean published)
	{
		this.published = published;
	}

	public boolean isExtensionComponent()
	{
		return extensionComponent;
	}

	public void setExtensionComponent(boolean extensionComponent)
	{
		this.extensionComponent = extensionComponent;
	}

	public String getExtensionId()
	{
		return extensionId;
	}

	public void setExtensionId(String extensionId)
	{
		this.extensionId = extensionId;
	}

	public boolean isActive()
	{
		return active;
	}

	public void setActive(boolean active)
	{
		this.active = active;
	}

}
