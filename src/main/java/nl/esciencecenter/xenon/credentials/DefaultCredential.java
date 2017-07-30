/**
 * Copyright 2013 Netherlands eScience Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.esciencecenter.xenon.credentials;

import java.util.Objects;

/**
 * This class represents the default credential that may be used by the various adaptors. 
 * 
 * It mainly serves as a placeholder to indicate that the adaptor must revert to whatever default behavior it defines.   
 */
public class DefaultCredential implements Credential {

	private final String username;
	
	public DefaultCredential() {
		username = System.getProperty("user.name");
	}
	
	public DefaultCredential(String username) {
		this.username = username;
	}
	
	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DefaultCredential that = (DefaultCredential) o;
		return Objects.equals(username, that.username);
	}

	@Override
	public int hashCode() {
		return Objects.hash(username);
	}
}
