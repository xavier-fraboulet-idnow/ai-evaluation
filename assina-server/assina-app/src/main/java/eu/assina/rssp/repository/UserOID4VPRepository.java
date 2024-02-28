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

package eu.assina.rssp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import eu.assina.rssp.api.model.UserOID4VP;

@Repository
public interface UserOID4VPRepository extends JpaRepository<UserOID4VP, String>{
    
    Optional<UserOID4VP> findByHash(String hash);

    @Query("SELECT u.issuingCountry FROM UserOID4VP u WHERE u.hash = ?1")
    Optional<String> findIssuingCountryByHash(String hash);
}
