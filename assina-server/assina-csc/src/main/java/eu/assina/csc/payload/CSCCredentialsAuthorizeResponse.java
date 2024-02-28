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

/**
 * Body for response of credentials/authorize - provides the client the SAD token needed to
 * authorize the subsequent request to sign a hash.
 * From section 11.6 of the CSC API V_1.0.4.0 spec
 */
public class CSCCredentialsAuthorizeResponse {

    // REQUIRED
    // The Signature Activation Data (SAD) to be used as input to the signatures/signHash method,
    // as defined in section 11.9.
    private String SAD;

    // OPTIONAL
    // The lifetime in seconds of the SAD. If omitted, the default expiration time is 3600 (1 hour).
    private long expiresIn;

    @JsonProperty("SAD") // must be uppercase
    public String getSAD() {
        return SAD;
    }

    public void setSAD(String SAD) {
        this.SAD = SAD;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
