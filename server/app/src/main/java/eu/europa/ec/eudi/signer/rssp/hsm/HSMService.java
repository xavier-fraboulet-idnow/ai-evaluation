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

// This Service will use the library JacknJi11 available at https://github.com/devisefutures/jacknji11
// to make requests to a HSM.

package eu.europa.ec.eudi.signer.rssp.hsm;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import org.pkcs11.jacknji11.*;
import org.springframework.stereotype.Component;

@Component
public class HSMService {

    private byte[] secretKey;
    private HSMInformation hsmInfo;

    public HSMService() {

        // Load test_slot from global variable
        long slot = 0;
        String testSlotEnv = System.getenv("JACKNJI11_TEST_TESTSLOT");
        if (testSlotEnv != null && testSlotEnv.length() > 0) {
            slot = Long.parseLong(testSlotEnv);
        }

        // Load user_pin from global variable
        byte[] pin = "userpin".getBytes();
        String userPinEnv = System.getenv("JACKNJI11_TEST_USER_PIN");
        if (userPinEnv != null && userPinEnv.length() > 0) {
            pin = userPinEnv.getBytes();
        }

        this.hsmInfo = new HSMInformation(slot, pin);
        CE.Initialize();
    }

    // Creates a new Secret Key that will be use for the operation of wrap and
    // unwrap:
    public byte[] initSecretKey() throws Exception {
        LongRef sessionRef = this.hsmInfo.getSession();
        long session = sessionRef.value();

        long secretKeyWrap = CE.GenerateKey(session, new CKM(CKM.AES_KEY_GEN),
                new CKA(CKA.VALUE_LEN, 32),
                new CKA(CKA.LABEL, "wrapKey"),
                new CKA(CKA.ID, "wrapKey"),
                new CKA(CKA.TOKEN, false),
                new CKA(CKA.SENSITIVE, false),
                new CKA(CKA.EXTRACTABLE, true),
                new CKA(CKA.DERIVE, true));
        byte[] secret_key = CE.GetAttributeValue(session, secretKeyWrap, CKA.VALUE).getValue();
        this.secretKey = secret_key;

        CE.DestroyObject(session, secretKeyWrap);
        this.hsmInfo.releaseSession(sessionRef);
        return secret_key;
    }

    public void setSecretKey(byte[] secretKeyBytes) throws Exception {
        LongRef sessionRef = this.hsmInfo.getSession();
        long session = sessionRef.value();

        CKA[] secretTempl = new CKA[] {
                new CKA(CKA.CLASS, CKO.SECRET_KEY),
                new CKA(CKA.KEY_TYPE, CKK.AES),
                new CKA(CKA.VALUE, secretKeyBytes),
                new CKA(CKA.LABEL, "wrapKey"),
                new CKA(CKA.ID, "wrapKey"),
                new CKA(CKA.TOKEN, false),
                new CKA(CKA.SENSITIVE, false),
                new CKA(CKA.EXTRACTABLE, true),
                new CKA(CKA.DERIVE, true)
        };
        long obj = CE.CreateObject(session, secretTempl);
        this.secretKey = secretKeyBytes;

        CE.DestroyObject(session, obj);
        this.hsmInfo.releaseSession(sessionRef);
    }

    // loads the secret key from the bytes for the current session
    public long loadSecretKey(long session, byte[] secretKeyBytes) throws Exception {
        CKA[] secretTempl = new CKA[] {
                new CKA(CKA.CLASS, CKO.SECRET_KEY),
                new CKA(CKA.KEY_TYPE, CKK.AES),
                new CKA(CKA.VALUE, secretKeyBytes),
                new CKA(CKA.LABEL, "wrapKey"),
                new CKA(CKA.ID, "wrapKey"),
                new CKA(CKA.TOKEN, false),
                new CKA(CKA.SENSITIVE, false),
                new CKA(CKA.EXTRACTABLE, true),
                new CKA(CKA.DERIVE, true)
        };
        long obj = CE.CreateObject(session, secretTempl);
        return obj;
    }

