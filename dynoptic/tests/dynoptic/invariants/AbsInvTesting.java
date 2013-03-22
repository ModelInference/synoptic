package dynoptic.invariants;

import org.junit.Before;

import dynoptic.DynopticTest;

import synoptic.model.channelid.ChannelId;
import synoptic.model.event.DistEventType;

public abstract class AbsInvTesting extends DynopticTest {

    DistEventType e0, e1, e2;
    ChannelId cid1, cid2;

    DistEventType fSynth1, sSynth1;
    DistEventType fSynth2, sSynth2;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        cid1 = new ChannelId(0, 1, 0);
        cid2 = new ChannelId(1, 0, 1);

        e0 = DistEventType.LocalEvent("e0", 0);
        e1 = DistEventType.SendEvent("e1", cid1);
        e2 = DistEventType.SendEvent("e2", cid2);

        ChannelId cid = new ChannelId(1, 1, 2);
        fSynth1 = DistEventType.SynthSendEvent(e1, cid, true);
        sSynth1 = DistEventType.SynthSendEvent(e2, cid, true);
        fSynth2 = DistEventType.SynthSendEvent(e1, cid, false);
        sSynth2 = DistEventType.SynthSendEvent(e2, cid, false);
    }
}
