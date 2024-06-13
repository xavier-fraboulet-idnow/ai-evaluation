/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.europa.ec.eudi.signer.rssp.api.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.europa.ec.eudi.signer.rssp.api.services.CredentialService;
import eu.europa.ec.eudi.signer.rssp.entities.Credential;
import eu.europa.ec.eudi.signer.rssp.repository.CredentialRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class CredentialServiceTest {

	@Autowired
	CredentialRepository credentialRepository;

	@Autowired
	CredentialService credentialService;

	@Test
	public void testCredentialCreationAndStorage() {
		final Credential credential;
		try {
			credential = credentialService.createCredential("bob", "bob", "bob", "bob", "Alias1", "PT");
		} catch (Exception e) {
			Assert.assertNull(e);
			return;
		}

		final String id = credential.getId();
		final String owner = credential.getOwner();
		final String alias = credential.getAlias();
		Credential loadedCredential = credentialService.getCredentialWithAlias("owner", "alias").get();
		Assert.assertEquals("Expected the credentials loaded to be equal to those saved",
				credential, loadedCredential);

		Pageable pageParams = PageRequest.of(0, 5);
		final Page<Credential> bobsCreds = credentialRepository.findByOwner("bob", pageParams);
		final boolean foundNewCreds = bobsCreds.stream().anyMatch(credential::equals);
		Assert.assertTrue("Expected to find the newly created creds in a search of all creds belonging to bob",
				foundNewCreds);

		credentialService.deleteCredentials("test", id);
		Assert.assertFalse("Expected credential to be deleted",
				credentialService.getCredentialWithAlias(owner, alias).isPresent());
	}
}
