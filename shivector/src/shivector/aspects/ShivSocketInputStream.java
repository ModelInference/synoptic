package shivector.aspects;

import java.io.IOException;
import java.io.InputStream;

/**
 * InputStream to parse VectorClocks from incoming messages.
 * 
 * @author jennyabrahamson
 */
public class ShivSocketInputStream extends InputStream {

    private InputStream in;
    private VectorClock clock;
    private int availableBytes;

    public ShivSocketInputStream(InputStream in, VectorClock clock) {
        this.in = in;
        this.clock = clock;
        availableBytes = 0;
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

    // @Override
    // public int read() throws IOException {
    // if (emptyBuffer()) {
    // return -1;
    // }
    // int byteRead = in.read();
    // if (byteRead != -1) {
    // availableBytes -= 1;
    // }
    //
    // return byteRead;
    // }
    //
    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    //
    // private boolean emptyBuffer() throws IOException {
    // if (availableBytes == 0) {
    // availableBytes = clock.parsePayloadAndMergeClocks(in);
    // if (availableBytes <= 0) {
    // availableBytes = 0;
    // return true;
    // }
    // }
    // return false;
    // }
    //
    // @Override
    // public int read(byte[] b, int off, int len) throws IOException {
    // if (emptyBuffer()) {
    // return -1;
    // }
    // int bytesRead = in.read(b, off, Math.min(availableBytes, len));
    // if (bytesRead >= 0) {
    // availableBytes -= bytesRead;
    // }
    // return bytesRead;
    // }

    @Override
    public int read() throws IOException {
        clock.parseClock(in);
        return in.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        clock.parseClock(in);
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