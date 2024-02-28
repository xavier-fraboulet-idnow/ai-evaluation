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

package eu.assina.rssp.security;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import eu.assina.rssp.api.model.UserOID4VP;
import eu.assina.rssp.repository.UserOID4VPRepository;

/**
 * Custom user details service for loading the user by username or email during authenticated requests
 */
@Service
public class AssinaUserOID4VPDetailsService implements UserDetailsService {

    @Autowired
    UserOID4VPRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String hash) throws UsernameNotFoundException {
        String[] n = hash.split(";");
        UserOID4VP user = userRepository.findByHash(n[0])
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with hash: ."+hash)
                );
        return UserPrincipal.create(user, n[1]);
    }
}
