package isp.handson;

import fri.isp.Agent;
import fri.isp.Environment;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * This is an implementation of AES using GCM mode -> Authenticated encryption
 * This type of cipher requires an IV that is preferably 12 bytes long (https://crypto.stackexchange.com/questions/41601/aes-gcm-recommended-iv-size-why-12-bytes/41610)
 *
 * Padding has no effect here, since GCM uses CTR internally (https://crypto.stackexchange.com/questions/26783/ciphertext-and-tag-size-and-iv-transmission-with-aes-in-gcm-mode/26787#26787?newreg=3942556a7f664d16a466b114e0fc993e)
 * The random IV must also be sent alongside the cipher text
 *
 * Because this is authenticated encryption, the authentication tags (16 bytes) will be sent alongside the message
 * The decryption automatically checks the tag and throws an error if it fails
 *
 * Authenticated encryption provides protection against padding oracle attacks (https://www.limited-entropy.com/padding-oracle-attacks/)
 */
public class HandsOnAssignment_AES_GCM {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        final Environment env = new Environment();

        final SecretKey key = KeyGenerator.getInstance("AES").generateKey();

        env.add(new Agent("alice") {
            @Override
            public void task() throws Exception {
                // This is used to create a random sequence of bytes that is much safer than just Random
                SecureRandom secureRandom = new SecureRandom();

                final byte[] plaintext = "Hey Bob, it's Alice".getBytes(StandardCharsets.UTF_8);

                System.out.println("PT: " + Agent.hex(plaintext));

                // Create a random IV, that is used by the parameter spec
                byte[] iv = new byte[12];

                // SecureRandom gives better random values than the Random class, use it to randomize the IV
                secureRandom.nextBytes(iv);

                // Create the cipher
                Cipher encryption = Cipher.getInstance("AES/GCM/NoPadding");

                // The GCMParameterSpec sets the tag length and the used IV
                GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);

                encryption.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

                final byte[] ciphertext = encryption.doFinal(plaintext);

                System.out.println("IV: " + Agent.hex(iv));
                System.out.println("CT: " + Agent.hex(ciphertext));

                // The IV may be sent unencrypted and over an insecure channel
                send("bob", iv);

                send("bob", ciphertext);
            }
        });

        env.add(new Agent("bob") {
            @Override
            public void task() throws Exception {
                final byte[] iv = receive("alice");
                final byte[] ciphertext = receive("alice");

                GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);

                Cipher encryption = Cipher.getInstance("AES/GCM/NoPadding");

                encryption.init(Cipher.DECRYPT_MODE, key, parameterSpec);

                final byte[] plaintext = encryption.doFinal(ciphertext);

                System.out.println("PT: " + Agent.hex(plaintext));

                print(new String(plaintext, StandardCharsets.UTF_8));
            }
        });

        env.connect("alice", "bob");
        env.start();
    }
}
