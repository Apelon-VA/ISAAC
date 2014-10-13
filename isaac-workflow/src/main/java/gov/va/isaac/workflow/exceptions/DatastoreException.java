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
package gov.va.isaac.workflow.exceptions;

import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link DatastoreException}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class DatastoreException extends Exception
{
	private static final Logger log = LoggerFactory.getLogger(DatastoreException.class);
	private static final long serialVersionUID = 932839320408796285L;
	
	public DatastoreException(String string)
	{
		super(string);
	}
	
	public DatastoreException(String string, Exception e)
	{
		super(string, e);
	}

	public DatastoreException(SQLException e)
	{
		super("Datastore failure", e);
		log.error("Datastore Failure", e);
	}
}
