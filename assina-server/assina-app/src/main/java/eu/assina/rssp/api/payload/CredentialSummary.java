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

package eu.assina.rssp.api.payload;

import java.time.Instant;

public class CredentialSummary {
    private String id;
    private String ownerUsername;
    private String ownerName;
    private Instant createdAt;
    private String description;

    public CredentialSummary(String id, String ownerUsername, String ownerName, Instant createdAt,
                             String description) {
        this.id = id;
        this.ownerUsername = ownerUsername;
        this.ownerName = ownerName;
        this.createdAt = createdAt;
        this.description = description;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getDescription() {
        return description;
    }
}
