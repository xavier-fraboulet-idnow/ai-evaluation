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

package eu.assina.rssp.api.services;

import eu.assina.rssp.common.error.AssinaError;
import eu.assina.rssp.common.model.AssinaCredential;
import eu.assina.rssp.api.controller.LoggerUtil;
import eu.assina.rssp.common.error.ApiException;
import eu.assina.rssp.crypto.AssinaCryptoService;
import eu.assina.rssp.repository.CredentialRepository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class CredentialService
{
	private CredentialRepository credentialRepository;
	private AssinaCryptoService cryptoService;
	
	/*@Autowired
	private LoggerUtil loggerUtil;*/

	public CredentialService(CredentialRepository credentialRepository,
							 AssinaCryptoService cryptoService)
	{
		this.credentialRepository = credentialRepository;
		this.cryptoService = cryptoService;
	}

	/**
	 * Creates a new certificate and keypair combinations and stores them in the repository.
	 * The private key is always stored encrypted.
	 *
	 * @param owner
	 * @param subjectDN name of the subject used in the certificate
	 * @return
	 */
	public AssinaCredential createCredential(String owner, String subjectDN, String aliases)
	{
		AssinaCredential credential = cryptoService.createCredential(owner, subjectDN, aliases);
		credential.setCreatedAt(Instant.now());
		credentialRepository.save(credential);
		return credential;
	}

	/**
	 * Gets credentials for the specified owner a page at a time
	 * @param owner user who owns the credentials (also the subject of the cert
	 * @param pageable
	 * @return
	 */
	public Page<AssinaCredential> getCredentialsByOwner(String owner, Pageable pageable)
	{
		Page<AssinaCredential> byOwner = credentialRepository.findByOwner(owner, pageable);
		return byOwner;
	}

	/**
	 * Gets count of credentials by this owner
	 * @param owner user who owns the credentials (also the subject of the cert
	 * @return
	 */
	public long countCredentialsByOwner(String owner)
	{
		long count = credentialRepository.countByOwner(owner);
		return count;
	}

	/**
	 * Gets credentials for all users a page at a time
	 * @param pageable
	 */
	public Page<AssinaCredential> getCredentials(Pageable pageable)
	{
		Page<AssinaCredential> credentials = credentialRepository.findAll(pageable);
		return credentials;
	}

	public Optional<AssinaCredential> getCredentialWithId(String id) {
		return credentialRepository.findById(id);
	}

	public Optional<AssinaCredential> getCredentialWithAlias(String owner, String alias) {
		return credentialRepository.findByOwnerAndAlias(owner, alias);
	}

	/**
	 * Deletes the credentials with the specified Id
	 *
	 * @param id id of the credentials to be deleted
	 */
	public void deleteCredentials(String credentialID, String userID) {
		try {
			credentialRepository.deleteByOwnerAndAlias(userID, credentialID);
		}
		catch (EmptyResultDataAccessException ex) {
			if(userID != "test") {
				LoggerUtil.logs_user(0, userID, 2);
			}
			throw new ApiException(AssinaError.CredentialNotFound, "Attempted to delete credentials that do not exist", credentialID);
		}
	}
}
