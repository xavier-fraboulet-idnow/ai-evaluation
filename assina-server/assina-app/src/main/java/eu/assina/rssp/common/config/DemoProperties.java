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

package eu.assina.rssp.common.config;

import eu.assina.rssp.api.model.User;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;


@ConfigurationProperties(prefix = "demo")
public class DemoProperties {
    private List<DemoUser> users = new ArrayList<>();

    public void setUsers(List<DemoUser> users) {
        this.users = users;
    }

    public List<DemoUser> getUsers() {
        return users;
    }

    public static class DemoUser extends User {
        private String username;
        private String name;
        private String email;
        private String role;

        private int numCredentials;

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public void setUsername(String username) {
            this.username = username;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String getEmail() {
            return email;
        }

        @Override
        public void setEmail(String email) {
            this.email = email;
        }

        @Override
        public String getRole() {
            return role;
        }

        @Override
        public void setRole(String role) {
            this.role = role;
        }

        public int getNumCredentials() {
            return numCredentials;
        }
        public void setNumCredentials(int numCredentials) {
            this.numCredentials = numCredentials;
        }
    }
}
