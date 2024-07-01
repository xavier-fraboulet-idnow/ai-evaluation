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

package eu.europa.ec.eudi.signer.rssp.security.openid4vp;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import eu.europa.ec.eudi.signer.common.FailedConnectionVerifier;
import eu.europa.ec.eudi.signer.common.TimeoutException;
import eu.europa.ec.eudi.signer.csc.payload.RedirectLinkResponse;
import eu.europa.ec.eudi.signer.rssp.common.config.VerifierProperties;
import eu.europa.ec.eudi.signer.rssp.common.error.ApiException;
import eu.europa.ec.eudi.signer.rssp.common.error.SignerError;
import eu.europa.ec.eudi.signer.rssp.util.WebUtils;

/**
 * Component responsible to make requests to an OpenID4VP Verifier
 * And create the links necessary to redirect the user to the Verifier
 */
@Component
public class VerifierClient {

    public static String Authentication = "Authentication";
    public static String Authorization = "Authorization";
    public static String PresentationDefinitionId = "32f54163-7166-48f1-93d8-ff217bdb0653";
    public static String PresentationDefinitionInputDescriptorsId = "eu.europa.ec.eudi.pid.1";

    private static final Logger log = LoggerFactory.getLogger(VerifierClient.class);

    private final VerifierProperties verifierProperties;
    private final VerifierCreatedVariables verifierVariables;

    public VerifierClient(VerifierProperties verifierProperties) {
        this.verifierProperties = verifierProperties;
        this.verifierVariables = new VerifierCreatedVariables();
    }

    /**
     * Function that allows to make a Presentation Request, following the OpenID for
     * Verifiable Presentations - draft 20, to the verifier
     * 
     * This function already writes the logs for the ApiException. The message in
     * that exceptions can also be used to display info to the user.
     * 
     * @param user an identifier of the user that made the request (ex: a cookie or
     *             an id)
     * @param type the type of the operation that requires the use of OID4VP (ex:
     *             authentication or authorization)
     * @return the deep link that redirects the user to the EUDI Wallet
     * @throws ApiException
     * @throws Exception
     */
    public RedirectLinkResponse initPresentationTransaction(String user, String type) throws ApiException, Exception {
        if (operationTypeIsInvalid(type)) {
            String logMessage = SignerError.UnexpectedOperationType.getCode()
                    + "(initPresentationTransaction in VerifierClient.class): "
                    + SignerError.UnexpectedOperationType.getDescription();
            log.error(logMessage);
            throw new ApiException(SignerError.UnexpectedOperationType,
                    SignerError.UnexpectedOperationType.getFormattedMessage());
        }

        // Set Headers
        Map<String, String> headers = getHeaders();
        String nonce = generateNonce();

        String presentationDefinition = "{" +
                "'id': '32f54163-7166-48f1-93d8-ff217bdb0653'," +
                "'input_descriptors': [{" +
                "'id': '"+PresentationDefinitionInputDescriptorsId+"'," +
                "'name': 'EUDI PID'," +
                "'purpose': 'We need to verify your identity'," +
                "'format': {'mso_mdoc': {" +
                "'alg': ['ES256', 'ES384', 'ES512', 'EdDSA'] } }," +
                "'constraints': {" +
                "'fields': [" +
                "{'path': [\"$['"+PresentationDefinitionInputDescriptorsId+"']['family_name']\"], 'intent_to_retain': true}," +
                "{\"path\": [\"$['"+PresentationDefinitionInputDescriptorsId+"']['given_name']\"],  \"intent_to_retain\": true}," +
                "{\"path\": [\"$['"+PresentationDefinitionInputDescriptorsId+"']['birth_date']\"],  \"intent_to_retain\": true}," +
                "{\"path\": [\"$['"+PresentationDefinitionInputDescriptorsId+"']['age_over_18']\"], \"intent_to_retain\": false}," +
                "{\"path\": [\"$['"+PresentationDefinitionInputDescriptorsId+"']['issuing_authority']\"], \"intent_to_retain\": true}," +
                "{\"path\": [\"$['"+PresentationDefinitionInputDescriptorsId+"']['issuing_country']\"], \"intent_to_retain\": true}" +
                "]}}]}";

        JSONObject presentationDefinitionJsonObject = new JSONObject(presentationDefinition);

        // Set JSON Body
        JSONObject jsonBodyToInitPresentation = new JSONObject();
        jsonBodyToInitPresentation.put("type", "vp_token");
        jsonBodyToInitPresentation.put("nonce", nonce);
        jsonBodyToInitPresentation.put("presentation_definition", presentationDefinitionJsonObject);

        // Send HTTP Post Request & Receives the Response
        JSONObject responseFromVerifierAfterInitPresentation;
        try {
            responseFromVerifierAfterInitPresentation = httpRequestToInitPresentation(
                    jsonBodyToInitPresentation.toString(), headers);
        } catch (Exception e) {
            String logMessage = SignerError.FailedConnectionToVerifier.getCode()
                    + " (initPresentationTransaction in VerifierClient.class) "
                    + SignerError.FailedConnectionToVerifier.getDescription() + ": " + e.getMessage();
            log.error(logMessage);
            throw new ApiException(SignerError.FailedConnectionToVerifier,
                    SignerError.FailedConnectionToVerifier.getFormattedMessage());
        }

        RedirectLinkResponse response = new RedirectLinkResponse();
        // throws an api exception that is already "handled"
        String presentation_id = getPresentationIdAndCreateDeepLink(responseFromVerifierAfterInitPresentation,
                response);

        verifierVariables.addUsersVerifierCreatedVariable(user, type, nonce, presentation_id);

        log.info("User " + user + " executed successfully the operation " + type + ". Nonce: " + nonce
                + " & Presentation_id: " + presentation_id);
        log.info("Current Verifier Variables State: " + verifierVariables);
        return response;
    }

