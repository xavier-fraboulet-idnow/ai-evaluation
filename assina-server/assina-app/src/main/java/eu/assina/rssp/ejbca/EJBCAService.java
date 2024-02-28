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

package eu.assina.rssp.ejbca;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import eu.assina.rssp.util.WebUtils;
import org.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

@Component
public class EJBCAService {

    private Properties properties;

    private static String filename = "ejbca.conf";
    
    private static final Logger log = LoggerFactory.getLogger(EJBCAService.class);
 
    public EJBCAService(){
        this.properties = new Properties();
        try{
            FileInputStream fis = new FileInputStream(filename);
            this.properties.load(fis);
        }
        catch(Exception e){
            log.error("Error initalizing the EJBCA Service.", e);
        }
    }


    // certreq <username> <subjectdn> <subjectaltname or NULL> <caname> 
    // <endentityprofilename> <certificateprofilename> <reqpath>
    // <reqtype (PKCS10|SPKAC|CRMF)> <encoding (DER|PEM)> <outputpath (optional)>

    // [0] : certificate
    // [1..] : certificate Chain
    public List<X509Certificate> certificateRequest(String certificateRequest) throws Exception{

        String CAHost = this.properties.getProperty("ejbca.CAHost");
        String endpoint = this.properties.getProperty("ejbca.Endpoint");
        String clientP12ArchiveFilepath = this.properties.getProperty("ejbca.clientP12ArchiveFilepath");
        String clientP12ArchivePassword = this.properties.getProperty("ejbca.clientP12ArchivePassword");
        String ManagementCA = this.properties.getProperty("ejbca.ManagementCA");
        String certificateProfileName = this.properties.getProperty("ejbca.certificateProfileName");
        String endEntityProfileName = this.properties.getProperty("ejbca.endEntityProfileName");
        String certificateAuthorityName = this.properties.getProperty("ejbca.certificateAuthorityName");
        String username = this.properties.getProperty("ejbca.username");
        String password = this.properties.getProperty("ejbca.password");
        boolean includeChain = Boolean.parseBoolean(this.properties.getProperty("ejbca.includeChain"));

        JSONObject JsonBody = new JSONObject();
        JsonBody.put("certificate_request", certificateRequest);
        JsonBody.put("certificate_profile_name", certificateProfileName);
        JsonBody.put("end_entity_profile_name", endEntityProfileName);
        JsonBody.put("certificate_authority_name", certificateAuthorityName);
        JsonBody.put("username", username);
        JsonBody.put("password", password);
        JsonBody.put("include_chain", includeChain);
        String jsonBody1 = JsonBody.toString();

        String postUrl = "https://" + CAHost + "/ejbca/ejbca-rest-api/v1" + endpoint;

        // Set up headers
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("Content-Type", "application/json");

        KeyManager[] keyStorePKCS12 = getKeyStoreFromPKCS12File(clientP12ArchiveFilepath, clientP12ArchivePassword);
        TrustManager[] trustManagerCA = getTrustManagerOfCACertificate(ManagementCA);
        HttpResponse response = WebUtils.httpPostRequestsWithCustomSSLContext(trustManagerCA, keyStorePKCS12, postUrl, jsonBody1, headers);

        if(response.getStatusLine().getStatusCode() != 201) {
            throw new Exception("Certificate was not created by EJBCA");
        }

        HttpEntity entity = response.getEntity();
        if(entity == null) {
            throw new Exception("Message from EJBCA is empty");
        }

        InputStream instream = entity.getContent();
        String result = WebUtils.convertStreamToString(instream);

        return getCertificateFromHttpResponse(result);
    }
   
    private static KeyManager[] getKeyStoreFromPKCS12File(String PKCS12File, String PKCS12password) throws Exception {

        //Load PKCS#12 certificate
        KeyStore clientstore = KeyStore.getInstance("PKCS12");
        char[] password = PKCS12password.toCharArray();
        FileInputStream fis = new FileInputStream(PKCS12File);
        clientstore.load(fis, password);

        // Create KeyManagerFactory
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(clientstore, password);

        return keyManagerFactory.getKeyManagers();
    }

    private static TrustManager[] getTrustManagerOfCACertificate(String CAFilepath) throws Exception {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("x509");
        FileInputStream caInputStream = new FileInputStream(CAFilepath);
        X509Certificate caCertificate = (X509Certificate) certificateFactory.generateCertificate(caInputStream);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        KeyStore caKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        caKeyStore.load(null, null);
        caKeyStore.setCertificateEntry("ca", caCertificate);       
        tmf.init(caKeyStore);
        return tmf.getTrustManagers();
    }

    // [0] : certificate
    // [1..] : certificate Chain
    private List<X509Certificate> getCertificateFromHttpResponse(String result) throws Exception{
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        List<X509Certificate> certs = new ArrayList<>();
        JSONObject jsonResult = new JSONObject(result);

        if(!jsonResult.keySet().contains("certificate"))
            throw new Exception("Response from EJBCA doesn't contain a certificate value.");
        
        // Get the Certificate from the response from the EJBCA.
        String certificateContent = jsonResult.getString("certificate");
        byte[] certificateBytes = Base64.getDecoder().decode(certificateContent);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(certificateBytes);
        X509Certificate certificate = (X509Certificate)certificateFactory.generateCertificate(inputStream);
        certs.add(certificate);

        // If the response from the EJBCA includes the Certificate Chain then get the Certificate Chain from the response
        if(Boolean.parseBoolean(this.properties.getProperty("ejbca.includeChain")) && jsonResult.keySet().contains("certificate_chain")){
            JSONArray certificateChain = jsonResult.getJSONArray("certificate_chain");
            for(int i = 0; i < certificateChain.length(); i++){
                byte[] singleCertificateBytes = Base64.getDecoder().decode(certificateChain.getString(i));
                ByteArrayInputStream inputStreamSingleCertificate = new ByteArrayInputStream(singleCertificateBytes);
                X509Certificate singleCertificate = (X509Certificate)certificateFactory.generateCertificate(inputStreamSingleCertificate);
                certs.add(singleCertificate);
            }
        }

        return certs;
    }
}