package dynoptic.model.alphabet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import dynoptic.DynopticTest;
import dynoptic.util.Util;

import synoptic.model.channelid.ChannelId;
import synoptic.model.event.DistEventType;

public class DistEventTypeTests extends DynopticTest {
    ChannelId cid;
    ChannelId cidCopy;
    ChannelId cid2;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        cid = new ChannelId(1, 2, 0, "A");
        cidCopy = new ChannelId(1, 2, 0, "A");
        cid2 = new ChannelId(2, 1, 1, "B");
    }

    @Test
    public void createLocal() {
        DistEventType e = DistEventType.LocalEvent("e", 1);
        assertFalse(e.isCommEvent());
        assertFalse(e.isSendEvent());
        assertFalse(e.isRecvEvent());
        assertFalse(e.isSynthSendEvent());
        assertEquals(e.getPid(), 1);
        logger.info(e.toString());

        assertFalse(e.equals(null));
        assertFalse(e.equals(""));
        assertTrue(e.equals(e));

        // equality:
        DistEventType e2 = DistEventType.LocalEvent("e", 1);
        assertEquals(e, e2);

        e2 = DistEventType.LocalEvent("e", 2);
        assertTrue(!e.equals(e2));

        e2 = DistEventType.LocalEvent("Z", 1);
        assertTrue(!e.equals(e2));

        e2 = DistEventType.SendEvent("e", cid);
        assertTrue(!e.equals(e2));

        e2 = DistEventType.RecvEvent("e", cid);
        assertTrue(!e.equals(e2));
    }

    @Test
    public void createSend() {
        DistEventType e = DistEventType.SendEvent("e", cid);
        assertTrue(e.isCommEvent());
        assertTrue(e.isSendEvent());
        assertFalse(e.isRecvEvent());
        assertFalse(e.isSynthSendEvent());
        assertEquals(e.getPid(), cid.getSrcPid());
        logger.info(e.toString());

        assertFalse(e.equals(null));
        assertFalse(e.equals(""));
        assertTrue(e.equals(e));

        // equality:
        DistEventType e2 = DistEventType.SendEvent("e", cidCopy);
        assertEquals(e, e2);

        e2 = DistEventType.SendEvent("Z", cid);
        assertTrue(!e.equals(e2));

        e2 = DistEventType.SendEvent("e", cid2);
        assertTrue(!e.equals(e2));

        e2 = DistEventType.RecvEvent("e", cid);
        assertTrue(!e.equals(e2));

        e2 = DistEventType.LocalEvent("e", 1);
        assertTrue(!e.equals(e2));
    }

    @Test
    public void createRecv() {
        DistEventType e = DistEventType.RecvEvent("e", cid);
        assertTrue(e.isCommEvent());
        assertFalse(e.isSendEvent());
        assertTrue(e.isRecvEvent());
        assertFalse(e.isSynthSendEvent());
        assertEquals(e.getPid(), cid.getDstPid());
        logger.info(e.toString());

        assertFalse(e.equals(null));
        assertFalse(e.equals(""));
        assertTrue(e.equals(e));

        // equality:
        DistEventType e2 = DistEventType.RecvEvent("e", cidCopy);
        assertEquals(e, e2);

        e2 = DistEventType.RecvEvent("Z", cid);
        assertTrue(!e.equals(e2));

        e2 = DistEventType.RecvEvent("e", cid2);
        assertTrue(!e.equals(e2));

        e2 = DistEventType.SendEvent("e", cid);
        assertTrue(!e.equals(e2));

        e2 = DistEventType.LocalEvent("e", 1);
        assertTrue(!e.equals(e2));
    }

    @Test
    public void createSynthSend() {
        DistEventType eToTrace = DistEventType.RecvEvent("e", cid);

        DistEventType e = DistEventType.SynthSendEvent(eToTrace, cid, true);

        assertTrue(e.isCommEvent());
        assertFalse(e.isSendEvent());
        assertFalse(e.isRecvEvent());
        assertTrue(e.isSynthSendEvent());
        assertEquals(e.getPid(), cid.getDstPid());
        logger.info(e.toString());

        assertFalse(e.equals(null));
        assertFalse(e.equals(""));
        assertTrue(e.equals(e));

        // equality:
        DistEventType e2 = DistEventType.SynthSendEvent(eToTrace, cid, true);
        assertEquals(e, e2);

        e2 = DistEventType.SynthSendEvent(eToTrace, cid, false);
        assertTrue(!e.equals(e2));

        e2 = DistEventType.RecvEvent("e", cid);
        assertTrue(!e.equals(e2));

        e2 = DistEventType.SendEvent("e", cid2);
        assertTrue(!e.equals(e2));

        e2 = DistEventType.LocalEvent("e", 1);
        assertTrue(!e.equals(e2));
    }

    @Test
    public void checkEventStrParsingSend() {
        DistEventType e = new DistEventType("A!m", "sender");
        List<ChannelId> cids = Util.newList();
        cids.add(cid);
        cids.add(cid2);
        // Should not return an error.
        assertEquals(e.interpretEType(cids), null);

        // Check the interpreted e instance.
        assertEquals(e.getChannelId(), cid);
        assertTrue(e.isCommEvent());
        assertTrue(e.isSendEvent());
        assertFalse(e.isRecvEvent());
        assertFalse(e.isLocalEvent());
        assertEquals(e.getPid(), 1);
        assertEquals(e.getEType(), "m");
    }

    @Test
    public void checkEventStrParsingRecv() {
        DistEventType e = new DistEventType("A?m", "receiver");
        List<ChannelId> cids = Util.newList();
        cids.add(cid);
        cids.add(cid2);
        // Should not return an error.
        assertEquals(e.interpretEType(cids), null);

        // Check the interpreted e instance.
        assertEquals(e.getChannelId(), cid);
        assertTrue(e.isCommEvent());
        assertFalse(e.isSendEvent());
        assertTrue(e.isRecvEvent());
        assertFalse(e.isLocalEvent());
        assertEquals(e.getPid(), 2);
        assertEquals(e.getEType(), "m");
    }

    @Test
    public void checkEventStrParsingLocal() {
        DistEventType e = new DistEventType("e", "1");
        List<ChannelId> cids = Util.newList();
        cids.add(cid);
        cids.add(cid2);
        // Should not return an error.
        assertEquals(e.interpretEType(cids), null);

        // Check the interpreted e instance.
        assertEquals(e.getChannelId(), null);
        assertFalse(e.isCommEvent());
        assertFalse(e.isSendEvent());
        assertFalse(e.isRecvEvent());
        assertTrue(e.isLocalEvent());
        assertEquals(e.getPid(), 1);
        assertEquals(e.getEType(), "e");
    }

    @Test
    public void checkEventStrParsingErr1() {
        DistEventType e = new DistEventType("A?m", "receiver");
        List<ChannelId> cids = Util.newList();
        // Leave out cid on purpose.
        // cids.add(cid);
        cids.add(cid2);
        // Should return an _error_ -- no 'A' channel specified.
        assertFalse(e.interpretEType(cids).equals(null));
    }

    @Test
    public void checkEventStrParsingErr2() {
        DistEventType e = new DistEventType("A??m", "receiver");
        List<ChannelId> cids = Util.newList();
        cids.add(cid);
        cids.add(cid2);
        // Should return an _error_ -- problem parsing ??.
        assertFalse(e.interpretEType(cids).equals(null));
    }
}
