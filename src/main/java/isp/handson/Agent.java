package isp.handson;

import java.security.Key;
import java.util.concurrent.BlockingQueue;

public abstract class Agent extends Thread {
    protected final BlockingQueue<byte[]> outgoing, incoming;

    protected final Key macKey, cryptoKey;
    protected final String cryptoAlgorithm, macAlgorithm;

    public Agent(final BlockingQueue<byte[]> outgoing, final BlockingQueue<byte[]> incoming, final Key cryptoKey,
            final String cryptoAlgorithm, final Key macKey, final String macAlgorithm) {
        this.outgoing = outgoing;
        this.incoming = incoming;
        this.cryptoKey = cryptoKey;
        this.cryptoAlgorithm = cryptoAlgorithm;
        this.macKey = macKey;
        this.macAlgorithm = macAlgorithm;
    }
}
