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

package eu.assina.rssp.api.controller;

import eu.assina.rssp.api.model.UserBase;
import eu.assina.rssp.api.model.UserOID4VP;
import eu.assina.rssp.common.model.AssinaCredential;
import eu.assina.rssp.api.model.AuthProvider;
import eu.assina.rssp.api.model.User;
import eu.assina.rssp.api.payload.CredentialSummary;
import eu.assina.rssp.api.services.CredentialService;
import eu.assina.rssp.api.services.UserService;
import eu.assina.rssp.common.error.ApiException;
import eu.assina.rssp.common.error.AssinaError;
import eu.assina.rssp.security.CurrentUser;
import eu.assina.rssp.security.UserPrincipal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Optional;

import javax.sound.midi.SysexMessage;

@RestController
@RequestMapping(value = "/credentials")
public class AssinaCredentialController
{
	
	private static final Logger logger = LogManager.getLogger(AssinaCredentialController.class);
	private CredentialService credentialService;
	private UserService userService;

	/*@Autowired
	private LoggerUtil loggerUtil;*/
   

	public AssinaCredentialController(@Autowired final CredentialService credentialService,
									  @Autowired final UserService userService)
	{
		this.credentialService = credentialService;
		this.userService = userService;
	}

	@GetMapping
	public Page<CredentialSummary> getCredentialsPaginated(Pageable pageable)
	{
		return credentialService.getCredentials(pageable).map(this::summarize);
	}

	@GetMapping("/{id}")
	public CredentialSummary getCredentialsByOwner(@PathVariable(value = "id") String id)
	{
		return credentialService.getCredentialWithId(id).map(this::summarize).orElseThrow(
				() -> new ApiException(AssinaError.CredentialNotFound, "Failed to find credential with id {}", id));
	}

	@PostMapping()
	@ResponseStatus(HttpStatus.CREATED)

	public ResponseEntity<AssinaCredential> createCredential(@CurrentUser UserPrincipal userPrincipal, 
															 @RequestParam("alias") String alias) {
	
		logger.warn(alias);
		String id = userPrincipal.getId();

		UserBase user;
		Optional<User> user1 = userService.getUserById(id);
		if(user1.isPresent()){
			user = user1.get();
		}
		else{
			Optional<UserOID4VP> user2 = userService.getUserOID4VPById(id);
			if(user2.isPresent()){
				user = user2.get();
			}
			else{
				throw new ApiException(AssinaError.UserNotFound, "Failed to find user {}", id);
			}
		}

		// use the id as the credential owner, and the username or email as the DN
		final AssinaCredential credential =
				credentialService.createCredential(user.getId(), userPrincipal.getName(), alias);

		URI location = ServletUriComponentsBuilder
				.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(credential.getId())
				.toUri();
		final ResponseEntity.BodyBuilder responseEntityBuilder = ResponseEntity.created(location);
		
		LoggerUtil.logs_user(1, id, 1);

		return responseEntityBuilder.body(credential);
	}

	@DeleteMapping("/{credentialId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteCredential(@PathVariable(value = "credentialId") String credentialId, @CurrentUser UserPrincipal userPrincipal)
	{
		// System.out.println("Credential to delete: "+ credentialId);
		String userID = userPrincipal.getId();
		credentialService.deleteCredentials(credentialId, userID);
		LoggerUtil.logs_user(1, userID, 2);
	}

	/**
	 * Functional method to convert a credential into a summary for returning
	 **/
	private CredentialSummary summarize(AssinaCredential credential) {
		return summarizeCredential(credential, userService);
	}

	public static CredentialSummary summarizeCredential(AssinaCredential credential, UserService userService) {
	    String ownerId = credential.getOwner();
		User dummy = new User(ownerId, "unknown", "unknown", AuthProvider.local);
		UserBase user;
		if (userService != null) {
			user = userService.getUserById(ownerId).orElse(dummy);
		}
		else {
			user = dummy; // used by tests that don't have a user service
		}
		return new CredentialSummary(credential.getId(), user.getUsername(), user.getName(), credential.getCreatedAt(),
				credential.getDescription());
	}
}
