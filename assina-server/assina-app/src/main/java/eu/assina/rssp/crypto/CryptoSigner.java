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

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.*;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.OutputEncryptor;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Utility for signing data like document hashes
 */
public class CryptoSigner {

    private CryptoConfig config;

    public CryptoSigner(CryptoConfig config) {
        this.config = config;
    }

    /**
     * Cryptographically sign the given data with the supplied signature and private key
     *
     * @param data data to sign (usually a document hash)
     * @param signingCertificate certificate with which to sign the hash (contains the public key)
     * @param signingKey private key paired to the public key in the signing certificate
     * @return signature for provided data
     *
     * @throws CertificateEncodingException
     * @throws OperatorCreationException
     * @throws CMSException
     * @throws IOException
     */
    public byte[] signData(byte[] data, final X509Certificate signingCertificate, List<X509Certificate> certificateChain, final PrivateKey signingKey)
            throws CertificateEncodingException, OperatorCreationException, CMSException, IOException {
        byte[] signedMessage = null;
        List<X509Certificate> certList = new ArrayList<X509Certificate>();
        CMSTypedData cmsData = new CMSProcessableByteArray(data);
        certList.addAll(certificateChain);
        certList.add(signingCertificate);
        
        // System.out.println("Number of certificates: " + certList.size());

        Store certs = new JcaCertStore(certList);
        CMSSignedDataGenerator cmsGenerator = new CMSSignedDataGenerator();

        String signatureAlgorithm = config.getSignatureAlgorithm();
        ContentSigner contentSigner = new JcaContentSignerBuilder(signatureAlgorithm).build(signingKey);

        cmsGenerator.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(
                new JcaDigestCalculatorProviderBuilder().setProvider("BC").build())
                                                    .build(contentSigner, signingCertificate));
        cmsGenerator.addCertificates(certs);
        CMSSignedData cms = cmsGenerator.generate(cmsData, false);
        signedMessage = cms.getEncoded();
        return signedMessage;
    }

        /**
         * Verfies thhe signed data
         * @param signedData
         * @return
         * @throws CMSException
         * @throws IOException
         * @throws OperatorCreationException
         * @throws CertificateException
         */
    public boolean verifSignData(final byte[] signedData) throws CMSException, IOException, OperatorCreationException, CertificateException {
        // TODO remove this or pass in the certs
        ByteArrayInputStream bIn = new ByteArrayInputStream(signedData);
        ASN1InputStream aIn = new ASN1InputStream(bIn);
        CMSSignedData s = new CMSSignedData(ContentInfo.getInstance(aIn.readObject()));
        aIn.close();
        bIn.close();
        Store certs = s.getCertificates();
        SignerInformationStore signers = s.getSignerInfos();
        Collection<SignerInformation> c = signers.getSigners();
        SignerInformation signer = c.iterator().next();
        Collection<X509CertificateHolder> certCollection = certs.getMatches(signer.getSID());
        Iterator<X509CertificateHolder> certIt = certCollection.iterator();
        X509CertificateHolder certHolder = certIt.next();
        boolean verifResult = signer.verify(new JcaSimpleSignerInfoVerifierBuilder().build(certHolder));
        if (!verifResult) {
            return false;
        }
        return true;
    }

    // TODO remove this encrypter if we don't end up using it
    public byte[] encryptData(final byte[] data, X509Certificate encryptionCertificate)
            throws CertificateEncodingException, CMSException, IOException {
        byte[] encryptedData = null;
        if (null != data && null != encryptionCertificate) {
            CMSEnvelopedDataGenerator cmsEnvelopedDataGenerator = new CMSEnvelopedDataGenerator();
            JceKeyTransRecipientInfoGenerator jceKey = new JceKeyTransRecipientInfoGenerator(encryptionCertificate);
            cmsEnvelopedDataGenerator.addRecipientInfoGenerator(jceKey);
            CMSTypedData msg = new CMSProcessableByteArray(data);
            OutputEncryptor encryptor = new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES128_CBC).setProvider("BC").build();
            CMSEnvelopedData cmsEnvelopedData = cmsEnvelopedDataGenerator.generate(msg, encryptor);
            encryptedData = cmsEnvelopedData.getEncoded();
        }
        return encryptedData;
    }

    public byte[] decryptData(final byte[] encryptedData, final PrivateKey decryptionKey) throws CMSException {
        byte[] decryptedData = null;
        if (null != encryptedData && null != decryptionKey) {
            CMSEnvelopedData envelopedData = new CMSEnvelopedData(encryptedData);
            Collection<RecipientInformation> recip = envelopedData.getRecipientInfos().getRecipients();
            KeyTransRecipientInformation recipientInfo = (KeyTransRecipientInformation) recip.iterator().next();
            JceKeyTransRecipient recipient = new JceKeyTransEnvelopedRecipient(decryptionKey);
            decryptedData = recipientInfo.getContent(recipient);
        }
        return decryptedData;
    }

    // For testing only
    public static byte[] readFromFile(String name) {
        Path dir = Paths.get("/Users/cwerner/um/src/lei/");
        Path path = dir.resolve(name);
        try {
            byte[] bytes = Files.readAllBytes(path);
            return bytes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // For testing only
    public static void writeToFile(String name, byte[] bytes) {
        Path dir = Paths.get("/Users/cwerner/um/src/lei/");
        Path path = dir.resolve(name);
        try {
            Files.write(path, bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // for testing only
    public static ByteArrayInputStream forkToFile(String name, InputStream input) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[2048]; // you can configure the buffer size
            int length;
            while ((length = input.read(buffer)) != -1) out.write(buffer, 0, length); //copy streams
            input.close(); // call this in a finally block
            byte[] result = out.toByteArray();
            writeToFile(name, result);
            return new ByteArrayInputStream(result);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
