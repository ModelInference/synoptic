package dynoptic.invariants;

import org.junit.Before;

import dynoptic.DynopticTest;
import dynoptic.model.alphabet.EventType;
import dynoptic.model.fifosys.channel.channelid.ChannelId;

public abstract class InvTesting extends DynopticTest {

    EventType e0, e1, e2;
    ChannelId cid1, cid2;

    EventType fSynth1, sSynth1;
    EventType fSynth2, sSynth2;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        cid1 = new ChannelId(0, 1, 0);
        cid2 = new ChannelId(1, 0, 1);

        e0 = EventType.LocalEvent("e0", 0);
        e1 = EventType.SendEvent("e1", cid1);
        e2 = EventType.SendEvent("e2", cid2);

        ChannelId cid = new ChannelId(1, 1, 2);
        fSynth1 = EventType.SynthSendEvent(e1, cid, true);
        sSynth1 = EventType.SynthSendEvent(e2, cid, true);
        fSynth2 = EventType.SynthSendEvent(e1, cid, false);
        sSynth2 = EventType.SynthSendEvent(e2, cid, false);
    }
}
