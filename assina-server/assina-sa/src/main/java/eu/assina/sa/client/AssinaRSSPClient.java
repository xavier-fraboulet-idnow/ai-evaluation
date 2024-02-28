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

package eu.assina.sa.client;

import eu.assina.csc.payload.*;
import eu.assina.sa.config.RSSPClientConfig;
import eu.assina.sa.error.CredentialNotFoundException;
import eu.assina.sa.error.InvalidRequestException;
import eu.assina.sa.error.RSSPClientException;
import eu.assina.sa.model.AssinaSigner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * REST client for the RSSP web service, used by signing applications to get credentials and sign hashes
 */
public class AssinaRSSPClient implements AssinaSigner {

    private WebClient webClient;

    private ClientContext context;
    private static final Logger log = LoggerFactory.getLogger(AssinaRSSPClient.class);

    public AssinaRSSPClient(RSSPClientConfig config) {
        webClient = WebClient.builder().baseUrl(config.setCscBaseUrl())
                            .defaultCookie("cookieKey", "cookieValue")
                            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).build();
    }



    @Override
    /** Prepares the AssignSigner */
    public ClientContext prepCredential() {
        // lista de alias
        final List<CredentialInfo> credentials = listCredentialsForCurrentUser();
        if (credentials == null || credentials.isEmpty()) {
            throw new CredentialNotFoundException("The current user has no credentials with which to sign");
        }

        List<String> credentialIDs = credentials.stream().map(CredentialInfo::getAlias).collect(Collectors.toList());

        // alias
        String credentialID = getContext().getCredentialID();
        if (StringUtils.hasText(credentialID)) {
            // make sure the current user owns the credential if it specified
            if (!credentialIDs.contains(credentialID)) {
                throw new InvalidRequestException("The current user does not own the specified credential");
            }
        } else {
            // convention: simply use the first credential if there is non specified in the context
            credentialID = credentialIDs.get(0);
        }
        context.setCredentialID(credentialID);

        final CSCCredentialsInfoResponse credentialInfo = getCredentialInfo(credentialID);
        if (credentialInfo == null) {
            throw new InvalidRequestException("Could not get info on the specified credential");
        }

        // get the algo from the credential info
        final CSCCredentialsInfoResponse.Key key = credentialInfo.getKey();
        if (key == null) {
            throw new InvalidRequestException("Could not get key info for the credential");
        }
        final List<String> algos = key.getAlgo();
        if (algos == null || algos.isEmpty()) {
            throw new InvalidRequestException("Could not get sign algos from the key info for the credential");
        }
        // simply use the fisrt algo
        String signAlgo = algos.get(0);
        context.setSignAlgo(signAlgo);

        final CSCCredentialsInfoResponse.Cert cert = credentialInfo.getCert();
        context.setSubject(cert.getSubjectDN());
        return context;
    }

    @Override
    public RedirectLinkResponse getOIDRedirectLink(){
        return getLink();
    }

    public RedirectLinkResponse getLink() {
        final RedirectLinkResponse response = requestOIDRedirectLink().block();
        assert response != null;
        return response;
    }

    public Mono<RedirectLinkResponse> requestOIDRedirectLink() {
        return webClient.get()
                .uri("/credentials/authorizationLink")
                .header("Authorization", buildAuthHeader())
                .retrieve()
                .bodyToMono(
                        RedirectLinkResponse.class
                );
    }


    @Override
    /** Implements the AssignSigner by signing the hash with several requests to the RSSP */
    public byte[] signHash(byte[] pdfHash, ClientContext context) {

        String credentialAlias = context.getCredentialID();
        // System.out.println("CredentialAlias: "+credentialAlias);
        String nonce = context.getNonce();
        String presentation_id = context.getPresentation_id();

        // now authorize it
        final String SAD = authorizeCredential(credentialAlias, getPIN(), nonce, presentation_id);
        if (SAD == null) {
            throw new InvalidRequestException("Could not authorize the credential with the PIN");
        }

        // and use it to sign
        log.info("Signing hash with credential: {}", credentialAlias);
        final String pdfHashB64 = Base64.getEncoder().encodeToString(pdfHash);

        final String signedHashB64 = signHash(pdfHashB64, credentialAlias, SAD, context.getSignAlgo());
        byte[] signedHash = Base64.getDecoder().decode(signedHashB64);

        return signedHash;
    }

    //lista de aliases
    public Mono<CSCCredentialsListResponse> requestCredentialList(CSCCredentialsListRequest request) {
        return webClient.post()
                       .uri("/credentials/list")
                       .bodyValue(request)
                       .header("Authorization", buildAuthHeader())
                       .exchangeToMono(response -> {
                           if (response.statusCode().equals(HttpStatus.OK)) {
                                return response.bodyToMono(CSCCredentialsListResponse.class);
                           }
                           else {
                               return Mono.error(new RSSPClientException(response));
                           }
                       });
    }

    public Mono<CSCCredentialsInfoResponse> requestCredentialInfo(CSCCredentialsInfoRequest request) {
        return webClient.post()
                       .uri("/credentials/info")
                       .bodyValue(request)
                       .header("Authorization", buildAuthHeader())
                       .exchangeToMono(response -> {
                           if (response.statusCode().equals(HttpStatus.OK)) {
                               return response.bodyToMono(CSCCredentialsInfoResponse.class);
                           }
                           else {
                               return Mono.error(new RSSPClientException(response));
                           }
                       });
    }

    public Mono<CSCCredentialsAuthorizeResponse> requestAuthorizeCredential(CSCCredentialsAuthorizeRequest request, String nonce, String presentation_id) {
        request.setClientData(nonce+"&"+presentation_id);

        return webClient.post()
                       .uri("/credentials/authorize")
                       .bodyValue(request)
                       .header("Authorization", buildAuthHeader())
                       .exchangeToMono(response -> {
                           if (response.statusCode().equals(HttpStatus.OK)) {
                               return response.bodyToMono(CSCCredentialsAuthorizeResponse.class);
                           }
                           else {
                               return Mono.error(new RSSPClientException(response));
                           }
                       });
    }

    public Mono<CSCSignaturesSignHashResponse> requestSignHash(CSCSignaturesSignHashRequest request) {
        return webClient.post()
                       .uri("/signatures/signHash")
                       .bodyValue(request)
                       .header("Authorization", buildAuthHeader())
                       .exchangeToMono(response -> {
                           if (response.statusCode().equals(HttpStatus.OK)) {
                               return response.bodyToMono(CSCSignaturesSignHashResponse.class);
                           }
                           else {
                               return Mono.error(new RSSPClientException(response));
                           }
                       });
    }

    // lista de aliases
    public List<CredentialInfo> listCredentialsForCurrentUser() {
        CSCCredentialsListRequest request = new CSCCredentialsListRequest();
        final CSCCredentialsListResponse response = requestCredentialList(request).block();
        return response.getCredentialInfo();
    }

    
    public CSCCredentialsInfoResponse getCredentialInfo(String credentialAlias) {
        CSCCredentialsInfoRequest request = new CSCCredentialsInfoRequest();
        request.setCredentialID(credentialAlias);
        request.setCertInfo(true);
        final CSCCredentialsInfoResponse response = requestCredentialInfo(request).block();
        return response;
    }


    public String authorizeCredential(String credentialAlias, String PIN, String nonce, String presentation_id) {
        CSCCredentialsAuthorizeRequest request = new CSCCredentialsAuthorizeRequest();
        if(Objects.equals(PIN, "")){
            PIN = "temporary";
        }
        request.setPIN(PIN);
        request.setCredentialID(credentialAlias);
        request.setNumSignatures(1);
        final CSCCredentialsAuthorizeResponse response = requestAuthorizeCredential(request, nonce, presentation_id).block();
        final String sad = response.getSAD();
        return sad;
    }

    public String signHash(String pdfHash, String credentialAlias, String SAD, String signAlgo) {
        CSCSignaturesSignHashRequest request = new CSCSignaturesSignHashRequest();
        request.setHash(Collections.singletonList(pdfHash));
        request.setCredentialID(credentialAlias);
        request.setSAD(SAD);
        request.setSignAlgo(signAlgo);
        final CSCSignaturesSignHashResponse response = requestSignHash(request).block();
        final List<String> signatures = response.getSignatures();
        return signatures.get(0);
    }

    /**
     * Set the context to be used in the next request
     */
    public void setContext(ClientContext context) {
        this.context = context;
    }

    private ClientContext getContext() {
        if (context == null) {
            throw new InvalidRequestException("ClientContext not set before using client");
        }
        return context;
    }

    private String getPIN() {
        String PIN = getContext().getPIN();
        if (PIN == null) {
            throw new InvalidRequestException("PIN not set in context");
        }
        return PIN;
    }

    private String buildAuthHeader() {
        String authorizationHeader = getContext().getAuthorizationHeader();
        if (authorizationHeader == null) {
            throw new InvalidRequestException("Authorization not set in context");
        }
        return authorizationHeader;
    }
}
