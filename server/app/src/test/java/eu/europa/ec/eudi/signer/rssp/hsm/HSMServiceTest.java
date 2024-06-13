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

package eu.europa.ec.eudi.signer.rssp.hsm;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.spec.RSAPublicKeySpec;
import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest(classes = { HSMService.class })
@RunWith(SpringJUnit4ClassRunner.class)
public class HSMServiceTest {
    // The aim of this class is to test that the implementation will be able to
    // respond to multiple threads.
    // Not every function in the HSMService will be tested

    @Autowired
    HSMService hsmService;

    @Test
    public void testMultiThreadToCreateRSAKeys() throws Exception {

        int numberOfThreads = 300;
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // HSMService hsmService = new HSMService();
        byte[] secretKey = hsmService.initSecretKey();
        try {
            SecretKey sk = new SecretKeySpec(secretKey, "AES");
            Assert.assertNotNull(sk);
        } catch (Exception e) {
            Assert.assertNull(e);
        }
        Assert.assertNotNull(secretKey);
        Assert.assertEquals(32, secretKey.length);

        final byte[][] public_key_modulus = new byte[numberOfThreads][];
        final byte[][] public_key_public_exponent = new byte[numberOfThreads][];
        final byte[][] private_key_wrapped = new byte[numberOfThreads][];

        for (int i = 0; i < numberOfThreads; i++) {
            final int index = i;
            service.submit(() -> {
                try {
                    byte[][] rsaKeyPair = this.hsmService.generateRSAKeyPair(2048);
                    Assert.assertNotNull(rsaKeyPair[0]);
                    private_key_wrapped[index] = rsaKeyPair[0];
                    Assert.assertNotNull(rsaKeyPair[1]);
                    public_key_modulus[index] = rsaKeyPair[1];
                    Assert.assertNotNull(rsaKeyPair[2]);
                    public_key_public_exponent[index] = rsaKeyPair[2];
                } catch (Exception e) {
                    Assert.assertNull(e);
                }
                latch.countDown();
            });
        }

        latch.await();

        for (int i = 0; i < numberOfThreads; i++) {
            BigInteger p_e_BI = new BigInteger(1, public_key_public_exponent[i]);
            Assert.assertEquals(65537, p_e_BI.intValue());
        }

        for (int i = 0; i < numberOfThreads; i++) {
            try {
                BigInteger modulusBI = new BigInteger(1, public_key_modulus[i]);
                BigInteger publicExponentBI = new BigInteger(1, public_key_public_exponent[i]);

                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                RSAPublicKeySpec pKeySpec = new RSAPublicKeySpec(modulusBI, publicExponentBI);
                RSAPublicKey pk = (RSAPublicKey) keyFactory.generatePublic(pKeySpec);
                Assert.assertNotNull(pk);
            } catch (Exception e) {
                Assert.assertNull(e);
            }
        }
    }
}
