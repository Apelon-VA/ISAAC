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

/**
 * The Class UnsupportedDialectOrLanguage represents an exception which occurred
 * due to an unsupported dialect or language. Throw this kind of exception when
 * working with dialects and languages rather than doing nothing if there is no
 * case for the specified language.
 *
 * {@link UnsupportedDialectOrLanguage}
 *
 * @author kec
 * @author afurber
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class UnsupportedDialectOrLanguage extends Exception
{

	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new unsupported dialect or language.
	 *
	 * @param thrwbl the throwable
	 */
	public UnsupportedDialectOrLanguage(Throwable thrwbl)
	{
		super(thrwbl);
	}

	/**
	 * Instantiates a new unsupported dialect or language.
	 *
	 * @param string representing a description of the exception
	 * @param thrwbl the throwable
	 */
	public UnsupportedDialectOrLanguage(String string, Throwable thrwbl)
	{
		super(string, thrwbl);
	}

	/**
	 * Instantiates a new unsupported dialect or language.
	 *
	 * @param string representing a description of the exception
	 */
	public UnsupportedDialectOrLanguage(String string)
	{
		super(string);
	}

	/**
	 * Instantiates a new unsupported dialect or language.
	 */
	public UnsupportedDialectOrLanguage()
	{
	}
}
