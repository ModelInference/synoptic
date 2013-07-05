package shivector.aspects;

import java.io.IOException;
import java.io.OutputStream;

/**
 * OutputStream to prepend VectorClocks on outgoing messages.
 * 
 * @author jennyabrahamson
 */
public class ShivSocketOutputStream extends OutputStream {

    private OutputStream out;
    private VectorClock clock;

    public ShivSocketOutputStream(OutputStream out, VectorClock clock) {
        this.out = out;
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
        this.write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        clock.writeClock(out);
        // clock.writeVectorClock(out, len);
        out.write(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
        // clock.writeVectorClock(out, 1);
        clock.writeClock(out);
        out.write(b);
    }
}
