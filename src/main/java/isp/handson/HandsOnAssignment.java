package isp.handson;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class HandsOnAssignment {
    public static void main(String[] args) throws Exception {

        final BlockingQueue<byte[]> alice2bob = new LinkedBlockingQueue<>();
        final BlockingQueue<byte[]> bob2alice = new LinkedBlockingQueue<>();

        final Agent alice = new Agent("alice", alice2bob, bob2alice, null, null) {
            @Override
            public void execute() throws Exception {
                outgoing.put("Hi Bob, it's Alice!".getBytes());
            }
        };

        final Agent bob = new Agent("bob", bob2alice, alice2bob, null, null) {
            @Override
            public void execute() throws Exception {
                final byte[] pt = incoming.take();
                print("Got '%s'", new String(pt));
            }
        };

        alice.start();
        bob.start();
    }
}