    /**
     * Function that generates a RSA key pair, and returns its ref in an array.
     * The first position of the array contains the private key bytes.
     * The second position of the array contains the public key modulus bytes.
     * The third position of the array contains the public key public_exponent
     * bytes.
     */
    public byte[][] generateRSAKeyPair(int keySize) throws Exception {
        LongRef sessionRef = this.hsmInfo.getSession();
        long session = sessionRef.value();

        long secretKeyObj = loadSecretKey(session, this.secretKey);

        CKA[] pubTempl = new CKA[] {
                new CKA(CKA.MODULUS_BITS, keySize),
                new CKA(CKA.PUBLIC_EXPONENT, Hex.s2b("010001")),
                new CKA(CKA.WRAP, true),
                new CKA(CKA.VERIFY, true),
                new CKA(CKA.TOKEN, true),
                new CKA(CKA.LABEL, "labelrsa-public"),
                new CKA(CKA.ID, "labelrsa3")
        };

        CKA[] privTempl = new CKA[] {
                new CKA(CKA.TOKEN, true),
                new CKA(CKA.PRIVATE, true),
                new CKA(CKA.SENSITIVE, true),
                new CKA(CKA.SIGN, true),
                new CKA(CKA.UNWRAP, true),
                new CKA(CKA.EXTRACTABLE, true),
                new CKA(CKA.LABEL, "labelrsa-private"),
                new CKA(CKA.ID, "labelrsa3"),
        };

        LongRef pubKey = new LongRef();
        LongRef privKey = new LongRef();
        CE.GenerateKeyPair(session, new CKM(CKM.RSA_PKCS_KEY_PAIR_GEN), pubTempl, privTempl, pubKey, privKey);

        byte[][] keyPair = new byte[3][];
        keyPair[0] = CE.WrapKey(session, new CKM(CKM.AES_CBC), secretKeyObj, privKey.value());
        keyPair[1] = CE.GetAttributeValue(session, pubKey.value(), CKA.MODULUS).getValue();
        keyPair[2] = CE.GetAttributeValue(session, pubKey.value(), CKA.PUBLIC_EXPONENT).getValue();

        CE.DestroyObject(session, secretKeyObj);
        this.hsmInfo.releaseSession(sessionRef);
        return keyPair;
    }

    public long UnwrapKey(long session, long secretKey, byte[] wrappedKey) {

        CKA[] secTemplUnwrap = new CKA[] {
                new CKA(CKA.CLASS, CKO.PRIVATE_KEY),
                new CKA(CKA.KEY_TYPE, CKK.RSA),
                new CKA(CKA.LABEL, "privatekeyunwrapped"),
                new CKA(CKA.ID, "privatekeyunwrapped"),
                new CKA(CKA.TOKEN, true),
                new CKA(CKA.SENSITIVE, true),
                new CKA(CKA.EXTRACTABLE, true),
                new CKA(CKA.SIGN, true),
        };
        return CE.UnwrapKey(session, new CKM(CKM.AES_CBC), secretKey, wrappedKey, secTemplUnwrap);
    }

    public byte[] signDTBSwithRSAPKCS11(byte[] wrappedPrivateKey, byte[] DTBSR) throws Exception {
        // init session
        LongRef sessionRef = this.hsmInfo.getSession();
        long session = sessionRef.value();

        long secretKeyObj = loadSecretKey(session, this.secretKey);

        // Unwrap private key
        long privateKey = UnwrapKey(session, secretKeyObj, wrappedPrivateKey);

        // Sign bytes
        CE.SignInit(session, new CKM(CKM.SHA256_RSA_PKCS), privateKey);
        byte[] signed = CE.Sign(session, DTBSR);

        CE.DestroyObject(session, secretKeyObj);
        this.hsmInfo.releaseSession(sessionRef);
        return signed;
    }

    public long SetAttributePublicKey(long session, byte[] publicKey) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec pKeySpec = new X509EncodedKeySpec(publicKey);
        RSAPublicKey pk = (RSAPublicKey) keyFactory.generatePublic(pKeySpec);

        CKA[] pubTempl = new CKA[] {
                new CKA(CKA.CLASS, CKO.PUBLIC_KEY),
                new CKA(CKA.KEY_TYPE, CKK.RSA),
                new CKA(CKA.MODULUS, pk.getModulus().toByteArray()),
                new CKA(CKA.PUBLIC_EXPONENT, pk.getModulus().toByteArray()),
                new CKA(CKA.WRAP, true),
                new CKA(CKA.VERIFY, true),
                new CKA(CKA.TOKEN, true),
                new CKA(CKA.LABEL, "labelrsa-publicloaded"),
                new CKA(CKA.ID, "labelrsa-publicloaded")
        };
        return CE.CreateObject(session, pubTempl);
    }

    public void VerifySignature(byte[] DTBSR, byte[] signature, byte[] publicKey) throws Exception {
        // init session
        LongRef sessionRef = this.hsmInfo.getSession();
        long session = sessionRef.value();

        // Get Public Key Parameters
        long publicKeyValue = 0;
        publicKeyValue = SetAttributePublicKey(session, publicKey);

        // Verify Signature
        CE.VerifyInit(session, new CKM(CKM.SHA256_RSA_PKCS), publicKeyValue);
        CE.Verify(session, DTBSR, signature);

        this.hsmInfo.CloseSession(sessionRef);
    }

}
