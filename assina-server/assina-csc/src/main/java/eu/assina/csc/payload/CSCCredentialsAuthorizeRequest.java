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

package eu.assina.csc.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.assina.csc.model.AssinaCSCConstants;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * Body for request of credentials/authorize - request for authorizing a user's credential
 * (certificate and private key) to be used for signing a hash in a subsequenet request.
 *
 * From section 11.6 of the CSC API V_1.0.4.0 spec
 */
public class CSCCredentialsAuthorizeRequest {

    public CSCCredentialsAuthorizeRequest() {
    }

    // REQUIRED
    // The unique identifier associated to the credential.
    @NotBlank(message = "MissingCredentialId")
    private String credentialID;

    @NotNull(message = "MissingNumSignatures")
    @Min(value = 1, message = "InvalidNumSignatures")
    @Max(value = AssinaCSCConstants.CSC_MAX_REQUEST_SIGNATURES, message = "TooHighNumSignatures")
    private int numSignatures;

    // REQUIRED Conditional
    // One or more Base64-encoded hash values to be signed.
    // It allows the server to bind the SAD to the hash(es), thus preventing an authorization
    // to be used to sign a different content. If the SCAL parameter returned by credentials/info
    // method, as defined in section 11.5, for the current credentialID is “2” the hash parameter
    // SHALL be used  and the number of hash values SHOULD correspond to the value in numSignatures.
    // If the SCAL parameter is “1”, the hash parameter  is OPTIONAL.
    // ASSINA uses SCAL 1 so ignored
    private List<String> hash;

    // REQUIRED Connditional (USED BY ASSINA)
    // The PIN provided by the user.
    // It SHALL be used only when authMode from credentials/info is “explicit” and PIN/presence is not “false”.
    // ASSINA expects a 4 digit pin but hashed with bcrpyt (so not numeric here)
    @NotBlank(message = "InvalidPin")
    @JsonProperty("PIN") // must be uppercase
    private String PIN;

    // not used in Assina
    @JsonProperty("OTP") // must be uppercase
    private String OTP;

    // A free form description of the authorization transaction in the lang language.
    // The maximum size of the string is 500 characters. It can be useful when authMode from
    // credentials/info method, as defined in section 11.5, is “implicit” to provide some hints
    // about the occurring transaction.
    @Size(max = 500) // per CSC 11.6
    private String description;

    // OPTIONAL
    // String The clientData as defined in the Input parameter table in section 8.3.2.
    private String clientData;

    public String getCredentialID() {
        return credentialID;
    }

    public void setCredentialID(String credentialID) {
        this.credentialID = credentialID;
    }

    public int getNumSignatures() {
        return numSignatures;
    }

    public void setNumSignatures(int numSignatures) {
        this.numSignatures = numSignatures;
    }

    public List<String> getHash() {
        return hash;
    }

    public void setHash(List<String> hash) {
        this.hash = hash;
    }

    public String getPIN() {
        return PIN;
    }

    public void setPIN(String PIN) {
        this.PIN = PIN;
    }

    public String getOTP() {
        return OTP;
    }

    public void setOTP(String OTP) {
        this.OTP = OTP;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getClientData() {
        return clientData;
    }

    public void setClientData(String clientData) {
        this.clientData = clientData;
    }
}
