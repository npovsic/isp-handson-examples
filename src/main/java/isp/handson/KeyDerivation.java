package isp.handson;

import fri.isp.Agent;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class KeyDerivation {
    public static void createKey(byte[] sharedSecret) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // password from which the key will be derived
        final String password = "password";

        // supposed to be random
        final byte[] salt = "89fjh3409fdj390fk".getBytes(StandardCharsets.UTF_8);

        final SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

        // provide the password, salt, number of iterations and the number of required bits
        final KeySpec specs = new PBEKeySpec(password.toCharArray(), salt, 1000000, 128);

        final SecretKey key = secretKeyFactory.generateSecret(specs);

        System.out.printf("key = %s%n", Agent.hex(key.getEncoded()));
        System.out.printf("len(key) = %d bytes", key.getEncoded().length);
    }
}
