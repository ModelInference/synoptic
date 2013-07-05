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
import java.util.concurrent.atomic.AtomicInteger;

import shivector.options.ShiVectorOptions;

/**
 * Container for a vector clock mapping with methods to parse/prepend clocks
 * from input/output streams.
 * 
 * @author jennyabrahamson
 */
public class VectorClock {
    public static final int INT_LENGTH = 4;
    private Map<String, Map<String, AtomicInteger>> masterClock;
    private String processId;
    private ShiVectorOptions options;

    private String hostId() {
        if (options.useThreadsAsHosts) {
            return this.processId + Thread.currentThread();
        }
        return this.processId;
    }

    public VectorClock(String processId, ShiVectorOptions options) {
        this.options = options;
        this.processId = processId;
        masterClock = new HashMap<String, Map<String, AtomicInteger>>();
    }

    @Override
    public String toString() {
        return this.hostId() + " " + json();
    }

    private String json() {
        StringBuilder sb = new StringBuilder("{");
        for (Map.Entry<String, AtomicInteger> entry : this.masterClock.get(
                hostId()).entrySet()) {
            sb.append("\"" + entry.getKey() + "\":" + entry.getValue() + ", ");
        }
        int comma = sb.lastIndexOf(",");
        sb.replace(comma, comma + 1, "}");
        return sb.toString();
    }

    private void initializeClock() {
        synchronized (masterClock) {
            if (!masterClock.containsKey(hostId())) {
                masterClock.put(hostId(), new HashMap<String, AtomicInteger>());
            }
            Map<String, AtomicInteger> clock = masterClock.get(hostId());
            if (!clock.containsKey(hostId())) {
                clock.put(hostId(), new AtomicInteger());
            }
        }
    }

    public synchronized void incrementClock() {
        initializeClock();
        // Increment this node's clock
        Map<String, AtomicInteger> clock = masterClock.get(hostId());
        clock.get(hostId()).incrementAndGet();
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

    /* Merges the given clock into our clock, taking the max time for each node */
    private synchronized void mergeClocks(Map<String, AtomicInteger> otherClock) {
        initializeClock();
        Map<String, AtomicInteger> clock = masterClock.get(hostId());
        for (String nodeId : otherClock.keySet()) {
            if (clock.containsKey(nodeId)) {
                int val = Math.max(clock.get(nodeId).get(),
                        otherClock.get(nodeId).get());
                clock.get(nodeId).set(val);
            } else {
                clock.put(nodeId, otherClock.get(nodeId));
            }
        }
    }

    // Socket read
    public void parseClock(InputStream in) throws IOException {
        byte[] buf = new byte[INT_LENGTH];
        in.read(buf);
        int mapLength = byteArrayToInt(buf);
        byte[] map = new byte[mapLength];
        in.read(map);

        // Parse byte array to Map
        readClock(new ByteArrayInputStream(map));
    }

    // Nio read
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

    // Mina read
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
            return objIn.readObject();
        } catch (ClassNotFoundException e) {
            // Error reading message
            System.out.println(e);
            return null;
        }
    }

    private void readClock(ByteArrayInputStream byteIn) throws IOException {
        ObjectInputStream objIn = new ObjectInputStream(byteIn);
        try {
            Map<String, AtomicInteger> otherClock = (Map<String, AtomicInteger>) objIn
                    .readObject();
            mergeClocks(otherClock);
        } catch (ClassNotFoundException e) {
            // Whelp we probably can't merge the clocks
            System.out.println(e);
        }
    }

    /** Write protocol: map length, map array, msg length, msg */
    // Socket write
    public void writeClock(OutputStream out) throws IOException {
        initializeClock();

        // Convert Map to byte array
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
        objOut.writeObject(masterClock.get(hostId()));

        byte[] mapArray = byteOut.toByteArray();
        byte[] mapLength = intToByteArray(mapArray.length);

        out.write(mapLength);
        out.write(mapArray);
    }

    // Nio write
    public void writeClock(SocketChannel s) throws IOException {
        initializeClock();

        // Convert Map to byte array
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
        objOut.writeObject(masterClock.get(hostId()));

        byte[] mapArray = byteOut.toByteArray();
        byte[] mapLength = intToByteArray(mapArray.length);
        ByteBuffer buf = ByteBuffer.allocate(INT_LENGTH);
        buf.put(mapLength);
        buf.rewind();
        s.write(buf);

        buf = ByteBuffer.allocate(mapArray.length);
        buf.put(mapArray);
        buf.rewind();
        s.write(buf);
    }

    // Mina write
    public byte[] getMessageArray(Object message) throws IOException {
        initializeClock();
        // Convert Map to byte array
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
        objOut.writeObject(masterClock.get(hostId()));

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

    /******************* Old logic that tracked available bytes *******************/

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
     * clock, and returns the length of the next payload.
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

    /**
     * Writes this vector clock to the output stream.
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
}
