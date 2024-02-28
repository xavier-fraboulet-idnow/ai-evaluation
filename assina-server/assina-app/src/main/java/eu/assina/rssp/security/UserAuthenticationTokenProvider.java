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

import eu.assina.rssp.common.config.AppProperties;
import eu.assina.rssp.security.jwt.JwtProvider;
import eu.assina.rssp.security.jwt.JwtProviderConfig;
import eu.assina.rssp.security.jwt.JwtToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class UserAuthenticationTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(UserAuthenticationTokenProvider.class);

    private JwtProvider jwtProvider;

    public UserAuthenticationTokenProvider(AppProperties appProperties) {
        JwtProviderConfig jwtConfig = appProperties.getAuth();
        jwtProvider = new JwtProvider(jwtConfig);
    }

    public String createToken(Authentication authentication) {
        try{
            if(authentication.getClass().equals(OID4VPAuthenticationToken.class)){
                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
                String username = userPrincipal.getUsername();
                String fullName = userPrincipal.getName();
                final JwtToken token = jwtProvider.createToken(username+";"+fullName);
                return token.getRawToken();
            }
            else{
                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
                String subject = userPrincipal.getUsername();
                final JwtToken token = jwtProvider.createToken(subject);
                return token.getRawToken();
            }
        }
        catch(Exception e){
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
