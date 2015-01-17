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

import java.util.UUID;

/**
 * 
 * {@link ResultsItem}
 * 
 * @author kec
 * @author afurber
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public abstract class ResultsItem
{
	private int errorCode;
	private String message;
	private Severity severity;
	private String ruleUuid;

	public enum Severity
	{
		ERROR(UUID.fromString("f9545a20-12cf-11e0-ac64-0800200c9a66"), "Error"), 
		WARNING(UUID.fromString("f9545a21-12cf-11e0-ac64-0800200c9a66"), "Warning"), 
		NOTIFICATION(UUID.fromString("f9545a22-12cf-11e0-ac64-0800200c9a66"), "Notification");

		private final UUID severityUuid;
		private final String name;

		Severity(UUID severityUuid, String name)
		{
			this.severityUuid = severityUuid;
			this.name = name;
		}

		public UUID getSeverityUuid()
		{
			return severityUuid;
		}

		public String getName()
		{
			return name;
		}

	}

	protected ResultsItem(Severity severity)
	{
		super();
		this.severity = severity;
	}

	protected ResultsItem(int errorCode, String message, Severity severity, String ruleUuid)
	{
		super();
		this.errorCode = errorCode;
		this.message = message;
		this.severity = severity;
		this.ruleUuid = ruleUuid;
	}

	public int getErrorCode()
	{
		return errorCode;
	}

	public void setErrorCode(int errorCode)
	{
		this.errorCode = errorCode;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public Severity getSeverity()
	{
		return severity;
	}

	public void setSeverity(Severity severity)
	{
		this.severity = severity;
	}

	public String getRuleUuid()
	{
		return ruleUuid;
	}

	public void setRuleUuid(String ruleUuid)
	{
		this.ruleUuid = ruleUuid;
	}

}
