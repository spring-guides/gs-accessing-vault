package hello;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.vault.core.VaultKeyValueOperationsSupport.KeyValueBackend;
import org.springframework.vault.core.VaultSysOperations;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.core.VaultTransitOperations;
import org.springframework.vault.support.VaultMount;
import org.springframework.vault.support.VaultResponse;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	ApplicationRunner runner(VaultTemplate vaultTemplate) {
		return args -> {

			// You usually would not print a secret to stdout
			VaultResponse response = vaultTemplate
					.opsForKeyValue("secret", KeyValueBackend.KV_2).get("github");
			if (response == null) {
				throw new IllegalStateException("No data found in secret/github");
			}
			System.out.println("Value of github.oauth2.key");
			System.out.println("-------------------------------");
			System.out.println(response.getData().get("github.oauth2.key"));
			System.out.println("-------------------------------");
			System.out.println();

			// Let's encrypt some data using the Transit backend.
			VaultTransitOperations transitOperations = vaultTemplate.opsForTransit();

			// We need to setup transit first (assuming you didn't set up it yet).
			VaultSysOperations sysOperations = vaultTemplate.opsForSys();

			if (!sysOperations.getMounts().containsKey("transit/")) {

				sysOperations.mount("transit", VaultMount.create("transit"));

				transitOperations.createKey("foo-key");
			}

			// Encrypt a plain-text value
			String ciphertext = transitOperations.encrypt("foo-key", "Secure message");

			System.out.println("Encrypted value");
			System.out.println("-------------------------------");
			System.out.println(ciphertext);
			System.out.println("-------------------------------");
			System.out.println();

			// Decrypt

			String plaintext = transitOperations.decrypt("foo-key", ciphertext);

			System.out.println("Decrypted value");
			System.out.println("-------------------------------");
			System.out.println(plaintext);
			System.out.println("-------------------------------");
			System.out.println();
		};
	}

}
