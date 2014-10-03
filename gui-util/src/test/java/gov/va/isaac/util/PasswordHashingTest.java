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
package gov.va.isaac.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * {@link PasswordHashingTest}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class PasswordHashingTest
{
	@Test
	public void testOne() throws Exception
	{
		String password = "My password is really good!";
		String passwordHash = PasswordHasher.getSaltedHash(password);
		Assert.assertTrue(PasswordHasher.check(password, passwordHash));
		Assert.assertFalse(PasswordHasher.check("not my password", passwordHash));
	}
	
	@Test
	public void testTwo() throws Exception
	{
		String password = "password";
		String passwordHash = PasswordHasher.getSaltedHash(password);
		Assert.assertTrue(PasswordHasher.check(password, passwordHash));
		Assert.assertFalse(PasswordHasher.check("fred", passwordHash));
	}
	
	@Test
	public void testThree() throws Exception
	{
		String password = "µJû5¥¨J«eÜäÅT5¼, BìRß¸jAf½çx.îüöìÍj(Çõïkêpùnðö7¾&Äÿ÷)ÆJgn,GÂá÷+¦òxÂÍ«`¯JXÁ%Ò*ÖtÝ]Ú%U~ÂÅ¿=Ü*º'X·íY(Ù0";
		String passwordHash = PasswordHasher.getSaltedHash(password);
		Assert.assertTrue(PasswordHasher.check(password, passwordHash));
		Assert.assertFalse(PasswordHasher.check("", passwordHash));
	}
	
	@Test
	public void testFour() throws Exception
	{
		String password = "$sentences_make_better_passwords....";
		String passwordHash = PasswordHasher.getSaltedHash(password);
		Assert.assertTrue(PasswordHasher.check(password, passwordHash));
		Assert.assertFalse(PasswordHasher.check("$", passwordHash));
	}
}
