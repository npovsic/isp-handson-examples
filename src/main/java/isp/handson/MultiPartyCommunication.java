package isp.handson;

import java.security.NoSuchAlgorithmException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This example demonstrates a multi-party communication, that is a communication between
 * more than two agents.
 * <p>
 * Since the Agent class was initially designed to offer communication between two agents only,
 * we have to slightly adapt our code.
 * <p>
 * To communicate between 3 parties, we need 6 unidirectional communication channels (queues)
 * that are defined globally inside the main method. Then, we have to use then these queues directly
 * for sending or receiving messages. When instantiating agents, simply pass in null as the incoming
 * and outgoing queue.
 * <p>
 * Again, we have to be careful not to use the incoming or outgoing member variables inside the agent
 * or we'll encounter a NullPointerException: for sending and receiving, we have to use globally defined
 * queues directly.
 * <p>
 * See example below.
 */
public class MultiPartyCommunication {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        // alice-bob
        final BlockingQueue<byte[]> bob2alice = new LinkedBlockingQueue<>();
        final BlockingQueue<byte[]> alice2bob = new LinkedBlockingQueue<>();

        // alice-charlie
        final BlockingQueue<byte[]> charlie2alice = new LinkedBlockingQueue<>();
        final BlockingQueue<byte[]> alice2charlie = new LinkedBlockingQueue<>();

        // bob-charlie
        final BlockingQueue<byte[]> charlie2bob = new LinkedBlockingQueue<>();
        final BlockingQueue<byte[]> bob2charlie = new LinkedBlockingQueue<>();

        final Agent alice = new Agent("alice", null, null, null, null) {
            @Override
            public void execute() throws Exception {
                alice2bob.put("From Alice".getBytes());
                alice2charlie.put("From Alice".getBytes());
                print("Got %s", new String(charlie2alice.take()));
                print("Got %s", new String(bob2alice.take()));
            }
        };

        final Agent bob = new Agent("bob", null, null, null, null) {
            @Override
            public void execute() throws Exception {
                bob2charlie.put("From Bob".getBytes());
                bob2alice.put("From Bob".getBytes());
                print("Got %s", new String(alice2bob.take()));
                print("Got %s", new String(charlie2bob.take()));
            }
        };

        final Agent charlie = new Agent("charlie", null, null, null, null) {
            @Override
            public void execute() throws Exception {
                charlie2alice.put("From Charlie".getBytes());
                charlie2bob.put("From Charlie".getBytes());
                print("Got %s", new String(alice2charlie.take()));
                print("Got %s", new String(bob2charlie.take()));

                try {
                    // this will throw a NPE
                    incoming.take();
                } catch (NullPointerException e) {
                    print("ERROR: Encountered a NPE: %s (this is expected)", e.getMessage());
                }
            }
        };

        alice.start();
        bob.start();
        charlie.start();
    }
}
