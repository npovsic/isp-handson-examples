package isp.handson;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class HandsOnAssignment {
    public static void main(String[] args) {
        final BlockingQueue<byte[]> alice2bob = new LinkedBlockingQueue<>();
        final BlockingQueue<byte[]> bob2alice = new LinkedBlockingQueue<>();

        final Agent alice = new Agent("alice", alice2bob, bob2alice, null, null) {
            @Override
            public void execute() throws Exception {
                final String message = "Hi Bob. Kisses, Alice.";
                final byte[] bytes = message.getBytes("UTF-8");

                print("Sending: '%s' (HEX: %s)", message, hex(bytes));
                outgoing.put(bytes);
            }
        };

        final Agent bob = new Agent("bob", bob2alice, alice2bob, null, null) {
            @Override
            public void execute() throws Exception {
                final byte[] bytes = incoming.take();
                final String message = new String(bytes, "UTF-8");
                print("Received: '%s' (HEX: %s)", message, hex(bytes));
            }
        };

        // start both threads
        bob.start();
        alice.start();
    }
}
