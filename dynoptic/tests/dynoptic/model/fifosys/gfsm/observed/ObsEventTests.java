package dynoptic.model.fifosys.gfsm.observed;

import org.junit.Test;

import dynoptic.DynopticTest;
import dynoptic.model.fifosys.channel.channelid.ChannelId;
import dynoptic.model.fifosys.gfsm.observed.ObsEvent;

public class ObsEventTests extends DynopticTest {

    @Test
    public void create() {
        ChannelId cid = new ChannelId(1, 2, 0);

        ObsEvent e = ObsEvent.LocalEvent("e", 1);
        e = ObsEvent.SendEvent("e", cid);
        e = ObsEvent.RecvEvent("e", cid);
        logger.info(e.toString());
    }

}
