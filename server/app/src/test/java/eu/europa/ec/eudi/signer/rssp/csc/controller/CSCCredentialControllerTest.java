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

package eu.europa.ec.eudi.signer.rssp.csc.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.europa.ec.eudi.signer.rssp.csc.services.CSCCredentialsService;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class CSCCredentialControllerTest {

  static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private CSCCredentialsService credentialsService;

  /*
   * @Test
   * 
   * @WithUserDetails("Carlos")
   * public void listCredentials_200Returned() throws Exception {
   * CSCCredentialsListResponse mockResponse = new CSCCredentialsListResponse();
   * mockResponse.setCredentialIDs(Arrays.asList("fake-credential-id-1",
   * "fake-credential-id-2"));
   * 
   * when(credentialsService.listCredentials(any(CSCCredentialsListRequest.class))
   * ).thenReturn(mockResponse);
   * 
   * this.mockMvc.perform(post("/csc/v1/credentials/list"))
   * .andDo(print())
   * .andExpect(status().isOk());
   * }
   */

  /*
   * @Test
   * 
   * @WithUserDetails("Carlos")
   * public void credentialInfo_200Returned() throws Exception {
   * CSCCredentialsInfoResponse mockResponse = new CSCCredentialsInfoResponse();
   * mockResponse.setDescription("mock response");
   * CSCCredentialsInfoRequest request = new CSCCredentialsInfoRequest();
   * request.setCredentialID("mock-credential-id");
   * 
   * when(credentialsService.getCredentialsInfoFromAlias(any())).thenReturn(
   * mockResponse);
   * 
   * this.mockMvc.perform(post("/csc/v1/credentials/info/")
   * .content(asJson(request))
   * .contentType(MediaType.APPLICATION_JSON))
   * .andDo(print())
   * .andExpect(status().isOk())
   * .andExpect(content().json(asJson(mockResponse)));
   * }
   */

  /*
   * @Test
   * 
   * @WithUserDetails("Carlos")
   */
  /**
   * Validate missing credneitalID per the CSC spec
   */
  /*
   * public void invalidCredentialId_400() throws Exception {
   * CSCCredentialsInfoResponse mockResponse = new CSCCredentialsInfoResponse();
   * CSCCredentialsInfoRequest request = new CSCCredentialsInfoRequest();
   * request.setCredentialID(null);
   * 
   * when(credentialsService.getCredentialsInfoFromAlias(request)).thenReturn(
   * mockResponse);
   * 
   * this.mockMvc.perform(post("/csc/v1/credentials/info/")
   * .content(asJson(request))
   * .contentType(MediaType.APPLICATION_JSON))
   * .andDo(print())
   * .andExpect(TestResponseMatches.validateCSCErrorResponse(CSCInvalidRequest.
   * MissingCredentialId));
   * }
   */

  private String asJson(final Object object) throws JsonProcessingException {
    return OBJECT_MAPPER.writeValueAsString(object);
  }
}
