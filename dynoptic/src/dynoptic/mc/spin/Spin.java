package dynoptic.mc.spin;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import dynoptic.mc.MC;
import dynoptic.mc.MCResult;

import synoptic.model.channelid.ChannelId;

public class Spin extends MC {

    static Logger logger = Logger.getLogger("Spin");

    public Spin(String mcPath) {
        super(mcPath);
    }

    @Override
    public void verify(String input, int timeoutSecs) throws IOException,
            InterruptedException {
        // TODO Auto-generated method stub

    }

    @Override
    public MCResult getVerifyResult(List<ChannelId> cids)
            throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

}
