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

package eu.europa.ec.eudi.signer.rssp.api.controller;

import eu.europa.ec.eudi.signer.common.SignerConstants;
import eu.europa.ec.eudi.signer.rssp.api.services.CredentialService;
import eu.europa.ec.eudi.signer.rssp.common.error.SignerError;
import eu.europa.ec.eudi.signer.rssp.entities.Credential;
import eu.europa.ec.eudi.signer.rssp.util.TestResponseMatches;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// TODO make this match section 11 of the CSC spec
//    https://stackoverflow.com/questions/50209302/spring-security-rest-unit-tests-fail-with-httpstatuscode-401-unauthorized
//@WebMvcTest(value = CredentialController.class)
//    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfigurer.class))
//    excludeAutoConfiguration = MockMvcSecurityAutoConfiguration.class)
//5/22/2021, 4:32:21 PM Deprecate 'secure' on WebMvcTest and AutoConfigureMockMvc · Issue #14227 · spring-projects/spring-boot
//    https://github.com/spring-projects/spring-boot/issues/14227#issuecomment-688824627
//@WebAppConfiguration
//@Import(value = {CredentialController.class, MockMvcAutoConfiguration.class})
//@EnablfindAlleConfigurationProperties({ResourceProperties.class, WebMvcProperties.class})
// TODO consider this: https://stackoverflow.com/questions/55448188/spring-boot-pagination-mockito-repository-findallpageable-returns-null/55448614
@AutoConfigureMockMvc
// TODO try one of the techniques above avoid running spring boot
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class CredentialControllerTest {

  static final String USER = "test-subject";
  static final String CREDENTIAL_ID = "1234-5678";
  static final String ALIAS = "test-credential-1";
  static Credential CREDENTIAL;

  private static String credPath(String id) {
    return SignerConstants.API_URL_ROOT + "/credentials/" + id;
  }

  static {
    try {
      CREDENTIAL = new Credential();
      CREDENTIAL.setId(CREDENTIAL_ID);
      CREDENTIAL.setOwner(USER);
      CREDENTIAL.setAlias(ALIAS);
      CREDENTIAL.setCertificate("mock-cert");
      CREDENTIAL.setPublicKeyHSM("mock-public-key".getBytes());
      CREDENTIAL.setPrivateKeyHSM("mock-private-key".getBytes());
      CREDENTIAL.setCreatedAt(Instant.now());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private CredentialService credentialService;

  @Test
  @WithMockUser("anyone")
  public void retrieveCredentialById_idNotFound_404() throws Exception {
    when(credentialService.getCredentialWithAlias(USER, ALIAS)).thenReturn(Optional.empty());
    this.mockMvc.perform(get(credPath(CREDENTIAL.getId())))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(TestResponseMatches.validateCSCErrorResponse(SignerError.CredentialNotFound));
  }

  @Test
  @WithMockUser("anyone")
  public void deleteCredential_idFound_204() throws Exception {
    this.mockMvc.perform(delete(credPath(CREDENTIAL.getId())))
        .andDo(print())
        .andExpect(status().isNoContent());
  }
}