    /**
     * Function that allows to get the VP Token from the Verifier.
     * This function realizes an active waiting
     * 
     * @param user an identifier of the user that made the request (ex: a cookie or
     *             an id)
     * @param type the type of the operation that requires the use of OID4VP (ex:
     *             authentication or authorization)
     * @return the VP Token received from the Verifier
     * @throws TimeoutException
     * @throws FailedConnectionVerifier
     */
    public String getVPTokenFromVerifier(String user, String type)
            throws TimeoutException, FailedConnectionVerifier, Exception {
        if (operationTypeIsInvalid(type)) {
            String logMessage = SignerError.UnexpectedOperationType.getCode()
                    + "(getVPTokenFromVerifier in VerifierClient.class): "
                    + SignerError.UnexpectedOperationType.getDescription();
            log.error(logMessage);
            throw new ApiException(SignerError.UnexpectedOperationType,
                    SignerError.UnexpectedOperationType.getFormattedMessage());
        }

        VerifierCreatedVariable variables = verifierVariables.getUsersVerifierCreatedVariable(user, type);
        if (variables == null) {
            String logMessage = SignerError.UnexpectedError.getCode()
                    + "(getVPTokenFromVerifier in VerifierClient.class) "
                    + SignerError.UnexpectedError.getDescription()
                    + " Variables required to receive answer from the Verifier were not found.";
            log.error(logMessage);
            throw new ApiException(SignerError.UnexpectedError,
                    SignerError.UnexpectedError.getFormattedMessage());
        }

        String nonce = variables.getNonce();
        String presentation_id = variables.getPresentation_id();
        log.info("Current Verifier Variables State: " + verifierVariables);
        log.info("User " + user + " tried executed the operation " + type + ". Nonce: " + nonce + " & Presentation_id: "
                + presentation_id);

        Map<String, String> headers = getHeaders();
        String url = uriToRequestWalletPID(presentation_id, nonce);

        String message = null;
        int responseCode = 400;
        long startTime = System.currentTimeMillis();
        while (responseCode != 200 && (System.currentTimeMillis() - startTime) < 60000) {
            WebUtils.StatusAndMessage response;
            try {
                response = WebUtils.httpGetRequests(url, headers);
            } catch (Exception e) {
                String logMessage = SignerError.FailedConnectionToVerifier.getCode()
                        + " (getVPTokenFromVerifier in VerifierClient.class) "
                        + SignerError.FailedConnectionToVerifier.getDescription() + ": " + e.getMessage();
                log.error(logMessage);
                throw new ApiException(SignerError.FailedConnectionToVerifier,
                        SignerError.FailedConnectionToVerifier.getFormattedMessage());
            }

            if (response.getStatusCode() == 404)
                throw new FailedConnectionVerifier();
            else if (response.getStatusCode() == 200) {
                responseCode = 200;
                message = response.getMessage();
            } else
                TimeUnit.SECONDS.sleep(1);
        }
        if (responseCode == 400 && (System.currentTimeMillis() - startTime) >= 60000)
            throw new TimeoutException();
        return message;
    }

