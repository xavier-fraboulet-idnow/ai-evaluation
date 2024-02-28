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

package eu.assina.rssp.crypto;

import eu.assina.rssp.api.controller.LoggerUtil;
import eu.assina.rssp.common.config.CSCProperties;
import eu.assina.rssp.common.error.ApiException;
import eu.assina.rssp.common.error.AssinaError;
import eu.assina.rssp.common.model.AssinaCredential;
import eu.assina.rssp.common.model.Certificate;
import eu.assina.rssp.ejbca.EJBCAService;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Component
public class AssinaCryptoService {

    private static final Logger log = LoggerFactory.getLogger(AssinaCryptoService.class);

    private final CertificateGenerator generator;
    private final CryptoSigner cryptoSigner;
    private final CryptoConfig config;
    private final PemConverter pemConverter;

    @Autowired
    private EJBCAService ejbcaService;

    public AssinaCryptoService(@Autowired CSCProperties cscProperties) {
        this.config = cscProperties.getCrypto();
        this.cryptoSigner = new CryptoSigner(config);
        this.generator = new CertificateGenerator(config);
        this.pemConverter = new PemConverter(config);
    }

    public AssinaCredential createCredential(String owner, String subjectDN, String alias)
    {
        // System.out.println(subjectDN);
        try {
            AssinaCredential credential = new AssinaCredential();

            // Replace this request by a HSM request
            final KeyPair keyPair = generator.generateKeyPair();
            PKCS10CertificationRequest certificate = generator.generateCSR(keyPair, subjectDN, "PT", "Trust Provider Signer EUDIW", alias);
  
		    LoggerUtil.logs_user(1, owner, 3);

            String certificateString = 
                "-----BEGIN CERTIFICATE REQUEST-----\n" +
                new String(Base64.getEncoder().encode(certificate.getEncoded())) + "\n" +
                "-----END CERTIFICATE REQUEST-----";

            List<X509Certificate> EJBCAcertificates = this.ejbcaService.certificateRequest(certificateString);
            X509Certificate ejbcaCert = EJBCAcertificates.get(0);
            List<X509Certificate> ejbcaCertificateChain = EJBCAcertificates.subList(1, EJBCAcertificates.size());
            // generator.writeCertToFileBase64Encoded(EJBCAcertificate, "/mnt/c/Users/Mariana Rodrigues/rQES_Projects/deviseFutures/assina/files/LEI_Assina_test.csr");

            credential.setKeyAlgorithmOIDs(generator.getKeyAlgorithmOIDs());
            credential.setKeyBitLength(generator.getKeyBitLength());
            credential.setECDSACurveOID(generator.getECSDACurveOID());
            credential.setKeyEnabled(true); // enabled by definition

            // convert keys and cert to Base64 strings
            credential.setCertificate(pemConverter.certificateToString(ejbcaCert));
            credential.setPrivateKey(pemConverter.privateKeyToString(keyPair.getPrivate()));
            credential.setPublicKey(pemConverter.publicKeyToString(keyPair.getPublic()));

            credential.setAlias(alias);
            credential.setOwner(owner);
            credential.setSubjectDN(ejbcaCert.getSubjectX500Principal().getName());
            credential.setIssuerDN(ejbcaCert.getIssuerX500Principal().getName());
            credential.setValidFrom(ejbcaCert.getNotBefore().toString());
            credential.setValidTo(ejbcaCert.getNotAfter().toString());

            List<Certificate> certs = new ArrayList<>();
            for(int i = 0; i < ejbcaCertificateChain.size(); i++){
                Certificate cert = new Certificate();
                cert.setCertificate(pemConverter.certificateToString(ejbcaCertificateChain.get(i)));
                cert.setAssinaCertificate(credential);
                certs.add(cert);
            }
            credential.setCertificateChains(certs);

            return credential;
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            LoggerUtil.logs_user(0, owner, 1);
            throw new ApiException(AssinaError.FailedCreatingCredential, e);
        }
    }


    public String signWithPemCertificate(String dataToSignB64, String pemCertificate, List<String> pemCertificateChain,
                                         String pemSigningKey, String signingAlgo, String signingAlgoParams) {
        // TODO validate signingAlgo against the signing algo params
        final X509Certificate x509Certificate = pemToX509Certificate(pemCertificate);
        final PrivateKey privateKey = pemToPrivateKey(pemSigningKey);

        /*System.out.println(x509Certificate.getSubjectX500Principal());
        System.out.println(pemCertificateChain.size());

        try{
            generator.writeCertToFileBase64Encoded(x509Certificate, "/mnt/c/Users/Mariana Rodrigues/rQES_Projects/deviseFutures/assina/files/certificate.crt");
        }
        catch(Exception e){
            e.printStackTrace();
        }*/



        List<X509Certificate> x509CertificateChain = new ArrayList<>();
        for (int i = 0; i < pemCertificateChain.size(); i++){
            x509CertificateChain.add(pemToX509Certificate(pemCertificateChain.get(i)));
            // System.out.println(x509CertificateChain.get(i).getSubjectDN());
        }

        /*try{
            generator.writeCertToFileBase64Encoded(x509CertificateChain.get(0), "/mnt/c/Users/Mariana Rodrigues/rQES_Projects/deviseFutures/assina/files/certificate_first_chain.crt");
        }
        catch(Exception e){
            e.printStackTrace();
        }*/

        try {
            byte[] dataToSign = Base64.getDecoder().decode(dataToSignB64);
            final byte[] bytes = cryptoSigner.signData(dataToSign, x509Certificate, x509CertificateChain, privateKey);

            // Write the bytes to a file
            /*FileOutputStream fos = new FileOutputStream("/mnt/c/Users/Mariana Rodrigues/rQES_Projects/deviseFutures/assina/files/signed_data.cms");
            fos.write(bytes);
            fos.close();*/

            final String signedDataB64 = Base64.getEncoder().encodeToString(bytes); 
            
            return signedDataB64;
        } catch (CertificateEncodingException | IOException | CMSException | OperatorCreationException e) {
            throw new ApiException(AssinaError.FailedSigningData, e);
        }
    }

    /**
     * Unmarshall the PEM string (Base64) form of the certificate into an X509Certificate object
     * @param pemCertificate
     * @return proper X509Certificate object
     */
    public X509Certificate pemToX509Certificate(String pemCertificate) {
        try {
            final X509Certificate x509Certificate = pemConverter.stringToCertificate(pemCertificate);
            return x509Certificate;
        } catch (IOException | CertificateException e) {
            throw new ApiException(AssinaError.FailedUnmarshallingPEM, e);
        }
    }

    /**
     * Unmarshall the PEM string (Base64) form of the certificate into an X509Certificate object
     * @param pemKey
     * @return proper X509Certificate object
     */
    public PrivateKey pemToPrivateKey(String pemKey) {
        try {
            final PrivateKey privateKey = pemConverter.stringToPrivateKey(pemKey);
            return privateKey;
        } catch (IOException | PKCSException e) {
            throw new ApiException(AssinaError.FailedUnmarshallingPEM, e);
        }
    }

    public boolean isCertificateExpired(X509Certificate x509Certificate) {
        return generator.isCertificateExpired(x509Certificate);
    }
}
