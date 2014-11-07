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
 * TemporaryUniqueIdCache
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.util;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * TemporaryUniqueIdCache
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public class TemporaryUniqueIdCache {
	private final static Map<ObjectWithTemporaryUniqueId, String> temporaryUniqueIdCache = Collections.synchronizedMap(new WeakHashMap<>());

	private final static String idPrefix = "TempUniqueId";
	/**
	 * 
	 */
	private TemporaryUniqueIdCache() {
	}
	
	public static synchronized String getTemporaryUniqueId(ObjectWithTemporaryUniqueId obj) {
		for (Map.Entry<ObjectWithTemporaryUniqueId, String> entry: temporaryUniqueIdCache.entrySet()) {
			if (entry.getKey() == obj) {
				return entry.getValue();
			}
		}

		// Ensure there are no collisions
		String newId = generatePotentialTemporaryUniqueIdString();
		while (newId == null || temporaryUniqueIdCache.values().contains(newId)) {
			newId = generatePotentialTemporaryUniqueIdString();
		}
		
		temporaryUniqueIdCache.put(obj, newId);
		
		return newId;
	}
	
	public static synchronized ObjectWithTemporaryUniqueId getObjectByUniqueId(String id) {
		for (Map.Entry<ObjectWithTemporaryUniqueId, String> entry: temporaryUniqueIdCache.entrySet()) {
			if (entry.getValue() == id || entry.getValue().equals(id)) {
				return entry.getKey();
			}
		}
		
		return null;
	}
	public static synchronized void removeUniqueIdByObject(ObjectWithTemporaryUniqueId obj) {
		temporaryUniqueIdCache.remove(obj);
	}
	public static synchronized void removeUniqueIdById(String id) {
		for (Map.Entry<ObjectWithTemporaryUniqueId, String> entry: temporaryUniqueIdCache.entrySet()) {
			if (entry.getValue() == id || entry.getValue().equals(id)) {
				temporaryUniqueIdCache.remove(entry.getValue());
			}
		}
	}

	private static String generatePotentialTemporaryUniqueIdString() {
		return idPrefix + UUID.randomUUID().toString();
	}
	
//	public static boolean isValidTemporaryUniqueIdString(String str) {
//		if (str.startsWith(idPrefix)) {
//			String uuidPortion = str.replaceFirst(idPrefix, "");
//			UUID uuid = null;
//			try {
//				uuid = UUID.fromString(uuidPortion);
//			} catch (Throwable t) {
//				//
//			}
//			
//			if (uuid != null) {
//				return true;
//			} else {
//				return false;
//			}
//		} else {
//			return false;
//		}
//	}
}
