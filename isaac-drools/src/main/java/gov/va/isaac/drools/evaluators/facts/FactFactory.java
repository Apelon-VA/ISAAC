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
package gov.va.isaac.drools.evaluators.facts;

import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.group.RelGroupVersionBI;
import org.ihtsdo.otf.tcc.api.spec.DescriptionSpec;
import org.ihtsdo.otf.tcc.api.spec.RelSpec;

/**
 * 
 * {@link FactFactory}
 * 
 * @author kec
 * @author afurber
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class FactFactory
{

	public static Fact<?> get(Context context, Object component, ViewCoordinate vc)
	{
		if (ConceptAttributeVersionBI.class.isAssignableFrom(component.getClass()))
		{
			return new ConAttrFact(context, (ConceptAttributeVersionBI<?>) component, vc);
		}
		if (ConceptVersionBI.class.isAssignableFrom(component.getClass()))
		{
			return new ConceptFact(context, (ConceptVersionBI) component, vc);
		}
		if (DescriptionVersionBI.class.isAssignableFrom(component.getClass()))
		{
			return new DescFact(context, (DescriptionVersionBI<?>) component, vc);
		}
		if (RelationshipVersionBI.class.isAssignableFrom(component.getClass()))
		{
			return new RelFact(context, (RelationshipVersionBI<?>) component, vc);
		}
		if (RelGroupVersionBI.class.isAssignableFrom(component.getClass()))
		{
			return new RelGroupFact(context, (RelGroupVersionBI) component, vc);
		}
		if (RelSpec.class.isAssignableFrom(component.getClass()))
		{
			return new RelSpecFact(context, (RelSpec) component);
		}
		if (DescriptionSpec.class.isAssignableFrom(component.getClass()))
		{
			return new DescSpecFact(context, (DescriptionSpec) component);
		}
		if (RefexVersionBI.class.isAssignableFrom(component.getClass()))
		{
			return new RefexFact(context, (RefexVersionBI<?>) component, vc);
		}
		throw new UnsupportedOperationException("Can't handle component: " + component);
	}

	public static Fact<?> get(View view)
	{
		return new ViewFact<View>(view);
	}
}
