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
import org.springframework.context.annotation.PropertySource;

@PropertySource("file:application.yml")
@ConfigurationProperties(prefix = "assina")
public class AppProperties {
    private final Auth auth = new Auth();

    public static class Auth {
        private String type;
        private long lifetimeMinutes;

        public String getType() {
            return this.type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public long getLifetimeMinutes() {
            return this.lifetimeMinutes;
        }

        public void setLifetimeMinutes(long lifetimeMinutes) {
            this.lifetimeMinutes = lifetimeMinutes;
        }
    }

    public Auth getAuth() {
        return auth;
    }

}
