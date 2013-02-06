import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Container for a vector clock mapping with methods to parse/prepend clocks
 * from input/output streams.
 * 
 * @author jennyabrahamson
 */
public class VectorClock {

    private Map<InetAddress, Integer> clock;
    public static final int INT_LENGTH = 4;

    public VectorClock() {
        this.clock = new HashMap<InetAddress, Integer>();
    }

    @Override
    public String toString() {
        return clock.toString();
    }

    /**
     * Parses a vector clock from the input stream, merges with this vector
     * clock, increments the recorded time for the given node.
     */
    public void parsePayloadAndMergeClocks(InetAddress node, InputStream in)
            throws IOException {

        byte[] clockMarker = new byte[INT_LENGTH];
        int bytesRead = in.read(clockMarker);
        if (bytesRead != INT_LENGTH) {
            System.err
                    .println("Error reading vector clock marker, incorrect length. Expecting "
                            + INT_LENGTH + " but read " + bytesRead);
        }
        int clockSize = byteArrayToInt(clockMarker);

        byte[] clockArray = new byte[clockSize];
        bytesRead = in.read(clockArray);
        if (bytesRead != clockSize) {
            System.err
                    .println("Error reading vector clock, incorrect length. Expecting "
                            + INT_LENGTH + " but read " + bytesRead);
        }

        // Parse byte array to Map
        ByteArrayInputStream byteIn = new ByteArrayInputStream(clockArray);
        ObjectInputStream objIn = new ObjectInputStream(byteIn);

        try {
            @SuppressWarnings("unchecked")
            Map<InetAddress, Integer> otherClock = (Map<InetAddress, Integer>) objIn
                    .readObject();
            mergeClocks(otherClock);
        } catch (ClassNotFoundException e) {
            // Whelp we probably can't merge the clocks
            System.out.println(e);
        }

        increment(node);
    }

    /**
     * Increments the recorded time for the given node and writes this vector
     * clock to the output stream.
     */
    public void writeVectorClock(InetAddress node, OutputStream out)
            throws IOException {
        increment(node);

        // Convert Map to byte array
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
        objOut.writeObject(clock);

        byte[] mapArray = byteOut.toByteArray();
        byte[] mapLength = intToByteArray(mapArray.length);

        out.write(mapLength);
        out.write(mapArray);
    }

    /* Merges the given clock into our clock, taking the max time for each node */
    private void mergeClocks(Map<InetAddress, Integer> otherClock) {
        for (InetAddress otherTime : otherClock.keySet()) {
            if (clock.containsKey(otherTime)) {
                clock.put(
                        otherTime,
                        Math.max(clock.get(otherTime),
                                otherClock.get(otherTime)));
            } else {
                clock.put(otherTime, otherClock.get(otherTime));
            }
        }
    }

    /* Increments the given node's time in our vector clock. */
    private void increment(InetAddress node) {
        if (clock.get(node) == null) {
            clock.put(node, 0);
        }
        clock.put(node, clock.get(node) + 1);
    }

    /* Converts and returns the given int to a byte array */
    private byte[] intToByteArray(int value) {
        return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16),
                (byte) (value >>> 8), (byte) value };
    }

    /* Converts and returns the first 4 bytes of a byte array to an int */
    private int byteArrayToInt(byte[] b) {
        int value = 0;
        for (int i = 0; i < INT_LENGTH; i++)
            value = (value << 8) | b[i];
        return value;
    }
}
