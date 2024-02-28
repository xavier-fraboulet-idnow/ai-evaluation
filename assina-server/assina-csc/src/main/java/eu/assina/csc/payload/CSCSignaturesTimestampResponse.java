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

/**
 * Body for request of signatures/timestamp for generating timestamp tokens for hashes.
 * From section 11.10 of the CSC API V_1.0.4.0 spec
 */
public class CSCSignaturesTimestampResponse {

    // REQUIRED
    // The Base64-encoded time-stamp token as defined in RFC 3161 [2] as updated by RFC 5816 [10].
    // If the nonce parameter is included in the request then it SHALL also be included in the
    // time-stamp token, otherwise the response SHALL be rejected.
    private String timestamp;

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
