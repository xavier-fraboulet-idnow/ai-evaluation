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

package eu.assina.rssp.csc.controller;

import eu.assina.csc.payload.*;
import eu.assina.rssp.csc.services.CSCCredentialsService;
import eu.assina.rssp.security.CurrentUser;
import eu.assina.rssp.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Credentials endpoints from:
 * From section 11.4/11.5 of the CSC API V_1.0.4.0 spec
 */
@RestController
@RequestMapping(value = "/credentials")
public class CSCCredentialsController
{
	private CSCCredentialsService credentialsService;

	@Autowired
	public CSCCredentialsController(CSCCredentialsService credentialsService) {
		this.credentialsService = credentialsService;
	}

	/**
	 * Returns the list of credentials associated with a user identifier.
	 * A user MAY have one or multiple credentials hosted by a single remote signing service
	 * provider.
	 * If the user is authenticated directly by the RSSP then the userID is implicit and SHALL
	 * NOT be specified.
	 * This method can also be used in case of a community of users, to let the client retrieve
	 * the list of credentials assigned to a specific user of the community. In this case the
	 * userID SHALL be passed explicitly to retrieve the list of credentialIDs for a specific
	 * user.
	 * Managing a community of users that are authenticated by the client using a specific
	 * authentication framework is out of the scope of this specification.
	 *
	 * Example request:
     *
	 *   POST /csc/v1/credentials/list HTTP/1.1
	 *   Host: service.domain.org
	 *   Authorization: Bearer 4/CKN69L8gdSYp5_pwH3XlFQZ3ndFhkXf9P2_TiHRG-bA
	 *   Content-Type: application/json
	 *   {
	 *     "maxResults": 10
	 *   }
	 *
	 * Example response:
	 *   HTTP/1.1 200 OK
	 *   Content-Type: application/json;charset=UTF-8
	 *   {
	 *     "credentialIDs": [ "GX0112348", "HX0224685" ]
	 *   }
	 *
	 * @param listRequest
	 */
	@PostMapping("list")
	@ResponseStatus(HttpStatus.OK)
	public CSCCredentialsListResponse list(@CurrentUser UserPrincipal userPrincipal,
										   @Valid @RequestBody(required = false) CSCCredentialsListRequest listRequest)
	{
		// System.out.println(userPrincipal.getName());
		// System.out.println(userPrincipal.getId());
		// Note required=false: if client POSTS with no body, we create one to add the currentuser
		if (listRequest == null) {
			listRequest = new CSCCredentialsListRequest();
		}
		listRequest.setUserId(userPrincipal.getId());
		CSCCredentialsListResponse credentialsList = credentialsService.listCredentials(listRequest);
		return credentialsList;
	}

