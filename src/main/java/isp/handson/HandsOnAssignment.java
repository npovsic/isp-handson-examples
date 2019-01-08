package isp.handson;

import fri.isp.Agent;
import fri.isp.Environment;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

public class HandsOnAssignment {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        final Environment env = new Environment();

        final SecretKey key = KeyGenerator.getInstance("AES").generateKey();

        env.add(new Agent("alice") {
            @Override
            public void task() throws Exception {
                final byte[] plaintext = "Hey Bob, it's Alice".getBytes(StandardCharsets.UTF_8);

            }
        });

        env.add(new Agent("bob") {
            @Override
            public void task() throws Exception {
                final byte[] dataFromAlice = receive("alice");
            }
        });

        env.connect("alice", "bob");
        env.start();
    }
}