    private boolean operationTypeIsInvalid(String type) {
        return !Objects.equals(type, Authorization) && !Objects.equals(type, Authentication);
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        // headers.put("Cookie",
        // "SERVERUSED=server1;
        // TS010b9524=01eb1053a0beaccfef181704d8d63c0d7987f347a2ec9fb8e7523c06298d62ad3dd30e17c3aa0a3535482f38f21aad94d3c37023fd39b9b7250ee76b594cb67c5aa2f212de");
        return headers;
    }

    private String generateNonce() throws Exception {
        SecureRandom prng = new SecureRandom();
        String randomNum = String.valueOf(prng.nextInt());
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] result = sha.digest(randomNum.getBytes());
        return Base64.getUrlEncoder().encodeToString(result);
    }

    private JSONObject httpRequestToInitPresentation(String jsonObjectString, Map<String, String> headers)
            throws Exception {
        HttpResponse response;
        try {
            response = WebUtils.httpPostRequest(verifierProperties.getUrl(), headers, jsonObjectString);
        } catch (Exception e) {
            throw new Exception("An error occurred when trying to connect to the Verifier");
        }

        if (response.getStatusLine().getStatusCode() != 200) {
            String error = WebUtils.convertStreamToString(response.getEntity().getContent());
            int statusCode = response.getStatusLine().getStatusCode();
            log.error("HTTP Post Request not successful. Error : " + statusCode);
            throw new Exception("HTTP Post Request not successful. Error : " + response.getStatusLine().getStatusCode());
        }

        HttpEntity entity = response.getEntity();
        if (entity == null) {
            throw new Exception("Response to the presentation request is empty.");
        }
        String result = WebUtils.convertStreamToString(entity.getContent());

        JSONObject responseVerifier;
        try{
            responseVerifier =  new JSONObject(result);
        }
        catch (JSONException e){
            throw new Exception("The response from the Verifier doesn't contain a correctly formatted JSON string.");
        }
        return responseVerifier;
    }

    // returns the presentation_id
    private String getPresentationIdAndCreateDeepLink(JSONObject responseFromVerifier, RedirectLinkResponse response)
            throws ApiException, Exception {
        Set<String> keys = responseFromVerifier.keySet();
        if (!keys.contains("request_uri") || !keys.contains("client_id") || !keys.contains("presentation_id")) {
            String logMessage = SignerError.MissingDataInResponseVerifier.getCode()
                    + "(getPresentationIdAndCreateDeepLink in VerifierClient.class) "
                    + SignerError.MissingDataInResponseVerifier.getDescription();
            log.error(logMessage);
            throw new ApiException(SignerError.MissingDataInResponseVerifier,
                    SignerError.MissingDataInResponseVerifier.getFormattedMessage());
        }

        String request_uri = responseFromVerifier.getString("request_uri");
        String client_id = responseFromVerifier.getString("client_id");
        if(!client_id.equals(this.verifierProperties.getAddress())){
            String logMessage = SignerError.UnexpectedError.getCode()
                    + "(getPresentationIdAndCreateDeepLink in VerifierClient.class) Message received from the Verifier doesn't contained the client_id expected.";
            log.error(logMessage);
            throw new ApiException(SignerError.UnexpectedError,
                    SignerError.UnexpectedError.getFormattedMessage());
        }
        String presentation_id = responseFromVerifier.getString("presentation_id");
        String encoded_request_uri = URLEncoder.encode(request_uri, StandardCharsets.UTF_8);

        // Generates a deepLink to the EUDIW App
        String deepLink = redirectUriDeepLink(encoded_request_uri, client_id);
        response.setLink(deepLink);
        return presentation_id;
    }

    private String redirectUriDeepLink(String request_uri, String client_id) {
        return "eudi-openid4vp://" +
                verifierProperties.getAddress() +
                "?client_id=" +
                client_id +
                "&request_uri=" +
                request_uri;
    }

    private String uriToRequestWalletPID(String presentation_id, String nonce) {
        return verifierProperties.getUrl() +
                "/" + presentation_id +
                "?nonce=" +
                nonce;
    }
}
