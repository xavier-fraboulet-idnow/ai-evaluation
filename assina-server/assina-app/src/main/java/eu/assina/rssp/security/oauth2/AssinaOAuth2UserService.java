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

package eu.assina.rssp.security.oauth2;

import eu.assina.rssp.api.model.AuthProvider;
import eu.assina.rssp.api.model.RoleName;
import eu.assina.rssp.api.model.User;
import eu.assina.rssp.repository.UserRepository;
import eu.assina.rssp.security.UserPrincipal;
import eu.assina.rssp.security.oauth2.user.OAuth2UserInfo;
import eu.assina.rssp.security.oauth2.user.OAuth2UserInfoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Optional;

/**
 * The AssinaOAuth2UserService extends Spring Security’s DefaultOAuth2UserService and implements
 * its loadUser() method.
 * This method is called after an access token is obtained from the OAuth2 provider.
 *
 * In this method, we first fetch the user’s details from the OAuth2 provider.
 * If a user with the same email already exists in our database then we update ther details,
 * otherwise, we register a new user.
 */
@Service
public class AssinaOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    /**
     * Called indirectly by the spring oauth2 login provider we use this to register the user with Assina
     * if we haven't seen them before (i.e. auto-onboard them)  or update their record in the database if
     * we have seen them.
      */
    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(oAuth2UserRequest.getClientRegistration().getRegistrationId(), oAuth2User.getAttributes());
        if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException(
                    "The OAuth2 provider needs to provide an email for your account.");
        }

        Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
        User user;
        if(userOptional.isPresent()) {
            user = userOptional.get();
            if(!user.getProvider().equals(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()))) {
                throw new OAuth2AuthenticationProcessingException("Looks like you're signed up with " +
                        user.getProvider() + " account. Please use your " + user.getProvider() +
                        " account to login.");
            }
            user = updateExistingUser(user, oAuth2UserInfo);
        } else {
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
        }

        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }

    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        User user = new User();
        // username is be same as email for oauth2 providers
        user.setUsername(oAuth2UserInfo.getEmail());
        user.setProvider(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()));
        user.setProviderId(oAuth2UserInfo.getId());
        user.setName(oAuth2UserInfo.getName());
        user.setEmail(oAuth2UserInfo.getEmail());
        user.setImageUrl(oAuth2UserInfo.getImageUrl());
        // oauth2 users default to regular user role
        user.setRole(RoleName.ROLE_USER.name());
        user.setCreatedAt(Instant.now());
        return userRepository.save(user);
    }

    /**
     * Keep existing users up to date with the oauth server - in case they change name, image etc.
     */
    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
        boolean updated = false;
        if (!StringUtils.hasText(existingUser.getName()) ||
                    !existingUser.getName().equals(oAuth2UserInfo.getName())) {
            existingUser.setName(oAuth2UserInfo.getName());
            updated = true;
        }
        if (!StringUtils.hasText(existingUser.getImageUrl()) ||
                    !existingUser.getImageUrl().equals(oAuth2UserInfo.getImageUrl())) {
            existingUser.setImageUrl(oAuth2UserInfo.getImageUrl());
            updated = true;
        }
        if (!StringUtils.hasText(existingUser.getRole())) {
            // just in case role was missing
            existingUser.setRole(RoleName.ROLE_USER.name());
            updated = true;
        }

        if (updated) {
            existingUser.setUpdatedAt(Instant.now());
        }

        return userRepository.save(existingUser);
    }

}
