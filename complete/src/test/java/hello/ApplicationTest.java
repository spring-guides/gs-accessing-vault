package hello;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.vault.VaultContainer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.vault.core.VaultKeyValueOperations;
import org.springframework.vault.core.VaultKeyValueOperationsSupport.KeyValueBackend;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.VaultResponse;
import org.springframework.vault.support.VaultResponseSupport;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
public class ApplicationTest {

	static final String VAULT_TOKEN = "00000000-0000-0000-0000-000000000000";

	@Container
	static VaultContainer<?> vaultContainer = new VaultContainer<>(DockerImageName.parse("hashicorp/vault:latest"))
			.withVaultToken(VAULT_TOKEN)
			.withInitCommand("kv put secret/github github.oauth2.key=foobar");

	@Autowired
	private VaultOperations vaultOperations;

	@DynamicPropertySource
	static void vaultProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.cloud.vault.uri", vaultContainer::getHttpHostAddress);
		registry.add("spring.cloud.vault.token", () -> VAULT_TOKEN);
	}

	@Test
	void readShouldRetrieveVaultData() {
		VaultResponse response = this.vaultOperations.opsForKeyValue("secret", KeyValueBackend.KV_2).get("github");
		assertThat(response).isNotNull();
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
		assertThat(mappedCredentials).isNotNull();
		assertThat(mappedCredentials.getData()).satisfies(data -> {
			assertThat(data.getUsername()).isEqualTo("john");
			assertThat(data.getPassword()).isEqualTo("doe");
		});
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
