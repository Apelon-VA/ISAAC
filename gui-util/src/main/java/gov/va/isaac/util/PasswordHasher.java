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

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * {@link PasswordHasher}
 * 
 * A safe, modern way to 1-way hash user passwords.  
 * Adapted from http://stackoverflow.com/a/11038230/2163960
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class PasswordHasher
{
	// The higher the number of iterations the more expensive computing the hash is for us
	// and also for a brute force attack.
	private static final int iterations = 10 * 1024;
	private static final int saltLen = 32;
	private static final int desiredKeyLen = 256;
	
	static
	{
		//generateSeed is extremely slow (and random) on linux, since it blocks for entropy, 
		//and many linux systems may not have enough entropy, and block for many seconds before returning a random value.
		//This tells java to use a slightly less perfect random number generator, if the proper random 
		//generator doesn't have enough entropy.  In the real world, for our purposes, I hightly doubt
		//we need the missing security.
		if (System.getProperty("os.name").equals("Linux"))
		{
			System.setProperty("java.security.egd", "file:/dev/./urandom");
		}
	}

	/**
	 * Computes a salted PBKDF2 hash of given plaintext password suitable for storing in a database.
	 * Empty passwords are not supported.
	 */
	public static String getSaltedHash(String password) throws Exception
	{
		byte[] salt = SecureRandom.getInstance("SHA1PRNG").generateSeed(saltLen);
		// store the salt with the password
		return Base64.getEncoder().encodeToString(salt) + "$" + hash(password, salt);
	}

	/**
	 * Checks whether given plaintext password corresponds to a stored salted hash of the password.
	 */
	public static boolean check(String password, String stored) throws Exception
	{
		String[] saltAndPass = stored.split("\\$");
		if (saltAndPass.length != 2)
		{
			return false;
		}
		if (password == null || password.length() == 0)
		{
			return false;
		}
		String hashOfInput = hash(password, Base64.getDecoder().decode(saltAndPass[0]));
		return hashOfInput.equals(saltAndPass[1]);
	}

	// using PBKDF2 from Sun
	private static String hash(String password, byte[] salt) throws Exception
	{
		if (password == null || password.length() == 0)
		{
			throw new IllegalArgumentException("Empty passwords are not supported.");
		}
		SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		SecretKey key = f.generateSecret(new PBEKeySpec(password.toCharArray(), salt, iterations, desiredKeyLen));
		return Base64.getEncoder().encodeToString(key.getEncoded());
	}
}
