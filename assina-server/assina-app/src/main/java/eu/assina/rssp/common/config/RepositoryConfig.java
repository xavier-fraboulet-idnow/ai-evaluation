/*
 Copyright 2024 European Commission

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      https://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package eu.assina.rssp.common.config;

import eu.assina.rssp.api.services.CredentialService;
import eu.assina.rssp.api.model.AuthProvider;
import eu.assina.rssp.api.model.RoleName;
import eu.assina.rssp.api.model.User;
import eu.assina.rssp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;

@Configuration
class RepositoryConfig {
	private static final Logger log = LoggerFactory.getLogger(RepositoryConfig.class);

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Bean
	CommandLineRunner initRepository(UserRepository userRepo, DemoProperties demoProperties,
									 CredentialService credentialService) {
		return args -> {
			if (userRepo.count() == 0) {
				User admin = new User("admin", "admin", "admin@assina.eu", AuthProvider.local);
				admin.setRole(RoleName.ROLE_ADMIN.name());
				// TODO admin password should be a setup feature - document this
				admin.setPassword(passwordEncoder.encode("admin"));
				admin.setEncodedPIN(passwordEncoder.encode("1234"));
				userRepo.save(admin);
				log.info("Added admin user {}", admin.getName());
				User bob = new User("bob", "bob", "bob@assina.eu", AuthProvider.local);
				bob.setRole(RoleName.ROLE_USER.name());
				bob.setPassword(passwordEncoder.encode("bob"));
				bob.setEncodedPIN(passwordEncoder.encode("1234"));
				userRepo.save(bob);
				log.info("Added regular user {}", bob.getName());
			}

			final List<DemoProperties.DemoUser> demoUsers = demoProperties.getUsers();
			if (demoUsers != null) {
				for (DemoProperties.DemoUser demoUser : demoUsers) {
					// allow for additional users without wiping old ones from last startup
					if (userRepo.existsByUsername(demoUser.getUsername())) {
						log.debug("Found demo user {} in database at startup", demoUser.getName());
					} else {
						User user = new User(demoUser.getUsername(), demoUser.getName(), demoUser.getEmail(), AuthProvider.local);
						user.setEmail(demoUser.getEmail());
						String demoPassword = demoUser.getPlainPassword();
						user.setPassword(passwordEncoder.encode(demoPassword));
						user.setRole(demoUser.getRole());
						String demoPIN = demoUser.getPlainPIN();
						user.setEncodedPIN(passwordEncoder.encode(demoPIN));
						user.setCreatedAt(Instant.now());
						user.setUpdatedAt(Instant.now());
						userRepo.save(user);
						log.info("Added demo user {} with role: {}", demoUser.getName(), demoUser.getRole());
						for (int i = 0; i < demoUser.getNumCredentials(); i++) {
						    // add credentials for the demo user
							String aliases = Integer.toString(i+1);
							credentialService.createCredential(user.getId(), user.getUsername(), aliases);
							log.info("Created demo credential for user {} with password {} and PIN {}",
									user.getUsername(), demoPassword, demoPIN);
						}
					}
				}
			}
		};
	}
}
