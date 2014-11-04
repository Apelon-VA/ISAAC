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
package gov.va.isaac.sync.git;

import java.util.Arrays;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

/**
 * {@link SSHFriendlyUsernamePasswordCredsProvider}
 * 
 * This implementation 99% copied from {@link UsernamePasswordCredentialsProvider} - but added support 
 * for some YES/No type questions
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class SSHFriendlyUsernamePasswordCredsProvider extends CredentialsProvider {
	private String username;

	private char[] password;

	/**
	 * Initialize the provider with a single username and password.
	 *
	 * @param username
	 * @param password
	 */
	public SSHFriendlyUsernamePasswordCredsProvider(String username, String password) {
		this(username, password.toCharArray());
	}

	/**
	 * Initialize the provider with a single username and password.
	 *
	 * @param username
	 * @param password
	 */
	public SSHFriendlyUsernamePasswordCredsProvider(String username, char[] password) {
		this.username = username;
		this.password = password;
	}

	@Override
	public boolean isInteractive() {
		return false;
	}

	@Override
	public boolean supports(CredentialItem... items) {
		for (CredentialItem i : items) {
			if (i instanceof CredentialItem.Username)
				continue;

			else if (i instanceof CredentialItem.Password)
				continue;
			
			else if (i instanceof CredentialItem.YesNoType)
				continue;

			else
				return false;
		}
		return true;
	}

	@Override
	public boolean get(URIish uri, CredentialItem... items)
			throws UnsupportedCredentialItem {
		for (CredentialItem i : items) {
			if (i instanceof CredentialItem.Username) {
				((CredentialItem.Username) i).setValue(username);
				continue;
			}
			if (i instanceof CredentialItem.Password) {
				((CredentialItem.Password) i).setValue(password);
				continue;
			}
			if (i instanceof CredentialItem.StringType) {
				if (i.getPromptText().equals("Password: ")) { //$NON-NLS-1$
					((CredentialItem.StringType) i).setValue(new String(
							password));
					continue;
				}
			}
			if (i instanceof CredentialItem.YesNoType) {
				//TODO - if we have a GUI - prompt the user
				if (i.getPromptText().startsWith("The authenticity of host '")) { //$NON-NLS-1$
					((CredentialItem.YesNoType) i).setValue(true);
					continue;
				}
			}
			throw new UnsupportedCredentialItem(uri, i.getClass().getName()
					+ ":" + i.getPromptText()); //$NON-NLS-1$
		}
		return true;
	}

	/** Destroy the saved username and password.. */
	public void clear() {
		username = null;

		if (password != null) {
			Arrays.fill(password, (char) 0);
			password = null;
		}
	}
}
