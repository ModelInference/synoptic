package shivector.aspects;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
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

    private Map<String, Map<String, Integer>> masterClock;
    // private Map<String, Integer> clock;
    private String node;

    public VectorClock(String node) {
        this.node = node;
        masterClock = new HashMap<String, Map<String, Integer>>();
        // this.clock = new HashMap<String, Integer>();
        // this.clock.put(node, 0);

        // Now that we're no longer doing IDs by thread, intialize here
        masterClock.put(node, new HashMap<String, Integer>());
        Map<String, Integer> clock = masterClock.get(node);
        clock.put(node, 0);
    }

    @Override
    public String toString() {
        // return this.node + Thread.currentThread() + " " + json();
        return this.node + " " + json();
    }

    private String json() {
        // String id = node + Thread.currentThread();
        String id = node;
        StringBuilder sb = new StringBuilder("{");
        for (Map.Entry<String, Integer> entry : this.masterClock.get(id)
                .entrySet()) {
            // for (Map.Entry<String, Integer> entry : this.clock.entrySet()) {
            sb.append("\"" + entry.getKey() + "\":" + entry.getValue() + ", ");
        }
        int comma = sb.lastIndexOf(",");
        sb.replace(comma, comma + 1, "}");
        return sb.toString();
    }

    /*
     * Attempts to read exactly b.length bytes into b. If all goes well, returns
     * b.length. If less than b.length bytes are available, assumes an error has
     * occurred, returns -1, and no assumptions should be made about the
     * contents of b
     */
    // private int read(byte[] b, InputStream in) throws IOException {
    // int totalLen = b.length;
    // int offset = 0;
    // while (offset < totalLen) {
    // int bytesRead = in.read(b, offset, totalLen - offset);
    // if (bytesRead < 0) {
    // return -1;
    // }
    // offset += bytesRead;
    // }
    // return offset;
    // }

    /**
     * Parses a vector clock from the input stream, merges with this vector
     * clock, increments the recorded time for the given node, and returns the
     * length of the next payload.
     */
    // public int parsePayloadAndMergeClocks(InputStream in) throws IOException
    // {
    // byte[] clockMarker = new byte[INT_LENGTH];
    // int bytesRead = read(clockMarker, in);
    // if (bytesRead <= 0) {
    // return -1;
    // }
    // int clockSize = byteArrayToInt(clockMarker);
    // byte[] clockArray = new byte[clockSize];
    // bytesRead = read(clockArray, in);
    //
    // // Parse byte array to Map
    // readClock(new ByteArrayInputStream(clockArray));
    //
    // byte[] payloadLength = new byte[INT_LENGTH];
    // bytesRead = read(payloadLength, in);
    //
    // return byteArrayToInt(payloadLength);
    // }

    public void parseClock(InputStream in) throws IOException {
        byte[] buf = new byte[INT_LENGTH];
        in.read(buf);
        int mapLength = byteArrayToInt(buf);
        byte[] map = new byte[mapLength];
        in.read(map);

        // Parse byte array to Map
        readClock(new ByteArrayInputStream(map));

    }

    public void parseClock(SocketChannel s) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(INT_LENGTH);
        int bytesRead = s.read(buf);
        if (bytesRead == 0) {
            return;
        }
        buf.rewind();
        byte[] mapLengthArray = new byte[INT_LENGTH];
        buf.get(mapLengthArray);
        int mapLength = byteArrayToInt(mapLengthArray);
        buf = ByteBuffer.allocate(mapLength);
        bytesRead = s.read(buf);
        buf.rewind();
        byte[] clockArray = new byte[mapLength];
        buf.get(clockArray);

        // Parse byte array to Map
        readClock(new ByteArrayInputStream(clockArray));
    }

    private void readClock(ByteArrayInputStream byteIn) throws IOException {
        ObjectInputStream objIn = new ObjectInputStream(byteIn);
        try {
            Map<String, Integer> otherClock = (Map<String, Integer>) objIn
                    .readObject();
            mergeClocks(otherClock);
        } catch (ClassNotFoundException e) {
            // Whelp we probably can't merge the clocks
            System.out.println(e);
        }
    }

    public void writeClock(OutputStream out) throws IOException {
        // initializeClock();
        // String id = node + Thread.currentThread();
        String id = node;
        // Convert Map to byte array
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
        objOut.writeObject(masterClock.get(id));

        byte[] mapArray = byteOut.toByteArray();
        byte[] mapLength = intToByteArray(mapArray.length);

        out.write(mapLength);
        out.write(mapArray);
    }

    public byte[] getMessageArray(Object message) throws IOException {
        // initializeClock();
        // String id = node + Thread.currentThread();
        String id = node;

        // Convert Map to byte array
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
        objOut.writeObject(masterClock.get(id));

        byte[] mapArray = byteOut.toByteArray();
        byte[] mapLength = intToByteArray(mapArray.length);

        ByteArrayOutputStream messageOut = new ByteArrayOutputStream();
        ObjectOutputStream objOutMsg = new ObjectOutputStream(messageOut);
        objOutMsg.writeObject(message);

        byte[] msgArray = messageOut.toByteArray();
        byte[] msgLength = intToByteArray(msgArray.length);

        byte[] out = new byte[mapLength.length + mapArray.length
                + msgLength.length + msgArray.length];
        ByteBuffer target = ByteBuffer.wrap(out);
        target.put(mapLength);
        target.put(mapArray);
        target.put(msgLength);
        target.put(msgArray);

        return out;
    }

    public Object parseMessageArray(byte[] msg) throws IOException {
        ByteBuffer source = ByteBuffer.wrap(msg);
        byte[] mapLength = new byte[INT_LENGTH];
        source.get(mapLength);
        byte[] map = new byte[byteArrayToInt(mapLength)];
        source.get(map);
        readClock(new ByteArrayInputStream(map));

        byte[] msgLength = new byte[INT_LENGTH];
        source.get(msgLength);
        byte[] msgArr = new byte[byteArrayToInt(msgLength)];
        source.get(msgArr);

        ObjectInputStream objIn = new ObjectInputStream(
                new ByteArrayInputStream(msgArr));
        try {
            Object message = (Object) objIn.readObject();
            return message;
        } catch (ClassNotFoundException e) {
            // Whelp we probably can't merge the clocks
            System.out.println(e);
            return null;
        }
    }

    public void writeClock(SocketChannel s) throws IOException {
        // initializeClock();
        // String id = node + Thread.currentThread();
        String id = node;
        // Convert Map to byte array
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
        objOut.writeObject(masterClock.get(id));

        byte[] mapArray = byteOut.toByteArray();
        byte[] mapLength = intToByteArray(mapArray.length);
        ByteBuffer buf = ByteBuffer.allocate(INT_LENGTH);
        // buf.putInt(mapArray.length);
        buf.put(mapLength);
        buf.rewind();
        s.write(buf);

        buf = ByteBuffer.allocate(mapArray.length);
        buf.put(mapArray);
        buf.rewind();
        s.write(buf);
    }

    /**
     * Increments the recorded time for the given node and writes this vector
     * clock to the output stream.
     */
    // public void writeVectorClock(OutputStream out, int payloadSize)
    // throws IOException {
    //
    // String id = node + Thread.currentThread();
    //
    // // Convert Map to byte array
    // ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    // ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
    // objOut.writeObject(masterClock.get(id));
    //
    // byte[] mapArray = byteOut.toByteArray();
    // byte[] mapLength = intToByteArray(mapArray.length);
    // byte[] payloadLength = intToByteArray(payloadSize);
    //
    // out.write(mapLength);
    // out.write(mapArray);
    // out.write(payloadLength);
    // }

    /* Merges the given clock into our clock, taking the max time for each node */
    private void mergeClocks(Map<String, Integer> otherClock) {
        // initializeClock();
        // String id = node + Thread.current();
        String id = node;
        Map<String, Integer> clock = masterClock.get(id);
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

    // private void initializeClock() {
    // synchronized (masterClock) {
    // // String id = node + Thread.currentThread();
    // String id = node;
    // if (!masterClock.containsKey(id)) {
    // masterClock.put(node, new HashMap<String, Integer>());
    // }
    // Map<String, Integer> clock = masterClock.get(id);
    // if (!clock.containsKey(id)) {
    // clock.put(id, 0);
    // }
    // }
    // }

    public synchronized void incrementClock() {
        // initializeClock();

        // Increment this node's clock
        // String id = node + Thread.currentThread();
        String id = node;
        Map<String, Integer> clock = masterClock.get(id);
        clock.put(id, clock.get(id) + 1);

    }
}
