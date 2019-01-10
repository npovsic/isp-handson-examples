package isp.handson;

import fri.isp.Agent;
import fri.isp.Environment;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHPublicKey;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;

/**
 * This is an example of how an entity could share a secret key with other entities using RSA
 */
public class HandsOnAssignment_MultiParty_RSA {
    public static void main(String[] args) {
        final Environment env = new Environment();

        env.add(new Agent("alice") {
            @Override
            public void task() throws Exception {
                final SecretKey aesKey = KeyGenerator.getInstance("AES").generateKey();

                final KeyPair aliceKP = KeyPairGenerator.getInstance("RSA").generateKeyPair();

                final byte[] bobEncodedPublicKey = receive("bob");

                final X509EncodedKeySpec keySpecBob = new X509EncodedKeySpec(bobEncodedPublicKey);
                final PublicKey bobPublicKey = KeyFactory.getInstance("RSA").generatePublic(keySpecBob);

                final byte[] carolEncodedPublicKey = receive("carol");

                final X509EncodedKeySpec keySpecCarol = new X509EncodedKeySpec(carolEncodedPublicKey);
                final PublicKey carolPublicKey = KeyFactory.getInstance("RSA").generatePublic(keySpecCarol);

                /*
                    Send Alice's public key to Bob and Carol
                 */

                send("bob", aliceKP.getPublic().getEncoded());
                send("carol", aliceKP.getPublic().getEncoded());

                /*
                    Create encryption
                 */

                Cipher encryption = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");

                /*
                    Initialize encryption for Bob and send him the symmetric key
                 */

                encryption.init(Cipher.ENCRYPT_MODE, bobPublicKey);

                byte[] encryptedKey = encryption.doFinal(aesKey.getEncoded());

                send("bob", encryptedKey);

                /*
                    Initialize encryption for Bob and send him the symmetric key
                 */

                encryption.init(Cipher.ENCRYPT_MODE, carolPublicKey);

                encryptedKey = encryption.doFinal(aesKey.getEncoded());

                send("carol", encryptedKey);

                // Now we can continue with symmetric encryption
            }
        });

        env.add(new Agent("bob") {
            @Override
            public void task() throws Exception {
                final KeyPair bobKP = KeyPairGenerator.getInstance("RSA").generateKeyPair();

                send("alice", bobKP.getPublic().getEncoded());

                final byte[] aliceEncodedPublicKey = receive("alice");

                final X509EncodedKeySpec keySpecAlice = new X509EncodedKeySpec(aliceEncodedPublicKey);
                final PublicKey alicePublicKey = KeyFactory.getInstance("RSA").generatePublic(keySpecAlice);

                /*
                    Get the secret key which Alice encrypted with Bob's public key
                 */

                final byte[] encryptedKey = receive("alice");

                final Cipher encryption = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");

                encryption.init(Cipher.DECRYPT_MODE, bobKP.getPrivate());

                byte[] secretKey = encryption.doFinal(encryptedKey);

                System.out.println("Secret key: " + Agent.hex(secretKey));

                // Now we can continue with symmetric encryption
            }
        });

        env.add(new Agent("carol") {
            @Override
            public void task() throws Exception {
                final KeyPair carolKP = KeyPairGenerator.getInstance("RSA").generateKeyPair();

                send("alice", carolKP.getPublic().getEncoded());

                final byte[] aliceEncodedPublicKey = receive("alice");

                final X509EncodedKeySpec keySpecAlice = new X509EncodedKeySpec(aliceEncodedPublicKey);
                final PublicKey alicePublicKey = KeyFactory.getInstance("RSA").generatePublic(keySpecAlice);

                /*
                    Get the secret key which Alice encrypted with Carol's public key
                 */

                final byte[] encryptedKey = receive("alice");

                final Cipher encryption = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");

                encryption.init(Cipher.DECRYPT_MODE, carolKP.getPrivate());

                byte[] secretKey = encryption.doFinal(encryptedKey);

                System.out.println("Secret key: " + Agent.hex(secretKey));

                // Now we can continue with symmetric encryption

            }
        });

        env.connect("alice", "bob");
        env.connect("bob", "carol");
        env.connect("carol", "alice");

        env.start();
    }


}
