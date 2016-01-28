package isp.handson;

import javax.xml.bind.DatatypeConverter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
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
                    System.out.printf("[Alice]: Sending to Bob: %s%n", message);
                } catch (Exception e) {
                    System.err.printf("[Alice] Exception: %s%n.", e.getMessage());
                    e.printStackTrace();
                }
            }
        };

        final Agent bob = new Agent(bob2alice, alice2bob, null, null, null, null) {

            @Override
            public void run() {
                try {
                    final byte[] payload = incoming.take();
                    final String message = new String(payload, "UTF-8");
                    System.out.printf("[Bob]: I have received '%s' which in UTF-8 is '%s'%n", str(payload), message);
                } catch (Exception e) {
                    System.err.printf("[Bob] Exception: %s%n.", e.getMessage());
                    e.printStackTrace();
                }
            }
        };

        bob.start();
        alice.start();
    }

    public static String str(final byte[] data) {
        return DatatypeConverter.printHexBinary(data);
    }
}
