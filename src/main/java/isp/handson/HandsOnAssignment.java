package isp.handson;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class HandsOnAssignment {
    public static void main(String[] args) throws Exception {

        final BlockingQueue<byte[]> alice2server = new LinkedBlockingQueue<>();
        final BlockingQueue<byte[]> alice2lock = new LinkedBlockingQueue<>();
        final BlockingQueue<byte[]> server2lock = new LinkedBlockingQueue<>();

        final Agent alice = new Agent("alice", null, null, null, null) {
            @Override
            public void execute() throws Exception {
                alice2server.put("from Alice".getBytes());
                alice2lock.put("from Alice".getBytes());
            }
        };

        final Agent server = new Agent("server", null, null, null, null) {
            @Override
            public void execute() throws Exception {
                server2lock.put("from Server".getBytes());
                final byte[] msg = alice2server.take();
                print("Got %s", new String(msg));
            }
        };

        final Agent lock = new Agent("lock", null, null, null, null) {
            @Override
            public void execute() throws Exception {
                final byte[] msg1 = alice2lock.take();
                print("Got %s", new String(msg1));

                final byte[] msg2 = server2lock.take();
                print("Got %s", new String(msg2));
            }
        };

        alice.start();
        server.start();
        lock.start();
    }
}
