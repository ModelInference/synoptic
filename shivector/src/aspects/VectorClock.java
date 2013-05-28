package aspects;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

/**
 * Container for a vector clock mapping with methods to parse/prepend clocks
 * from input/output streams.
 * 
 * @author jennyabrahamson
 */
public class VectorClock {
    public static final int INT_LENGTH = 4;

    private Map<String, Integer> clock;
    private String node;

    public VectorClock(String node) {
        this.node = node;
        this.clock = new HashMap<String, Integer>();
        this.clock.put(node, 0);
    }

    @Override
    public String toString() {
        return clock.toString();
    }

    /*
     * Attempts to read exactly b.length bytes into b. If all goes well, returns
     * b.length. If less than b.length bytes are available, assumes an error has
     * occurred, returns -1, and no assumptions should be made about the
     * contents of b
     */
    private int read(byte[] b, InputStream in) throws IOException {
        int totalLen = b.length;
        int offset = 0;
        while (offset < totalLen) {
            int bytesRead = in.read(b, offset, totalLen - offset);
            if (bytesRead < 0) {
                return -1;
            }
            offset += bytesRead;
        }
        return offset;
    }

    /**
     * Parses a vector clock from the input stream, merges with this vector
     * clock, increments the recorded time for the given node, and returns the
     * length of the next payload.
     */
    public int parsePayloadAndMergeClocks(InputStream in) throws IOException {

        byte[] clockMarker = new byte[INT_LENGTH];
        int bytesRead = read(clockMarker, in);
        if (bytesRead <= 0) {
            return -1;
        }
        int clockSize = byteArrayToInt(clockMarker);
        byte[] clockArray = new byte[clockSize];
        bytesRead = read(clockArray, in);

        // Parse byte array to Map
        ByteArrayInputStream byteIn = new ByteArrayInputStream(clockArray);
        ObjectInputStream objIn = new ObjectInputStream(byteIn);

        try {
            Map<String, Integer> otherClock = (Map<String, Integer>) objIn
                    .readObject();
            mergeClocks(otherClock);
        } catch (ClassNotFoundException e) {
            // Whelp we probably can't merge the clocks
            System.out.println(e);
        }

        // Increment this node's clock.
        clock.put(node, clock.get(node) + 1);

        byte[] payloadLength = new byte[INT_LENGTH];
        bytesRead = read(payloadLength, in);

        return byteArrayToInt(payloadLength);
    }

    /**
     * Increments the recorded time for the given node and writes this vector
     * clock to the output stream.
     */
    public void writeVectorClock(OutputStream out, int payloadSize)
            throws IOException {
        // Increment this node's clock
        clock.put(node, clock.get(node) + 1);

        // Convert Map to byte array
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
        objOut.writeObject(clock);

        byte[] mapArray = byteOut.toByteArray();
        byte[] mapLength = intToByteArray(mapArray.length);
        byte[] payloadLength = intToByteArray(payloadSize);

        out.write(mapLength);
        out.write(mapArray);
        out.write(payloadLength);
    }

    /* Merges the given clock into our clock, taking the max time for each node */
    private void mergeClocks(Map<String, Integer> otherClock) {
        for (String nodeId : otherClock.keySet()) {
            if (clock.containsKey(nodeId)) {
                clock.put(nodeId,
                        Math.max(clock.get(nodeId), otherClock.get(nodeId)));
            } else {
                clock.put(nodeId, otherClock.get(nodeId));
            }
        }
    }

    /* Converts and returns the given int to a byte array */
    private byte[] intToByteArray(int value) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
                .putInt(value).array();
    }

    /* Converts and returns the first 4 bytes of a byte array to an int */
    private int byteArrayToInt(byte[] b) {
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }
}
