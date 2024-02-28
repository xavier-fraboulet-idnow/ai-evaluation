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
public class RedirectLinkResponse {
    public String link;
    public String presentationId;
    public String nonce;

    public void setLink(String link){
        this.link = link;
    }

    public String getLink(){
        return this.link;
    }

    public String getPresentationId() {
        return presentationId;
    }

    public void setPresentationId(String presentation_id) {
        this.presentationId = presentation_id;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }
}