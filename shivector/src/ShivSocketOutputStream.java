import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * OutputStream to prepend VectorClocks on outgoing messages.
 * 
 * @author jennyabrahamson
 */
public class ShivSocketOutputStream extends OutputStream {

    private final OutputStream out;
    private Socket socket;
    private VectorClock clock;

    public ShivSocketOutputStream(OutputStream out, Socket socket,
            VectorClock clock) {
        this.out = out;
        this.socket = socket;
        this.clock = clock;
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void write(byte[] b) throws IOException {
        clock.writeVectorClock(socket.getInetAddress(), out);
        out.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        clock.writeVectorClock(socket.getInetAddress(), out);
        out.write(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
        clock.writeVectorClock(socket.getInetAddress(), out);
        out.write(b);
    }
}
