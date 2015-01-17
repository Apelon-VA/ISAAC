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
 * {@link DescriptionTemplate}
 *
 * @author kec
 * @author afurber
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DescriptionTemplate extends AbstractTemplate
{

	private String text;
	private String langCode;
	private boolean initialCaseSignificant;

	public DescriptionTemplate()
	{
		setType(TemplateType.DESCRIPTION);
	}

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public String getLangCode()
	{
		return langCode;
	}

	public void setLangCode(String langCode)
	{
		this.langCode = langCode;
	}

	public boolean isInitialCaseSignificant()
	{
		return initialCaseSignificant;
	}

	public void setInitialCaseSignificant(boolean initialCaseSignificant)
	{
		this.initialCaseSignificant = initialCaseSignificant;
	}

}
