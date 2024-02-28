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

package eu.assina.rssp.api.services;

import eu.assina.rssp.api.model.User;
import eu.assina.rssp.api.model.UserBase;
import eu.assina.rssp.api.model.UserOID4VP;
import eu.assina.rssp.api.payload.UserProfile;
import eu.assina.rssp.common.error.ApiException;
import eu.assina.rssp.common.error.AssinaError;
import eu.assina.rssp.repository.CredentialRepository;
import eu.assina.rssp.repository.UserOID4VPRepository;
import eu.assina.rssp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// TODO rename to AssinaUserService
@Service
public class UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserRepository userRepository;
    private UserOID4VPRepository userOID4VPRepository;
    private CredentialRepository credentialRepository;

    @Autowired
    public UserService(UserRepository userRepository,
                       UserOID4VPRepository userOID4VPRepository,
                       CredentialRepository credentialRepository) {
        this.userRepository = userRepository;
        this.userOID4VPRepository = userOID4VPRepository;
        this.credentialRepository = credentialRepository;
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    public Optional<UserOID4VP> getUserOID4VPById(String id){
        return userOID4VPRepository.findById(id);
    }

    /*public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }*/

    public boolean isUsernameAvailable(String username) {
        Boolean isAvailable = !userRepository.existsByUsername(username);
        return !isAvailable;
    }

    public boolean isEmailAvailable(String email) {
        Boolean isAvailable = !userRepository.existsByEmail(email);
        return isAvailable;
    }

    public User updateUser(User update) {
        User user = getUserById(update.getId()).orElseThrow(
                () -> new ApiException(AssinaError.UserNotFound, "Failed to find user {}", update.getId()));

        if (StringUtils.hasText(update.getPlainPassword())) {
            user.setPassword(passwordEncoder.encode(update.getPlainPassword()));
        }
        if (StringUtils.hasText(update.getPlainPIN())) {
            user.setEncodedPIN(passwordEncoder.encode(update.getPlainPIN()));
        }
        if (StringUtils.hasText(update.getEmail())) {
            user.setEmail(update.getEmail());
        }
        if (StringUtils.hasText(update.getName())) {
            // can update the name but not the username
            user.setName(update.getName());
        }
        if (StringUtils.hasText(update.getImageUrl())) {
            // can update the name but not the username
            user.setImageUrl(update.getImageUrl());
        }
        user.setUpdatedAt(Instant.now());
        final User updated = userRepository.save(user);
        return updated;
    }

    public Optional<UserProfile> getUserProfile(String id) {
        return getUserById(id).map(this::profile);
    }

    public List<UserProfile> getUserProfiles(Pageable pageable) {
        List<UserProfile> profiles = userRepository.findAll(pageable).stream().map(this::profile)
                                             .collect(Collectors.toList());

        return profiles;
    }

    public void deleteUser(String userId) {
        try {
            userRepository.deleteById(userId);
        }
        catch (EmptyResultDataAccessException ex) {
            throw new ApiException(AssinaError.UserNotFound, "Attempted to delete user that does exist", userId);
        }
    }

    /**
     * Functional to wrap user in profile
     */
    private UserProfile profile(User user) {
        return new UserProfile(user.getId(), user.getName(), user.getName(), user.getCreatedAt(),
                credentialRepository.countByOwner(user.getId()));
    }
}
