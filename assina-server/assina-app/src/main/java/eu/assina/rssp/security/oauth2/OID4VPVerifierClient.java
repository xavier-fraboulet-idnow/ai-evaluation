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

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import eu.assina.csc.payload.RedirectLinkResponse;
import eu.assina.rssp.common.config.VerifierProperties;
import eu.assina.rssp.util.WebUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import eu.assina.rssp.security.AssinaOID4VPController;

/**
 * Component responsible to make requests to an OpenID4VP Verifier
 * And create the links necessary to redirect the user to the Verifier
 */
@Component
public class OID4VPVerifierClient{

    private VerifierProperties verifierProperties;

    private static final Logger log = LoggerFactory.getLogger(AssinaOID4VPController.class);

    public OID4VPVerifierClient(VerifierProperties verifierProperties){
        this.verifierProperties = verifierProperties;
    }


    /**
     * This function makes a post request to https://eudi.netcompany-intrasoft.com/ui/presentations/ to init the Presentation Transaction.
     * @return a deep link to the EUDIW application
     * @throws Exception
     */
    public RedirectLinkResponse initPresentationTransaction() throws Exception {
        RedirectLinkResponse response = new RedirectLinkResponse();

        // Set Headers
        Map<String, String> headers = getHeaders();

        // Set JSON Body
        String nonce = generateNonce();
        response.setNonce(nonce);

        // String presentationDefinition = "{'id':'32f54163-7166-48f1-93d8-ff217bdb0653'," +
        //                                 "'input_descriptors':[{'id':'eudi_pid','name':'EUDI PID','purpose':'We need to verify your identity','constraints':{'fields':[{'path':['$.mdoc.doctype'],'filter':{'type':'string','const':'eu.europa.ec.eudiw.pid.1'}},{'path':['$.mdoc.namespace'],'filter':{'type':'string','const':'eu.europa.ec.eudiw.pid.1'}},{'path':['$.mdoc.family_name'],'intent_to_retain':false},{'path':['$.mdoc.given_name'],'intent_to_retain':false},{'path':['$.mdoc.birth_date'],'intent_to_retain':false},{'path':['$.mdoc.age_over_18'],'intent_to_retain':false},{'path':['$.mdoc.age_in_years'],'intent_to_retain':false},{'path':['$.mdoc.age_birth_year'],'intent_to_retain':false},{'path':['$.mdoc.family_name_birth'],'intent_to_retain':false},{'path':['$.mdoc.given_name_birth'],'intent_to_retain':false},{'path':['$.mdoc.birth_place'],'intent_to_retain':false},{'path':['$.mdoc.birth_country'],'intent_to_retain':false},{'path':['$.mdoc.birth_state'],'intent_to_retain':false},{'path':['$.mdoc.birth_city'],'intent_to_retain':false},{'path':['$.mdoc.resident_address'],'intent_to_retain':false},{'path':['$.mdoc.resident_country'],'intent_to_retain':false},{'path':['$.mdoc.resident_state'],'intent_to_retain':false},{'path':['$.mdoc.resident_city'],'intent_to_retain':false},{'path':['$.mdoc.resident_postal_code'],'intent_to_retain':false},{'path':['$.mdoc.resident_street'],'intent_to_retain':false},{'path':['$.mdoc.resident_house_number'],'intent_to_retain':false},{'path':['$.mdoc.gender'],'intent_to_retain':false},{'path':['$.mdoc.nationality'],'intent_to_retain':false},{'path':['$.mdoc.issuance_date'],'intent_to_retain':false},{'path':['$.mdoc.expiry_date'],'intent_to_retain':false},{'path':['$.mdoc.issuing_authority'],'intent_to_retain':false},{'path':['$.mdoc.document_number'],'intent_to_retain':false},{'path':['$.mdoc.administrative_number'],'intent_to_retain':false},{'path':['$.mdoc.issuing_country'],'intent_to_retain':false},{'path':['$.mdoc.issuing_jurisdiction'],'intent_to_retain':false}]}}]}";
        String presentationDefinition = "{'id':'32f54163-7166-48f1-93d8-ff217bdb0653'," +
                "'input_descriptors':[{'id':'eudi_pid'," +
                                      "'name':'EUDI PID'," +
                                      "'purpose':'We need to verify your identity'," +
                                      "'constraints':{'fields':[" +
                                                                "{'path':['$.mdoc.doctype'],'filter':{'type':'string','const':'eu.europa.ec.eudiw.pid.1'}}," +
                                                                "{'path':['$.mdoc.namespace'],'filter':{'type':'string','const':'eu.europa.ec.eudiw.pid.1'}}," +
                                                                "{'path':['$.mdoc.family_name'],'intent_to_retain':false}," +
                                                                "{'path':['$.mdoc.given_name'],'intent_to_retain':false}," +
                                                                "{'path':['$.mdoc.birth_date'],'intent_to_retain':false}," +
                                                                "{'path':['$.mdoc.age_over_18'],'intent_to_retain':false}," +
                                                                "{'path':['$.mdoc.issuing_authority'],'intent_to_retain':false},"+
                                                                "{'path':['$.mdoc.issuing_country'],'intent_to_retain':false}"+
                                      "]}}]}";

        JSONObject presentationDefinitionJsonObject = new JSONObject(presentationDefinition);

        JSONObject jsonBodyToInitPresentation = new JSONObject();
        jsonBodyToInitPresentation.put("type", "vp_token");
        jsonBodyToInitPresentation.put("nonce", nonce);
        jsonBodyToInitPresentation.put("presentation_definition", presentationDefinitionJsonObject);

        // Send HTTP Post Request & Receives the Response
        JSONObject responseFromVerifierAfterInitPresentation = httpRequestToInitPresentation(jsonBodyToInitPresentation.toString(), headers);
        createDeepLink(responseFromVerifierAfterInitPresentation, response);
        // System.out.println(response.getLink());
        return response;
    }

