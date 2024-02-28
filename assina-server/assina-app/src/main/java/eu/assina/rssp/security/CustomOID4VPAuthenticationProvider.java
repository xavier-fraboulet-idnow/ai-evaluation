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

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class CustomOID4VPAuthenticationProvider implements AuthenticationProvider{
    
    private final AssinaUserOID4VPDetailsService userDetailsService;

    public CustomOID4VPAuthenticationProvider(AssinaUserOID4VPDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if(authentication.getClass().equals(OID4VPAuthenticationToken.class)){
            OID4VPAuthenticationToken auth = (OID4VPAuthenticationToken) authentication;
            String hash = (authentication.getPrincipal() == null) ? "NONE_PROVIDED" : auth.getHash();
            String n = hash+";"+auth.getFullName();
            UserDetails userDetails = userDetailsService.loadUserByUsername(n);
            if(userDetails == null) throw new UsernameNotFoundException("User Not Found");
            OID4VPAuthenticationToken result = new OID4VPAuthenticationToken(
                    userDetails, userDetails.getAuthorities());
            result.setDetails(authentication.getDetails());
            return result;
        }
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(OID4VPAuthenticationToken.class);
    }

}
