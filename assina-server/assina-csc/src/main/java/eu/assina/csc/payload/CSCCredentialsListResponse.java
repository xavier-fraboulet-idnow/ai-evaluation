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

import java.util.List;

/**
 * Body for response of credentials/list - a list, possibly paginated, of credential IDs
 * From section 11.4 of the CSC API V_1.0.4.0 spec
 */
public class CSCCredentialsListResponse {

    // REQUIRED
    // One or more credentialID(s) associated with the provided or implicit userID.
    // No more than maxResults items SHALL be returned.
    private List<CredentialInfo> credentialIDs;

    // OPTIONAL
    // The page token required to retrieve the next page of results.
    // No value SHALL be returned if the remote service
    // does not suport items pagination or the response relates to the last page of results.
    private String nextPageToken;




    public List<CredentialInfo> getCredentialInfo() {
        return credentialIDs;
    }

    public void setCredentialInfo(List<CredentialInfo> credentialIDs) {
        this.credentialIDs = credentialIDs;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }
}