    public String getVPTokenFromVerifier(String nonce, String presentation_id) throws Exception{
        Map<String, String> headers = getHeaders();

        String url = uriToRequestWalletPID(presentation_id, nonce);
        // System.out.println(url);

        String message = "";
        int responseCode = 400;
        long startTime = System.currentTimeMillis(); //fetch starting time
        while(responseCode != 200 && (System.currentTimeMillis()-startTime)<300000) {
            HttpResponse response = WebUtils.httpGetRequests(url, headers);

            if(response.getStatusLine().getStatusCode() == 404) {
                throw new Exception("Failed connection to Verifier: impossible to get vp_token");
            }
            else if(response.getStatusLine().getStatusCode() == 200){
                responseCode = 200;
                HttpEntity entity = response.getEntity();
                if(entity == null){
                    throw new Exception("Response from EUDIW is empty.");
                }

                InputStream instream = entity.getContent();
                message = WebUtils.convertStreamToString(instream);
            }
            else{
                TimeUnit.SECONDS.sleep(1);
            }
        }
        return message;
    }

    private Map<String, String> getHeaders(){
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Cookie", "SERVERUSED=server1; TS010b9524=01eb1053a0beaccfef181704d8d63c0d7987f347a2ec9fb8e7523c06298d62ad3dd30e17c3aa0a3535482f38f21aad94d3c37023fd39b9b7250ee76b594cb67c5aa2f212de");
        return headers;
    }

    private String generateNonce() throws Exception{
        SecureRandom prng = new SecureRandom();
        String randomNum = String.valueOf(prng.nextInt());
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] result = sha.digest(randomNum.getBytes());
        return Base64.getUrlEncoder().encodeToString(result);
    }

    private JSONObject httpRequestToInitPresentation(String jsonObjectString, Map<String, String> headers) throws Exception{
        HttpResponse response = WebUtils.httpPostRequest(verifierProperties.getUrl(), headers, jsonObjectString);

        if(response.getStatusLine().getStatusCode() != 200){
            String error = WebUtils.convertStreamToString(response.getEntity().getContent());
            throw new Exception("HTTP Post Request not successful: "+ error);
        }

        HttpEntity entity = response.getEntity();
        if(entity == null){
            throw new Exception("Response after initialize presentation is empty.");
        }

        String result = WebUtils.convertStreamToString(entity.getContent());
        return new JSONObject(result);
    }

    private void createDeepLink(JSONObject responseFromVerifier, RedirectLinkResponse response) throws Exception{
        String request_uri = responseFromVerifier.getString("request_uri");
        String client_id = responseFromVerifier.getString("client_id");
        String presentation_id = responseFromVerifier.getString("presentation_id");
        response.setPresentationId(presentation_id);

        String encoded_request_uri = URLEncoder.encode(request_uri, StandardCharsets.UTF_8);

        // Generates a deepLink to the EUDIW App
        String deepLink = redirectUriDeepLink(encoded_request_uri, client_id);
        response.setLink(deepLink);
    }

    private String redirectUriDeepLink(String request_uri, String client_id){
        return "eudi-openid4vp://" +
                verifierProperties.getAddress() +
                "?client_id=" +
                client_id +
                "&request_uri=" +
                request_uri;
    }

    private String uriToRequestWalletPID(String presentation_id, String nonce){
        return verifierProperties.getUrl() +
                "/" + presentation_id +
                "?nonce=" +
                nonce;
    }
}
