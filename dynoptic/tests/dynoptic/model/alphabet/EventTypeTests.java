package dynoptic.model.alphabet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import dynoptic.DynopticTest;
import dynoptic.model.fifosys.channel.channelid.LocalEventsChannelId;

import synoptic.model.channelid.ChannelId;
import synoptic.model.event.DistEventType;

public class EventTypeTests extends DynopticTest {
    ChannelId cid;
    ChannelId cidCopy;
    ChannelId cid2;
    LocalEventsChannelId localChId;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        cid = new ChannelId(1, 2, 0);
        cidCopy = new ChannelId(1, 2, 0);
        cid2 = new ChannelId(2, 1, 1);
        localChId = new LocalEventsChannelId(2);
    }

    @Test
    public void createLocal() {
        DistEventType e = DistEventType.LocalEvent("e", 1);
        assertFalse(e.isCommEvent());
        assertFalse(e.isSendEvent());
        assertFalse(e.isRecvEvent());
        assertFalse(e.isSynthSendEvent());
        assertEquals(e.getEventPid(), 1);
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
        assertEquals(e.getEventPid(), cid.getSrcPid());
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
        assertEquals(e.getEventPid(), cid.getDstPid());
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
        assertEquals(e.getEventPid(), cid.getDstPid());
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
}
