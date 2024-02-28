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

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class OID4VPAuthenticationToken extends AbstractAuthenticationToken {
    private final String hash;
    private final String fullName;
    private final Object principal;
    private final Object credentials;

    public OID4VPAuthenticationToken(String hash, String fullName){
        super(null);
        this.hash = hash;
        this.fullName = fullName;
        this.principal = hash;
        this.credentials = null;
    }

    public OID4VPAuthenticationToken(Object userPrincipal, Collection<? extends GrantedAuthority> authorities){
        super(authorities);
        UserPrincipal user = (UserPrincipal) userPrincipal;
        this.hash = user.getUsername();
        this.principal = user;
        this.credentials = user.getPassword();
        this.fullName = user.getName();
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    @Override
    public String getName(){
        return this.hash;
    }

    public String getHash(){
        return this.hash;
    }
    public String getFullName(){
        return this.fullName;
    }
}
