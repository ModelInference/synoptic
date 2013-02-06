import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link VectorClock}.
 * 
 * @author jennyabrahamson
 */
@RunWith(JUnit4.class)
public class VectorClockTest {

    @Test
    public void prependClock() throws IOException {
        InetAddress myAddress = InetAddress.getLocalHost();
        VectorClock clock = new VectorClock();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte[] packet = new byte[] { 'a', 'b', 'c', 'd', 'e' };

        clock.writeVectorClock(myAddress, out);
        out.write(packet);

        byte[] prependedPacket = out.toByteArray();

        InputStream in = new ByteArrayInputStream(prependedPacket);
        clock.parsePayloadAndMergeClocks(myAddress, in);

        byte[] result = new byte[packet.length];
        assertEquals(packet.length, in.read(result));
        assertEquals(Arrays.toString(packet), Arrays.toString(result));
    }
}
