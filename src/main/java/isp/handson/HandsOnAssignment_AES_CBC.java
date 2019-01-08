package isp.handson;

import fri.isp.Agent;
import fri.isp.Environment;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

/**
 * AES in CBC mode requires a key and initialization vector IV (16 bytes)
 * Every block is dependent on the block before it, as it is xor-ed with it together with the key (16 bytes)
 * The first block is xor-ed with the IV and the key
 *
 * Padding has no security implications so just use PKCS5Padding
 * If the input is not a multiple of 16 bytes padding is required!
 *
 * In CBC (and CTR mode), you have to also
 * send the IV. The IV can be accessed via the
 * encryption.getIV() call
 */
public class HandsOnAssignment_AES_CBC {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        final Environment env = new Environment();

        final SecretKey key = KeyGenerator.getInstance("AES").generateKey();

        env.add(new Agent("alice") {
            @Override
            public void task() throws Exception {
                final byte[] plaintext = "Hey Bob, it's Alice".getBytes(StandardCharsets.UTF_8);

                System.out.println("PT: " + Agent.hex(plaintext));

                /*
                    If the plaintext is a multiple of 16 bytes, padding is not needed, but since it has no security implications
                    just use PKCS5Padding and you'll be fine
                 */
                Cipher encryption = Cipher.getInstance("AES/CBC/PKCS5Padding");

                encryption.init(Cipher.ENCRYPT_MODE, key);

                // The IV was created by the init function, but we can create it ourselves, there's just no need
                final byte[] iv = encryption.getIV();

                byte[] ciphertext = encryption.doFinal(plaintext);

                System.out.println("CT: " + Agent.hex(ciphertext));

                /*
                    The attack used in CTR mode does not work here, even if the attacker knows the part of the plaintext,
                    because every change in a block will affect every block after it -> the decrypted ciphertext will result in gibberish
                    The recipient however has no idea that the ciphertext was tampered with, since no authentication was provided

                    CBC mode is susceptible to an attack known as padding oracle attacks (https://www.limited-entropy.com/padding-oracle-attacks/)
                 */
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

                Cipher encryption = Cipher.getInstance("AES/CBC/PKCS5Padding");

                // An IvParameterSpec must be constructed to decrypt the message -> the decryption algorithm needs to know the starting value
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

    public static byte[] simulateAttack(byte[] ciphertext) {
        byte[] plaintext = "Hey Bob, it's Alice".getBytes(StandardCharsets.UTF_8);

        ciphertext[0] = (byte) (ciphertext[0] ^ plaintext[0] ^ (byte) 'D');
        ciphertext[1] = (byte) (ciphertext[1] ^ plaintext[1] ^ (byte) 'i');
        ciphertext[2] = (byte) (ciphertext[2] ^ plaintext[2] ^ (byte) 'e');

        return ciphertext;
    }
}
