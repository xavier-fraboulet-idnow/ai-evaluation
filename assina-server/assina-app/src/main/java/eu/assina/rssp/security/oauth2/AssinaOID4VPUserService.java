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

package eu.assina.rssp.security.oauth2;

import java.util.Base64;
import java.util.Optional;

import eu.assina.rssp.api.controller.LoggerUtil;
import eu.assina.rssp.api.payload.AuthResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.upokecenter.cbor.CBOREncodeOptions;
import com.upokecenter.cbor.CBORObject;

import eu.assina.rssp.api.model.RoleName;
import eu.assina.rssp.api.model.UserOID4VP;
import eu.assina.rssp.repository.UserOID4VPRepository;
import eu.assina.rssp.security.OID4VPAuthenticationToken;
import eu.assina.rssp.security.UserAuthenticationTokenProvider;


@Service
public class AssinaOID4VPUserService {

    @Autowired
    private UserOID4VPRepository repository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserAuthenticationTokenProvider tokenProvider;


    public AuthResponse getUserFromVerifierResponseAndGetJWTToken(String messageFromVerifier) throws Exception{
        JSONObject messageFromVerifierJsonObject = new JSONObject(messageFromVerifier);
        String vp_token = messageFromVerifierJsonObject.getString("vp_token");
        Aux a = loadUserOID4VPFromCBOR(vp_token);
        String token = addToDBandCreateJWTToken(a.getUser(), a.getFullName());
        return new AuthResponse(token);
    }

    public class Aux{
        private UserOID4VP user;
        private String givenName;
        private String familyName;

        public Aux(UserOID4VP user, String givenName, String familyName){
            this.user = user;
            this.givenName = givenName;
            this.familyName = familyName;
        }

        public UserOID4VP getUser(){
            return this.user;
        }

        public String getGivenName(){
            return this.givenName;
        }

        public String getFamilyName(){
            return this.familyName;
        }

        public String getFullName(){
            return givenName + " " + familyName;
        }
    }

    public Aux loadUserOID4VPFromCBOR(String vpTokenCBOR) throws Exception {
        String familyName = null; String givenName = null; String birthDate = null;
        String issuingCountry = null; String issuanceAuthority = null;

        JSONObject jsonVPToken = fromCBORToJSONObject(vpTokenCBOR);
        JSONArray jarr = jsonVPToken.getJSONArray("documents").getJSONObject(0).getJSONObject("issuerSigned").getJSONObject("nameSpaces").getJSONArray("eu.europa.ec.eudiw.pid.1");

        for (Object object : jarr) {
            JSONObject j = fromCBORToJSONObject(object.toString());
            if(j.getString("elementIdentifier").equals("family_name")){
                familyName = j.getString("elementValue");
            }
            else if(j.getString("elementIdentifier").equals("given_name")){
                givenName = j.getString("elementValue");
            }
            else if(j.getString("elementIdentifier").equals("birth_date")){
                birthDate = j.getString("elementValue");
            }
            else if(j.getString("elementIdentifier").equals("issuing_authority")){
                issuanceAuthority = j.getString("elementValue");
            }
            else if(j.getString("elementIdentifier").equals("issuing_country")){
                issuingCountry = j.getString("elementValue");
            }
        }
        if(familyName == null || givenName == null || birthDate == null || issuingCountry == null){
            throw new Exception("The vp_token doesn't have all the required parameters to create an User.");
        }
        UserOID4VP user = new UserOID4VP(familyName, givenName, birthDate, issuingCountry, issuanceAuthority, RoleName.ROLE_USER.name());
        return new Aux(user, givenName, familyName);
    }

    private JSONObject fromCBORToJSONObject(String vp_token){
        byte[] bytesCBOR = Base64.getUrlDecoder().decode(vp_token);
        CBORObject cborObject = CBORObject.DecodeFromBytes(bytesCBOR, CBOREncodeOptions.Default);
        return new JSONObject(cborObject.ToJSONString());
    }

    private String addToDBandCreateJWTToken(UserOID4VP userFromVerifierResponse, String fullName) throws Exception{
        Optional<UserOID4VP> userInDatabase = repository.findByHash(userFromVerifierResponse.getHash());
        if(userInDatabase.isEmpty()){
            repository.save(userFromVerifierResponse);
        }



        Authentication authentication = authenticationManager.authenticate(
                new OID4VPAuthenticationToken(userFromVerifierResponse.getHash(), fullName)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        LoggerUtil.logs_user(1, userFromVerifierResponse.getId(), 4);
        return tokenProvider.createToken(authentication);
    }
}
