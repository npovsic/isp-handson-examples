package isp.handson;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HandsOnAssignment {
    private final static Logger LOG = Logger.getLogger(HandsOnAssignment.class.getCanonicalName());

    public static void main(String[] args) {

        final BlockingQueue<byte[]> alice2bob = new LinkedBlockingQueue<>();
        final BlockingQueue<byte[]> bob2alice = new LinkedBlockingQueue<>();

        final Agent alice = new Agent(alice2bob, bob2alice, null, null, null, null) {
            @Override
            public void run() {
                try {
                    final String message = "I love you Bob. Kisses, Alice.";
                    outgoing.put(message.getBytes("UTF-8"));
                    LOG.info("[Alice]: Sending to Bob: " + message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        final Agent bob = new Agent(bob2alice, alice2bob, null, null, null, null) {

            @Override
            public void run() {
                try {
                    final String message = new String(incoming.take(), "UTF-8");
                    LOG.log(Level.INFO, "[Bob]: I have received: " + message);
                } catch (Exception ex) {
                }
            }
        };

        bob.start();
        alice.start();
    }
}
