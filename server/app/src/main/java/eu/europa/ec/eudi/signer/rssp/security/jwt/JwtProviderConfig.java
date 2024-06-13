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

package eu.europa.ec.eudi.signer.rssp.security.jwt;

/**
 * Minimal configuration for JWT token creation and validation
 */
public class JwtProviderConfig {
    private long lifetimeMinutes;
    private String tokenSecret;
    private String type;

    public long getLifetimeMinutes() {
        return lifetimeMinutes;
    }

    public void setLifetimeMinutes(long lifetimeMinutes) {
        this.lifetimeMinutes = lifetimeMinutes;
    }

    public String getTokenSecret() {
        return tokenSecret;
    }

    public void setTokenSecret(String tokenSecret) {
        this.tokenSecret = tokenSecret;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
