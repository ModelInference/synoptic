package dynoptic.invariants;

import java.util.Arrays;

import org.junit.Before;

import dynoptic.DynopticTest;
import dynoptic.model.alphabet.EventType;
import dynoptic.model.alphabet.FSMAlphabet;
import dynoptic.model.fifosys.channel.ChannelId;

public abstract class InvTesting extends DynopticTest {

    EventType e0, e1, e2;
    ChannelId cid1, cid2;
    FSMAlphabet alphabet;

    EventType fSynth, sSynth;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        cid1 = new ChannelId(0, 1, 0);
        cid2 = new ChannelId(1, 0, 1);

        e0 = EventType.LocalEvent("e0", 0);
        e1 = EventType.SendEvent("e1", cid1);
        e2 = EventType.SendEvent("e2", cid2);

        alphabet = new FSMAlphabet();
        alphabet.addAll(Arrays.asList(new EventType[] { e0, e1, e2 }));

        ChannelId cid = new ChannelId(1, 1, 2);
        fSynth = EventType.SynthSendEvent(e1, cid);
        sSynth = EventType.SynthSendEvent(e2, cid);

        alphabet.add(fSynth);
        alphabet.add(sSynth);
    }

}
