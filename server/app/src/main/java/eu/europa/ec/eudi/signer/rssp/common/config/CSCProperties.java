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

import eu.europa.ec.eudi.signer.csc.model.AbstractInfo;

@PropertySource("file:application.yml")
@ConfigurationProperties(prefix = "csc")
public class CSCProperties {

    // properties mapping to the CSC /info request
    private final Info info = new Info();

    // properties for controlling the API
    private final Api api = new Api();

    // properties for controlling crypto algos, signing etc for CSC
    private final Crypto crypto = new Crypto();

    // SAD config properties
    private final Sad sad = new Sad();

    // All CSC info properties are in the YAML file or environment
    public static class Info extends AbstractInfo {
    }

    public static class Crypto extends CryptoConfig {
    }

    public static class Sad {
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

    public static class Api {
        private int pageSize;
        private int maxPageSize;

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        public int getMaxPageSize() {
            return maxPageSize;
        }

        public void setMaxPageSize(int maxPageSize) {
            this.maxPageSize = maxPageSize;
        }
    }

    public CryptoConfig getCrypto() {
        return crypto;
    }

    public Sad getSad() {
        return sad;
    }

    public Api getApi() {
        return api;
    }

    public Info getInfo() {
        return info;
    }
}
