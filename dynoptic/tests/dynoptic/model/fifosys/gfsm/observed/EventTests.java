package dynoptic.model.fifosys.gfsm.observed;

import org.junit.Test;

import dynoptic.DynopticTest;

import synoptic.model.channelid.ChannelId;
import synoptic.model.event.DistEventType;
import synoptic.model.event.Event;

public class EventTests extends DynopticTest {

    @Test
    public void create() {
        ChannelId cid = new ChannelId(1, 2, 0);

        Event e = new Event(DistEventType.LocalEvent("e", 1));
        e = new Event(DistEventType.SendEvent("e", cid));
        e = new Event(DistEventType.RecvEvent("e", cid));
        logger.info(e.toString());
    }

}
