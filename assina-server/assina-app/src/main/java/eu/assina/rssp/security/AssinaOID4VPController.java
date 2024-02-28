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

package eu.assina.rssp.security;

import eu.assina.csc.payload.RedirectLinkResponse;
import eu.assina.rssp.api.controller.LoggerUtil;
import eu.assina.rssp.api.payload.AuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import eu.assina.rssp.security.oauth2.AssinaOID4VPUserService;
import eu.assina.rssp.security.oauth2.OID4VPVerifierClient;

@RestController
@RequestMapping("/oauth2")
public class AssinaOID4VPController {
    
    private static final Logger log = LoggerFactory.getLogger(AssinaOID4VPController.class);

    @Autowired
    private OID4VPVerifierClient verifierClient;

    @Autowired
    AssinaOID4VPUserService userService;

    @GetMapping("link")
    public ResponseEntity<?> initPresentationTransaction(){
        try{
            ResponseEntity<RedirectLinkResponse> responseEntity;
            RedirectLinkResponse response = this.verifierClient.initPresentationTransaction();
            responseEntity = ResponseEntity.ok(response);
            return responseEntity;
        }
        catch(Exception e){
            log.error("Error while creating link for the EUDI Wallet",e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("token")
    public ResponseEntity<?> waitResponse(@RequestParam("nonce") String nonce,
                                          @RequestParam("presentationId") String presentationId){
        try{
            // System.out.println(nonce);
            // System.out.println(presentationId);

            String messageFromVerifier = verifierClient.getVPTokenFromVerifier(nonce, presentationId);

            if(messageFromVerifier.isEmpty()){
                log.warn("No vp_token was receive in time.");
                return ResponseEntity.badRequest().build();
            }
            else{
                AuthResponse JWTToken = this.userService.getUserFromVerifierResponseAndGetJWTToken(messageFromVerifier);
                return ResponseEntity.ok(JWTToken);
            }
        }
        catch(Exception e){
            log.error("Error when trying to obtain the vp_token", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
