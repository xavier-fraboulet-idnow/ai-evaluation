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

import static eu.europa.ec.eudi.signer.rssp.common.config.SignerConstants.CSC_URL_ROOT;
import eu.europa.ec.eudi.signer.rssp.security.openid4vp.OpenId4VPAuthenticationProvider;

import eu.europa.ec.eudi.signer.rssp.security.openid4vp.OpenId4VPUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.regex.Pattern;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

    private final OpenId4VPAuthenticationProvider customOID4VPAuthenticationProvider;
    private final UserAuthenticationTokenProvider tokenProvider;
    private final OpenId4VPUserDetailsService customUserOID4VPDetailsService;

    public SpringSecurityConfig(OpenId4VPAuthenticationProvider customAuthenticationProvider, @Autowired UserAuthenticationTokenProvider tokenProvider, @Autowired OpenId4VPUserDetailsService customUserOID4VPDetailsService) {
        this.customOID4VPAuthenticationProvider = customAuthenticationProvider;
        this.tokenProvider = tokenProvider;
        this.customUserOID4VPDetailsService = customUserOID4VPDetailsService;
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(this.tokenProvider, this.customUserOID4VPDetailsService);
    }

    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder
                .authenticationProvider(this.customOID4VPAuthenticationProvider);
    }

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {

        return super.authenticationManagerBean();
    }

    public boolean testFrenchMsisdn(String msisdn) {
        // Regular expression to match French mobile numbers
        String frenchMsisdnPattern = "^(\\+33|0)[67]\\d{8}$";
        Pattern pattern = Pattern.compile(frenchMsisdnPattern);
        return pattern.matcher(msisdn).matches();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .csrf()
                .disable()
                // .formLogin()
                // .disable()
                // .httpBasic()
                // .disable()
                .exceptionHandling()
                .authenticationEntryPoint(new RestAuthenticationEntryPoint())
                .and()
                .authorizeRequests()
                .antMatchers("/",
                        "/error",
                        "/favicon.ico",
                        "/**/*.png",
                        "/**/*.gif",
                        "/**/*.svg",
                        "/**/*.jpg",
                        "/**/*.html",
                        "/**/*.css",
                        "/**/*.js")
                .permitAll()
                .antMatchers("/auth/**") // permit local login
                .permitAll()
                .antMatchers(CSC_URL_ROOT + "/info").permitAll()
                .anyRequest()
                .authenticated();
        // Add our custom Token based authentication filter
        http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }
}