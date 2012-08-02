package dynoptic.model.fifosys.gfsm.trace;

import org.junit.Test;

import dynoptic.DynopticTest;
import dynoptic.model.fifosys.channel.ChannelId;

public class ObservedEventTests extends DynopticTest {

    @Test
    public void create() {
        ChannelId cid = new ChannelId(1, 2, 0);

        ObservedEvent e = ObservedEvent.LocalEvent("e", 1);
        e = ObservedEvent.SendEvent("e", cid);
        e = ObservedEvent.RecvEvent("e", cid);
        logger.info(e.toString());
    }

}
