package isp.handson;

import fri.isp.Agent;
import fri.isp.Environment;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * This turns a block cipher into a stream cipher basically, therefore no padding is required
 * Output cipher text is the same length as the plain text
 * Always use the same key and IV pair ONCE! If the pair is used multiple times, we are vulnerable to a two-time pad attack
 *
 * If we know the plain text we can easily and predictably change the cipher text by flipping bits (as seen in the man-in-the-middle homework exercise)
 *
 * In CBC (and CTR mode), you have to also
 * send the IV. The IV can be accessed via the
 * encryption.getIV() call
 */
public class HandsOnAssignment_AES_CTR {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        final Environment env = new Environment();

        final SecretKey key = KeyGenerator.getInstance("AES").generateKey();

        env.add(new Agent("alice") {
            @Override
            public void task() throws Exception {
                final byte[] plaintext = "Hey Bob, it's Alice".getBytes(StandardCharsets.UTF_8);

                System.out.println("PT: " + Agent.hex(plaintext));

                Cipher encryption = Cipher.getInstance("AES/CTR/NoPadding");

                encryption.init(Cipher.ENCRYPT_MODE, key);

                // We should let the cipher init process create the IV, which will give us a higher chance of randomness
                final byte[] iv = encryption.getIV();

                byte[] ciphertext = encryption.doFinal(plaintext);

                System.out.println("CT: " + Agent.hex(ciphertext));

                // ciphertext = simulateAttack(ciphertext);

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

                Cipher encryption = Cipher.getInstance("AES/CTR/NoPadding");

                // CTR requires an IvParameterSpec to be added to the init function
                IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

                encryption.init(Cipher.DECRYPT_MODE, key, ivParameterSpec);

                final byte[] plaintext = encryption.doFinal(ciphertext);

                System.out.println("PT: " + Agent.hex(plaintext));

                print(new String(plaintext, StandardCharsets.UTF_8));
            }
        });

        env.connect("alice", "bob");
        env.start();
    }

    /**
     * This function shows the vulnerability of CTR mode which has no authentication
     * If an attacker knows the part of the original plaintext message, they can very easily and predictably
     * change the plaintext even without knowing the key -> this is because it is basically a stream cipher with
     * a known length
     *
     * @param ciphertext
     * @return
     */
    public static byte[] simulateAttack(byte[] ciphertext) {
        byte[] plaintext = "Hey Bob, it's Alice".getBytes(StandardCharsets.UTF_8);

        ciphertext[0] = (byte) (ciphertext[0] ^ plaintext[0] ^ (byte) 'D');
        ciphertext[1] = (byte) (ciphertext[1] ^ plaintext[1] ^ (byte) 'i');
        ciphertext[2] = (byte) (ciphertext[2] ^ plaintext[2] ^ (byte) 'e');

        return ciphertext;
    }
}