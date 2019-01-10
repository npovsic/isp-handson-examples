package isp.handson;

import fri.isp.Agent;
import fri.isp.Environment;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import java.nio.charset.StandardCharsets;
import java.security.Key;

/**
 * This is an example of RC4, a very insecure stream cipher, which should not be used
 */
public class HandsOnAssignment_RC4 {
    public static void main(String[] args) throws Exception {
        final Key key = KeyGenerator.getInstance("RC4").generateKey();

        final Environment env = new Environment();

        env.add(new Agent("alice") {
            @Override
            public void task() throws Exception {
                final byte[] plaintext = "Hello Bob, this is confidential message. Regards, Alice.".getBytes();

                final Cipher cipher = Cipher.getInstance("RC4");

                cipher.init(Cipher.ENCRYPT_MODE, key);

                final byte[] ciphertext = cipher.doFinal(plaintext);

                send("bob", ciphertext);
            }
        });

        env.add(new Agent("bob") {
            @Override
            public void task() throws Exception {
                final byte[] ciphertext = receive("alice");

                final Cipher decryption = Cipher.getInstance("RC4");

                decryption.init(Cipher.DECRYPT_MODE, key);

                final byte[] plaintext = decryption.doFinal(ciphertext);

                System.out.println("PT: " + new String(plaintext, StandardCharsets.UTF_8));
            }
        });

        env.connect("alice", "bob");
        env.start();
    }
}