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
package gov.va.issac.drools.helper;

import gov.va.isaac.drools.helper.MatchParen;

import java.beans.PropertyVetoException;
import java.io.IOException;

import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.junit.Test;
import org.junit.Assert;

/**
 * {@link MatchParenTest}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class MatchParenTest
{
	@Test
	public void testMatchParen() throws PropertyVetoException, IOException, ContradictionException
	{
		Assert.assertFalse(test("(3+(4*5)/6*7"));
		Assert.assertTrue(test("(3+(4*5)/6)*7"));
		Assert.assertFalse(test("(3+(4*5)/6))*7"));
		Assert.assertTrue(test("(3+[4*5]/6)*7"));
		Assert.assertFalse(test("(3+[4*5)/6]*7"));
		Assert.assertFalse(test("(3+[4*5])/6*7)"));
		Assert.assertTrue(test("sin parentesis"));
	}

	private boolean test(String s)
	{
		MatchParen mp = new MatchParen();
		mp.add(s);
		return mp.isMatching();
	}
}
