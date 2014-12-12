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
package gov.va.isaac.drools.helper.templates;

/**
 * 
 * {@link AbstractTemplate}
 *
 * @author kec
 * @author afurber
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public abstract class AbstractTemplate
{

	public enum TemplateType
	{

		DESCRIPTION, CONCEPT, RELATIONSHIP, EXTENSION;
	}
	private String componentUuid;
	private TemplateType type;
	private String statusUuid;
	private String author;

	public TemplateType getType()
	{
		return type;
	}

	public void setType(TemplateType type)
	{
		this.type = type;
	}

	public String getStatusUuid()
	{
		return statusUuid;
	}

	public void setStatusUuid(String statusUuid)
	{
		this.statusUuid = statusUuid;
	}

	public String getAuthor()
	{
		return author;
	}

	public void setAuthor(String author)
	{
		this.author = author;
	}

	public String getComponentUuid()
	{
		return componentUuid;
	}

	public void setComponentUuid(String componentUuid)
	{
		this.componentUuid = componentUuid;
	}
}
