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
 *    http://www.apache.org/licenses/LICENSE-2.0
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
 * {@link DrIdentifier}
 *
 * @author kec
 * @author afurber
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DrIdentifier extends DrComponent {

	private String primordialUuid;

	private String componentUuid;

	private String authorityUuid;
	private String denotation;

	// Inferred properties
	// none yet
	
	public DrIdentifier() {
		this.primordialUuid = "";
		this.componentUuid = "";
		this.authorityUuid = "";
		this.denotation = "";
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("");
		try {
			sb.append("primordialUuid: " + primordialUuid + ",");

			try {
				ConceptChronicleBI component = AppContext.getService(TerminologyStoreDI.class).getConcept(UUID.fromString(componentUuid));
				sb.append(" Source Rel: " + component + " (" + componentUuid + "),");
			} catch (IllegalArgumentException ex) {
			}

			try {
				ConceptChronicleBI authority = AppContext.getService(TerminologyStoreDI.class).getConcept(UUID.fromString(authorityUuid));
				sb.append(" Type: " + authority + " (" + authorityUuid + "),");
			} catch (IllegalArgumentException ex) {
			}

			sb.append(" Denotation: " + denotation + ",");
			sb.append(" DRCOMPONENT FIELDS: {" + super.toString() + "}, ");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	public String getPrimordialUuid() {
		return primordialUuid;
	}

	public void setPrimordialUuid(String primordialUuid) {
		this.primordialUuid = primordialUuid;
	}

	public String getComponentUuid() {
		return componentUuid;
	}

	public void setComponentUuid(String componentUuid) {
		this.componentUuid = componentUuid;
	}

	public String getAuthorityUuid() {
		return authorityUuid;
	}

	public void setAuthorityUuid(String authorityUuid) {
		this.authorityUuid = authorityUuid;
	}

	public String getDenotation() {
		return denotation;
	}

	public void setDenotation(String denotation) {
		this.denotation = denotation;
	}
}
