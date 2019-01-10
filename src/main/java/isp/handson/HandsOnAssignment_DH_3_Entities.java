package isp.handson;

import fri.isp.Agent;
import fri.isp.Environment;

import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.X509EncodedKeySpec;

/**
 * ("PK": A = g^a, "SK": a)
 */
public class HandsOnAssignment_DH_3_Entities {
    public static void main(String[] args) {
        final Environment env = new Environment();

        env.add(new Agent("alice") {
            @Override
            public void task() throws Exception {
                /*
                    1. Alice creates her DH pair with a key size of 2048
                 */

                final KeyPairGenerator kpg = KeyPairGenerator.getInstance("DH");

                kpg.initialize(2048);

                final KeyPair keyPair = kpg.generateKeyPair();

                /*
                    2. Alice sends her encoded public key to Bob
                 */

                send("bob", keyPair.getPublic().getEncoded());

                /*
                    3. Alice initializes the key agreement
                 */

                KeyAgreement keyAgreement = KeyAgreement.getInstance("DH");

                keyAgreement.init(keyPair.getPrivate());

                /*
                    4. Alice receives Carol's encoded public key and creates a public key object
                 */

                final byte[] carolEncodedPublicKey = receive("carol");

                final X509EncodedKeySpec keySpecCarol = new X509EncodedKeySpec(carolEncodedPublicKey);
                final DHPublicKey carolPublicKey = (DHPublicKey) KeyFactory.getInstance("DH").generatePublic(keySpecCarol);

                /*
                    5. Alice uses Carol's key and sends the result to Bob
                 */

                Key aliceAndCarolKey = keyAgreement.doPhase(carolPublicKey, false);

                send("bob", aliceAndCarolKey.getEncoded());

                /*
                    6. Alice uses Carol's computation for hers and Bob's key
                 */

                final byte[] carolAndBobEncodedKey = receive("carol");

                final X509EncodedKeySpec keySpecCarolAndBob = new X509EncodedKeySpec(carolAndBobEncodedKey);
                final DHPublicKey carolAndBobKey = (DHPublicKey) KeyFactory.getInstance("DH").generatePublic(keySpecCarolAndBob);

                keyAgreement.doPhase(carolAndBobKey, true);

                /*
                    7. Alice computes the shared secret
                 */

                byte[] aliceSharedSecret = keyAgreement.generateSecret();

                print("Shared secret: " + Agent.hex(aliceSharedSecret));
            }
        });

        env.add(new Agent("bob") {
            @Override
            public void task() throws Exception {
                /*
                    1. Bob creates his DH pair with a key size of 2048
                 */

                final KeyPairGenerator kpg = KeyPairGenerator.getInstance("DH");

                kpg.initialize(2048);

                final KeyPair keyPair = kpg.generateKeyPair();

                /*
                    2. Bob sends his encoded public key to Carol
                 */

                send("carol", keyPair.getPublic().getEncoded());

                /*
                    3. Bob initializes the key agreement
                 */

                KeyAgreement keyAgreement = KeyAgreement.getInstance("DH");

                keyAgreement.init(keyPair.getPrivate());

                /*
                    4. Bob receives Alice's encoded public key and creates a public key object
                 */

                final byte[] aliceEncodedPublicKey = receive("alice");

                final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(aliceEncodedPublicKey);
                final DHPublicKey alicePublicKey = (DHPublicKey) KeyFactory.getInstance("DH").generatePublic(keySpec);

                /*
                    5. Bob uses Alice's key and sends the result to Carol
                 */

                Key bobAndAliceKey = keyAgreement.doPhase(alicePublicKey, false);

                send("carol", bobAndAliceKey.getEncoded());

                /*
                    6. Bob uses Alice's computation for hers and Carol's key
                 */

                final byte[] carolAndAliceEncodedKey = receive("alice");

                final X509EncodedKeySpec keySpecCarolAndBob = new X509EncodedKeySpec(carolAndAliceEncodedKey);
                final DHPublicKey carolAndAliceKey = (DHPublicKey) KeyFactory.getInstance("DH").generatePublic(keySpecCarolAndBob);

                keyAgreement.doPhase(carolAndAliceKey, true);

                /*
                    7. Bob computes the shared secret
                 */

                byte[] aliceSharedSecret = keyAgreement.generateSecret();

                print("Shared secret: " + Agent.hex(aliceSharedSecret));
            }
        });

        env.add(new Agent("carol") {
            @Override
            public void task() throws Exception {
                /*
                    1. Carol creates her DH pair with a key size of 2048
                 */

                final KeyPairGenerator kpg = KeyPairGenerator.getInstance("DH");

                kpg.initialize(2048);

                final KeyPair keyPair = kpg.generateKeyPair();

                /*
                    2. Carol sends her encoded public key to Alice
                 */

                send("alice", keyPair.getPublic().getEncoded());

                /*
                    3. Carol initializes the key agreement
                 */

                KeyAgreement keyAgreement = KeyAgreement.getInstance("DH");

                keyAgreement.init(keyPair.getPrivate());

                /*
                    4. Carol receives Bob's encoded public key and creates a public key object
                 */

                final byte[] bobEncodedPublicKey = receive("bob");

                final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bobEncodedPublicKey);
                final DHPublicKey bobPublicKey = (DHPublicKey) KeyFactory.getInstance("DH").generatePublic(keySpec);

                /*
                    5. Carol uses Bob's key and sends the result to Alice
                 */

                Key carolAndBobKey = keyAgreement.doPhase(bobPublicKey, false);

                send("alice", carolAndBobKey.getEncoded());

                /*
                    6. Carol uses Bob's computation for his and Alice's key
                 */

                final byte[] aliceAndBobEncodedKey = receive("bob");

                final X509EncodedKeySpec keySpecAliceAndBob = new X509EncodedKeySpec(aliceAndBobEncodedKey);
                final DHPublicKey aliceAndBobKey = (DHPublicKey) KeyFactory.getInstance("DH").generatePublic(keySpecAliceAndBob);

                keyAgreement.doPhase(aliceAndBobKey, true);

                /*
                    7. Carol computes the shared secret
                 */

                byte[] aliceSharedSecret = keyAgreement.generateSecret();

                print("Shared secret: " + Agent.hex(aliceSharedSecret));
            }
        });

        env.connect("alice", "bob");
        env.connect("bob", "carol");
        env.connect("carol", "alice");

        env.start();
    }


}
