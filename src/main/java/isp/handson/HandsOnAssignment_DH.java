package isp.handson;

import fri.isp.Agent;
import fri.isp.Environment;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

/**
 * The basic idea behind Diffie-Hellman (very simplified):
 *      (https://security.stackexchange.com/questions/45963/diffie-hellman-key-exchange-in-plain-english)
 *
 *      1. I come up with two prime numbers g and p and tell you what they are.
 *
 *      2. You then pick a secret number (a), but you don't tell anyone. Instead you compute g*a mod(p) and send that result
 *      back to me. (We'll call that A since it came from a).
 *
 *      3. I do the same thing, but we'll call my secret number b and the computed number B. So I compute g*b mod(p) and
 *      send you the result (called B)
 *
 *      4. Now, you take the number I sent you and do the exact same operation with it. So that's B*a mod(p).
 *
 *      5. I do the same operation with the result you sent me, so: A*b mod(p).
 *
 * The results:
 *
 *      (g*a mod (p))* b (mod p) = g*a*b mod(p) = SECRET
 *      (g*b mod (p))* a (mod p) = g*b*a mod(p) = SECRET
 *
 * Public key: A = g^a
 * Private key: a
 */
public class HandsOnAssignment_DH {
    public static void main(String[] args) {
        final Environment env = new Environment();

        env.add(new Agent("alice") {
            @Override
            public void task() throws Exception {
                final KeyPairGenerator kpg = KeyPairGenerator.getInstance("DH");

                kpg.initialize(2048);

                final KeyPair keyPair = kpg.generateKeyPair();

                byte[] publicKey = keyPair.getPublic().getEncoded();

                print("Alice's contribution to DH: %s", hex(publicKey));

                send("bob", publicKey);

                /*
                    Receive public key from Bob

                    The key exchange is now finished, both entities have all the values they require
                 */

                byte[] receivedPublicKey = receive("bob");

                final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(receivedPublicKey);
                final DHPublicKey bobPublicKey = (DHPublicKey) KeyFactory.getInstance("DH").generatePublic(keySpec);

                final KeyAgreement dh = KeyAgreement.getInstance("DH");

                dh.init(keyPair.getPrivate());

                dh.doPhase(bobPublicKey, true);

                final byte[] sharedSecret = dh.generateSecret();
                print("Shared secret: %s", hex(sharedSecret));

                /*
                    By default the shared secret will be 32 bytes long, but our cipher requires keys of length 16 bytes
                    IMPORTANT: It is better not to create the key directly from the shared secret, but derive it using a
                    key derivation function
                 */
                final SecretKeySpec aesKey = new SecretKeySpec(KeyDerivation.deriveKey(sharedSecret), "AES");

                final Cipher aes = Cipher.getInstance("AES/GCM/NoPadding");
                aes.init(Cipher.ENCRYPT_MODE, aesKey);

                final byte[] ct = aes.doFinal("Hey Bob, this is Alice".getBytes(StandardCharsets.UTF_8));
                final byte[] iv = aes.getIV();

                send("bob", iv);
                send("bob", ct);
            }
        });

        env.add(new Agent("bob") {
            @Override
            public void task() throws Exception {
                byte[] receivedPublicKey = receive("alice");

                final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(receivedPublicKey);

                final DHPublicKey alicePublicKey = (DHPublicKey) KeyFactory.getInstance("DH").generatePublic(keySpec);

                final DHParameterSpec dhParamSpec = alicePublicKey.getParams();

                final KeyPairGenerator kpg = KeyPairGenerator.getInstance("DH");

                kpg.initialize(dhParamSpec);

                final KeyPair keyPair = kpg.generateKeyPair();

                byte[] publicKey = keyPair.getPublic().getEncoded();

                print("Bob's contribution to DH: %s", hex(publicKey));

                send("alice", publicKey);


                /*
                    The key exchange is now finished, both entities have all the values they require
                 */

                final KeyAgreement dh = KeyAgreement.getInstance("DH");

                dh.init(keyPair.getPrivate());

                dh.doPhase(alicePublicKey, true);

                final byte[] sharedSecret = dh.generateSecret();

                print("Shared secret: %s", hex(sharedSecret));

                /*
                    By default the shared secret will be 32 bytes long, but our cipher requires keys of length 16 bytes
                    IMPORTANT: It is better not to create the key directly from the shared secret, but derive it using a
                    key derivation function
                 */
                final SecretKeySpec aesKey = new SecretKeySpec(KeyDerivation.deriveKey(sharedSecret), "AES");

                final Cipher aes = Cipher.getInstance("AES/GCM/NoPadding");

                final byte[] iv = receive("alice");
                final byte[] ct = receive("alice");

                aes.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(128, iv));

                final byte[] pt = aes.doFinal(ct);

                print("Bob received: %s", new String(pt, StandardCharsets.UTF_8));
            }
        });

        env.connect("alice", "bob");
        env.start();
    }


}
