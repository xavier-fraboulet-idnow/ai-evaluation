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
package eu.assina.rssp.api.service;

import eu.assina.rssp.common.model.AssinaCredential;
import eu.assina.rssp.repository.CredentialRepository;
import eu.assina.rssp.api.services.CredentialService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class CredentialServiceTest {

	@Autowired
	CredentialRepository credentialRepository;

	@Autowired
	CredentialService credentialService;

	@Test
	public void testCredentialCreationAndStorage() {
		// TODO move this test elsehwer
		// TODO the first param must be an ID - make a constant ID for bob
		final AssinaCredential credential = credentialService.createCredential("bob", "bob", "Alias1");
		final String id = credential.getId();
		AssinaCredential loadedCredential = credentialService.getCredentialWithId(id).get();
		Assert.assertEquals("Expected the credentials loaded to be equal to those saved",
				credential, loadedCredential);

		Pageable pageParams = PageRequest.of(0, 5);
		final Page<AssinaCredential> bobsCreds = credentialRepository.findByOwner("bob", pageParams);
		final boolean foundNewCreds = bobsCreds.stream().anyMatch(credential::equals);
		Assert.assertTrue("Expected to find the newly created creds in a search of all creds belonging to bob",
				foundNewCreds);

		credentialService.deleteCredentials(id, "test");
		Assert.assertFalse("Expected credential to be deleted",
				credentialService.getCredentialWithId(id).isPresent());
	}
}
