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

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.*;
import org.bouncycastle.openssl.bc.BcPEMDecryptorProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcePEMEncryptorBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.pkcs.jcajce.JcePKCSPBEInputDecryptorProviderBuilder;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class PemConverter {

    private PEMEncryptor pemEncryptor = null;
    private InputDecryptorProvider inputDecryptorProvider = null;
    private BcPEMDecryptorProvider bcPEMDecryptorProvider = null;
    private boolean encryptionEnabled = false;

    public PemConverter(CryptoConfig config) {
        char[] passPhraseChars = config.getPassphrase().toCharArray();
        // Make sure the BC Provider is available
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        if (passPhraseChars != null && passPhraseChars.length > 0) {
            pemEncryptor = (new JcePEMEncryptorBuilder("AES-256-CFB")).build(passPhraseChars);
            JcePKCSPBEInputDecryptorProviderBuilder builder
                    = new JcePKCSPBEInputDecryptorProviderBuilder().setProvider("BC");
            inputDecryptorProvider = builder.build(passPhraseChars);
            bcPEMDecryptorProvider = new BcPEMDecryptorProvider(passPhraseChars);
            encryptionEnabled = true;
        }
    }

    public String certificateToString(X509Certificate certificate) throws IOException {
        return objectToPEM(certificate);
    }

    private String objectToPEM(Object object) throws IOException {
        try (StringWriter sw = new StringWriter();
             JcaPEMWriter pemWriter = new JcaPEMWriter(sw)) {
            pemWriter.writeObject(object);
            pemWriter.flush();
            return sw.toString();
        }
    }

    public X509Certificate stringToCertificate(String certificateString) throws IOException, CertificateException {
        try (StringReader stringReader = new StringReader(certificateString); PEMParser pemParser = new PEMParser(stringReader)) {
            Object object = pemParser.readObject();
            final X509Certificate certificate = new JcaX509CertificateConverter().getCertificate((X509CertificateHolder) object);
            return certificate;
        }
    }

    public String publicKeyToString(PublicKey publicKey) throws IOException {
        return objectToPEM(publicKey);
    }

    public PublicKey stringToPublicKey(String keyString) throws IOException {
        try (StringReader keyReader = new StringReader(keyString);
            PEMParser pemParser = new PEMParser(keyReader)) {
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(pemParser.readObject());
            return converter.getPublicKey(publicKeyInfo);
        }
    }

    public String privateKeyToString(PrivateKey privateKey)
            throws IOException
    {
        try (StringWriter sw = new StringWriter();
        JcaPEMWriter pemWriter = new JcaPEMWriter(sw)) {
            if (pemEncryptor != null) {
                pemWriter.writeObject(privateKey, pemEncryptor);
            }
            else {
                pemWriter.writeObject(privateKey);
            }

            pemWriter.flush();
            pemWriter.close();
            return sw.toString();
        }
    }

    public PrivateKey stringToPrivateKey(String privateKeyPemString) throws IOException, PKCSException {
        PrivateKeyInfo pki;
        try (PEMParser pemParser = new PEMParser(new StringReader(privateKeyPemString))) {
            Object o = pemParser.readObject();
            if (o instanceof PKCS8EncryptedPrivateKeyInfo) {
                if (!encryptionEnabled) {
                    throw new IllegalStateException("Expected pass-phrase to decrypt the private key");
                }
                PKCS8EncryptedPrivateKeyInfo epki = (PKCS8EncryptedPrivateKeyInfo) o;
                pki = epki.decryptPrivateKeyInfo(inputDecryptorProvider);
            }
            else if (o instanceof PEMEncryptedKeyPair) {
                if (!encryptionEnabled) {
                    throw new IllegalStateException("Expected pass-phrase to decrypt the private key");
                }
                PEMEncryptedKeyPair epki = (PEMEncryptedKeyPair) o;
                PEMKeyPair pkp = epki.decryptKeyPair(bcPEMDecryptorProvider);
                pki = pkp.getPrivateKeyInfo();
            }
            else if (o instanceof PEMKeyPair) {
                if (encryptionEnabled) {
                    throw new IllegalStateException("Expected private key to be encrypted!");
                }
                pki = ((PEMKeyPair)o).getPrivateKeyInfo();
            } else {
                throw new IllegalArgumentException("Invalid encrypted private key class: " + o.getClass().getName());
            }

            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            return converter.getPrivateKey(pki);
        }
    }
}
