/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hello;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.core.VaultKeyValueOperations;
import org.springframework.vault.core.VaultKeyValueOperationsSupport.KeyValueBackend;
import org.springframework.vault.support.VaultResponse;
import org.springframework.vault.support.VaultResponseSupport;

@SpringBootTest
public class VaultOperationsTest {

	@Autowired
	private VaultOperations vaultOperations;

	@Test
	void readShouldRetrieveVaultData() {

		VaultResponse response = this.vaultOperations.opsForKeyValue("secret", KeyValueBackend.KV_2).get("github");

		assertThat(response.getData()).containsEntry("github.oauth2.key", "foobar");
	}

	@Test
	void writeShouldStoreVaultData() {

		Map<String, String> credentials = new HashMap<>();
		credentials.put("username", "john");
		credentials.put("password", "doe");

		VaultKeyValueOperations kv = this.vaultOperations.opsForKeyValue("secret", KeyValueBackend.KV_2);
		kv.put("secret/database", credentials);

		VaultResponseSupport<Credentials> mappedCredentials = kv.get("secret/database", Credentials.class);

		assertThat(mappedCredentials.getData().getUsername()).isEqualTo("john");
		assertThat(mappedCredentials.getData().getPassword()).isEqualTo("doe");
	}

	static class Credentials {

		private String username;
		private String password;

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
	}
}
