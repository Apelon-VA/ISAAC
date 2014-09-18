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
 * STAMP
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.workflow;

import java.util.Date;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * STAMP
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public final class STAMP {
	private final static Logger LOG = LoggerFactory.getLogger(STAMP.class);

	// S: Status.IorA| T: 1241251251 | A: UUID of author | M: UUID of MOdule | P: UUID of Path
	private static final String delim = "|";
	
	private Status status;// I or A
	private Date timeStamp;
	
	private UUID author;
	private UUID module;
	private UUID path;

	public STAMP() {}
	
	public STAMP(
			Status status, 
			Date timeStamp, 
			UUID author, 
			UUID module,
			UUID path) {
		super();
		this.status = status;
		this.timeStamp = timeStamp;
		this.author = author;
		this.module = module;
		this.path = path;
	}

	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	public Date getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}
	public UUID getAuthor() {
		return author;
	}
	public void setAuthor(UUID author) {
		this.author = author;
	}
	public UUID getModule() {
		return module;
	}
	public void setModule(UUID module) {
		this.module = module;
	}
	public UUID getPath() {
		return path;
	}
	public void setPath(UUID path) {
		this.path = path;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((author == null) ? 0 : author.hashCode());
		result = prime * result + ((module == null) ? 0 : module.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result
				+ ((timeStamp == null) ? 0 : timeStamp.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof STAMP)) {
			return false;
		}
		STAMP other = (STAMP) obj;
		if (author == null) {
			if (other.author != null) {
				return false;
			}
		} else if (!author.equals(other.author)) {
			return false;
		}
		if (module == null) {
			if (other.module != null) {
				return false;
			}
		} else if (!module.equals(other.module)) {
			return false;
		}
		if (path == null) {
			if (other.path != null) {
				return false;
			}
		} else if (!path.equals(other.path)) {
			return false;
		}
		if (status != other.status) {
			return false;
		}
		if (timeStamp == null) {
			if (other.timeStamp != null) {
				return false;
			}
		} else if (!timeStamp.equals(other.timeStamp)) {
			return false;
		}
		return true;
	}
	
	public static STAMP deserializeFromString(String str) {
		STAMP stamp = new STAMP();
		String[] components = null;
		
		try {
			components = str.split(delim);
		} catch (RuntimeException e) {
			LOG.error("Failed parsing STAMP component array from \"{}\"-delimited string: {}", delim, str);
			throw e;
		}
		
		try {
			stamp.setStatus(Status.valueOf(components[0]));
		} catch (RuntimeException e) {
			LOG.error("Failed parsing STAMP Status from index 0 of array parsed from \"{}\"-delimited string: {}", delim, str);
			throw e;
		}
		
		try {
			stamp.setTimeStamp(new Date(Long.valueOf(components[1])));
		} catch (RuntimeException e) {
			LOG.error("Failed parsing STAMP Date from index 1 of array parsed from \"{}\"-delimited string: {}", delim, str);
			throw e;
		}
		
		try {
			stamp.setAuthor(UUID.fromString(components[2]));
		} catch (RuntimeException e) {
			LOG.error("Failed parsing STAMP author UUID from index 2 of array parsed from \"{}\"-delimited string: {}", delim, str);
			throw e;
		}
		
		try {
			stamp.setModule(UUID.fromString(components[3]));
		} catch (RuntimeException e) {
			LOG.error("Failed parsing STAMP module UUID from index 3 of array parsed from \"{}\"-delimited string: {}", delim, str);
			throw e;
		}

		try {
			stamp.setPath(UUID.fromString(components[4]));
		} catch (RuntimeException e) {
			LOG.error("Failed parsing STAMP path UUID from index 3 of array parsed from \"{}\"-delimited string: {}", delim, str);
			throw e;
		}

		return stamp;
	}

	public String serializeToString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append(status);
		builder.append(delim);
		
		builder.append(timeStamp.getTime());
		builder.append(delim);

		builder.append(author);
		builder.append(delim);
		
		builder.append(module);
		builder.append(delim);
		
		builder.append(path);
		
		return builder.toString();
	}
	
	@Override
	public String toString() {
		return "STAMP [status=" + status + ", timeStamp=" + timeStamp
				+ ", author=" + author + ", module=" + module + ", path="
				+ path + "]";
	}
}
