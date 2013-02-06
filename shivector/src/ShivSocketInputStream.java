import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * InputStream to parse VectorClocks from incoming messages.
 * 
 * @author jennyabrahamson
 */
public class ShivSocketInputStream extends InputStream {

    private final InputStream in;
    private Socket socket;
    private VectorClock clock;

    public ShivSocketInputStream(InputStream in, Socket socket,
            VectorClock clock) {
        this.in = in;
        this.socket = socket;
        this.clock = clock;
    }

    @Override
    public int available() throws IOException {
        return in.available();
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        in.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return in.markSupported();
    }

    @Override
    public int read() throws IOException {
        // TODO(jennya): this is going to cause problems if the input stream is
        // read in different increments from how the output stream was written
        clock.parsePayloadAndMergeClocks(socket.getInetAddress(), in);
        return in.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        clock.parsePayloadAndMergeClocks(socket.getInetAddress(), in);
        return in.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        clock.parsePayloadAndMergeClocks(socket.getInetAddress(), in);
        return in.read(b, off, len);
    }

    @Override
    public synchronized void reset() throws IOException {
        in.reset();
    }

    @Override
    public long skip(long n) throws IOException {
        return in.skip(n);
    }
}