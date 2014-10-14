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

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link PasswordHasher}
 * 
 * A safe, modern way to 1-way hash user passwords.  
 * Adapted and enhanced from http://stackoverflow.com/a/11038230/2163960 
 * 
 * Later, added the ability to encrypt and decrypt arbitrary data - using many of the same 
 * techniques.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class PasswordHasher
{
	private static final Logger log_ = LoggerFactory.getLogger(PasswordHasher.class);
	// The higher the number of iterations the more expensive computing the hash is for us
	// and also for a brute force attack.
	private static final int iterations = 10 * 1024;
	private static final int saltLen = 32;
	private static final int desiredKeyLen = 256;
	private static final String keyFactoryAlgorithm = "PBKDF2WithHmacSHA1";
	private static final String cipherAlgorithm = "PBEWithSHA1AndDESede";
	private static final Random random = new Random();  //Note, it would be more secure to use SecureRandom... but the entropy issues on Linux are a nasty issue
	//and it results in SecureRandom.getInstance(...).generateSeed(...) blocking for long periods of time.  A regular random is certainly good enough
	//for our encryption purposes.
	
	

	/**
	 * Computes a salted PBKDF2 hash of given plaintext password suitable for storing in a database.
	 * Empty passwords are not supported.
	 */
	public static String getSaltedHash(String password) throws Exception
	{
		long startTime = System.currentTimeMillis();
		byte[] salt = new byte[saltLen];
		random.nextBytes(salt);
		// store the salt with the password
		String result = Base64.getEncoder().encodeToString(salt) + "$$$" + hash(password, salt);
		log_.debug("Compute Salted Hash time {} ms", System.currentTimeMillis() - startTime);
		return result;
	}

	/**
	 * Checks whether given plaintext password corresponds to a stored salted hash of the password.
	 */
	public static boolean check(String password, String stored) throws Exception
	{
		String[] saltAndPass = stored.split("\\$\\$\\$");
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
		long startTime = System.currentTimeMillis();
		if (password == null || password.length() == 0)
		{
			throw new IllegalArgumentException("Empty passwords are not supported.");
		}
		SecretKeyFactory f = SecretKeyFactory.getInstance(keyFactoryAlgorithm);
		SecretKey key = f.generateSecret(new PBEKeySpec(password.toCharArray(), salt, iterations, desiredKeyLen));
		String result = Base64.getEncoder().encodeToString(key.getEncoded());
		log_.debug("Password compute time: {} ms", System.currentTimeMillis() - startTime);
		return result;
	}
	
	public static String encrypt(String password, String data) throws Exception
	{
		return encrypt(password, data.getBytes("UTF-8"));
	}
	
	public static String encrypt(String password, byte[] data) throws Exception
	{
		long startTime = System.currentTimeMillis();
		byte[] salt = new byte[saltLen];
		random.nextBytes(salt);
		// store the salt with the password
		String result = Base64.getEncoder().encodeToString(salt) + "$$$" + encrypt(password, salt, data);
		log_.debug("Encrypt Time {} ms", System.currentTimeMillis() - startTime);
		return result;
	}
	
	private static String encrypt(String password, byte[] salt, byte[] data) throws Exception
	{
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(keyFactoryAlgorithm);
		SecretKey key = keyFactory.generateSecret(new PBEKeySpec(password.toCharArray(), salt, iterations, desiredKeyLen));
		Cipher pbeCipher = Cipher.getInstance(cipherAlgorithm);
		pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(salt, iterations));
		
		//attach a sha1 checksum to the end of the data, so we know if we decrypted it properly.
		byte[] dataCheckSum = computeChecksum("SHA1", data).getBytes();
		ByteBuffer temp = ByteBuffer.allocate(data.length + dataCheckSum.length);
		temp.put(data);
		temp.put(dataCheckSum);
		return Base64.getEncoder().encodeToString(pbeCipher.doFinal(temp.array()));
	}
	
	public static String decryptToString(String password, String encryptedData) throws Exception
	{
		return new String(decrypt(password, encryptedData), "UTF-8");
	}
	
	public static byte[] decrypt(String password, String encryptedData) throws Exception
	{
		long startTime = System.currentTimeMillis();
		String[] saltAndPass = encryptedData.split("\\$\\$\\$");
		if (saltAndPass.length != 2)
		{
			throw new Exception("Invalid encrypted data, can't find salt");
		}
		byte[] result = decrypt(password, Base64.getDecoder().decode(saltAndPass[0]), saltAndPass[1]);
		log_.debug("Decrypt Time {} ms", System.currentTimeMillis() - startTime);
		return result;
	}

	private static byte[] decrypt(String password, byte[] salt, String data) throws Exception
	{
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(keyFactoryAlgorithm);
		SecretKey key = keyFactory.generateSecret(new PBEKeySpec(password.toCharArray(), salt, iterations, desiredKeyLen));
		Cipher pbeCipher = Cipher.getInstance(cipherAlgorithm);
		pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(salt, iterations));
		byte[] decrypted;
		try
		{
			decrypted = pbeCipher.doFinal(Base64.getDecoder().decode(data));
		}
		catch (Exception e)
		{
			throw new Exception("Invalid decryption password");
		}
		if (decrypted.length >= 40)
		{
			//The last 40 bytes should be the SHA1 Sum
			String checkSum = new String(Arrays.copyOfRange(decrypted, decrypted.length - 40, decrypted.length));
			byte[] userData = Arrays.copyOf(decrypted, decrypted.length - 40);
			String computed = computeChecksum("SHA1", userData);
			if (!checkSum.equals(computed))
			{
				throw new Exception("Invalid decryption password, or truncated data");
			}
			else
			{
				return userData;
			}
		}
		else
		{
			throw new Exception("Truncated data");
		}
	}
	
	/**
	 * Type can be any supported type, like 'MD5' or 'SHA1'
	 * @throws NoSuchAlgorithmException 
	 */
	public static String computeChecksum(String type, byte[] data) throws NoSuchAlgorithmException
	{
		MessageDigest md = MessageDigest.getInstance(type);
		byte[] digest = md.digest(data);
		return new BigInteger(1, digest).toString(16);
	}
}
