package tests.units;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import aspects.ShivSocketInputStream;
import aspects.VectorClock;

/**
 * Tests for {@link VectorClock}.
 * 
 * @author jennyabrahamson
 */
@RunWith(JUnit4.class)
public class VectorClockTests {

    @Test
    public void prependClock() throws IOException {
        VectorClock clock = new VectorClock("MyId!!!");

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte[] packet = new byte[] { 'a', 'b', 'c', 'd', 'e' };

        clock.writeVectorClock(out, packet.length);
        out.write(packet);

        byte[] prependedPacket = out.toByteArray();

        InputStream in = new ByteArrayInputStream(prependedPacket);
        clock.parsePayloadAndMergeClocks(in);

        byte[] result = new byte[packet.length];
        assertEquals(packet.length, in.read(result));
        assertEquals(Arrays.toString(packet), Arrays.toString(result));
    }

    @Test
    public void testMultipleWrites() throws IOException {
        VectorClock clock = new VectorClock("MyId!!!");

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte[] packet = new byte[] { 'a', 'b', 'c', 'd', 'e' };

        clock.writeVectorClock(out, packet.length);
        out.write(packet);
        clock.writeVectorClock(out, packet.length);
        out.write(packet);

        byte[] stream = out.toByteArray();

        InputStream in = new ByteArrayInputStream(stream);
        ShivSocketInputStream shivIn = new ShivSocketInputStream(in, clock);
        byte[] parsedPacket = new byte[10];
        assertEquals(packet.length, shivIn.read(parsedPacket));
        assertEquals(packet.length, shivIn.read(parsedPacket));
        assertEquals(-1, shivIn.read(parsedPacket));
    }
}
