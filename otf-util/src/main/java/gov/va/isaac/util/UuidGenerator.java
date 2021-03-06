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

/**
 * UuidGenerator
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.uuid.UuidT5Generator;

/**
 * UuidGenerator
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public class UuidGenerator {

	/**
	 * 
	 */
	private UuidGenerator() {
	}

	public static UUID get(UUID domainSeed, String seed) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		return UuidT5Generator.get(domainSeed, seed);
	}
	public static UUID get(String seed) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		return get(UuidT5Generator.PATH_ID_FROM_FS_DESC, seed);
	}

	public static void main(String[] args) throws NoSuchAlgorithmException, IOException
	{
		UUID domainSeed = UuidT5Generator.PATH_ID_FROM_FS_DESC;
		String seed = null;
		if (args.length == 0)
		{
			System.out.println("Enter text:");
			if (System.console() != null)
			{
				seed = System.console().readLine();
			}
			else
			{
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				seed = br.readLine();
			}
		}
		else if (args.length == 1) {
			seed = args[0];
		} else if (args.length == 1) {
			domainSeed = UUID.fromString(args[0]);
			seed = args[1];
		}
		else
		{
			throw new RuntimeException("Unsupported number of arguments");
		}
		
		UUID uuid = get(domainSeed, seed);
		
		System.out.println("Using the text '" + seed + "'");
		System.out.println("and the domain '" + domainSeed+ "'" + (domainSeed == UuidT5Generator.PATH_ID_FROM_FS_DESC ? " (path ID from FSN description)" : ""));
		System.out.println("the UUID is '" + uuid + "'");
	}
}
