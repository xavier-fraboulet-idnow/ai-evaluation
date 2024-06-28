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

package eu.europa.ec.eudi.signer.rssp.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import eu.europa.ec.eudi.signer.rssp.common.config.AppProperties;
import eu.europa.ec.eudi.signer.rssp.common.config.AuthProperties;
import eu.europa.ec.eudi.signer.rssp.common.config.AppProperties.Auth;
import eu.europa.ec.eudi.signer.rssp.security.jwt.JwtProvider;
import eu.europa.ec.eudi.signer.rssp.security.jwt.JwtProviderConfig;
import eu.europa.ec.eudi.signer.rssp.security.jwt.JwtToken;
import eu.europa.ec.eudi.signer.rssp.security.openid4vp.OpenId4VPAuthenticationToken;

@Service
public class UserAuthenticationTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(UserAuthenticationTokenProvider.class);

    private final JwtProvider jwtProvider;

    public UserAuthenticationTokenProvider(AppProperties appProperties, AuthProperties authProperties) {
        Auth jwtConfig = appProperties.getAuth();
        JwtProviderConfig jwtProviderConfig = new JwtProviderConfig();
        jwtProviderConfig.setLifetimeMinutes(jwtConfig.getLifetimeMinutes());
        jwtProviderConfig.setType(jwtConfig.getType());
        jwtProviderConfig.setTokenSecret(authProperties.getJwtTokenSecret());
        jwtProvider = new JwtProvider(jwtProviderConfig);
    }

    public String createToken(Authentication authentication) {
        try {
            if (authentication.getClass().equals(OpenId4VPAuthenticationToken.class)) {
                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
                String username = userPrincipal.getUsername();
                String givenName = userPrincipal.getGivenName();
                String surname = userPrincipal.getSurname();
                final JwtToken token = jwtProvider.createToken(username + ";" + givenName + ";" + surname);
                return token.getRawToken();
            } else {
                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
                String subject = userPrincipal.getUsername();
                final JwtToken token = jwtProvider.createToken(subject);
                return token.getRawToken();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public JwtToken validateToken(String authToken) {
        JwtToken token = jwtProvider.validateToken(authToken);
        if (!token.isValid()) {
            log.error(token.getError());
        }
        return token;
    }

}
