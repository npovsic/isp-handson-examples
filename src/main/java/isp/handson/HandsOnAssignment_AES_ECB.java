package isp.handson;

import fri.isp.Agent;
import fri.isp.Environment;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

/**
 * This example uses AES in ECB mode
 * This mode is only suitable for small amounts of data which are smaller than 16 bytes since it uses the same key for
 * every block without an IV
 */
public class HandsOnAssignment_AES_ECB {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        final Environment env = new Environment();

        final SecretKey key = KeyGenerator.getInstance("AES").generateKey();

        env.add(new Agent("alice") {
            @Override
            public void task() throws Exception {
                // Plaintext is exactly 32 bytes long
                final byte[] plaintext = "This will repeatThis will repeat".getBytes(StandardCharsets.UTF_8);

                System.out.println("PT: " + Agent.hex(plaintext));

                /*
                    Since the plaintext is a multiple of 16 bytes no padding is needed
                    Only used as a demonstration of ECB shortcomings
                 */
                Cipher encryption = Cipher.getInstance("AES/ECB/NoPadding");

                encryption.init(Cipher.ENCRYPT_MODE, key);

                byte[] ciphertext = encryption.doFinal(plaintext);

                System.out.println("CT: " + Agent.hex(ciphertext));

                send("bob", ciphertext);
            }
        });

        env.add(new Agent("bob") {
            @Override
            public void task() throws Exception {
                final byte[] ciphertext = receive("alice");

                /*
                    Observe the ciphertext when it is encrypted in ECB mode and repeats

                    The encryption for "This will repeatThis will repeat" is:

                        First block                      Second block
                        49367445AEEC6CDA8E18AD72FC43425A 49367445AEEC6CDA8E18AD72FC43425A

                    As you can see, they are exactly the same
                    Of course this only works if the data inside two blocks is identical, however it is still a large
                    security concern
                 */

                Cipher encryption = Cipher.getInstance("AES/ECB/NoPadding");

                encryption.init(Cipher.DECRYPT_MODE, key);

                byte[] plaintext = encryption.doFinal(ciphertext);

                System.out.println("PT: " + Agent.hex(plaintext));

                print(new String(plaintext, StandardCharsets.UTF_8));
            }
        });

        env.connect("alice", "bob");
        env.start();
    }
}
