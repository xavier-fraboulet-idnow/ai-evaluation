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

package eu.europa.ec.eudi.signer.rssp.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

// @PropertySource("file:application-auth.yml")
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {
    private String datasourceUsername;
    private String datasourcePassword;
    private String jwtTokenSecret;
    private String sadTokenSecret;
    private String dbEncryptionPassphrase;
    private String dbEncryptionSalt;

    public String getDatasourceUsername() {
        return this.datasourceUsername;
    }

    public void setDatasourceUsername(String datasourceUsername) {
        this.datasourceUsername = datasourceUsername;
    }

    public String getDatasourcePassword() {
        return this.datasourcePassword;
    }

    public void setDatasourcePassword(String datasourcePassword) {
        this.datasourcePassword = datasourcePassword;
    }

    public String getJwtTokenSecret() {
        return this.jwtTokenSecret;
    }

    public void setJwtTokenSecret(String jwtTokenSecret) {
        this.jwtTokenSecret = jwtTokenSecret;
    }

    public String getSadTokenSecret() {
        return this.sadTokenSecret;
    }

    public void setSadTokenSecret(String sadTokenSecret) {
        this.sadTokenSecret = sadTokenSecret;
    }

    public String getDbEncryptionPassphrase() {
        return this.dbEncryptionPassphrase;
    }

    public void setDbEncryptionPassphrase(String dbEncryptionPassphrase) {
        this.dbEncryptionPassphrase = dbEncryptionPassphrase;
    }

    public String getDbEncryptionSalt() {
        return this.dbEncryptionSalt;
    }

    public void setDbEncryptionSalt(String dbEncryptionSalt) {
        this.dbEncryptionSalt = dbEncryptionSalt;
    }

}