	/**
	 * Returns info about specified credential object
	 *
	 * Example request
	 *   POST /csc/v1/credentials/info HTTP/1.1
	 *   Host: service.domain.org
	 *   Authorization: Bearer 4/CKN69L8gdSYp5_pwH3XlFQZ3ndFhkXf9P2_TiHRG-bA
	 *   Content-Type: application/json
	 *   {
	 *     "credentialID": "GX0112348",
	 *     "certificates": "single",
	 *     "certInfo": true,
	 *     "authInfo": true
	 *   }
	 *
	 * Example response:
	 *   HTTP/1.1 200 OK
	 *   Content-Type: application/json;charset=UTF-8
	 *   {
	 *     "key": {
	 *       "status": "enabled",
	 *       "algo": [ "1.2.840.113549.1.1.1", "0.4.0.127.0.7.1.1.4.1.3" ],     "len": 2048   },
	 *       "cert":  {
	 *         "status": "valid",
	 *         "certificates":
	 *         [
	 *           "<Base64-encoded_X.509_end_entity_certificate>",
	 *           "<Base64-encoded_X.509_intermediate_CA_certificate>",
	 *           "<Base64-encoded_X.509_root_CA_certificate>"
	 *         ],
	 *         "issuerDN": "<X.500_issuer_DN_printable_string>",
	 *         "serialNumber": "5AAC41CD8FA22B953640",
	 *         "subjectDN": "<X.500_subject_DN_printable_string>",
	 *         "validFrom": "20180101100000Z",
	 *         "validTo": "20190101095959Z"
	 *      },
	 *      "authMode": "explicit",
	 *      "PIN": {
	 *        "presence": "true",
	 *        "format": "N",
	 *        "label": "PIN",
	 *        "description": "Please enter the signature PIN"
	 *       },
	 *       "OTP": {
	 *         "presence": "true",
	 *         "type": "offline",
	 *         "ID": "MB01-K741200",
	 *         "provider": "totp",
	 *         "format": "N",
	 *         "label": "Mobile OTP",
	 *         "description": "Please enter the 6 digit code you received by SMS"
	 *       },
	 *      "multisign": 5,
	 *      "lang": "en-US"
	 *    }
	 * @param infoRequest
	 */
	@PostMapping("info")
	@ResponseStatus(HttpStatus.OK)
	public CSCCredentialsInfoResponse info( @CurrentUser UserPrincipal userPrincipal,
											@Valid @RequestBody CSCCredentialsInfoRequest infoRequest)
	{
		CSCCredentialsInfoResponse credentialsInfo =
				credentialsService.getCredentialsInfoFromAlias(userPrincipal, infoRequest);
		return credentialsInfo;
	}

	/**
	 * Authorizes a specified credential to sign a request, by returning a SAD to use
	 * Per 11.6 of the CSC Spec.
	 * The SAD has a 5 minute limit.
	 *
	 * Example request:
	 *   POST /csc/v1/credentials/authorize HTTP/1.1
	 *   Host: service.domain.org
	 *   Content-Type: application/json
	 *   Authorization: Bearer 4/CKN69L8gdSYp5_pwH3XlFQZ3ndFhkXf9P2_TiHRG-bA
	 *
	 *   {
	 *     "credentialID": "GX0112348",   "numSignatures": 2,   "hash":
	 *     [
	 *       "sTOgwOm+474gFj0q0x1iSNspKqbcse4IeiqlDg/HWuI=",
	 *       "c1RPZ3dPbSs0NzRnRmowcTB4MWlTTnNwS3FiY3NlNEllaXFsRGcvSFd1ST0="   ],
	 *     "PIN": "12345678",
	 *     "OTP": "738496",
	 *     "clientData": "12345678"
	 *   }
	 *
	 * Example response:
	 *   HTTP/1.1 200 OK
	 *   Content-Type: application/json;charset=UTF-8
	 *   {
	 *     "SAD":
	 *     "_TiHRG-bAH3XlFQZ3ndFhkXf9P24/CKN69L8gdSYp5_pw"
	 *   }
	 */
     @PostMapping("authorize")
	 @ResponseStatus(HttpStatus.OK)
	 public CSCCredentialsAuthorizeResponse authorize(
			@CurrentUser UserPrincipal userPrincipal,
			@Valid @RequestBody CSCCredentialsAuthorizeRequest authorizeRequest) {
		try{
			String nonceAndPresentation_id = authorizeRequest.getClientData();
			String [] nonceAndPresentation_idArray = nonceAndPresentation_id.split("&");
			return credentialsService.authorizeCredential(userPrincipal, authorizeRequest, nonceAndPresentation_idArray[0], nonceAndPresentation_idArray[1]);
		 }
		 catch (Exception e){
			 // System.out.println(e.getMessage());
			 return new CSCCredentialsAuthorizeResponse();
		 }
	 }

	@GetMapping("authorizationLink")
	@ResponseStatus(HttpStatus.OK)
	public RedirectLinkResponse authorizeLink(@CurrentUser UserPrincipal userPrincipal) {
		try{
			return credentialsService.authorizationLinkCredential(userPrincipal);
		}
		catch (Exception e){
			return new RedirectLinkResponse();
		}
	}

}
