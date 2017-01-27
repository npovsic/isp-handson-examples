package isp.handson;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class HandsOnRetake {
    public static void main(String[] args) throws Exception {

        final BlockingQueue<byte[]> alice2bob = new LinkedBlockingQueue<>();

        final Agent alice = new Agent("alice", null, null, null, null) {
            @Override
            public void execute() throws Exception {
                alice2bob.put("Hi Bob, it's Alice!".getBytes());
            }
        };

        final Agent bob = new Agent("bob", null, null, null, null) {
            @Override
            public void execute() throws Exception {
                final byte[] pt = alice2bob.take();
                print("Got %s", new String(pt));
            }
        };

        alice.start();
        bob.start();
    }
}
